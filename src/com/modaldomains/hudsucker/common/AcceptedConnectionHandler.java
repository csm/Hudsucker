package com.modaldomains.hudsucker.common;

import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public interface AcceptedConnectionHandler
{
	void didAcceptConnection(ServerSocketChannel serverChannel, SocketChannel channel);
}
