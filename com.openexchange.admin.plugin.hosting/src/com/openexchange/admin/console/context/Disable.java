package com.openexchange.admin.console.context;

import java.rmi.Naming;

import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.rmi.OXContextInterface;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;

public class Disable extends ContextHostingAbstraction {

    public Disable(final String[] args2) {

        final AdminParser parser = new AdminParser("disablecontext");

        setOptions(parser);

        try {

            parser.ownparse(args2);
            final Context ctx = contextparsing(parser);

            final Credentials auth = credentialsparsing(parser);

            // get rmi ref
            final OXContextInterface oxres = (OXContextInterface) Naming.lookup(RMI_HOSTNAME +OXContextInterface.RMI_NAME);

            /*final MaintenanceReason mr = new MaintenanceReason(Integer.parseInt((String) parser.getOptionValue(this.maintenanceReasonIDOption)));
            oxres.disable(ctx, mr, auth); */
            oxres.disable(ctx, auth);

            displayDisabledMessage(ctxid, null);
            sysexit(0);
        } catch (final Exception e) {
            printErrors(ctxid, null, e, parser);
        }
    }

    public static void main(final String args[]) {
        new Disable(args);
    }

    private void setOptions(final AdminParser parser) {
        setDefaultCommandLineOptions(parser);
        //setMaintenanceReasodIDOption(parser, true);
    }
}
