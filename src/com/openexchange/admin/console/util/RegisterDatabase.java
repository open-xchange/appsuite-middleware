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
            
            displayRegisteredMessage(oxutil.registerDatabase(db, auth).getId());
            sysexit(0);
        } catch (final java.rmi.ConnectException neti) {
            printError(null, null, neti.getMessage());
            sysexit(SYSEXIT_COMMUNICATION_ERROR);
        } catch (final java.lang.NumberFormatException num) {
            printInvalidInputMsg(null, null, "Ids must be numbers!");
            sysexit(1);
        } catch (final MalformedURLException e) {
            printServerException(null, null, e);
            sysexit(1);
        } catch (final RemoteException e) {
            printServerException(null, null, e);
            sysexit(SYSEXIT_REMOTE_ERROR);
        } catch (final NotBoundException e) {
            printNotBoundResponse(null, null, e);
            sysexit(1);
        } catch (final StorageException e) {
            printServerException(null, null, e);
            sysexit(SYSEXIT_SERVERSTORAGE_ERROR);
        } catch (final InvalidCredentialsException e) {
            printServerException(null, null, e);
            sysexit(SYSEXIT_INVALID_CREDENTIALS);
        } catch (final InvalidDataException e) {
            printServerException(null, null, e);
            sysexit(SYSEXIT_INVALID_DATA);
        } catch (final IllegalOptionValueException e) {
            printError(null, null, "Illegal option value : " + e.getMessage());
            parser.printUsage();
            sysexit(SYSEXIT_ILLEGAL_OPTION_VALUE);
        } catch (final UnknownOptionException e) {
            printError(null, null, "Unrecognized options on the command line: " + e.getMessage());
            parser.printUsage();
            sysexit(SYSEXIT_UNKNOWN_OPTION);
        } catch (final MissingOptionException e) {
            printError(null, null, e.getMessage());
            parser.printUsage();
            sysexit(SYSEXIT_MISSING_OPTION);
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
