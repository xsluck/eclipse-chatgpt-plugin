package com.github.gradusnikov.eclipse.assistai.jobs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.e4.core.di.annotations.Creatable;

import com.github.gradusnikov.eclipse.assistai.chat.Attachment;
import com.github.gradusnikov.eclipse.assistai.chat.ChatMessage;
import com.github.gradusnikov.eclipse.assistai.chat.Conversation;
import com.github.gradusnikov.eclipse.assistai.chat.FunctionCall;
import com.github.gradusnikov.eclipse.assistai.chat.FunctionResp;
import com.github.gradusnikov.eclipse.assistai.mcp.McpClientRetistry;
import com.github.gradusnikov.eclipse.assistai.tools.FunctionCallParser;
import com.github.gradusnikov.eclipse.assistai.tools.JsonUtils;
import com.github.gradusnikov.eclipse.assistai.view.ChatViewPresenter;

import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import jakarta.inject.Inject;
import jakarta.inject.Provider;

@Creatable
public class ExecuteFunctionCallJob extends Job {
	private static final String JOB_NAME = AssistAIJobConstants.JOB_PREFIX + " execute function calls";
	private static final String CLIENT_TOOL_SEPARATOR = "__";

	/** 每个工具调用的超时时间（毫秒） */
	private static final long TOOL_TIMEOUT_MS = 30_000L;

	/** 并发线程池最大并发数（可按需调整） */
	private static final int MAX_CONCURRENT_THREADS = 5;

	@Inject
	private ILog logger;

	private ChatViewPresenter presenter;
	@Inject
	private Provider<SendConversationJob> sendConversationJobProvider;

	private HashMap<String, ChatMessage> currentFunctionRespMessage;
	@Inject
	private Conversation conversation;
	@Inject
	private McpClientRetistry mcpClientRetistry;

	private List<FunctionCall> functionCalls = Collections.emptyList();

	public ExecuteFunctionCallJob() {
		super(JOB_NAME);
		currentFunctionRespMessage = new HashMap<String, ChatMessage>();
		super.setRule(new AssistAIJobRule());
	}

	public void setPresenter(ChatViewPresenter presenter) {
		this.presenter = presenter;
	}

