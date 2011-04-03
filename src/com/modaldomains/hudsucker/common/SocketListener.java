package com.modaldomains.hudsucker.common;

import java.util.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;

public class SocketListener implements Runnable
{
	protected ServerSocketChannel channel;
	protected Selector selector;
	protected boolean isRunning;
	protected Set<AcceptedConnectionHandler> acceptHandlers;

	public SocketListener(final ServerSocketChannel channel)
		throws IOException
	{
		this.channel = channel;
		this.selector = Selector.open();
		this.channel.register(this.selector, SelectionKey.OP_ACCEPT);
		
		this.acceptHandlers = new HashSet<AcceptedConnectionHandler>();
	}
	
	@Override
	public void run()
	{
		if (isRunning)
			return;
		isRunning = true;
		while (isRunning)
		{
			try
			{
				this.runOne();
			}
			catch (IOException ioe)
			{
				// pass -- FIXME
			}
		}
	}
	
	public void runOne()
		throws IOException
	{
		int n = selector.selectNow();
		if (n > 0)
		{
			SocketChannel clientChannel = channel.accept();
			for (AcceptedConnectionHandler handler : acceptHandlers)
				handler.didAcceptConnection(channel, clientChannel);
		}
	}
	
	public void addAcceptHandler(AcceptedConnectionHandler handler)
	{
		acceptHandlers.add(handler);
	}
	
	public void removeAcceptHandler(AcceptedConnectionHandler handler)
	{
		acceptHandlers.remove(handler);
	}
}
