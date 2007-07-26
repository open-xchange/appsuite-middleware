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
public class CreateReason extends ReasonAbstraction {
    
    private final static char OPT_NAME_REASON_TEXT_SHORT = 'r';

    private final static String OPT_NAME_REASON_TEXT_LONG = "reasontext";

    private Option reasonTextOption = null;

    public CreateReason(final String[] args2) {
    
        final AdminParser parser = new AdminParser("createreason");
    
        setOptions(parser);
    
        try {
            parser.ownparse(args2);
    
            final Credentials auth = new Credentials((String)parser.getOptionValue(this.adminUserOption),(String)parser.getOptionValue(this.adminPassOption));
            
            // get rmi ref
            final OXUtilInterface oxutil = (OXUtilInterface) Naming.lookup(RMI_HOSTNAME +OXUtilInterface.RMI_NAME);
    
            final MaintenanceReason reason = new MaintenanceReason((String)parser.getOptionValue(this.reasonTextOption));
    
            displayCreatedMessage(oxutil.createMaintenanceReason(reason, auth).getId(), null);
            sysexit(0);
        } catch (final Exception e) {
            printErrors(null, null, e, parser);
        }
    
    }

    public static void main(final String args[]) {
        new CreateReason(args);
    }

    private void setOptions(final AdminParser parser) {
        
        setDefaultCommandLineOptionsWithoutContextID(parser);

        this.reasonTextOption = setShortLongOpt(parser, OPT_NAME_REASON_TEXT_SHORT,OPT_NAME_REASON_TEXT_LONG,"the text for the added reason",true, NeededTriState.needed);
                
    }
}
