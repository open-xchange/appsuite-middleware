
package com.openexchange.admin.console.user;

import java.rmi.RemoteException;

import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.console.CmdLineParser.Option;
import com.openexchange.admin.rmi.OXUserInterface;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.rmi.dataobjects.UserModuleAccess;
import com.openexchange.admin.rmi.exceptions.DatabaseUpdateException;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.NoSuchContextException;
import com.openexchange.admin.rmi.exceptions.StorageException;

public class Create extends CreateCore {
	
	// which access rights template should be used
    protected Option accessRightsCombinationName = null;
    
    protected static final String OPT_ACCESSRIGHTS_COMBINATION_NAME = "access-combination-name";
    
    

    public static void main(final String[] args) {
        new Create(args);
    }

    public Create(final String[] args2) {
        final AdminParser parser = new AdminParser("createuser");    
        
        commonfunctions(parser, args2);
        
    }

    @Override
    protected void maincall(final AdminParser parser, final OXUserInterface oxusr, final Context ctx, final User usr, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, DatabaseUpdateException {
        
    	final String accesscombinationname = (String) parser.getOptionValue(this.accessRightsCombinationName);
        if (null != accesscombinationname) {
            // Create user with access rights combination name
        	final Integer id = oxusr.create(ctx, usr,accesscombinationname, auth).getId();
        	displayCreatedMessage(String.valueOf(id), ctx.getId(), parser);
        }else{
        	// to check if access rights were not supplied
        	final UserModuleAccess EMPTY_ACCESS_RIGHTS = new UserModuleAccess(); 
        	EMPTY_ACCESS_RIGHTS.disableAll();
        	
        	final UserModuleAccess access = new UserModuleAccess();
            // webmail package access per default
            access.disableAll();            
            // set module access rights supplied from cmd line
            setModuleAccessOptionsinUserChange(parser, access);
            
            // if NO rights were supplied from cmd line
            // we will call the create user method without any access
            // because then the create will use the context(admin) access rights.
            if(access.equals(EMPTY_ACCESS_RIGHTS)){
            	// call create without any access rights object
            	final Integer id = oxusr.create(ctx, usr, auth).getId();
            	displayCreatedMessage(String.valueOf(id), ctx.getId(), parser);
            }else{
            	// call OLD create with supplied access rights from cmd line
            	final Integer id = oxusr.create(ctx, usr,access, auth).getId();
            	displayCreatedMessage(String.valueOf(id), ctx.getId(), parser);
            }
        }
        
    }

    @Override
    protected void setFurtherOptions(final AdminParser parser) {
    	this.accessRightsCombinationName = setLongOpt(parser,OPT_ACCESSRIGHTS_COMBINATION_NAME,"Access combination name", true, false,true);
        
    }
}
