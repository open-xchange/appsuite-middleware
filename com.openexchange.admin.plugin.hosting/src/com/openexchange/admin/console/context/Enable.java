package com.openexchange.admin.console.context;

import java.rmi.Naming;

import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.rmi.OXContextInterface;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;

public class Enable extends ContextHostingAbstraction {

    public Enable(final String[] args2) {

        final AdminParser parser = new AdminParser("enablecontext");

        setOptions(parser);

        try {
            parser.ownparse(args2);
            final Context ctx = contextparsing(parser);
            
            final Credentials auth = credentialsparsing(parser);

            // get rmi ref
            final OXContextInterface oxres = (OXContextInterface) Naming.lookup(RMI_HOSTNAME +OXContextInterface.RMI_NAME);

            oxres.enable(ctx, auth);

            displayEnabledMessage(ctxid, null);
            sysexit(0);
        } catch (final Exception e) {
            printErrors(String.valueOf(ctxid), null, e, parser);
        }

    }

    public static void main(final String args[]) {
        new Enable(args);
    }

    private void setOptions(final AdminParser parser) {
        setDefaultCommandLineOptions(parser);
    }
}
