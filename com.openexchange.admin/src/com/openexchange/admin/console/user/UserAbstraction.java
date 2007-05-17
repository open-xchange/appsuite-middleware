/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the Open-Xchange, Inc. group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */
package com.openexchange.admin.console.user;


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.TimeZone;

import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.console.BasicCommandlineOptions;
import com.openexchange.admin.console.CmdLineParser.Option;
import com.openexchange.admin.rmi.dataobjects.User;
//import com.openexchange.admin.rmi.dataobjects.User.PASSWORDMECH;
import com.openexchange.admin.rmi.extensions.OXUserExtensionInterface;

public abstract class UserAbstraction extends BasicCommandlineOptions {
    
    protected class MethodAndNames {
        private Method method = null;
        
        private String name = null;
        
        private String returntype = null;
        
        /**
         * @param method
         * @param name
         */
        public MethodAndNames(final Method method, final String name, final String returntype) {
            super();
            this.method = method;
            this.name = name;
            this.returntype = returntype;
        }
        
        public Method getMethod() {
            return this.method;
        }
        
        public void setMethod(final Method method) {
            this.method = method;
        }
        
        public String getName() {
            return this.name;
        }
        
        public void setName(final String name) {
            this.name = name;
        }
        
        public final void setReturntype(final String returntype) {
            this.returntype = returntype;
        }
        
        public final String getReturntype() {
            return this.returntype;
        }
        
    }

    protected class OptionAndMethod {
        private Method method = null;
        
        private Option option = null;
        
        private String returntype = null;
        
        public final Method getMethod() {
            return this.method;
        }
        
        public final void setMethod(final Method method) {
            this.method = method;
        }
        
        public final Option getOption() {
            return this.option;
        }
        
        public final void setOption(final Option option) {
            this.option = option;
        }
        
        /**
         * @param method
         * @param option
         */
        public OptionAndMethod(final Method method, final Option option, final String returntype) {
            super();
            this.method = method;
            this.option = option;
            this.returntype = returntype;
        }
        
        public final String getReturntype() {
            return this.returntype;
        }
        
        public final void setReturntype(final String returntype) {
            this.returntype = returntype;
        }
        
    }

    protected static final char OPT_ID_SHORT = 'i';
    protected static final String OPT_ID_LONG = "userid";
    protected static final char OPT_USERNAME_SHORT = 'u';
    protected static final String OPT_USERNAME_LONG = "username";
    protected static final char OPT_DISPLAYNAME_SHORT = 'd';
    protected static final String OPT_DISPLAYNAME_LONG = "displayname";
    protected static final char OPT_PASSWORD_SHORT = 'p';
    protected static final String OPT_PASSWORD_LONG = "password";
    protected static final char OPT_GIVENNAME_SHORT = 'g';
    protected static final String OPT_GIVENNAME_LONG = "givenname";
    protected static final char OPT_SURNAME_SHORT = 's';
    protected static final String OPT_SURNAME_LONG = "surname";
    protected static final char OPT_LANGUAGE_SHORT = 'l';
    protected static final String OPT_LANGUAGE_LONG = "language";
    protected static final char OPT_TIMEZONE_SHORT = 't';
    protected static final String OPT_TIMEZONE_LONG = "timezone";
    protected static final char OPT_PRIMARY_EMAIL_SHORT = 'e';
    protected static final String OPT_PRIMARY_EMAIL_LONG = "email";
    protected static final char OPT_DEPARTMENT_SHORT = 'x';
    protected static final String OPT_DEPARTMENT_LONG = "department";
    protected static final char OPT_COMPANY_SHORT = 'z';
    protected static final String OPT_COMPANY_LONG = "company";
    protected static final char OPT_ALIASES_SHORT = 'a';
    protected static final String OPT_ALIASES_LONG = "aliases";
    
    protected static final String OPT_EXTENDED_LONG = "extendedoptions";
    
