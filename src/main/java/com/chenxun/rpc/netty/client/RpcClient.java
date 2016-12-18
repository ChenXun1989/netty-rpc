package com.chenxun.rpc.netty.client;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import com.chenxun.rpc.netty.protocol.RpcDecoder;
import com.chenxun.rpc.netty.protocol.RpcEncoder;
import com.chenxun.rpc.netty.protocol.RpcRequest;
import com.chenxun.rpc.netty.protocol.RpcResponse;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

public class RpcClient {
	
    private final String host;
    
    private final int port;
    
    private RpcClient (String host,int port){
    	this.host=host;
    	this.port=port;
    	
    	init();
    }
    
    private static volatile RpcClient client;
    
    public static RpcClient newInstance(){
    	if(client==null){
        	synchronized (RpcClient.class) {
    			if(client==null){
    				client=new RpcClient("127.0.0.1",9999);
    			}
    		}
    	}
    	

    	return client;
    	
    }
    
	private  long connectTimeoutMillis = 6000;
    
	private RpcClientHandler handler;
    
    private ReentrantLock lock = new ReentrantLock();
	private Condition connected = lock.newCondition();
	
   private  ExecutorService threadPool = Executors.newSingleThreadExecutor();
   
   private EventLoopGroup eventLoopGroup = new NioEventLoopGroup(4);
	
	private void init(){
		threadPool.submit(new Runnable() {
			public void run() {
				Bootstrap b = new Bootstrap();
				b.group(eventLoopGroup).channel(NioSocketChannel.class).handler(new ChannelInitializer<SocketChannel>(){

					@Override
					protected void initChannel(SocketChannel socketChannel) throws Exception {
						ChannelPipeline cp = socketChannel.pipeline();
						cp.addLast(new RpcEncoder(RpcRequest.class));
						cp.addLast(new LengthFieldBasedFrameDecoder(65536, 0, 4, 0, 0));
						cp.addLast(new RpcDecoder(RpcResponse.class));
						cp.addLast(new RpcClientHandler());
						
					}
					
				});

				ChannelFuture channelFuture = b.connect(host, port);
				channelFuture.addListener(new ChannelFutureListener() {
					public void operationComplete(final ChannelFuture channelFuture) throws Exception {
						if (channelFuture.isSuccess()) {
							handler= channelFuture.channel().pipeline().get(RpcClientHandler.class);
							signalAvailableHandler();
						}
					}
				});
			}
		});
	  
	}
	
	private void signalAvailableHandler() {
		lock.lock();
		try {
			connected.signalAll();
		} finally {
			lock.unlock();
		}
	}
	
	public RpcClientHandler   getChannel(){
		while(handler==null){
			try {
				waitingForHandler();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return handler;
	}
	
	private boolean waitingForHandler() throws InterruptedException {
		lock.lock();
		try {
			return connected.await(this.connectTimeoutMillis, TimeUnit.MILLISECONDS);
		} finally {
			lock.unlock();
		}
	}

}
