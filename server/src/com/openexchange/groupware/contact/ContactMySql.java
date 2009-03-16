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

package com.openexchange.groupware.contact;

import static com.openexchange.java.Autoboxing.I;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.openexchange.api2.OXException;
import com.openexchange.groupware.contact.Contacts.mapper;
import com.openexchange.groupware.container.ContactObject;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextException;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.search.ContactSearchObject;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.groupware.userconfiguration.UserConfigurationStorage;
import com.openexchange.server.impl.EffectivePermission;
import com.openexchange.session.Session;
import com.openexchange.tools.StringCollection;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorException;
import com.openexchange.tools.oxfolder.OXFolderAccess;
import com.openexchange.tools.oxfolder.OXFolderIteratorSQL;

/**
 * {@link ContactMySql} - The MySQL implementation of {@link ContactSql}.
 * <p>
 * This implementation is <b>not</b> designed for multi-threaded access and therefore is not thread-safe.
 * 
 * @author <a href="mailto:ben.pahne@comfire.de">Benjamin Frederic Pahne</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class ContactMySql implements ContactSql {

    private static final String STR_PERCENT = "%";

    private static final Log LOG = LogFactory.getLog(ContactMySql.class);

    private String select = "SELECT co.intfield01" + ",co.cid," + "co.timestampfield01," + "co.field03," + "co.field04," + "co.field06," + "co.field07," + "co.field09," + "co.field10," + "co.intfield03," + "co.field79 FROM prg_contacts AS co ";

    private String where;

    private String order = "";

    private int user;

    private int can_read_only_own;

    private int folder;

    private String all_folders;

    private ContactSearchObject cso;

    private long changed_since;

    private long created_since;

    private long both_since;

    private String search_habit = " AND ";

    private int[][] object_id_array;

    private int userid;

    private boolean internal_user_only;

    private int objectID;

    private final List<SQLInjector> injectors = new ArrayList<SQLInjector>();

    private Context ctx;

    /**
     * Initializes a new {@link ContactMySql}
     * 
     * @param so The session
     * @throws ContextException If context cannot be resolved
     */
    public ContactMySql(final Session so) throws ContextException {
        if (so != null) {
            this.ctx = ContextStorage.getStorageContext(so.getContextId());
            this.user = so.getUserId();
        }
    }

    /**
     * Initializes a new {@link ContactMySql}
     * 
     * @param ctx The context
     * @param userId The user ID
     */
    public ContactMySql(final Context ctx, final int userId) {
        this.ctx = ctx;
        this.user = userId;
    }

    /**
     * Initializes a new {@link ContactMySql}
     * 
     * @param so The session
     * @param ctx The context
     */
    public ContactMySql(final Session so, final Context ctx) {
        this.ctx = ctx;
        this.user = so.getUserId();
    }

    public String getOrder() {
        return order;
    }

    public PreparedStatement getSqlStatement(final Connection con) throws SQLException {
        final StringBuilder sb = new StringBuilder(256);
        sb.append(getSelect());
        if (all_folders != null && all_folders.length() > 1) {
            sb.append(all_folders);
        }
        sb.append(getWhere());
        sb.append(getOrder());
        if (LOG.isDebugEnabled()) {
            LOG.debug(new StringBuilder("ContactSQL Query: ").append(sb.toString()));
        }
        // System.out.println("-> "+sb.toString());
        final PreparedStatement ps = con.prepareStatement(sb.toString());
        final int size = injectors.size();
        for (int i = 0; i < size; i++) {
            injectors.get(i).inject(ps, i + 1);
        }
        injectors.clear();
        return ps;
    }

    private String getWhere() {

        if (null != where) {
            return where;
        }

        final StringBuilder sb = new StringBuilder(256);
        sb.append(" WHERE co.cid = ").append(ctx.getContextId()).append(" AND ");

        // Can read only own objects in folder
        if (can_read_only_own != 0) {
            sb.append(" (co.created_from = ?) AND ");
            injectors.add(new IntSQLInjector(can_read_only_own));
        }

        // only internal user
        if (internal_user_only) {
            sb.append(" (co.userid is not null) AND (fid = ").append(FolderObject.SYSTEM_LDAP_FOLDER_ID).append(") AND ");
        }

        // get a user by id
        if (userid > 0) {
            sb.append(" (co.userid = ").append(userid).append(") AND ");
        }

        // range search in time for field changed_from
        if (changed_since > 0) {
            sb.append(" (co.changing_date >= ").append(changed_since).append(") AND ");
        }

        // range search in time for field created_from
        if (created_since > 0) {
            sb.append(" (co.creating_date >= ").append(created_since).append(") AND ");
        }

        // range search in time for field created_from and changed_from
        if (created_since > 0) {
            sb.append(" (co.creating_date >= ").append(both_since).append(" OR (co.changed_from >= ").append(both_since).append(")) AND ");
        }

        // get an object by id
        if (objectID > 0) {
            sb.append(" (co.intfield01 = ").append(objectID).append(") AND ");
        }

        // get a bunch of objects by id
        if (object_id_array != null && object_id_array.length > 0) {
            sb.append(" ( ");
            for (int i = 0; i < object_id_array.length; i++) {
                final int oidx = object_id_array[i][0];
                final int fidx = object_id_array[i][1];
                sb.append(" (co.intfield01 = ").append(oidx).append(" AND co.fid = ").append(fidx).append(") ");
                if (i < (object_id_array.length - 1)) {
                    sb.append(" OR ");
                }
            }
            sb.append(" ) AND ");
        }

        if (cso != null) {

            if (cso.getEmailAutoComplete() || cso.isOrSearch()) {
                search_habit = " OR ";
            }

            sb.append(" ( ");

            /*********************** * search all fields * ***********************/

            if (cso.getPattern() != null && cso.getPattern().length() > 0) {
                if (cso.isStartLetter()) {
                    final String field = ContactConfig.getInstance().getString(ContactConfig.Property.LETTER_FIELD);
                    final String p = cso.getPattern().trim();

                    if (".".equals(p) || "#".equals(p)) {
                        sb.append(" (");
                        sb.append(field);
                        sb.append(" < '0%' OR ");
                        sb.append(field);
                        sb.append(" > 'z%') AND ");
                        sb.append(field);
                        sb.append(" NOT LIKE 'z%' AND ");
                    } else if (p.matches("\\d")) {
                        sb.append(" ");
                        sb.append(field);
                        sb.append(" > '0%' AND ");
                        sb.append(field);
                        sb.append(" < 'a%' AND ");
                    } else if (!".".equals(p) && !"all".equals(p)) {
                        sb.append(" ");
                        sb.append(field);
                        sb.append(" LIKE ? AND ");
                        injectors.add(new StringSQLInjector(p, STR_PERCENT));
                    }
                } else {
                    cso.setDisplayName(cso.getPattern());
                }
            }

            if (cso.getDynamicSearchField() != null && cso.getDynamicSearchField().length > 0) {
                final int[] fields = cso.getDynamicSearchField();
                final String[] values = cso.getDynamicSearchFieldValue();

                for (int i = 0; i < fields.length; i++) {
                    if ((fields[i] == ContactObject.ANNIVERSARY) || (fields[i] == ContactObject.BIRTHDAY)) {
                        String field = "";
                        if (fields[i] == ContactObject.ANNIVERSARY) {
                            field = Contacts.mapping[ContactObject.ANNIVERSARY].getDBFieldName();
                        } else if (fields[i] == ContactObject.BIRTHDAY) {
                            field = Contacts.mapping[ContactObject.BIRTHDAY].getDBFieldName();
                        }
                        /*
                         * TODO: BIRTHDAY: `timestampfield01` date default NULL, ANNIVERSARY: `timestampfield02` date default NULL,
                         */
                        final String value = values[i];
                        sb.append(" ( co.").append(field).append(" LIKE ").append(value).append(") ").append(search_habit).append(' ');
                    } else if (fields[i] == ContactObject.NUMBER_OF_DISTRIBUTIONLIST || fields[i] == ContactObject.NUMBER_OF_LINKS) {
                        String field = "";
                        if (fields[i] == ContactObject.NUMBER_OF_DISTRIBUTIONLIST) {
                            field = Contacts.mapping[ContactObject.NUMBER_OF_DISTRIBUTIONLIST].getDBFieldName();
                        } else if (fields[i] == ContactObject.NUMBER_OF_LINKS) {
                            field = Contacts.mapping[ContactObject.NUMBER_OF_LINKS].getDBFieldName();
                        }
                        final String value = values[i];
                        sb.append('(').append("co.").append(field).append(" = ").append(value).append(") ").append(search_habit).append(' ');
                    } else if (fields[i] == ContactObject.CATEGORIES) {
                        final String field = Contacts.mapping[ContactObject.CATEGORIES].getDBFieldName();
                        String value = values[i];

                        if (!"*".equals(value) && null != value) {
                            value = StringCollection.prepareForSearch(value, false);

                            if (value.indexOf(',') == -1) {
                                // No comma-separated value
                                sb.append('(').append("co.").append(field).append(" LIKE ?) ").append(search_habit).append(' ');
                                injectors.add(new StringSQLInjector(STR_PERCENT, value, STR_PERCENT));
                            } else {
                                // Comma-separated value
                                final String[] tokens = value.trim().split("\\s*,\\s*");
                                sb.append('(');

                                sb.append(" ( co.").append(field).append(" LIKE ? )");
                                injectors.add(new StringSQLInjector(STR_PERCENT, tokens[0].toUpperCase(), STR_PERCENT));
                                for (int j = 1; j < tokens.length; j++) {
                                    sb.append(" OR").append(" ( co.").append(field).append(" LIKE ? )");
                                    injectors.add(new StringSQLInjector(STR_PERCENT, tokens[j].toUpperCase(), STR_PERCENT));
                                }

                                sb.append(") ").append(search_habit).append(' ');
                            }
                        }
                    } else {
                        final String field = Contacts.mapping[fields[i]].getDBFieldName();
                        String value = values[i];

                        if (!"*".equals(value)) {
                            value = StringCollection.prepareForSearch(value);

                            sb.append("( co.").append(field).append(" LIKE ? ) ").append(search_habit).append(' ');
                            injectors.add(new StringSQLInjector(value));
                        }
                    }
                }
            }

            /*********************** * search ranges * ***********************/

            // final String language = UserStorage.getStorageUser(user, ctx).getLocale().getLanguage();
            if (cso.getAnniversaryRange() != null && cso.getAnniversaryRange().length > 0) {
                final Date[] d = cso.getAnniversaryRange();
                try {
                    final String field = Contacts.mapping[ContactObject.ANNIVERSARY].getDBFieldName();
                    sb.append("co.").append(field).append(" >= ? ").append(search_habit).append(' ');
                    sb.append("co.").append(field).append(" <= ? ").append(search_habit).append(' ');
                    injectors.add(new TimestampSQLInjector(d[0]));
                    injectors.add(new TimestampSQLInjector(d[1]));
                    /*
                     * final String a = new FormatDate(language.toLowerCase(), language.toUpperCase()) .formatDateForPostgres(d[0], false);
                     * final String b = new FormatDate(language.toLowerCase(), language.toUpperCase()) .formatDateForPostgres(d[0], false);
                     * sb.append(getRangeSearch(Contacts.mapping[ContactObject.ANNIVERSARY].getDBFieldName(), a, b, search_habit));
                     */
                } catch (final Exception e) {
                    LOG.error("Could not Format Anniversary Date for Range Search! ", e);
                }
            }
            if (cso.getBirthdayRange() != null && cso.getBirthdayRange().length > 0) {
                final Date[] d = cso.getBirthdayRange();
                try {
                    final String field = Contacts.mapping[ContactObject.BIRTHDAY].getDBFieldName();
                    sb.append("co.").append(field).append(" >= ? ").append(search_habit).append(' ');
                    sb.append("co.").append(field).append(" <= ? ").append(search_habit).append(' ');
                    injectors.add(new TimestampSQLInjector(d[0]));
                    injectors.add(new TimestampSQLInjector(d[1]));
                    /*
                     * final String a = new FormatDate(language.toLowerCase(), language.toUpperCase()) .formatDateForPostgres(d[0], false);
                     * final String b = new FormatDate(language.toLowerCase(), language.toUpperCase()) .formatDateForPostgres(d[0], false);
                     * sb.append(getRangeSearch(Contacts.mapping[ContactObject.BIRTHDAY].getDBFieldName(), a, b, search_habit));
                     */
                } catch (final Exception e) {
                    LOG.error("Could not Format Birthday Date for Range Search! ", e);
                }
            }
            if (cso.getBusinessPostalCodeRange() != null && cso.getBusinessPostalCodeRange().length > 0) {
                final String[] x = cso.getBusinessPostalCodeRange();
                sb.append(getRangeSearch(Contacts.mapping[ContactObject.POSTAL_CODE_BUSINESS].getDBFieldName(), x[0], x[1], search_habit));
            }
            if (cso.getCreationDateRange() != null && cso.getCreationDateRange().length > 0) {
                final Date[] d = cso.getCreationDateRange();
                try {
                    final String field = Contacts.mapping[ContactObject.CREATION_DATE].getDBFieldName();
                    sb.append("co.").append(field).append(" >= ? ").append(search_habit).append(' ');
                    sb.append("co.").append(field).append(" <= ? ").append(search_habit).append(' ');
                    injectors.add(new TimestampSQLInjector(d[0]));
                    injectors.add(new TimestampSQLInjector(d[1]));
                    /*
                     * final String a = new FormatDate(language.toLowerCase(), language.toUpperCase()) .formatDateForPostgres(d[0], false);
                     * final String b = new FormatDate(language.toLowerCase(), language.toUpperCase()) .formatDateForPostgres(d[0], false);
                     * sb.append(getRangeSearch(Contacts.mapping[ContactObject.CREATION_DATE].getDBFieldName(), a, b, search_habit));
                     */
                } catch (final Exception e) {
                    LOG.error("Could not Format Creating_Date Date for Range Search! ", e);
                }
            }
            if (cso.getLastModifiedRange() != null && cso.getLastModifiedRange().length > 0) {
                final Date[] d = cso.getLastModifiedRange();
                try {
                    final String field = Contacts.mapping[ContactObject.LAST_MODIFIED].getDBFieldName();
                    sb.append("co.").append(field).append(" >= ? ").append(search_habit).append(' ');
                    sb.append("co.").append(field).append(" <= ? ").append(search_habit).append(' ');
                    injectors.add(new TimestampSQLInjector(d[0]));
                    injectors.add(new TimestampSQLInjector(d[1]));
                    /*
                     * final String a = new FormatDate(language.toLowerCase(), language.toUpperCase()) .formatDateForPostgres(d[0], false);
                     * final String b = new FormatDate(language.toLowerCase(), language.toUpperCase()) .formatDateForPostgres(d[0], false);
                     * sb.append(getRangeSearch(Contacts.mapping[ContactObject.LAST_MODIFIED].getDBFieldName(), a, b, search_habit));
                     */
                } catch (final Exception e) {
                    LOG.error("Could not Format LastModified Date for Range Search! ", e);
                }
            }
            if (cso.getNumberOfEmployeesRange() != null && cso.getNumberOfEmployeesRange().length > 0) {
                final String[] x = cso.getNumberOfEmployeesRange();
                sb.append(getRangeSearch(Contacts.mapping[ContactObject.NUMBER_OF_EMPLOYEE].getDBFieldName(), x[0], x[1], search_habit));
            }
            if (cso.getOtherPostalCodeRange() != null && cso.getOtherPostalCodeRange().length > 0) {
                final String[] x = cso.getOtherPostalCodeRange();
                sb.append(getRangeSearch(Contacts.mapping[ContactObject.POSTAL_CODE_OTHER].getDBFieldName(), x[0], x[1], search_habit));
            }
            if (cso.getPrivatePostalCodeRange() != null && cso.getPrivatePostalCodeRange().length > 0) {
                final String[] x = cso.getPrivatePostalCodeRange();
                sb.append(getRangeSearch(Contacts.mapping[ContactObject.POSTAL_CODE_HOME].getDBFieldName(), x[0], x[1], search_habit));
            }
            if (cso.getSalesVolumeRange() != null && cso.getSalesVolumeRange().length > 0) {
                final String[] x = cso.getSalesVolumeRange();
                sb.append(getRangeSearch(Contacts.mapping[ContactObject.SALES_VOLUME].getDBFieldName(), x[0], x[1], search_habit));
            }

            /*********************** * search single field * ***********************/

            if (cso.getGivenName() != null && cso.getGivenName().length() > 0) {
                final String field = Contacts.mapping[ContactObject.GIVEN_NAME].getDBFieldName();

                final String value = StringCollection.prepareForSearch(cso.getGivenName());

                if (STR_PERCENT.equals(value)) {
                    sb.append(' ');
                } else {
                    sb.append('(').append("co.").append(field).append(" LIKE ?) ").append(search_habit).append(' ');
                    injectors.add(new StringSQLInjector(value));
                }
            }
            if (cso.getSurname() != null && cso.getSurname().length() > 0) {
                final String field = Contacts.mapping[ContactObject.SUR_NAME].getDBFieldName();

                final String value = StringCollection.prepareForSearch(cso.getSurname());

                if (STR_PERCENT.equals(value)) {
                    sb.append(' ');
                } else {
                    sb.append('(').append("co.").append(field).append(" LIKE ?) ").append(search_habit).append(' ');
                    injectors.add(new StringSQLInjector(value));
                }
            }
            if (cso.getDisplayName() != null && cso.getDisplayName().length() > 0) {
                final String field = Contacts.mapping[ContactObject.DISPLAY_NAME].getDBFieldName();

                final String value = StringCollection.prepareForSearch(cso.getDisplayName());

                if (STR_PERCENT.equals(value)) {
                    sb.append(' ');
                } else {
                    sb.append('(').append("co.").append(field).append(" LIKE ?) ").append(search_habit).append(' ');
                    injectors.add(new StringSQLInjector(value));
                }
            }
            if (cso.getEmail1() != null && cso.getEmail1().length() > 0) {
                final String field = Contacts.mapping[ContactObject.EMAIL1].getDBFieldName();

                final String value = StringCollection.prepareForSearch(cso.getEmail1());

                if (STR_PERCENT.equals(value)) {
                    sb.append(' ');
                } else {
                    sb.append('(').append("co.").append(field).append(" LIKE ?) ").append(search_habit).append(' ');
                    injectors.add(new StringSQLInjector(value));
                }
            }
            if (cso.getEmail2() != null && cso.getEmail2().length() > 0) {
                final String field = Contacts.mapping[ContactObject.EMAIL2].getDBFieldName();

                final String value = StringCollection.prepareForSearch(cso.getEmail2());

                if (STR_PERCENT.equals(value)) {
                    sb.append(' ');
                } else {
                    sb.append('(').append("co.").append(field).append(" LIKE ?) ").append(search_habit).append(' ');
                    injectors.add(new StringSQLInjector(value));
                }
            }
            if (cso.getEmail3() != null && cso.getEmail3().length() > 0) {
                final String field = Contacts.mapping[ContactObject.EMAIL3].getDBFieldName();

                final String value = StringCollection.prepareForSearch(cso.getEmail3());

                if (STR_PERCENT.equals(value)) {
                    sb.append(' ');
                } else {
                    sb.append('(').append("co.").append(field).append(" LIKE ?) ").append(search_habit).append(' ');
                    injectors.add(new StringSQLInjector(value));
                }
            }
            if (cso.getCatgories() != null && cso.getCatgories().length() > 0) {
                final String field = Contacts.mapping[ContactObject.CATEGORIES].getDBFieldName();
                String value = cso.getCatgories().trim();

                if (!"*".equals(value)) {
                    value = StringCollection.prepareForSearch(value, false);

                    if (value.indexOf(',') == -1) {
                        // No comma-separated value
                        sb.append('(').append("co.").append(field).append(" LIKE ?) ").append(search_habit).append(' ');
                        injectors.add(new StringSQLInjector(STR_PERCENT, value, STR_PERCENT));
                    } else {
                        // Comma-separated value
                        final String[] tokens = value.split("\\s*,\\s*");
                        sb.append('(');

                        sb.append("( co.").append(field).append(" LIKE ? )");
                        injectors.add(new StringSQLInjector(STR_PERCENT, tokens[0], STR_PERCENT));
                        for (int i = 1; i < tokens.length; i++) {
                            sb.append(" OR ").append("( co.").append(field).append(" LIKE ? )");
                            injectors.add(new StringSQLInjector(STR_PERCENT, tokens[i], STR_PERCENT));
                        }

                        sb.append(") ").append(search_habit).append(' ');
                    }
                }
            }
            if (cso.getCompany() != null && cso.getCompany().length() > 0) {
                final String field = Contacts.mapping[ContactObject.COMPANY].getDBFieldName();

                final String value = StringCollection.prepareForSearch(cso.getCompany());

                if (STR_PERCENT.equals(value)) {
                    sb.append(' ');
                } else {
                    sb.append("( co.").append(field).append(" LIKE ? ) ").append(search_habit).append(' ');
                    injectors.add(new StringSQLInjector(value));
                }
            }
            if (cso.getIgnoreOwn() > 0) {
                sb.append("( co.intfield01 != ").append(cso.getIgnoreOwn()).append(") ").append(search_habit).append(' ');
            }

            final int pos = endsWith(sb, "(", true);
            if (pos == -1) {
                final int pos2 = endsWith(sb, search_habit, true);
                if (pos2 != -1) {
                    sb.delete(pos2, sb.length());
                }
                sb.append(") AND ");
            } else {
                sb.delete(pos, sb.length());
            }

            if (null != cso.getAllFolderSQLINString()) {
                folder = -1;
                sb.append(cso.getAllFolderSQLINString());
                sb.append(" AND ");
            }
            // Special condition for email auto complete
            if (cso.getEmailAutoComplete()) {
                sb.append('(');
                sb.append(Contacts.mapping[ContactObject.EMAIL1].getDBFieldName());
                sb.append(" is not null OR ");
                sb.append(Contacts.mapping[ContactObject.EMAIL2].getDBFieldName());
                sb.append(" is not null OR ");
                sb.append(Contacts.mapping[ContactObject.EMAIL3].getDBFieldName());
                sb.append(" is not null OR ");
                sb.append(Contacts.mapping[ContactObject.MARK_AS_DISTRIBUTIONLIST].getDBFieldName());
                sb.append(" > 0) AND ");
            }
        }

        // Normal Folder
        if (folder != 0 && folder != -1) {
            sb.append(" (co.fid = ").append(folder).append(") AND ");
        }

        sb.append(' ');

        // Remove ending " AND "
        final int pos = endsWith(sb, "AND", true);
        if (pos != -1) {
            sb.delete(pos, sb.length());
        }

        /*
         * Private Flag
         */
        where = sb.append(" AND ((co.pflag = 1 and co.created_from = ").append(user).append(") OR (co.pflag is null))").toString();
        return where;
    }

    public String getSelect() {
        return this.select;
    }

    public void setSelect(final String select) {
        this.select = select;
    }

    public void setOrder(final String order) {
        this.order = order;
    }

    public void setFolder(final int folder) {
        this.folder = folder;
    }

    public void setObjectID(final int objectID) {
        this.objectID = objectID;
    }

    public void setReadOnlyOwnFolder(final int onlyown) {
        this.can_read_only_own = onlyown;
    }

    public void setContactSearchObject(final ContactSearchObject cso) {
        this.cso = cso;
    }

    public void setObjectArray(final int[][] object_id) {
        this.object_id_array = new int[object_id.length][];
        for (int i = 0; i < object_id.length; i++) {
            this.object_id_array[i] = new int[object_id[i].length];
            System.arraycopy(object_id[i], 0, this.object_id_array[i], 0, object_id[i].length);
        }
        // this.object_id_array = object_id;
    }

    public void getInternalUsers() {
        this.internal_user_only = true;
    }

    public void setInternalUser(final int userid) {
        this.userid = userid;
    }

    public void setSearchHabit(final String habit) {
        this.search_habit = habit;
    }

    public void getAllChangedSince(final long chs) {
        this.changed_since = chs;
    }

    public void getAllCreatedSince(final long crs) {
        this.created_since = crs;
    }

    public void getAllSince(final long bs) {
        this.both_since = bs;
    }

    public String buildContactSelectString(final int cols[]) {
        final StringBuilder sb = new StringBuilder();
        for (int a = 0; a < cols.length; a++) {
            final mapper m = Contacts.mapping[cols[a]];
            if (m == null) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("UNKNOWN FIELD -> " + cols[a]);
                }
            } else {
                sb.append("co.").append(m.getDBFieldName()).append(',');
            }
        }
        final int len = sb.length();
        return len > 0 ? sb.toString().substring(0, len - 1) : sb.toString();
    }

    public String getRangeSearch(final String field, final String a, final String b, final String sh) {
        final StringBuilder sb = new StringBuilder(32);

        String von = "*";
        String bis = "*";

        if (a != null && a.length() > 0 && !a.equals("*")) {
            von = a;
        }
        if (b != null && b.length() > 0 && !b.equals("*")) {
            bis = b;
        }
        if (!"*".equals(von)) {
            sb.append("co.").append(field).append(" >= ? ").append(sh).append(' ');
            injectors.add(new StringSQLInjector(von));
        }
        if (!"*".equals(bis)) {
            sb.append("co.").append(field).append(" <= ? ").append(sh).append(' ');
            injectors.add(new StringSQLInjector(bis));
        }
        return sb.toString();
    }

    public String buildAllFolderSearchString(final int user, final int[] group, final Session so) throws OXException, SearchIteratorException {
        final UserConfiguration config = UserConfigurationStorage.getInstance().getUserConfiguration(user, ctx);
        final SearchIterator<FolderObject> si = OXFolderIteratorSQL.getAllVisibleFoldersIteratorOfModule(
            user,
            group,
            config.getAccessibleModules(),
            FolderObject.CONTACT,
            ctx);
        final List<Integer> tmp = new ArrayList<Integer>();
        while (si.hasNext()) {
            tmp.add(I(si.next().getObjectID()));
        }
        final int[] folders = new int[tmp.size()];
        for (int i = 0; i < tmp.size(); i++) {
            folders[i] = tmp.get(i).intValue();
        }
        return buildFolderSearch(user, group, folders, so);
    }

    public String buildFolderSearch(final int user, final int[] group, final int[] folders, final Session so) throws OXException {
        final UserConfiguration config = UserConfigurationStorage.getInstance().getUserConfiguration(user, ctx);
        final StringBuilder read_all = new StringBuilder();
        final StringBuilder read_own = new StringBuilder();
        final OXFolderAccess ofa = new OXFolderAccess(ctx);
        for (final int folder : folders) {
            final EffectivePermission oclp = ofa.getFolderPermission(folder, user, config);
            if (oclp.canReadAllObjects()) {
                read_all.append(folder).append(',');
            } else if (oclp.canReadOwnObjects()) {
                read_own.append(folder).append(',');
            }
        }
        if (read_all.length() > 0) {
            read_all.deleteCharAt(read_all.length() - 1);
        }
        if (read_own.length() > 0) {
            read_own.deleteCharAt(read_own.length() - 1);
        }
        final StringBuilder result = new StringBuilder();
        result.append('(');
        if (read_all.length() > 0) {
            result.append("(co.fid IN (");
            result.append(read_all);
            result.append("))");
        }
        if (read_all.length() > 0 && read_own.length() > 0) {
            result.append(" OR ");
        }
        if (read_own.length() > 0) {
            result.append("(co.fid IN (");
            result.append(read_own);
            result.append(") AND co.created_from=");
            result.append(user);
            result.append(')');
        }
        if (read_all.length() == 0 && read_own.length() == 0) {
            result.append("false");
        }
        result.append(')');
        return result.toString();
    }
    
    /*************************************************************************/

    private static String rightsSelectString = "SELECT co.intfield01,co.intfield02,co.intfield03,co.intfield04,co.fid,co.created_from,co.pflag,co.cid FROM prg_contacts AS co ";

    public String iFgetRightsSelectString() {
        return rightsSelectString;
    }

    public String iFgetFolderSelectString(final int fid, final int cid) {
        return new StringBuilder(rightsSelectString).append(" where fid = ").append(fid).append(" AND cid = ").append(cid).toString();
    }

    public String iFgetNumberOfContactsString() {
        return "SELECT COUNT(co.intfield01) FROM prg_contacts AS co ";
    }

    public String iFgetRightsSelectString(final int uid, final int cid) {
        return new StringBuilder(rightsSelectString).append(" where created_from = ").append(uid).append(" AND cid = ").append(cid).toString();
    }

    public String iFcontainsForeignObjectInFolder(final int fid, final int uid, final int cid) {
        return new StringBuilder(" SELECT intfield01 FROM prg_contacts where fid = ").append(fid).append(" AND cid = ").append(cid).append(
            " AND created_from != ").append(uid).append(" AND ((pflag = 1 and created_from != ").append(uid).append(") OR (pflag is null))").toString();
    }

    public String iFdeleteDistributionListEntriesByIds(final int cid) {
        return new StringBuilder("DELETE FROM prg_dlist where intfield01 = ? AND intfield02 IS NULL AND intfield03 IS NULL AND cid = ").append(
            cid).toString();
    }

    public String iFfillDistributionListArray(final int id, final int cid) {
        return new StringBuilder(
            "Select intfield01, intfield02, intfield03, intfield04, field01, field02, field03, field04 from prg_dlist where intfield01 = ").append(
            id).append(" AND cid = ").append(cid).toString();
    }

    public String iFwriteDistributionListArrayInsert() {
        return "INSERT INTO prg_dlist (intfield01, intfield02, intfield03, field01, field02, field03, field04, cid, intfield04) VALUES (?,?,?,?,?,?,?,?,?)";
    }

    public String iFupdateDistributionListEntriesByIds() {
        return "UPDATE prg_dlist set intfield01 = ?, intfield02 = ?, intfield03 = ?, intfield04 = ?, field01 = ?, field02 = ?, field03 = ?, field04 = ? WHERE (intfield01 = ?) AND (intfield02 = ?) AND (intfield03 = ?) AND (cid = ?)";
    }

    public String iFdeleteDistributionListEntriesByIds2() {
        return "DELETE FROM prg_dlist where intfield01 = ? AND intfield02 = ? AND intfield03 = ? AND  cid = ?";
    }

    public String iFgetFillLinkArrayString(final int id, final int cid) {
        return new StringBuilder("Select intfield01, intfield02, field01, field02 from prg_contacts_linkage where intfield01 = ").append(id).append(
            " AND cid = ").append(cid).toString();
    }

    public String iFwriteContactLinkArrayInsert() {
        return "INSERT INTO prg_contacts_linkage (intfield01, intfield02, field01, field02, cid) VALUES (?,?,?,?,?)";
    }

    public String iFgetdeleteLinkEntriesByIdsString() {
        return "DELETE FROM prg_contacts_linkage where intfield01 = ? AND intfield02 = ? AND cid = ?";
    }

    public String iFgetContactImageLastModified(final int id, final int cid) {
        return new StringBuilder("SELECT changing_date from prg_contacts_image WHERE intfield01 = ").append(id).append(" AND cid = ").append(
            cid).toString();
    }

    public String iFgetContactImage(final int contact_id, final int cid) {
        return new StringBuilder("SELECT image1, changing_date, mime_type  from prg_contacts_image WHERE intfield01 = ").append(contact_id).append(
            " AND cid = ").append(cid).toString();
    }

    public String iFwriteContactImage() {
        return new StringBuilder("INSERT INTO prg_contacts_image (intfield01, image1, mime_type, cid, changing_date) VALUES (?,?,?,?,").append(
            System.currentTimeMillis()).append(')').toString();
    }

    public String iFupdateContactImageString() {
        return new StringBuilder("UPDATE prg_contacts_image SET intfield01 = ?, image1 = ?, mime_type = ?, cid = ?, changing_date = ").append(
            System.currentTimeMillis()).append(" WHERE intfield01 = ? AND cid = ? ").toString();
    }

    public StringBuilder iFperformContactStorageInsert(final StringBuilder insert_fields, final StringBuilder insert_values, final int user, final long lmd, final int cid, final int id) {
        final StringBuilder insert = new StringBuilder("INSERT INTO prg_contacts (").append(insert_fields).append("created_from,").append(
            "changed_from,").append("creating_date,").append("changing_date,").append("intfield01,").append("cid ").append(") VALUES ( ").append(
            insert_values.toString()).append(user).append(',').append(user).append(',').append(lmd).append(',').append(lmd).append(',').append(
            id).append(',').append(cid).append(") ");
        return insert;
    }

    public StringBuilder iFperformContactStorageUpdate(final StringBuilder update, final long lmd, final int id, final int cid) {
        final StringBuilder updater = new StringBuilder("UPDATE prg_contacts SET ").append(update).append("changed_from = ").append(user).append(
            ',').append("changing_date =  ").append(lmd).append(" WHERE intfield01 = ").append(id).append(" AND cid = ").append(cid);
        return updater;
    }

    public StringBuilder iFgetContactById(final String fieldList) {
        final StringBuilder sb = new StringBuilder("SELECT ").append(fieldList);
        sb.append(" from prg_contacts AS co ");
        return sb;
    }

    public String iFdeleteContactObject(final int oid, final int cid) {
        return new StringBuilder("SELECT fid, created_from, changing_date, pflag from prg_contacts where intfield01 = ").append(oid).append(
            " AND cid = ").append(cid).toString();
    }

    private static final String PREFIXED_FIELDS = "co.fid,co.cid,co.created_from,co.creating_date," + "co.changed_from,co.changing_date, co.intfield01";

    public StringBuilder iFgetColsStringFromDeleteTable(final int[] cols) {
        final String fields = buildContactSelectString(cols);
        final int len = fields.length();
        final StringBuilder sb = new StringBuilder(len + 256).append("SELECT ");
        sb.append(PREFIXED_FIELDS);
        if (len > 0) {
            sb.append(',').append(fields);
        }
        sb.append(" FROM del_contacts AS co ");
        return sb;
    }

    public StringBuilder iFgetColsString(final int[] cols) {
        final String fields = buildContactSelectString(cols);
        final int len = fields.length();
        final StringBuilder sb = new StringBuilder(len + 256).append("SELECT ");
        sb.append(PREFIXED_FIELDS);
        if (len > 0) {
            sb.append(',').append(fields);
        }
        sb.append(" FROM prg_contacts AS co ");
        return sb;
    }

    public void iFdeleteContact(final int id, final int cid, final Statement del) throws SQLException {
        final StringBuilder tmp = new StringBuilder(256);
        tmp.append("DELETE FROM del_contacts WHERE cid = ").append(cid).append(" AND intfield01 = ").append(id);
        if (LOG.isDebugEnabled()) {
            LOG.debug(tmp.toString());
        }
        del.execute(tmp.toString());

        tmp.setLength(0);
        tmp.append("INSERT INTO del_contacts SELECT * FROM prg_contacts WHERE intfield01 = ").append(id).append(" AND  cid = ").append(cid);
        if (LOG.isDebugEnabled()) {
            LOG.debug(tmp.toString());
        }
        del.execute(tmp.toString());

        tmp.setLength(0);
        tmp.append("DELETE FROM prg_contacts WHERE cid = ").append(cid).append(" AND intfield01 = ").append(id);
        if (LOG.isDebugEnabled()) {
            LOG.debug(tmp.toString());
        }
        del.execute(tmp.toString());

        tmp.setLength(0);
        tmp.append("UPDATE del_contacts SET changing_date = ").append(System.currentTimeMillis()).append(" WHERE cid = ").append(cid).append(
            " AND intfield01 = ").append(id);
        if (LOG.isDebugEnabled()) {
            LOG.debug(tmp.toString());
        }
        del.execute(tmp.toString());
    }

    public void iFtrashContactsFromFolder(final boolean deleteit, final Statement del, final int oid, final int cid) throws SQLException {
        final StringBuilder tmp = new StringBuilder(256);
        if (deleteit) {
            tmp.append("DELETE FROM prg_contacts WHERE cid = ").append(cid).append(" AND intfield01 = ").append(oid);
            if (LOG.isDebugEnabled()) {
                LOG.debug(tmp.toString());
            }
            del.execute(tmp.toString());
        } else {
            tmp.append("DELETE FROM del_contacts WHERE cid = ").append(cid).append(" AND intfield01 = ").append(oid);
            if (LOG.isDebugEnabled()) {
                LOG.debug(tmp.toString());
            }
            del.execute(tmp.toString());

            tmp.setLength(0);
            tmp.append("INSERT INTO del_contacts SELECT * FROM prg_contacts WHERE intfield01 = ").append(oid).append(" AND  cid = ").append(
                cid);
            if (LOG.isDebugEnabled()) {
                LOG.debug(tmp.toString());
            }
            del.execute(tmp.toString());

            tmp.setLength(0);
            tmp.append("DELETE FROM prg_contacts WHERE cid = ").append(cid).append(" AND intfield01 = ").append(oid);
            if (LOG.isDebugEnabled()) {
                LOG.debug(tmp.toString());
            }
            del.execute(tmp.toString());
        }
    }

    public void iFbackupContact(final Statement stmt, final int cid, final int oid, final int uid) throws SQLException {
        final StringBuilder tmp = new StringBuilder(256);
        tmp.append("DELETE FROM del_contacts WHERE cid = ").append(cid).append(" AND intfield01 = ").append(oid);
        if (LOG.isDebugEnabled()) {
            LOG.debug(tmp.toString());
        }
        stmt.execute(tmp.toString());

        tmp.setLength(0);
        tmp.append("INSERT INTO del_contacts SELECT * FROM prg_contacts WHERE intfield01 = ").append(oid).append(" AND  cid = ").append(cid);
        if (LOG.isDebugEnabled()) {
            LOG.debug(tmp.toString());
        }
        stmt.execute(tmp.toString());

        tmp.setLength(0);
        tmp.append("UPDATE del_contacts SET changing_date = ").append(System.currentTimeMillis()).append(", changed_from = ").append(uid).append(
            " WHERE cid = ").append(cid).append(" AND intfield01 = ").append(oid);
        if (LOG.isDebugEnabled()) {
            LOG.debug(tmp.toString());
        }
        stmt.execute(tmp.toString());
    }

    public String iFtrashContactsFromFolderUpdateString(final int fid, final int cid) {
        return new StringBuilder("UPDATE del_contacts SET changing_date = ").append(System.currentTimeMillis()).append(" WHERE cid = ").append(
            cid).append(" AND fid = ").append(fid).toString();
    }

    public void iFtrashDistributionList(final boolean delete, final int id, final int cid, final Statement smt) throws SQLException {
        if (delete) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(new StringBuilder("DELETE from prg_dlist where intfield01 = ").append(id).append(" AND cid = ").append(cid));
            }
            smt.execute(new StringBuilder("DELETE from prg_dlist where intfield01 = ").append(id).append(" AND cid = ").append(cid).toString());
        } else {
            final StringBuilder sb = new StringBuilder(256);

            sb.append("DELETE FROM del_dlist WHERE cid = ").append(cid).append(" AND intfield01 = ").append(id);
            if (LOG.isDebugEnabled()) {
                LOG.debug(sb.toString());
            }
            smt.execute(sb.toString());

            sb.setLength(0);
            sb.append("INSERT INTO del_dlist SELECT * FROM prg_dlist WHERE intfield01 = ").append(id).append(" AND  cid = ").append(cid);
            if (LOG.isDebugEnabled()) {
                LOG.debug(sb.toString());
            }
            smt.execute(sb.toString());

            sb.setLength(0);
            sb.append("DELETE FROM prg_dlist WHERE cid = ").append(cid).append(" AND intfield01 = ").append(id);
            if (LOG.isDebugEnabled()) {
                LOG.debug(sb.toString());
            }
            smt.execute(sb.toString());
        }
    }

    public void iFtrashLinks(final boolean delete, final Statement smt, final int id, final int cid) throws SQLException {
        final StringBuilder tmp = new StringBuilder("DELETE from prg_contacts_linkage where (intfield01 = ").append(id).append(
            " OR intfield02 = ").append(id).append(") AND cid = ").append(cid);
        if (LOG.isDebugEnabled()) {
            LOG.debug(tmp.toString());
        }
        smt.execute(tmp.toString());
    }

    public void iFgiveUserContacToAdmin(final Statement smt, final int oid, final int admin_fid, final Context ct) throws SQLException {
        final StringBuilder tmp = new StringBuilder("UPDATE prg_contacts SET changed_from = ").append(ct.getMailadmin()).append(
            ", created_from = ").append(ct.getMailadmin()).append(", changing_date = ").append(System.currentTimeMillis()).append(
            ", fid = ").append(admin_fid).append(" WHERE intfield01 = ").append(oid).append(" and cid = ").append(ct.getContextId());
        if (LOG.isDebugEnabled()) {
            LOG.debug(tmp.toString());
        }
        smt.execute(tmp.toString());
    }

    public void iFtrashImage(final boolean delete, final Statement smt, final int id, final int cid) throws SQLException {
        if (delete) {
            final StringBuilder tmp = new StringBuilder("DELETE from prg_contacts_image where intfield01 = ").append(id).append(
                " AND cid = ").append(cid);
            if (LOG.isDebugEnabled()) {
                LOG.debug(tmp.toString());
            }
            smt.execute(tmp.toString());
        } else {
            final StringBuilder tmp = new StringBuilder(256);

            tmp.append("DELETE from del_contacts_image where intfield01 = ").append(id).append(" AND cid = ").append(cid);
            if (LOG.isDebugEnabled()) {
                LOG.debug(tmp.toString());
            }
            smt.execute(tmp.toString());

            tmp.setLength(0);
            tmp.append("INSERT INTO del_contacts_image SELECT * FROM prg_contacts_image WHERE intfield01 = ").append(id).append(
                " AND  cid = ").append(cid);
            if (LOG.isDebugEnabled()) {
                LOG.debug(tmp.toString());
            }
            smt.execute(tmp.toString());

            tmp.setLength(0);
            tmp.append("DELETE from prg_contacts_image where intfield01 = ").append(id).append(" AND cid = ").append(cid);
            if (LOG.isDebugEnabled()) {
                LOG.debug(tmp.toString());
            }
            smt.execute(tmp.toString());
        }
    }

    public void iFtrashAllUserContacts(final boolean delete, final Statement del, final int cid, final int oid, final int uid, final ResultSet rs, final Session so) throws SQLException {

        final StringBuilder tmp = new StringBuilder(256);

        if (delete) {
            tmp.append("DELETE from prg_dlist where intfield01 = ").append(oid).append(" AND cid = ").append(cid);
            if (LOG.isDebugEnabled()) {
                LOG.debug(tmp.toString());
            }
            del.execute(tmp.toString());

            tmp.setLength(0);
            tmp.append("DELETE from prg_contacts_linkage where (intfield01 = ").append(oid).append(" OR intfield02 = ").append(oid).append(
                ") AND cid = ").append(cid);
            if (LOG.isDebugEnabled()) {
                LOG.debug(tmp.toString());
            }
            del.execute(tmp.toString());

            tmp.setLength(0);
            tmp.append("DELETE from prg_contacts_image where intfield01 = ").append(oid).append(" AND cid = ").append(cid);
            if (LOG.isDebugEnabled()) {
                LOG.debug(tmp.toString());
            }
            del.execute(tmp.toString());

            tmp.setLength(0);
            tmp.append("DELETE from prg_contacts WHERE cid = ").append(cid).append(" AND intfield01 = ").append(oid);
            if (LOG.isDebugEnabled()) {
                LOG.debug(tmp.toString());
            }
            // FIXME quick fix. deleteRow doesn't work because del.execute
            // creates new resultset
            del.execute(tmp.toString());
            // rs.deleteRow();

        } else {
            /*
             * tmp = newStringBuilder( "INSERT INTO del_contacts_image SELECT * FROM prg_contacts_image WHERE intfield01 = " + oid +
             * " AND  cid = "+cid); LOG.debug(tmp.toString()); del.execute(tmp.toString()); tmp = new
             * StringBuilder("DELETE from prg_contacts_image where intfield01 = " +oid+" AND cid = "+cid); LOG.debug(tmp.toString());
             * del.execute(tmp.toString()); tmp = newStringBuilder( "INSERT INTO del_dlist SELECT * FROM prg_dlist WHERE intfield01 = " +
             * oid + " AND  cid = "+cid); LOG.debug(tmp.toString()); del.execute(tmp.toString()); tmp = new
             * StringBuilder("DELETE FROM prg_dlist WHERE cid = " + cid + " AND intfield01 = " + oid); LOG.debug(tmp.toString());
             * del.execute(tmp.toString()); tmp = new StringBuilder("DELETE from prg_contacts_linkage where (intfield01 = "
             * +oid+" OR intfield02 = "+oid+") AND cid = "+cid); LOG.debug(tmp.toString()); del.execute(tmp.toString()); tmp =
             * newStringBuilder( "INSERT INTO del_contacts SELECT * FROM prg_contacts WHERE intfield01 = " + oid + " AND  cid = "+cid);
             * LOG.debug(tmp.toString()); del.execute(tmp.toString()); tmp = new StringBuilder("DELETE from prg_contacts WHERE cid = "+cid
             * +" AND intfield01 = "+oid); LOG.debug(tmp.toString()); del.execute(tmp.toString()); // rs.deleteRow(); tmp = new
             * StringBuilder("UPDATE del_contacts SET changed_from = "+ so.getContext ().getMailadmin()+", created_from = "+so.getContext()
             * .getMailadmin()+", changing_date = "+System.currentTimeMillis()+ " WHERE intfield01 = "+oid); LOG.debug(tmp.toString());
             * del.execute(tmp.toString());
             */

            tmp.append("UPDATE prg_contacts SET changed_from = ").append(ctx.getMailadmin()).append(", created_from = ").append(
                ctx.getMailadmin()).append(", changing_date = ").append(System.currentTimeMillis()).append(" WHERE intfield01 = ").append(
                oid).append(" AND cid = ").append(cid);
            if (LOG.isDebugEnabled()) {
                LOG.debug(tmp.toString());
            }
            del.execute(tmp.toString());

        }
    }

    public void iFtrashAllUserContactsDeletedEntries(final Statement del, final int cid, final int uid, final Context ct) throws SQLException {
        final StringBuilder tmp = new StringBuilder("UPDATE del_contacts SET changed_from = ").append(ctx.getMailadmin()).append(
            ", created_from = ").append(ctx.getMailadmin()).append(", changing_date = ").append(System.currentTimeMillis()).append(
            " WHERE created_from = ").append(uid).append(" and cid = ").append(cid);
        if (LOG.isDebugEnabled()) {
            LOG.debug(tmp.toString());
        }
        del.execute(tmp.toString());
    }

    public void iFtrashAllUserContactsDeletedEntriesFromAdmin(final Statement del, final int cid, final int uid) throws SQLException {
        final StringBuilder tmp = new StringBuilder("DELETE FROM del_contacts WHERE created_from = ").append(uid).append(" and cid = ").append(
            cid);
        if (LOG.isDebugEnabled()) {
            LOG.debug(tmp.toString());
        }
        del.execute(tmp.toString());
    }

    public void iFtrashTheAdmin(final Statement del, final int cid, final int uid) throws SQLException {
        final StringBuilder tmp = new StringBuilder("DELETE FROM del_contacts WHERE intfield01 = ").append(uid).append(" and cid = ").append(
            cid);
        if (LOG.isDebugEnabled()) {
            LOG.debug(tmp.toString());
        }
        del.execute(tmp.toString());
    }

    private static interface SQLInjector {

        /**
         * Injects this injector's value into given prepared statement
         * 
         * @param ps The prepared statement
         * @param parameterIndex The parameter index; the first parameter is 1, the second is 2, ...
         * @throws SQLException If a database access error occurs
         */
        public void inject(PreparedStatement ps, int parameterIndex) throws SQLException;
    }

    private static final class IntSQLInjector implements SQLInjector {

        private final int value;

        public IntSQLInjector(final int value) {
            super();
            this.value = value;
        }

        public void inject(final PreparedStatement ps, final int parameterIndex) throws SQLException {
            ps.setInt(parameterIndex, value);
        }

    }

    private static final class StringSQLInjector implements SQLInjector {

        private final String value;

        public StringSQLInjector(final String value) {
            super();
            this.value = value;
        }

        public StringSQLInjector(final String... values) {
            super();
            final StringBuilder builder = new StringBuilder(values.length << 3);
            for (int i = 0; i < values.length; i++) {
                builder.append(values[i]);
            }
            this.value = builder.toString();
        }

        public void inject(final PreparedStatement ps, final int parameterIndex) throws SQLException {
            ps.setString(parameterIndex, value);
        }

    }

    private static final class TimestampSQLInjector implements SQLInjector {

        private final java.sql.Timestamp value;

        public TimestampSQLInjector(final Date value) {
            super();
            this.value = new java.sql.Timestamp(value.getTime());
        }

        public void inject(final PreparedStatement ps, final int parameterIndex) throws SQLException {
            ps.setTimestamp(parameterIndex, value);
        }

    }

    /**
     * Checks if specified {@link StringBuilder string builder} ends with given suffix
     * 
     * @param stringBuilder The string builder to check
     * @param suffix The suffix
     * @param ignoreTrailingWhitespaces <code>true</code> to ignore trailing whitespace characters following after suffix location;
     *            otherwise <code>false</code>
     * @return The suffix' index position if the character sequence represented by the argument is a suffix of the character sequence
     *         represented by specified string builder; <code>-1</code> otherwise.
     */
    private static int endsWith(final StringBuilder stringBuilder, final String suffix, final boolean ignoreTrailingWhitespaces) {
        final int pos = stringBuilder.lastIndexOf(suffix);
        if (pos == -1) {
            return -1;
        }
        final int len = stringBuilder.length();
        if (ignoreTrailingWhitespaces) {
            for (int i = pos + suffix.length(); i < len; i++) {
                if (!Character.isWhitespace(stringBuilder.charAt(i))) {
                    return -1;
                }
            }
            return pos;
        }
        return (pos + suffix.length() == len ? pos : -1);
    }
}
