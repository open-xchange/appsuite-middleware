package com.openexchange.admin.console.context;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Iterator;

import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.console.AdminParser.MissingOptionException;
import com.openexchange.admin.console.CmdLineParser.IllegalOptionValueException;
import com.openexchange.admin.console.CmdLineParser.UnknownOptionException;
import com.openexchange.admin.rmi.OXContextInterface;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.StorageException;

public class List extends ContextAbtraction {

    public List(final String[] args2) {

        final AdminParser parser = new AdminParser("searchcontexts");

        setOptions(parser);
        setCSVOutputOption(parser);

        try {

            parser.ownparse(args2);

            final Credentials auth = new Credentials((String) parser.getOptionValue(this.adminUserOption), (String) parser.getOptionValue(this.adminPassOption));

            // get rmi ref
            final OXContextInterface oxctx = (OXContextInterface) Naming.lookup(RMI_HOSTNAME +OXContextInterface.RMI_NAME);

            String pattern = "*";
            if (parser.getOptionValue(this.searchOption) != null) {
                pattern = (String) parser.getOptionValue(this.searchOption);
            }

            final Context[] ctxs = oxctx.list(pattern, auth);

            // needed for csv output, KEEP AN EYE ON ORDER!!!
            final ArrayList<String> columns = new ArrayList<String>();
            columns.add("id");
            columns.add("name");
            columns.add("enabled");
            columns.add("filestore_id");
            columns.add("filestore_name");
            columns.add("used_quota");
            columns.add("max_quota");

            final ArrayList<ArrayList<String>> data = new ArrayList<ArrayList<String>>();

            final String HEADER_FORMAT = "%-7s %-5s %-20s %-10s %-10s %-10s %-20s %s\n";
            final String VALUE_FORMAT  = "%-7s %-5s %-20s %-10s %-10s %-10s %-20s %s\n";
            if(parser.getOptionValue(this.csvOutputOption) == null) {
                System.out.format(HEADER_FORMAT, "cid", "fid", "fname", "enabled", "qmax", "qused", "name","lmappings");
            }
            for (Context ctx_tmp : ctxs) {
                if (parser.getOptionValue(this.csvOutputOption) != null) {
                    data.add(makeCSVData(ctx_tmp));
                } else {
                    // loginl mappings
                    StringBuilder sb = new StringBuilder();
                    if(ctx_tmp.getLoginMappings()!=null &&ctx_tmp.getLoginMappings().size()>0 ){
                        Iterator itr = ctx_tmp.getLoginMappings().iterator();
                        while(itr.hasNext()){
                            sb.append("\"");
                            sb.append((String)itr.next());
                            sb.append("\"");
                            sb.append(",");
                        }
                        sb.deleteCharAt(sb.length()-1);
                    }
                    
                    System.out.format(VALUE_FORMAT,
                            ctx_tmp.getIdAsInt(),
                            ctx_tmp.getFilestore().getId(),
                            ctx_tmp.getFilestore().getName(),
                            ctx_tmp.isEnabled(),
                            ctx_tmp.getMaxQuota(),
                            ctx_tmp.getUsedQuota(),                            
                            ctx_tmp.getName(),
                            sb.toString());
                }
            }

            if (parser.getOptionValue(this.csvOutputOption) != null) {
                doCSVOutput(columns, data);
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

    public static void main(final String args[]) {
        new List(args);
    }

    private void setOptions(final AdminParser parser) {
        setDefaultCommandLineOptionsWithoutContextID(parser);
        setSearchOption(parser, false);
    }

    private ArrayList<String> makeCSVData(final Context ctx) {
        final ArrayList<String> srv_data = new ArrayList<String>();
        srv_data.add(String.valueOf(ctx.getIdAsInt()));
        if (ctx.getName() != null) {
            srv_data.add(ctx.getName());
        } else {
            srv_data.add(null);
        }
        if (ctx.isEnabled() != null) {
            srv_data.add(String.valueOf(ctx.isEnabled()));
        } else {
            srv_data.add(null);
        }

        if (ctx.getFilestore() != null) {
            if (ctx.getFilestore().getId() != null) {
                srv_data.add(String.valueOf(ctx.getFilestore().getId()));
            } else {
                srv_data.add(null);
            }
            if (ctx.getFilestore().getName() != null) {
                srv_data.add(ctx.getFilestore().getName());
            } else {
                srv_data.add(null);
            }
        } else {
            srv_data.add(null);
            srv_data.add(null);
        }

        if(ctx.getUsedQuota() != null ) {
            srv_data.add(String.valueOf(ctx.getUsedQuota()));
        } else {
            srv_data.add(null);
        }

        if(ctx.getMaxQuota() != null ) {
            srv_data.add(String.valueOf(ctx.getMaxQuota()));
        } else {
            srv_data.add(null);
        }
        return srv_data;
    }
}
