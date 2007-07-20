package com.openexchange.admin.console.context;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.rmi.OXContextInterface;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.rmi.exceptions.ContextExistsException;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.StorageException;

public class Create extends CreateCore {

    public Create(final String[] args2) {

        final AdminParser parser = new AdminParser("createcontext");

        commonfunctions(parser, args2);
    }

    public static void main(final String args[]) {
        new Create(args);
    }

    @Override
    protected void setFurtherOptions(final AdminParser parser) {
        // Nothing to do here
    }

    @Override
    protected Context maincall(final AdminParser parser, final Context ctx, final User usr, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException, MalformedURLException, NotBoundException, ContextExistsException {
        // get rmi ref
        final OXContextInterface oxctx = (OXContextInterface) Naming.lookup(RMI_HOSTNAME +OXContextInterface.RMI_NAME);
        return oxctx.create(ctx, usr, auth);
    }
}