    protected static final String JAVA_UTIL_TIME_ZONE = "java.util.TimeZone";
    protected static final String PASSWORDMECH_CLASS = "com.openexchange.admin.rmi.dataobjects.User$PASSWORDMECH";
    protected static final String JAVA_UTIL_HASH_SET = "java.util.HashSet";
    protected static final String JAVA_UTIL_DATE = "java.util.Date";
    protected static final String JAVA_LANG_BOOLEAN = "java.lang.Boolean";
    protected static final String JAVA_LANG_INTEGER = "java.lang.Integer";
    protected static final String JAVA_UTIL_ARRAY_LIST = "java.util.ArrayList";
    protected static final String JAVA_LANG_LONG = "java.lang.Long";
    protected static final String JAVA_LANG_STRING = "java.lang.String";
    protected static final String SIMPLE_INT = "int";
    protected static final String OPT_IMAPONLY_LONG = "imaponly";
    protected static final String OPT_DBONLY_LONG = "dbonly";
    protected static final String OPT_IMAP_QUOTA_LONG = "imapquota";
    protected static final char OPT_IMAP_QUOTA_SHORT = 'I';
    protected static final String OPT_NO_INET_MAIL_ACCESS_LONG = "inetmailaccess";
    protected static final char OPT_NO_INET_MAIL_ACCESS_SHORT = 'M';
    protected static final char OPT_SPAM_FILTER_ENABLE_SHORT = 'S';
    protected static final String OPT_SPAM_FILTER_ENABLE_LONG = "spamfilter";
    public static final ArrayList<OptionAndMethod> optionsandmethods = new ArrayList<OptionAndMethod>();

    protected Option userNameOption = null;
    protected Option displayNameOption = null;
    protected Option givenNameOption = null;
    protected Option surNameOption = null;
    protected Option passwordOption = null;
    protected Option primaryMailOption = null;
    protected Option languageOption = null;
    protected Option timezoneOption = null;
    protected Option departmentOption = null;
    protected Option companyOption = null;
    protected Option aliasesOption = null;
    protected Option idOption = null;
    protected Option imapOnlyOption = null;
    protected Option dbOnlyOption = null;
    protected Option extendedOption = null;
    protected Option imapQuotaOption = null;
    protected Option inetMailAccessOption = null;
    protected Option spamFilterOption = null;
    
    /**
     * This field holds all the options which are displayed by default. So this options can be
     * deducted from the other dynamically created options
     */
    public static final HashSet<String> standardoptions = new HashSet<String>(15);
    
    static {
        standardoptions.add(OPT_IMAP_QUOTA_LONG);
        standardoptions.add(OPT_NO_INET_MAIL_ACCESS_LONG);
        standardoptions.add("spam_filter_enabled");
        standardoptions.add("id");
        standardoptions.add(OPT_USERNAME_LONG);
        standardoptions.add("display_name");
        standardoptions.add(OPT_PASSWORD_LONG);
        standardoptions.add("given_name");
        standardoptions.add("sur_name");
        standardoptions.add(OPT_LANGUAGE_LONG);
        standardoptions.add("primaryemail");
        standardoptions.add(OPT_DEPARTMENT_LONG);
        standardoptions.add(OPT_COMPANY_LONG);
        standardoptions.add(OPT_ALIASES_LONG);
    }

    
    protected final void printExtensionsError(final User usr){
        //+ loop through extensions and check for errors       
        if(usr!=null && usr.getExtensions()!=null){
            final ArrayList<OXUserExtensionInterface> usr_exts = usr.getExtensions();
//            boolean has_error = false;
            for (final OXUserExtensionInterface usr_extension : usr_exts) {
                if(usr_extension.getExtensionError()!=null){
                    printServerResponse(usr_extension.getExtensionError());
//                    has_error = true;
                }
            }
//            if(has_error){
//                System.exit(SYSEXIT_SERVERSTORAGE_ERROR);
//            }
        }
    }
    
    protected final void setIdOption(final AdminParser admp){
        this.idOption =  setShortLongOpt(admp,OPT_ID_SHORT,OPT_ID_LONG,"Id of the user", true, true);
    }
    
    protected final void setUsernameOption(final AdminParser admp){
        this.userNameOption = setShortLongOpt(admp,OPT_USERNAME_SHORT,OPT_USERNAME_LONG,"Username of the user", true, true);
    }
    
