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
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.NoSuchContextException;
import com.openexchange.admin.rmi.exceptions.NoSuchUserException;
import com.openexchange.admin.rmi.exceptions.StorageException;

public abstract class ChangeCore extends UserAbstraction {

    protected final void setOptions(final AdminParser parser) {
        
        setExtendedOption(parser);
        setDefaultCommandLineOptions(parser);

        // required
        setIdOption(parser);

        displayNameOption = setShortLongOpt(parser,OPT_DISPLAYNAME_SHORT,OPT_DISPLAYNAME_LONG,"Display name of the user", true, false); 
        givenNameOption =  setShortLongOpt(parser,OPT_GIVENNAME_SHORT,OPT_GIVENNAME_LONG,"Given name for the user", true, false); 
        surNameOption =  setShortLongOpt(parser,OPT_SURNAME_SHORT,OPT_SURNAME_LONG,"Sur name for the user", true, false); 
        passwordOption = setShortLongOpt(parser,OPT_PASSWORD_SHORT,OPT_PASSWORD_LONG,"Password for the user", true, false); 
        primaryMailOption = setShortLongOpt(parser,OPT_PRIMARY_EMAIL_SHORT,OPT_PRIMARY_EMAIL_LONG,"Primary mail address", true, false); 

        
        // add optional opts
        setOptionalOptions(parser);
        
        // module access params
        setModuleAccessOptions(parser);
        
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

            // set mandatory id
            usr.setId(Integer.parseInt((String) parser.getOptionValue(this.idOption)));

            // fill user obj with mandatory values from console
            final String optionValue2 = (String) parser.getOptionValue(this.displayNameOption);
            if (null != optionValue2) {
                usr.setDisplay_name(optionValue2);
            }        
            
            final String optionValue3 = (String) parser.getOptionValue(this.givenNameOption);
            if (null != optionValue3) {
                usr.setGiven_name(optionValue3);
            }
            
            final String optionValue4 = (String) parser.getOptionValue(this.surNameOption);
            if (null != optionValue4) {
                usr.setSur_name(optionValue4);
            }
            final String optionValue5 = (String) parser.getOptionValue(this.passwordOption);
            if (null != optionValue5) {
                usr.setPassword(optionValue5);
            }   
            final String optionValue6 = (String) parser.getOptionValue(this.primaryMailOption);
            if (null != optionValue6) {
                usr.setPrimaryEmail(optionValue6);
                usr.setEmail1(optionValue6);
            }        

            // add optional values if set
            setOptionalOptionsinUser(parser, usr);

            applyExtendedOptionsToUser(parser, usr);
            
            maincall(parser, oxusr, ctx, usr, auth);

            // now change module access
            // first load current module access rights from server
            UserModuleAccess access = oxusr.getModuleAccess(ctx, usr, auth);                    
            
            // apply rights from commandline
            setModuleAccessOptionsinUserChange(parser, access);
            
            // apply changes in module access on server
            oxusr.changeModuleAccess(ctx, usr, access, auth);

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
        } catch (final StorageException e) {
            printServerException(e);
            sysexit(SYSEXIT_SERVERSTORAGE_ERROR);
        } catch (final InvalidCredentialsException e) {
            printServerException(e);
            sysexit(SYSEXIT_INVALID_CREDENTIALS);
        } catch (final NoSuchContextException e) {
            printServerException(e);
            sysexit(SYSEXIT_NO_SUCH_CONTEXT);
        } catch (final InvalidDataException e) {
            printServerException(e);
            sysexit(SYSEXIT_INVALID_DATA);
        } catch (final DatabaseUpdateException e) {
            printServerException(e);
            sysexit(1);
        } catch (final NoSuchUserException e) {
            printServerException(e);
            sysexit(SYSEXIT_NO_SUCH_USER);
        }

    }

    protected abstract void maincall(final AdminParser parser, final OXUserInterface oxusr, final Context ctx, final User usr, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, DatabaseUpdateException, NoSuchUserException;

}
