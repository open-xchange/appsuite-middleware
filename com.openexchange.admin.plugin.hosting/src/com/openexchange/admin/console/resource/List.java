
package com.openexchange.admin.console.resource;

import java.rmi.RemoteException;
import java.util.ArrayList;

import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.rmi.OXResourceInterface;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.Resource;
import com.openexchange.admin.rmi.exceptions.DatabaseUpdateException;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.NoSuchContextException;
import com.openexchange.admin.rmi.exceptions.NoSuchResourceException;
import com.openexchange.admin.rmi.exceptions.StorageException;

public class List extends ListCore {

    public static void main(final String[] args) {
        new List(args);
    }

    public List(final String[] args2) {

        final AdminParser parser = new AdminParser("listresource");

        commonfunctions(parser, args2);
    }

    @Override
    protected void extendscvscolumns(final ArrayList<String> columns) {
        // Nothing to do here
    }
    
    @Override
    protected void extendmakeCSVData(final Resource my_res, final ArrayList<String> res_data) {
        // Nothing to do here
    }

    @Override
    protected void maincall(final AdminParser parser, final OXResourceInterface oxres, final Context ctx, final ArrayList<Resource> reslist, final Resource[] allres, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, DatabaseUpdateException, NoSuchResourceException {
        for (final Resource da_res : allres) {
            reslist.add(oxres.getData(ctx, da_res, auth));
        }
    }
}
