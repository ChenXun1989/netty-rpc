package com.chenxun.rpc.netty.service;

import com.chenxun.rpc.netty.protocol.RpcDecoder;
import com.chenxun.rpc.netty.protocol.RpcEncoder;
import com.chenxun.rpc.netty.protocol.RpcRequest;
import com.chenxun.rpc.netty.protocol.RpcResponse;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

public class RpcServer {
	
	private final String host;
	
	private final int port;
	
	public RpcServer(String host,int port){
		this.host=host;
		this.port=port;
		init();
	}
	
	private void init(){
		EventLoopGroup bossGroup = new NioEventLoopGroup();
		EventLoopGroup workerGroup = new NioEventLoopGroup();

		try {
			ServerBootstrap bootstrap = new ServerBootstrap();
			bootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
					.childHandler(new ChannelInitializer<SocketChannel>() {
						@Override
						public void initChannel(SocketChannel channel) throws Exception {
							channel.pipeline().addLast(new LengthFieldBasedFrameDecoder(65536, 0, 4, 0, 0))
									// 将 RPC 请求进行解码（为了处理请求）
									.addLast(new RpcDecoder(RpcRequest.class))
									// 将 RPC 响应进行编码（为了返回响应）
									.addLast(new RpcEncoder(RpcResponse.class))
									// 处理 RPC 请求
									.addLast(new RpcServerHandler());
						}
					}).option(ChannelOption.SO_BACKLOG, 128).childOption(ChannelOption.SO_KEEPALIVE, true);

			ChannelFuture future = bootstrap.bind(host, port).sync();

			future.channel().closeFuture().sync();
		} catch (Exception e) {
		} finally {
			workerGroup.shutdownGracefully();
			bossGroup.shutdownGracefully();
		}
		
	}

}
