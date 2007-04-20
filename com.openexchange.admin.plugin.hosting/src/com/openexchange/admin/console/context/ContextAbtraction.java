//package com.openexchange.admin.console.context;
//
//import org.apache.commons.cli.Option;
//
//import com.openexchange.admin.console.user.UserAbstraction;
//
//public abstract class ContextAbtraction extends UserAbstraction {
//
//    protected static final String OPT_NAME_ADMINPASS_DESCRIPTION="Admin master password";
//    protected static final String OPT_NAME_ADMINUSER_DESCRIPTION="Admin master username";
//
//    @Override
//    protected Option getAdminPassOption() {
//        final Option retval = getShortLongOpt(OPT_NAME_ADMINPASS_SHORT, OPT_NAME_ADMINPASS_LONG, OPT_NAME_ADMINPASS_DESCRIPTION, true, true);
//        retval.setArgName("Admin master password");
//        return retval;
//    }
//    
//    @Override
//    protected Option getAdminUserOption() {
//        final Option retval = getShortLongOpt(OPT_NAME_ADMINUSER_SHORT, OPT_NAME_ADMINUSER_LONG, OPT_NAME_ADMINUSER_DESCRIPTION, true, true);
//        retval.setArgName("Admin master username");
//        return retval;
//    }
//
//    @Override
//    protected Option getDisplayNameOption(){
//        return getShortLongOpt(OPT_DISPLAYNAME_SHORT,OPT_DISPLAYNAME_LONG,"Display name of the admin user", true, true); 
//    }
//
//    @Override
//    protected Option getGivenNameOption(){
//        return getShortLongOpt(OPT_GIVENNAME_SHORT,OPT_GIVENNAME_LONG,"Given name for the admin user", true, true); 
//    }
//
//    @Override
//    protected Option getPasswordOption(){
//        return getShortLongOpt(OPT_PASSWORD_SHORT,OPT_PASSWORD_LONG,"Password for the admin user", true, true); 
//    }
//
//    protected Option getSurNameOption(){
//        return getShortLongOpt(OPT_SURNAME_SHORT,OPT_SURNAME_LONG,"Sur name for the admin user", true, true); 
//    }
//
//    protected Option getUsernameOption(){
//        return getShortLongOpt(OPT_USERNAME_SHORT,OPT_USERNAME_LONG,"Username of the admin user", true, true); 
//    }
//
//}
