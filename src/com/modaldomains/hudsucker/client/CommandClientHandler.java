package com.modaldomains.hudsucker.client;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;

import com.modaldomains.hudsucker.common.Commands.CommandRequest;
import com.modaldomains.hudsucker.common.Commands.CommandResponse;
import com.modaldomains.hudsucker.common.Commands.CommandType;

public class CommandClientHandler extends SimpleChannelUpstreamHandler
{
	private static final Logger logger = Logger.getLogger(CommandClientHandler.class.getName());
	private volatile Channel channel;
	private ConcurrentHashMap<String, CommandRequest> requests = new ConcurrentHashMap<String, CommandRequest>();
	
	public void register()
	{
		CommandRequest.Builder builder = CommandRequest.newBuilder();
		builder.setType(CommandType.REGISTER_CLIENT);
		builder.setRequestId(UUID.randomUUID().toString());
		// TODO -- parameters: address, port
		CommandRequest request = builder.build();
		requests.put(request.getRequestId(), request);
		channel.write(request);
	}
	
	public void deregister()
	{
		CommandRequest.Builder builder = CommandRequest.newBuilder();
		builder.setType(CommandType.DEREGISTER_CLIENT);
		builder.setRequestId(UUID.randomUUID().toString());
		CommandRequest request = builder.build();
		requests.put(request.getRequestId(), request);
		channel.write(request);
	}
	
	public void ping()
	{
		CommandRequest.Builder builder = CommandRequest.newBuilder();
		builder.setType(CommandType.PING);
		builder.setRequestId(UUID.randomUUID().toString());
		CommandRequest request = builder.build();
		requests.put(request.getRequestId(), request);
		channel.write(request);
	}
	
	@Override
	public void handleUpstream(ChannelHandlerContext arg0, ChannelEvent arg1)
			throws Exception
	{
		// TODO Auto-generated method stub
		super.handleUpstream(arg0, arg1);
	}
	
	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
			throws Exception
	{
		CommandResponse response = (CommandResponse) e.getMessage();
		CommandRequest request = requests.get(response.getRequestId());
		if (request != null)
		{
			requests.remove(response.getRequestId());
			if (!response.getSuccess())
			{
				String reason = "unknown";
				for (CommandResponse.Parameter p : response.getParametersList())
				{
					if ("reason".equals(p.getName()))
					{
						reason = p.getValue();
						break;
					}
				}
				logger.log(Level.WARNING, "Request {0} failed; reason: {1}",
						new Object[] { request.getType(), reason });
			}
			else
			{
				logger.log(Level.INFO, "Request {0}:{1} successful",
						new Object[] { response.getRequestId(), response.getType() });
			}
		}
		else
		{
			logger.log(Level.WARNING, "Response {0} {1} did not match any request.",
					new Object[] { response.getRequestId(), response.getType() });
		}
	}
	
	@Override
	public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e)
			throws Exception
	{
		channel = e.getChannel();
		super.channelOpen(ctx, e);
	}
}
