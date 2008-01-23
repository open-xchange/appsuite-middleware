package com.openexchange.admin.console.user;

import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.console.ObjectNamingAbstraction;
import com.openexchange.admin.console.CmdLineParser.Option;

public class UserHostingAbstraction extends ObjectNamingAbstraction {
    
    // which access rights template should be used
    private Option accessRightsCombinationName = null;

    private static final String OPT_ACCESSRIGHTS_COMBINATION_NAME = "access-combination-name";
    
    public static final String ACCESS_COMBINATION_NAME_AND_ACCESS_RIGHTS_DETECTED_ERROR = "You can´t specify access combination name AND single access attributes simultaneously!";
    
    @Override
    protected String getObjectName() {
        return "user";
    }

    public void setAddAccessRightCombinationNameOption(final AdminParser parser, final boolean required) {
        this.accessRightsCombinationName = setLongOpt(parser,OPT_ACCESSRIGHTS_COMBINATION_NAME,"Access combination name", true, false,false);
    }

    public String parseAndSetAccessCombinationName(final AdminParser parser) {
        return (String) parser.getOptionValue(this.accessRightsCombinationName);
    }

}
