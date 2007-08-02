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
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TimeZone;

import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.console.AdminParser.NeededQuadState;
import com.openexchange.admin.rmi.OXUserInterface;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.rmi.dataobjects.UserModuleAccess;
import com.openexchange.admin.rmi.dataobjects.User.PASSWORDMECH;
import com.openexchange.admin.rmi.exceptions.DatabaseUpdateException;
import com.openexchange.admin.rmi.exceptions.DuplicateExtensionException;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.NoSuchContextException;
import com.openexchange.admin.rmi.exceptions.NoSuchUserException;
import com.openexchange.admin.rmi.exceptions.StorageException;

public abstract class ListCore extends UserAbstraction {

    private static final String FALSE_STRING = "false";
    private static final String TRUE_STRING = "true";

    protected final void setOptions(final AdminParser parser) {
        setDefaultCommandLineOptions(parser);
        
        setCSVOutputOption(parser);
        this.searchOption = setShortLongOpt(parser, OPT_NAME_SEARCHPATTERN, OPT_NAME_SEARCHPATTERN_LONG, "The search pattern which is used for listing. This applies to name.", true, NeededQuadState.notneeded);
        setFurtherOptions(parser);
    }

    protected abstract void setFurtherOptions(final AdminParser parser);

    protected final void commonfunctions(final AdminParser parser, final String[] args) {
        // set all needed options in our parser
        setOptions(parser);

        // parse the command line
        try {
            parser.ownparse(args);

            final Context ctx = contextparsing(parser);

            final Credentials auth = credentialsparsing(parser);

            // get rmi ref
            final OXUserInterface oxusr = getUserInterface();

            String pattern = (String) parser.getOptionValue(this.searchOption);

            if (null == pattern) {
                pattern = "*";
            }

            final User[] allusers = maincall(parser, oxusr, pattern, ctx, auth);

            // map user data to corresponding module access
            final HashMap<Integer, UserModuleAccess> usr2axs = new HashMap<Integer, UserModuleAccess>();

            for (final User user : allusers) {      
                // fetch module access for every user
                usr2axs.put(user.getId(), oxusr.getModuleAccess(ctx, user, auth));
            }           

            if (null != parser.getOptionValue(this.csvOutputOption)) {
                precsvinfos(allusers,usr2axs);
            } else {
                sysoutOutput(allusers,usr2axs);
            }

            sysexit(0);
        } catch (final Exception e) {
            printErrors(null, ctxid, e, parser);
        }

    }