	public void setFunctionCalls(List<FunctionCall> functionCalls) {
		this.functionCalls = Objects.requireNonNull(functionCalls, "Function calls cannot be null");
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		if (functionCalls == null || functionCalls.isEmpty()) {
			return Status.error("No function calls to execute");
		}

		logger.info("Executing " + functionCalls.size() + " function calls in parallel...");

		final int poolSize = Math.min(functionCalls.size(), MAX_CONCURRENT_THREADS);
		ExecutorService executor = Executors.newFixedThreadPool(poolSize);
		ScheduledExecutorService canceller = new ScheduledThreadPoolExecutor(1);
		ExecutorCompletionService<CallToolResult> completionService = new ExecutorCompletionService<>(executor);

		// 保存 Future -> FunctionCall 映射，便于从完成的 Future 找到对应的 FunctionCall
		Map<Future<CallToolResult>, FunctionCall> futureToCall = new ConcurrentHashMap<>();

		try {
			// 提交所有任务
			for (FunctionCall fc : functionCalls) {
				Callable<CallToolResult> task = () -> {
					return callToolWithCatch(fc);
				};
				Future<CallToolResult> future = completionService.submit(task);
				futureToCall.put(future, fc);

				// 在 TOOL_TIMEOUT_MS 后尝试取消该 future（中断线程）
				canceller.schedule(() -> {
					if (!future.isDone()) {
						future.cancel(true);
						logger.info("Cancelled future (timeout requested) for: " + fc.name());
					}
				}, TOOL_TIMEOUT_MS, TimeUnit.MILLISECONDS);
			}

			// 等待并处理每个完成的任务（无论成功/失败/取消）
			int remaining = functionCalls.size();
			while (remaining > 0) {
				Future<CallToolResult> completedFuture;
				try {
					completedFuture = completionService.take(); // 阻塞直到任一任务完成或被取消
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					logger.error("Job interrupted while waiting for task completion", e);
					break;
				}

				FunctionCall finishedCall = futureToCall.remove(completedFuture);
				if (finishedCall == null) {
					// 不太可能发生，但防御性处理
					remaining--;
					continue;
				}

				try {
					if (completedFuture.isCancelled()) {
						// 视为超时/被取消
						logger.info("Function call cancelled/timed out: " + finishedCall.name());
						List<McpSchema.Content> errorContent = List
								.of(new McpSchema.TextContent("Timeout after " + TOOL_TIMEOUT_MS + " ms"));
						CallToolResult timeoutResult = new CallToolResult(errorContent, true);

						handleFunctionResult(finishedCall, timeoutResult);
					} else {
						// get() 不会阻塞太久，因为这是已经完成的 future
						CallToolResult result = completedFuture.get();
						if (result == null) {
							// 防御性：若返回 null，构造错误结果
							List<McpSchema.Content> nullContext = List
									.of(new McpSchema.TextContent("Tool returned null result"));
							CallToolResult nullResult = new CallToolResult(nullContext, true);

							handleFunctionResult(finishedCall, nullResult);
						} else {
							handleFunctionResult(finishedCall, result);
						}
					}
				} catch (CancellationException ce) {
					logger.info("Function call cancelled (CancellationException): " + finishedCall.name());

					List<McpSchema.Content> timeContext = List
							.of(new McpSchema.TextContent("Timeout after " + TOOL_TIMEOUT_MS + " ms"));
					CallToolResult timeoutResult = new CallToolResult(timeContext, true);
					handleFunctionResult(finishedCall, timeoutResult);
				} catch (ExecutionException ee) {
					Throwable cause = ee.getCause() == null ? ee : ee.getCause();
					logger.error("ExecutionException for function call: " + finishedCall.name(), cause);
					List<McpSchema.Content> errorContext = List
							.of(new McpSchema.TextContent("Execution error: " + cause.getMessage()));
					CallToolResult errorResult = new CallToolResult(errorContext, true);
					handleFunctionResult(finishedCall, errorResult);
				} catch (InterruptedException ie) {
					Thread.currentThread().interrupt();
					logger.error("Interrupted while retrieving result for: " + finishedCall.name(), ie);

					List<McpSchema.Content> errorContext = List
							.of(new McpSchema.TextContent("Interrupted while waiting for result"));
					CallToolResult errorResult = new CallToolResult(errorContext, true);
					handleFunctionResult(finishedCall, errorResult);
				} finally {
					remaining--;
				}
			}

		} finally {
			// 尝试优雅关闭调度器和线程池
			try {
				canceller.shutdownNow();
			} catch (Exception ex) {
				logger.error("Error shutting down canceller", ex);
			}
			try {
				executor.shutdownNow();
			} catch (Exception ex) {
				logger.error("Error shutting down executor", ex);
			}
		}

		return Status.OK_STATUS;
	}

	/**
	 * 真正执行工具调用的逻辑（封装异常为 CallToolResult 使用 builder）
	 */
	private CallToolResult callToolWithCatch(FunctionCall functionCall) {
		handleFunctionCall(functionCall);
		String clientToolName = functionCall.name();
		int separatorIndex = clientToolName.indexOf(CLIENT_TOOL_SEPARATOR);

		if (separatorIndex == -1) {
			List<McpSchema.Content> errorContext = List
					.of(new McpSchema.TextContent("Invalid function call format: " + clientToolName));
			return new CallToolResult(errorContext, true);
		}

		String clientName = clientToolName.substring(0, separatorIndex);
		String toolName = clientToolName.substring(separatorIndex + CLIENT_TOOL_SEPARATOR.length());

		var clientOpt = mcpClientRetistry.findClient(clientName);
		if (clientOpt.isEmpty()) {
			List<McpSchema.Content> errorContext = List
					.of(new McpSchema.TextContent("Tool not found: " + clientName + ":" + toolName));
			return new CallToolResult(errorContext, true);
		}

		try {
			CallToolRequest request = new CallToolRequest(toolName, functionCall.arguments());
			// 这里假设 callTool 是阻塞调用并返回 CallToolResult（你的现有代码亦是如此）
			CallToolResult result = clientOpt.get().callTool(request);
			return result;
		} catch (Throwable t) {
			logger.error("Tool execution exception for " + clientToolName + ": " + t.getMessage(), t);

			List<McpSchema.Content> errorContext = List
					.of(new McpSchema.TextContent("Execution error: " + t.getMessage()));
			return new CallToolResult(errorContext, true);
		}
	}

