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
import java.net.MalformedURLException;
import java.rmi.ConnectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.console.AdminParser.MissingOptionException;
import com.openexchange.admin.console.AdminParser.NeededTriState;
import com.openexchange.admin.console.CmdLineParser.IllegalOptionValueException;
import com.openexchange.admin.console.CmdLineParser.UnknownOptionException;
import com.openexchange.admin.rmi.OXUserInterface;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.rmi.dataobjects.UserModuleAccess;
import com.openexchange.admin.rmi.exceptions.DatabaseUpdateException;
import com.openexchange.admin.rmi.exceptions.DuplicateExtensionException;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.NoSuchContextException;
import com.openexchange.admin.rmi.exceptions.NoSuchUserException;
import com.openexchange.admin.rmi.exceptions.StorageException;

public abstract class ChangeCore extends UserAbstraction {

    protected final void setOptions(final AdminParser parser) {
        
        setExtendedOption(parser);
        setDefaultCommandLineOptions(parser);

        // required
        setIdOption(parser);

        displayNameOption = setShortLongOpt(parser,OPT_DISPLAYNAME_SHORT,OPT_DISPLAYNAME_LONG,"Display name of the user", true, NeededTriState.notneeded); 
        givenNameOption =  setShortLongOpt(parser,OPT_GIVENNAME_SHORT,OPT_GIVENNAME_LONG,"Given name for the user", true, NeededTriState.notneeded); 
        surNameOption =  setShortLongOpt(parser,OPT_SURNAME_SHORT,OPT_SURNAME_LONG,"Sur name for the user", true, NeededTriState.notneeded); 
        passwordOption = setShortLongOpt(parser,OPT_PASSWORD_SHORT,OPT_PASSWORD_LONG,"Password for the user", true, NeededTriState.notneeded); 
        primaryMailOption = setShortLongOpt(parser,OPT_PRIMARY_EMAIL_SHORT,OPT_PRIMARY_EMAIL_LONG,"Primary mail address", true, NeededTriState.notneeded); 

        
        // add optional opts
        setOptionalOptions(parser);
        
        setFurtherOptions(parser);
    }

    protected abstract void setFurtherOptions(final AdminParser parser);

    protected final void commonfunctions(final AdminParser parser, final String[] args) {
        // set all needed options in our parser
        setOptions(parser);

        setExtendedOptions(parser);

        // parse the command line
        try {
            parser.ownparse(args);

            printExtendedOutputIfSet(parser);
            
            final Context ctx = contextparsing(parser);

            final Credentials auth = credentialsparsing(parser);

            // get rmi ref
            final OXUserInterface oxusr = getUserInterface();

            // create user obj
            final User usr = new User();

            // set mandatory id
            usr.setId(Integer.parseInt((String) parser.getOptionValue(this.idOption)));

            // fill user obj with mandatory values from console
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

            // add optional values if set
            setOptionalOptionsinUser(parser, usr);

            applyExtendedOptionsToUser(parser, usr);
            
            maincall(parser, oxusr, ctx, usr, auth);

            // now change module access
            // first load current module access rights from server
            UserModuleAccess access = oxusr.getModuleAccess(ctx, usr, auth);                    
            
            // apply rights from commandline
            setModuleAccessOptionsinUserChange(parser, access);
            
            // apply changes in module access on server
            oxusr.changeModuleAccess(ctx, usr, access, auth);

            sysexit(0);
        } catch (final ConnectException neti) {
            printError(neti.getMessage());
            sysexit(SYSEXIT_COMMUNICATION_ERROR);
        } catch (final NumberFormatException num) {
            printInvalidInputMsg("Ids must be numbers!");
            sysexit(1);
        } catch (final IllegalOptionValueException e) {
            printError("Illegal option value : " + e.getMessage());
            printrightoptions(parser);
            sysexit(SYSEXIT_ILLEGAL_OPTION_VALUE);
        } catch (final UnknownOptionException e) {
            printError("Unrecognized options on the command line: " + e.getMessage());
            printrightoptions(parser);
            sysexit(SYSEXIT_UNKNOWN_OPTION);
        } catch (final MissingOptionException e) {
            printError(e.getMessage());
            printrightoptions(parser);
            sysexit(SYSEXIT_MISSING_OPTION);
        } catch (final MalformedURLException e) {
            printServerException(e);
            sysexit(1);
        } catch (final RemoteException e) {
            printServerException(e);
            sysexit(SYSEXIT_REMOTE_ERROR);
        } catch (final NotBoundException e) {
            printServerException(e);
            sysexit(1);
        } catch (final IllegalArgumentException e) {
            printError(e.getMessage());
            sysexit(1);
        } catch (final IllegalAccessException e) {
            printError(e.getMessage());
            sysexit(1);
        } catch (final InvocationTargetException e) {
            printError(e.getMessage());
            sysexit(1);
        } catch (final StorageException e) {
            printServerException(e);
            sysexit(SYSEXIT_SERVERSTORAGE_ERROR);
        } catch (final InvalidCredentialsException e) {
            printServerException(e);
            sysexit(SYSEXIT_INVALID_CREDENTIALS);
        } catch (final NoSuchContextException e) {
            printServerException(e);
            sysexit(SYSEXIT_NO_SUCH_CONTEXT);
        } catch (final InvalidDataException e) {
            printServerException(e);
            sysexit(SYSEXIT_INVALID_DATA);
        } catch (final DatabaseUpdateException e) {
            printServerException(e);
            sysexit(1);
        } catch (final NoSuchUserException e) {
            printServerException(e);
            sysexit(SYSEXIT_NO_SUCH_USER);
        } catch (final DuplicateExtensionException e) {
            printServerException(e);
            sysexit(1);
        }

    }

    protected abstract void maincall(final AdminParser parser, final OXUserInterface oxusr, final Context ctx, final User usr, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, DatabaseUpdateException, NoSuchUserException, MalformedURLException, NotBoundException, DuplicateExtensionException;

}
