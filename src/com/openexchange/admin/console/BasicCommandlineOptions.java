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
import java.util.ArrayList;

import com.openexchange.admin.console.CmdLineParser.Option;


/**
 * 
 * @author cutmasta,d7
 *
 */
public abstract class BasicCommandlineOptions {
    
    protected static final int DEFAULT_CONTEXT=1;
    protected static final char OPT_NAME_CONTEXT_SHORT='c';
    protected static final String OPT_NAME_CONTEXT_LONG="contextid";
    protected static final char OPT_NAME_CONTEXT_NAME_SHORT='N';
    protected static final String OPT_NAME_CONTEXT_NAME_LONG="contextname";
    protected static final String OPT_NAME_CONTEXT_NAME_DESCRIPTION="The name of the context";
    protected static final String OPT_NAME_CONTEXT_DESCRIPTION="The id of the context";
    protected static final char OPT_NAME_ADMINUSER_SHORT='A';
    protected static final String OPT_NAME_ADMINUSER_LONG="adminuser";
    protected static final char OPT_NAME_ADMINPASS_SHORT='P';
    protected static final String OPT_NAME_ADMINPASS_LONG="adminpass";
    protected static final String OPT_NAME_ADMINPASS_DESCRIPTION="Admin password";
    protected static final String OPT_NAME_ADMINUSER_DESCRIPTION="Admin username";
    protected static final String OPT_NAME_SEARCHPATTERN_LONG = "searchpattern";
    protected static final char OPT_NAME_SEARCHPATTERN = 's';
    
    protected static final String OPT_NAME_CSVOUTPUT_LONG = "csv";
    protected static final String OPT_NAME_CSVOUTPUT_DESCRIPTION = "Format output to csv";
    
    protected static String RMI_HOSTNAME ="rmi://localhost";
    
    protected Option contextOption = null;
    protected Option adminUserOption = null;
    protected Option adminPassOption = null;
    protected Option searchOption = null;
    protected Option csvOutputOption = null;
    
    protected static void printServerResponse(String msg){
        System.err.println("Server response:\n "+msg);    
    }
    
    protected static void printNotBoundResponse(NotBoundException nbe){
        printServerResponse("RMI module "+nbe.getMessage()+" not available on server");
    }
    
    protected static void printError(String msg){
        System.err.println("Error:\n "+msg+"\n");    
    }
    
