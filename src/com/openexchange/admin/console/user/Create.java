
package com.openexchange.admin.console.user;

import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.rmi.ConnectException;
import java.rmi.Naming;
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
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.NoSuchContextException;
import com.openexchange.admin.rmi.exceptions.StorageException;

public class Create extends UserAbstraction {

    public static void main(final String[] args) {
        new Create(args);
    }

    public Create(final String[] args2) {

        final AdminParser parser = new AdminParser("create");

        // set all needed options in our parser
        setOptions(parser);

        setExtendedOptions(parser);

        try {

            // parse the command line
            parser.ownparse(args2);

            printExtendedOutputIfSet(parser);

            final Context ctx = new Context(DEFAULT_CONTEXT);

            if (parser.getOptionValue(this.contextOption) != null) {
                ctx.setID(Integer.parseInt((String) parser.getOptionValue(this.contextOption)));
            }

            final Credentials auth = new Credentials((String) parser.getOptionValue(this.adminUserOption), (String) parser.getOptionValue(this.adminPassOption));

            // get rmi ref
            final OXUserInterface oxres = (OXUserInterface) Naming.lookup(RMI_HOSTNAME + OXUserInterface.RMI_NAME);

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
            
//          set module access rights
            setModuleAccessOptionsinUserCreate(parser, access);

            /*
             * ********************* The extensions
             */

            System.out.println(oxres.create(ctx, usr, access, auth));
            sysexit(0);
        } catch (final ConnectException neti) {
            printError(neti.getMessage());
            sysexit(SYSEXIT_COMMUNICATION_ERROR);
        } catch (final NumberFormatException num) {
            printInvalidInputMsg("Ids must be numbers!");
            sysexit(1);
        } catch (final MalformedURLException e) {
            printServerResponse(e.getMessage());
            sysexit(1);
        } catch (final RemoteException e) {
            printServerResponse(e.getMessage());
            sysexit(SYSEXIT_REMOTE_ERROR);
        } catch (final NotBoundException e) {
            printServerResponse(e.getMessage());
            sysexit(1);
        } catch (final StorageException e) {
            printServerResponse(e.getMessage());
            sysexit(SYSEXIT_SERVERSTORAGE_ERROR);
        } catch (final InvalidCredentialsException e) {
            printServerResponse(e.getMessage());
            sysexit(SYSEXIT_INVALID_CREDENTIALS);
        } catch (final NoSuchContextException e) {
            printServerResponse(e.getMessage());
            sysexit(SYSEXIT_NO_SUCH_CONTEXT);
        } catch (final InvalidDataException e) {
            printServerResponse(e.getMessage());
            sysexit(SYSEXIT_INVALID_DATA);
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
        } catch (final DatabaseUpdateException e) {
            printServerResponse(e.getMessage());
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
        }

    }

    private final void setOptions(final AdminParser parser) {

        setExtendedOption(parser);
        setDefaultCommandLineOptions(parser);

        // add mandatory options
        setMandatoryOptions(parser);

        // add optional opts
        setOptionalOptions(parser);
        
        // module access params
        setModuleAccessOptions(parser);
    }
}
