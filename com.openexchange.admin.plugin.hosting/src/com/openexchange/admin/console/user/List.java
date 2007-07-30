
package com.openexchange.admin.console.user;

import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.util.ArrayList;

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

public class List extends ListCore {

    public static void main(final String[] args) {
        new List(args);
    }

    public List(final String[] args2) {

        final AdminParser parser = new AdminParser("listuser");

        commonfunctions(parser, args2);
    }

    @Override
    protected User[] maincall(final AdminParser parser, final OXUserInterface oxusr, final String search_pattern, final Context ctx, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, DatabaseUpdateException, NoSuchUserException {
        final User[] allusers = oxusr.list(ctx, search_pattern, auth);            
        if( allusers.length == 0 ) {
            User []users = new User[0];
            return users;
        } else {
            return oxusr.getData(ctx, allusers, auth);
        }
    }

    @Override
    protected void setFurtherOptions(final AdminParser parser) {
    }

    @Override
    protected ArrayList<String> getColumnsOfAllExtensions(final User user) {
        return new ArrayList<String>();
    }

    @Override
    protected ArrayList<String> getDataOfAllExtensions(final User user) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        return new ArrayList<String>();
    }
}
