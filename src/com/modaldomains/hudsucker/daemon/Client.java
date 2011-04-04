package com.modaldomains.hudsucker.daemon;

import java.net.*;
import java.util.Collection;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

import com.modaldomains.hudsucker.common.ByteString;

public class Client
{
	private final InetSocketAddress address;
	private Date lastPingTime;
	private Date lastConnectTime;
	private ConcurrentHashMap<ByteString, Date> tokens;
	
	public Client(final InetSocketAddress address)
	{
		this.address = address;
		tokens = new ConcurrentHashMap<ByteString, Date>();
		lastConnectTime = new Date(0);
	}
	
	public void ping()
	{
		lastPingTime = new Date();
	}
	
	public void addToken(ByteString token)
	{
		tokens.put(token, new Date());
	}
	
	public Collection<ByteString> tokens()
	{
		return tokens.keySet();
	}
	
	public InetSocketAddress address()
	{
		return address;
	}
	
	public Date lastPingTime()
	{
		return lastPingTime;
	}
	
	public Date lastConnectTime()
	{
		return lastConnectTime;
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
