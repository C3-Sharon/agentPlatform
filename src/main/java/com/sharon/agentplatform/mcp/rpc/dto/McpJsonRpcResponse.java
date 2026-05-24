package com.sharon.agentplatform.mcp.rpc.dto;

public class McpJsonRpcResponse {

    private String jsonrpc;
    private Object id;
    private Object result;
    private McpJsonRpcError error;

    public static McpJsonRpcResponse success(Object id, Object result) {
        McpJsonRpcResponse response = new McpJsonRpcResponse();
        response.setJsonrpc("2.0");
        response.setId(id);
        response.setResult(result);
        response.setError(null);
        return response;
    }

    public static McpJsonRpcResponse error(Object id, int code, String message) {
        McpJsonRpcError error = new McpJsonRpcError();
        error.setCode(code);
        error.setMessage(message);

        McpJsonRpcResponse response = new McpJsonRpcResponse();
        response.setJsonrpc("2.0");
        response.setId(id);
        response.setResult(null);
        response.setError(error);
        return response;
    }

    public String getJsonrpc() {
        return jsonrpc;
    }

    public void setJsonrpc(String jsonrpc) {
        this.jsonrpc = jsonrpc;
    }

    public Object getId() {
        return id;
    }

    public void setId(Object id) {
        this.id = id;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public McpJsonRpcError getError() {
        return error;
    }

    public void setError(McpJsonRpcError error) {
        this.error = error;
    }
}
