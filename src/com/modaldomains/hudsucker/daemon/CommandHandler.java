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
import com.modaldomains.hudsucker.common.Commands.CommandResponse;
import com.modaldomains.hudsucker.common.Commands.CommandType;
import com.modaldomains.hudsucker.common.OneRunnable;
import com.modaldomains.hudsucker.common.Commands;
import com.modaldomains.hudsucker.common.Commands.CommandRequest;
import com.modaldomains.hudsucker.common.Utils;

public class CommandHandler implements AcceptedConnectionHandler, OneRunnable, Runnable
{
	class Runner implements Runnable
	{
		ByteBuffer buffer;
		ByteBuffer outBuffer;
		SocketChannel channel;
		
		public Runner(SocketChannel channel)
		{
			buffer = ByteBuffer.allocate(16);
			this.channel = channel;
		}
		
		public void run()
		{
			try
			{
				byte[] data = new byte[buffer.remaining()];
				buffer.duplicate().get(data);
				Commands.CommandRequest request = Commands.CommandRequest.parseFrom(data);
				Commands.CommandResponse response = null;
				
				switch (request.getType())
				{
				case PING:
				{
					SocketAddress address = channel.socket().getRemoteSocketAddress();
					Client client = ClientMap.INSTANCE.getClient(address);
					if (client != null)
						client.ping();
					
					Commands.CommandResponse.Builder builder = Commands.CommandResponse.newBuilder();
					builder.setType(CommandType.PING);
					builder.setRequestId(request.getRequestId());
					builder.setResponseId(UUID.randomUUID().toString());
					response = builder.build();
				}
					break;
					
				case DEREGISTER_CLIENT:
				{
					SocketAddress address = channel.socket().getRemoteSocketAddress();
					ClientMap.INSTANCE.removeClient(address);
					Commands.CommandResponse.Builder builder = Commands.CommandResponse.newBuilder();
					builder.setType(CommandType.DEREGISTER_CLIENT);
					builder.setRequestId(request.getRequestId());
					builder.setResponseId(UUID.randomUUID().toString());
					response = builder.build();
				}
					break;
					
				case REGISTER_CLIENT:
				{
					SocketAddress address = channel.socket().getRemoteSocketAddress();
					Client client = new Client((InetSocketAddress) address);
					ClientMap.INSTANCE.addClient(client);
					Commands.CommandResponse.Builder builder = Commands.CommandResponse.newBuilder();
					builder.setType(CommandType.REGISTER_CLIENT);
					builder.setRequestId(request.getRequestId());
					builder.setResponseId(UUID.randomUUID().toString());
					response = builder.build();
				}
					break;
					
				default:
				{
					Commands.CommandResponse.Builder builder = Commands.CommandResponse.newBuilder();
					builder.setType(CommandType.ERROR);
					builder.setRequestId(request.getRequestId());
					builder.setResponseId(UUID.randomUUID().toString());
					Commands.CommandResponse.Parameter.Builder reason = Commands.CommandResponse.Parameter.newBuilder();
					reason.setName("reason");
					reason.setValue("unknown-command");
					builder.addParameters(reason);
					response = builder.build();
				}
					break;
				}
				
				outBuffer = ByteBuffer.wrap(response.toByteArray());
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
			channel.configureBlocking(false);
			channel.register(selector, SelectionKey.OP_READ, new Runner(channel));
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
				for (SelectionKey k : selector.selectedKeys())
				{
					SocketChannel channel = (SocketChannel) k.channel();
					Runner r = (Runner) k.attachment();
					if (!channel.isOpen())
					{
						// Remote hung up?
						k.cancel();
						channel.close();
						this.channels.remove(channel);
					}
					if (k.isReadable())
					{
						if (r.buffer.remaining() == 0)
						{
							r.buffer = Utils.resize(r.buffer);
						}
						while (channel.read(r.buffer) > 0)
						{
							if (r.buffer.remaining() == 0)
								r.buffer = Utils.resize(r.buffer);
						}
						this.taskQueue.add(r);
					}
					if (k.isWritable())
					{
						if (r.outBuffer != null)
						{
							while (r.outBuffer.hasRemaining())
							{
								if (channel.write(r.outBuffer) == 0)
									break;
							}
							if (!r.outBuffer.hasRemaining())
								r.outBuffer = null;
						}
					}
				}
			}
		}
		catch (java.io.IOException ioe)
		{
		}
	}
}
