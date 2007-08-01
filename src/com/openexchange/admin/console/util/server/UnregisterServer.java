package com.openexchange.admin.console.util.server;

import java.rmi.Naming;

import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.console.AdminParser.NeededTriState;
import com.openexchange.admin.console.CmdLineParser.Option;
import com.openexchange.admin.rmi.OXUtilInterface;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.Server;

/**
 * 
 * @author d7,cutmasta
 * 
 */
public class UnregisterServer extends ServerAbstraction {

    // Setting names for options
    private final static char OPT_NAME_SERVER_ID_SHORT = 'i';

    private final static String OPT_NAME_SERVER_ID_LONG = "id";

    private Option serverIdOption = null;

    public UnregisterServer(final String[] args2) {

        AdminParser parser = new AdminParser("unregisterserver");

        setOptions(parser);

        String serverid = null;
        try {
            parser.ownparse(args2);

            final Credentials auth = credentialsparsing(parser);

            // get rmi ref
            final OXUtilInterface oxutil = (OXUtilInterface) Naming.lookup(RMI_HOSTNAME +OXUtilInterface.RMI_NAME);
            Server sv = new Server();
            serverid = (String) parser.getOptionValue(serverIdOption);
            sv.setId(Integer.parseInt(serverid));
            oxutil.unregisterServer(sv, auth);
            
            displayUnregisteredMessage(serverid);
            sysexit(0);
        } catch (final Exception e) {
            printErrors(serverid, null, e, parser);
        }
    }

    public static void main(final String args[]) {
        new UnregisterServer(args);
    }

    private void setOptions(AdminParser parser) {
        setDefaultCommandLineOptionsWithoutContextID(parser);

        serverIdOption = setShortLongOpt(parser, OPT_NAME_SERVER_ID_SHORT, OPT_NAME_SERVER_ID_LONG, "The id of the server which should be deleted", true, NeededTriState.needed);
    }
}
