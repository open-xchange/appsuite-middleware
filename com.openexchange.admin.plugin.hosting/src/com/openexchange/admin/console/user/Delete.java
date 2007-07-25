
package com.openexchange.admin.console.user;

import java.rmi.RemoteException;

import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.rmi.OXUserInterface;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.rmi.exceptions.DatabaseUpdateException;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.NoSuchContextException;
import com.openexchange.admin.rmi.exceptions.NoSuchUserException;
import com.openexchange.admin.rmi.exceptions.StorageException;

public class Delete extends DeleteCore {

    public static void main(final String[] args) {
        new Delete(args);
    }

    public Delete(final String[] args2) {

        final AdminParser parser = new AdminParser("deleteuser");

        commonfunctions(parser, args2);
    }

    @Override
    protected void maincall(final AdminParser parser, final OXUserInterface oxusr, final Context ctx, final User usr, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, DatabaseUpdateException, NoSuchUserException {
        oxusr.delete(ctx, usr, auth);
        displayDeletedMessage(usr.getId(), ctx.getIdAsInt());
    }

    @Override
    protected void setFurtherOptions(AdminParser parser) {
    }
}
