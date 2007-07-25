package com.openexchange.admin.console.context;

import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.console.CmdLineParser.Option;

public class ContextHostingAbstraction extends ContextAbstraction {
    private final static char OPT_REASON_SHORT = 'r';
    private final static String OPT_REASON_LONG= "reason";

    private final static char OPT_CONTEXT_ADD_LOGIN_MAPPINGS_SHORT = 'L';
    private final static String OPT_CONTEXT_ADD_LOGIN_MAPPINGS_LONG = "addmapping";
    
    private final static char OPT_CONTEXT_REMOVE_LOGIN_MAPPINGS_SHORT = 'R';
    private final static String OPT_CONTEXT_REMOVE_LOGIN_MAPPINGS_LONG = "removemapping";
    
    protected Option addLoginMappingOption = null;
    protected Option removeLoginMappingOption = null;
    protected Option maintenanceReasonIDOption = null;


    protected void setMaintenanceReasodIDOption(final AdminParser parser,final boolean required){
        this.maintenanceReasonIDOption = setShortLongOpt(parser, OPT_REASON_SHORT,OPT_REASON_LONG,"Maintenance reason id",true, convertBooleantoTriState(required));
    }
    
    protected void setAddMappingOption(final AdminParser parser,final boolean required ){
        this.addLoginMappingOption = setShortLongOpt(parser, OPT_CONTEXT_ADD_LOGIN_MAPPINGS_SHORT,OPT_CONTEXT_ADD_LOGIN_MAPPINGS_LONG,"Add login mappings.Seperated by \",\"",true, convertBooleantoTriState(required));
    }
    
    protected void setRemoveMappingOption(final AdminParser parser,final boolean required ){
        this.removeLoginMappingOption = setShortLongOpt(parser, OPT_CONTEXT_REMOVE_LOGIN_MAPPINGS_SHORT,OPT_CONTEXT_REMOVE_LOGIN_MAPPINGS_LONG,"Remove login mappings.Seperated by \",\"",true, convertBooleantoTriState(required));
    }
    
    protected final void displayDisabledMessage(final Integer id, final Integer ctxid) {
        createMessageForStdout(id, ctxid, "disabled");
    }

    protected final void displayEnabledMessage(final Integer id, final Integer ctxid) {
        createMessageForStdout(id, ctxid, "enabled");
    }

    protected final void displayMovedMessage(final Integer id, final Integer ctxid) {
        createMessageForStdout(id, ctxid, "moved");
    }

}
