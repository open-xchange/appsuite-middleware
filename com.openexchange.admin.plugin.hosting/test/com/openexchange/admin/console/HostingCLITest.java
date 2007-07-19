package com.openexchange.admin.console;

import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;

import com.openexchange.admin.tools.ShellExecutor;
import com.openexchange.admin.tools.ShellExecutor.ArrayOutput;

public class HostingCLITest {

    private static final String ADMIN_USER = "oxadmin";
    private static final String ADMIN_PW = "secret";
    private static final String MASTER_PW = "secret";
    private static final String OXADMINMASTER = "oxadminmaster";
    private static String ctxid = "666";

    @After
    public void teardown() throws IOException, InterruptedException {
        final ShellExecutor se = new ShellExecutor();
        final ArrayOutput deleteresult = se.executeprocargs(new String[] {
                "deletecontext", "-c", ctxid, "-A", OXADMINMASTER, "-P",
                MASTER_PW });
        assertTrue("Deleting of context failed", deleteresult.exitstatus==0);
    }
    
    @Test
    public void testall() throws IOException, InterruptedException {
        final ShellExecutor se = new ShellExecutor();
        final ArrayOutput result = se.executeprocargs(new String[] {
                "createcontext", "-c", ctxid, "-A", OXADMINMASTER, "-P",
                MASTER_PW, "-N", "test", "-q", "1000", "-u", ADMIN_USER, "-d",
                "admin", "-g", "admin", "-s", "admin", "-e", "xyz@bla.de",
                "-p", ADMIN_PW });
        assertTrue("Creation of context failed: " + result.errOutput, result.exitstatus==0);
        
        
        final ArrayOutput listresult = se.executeprocargs(new String[] {
                "listcontext", "-A", OXADMINMASTER, "-P", MASTER_PW, "--csv" });
        assertTrue("Listing of contexts failed", listresult.exitstatus==0);
        
        // Check right context id
        final int column = getCSVColumnFor(listresult.stdOutput.get(0), "id");
        assertTrue("Couldn't find column heading", column != -1);
        final int row = getCSVRow(listresult.stdOutput, column, ctxid);
        assertTrue("Couldn't find value for context", row != -1);
        
    }
    
    private int getCSVColumnFor(final String csvcolumns, final String columnname) {
        final String[] columns = csvcolumns.split(",");
        for (int i = 0; i < columns.length; i++) {
            if (columns[i].equals(columnname)) {
                return i;
            }
        }
        return -1;
    }
    
    private int getCSVRow(final ArrayList<String> csvdata, final int column, final String value) {
        for (int i = 0; i < csvdata.size(); i++) {
            final String[] split = csvdata.get(i).split(",");
            if (split[column].equals("\"" + value + "\"")) {
                return i;
            }
        }
        return -1;
    }
    
    private String getCSVValue(final ArrayList<String> csvdata, final int column, final int row) {
        final String line = csvdata.get(row);
        final String[] values = line.split(",");
        return values[column].replace("\"", "");
    }
    
//    Some test calls:
//    registerdatabase -A oxadminmaster -P secret -n dennis2 -H sevy -u oxadmin -p secret -m true
//    changedatabase -A oxadminmaster -P secret -l 20 -i 11
//    listdatabase -A oxadminmaster -P secret
//    changedatabase -A oxadminmaster -P secret -l 20 -i 13
//    changedatabase -A oxadminmaster -P secret -l true -i 13
//    changedatabase -A oxadminmaster -P secret -l false -i 13
//    unregisterdatabase -A oxadminmaster -P secret -i 13
//    registerdatabase -A oxadminmaster -P secret -n dennis2 -H sevy -u oxadmin -p secret -m true
//    changedatabase -A oxadminmaster -P secret -l false -i 15
//    changedatabase -A oxadminmaster -P secret -l false -i 15 --help
//    unregisterdatabase -A oxadminmaster -P secret -i 15
//    registerdatabase -A oxadminmaster -P secret -n dennis2 -H sevy -u oxadmin -p secret -m true
//    listdatabase -A oxadminmaster -P secret
//    registerdatabase -A oxadminmaster -P secret -n dennis2 -H sevy -u oxadmin -p secret -m true
//    registerdatabase -A oxadminmaster -P secret -n dennis2 -H sevy -u oxadmin -p secret -m true
//    registerdatabase -A oxadminmaster -P secret -n dennis3 -H sevy -u oxadmin -p secret -m true
//    registerdatabase -A oxadminmaster -P secret -n dennis4 -H sevy -u oxadmin -p secret -m true
//    changedatabase -A oxadminmaster -P secret -l false -i 19
//    changedatabase -A oxadminmaster -P secret -l true -i 19
//    registerdatabase -A oxadminmaster -P secret -n dennis4 -H sevy
//    registerdatabase -A oxadminmaster -P secret -n dennis4 -H sevy -p secret -m true
//    unregisterdatabase -A oxadminmaster -P secret -i 19
//    changedatabase -A oxadminmaster -P secret -a 50 -i 19
//    changedatabase -A oxadminmaster -P secret -a 50 -i 23
//    changedatabase -A oxadminmaster -P secret -o 3 -i 23
//    changedatabase -A oxadminmaster -P secret -i 23
//    changedatabase -A oxadminmaster -P secret -i 27
//    changedatabase -A oxadminmaster -P secret -i 27 --help
//    changedatabase -A oxadminmaster -P secret -i 27 -M 1
//    changedatabase -A oxadminmaster -P secret -i 27 -m false -M 1
//    changedatabase -A oxadminmaster -P secret -i 27 -x 500
//    changedatabase -A oxadminmaster -P secret -i 27 -w 500
//    changedatabase -A oxadminmaster -P secret -i 27 -w 50
//    registerdatabase -A oxadminmaster -P secret -n dennis4 -H sevy -p secret -m true --help
//    registerdatabase -A oxadminmaster -P secret -n dennis4 -H sevy -p secret -m true -o 5
//    registerdatabase -A oxadminmaster -P secret -n dennis4 -H sevy -p secret -m true -a 100
//    registerdatabase -A oxadminmaster -P secret -n dennis4 -H sevy -p secret -m true -a 105
//    registerdatabase -A oxadminmaster -P secret -n dennis4 -H sevy -p secret -m true -l 23
//    registerdatabase -A oxadminmaster -P secret -n dennis4 -H sevy -p secret -m true -l true

}
