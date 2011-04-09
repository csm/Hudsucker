package com.modaldomains.hudsucker.daemon;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Collection;
import java.util.Comparator;
import java.util.concurrent.*;

import com.modaldomains.hudsucker.common.ByteString;
import com.modaldomains.hudsucker.common.FP;

public class ClientMap
{
	private ConcurrentHashMap<Client, Client> clients;
	
	public static ClientMap INSTANCE = new ClientMap();
	
	private ClientMap()
	{
		clients = new ConcurrentHashMap<Client, Client>();
	}
	
	public void addClient(final Client c)
	{
		Collection<Client> cl = FP.filter(clients.values(), new FP.P<Client>()
		{
			@Override
			public Boolean a(Client input)
			{
				InetAddress a1 = c.address().getAddress();
				InetAddress a2 = input.address().getAddress();
				return a1.equals(a2);
			}
			
		});
		clients.keySet().removeAll(cl);
		clients.put(c, c);
	}
	
	public Client getClient(final SocketAddress address)
	{
		Collection<Client> c = FP.filter(clients.values(), new FP.P<Client>()
		{
			@Override
			public Boolean a(Client input)
			{
				InetAddress a1 = input.address().getAddress();
				InetAddress a2 = ((InetSocketAddress) address).getAddress();
				return a1.equals(a2);
			}
			
		});
		return FP.first(c);
	}
	
	public void removeClient(Client c)
	{
		clients.remove(c);
	}
	
	public void removeClient(final SocketAddress address)
	{
		Collection<Client> c = FP.filter(clients.values(), new FP.P<Client>()
		{
			@Override
			public Boolean a(Client input)
			{
				InetAddress a1 = input.address().getAddress();
				InetAddress a2 = ((InetSocketAddress) address).getAddress();
				return a1.equals(a2);
			}
		});
		clients.keySet().removeAll(c);
	}
	
	public Client fetchClient(final ByteString token)
	{
		Client c = null;
		if (token != null)
			c = clientForToken(token);
		if (c == null)
		{
			c = FP.min(clients.values(), new Comparator<Client>()
			{
				@Override
				public int compare(Client c0, Client c1)
				{
					return c0.lastConnectTime().compareTo(c1.lastConnectTime());
				}
			});
		}
		return c;
	}
	
	public Client clientForToken(final ByteString token)
	{
		Collection<Client> c = FP.filter(clients.values(), new FP.P<Client>()
		{
			@Override
			public Boolean a(Client input)
			{
				return input.tokens().contains(token);
			}
		});
		return FP.first(c);
	}
}
