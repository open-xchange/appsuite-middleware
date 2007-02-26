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

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

import com.openexchange.admin.rmi.OXUserInterface;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.rmi.dataobjects.UserModuleAccess;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.NoSuchContextException;
import com.openexchange.admin.rmi.exceptions.StorageException;

public class Create extends UserAbstraction {

    public static void main(String[] args) {
        new Create(args);
    }


    
    public Create(String[] args2) {
        
        CommandLineParser parser = new PosixParser();

        Options options = getOptions();

        try {
            CommandLine cmd = parser.parse(options, args2);            
            Context ctx = new Context(DEFAULT_CONTEXT);
            
            if(cmd.hasOption(OPT_NAME_CONTEXT_SHORT)){
                ctx.setID(Integer.parseInt(cmd.getOptionValue(OPT_NAME_CONTEXT_SHORT)));
            }
            
            Credentials auth = new Credentials(cmd.getOptionValue(OPT_NAME_ADMINUSER_SHORT),cmd.getOptionValue(OPT_NAME_ADMINPASS_SHORT));
            
            
            // get rmi ref
            OXUserInterface oxres = (OXUserInterface)Naming.lookup(OXUserInterface.RMI_NAME);
            
            // create user obj
            User usr = new User();
            
            // fill user obj with mandatory values from console
            usr.setUsername(cmd.getOptionValue(OPT_USERNAME_SHORT));
            usr.setDisplay_name(cmd.getOptionValue(OPT_DISPLAYNAME_SHORT));
            usr.setGiven_name(cmd.getOptionValue(OPT_GIVENNAME_SHORT));
            usr.setSur_name(cmd.getOptionValue(OPT_SURNAME_SHORT));
            usr.setPassword(cmd.getOptionValue(OPT_PASSWORD_SHORT));
            usr.setPrimaryEmail(cmd.getOptionValue(OPT_PRIMARY_EMAIL_SHORT));
            usr.setEmail1(cmd.getOptionValue(OPT_PRIMARY_EMAIL_SHORT));
            
            
            // add optional values if set
            if(cmd.hasOption(OPT_COMPANY_SHORT)){
                usr.setCompany(cmd.getOptionValue(OPT_COMPANY_SHORT));
            }
            
            if(cmd.hasOption(OPT_DEPARTMENT_SHORT)){
                usr.setDepartment((cmd.getOptionValue(OPT_DEPARTMENT_SHORT)));
            }
            
            if(cmd.hasOption(OPT_LANGUAGE_SHORT)){                
                usr.setLanguage(new Locale(cmd.getOptionValue(OPT_LANGUAGE_SHORT)));
            }
            
            if(cmd.hasOption(OPT_TIMEZONE_SHORT)){                
                usr.setTimezone(TimeZone.getTimeZone(cmd.getOptionValue(OPT_TIMEZONE_SHORT)));
            }
            
            // default set all access rights
            UserModuleAccess access = new UserModuleAccess();
            access.enableAll();
            
            System.out.println(oxres.create(ctx, usr, access, auth));
            
        }catch(java.rmi.ConnectException neti){
            printError(neti.getMessage());            
        }catch(java.lang.NumberFormatException num){
            printInvalidInputMsg("Ids must be numbers!");
        }catch(org.apache.commons.cli.MissingArgumentException as){
            printError("Missing arguments on the command line: " + as.getMessage());;
            printHelpText("create", options);
        }catch(org.apache.commons.cli.UnrecognizedOptionException ux){
            printError("Unrecognized options on the command line: " + ux.getMessage());;
            printHelpText("create", options);
        } catch (org.apache.commons.cli.MissingOptionException mis) {
            printError("Missing options on the command line: " + mis.getMessage());;
            printHelpText("create", options);
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {            
            printServerResponse(e.getMessage());
        } catch (RemoteException e) {            
            printServerResponse(e.getMessage());
        } catch (NotBoundException e) {
            printServerResponse(e.getMessage());
        } catch (StorageException e) {            
            printServerResponse(e.getMessage());
        } catch (InvalidCredentialsException e) {
            printServerResponse(e.getMessage());
        } catch (NoSuchContextException e) {            
            printServerResponse(e.getMessage());
        } catch (InvalidDataException e) {            
            printServerResponse(e.getMessage());
        }

    }



    private Options getOptions() {
        Options retval = getDefaultCommandLineOptions();
        
        // add mandattory  options
        retval.addOption(getUsernameOption());
        retval.addOption(getDisplayNameOption());
        retval.addOption(getGivenNameOption());
        retval.addOption(getSurNameOption());
        retval.addOption(getPasswordOption());
        retval.addOption(getPrimaryMailOption());
        
        
        // add optional opts
        retval.addOption(getLanguageOption());
        retval.addOption(getTimezoneOption());        
        retval.addOption(getDepartmentOption());
        retval.addOption(getCompanyOption());
        
        return retval;
        
    }
    
//  private Options generateOptions() {
//  Options options = getDefaultCommandLineOptions();
//
//  // generate list of available shortopts
//  char []shorts = generateShortOpts();
//  int cnt=0;
//  System.out.println(shorts);
//  // create options for all member variables of User class
//  for (final Field f : User.class.getDeclaredFields()) {
//      try {
//          String longopt = f.getName();
//          // exclude some members, because we only have a limited amount
//          // of short options (2*26+10)
//          if( isInCmdLine(longopt) ) {
//              String shortopt = String.valueOf(shorts[cnt++]);
//              System.out.println("SHORT: " + shortopt);
//              options.addOption(shortopt, longopt, false, "");
//          }
//      } catch (IllegalArgumentException e) {
//          e.printStackTrace();
//          System.out.println("IllegalArgument\n");
//      }
//  }
//  return options;
//}


}
