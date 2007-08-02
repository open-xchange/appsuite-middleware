package com.openexchange.admin.console.util;

import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.console.ObjectNamingAbstraction;
import com.openexchange.admin.console.AdminParser.NeededQuadState;
import com.openexchange.admin.console.CmdLineParser.Option;

/**
 * 
 * @author d7,cutmasta
 *
 */
public abstract class UtilAbstraction extends ObjectNamingAbstraction {
    
    // for all tools
    protected Option searchOption = null;

    //  Setting names for options
    protected final static char OPT_NAME_SEARCH_PATTERN_SHORT = 's';
    protected final static String OPT_NAME_SEARCH_PATTERN_LONG = "searchpattern";
    
    protected void setSearchOption(final AdminParser parser){
        this.searchOption = setShortLongOpt(parser, OPT_NAME_SEARCH_PATTERN_SHORT,OPT_NAME_SEARCH_PATTERN_LONG,"Search/List pattern!",true, NeededQuadState.notneeded);
    }
    
    protected void displayRegisteredMessage(final String id) {
        createMessageForStdout(id, null, "registered");
    }

    protected void displayUnregisteredMessage(final String id) {
        createMessageForStdout(id, null, "unregistered");
    }

    @Override
    protected void printFirstPartOfErrorText(final String id, final Integer ctxid) {
        // Be aware of the order register matches also unregister so unregister must
        // be checked first
        if (getClass().getName().matches("^.*\\.\\w*(?i)unregister\\w*$")) {
            createMessageForStderr(id, ctxid, "could not be unregistered: ");
        } else if (getClass().getName().matches("^.*\\.\\w*(?i)register\\w*$")) {
            createMessageForStderr(id, ctxid, "could not be registered: ");
        } else {
            super.printFirstPartOfErrorText(id, ctxid);
        }
    }
}
