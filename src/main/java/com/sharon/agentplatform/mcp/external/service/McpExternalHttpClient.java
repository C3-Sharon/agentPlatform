package com.sharon.agentplatform.mcp.external.service;

import com.sharon.agentplatform.common.exception.BusinessException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
public class McpExternalHttpClient {

    private final RestTemplate restTemplate = new RestTemplate();

    public Map<String, Object> listTools(String baseUrl) {
        return post(baseUrl, Map.of(
                "jsonrpc", "2.0",
                "id", 1,
                "method", "tools/list"
        ));
    }

    public Map<String, Object> callTool(String baseUrl, String toolName, Map<String, Object> arguments) {
        return post(baseUrl, Map.of(
                "jsonrpc", "2.0",
                "id", 1,
                "method", "tools/call",
                "params", Map.of(
                        "name", toolName,
                        "arguments", arguments == null ? Map.of() : arguments
                )
        ));
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> post(String baseUrl, Map<String, Object> body) {
        try {
            Map<String, Object> response = restTemplate.postForObject(baseUrl, body, Map.class);
            if (response == null) {
                throw new BusinessException("External MCP server returned empty response");
            }
            Object error = response.get("error");
            if (error instanceof Map<?, ?> errorMap) {
                Object message = errorMap.get("message");
                throw new BusinessException("External MCP error: " + (message == null ? errorMap : message));
            }
            return response;
        } catch (BusinessException exception) {
            throw exception;
        } catch (RestClientException exception) {
            throw new BusinessException("External MCP HTTP call failed: " + exception.getMessage(), exception);
        }
    }
}