    protected final void setDisplayNameOption(final AdminParser admp){
        this.displayNameOption = setShortLongOpt(admp,OPT_DISPLAYNAME_SHORT,OPT_DISPLAYNAME_LONG,"Display name of the user", true, true); 
    }
    
    protected final void setPasswordOption(final AdminParser admp){
        this.passwordOption =  setShortLongOpt(admp,OPT_PASSWORD_SHORT,OPT_PASSWORD_LONG,"Password for the user", true, true); 
    }
    
    protected final void setGivenNameOption(final AdminParser admp){
        this.givenNameOption =  setShortLongOpt(admp,OPT_GIVENNAME_SHORT,OPT_GIVENNAME_LONG,"Given name for the user", true, true); 
    }
    
    protected final void setSurNameOption(final AdminParser admp){
        this.surNameOption =  setShortLongOpt(admp,OPT_SURNAME_SHORT,OPT_SURNAME_LONG,"Sur name for the user", true, true); 
    }
    
    protected final void setLanguageOption(final AdminParser admp){
        this.languageOption =  setShortLongOpt(admp,OPT_LANGUAGE_SHORT,OPT_LANGUAGE_LONG,"Language for the user (de_DE,en_US)", true, false); 
    }
    
    protected final void setTimezoneOption(final AdminParser admp){
        this.timezoneOption =  setShortLongOpt(admp,OPT_TIMEZONE_SHORT,OPT_TIMEZONE_LONG,"Timezone of the user (Europe/Berlin)", true, false); 
    }
    
    protected final void setPrimaryMailOption(final AdminParser admp){
        this.primaryMailOption =  setShortLongOpt(admp,OPT_PRIMARY_EMAIL_SHORT,OPT_PRIMARY_EMAIL_LONG,"Primary mail address", true, true); 
    }
    
    protected final void setDepartmentOption(final AdminParser admp){
        this.departmentOption = setShortLongOpt(admp,OPT_DEPARTMENT_SHORT,OPT_DEPARTMENT_LONG,"Department of the user", true, false); 
    }
    
    protected final void setCompanyOption(final AdminParser admp){
        this.companyOption = setShortLongOpt(admp,OPT_COMPANY_SHORT,OPT_COMPANY_LONG,"Company of the user", true, false); 
    }

    protected final void setAliasesOption(final AdminParser admp){
        this.aliasesOption = setShortLongOpt(admp,OPT_ALIASES_SHORT,OPT_ALIASES_LONG,"Email aliases of the user", true, false); 
    }
    
    protected final void setImapOnlyOption(final AdminParser admp){
        this.imapOnlyOption =  setLongOpt(admp,OPT_IMAPONLY_LONG,"Do this operation only for the IMAP account of the user", false, false); 
    }
    
    protected final void setDBOnlyOption(final AdminParser admp){
        this.dbOnlyOption =  setLongOpt(admp,OPT_DBONLY_LONG,"Do this operation only in Database system (parameters which apply to extensions will be ignored)", false, false); 
    }
    
    protected final void setExtendedOption(final AdminParser admp) {
        this.extendedOption  = setLongOpt(admp, OPT_EXTENDED_LONG, "Set this if you want to see all options, use this instead of help option", false, false);
    }

    /**
     * @param theMethods
     * @param notallowed Here we define the methods we don't want. The name is the name of method without the prefix
     * get or is
     * @return
     */
    protected final ArrayList<MethodAndNames> getGetters(final Method[] theMethods, final HashSet<String> notallowed) {
    
        // Define the returntypes we search for
        final HashSet<String> returntypes = new HashSet<String>(7);
        returntypes.add(JAVA_LANG_STRING);
        returntypes.add(JAVA_LANG_INTEGER);
        returntypes.add(JAVA_LANG_BOOLEAN);
        returntypes.add(JAVA_UTIL_DATE);
        returntypes.add(JAVA_UTIL_HASH_SET);
        returntypes.add(JAVA_UTIL_TIME_ZONE);
        returntypes.add(PASSWORDMECH_CLASS);
        
        return getGetterGeneral(theMethods, notallowed, returntypes);
    }

