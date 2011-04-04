package com.modaldomains.hudsucker.common;

import java.util.Arrays;
import java.util.Formatter;

public class ByteString implements Cloneable, Comparable<ByteString>
{
	private final byte[] bytes;
	
	private ByteString(ByteString that)
	{
		this.bytes = that.bytes;
	}
	
	public ByteString(final byte[] bytes)
	{
		this(bytes, 0, bytes.length);
	}
	
	public ByteString(final byte[] bytes, final int offset, final int length)
	{
		this.bytes = new byte[length];
		System.arraycopy(bytes, offset, this.bytes, 0, length);
	}
	
	public int length()
	{
		return bytes.length;
	}
	
	public Object clone()
	{
		return new ByteString(this);
	}
	
	@Override
	public int compareTo(ByteString that)
	{
		if (this.length() < that.length())
			return -1;
		if (this.length() > that.length())
			return 1;
		for (int i = 0; i < bytes.length; i++)
		{
			int b1 = this.bytes[i] & 0xFF;
			int b2 = that.bytes[i] & 0xFF;
	        if (b1 < b2)
	        	return -1;
	        if (b1 > b2)
	        	return 1;
		}
		return 0;
	}
	
	private static String HEX = "0123456789abcdef";
	
	@Override
	public String toString()
	{
		StringBuilder str = new StringBuilder();
		for (int i = 0; i < bytes.length; i++)
		{
			if (i > 0)
				str.append(':');
			str.append(HEX.charAt(bytes[i] >>> 4)).append(HEX.charAt(bytes[i] & 0xf));
		}
		return str.toString();
	}
	
	@Override
	public boolean equals(Object obj)
	{
		try
		{
			ByteString that = (ByteString) obj;
			if (this.length() != that.length())
				return false;
			if (!Arrays.equals(this.bytes, that.bytes))
				return false;
			return true;
		}
		catch (NullPointerException npe)
		{
			return false;
		}
		catch (ClassCastException cce)
		{
			return false;
		}
	}
	
	@Override
	public int hashCode()
	{
		int code = 0;
		for (int i = 0; i < bytes.length; i++)
			code += (int) bytes[i] & 0xff;
		return code;
	}
}
