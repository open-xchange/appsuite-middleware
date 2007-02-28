package com.openexchange.admin.console.util;

import org.apache.commons.cli.Options;

import com.openexchange.admin.console.BasicCommandlineOptions;

public class UtilAbstraction extends BasicCommandlineOptions {
    /**
     * 
     * @return Options containing context,adminuser,adminpass Option objects.
     */
    protected Options getDefaultCommandLineOptions(){
        
        Options options = new Options();
        
        options.addOption(getAdminUserOption());
        options.addOption(getAdminPassOption());
        
        return options;
    }
}
