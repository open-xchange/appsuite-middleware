package com.openexchange.admin.console.context;

import java.rmi.Naming;

import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.console.AdminParser.NeededQuadState;
import com.openexchange.admin.rmi.OXContextInterface;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.Filestore;

public class MoveContextFilestore extends ContextAbstraction {

    private final ContextHostingAbstraction ctxabs = new ContextHostingAbstraction();
    
    public MoveContextFilestore(final String[] args2) {

        final AdminParser parser = new AdminParser("movecontextfilestore");
        setOptions(parser);

        String successtext = null;
        try {
            parser.ownparse(args2);
            final Context ctx = contextparsing(parser);
            parseAndSetContextName(parser, ctx);
            
            successtext = nameOrIdSetInt(this.ctxid, this.contextname, "context");
            
            final Credentials auth = credentialsparsing(parser);

            // get rmi ref
            final OXContextInterface oxres = (OXContextInterface) Naming.lookup(RMI_HOSTNAME +OXContextInterface.RMI_NAME);

            final Filestore fs = ctxabs.parseAndSetFilestoreId(parser);
            
            /*final MaintenanceReason mr = new MaintenanceReason(Integer.parseInt((String) parser.getOptionValue(this.maintenanceReasonIDOption)));

            oxres.moveContextFilestore(ctx, fs, mr, auth);*/
            final int jobId = oxres.moveContextFilestore(ctx, fs, auth);

            ctxabs.displayMovedMessage(successtext, null, "to filestore " + ctxabs.getFilestoreid() + " scheduled as job " + jobId, parser);
            sysexit(0);
        } catch (final Exception e) {
            // In this special case the second parameter is not the context id but the filestore id
            // this also applies to all following error outputting methods
            // see com.openexchange.admin.console.context.ContextHostingAbstraction.printFirstPartOfErrorText(Integer, Integer)
            printErrors(successtext, ctxabs.getFilestoreid(), e, parser);
        }
    }

    public static void main(final String args[]) {
        new MoveContextFilestore(args);
    }

    private void setOptions(final AdminParser parser) {
        setDefaultCommandLineOptionsWithoutContextID(parser);
        setContextOption(parser, NeededQuadState.eitheror);
        setContextNameOption(parser, NeededQuadState.eitheror);
        //setMaintenanceReasodIDOption(parser, true);
        ctxabs.setFilestoreIdOption(parser);
    }
}
