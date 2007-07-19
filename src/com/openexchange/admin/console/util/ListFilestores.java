package com.openexchange.admin.console.util;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;

import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.console.AdminParser.MissingOptionException;
import com.openexchange.admin.console.CmdLineParser.IllegalOptionValueException;
import com.openexchange.admin.console.CmdLineParser.UnknownOptionException;
import com.openexchange.admin.rmi.OXUtilInterface;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.Filestore;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.StorageException;

/**
 * 
 * @author d7,cutmasta
 * 
 */
public class ListFilestores extends UtilAbstraction {

    public ListFilestores(final String[] args2) {

        final AdminParser parser = new AdminParser("listfilestores");

        setOptions(parser);
        setCSVOutputOption(parser);
        try {
            parser.ownparse(args2);

            final Credentials auth = new Credentials((String) parser.getOptionValue(this.adminUserOption), (String) parser.getOptionValue(this.adminPassOption));

            // get rmi ref
            final OXUtilInterface oxutil = (OXUtilInterface) Naming.lookup(RMI_HOSTNAME +OXUtilInterface.RMI_NAME);

            String searchpattern = "*";
            if (parser.getOptionValue(this.searchOption) != null) {
                searchpattern = (String) parser.getOptionValue(this.searchOption);
            }
            // Setting the options in the dataobject

            final Filestore[] filestores = oxutil.listFilestores(searchpattern, auth);

            if (null != parser.getOptionValue(this.csvOutputOption)) {
                precsvinfos(filestores);
            } else {
                sysoutOutput(filestores);
            }

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
        } catch (URISyntaxException e) {
            printServerException(e);
            sysexit(1);
        }
    }

    private void sysoutOutput(final Filestore[] filestores) throws InvalidDataException, URISyntaxException {
        final ArrayList<ArrayList<String>> data = new ArrayList<ArrayList<String>>();
        for (final Filestore filestore : filestores) {
            data.add(makeStandardData(filestore, false));
        }
        
        doOutput(new String[] { "3r", "35l", "7r", "7r", "7r", "7r", "7r" },
                 new String[] { "id", "path", "size", "qmax", "qused", "maxctx", "curctx" }, data);
    }

    private void precsvinfos(final Filestore[] filestores) throws URISyntaxException {
        // needed for csv output, KEEP AN EYE ON ORDER!!!
        final ArrayList<String> columns = new ArrayList<String>();
        columns.add("id");
        columns.add("uri");
        columns.add("size");
        columns.add("quota_max");
        columns.add("quota_used");
        columns.add("maxcontexts");
        columns.add("currentcontexts");
        columns.add("login");
        columns.add("password");
        columns.add("name");
        // Needed for csv output
        final ArrayList<ArrayList<String>> data = new ArrayList<ArrayList<String>>();

        for (final Filestore filestore : filestores) {
            data.add(makeCSVData(filestore));
        }

        doCSVOutput(columns, data);
    }

    public static void main(final String args[]) {
        new ListFilestores(args);
    }

    private void setOptions(final AdminParser parser) {
        setDefaultCommandLineOptionsWithoutContextID(parser);
        setSearchOption(parser);
    }

    /**
     * 
     * @param fstore
     * @return
     * @throws URISyntaxException 
     */
    private ArrayList<String> makeCSVData(final Filestore fstore) throws URISyntaxException {
        final ArrayList<String> rea_data = makeStandardData(fstore, true);

        if (fstore.getLogin() != null) {
            rea_data.add(fstore.getLogin());
        } else {
            rea_data.add(null);
        }

        if (fstore.getPassword() != null) {
            rea_data.add(fstore.getPassword());
        } else {
            rea_data.add(null);
        }

        if (fstore.getName() != null) {
            rea_data.add(fstore.getName());
        } else {
            rea_data.add(null);
        }

        return rea_data;
    }

    private ArrayList<String> makeStandardData(final Filestore fstore, final boolean csv) throws URISyntaxException {
        final ArrayList<String> rea_data = new ArrayList<String>();

        rea_data.add(fstore.getId().toString());

        if (fstore.getUrl() != null) {
            if (csv) {
                rea_data.add(fstore.getUrl());
            } else {
                rea_data.add(new URI(fstore.getUrl()).getPath());
            }
        } else {
            rea_data.add(null);
        }

        if (fstore.getSize() != null) {
            rea_data.add(fstore.getSize().toString());
        } else {
            rea_data.add(null);
        }

        if (fstore.getQuota_max() != null) {
            rea_data.add(fstore.getQuota_max().toString());
        } else {
            rea_data.add(null);
        }
        
        if (fstore.getQuota_used() != null) {
            rea_data.add(fstore.getQuota_used().toString());
        } else {
            rea_data.add(null);
        }
        
        if (fstore.getMaxContexts() != null) {
            rea_data.add(fstore.getMaxContexts().toString());
        } else {
            rea_data.add(null);
        }

        if (fstore.getCurrentContexts() != null) {
            rea_data.add(fstore.getCurrentContexts().toString());
        } else {
            rea_data.add(null);
        }
        
        return rea_data;
    }
}
