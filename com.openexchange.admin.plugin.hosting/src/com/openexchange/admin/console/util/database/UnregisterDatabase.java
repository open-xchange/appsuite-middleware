package com.openexchange.admin.console.util.database;

import java.rmi.Naming;

import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.console.AdminParser.NeededQuadState;
import com.openexchange.admin.rmi.OXUtilInterface;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.Database;

/**
 * 
 * @author d7,cutmasta
 *
 */
public class UnregisterDatabase extends DatabaseAbstraction {
    public UnregisterDatabase(final String[] args2) {

        final AdminParser parser = new AdminParser("unregisterdatabase");

        setOptions(parser);

        String successtext = null;
        try {
            parser.ownparse(args2);

            final Database db = new Database();
            
            parseAndSetDatabaseID(parser, db);
            parseAndSetDatabasename(parser, db);
            
            successtext = databasenameOrIdSet();
            
            final Credentials auth = credentialsparsing(parser);
            
            // get rmi ref
            final OXUtilInterface oxutil = (OXUtilInterface) Naming.lookup(RMI_HOSTNAME +OXUtilInterface.RMI_NAME);

            oxutil.unregisterDatabase(db, auth);
            
            displayUnregisteredMessage(successtext);
            sysexit(0);
        } catch (final Exception e) {
            printErrors(successtext, null, e, parser);
        }
    }

    public static void main(final String args[]) {
        new UnregisterDatabase(args);
    }

    private void setOptions(final AdminParser parser) {
        setDefaultCommandLineOptionsWithoutContextID(parser);

        setDatabaseIDOption(parser);
        setDatabaseNameOption(parser, NeededQuadState.eitheror);
    }
}
