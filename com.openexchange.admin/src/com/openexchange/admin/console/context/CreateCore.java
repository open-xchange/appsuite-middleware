package com.openexchange.admin.console.context;

import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Locale;
import java.util.TimeZone;

import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.console.AdminParser.MissingOptionException;
import com.openexchange.admin.console.CmdLineParser.IllegalOptionValueException;
import com.openexchange.admin.console.CmdLineParser.UnknownOptionException;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.rmi.exceptions.ContextExistsException;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.StorageException;

public abstract class CreateCore extends ContextAbstraction {
    protected void setOptions(final AdminParser parser) {
        setDefaultCommandLineOptions(parser);
        setContextNameOption(parser);
        setMandatoryOptions(parser);
        
        setLanguageOption(parser);
        setTimezoneOption(parser);

        setContextQuotaOption(parser, true);
        
        setFurtherOptions(parser);
    }
    
    protected final void commonfunctions(final AdminParser parser, final String[] args) {
        setOptions(parser);

        try {

            parser.ownparse(args);

            final Context ctx = contextparsing(parser);

            final String optionValue = (String) parser.getOptionValue(contextNameOption);
            if (optionValue != null) {
                ctx.setName(optionValue);
            }

            final Credentials auth = credentialsparsing(parser);

            // create user obj
            final User usr = new User();

            // fill user obj with mandatory values from console
            setMandatoryOptionsinUser(parser, usr);
            // fill user obj with mandatory values from console
            final String tz = (String) parser.getOptionValue(this.timezoneOption);
            if (null != tz) {
                usr.setTimezone(TimeZone.getTimeZone(tz));
            }

            final String languageoptionvalue = (String) parser.getOptionValue(this.languageOption);
            if (languageoptionvalue != null) {
                final String[] lange = languageoptionvalue.split("_");
                if (lange != null && lange.length == 2) {
                    usr.setLanguage(new Locale(lange[0].toLowerCase(), lange[1].toUpperCase()));
                }
            }

            ctx.setMaxQuota(Long.parseLong((String) parser.getOptionValue(this.contextQuotaOption)));

            System.out.println(maincall(parser, ctx, usr, auth).getIdAsString());
            
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
        } catch (final ContextExistsException e) {
            printServerException(e);
            sysexit(1);
        }
    }
    
    protected abstract Context maincall(final AdminParser parser, Context ctx, User usr, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException, MalformedURLException, NotBoundException, ContextExistsException;
        
    protected abstract void setFurtherOptions(final AdminParser parser);
}
