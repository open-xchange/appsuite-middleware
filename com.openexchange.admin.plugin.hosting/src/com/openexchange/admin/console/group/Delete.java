package com.openexchange.admin.console.group;

import java.rmi.RemoteException;

import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.rmi.OXGroupInterface;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.Group;

public class Delete extends DeleteCore {

    public static void main(final String[] args) {
        new Delete(args);
    }

    public Delete(final String[] args2) {

        final AdminParser parser = new AdminParser("deletegroup");
        
        commonfunctions(parser, args2);
    }

    @Override
    protected void maincall(final AdminParser parser, final OXGroupInterface oxgrp, final Context ctx, final Group grp, final Credentials auth) throws RemoteException {
        // Nothing to do here
    }
}
