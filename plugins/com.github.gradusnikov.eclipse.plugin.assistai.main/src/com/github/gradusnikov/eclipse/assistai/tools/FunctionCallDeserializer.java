package com.github.gradusnikov.eclipse.assistai.tools;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.github.gradusnikov.eclipse.assistai.chat.FunctionCall;

public class FunctionCallDeserializer extends StdDeserializer<FunctionCall> {

    public FunctionCallDeserializer() {
        super(FunctionCall.class);
    }

    @Override
    public FunctionCall deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        ObjectMapper mapper = (ObjectMapper) p.getCodec();
        JsonNode node = mapper.readTree(p);

        String id = null;
        String name = null;
        Map<String, Object> arguments = Collections.emptyMap();

        // ==== 新接口 (tool_calls) ====
        if (node.has("function")) {
            id = node.has("id") ? node.get("id").asText() : null;
            JsonNode fnNode = node.get("function");
            if (fnNode != null) {
                if (fnNode.has("name")) {
                    name = fnNode.get("name").asText();
                }
                if (fnNode.has("arguments")) {
                    String argsStr = fnNode.get("arguments").asText("{}"); // arguments 是字符串
                    arguments = mapper.readValue(argsStr, Map.class);
                }
            }
        }
        // ==== 旧接口 (function_call) ====
        else {
            if (node.has("id")) {
                id = node.get("id").asText();
            }
            if (node.has("name")) {
                name = node.get("name").asText();
            }
            if (node.has("arguments")) {
                if (node.get("arguments").isTextual()) {
                    arguments = mapper.readValue(node.get("arguments").asText("{}"), Map.class);
                } else {
                    arguments = mapper.convertValue(node.get("arguments"), Map.class);
                }
            }
        }

        return new FunctionCall(id, name, arguments);
    }
}
