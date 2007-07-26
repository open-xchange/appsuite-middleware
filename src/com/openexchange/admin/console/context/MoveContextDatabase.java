package com.openexchange.admin.console.context;

import java.rmi.Naming;

import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.console.AdminParser.NeededTriState;
import com.openexchange.admin.console.CmdLineParser.Option;
import com.openexchange.admin.rmi.OXContextInterface;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.Database;

public class MoveContextDatabase extends ContextHostingAbstraction {

    private final static char OPT_DATABASE_SHORT = 'd';

    private final static String OPT_DATABASE_LONG = "database";

    protected Option targetDatabaseIDOption = null;

    
    public MoveContextDatabase(final String[] args2) {

        final AdminParser parser = new AdminParser("movecontextdatabase");
        setOptions(parser);

        Integer dbid = null;
        
        try {
            parser.ownparse(args2);
            final Context ctx = contextparsing(parser);

            final Credentials auth = credentialsparsing(parser);

            // get rmi ref
            final OXContextInterface oxres = (OXContextInterface) Naming.lookup(RMI_HOSTNAME +OXContextInterface.RMI_NAME);

            dbid = Integer.parseInt((String) parser.getOptionValue(this.targetDatabaseIDOption));
            final Database db = new Database(dbid);

            /*final MaintenanceReason mr = new MaintenanceReason(Integer.parseInt((String) parser.getOptionValue(this.maintenanceReasonIDOption)));

            oxres.moveContextDatabase(ctx, db, mr, auth);*/
            final int jobId = oxres.moveContextDatabase(ctx, db, auth);

            displayMovedMessage(ctxid, null, "to database " + dbid + " scheduled as job " + jobId);
            sysexit(0);
        } catch (final Exception e) {
            // In this special case the second parameter is not the context id but the database id
            // this also applies to all following error outputting methods
            // see com.openexchange.admin.console.context.ContextHostingAbstraction.printFirstPartOfErrorText(Integer, Integer)
            printErrors(ctxid, dbid, e, parser);
        }
    }

    public static void main(final String args[]) {
        new MoveContextDatabase(args);
    }

    private void setOptions(final AdminParser parser) {
        setDefaultCommandLineOptions(parser);
        //setMaintenanceReasodIDOption(parser, true);

        this.targetDatabaseIDOption = setShortLongOpt(parser, OPT_DATABASE_SHORT, OPT_DATABASE_LONG, "Target database id", true, NeededTriState.needed);
    }

    @Override
    protected String getObjectName() {
        return "move context";
    }
}
