package com.openexchange.admin.console.context;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.console.AdminParser.MissingOptionException;
import com.openexchange.admin.console.CmdLineParser.IllegalOptionValueException;
import com.openexchange.admin.console.CmdLineParser.UnknownOptionException;
import com.openexchange.admin.rmi.OXContextInterface;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.rmi.exceptions.ContextExistsException;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.StorageException;

public class Create extends ContextAbtraction {

    public Create(final String[] args2) {

        final AdminParser parser = new AdminParser("createcontext");
        setOptions(parser);

        try {
            parser.ownparse(args2);

            final Context ctx = new Context();
            ctx.setID(Integer.parseInt((String) parser.getOptionValue(this.contextOption)));

            if (parser.getOptionValue(this.contextNameOption) != null) {
                ctx.setName((String) parser.getOptionValue(this.contextNameOption));
            }
            
            final Credentials auth = new Credentials((String) parser.getOptionValue(this.adminUserOption), (String) parser.getOptionValue(this.adminPassOption));

            // get rmi ref
            final OXContextInterface oxctx = (OXContextInterface) Naming.lookup(RMI_HOSTNAME +OXContextInterface.RMI_NAME);

            // create user obj
            final User usr = new User();

            // fill user obj with mandatory values from console
            setMandatoryOptionsinUser(parser, usr);

            ctx.setMaxQuota(Long.parseLong((String) parser.getOptionValue(this.filestoreContextQuotaOption)));

            System.out.println(oxctx.create(ctx, usr, auth).getIdAsInt());

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
        } catch (final ContextExistsException e) {
            printServerException(e);
            sysexit(SYSEXIT_CONTEXT_ALREADY_EXISTS);
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
        new Create(args);
    }

    private void setOptions(final AdminParser parser) {
        setDefaultCommandLineOptions(parser);

        setContextQuotaOption(parser, true);
        
        setContextNameOption(parser,false);
        
        setMandatoryOptions(parser);
    }
}
