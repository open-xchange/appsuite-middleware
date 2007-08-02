package com.openexchange.admin.console.util.database;

import java.net.URI;
import java.net.URISyntaxException;
import java.rmi.Naming;
import java.util.ArrayList;

import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.rmi.OXUtilInterface;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.Database;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;

/**
 * 
 * @author d7,cutmasta
 *
 */
public class ListDatabases extends DatabaseAbstraction {

    public ListDatabases(final String[] args2) {

        final AdminParser parser = new AdminParser("listdatabases");

        setOptions(parser);
        setCSVOutputOption(parser);
        try {
            parser.ownparse(args2);

            final Credentials auth = credentialsparsing(parser);
            
            // get rmi ref
            final OXUtilInterface oxutil = (OXUtilInterface) Naming.lookup(RMI_HOSTNAME +OXUtilInterface.RMI_NAME);
            
            String searchpattern = "*";
            if (parser.getOptionValue(this.searchOption) != null) {
                searchpattern = (String)parser.getOptionValue(this.searchOption);
            }
            final Database[] databases = oxutil.listDatabases(searchpattern, auth);
            
            if (null != parser.getOptionValue(this.csvOutputOption)) {
                precsvinfos(databases);
            } else {
                sysoutOutput(databases);
            }

            sysexit(0);
        } catch (final Exception e) {
            printErrors(null, ctxid, e, parser);
        }

    }

    private void sysoutOutput(Database[] databases) throws InvalidDataException, URISyntaxException {
        final ArrayList<ArrayList<String>> data = new ArrayList<ArrayList<String>>();
        for (final Database database : databases) {
            data.add(makeStandardData(database, false));
        }
        
        doOutput(new String[] { "3r", "10l", "20l", "7l", "7r", "7r", "7r", "7r", "6l", "4r", "7r" },
                 new String[] { "id", "name", "hostname", "master", "mid", "weight", "maxctx", "curctx", "hlimit", "max", "inital" }, data);
    }

    private void precsvinfos(Database[] databases) throws URISyntaxException {
        // needed for csv output, KEEP AN EYE ON ORDER!!!
        final ArrayList<String> columns = new ArrayList<String>();
        columns.add("id");
        columns.add("display_name");
        columns.add("url");
        columns.add("master");
        columns.add("masterid");
        columns.add("clusterweight");
        columns.add("maxUnits");
        columns.add("currentunits");
        columns.add("poolHardLimit");
        columns.add("poolMax");
        columns.add("poolInitial");
        columns.add("login");
        columns.add("password");
        columns.add("driver");
        columns.add("read_id");
        columns.add("scheme");
        
        final ArrayList<ArrayList<String>> data = new ArrayList<ArrayList<String>>();
        
        for (final Database database : databases) {
            data.add(makeCSVData(database));
        }
        
        doCSVOutput(columns, data);
    }

    public static void main(final String args[]) {
        new ListDatabases(args);
    }

    private void setOptions(final AdminParser parser) {
        setDefaultCommandLineOptionsWithoutContextID(parser);
        setSearchOption(parser);       
    }
    
    /**
     * @param db
     * @return
     * @throws URISyntaxException
     */
    private ArrayList<String> makeCSVData(final Database db) throws URISyntaxException{
        final ArrayList<String> rea_data = makeStandardData(db, true);
        
        if (null != db.getLogin()) {
            rea_data.add(db.getLogin());
        } else {
            rea_data.add(null);
        }

        if (null != db.getPassword()) {
            rea_data.add(db.getPassword());
        } else {
            rea_data.add(null);
        }

        if (null != db.getDriver()) {
            rea_data.add(db.getDriver());
        } else {
            rea_data.add(null);
        }

        if (null != db.getRead_id()) {
            rea_data.add(db.getRead_id().toString());
        } else {
            rea_data.add(null);
        }

        if (null != db.getScheme()) {
            rea_data.add(db.getScheme().toString());
        } else {
            rea_data.add(null);
        }
        
        
        return rea_data;
    }

    private ArrayList<String> makeStandardData(final Database db, boolean csv) throws URISyntaxException {
        final ArrayList<String> rea_data = new ArrayList<String>();
        
        rea_data.add(db.getId().toString());
        
        if (null != db.getName()) {
            rea_data.add(db.getName());
        } else {
            rea_data.add(null);
        }
        
        if (null != db.getUrl()) {
            if (csv) {
                rea_data.add(db.getUrl());
            } else {
                rea_data.add(new URI(db.getUrl().substring("jdbc:".length())).getHost());
            }
        } else {
            rea_data.add(null);
        }
        
        if (null != db.isMaster()) {
            rea_data.add(db.isMaster().toString());
        } else {
            rea_data.add(null);
        }
        
        if (null != db.getMasterId()) {
            rea_data.add(db.getMasterId().toString());
        } else {
            rea_data.add(null);
        }
        
        if (null != db.getClusterWeight()) {
            rea_data.add(db.getClusterWeight().toString());
        } else {
            rea_data.add(null);
        }
        
        if (null != db.getMaxUnits()) {
            rea_data.add(db.getMaxUnits().toString());
        } else {
            rea_data.add(null);
        }
        
        if (null != db.getCurrentUnits()) {
            rea_data.add(db.getCurrentUnits().toString());
        } else {
            rea_data.add(null);
        }
        
        if (null != db.getPoolHardLimit()) {
            if (csv) {
                rea_data.add(db.getPoolHardLimit().toString());
            } else {
                rea_data.add(db.getPoolHardLimit() > 0 ? "true" : "false");
            }
        } else {
            rea_data.add(null);
        }
        
        if (null != db.getPoolMax()) {
            rea_data.add(db.getPoolMax().toString());
        } else {
            rea_data.add(null);
        }
        
        if (null != db.getPoolInitial()) {
            rea_data.add(db.getPoolInitial().toString());
        } else {
            rea_data.add(null);
        }
        return rea_data;
    }
    
    @Override
    protected final String getObjectName() {
        return "databases";
    }

}
