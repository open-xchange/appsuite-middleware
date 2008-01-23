
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
import com.openexchange.admin.rmi.exceptions.NoSuchUserException;
import com.openexchange.admin.rmi.exceptions.StorageException;

public class Change extends ChangeCore {

    private final UserHostingAbstraction usrabs = new UserHostingAbstraction();
	
    public static void main(final String[] args) {
        new Change(args);
    }

    public Change(final String[] args2) {

        final AdminParser parser = new AdminParser("changeuser");

        commonfunctions(parser, args2);
    }

    @Override
    protected void maincall(final AdminParser parser, final OXUserInterface oxusr, final Context ctx, final User usr, UserModuleAccess access, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, DatabaseUpdateException, NoSuchUserException {
        oxusr.change(ctx, usr, auth);
        
        // Change module access IF an access combination name was supplied!
        // The normal change of access rights is done in the "commonfunctions"
        final String accesscombinationname = usrabs.parseAndSetAccessCombinationName(parser);
        if (null != accesscombinationname) {
            // Change user with access rights combination name
        	oxusr.changeModuleAccess(ctx, usr, accesscombinationname, auth);        	
        }
        
    }

    @Override
    protected void setFurtherOptions(final AdminParser parser) {
        usrabs.setAddAccessRightCombinationNameOption(parser, false);
    }

}
