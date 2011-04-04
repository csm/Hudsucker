/* ServerHello.java -- SSL ServerHello message.
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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * The server hello message.
 *
 * <pre>
struct
{
  ProtocolVersion server_version;
  Random random;
  SessionID session_id;
  CipherSuite cipher_suite;
  CompressionMethod compression_method;
  Extensions server_hello_extension_list&lt;0..2^16-1&gt;
} ServerHello;
</pre>
 *
 * <p>Server hello messages may contain extra data after the
 * <tt>compression_method</tt> field, which are interpreted as
 * extensions to the basic handshake.
 */
public class ServerHello implements Handshake.Body
{

  // Fields.
  // -------------------------------------------------------------------------

  protected static final int RANDOM_OFFSET = 2;
  protected static final int SESSID_OFFSET = 32 + RANDOM_OFFSET;
  protected static final int SESSID_OFFSET2 = SESSID_OFFSET + 1;

  protected ByteBuffer buffer;

  // Constructor.
  // -------------------------------------------------------------------------

  public ServerHello (final ByteBuffer buffer)
  {
    this.buffer = buffer.duplicate().order(ByteOrder.BIG_ENDIAN);
  }

  /**
   * Returns the server's protocol version. This will read two bytes
   * from the beginning of the underlying buffer, and return an
   * instance of the appropriate {@link ProtocolVersion}; if the
   * version read is a supported version, this method returns a static
   * constant instance.
   *
   * @return The server's protocol version.
   */
  public ProtocolVersion version()
  {
    return ProtocolVersion.getInstance (buffer.getShort (0));
  }

  /**
   * Returns the server's random value. This method returns a
   * lightwieght wrapper around the existing bytes; modifications to
   * the underlying buffer will modify the returned object, and
   * vice-versa.
   *
   * @return The server's random value.
   */
  public byte[] random()
  {
    ByteBuffer randomBuf =
      ((ByteBuffer) buffer.duplicate ().position (RANDOM_OFFSET)
       .limit (SESSID_OFFSET)).slice ();
    return randomBuf.array();
  }

  /**
   * Returns the session ID. This method returns a new byte array with
   * the session ID bytes.
   *
   * @return The session ID.
   */
  public byte[] sessionId()
  {
    int idlen = buffer.get (SESSID_OFFSET) & 0xFF;
    byte[] sessionId = new byte[idlen];
    buffer.position (SESSID_OFFSET2);
    buffer.get (sessionId);
    return sessionId;
  }
}
