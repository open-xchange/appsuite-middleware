package com.openexchange.admin.console.context;



import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

import com.openexchange.admin.console.user.UserAbstraction;
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
    
 public Create(String[] args2) {
        
        CommandLineParser parser = new PosixParser();

        Options options = getOptions();

        try {
            CommandLine cmd = parser.parse(options, args2);            
            Context ctx = new Context(DEFAULT_CONTEXT);
            
            if(cmd.hasOption(OPT_NAME_CONTEXT_SHORT)){
                ctx.setID(Integer.parseInt(cmd.getOptionValue(OPT_NAME_CONTEXT_SHORT)));
            }
            
            Credentials auth = new Credentials(cmd.getOptionValue(OPT_NAME_ADMINUSER_SHORT),cmd.getOptionValue(OPT_NAME_ADMINPASS_SHORT));
            
            
            // get rmi ref
            OXUserInterface oxres = (OXUserInterface)Naming.lookup(OXUserInterface.RMI_NAME);
            
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
            
            
            // add optional values if set
            if(cmd.hasOption(OPT_COMPANY_SHORT)){
                usr.setCompany(cmd.getOptionValue(OPT_COMPANY_SHORT));
            }
            
            if(cmd.hasOption(OPT_DEPARTMENT_SHORT)){
                usr.setDepartment((cmd.getOptionValue(OPT_DEPARTMENT_SHORT)));
            }
            
            if(cmd.hasOption(OPT_LANGUAGE_SHORT)){                
                usr.setLanguage(new Locale(cmd.getOptionValue(OPT_LANGUAGE_SHORT)));
            }
            
            if(cmd.hasOption(OPT_TIMEZONE_SHORT)){                
                usr.setTimezone(TimeZone.getTimeZone(cmd.getOptionValue(OPT_TIMEZONE_SHORT)));
            }
            
            // default set all access rights
            UserModuleAccess access = new UserModuleAccess();
            access.enableAll();
            
            System.out.println(oxres.create(ctx, usr, access, auth));
            
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
        } catch (NoSuchContextException e) {            
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
        
        // add mandattory  options
        retval.addOption(getUsernameOption());
        retval.addOption(getDisplayNameOption());
        retval.addOption(getGivenNameOption());
        retval.addOption(getSurNameOption());
        retval.addOption(getPasswordOption());
        retval.addOption(getPrimaryMailOption());
        
        return retval;
    }
    
    

}
