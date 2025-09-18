package com.github.gradusnikov.eclipse.assistai.chat;

import java.util.Map;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.github.gradusnikov.eclipse.assistai.tools.FunctionCallDeserializer;

@JsonDeserialize(using = FunctionCallDeserializer.class)
public record FunctionCall(String id, String name, Map<String, Object> arguments) {}
