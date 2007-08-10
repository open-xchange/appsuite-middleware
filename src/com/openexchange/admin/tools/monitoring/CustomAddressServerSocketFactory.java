
package com.openexchange.admin.tools.monitoring;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.rmi.server.RMIServerSocketFactory;

/**
 * @author cutmasta
 *
 */
public class CustomAddressServerSocketFactory implements RMIServerSocketFactory {
    private final InetAddress address;
    /**
     * 
     */
    public CustomAddressServerSocketFactory(InetAddress addr) {
        this.address = addr;
    }

    
    public ServerSocket createServerSocket(int port) throws IOException {
        return new ServerSocket(port, 0, address);
    }

}
