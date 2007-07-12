
package com.openexchange.admin.console.resource;

import java.rmi.RemoteException;

import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.rmi.OXResourceInterface;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.Resource;

public class Create extends CreateCore {

    public static void main(final String[] args) {
        new Create(args);
    }

    public Create(final String[] args2) {

        final AdminParser parser = new AdminParser("createresource");
        
        commonfunctions(parser, args2);
    }

    @Override
    protected void maincall(AdminParser parser, OXResourceInterface oxres, Context ctx, Resource res, Credentials auth) throws RemoteException {
        // Nothing to do here
    }

    @Override
    protected void setFurtherOptions(AdminParser parser) {
        // Nothing to do here
    }

}