package com.openexchange.admin.console.context;

import java.rmi.Naming;

import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.console.AdminParser.NeededQuadState;
import com.openexchange.admin.rmi.OXContextInterface;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;

public class GetAccessCombinationName extends ContextAbstraction {
   
    public GetAccessCombinationName(final String[] args2) {

        final AdminParser parser = new AdminParser("getaccesscombinationnameforcontext");

        setOptions(parser);

        String successtext = null;
        
        try {
            parser.ownparse(args2);
            final Context ctx = contextparsing(parser);
            
            parseAndSetContextName(parser, ctx);
            
            successtext = nameOrIdSetInt(this.ctxid, this.contextname, "context");
            
            final Credentials auth = credentialsparsing(parser);

            // get rmi ref
            final OXContextInterface oxres = (OXContextInterface) Naming.lookup(RMI_HOSTNAME +OXContextInterface.RMI_NAME);

            // Fetch access combination name and print out           
            System.out.println(oxres.getAccessCombinationName(ctx, auth));
            
            // exit application
            sysexit(0);
        } catch (final Exception e) {
            printErrors(successtext, null, e, parser);
        }

    }
	
	public static void main(final String args[]) {
        new GetAccessCombinationName(args);
    }

    private void setOptions(final AdminParser parser) {
        setDefaultCommandLineOptionsWithoutContextID(parser);
        setContextOption(parser, NeededQuadState.eitheror);        
        setContextNameOption(parser, NeededQuadState.eitheror);
    }
}
