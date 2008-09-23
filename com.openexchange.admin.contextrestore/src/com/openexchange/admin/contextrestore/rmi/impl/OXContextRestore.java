package com.openexchange.admin.contextrestore.rmi.impl;

import java.rmi.RemoteException;

import com.openexchange.admin.contextrestore.rmi.OXContextRestoreInterface;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;

/**
 * This class contains the implementation of the API defined in {@link OXContextRestoreInterface}
 * 
 * @author <a href="mailto:dennis.sieben@open-xchange.com">Dennis Sieben</a>
 *
 */
public class OXContextRestore implements OXContextRestoreInterface {

    public void restore(Context ctx, String[] filenames, Credentials auth) throws RemoteException {
        // TODO Auto-generated method stub

    }

}
