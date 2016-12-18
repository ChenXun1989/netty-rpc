package com.chenxun.rpc.netty.client;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.concurrent.locks.ReentrantLock;

import com.chenxun.rpc.netty.protocol.RpcRequest;
import com.chenxun.rpc.netty.protocol.RpcResponse;

public class RPCFuture implements Future<Object> {

	private Sync sync;
	private RpcRequest request;
	private RpcResponse response;
	private long startTime;

	private long responseTimeThreshold = 5000;

	private ReentrantLock lock = new ReentrantLock();

	public RPCFuture(RpcRequest request) {
		this.sync = new Sync();
		this.request = request;
		this.startTime = System.currentTimeMillis();
	}

	public boolean isDone() {
		return sync.isDone();
	}

	public Object get() throws InterruptedException, ExecutionException {
		sync.acquire(-1);
        return this.response;
	}

	

	public boolean isCancelled() {
		throw new UnsupportedOperationException();
	}

	public boolean cancel(boolean mayInterruptIfRunning) {
		throw new UnsupportedOperationException();
	}

	public void done(RpcResponse reponse) {
		this.response = reponse;
		sync.release(1);

	}


	static class Sync extends AbstractQueuedSynchronizer {

		private static final long serialVersionUID = 1L;

		// future status
		private final int done = 1;
		private final int pending = 0;

		protected boolean tryAcquire(int acquires) {
			return getState() == done ? true : false;
		}

		protected boolean tryRelease(int releases) {
			if (getState() == pending) {
				if (compareAndSetState(pending, done)) {
					return true;
				}
			}
			return false;
		}

		public boolean isDone() {
			getState();
			return getState() == done;
		}
	}


	public Object get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
		boolean success = sync.tryAcquireNanos(-1, unit.toNanos(timeout));
		if (success) {
			if (this.response != null) {
				return this.response.getResult();
			} else {
				return null;
			}
		} else {
			throw new RuntimeException(
					"Timeout exception. Request id: " + this.request.getRequestId() + ". Request class name: "
							+ this.request.getClassName() + ". Request method: " + this.request.getMethodName());
		}
	}
}
