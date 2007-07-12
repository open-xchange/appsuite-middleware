package com.openexchange.admin.console.util;

import java.net.MalformedURLException;
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
public class ListFilestore extends UtilAbstraction {

    public ListFilestore(final String[] args2) {

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
        }
    }

    private void sysoutOutput(Filestore[] filestores) throws InvalidDataException {
        final ArrayList<ArrayList<String>> data = new ArrayList<ArrayList<String>>();
        for (final Filestore filestore : filestores) {
            data.add(makeStandardData(filestore));
        }
        
//        final String HEADER_FORMAT = "%-7s %-35s %-7s %-7s %-7s %-7s %s\n";
//            System.out.format(HEADER_FORMAT, "id", "path", "size", "qmax", "qused", "maxctx", "curctx");
        doOutput(new int[] { 3, 35, 7, 7, 7, 7, 7 }, new String[] { "id", "path", "size", "qmax", "qused", "maxctx", "curctx" }, data);
    }

    private void precsvinfos(Filestore[] filestores) {
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
        new ListFilestore(args);
    }

    private void setOptions(final AdminParser parser) {
        setDefaultCommandLineOptions(parser);
        setSearchOption(parser);
    }

    /**
     * 
     * @param fstore
     * @return
     */
    private ArrayList<String> makeCSVData(final Filestore fstore) {
        final ArrayList<String> rea_data = makeStandardData(fstore);

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

    private ArrayList<String> makeStandardData(final Filestore fstore) {
        final ArrayList<String> rea_data = new ArrayList<String>();

        rea_data.add(fstore.getId().toString());

        if (fstore.getUrl() != null) {
            rea_data.add(fstore.getUrl());
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
