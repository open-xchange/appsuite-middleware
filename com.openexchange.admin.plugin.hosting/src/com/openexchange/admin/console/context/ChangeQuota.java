package com.openexchange.admin.console.context;



import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

import com.openexchange.admin.rmi.OXContextInterface;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.NoSuchContextException;
import com.openexchange.admin.rmi.exceptions.StorageException;

public class ChangeQuota extends ContextAbtraction {
    
    private final static String OPT_QUOTA_SHORT = "q";
    private final static String OPT_QUOTA_LONG = "quota";
    
 public ChangeQuota(final String[] args2) {
        
        final CommandLineParser parser = new PosixParser();

        final Options options = getOptions();

        try {
            
            final CommandLine cmd = parser.parse(options, args2);            
            final Context ctx = new Context();            
            
            ctx.setID(Integer.parseInt(cmd.getOptionValue(OPT_NAME_CONTEXT_SHORT)));            
            
            final Credentials auth = new Credentials(cmd.getOptionValue(OPT_NAME_ADMINUSER_SHORT),cmd.getOptionValue(OPT_NAME_ADMINPASS_SHORT));
                        
            // get rmi ref
            final OXContextInterface oxres = (OXContextInterface)Naming.lookup(OXContextInterface.RMI_NAME);
            
            final long quota = Long.parseLong(cmd.getOptionValue(OPT_QUOTA_SHORT));            
                       
            oxres.changeQuota(ctx, quota, auth);
            
        }catch(final java.rmi.ConnectException neti){
            printError(neti.getMessage());            
        }catch(final java.lang.NumberFormatException num){
            printInvalidInputMsg("Ids must be numbers!");
        }catch(final org.apache.commons.cli.MissingArgumentException as){
            printError("Missing arguments on the command line: " + as.getMessage());;
            printHelpText("changequota", options);
        }catch(final org.apache.commons.cli.UnrecognizedOptionException ux){
            printError("Unrecognized options on the command line: " + ux.getMessage());;
            printHelpText("changequota", options);
        } catch (final org.apache.commons.cli.MissingOptionException mis) {
            printError("Missing options on the command line: " + mis.getMessage());;
            printHelpText("changequota", options);
        } catch (final ParseException e) {
            e.printStackTrace();
        } catch (final MalformedURLException e) {            
            printServerResponse(e.getMessage());
        } catch (final RemoteException e) {            
            printServerResponse(e.getMessage());
        } catch (final NotBoundException e) {
            printNotBoundResponse(e);
        } catch (final StorageException e) {            
            printServerResponse(e.getMessage());
        } catch (final InvalidCredentialsException e) {
            printServerResponse(e.getMessage());        
        } catch (final InvalidDataException e) {            
            printServerResponse(e.getMessage());
        } catch (final NoSuchContextException e) {
            printServerResponse(e.getMessage());
        }

    }

    public static void main(final String args[]){
        new ChangeQuota(args);
    }
    
    private Options getOptions() {
        final Options retval = getDefaultCommandLineOptions();
        
        // this time context id is mandatory
        final Option tmp = retval.getOption(OPT_NAME_CONTEXT_SHORT);
        tmp.setRequired(true);
        retval.addOption(tmp);      
        
        final Option quota = getShortLongOpt(OPT_QUOTA_SHORT, OPT_QUOTA_LONG, "How much quota the context can use for filestore", true, true);
        retval.addOption(quota);
        
        return retval;
    }
    
    

}
