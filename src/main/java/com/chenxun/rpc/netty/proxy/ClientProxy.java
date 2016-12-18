package com.chenxun.rpc.netty.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;

import com.chenxun.rpc.netty.client.RPCFuture;
import com.chenxun.rpc.netty.client.RpcClient;
import com.chenxun.rpc.netty.protocol.RpcRequest;
import com.chenxun.rpc.netty.protocol.RpcResponse;

public class ClientProxy {
	
	
	public <T> T proxy(Class<T> t){
		if(!t.isInterface()){
			throw new RuntimeException("只支持接口代理");
		}
		
		return (T) Proxy.newProxyInstance(t.getClassLoader(), new Class[]{t}, new ClientInvocationHandler());
		
	}
	
	private  class ClientInvocationHandler implements InvocationHandler{

		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			final RpcRequest request = new RpcRequest();
			request.setRequestId(UUID.randomUUID().toString());
			request.setClassName(method.getDeclaringClass().getName());
			request.setMethodName(method.getName());
			request.setParameterTypes(method.getParameterTypes());
			request.setParameters(args);
			
			// 发送请求
			RPCFuture future =RpcClient.newInstance().getChannel().sendRequest(request);
			RpcResponse response=(RpcResponse) future.get();
			if(response.getError()!=null){
				throw response.getError();
			}
			return response.getResult();
		}
		
	}

}
