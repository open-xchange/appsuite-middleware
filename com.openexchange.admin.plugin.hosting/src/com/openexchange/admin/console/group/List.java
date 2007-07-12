
package com.openexchange.admin.console.group;

import java.rmi.RemoteException;
import java.util.ArrayList;

import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.rmi.OXGroupInterface;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.Group;
import com.openexchange.admin.rmi.exceptions.DatabaseUpdateException;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.NoSuchContextException;
import com.openexchange.admin.rmi.exceptions.NoSuchGroupException;
import com.openexchange.admin.rmi.exceptions.StorageException;

public class List extends ListCore {

    public static void main(final String[] args) {
        new List(args);
    }

    public List(final String[] args2) {

        final AdminParser parser = new AdminParser("listgroup");

        commonfunctions(parser, args2);
    }

    @Override
    protected void extendscvscolumns(ArrayList<String> columns) {
        // Nothing to do here
    }

    @Override
    protected void extendmakeCSVData(Group group, ArrayList<String> grp_data) {
        // Nothing to do here
    }

    @Override
    protected void maincall(AdminParser parser, OXGroupInterface oxgrp, Context ctx, ArrayList<Group> grplist, Group[] allgrps, Credentials auth) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException, DatabaseUpdateException, NoSuchGroupException {
        for (final Group group : allgrps) {
            grplist.add(oxgrp.getData(ctx, group, auth));
        }
    }
}