    protected abstract User[] maincall(final AdminParser parser, final OXUserInterface oxusr, final String search_pattern, final Context ctx, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, DatabaseUpdateException, NoSuchUserException, DuplicateExtensionException;

    /**
     * This method is used to define how a date value is transferred to string
     * 
     * @param date
     * @return the string representation of this date
     */
    protected final String datetostring(final Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat(COMMANDLINE_DATEFORMAT);
        sdf.setTimeZone(TimeZone.getTimeZone(COMMANDLINE_TIMEZONE));
        if (null != date) {
            return sdf.format(date);
        } else {
            return null;
        }
    }

    /**
     * This method is used to define how a boolean value is transferred to string
     * 
     * @param boolean1
     * @return the string representation of this boolean
     */
    protected final String booleantostring(final Boolean boolean1) {
        if (null != boolean1) {
            if (boolean1) {
                return TRUE_STRING;
            } else {
                return FALSE_STRING;
            }
        } else {
            return null;
        }
    }

    protected final String hashtostring(final HashSet<?> set) {
        if (null != set && set.size() > 0) {
            final String[] hashvalues = set.toArray(new String[set.size()]);
            final StringBuilder sb = new StringBuilder();
            for (final String value : hashvalues) {
                sb.append(value);
                sb.append(", ");
            }
            sb.deleteCharAt(sb.length()-1);
            sb.deleteCharAt(sb.length()-1);
            return sb.toString();
        } else {
            return null;
        }
    }

    protected final String passwordtostring(final PASSWORDMECH passwordmech2) {
        if (passwordmech2 == PASSWORDMECH.CRYPT) {
            return "crypt";
        } else {
            return "sha";
        }
    }

    protected final void sysoutOutput(final User[] users, final HashMap<Integer, UserModuleAccess> user_access) throws InvalidDataException {

        final ArrayList<ArrayList<String>> data = new ArrayList<ArrayList<String>>();
        for (final User user : users) {
            printExtensionsError(user);
            data.add(makeStandardData(user));
        }
        
        doOutput(new String[] { "3r", "30l", "30l", "14l" },
                 new String[] { "Id", "Name", "Displayname", "Email" }, data);
    }

    private ArrayList<String> makeStandardData(final User user) {
        final ArrayList<String> res_data = new ArrayList<String>();
        
        res_data.add(String.valueOf(user.getId())); // id
    
        final String name = user.getUsername();
        if (name != null && name.trim().length() > 0) {
            res_data.add(name); // name
        } else {
            res_data.add(null); // name
        }
    
        final String displayname = user.getDisplay_name();
        if (displayname != null && displayname.trim().length() > 0) {
            res_data.add(displayname); // displayname
        } else {
            res_data.add(null); // displayname
        }
    
        final String email = user.getPrimaryEmail();
        if (email != null && email.trim().length() > 0) {
            res_data.add(email); // email
        } else {
            res_data.add(null); // email
        }
        return res_data;
    }

    protected final String timezonetostring(final TimeZone zone) {
        return zone.getDisplayName();
    }

    /**
     * This methods collects the information from the user object and calls the
     * general cvs output method
     * @throws InvocationTargetException 
     * @throws IllegalAccessException 
     * @throws IllegalArgumentException 
     * 
     */
    private void precsvinfos(final User[] users,final HashMap<Integer, UserModuleAccess> access_map) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        final Method[] methods = User.class.getMethods();
        final ArrayList<MethodAndNames> methArrayList = getGetters(methods, new HashSet<String>());
        
        final ArrayList<String> columnnames = new ArrayList<String>();
        for (final MethodAndNames methodandnames : methArrayList) {
            columnnames.add(methodandnames.getName());
        }
        
        if (users.length > 0) {
            columnnames.addAll(getColumnsOfAllExtensions(users[0]));
            
            // module access columns
            columnnames.add(UserAbstraction.OPT_ACCESS_CALENDAR);
            columnnames.add(UserAbstraction.OPT_ACCESS_CONTACTS);
            columnnames.add(UserAbstraction.OPT_ACCESS_DELEGATE_TASKS);
            columnnames.add(UserAbstraction.OPT_ACCESS_EDIT_PUBLIC_FOLDERS);
            columnnames.add(UserAbstraction.OPT_ACCESS_FORUM);
            columnnames.add(UserAbstraction.OPT_ACCESS_ICAL);
            columnnames.add(UserAbstraction.OPT_ACCESS_INFOSTORE);
            columnnames.add(UserAbstraction.OPT_ACCESS_PINBOARD_WRITE);
            columnnames.add(UserAbstraction.OPT_ACCESS_PROJECTS);
            columnnames.add(UserAbstraction.OPT_ACCESS_READCREATE_SHARED_FOLDERS);
            columnnames.add(UserAbstraction.OPT_ACCESS_RSS_BOOKMARKS);
            columnnames.add(UserAbstraction.OPT_ACCESS_RSS_PORTAL);
            columnnames.add(UserAbstraction.OPT_ACCESS_SYNCML);
            columnnames.add(UserAbstraction.OPT_ACCESS_TASKS);
            columnnames.add(UserAbstraction.OPT_ACCESS_VCARD);
            columnnames.add(UserAbstraction.OPT_ACCESS_WEBDAV);
            columnnames.add(UserAbstraction.OPT_ACCESS_WEBDAV_XML);
            columnnames.add(UserAbstraction.OPT_ACCESS_WEBMAIL);
            
            
        }        
        final ArrayList<ArrayList<String>> data = new ArrayList<ArrayList<String>>();
        for (final User user : users) {
            final ArrayList<String> datarow = new ArrayList<String>();
            for (final MethodAndNames methodandnames : methArrayList) {
                final String returntype = methodandnames.getReturntype();
                if (returntype.equals(JAVA_LANG_STRING)) {
                    datarow.add((String)methodandnames.getMethod().invoke(user, (Object[]) null));
                } else if (returntype.equals(JAVA_LANG_INTEGER)) {
                    datarow.add(String.valueOf(methodandnames.getMethod().invoke(user, (Object[]) null)));
                } else if (returntype.equals(JAVA_LANG_BOOLEAN)) {
                    datarow.add(booleantostring((Boolean)methodandnames.getMethod().invoke(user, (Object[]) null)));
                } else if (returntype.equals(JAVA_UTIL_DATE)) {
                    datarow.add(datetostring((Date)methodandnames.getMethod().invoke(user, (Object[]) null)));
                } else if (returntype.equals(JAVA_UTIL_HASH_SET)) {
                    datarow.add(hashtostring((HashSet<?>)methodandnames.getMethod().invoke(user, (Object[]) null)));
                } else if (returntype.equals(PASSWORDMECH_CLASS)) {
                    datarow.add(passwordtostring((PASSWORDMECH)methodandnames.getMethod().invoke(user, (Object[]) null)));
                } else if (returntype.equals(JAVA_UTIL_TIME_ZONE)) {
                    datarow.add(timezonetostring((TimeZone)methodandnames.getMethod().invoke(user, (Object[]) null)));
                }
            }
            datarow.addAll(getDataOfAllExtensions(user));
            
            // add module access 
            UserModuleAccess access = access_map.get(user.getId());
            datarow.add(String.valueOf(access.getCalendar()));
            datarow.add(String.valueOf(access.getContacts()));
            datarow.add(String.valueOf(access.getDelegateTask()));
            datarow.add(String.valueOf(access.getEditPublicFolders()));
            datarow.add(String.valueOf(access.getForum()));
            datarow.add(String.valueOf(access.getIcal()));
            datarow.add(String.valueOf(access.getInfostore()));
            datarow.add(String.valueOf(access.getPinboardWrite()));
            datarow.add(String.valueOf(access.getProjects()));
            datarow.add(String.valueOf(access.getReadCreateSharedFolders()));
            datarow.add(String.valueOf(access.getRssBookmarks()));
            datarow.add(String.valueOf(access.getRssPortal()));
            datarow.add(String.valueOf(access.getSyncml()));
            datarow.add(String.valueOf(access.getTasks()));
            datarow.add(String.valueOf(access.getVcard()));
            datarow.add(String.valueOf(access.getWebdav()));
            datarow.add(String.valueOf(access.getWebdavXml()));
            datarow.add(String.valueOf(access.getWebmail()));
            
            data.add(datarow);
            printExtensionsError(user);
        }
        doCSVOutput(columnnames, data);
    }

    protected abstract ArrayList<String> getDataOfAllExtensions(final User user) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException;

    protected abstract ArrayList<String> getColumnsOfAllExtensions(final User user);

    @Override
    protected final String getObjectName() {
        return "users";
    }
}
