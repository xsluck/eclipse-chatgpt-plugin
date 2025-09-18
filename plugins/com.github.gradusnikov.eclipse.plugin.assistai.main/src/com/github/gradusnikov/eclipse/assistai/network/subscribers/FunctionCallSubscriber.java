package com.github.gradusnikov.eclipse.assistai.network.subscribers;

import java.util.List;
import java.util.concurrent.Flow;
import java.util.concurrent.Flow.Subscription;

import org.eclipse.core.runtime.ILog;
import org.eclipse.e4.core.di.annotations.Creatable;

import com.github.gradusnikov.eclipse.assistai.chat.FunctionCall;
import com.github.gradusnikov.eclipse.assistai.chat.Incoming;
import com.github.gradusnikov.eclipse.assistai.jobs.ExecuteFunctionCallJob;
import com.github.gradusnikov.eclipse.assistai.tools.FunctionCallParser;
import com.github.gradusnikov.eclipse.assistai.view.ChatViewPresenter;
import com.github.gradusnikov.eclipse.assistai.view.PartAccessor;

import jakarta.inject.Inject;
import jakarta.inject.Provider;

@Creatable
public class FunctionCallSubscriber implements Flow.Subscriber<Incoming> {
	@Inject
	private ILog logger;
	@Inject
	private Provider<ExecuteFunctionCallJob> executeFunctionCallJobProvider;


	@Inject
	private ChatViewPresenter presenter;
	private Subscription subscription;
	private final StringBuffer jsonBuffer;

	public FunctionCallSubscriber() {
		jsonBuffer = new StringBuffer();
	}

	@Override
	public void onSubscribe(Subscription subscription) {
		this.subscription = subscription;
		jsonBuffer.setLength(0);
		subscription.request(1);
	}

	@Override
	public void onNext(Incoming item) {
		if (Incoming.Type.FUNCTION_CALL == item.type()) {
			jsonBuffer.append(item.payload());
		}
		subscription.request(1);
	}

	@Override
	public void onError(Throwable throwable) {
		jsonBuffer.setLength(0);
	}

	@Override
	public void onComplete() {
		String json = jsonBuffer.toString();
		System.out.println("SSSSSSSSSSSSS:" + json);
		if (!json.startsWith("function_call")) {
			subscription.request(1);
			return;
		}
		try {
//			if (json.endsWith(":")) {
//				json += "{}";
//			}
//			json += "}";
			String substring = json.substring(13);
			substring = "[" + substring + "]";
			List<FunctionCall> tools = FunctionCallParser.parseFunctionCalls(substring);
			if (!tools.isEmpty()) {
				ExecuteFunctionCallJob job = executeFunctionCallJobProvider.get();
				job.setFunctionCalls(tools);
				job.setSystem(true);
				job.setPresenter(presenter);
				job.schedule();
				logger.info("Scheduled batch job with " + tools.size() + " function calls");
			}

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		subscription.request(1);
	}

}
