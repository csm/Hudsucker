package com.modaldomains.hudsucker.daemon;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;

import com.modaldomains.hudsucker.client.CommandClient;
import com.modaldomains.hudsucker.common.Commands.CommandRequest;
import com.modaldomains.hudsucker.common.Commands.CommandResponse;
import com.modaldomains.hudsucker.common.Commands.CommandType;
import com.modaldomains.hudsucker.common.Constants;

public class CommandServerHandler extends SimpleChannelHandler
{
	private static final Logger logger = Logger.getLogger(CommandServerHandler.class.getName());
	
	@Override
	public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e)
			throws Exception {
		// TODO Auto-generated method stub
		super.channelConnected(ctx, e);
	}
	
	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
			throws Exception
	{
		CommandRequest request = (CommandRequest) e.getMessage();
		CommandResponse.Builder builder = CommandResponse.newBuilder();
		builder.setRequestId(request.getRequestId());
		builder.setResponseId(UUID.randomUUID().toString());
		logger.log(Level.INFO, "CommandRequest from {0}: {1}", new Object[] { e.getChannel().getRemoteAddress(), request.getType() });
		switch (request.getType())
		{
		case REGISTER_CLIENT:
		{
			builder.setType(CommandType.REGISTER_CLIENT);
			SocketAddress address = e.getChannel().getRemoteAddress();
			int port = Constants.DEFAULT_PORT;
			InetAddress inetAddress = ((InetSocketAddress) address).getAddress();
			boolean success = true;
			Exception reason = null;
			for (CommandRequest.Parameter p : request.getParametersList())
			{
				if ("port".equals(p.getName()))
				{
					try
					{
						port = Integer.parseInt(p.getValue());
						if (port < 1)
							throw new IllegalArgumentException("invalid port specified: " + p.getValue());
					}
					catch (Exception x)
					{
						success = false;
						reason = x;
						break;
					}
				}
				if ("address".equals(p.getName()))
				{
					try
					{
						inetAddress = InetAddress.getByName(p.getValue());
					}
					catch (UnknownHostException uhe)
					{
						success = false;
						reason = uhe;
						break;
					}
				}
			}
			if (success)
			{
				Client c = new Client(new InetSocketAddress(inetAddress, port));
				ClientMap.INSTANCE.addClient(c);
				builder.setSuccess(true);
			}
			else
			{
				builder.setSuccess(false);
				if (reason != null)
				{
					CommandResponse.Parameter.Builder pb = CommandResponse.Parameter.newBuilder();
					pb.setName("reason");
					pb.setValue(reason.toString());
				}
			}
		}
			break;
			
		case DEREGISTER_CLIENT:
		{
			ClientMap.INSTANCE.removeClient(e.getChannel().getRemoteAddress());
			builder.setType(CommandType.DEREGISTER_CLIENT);
			builder.setSuccess(true);
		}
			break;
			
		case PING:
		{
			builder.setType(CommandType.PING);
			SocketAddress address = e.getChannel().getRemoteAddress();
			int port = Constants.DEFAULT_PORT;
			InetAddress inetAddress = ((InetSocketAddress) address).getAddress();
			boolean success = true;
			Exception reason = null;
			for (CommandRequest.Parameter p : request.getParametersList())
			{
				if ("port".equals(p.getName()))
				{
					try
					{
						port = Integer.parseInt(p.getValue());
						if (port < 1)
							throw new IllegalArgumentException("invalid port specified: " + p.getValue());
					}
					catch (Exception x)
					{
						success = false;
						reason = x;
						break;
					}
				}
				if ("address".equals(p.getName()))
				{
					try
					{
						inetAddress = InetAddress.getByName(p.getValue());
					}
					catch (UnknownHostException uhe)
					{
						success = false;
						reason = uhe;
						break;
					}
				}
			}
			if (success)
			{
				Client client = ClientMap.INSTANCE.getClient(new InetSocketAddress(inetAddress, port));
				if (client != null)
				{
					client.ping();
					builder.setSuccess(true);
				}
				else
				{
					builder.setSuccess(false);
					CommandResponse.Parameter.Builder pb = CommandResponse.Parameter.newBuilder();
					pb.setName("reason");
					pb.setValue("not-found");
					builder.addParameters(pb.build());
				}
			}
			else
			{
				builder.setSuccess(false);
				if (reason != null)
				{
					CommandResponse.Parameter.Builder pb = CommandResponse.Parameter.newBuilder();
					pb.setName("reason");
					pb.setValue(reason.toString());
					builder.addParameters(pb.build());
				}
			}
		}
			break;
			
		default:
		{
			builder.setType(CommandType.ERROR);
			builder.setSuccess(false);
			CommandResponse.Parameter.Builder pb = CommandResponse.Parameter.newBuilder();
			pb.setName("reason");
			pb.setValue("unknown-command:" + request.getType());
			builder.addParameters(pb.build());
		}
			break;
		}
		
		e.getChannel().write(builder.build());
	}
}
