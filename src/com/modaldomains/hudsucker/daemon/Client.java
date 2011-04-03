package com.modaldomains.hudsucker.daemon;

import java.net.*;
import java.util.Collection;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

public class Client
{
	private final InetSocketAddress address;
	private Date lastPingTime;
	private ConcurrentHashMap<String, String> tokens;
	
	public Client(final InetSocketAddress address)
	{
		this.address = address;
		tokens = new ConcurrentHashMap<String, String>();
	}
	
	public void ping()
	{
		lastPingTime = new Date();
	}
	
	public Collection<String> tokens()
	{
		return tokens.keySet();
	}

	@Override
	public int hashCode()
	{
		return address.hashCode();
	}
	
	@Override
	public boolean equals(Object other)
	{
		try
		{
			return this.equals((Client) other);
		}
		catch (ClassCastException cce)
		{
			return false;
		}
	}
	
	public boolean equals(Client that)
	{
		if (that == null) return false;
		return address.equals(that.address);
	}
}
