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
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.NoSuchContextException;
import com.openexchange.admin.rmi.exceptions.StorageException;

public class Change extends ContextHostingAbstraction {

    
    public Change(final String[] args2) {
        final AdminParser parser = new AdminParser("changecontext");

        setOptions(parser);
        
        try {
            
            int doChange = 0;
            
            String[] remove_mappings = null;
            String[] add_mappings = null;
            
            parser.ownparse(args2);
            final Context ctx = contextparsing(parser);
            
            // context filestore quota
            if(parser.getOptionValue(this.contextQuotaOption)!=null){                
                ctx.setMaxQuota(Long.parseLong((String) parser.getOptionValue(this.contextQuotaOption)));
                doChange++;
            }
            
            // context name 
            if (parser.getOptionValue(this.contextNameOption) != null) {
                ctx.setName((String) parser.getOptionValue(this.contextNameOption));
                doChange++;
            }
            
            // add login mappings
            if (parser.getOptionValue(this.addLoginMappingOption) != null) {
                add_mappings = ((String) parser.getOptionValue(this.addLoginMappingOption)).split(",");
                doChange++;
            }
            
            // remove login mappings
            if (parser.getOptionValue(this.removeLoginMappingOption) != null) {
                remove_mappings = ((String) parser.getOptionValue(this.removeLoginMappingOption)).split(",");
                doChange++;
            }
            
            final Credentials auth = credentialsparsing(parser);
                        
            // only if he supplied args, make the  call
            if(doChange>0){
                
                // get rmi ref
                final OXContextInterface oxres = (OXContextInterface) Naming.lookup(RMI_HOSTNAME +OXContextInterface.RMI_NAME);
                
                // check if wants to change login mappings, then first load current mappings from server
                if(add_mappings!=null || remove_mappings!=null){
                    Context server_ctx = oxres.getData(ctx, auth);
                    ctx.setLoginMappings(server_ctx.getLoginMappings());
                    
                    
                    // add new mappings
                    if(add_mappings!=null){
                        for (String login_mapping : add_mappings) {
                            ctx.addLoginMapping(login_mapping);
                        }
                    }
                    
                    // remove mappings
                    if(remove_mappings!=null){
                        for (String login_mapping : remove_mappings) {
                            ctx.removeLoginMapping(login_mapping);
                        }
                    }
                }
                
                // do the change
                oxres.change(ctx, auth);
                
                displayChangedMessage(null, null);
                sysexit(0);
            }
        } catch (final java.rmi.ConnectException neti) {
            printError(ctxid, null, neti.getMessage());
            sysexit(SYSEXIT_COMMUNICATION_ERROR);
        } catch (final java.lang.NumberFormatException num) {
            printInvalidInputMsg(ctxid, null, "Ids must be numbers!");
            sysexit(1);
        } catch (final MalformedURLException e) {
            printServerException(ctxid, null, e);
            sysexit(1);
        } catch (final RemoteException e) {
            printServerException(ctxid, null, e);
            sysexit(SYSEXIT_REMOTE_ERROR);
        } catch (final NotBoundException e) {
            printNotBoundResponse(ctxid, null, e);
            sysexit(1);
        } catch (final StorageException e) {
            printServerException(ctxid, null, e);
            sysexit(SYSEXIT_SERVERSTORAGE_ERROR);
        } catch (final InvalidCredentialsException e) {
            printServerException(ctxid, null, e);
            sysexit(SYSEXIT_INVALID_CREDENTIALS);
        } catch (final InvalidDataException e) {
            printServerException(ctxid, null, e);
            sysexit(SYSEXIT_INVALID_DATA);
        } catch (final NoSuchContextException e) {
            printServerException(ctxid, null, e);
            sysexit(SYSEXIT_NO_SUCH_CONTEXT);
        } catch (final IllegalOptionValueException e) {
            printError(ctxid, null, "Illegal option value : " + e.getMessage());
            parser.printUsage();
            sysexit(SYSEXIT_ILLEGAL_OPTION_VALUE);
        } catch (final UnknownOptionException e) {
            printError(ctxid, null, "Unrecognized options on the command line: " + e.getMessage());
            parser.printUsage();
            sysexit(SYSEXIT_UNKNOWN_OPTION);
        } catch (final MissingOptionException e) {
            printError(ctxid, null, e.getMessage());
            parser.printUsage();
            sysexit(SYSEXIT_MISSING_OPTION);
        }
    }

    public static void main(final String args[]) {
        new Change(args);
    }

    private void setOptions(final AdminParser parser) {
        setDefaultCommandLineOptions(parser);
        
        setContextNameOption(parser,false);
        
        setAddMappingOption(parser, false);
        
        setRemoveMappingOption(parser, false);
        
        setContextQuotaOption(parser, false);
    }
}
