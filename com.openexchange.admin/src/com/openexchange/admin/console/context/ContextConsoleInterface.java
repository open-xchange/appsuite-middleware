package com.openexchange.admin.console.context;

import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.rmi.dataobjects.Context;


/**
 * This interface must be implemented by a class in the console package of a plugin so that it
 * can extend the basic command line options.
 *
 * @author <a href="mailto:dennis.sieben@open-xchange.com">Dennis Sieben</a>
 *
 */
public interface ContextConsoleInterface {

    /**
     * This method adds the extension options which are provided by this plugin to the given
     * parser object
     * 
     * @param parser
     */
    public void addExtensionOptions(final AdminParser parser);
    
    /**
     * This method read the options which were set with the {@link #addExtensionOptions(AdminParser)}
     * method and fills them into an extension object which is used by this class. The extension
     * object is then returned
     * @param ctx TODO
     */
    public void setAndFillExtension(final AdminParser parser, final Context ctx);
    

}
