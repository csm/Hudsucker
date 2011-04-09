package com.modaldomains.hudsucker.daemon;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;

public class ConnectionClientHandler extends SimpleChannelHandler
{
	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
			throws Exception
	{
		// TODO Auto-generated method stub
		super.messageReceived(ctx, e);
	}
}
