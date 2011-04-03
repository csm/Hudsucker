package com.modaldomains.hudsucker.daemon;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.modaldomains.hudsucker.common.FP;
import com.modaldomains.hudsucker.common.OneRunnable;

public class ConnectionHandler implements OneRunnable, Runnable
{
	ConcurrentHashMap<SocketChannel, SocketChannel> sockets;
	Selector selector;
	boolean running;
	
	public ConnectionHandler() throws java.io.IOException
	{
		sockets = new ConcurrentHashMap<SocketChannel, SocketChannel>();
		selector = Selector.open();
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
						final SocketChannel source = (SocketChannel) k.channel();
						final SocketChannel sink = sockets.get(source);
						
						// Check if the sink is writable
						if (FP.any(selector.selectedKeys(), new FP.FN<SelectionKey, Boolean>() {
							public Boolean transform(SelectionKey k)
							{
								return k.isWritable() && sink == k.channel();
							}
						}))
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
		catch (java.io.IOException ioe)
		{
			// pass ?
		}
	}
}
