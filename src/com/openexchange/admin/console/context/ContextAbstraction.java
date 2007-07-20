package com.openexchange.admin.console.context;


import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.console.CmdLineParser.Option;
import com.openexchange.admin.console.user.UserAbstraction;

public abstract class ContextAbstraction extends UserAbstraction {   

    protected final static char OPT_NAME_COMMON_ID_SHORT = 'i';
    protected final static String OPT_NAME_COMMON_ID_LONG  = "id";
    
    private final static char OPT_QUOTA_SHORT = 'q';
    private final static String OPT_QUOTA_LONG = "quota";
    
    protected Option searchOption = null;
    protected Option commonIDOption = null;
    protected Option contextQuotaOption = null;

    protected void setCommonIDOption(final AdminParser parser,final boolean required ){
        this.commonIDOption = setShortLongOpt(parser, OPT_NAME_COMMON_ID_SHORT,OPT_NAME_COMMON_ID_LONG,"Object Id",true, convertBooleantoTriState(required));
    }
    
    protected void setContextQuotaOption(final AdminParser parser,final boolean required ){
        this.contextQuotaOption = setShortLongOpt(parser, OPT_QUOTA_SHORT,OPT_QUOTA_LONG,"How much quota the context.",true, convertBooleantoTriState(required));
    }
    
    protected void setContextNameOption(final AdminParser parser,final boolean required ){
        this.contextNameOption = setShortLongOpt(parser, OPT_NAME_CONTEXT_NAME_SHORT,OPT_NAME_CONTEXT_NAME_LONG,OPT_NAME_CONTEXT_NAME_DESCRIPTION,true, convertBooleantoTriState(required));
    }
}
