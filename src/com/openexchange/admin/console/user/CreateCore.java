package com.openexchange.admin.console.user;

import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.rmi.ConnectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.console.AdminParser.MissingOptionException;
import com.openexchange.admin.console.CmdLineParser.IllegalOptionValueException;
import com.openexchange.admin.console.CmdLineParser.UnknownOptionException;
import com.openexchange.admin.rmi.OXUserInterface;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.rmi.dataobjects.UserModuleAccess;
import com.openexchange.admin.rmi.exceptions.DatabaseUpdateException;
import com.openexchange.admin.rmi.exceptions.DuplicateExtensionException;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.NoSuchContextException;
import com.openexchange.admin.rmi.exceptions.StorageException;

public abstract class CreateCore extends UserAbstraction {

    protected final void setOptions(final AdminParser parser) {
    
        setExtendedOption(parser);
        setDefaultCommandLineOptions(parser);
    
        // add mandatory options
        setMandatoryOptions(parser);
    
        // add optional opts
        setOptionalOptions(parser);
        
        setFurtherOptions(parser);
    }

    protected abstract void setFurtherOptions(final AdminParser parser);

    protected final void commonfunctions(final AdminParser parser, final String[] args) {
        // set all needed options in our parser
        setOptions(parser);

        setExtendedOptions(parser);

        // parse the command line
        try {
            parser.ownparse(args);

            printExtendedOutputIfSet(parser);
            
            final Context ctx = contextparsing(parser);

            final Credentials auth = credentialsparsing(parser);

            // get rmi ref
            final OXUserInterface oxusr = getUserInterface();

            // create user obj
            final User usr = new User();

            // fill user obj with mandatory values from console
            setMandatoryOptionsinUser(parser, usr);

            // add optional values if set
            setOptionalOptionsinUser(parser, usr);

            applyExtendedOptionsToUser(parser, usr);

            // default set all access rights
            final UserModuleAccess access = new UserModuleAccess();
            access.enableAll();

            // set module access rights
            setModuleAccessOptionsinUserCreate(parser, access);
            
            maincall(parser, oxusr, ctx, usr, access, auth);
            
            sysexit(0);
        } catch (final ConnectException neti) {
            printError(neti.getMessage());
            sysexit(SYSEXIT_COMMUNICATION_ERROR);
        } catch (final NumberFormatException num) {
            printInvalidInputMsg("Ids must be numbers!");
            sysexit(1);
        } catch (final IllegalOptionValueException e) {
            printError("Illegal option value : " + e.getMessage());
            printrightoptions(parser);
            sysexit(SYSEXIT_ILLEGAL_OPTION_VALUE);
        } catch (final UnknownOptionException e) {
            printError("Unrecognized options on the command line: " + e.getMessage());
            printrightoptions(parser);
            sysexit(SYSEXIT_UNKNOWN_OPTION);
        } catch (final MissingOptionException e) {
            printError(e.getMessage());
            printrightoptions(parser);
            sysexit(SYSEXIT_MISSING_OPTION);
        } catch (final MalformedURLException e) {
            printServerException(e);
            sysexit(1);
        } catch (final RemoteException e) {
            printServerException(e);
            sysexit(SYSEXIT_REMOTE_ERROR);
        } catch (final NotBoundException e) {
            printServerException(e);
            sysexit(1);
        } catch (final IllegalArgumentException e) {
            printError(e.getMessage());
            sysexit(1);
        } catch (final IllegalAccessException e) {
            printError(e.getMessage());
            sysexit(1);
        } catch (final InvocationTargetException e) {
            printError(e.getMessage());
            sysexit(1);
        } catch (final InvalidDataException e) {
            printServerException(e);
            sysexit(SYSEXIT_INVALID_DATA);
        } catch (final StorageException e) {
            printServerException(e);
            sysexit(1);
        } catch (final InvalidCredentialsException e) {
            printServerException(e);
            sysexit(SYSEXIT_INVALID_CREDENTIALS);
        } catch (final NoSuchContextException e) {
            printServerException(e);
            sysexit(SYSEXIT_NO_SUCH_CONTEXT);
        } catch (final DatabaseUpdateException e) {
            printServerException(e);
            sysexit(1);
        } catch (final DuplicateExtensionException e) {
            printServerException(e);
            sysexit(1);
        }
    }

    protected abstract void maincall(final AdminParser parser, final OXUserInterface oxusr, final Context ctx, final User usr, final UserModuleAccess access, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, DatabaseUpdateException, DuplicateExtensionException, MalformedURLException, NotBoundException;
}
