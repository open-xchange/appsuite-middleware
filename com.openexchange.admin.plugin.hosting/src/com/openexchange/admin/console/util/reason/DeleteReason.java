package com.openexchange.admin.console.util.reason;

import java.rmi.Naming;

import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.console.AdminParser.NeededTriState;
import com.openexchange.admin.console.CmdLineParser.Option;
import com.openexchange.admin.rmi.OXUtilInterface;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.MaintenanceReason;

/**
 * 
 * @author d7,cutmasta
 * 
 */
public class DeleteReason extends ReasonAbstraction {

    private final static char OPT_NAME_REASON_ID_SHORT = 'i';

    private final static String OPT_NAME_REASON_ID_LONG = "reasonid";

    private Option reasonIDOption = null;

    public DeleteReason(final String[] args2) {
    
        final AdminParser parser = new AdminParser("deletereason");
    
        setOptions(parser);
    
        Integer reason_id = null;
        try {
            parser.ownparse(args2);
    
            final Credentials auth = new Credentials((String) parser.getOptionValue(this.adminUserOption), (String) parser.getOptionValue(this.adminPassOption));
    
            // get rmi ref
            final OXUtilInterface oxutil = (OXUtilInterface) Naming.lookup(RMI_HOSTNAME +OXUtilInterface.RMI_NAME);
    
            reason_id = Integer.parseInt((String) parser.getOptionValue(this.reasonIDOption));
            MaintenanceReason[] mrs = new MaintenanceReason[1];
            mrs[0] = new MaintenanceReason();
            mrs[0].setId(reason_id);
            oxutil.deleteMaintenanceReason(mrs, auth);
            
            displayDeletedMessage(reason_id, null);
            sysexit(0);
        } catch (final Exception e) {
            printErrors(reason_id, null, e, parser);
        }
    }

    public static void main(final String args[]) {
        new DeleteReason(args);
    }

    private void setOptions(final AdminParser parser) {
        setDefaultCommandLineOptionsWithoutContextID(parser);

        this.reasonIDOption = setShortLongOpt(parser, OPT_NAME_REASON_ID_SHORT, OPT_NAME_REASON_ID_LONG, "the id for the reason to be deleted", true, NeededTriState.needed);
    }
}