    /**
     * @param theMethods
     * @param notallowed Here we define the methods we don't want. The name is the name of method without the prefix
     * get or is
     * @return
     */
    protected final ArrayList<MethodAndNames> getGettersforExtensions(final Method[] theMethods, final HashSet<String> notallowed) {
        
        // Define the returntypes we search for
        final HashSet<String> returntypes = new HashSet<String>(7);
        returntypes.add(JAVA_LANG_STRING);
        returntypes.add(JAVA_LANG_INTEGER);
        returntypes.add(JAVA_LANG_LONG);
        returntypes.add(SIMPLE_INT);
        returntypes.add(JAVA_UTIL_ARRAY_LIST);
        
        return getGetterGeneral(theMethods, notallowed, returntypes);
    }

    private final ArrayList<MethodAndNames> getGetterGeneral(final Method[] theMethods, final HashSet<String> notallowed, final HashSet<String> returntypes) {
        final ArrayList<MethodAndNames> retlist = new ArrayList<MethodAndNames>();
        // First we get all the getters of the user data class
        for (final Method method : theMethods) {
            final String methodname = method.getName();
    
            if (methodname.startsWith("get")) {
                final String methodnamewithoutprefix = methodname.substring(3);
                if (!notallowed.contains(methodnamewithoutprefix)) {
                    final String returntype = method.getReturnType().getName();
                    if (returntypes.contains(returntype)) {
                        retlist.add(new MethodAndNames(method, methodnamewithoutprefix, returntype));
                    }
                }
            } else if (methodname.startsWith("is")) {
                final String methodnamewithoutprefix = methodname.substring(2);
                if (!notallowed.contains(methodnamewithoutprefix)) {
                    final String returntype = method.getReturnType().getName();
                    if (returntypes.contains(returntype)) {
                        retlist.add(new MethodAndNames(method, methodnamewithoutprefix, returntype));
                    }
                }
            }
        }
        return retlist;
    }
    
    protected final ArrayList<MethodAndNames> getSetters(final Method[] theMethods) {
        final ArrayList<MethodAndNames> retlist = new ArrayList<MethodAndNames>();
        
        // Here we define which methods we don't want to get
        final HashSet<String> notallowed = new HashSet<String>();
        notallowed.add("test");
        
        // Define the returntypes we search for
        final HashSet<String> allowedparametertypes = new HashSet<String>(7);
        allowedparametertypes.add(JAVA_LANG_STRING);
        allowedparametertypes.add(JAVA_LANG_INTEGER);
        allowedparametertypes.add(JAVA_LANG_BOOLEAN);
        allowedparametertypes.add(JAVA_UTIL_DATE);
        allowedparametertypes.add(JAVA_UTIL_TIME_ZONE);
//        allowedparametertypes.add(PASSWORDMECH_CLASS);
        
        // First we get all the getters of the user data class
        for (final Method method : theMethods) {
            final String methodname = method.getName();
            
            if (methodname.startsWith("set")) {
                final String methodnamewithoutprefix = methodname.substring(3);
                if (!notallowed.contains(methodnamewithoutprefix)) {
                    final Class[] parametertypes = method.getParameterTypes();
                    if (parametertypes.length == 1) {
                        final String parametertype = parametertypes[0].getName();
                        if (allowedparametertypes.contains(parametertype)) {
                            retlist.add(new MethodAndNames(method, methodnamewithoutprefix, parametertype));
                        }
                    }
                }
            } 
        }
        return retlist;
    }

    /**
     * This method is used to show the full help text, if the extended option is set.
     * Be careful to check only for that option, if you have added this option before.
     * 
     * @param parser
     */
    protected final void printExtendedOutputIfSet(final AdminParser parser) {
        if (null != parser.getOptionValue(this.extendedOption)) {
            parser.printUsageExtended();
            System.exit(0);
        }
    }

