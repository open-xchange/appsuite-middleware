
package com.openexchange.admin.console.user;

import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.console.AdminParser.NeededQuadState;
import com.openexchange.admin.rmi.OXUserInterface;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.User;

public class GetAccessCombinationName extends UserAbstraction {

    public static void main(final String[] args) {
        new GetAccessCombinationName(args);
    }
    
    protected final void setOptions(final AdminParser parser) {
        setDefaultCommandLineOptions(parser);
        setIdOption(parser);
        setUsernameOption(parser, NeededQuadState.eitheror);        
    }

    public GetAccessCombinationName(final String[] args2) {

        final AdminParser parser = new AdminParser("getaccesscombinationnameforuser");

        // set all needed options in our parser
        setOptions(parser);

        String successtext = null;
        
        try {
            parser.ownparse(args2);

            // create user obj
            final User usr = new User();
            
            parseAndSetUserId(parser, usr);
            parseAndSetUsername(parser, usr);

            successtext = nameOrIdSetInt(this.userid, this.username, "user");

            final Context ctx = contextparsing(parser);

            final Credentials auth = credentialsparsing(parser);

            // rmi interface
            final OXUserInterface oxusr = getUserInterface();
            
            // printout access name
            System.out.println(oxusr.getAccessCombinationName(ctx, usr, auth));
            
            sysexit(0);
        } catch (final Exception e) {
            printErrors(successtext, ctxid, e, parser);
        }
    }
   
}
