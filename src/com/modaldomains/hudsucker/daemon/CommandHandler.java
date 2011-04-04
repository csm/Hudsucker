package com.modaldomains.hudsucker.daemon;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;
import java.util.concurrent.*;

import com.modaldomains.hudsucker.common.AcceptedConnectionHandler;
import com.modaldomains.hudsucker.common.OneRunnable;
import com.modaldomains.hudsucker.common.Commands;
import com.modaldomains.hudsucker.common.Commands.CommandRequest;

public class CommandHandler implements AcceptedConnectionHandler, OneRunnable, Runnable
{
	class Runner implements Runnable
	{
		ByteBuffer buffer;
		SocketChannel channel;
		
		public Runner(SocketChannel channel)
		{
			buffer = ByteBuffer.allocate(10);
			this.channel = channel;
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
				{
					SocketAddress address = channel.socket().getRemoteSocketAddress();
					Client client = ClientMap.INSTANCE.getClient(address);
					if (client != null)
						client.ping();
				}
					break;
					
				case DEREGISTER_CLIENT:
				{
					SocketAddress address = channel.socket().getRemoteSocketAddress();
					ClientMap.INSTANCE.removeClient(address);
				}
					break;
					
				case REGISTER_CLIENT:
				{
					SocketAddress address = channel.socket().getRemoteSocketAddress();
					Client client = new Client(address);
					ClientMap.INSTANCE.addClient(client);
				}
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
