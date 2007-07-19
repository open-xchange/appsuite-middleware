package com.openexchange.admin.console.util;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.console.AdminParser.MissingOptionException;
import com.openexchange.admin.console.CmdLineParser.IllegalOptionValueException;
import com.openexchange.admin.console.CmdLineParser.UnknownOptionException;
import com.openexchange.admin.rmi.OXUtilInterface;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.Database;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.StorageException;

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

            final Credentials auth = new Credentials((String) parser.getOptionValue(this.adminUserOption), (String) parser.getOptionValue(this.adminPassOption));

            // get rmi ref
            final OXUtilInterface oxutil = (OXUtilInterface) Naming.lookup(RMI_HOSTNAME + OXUtilInterface.RMI_NAME);

            final Database db = new Database();

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

            // NEEDED ID
            db.setId(Integer.parseInt((String) parser.getOptionValue(this.databaseIdOption)));
            oxutil.changeDatabase(db, auth);
            System.out.println(SUCCESSFULLY_CHANGED);
            sysexit(0);
        } catch (final java.rmi.ConnectException neti) {
            printError(neti.getMessage());
            sysexit(SYSEXIT_COMMUNICATION_ERROR);
        } catch (final java.lang.NumberFormatException num) {
            printInvalidInputMsg("Ids must be numbers!");
            sysexit(1);
        } catch (final MalformedURLException e) {
            printServerException(e);
            sysexit(1);
        } catch (final RemoteException e) {
            printServerException(e);
            sysexit(SYSEXIT_REMOTE_ERROR);
        } catch (final NotBoundException e) {
            printNotBoundResponse(e);
            sysexit(1);
        } catch (final StorageException e) {
            printServerException(e);
            sysexit(SYSEXIT_SERVERSTORAGE_ERROR);
        } catch (final InvalidCredentialsException e) {
            printServerException(e);
            sysexit(SYSEXIT_INVALID_CREDENTIALS);
        } catch (final InvalidDataException e) {
            printServerException(e);
            sysexit(SYSEXIT_INVALID_DATA);
        } catch (final IllegalOptionValueException e) {
            printError("Illegal option value : " + e.getMessage());
            parser.printUsage();
            sysexit(SYSEXIT_ILLEGAL_OPTION_VALUE);
        } catch (final UnknownOptionException e) {
            printError("Unrecognized options on the command line: " + e.getMessage());
            parser.printUsage();
            sysexit(SYSEXIT_UNKNOWN_OPTION);
        } catch (final MissingOptionException e) {
            printError(e.getMessage());
            parser.printUsage();
            sysexit(SYSEXIT_MISSING_OPTION);
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
