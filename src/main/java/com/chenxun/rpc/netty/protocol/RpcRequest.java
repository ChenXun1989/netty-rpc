package com.chenxun.rpc.netty.protocol;

import lombok.Data;

/**
 * 封装 RPC 请求
 */
@Data
public class RpcRequest {

	private String requestId;
	private String className;
	private String methodName;
	private Class<?>[] parameterTypes;
	private Object[] parameters;


}