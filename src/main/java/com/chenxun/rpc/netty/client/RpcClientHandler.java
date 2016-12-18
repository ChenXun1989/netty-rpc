package com.chenxun.rpc.netty.client;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.chenxun.rpc.netty.protocol.RpcRequest;
import com.chenxun.rpc.netty.protocol.RpcResponse;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class RpcClientHandler extends ChannelInboundHandlerAdapter {

	private ExecutorService threadPool = Executors.newSingleThreadExecutor();

	private RpcResponse response;

	private Channel channel;

	private ConcurrentHashMap<String , RPCFuture> futureMap=new ConcurrentHashMap<String,RPCFuture>();
	
	@Override
	public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
		// TODO Auto-generated method stub
		super.channelRegistered(ctx);
		this.channel=ctx.channel();
	}
	
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		this.response=(RpcResponse)msg;
		String requestId = response.getRequestId();
		RPCFuture rpcFuture = futureMap.get(requestId);
		if (rpcFuture != null) {
			futureMap.remove(requestId);
			rpcFuture.done(response);
		}
	}
	

	public RPCFuture sendRequest(RpcRequest request) {
		RPCFuture rpcFuture = new RPCFuture(request);
		futureMap.put(request.getRequestId(), rpcFuture);
		channel.writeAndFlush(request);

		return rpcFuture;
	}

}