    /**
     * Get the mandatory options from the command line and set's them in the user object
     * 
     * @param parser The parser object
     * @param usr User object which will be changed
     */
    protected final void setMandatoryOptionsinUser(final AdminParser parser, final User usr) {
        final String optionValue = (String) parser.getOptionValue(this.userNameOption);
        if (null != optionValue) {
            usr.setUsername(optionValue);
        }        
        final String optionValue2 = (String) parser.getOptionValue(this.displayNameOption);
        if (null != optionValue2) {
            usr.setDisplay_name(optionValue2);
        }        
        final String optionValue3 = (String) parser.getOptionValue(this.givenNameOption);
        if (null != optionValue3) {
            usr.setGiven_name(optionValue3);
        }        
        final String optionValue4 = (String) parser.getOptionValue(this.surNameOption);
        if (null != optionValue4) {
            usr.setSur_name(optionValue4);
        }        
        final String optionValue5 = (String) parser.getOptionValue(this.passwordOption);
        if (null != optionValue5) {
            usr.setPassword(optionValue5);
        }        
        final String optionValue6 = (String) parser.getOptionValue(this.primaryMailOption);
        if (null != optionValue6) {
            usr.setPrimaryEmail(optionValue6);
            usr.setEmail1(optionValue6);
        }        
    }

    /**
     * Get the optional options from the command line and set's them in the user object
     * 
     * @param parser The parser object
     * @param usr User object which will be changed
     */
    protected final void setOptionalOptionsinUser(final AdminParser parser, final User usr) {
        final String optionValue = (String) parser.getOptionValue(this.companyOption);
        if (null != optionValue) {
            usr.setCompany(optionValue);
        }
    
        final String optionValue2 = (String) parser.getOptionValue(this.departmentOption);
        if (null != optionValue2) {
            usr.setDepartment(optionValue2);
        }
    
        final String optionValue3 = (String) parser.getOptionValue(this.languageOption);
        if (null != optionValue3) {
            final String[] lange = optionValue3.split("_");
            if (lange != null && lange.length == 2) {
                usr.setLanguage(new Locale(lange[0].toLowerCase(), lange[1].toUpperCase()));
            }
        }
    
        final String optionValue4 = (String) parser.getOptionValue(this.timezoneOption);
        if (null != optionValue4) {
            usr.setTimezone(TimeZone.getTimeZone(optionValue4));
        }
    
        final String aliasOpt = (String) parser.getOptionValue(this.aliasesOption);
        if (null != aliasOpt) {
            final HashSet<String> aliases = new HashSet<String>();
            for (final String alias : aliasOpt.split(",")) {
                aliases.add(alias.trim());
            }
            usr.setAliases(aliases);
        }
    }

    protected final void setMandatoryOptions(final AdminParser parser) {
        setUsernameOption(parser);
        setDisplayNameOption(parser);
        setGivenNameOption(parser);
        setSurNameOption(parser);
        setPasswordOption(parser);
        setPrimaryMailOption(parser);
    }

    protected final void setOptionalOptions(final AdminParser parser) {
        setLanguageOption(parser);
        setTimezoneOption(parser);
        setDepartmentOption(parser);
        setCompanyOption(parser);
        setAliasesOption(parser);
    }

