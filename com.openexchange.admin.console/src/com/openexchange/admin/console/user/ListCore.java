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
 *    trademarks of the OX Software GmbH group of companies.
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
 *     Copyright (C) 2016-2020 OX Software GmbH
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
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.Map.Entry;
import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.rmi.OXUserInterface;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.rmi.dataobjects.UserModuleAccess;
import com.openexchange.admin.rmi.exceptions.DatabaseUpdateException;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.NoSuchContextException;
import com.openexchange.admin.rmi.exceptions.NoSuchUserException;
import com.openexchange.admin.rmi.exceptions.StorageException;


/**
 * {@link ListCore}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.0
 */
public abstract class ListCore extends UserAbstraction {

    private static final Object USER_ATTRIBUTES = "UserAttributes";

    protected void setOptions(final AdminParser parser) {
        setDefaultCommandLineOptions(parser);
        setCSVOutputOption(parser);
        setFurtherOptions(parser);
    }

    protected abstract void setFurtherOptions(final AdminParser parser);

    protected void commonfunctions(final AdminParser parser, final String[] args) {
        // set all needed options in our parser
        setOptions(parser);
        try {
            parser.ownparse(args);
            final Context ctx = contextparsing(parser);
            final Credentials auth = credentialsparsing(parser);
            // get rmi ref
            final OXUserInterface oxusr = getUserInterface();

            User[] allusers = maincall(parser, oxusr, ctx, auth);

            if (null != parser.getOptionValue(this.csvOutputOption)) {
                // map user data to corresponding module access
                final HashMap<Integer, UserModuleAccess> usr2axs = new HashMap<Integer, UserModuleAccess>();

                for (final User user : allusers) {
                    // fetch module access for every user
                    usr2axs.put(user.getId(), oxusr.getModuleAccess(ctx, user, auth));
                }
                precsvinfos(allusers, usr2axs);
            } else {
                sysoutOutput(allusers);
            }

            sysexit(0);
        } catch (Exception e) {
            printErrors(null, ctxid, e, parser);
        }
    }

