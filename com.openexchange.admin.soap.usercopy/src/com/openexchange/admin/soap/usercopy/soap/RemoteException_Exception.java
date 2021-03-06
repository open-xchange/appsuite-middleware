
package com.openexchange.admin.soap.usercopy.soap;

import javax.xml.ws.WebFault;


/**
 * This class was generated by Apache CXF 2.6.0
 * 2012-06-06T08:52:52.848+02:00
 * Generated source version: 2.6.0
 */

@WebFault(name = "RemoteException", targetNamespace = "http://soap.copy.user.admin.openexchange.com")
public class RemoteException_Exception extends java.lang.Exception {

    private com.openexchange.admin.soap.usercopy.soap.RemoteException remoteException;

    public RemoteException_Exception() {
        super();
    }

    public RemoteException_Exception(String message) {
        super(message);
    }

    public RemoteException_Exception(String message, Throwable cause) {
        super(message, cause);
    }

    public RemoteException_Exception(String message, com.openexchange.admin.soap.usercopy.soap.RemoteException remoteException) {
        super(message);
        this.remoteException = remoteException;
    }

    public RemoteException_Exception(String message, com.openexchange.admin.soap.usercopy.soap.RemoteException remoteException, Throwable cause) {
        super(message, cause);
        this.remoteException = remoteException;
    }

    public com.openexchange.admin.soap.usercopy.soap.RemoteException getFaultInfo() {
        return this.remoteException;
    }
}
