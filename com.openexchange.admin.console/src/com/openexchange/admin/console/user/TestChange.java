package com.openexchange.admin.console.user;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.HashSet;
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

public class TestChange {

    /**
     * @param args
     * @throws NotBoundException
     * @throws RemoteException
     * @throws MalformedURLException
     * @throws NoSuchUserException
     * @throws DatabaseUpdateException
     * @throws InvalidDataException
     * @throws NoSuchContextException
     * @throws InvalidCredentialsException
     * @throws StorageException
     */
    public static void main(String[] args) throws MalformedURLException, RemoteException, NotBoundException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, DatabaseUpdateException, NoSuchUserException {
        final User user = new User();
        user.setName("marcus.klein");
        user.setDisplay_name("Marcus Klein");
        user.setEmail1("marcus.klein@premium");
        user.setPrimaryEmail("marcus.klein@premium");
        user.setDefaultSenderAddress("marcus.klein@premium");
        final HashSet<String> aliases = new HashSet<String>();
        aliases.add("marcus.klein@premium");
        user.setAliases(aliases);
        user.setId(4);
        final Context ctx = new Context();
        ctx.setId(424242669);
        final Credentials creds = new Credentials();
        creds.setLogin("oxadmin");
        creds.setPassword("secret");
        OXUserInterface oxu = (OXUserInterface) Naming.lookup("rmi://localhost:1099/" + OXUserInterface.RMI_NAME);
        oxu.change(ctx, user, creds);
    }

}
