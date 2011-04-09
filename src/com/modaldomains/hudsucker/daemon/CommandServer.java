package com.modaldomains.hudsucker.daemon;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

public class CommandServer implements Runnable
{
	private final String address;
	private final int port;
	
	public CommandServer(String address, int port)
	{
		this.address = address;
		this.port = port;
	}
	
	@Override
	public void run()
	{
		ServerBootstrap bootstrap = new ServerBootstrap(
				new NioServerSocketChannelFactory(Executors.newCachedThreadPool(),
						Executors.newCachedThreadPool()));
		bootstrap.setPipelineFactory(new CommandServerPipelineFactory());
		InetSocketAddress addr = null;
		if (address != null)
		{
			try {
				addr = new InetSocketAddress(InetAddress.getByName(address), port);
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return;
			}
		}
		else
			addr = new InetSocketAddress(port);
		bootstrap.bind(addr);
	}

}
