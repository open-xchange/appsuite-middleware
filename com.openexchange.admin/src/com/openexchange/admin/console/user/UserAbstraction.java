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


import org.apache.commons.cli.Option;


import com.openexchange.admin.console.BasicCommandlineOptions;

public abstract class UserAbstraction extends BasicCommandlineOptions {
    
    
    protected Option getIdOption(){
        return getShortLongOpt(OPT_ID_SHORT,OPT_ID_LONG,"Id of the user", true, true); 
    }
    
    protected Option getUsernameOption(){
        return getShortLongOpt(OPT_USERNAME_SHORT,OPT_USERNAME_LONG,"Username of the user", true, true); 
    }
    
    protected Option getDisplayNameOption(){
        return getShortLongOpt(OPT_DISPLAYNAME_SHORT,OPT_DISPLAYNAME_LONG,"Display name of the user", true, true); 
    }
    
    protected Option getPasswordOption(){
        return getShortLongOpt(OPT_PASSWORD_SHORT,OPT_PASSWORD_LONG,"Password for the user", true, true); 
    }
    
    protected Option getGivenNameOption(){
        return getShortLongOpt(OPT_GIVENNAME_SHORT,OPT_GIVENNAME_LONG,"Given name for the user", true, true); 
    }
    
    protected Option getSurNameOption(){
        return getShortLongOpt(OPT_SURNAME_SHORT,OPT_SURNAME_LONG,"Sur name for the user", true, true); 
    }
    
    protected Option getLanguageOption(){
        return getShortLongOpt(OPT_LANGUAGE_SHORT,OPT_LANGUAGE_LONG,"Language for the user (de_DE,en_US)", true, false); 
    }
    
    protected Option getTimezoneOption(){
        return getShortLongOpt(OPT_TIMEZONE_SHORT,OPT_TIMEZONE_LONG,"Timezone of the user (Europe/Berlin)", true, false); 
    }
    
    protected Option getPrimaryMailOption(){
        return getShortLongOpt(OPT_PRIMARY_EMAIL_SHORT,OPT_PRIMARY_EMAIL_LONG,"Primary mail address", true, true); 
    }
    
    protected Option getDepartmentOption(){
        return getShortLongOpt(OPT_DEPARTMENT_SHORT,OPT_DEPARTMENT_LONG,"Department of the user", true, false); 
    }
    
    protected Option getCompanyOption(){
        return getShortLongOpt(OPT_COMPANY_SHORT,OPT_COMPANY_LONG,"Company of the user", true, false); 
    }
    
    protected static final String OPT_ID_SHORT = "i";
    protected static final String OPT_ID_LONG = "userid";
    
    protected static final String OPT_USERNAME_SHORT = "u";
    protected static final String OPT_USERNAME_LONG = "username";
    
    protected static final String OPT_DISPLAYNAME_SHORT = "d";
    protected static final String OPT_DISPLAYNAME_LONG = "displayname";
    
    protected static final String OPT_PASSWORD_SHORT = "p";
    protected static final String OPT_PASSWORD_LONG = "password";
    
    protected static final String OPT_GIVENNAME_SHORT = "g";
    protected static final String OPT_GIVENNAME_LONG = "givenname";
    
    protected static final String OPT_SURNAME_SHORT = "s";
    protected static final String OPT_SURNAME_LONG = "surname";
    
    protected static final String OPT_LANGUAGE_SHORT = "l";
    protected static final String OPT_LANGUAGE_LONG = "language";
    
    protected static final String OPT_TIMEZONE_SHORT = "t";
    protected static final String OPT_TIMEZONE_LONG = "timezone";
    
    protected static final String OPT_PRIMARY_EMAIL_SHORT = "e";
    protected static final String OPT_PRIMARY_EMAIL_LONG = "email";
    
    protected static final String OPT_DEPARTMENT_SHORT = "x";
    protected static final String OPT_DEPARTMENT_LONG = "department";
    
    protected static final String OPT_COMPANY_SHORT = "z";
    protected static final String OPT_COMPANY_LONG = "company";
    

}
