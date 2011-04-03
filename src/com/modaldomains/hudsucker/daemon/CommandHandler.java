package com.modaldomains.hudsucker.daemon;

import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;
import java.util.concurrent.*;

import com.modaldomains.hudsucker.common.AcceptedConnectionHandler;
import com.modaldomains.hudsucker.common.OneRunnable;
import com.modaldomains.hudsucker.common.Commands;

public class CommandHandler implements AcceptedConnectionHandler, OneRunnable, Runnable
{
	class Runner implements Runnable
	{
		ByteBuffer buffer;
		
		public Runner()
		{
			buffer = ByteBuffer.allocate(10);
		}
		
		public void run()
		{
			try
			{
				byte[] data = buffer.array();
				Commands.CommandRequest request = Commands.CommandRequest.parseFrom(data);
				
				switch (request.getType())
				{
				case PING:
					break;
					
				case DEREGISTER_CLIENT:
					break;
					
				case REGISTER_CLIENT:
					break;
					
				case ATTACH_TOKEN:
					break;
				}
			}
			catch (Exception e)
			{
				// pass?
			}
		}
	}

	private Set<SocketChannel> channels;
	private Selector selector;
	private boolean running;
	
	private BlockingQueue<Runnable> taskQueue;
	private Executor executor;
	
	public CommandHandler()
		throws java.io.IOException
	{
		channels = new HashSet<SocketChannel>();
		selector = Selector.open();
		
		taskQueue = new ArrayBlockingQueue<Runnable>(10);
		executor = new ThreadPoolExecutor(5, 10, 1, TimeUnit.MINUTES, taskQueue);
	}
	
	@Override
	public void didAcceptConnection(ServerSocketChannel serverChannel,
			SocketChannel channel)
	{
		try
		{
			channels.add(channel);
			channel.register(selector, SelectionKey.OP_READ);
		}
		catch (java.io.IOException ioe)
		{
			if (channels.contains(channel))
				channels.remove(channel);
		}
	}
	
	public void run()
	{
		if (running) return;
		running = true;
		while (running)
		{
			this.runOne(1000);
		}
	}

	public void runOne(long timeout)
	{
		try
		{
			int n = selector.select(timeout);
			if (n > 0)
			{
				
			}
		}
		catch (java.io.IOException ioe)
		{
		}
	}
}
