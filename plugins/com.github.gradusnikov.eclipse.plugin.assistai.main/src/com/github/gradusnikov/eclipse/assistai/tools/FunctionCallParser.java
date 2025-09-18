package com.github.gradusnikov.eclipse.assistai.tools;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.gradusnikov.eclipse.assistai.chat.FunctionCall;

public class FunctionCallParser {

	private static final ObjectMapper mapper = new ObjectMapper();

	/**
	 * 将 JSON 字符串解析为 FunctionCall 列表
	 */
	public static List<FunctionCall> parseFunctionCalls(String json) {
		try {
			// 首先将 JSON 解析为 Map 列表
			List<Map<String, Object>> rawList = mapper.readValue(json, new TypeReference<List<Map<String, Object>>>() {
			});

			// 转换为 FunctionCall 列表
			List<FunctionCall> functionCalls = new ArrayList<>();
			for (Map<String, Object> rawMap : rawList) {
				FunctionCall functionCall = mapToFunctionCall(rawMap);
				functionCalls.add(functionCall);
			}

			return functionCalls;

		} catch (Exception e) {
			throw new RuntimeException("解析 JSON 失败: " + e.getMessage(), e);
		}
	}

	/**
	 * 将单个 Map 转换为 FunctionCall
	 */
	private static FunctionCall mapToFunctionCall(Map<String, Object> map) {
		String name = (String) map.get("name");
		String id = (String) map.get("id");

		@SuppressWarnings("unchecked")
		Map<String, Object> arguments = (Map<String, Object>) map.get("arguments");

		return new FunctionCall(id, name, arguments);
	}

	/**
	 * 将 FunctionCall 列表转换回 JSON 字符串
	 */
	public static String toJson(List<FunctionCall> functionCalls) {
		try {
			return mapper.writeValueAsString(functionCalls);
		} catch (Exception e) {
			throw new RuntimeException("转换为 JSON 失败: " + e.getMessage(), e);
		}
	}
}