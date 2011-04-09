package com.modaldomains.hudsucker.daemon;

import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;

public class ConnectionClient
{
	private ClientBootstrap bootstrap;
	
	public ConnectionClient()
	{
		bootstrap = new ClientBootstrap(
				new NioClientSocketChannelFactory(Executors.newCachedThreadPool(), Executors.newCachedThreadPool()));
		bootstrap.setPipelineFactory(new ConnectionClientPipelineFactory());
	}
	
	public ClientBootstrap bootstrap()
	{
		return bootstrap;
	}
}
