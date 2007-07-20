package com.openexchange.admin.console.context;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.rmi.OXContextInterface;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.StorageException;

public class List extends ListCore {

    public List(final String[] args2) {

        final AdminParser parser = new AdminParser("listcontext");

        commonfunctions(parser, args2);
    }

    public static void main(final String args[]) {
        new List(args);
    }

    @Override
    protected Context[] maincall(AdminParser parser, String search_pattern, Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException, MalformedURLException, NotBoundException {
        final OXContextInterface oxctx = (OXContextInterface) Naming.lookup(RMI_HOSTNAME +OXContextInterface.RMI_NAME);
        return oxctx.list(search_pattern, auth);
    }

    @Override
    protected void setFurtherOptions(AdminParser parser) {
        setSearchPatternOption(parser);
    }

    @Override
    protected String getSearchPattern(AdminParser parser) {
        String pattern = (String) parser.getOptionValue(this.searchOption);

        if (null == pattern) {
            pattern = "*";
        }
        return pattern;
    }
}
