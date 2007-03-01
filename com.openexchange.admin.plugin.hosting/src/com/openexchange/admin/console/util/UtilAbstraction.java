package com.openexchange.admin.console.util;

import org.apache.commons.cli.Option;
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
    
    protected Option addArgName(final Option option, final String argname) {
        final Option retval = option;
        retval.setArgName(argname);
        return retval;
    }
    
    protected Option addDefaultArgName(final Option option) {
        return addArgName(option, option.getLongOpt());
    }
    
    protected Option getShortLongOptWithDefault(final String shortopt, final String longopt, final String description, final String defaultvalue, final boolean hasarg, final boolean required) {
        final StringBuilder desc = new StringBuilder();
        desc.append(description);
        desc.append(". Default: ");
        desc.append(defaultvalue);
        final Option retval = new Option(shortopt, longopt, hasarg, desc.toString());
        retval.setRequired(required);
        return retval;
    }



}
