package com.openexchange.admin.console.util.server;

import java.rmi.Naming;

import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.console.AdminParser.NeededQuadState;
import com.openexchange.admin.rmi.OXUtilInterface;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.Server;

/**
 * 
 * @author d7,cutmasta
 * 
 */
public class UnregisterServer extends ServerAbstraction {

    public UnregisterServer(final String[] args2) {

        AdminParser parser = new AdminParser("unregisterserver");

        setOptions(parser);

        String successtext = null;
        try {
            parser.ownparse(args2);

            final Server sv = new Server();
            
            parseAndSetServerID(parser, sv);
            parseAndSetServername(parser, sv);
            
            successtext = nameOrIdSet(this.serverid, this.servername, "server");

            final Credentials auth = credentialsparsing(parser);

            // get rmi ref
            final OXUtilInterface oxutil = (OXUtilInterface) Naming.lookup(RMI_HOSTNAME + OXUtilInterface.RMI_NAME);

            oxutil.unregisterServer(sv, auth);
            
            displayUnregisteredMessage(successtext, parser);
            sysexit(0);
        } catch (final Exception e) {
            printErrors(successtext, null, e, parser);
        }
    }

    public static void main(final String args[]) {
        new UnregisterServer(args);
    }

    private void setOptions(AdminParser parser) {
        setDefaultCommandLineOptionsWithoutContextID(parser);

        setServeridOption(parser);
        setServernameOption(parser, NeededQuadState.eitheror);
    }
}
