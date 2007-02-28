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

import com.openexchange.admin.console.user.UserAbstraction;
import com.openexchange.admin.rmi.OXContextInterface;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.NoSuchContextException;
import com.openexchange.admin.rmi.exceptions.StorageException;

public class Enable extends UserAbstraction {    
     
    
 public Enable(String[] args2) {
        
        CommandLineParser parser = new PosixParser();

        Options options = getOptions();

        try {
            
            CommandLine cmd = parser.parse(options, args2);            
            Context ctx = new Context();            
            
            ctx.setID(Integer.parseInt(cmd.getOptionValue(OPT_NAME_CONTEXT_SHORT)));            
            
            Credentials auth = new Credentials(cmd.getOptionValue(OPT_NAME_ADMINUSER_SHORT),cmd.getOptionValue(OPT_NAME_ADMINPASS_SHORT));
                        
            // get rmi ref
            OXContextInterface oxres = (OXContextInterface)Naming.lookup(OXContextInterface.RMI_NAME);            
            
            oxres.enable(ctx, auth);
            
        }catch(java.rmi.ConnectException neti){
            printError(neti.getMessage());            
        }catch(java.lang.NumberFormatException num){
            printInvalidInputMsg("Ids must be numbers!");
        }catch(org.apache.commons.cli.MissingArgumentException as){
            printError("Missing arguments on the command line: " + as.getMessage());;
            printHelpText("enable", options);
        }catch(org.apache.commons.cli.UnrecognizedOptionException ux){
            printError("Unrecognized options on the command line: " + ux.getMessage());;
            printHelpText("enable", options);
        } catch (org.apache.commons.cli.MissingOptionException mis) {
            printError("Missing options on the command line: " + mis.getMessage());;
            printHelpText("enable", options);
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {            
            printServerResponse(e.getMessage());
        } catch (RemoteException e) {            
            printServerResponse(e.getMessage());
        } catch (NotBoundException e) {
            printServerResponse(e.getMessage());
        } catch (StorageException e) {            
            printServerResponse(e.getMessage());
        } catch (InvalidCredentialsException e) {
            printServerResponse(e.getMessage());      
        } catch (NoSuchContextException e) {
            printServerResponse(e.getMessage());
        }

    }

    public static void main(String args[]){
        new Enable(args);
    }
    
    private Options getOptions() {
        
        Options retval = getDefaultCommandLineOptions();
        
        // this time context id is mandatory
        Option tmp = retval.getOption(OPT_NAME_CONTEXT_SHORT);
        tmp.setRequired(true);
        retval.addOption(tmp);      
        
        return retval;
    }
    
    

}
