package com.openexchange.admin.console.util.database;

import java.rmi.Naming;

import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.rmi.OXUtilInterface;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.Database;
import com.openexchange.admin.rmi.impl.OXUtil;

/**
 * 
 * @author d7,cutmasta
 * 
 */
public class RegisterDatabase extends DatabaseAbstraction {

    public RegisterDatabase(final String[] args2) {

        final AdminParser parser = new AdminParser("registerdatabase");

        setOptions(parser);

        try {
            parser.ownparse(args2);

            final Credentials auth = credentialsparsing(parser);

            // get rmi ref
            final OXUtilInterface oxutil = (OXUtilInterface) Naming.lookup(RMI_HOSTNAME + OXUtilInterface.RMI_NAME);

            final Database db = new Database();

            parseAndSetHostname(parser, db);

            parseAndSetDatabasename(parser, db);
            
            parseAndSetPasswd(parser, db);
            
            parseAndSetDriver(parser, db);
            
            parseAndSetDBUsername(parser, db);
            
            parseAndSetMaxUnits(parser, db);
            
            parseAndSetPoolHardLimit(parser, db);
            
            parseAndSetPoolInitial(parser, db);
            
            parseAndSetPoolmax(parser, db);
            
            parseAndSetDatabaseWeight(parser, db);
            
            parseAndSetMasterAndID(parser, db);
            
            displayRegisteredMessage(String.valueOf(oxutil.registerDatabase(db, auth).getId()));
            sysexit(0);
        } catch (final Exception e) {
            printErrors(null, null, e, parser);
        }

    }

    public static void main(final String args[]) {
        new RegisterDatabase(args);
    }

    private void setOptions(final AdminParser parser) {
        setDefaultCommandLineOptionsWithoutContextID(parser);

        setDatabaseNameOption(parser, true);
        setDatabaseHostnameOption(parser, false);
        setDatabaseUsernameOption(parser, false);
        setDatabaseDriverOption(parser, OXUtil.DEFAULT_DRIVER, false);
        setDatabasePasswdOption(parser, true);
        setDatabaseIsMasterOption(parser, true);
        setDatabaseMasterIDOption(parser, false);
        setDatabaseWeightOption(parser, String.valueOf(OXUtil.DEFAULT_DB_WEIGHT), false);
        setDatabaseMaxUnitsOption(parser, String.valueOf(OXUtil.DEFAULT_MAXUNITS), false);
        setDatabasePoolHardlimitOption(parser, String.valueOf(OXUtil.DEFAULT_POOL_HARD_LIMIT), false);
        setDatabasePoolInitialOption(parser, String.valueOf(OXUtil.DEFAULT_POOL_INITIAL), false);
        setDatabasePoolMaxOption(parser, String.valueOf(OXUtil.DEFAULT_POOL_MAX), false);
    }
}
