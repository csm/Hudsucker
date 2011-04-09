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

package com.modaldomains.hudsucker.client;

import java.net.InetAddress;
import java.util.concurrent.TimeUnit;

import org.jboss.netty.util.HashedWheelTimer;
import org.jboss.netty.util.Timeout;
import org.jboss.netty.util.Timer;
import org.jboss.netty.util.TimerTask;

import com.modaldomains.hudsucker.common.Constants;

import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;

public class Driver
{
	
	private static CommandClient client;
	private static Timer timer;

	/**
	 * @param args
	 */
	public static void main(final String[] args) throws Exception
	{
		Getopt getopt = new Getopt("hudc", args, "h:p:Hv", new LongOpt[] {
			new LongOpt("host", LongOpt.REQUIRED_ARGUMENT, null, 'h'),
			new LongOpt("port", LongOpt.REQUIRED_ARGUMENT, null, 'p'),
			new LongOpt("help", LongOpt.NO_ARGUMENT, null, 'H'),
			new LongOpt("version", LongOpt.NO_ARGUMENT, null, 'v')
		});
		
		String host = null;
		int port = Constants.DEFAULT_COMMAND_PORT;
		
		int c = -1;
		while ((c = getopt.getopt()) != -1)
		{
			switch (c)
			{
			case 'h':
				host = getopt.getOptarg();
				break;
				
			case 'p':
				port = Integer.parseInt(getopt.getOptarg());
				break;
				
			case 'H':
				System.out.println("usage: hudc [options]");
				System.out.println();
				System.out.println(" -h, --host HOST   Connect to server HOST (required argument).");
				System.out.println(" -p, --port PORT   Connect over PORT (default: " + Constants.DEFAULT_COMMAND_PORT + ")");
				System.out.println(" -H, --help        Show this help and exit.");
				System.out.println(" -v, --version     Show version info and exit.");
				System.exit(0);
				
			case 'v':
				System.exit(0);
				
			case '?':
				System.err.format("Try `hudc --help' for more info.%n");
				System.exit(1);
			}
		}
		
		if (host == null)
		{
			System.err.println("The --host option is required.");
			System.exit(1);
		}
		
		InetAddress address = InetAddress.getByName(host);
		
		client = new CommandClient(address, port);
		client.run();
		
		// TODO -- actually monitor the server, register, deregister, and ping.
		
		client.register();
		
		timer = new HashedWheelTimer();
		TimerTask timerTask = new TimerTask()
		{
			@Override
			public void run(Timeout arg0) throws Exception
			{
				client.ping();
				timer.newTimeout(this, 5, TimeUnit.SECONDS);
			}
		};
		timer.newTimeout(timerTask, 5, TimeUnit.SECONDS);
	}
}
