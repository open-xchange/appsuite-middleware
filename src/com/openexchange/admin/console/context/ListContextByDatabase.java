package com.openexchange.admin.console.context;

import java.rmi.Naming;

import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.console.AdminParser.NeededQuadState;
import com.openexchange.admin.rmi.OXContextInterface;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.Database;

public class ListContextByDatabase extends ContextAbstraction {

    private final ContextHostingAbstraction ctxabs = new ContextHostingAbstraction();
    
    protected void setOptions(final AdminParser parser) {
        setDefaultCommandLineOptionsWithoutContextID(parser);
        ctxabs.setDatabaseIDOption(parser);
        ctxabs.setDatabaseNameOption(parser, NeededQuadState.eitheror);
        
        setCSVOutputOption(parser);
    }

    public ListContextByDatabase(final String[] args2) {

        final AdminParser parser = new AdminParser("listcontext");

        setOptions(parser);
        
        String successtext = null;
        try {
            parser.ownparse(args2);

            final Database db = new Database();

            ctxabs.parseAndSetDatabaseID(parser, db);
            ctxabs.parseAndSetDatabasename(parser, db);

            successtext = nameOrIdSetInt(db.getId(), db.getName(), "database");

            final Credentials auth = credentialsparsing(parser);

            final OXContextInterface oxctx = (OXContextInterface) Naming.lookup(RMI_HOSTNAME + OXContextInterface.RMI_NAME);
            final Context[] ctxs = oxctx.listByDatabase(db, auth);

            if (null != parser.getOptionValue(this.csvOutputOption)) {
                precsvinfos(ctxs);
            } else {
                sysoutOutput(ctxs);
            }

            sysexit(0);
        } catch (final Exception e) {
            printErrors(successtext, null, e, parser);
        }
    }

    public static void main(final String args[]) {
        new ListContextByDatabase(args);
    }
    
    @Override
    protected final String getObjectName() {
        return "contexts for database";
    }
}
