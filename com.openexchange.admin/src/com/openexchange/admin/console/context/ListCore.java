package com.openexchange.admin.console.context;

import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashSet;

import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.console.BasicCommandlineOptions;
import com.openexchange.admin.console.AdminParser.MissingOptionException;
import com.openexchange.admin.console.CmdLineParser.IllegalOptionValueException;
import com.openexchange.admin.console.CmdLineParser.UnknownOptionException;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.StorageException;

public abstract class ListCore extends BasicCommandlineOptions {

    protected final void setOptions(final AdminParser parser) {
        setDefaultCommandLineOptionsWithoutContextID(parser);
        setCSVOutputOption(parser);
        
        setFurtherOptions(parser);
    }

    protected abstract void setFurtherOptions(final AdminParser parser);
    
    protected final void commonfunctions(final AdminParser parser, final String[] args) {
        setOptions(parser);
        
        try {
            parser.ownparse(args);
            final Credentials auth = credentialsparsing(parser);
            
            String pattern = getSearchPattern(parser);

            final Context[] ctxs = maincall(parser, pattern, auth);
            
            if (null != parser.getOptionValue(this.csvOutputOption)) {
                precsvinfos(ctxs);
            } else {
                sysoutOutput(ctxs);
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
        } catch (final InvalidDataException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    protected abstract String getSearchPattern(final AdminParser parser);
    
    protected abstract Context[] maincall(final AdminParser parser, final String search_pattern, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException, MalformedURLException, NotBoundException;

    private void sysoutOutput(final Context[] ctxs) throws InvalidDataException, StorageException {
        final ArrayList<ArrayList<String>> data = new ArrayList<ArrayList<String>>();
        for (final Context ctx : ctxs) {
            data.add(makeCSVData(ctx));
        }
        
        doOutput(new String[] { "3r", "3r", "21l", "10l", "10r", "10r", "10l", "10l" },
                 new String[] { "cid", "fid", "fname", "enabled", "qmax", "qused", "name", "lmappings" }, data);
    }

    private void precsvinfos(final Context[] ctxs) throws StorageException {
        // needed for csv output, KEEP AN EYE ON ORDER!!!
        final ArrayList<String> columns = new ArrayList<String>();
        columns.add("id");
        columns.add("filestore_id");
        columns.add("enabled");
        columns.add("max_quota");
        columns.add("used_quota");
        columns.add("name");
        columns.add("lmappings");
    
        final ArrayList<ArrayList<String>> data = new ArrayList<ArrayList<String>>();
    
        for (final Context ctx_tmp : ctxs) {
            data.add(makeCSVData(ctx_tmp));
        }
        
        doCSVOutput(columns, data);
    }

    public ArrayList<String> makeCSVData(final Context ctx) throws StorageException {
            final ArrayList<String> srv_data = new ArrayList<String>();
            srv_data.add(String.valueOf(ctx.getIdAsInt()));
    
            final Integer filestoreId = ctx.getFilestoreId();
            if (filestoreId != null) {
                srv_data.add(String.valueOf(filestoreId));
            } else {
                srv_data.add(null);
            }
    
            final String filestore_name = ctx.getFilestore_name();
            if (filestore_name != null) {
                srv_data.add(filestore_name);
            } else {
                srv_data.add(null);
            }
    
            final Boolean enabled = ctx.isEnabled();
            if (enabled != null) {
                srv_data.add(String.valueOf(enabled));
            } else {
                srv_data.add(null);
            }
    
            final Long maxQuota = ctx.getMaxQuota();
            if (maxQuota != null) {
                srv_data.add(String.valueOf(maxQuota));
            } else {
                srv_data.add(null);
            }
    
            final Long usedQuota = ctx.getUsedQuota();
            if (usedQuota != null) {
                srv_data.add(String.valueOf(usedQuota));
            } else {
                srv_data.add(null);
            }
    
            final String name = ctx.getName();
            if (name != null) {
                srv_data.add(name);
            } else {
                srv_data.add(null);
            }
            
    //      loginl mappings
            
            final HashSet<String> loginMappings = ctx.getLoginMappings();
            if (loginMappings != null && loginMappings.size() > 0) {
                srv_data.add(getObjectsAsString(loginMappings.toArray()));
            } else {
                srv_data.add(null);
            }
            
            return srv_data;
        }
    
}
