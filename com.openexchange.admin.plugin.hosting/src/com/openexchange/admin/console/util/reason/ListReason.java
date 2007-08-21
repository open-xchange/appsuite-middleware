package com.openexchange.admin.console.util.reason;

import java.rmi.Naming;
import java.util.ArrayList;

import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.rmi.OXUtilInterface;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.MaintenanceReason;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;

/**
 * 
 * @author d7,cutmasta
 * 
 */
public class ListReason extends ReasonAbstraction {

    public ListReason(final String[] args2) {

        final AdminParser parser = new AdminParser("listreason");
        setOptions(parser);
        setCSVOutputOption(parser);
        try {
            parser.ownparse(args2);

            final Credentials auth = credentialsparsing(parser);

            // get rmi ref
            final OXUtilInterface oxutil = (OXUtilInterface) Naming.lookup(RMI_HOSTNAME +OXUtilInterface.RMI_NAME);

            String pattern = "*";
            if (parser.getOptionValue(this.searchOption) != null) {
                pattern = (String) parser.getOptionValue(this.searchOption);
            }

            final MaintenanceReason[] mrs = oxutil.listMaintenanceReason(pattern, auth);

            if (null != parser.getOptionValue(this.csvOutputOption)) {
                precsvinfos(mrs);
            } else {
                sysoutOutput(mrs);
            }

            sysexit(0);
        } catch (final Exception e) {
            printErrors(null, null, e, parser);
        }
    }

    public static void main(final String args[]) {
        new ListReason(args);
    }

    private void setOptions(final AdminParser parser) {
        setDefaultCommandLineOptionsWithoutContextID(parser);
        setSearchOption(parser);
    }

    private void sysoutOutput(final MaintenanceReason[] mrs) throws InvalidDataException {
        final ArrayList<ArrayList<String>> data = new ArrayList<ArrayList<String>>();
        for (final MaintenanceReason mr : mrs) {
            data.add(makeCSVData(mr));
        }
        
        //doOutput(new String[] { "3r", "72l" }, new String[] { "Id", "Text" }, data);
        doOutput(new String[] { "r", "l" }, new String[] { "Id", "Text" }, data);
    }

    private void precsvinfos(final MaintenanceReason[] mrs) throws InvalidDataException {
        // needed for csv output, KEEP AN EYE ON ORDER!!!
        final ArrayList<String> columns = new ArrayList<String>();
        columns.add("id");
        columns.add("text");
    
        // Needed for csv output
        final ArrayList<ArrayList<String>> data = new ArrayList<ArrayList<String>>();
    
        for (final MaintenanceReason mr : mrs) {
            data.add(makeCSVData(mr));
        }
    
        doCSVOutput(columns, data);
    }

    private ArrayList<String> makeCSVData(final MaintenanceReason mr) {
        final ArrayList<String> rea_data = new ArrayList<String>();
        rea_data.add(mr.getId().toString());
        rea_data.add(mr.getText());
    
        return rea_data;
    }
    
    @Override
    protected final String getObjectName() {
        return "reasons";
    }
}
