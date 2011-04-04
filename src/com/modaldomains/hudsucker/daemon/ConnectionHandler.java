package com.modaldomains.hudsucker.daemon;

import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.modaldomains.hudsucker.common.AcceptedConnectionHandler;
import com.modaldomains.hudsucker.common.ByteString;
import com.modaldomains.hudsucker.common.FP;
import com.modaldomains.hudsucker.common.OneRunnable;

public class ConnectionHandler implements AcceptedConnectionHandler, OneRunnable, Runnable
{
	private static class ConnectionState
	{
		ConnectionState(boolean isClient)
		{
			this.isClient = isClient;
		}
		
		boolean isClient;

		/**
		 * If true, we need to look at the data stream.
		 */
		boolean inspect = true;
		
		ByteBuffer buffer;
	}
	
	private ConcurrentLinkedQueue<SocketChannel> incomingSockets;
	private ConcurrentHashMap<SocketChannel, SocketChannel> sockets;
	private Selector selector;
	private boolean running;
	
	public ConnectionHandler() throws java.io.IOException
	{
		incomingSockets = new ConcurrentLinkedQueue<SocketChannel>();
		sockets = new ConcurrentHashMap<SocketChannel, SocketChannel>();
		selector = Selector.open();
	}
	
	@Override
	public void didAcceptConnection(ServerSocketChannel serverChannel,
			SocketChannel channel)
	{
		try
		{
			channel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE, new ConnectionState(true));
			selector.wakeup();
			incomingSockets.add(channel);
		}
		catch (java.io.IOException ioe)
		{
			// pass?
		}
	}
	
	public void run()
	{
		if (running)
			return;
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
					if (k.isReadable())
					{
						SocketChannel source = (SocketChannel) k.channel();
						SocketChannel sink = sockets.get(source);
						ConnectionState state = (ConnectionState) k.attachment();
						
						if (state.isClient && state.inspect)
						{
							SocketChannel channel = (SocketChannel) k.channel();
							ByteBuffer buffer = ByteBuffer.allocate(256);
							int len = channel.read(buffer);
							buffer.flip();
							ByteString token = null;
							Handshake hs = new Handshake(buffer);
							if (Handshake.Type.CLIENT_HELLO == hs.type())
							{
								ClientHello hello = (ClientHello) hs.body();
								token = new ByteString(hello.sessionId());
							}
							Client client = ClientMap.INSTANCE.fetchClient(token);
							sink = SocketChannel.open(client.address());
							sink.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE,
									new ConnectionState(false));
							sockets.put(source, sink);
							sockets.put(sink, source);
							state.inspect = false;
							state.buffer = buffer.duplicate();
						}
						else if (!state.isClient && state.inspect)
						{
							SocketChannel channel = (SocketChannel) k.channel();
							ByteBuffer buffer = ByteBuffer.allocate(256);
							int len = channel.read(buffer);
							buffer.flip();
							Handshake hs = new Handshake(buffer);
							if (Handshake.Type.SERVER_HELLO == hs.type())
							{
								ServerHello hello = (ServerHello) hs.body();
								ByteString token = new ByteString(hello.sessionId());
								Client client = ClientMap.INSTANCE.getClient(channel.socket().getRemoteSocketAddress());
								if (client != null)
									client.addToken(token);
							}
							state.inspect = false;
							state.buffer = buffer;
						}
						else
						{
							// Check if the sink is writable
							final SocketChannel __sink = sink;
							if (FP.any(selector.selectedKeys(), new FP.P<SelectionKey>() {
								public Boolean a(SelectionKey k)
								{
									return k.isWritable() && __sink == k.channel();
								}
							}))
							{
								if (state.buffer != null)
								{
									sink.write(state.buffer);
									if (state.buffer.remaining() == 0)
										state.buffer = null;
								}
								else
								{
									ByteBuffer buffer = ByteBuffer.allocate(1024);
									while (source.read(buffer) > 0)
									{
										buffer.flip();
										sink.write(buffer);
										buffer.clear();
									}
								}
							}
						}
					}
				}
			}
		}
		catch (java.io.IOException ioe)
		{
			// pass ?
		}
	}
}
