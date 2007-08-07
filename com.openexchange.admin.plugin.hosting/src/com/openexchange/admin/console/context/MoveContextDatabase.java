package com.openexchange.admin.console.context;

import java.rmi.Naming;

import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.console.AdminParser.NeededQuadState;
import com.openexchange.admin.rmi.OXContextInterface;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.Database;

public class MoveContextDatabase extends ContextHostingAbstraction {

    public MoveContextDatabase(final String[] args2) {

        final AdminParser parser = new AdminParser("movecontextdatabase");
        setOptions(parser);

        String successcontext = null;
        try {
            parser.ownparse(args2);
            final Context ctx = contextparsing(parser);
            parseAndSetContextName(parser, ctx);
            
            successcontext = nameOrIdSetInt(this.ctxid, this.contextname, "context");
            
            final Credentials auth = credentialsparsing(parser);

            // get rmi ref
            final OXContextInterface oxres = (OXContextInterface) Naming.lookup(RMI_HOSTNAME +OXContextInterface.RMI_NAME);

            final Database db = new Database();
            parseAndSetDatabaseID(parser, db);
            parseAndSetDatabasename(parser, db);

            /*final MaintenanceReason mr = new MaintenanceReason(Integer.parseInt((String) parser.getOptionValue(this.maintenanceReasonIDOption)));

            oxres.moveContextDatabase(ctx, db, mr, auth);*/
            final int jobId = oxres.moveContextDatabase(ctx, db, auth);

            displayMovedMessage(successcontext, null, "to database " + dbid + " scheduled as job " + jobId);
            sysexit(0);
        } catch (final Exception e) {
            // In this special case the second parameter is not the context id but the database id
            // this also applies to all following error outputting methods
            // see com.openexchange.admin.console.context.ContextHostingAbstraction.printFirstPartOfErrorText(Integer, Integer)
            printErrors(successcontext, dbid, e, parser);
        }
    }

    public static void main(final String args[]) {
        new MoveContextDatabase(args);
    }

    private void setOptions(final AdminParser parser) {
        setDefaultCommandLineOptionsWithoutContextID(parser);
        setContextOption(parser, NeededQuadState.eitheror);
        setContextNameOption(parser, NeededQuadState.eitheror);
        //setMaintenanceReasodIDOption(parser, true);

        setDatabaseIDOption(parser);
        setDatabaseNameOption(parser, NeededQuadState.eitheror);
    }

    @Override
    protected String getObjectName() {
        return "move context";
    }
}
