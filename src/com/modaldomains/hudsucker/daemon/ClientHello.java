/* ClientHello.java -- SSL ClientHello message.
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

import org.jboss.netty.buffer.ChannelBuffer;

/**
 * A ClientHello handshake message.
 *
 * <pre>
struct
{
  ProtocolVersion   client_version;                // 2
  Random            random;                        // 32
  SessionID         session_id;                    // 1 + 0..32
  CipherSuite       cipher_suites&lt;2..2^16-1&gt;
  CompressionMethod compression_methods&lt;1..2^8-1&gt;
  Extension         client_hello_extension_list&lt;0..2^16-1&gt;
} ClientHello;
</pre>
 */
public class ClientHello implements Handshake.Body
{

  // Fields.
  // -------------------------------------------------------------------------

  // To help track offsets into the message:
  // The location of the 'random' field.
  protected static final int RANDOM_OFFSET = 2;
  // The location of the sesion_id length.
  protected static final int SESSID_OFFSET = 32 + RANDOM_OFFSET;
  // The location of the session_id bytes (if any).
  protected static final int SESSID_OFFSET2 = SESSID_OFFSET + 1;

  protected ChannelBuffer buffer;

  // Constructor.
  // -------------------------------------------------------------------------

  public ClientHello (final ChannelBuffer buffer)
  {
	  this.buffer = buffer;
  }

  // Instance methods.
  // -------------------------------------------------------------------------

  /**
   * Gets the protocol version field.
   *
   * @return The protocol version field.
   */
  public ProtocolVersion version()
  {
    return ProtocolVersion.getInstance (buffer.getShort (0));
  }

  /**
   * Gets the SSL nonce.
   *
   * @return The nonce.
   */
  public byte[] random()
  {
	  byte[] ret = new byte[SESSID_OFFSET - RANDOM_OFFSET];
	  buffer.getBytes(RANDOM_OFFSET, ret);
	  return ret;
  }

  public byte[] sessionId()
  {
	  int idlen = buffer.getByte(SESSID_OFFSET) & 0xFF;
	  byte[] sessionId = new byte[idlen];
	  buffer.getBytes(SESSID_OFFSET2, sessionId);
	  return sessionId;
  }
}
