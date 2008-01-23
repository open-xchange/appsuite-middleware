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
import com.openexchange.admin.rmi.dataobjects.UserModuleAccess;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.NoSuchContextException;
import com.openexchange.admin.rmi.exceptions.StorageException;

public class Change extends ChangeCore {

    private final ContextHostingAbstraction ctxabs = new ContextHostingAbstraction();
    
    public Change(final String[] args2) {
        final AdminParser parser = new AdminParser("changecontext");

        commonfunctions(parser, args2);
    }

    public static void main(final String args[]) {
        new Change(args);
    }

    @Override
    protected void maincall(final AdminParser parser, final Context ctx, final Credentials auth) throws MalformedURLException, RemoteException, NotBoundException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException {
        // get rmi ref
        final OXContextInterface oxctx = (OXContextInterface) Naming.lookup(RMI_HOSTNAME +OXContextInterface.RMI_NAME);

        // add login mappings
        ctxabs.parseAndSetAddLoginMapping(parser);
        
        // remove login mappings
        ctxabs.parseAndSetRemoveLoginMapping(parser);
        
        ctxabs.changeMappingSetting(oxctx, ctx, auth, true);
                
        // do the change
        oxctx.change(ctx, auth);
        
        
        // needed for comparison
        UserModuleAccess NO_RIGHTS_ACCESS = new UserModuleAccess();
        NO_RIGHTS_ACCESS.disableAll();
        
        // now check which create method we must call, 
        // this depends on the access rights supplied by the client
        UserModuleAccess parsed_access = new UserModuleAccess();
        parsed_access.disableAll();
        
        // parse access options
        setModuleAccessOptionsinUserChange(parser, parsed_access);
        
        String accessCombinationName = ctxabs.parseAndSetAccessCombinationName(parser);
        
        if(!parsed_access.equals(NO_RIGHTS_ACCESS) && null != accessCombinationName){
        	// BOTH WAYS TO SPECIFY ACCESS RIGHTS ARE INVALID!
        	throw new InvalidDataException(UserHostingAbstraction.ACCESS_COMBINATION_NAME_AND_ACCESS_RIGHTS_DETECTED_ERROR);        	
        }        
       
        if (null != accessCombinationName ) {
        	// Client supplied access combination name. change context with this name
        	oxctx.changeModuleAccess(ctx,  accessCombinationName, auth);
        }
        
        if(!parsed_access.equals(NO_RIGHTS_ACCESS)){
        	// Client supplied access attributes
        	oxctx.changeModuleAccess(ctx,  parsed_access, auth);
        }
        
    }

    @Override
    protected void setFurtherOptions(final AdminParser parser) {
    	parser.setExtendedOptions();
        ctxabs.setAddMappingOption(parser, false);        
        ctxabs.setRemoveMappingOption(parser, false);
        ctxabs.setAddAccessRightCombinationNameOption(parser, false);
        setModuleAccessOptions(parser);
    }
}
