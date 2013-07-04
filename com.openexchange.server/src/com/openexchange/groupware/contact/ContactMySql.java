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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.logging.Log;
import com.openexchange.java.Strings;
import com.openexchange.log.LogFactory;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.Contacts.Mapper;
import com.openexchange.groupware.contact.sqlinjectors.IntSQLInjector;
import com.openexchange.groupware.contact.sqlinjectors.SQLInjector;
import com.openexchange.groupware.contact.sqlinjectors.StringSQLInjector;
import com.openexchange.groupware.contact.sqlinjectors.TimestampSQLInjector;
import com.openexchange.groupware.container.CommonObject;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.DataObject;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.search.ContactSearchObject;
import com.openexchange.groupware.tools.iterator.FolderObjectIterator;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.groupware.userconfiguration.UserConfigurationStorage;
import com.openexchange.java.Strings;
import com.openexchange.log.LogFactory;
import com.openexchange.server.impl.EffectivePermission;
import com.openexchange.session.Session;
import com.openexchange.tools.StringCollection;
import com.openexchange.tools.iterator.SearchIteratorException;
import com.openexchange.tools.oxfolder.OXFolderAccess;
import com.openexchange.tools.oxfolder.OXFolderIteratorSQL;

/**
 * {@link ContactMySql} - The MySQL implementation of {@link ContactSql}.
 * <p>
 * This implementation is <b>not</b> designed for multi-threaded access and therefore is not thread-safe.
 *
 * @author <a href="mailto:ben.pahne@open-xchange.com">Benjamin Frederic Pahne</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class ContactMySql implements ContactSql {

    private static final String STR_PERCENT = "%";

    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(ContactMySql.class));

    private static final boolean DEBUG = LOG.isDebugEnabled();

    private String select =
        "SELECT co.intfield01" + ",co.cid," + "co.timestampfield01," + "co.field03," + "co.field04," + "co.field06," + "co.field07," + "co.field09," + "co.field10," + "co.intfield03," + "co.field79 FROM prg_contacts AS co ";

    private String[] where;

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

    private int[] userIds;

    private boolean internal_user_only;

    private int objectID;

    private final List<SQLInjector> injectors = new ArrayList<SQLInjector>();

    private Context ctx;

    /**
     * Initializes a new {@link ContactMySql}
     *
     * @param so The session
     * @throws OXException If context cannot be resolved
     */
    public ContactMySql(final Session so) throws OXException {
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

    @Override
    public String getOrder() {
        return order;
    }

    @Override
    public PreparedStatement getSqlStatement(final Connection con) throws SQLException {
        final StringBuilder sb = new StringBuilder(256);

        String select = getSelect();
        String orderBy = getOrder();
        if (null != orderBy) {
            int pos = select.indexOf(" FROM");
            if (pos < 0 && (pos = select.indexOf(" from")) < 0) {
                throw new SQLException("SELECT statement does not contain \"FROM\".");
            }
            final String[] orderFields = parseFieldsFromOrderBy(orderBy);
            if (0 < orderFields.length) {
                sb.append(select.substring(0, pos));
                for (final String orderField : orderFields) {
                    if (-1 == select.indexOf(orderField)) {
                        sb.append(',').append(orderField);
                    }
                }
                sb.append(" FROM").append(select.substring(pos + 5));
                select = sb.toString();
                sb.setLength(0);
                orderBy = prepareOrderBy(orderBy);
            }
        }

        final String[] whereClauses = getWhere();
        {
            sb.append(select);
            if (all_folders != null && all_folders.length() > 1) {
                sb.append(all_folders);
            }
            sb.append(whereClauses[0]);
        }
        for (int i = 1; i < whereClauses.length; i++) {
            sb.append(" UNION ");
            sb.append(select);
            if (all_folders != null && all_folders.length() > 1) {
                sb.append(all_folders);
            }
            sb.append(whereClauses[i]);
        }
        sb.append(orderBy);

        final PreparedStatement ps = con.prepareStatement(sb.toString());
        final int size = injectors.size();
        for (int i = 0; i < size; i++) {
            injectors.get(i).inject(ps, i + 1);
        }
        injectors.clear();
        if (DEBUG) {
            final String sql = ps.toString();
            LOG.debug(new StringBuilder().append("\nContactSQL Query: ").append(sql.substring(sql.indexOf(": ") + 2)).toString());
        }

        return ps;
    }

    private static final Pattern PAT_ORDER_BY =
        Pattern.compile(
            "( *ORDER +BY +)(?:([a-zA-Z0-9_.]+)(?: *ASC *| *DESC *)?)(?:, *[a-zA-Z0-9_.]+(?: *ASC *| *DESC *)?)?",
            Pattern.CASE_INSENSITIVE);

    private static final Pattern PAT_FIELD = Pattern.compile("(?:([a-zA-Z0-9_.]+)(?: *ASC| *DESC)?)", Pattern.CASE_INSENSITIVE);

    /**
     * Parses denoted fields out of specified <code>ORDER BY</code> statement; <code>" ORDER BY co.field01 DESC "</code>
     *
     * @param orderBy The <code>ORDER BY</code> statement
     * @return The parsed fields
     */
    private static String[] parseFieldsFromOrderBy(final String orderBy) {
        // ORDER BY oc.field01 DESC
        Matcher m = PAT_ORDER_BY.matcher(orderBy);
        if (m.matches()) {
            m = PAT_FIELD.matcher(orderBy.substring(m.end(1)));
            final List<String> l = new ArrayList<String>(2);
            while (m.find()) {
                l.add(m.group(1));
            }
            return l.toArray(new String[l.size()]);
        }
        return new String[0];
    }

    private static final Pattern PAT_PREP = Pattern.compile("[a-zA-Z0-9_]+\\.([a-zA-Z0-9_])");

    /**
     * Prepares given <code>ORDER BY</code> statement to be used within a <code>UNION</code> statement.
     *
     * <pre>
     * ORDER BY co.field01 DESC -&gt; ORDER BY field01 DESC
     * </pre>
     *
     * @param orderBy The <code>ORDER BY</code> statement
     * @return The prepared <code>ORDER BY</code> statement
     */
    private static final String prepareOrderBy(final String orderBy) {
        return PAT_PREP.matcher(orderBy).replaceAll("$1");
    }

    protected String[] getWhere() {

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
        if (userIds != null) {
            sb.append(" (co.userid IN (");
            for (final int userId : userIds) {
                sb.append(userId).append(',');
            }
            sb.setCharAt(sb.length() - 1, ')');
            sb.append(") AND ");
        }

        // range search in time for field changed_from
        if (changed_since > 0) {
            sb.append(" (co.changing_date > ").append(changed_since).append(") AND ");
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
            /*
             * With search object
             */

            if (cso.isEmailAutoComplete() || cso.isOrSearch()) {
                search_habit = " OR ";
            }

            sb.append(" ( ");

            if (" OR ".equals(search_habit)) {
                /*
                 * Create a UNION statement
                 */
                final List<String> whereClauses = new ArrayList<String>(16);

                final String preWhere = sb.toString();
                final int preWhereLen = preWhere.length();

                for (final SearchFiller searchFiller : SEARCH_FILLERS) {
                    sb.setLength(0);
                    sb.append(preWhere);
                    final int field = searchFiller.fillSearchCriteria(this, sb, false);
                    final int sbLen = sb.length();
                    if ((sbLen - preWhereLen) > 1) {
                        // Filler appended something to string builder
                        appendixWithCSO(sb, field, true);
                        whereClauses.add(sb.toString());
                    }
                }

                if (whereClauses.isEmpty()) {
                    appendixWithCSO(sb, -1, false);
                    where = new String[] { sb.toString() };
                } else {
                    where = whereClauses.toArray(new String[whereClauses.size()]);
                }
            } else {
                /*
                 * Create a single statement with conditions linked with "AND"
                 */
                for (final SearchFiller searchFiller : SEARCH_FILLERS) {
                    searchFiller.fillSearchCriteria(this, sb, true);
                }
                appendixWithCSO(sb, -1, false);

                where = new String[] { sb.toString() };
            }
        } else {
            /*
             * No search object
             */

            appendix(sb);
            where = new String[] { sb.toString() };
        }
        return where;
    }

    private static final int[] AUTOCOMPLETE_FIELDS = { Contact.EMAIL1, Contact.EMAIL2, Contact.EMAIL3 };

    /**
     * Appends appendix to string builder with contact search object proeprly set.
     *
     * @param sb The string builder
     * @param field The affected field
     * @param union <code>true</code> if a UNION statement is generated
     */
    private void appendixWithCSO(final StringBuilder sb, final int field, final boolean union) {
        if (cso.getIgnoreOwn() > 0) {
            sb.append("( co.intfield01 != ").append(cso.getIgnoreOwn()).append(") ").append(search_habit).append(' ');
        }
        {
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
        }
        final String allFolderSQLINString = cso.getAllFolderSQLINString();
        if (null != allFolderSQLINString) {
            folder = -1;
            sb.append(allFolderSQLINString);
            sb.append(" AND ");
        }
        if (union) {
            // Special condition for email auto complete
            if (cso.isEmailAutoComplete() && Arrays.binarySearch(AUTOCOMPLETE_FIELDS, field) >= 0) {
                sb.append('(');
                sb.append(Contacts.mapping[field].getDBFieldName());
                sb.append(" is not null OR ");
                sb.append(Contacts.mapping[Contact.MARK_AS_DISTRIBUTIONLIST].getDBFieldName());
                sb.append(" > 0) AND ");
            }
        } else {
            // Special condition for email auto complete
            if (cso.isEmailAutoComplete()) {
                sb.append('(');
                sb.append(Contacts.mapping[Contact.EMAIL1].getDBFieldName());
                sb.append(" is not null OR ");
                sb.append(Contacts.mapping[Contact.EMAIL2].getDBFieldName());
                sb.append(" is not null OR ");
                sb.append(Contacts.mapping[Contact.EMAIL3].getDBFieldName());
                sb.append(" is not null OR ");
                sb.append(Contacts.mapping[Contact.MARK_AS_DISTRIBUTIONLIST].getDBFieldName());
                sb.append(" > 0) AND ");
            }
        }
        // Append appendix
        appendix(sb);
    }

    /**
     * Appends appendix to string builder
     *
     * @param sb The string builder
     */
    private void appendix(final StringBuilder sb) {
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
        sb.append(" AND ((co.pflag = 1 and co.created_from = ").append(user).append(") OR (co.pflag is null))");
    }

    @Override
    public String getSelect() {
        return this.select;
    }

    @Override
    public void setSelect(final String select) {
        this.select = select;
    }

    @Override
    public void setOrder(final String order) {
        this.order = order;
    }

    @Override
    public void setFolder(final int folder) {
        this.folder = folder;
    }

    @Override
    public void setObjectID(final int objectID) {
        this.objectID = objectID;
    }

    @Override
    public void setReadOnlyOwnFolder(final int onlyown) {
        this.can_read_only_own = onlyown;
    }

    @Override
    public void setContactSearchObject(final ContactSearchObject cso) {
        this.cso = cso;
    }

    @Override
    public void setObjectArray(final int[][] object_id) {
        this.object_id_array = new int[object_id.length][];
        for (int i = 0; i < object_id.length; i++) {
            this.object_id_array[i] = new int[object_id[i].length];
            System.arraycopy(object_id[i], 0, this.object_id_array[i], 0, object_id[i].length);
        }
        // this.object_id_array = object_id;
    }

    @Override
    public void getInternalUsers() {
        this.internal_user_only = true;
    }

    @Override
    public void setInternalUser(final int userid) {
        this.userid = userid;
    }

    @Override
    public void setInternalUsers(final int[] userIds) {
        this.where = null;
        this.userIds = userIds;
    }

    @Override
    public void setSearchHabit(final String habit) {
        this.search_habit = habit;
    }

    @Override
    public void getAllChangedSince(final long chs) {
        this.changed_since = chs;
    }

    @Override
    public void getAllCreatedSince(final long crs) {
        this.created_since = crs;
    }

    @Override
    public void getAllSince(final long bs) {
        this.both_since = bs;
    }

    @Override
    public String buildContactSelectString(final int cols[]) {
        final StringBuilder sb = new StringBuilder();
        for (int a = 0; a < cols.length; a++) {
            final Mapper m = Contacts.mapping[cols[a]];
            if (m == null) {
                if (DEBUG) {
                    LOG.debug("UNKNOWN FIELD -> " + cols[a]);
                }
            } else {
                sb.append("co.").append(m.getDBFieldName()).append(',');
            }
        }
        final int len = sb.length();
        return len > 0 ? sb.toString().substring(0, len - 1) : sb.toString();
    }

    @Override
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

    @Override
    public String buildAllFolderSearchString(final int user, final int[] group, final Session so) throws OXException, SearchIteratorException {
        final UserConfiguration config = UserConfigurationStorage.getInstance().getUserConfiguration(user, ctx);
        final List<FolderObject> list =
            ((FolderObjectIterator) OXFolderIteratorSQL.getAllVisibleFoldersIteratorOfModule(
                user,
                group,
                config.getAccessibleModules(),
                FolderObject.CONTACT,
                ctx)).asList();
        final int size = list.size();
        final int[] folders = new int[size];
        for (int i = 0; i < size; i++) {
            folders[i] = list.get(i).getObjectID();
        }
        return buildFolderSearch(user, group, folders, so);
    }

    @Override
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

    private static String rightsSelectString =
        "SELECT co.intfield01,co.intfield02,co.intfield03,co.intfield04,co.fid,co.created_from,co.pflag,co.cid FROM prg_contacts AS co ";

    @Override
    public String iFgetRightsSelectString() {
        return rightsSelectString;
    }

    @Override
    public String iFgetFolderSelectString(final int fid, final int cid) {
        return new StringBuilder(rightsSelectString).append(" where fid = ").append(fid).append(" AND cid = ").append(cid).toString();
    }

    @Override
    public String iFgetNumberOfContactsString() {
        return "SELECT COUNT(co.intfield01) FROM prg_contacts AS co ";
    }

    @Override
    public String iFgetRightsSelectString(final int uid, final int cid) {
        return new StringBuilder(rightsSelectString).append(" where created_from = ").append(uid).append(" AND cid = ").append(cid).toString();
    }

    @Override
    public String iFcontainsForeignObjectInFolder(final int fid, final int uid, final int cid) {
        return new StringBuilder(" SELECT intfield01 FROM prg_contacts where fid = ").append(fid).append(" AND cid = ").append(cid).append(
            " AND created_from != ").append(uid).append(" AND ((pflag = 1 and created_from != ").append(uid).append(") OR (pflag is null))").toString();
    }

    @Override
    public String iFdeleteDistributionListEntriesByIds(final int cid) {
        return new StringBuilder("DELETE FROM prg_dlist where intfield01 = ? AND intfield02 IS NULL AND intfield03 IS NULL AND cid = ").append(
            cid).toString();
    }

    @Override
    public String iFfillDistributionListArray(final int id, final int cid) {
        return new StringBuilder(
            "Select intfield01, intfield02, intfield03, intfield04, field01, field02, field03, field04 from prg_dlist where intfield01 = ").append(
            id).append(" AND cid = ").append(cid).toString();
    }

    @Override
    public String iFwriteDistributionListArrayInsert() {
        return "INSERT INTO prg_dlist (intfield01, intfield02, intfield03, field01, field02, field03, field04, cid, intfield04, uuid) VALUES (?,?,?,?,?,?,?,?,?,?)";
    }

    @Override
    public String iFupdateDistributionListEntriesByIds() {
        return "UPDATE prg_dlist set intfield01 = ?, intfield02 = ?, intfield03 = ?, intfield04 = ?, field01 = ?, field02 = ?, field03 = ?, field04 = ? WHERE (intfield01 = ?) AND (intfield02 = ?) AND (intfield03 = ?) AND (cid = ?)";
    }

    @Override
    public String iFdeleteDistributionListEntriesByIds2() {
        return "DELETE FROM prg_dlist where intfield01 = ? AND intfield02 = ? AND intfield03 = ? AND  cid = ?";
    }

    @Override
    public String iFgetFillLinkArrayString(final int id, final int cid) {
        return new StringBuilder("Select intfield01, intfield02, field01, field02 from prg_contacts_linkage where intfield01 = ").append(id).append(
            " AND cid = ").append(cid).toString();
    }

    @Override
    public String iFwriteContactLinkArrayInsert() {
        return "INSERT INTO prg_contacts_linkage (intfield01, intfield02, field01, field02, cid, uuid) VALUES (?,?,?,?,?,?)";
    }

    @Override
    public String iFgetdeleteLinkEntriesByIdsString() {
        return "DELETE FROM prg_contacts_linkage where intfield01 = ? AND intfield02 = ? AND cid = ?";
    }

    @Override
    public String iFgetContactImageLastModified(final int id, final int cid) {
        return new StringBuilder("SELECT changing_date from prg_contacts_image WHERE intfield01 = ").append(id).append(" AND cid = ").append(
            cid).toString();
    }

    @Override
    public String iFgetContactImageContentType(final int id, final int cid) {
        return new StringBuilder("SELECT mime_type from prg_contacts_image WHERE intfield01 = ").append(id).append(" AND cid = ").append(
            cid).toString();
    }

    @Override
    public String iFgetContactImage(final int contact_id, final int cid) {
        return new StringBuilder("SELECT image1, changing_date, mime_type  from prg_contacts_image WHERE intfield01 = ").append(contact_id).append(
            " AND cid = ").append(cid).toString();
    }

    @Override
    public String iFwriteContactImage() {
        return new StringBuilder("INSERT INTO prg_contacts_image (intfield01, image1, mime_type, cid, changing_date) VALUES (?,?,?,?,?)").toString();
    }

    @Override
    public String iFupdateContactImageString() {
        return new StringBuilder("UPDATE prg_contacts_image SET intfield01 = ?, image1 = ?, mime_type = ?, cid = ?, changing_date = ? WHERE intfield01 = ? AND cid = ? ").toString();
    }

    @Override
    public StringBuilder iFperformContactStorageInsert(final StringBuilder insert_fields, final StringBuilder insert_values, final int user, final long lmd, final int cid, final int id) {
        final StringBuilder insert =
            new StringBuilder("INSERT INTO prg_contacts (").append(insert_fields).append("created_from,").append("changed_from,").append(
                "creating_date,").append("changing_date,").append("intfield01,").append("cid ").append(") VALUES ( ").append(
                insert_values.toString()).append(user).append(',').append(user).append(',').append(lmd).append(',').append(lmd).append(',').append(
                id).append(',').append(cid).append(") ");
        return insert;
    }

    @Override
    public StringBuilder iFperformOverridingContactStorageInsert(final StringBuilder insert_fields, final StringBuilder insert_values, final int user, final long lmd, final int cid, final int id) {
        final StringBuilder insert =
            new StringBuilder("INSERT IGNORE INTO prg_contacts (").append(insert_fields).append("created_from,").append("changed_from,").append(
                "creating_date,").append("changing_date,").append("intfield01,").append("cid ").append(") VALUES ( ").append(
                insert_values.toString()).append(user).append(',').append(user).append(',').append(lmd).append(',').append(lmd).append(',').append(
                id).append(',').append(cid).append(") ");
        return insert;
    }

    @Override
    public StringBuilder iFperformContactStorageUpdate(final StringBuilder update, final long lmd, final int id, final int cid) {
        final StringBuilder updater =
            new StringBuilder("UPDATE prg_contacts SET ").append(update).append("changed_from = ").append(user).append(',').append(
                "changing_date =  ").append(lmd).append(" WHERE intfield01 = ").append(id).append(" AND cid = ").append(cid);
        return updater;
    }

    @Override
    public StringBuilder iFgetContactById(final String fieldList) {
        final StringBuilder sb = new StringBuilder("SELECT ").append(fieldList);
        sb.append(" from prg_contacts AS co ");
        return sb;
    }

    @Override
    public String iFdeleteContactObject(final int oid, final int cid) {
        return new StringBuilder("SELECT fid, created_from, changing_date, pflag from prg_contacts where intfield01 = ").append(oid).append(
            " AND cid = ").append(cid).toString();
    }

    public static final String PREFIXED_FIELDS = "co.fid,co.cid,co.created_from,co.creating_date,co.changed_from,co.changing_date,co.intfield01";

    @Override
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

    @Override
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

    @Override
    public void iFdeleteContact(final int id, final int cid, final Statement del) throws SQLException {
        final StringBuilder tmp = new StringBuilder(256);
        tmp.append("DELETE FROM del_contacts WHERE cid = ").append(cid).append(" AND intfield01 = ").append(id);
        if (DEBUG) {
            LOG.debug(tmp.toString());
        }
        del.execute(tmp.toString());

        tmp.setLength(0);
        tmp.append("INSERT INTO del_contacts SELECT * FROM prg_contacts WHERE intfield01 = ").append(id).append(" AND  cid = ").append(cid);
        if (DEBUG) {
            LOG.debug(tmp.toString());
        }
        del.execute(tmp.toString());

        tmp.setLength(0);
        tmp.append("DELETE FROM prg_contacts WHERE cid = ").append(cid).append(" AND intfield01 = ").append(id);
        if (DEBUG) {
            LOG.debug(tmp.toString());
        }
        del.execute(tmp.toString());

        tmp.setLength(0);
        tmp.append("DELETE FROM prg_dlist WHERE cid = ").append(cid).append("  AND intfield03 IS NOT NULL AND intfield03 <> 0 AND intfield02 IS NOT NULL AND intfield02 = ").append(id);
        if (DEBUG) {
            LOG.debug(tmp.toString());
        }
        del.execute(tmp.toString());

        tmp.setLength(0);
        tmp.append("DELETE FROM del_dlist WHERE cid = ").append(cid).append("  AND intfield03 IS NOT NULL AND intfield03 <> 0 AND intfield02 IS NOT NULL AND intfield02 = ").append(id);
        if (DEBUG) {
            LOG.debug(tmp.toString());
        }
        del.execute(tmp.toString());

        tmp.setLength(0);
        tmp.append("UPDATE del_contacts SET changing_date = ").append(System.currentTimeMillis()).append(" WHERE cid = ").append(cid).append(
            " AND intfield01 = ").append(id);
        if (DEBUG) {
            LOG.debug(tmp.toString());
        }
        del.execute(tmp.toString());
    }

    @Override
    public void iFtrashContactsFromFolder(final boolean deleteit, final Statement del, final int oid, final int cid) throws SQLException {
        final StringBuilder tmp = new StringBuilder(256);
        if (deleteit) {
            tmp.append("DELETE FROM prg_contacts WHERE cid = ").append(cid).append(" AND intfield01 = ").append(oid);
            if (DEBUG) {
                LOG.debug(tmp.toString());
            }
            del.execute(tmp.toString());
        } else {
            tmp.append("DELETE FROM del_contacts WHERE cid = ").append(cid).append(" AND intfield01 = ").append(oid);
            if (DEBUG) {
                LOG.debug(tmp.toString());
            }
            del.execute(tmp.toString());

            tmp.setLength(0);
            tmp.append("INSERT INTO del_contacts SELECT * FROM prg_contacts WHERE intfield01 = ").append(oid).append(" AND  cid = ").append(
                cid);
            if (DEBUG) {
                LOG.debug(tmp.toString());
            }
            del.execute(tmp.toString());

            tmp.setLength(0);
            tmp.append("DELETE FROM prg_contacts WHERE cid = ").append(cid).append(" AND intfield01 = ").append(oid);
            if (DEBUG) {
                LOG.debug(tmp.toString());
            }
            del.execute(tmp.toString());
        }
        tmp.setLength(0);
        tmp.append("DELETE FROM prg_dlist WHERE cid = ").append(cid).append("  AND intfield03 IS NOT NULL AND intfield03 <> 0 AND intfield02 IS NOT NULL AND intfield02 = ").append(oid);
        if (DEBUG) {
            LOG.debug(tmp.toString());
        }
        del.execute(tmp.toString());

        tmp.setLength(0);
        tmp.append("DELETE FROM del_dlist WHERE cid = ").append(cid).append("  AND intfield03 IS NOT NULL AND intfield03 <> 0 AND intfield02 IS NOT NULL AND intfield02 = ").append(oid);
        if (DEBUG) {
            LOG.debug(tmp.toString());
        }
        del.execute(tmp.toString());
    }

    @Override
    public void iFbackupContact(final Statement stmt, final int cid, final int oid, final int uid) throws SQLException {
        final StringBuilder tmp = new StringBuilder(256);
        tmp.append("DELETE FROM del_contacts WHERE cid = ").append(cid).append(" AND intfield01 = ").append(oid);
        if (DEBUG) {
            LOG.debug(tmp.toString());
        }
        stmt.execute(tmp.toString());

        tmp.setLength(0);
        tmp.append("INSERT INTO del_contacts SELECT * FROM prg_contacts WHERE intfield01 = ").append(oid).append(" AND  cid = ").append(cid);
        if (DEBUG) {
            LOG.debug(tmp.toString());
        }
        stmt.execute(tmp.toString());

        tmp.setLength(0);
        tmp.append("UPDATE del_contacts SET changing_date = ").append(System.currentTimeMillis()).append(", changed_from = ").append(uid).append(
            " WHERE cid = ").append(cid).append(" AND intfield01 = ").append(oid);
        if (DEBUG) {
            LOG.debug(tmp.toString());
        }
        stmt.execute(tmp.toString());
    }

    @Override
    public String iFtrashContactsFromFolderUpdateString(final int fid, final int cid) {
        return new StringBuilder("UPDATE del_contacts SET changing_date = ").append(System.currentTimeMillis()).append(" WHERE cid = ").append(
            cid).append(" AND fid = ").append(fid).toString();
    }

    @Override
    public void iFtrashDistributionList(final boolean delete, final int id, final int cid, final Statement smt) throws SQLException {
        if (delete) {
            if (DEBUG) {
                LOG.debug(new StringBuilder("DELETE from prg_dlist where intfield01 = ").append(id).append(" AND cid = ").append(cid));
            }
            smt.execute(new StringBuilder("DELETE from prg_dlist where intfield01 = ").append(id).append(" AND cid = ").append(cid).toString());
        } else {
            final StringBuilder sb = new StringBuilder(256);

            sb.append("DELETE FROM del_dlist WHERE cid = ").append(cid).append(" AND intfield01 = ").append(id);
            if (DEBUG) {
                LOG.debug(sb.toString());
            }
            smt.execute(sb.toString());

            sb.setLength(0);
            sb.append("INSERT INTO del_dlist SELECT * FROM prg_dlist WHERE intfield01 = ").append(id).append(" AND  cid = ").append(cid);
            if (DEBUG) {
                LOG.debug(sb.toString());
            }
            smt.execute(sb.toString());

            sb.setLength(0);
            sb.append("DELETE FROM prg_dlist WHERE cid = ").append(cid).append(" AND intfield01 = ").append(id);
            if (DEBUG) {
                LOG.debug(sb.toString());
            }
            smt.execute(sb.toString());
        }
    }

    @Override
    public void iFtrashLinks(final boolean delete, final Statement smt, final int id, final int cid) throws SQLException {
        final StringBuilder tmp =
            new StringBuilder("DELETE from prg_contacts_linkage where (intfield01 = ").append(id).append(" OR intfield02 = ").append(id).append(
                ") AND cid = ").append(cid);
        if (DEBUG) {
            LOG.debug(tmp.toString());
        }
        smt.execute(tmp.toString());
    }

    @Override
    public void iFgiveUserContacToAdmin(final Statement smt, final int oid, final int admin_fid, final Context ct) throws SQLException {
        final StringBuilder tmp =
            new StringBuilder("UPDATE prg_contacts SET changed_from = ").append(ct.getMailadmin()).append(", created_from = ").append(
                ct.getMailadmin()).append(", changing_date = ").append(System.currentTimeMillis()).append(", fid = ").append(admin_fid).append(
                " WHERE intfield01 = ").append(oid).append(" and cid = ").append(ct.getContextId());
        if (DEBUG) {
            LOG.debug(tmp.toString());
        }
        smt.execute(tmp.toString());
    }

    @Override
    public void iFtrashImage(final boolean delete, final Statement smt, final int id, final int cid) throws SQLException {
        if (delete) {
            final StringBuilder tmp =
                new StringBuilder("DELETE from prg_contacts_image where intfield01 = ").append(id).append(" AND cid = ").append(cid);
            if (DEBUG) {
                LOG.debug(tmp.toString());
            }
            smt.execute(tmp.toString());
        } else {
            final StringBuilder tmp = new StringBuilder(256);

            tmp.append("DELETE from del_contacts_image where intfield01 = ").append(id).append(" AND cid = ").append(cid);
            if (DEBUG) {
                LOG.debug(tmp.toString());
            }
            smt.execute(tmp.toString());

            tmp.setLength(0);
            tmp.append("INSERT INTO del_contacts_image SELECT * FROM prg_contacts_image WHERE intfield01 = ").append(id).append(
                " AND  cid = ").append(cid);
            if (DEBUG) {
                LOG.debug(tmp.toString());
            }
            smt.execute(tmp.toString());

            tmp.setLength(0);
            tmp.append("DELETE from prg_contacts_image where intfield01 = ").append(id).append(" AND cid = ").append(cid);
            if (DEBUG) {
                LOG.debug(tmp.toString());
            }
            smt.execute(tmp.toString());
        }
    }

    @Override
    public void iFtrashAllUserContacts(final boolean delete, final Statement del, final int cid, final int oid, final int uid, final ResultSet rs, final Session so) throws SQLException {

        final StringBuilder tmp = new StringBuilder(256);

        if (delete) {
            tmp.append("DELETE from prg_dlist where intfield01 = ").append(oid).append(" AND cid = ").append(cid);
            if (DEBUG) {
                LOG.debug(tmp.toString());
            }
            del.execute(tmp.toString());

            tmp.setLength(0);
            tmp.append("DELETE from prg_contacts_linkage where (intfield01 = ").append(oid).append(" OR intfield02 = ").append(oid).append(
                ") AND cid = ").append(cid);
            if (DEBUG) {
                LOG.debug(tmp.toString());
            }
            del.execute(tmp.toString());

            tmp.setLength(0);
            tmp.append("DELETE from prg_contacts_image where intfield01 = ").append(oid).append(" AND cid = ").append(cid);
            if (DEBUG) {
                LOG.debug(tmp.toString());
            }
            del.execute(tmp.toString());

            tmp.setLength(0);
            tmp.append("DELETE from prg_contacts WHERE cid = ").append(cid).append(" AND intfield01 = ").append(oid);
            if (DEBUG) {
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
            if (DEBUG) {
                LOG.debug(tmp.toString());
            }
            del.execute(tmp.toString());

        }
    }

    @Override
    public void iFtrashAllUserContactsDeletedEntries(final Statement del, final int cid, final int uid, final Context ct) throws SQLException {
        final StringBuilder tmp =
            new StringBuilder("UPDATE del_contacts SET changed_from = ").append(ctx.getMailadmin()).append(", created_from = ").append(
                ctx.getMailadmin()).append(", changing_date = ").append(System.currentTimeMillis()).append(" WHERE created_from = ").append(
                uid).append(" and cid = ").append(cid);
        if (DEBUG) {
            LOG.debug(tmp.toString());
        }
        del.execute(tmp.toString());
    }

    @Override
    public void iFtrashAllUserContactsDeletedEntriesFromAdmin(final Statement del, final int cid, final int uid) throws SQLException {
        final StringBuilder tmp =
            new StringBuilder("DELETE FROM del_contacts WHERE created_from = ").append(uid).append(" and cid = ").append(cid);
        if (DEBUG) {
            LOG.debug(tmp.toString());
        }
        del.execute(tmp.toString());
    }

    @Override
    public void iFtrashTheAdmin(final Statement del, final int cid, final int uid) throws SQLException {
        final StringBuilder tmp =
            new StringBuilder("DELETE FROM del_contacts WHERE intfield01 = ").append(uid).append(" and cid = ").append(cid);
        if (DEBUG) {
            LOG.debug(tmp.toString());
        }
        del.execute(tmp.toString());
    }




    private static interface SearchFiller {

        public int fillSearchCriteria(ContactMySql instance, StringBuilder sb, boolean isSingleSelect);

    }

    private static final List<SearchFiller> SEARCH_FILLERS;

    static {
        final List<SearchFiller> searchFillers = new ArrayList<SearchFiller>(32);

        /*********************** * search all fields * ***********************/

        searchFillers.add(new SearchFiller() {

            @Override
            public int fillSearchCriteria(final ContactMySql instance, final StringBuilder sb, final boolean isSingleSelect) {
                final ContactSearchObject cso = instance.cso;
                if (cso.getPattern() != null && cso.getPattern().length() > 0) {
                    if (cso.isStartLetter()) {
                        final String field = ContactConfig.getInstance().getString(ContactConfig.Property.LETTER_FIELD);
                        final String p = cso.getPattern().trim();

                        final String dot = ".";
                        if (dot.equals(p) || "#".equals(p)) {
                            sb.append(" (");
                            sb.append(field);
                            sb.append(" < '0%' OR ");
                            sb.append(field);
                            sb.append(" > 'z%') AND ");
                            sb.append(field);
                            sb.append(" NOT LIKE 'z%' AND ");
                        } else if (p.matches("\\d")) {
                            sb.append(' ');
                            sb.append(field);
                            sb.append(" > '0%' AND ");
                            sb.append(field);
                            sb.append(" < 'a%' AND ");
                        } else if (!dot.equals(p) && !"all".equals(p)) {
                            final String fallbackField = Contacts.mapping[Contact.DISPLAY_NAME].getDBFieldName();
                            sb.append(' ');

                            sb.append('(').append(field).append(" IS NOT ? AND ").append(field).append(" LIKE ?)");
                            sb.append(" OR (").append(field).append(" IS ? AND ").append(fallbackField).append(" LIKE ?)");
                            sb.append(" AND ");

                            /*-
                             *
                            sb.append(field);
                            sb.append(" LIKE ? AND ");
                             */

                            final List<SQLInjector> injectors = instance.injectors;
                            injectors.add(new StringSQLInjector());
                            injectors.add(new StringSQLInjector(p, STR_PERCENT));
                            injectors.add(new StringSQLInjector());
                            injectors.add(new StringSQLInjector(p, STR_PERCENT));
                        }
                    } else {
                        cso.setDisplayName(cso.getPattern());
                    }
                }
                return -1;
            }
        });

        /*********************** * search dynamic fields * ***********************/

        searchFillers.add(new SearchFiller() {

            @Override
            public int fillSearchCriteria(final ContactMySql instance, final StringBuilder sb, final boolean isSingleSelect) {
                final ContactSearchObject cso = instance.cso;
                if (cso.getDynamicSearchField() != null && cso.getDynamicSearchField().length > 0) {
                    final String searchHabit = instance.search_habit;
                    final List<SQLInjector> injectors = instance.injectors;
                    final int[] fields = cso.getDynamicSearchField();
                    final String[] values = cso.getDynamicSearchFieldValue();

                    boolean modified = false;

                    for (int i = 0; i < fields.length; i++) {
                        if ((fields[i] == Contact.ANNIVERSARY) || (fields[i] == Contact.BIRTHDAY)) {
                            String field = "";
                            if (fields[i] == Contact.ANNIVERSARY) {
                                field = Contacts.mapping[Contact.ANNIVERSARY].getDBFieldName();
                            } else if (fields[i] == Contact.BIRTHDAY) {
                                field = Contacts.mapping[Contact.BIRTHDAY].getDBFieldName();
                            }
                            /*
                             * TODO: BIRTHDAY: `timestampfield01` date default NULL, ANNIVERSARY: `timestampfield02` date default NULL,
                             */
                            final String value = values[i];
                            sb.append(" ( co.").append(field).append(" LIKE ").append(value).append(") ").append(searchHabit).append(' ');
                            modified = true;
                        } else if (fields[i] == Contact.NUMBER_OF_DISTRIBUTIONLIST || fields[i] == CommonObject.NUMBER_OF_LINKS) {
                            String field = "";
                            if (fields[i] == Contact.NUMBER_OF_DISTRIBUTIONLIST) {
                                field = Contacts.mapping[Contact.NUMBER_OF_DISTRIBUTIONLIST].getDBFieldName();
                            } else if (fields[i] == CommonObject.NUMBER_OF_LINKS) {
                                field = Contacts.mapping[CommonObject.NUMBER_OF_LINKS].getDBFieldName();
                            }
                            final String value = values[i];
                            sb.append('(').append("co.").append(field).append(" = ").append(value).append(") ").append(searchHabit).append(
                                ' ');
                            modified = true;
                        } else {
                            if (fields[i] == CommonObject.CATEGORIES) {
                                final String field = Contacts.mapping[CommonObject.CATEGORIES].getDBFieldName();
                                String value = values[i];

                                if (!"*".equals(value) && null != value) {
                                    value = StringCollection.prepareForSearch(value, false);

                                    if (value.indexOf(',') == -1) {
                                        // No comma-separated value
                                        sb.append('(').append("co.").append(field).append(" LIKE ?) ").append(searchHabit).append(' ');
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

                                        sb.append(") ").append(searchHabit).append(' ');
                                        modified = true;
                                    }
                                }
                            } else {
                                final String field = Contacts.mapping[fields[i]].getDBFieldName();
                                String value = values[i];

                                if (!"*".equals(value)) {
                                    value = StringCollection.prepareForSearch(value);

                                    sb.append("( co.").append(field).append(" LIKE ? ) ").append(searchHabit).append(' ');
                                    injectors.add(new StringSQLInjector(value));
                                    modified = true;
                                }
                            }
                        }
                    }
                    if (modified && !isSingleSelect) {
                        final int pos2 = endsWith(sb, searchHabit, true);
                        if (pos2 != -1) {
                            sb.delete(pos2, sb.length());
                        }
                    }
                }
                return -1;
            }
        });

        /*********************** * search ranges * ***********************/

        searchFillers.add(new SearchFiller() {

            @Override
            public int fillSearchCriteria(final ContactMySql instance, final StringBuilder sb, final boolean isSingleSelect) {
                final ContactSearchObject cso = instance.cso;
                if (cso.getAnniversaryRange() != null && cso.getAnniversaryRange().length > 0) {
                    final String searchHabit = instance.search_habit;
                    final List<SQLInjector> injectors = instance.injectors;
                    final Date[] d = cso.getAnniversaryRange();
                    try {
                        final String field = Contacts.mapping[Contact.ANNIVERSARY].getDBFieldName();
                        sb.append("co.").append(field).append(" >= ? ").append(searchHabit).append(' ');
                        sb.append("co.").append(field).append(" <= ? ");
                        if (isSingleSelect) {
                            sb.append(searchHabit).append(' ');
                        }
                        injectors.add(new TimestampSQLInjector(d[0]));
                        injectors.add(new TimestampSQLInjector(d[1]));
                        /*
                         * final String a = new FormatDate(language.toLowerCase(), language.toUpperCase()) .formatDateForPostgres(d[0],
                         * false); final String b = new FormatDate(language.toLowerCase(), language.toUpperCase())
                         * .formatDateForPostgres(d[0], false);
                         * sb.append(getRangeSearch(Contacts.mapping[ContactObject.ANNIVERSARY].getDBFieldName(), a, b, search_habit));
                         */
                    } catch (final Exception e) {
                        LOG.error("Could not Format Anniversary Date for Range Search! ", e);
                    }
                }
                return Contact.ANNIVERSARY;
            }
        });
        searchFillers.add(new SearchFiller() {

            @Override
            public int fillSearchCriteria(final ContactMySql instance, final StringBuilder sb, final boolean isSingleSelect) {
                final ContactSearchObject cso = instance.cso;
                if (cso.getBirthdayRange() != null && cso.getBirthdayRange().length > 0) {
                    final String searchHabit = instance.search_habit;
                    final List<SQLInjector> injectors = instance.injectors;
                    final Date[] d = cso.getBirthdayRange();
                    try {
                        final String field = Contacts.mapping[Contact.BIRTHDAY].getDBFieldName();
                        sb.append("co.").append(field).append(" >= ? ").append(searchHabit).append(' ');
                        sb.append("co.").append(field).append(" <= ? ");
                        if (isSingleSelect) {
                            sb.append(searchHabit).append(' ');
                        }
                        injectors.add(new TimestampSQLInjector(d[0]));
                        injectors.add(new TimestampSQLInjector(d[1]));
                        /*
                         * final String a = new FormatDate(language.toLowerCase(), language.toUpperCase()) .formatDateForPostgres(d[0],
                         * false); final String b = new FormatDate(language.toLowerCase(), language.toUpperCase())
                         * .formatDateForPostgres(d[0], false);
                         * sb.append(getRangeSearch(Contacts.mapping[ContactObject.BIRTHDAY].getDBFieldName(), a, b, search_habit));
                         */
                    } catch (final Exception e) {
                        LOG.error("Could not Format Birthday Date for Range Search! ", e);
                    }
                }
                return Contact.BIRTHDAY;
            }
        });
        searchFillers.add(new SearchFiller() {

            @Override
            public int fillSearchCriteria(final ContactMySql instance, final StringBuilder sb, final boolean isSingleSelect) {
                final ContactSearchObject cso = instance.cso;
                if (cso.getBusinessPostalCodeRange() != null && cso.getBusinessPostalCodeRange().length > 0) {
                    final String searchHabit = instance.search_habit;
                    final String[] x = cso.getBusinessPostalCodeRange();
                    sb.append(instance.getRangeSearch(
                        Contacts.mapping[Contact.POSTAL_CODE_BUSINESS].getDBFieldName(),
                        x[0],
                        x[1],
                        searchHabit));
                    if (!isSingleSelect) {
                        final int pos2 = endsWith(sb, searchHabit, true);
                        if (pos2 != -1) {
                            sb.delete(pos2, sb.length());
                        }
                    }
                }
                return Contact.POSTAL_CODE_BUSINESS;
            }
        });
        searchFillers.add(new SearchFiller() {

            @Override
            public int fillSearchCriteria(final ContactMySql instance, final StringBuilder sb, final boolean isSingleSelect) {
                final ContactSearchObject cso = instance.cso;
                if (cso.getCreationDateRange() != null && cso.getCreationDateRange().length > 0) {
                    final String searchHabit = instance.search_habit;
                    final List<SQLInjector> injectors = instance.injectors;
                    final Date[] d = cso.getCreationDateRange();
                    try {
                        final String field = Contacts.mapping[DataObject.CREATION_DATE].getDBFieldName();
                        sb.append("co.").append(field).append(" >= ? ").append(searchHabit).append(' ');
                        sb.append("co.").append(field).append(" <= ? ");
                        if (isSingleSelect) {
                            sb.append(searchHabit).append(' ');
                        }
                        injectors.add(new TimestampSQLInjector(d[0]));
                        injectors.add(new TimestampSQLInjector(d[1]));
                        /*
                         * final String a = new FormatDate(language.toLowerCase(), language.toUpperCase()) .formatDateForPostgres(d[0],
                         * false); final String b = new FormatDate(language.toLowerCase(), language.toUpperCase())
                         * .formatDateForPostgres(d[0], false);
                         * sb.append(getRangeSearch(Contacts.mapping[ContactObject.CREATION_DATE].getDBFieldName(), a, b, search_habit));
                         */
                    } catch (final Exception e) {
                        LOG.error("Could not Format Creating_Date Date for Range Search! ", e);
                    }
                }
                return DataObject.CREATION_DATE;
            }
        });
        searchFillers.add(new SearchFiller() {

            @Override
            public int fillSearchCriteria(final ContactMySql instance, final StringBuilder sb, final boolean isSingleSelect) {
                final ContactSearchObject cso = instance.cso;
                if (cso.getLastModifiedRange() != null && cso.getLastModifiedRange().length > 0) {
                    final String searchHabit = instance.search_habit;
                    final List<SQLInjector> injectors = instance.injectors;
                    final Date[] d = cso.getLastModifiedRange();
                    try {
                        final String field = Contacts.mapping[DataObject.LAST_MODIFIED].getDBFieldName();
                        sb.append("co.").append(field).append(" >= ? ").append(searchHabit).append(' ');
                        sb.append("co.").append(field).append(" <= ? ");
                        if (isSingleSelect) {
                            sb.append(searchHabit).append(' ');
                        }
                        injectors.add(new TimestampSQLInjector(d[0]));
                        injectors.add(new TimestampSQLInjector(d[1]));
                        /*
                         * final String a = new FormatDate(language.toLowerCase(), language.toUpperCase()) .formatDateForPostgres(d[0],
                         * false); final String b = new FormatDate(language.toLowerCase(), language.toUpperCase())
                         * .formatDateForPostgres(d[0], false);
                         * sb.append(getRangeSearch(Contacts.mapping[ContactObject.LAST_MODIFIED].getDBFieldName(), a, b, search_habit));
                         */
                    } catch (final Exception e) {
                        LOG.error("Could not Format LastModified Date for Range Search! ", e);
                    }
                }
                return DataObject.LAST_MODIFIED;
            }
        });
        searchFillers.add(new SearchFiller() {

            @Override
            public int fillSearchCriteria(final ContactMySql instance, final StringBuilder sb, final boolean isSingleSelect) {
                final ContactSearchObject cso = instance.cso;
                if (cso.getNumberOfEmployeesRange() != null && cso.getNumberOfEmployeesRange().length > 0) {
                    final String searchHabit = instance.search_habit;
                    final String[] x = cso.getNumberOfEmployeesRange();
                    sb.append(instance.getRangeSearch(
                        Contacts.mapping[Contact.NUMBER_OF_EMPLOYEE].getDBFieldName(),
                        x[0],
                        x[1],
                        searchHabit));
                    if (!isSingleSelect) {
                        final int pos2 = endsWith(sb, searchHabit, true);
                        if (pos2 != -1) {
                            sb.delete(pos2, sb.length());
                        }
                    }
                }
                return Contact.NUMBER_OF_EMPLOYEE;
            }
        });
        searchFillers.add(new SearchFiller() {

            @Override
            public int fillSearchCriteria(final ContactMySql instance, final StringBuilder sb, final boolean isSingleSelect) {
                final ContactSearchObject cso = instance.cso;
                if (cso.getOtherPostalCodeRange() != null && cso.getOtherPostalCodeRange().length > 0) {
                    final String searchHabit = instance.search_habit;
                    final String[] x = cso.getOtherPostalCodeRange();
                    sb.append(instance.getRangeSearch(Contacts.mapping[Contact.POSTAL_CODE_OTHER].getDBFieldName(), x[0], x[1], searchHabit));
                    if (!isSingleSelect) {
                        final int pos2 = endsWith(sb, searchHabit, true);
                        if (pos2 != -1) {
                            sb.delete(pos2, sb.length());
                        }
                    }
                }
                return Contact.POSTAL_CODE_OTHER;
            }
        });
        searchFillers.add(new SearchFiller() {

            @Override
            public int fillSearchCriteria(final ContactMySql instance, final StringBuilder sb, final boolean isSingleSelect) {
                final ContactSearchObject cso = instance.cso;
                if (cso.getPrivatePostalCodeRange() != null && cso.getPrivatePostalCodeRange().length > 0) {
                    final String searchHabit = instance.search_habit;
                    final String[] x = cso.getPrivatePostalCodeRange();
                    sb.append(instance.getRangeSearch(Contacts.mapping[Contact.POSTAL_CODE_HOME].getDBFieldName(), x[0], x[1], searchHabit));
                    if (!isSingleSelect) {
                        final int pos2 = endsWith(sb, searchHabit, true);
                        if (pos2 != -1) {
                            sb.delete(pos2, sb.length());
                        }
                    }
                }
                return Contact.POSTAL_CODE_HOME;
            }
        });
        searchFillers.add(new SearchFiller() {

            @Override
            public int fillSearchCriteria(final ContactMySql instance, final StringBuilder sb, final boolean isSingleSelect) {
                final ContactSearchObject cso = instance.cso;
                if (cso.getSalesVolumeRange() != null && cso.getSalesVolumeRange().length > 0) {
                    final String[] x = cso.getSalesVolumeRange();
                    final String searchHabit = instance.search_habit;
                    sb.append(instance.getRangeSearch(Contacts.mapping[Contact.SALES_VOLUME].getDBFieldName(), x[0], x[1], searchHabit));
                    if (!isSingleSelect) {
                        final int pos2 = endsWith(sb, searchHabit, true);
                        if (pos2 != -1) {
                            sb.delete(pos2, sb.length());
                        }
                    }
                }
                return Contact.SALES_VOLUME;
            }

        });

        /*********************** * search single field * ***********************/

        searchFillers.add(new SearchFiller() {

            @Override
            public int fillSearchCriteria(final ContactMySql instance, final StringBuilder sb, final boolean isSingleSelect) {
                final ContactSearchObject cso = instance.cso;
                if (cso.getGivenName() != null && cso.getGivenName().length() > 0) {
                    final String searchHabit = instance.search_habit;
                    final List<SQLInjector> injectors = instance.injectors;

                    final String field = Contacts.mapping[Contact.GIVEN_NAME].getDBFieldName();

                    String value = StringCollection.prepareForSearch(cso.getGivenName());

                    if (STR_PERCENT.equals(value)) {
                        sb.append(' ');
                    } else {
                        sb.append('(').append("co.").append(field).append(" LIKE ?) ");
                        if (isSingleSelect) {
                            sb.append(searchHabit).append(' ');
                            injectors.add(new StringSQLInjector(value));
                        } else {
                            /*
                             * Don't force starting '%' to let optimizer use appropriate index
                             */
                            value = StringCollection.prepareForSearch(cso.getGivenName(), false, true, true);
                            injectors.add(new StringSQLInjector(value));
                        }
                    }
                }
                return Contact.GIVEN_NAME;
            }

        });
        searchFillers.add(new SearchFiller() {

            @Override
            public int fillSearchCriteria(final ContactMySql instance, final StringBuilder sb, final boolean isSingleSelect) {
                final ContactSearchObject cso = instance.cso;
                if (cso.getYomiFirstName() != null && cso.getYomiFirstName().length() > 0) {
                    final String searchHabit = instance.search_habit;
                    final List<SQLInjector> injectors = instance.injectors;

                    final String field = Contacts.mapping[Contact.YOMI_FIRST_NAME].getDBFieldName();

                    String value = StringCollection.prepareForSearch(cso.getYomiFirstName());

                    if (STR_PERCENT.equals(value)) {
                        sb.append(' ');
                    } else {
                        sb.append('(').append("co.").append(field).append(" LIKE ?) ");
                        if (isSingleSelect) {
                            sb.append(searchHabit).append(' ');
                            injectors.add(new StringSQLInjector(value));
                        } else {
                            /*
                             * Don't force starting '%' to let optimizer use appropriate index
                             */
                            value = StringCollection.prepareForSearch(cso.getYomiFirstName(), false, true, true);
                            injectors.add(new StringSQLInjector(value));
                        }
                    }
                }
                return Contact.YOMI_FIRST_NAME;
            }

        });

        searchFillers.add(new SearchFiller() {

            @Override
            public int fillSearchCriteria(final ContactMySql instance, final StringBuilder sb, final boolean isSingleSelect) {
                final ContactSearchObject cso = instance.cso;
                if (cso.getSurname() != null && cso.getSurname().length() > 0) {
                    final String searchHabit = instance.search_habit;
                    final List<SQLInjector> injectors = instance.injectors;

                    final String field = Contacts.mapping[Contact.SUR_NAME].getDBFieldName();

                    String value = StringCollection.prepareForSearch(cso.getSurname());

                    if (STR_PERCENT.equals(value)) {
                        sb.append(' ');
                    } else {
                        sb.append('(').append("co.").append(field).append(" LIKE ?) ");
                        if (isSingleSelect) {
                            sb.append(searchHabit).append(' ');
                            injectors.add(new StringSQLInjector(value));
                        } else {
                            /*
                             * Don't force starting '%' to let optimizer use appropriate index
                             */
                            value = StringCollection.prepareForSearch(cso.getSurname(), false, true, true);
                            injectors.add(new StringSQLInjector(value));
                        }
                    }
                }
                return Contact.SUR_NAME;
            }

        });
        searchFillers.add(new SearchFiller() {

            @Override
            public int fillSearchCriteria(final ContactMySql instance, final StringBuilder sb, final boolean isSingleSelect) {
                final ContactSearchObject cso = instance.cso;
                if (cso.getYomiLastName() != null && cso.getYomiLastName().length() > 0) {
                    final String searchHabit = instance.search_habit;
                    final List<SQLInjector> injectors = instance.injectors;

                    final String field = Contacts.mapping[Contact.YOMI_LAST_NAME].getDBFieldName();

                    String value = StringCollection.prepareForSearch(cso.getYomiLastName());

                    if (STR_PERCENT.equals(value)) {
                        sb.append(' ');
                    } else {
                        sb.append('(').append("co.").append(field).append(" LIKE ?) ");
                        if (isSingleSelect) {
                            sb.append(searchHabit).append(' ');
                            injectors.add(new StringSQLInjector(value));
                        } else {
                            /*
                             * Don't force starting '%' to let optimizer use appropriate index
                             */
                            value = StringCollection.prepareForSearch(cso.getYomiLastName(), false, true, true);
                            injectors.add(new StringSQLInjector(value));
                        }
                    }
                }
                return Contact.YOMI_LAST_NAME;
            }

        });

        searchFillers.add(new SearchFiller() {

            @Override
            public int fillSearchCriteria(final ContactMySql instance, final StringBuilder sb, final boolean isSingleSelect) {
                final ContactSearchObject cso = instance.cso;
                if (cso.getDisplayName() != null && cso.getDisplayName().length() > 0) {
                    final String searchHabit = instance.search_habit;
                    final List<SQLInjector> injectors = instance.injectors;

                    final String field = Contacts.mapping[Contact.DISPLAY_NAME].getDBFieldName();

                    String value = StringCollection.prepareForSearch(cso.getDisplayName());

                    if (STR_PERCENT.equals(value)) {
                        sb.append(' ');
                    } else {
                        sb.append('(').append("co.").append(field).append(" LIKE ?) ");
                        if (isSingleSelect) {
                            sb.append(searchHabit).append(' ');
                            injectors.add(new StringSQLInjector(value));
                        } else {
                            /*
                             * Don't force starting '%' to let optimizer use appropriate index
                             */
                            value = StringCollection.prepareForSearch(cso.getDisplayName(), false, true, true);
                            injectors.add(new StringSQLInjector(value));
                        }
                    }
                }
                return Contact.DISPLAY_NAME;
            }

        });
        searchFillers.add(new SearchFiller() {

            @Override
            public int fillSearchCriteria(final ContactMySql instance, final StringBuilder sb, final boolean isSingleSelect) {
                final ContactSearchObject cso = instance.cso;
                if (cso.getEmail1() != null && cso.getEmail1().length() > 0) {
                    final String searchHabit = instance.search_habit;
                    final List<SQLInjector> injectors = instance.injectors;

                    final String field = Contacts.mapping[Contact.EMAIL1].getDBFieldName();

                    String value = StringCollection.prepareForSearch(cso.getEmail1());

                    if (STR_PERCENT.equals(value)) {
                        sb.append(' ');
                    } else {
                        sb.append('(').append("co.").append(field).append(" LIKE ?) ");
                        if (isSingleSelect) {
                            sb.append(searchHabit).append(' ');
                            injectors.add(new StringSQLInjector(value));
                        } else {
                            /*
                             * Don't force starting '%' to let optimizer use appropriate index
                             */
                            value = StringCollection.prepareForSearch(cso.getEmail1(), false, true, true);
                            injectors.add(new StringSQLInjector(value));
                        }
                    }
                }
                return Contact.EMAIL1;
            }

        });
        searchFillers.add(new SearchFiller() {

            @Override
            public int fillSearchCriteria(final ContactMySql instance, final StringBuilder sb, final boolean isSingleSelect) {
                final ContactSearchObject cso = instance.cso;
                if (cso.getEmail2() != null && cso.getEmail2().length() > 0) {
                    final String searchHabit = instance.search_habit;
                    final List<SQLInjector> injectors = instance.injectors;

                    final String field = Contacts.mapping[Contact.EMAIL2].getDBFieldName();

                    String value = StringCollection.prepareForSearch(cso.getEmail2());

                    if (STR_PERCENT.equals(value)) {
                        sb.append(' ');
                    } else {
                        sb.append('(').append("co.").append(field).append(" LIKE ?) ");
                        if (isSingleSelect) {
                            sb.append(searchHabit).append(' ');
                            injectors.add(new StringSQLInjector(value));
                        } else {
                            /*
                             * Don't force starting '%' to let optimizer use appropriate index
                             */
                            value = StringCollection.prepareForSearch(cso.getEmail2(), false, true, true);
                            injectors.add(new StringSQLInjector(value));
                        }
                    }
                }
                return Contact.EMAIL2;
            }
        });
        searchFillers.add(new SearchFiller() {

            @Override
            public int fillSearchCriteria(final ContactMySql instance, final StringBuilder sb, final boolean isSingleSelect) {
                final ContactSearchObject cso = instance.cso;
                if (cso.getEmail3() != null && cso.getEmail3().length() > 0) {
                    final String searchHabit = instance.search_habit;
                    final List<SQLInjector> injectors = instance.injectors;

                    final String field = Contacts.mapping[Contact.EMAIL3].getDBFieldName();

                    String value = StringCollection.prepareForSearch(cso.getEmail3());

                    if (STR_PERCENT.equals(value)) {
                        sb.append(' ');
                    } else {
                        sb.append('(').append("co.").append(field).append(" LIKE ?) ");
                        if (isSingleSelect) {
                            sb.append(searchHabit).append(' ');
                            injectors.add(new StringSQLInjector(value));
                        } else {
                            /*
                             * Don't force starting '%' to let optimizer use appropriate index
                             */
                            value = StringCollection.prepareForSearch(cso.getEmail3(), false, true, true);
                            injectors.add(new StringSQLInjector(value));
                        }
                    }
                }
                return Contact.EMAIL3;
            }
        });
        searchFillers.add(new SearchFiller() {

            @Override
            public int fillSearchCriteria(final ContactMySql instance, final StringBuilder sb, final boolean isSingleSelect) {
                final ContactSearchObject cso = instance.cso;
                if (cso.getCatgories() != null && cso.getCatgories().length() > 0) {
                    final String searchHabit = instance.search_habit;
                    final List<SQLInjector> injectors = instance.injectors;

                    final String field = Contacts.mapping[CommonObject.CATEGORIES].getDBFieldName();
                    String value = cso.getCatgories().trim();

                    if (!"*".equals(value)) {
                        value = StringCollection.prepareForSearch(value, false);

                        if (value.indexOf(',') == -1) {
                            // No comma-separated value
                            sb.append('(').append("co.").append(field).append(" LIKE ?) ");
                            if (isSingleSelect) {
                                sb.append(searchHabit).append(' ');
                            }
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

                            sb.append(") ");
                            if (isSingleSelect) {
                                sb.append(searchHabit).append(' ');
                            }
                        }
                    }
                }
                return CommonObject.CATEGORIES;
            }

        });
        searchFillers.add(new SearchFiller() {

            @Override
            public int fillSearchCriteria(final ContactMySql instance, final StringBuilder sb, final boolean isSingleSelect) {
                final ContactSearchObject cso = instance.cso;
                if (cso.getCompany() != null && cso.getCompany().length() > 0) {
                    final String searchHabit = instance.search_habit;
                    final List<SQLInjector> injectors = instance.injectors;

                    final String field = Contacts.mapping[Contact.COMPANY].getDBFieldName();

                    final String value = StringCollection.prepareForSearch(cso.getCompany());

                    if (STR_PERCENT.equals(value)) {
                        sb.append(' ');
                    } else {
                        sb.append("( co.").append(field).append(" LIKE ? ) ");
                        if (isSingleSelect) {
                            sb.append(searchHabit).append(' ');
                        }
                        injectors.add(new StringSQLInjector(value));
                    }
                }
                return Contact.COMPANY;
            }

        });
        searchFillers.add(new SearchFiller() {

            @Override
            public int fillSearchCriteria(final ContactMySql instance, final StringBuilder sb, final boolean isSingleSelect) {
                final ContactSearchObject cso = instance.cso;
                if (cso.getYomiCompany() != null && cso.getYomiCompany().length() > 0) {
                    final String searchHabit = instance.search_habit;
                    final List<SQLInjector> injectors = instance.injectors;

                    final String field = Contacts.mapping[Contact.YOMI_COMPANY].getDBFieldName();

                    final String value = StringCollection.prepareForSearch(cso.getYomiCompany());

                    if (STR_PERCENT.equals(value)) {
                        sb.append(' ');
                    } else {
                        sb.append("( co.").append(field).append(" LIKE ? ) ");
                        if (isSingleSelect) {
                            sb.append(searchHabit).append(' ');
                        }
                        injectors.add(new StringSQLInjector(value));
                    }
                }
                return Contact.YOMI_COMPANY;
            }

        });
        searchFillers.add(new SearchFiller() {

            @Override
            public int fillSearchCriteria(final ContactMySql instance, final StringBuilder sb, final boolean isSingleSelect) {
                final ContactSearchObject cso = instance.cso;
                if (cso.getDepartment() != null && cso.getDepartment().length() > 0) {
                    final String searchHabit = instance.search_habit;
                    final List<SQLInjector> injectors = instance.injectors;

                    final String field = Contacts.mapping[Contact.DEPARTMENT].getDBFieldName();

                    final String value = StringCollection.prepareForSearch(cso.getDepartment());

                    if (STR_PERCENT.equals(value)) {
                        sb.append(' ');
                    } else {
                        sb.append("( co.").append(field).append(" LIKE ? ) ");
                        if (isSingleSelect) {
                            sb.append(searchHabit).append(' ');
                        }
                        injectors.add(new StringSQLInjector(value));
                    }
                }
                return Contact.DEPARTMENT;
            }
        });
        searchFillers.add(new SearchFiller() {

            @Override
            public int fillSearchCriteria(final ContactMySql instance, final StringBuilder sb, final boolean isSingleSelect) {
                final ContactSearchObject cso = instance.cso;
                if (cso.getStreetBusiness() != null && cso.getStreetBusiness().length() > 0) {
                    final String searchHabit = instance.search_habit;
                    final List<SQLInjector> injectors = instance.injectors;

                    final String field = Contacts.mapping[Contact.STREET_BUSINESS].getDBFieldName();

                    final String value = StringCollection.prepareForSearch(cso.getStreetBusiness());

                    if (STR_PERCENT.equals(value)) {
                        sb.append(' ');
                    } else {
                        sb.append("( co.").append(field).append(" LIKE ? ) ");
                        if (isSingleSelect) {
                            sb.append(searchHabit).append(' ');
                        }
                        injectors.add(new StringSQLInjector(value));
                    }
                }
                return Contact.STREET_BUSINESS;
            }
        });
        searchFillers.add(new SearchFiller() {

            @Override
            public int fillSearchCriteria(final ContactMySql instance, final StringBuilder sb, final boolean isSingleSelect) {
                final ContactSearchObject cso = instance.cso;
                if (cso.getCityBusiness() != null && cso.getCityBusiness().length() > 0) {
                    final String searchHabit = instance.search_habit;
                    final List<SQLInjector> injectors = instance.injectors;

                    final String field = Contacts.mapping[Contact.CITY_BUSINESS].getDBFieldName();

                    final String value = StringCollection.prepareForSearch(cso.getCityBusiness());

                    if (STR_PERCENT.equals(value)) {
                        sb.append(' ');
                    } else {
                        sb.append("( co.").append(field).append(" LIKE ? ) ");
                        if (isSingleSelect) {
                            sb.append(searchHabit).append(' ');
                        }
                        injectors.add(new StringSQLInjector(value));
                    }
                }
                return Contact.CITY_BUSINESS;
            }
        });

        SEARCH_FILLERS = new CopyOnWriteArrayList<SearchFiller>(searchFillers);
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
    static int endsWith(final StringBuilder stringBuilder, final String suffix, final boolean ignoreTrailingWhitespaces) {
        final int pos = stringBuilder.lastIndexOf(suffix);
        if (pos == -1) {
            return -1;
        }
        final int len = stringBuilder.length();
        if (ignoreTrailingWhitespaces) {
            for (int i = pos + suffix.length(); i < len; i++) {
                if (!Strings.isWhitespace(stringBuilder.charAt(i))) {
                    return -1;
                }
            }
            return pos;
        }
        return (pos + suffix.length() == len ? pos : -1);
    }

    static void removeMultipleTrailingWhitespaces(final StringBuilder stringBuilder) {
        final int length = stringBuilder.length();
        int pos = length - 1;
        while (pos > 0 && Strings.isWhitespace(stringBuilder.charAt(pos))) {
            pos--;
        }
        if ((pos < length - 1)) {
            stringBuilder.delete(pos + 1, length);
        }
    }

}
