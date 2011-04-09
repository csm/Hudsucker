/* Handshake.java -- SSL Handshake message.
   Copyright (C) 2006  Free Software Foundation, Inc.

This file is a part of GNU Classpath.

GNU Classpath is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or (at
your option) any later version.

GNU Classpath is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with GNU Classpath; if not, write to the Free Software
Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301
USA

Linking this library statically or dynamically with other modules is
making a combined work based on this library.  Thus, the terms and
conditions of the GNU General Public License cover the whole
combination.

As a special exception, the copyright holders of this library give you
permission to link this library with independent modules to produce an
executable, regardless of the license terms of these independent
modules, and to copy and distribute the resulting executable under
terms of your choice, provided that you also meet, for each linked
independent module, the terms and conditions of the license of that
module.  An independent module is a module which is not derived from
or based on this library.  If you modify this library, you may extend
this exception to your version of the library, but you are not
obligated to do so.  If you do not wish to do so, delete this
exception statement from your version.  */


//package gnu.javax.net.ssl.provider;
package com.modaldomains.hudsucker.daemon;

import java.io.PrintWriter;
import java.io.StringWriter;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.jboss.netty.buffer.ChannelBuffer;

/**
 * An SSL handshake message. SSL handshake messages have the following
 * form:
 *
 * <pre>
struct
{
  HandshakeType msg_type;
  uint24        length;
  select (msg_type)
  {
    case hello_request:       HelloRequest;
    case client_hello:        ClientHello;
    case server_hello:        ServerHello;
    case certificate:         Certificate;
    case server_key_exchange: ServerKeyExchange;
    case certificate_request: CertificateRequest;
    case server_hello_done:   ServerHelloDone;
    case certificate_verify:  CertificateVerify;
    case client_key_exchange: ClientKeyExchange;
    case finished:            Finished;
  } body;
};</pre>
 */
public final class Handshake //implements Constructed
{

  // Fields.
  // -------------------------------------------------------------------------

  private final ChannelBuffer buffer;
  private final ProtocolVersion version;

  // Constructors.
  // -------------------------------------------------------------------------

  public Handshake (final ChannelBuffer buffer)
  {
	  this.buffer = buffer.duplicate();
	  this.version = ProtocolVersion.TLS_1_1;
  }

  // Instance methods.
  // -------------------------------------------------------------------------

  /**
   * Returns the handshake type.
   *
   * @return The handshake type.
   */
  public Type type()
  {
    return Type.forInteger(buffer.getByte(0) & 0xFF);
  }

  /**
   * Returns the message length.
   *
   * @return The message length.
   */
  public int length ()
  {
    // Length is a uint24.
    return buffer.getInt(0) & 0xFFFFFF;
  }

  /**
   * Returns the handshake message body. Depending on the handshake
   * type, some implementation of the Body interface is returned.
   *
   * @return The handshake body.
   */
  public Body body()
  {
    Type type = type ();
    ChannelBuffer bodyBuffer = bodyBuffer();
    switch (type)
      {
      case CLIENT_HELLO:
        return new ClientHello(bodyBuffer);

      case SERVER_HELLO:
        return new ServerHello(bodyBuffer);
      }
    return null;
  }

  /**
   * Returns a subsequence of the underlying buffer, containing only
   * the bytes that compose the handshake body.
   *
   * @return The body's byte buffer.
   */
  public ChannelBuffer bodyBuffer()
  {
	  int length = length();
	  if (length > buffer.readableBytes() + 4)
		  length = buffer.readableBytes() - 4;
	  return buffer.slice(4, length);
  }

  // Inner class.
  // -------------------------------------------------------------------------

  public static interface Body
  {
  }

  public static enum Type
  {
    CLIENT_HELLO        ( 1),
    SERVER_HELLO        ( 2);

    private final int value;

    private Type(int value)
    {
      this.value = value;
    }

    // Class methods.
    // -----------------------------------------------------------------------

    /**
     * Convert a raw handshake type value to a type enum value.
     *
     * @return The corresponding enum value for the raw integer value.
     * @throws IllegalArgumentException If the value is not a known handshake
     *  type.
     */
    public static Type forInteger (final int value)
    {
      switch (value & 0xFF)
        {
        case 1:  return CLIENT_HELLO;
        case 2:  return SERVER_HELLO;
        default: return null;
        }
    }

    public int getValue()
    {
      return value;
    }
  }
}
