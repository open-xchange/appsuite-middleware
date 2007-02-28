package com.openexchange.admin.console.context;



import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

import com.openexchange.admin.console.user.UserAbstraction;
import com.openexchange.admin.rmi.OXContextInterface;
import com.openexchange.admin.rmi.OXUserInterface;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.rmi.dataobjects.UserModuleAccess;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.NoSuchContextException;
import com.openexchange.admin.rmi.exceptions.StorageException;

public class Create extends UserAbstraction {
    
    private final static String OPT_QUOTA_SHORT = "q";
    private final static String OPT_QUOTA_LONG = "quota";
    
 public Create(String[] args2) {
        
        CommandLineParser parser = new PosixParser();

        Options options = getOptions();

        try {
            
            CommandLine cmd = parser.parse(options, args2);        
            
            Context ctx = new Context();
            ctx.setID(Integer.parseInt(cmd.getOptionValue(OPT_NAME_CONTEXT_SHORT)));
            
            
            Credentials auth = new Credentials(cmd.getOptionValue(OPT_NAME_ADMINUSER_SHORT),cmd.getOptionValue(OPT_NAME_ADMINPASS_SHORT));
            
            
            // get rmi ref
            OXContextInterface oxres = (OXContextInterface)Naming.lookup(OXContextInterface.RMI_NAME);
            
            // create user obj
            User usr = new User();
            
            // fill user obj with mandatory values from console
            usr.setUsername(cmd.getOptionValue(OPT_USERNAME_SHORT));
            usr.setDisplay_name(cmd.getOptionValue(OPT_DISPLAYNAME_SHORT));
            usr.setGiven_name(cmd.getOptionValue(OPT_GIVENNAME_SHORT));
            usr.setSur_name(cmd.getOptionValue(OPT_SURNAME_SHORT));
            usr.setPassword(cmd.getOptionValue(OPT_PASSWORD_SHORT));
            usr.setPrimaryEmail(cmd.getOptionValue(OPT_PRIMARY_EMAIL_SHORT));
            usr.setEmail1(cmd.getOptionValue(OPT_PRIMARY_EMAIL_SHORT));
            
            long quota = Long.parseLong(cmd.getOptionValue(OPT_QUOTA_SHORT));            
                       
            oxres.create(ctx, usr, quota, auth);
            
        }catch(java.rmi.ConnectException neti){
            printError(neti.getMessage());            
        }catch(java.lang.NumberFormatException num){
            printInvalidInputMsg("Ids must be numbers!");
        }catch(org.apache.commons.cli.MissingArgumentException as){
            printError("Missing arguments on the command line: " + as.getMessage());;
            printHelpText("create", options);
        }catch(org.apache.commons.cli.UnrecognizedOptionException ux){
            printError("Unrecognized options on the command line: " + ux.getMessage());;
            printHelpText("create", options);
        } catch (org.apache.commons.cli.MissingOptionException mis) {
            printError("Missing options on the command line: " + mis.getMessage());;
            printHelpText("create", options);
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
        } catch (InvalidDataException e) {            
            printServerResponse(e.getMessage());
        }

    }

    public static void main(String args[]){
        new Create(args);
    }
    
    private Options getOptions() {
        Options retval = getDefaultCommandLineOptions();
        
        // this time context id is mandatory
        Option tmp = retval.getOption(OPT_NAME_CONTEXT_SHORT);
        tmp.setRequired(true);
        retval.addOption(tmp);
        
        // add mandattory  options
        retval.addOption(getUsernameOption());
        retval.addOption(getDisplayNameOption());
        retval.addOption(getGivenNameOption());
        retval.addOption(getSurNameOption());
        retval.addOption(getPasswordOption());
        retval.addOption(getPrimaryMailOption());
        
        Option quota = getShortLongOpt(OPT_QUOTA_SHORT, OPT_QUOTA_LONG, "How much quota the context can use for filestore", true, true);
        retval.addOption(quota);
        
        return retval;
    }
    
    

}