    protected static void printInvalidInputMsg(String msg){
        System.err.println("Invalid input detected: "+msg);    
    }    
    
    
    /**
     * Prints out the given data as csv output.
     * The first ArrayList contains the columns which describe the following data lines.<br><br>
     * 
     * Example output:<br><br>
     * username,email,mycolumn<br>
     * testuser,test@test.org,mycolumndata<br>
     * 
     * @param columns
     * @param data
     */
    protected static void doCSVOutput(final ArrayList<String> columns, ArrayList<ArrayList<String>> data){
        if(columns!=null && data!=null){
            
            // first prepare the columns line
            StringBuilder sb = new StringBuilder();
            for (String column_entry : columns) {
                sb.append(column_entry);
                sb.append(",");
            }
            // remove last ","
            sb.deleteCharAt(sb.length()-1);
            
            // print the columns line
            System.out.println(sb.toString()+"\n");            
            
            // now prepare all data lines
            for (ArrayList<String> data_list : data) {
                sb = new StringBuilder();
                for (String data_column : data_list) {
                    if(data_column!=null){
                        sb.append("\"");
                        sb.append(data_column);
                        sb.append("\"");
                    }
                    sb.append(",");
                }
                // remove trailing ","
                sb.deleteCharAt(sb.length()-1);               
                // print out data line with linessbreak
                System.out.println(sb.toString()+"\n");
            }
        }
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
   
    protected Option setLongOpt(final AdminParser admp,final String longopt, final String description, final boolean hasarg, final boolean required) {
        
        final Option retval = admp.addOption(longopt, longopt, description, required,hasarg);
//        //OptionBuilder.withLongOpt( longopt ).withDescription( description ).withValueSeparator( '=' ).create();
//        if (hasarg) {
//            retval.hasArg();
//        }
//        retval.setRequired(required);
        return retval;
    }

    protected Option setShortLongOpt(final AdminParser admp,final char shortopt, final String longopt, final String description, final boolean hasarg, final boolean required) {
        final Option retval = admp.addOption(shortopt,longopt,longopt, description, required,hasarg);       
        return retval;
    }
    
    protected Option setShortLongOptWithDefault(final AdminParser admp,final char shortopt, final String longopt, final String description, final String defaultvalue, final boolean hasarg, final boolean required) {
        final StringBuilder desc = new StringBuilder();
        desc.append(description);
        desc.append(". Default: ");
        desc.append(defaultvalue);
        
        return setShortLongOpt(admp, shortopt, longopt, desc.toString(), hasarg, required);
    }

    protected Option getContextOption(final AdminParser admp) {
        contextOption = setShortLongOpt(admp,OPT_NAME_CONTEXT_SHORT, OPT_NAME_CONTEXT_LONG, OPT_NAME_CONTEXT_DESCRIPTION, true, false);        
//        retval.setArgName("Context ID");
        return contextOption;
    }
    
    protected Option getContextNameOption(final AdminParser admp) {
        final Option retval = setShortLongOpt(admp,OPT_NAME_CONTEXT_NAME_SHORT, OPT_NAME_CONTEXT_NAME_LONG, OPT_NAME_CONTEXT_NAME_DESCRIPTION, true, false);
//        retval.setArgName("Context Name");
        return retval;
    }
    
    protected Option getAdminPassOption(final AdminParser admp) {
        adminPassOption = setShortLongOpt(admp,OPT_NAME_ADMINPASS_SHORT, OPT_NAME_ADMINPASS_LONG, OPT_NAME_ADMINPASS_DESCRIPTION, true, true);
//        retval.setArgName("Admin password");
        return adminPassOption;
    }
    
    protected void setCSVOutputOption(final AdminParser admp) {
        csvOutputOption = setLongOpt(admp, OPT_NAME_CSVOUTPUT_LONG, OPT_NAME_CSVOUTPUT_DESCRIPTION, false, false);
    }
    
    protected Option getAdminUserOption(final AdminParser admp) {
        adminUserOption= setShortLongOpt(admp,OPT_NAME_ADMINUSER_SHORT, OPT_NAME_ADMINUSER_LONG, OPT_NAME_ADMINUSER_DESCRIPTION, true, true);
//        retval.setArgName("Admin username");
        return adminUserOption;
    }
    
    protected Option getSearchPatternOption(final AdminParser admp){
        searchOption = setShortLongOpt(admp,OPT_NAME_SEARCHPATTERN, OPT_NAME_SEARCHPATTERN_LONG, "The search pattern which is used for listing", true, true);
//        opt.setArgName(OPT_NAME_SEARCHPATTERN_LONG);
        return  searchOption;
    }

//    protected Option addArgName(final Option option, final String argname) {
//        final Option retval = option;
////        retval.setArgName(argname);
//        return retval;
//    }
    
    @Deprecated
    protected Option addDefaultArgName(final AdminParser admp,final Option option) {
//        return addArgName(option, option.getLongOpt(admp));
        // FIXME
        return null;
    }

    protected int testStringAndGetIntOrDefault(final String test, final int defaultvalue) throws NumberFormatException {
        if (null != test) {
            return Integer.parseInt(test);
        } else {
            return defaultvalue;
        }
    }
    
    protected String testStringAndGetStringOrDefault(final String test, final String defaultvalue) {
        if (null != test) {
            return test;
        } else {
            return defaultvalue;
        }
    }

    /**
     * 
     * @return Options containing context,adminuser,adminpass Option objects.
     */
    protected void setDefaultCommandLineOptions(final AdminParser admp){
        
        Option[] options = new Option[3];
        contextOption = getContextOption(admp);
        adminUserOption = getAdminUserOption(admp); 
        adminPassOption = getAdminPassOption(admp);
        
        options[0] = contextOption;
        options[1] = adminUserOption;
        options[2] = adminPassOption;
    }
}
