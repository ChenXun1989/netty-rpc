package com.chenxun.rpc.netty.protocol;

import lombok.Data;

/**
 * 封装 RPC 响应
 */
@Data
public class RpcResponse {

	private String requestId;
	private RuntimeException error;
	private Object result;




}
