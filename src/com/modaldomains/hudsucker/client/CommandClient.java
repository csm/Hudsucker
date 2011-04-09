package com.modaldomains.hudsucker.client;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;

public class CommandClient implements Runnable
{
	private final InetAddress host;
	private final int port;
	
	private ClientBootstrap bootstrap;
	private Channel channel;
	
	public CommandClient(final InetAddress host, final int port)
	{
		this.host = host;
		this.port = port;
	}
	
	@Override
	public void run()
	{
		bootstrap = new ClientBootstrap(
				new NioClientSocketChannelFactory(Executors.newCachedThreadPool(),
						Executors.newCachedThreadPool()));
		bootstrap.setPipelineFactory(new CommandClientPipelineFactory());
		ChannelFuture connectFuture = bootstrap.connect(new InetSocketAddress(host, port));
		channel = connectFuture.awaitUninterruptibly().getChannel();
	}

	public void register()
	{
		CommandClientHandler handler = channel.getPipeline().get(CommandClientHandler.class);
		handler.register();
	}
	
	public void ping()
	{
		CommandClientHandler handler = channel.getPipeline().get(CommandClientHandler.class);
		handler.ping();
	}
	
	public void deregister()
	{
		CommandClientHandler handler = channel.getPipeline().get(CommandClientHandler.class);
		handler.deregister();
	}
	
	public void close()
	{
		ChannelFuture closeFuture = channel.close();
		closeFuture.addListener(new ChannelFutureListener()
		{	
			@Override
			public void operationComplete(ChannelFuture arg0) throws Exception
			{
				bootstrap.releaseExternalResources();
			}
		});
	}
}