    protected void setExtendedOptions(final AdminParser parser) {
        final Method[] methods = User.class.getMethods();
        final ArrayList<MethodAndNames> methArrayList = getSetters(methods);
    
        for (final MethodAndNames methodandnames : methArrayList) {
            
            if (!standardoptions.contains(methodandnames.getName().toLowerCase())) {
                if (methodandnames.getReturntype().equals(JAVA_LANG_STRING)) {
                    final Option option = setLongOpt(parser, methodandnames.getName().toLowerCase(), "stringvalue", methodandnames.getName(), true, false, true);
                    optionsandmethods.add(new OptionAndMethod(methodandnames.getMethod(), option, methodandnames.getReturntype()));
                } else if (methodandnames.getReturntype().equals(JAVA_LANG_INTEGER)) {
                    final Option option = setLongOpt(parser, methodandnames.getName().toLowerCase(), "intvalue", methodandnames.getName(), true, false, true);
                    optionsandmethods.add(new OptionAndMethod(methodandnames.getMethod(), option, methodandnames.getReturntype()));
                } else if (methodandnames.getReturntype().equals(JAVA_LANG_BOOLEAN)) {
                    final Option option = setSettableBooleanLongOpt(parser, methodandnames.getName().toLowerCase(), "booleanvalue", methodandnames.getName(), true, false, true);
                    optionsandmethods.add(new OptionAndMethod(methodandnames.getMethod(), option, methodandnames.getReturntype()));
                } else if (methodandnames.getReturntype().equals(JAVA_UTIL_DATE)) {
                    final Option option = setLongOpt(parser, methodandnames.getName().toLowerCase(), "datevalue", methodandnames.getName(), true, false, true);
                    optionsandmethods.add(new OptionAndMethod(methodandnames.getMethod(), option, methodandnames.getReturntype()));
//                } else if (methodandnames.getReturntype().equals(PASSWORDMECH_CLASS)) {
//                    final Option option = setLongOpt(parser, methodandnames.getName().toLowerCase(), "CRYPT/SHA", methodandnames.getName(), true, false, true);
//                    optionsandmethods.add(new OptionAndMethod(methodandnames.getMethod(), option, methodandnames.getReturntype()));
                }
            }            
        }
    
    }

    /**
     * This method goes through the dynamically created options, and sets the corresponding values
     * in the user object.
     * 
     * Attention the user object given as parameter is changed
     * 
     * @param parser
     * @param usr
     * @throws InvocationTargetException 
     * @throws IllegalAccessException 
     * @throws IllegalArgumentException 
     */
    protected final void applyExtendedOptionsToUser(final AdminParser parser, final User usr) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        for (final OptionAndMethod optionAndMethod : optionsandmethods) {
            if (optionAndMethod.getReturntype().equals(JAVA_LANG_STRING)) {
                final String value = (String)parser.getOptionValue(optionAndMethod.getOption());
                if (null != value) {
                    optionAndMethod.getMethod().invoke(usr, value);
                }
            } else if (optionAndMethod.getReturntype().equals(JAVA_LANG_INTEGER)) {
                final Integer value = (Integer)parser.getOptionValue(optionAndMethod.getOption());
                if (null != value) {
                    optionAndMethod.getMethod().invoke(usr, value);
                }
            } else if (optionAndMethod.getReturntype().equals(JAVA_LANG_BOOLEAN)) {
                final Boolean value = (Boolean)parser.getOptionValue(optionAndMethod.getOption());
                if (null != value) {
                    optionAndMethod.getMethod().invoke(usr, value);
                }
            } else if (optionAndMethod.getReturntype().equals(JAVA_UTIL_DATE)) {
                final Date value = (Date)parser.getOptionValue(optionAndMethod.getOption());
                if (null != value) {
                    optionAndMethod.getMethod().invoke(usr, value);
                }
            } else if (optionAndMethod.getReturntype().equals(JAVA_UTIL_HASH_SET)) {
                final HashSet value = (HashSet)parser.getOptionValue(optionAndMethod.getOption());
                if (null != value) {
                    optionAndMethod.getMethod().invoke(usr, value);
                }
//            } else if (optionAndMethod.getReturntype().equals(PASSWORDMECH_CLASS)) {
//                final String value = (String)parser.getOptionValue(optionAndMethod.getOption());
//                PASSWORDMECH pwmech = null;
//                if (value.equalsIgnoreCase("sha")) {
//                    pwmech = PASSWORDMECH.SHA;
//                } else if (value.equalsIgnoreCase("crypt")) {
//                    pwmech = PASSWORDMECH.CRYPT;
//                } else {
//                    throw new IllegalArgumentException("Argument for passwordmech is wrong.");
//                }
//                if (null != value) {
//                    optionAndMethod.getMethod().invoke(usr, pwmech);
//                }
            }
    
        }
    }

    /**
     * This method is used to find the right output
     * 
     * @param parser
     */
    protected void printrightoptions(final AdminParser parser) {
        if (null != parser.getOptionValue(this.extendedOption)) {
            parser.printUsageExtended();
        } else {
            parser.printUsage();
        }
    }
}


