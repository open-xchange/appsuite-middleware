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


import java.util.ArrayList;

import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.console.BasicCommandlineOptions;
import com.openexchange.admin.console.CmdLineParser.Option;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.rmi.extensions.OXUserExtensionInterface;

public abstract class UserAbstraction extends BasicCommandlineOptions {
    
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
    
    protected void printExtensionsError(User usr){
        //+ loop through extensions and check for errors       
        if(usr!=null && usr.getExtensions()!=null){
            ArrayList<OXUserExtensionInterface> usr_exts = usr.getExtensions();
            for (OXUserExtensionInterface usr_extension : usr_exts) {
                if(usr_extension.getExtensionError()!=null){
                    printServerResponse(usr_extension.getExtensionError());
                }
            }
        }
    }
    
    protected void setIdOption(final AdminParser admp){
        idOption =  setShortLongOpt(admp,OPT_ID_SHORT,OPT_ID_LONG,"Id of the user", true, true);
    }
    
    protected void setUsernameOption(final AdminParser admp){
        userNameOption = setShortLongOpt(admp,OPT_USERNAME_SHORT,OPT_USERNAME_LONG,"Username of the user", true, true);
    }
    
    protected void setDisplayNameOption(final AdminParser admp){
        displayNameOption = setShortLongOpt(admp,OPT_DISPLAYNAME_SHORT,OPT_DISPLAYNAME_LONG,"Display name of the user", true, true); 
    }
    
    protected void setPasswordOption(final AdminParser admp){
        passwordOption =  setShortLongOpt(admp,OPT_PASSWORD_SHORT,OPT_PASSWORD_LONG,"Password for the user", true, true); 
    }
    
    protected void setGivenNameOption(final AdminParser admp){
        givenNameOption =  setShortLongOpt(admp,OPT_GIVENNAME_SHORT,OPT_GIVENNAME_LONG,"Given name for the user", true, true); 
    }
    
    protected void setSurNameOption(final AdminParser admp){
        surNameOption =  setShortLongOpt(admp,OPT_SURNAME_SHORT,OPT_SURNAME_LONG,"Sur name for the user", true, true); 
    }
    
    protected void setLanguageOption(final AdminParser admp){
        languageOption =  setShortLongOpt(admp,OPT_LANGUAGE_SHORT,OPT_LANGUAGE_LONG,"Language for the user (de_DE,en_US)", true, false); 
    }
    
    protected void setTimezoneOption(final AdminParser admp){
        timezoneOption =  setShortLongOpt(admp,OPT_TIMEZONE_SHORT,OPT_TIMEZONE_LONG,"Timezone of the user (Europe/Berlin)", true, false); 
    }
    
    protected void setPrimaryMailOption(final AdminParser admp){
        primaryMailOption =  setShortLongOpt(admp,OPT_PRIMARY_EMAIL_SHORT,OPT_PRIMARY_EMAIL_LONG,"Primary mail address", true, true); 
    }
    
    protected void setDepartmentOption(final AdminParser admp){
        departmentOption = setShortLongOpt(admp,OPT_DEPARTMENT_SHORT,OPT_DEPARTMENT_LONG,"Department of the user", true, false); 
    }
    
    protected void setCompanyOption(final AdminParser admp){
        companyOption = setShortLongOpt(admp,OPT_COMPANY_SHORT,OPT_COMPANY_LONG,"Company of the user", true, false); 
    }

    protected void setAliasesOption(final AdminParser admp){
        aliasesOption =  setShortLongOpt(admp,OPT_ALIASES_SHORT,OPT_ALIASES_LONG,"Email aliases of the user", true, false); 
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
}
