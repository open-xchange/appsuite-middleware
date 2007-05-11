package com.openexchange.admin.rmi;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import org.junit.Test;

public class AdminCoreTest extends AbstractTest {

    @Test
    public void testAllPluginsLoaded() throws MalformedURLException, RemoteException, NotBoundException {
        final OXAdminCoreInterface oxadmincore = (OXAdminCoreInterface) Naming.lookup(getRMIHostUrl()+ OXAdminCoreInterface.RMI_NAME);
        System.out.println(oxadmincore.allPluginsLoaded());
    }

}
