package com.chenxun.rpc.netty.test;

import com.chenxun.rpc.netty.service.RpcServer;

public class TestServer {

	public static void main(String[] args) {
		RpcServer server=new RpcServer("127.0.0.1", 9999);

	}

}
