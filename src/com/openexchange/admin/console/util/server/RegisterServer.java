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
public class RegisterServer extends ServerAbstraction {

    public RegisterServer(final String[] args2) {

        final AdminParser parser = new AdminParser("registerserver");

        setOptions(parser);

        try {
            parser.ownparse(args2);

            final Credentials auth = credentialsparsing(parser);

            // get rmi ref
            final OXUtilInterface oxutil = (OXUtilInterface) Naming.lookup(RMI_HOSTNAME + OXUtilInterface.RMI_NAME);

            final Server srv = new Server();

            srv.setName((String) parser.getOptionValue(this.serverNameOption));

            displayRegisteredMessage(String.valueOf(oxutil.registerServer(srv, auth).getId()), parser);
            sysexit(0);
        } catch (final Exception e) {
            printErrors(null, null, e, parser);
        }
    }

    public static void main(final String args[]) {
        new RegisterServer(args);
    }

    private void setOptions(final AdminParser parser) {
        setDefaultCommandLineOptionsWithoutContextID(parser);

        setServernameOption(parser, NeededQuadState.needed);
    }
}
