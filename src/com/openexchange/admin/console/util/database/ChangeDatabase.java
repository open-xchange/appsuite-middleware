package com.openexchange.admin.console.util.database;

import java.rmi.Naming;

import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.rmi.OXUtilInterface;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.Database;

/**
 * 
 * @author d7,cutmasta
 * 
 */
public class ChangeDatabase extends DatabaseAbstraction {

    public ChangeDatabase(final String[] args2) {

        final AdminParser parser = new AdminParser("changedatabase");

        setOptions(parser);

        try {
            parser.ownparse(args2);

            final Database db = new Database();

            parseAndSetDatabaseID(parser, db);
            
            final Credentials auth = new Credentials((String) parser.getOptionValue(this.adminUserOption), (String) parser.getOptionValue(this.adminPassOption));

            // get rmi ref
            final OXUtilInterface oxutil = (OXUtilInterface) Naming.lookup(RMI_HOSTNAME + OXUtilInterface.RMI_NAME);

            parseAndSetHostname(parser, db);
            
            parseAndSetDatabasename(parser, db);

            parseAndSetDriver(parser, db);

            parseAndSetDBUsername(parser, db);
            
            parseAndSetPasswd(parser, db);

            parseAndSetMaxUnits(parser, db);

            parseAndSetPoolHardLimit(parser, db);

            parseAndSetPoolInitial(parser, db);

            parseAndSetPoolmax(parser, db);

            parseAndSetDatabaseWeight(parser, db);

//            parseAndSetMasterAndID(parser, db);

            oxutil.changeDatabase(db, auth);
            
            displayChangedMessage(String.valueOf(dbid), null);
            sysexit(0);
        } catch (final Exception e) {
            printErrors(String.valueOf(dbid), null, e, parser);
        }
    }

    public static void main(final String args[]) {
        new ChangeDatabase(args);
    }

    private void setOptions(final AdminParser parser) {
        // oxadmin,oxadmin passwd
        setDefaultCommandLineOptionsWithoutContextID(parser);

        setDatabaseNameOption(parser, false);
        setDatabaseHostnameOption(parser, false);
        setDatabaseUsernameOption(parser, false);
        setDatabaseDriverOption(parser, null, false);
        setDatabasePasswdOption(parser, false);
//        setDatabaseIsMasterOption(parser, false);
//        setDatabaseMasterIDOption(parser, false);

        setDatabaseWeightOption(parser, null, false);
        setDatabaseMaxUnitsOption(parser, null, false);
        setDatabasePoolHardlimitOption(parser, null, false);
        setDatabasePoolInitialOption(parser, null, false);
        setDatabasePoolMaxOption(parser, null, false);

        setDatabaseIDOption(parser);
    }
}
