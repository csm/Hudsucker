package com.modaldomains.hudsucker.daemon;

import java.util.concurrent.*;

public class ClientMap
{
	private ConcurrentHashMap<Client, Client> clients;
	private ConcurrentHashMap<String, Client> tokenClients;
	
	public static ClientMap INSTANCE = new ClientMap();
	
	private ClientMap()
	{
		clients = new ConcurrentHashMap<Client, Client>();
		tokenClients = new ConcurrentHashMap<String, Client>();
	}
	
	public void addClient(Client c)
	{
		clients.put(c, c);
	}
	
	public void removeClient(Client c)
	{
		clients.remove(c);
		for (String token : c.tokens())
			tokenClients.remove(token);
	}
	
	public Client clientForToken(String token)
	{
		return tokenClients.get(token);
	}
}
