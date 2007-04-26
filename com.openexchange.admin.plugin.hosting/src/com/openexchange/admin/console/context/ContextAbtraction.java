package com.openexchange.admin.console.context;


import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.console.CmdLineParser.Option;
import com.openexchange.admin.console.user.UserAbstraction;

public abstract class ContextAbtraction extends UserAbstraction {   

    protected final static char OPT_NAME_SEARCH_PATTERN_SHORT = 's';
    protected final static String OPT_NAME_SEARCH_PATTERN_LONG = "searchpattern";
    
    protected final static char OPT_NAME_COMMON_ID_SHORT = 'i';
    protected final static String OPT_NAME_COMMON_ID_LONG  = "id";
    
    private final static char OPT_REASON_SHORT = 'r';
    private final static String OPT_REASON_LONG= "reason";
    
    private final static char OPT_QUOTA_SHORT = 'q';
    private final static String OPT_QUOTA_LONG = "quota";
    
    
    protected void setSearchOption(AdminParser parser,boolean required){
        searchOption = setShortLongOpt(parser, OPT_NAME_SEARCH_PATTERN_SHORT,OPT_NAME_SEARCH_PATTERN_LONG,"Search/List pattern!",true, required);
    }
    
    protected void setCommonIDOption(AdminParser parser,boolean required ){
        commonIDOption = setShortLongOpt(parser, OPT_NAME_COMMON_ID_SHORT,OPT_NAME_COMMON_ID_LONG,"Object Id",true, required);
    }
    
    protected void setContextIDOption(AdminParser parser,boolean required ){
        contextIDOption = setShortLongOpt(parser, OPT_NAME_CONTEXT_SHORT,OPT_NAME_CONTEXT_LONG,OPT_NAME_CONTEXT_DESCRIPTION,true, required);
    }
    
    protected void setDefaultCommandLineOptions(AdminParser parser){          
        
        getAdminUserOption(parser);
        getAdminPassOption(parser);        
        
    }
    
    protected void setMaintenanceReasodIDOption(AdminParser parser,boolean required){
        maintenanceReasonIDOption = setShortLongOpt(parser, OPT_REASON_SHORT,OPT_REASON_LONG,"Maintenance reason id",true, required);
    }
    
    protected void setContextQuotaOption(AdminParser parser,boolean required ){
        filestoreContextQuotaOption = setShortLongOpt(parser, OPT_QUOTA_SHORT,OPT_QUOTA_LONG,"How much quota the context can use for filestore",true, required);
    }
    
    protected Option searchOption = null;
    protected Option commonIDOption = null;
    protected Option contextIDOption = null;
    protected Option maintenanceReasonIDOption = null;
    protected Option filestoreContextQuotaOption = null;
    
}
