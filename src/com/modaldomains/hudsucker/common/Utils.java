package com.modaldomains.hudsucker.common;

import java.nio.ByteBuffer;

public class Utils
{
	/**
	 * Allocate a larger buffer that contains the data of an existing buffer.
	 * @param buffer
	 * @return
	 */
	public static ByteBuffer resize(ByteBuffer buffer)
	{
		int capacity = buffer.capacity();
		if (capacity > 2048)
			capacity += 1024;
		else
			capacity *= 2;
		ByteBuffer newBuffer = ByteBuffer.allocate(capacity);
		buffer.flip();
		newBuffer.put(buffer);
		return newBuffer;
	}
}
