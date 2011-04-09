/* Driver.java -- main method.
 * Copyright (C) 2011 Modal Domains.
 * 
 * This file is a part of Hudsucker.
 * 
 * Hudsucker is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Hudsucker is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Hudsucker.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.modaldomains.hudsucker.daemon;

import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;

import com.modaldomains.hudsucker.common.SocketListener;

import gnu.getopt.*;

public class Driver
{
	static String VERSION = "0.0.1";

	/**
	 * @param args
	 */
	public static void main(final String[] args) throws Exception
	{
		Getopt getopt = new Getopt("hudd", args, "l:L:p:P:hv", new LongOpt[]
		{
			new LongOpt("listen-address", LongOpt.REQUIRED_ARGUMENT, null, 'l'),
			new LongOpt("command-adddress", LongOpt.REQUIRED_ARGUMENT, null, 'L'),
			new LongOpt("port", LongOpt.REQUIRED_ARGUMENT, null, 'p'),
			new LongOpt("command-port", LongOpt.REQUIRED_ARGUMENT, null, 'P'),
			new LongOpt("help", LongOpt.NO_ARGUMENT, null, 'h')
        });
		
		int lport = 443;
		int cport = 9999;
		String laddr = null;
		String caddr = null;
		
		int opt = 0;
		while ((opt = getopt.getopt()) != -1)
		{
			switch (opt)
			{
			case 'h':
				System.out.println("usage: hudd [options]");
				System.out.println();
				System.out.println(" -l, --listen-address ADDR    Bind to ADDR to accept connections (default: any address).");
				System.out.println(" -L, --command-address ADDR   Bind to ADDR to accept command connections (default: any address).");
				System.out.println(" -p, --port PORT              Listen for connections on PORT (default: 443).");
				System.out.println(" -P, --command-port PORT      Listen for command connections on PORT (default: 9999)");
				System.out.println(" -h, --help                   Show this message and exit.");
				System.out.println(" -v, --version                Print version number and exit.");
				System.exit(0);
				
			case 'v':
				System.out.println("hudd version " + VERSION);
				System.exit(0);
				
			case 'p':
				lport = Integer.parseInt(getopt.getOptarg());
				break;
				
			case 'P':
				cport = Integer.parseInt(getopt.getOptarg());
				break;
			}
		}
		
		CommandServer server = new CommandServer(caddr, cport);
		Thread commandThread = new Thread(server);
		commandThread.setName("CommandThread");
		commandThread.start();
		
		/*ConnectionHandler mainHandler = new ConnectionHandler();
		ServerSocketChannel mainChannel = ServerSocketChannel.open();
		mainChannel.socket().bind(new InetSocketAddress(lport));
		SocketListener mainListener = new SocketListener(mainChannel);
		mainListener.addAcceptHandler(mainHandler);
		mainListener.run();*/
	}
}
