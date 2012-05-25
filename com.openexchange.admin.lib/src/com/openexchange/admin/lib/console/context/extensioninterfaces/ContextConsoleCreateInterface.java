
package com.openexchange.admin.lib.console.context.extensioninterfaces;

import java.util.HashMap;
import com.openexchange.admin.lib.console.exception.OXConsolePluginException;
import com.openexchange.admin.lib.console.user.UserAbstraction.CSVConstants;
import com.openexchange.admin.lib.rmi.dataobjects.Context;


/**
 * This interface must be implemented by a class in the console package of a plugin so that it
 * can extend the basic command line options. To offer your own implementation of this interface
 * to the core. The ServiceLoader mechanism of JDK 6 is used which requests a directory
 * META-INF/services under which a text file whose name is the full-qualified binary name
 * of this interface (com.openexchange.admin.console.context.ContextConsoleCreateInterface). And the
 * content of this file must be the full-qualified binary name of your implementation.
 * 
 * @author <a href="mailto:dennis.sieben@open-xchange.com">Dennis Sieben</a>
 * 
 */
public interface ContextConsoleCreateInterface extends ContextConsoleCommonInterface {

    /**
     * This method processes a {@link HashMap} of CSVConstants with their name. This method can be used to
     * modify the map, so you can add or remove parameter which can be used in the CSV file. Or you can
     * change if a parameter is required or not
     * 
     * @param constantsMap - the {@link HashMap}
     */
    public void processCSVConstants(HashMap<String, CSVConstants> constantsMap);

    /**
     * This method processes a single line from a CSV file and adds the results to the corresponding context
     * object
     * 
     * @param nextLine
     * @param idarray
     * @param context 
     * @throws OXConsolePluginException 
     */
    public void applyExtensionValuesFromCSV(final String[] nextLine, final int[] idarray, final Context context) throws OXConsolePluginException;

}
