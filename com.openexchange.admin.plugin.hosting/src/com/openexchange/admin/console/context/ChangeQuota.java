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

public class ChangeQuota extends ContextAbtraction {
    
   
 public ChangeQuota(final String[] args2) {
        
     AdminParser parser = new AdminParser("changequota");

     setOptions(parser);

     try {
         
            parser.ownparse(args2);    
            final Context ctx = new Context();            
            
            ctx.setID(Integer.parseInt((String)parser.getOptionValue(contextIDOption)));               
            
            final Credentials auth = new Credentials((String)parser.getOptionValue(adminUserOption),(String)parser.getOptionValue(adminPassOption));
                
            // get rmi ref
            final OXContextInterface oxres = (OXContextInterface)Naming.lookup(OXContextInterface.RMI_NAME);
            
            final long quota = Long.parseLong((String)parser.getOptionValue(filestoreContextQuotaOption));            
                       
            oxres.changeQuota(ctx, quota, auth);
            
        }catch(final java.rmi.ConnectException neti){
            printError(neti.getMessage());            
            System.exit(1);
        }catch(final java.lang.NumberFormatException num){
            printInvalidInputMsg("Ids must be numbers!");
            System.exit(1);
        } catch (final MalformedURLException e) {            
            printServerResponse(e.getMessage());
            System.exit(1);
        } catch (final RemoteException e) {            
            printServerResponse(e.getMessage());
            System.exit(1);
        } catch (final NotBoundException e) {
            printNotBoundResponse(e);
            System.exit(1);
        } catch (final StorageException e) {            
            printServerResponse(e.getMessage());
            System.exit(1);
        } catch (final InvalidCredentialsException e) {
            printServerResponse(e.getMessage());
            System.exit(1);
        } catch (final InvalidDataException e) {            
            printServerResponse(e.getMessage());
            System.exit(1);
        } catch (final NoSuchContextException e) {
            printServerResponse(e.getMessage());
            System.exit(1);
        } catch (IllegalOptionValueException e) {            
            printError("Illegal option value : " + e.getMessage());
            parser.printUsage();
            System.exit(1);
        } catch (UnknownOptionException e) {
            printError("Unrecognized options on the command line: " + e.getMessage());
            parser.printUsage();
            System.exit(1);
        } catch (MissingOptionException e) {
            printError(e.getMessage());
            parser.printUsage();
            System.exit(1);
        }

    }

    public static void main(final String args[]){
        new ChangeQuota(args);
    }
    
    private void setOptions(AdminParser parser) {
        setDefaultCommandLineOptions(parser);
        
        setContextIDOption(parser, true);
        
        setContextQuotaOption(parser,true);
    }
    
    
    
    

}
