package com.openexchange.admin.console.resource;

import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.console.AdminParser.MissingOptionException;
import com.openexchange.admin.console.CmdLineParser.IllegalOptionValueException;
import com.openexchange.admin.console.CmdLineParser.UnknownOptionException;
import com.openexchange.admin.rmi.OXResourceInterface;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.Resource;
import com.openexchange.admin.rmi.exceptions.DatabaseUpdateException;
import com.openexchange.admin.rmi.exceptions.DuplicateExtensionException;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.NoSuchContextException;
import com.openexchange.admin.rmi.exceptions.NoSuchResourceException;
import com.openexchange.admin.rmi.exceptions.StorageException;

public abstract class ChangeCore extends ResourceAbstraction {
    
    protected final void setOptions(final AdminParser parser) {
        setDefaultCommandLineOptions(parser);

        // id is required
        setIdOption(parser, true);

        // optional
        setNameOption(parser, false);
        setDisplayNameOption(parser, false);
        setAvailableOption(parser, false);
        setDescriptionOption(parser, false);
        setEmailOption(parser, false);
        
        setFurtherOptions(parser);
    }

    protected abstract void setFurtherOptions(final AdminParser parser);

    protected final void commonfunctions(final AdminParser parser, final String[] args) {
        setOptions(parser);

        try {
            parser.ownparse(args);

            final Context ctx = contextparsing(parser);

            final Credentials auth = credentialsparsing(parser);
            
            final OXResourceInterface oxres = getResourceInterface();
            final Resource res = new Resource();

            res.setId(Integer.parseInt((String) parser.getOptionValue(this.resourceIdOption)));

            if (parser.getOptionValue(this.resourceAvailableOption) != null) {
                res.setAvailable(Boolean.parseBoolean(parser.getOptionValue(this.resourceAvailableOption).toString()));
            }

            if (parser.getOptionValue(this.resourceDescriptionOption) != null) {
                res.setDescription((String) parser.getOptionValue(this.resourceDescriptionOption));
            }

            if (parser.getOptionValue(this.resourceDisplayNameOption) != null) {
                res.setDisplayname((String) parser.getOptionValue(this.resourceDisplayNameOption));
            }

            if (parser.getOptionValue(this.resourceEmailOption) != null) {
                res.setEmail((String) parser.getOptionValue(this.resourceEmailOption));
            }

            if (parser.getOptionValue(this.resourceNameOption) != null) {
                res.setName((String) parser.getOptionValue(this.resourceNameOption));
            }

            maincall(parser, oxres, ctx, res, auth);
            
            oxres.change(ctx, res, auth);

            displayChangedMessage();
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
            printServerException(e);
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
        } catch (final DatabaseUpdateException e) {
            printServerException(e);
            sysexit(1);
        } catch (final NoSuchResourceException e) {
            printServerException(e);
            sysexit(SYSEXIT_NO_SUCH_RESOURCE);
        } catch (final DuplicateExtensionException e) {
            printServerException(e);
            sysexit(1);
        }
    }

    protected abstract void maincall(final AdminParser parser, final OXResourceInterface oxres, final Context ctx, final Resource res, final Credentials auth) throws RemoteException, DuplicateExtensionException;
}
