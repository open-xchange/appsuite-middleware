package com.openexchange.admin.console.context;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.console.user.UserHostingAbstraction;
import com.openexchange.admin.rmi.OXContextInterface;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.rmi.dataobjects.UserModuleAccess;
import com.openexchange.admin.rmi.exceptions.ContextExistsException;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.NoSuchContextException;
import com.openexchange.admin.rmi.exceptions.StorageException;

public class Create extends CreateCore {
	
    private final ContextHostingAbstraction ctxabs = new ContextHostingAbstraction();
    
    public Create(final String[] args2) {

        final AdminParser parser = new AdminParser("createcontext");

        commonfunctions(parser, args2);
    }

    public static void main(final String args[]) {
        new Create(args);
    }

    @Override
    protected void setFurtherOptions(final AdminParser parser) {
    	 parser.setExtendedOptions();
    	 ctxabs.setAddMappingOption(parser, false);
    	 ctxabs.setAddAccessRightCombinationNameOption(parser, false);
    	 setModuleAccessOptions(parser);
    	 
    }

    @Override
    protected Context maincall(final AdminParser parser, final Context ctx, final User usr, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException, MalformedURLException, NotBoundException, ContextExistsException, NoSuchContextException {
        // get rmi ref
        final OXContextInterface oxctx = (OXContextInterface) Naming.lookup(RMI_HOSTNAME +OXContextInterface.RMI_NAME);

        // add login mappings
        ctxabs.parseAndSetAddLoginMapping(parser);

        ctxabs.changeMappingSetting(oxctx, ctx, auth, false);

        Context createdctx = null;
        
        // needed for comparison
        UserModuleAccess NO_RIGHTS_ACCESS = new UserModuleAccess();
        NO_RIGHTS_ACCESS.disableAll();
        
        // now check which create method we must call, 
        // this depends on the access rights supplied by the client
        UserModuleAccess parsed_access = new UserModuleAccess();
        parsed_access.disableAll();
        
        // parse access options
        setModuleAccessOptionsinUserCreate(parser, parsed_access);
        
        String accessCombinationName = ctxabs.parseAndSetAccessCombinationName(parser);
        
        if(!parsed_access.equals(NO_RIGHTS_ACCESS) && null != accessCombinationName){
        	// BOTH WAYS TO SPECIFY ACCESS RIGHTS ARE INVALID!
        	throw new InvalidDataException(UserHostingAbstraction.ACCESS_COMBINATION_NAME_AND_ACCESS_RIGHTS_DETECTED_ERROR);        	
        }
        
        if (null != accessCombinationName ) {
        	// Client supplied access combination name. create context with this name
        	createdctx = oxctx.create(ctx, usr, accessCombinationName, auth);
        }else if(!parsed_access.equals(NO_RIGHTS_ACCESS)){
        	// Client supplied access attributes
        	createdctx = oxctx.create(ctx, usr,parsed_access, auth);
        }else{
        	createdctx = oxctx.create(ctx, usr, auth);
        }
        
        // TODO: We have to add a cleanup here. If creation of mappings fails the context should be deleted
        return createdctx;
    }
}
