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
    
    protected void displayRegisteredMessage(final String id, final AdminParser parser) {
        createMessageForStdout(id, null, "registered", parser);
    }

    protected void displayUnregisteredMessage(final String id, final AdminParser parser) {
        createMessageForStdout(id, null, "unregistered", parser);
    }

    @Override
    protected void printFirstPartOfErrorText(final String id, final Integer ctxid, final AdminParser parser) {
        // Be aware of the order register matches also unregister so unregister must
        // be checked first
        if (getClass().getName().matches("^.*\\.\\w*(?i)unregister\\w*$")) {
            createMessageForStderr(id, ctxid, "could not be unregistered: ", parser);
        } else if (getClass().getName().matches("^.*\\.\\w*(?i)register\\w*$")) {
            createMessageForStderr(id, ctxid, "could not be registered: ", parser);
        } else {
            super.printFirstPartOfErrorText(id, ctxid, parser);
        }
    }
}
