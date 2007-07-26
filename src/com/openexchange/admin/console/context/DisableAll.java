package com.openexchange.admin.console.context;

import java.rmi.Naming;

import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.rmi.OXContextInterface;
import com.openexchange.admin.rmi.dataobjects.Credentials;

public class DisableAll extends ContextHostingAbstraction {

    public DisableAll(final String[] args2) {

        final AdminParser parser = new AdminParser("disableallcontext");

        setOptions(parser);

        try {
            parser.ownparse(args2);

            final Credentials auth = new Credentials((String) parser.getOptionValue(this.adminUserOption), (String) parser.getOptionValue(this.adminPassOption));

            // get rmi ref
            final OXContextInterface oxres = (OXContextInterface) Naming.lookup(RMI_HOSTNAME +OXContextInterface.RMI_NAME);

            /* final MaintenanceReason mr = new MaintenanceReason(Integer.parseInt((String) parser.getOptionValue(this.maintenanceReasonIDOption)));
            oxres.disableAll(mr, auth); */
            oxres.disableAll(auth);

            displayDisabledMessage(null, null);
            sysexit(0);
        } catch (final Exception e) {
            printErrors(null, null, e, parser);
        }
    }

    public static void main(final String args[]) {
        new DisableAll(args);
    }

    private void setOptions(final AdminParser parser) {
        setDefaultCommandLineOptionsWithoutContextID(parser);
        //setMaintenanceReasodIDOption(parser, true);
    }

    @Override
    protected String getObjectName() {
        return "all contexts";
    }
}
