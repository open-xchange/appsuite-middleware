package com.openexchange.admin.console.util.server;

import java.rmi.Naming;
import java.util.ArrayList;

import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.rmi.OXUtilInterface;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.Server;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;

/**
 * 
 * @author d7
 * 
 */
public class ListServers extends ServerAbstraction {

    public ListServers(final String[] args2) {

        final AdminParser parser = new AdminParser("listservers");

        setOptions(parser);
        setCSVOutputOption(parser);

        try {
            parser.ownparse(args2);

            final Credentials auth = credentialsparsing(parser);

            // get rmi ref
            final OXUtilInterface oxutil = (OXUtilInterface) Naming.lookup(RMI_HOSTNAME +OXUtilInterface.RMI_NAME);

            String searchpattern = "*";
            if (parser.getOptionValue(this.searchOption) != null) {
                searchpattern = (String) parser.getOptionValue(this.searchOption);
            }
            // Setting the options in the dataobject
            final Server[] servers = oxutil.listServers(searchpattern, auth);

            if (null != parser.getOptionValue(this.csvOutputOption)) {
                precsvinfos(servers);
            } else {
                sysoutOutput(servers);
            }

            sysexit(0);
        } catch (final Exception e) {
            printErrors(null, null, e, parser);
        }
    }

    public static void main(final String args[]) {
        new ListServers(args);
    }

    private void setOptions(final AdminParser parser) {
        setDefaultCommandLineOptionsWithoutContextID(parser);
        setSearchOption(parser);
    }

    private void sysoutOutput(Server[] servers) throws InvalidDataException {
        final ArrayList<ArrayList<String>> data = new ArrayList<ArrayList<String>>();
        for (final Server server : servers) {
            data.add(makeCSVData(server));
        }
        
        doOutput(new String[] { "3r", "35l" }, new String[] { "Id", "Name" }, data);
    }

    private void precsvinfos(Server[] servers) {
        // needed for csv output, KEEP AN EYE ON ORDER!!!
        final ArrayList<String> columns = new ArrayList<String>();
        columns.add("id");
        columns.add("name");
    
        final ArrayList<ArrayList<String>> data = new ArrayList<ArrayList<String>>();
    
        for (final Server server : servers) {
            data.add(makeCSVData(server));
        }
    
        doCSVOutput(columns, data);
    }

    private ArrayList<String> makeCSVData(Server server) {
        final ArrayList<String> srv_data = new ArrayList<String>();
        srv_data.add(String.valueOf(server.getId()));
        final String servername = server.getName();
        if (servername != null) {
            srv_data.add(servername);
        } else {
            srv_data.add(null);
        }
        return srv_data;
    }

    @Override
    protected final String getObjectName() {
        return "servers";
    }

}
