
package com.openexchange.admin.console.resource;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;

import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.console.AdminParser.MissingOptionException;
import com.openexchange.admin.console.CmdLineParser.IllegalOptionValueException;
import com.openexchange.admin.console.CmdLineParser.UnknownOptionException;
import com.openexchange.admin.rmi.OXResourceInterface;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.Resource;
import com.openexchange.admin.rmi.exceptions.DatabaseUpdateException;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.NoSuchContextException;
import com.openexchange.admin.rmi.exceptions.NoSuchResourceException;
import com.openexchange.admin.rmi.exceptions.StorageException;

public class Change extends ResourceAbstraction {

    public static void main(final String[] args) {
        new Change(args);
    }

    public Change(final String[] args2) {

        final AdminParser parser = new AdminParser("change");

        setOptions(parser);

        try {
            parser.ownparse(args2);

            final Context ctx = new Context(DEFAULT_CONTEXT);

            if (parser.getOptionValue(this.contextOption) != null) {
                ctx.setID(Integer.parseInt((String) parser.getOptionValue(this.contextOption)));
            }

            final Credentials auth = new Credentials((String) parser.getOptionValue(this.adminUserOption), (String) parser.getOptionValue(this.adminPassOption));

            final OXResourceInterface oxres = (OXResourceInterface) Naming.lookup(RMI_HOSTNAME + OXResourceInterface.RMI_NAME);
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

            if (parser.getOptionValue(this.resourceRecipientsOption) != null) {
                final String vals = (String) parser.getOptionValue(this.resourceRecipientsOption);
                final ArrayList<String> recs = new ArrayList<String>();
                if (vals.contains(",")) {
                    for (final String s : vals.split(",")) {
                        recs.add(s.trim());
                    }
                } else {
                    recs.add(vals.trim());
                }
            }

            oxres.change(ctx, res, auth);

            sysexit(0);
        } catch (final java.rmi.ConnectException neti) {
            printError(neti.getMessage());
            sysexit(SYSEXIT_COMMUNICATION_ERROR);
        } catch (final java.lang.NumberFormatException num) {
            printInvalidInputMsg("Ids must be numbers!");
            sysexit(1);
        } catch (final MalformedURLException e) {
            printServerResponse(e);
            sysexit(1);
        } catch (final RemoteException e) {
            printServerResponse(e);
            sysexit(SYSEXIT_REMOTE_ERROR);
        } catch (final NotBoundException e) {
            printServerResponse(e);
            sysexit(1);
        } catch (final StorageException e) {
            printServerResponse(e);
            sysexit(SYSEXIT_SERVERSTORAGE_ERROR);
        } catch (final InvalidCredentialsException e) {
            printServerResponse(e);
            sysexit(SYSEXIT_INVALID_CREDENTIALS);
        } catch (final NoSuchContextException e) {
            printServerResponse(e);
            sysexit(SYSEXIT_NO_SUCH_CONTEXT);
        } catch (final InvalidDataException e) {
            printServerResponse(e);
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
            printServerResponse(e);
            sysexit(1);
        } catch (final NoSuchResourceException e) {
            printServerResponse(e);
            sysexit(SYSEXIT_NO_SUCH_RESOURCE);
        }

    }

    private void setOptions(final AdminParser parser) {
        setDefaultCommandLineOptions(parser);

        // id is required
        setIdOption(parser, true);

        // optional
        setNameOption(parser, false);
        setDisplayNameOption(parser, false);
        setAvailableOption(parser, false);
        setDescriptionOption(parser, false);
        setEmailOption(parser, false);
        setRecipientsOption(parser, false);

    }
}
