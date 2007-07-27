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
import com.openexchange.admin.rmi.exceptions.NoSuchContextException;
import com.openexchange.admin.rmi.exceptions.StorageException;

public class Change extends ChangeCore {

    private final ContextHostingAbstraction ctxabs = new ContextHostingAbstraction();
    
    public Change(final String[] args2) {
        final AdminParser parser = new AdminParser("changecontext");

        commonfunctions(parser, args2);
    }

    public static void main(final String args[]) {
        new Change(args);
    }

    @Override
    protected void maincall(final AdminParser parser, final Context ctx, final Credentials auth) throws MalformedURLException, RemoteException, NotBoundException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException {
        // get rmi ref
        final OXContextInterface oxctx = (OXContextInterface) Naming.lookup(RMI_HOSTNAME +OXContextInterface.RMI_NAME);

        // add login mappings
        ctxabs.parseAndSetAddLoginMapping(parser);
        
        // remove login mappings
        ctxabs.parseAndSetRemoveLoginMapping(parser);
        
        ctxabs.changeMappingSetting(oxctx, ctx, auth, true);
                    
        // do the change
        oxctx.change(ctx, auth);
    }

    @Override
    protected void setFurtherOptions(final AdminParser parser) {
        ctxabs.setAddMappingOption(parser, false);
        
        ctxabs.setRemoveMappingOption(parser, false);
    }
}
