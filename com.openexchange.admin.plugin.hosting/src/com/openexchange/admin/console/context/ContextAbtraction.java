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
    
    private final static char OPT_CONTEXT_ADD_LOGIN_MAPPINGS_SHORT = 'L';
    private final static String OPT_CONTEXT_ADD_LOGIN_MAPPINGS_LONG = "addmapping";
    
    private final static char OPT_CONTEXT_REMOVE_LOGIN_MAPPINGS_SHORT = 'R';
    private final static String OPT_CONTEXT_REMOVE_LOGIN_MAPPINGS_LONG = "removemapping";
    
    protected Option searchOption = null;
    protected Option commonIDOption = null;
    protected Option contextNameOption = null;
    protected Option addLoginMappingOption = null;
    protected Option removeLoginMappingOption = null;
    protected Option maintenanceReasonIDOption = null;
    protected Option filestoreContextQuotaOption = null;

    protected void setSearchOption(final AdminParser parser,final boolean required){
        this.searchOption = setShortLongOpt(parser, OPT_NAME_SEARCH_PATTERN_SHORT,OPT_NAME_SEARCH_PATTERN_LONG,"Search/List pattern!",true, convertBooleantoTriState(required));
    }
    
    protected void setCommonIDOption(final AdminParser parser,final boolean required ){
        this.commonIDOption = setShortLongOpt(parser, OPT_NAME_COMMON_ID_SHORT,OPT_NAME_COMMON_ID_LONG,"Object Id",true, convertBooleantoTriState(required));
    }
    
    protected void setMaintenanceReasodIDOption(final AdminParser parser,final boolean required){
        this.maintenanceReasonIDOption = setShortLongOpt(parser, OPT_REASON_SHORT,OPT_REASON_LONG,"Maintenance reason id",true, convertBooleantoTriState(required));
    }
    
    protected void setContextQuotaOption(final AdminParser parser,final boolean required ){
        this.filestoreContextQuotaOption = setShortLongOpt(parser, OPT_QUOTA_SHORT,OPT_QUOTA_LONG,"How much quota the context can use for filestore",true, convertBooleantoTriState(required));
    }
    
    protected void setContextNameOption(final AdminParser parser,final boolean required ){
        this.contextNameOption = setShortLongOpt(parser, OPT_NAME_CONTEXT_NAME_SHORT,OPT_NAME_CONTEXT_NAME_LONG,OPT_NAME_CONTEXT_NAME_DESCRIPTION,true, convertBooleantoTriState(required));
    }
    
    protected void setAddMappingOption(final AdminParser parser,final boolean required ){
        this.addLoginMappingOption = setShortLongOpt(parser, OPT_CONTEXT_ADD_LOGIN_MAPPINGS_SHORT,OPT_CONTEXT_ADD_LOGIN_MAPPINGS_LONG,"Add login mappings.Seperated by \",\"",true, convertBooleantoTriState(required));
    }
    
    protected void setRemoveMappingOption(final AdminParser parser,final boolean required ){
        this.removeLoginMappingOption = setShortLongOpt(parser, OPT_CONTEXT_REMOVE_LOGIN_MAPPINGS_SHORT,OPT_CONTEXT_REMOVE_LOGIN_MAPPINGS_LONG,"Remove login mappings.Seperated by \",\"",true, convertBooleantoTriState(required));
    }
}