    protected abstract User[] maincall(final AdminParser parser, final OXUserInterface oxusr, final Context ctx, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, DatabaseUpdateException, NoSuchUserException, Exception;

    /**
     * This methods collects the information from the user object and calls the
     * general cvs output method
     * 
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws InvalidDataException
     *
     */
    @SuppressWarnings("unchecked")
    protected void precsvinfos(final User[] users, final HashMap<Integer, UserModuleAccess> access_map) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, InvalidDataException {
        final Method[] methods = User.class.getMethods();
        // Filter out the old gui_spam_filter_enabled getters
        final Map<String, String> notallowedOrReplace = new HashMap<String, String>();
        notallowedOrReplace.put("GUI_Spam_filter_capabilities_enabled", "");
        notallowedOrReplace.put("Spam_filter_enabled", "");
        notallowedOrReplace.put("Gui_spam_filter_enabled", "GUI_Spam_filter_capabilities_enabled");
        notallowedOrReplace.put("ImapSchema", "");
        notallowedOrReplace.put("ImapServer", "");
        notallowedOrReplace.put("ImapServerString", "ImapServer");
        notallowedOrReplace.put("ImapPort", "");
        notallowedOrReplace.put("SmtpSchema", "");
        notallowedOrReplace.put("SmtpServer", "");
        notallowedOrReplace.put("SmtpServerString", "SmtpServer");
        notallowedOrReplace.put("SmtpPort", "");
        final ArrayList<MethodAndNames> methArrayList = getGetters(methods, notallowedOrReplace);

        final ArrayList<String> columnnames = new ArrayList<String>(32);
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
            columnnames.add(UserAbstraction.OPT_ACCESS_ICAL);
            columnnames.add(UserAbstraction.OPT_ACCESS_INFOSTORE);
            columnnames.add(UserAbstraction.OPT_ACCESS_READCREATE_SHARED_FOLDERS);
            columnnames.add(UserAbstraction.OPT_ACCESS_SYNCML);
            columnnames.add(UserAbstraction.OPT_ACCESS_TASKS);
            columnnames.add(UserAbstraction.OPT_ACCESS_VCARD);
            columnnames.add(UserAbstraction.OPT_ACCESS_WEBDAV);
            columnnames.add(UserAbstraction.OPT_ACCESS_WEBDAV_XML);
            columnnames.add(UserAbstraction.OPT_ACCESS_WEBMAIL);
            columnnames.add(UserAbstraction.OPT_ACCESS_EDIT_GROUP);
            columnnames.add(UserAbstraction.OPT_ACCESS_EDIT_RESOURCE);
            columnnames.add(UserAbstraction.OPT_ACCESS_EDIT_PASSWORD);
            columnnames.add(UserAbstraction.OPT_ACCESS_COLLECT_EMAIL_ADDRESSES);
            columnnames.add(UserAbstraction.OPT_ACCESS_MULTIPLE_MAIL_ACCOUNTS);
            columnnames.add(UserAbstraction.OPT_ACCESS_SUBSCRIPTION);
            columnnames.add(UserAbstraction.OPT_ACCESS_PUBLICATION);
            columnnames.add(UserAbstraction.OPT_ACCESS_ACTIVE_SYNC);
            columnnames.add(UserAbstraction.OPT_ACCESS_USM);
            columnnames.add(UserAbstraction.OPT_ACCESS_OLOX20);
            columnnames.add(UserAbstraction.OPT_ACCESS_DENIED_PORTAL);
            columnnames.add(UserAbstraction.OPT_DISABLE_GAB);
            columnnames.add(UserAbstraction.OPT_ACCESS_PUBLIC_FOLDER_EDITABLE);

        }
        final ArrayList<ArrayList<String>> data = new ArrayList<ArrayList<String>>();
        for (final User user : users) {
            final ArrayList<String> datarow = new ArrayList<String>();
            for (final MethodAndNames methodandnames : methArrayList) {
                final String returntype = methodandnames.getReturntype();
                if (returntype.equals(JAVA_LANG_STRING)) {
                    datarow.add((String) methodandnames.getMethod().invoke(user, (Object[]) null));
                } else if (returntype.equals(JAVA_LANG_INTEGER)) {
                    datarow.add(String.valueOf(methodandnames.getMethod().invoke(user, (Object[]) null)));
                } else if (returntype.equals(JAVA_LANG_LONG)) {
                    datarow.add(String.valueOf(methodandnames.getMethod().invoke(user, (Object[]) null)));
                } else if (returntype.equals(JAVA_LANG_BOOLEAN)) {
                    datarow.add(booleantostring((Boolean) methodandnames.getMethod().invoke(user, (Object[]) null)));
                } else if (returntype.equals(JAVA_UTIL_DATE)) {
                    datarow.add(datetostring((Date) methodandnames.getMethod().invoke(user, (Object[]) null)));
                } else if (returntype.equals(JAVA_UTIL_HASH_SET)) {
                    datarow.add(hashtostring((HashSet<?>) methodandnames.getMethod().invoke(user, (Object[]) null)));
                } else if (returntype.equals(JAVA_UTIL_MAP)) {
                    if (methodandnames.getName().equals(USER_ATTRIBUTES)) {
                        datarow.add(userattributestostring((Map<String, Map<String, String>>) methodandnames.getMethod().invoke(user, (Object[]) null)));
                    } else {
                        datarow.add(maptostring((HashMap<?, ?>) methodandnames.getMethod().invoke(user, (Object[]) null)));
                    }
                } else if (returntype.equals(JAVA_UTIL_TIME_ZONE)) {
                    datarow.add(timezonetostring((TimeZone) methodandnames.getMethod().invoke(user, (Object[]) null)));
                } else if (returntype.equals(JAVA_UTIL_LOCALE)) {
                    datarow.add(((Locale) methodandnames.getMethod().invoke(user, (Object[]) null)).toString());
                } else if (returntype.equals(SIMPLE_INT)) {
                    datarow.add(((Integer) methodandnames.getMethod().invoke(user, (Object[]) null)).toString());
                }
            }
            datarow.addAll(getDataOfAllExtensions(user));

            // add module access
            final UserModuleAccess access = access_map.get(user.getId());
            datarow.add(String.valueOf(access.getCalendar()));
            datarow.add(String.valueOf(access.getContacts()));
            datarow.add(String.valueOf(access.getDelegateTask()));
            datarow.add(String.valueOf(access.getEditPublicFolders()));
            datarow.add(String.valueOf(access.getIcal()));
            datarow.add(String.valueOf(access.getInfostore()));
            datarow.add(String.valueOf(access.getReadCreateSharedFolders()));
            datarow.add(String.valueOf(access.getSyncml()));
            datarow.add(String.valueOf(access.getTasks()));
            datarow.add(String.valueOf(access.getVcard()));
            datarow.add(String.valueOf(access.getWebdav()));
            datarow.add(String.valueOf(access.getWebdavXml()));
            datarow.add(String.valueOf(access.getWebmail()));
            datarow.add(String.valueOf(access.getEditGroup()));
            datarow.add(String.valueOf(access.getEditResource()));
            datarow.add(String.valueOf(access.getEditPassword()));
            datarow.add(String.valueOf(access.isCollectEmailAddresses()));
            datarow.add(String.valueOf(access.isMultipleMailAccounts()));
            datarow.add(String.valueOf(access.isSubscription()));
            datarow.add(String.valueOf(access.isPublication()));
            datarow.add(String.valueOf(access.isActiveSync()));
            datarow.add(String.valueOf(access.isUSM()));
            datarow.add(String.valueOf(access.isOLOX20()));
            datarow.add(String.valueOf(access.isDeniedPortal()));
            datarow.add(String.valueOf(access.isGlobalAddressBookDisabled()));
            datarow.add(String.valueOf(access.isPublicFolderEditable()));
            data.add(datarow);
            printExtensionsError(user);
        }
        doCSVOutput(columnnames, data);
    }

