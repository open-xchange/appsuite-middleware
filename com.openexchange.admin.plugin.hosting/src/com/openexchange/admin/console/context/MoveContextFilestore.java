package com.openexchange.admin.console.context;

import java.rmi.Naming;

import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.console.AdminParser.NeededTriState;
import com.openexchange.admin.console.CmdLineParser.Option;
import com.openexchange.admin.rmi.OXContextInterface;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.Filestore;

public class MoveContextFilestore extends ContextHostingAbstraction {

    private final static char OPT_FILESTORE_SHORT = 'f';

    private final static String OPT_FILESTORE_LONG = "filestore";

    protected Option targetFilestoreIDOption = null;

    public MoveContextFilestore(final String[] args2) {

        final AdminParser parser = new AdminParser("movecontextfilestore");
        setOptions(parser);

        Integer filestoreid = null;
        
        try {
            parser.ownparse(args2);
            final Context ctx = contextparsing(parser);
            
            final Credentials auth = credentialsparsing(parser);

            // get rmi ref
            final OXContextInterface oxres = (OXContextInterface) Naming.lookup(RMI_HOSTNAME +OXContextInterface.RMI_NAME);

            filestoreid = Integer.parseInt((String) parser.getOptionValue(this.targetFilestoreIDOption));
            final Filestore fs = new Filestore(filestoreid);

            /*final MaintenanceReason mr = new MaintenanceReason(Integer.parseInt((String) parser.getOptionValue(this.maintenanceReasonIDOption)));

            oxres.moveContextFilestore(ctx, fs, mr, auth);*/
            final int jobId = oxres.moveContextFilestore(ctx, fs, auth);

            displayMovedMessage(ctxid, null, "to filestore " + filestoreid + " scheduled as job " + jobId);
            sysexit(0);
        } catch (final Exception e) {
            // In this special case the second parameter is not the context id but the filestore id
            // this also applies to all following error outputting methods
            // see com.openexchange.admin.console.context.ContextHostingAbstraction.printFirstPartOfErrorText(Integer, Integer)
            printErrors(String.valueOf(ctxid), filestoreid, e, parser);
        }
    }

    public static void main(final String args[]) {
        new MoveContextFilestore(args);
    }

    private void setOptions(final AdminParser parser) {
        setDefaultCommandLineOptions(parser);
        //setMaintenanceReasodIDOption(parser, true);
        this.targetFilestoreIDOption = setShortLongOpt(parser, OPT_FILESTORE_SHORT, OPT_FILESTORE_LONG, "Target filestore id", true, NeededTriState.needed);
    }
}