	private void handleFunctionResult(FunctionCall functionCall, CallToolResult result) {
		try {
			logger.info("Finished function call " + functionCall.name() + " -> "
					+ (Boolean.TRUE.equals(result.isError()) ? "error" : "success"));

			// 1. assistant 消息（表明调用了 function）
			ChatMessage assistantMessage = new ChatMessage(UUID.randomUUID().toString(), "assistant");
			assistantMessage.setFunctionCall(functionCall);
			conversation.add(assistantMessage);

			// 2. result 消息（返回给 AI）
			ChatMessage resultMessage = createFunctionResultMessage(functionCall, result);
			conversation.add(resultMessage);
			// 3. 发回 LLM
			scheduleConversationSending();

		} catch (Exception e) {
			logger.error("Error handling function result: " + e.getMessage(), e);
		}
	}

	private ChatMessage createFunctionResultMessage(FunctionCall functionCall, CallToolResult result) throws Exception {
		ChatMessage resultMessage = new ChatMessage(UUID.randomUUID().toString(), functionCall.name(), "function");

		StringBuilder textContent = new StringBuilder();
		List<Attachment> attachments = new ArrayList<>();

		boolean isError = Boolean.TRUE.equals(result.isError());
		if (isError) {
			textContent.append("Error: ");
		}

		// 兼容原来代码风格：使用 result.content()，并将文本类型拼接到 content 字段
		var contentParts = Optional.ofNullable(result.content()).orElse(Collections.emptyList());
		for (McpSchema.Content content : contentParts) {
			// 目前只处理 text content
			try {
				// 你的项目里，TextContent 可能有 text() 方法或 getText()，这里使用 cast + text()
				if ("text".equals(content.type())) {
					textContent.append(((McpSchema.TextContent) content).text()).append("\n");
				} else {
					logger.error("Unsupported result content type: " + content.type());
				}
			} catch (NoSuchMethodError | ClassCastException ex) {
				// 若生成类方法签名与预期不一致，可以在此处扩展兼容性处理
				logger.error("Error reading content part: " + ex.getMessage(), ex);
			}
		}
		handleFunctionResp(new FunctionResp(functionCall.id(), textContent.toString()));
		resultMessage.setAttachments(attachments);
		resultMessage.setContent(textContent.toString());
		resultMessage.setFunctionCall(functionCall);

		return resultMessage;
	}

	private void handleFunctionCall(FunctionCall functionCall) {
		System.out.println("工具显示：" + functionCall.id());
		ChatMessage message = presenter.beginFunctionCallMessage();
		message.setContent("function_call " + JsonUtils.toJsonString(functionCall));
		presenter.updateMessageFromAssistant(message);
		currentFunctionRespMessage.put(functionCall.id(), message);
	}

	private void handleFunctionResp(FunctionResp functionResp) {
		System.out.println("工具响应:" + functionResp.id());
		if (currentFunctionRespMessage != null) {
			ChatMessage chatMessage = currentFunctionRespMessage.get(functionResp.id());
			if (Objects.nonNull(chatMessage)) {
				chatMessage.setContent(chatMessage.getContent() + "\nresp:" + functionResp.resp());
				presenter.updateMessageFromAssistant(chatMessage);
			}
		}
	}

	private void scheduleConversationSending() {
		SendConversationJob job = sendConversationJobProvider.get();
		job.setSystem(true);
		job.schedule();
	}

}
