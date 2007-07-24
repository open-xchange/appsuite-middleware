
package com.openexchange.admin.console.group;

import java.rmi.RemoteException;

import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.rmi.OXGroupInterface;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.Group;

public class Change extends ChangeCore {

    public static void main(final String[] args) {
        new Change(args);
    }

    public Change(final String[] args2) {

        final AdminParser parser = new AdminParser("changegroup");
        
        commonfunctions(parser, args2);
    }

    @Override
    protected void maincall(AdminParser parser, OXGroupInterface oxgrp, Context ctx, Group grp, Credentials auth) throws RemoteException {
        // Nothing to do here
    }

    @Override
    protected void setFurtherOptions(AdminParser parser) {
        // Nothing to do here
    }
}
