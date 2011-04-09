package com.modaldomains.hudsucker.daemon;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

public class ConnectionServer implements Runnable
{
	private final InetAddress address;
	private final int port;
	
	public ConnectionServer(final InetAddress address, final int port)
	{
		this.address = address;
		this.port = port;
	}
	
	public ConnectionServer(final int port)
	{
		this(null, port);
	}
	
	@Override
	public void run()
	{
		ServerBootstrap bootstrap = new ServerBootstrap(
				new NioServerSocketChannelFactory(Executors.newCachedThreadPool(), Executors.newCachedThreadPool()));
		bootstrap.setPipelineFactory(new ConnectionServerPipelineFactory());
		InetSocketAddress addr = null;
		if (address != null)
		{
			addr = new InetSocketAddress(address, port);
		}
		else
		{
			addr = new InetSocketAddress(port);
		}
		bootstrap.bind(addr);
	}
}
