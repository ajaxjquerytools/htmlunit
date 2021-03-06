/*
 * Copyright (c) 2002-2011 Gargoyle Software Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.gargoylesoftware.htmlunit;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.net.SocketTimeoutException;

import org.apache.http.HttpHost;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.scheme.SocketFactory;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

/**
 * SOCKS {@link SocksSocketFactory}.
 *
 * @version $Revision: 6204 $
 * @author Ahmed Ashour
 */
class SocksSocketFactory implements SocketFactory {

    private HttpHost socksProxy_;

    void setSocksProxy(final HttpHost socksProxy) {
        socksProxy_ = socksProxy;
    }

    /**
     * {@inheritDoc}
     */
    public Socket createSocket() {
        if (socksProxy_ != null) {
            final InetSocketAddress address = new InetSocketAddress(socksProxy_.getHostName(), socksProxy_.getPort());
            final Proxy proxy = new Proxy(Proxy.Type.SOCKS, address);
            return new Socket(proxy);
        }
        else {
            return new Socket();
        }
    }

    /**
     * {@inheritDoc}
     */
    public Socket connectSocket(Socket sock, final String host, final int port, final InetAddress localAddress,
            int localPort, final HttpParams params)
        throws IOException {

        if (host == null) {
            throw new IllegalArgumentException("Target host may not be null.");
        }
        if (params == null) {
            throw new IllegalArgumentException("Parameters may not be null.");
        }

        if (sock == null) {
            sock = createSocket();
        }

        if (localAddress != null || localPort > 0) {
            if (localPort < 0) {
                localPort = 0; // indicates "any"
            }

            sock.bind(new InetSocketAddress(localAddress, localPort));
        }

        final int timeout = HttpConnectionParams.getConnectionTimeout(params);

        final InetSocketAddress remoteAddress = new InetSocketAddress(host, port);
        try {
            sock.connect(remoteAddress, timeout);
        }
        catch (final SocketTimeoutException ex) {
            throw new ConnectTimeoutException("Connect to " + remoteAddress + " timed out");
        }
        return sock;
    }

    /**
     * Checks whether a socket connection is secure.
     * This factory creates plain socket connections which are not considered secure.
     *
     * @param sock the connected socket
     * @return <code>false</code>
     * @throws IllegalArgumentException if the argument is invalid
     */
    public final boolean isSecure(final Socket sock) throws IllegalArgumentException {
        if (sock == null) {
            throw new IllegalArgumentException("Socket may not be null.");
        }
        // This check is performed last since it calls a method implemented
        // by the argument object. getClass() is final in java.lang.Object.
        if (sock.isClosed()) {
            throw new IllegalArgumentException("Socket is closed.");
        }
        return false;
    }

}
