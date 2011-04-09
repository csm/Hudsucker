package com.modaldomains.hudsucker.daemon;

import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.jboss.netty.buffer.ByteBufferBackedChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;

import com.modaldomains.hudsucker.common.ByteString;
import com.modaldomains.hudsucker.daemon.Handshake.Type;

public class ConnectionServerHandler extends SimpleChannelHandler
{
	static class Entry
	{
		Client client;
		Channel channel;
		ConcurrentLinkedQueue<ChannelBuffer> dataBuffers = new ConcurrentLinkedQueue<ChannelBuffer>();
	}
	
	private ConcurrentHashMap<SocketAddress, Entry> channelMap;
	private ConnectionClient connectionClient;
	
	public ConnectionServerHandler()
	{
		channelMap = new ConcurrentHashMap<SocketAddress, Entry>();
		connectionClient = new ConnectionClient();
	}
	
	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
			throws Exception
	{
		Entry entry = channelMap.get(ctx.getChannel().getRemoteAddress());
		if (entry != null)
		{
			entry.channel.write(e.getMessage());
			return;
		}
		ChannelBuffer buffer = (ChannelBuffer) e.getMessage();
		Handshake handshake = new Handshake(buffer);
		Client client = null;
		ByteString token = null;
		try
		{
			if (handshake.type() == Type.CLIENT_HELLO)
				token = new ByteString(((ClientHello) handshake.body()).sessionId());
		}
		catch (Exception x)
		{
		}
		client = ClientMap.INSTANCE.clientForToken(token);
		entry = new Entry();
		entry.client = client;
		entry.dataBuffers.add(buffer.copy());
		final Entry entry2 = entry;
		connectionClient.bootstrap().connect(client.address()).addListener(new ChannelFutureListener()
		{	
			@Override
			public void operationComplete(ChannelFuture future) throws Exception
			{
				Channel channel = future.getChannel();
				entry2.channel = channel;
				ChannelBuffer buffer = entry2.dataBuffers.poll();
				if (buffer != null)
					entry2.channel.write(buffer);
			}
		});
		channelMap.put(ctx.getChannel().getRemoteAddress(), entry);
	}
	
	@Override
	public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e)
			throws Exception
	{
		// TODO Auto-generated method stub
		super.channelClosed(ctx, e);
	}
}
