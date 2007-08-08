
package com.openexchange.admin.console.user;

import java.rmi.RemoteException;

import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.rmi.OXUserInterface;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.rmi.dataobjects.UserModuleAccess;
import com.openexchange.admin.rmi.exceptions.DatabaseUpdateException;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.NoSuchContextException;
import com.openexchange.admin.rmi.exceptions.StorageException;

public class Create extends CreateCore {

    public static void main(final String[] args) {
        new Create(args);
    }

    public Create(final String[] args2) {
        final AdminParser parser = new AdminParser("createuser");

        commonfunctions(parser, args2);
    }

    @Override
    protected void maincall(final AdminParser parser, final OXUserInterface oxusr, final Context ctx, final User usr, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, DatabaseUpdateException {
        final UserModuleAccess access = new UserModuleAccess();
        // webmail package access per default
        access.disableAll();
        access.setWebmail(true);
        access.setContacts(true);
        // set module access rights
        setModuleAccessOptionsinUserChange(parser, access);
        
        final Integer id = oxusr.create(ctx, usr, access, auth).getId();
        displayCreatedMessage(String.valueOf(id), ctx.getId(), parser);
    }

    @Override
    protected void setFurtherOptions(final AdminParser parser) {
    }
}
