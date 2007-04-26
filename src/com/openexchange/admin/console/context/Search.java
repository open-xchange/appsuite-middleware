package com.openexchange.admin.console.context;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;

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

public class Search extends ContextAbtraction {    
       
 public Search(final String[] args2) {
        
        AdminParser parser = new AdminParser("listcontexts");

        setOptions(parser);
        setCSVOutputOption(parser);
        
        try {
            
             parser.ownparse( args2); 
            
             final Credentials auth = new Credentials((String)parser.getOptionValue(adminUserOption),(String)parser.getOptionValue(adminPassOption));
                        
            // get rmi ref
            final OXContextInterface oxres = (OXContextInterface)Naming.lookup(OXContextInterface.RMI_NAME);
                        
            String pattern = "*";
            if(parser.getOptionValue(searchOption)!=null){
                pattern = (String)parser.getOptionValue(searchOption);
            }
            
            final Context[] ctxs = oxres.search(pattern, auth);
            
//          needed for csv output, KEEP AN EYE ON ORDER!!!
            ArrayList<String> columns = new ArrayList<String>();
            columns.add("id");
            columns.add("name");
            columns.add("enabeld");
            columns.add("filestore_id");
            columns.add("filestore_name");
            
            ArrayList<ArrayList<String>> data = new ArrayList<ArrayList<String>>();
            
            
            for (final Context ctx_tmp : ctxs) {
                if (parser.getOptionValue(csvOutputOption) != null) { 
                    data.add(makeCSVData(ctx_tmp));
                }else{
                    System.out.println(ctx_tmp.toString());
                }
            }
            
            if (parser.getOptionValue(csvOutputOption) != null) {
                doCSVOutput(columns, data);
            }
            
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
        new Search(args);
    }
    
    private void setOptions(AdminParser parser) {
        setDefaultCommandLineOptions(parser);
        setSearchOption(parser,false);
    }
    
    private ArrayList<String> makeCSVData(Context ctx){
        ArrayList<String> srv_data = new ArrayList<String>();
        srv_data.add(String.valueOf(ctx.getIdAsInt()));
        if(ctx.getName()!=null){
            srv_data.add(ctx.getName());
        }else{
            srv_data.add(null);
        }
        if(ctx.isEnabled()!=null){
            srv_data.add(String.valueOf(ctx.isEnabled()));
        }else{
            srv_data.add(null);
        }
        
        if(ctx.getFilestore()!=null){
            if(ctx.getFilestore().getId()!=null){
                srv_data.add(String.valueOf(ctx.getFilestore().getId()));
            }else{
                srv_data.add(null);
            }
            if(ctx.getFilestore().getName()!=null){
                srv_data.add(ctx.getFilestore().getName());
            }else{
                srv_data.add(null);
            }
        }else{
            srv_data.add(null);
            srv_data.add(null);
        }
        
        return srv_data;
    }
    

}
