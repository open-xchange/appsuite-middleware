package com.openexchange.http.client.apache;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;

import org.apache.commons.httpclient.ConnectTimeoutException;
import org.apache.commons.httpclient.params.HttpConnectionParams;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;

import com.openexchange.tools.ssl.TrustAllSSLSocketFactory;

public class TrustAllAdapter implements ProtocolSocketFactory {
    
    private TrustAllSSLSocketFactory delegate = (TrustAllSSLSocketFactory) TrustAllSSLSocketFactory.getDefault();

    public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
        return delegate.createSocket(host, port);
    }

    public Socket createSocket(String host, int port, InetAddress localAddress, int localPort) throws IOException, UnknownHostException {
        return delegate.createSocket(host, port, localAddress, localPort);
    }

    public Socket createSocket(String host, int port, InetAddress localAddress, int localPort, HttpConnectionParams params) throws IOException, UnknownHostException, ConnectTimeoutException {
        Socket socket;
        int timeout = params.getConnectionTimeout();
        if (timeout == 0) {
            socket = createSocket(host, port, localAddress, localPort);
        } else {
            socket = delegate.createSocket();
            SocketAddress localaddr = new InetSocketAddress(localAddress, localPort);
            SocketAddress remoteaddr = new InetSocketAddress(host, port);
            socket.bind(localaddr);
            socket.connect(remoteaddr, timeout);
            return socket;
        }
        
        
        int linger = params.getLinger();
        if(linger == 0) {
            socket.setSoLinger(false, 0);
        } else if (linger > 0) {
            socket.setSoLinger(true, linger);
        }
        
        socket.setSoTimeout(params.getSoTimeout());
        socket.setTcpNoDelay(params.getTcpNoDelay());
        
        return socket;
    }
    
 
}

