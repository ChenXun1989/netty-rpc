package com.chenxun.rpc.netty.service;

import java.lang.reflect.Method;

import org.omg.CORBA.Request;

import com.chenxun.rpc.netty.protocol.RpcRequest;
import com.chenxun.rpc.netty.protocol.RpcResponse;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class RpcServerHandler extends ChannelInboundHandlerAdapter {

	
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
			RpcRequest request=(RpcRequest)msg;			
			RpcResponse response = new RpcResponse();
			response.setRequestId(request.getRequestId());
			try {
				Object result = handle(request);
				response.setResult(result);
			} catch (Throwable t) {
				response.setError(new RuntimeException(t));
			}
			ctx.writeAndFlush(response);
		
	}


	private Object handle(RpcRequest request) throws Throwable {
		String className = request.getClassName();
		Class<?> serviceClass = Class.forName(request.getClassName()+"Impl");
		String methodName = request.getMethodName();
		Class<?>[] parameterTypes = request.getParameterTypes();
		Object[] parameters = request.getParameters();
		Method method = serviceClass.getMethod(methodName, parameterTypes);
		method.setAccessible(true);
		return method.invoke(serviceClass.newInstance(), parameters);
	}

}
