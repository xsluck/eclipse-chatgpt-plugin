package com.github.gradusnikov.eclipse.assistai.network.subscribers;

import java.util.Objects;
import java.util.concurrent.Flow;
import java.util.concurrent.Flow.Subscription;

import org.eclipse.core.runtime.ILog;
import org.eclipse.e4.core.di.annotations.Creatable;

import com.github.gradusnikov.eclipse.assistai.chat.ChatMessage;
import com.github.gradusnikov.eclipse.assistai.chat.Incoming;
import com.github.gradusnikov.eclipse.assistai.chat.Incoming.Type;
import com.github.gradusnikov.eclipse.assistai.view.ChatViewPresenter;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Creatable
@Singleton
public class AppendMessageToViewSubscriber implements Flow.Subscriber<Incoming> {
	@Inject
	private ILog logger;

	private Flow.Subscription subscription;

	private ChatViewPresenter presenter;

	private ChatMessage currentMessage; // 普通文本 / 工具返回
	private ChatMessage currentFunctionCallMessage; // 工具调用

	private Type lastType;

	public AppendMessageToViewSubscriber() {
	}

	public void setPresenter(ChatViewPresenter presenter) {
		this.presenter = presenter;
	}

	@Override
	public void onSubscribe(Subscription subscription) {
		Objects.requireNonNull(presenter);
		this.subscription = subscription;
		this.lastType = null;
		this.currentMessage = null;
		this.currentFunctionCallMessage = null;
		subscription.request(1);
	}

	@Override
	public void onNext(Incoming item) {
		Objects.requireNonNull(presenter);
		Objects.requireNonNull(subscription);

		// 当类型切换时结束上一条对应消息
		if (item.type() != lastType) {
			endCurrentMessages();
			lastType = item.type();
		}

		switch (item.type()) {
		case CONTENT -> handleContentMessage(item.payload());
		}

		subscription.request(1);
	}

	private void handleContentMessage(Object payload) {
		if (Objects.isNull(currentMessage)) {
			currentMessage = presenter.beginMessageFromAssistant();
		}
		if (Objects.nonNull(currentMessage)) {
			currentMessage.append(payload.toString());
			presenter.updateMessageFromAssistant(currentMessage);
		}
	}

	private void endCurrentMessages() {
		if (Objects.nonNull(currentMessage)) {
			presenter.endMessageFromAssistant(currentMessage);
			currentMessage = null;
		}
		if (Objects.nonNull(currentFunctionCallMessage)) {
			presenter.endMessageFromAssistant(currentFunctionCallMessage);
			currentFunctionCallMessage = null;
		}
	}

	@Override
	public void onError(Throwable throwable) {
		logger.error(throwable.getMessage(), throwable);
	}

	@Override
	public void onComplete() {
		endCurrentMessages();
		subscription = null;
	}
}