    protected abstract ArrayList<String> getColumnsOfAllExtensions(final User user);

    protected abstract ArrayList<String> getDataOfAllExtensions(final User user) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException;

    private String userattributestostring(final Map<String, Map<String, String>> dynamicAttributes) {
        if (dynamicAttributes.size() == 0) {
            return "";
        }
        final StringBuilder builder = new StringBuilder(128);
        for (final Map.Entry<String, Map<String, String>> namespaced : dynamicAttributes.entrySet()) {
            builder.append(namespaced.getKey()).append("=[").append(maptostring(namespaced.getValue())).append("],");
        }
        builder.deleteCharAt(builder.length() - 1);
        return builder.toString();
    }

    @Override
    protected final String getObjectName() {
        return "users";
    }

    protected final String maptostring(final Map<?, ?> map) {
        if (null != map && map.size() > 0) {
            @SuppressWarnings("unchecked")
            final HashMap<String, String> hashMap = (HashMap<String, String>) map;
            final Iterator<Entry<String, String>> i = hashMap.entrySet().iterator();
            final StringBuilder sb = new StringBuilder();
            while (i.hasNext()) {
                final Entry<String, String> e = i.next();
                sb.append(e.getKey());
                sb.append("=");
                sb.append(e.getValue());
                sb.append(", ");
            }
            sb.deleteCharAt(sb.length() - 1);
            sb.deleteCharAt(sb.length() - 1);
            return sb.toString();
        }
        return null;
    }

    protected final void sysoutOutput(final User[] users) throws InvalidDataException {

        final ArrayList<ArrayList<String>> data = new ArrayList<ArrayList<String>>();
        for (final User user : users) {
            printExtensionsError(user);
            data.add(makeStandardData(user));
        }

        //        doOutput(new String[] { "3r", "30l", "30l", "14l" },
        doOutput(new String[] { "r", "l", "l", "l", "l", "l" },
            new String[] { "Id", "Name", "Displayname", "Email", "qmax", "qused" }, data);
    }

    private ArrayList<String> makeStandardData(final User user) {
        final ArrayList<String> res_data = new ArrayList<String>();

        res_data.add(String.valueOf(user.getId()));// id

        {
            final String name = user.getName();
            if (name != null && name.trim().length() > 0) {
                res_data.add(name);// name
            } else {
                res_data.add(null);// name
            }
        }

        {
            final String displayname = user.getDisplay_name();
            if (displayname != null && displayname.trim().length() > 0) {
                res_data.add(displayname);// displayname
            } else {
                res_data.add(null);// displayname
            }
        }

        {
            final String email = user.getPrimaryEmail();
            if (email != null && email.trim().length() > 0) {
                res_data.add(email);// email
            } else {
                res_data.add(null);// email
            }
        }

        {
            final Long qmax = user.getMaxQuota();
            if (null != qmax) {
                res_data.add(qmax.toString());// qmax
            } else {
                res_data.add(null);// qmax
            }
        }

        {
            final Long qused = user.getUsedQuota();
            if (null != qused) {
                res_data.add(qused.toString());// qused
            } else {
                res_data.add(null);// qused
            }
        }

        return res_data;
    }

    protected final String timezonetostring(final TimeZone zone) {
        return zone.getID();
    }

    /**
     * This method is used to define how a date value is transferred to string
     *
     * @param date
     * @return the string representation of this date
     */
    protected final String datetostring(final Date date) {
        final SimpleDateFormat sdf = new SimpleDateFormat(COMMANDLINE_DATEFORMAT);
        sdf.setTimeZone(TimeZone.getTimeZone(COMMANDLINE_TIMEZONE));
        return null == date ? null : sdf.format(date);
    }

    /**
     * We need <code>null</code> instead of "null", so using {@link String#valueOf(Object)} is not sufficient.
     *
     * @param boolean1
     * @return the string representation of this boolean
     */
    protected final String booleantostring(final Boolean boolean1) {
        return null == boolean1 ? null : boolean1.toString();
    }

    protected final String hashtostring(final HashSet<?> set) {
        if (null != set && set.size() > 0) {
            final String[] hashvalues = set.toArray(new String[set.size()]);
            final StringBuilder sb = new StringBuilder();
            for (final String value : hashvalues) {
                sb.append(value);
                sb.append(", ");
            }
            sb.deleteCharAt(sb.length() - 1);
            sb.deleteCharAt(sb.length() - 1);
            return sb.toString();
        }
        return null;
    }
}
