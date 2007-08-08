package com.openexchange.admin.console.context;

import java.rmi.Naming;

import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.rmi.OXContextInterface;
import com.openexchange.admin.rmi.dataobjects.Credentials;

public class EnableAll extends ContextHostingAbstraction {

    public EnableAll(final String[] args2) {

        final AdminParser parser = new AdminParser("enableallcontexts");

        setDefaultCommandLineOptionsWithoutContextID(parser);
        try {
            parser.ownparse(args2);

            final Credentials auth = new Credentials((String) parser.getOptionValue(this.adminUserOption), (String) parser.getOptionValue(this.adminPassOption));

            // get rmi ref
            final OXContextInterface oxres = (OXContextInterface) Naming.lookup(RMI_HOSTNAME +OXContextInterface.RMI_NAME);

            oxres.enableAll(auth);

            displayEnabledMessage(null, null, parser);
            sysexit(0);
        } catch (final Exception e) {
            printErrors(null, null, e, parser);
        }

    }

    public static void main(final String args[]) {
        new EnableAll(args);
    }

    @Override
    protected String getObjectName() {
        return "all contexts";
    }
    
    
}
