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
package com.openexchange.admin.console;

import java.rmi.NotBoundException;

import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;

public abstract class BasicCommandlineOptions {
    
    protected static final int DEFAULT_CONTEXT=1;
    protected static final String OPT_NAME_CONTEXT_SHORT="c";
    protected static final String OPT_NAME_CONTEXT_LONG="contextid";
    protected static final String OPT_NAME_CONTEXT_NAME_SHORT="N";
    protected static final String OPT_NAME_CONTEXT_NAME_LONG="contextname";
    protected static final String OPT_NAME_CONTEXT_NAME_DESCRIPTION="The name of the context";
    protected static final String OPT_NAME_CONTEXT_DESCRIPTION="The id of the context";
    protected static final String OPT_NAME_ADMINUSER_SHORT="A";
    protected static final String OPT_NAME_ADMINUSER_LONG="adminuser";
    protected static final String OPT_NAME_ADMINPASS_SHORT="P";
    protected static final String OPT_NAME_ADMINPASS_LONG="adminpass";
    protected static final String OPT_NAME_ADMINPASS_DESCRIPTION="Admin password";
    protected static final String OPT_NAME_ADMINUSER_DESCRIPTION="Admin username";
    protected static final String OPT_NAME_SEARCHPATTERN_LONG = "searchpattern";
    protected static final String OPT_NAME_SEARCHPATTERN = "s";
    
    protected static String RMI_HOSTNAME ="rmi://localhost";    
   
    
    protected static void printServerResponse(String msg){
        System.err.println("Server response:\n "+msg);    
    }
    
    protected static void printNotBoundResponse(NotBoundException nbe){
        printServerResponse("RMI module "+nbe.getMessage()+" not available on server");
    }
    
    protected static void printError(String msg){
        System.err.println("Error:\n "+msg);    
    }
    
    protected static void printInvalidInputMsg(String msg){
        System.err.println("Invalid input detected: "+msg);    
    }
    
    protected static void printHelpText(String msg,Options opts){
        HelpFormatter helpf = new HelpFormatter();
        helpf.printHelp(msg,opts);
    }
    
    protected static void setRMI_HOSTNAME(String rmi_hostname) {       
        String host = rmi_hostname;
        if(!host.startsWith("rmi://")){
            host = "rmi://"+host;
        }
        if(!host.endsWith("/")){
            host = host+"/";
        }
        RMI_HOSTNAME = host;
        
    }
   
    protected Option getLongOpt(final String longopt, final String description, final boolean hasarg, final boolean required) {
        final Option retval = OptionBuilder.withLongOpt( longopt ).withDescription( description ).withValueSeparator( '=' ).create();
        if (hasarg) {
            retval.hasArg();
        }
        retval.setRequired(required);
        return retval;
    }
    
    protected Option getShortOpt(final String shortopt, final String description, final boolean hasarg, final boolean required) {
        final Option retval = new Option(shortopt, hasarg, description);
        retval.setRequired(required);
        return retval;
    }

    protected Option getShortLongOpt(final String shortopt, final String longopt, final String description, final boolean hasarg, final boolean required) {
        final Option retval = new Option(shortopt, longopt, hasarg, description);
        retval.setRequired(required);
        return retval;
    }
    
    protected Option getContextOption() {
        final Option retval = getShortLongOpt(OPT_NAME_CONTEXT_SHORT, OPT_NAME_CONTEXT_LONG, OPT_NAME_CONTEXT_DESCRIPTION, true, false);
        retval.setArgName("Context ID");
        return retval;
    }
    
    protected Option getContextNameOption() {
        final Option retval = getShortLongOpt(OPT_NAME_CONTEXT_NAME_SHORT, OPT_NAME_CONTEXT_NAME_LONG, OPT_NAME_CONTEXT_NAME_DESCRIPTION, true, false);
        retval.setArgName("Context Name");
        return retval;
    }
    
    protected Option getAdminPassOption() {
        final Option retval = getShortLongOpt(OPT_NAME_ADMINPASS_SHORT, OPT_NAME_ADMINPASS_LONG, OPT_NAME_ADMINPASS_DESCRIPTION, true, true);
        retval.setArgName("Admin password");
        return retval;
    }
    
    protected Option getAdminUserOption() {
        final Option retval = getShortLongOpt(OPT_NAME_ADMINUSER_SHORT, OPT_NAME_ADMINUSER_LONG, OPT_NAME_ADMINUSER_DESCRIPTION, true, true);
        retval.setArgName("Admin username");
        return retval;
    }
    
    protected Option getSearchPatternOption(){
        final Option opt = getShortLongOpt(OPT_NAME_SEARCHPATTERN, OPT_NAME_SEARCHPATTERN_LONG, "The search pattern which is used for listing", true, true);
        opt.setArgName(OPT_NAME_SEARCHPATTERN_LONG);
        return  opt;
    }
    
    
    /**
     * 
     * @return Options containing context,adminuser,adminpass Option objects.
     */
    protected Options getDefaultCommandLineOptions(){
        
        Options options = new Options();
        
        options.addOption(getContextOption());        
        options.addOption(getAdminUserOption());
        options.addOption(getAdminPassOption());
        
        return options;
    }
}
