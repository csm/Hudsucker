package com.modaldomains.hudsucker.daemon;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;

public class ConnectionServerPipelineFactory implements ChannelPipelineFactory
{
	@Override
	public ChannelPipeline getPipeline() throws Exception
	{
		ChannelPipeline pipeline = Channels.pipeline();
		pipeline.addLast("handler", new ConnectionServerHandler());
		return pipeline;
	}

}
