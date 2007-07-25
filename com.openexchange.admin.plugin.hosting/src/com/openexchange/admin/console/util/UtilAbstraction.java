package com.openexchange.admin.console.util;

import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.console.ObjectNamingAbstraction;
import com.openexchange.admin.console.AdminParser.NeededTriState;
import com.openexchange.admin.console.CmdLineParser.Option;

/**
 * 
 * @author d7,cutmasta
 *
 */
public abstract class UtilAbstraction extends ObjectNamingAbstraction {
    
    //  Setting names for options
    protected final static char OPT_NAME_SEARCH_PATTERN_SHORT = 's';
    protected final static String OPT_NAME_SEARCH_PATTERN_LONG = "searchpattern";
    
    protected void setSearchOption(final AdminParser parser){
        this.searchOption = setShortLongOpt(parser, OPT_NAME_SEARCH_PATTERN_SHORT,OPT_NAME_SEARCH_PATTERN_LONG,"Search/List pattern!",true, NeededTriState.notneeded);
    }
    
    protected void displayRegisteredMessage(final Integer id) {
        createMessage(id, null, "registered");
    }

    protected void displayUnregisteredMessage(final Integer id) {
        createMessage(id, null, "unregistered");
    }

    // for all tools
    protected Option searchOption = null;
}
