package com.chenxun.rpc.netty.test;

import com.chenxun.rpc.netty.client.RpcClient;
import com.chenxun.rpc.netty.demo.HelloService;
import com.chenxun.rpc.netty.proxy.ClientProxy;

public class TestClient {

	public static void main(String[] args) {
       RpcClient client=RpcClient.newInstance();
		
		ClientProxy proxy=new ClientProxy();
		HelloService hello=proxy.proxy(HelloService.class);
		String str=hello.say();
		System.out.println(str);

	}

}
