package com.openexchange.admin.console.context;



import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

import com.openexchange.admin.console.BasicCommandlineOptions;
import com.openexchange.admin.rmi.OXContextInterface;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.StorageException;

public class EnableAll extends BasicCommandlineOptions{    
      
    
 public EnableAll(String[] args2) {
        
        CommandLineParser parser = new PosixParser();

        Options options = getOptions();

        try {
            
            CommandLine cmd = parser.parse(options, args2);            
                        
            Credentials auth = new Credentials(cmd.getOptionValue(OPT_NAME_ADMINUSER_SHORT),cmd.getOptionValue(OPT_NAME_ADMINPASS_SHORT));
                        
            // get rmi ref
            OXContextInterface oxres = (OXContextInterface)Naming.lookup(OXContextInterface.RMI_NAME);            
            
            oxres.enableAll(auth);
            
        }catch(java.rmi.ConnectException neti){
            printError(neti.getMessage());            
        }catch(java.lang.NumberFormatException num){
            printInvalidInputMsg("Ids must be numbers!");
        }catch(org.apache.commons.cli.MissingArgumentException as){
            printError("Missing arguments on the command line: " + as.getMessage());;
            printHelpText("enableall", options);
        }catch(org.apache.commons.cli.UnrecognizedOptionException ux){
            printError("Unrecognized options on the command line: " + ux.getMessage());;
            printHelpText("enableall", options);
        } catch (org.apache.commons.cli.MissingOptionException mis) {
            printError("Missing options on the command line: " + mis.getMessage());;
            printHelpText("enableall", options);
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {            
            printServerResponse(e.getMessage());
        } catch (RemoteException e) {            
            printServerResponse(e.getMessage());
        } catch (NotBoundException e) {
            printNotBoundResponse(e);
        } catch (StorageException e) {            
            printServerResponse(e.getMessage());
        } catch (InvalidCredentialsException e) {
            printServerResponse(e.getMessage());  
        }

    }

    public static void main(String args[]){
        new EnableAll(args);
    }
    
    private Options getOptions() {
        Options retval = new Options();
        retval.addOption(getAdminUserOption());
        retval.addOption(getAdminPassOption());    
        
        return retval;
    }
    
    

}
