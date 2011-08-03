
package com.openexchange.admin.console.context.extensioninterfaces;

import java.util.ArrayList;
import com.openexchange.admin.plugins.PluginException;
import com.openexchange.admin.rmi.dataobjects.Context;


/**
 * This interface must be implemented by a class in the console package of a plugin so that it
 * can extend the basic command line options. To offer your own implementation of this interface
 * to the core. The ServiceLoader mechanism of JDK 6 is used which requests a directory
 * META-INF/services under which a text file whose name is the full-qualified binary name
 * of this interface (com.openexchange.admin.console.context.ContextConsoleListInterface). And the
 * content of this file must be the full-qualified binary name of your implementation.
 * 
 * @author <a href="mailto:dennis.sieben@open-xchange.com">Dennis Sieben</a>
 * 
 */
public interface ContextConsoleListInterface extends ContextConsoleCommonInterface {

    /**
     * This method can be implemented to set the names of the columns which should extend the normal output
     * 
     * @return
     */
    public ArrayList<String> getColumnNamesHumanReadable();

    /**
     * This method can be implemented to set the names of the columns which should extend the csv output
     * 
     * @return
     */
    public ArrayList<String> getColumnNamesCSV();
    
    /**
     * This method can be implemented to set the data in the normal output.
     * Note: If the data is empty null must be inserted in the array at that point.
     * 
     * @return
     * @throws PluginException 
     */
    public ArrayList<String> getHumanReadableData(final Context ctx) throws PluginException;
    
    /**
     * This method can be implemented to set the data in the CSV output.
     * Note: If the data is empty null must be inserted in the array at that point.
     * 
     * @return
     * @throws PluginException 
     */
    public ArrayList<String> getCSVData(final Context ctx) throws PluginException;

}
