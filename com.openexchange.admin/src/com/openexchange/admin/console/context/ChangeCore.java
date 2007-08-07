package com.openexchange.admin.console.context;

import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.console.AdminParser.NeededQuadState;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.NoSuchContextException;
import com.openexchange.admin.rmi.exceptions.StorageException;

public abstract class ChangeCore extends ContextAbstraction {
    
    protected void setOptions(final AdminParser parser) {
        setDefaultCommandLineOptionsWithoutContextID(parser);
        
        setContextOption(parser, NeededQuadState.eitheror);
        setContextNameOption(parser, NeededQuadState.eitheror);

        setContextQuotaOption(parser, false);
        
        setFurtherOptions(parser);
    }
    
    protected final void commonfunctions(final AdminParser parser, final String[] args) {
        setOptions(parser);
        
        String successtext = null;
        try {
            parser.ownparse(args);
            final Context ctx = contextparsing(parser);

            // context name
            parseAndSetContextName(parser, ctx);
            
            successtext = nameOrIdSetInt(this.ctxid, this.contextname, "context");
            
            // context filestore quota
            parseAndSetContextQuota(parser, ctx);
            
            final Credentials auth = credentialsparsing(parser);

            maincall(parser, ctx, auth);

            displayChangedMessage(successtext, null);
            sysexit(0);
        } catch (final Exception e) {
            printErrors(successtext, null, e, parser);
        }
    }

    protected abstract void maincall(final AdminParser parser, final Context ctx, final Credentials auth) throws MalformedURLException, RemoteException, NotBoundException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException;
        
    protected abstract void setFurtherOptions(final AdminParser parser);
}
