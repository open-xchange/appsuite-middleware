package com.openexchange.admin.console.context;


import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.console.CmdLineParser.Option;
import com.openexchange.admin.console.user.UserAbstraction;

public abstract class ContextAbtraction extends UserAbstraction {   

    protected final static char OPT_NAME_SEARCH_PATTERN_SHORT = 's';
    protected final static String OPT_NAME_SEARCH_PATTERN_LONG = "searchpattern";
    
    protected final static char OPT_NAME_COMMON_ID_SHORT = 'i';
    protected final static String OPT_NAME_COMMON_ID_LONG  = "id";
    
    protected void setSearchOption(AdminParser parser){
        searchOption = setShortLongOpt(parser, OPT_NAME_SEARCH_PATTERN_SHORT,OPT_NAME_SEARCH_PATTERN_LONG,"Search/List pattern!",true, false);
    }
    
    protected void setCommonIDOption(AdminParser parser,boolean required ){
        commonIDOption = setShortLongOpt(parser, OPT_NAME_COMMON_ID_SHORT,OPT_NAME_COMMON_ID_LONG,"Object Id",true, required);
    }
    
    protected void setDefaultCommandLineOptions(AdminParser parser){          
        
        getAdminUserOption(parser);
        getAdminPassOption(parser);        
        
    }
    
    protected Option searchOption = null;
    protected Option commonIDOption = null;
    
}
