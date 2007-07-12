
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
        final AdminParser parser = new AdminParser("create");

        commonfunctions(parser, args2);
    }

    @Override
    protected void maincall(final AdminParser parser, final OXUserInterface oxusr, final Context ctx, final User usr, final UserModuleAccess access, final Credentials auth) throws RemoteException {
        try {
            System.out.println(oxusr.create(ctx, usr, access, auth).getId());
        } catch (final StorageException e) {
            printServerException(e);
            sysexit(SYSEXIT_SERVERSTORAGE_ERROR);
        } catch (final InvalidCredentialsException e) {
            printServerException(e);
            sysexit(SYSEXIT_INVALID_CREDENTIALS);
        } catch (final NoSuchContextException e) {
            printServerException(e);
            sysexit(SYSEXIT_NO_SUCH_CONTEXT);
        } catch (final InvalidDataException e) {
            printServerException(e);
            sysexit(SYSEXIT_INVALID_DATA);
        } catch (final DatabaseUpdateException e) {
            printServerException(e);
            sysexit(1);
        }
    }

    @Override
    protected void setFurtherOptions(final AdminParser parser) {
    }
}
