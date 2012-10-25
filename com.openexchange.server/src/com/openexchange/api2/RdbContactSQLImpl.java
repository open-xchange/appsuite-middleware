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

package com.openexchange.api2;

import static com.openexchange.groupware.contact.Contacts.performContactReadCheck;
import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.b;
import static com.openexchange.tools.StringCollection.prepareForSearch;
import static com.openexchange.tools.sql.DBUtils.closeSQLStuff;
import static com.openexchange.tools.sql.DBUtils.forSQLCommand;
import static com.openexchange.tools.sql.DBUtils.getStatement;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.apache.commons.logging.Log;
import com.openexchange.ajax.parser.ContactSearchtermSqlConverter;
import com.openexchange.contact.LdapServer;
import com.openexchange.database.provider.SimpleDBProvider;
import com.openexchange.event.impl.EventClient;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.attach.Attachments;
import com.openexchange.groupware.contact.ContactConfig;
import com.openexchange.groupware.contact.ContactConfig.Property;
import com.openexchange.groupware.contact.ContactExceptionCodes;
import com.openexchange.groupware.contact.ContactMySql;
import com.openexchange.groupware.contact.ContactSql;
import com.openexchange.groupware.contact.Contacts;
import com.openexchange.groupware.contact.Contacts.Mapper;
import com.openexchange.groupware.contact.OverridingContactInterface;
import com.openexchange.groupware.contact.Search;
import com.openexchange.groupware.contact.helpers.CollationContactComparator;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.contact.helpers.ContactSetter;
import com.openexchange.groupware.contact.helpers.ContactSwitcher;
import com.openexchange.groupware.contact.helpers.ContactSwitcherForTimestamp;
import com.openexchange.groupware.contact.helpers.SpecialAlphanumSortContactComparator;
import com.openexchange.groupware.contact.helpers.UseCountComparator;
import com.openexchange.groupware.contact.sqlinjectors.SQLInjector;
import com.openexchange.groupware.container.CommonObject;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.DataObject;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.search.ContactSearchObject;
import com.openexchange.groupware.search.Order;
import com.openexchange.groupware.tools.iterator.PrefetchIterator;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.groupware.userconfiguration.UserConfigurationStorage;
import com.openexchange.l10n.SuperCollator;
import com.openexchange.log.LogFactory;
import com.openexchange.preferences.ServerUserSetting;
import com.openexchange.search.SearchTerm;
import com.openexchange.server.impl.DBPool;
import com.openexchange.server.impl.EffectivePermission;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.session.Session;
import com.openexchange.tools.arrays.Arrays;
import com.openexchange.tools.iterator.ArrayIterator;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorAdapter;
import com.openexchange.tools.iterator.SearchIteratorDelegator;
import com.openexchange.tools.iterator.SearchIteratorException;
import com.openexchange.tools.oxfolder.OXFolderAccess;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.sql.DBUtils;

public class RdbContactSQLImpl implements ContactSQLInterface, OverridingContactInterface {

    private final int userId;

    private final int[] memberInGroups;

    private final Context ctx;

    private final Session session;

    private final UserConfiguration userConfiguration;

    private Locale locale;

    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(RdbContactSQLImpl.class));

    public RdbContactSQLImpl(final Session session) throws OXException {
        super();
        this.ctx = ContextStorage.getStorageContext(session);
        this.userId = session.getUserId();
        this.memberInGroups = UserStorage.getStorageUser(session.getUserId(), ctx).getGroups();
        this.session = session;
        userConfiguration = UserConfigurationStorage.getInstance().getUserConfigurationSafe(session.getUserId(), ctx);
    }

    public RdbContactSQLImpl(final Session session, final Context ctx) {
        super();
        this.ctx = ctx;
        this.userId = session.getUserId();
        this.memberInGroups = UserStorage.getStorageUser(session.getUserId(), ctx).getGroups();
        this.session = session;
        userConfiguration = UserConfigurationStorage.getInstance().getUserConfigurationSafe(session.getUserId(), ctx);
    }

    private Locale getLocale() {
        if (null == locale) {
            if (session instanceof ServerSession) {
                locale = ((ServerSession) session).getUser().getLocale();
            } else {
                locale = UserStorage.getStorageUser(session.getUserId(), ctx).getLocale();
            }
        }
        return locale;
    }

    @Override
    public void insertContactObject(final Contact co) throws OXException {
        insertContactObject(co, false);
    }

    @Override
    public void forceInsertContactObject(final Contact co) throws OXException {
        insertContactObject(co, true);
    }

    protected void insertContactObject(final Contact co, final boolean override) throws OXException {
        try {
            Contacts.performContactStorageInsert(co, userId, session, override);
            final EventClient ec = new EventClient(session);
            ec.create(co);
        } catch (final OXException e) {
            throw new OXException(e);
        }
    }

    @Override
    public void updateContactObject(final Contact co, final int fid, final java.util.Date d) throws OXException {

        try {
            final Contact storageVersion = Contacts.getContactById(co.getObjectID(), session);
            Contacts.performContactStorageUpdate(co, fid, d, userId, memberInGroups, ctx, userConfiguration);
            final EventClient ec = new EventClient(session);
            ec.modify(storageVersion, co, new OXFolderAccess(ctx).getFolderObject(co.getParentFolderID()));
        } catch (final OXException e) {
            throw e;
        }
    }

    @Override
    public void updateUserContact(final Contact contact, final java.util.Date lastModified) throws OXException {
        try {
            final Contact storageVersion = Contacts.getContactById(contact.getObjectID(), session);
            Contacts.performUserContactStorageUpdate(contact, lastModified, userId, memberInGroups, ctx, userConfiguration);
            final EventClient ec = new EventClient(session);
            ec.modify(storageVersion, contact, new OXFolderAccess(ctx).getFolderObject(contact.getParentFolderID()));
        } catch (final OXException e) {
            throw e;
        }
    }

    @Override
    public int getNumberOfContacts(final int folderId) throws OXException {
        final Connection readCon;
        try {
            readCon = DBPool.pickup(ctx);
        } catch (final OXException e) {
            throw ContactExceptionCodes.INIT_CONNECTION_FROM_DBPOOL.create(e);
        }
        try {
            final FolderObject contactFolder = new OXFolderAccess(readCon, ctx).getFolderObject(folderId);
            if (contactFolder.getModule() != FolderObject.CONTACT) {
                throw ContactExceptionCodes.NON_CONTACT_FOLDER.create(I(folderId), I(ctx.getContextId()), I(userId));
            }
            final ContactSql contactSQL = new ContactMySql(session, ctx);
            final EffectivePermission oclPerm = new OXFolderAccess(readCon, ctx).getFolderPermission(folderId, userId, userConfiguration);
            if (oclPerm.getFolderPermission() <= OCLPermission.NO_PERMISSIONS) {
                throw ContactExceptionCodes.NO_ACCESS_PERMISSION.create(I(folderId), I(ctx.getContextId()), I(userId));
            }
            if (!oclPerm.canReadAllObjects()) {
                if (oclPerm.canReadOwnObjects()) {
                    contactSQL.setReadOnlyOwnFolder(userId);
                } else {
                    throw ContactExceptionCodes.NO_ACCESS_PERMISSION.create(I(folderId), I(ctx.getContextId()), I(userId));
                }
            }
            contactSQL.setSelect(contactSQL.iFgetNumberOfContactsString());
            contactSQL.setFolder(folderId);
            int retval = 0;
            Statement stmt = null;
            ResultSet rs = null;
            try {
                stmt = contactSQL.getSqlStatement(readCon);
                rs = ((PreparedStatement) stmt).executeQuery();

                if (rs.next()) {
                    retval = rs.getInt(1);
                }
            } catch (final SQLException e) {
                throw ContactExceptionCodes.SQL_PROBLEM.create(e);
            } finally {
                closeSQLStuff(rs, stmt);
            }
            return retval;
        } catch (final OXException e) {
            throw e;
        } finally {
            DBPool.closeReaderSilent(ctx, readCon);
        }
    }

    @Override
    public SearchIterator<Contact> getContactsInFolder(final int folderId, final int from, final int to, final int order_field, final Order orderMechanism, final String collation2, final int[] cols) throws OXException {
        int[] extendedCols = checkColumns(cols);
        final ContactSql cs = new ContactMySql(session, ctx);
        cs.setFolder(folderId);

        final OXFolderAccess folderAccess = new OXFolderAccess(ctx);
        final FolderObject contactFolder = folderAccess.getFolderObject(folderId);
        if (contactFolder.getModule() != FolderObject.CONTACT) {
            throw ContactExceptionCodes.NON_CONTACT_FOLDER.create(I(folderId), I(ctx.getContextId()), I(userId));
        }
        final EffectivePermission oclPerm = folderAccess.getFolderPermission(folderId, userId, userConfiguration);
        if (oclPerm.getFolderPermission() <= OCLPermission.NO_PERMISSIONS) {
            throw ContactExceptionCodes.NO_ACCESS_PERMISSION.create(I(folderId), I(ctx.getContextId()), I(userId));
        }
        if (!oclPerm.canReadAllObjects()) {
            if (oclPerm.canReadOwnObjects()) {
                cs.setReadOnlyOwnFolder(userId);
            } else {
                throw ContactExceptionCodes.NO_ACCESS_PERMISSION.create(I(folderId), I(ctx.getContextId()), I(userId));
            }
        }

        final SuperCollator collation = SuperCollator.get(collation2);
        final StringBuilder order = new StringBuilder();
        if (order_field > 0 && order_field != Contact.USE_COUNT_GLOBAL_FIRST && order_field != Contact.SPECIAL_SORTING && !Order.NO_ORDER.equals(orderMechanism)) {
            final String orderSQL = generateOrder(order_field, orderMechanism, collation);
            if (null != orderSQL) {
                order.append(orderSQL);
            }
        } else {
            extendedCols = Arrays.addUniquely(extendedCols, new int[] {
                Contact.YOMI_LAST_NAME, Contact.SUR_NAME, Contact.YOMI_FIRST_NAME, Contact.GIVEN_NAME, Contact.DISPLAY_NAME, Contact.YOMI_COMPANY, Contact.COMPANY, Contact.EMAIL1, Contact.EMAIL2, Contact.USE_COUNT });
        }

        if (from != 0 || to != 0) {
            order.append(" LIMIT ");
            order.append(from);
            order.append(',');
            order.append(to - from);
        }
        cs.setOrder(order.toString());
        cs.setSelect(cs.iFgetColsString(extendedCols).toString());

        final Connection con = DBPool.pickup(ctx);
        final Contact[] contacts;
        ResultSet result = null;
        PreparedStatement stmt = null;
        try {
            stmt = cs.getSqlStatement(con);
            result = stmt.executeQuery();
            final List<Contact> tmp = new ArrayList<Contact>();
            while (result.next()) {
                final Contact contact = convertResultSet2ContactObject(result, extendedCols, false, con);
                tmp.add(contact);
            }
            contacts = tmp.toArray(new Contact[tmp.size()]);
        } catch (final SQLException e) {
            throw ContactExceptionCodes.SQL_PROBLEM.create(e);
        } finally {
            DBUtils.closeSQLStuff(result, stmt);
            DBPool.closeReaderSilent(ctx, con);
        }
        if (order_field == Contact.USE_COUNT_GLOBAL_FIRST) {
            java.util.Arrays.sort(contacts, new UseCountComparator(false, getLocale()));
        } else if(collation == null) {
        	java.util.Arrays.sort(contacts, new SpecialAlphanumSortContactComparator(getLocale()));
        }

        return new ArrayIterator<Contact>(contacts);
    }

	@Override
    public <T> SearchIterator<Contact> getContactsByExtendedSearch(final SearchTerm<T> searchterm, final int order_field, final Order order, final String collation, final int[] cols) throws OXException, OXException {
        final ContactSql cs = new ContactMySql(session, ctx);
        final ContactSearchtermSqlConverter conv = new ContactSearchtermSqlConverter();
        conv.setCharset(collation);
        conv.parse(searchterm);

        //generate parts of query
        int[] extendedCols = cols;
        final boolean specialSort;
        if (order_field <= 0 || order_field == Contact.SPECIAL_SORTING || order_field == Contact.USE_COUNT_GLOBAL_FIRST || Order.NO_ORDER.equals(order)) {
            extendedCols = Arrays.addUniquely(extendedCols, new int[] {
                Contact.YOMI_LAST_NAME, Contact.SUR_NAME, Contact.YOMI_FIRST_NAME, Contact.GIVEN_NAME, Contact.DISPLAY_NAME, Contact.YOMI_COMPANY, Contact.COMPANY, Contact.EMAIL1, Contact.EMAIL2, Contact.USE_COUNT });
            specialSort = true;
        } else {
            specialSort = false;
        }
        final String select = generateSelect(extendedCols);
        final String whereFolder = checkFolderRights(conv, cs);
        final String whereConditions = conv.getPreparedWhereString();
        final String sqlOrder = specialSort ? null : generateOrder(order_field, order, SuperCollator.get(collation));

        //build query
        final StringBuilder query = new StringBuilder(select);

        query.append("WHERE (co.cid=").append(ctx.getContextId()).append(") "); //never forget the context

       	if(whereConditions != null && whereConditions.length() > 0) {
            query.append(" AND ").append(whereConditions);
        }
        if(whereFolder != null && !conv.hasFolders()){ //if the filter does not check for folders...
        	query.append(" AND ").append(whereFolder).append(' '); //...search all folders you have access rights to
        }
        if (null != sqlOrder) {
        	query.append(sqlOrder).append(' ');
        }

        final String queryStr = query.toString();

        final Connection con = DBPool.pickup(ctx);

        Contact[] contacts;
        ResultSet result = null;
        PreparedStatement stmt = null;
        try {
			stmt = con.prepareStatement(queryStr);
			final List<SQLInjector> injectors = conv.getInjectors();
            for (int i = 0; i < injectors.size(); i++) {
            	injectors.get(i).inject(stmt, i+1);
            }

            result = stmt.executeQuery();
            final List<Contact> tmp = new ArrayList<Contact>();
            while (result.next()) {
                final Contact contact = convertResultSet2ContactObject(result, extendedCols, false, con);
                tmp.add(contact);
            }
            if (tmp.isEmpty()) {
                return SearchIteratorAdapter.<Contact> emptyIterator();
            }
            /*
             * Sort manually according to following conditions
             */
            if (null != collation) {
                sortByCollation(tmp, order_field, order, collation);
            } else if (order_field == Contact.USE_COUNT_GLOBAL_FIRST) {
                java.util.Collections.sort(tmp, new UseCountComparator(specialSort, getLocale()));
            } else if (specialSort) {
                java.util.Collections.sort(tmp, new SpecialAlphanumSortContactComparator(getLocale()));
            }
            contacts = tmp.toArray(new Contact[tmp.size()]);
        } catch (final SQLException e) {
            throw ContactExceptionCodes.SQL_PROBLEM.create(e);
        } finally {
            DBUtils.closeSQLStuff(result, stmt);
            DBPool.closeReaderSilent(ctx, con);
        }
        return new ArrayIterator<Contact>(contacts);
    }


	private void sortByCollation(final List<Contact> tmp, final int orderField, final Order order, final String collation) {
		if(collation == null) {
            return;
        }

		final ContactField field = ContactField.getByValue(orderField);
		final SuperCollator mapping = SuperCollator.get(collation);
		if(field == null){
			LOG.error("Sorting of requested contacts failed, because field #"+orderField+" could not be mapped to a contact field");
			return;
		}
		if(mapping == null){
			LOG.error("Sorting of requested contacts failed, because no collation could be founf for "+orderField);
			return;
		}
		final CollationContactComparator comparator = new CollationContactComparator(field, order, mapping.getJavaLocale());
		Collections.sort(tmp, comparator);
	}

	private String generateSelect(final int[] cols) {
        final StringBuilder fieldsBuilder = new StringBuilder();
        for (int a = 0; a < cols.length; a++) {
            final Mapper m = Contacts.mapping[cols[a]];
            if (m != null) {
            	fieldsBuilder.append("co.").append(m.getDBFieldName()).append(',');
            }
        }
        int len = fieldsBuilder.length();
        if(len > 0) {
            fieldsBuilder.deleteCharAt(len - 1);
        }
        final String fields = fieldsBuilder.toString();

        len = fields.length();
        final StringBuilder sb = new StringBuilder(len + 256).append("SELECT ");
        sb.append(ContactMySql.PREFIXED_FIELDS);
        if (len > 0) {
            sb.append(',').append(fields);
        }
        sb.append(" FROM prg_contacts AS co ");
        return sb.toString();
	}

	@Override
    public SearchIterator<Contact> getContactsByExtendedSearch(final ContactSearchObject searchobject, final int order_field, final Order order, final String collation, final int[] cols) throws OXException, OXException {
        int[] extendedCols = cols;
        final OXFolderAccess oxfs = new OXFolderAccess(ctx);
        final ContactSql cs = new ContactMySql(session, ctx);
        boolean considerUsersAliases = false;
        if (searchobject.isEmailAutoComplete()) {
            final boolean allFolders = b(ContactConfig.getInstance().getBoolean(Property.ALL_FOLDERS_FOR_AUTOCOMPLETE));
            if (!searchobject.hasFolders() && !allFolders) {
                searchobject.addFolder(oxfs.getDefaultFolder(userId, FolderObject.CONTACT).getObjectID());
                try {
                    final Integer contactCollectFolder = ServerUserSetting.getInstance().getContactCollectionFolder(
                        ctx.getContextId(),
                        userId);
                    if (null != contactCollectFolder && oxfs.exists(contactCollectFolder.intValue())) {
                        searchobject.addFolder(contactCollectFolder.intValue());
                    }
                } catch (final OXException e) {
                    LOG.error(e.getMessage(), e);
                }
                final EffectivePermission oclPerm = oxfs.getFolderPermission(FolderObject.SYSTEM_LDAP_FOLDER_ID, userId, userConfiguration);
                if (oclPerm.isFolderVisible() && oclPerm.canReadAllObjects()) {
                    searchobject.addFolder(FolderObject.SYSTEM_LDAP_FOLDER_ID);
                    considerUsersAliases = true;
                }
            }
        }
        if (searchobject.hasFolders()) {
            final int[] folders = searchobject.getFolders();
            final OXFolderAccess folderAccess = new OXFolderAccess(ctx);
            for (final int folderId : folders) {
                final FolderObject contactFolder = folderAccess.getFolderObject(folderId);
                if (contactFolder.getModule() != FolderObject.CONTACT) {
                    throw ContactExceptionCodes.NON_CONTACT_FOLDER.create(I(folderId), I(ctx.getContextId()), I(userId));
                }
                final EffectivePermission oclPerm = oxfs.getFolderPermission(folderId, userId, userConfiguration);
                if (oclPerm.getFolderPermission() <= OCLPermission.NO_PERMISSIONS) {
                    throw ContactExceptionCodes.NO_ACCESS_PERMISSION.create(I(folderId), I(ctx.getContextId()), I(userId));
                }
                if (!oclPerm.canReadOwnObjects()) {
                    throw ContactExceptionCodes.NO_ACCESS_PERMISSION.create(I(folderId), I(ctx.getContextId()), I(userId));
                }
            }
            searchobject.setAllFolderSQLINString(cs.buildFolderSearch(userId, memberInGroups, folders, session));
        } else {
            try {
                searchobject.setAllFolderSQLINString(cs.buildAllFolderSearchString(userId, memberInGroups, session).toString());
            } catch (final SearchIteratorException e) {
                throw new OXException(e);
            }
        }
        Search.checkPatternLength(searchobject);
        final StringBuilder sqlOrder = new StringBuilder();
        final boolean specialSort;
        if (order_field > 0 && order_field != Contact.SPECIAL_SORTING && order_field != Contact.USE_COUNT_GLOBAL_FIRST && !Order.NO_ORDER.equals(order)) {
            specialSort = false;
            sqlOrder.append(" ORDER BY co.");
            final int realOrderField = order_field == Contact.USE_COUNT_GLOBAL_FIRST ? Contact.USE_COUNT : order_field;
            sqlOrder.append(Contacts.mapping[realOrderField].getDBFieldName());
            sqlOrder.append(' ');
            final String realOrderMechanism = order_field == Contact.USE_COUNT_GLOBAL_FIRST ? "DESC" : forSQLCommand(order);
            if (realOrderMechanism != null && realOrderMechanism.length() > 0) {
                sqlOrder.append(realOrderMechanism);
            } else {
                sqlOrder.append("ASC");
            }
            sqlOrder.append(' ');
        } else {
            extendedCols = Arrays.addUniquely(extendedCols, new int[] {
                Contact.YOMI_LAST_NAME, Contact.SUR_NAME, Contact.YOMI_FIRST_NAME, Contact.GIVEN_NAME, Contact.DISPLAY_NAME, Contact.YOMI_COMPANY, Contact.COMPANY, Contact.EMAIL1, Contact.EMAIL2, Contact.USE_COUNT });
            specialSort = true;
        }
        cs.setOrder(sqlOrder.toString());
        cs.setContactSearchObject(searchobject);
        cs.setSelect(cs.iFgetColsString(extendedCols).toString());
        final Connection con = DBPool.pickup(ctx);
        final List<Contact> contacts = new ArrayList<Contact>(32);
        try {
            final Set<String> foundAddresses = new HashSet<String>();
            ResultSet result = null;
            PreparedStatement stmt = null;
            try {
                stmt = cs.getSqlStatement(con);
                result = stmt.executeQuery();
                while (result.next()) {
                    final Contact contact = convertResultSet2ContactObject(result, extendedCols, false, con);
                    contacts.add(contact);
                    foundAddresses.add(contact.getEmail1());
                }
            } catch (final SQLException e) {
                throw ContactExceptionCodes.SQL_PROBLEM.create(e);
            } finally {
                DBUtils.closeSQLStuff(result, stmt);
                result = null;
                stmt = null;
            }

            if (considerUsersAliases) {
                final String email1 = searchobject.getEmail1();
                if (null != email1) {
                    try {
                        int pos;
                        final String aliasColumn = "ua.value";
                        {
                            /*
                             * Compose statement: Search for matching users' aliases but ignore own aliases
                             */
                            String select = cs.getSelect();
                            final StringBuilder sb = new StringBuilder(select.length());
                            pos = select.indexOf(" FROM");
                            if (pos < 0 && (pos = select.indexOf(" from")) < 0) {
                                throw new SQLException("SELECT statement does not contain \"FROM\".");
                            }
                            select =
                                sb.append(select.substring(0, pos)).append(',').append(aliasColumn).append(select.substring(pos)).toString();
                            sb.setLength(0);
                            sb.append(select);
                            sb.append(" JOIN user_attribute AS ua ON co.cid = ? AND ua.cid = ? AND co.userid = ua.id");
                            sb.append(" WHERE ua.name = ? AND value LIKE ? AND ua.id != ? AND co.userid IS NOT NULL");
                            stmt = con.prepareStatement(sb.toString());
                        }
                        pos = 1;
                        stmt.setInt(pos++, ctx.getContextId());
                        stmt.setInt(pos++, ctx.getContextId());
                        stmt.setString(pos++, "alias");
                        stmt.setString(pos++, prepareForSearch(email1, false, true, true));
                        stmt.setInt(pos++, userId);
                        result = stmt.executeQuery();
                        while (result.next()) {
                            /*
                             * Check if associated contact was already found by previous search
                             */
                            final String alias = result.getString(aliasColumn);
                            if (!foundAddresses.contains(alias)) {
                                final Contact contact = convertResultSet2ContactObject(result, extendedCols, false, con);
                                contact.setEmail1(alias);
                                contacts.add(contact);
                            }
                        }
                    } catch (final SQLException e) {
                        throw ContactExceptionCodes.SQL_PROBLEM.create(e, getStatement(stmt));
                    } finally {
                        DBUtils.closeSQLStuff(result, stmt);
                        result = null;
                        stmt = null;
                    }
                }
            }
        } finally {
            DBPool.closeReaderSilent(ctx, con);
        }

        if (order_field == Contact.USE_COUNT_GLOBAL_FIRST) {
            java.util.Collections.sort(contacts, new UseCountComparator(specialSort, getLocale()));
        } else if (specialSort) {
            java.util.Collections.sort(contacts, new SpecialAlphanumSortContactComparator(getLocale()));
        }
        return new SearchIteratorAdapter<Contact>(contacts.iterator(), contacts.size());
    }

    @Override
    public SearchIterator<Contact> searchContacts(final String searchpattern, final int folderId, final int order_field, final Order order, final int[] cols) throws OXException {
        boolean error = false;
        String orderDir = forSQLCommand(order);
        int orderBy = order_field;
        if (orderBy == 0) {
            orderBy = 502;
        }
        if (" ".equals(orderDir)) {
            orderDir = " ASC ";
        }
        final Connection readcon = DBPool.pickup(ctx);

        final OXFolderAccess folderAccess = new OXFolderAccess(readcon, ctx);

        try {
            final FolderObject contactFolder = folderAccess.getFolderObject(folderId);
            if (contactFolder.getModule() != FolderObject.CONTACT) {
                throw ContactExceptionCodes.NON_CONTACT_FOLDER.create(I(folderId), I(ctx.getContextId()), I(userId));
            }
        } catch (final OXException e) {
            if (readcon != null) {
                DBPool.closeReaderSilent(ctx, readcon);
            }
            throw e;
        }

        Search.checkPatternLength(searchpattern);

        SearchIterator<Contact> si = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            final ContactSql cs = new ContactMySql(session, ctx);
            cs.setFolder(folderId);
            cs.setSearchHabit(" OR ");
            final String sqlOrder = new StringBuilder(32).append(" ORDER BY co.").append(Contacts.mapping[orderBy].getDBFieldName()).append(
                ' ').append(orderDir).append(' ').toString();
            cs.setOrder(sqlOrder);

            final EffectivePermission oclPerm = folderAccess.getFolderPermission(folderId, userId, userConfiguration);
            if (oclPerm.getFolderPermission() <= OCLPermission.NO_PERMISSIONS) {
                throw ContactExceptionCodes.NO_ACCESS_PERMISSION.create(I(folderId), I(ctx.getContextId()), I(userId));
            }
            if (!oclPerm.canReadAllObjects()) {
                if (oclPerm.canReadOwnObjects()) {
                    cs.setReadOnlyOwnFolder(userId);
                } else {
                    throw ContactExceptionCodes.NO_ACCESS_PERMISSION.create(I(folderId), I(ctx.getContextId()), I(userId));
                }
            }

            final ContactSearchObject cso = new ContactSearchObject();
            cso.setDisplayName(searchpattern);
            cso.setGivenName(searchpattern);
            cso.setSurname(searchpattern);
            cso.setEmail1(searchpattern);
            cso.setEmail2(searchpattern);
            cso.setEmail3(searchpattern);
            cso.setCatgories(searchpattern);

            cs.setContactSearchObject(cso);

            cs.setSelect(cs.iFgetColsString(cols).toString());
            stmt = cs.getSqlStatement(readcon);
            rs = ((PreparedStatement) stmt).executeQuery();
            si = new ContactObjectIterator(rs, stmt, cols, false, readcon);
        } catch (final SearchIteratorException e) {
            error = true;
            throw new OXException(e);
        } catch (final SQLException e) {
            error = true;
            throw ContactExceptionCodes.SQL_PROBLEM.create(e);
        } catch (final OXException e) {
            error = true;
            throw e;
        } finally {
            if (error) {
                closeSQLStuff(rs, stmt);
                try {
                    if (readcon != null) {
                        DBPool.closeReaderSilent(ctx, readcon);
                    }
                } catch (final Exception ex) {
                    LOG.error("Unable to return Connection", ex);
                }
            }
        }

        try {
            return new PrefetchIterator<Contact>(si);
        } catch (final OXException e) {
            throw new OXException(e);
        }
    }

    @Override
    public Contact getObjectById(final int objectId, final int fid) throws OXException {
        if (objectId <= 0) {
            throw ContactExceptionCodes.CONTACT_NOT_FOUND.create(I(objectId), I(ctx.getContextId()));
        }
        final FolderObject contactFolder;
        try {
            contactFolder = new OXFolderAccess(ctx).getFolderObject(fid);
        } catch (final OXException e) {
            throw new OXException(e);
        }
        if (contactFolder.getModule() != FolderObject.CONTACT) {
            throw ContactExceptionCodes.NON_CONTACT_FOLDER.create(I(fid), I(ctx.getContextId()), I(userId));
        }
        final Connection con = DBPool.pickup(ctx);
        final Contact co;
        try {
            co = Contacts.getContactById(objectId, userId, memberInGroups, ctx, userConfiguration, con);
            if (!performContactReadCheck(contactFolder, userId, co.getCreatedBy(), userConfiguration, con)) {
                throw ContactExceptionCodes.NO_ACCESS_PERMISSION.create(I(fid), I(ctx.getContextId()), I(userId));
            }
            final Date creationDate = Attachments.getInstance(new SimpleDBProvider(con, null)).getNewestCreationDate(
                ctx,
                Types.CONTACT,
                objectId);
            if (null != creationDate) {
                co.setLastModifiedOfNewestAttachment(creationDate);
            }
        } catch (final OXException e) {
            throw new OXException(e);
        } finally {
            DBPool.closeReaderSilent(ctx, con);
        }
        return co;
    }

    @Override
    public Contact getUserById(final int userid) throws OXException {
        return getUsersById(new int[] { userid }, true)[0];
    }

    @Override
    public Contact getUserById(final int userId, final boolean performReadCheck) throws OXException {
        return getUsersById(new int[] { userId }, performReadCheck)[0];
    }

    @Override
    public Contact[] getUsersById(final int[] userIds, final boolean performReadCheck) throws OXException {
        Connection readCon = null;
        final Contact[] contacts;
        final int fid = FolderObject.SYSTEM_LDAP_FOLDER_ID;
        try {
            readCon = DBPool.pickup(ctx);
            final FolderObject contactFolder = new OXFolderAccess(readCon, ctx).getFolderObject(fid);
            if (contactFolder.getModule() != FolderObject.CONTACT) {
                throw ContactExceptionCodes.NON_CONTACT_FOLDER.create(I(fid), I(ctx.getContextId()), I(userId));
            }
            contacts = Contacts.getUsersById(userIds, userId, memberInGroups, ctx, userConfiguration, readCon);
            for (final Contact contact : contacts) {
                if (contact.getParentFolderID() != fid) {
                    throw ContactExceptionCodes.USER_OUTSIDE_GLOBAL.create(I(contact.getParentFolderID()), I(ctx.getContextId()));
                }
                if (performReadCheck && !performSecurityReadCheck(fid, contact.getCreatedBy(), userId, session, readCon, ctx)) {
                    throw ContactExceptionCodes.NO_ACCESS_PERMISSION.create(I(fid), I(ctx.getContextId()), I(userId));
                }
            }
        } finally {
            if (readCon != null) {
                DBPool.closeReaderSilent(ctx, readCon);
            }
        }
        return contacts;
    }

    public Contact getUserById(final int userid, final boolean performReadCheck, final Connection readCon) throws OXException {
        Contact co = null;
        final int fid = FolderObject.SYSTEM_LDAP_FOLDER_ID;
        if (userid > 0) {
            co = Contacts.getUserById(userid, userId, memberInGroups, ctx, userConfiguration, readCon);
        } else {
            throw ContactExceptionCodes.CONTACT_NOT_FOUND.create(I(userId), I(ctx.getContextId()));
        }

        final int folderId = co.getParentFolderID();

        final FolderObject contactFolder = new OXFolderAccess(readCon, ctx).getFolderObject(folderId);
        if (contactFolder.getModule() != FolderObject.CONTACT) {
            throw ContactExceptionCodes.NON_CONTACT_FOLDER.create(I(fid), I(ctx.getContextId()), I(userId));
        }

        if (performReadCheck && !performSecurityReadCheck(folderId, co.getCreatedBy(), userId, session, readCon, ctx)) {
            throw ContactExceptionCodes.NO_ACCESS_PERMISSION.create(I(fid), I(ctx.getContextId()), I(userId));
        }
        return co;
    }

    @Override
    public SearchIterator<Contact> getModifiedContactsInFolder(final int folderId, final int[] cols, final Date since) throws OXException {
        boolean error = false;
        Connection readCon = null;
        try {
            readCon = DBPool.pickup(ctx);
        } catch (final Exception e) {
            throw ContactExceptionCodes.INIT_CONNECTION_FROM_DBPOOL.create(e);
        }
        final OXFolderAccess folderAccess = new OXFolderAccess(readCon, ctx);
        try {
            final FolderObject contactFolder = folderAccess.getFolderObject(folderId);
            if (contactFolder.getModule() != FolderObject.CONTACT) {
                throw ContactExceptionCodes.NON_CONTACT_FOLDER.create(I(folderId), I(ctx.getContextId()), I(userId));
            }
        } catch (final OXException e) {
            if (readCon != null) {
                DBPool.closeReaderSilent(ctx, readCon);
            }
            throw new OXException(e);
        }
        SearchIterator<Contact> si = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            final ContactSql cs = new ContactMySql(session, ctx);

            final EffectivePermission oclPerm = folderAccess.getFolderPermission(folderId, userId, userConfiguration);
            if (oclPerm.getFolderPermission() <= OCLPermission.NO_PERMISSIONS) {
                throw ContactExceptionCodes.NO_ACCESS_PERMISSION.create(I(folderId), I(ctx.getContextId()), I(userId));
            }
            if (!oclPerm.canReadAllObjects()) {
                if (oclPerm.canReadOwnObjects()) {
                    cs.setReadOnlyOwnFolder(userId);
                } else {
                    throw ContactExceptionCodes.NO_ACCESS_PERMISSION.create(I(folderId), I(ctx.getContextId()), I(userId));
                }
            }

            if (folderId == FolderObject.SYSTEM_LDAP_FOLDER_ID) {
                cs.getInternalUsers();
            } else {
                cs.setFolder(folderId);
            }

            cs.getAllChangedSince(since.getTime());
            cs.setSelect(cs.iFgetColsString(cols).toString());

            stmt = cs.getSqlStatement(readCon);
            rs = ((PreparedStatement) stmt).executeQuery();

            si = new ContactObjectIterator(rs, stmt, cols, false, readCon);
        } catch (final SearchIteratorException e) {
            error = true;
            throw new OXException(e);
        } catch (final SQLException e) {
            error = true;
            throw ContactExceptionCodes.SQL_PROBLEM.create(e);
        } catch (final OXException e) {
            error = true;
            throw e;
        } finally {
            if (error) {
                closeSQLStuff(rs, stmt);
                try {
                    if (readCon != null) {
                        DBPool.closeReaderSilent(ctx, readCon);
                    }
                } catch (final Exception ex) {
                    LOG.error("Unable to return Connection", ex);
                }
            }
        }
        try {
            return new PrefetchIterator<Contact>(si);
        } catch (final OXException e) {
            throw new OXException(e);
        }
    }

    @Override
    public SearchIterator<Contact> getDeletedContactsInFolder(final int folderId, final int[] cols, final Date since) throws OXException {
        boolean error = false;
        SearchIterator<Contact> si = null;
        Connection readcon = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            readcon = DBPool.pickup(ctx);

            final ContactSql cs = new ContactMySql(session, ctx);
            cs.setFolder(folderId);

            cs.getAllChangedSince(since.getTime());
            cs.setSelect(cs.iFgetColsStringFromDeleteTable(cols).toString());
            cs.setOrder(" ORDER BY co.field02 ");

            stmt = cs.getSqlStatement(readcon);
            rs = ((PreparedStatement) stmt).executeQuery();

            si = new ContactObjectIterator(rs, stmt, cols, false, readcon);
        } catch (final SearchIteratorException e) {
            error = true;
            throw new OXException(e);
        } catch (final SQLException e) {
            error = true;
            throw ContactExceptionCodes.SQL_PROBLEM.create(e);
        } finally {
            if (error) {
                closeSQLStuff(rs, stmt);
                try {
                    if (readcon != null) {
                        DBPool.closeReaderSilent(ctx, readcon);
                    }
                } catch (final Exception ex) {
                    LOG.error("Unable to return Connection", ex);
                }
            }
        }
        try {
            return new PrefetchIterator<Contact>(si);
        } catch (final OXException e) {
            throw new OXException(e);
        }
    }

    @Override
    public void deleteContactObject(final int oid, final int fuid, final Date client_date) throws OXException {
        if (FolderObject.SYSTEM_LDAP_FOLDER_ID == fuid) {
            throw ContactExceptionCodes.NO_USER_CONTACT_DELETE.create();
        }
        Connection readcon = null;
        EffectivePermission oclPerm = null;
        int created_from = 0;
        final Contact co = new Contact();
        Statement stmt = null;
        ResultSet rs = null;
        try {
            readcon = DBPool.pickup(ctx);

            boolean pflag = false;
            Date changing_date = null;
            final ContactSql cs = new ContactMySql(session, ctx);
            stmt = readcon.createStatement();
            rs = stmt.executeQuery(cs.iFdeleteContactObject(oid, ctx.getContextId()));
            if (rs.next()) {
                created_from = rs.getInt(2);

                co.setCreatedBy(created_from);
                co.setParentFolderID(fuid);
                co.setObjectID(oid);

                final long xx = rs.getLong(3);
                changing_date = new java.util.Date(xx);
                final int pf = rs.getInt(4);
                if (!rs.wasNull() && pf > 0) {
                    pflag = true;
                }
            } else {
                throw ContactExceptionCodes.CONTACT_NOT_FOUND.create(I(oid), I(ctx.getContextId()));
            }

            if ((client_date != null && client_date.getTime() >= 0) && (client_date.before(changing_date))) {
                throw ContactExceptionCodes.OBJECT_HAS_CHANGED.create(I(ctx.getContextId()), I(fuid), I(userId), I(oid));
            }
            final OXFolderAccess folderAccess = new OXFolderAccess(readcon, ctx);
            final FolderObject contactFolder = folderAccess.getFolderObject(fuid);
            if (contactFolder.getModule() != FolderObject.CONTACT) {
                throw ContactExceptionCodes.NON_CONTACT_FOLDER.create(I(fuid), I(ctx.getContextId()), I(userId));
            }
            if ((contactFolder.getType() != FolderObject.PRIVATE) && pflag) {
                LOG.debug(new StringBuilder("Here is a contact in a non PRIVATE folder with a set private flag -> (cid=").append(
                    ctx.getContextId()).append(" fid=").append(fuid).append(" oid=").append(oid).append(')'));
            } else if ((contactFolder.getType() == FolderObject.PRIVATE) && pflag && created_from != userId) {
                throw ContactExceptionCodes.NO_DELETE_PERMISSION.create(I(fuid), I(ctx.getContextId()), I(userId));
            }

            oclPerm = folderAccess.getFolderPermission(fuid, userId, userConfiguration);
            if (oclPerm.getFolderPermission() <= OCLPermission.NO_PERMISSIONS) {
                throw ContactExceptionCodes.NO_DELETE_PERMISSION.create(I(fuid), I(ctx.getContextId()), I(userId));
            }
        } catch (final SQLException e) {
            throw ContactExceptionCodes.SQL_PROBLEM.create(e);
        } catch (final OXException e) {
            throw e;
        } finally {
            DBUtils.closeSQLStuff(rs, stmt);
            try {
                if (readcon != null) {
                    DBPool.closeReaderSilent(ctx, readcon);
                }
            } catch (final Exception ex) {
                LOG.error("Unable to return Connection", ex);
            }
        }
        Connection writecon = null;
        try {
            writecon = DBPool.pickupWriteable(ctx);

            final int deletePermission = oclPerm.getDeletePermission();
            if (deletePermission >= OCLPermission.DELETE_ALL_OBJECTS) {
                // May delete any contact
                Contacts.deleteContact(oid, ctx.getContextId(), writecon);
            } else {
                if ((deletePermission < OCLPermission.DELETE_OWN_OBJECTS) || created_from != userId) {
                    throw ContactExceptionCodes.NO_DELETE_PERMISSION.create(I(fuid), I(ctx.getContextId()), I(userId));
                }
                // May delete own contact
                Contacts.deleteContact(oid, ctx.getContextId(), writecon);
            }
            final EventClient ec = new EventClient(session);
            ec.delete(co);
        } catch (final OXException e) {
            throw e;
        } finally {
            if (writecon != null) {
                DBPool.closeWriterSilent(ctx, writecon);
            }
        }
    }

    @Override
    public SearchIterator<Contact> getObjectsById(final int[][] object_id, final int[] cols) throws OXException, OXException {
        final int[] myCols = checkColumns(cols);
        try {
            final List<Contact> retval = new ArrayList<Contact>(object_id.length);

            int remain = object_id.length;
            int offset = 0;
            final int blockSize = 10;
            while (remain > blockSize) {
                // Copy block
                final int[][] block_object_id = new int[blockSize][];
                System.arraycopy(object_id, offset, block_object_id, 0, block_object_id.length);
                // Add contacts
                addQueriedContacts(myCols, retval, block_object_id);
                remain -= blockSize;
                offset += blockSize;
            }
            if (remain > 0) {
                final int[][] block_object_id = new int[remain][];
                System.arraycopy(object_id, offset, block_object_id, 0, block_object_id.length);
                // Add contacts
                addQueriedContacts(myCols, retval, block_object_id);
            }
            final int size = retval.size();
            return new SearchIteratorDelegator<Contact>(retval.iterator(), size);
        } catch (final OXException e) {
            throw e;
        } catch (final SQLException e) {
            throw ContactExceptionCodes.SQL_PROBLEM.create(e);
        }
    }

    private int[] checkColumns(final int[] cols) {
        final TIntSet tmp = new TIntHashSet();
        for (final int col : cols) {
            if (Contacts.mapping[col] != null) {
                tmp.add(col);
            } else if (Contact.IMAGE1_URL == col) {
                tmp.add(col);
                final int imageId = Contact.IMAGE1;
                if (!Arrays.contains(cols, Contact.IMAGE1) || !tmp.contains(imageId)) {
                    tmp.add(imageId);
                }
            } else if (CommonObject.LAST_MODIFIED_OF_NEWEST_ATTACHMENT == col) {
                tmp.add(col);
            } else if (Contact.CONTACT_NUMBER_OF_LINKS == col) {
                tmp.add(CommonObject.NUMBER_OF_LINKS);
            } else if (DataObject.LAST_MODIFIED_UTC == col) {
                tmp.add(DataObject.LAST_MODIFIED);
            } else {
                LOG.warn("UNKNOWN FIELD -> " + col);
            }
        }
        return tmp.toArray();
    }

    private int addQueriedContacts(final int[] cols, final List<Contact> retval, final int[][] object_id) throws SQLException, OXException {
        final Connection readcon = DBPool.pickup(ctx);
        boolean closeCon = true;
        try {
            /*
             * Create new contact SQL
             */
            final ContactSql contactSQL = new ContactMySql(session, ctx);
            contactSQL.setSelect(contactSQL.iFgetColsString(cols).toString());
            contactSQL.setObjectArray(object_id);
            /*
             * Necessary resources
             */
            PreparedStatement ps = null;
            ResultSet res = null;
            boolean closeStuff = true;
            SearchIterator<Contact> searchIterator = null;
            try {
                ps = contactSQL.getSqlStatement(readcon);
                res = ps.executeQuery();
                searchIterator = new ContactObjectIterator(res, ps, cols, true, readcon);
                if (searchIterator.size() != -1) {
                    final int size = searchIterator.size();
                    for (int i = 0; i < size; i++) {
                        retval.add(searchIterator.next());
                    }
                    return size;
                }
                int count = 0;
                while (searchIterator.hasNext()) {
                    retval.add(searchIterator.next());
                    count++;
                }
                return count;
            } finally {
                if (searchIterator != null) {
                    try {
                        searchIterator.close();
                        closeCon = false;
                        closeStuff = false;
                    } catch (final SearchIteratorException e) {
                        LOG.error("Unable to close search iterator", e);
                    }
                }
                if (closeStuff) {
                    DBUtils.closeSQLStuff(res, ps);
                }
            }
        } finally {
            if (closeCon) {
                DBPool.closeReaderSilent(ctx, readcon);
            }
        }
    }

    public static boolean performSecurityReadCheck(final int fid, final int created_from, final int user, final Session so, final Connection readcon, final Context ctx) {
        return Contacts.performContactReadCheck(
            fid,
            created_from,
            user,
            ctx,
            UserConfigurationStorage.getInstance().getUserConfigurationSafe(so.getUserId(), ctx),
            readcon);
    }

    protected Contact convertResultSet2ContactObject(final ResultSet rs, final int cols[], final boolean check, final Connection con) throws OXException {
        final Contact co = new Contact();
        Statement stmt = null;
        try {
            stmt = rs.getStatement();
            co.setParentFolderID(rs.getInt(1));
            co.setContextId(rs.getInt(2));
            co.setCreatedBy(rs.getInt(3));

            final long xx = rs.getLong((4));
            Date mi = new java.util.Date(xx);
            co.setCreationDate(mi);

            co.setModifiedBy(rs.getInt(5));

            final long xx2 = rs.getLong((6));
            mi = new java.util.Date(xx2);
            co.setLastModified(mi);

            co.setObjectID(rs.getInt(7));
            // Start at row count 8 to pass prefixed fields
            int cnt = 8;
            for (int a = 0; a < cols.length; a++) {
                final Mapper m = Contacts.mapping[cols[a]];
                if (m != null) {
                    m.addToContactObject(rs, cnt, co, con, userId, memberInGroups, ctx, userConfiguration);
                    cnt++;
                }
            }

            if (check && !performSecurityReadCheck(co.getParentFolderID(), co.getCreatedBy(), userId, session, con, ctx)) {
                throw ContactExceptionCodes.NO_ACCESS_PERMISSION.create(I(co.getParentFolderID()), I(ctx.getContextId()), I(userId));
            }
            if (Arrays.contains(cols, CommonObject.LAST_MODIFIED_OF_NEWEST_ATTACHMENT)) {
                final Date creationDate = Attachments.getInstance(new SimpleDBProvider(con, null)).getNewestCreationDate(
                    ctx,
                    Types.CONTACT,
                    co.getObjectID());
                if (null != creationDate) {
                    co.setLastModifiedOfNewestAttachment(creationDate);
                }
            }
        } catch (final SQLException e) {
            throw ContactExceptionCodes.SQL_PROBLEM.create(e);
        } catch (final OXException e) {
            throw new OXException(e);
        }

        return co;
    }

    protected List<Contact> convertResultsetsToContactsLikeAGrownup(final ResultSet rs) throws SQLException, OXException {
        final List<Contact> results = new LinkedList<Contact>();

        final ResultSetMetaData metaData = rs.getMetaData();
        final List<ContactField> header = new LinkedList<ContactField>();
        final int limit = metaData.getColumnCount();

        for (int i = 0; i < limit; i++) {
            final ContactField field = ContactField.getByFieldName(metaData.getColumnName(i + 1));
            header.add(field);
        }

        final ContactSwitcher setter = new ContactSwitcherForTimestamp(){{setDelegate(new ContactSetter());}};

        while (rs.next()) {
            final Contact contact = new Contact();
            for (int i = 0; i < limit; i++) {
                final ContactField field = header.get(i);
                if (field == null) {
                    continue;
                }
                field.doSwitch(setter, contact, rs.getObject(i+1));
            }
        }
        return results;
    }

    private class ContactObjectIterator implements SearchIterator<Contact> {

        private Contact nexto;

        private Contact pre;

        private final ResultSet rs;

        private final Statement stmt;

        private final Connection readcon;

        private final int[] cols;

        private boolean first = true;

        private final boolean securecheck;

        private final List<OXException> warnings;

        public ContactObjectIterator(final ResultSet rs, final Statement stmt, final int[] cols, final boolean securecheck, final Connection readcon) throws OXException {
            this.warnings = new ArrayList<OXException>(2);
            this.rs = rs;
            this.stmt = stmt;
            this.cols = cols;
            this.readcon = readcon;
            this.securecheck = securecheck;

            try {
                if (rs.next()) {
                    if (securecheck) {
                        nexto = convertResultSet2ContactObject(rs, cols, true, readcon);
                    } else {
                        nexto = convertResultSet2ContactObject(rs, cols, false, readcon);
                    }
                }
            } catch (final SQLException e) {
                throw ContactExceptionCodes.SQL_PROBLEM.create(e);
            } catch (final OXException e) {
                throw e;
            }
        }

        @Override
        public void close() throws OXException {
            closeSQLStuff(rs, stmt);
            if (readcon != null) {
                DBPool.closeReaderSilent(ctx, readcon);
            }
        }

        @Override
        public boolean hasNext() throws OXException {
            if (!first) {
                nexto = pre;
            }
            return nexto != null;
        }

        @Override
        public Contact next() throws SearchIteratorException, OXException {
            try {
                if (rs.next()) {
                    try {
                        if (securecheck) {
                            pre = convertResultSet2ContactObject(rs, cols, true, readcon);
                        } else {
                            pre = convertResultSet2ContactObject(rs, cols, false, readcon);
                        }
                    } catch (final OXException e) {
                        throw e;
                    }
                } else {
                    pre = null;
                }
                if (first) {
                    first = false;
                }

                return nexto;
            } catch (final SQLException e) {
                throw ContactExceptionCodes.SQL_PROBLEM.create(e);
            }
        }

        @Override
        public int size() {
            return -1;
        }

        public boolean hasSize() {
            return false;
        }

        @Override
        public void addWarning(final OXException warning) {
            warnings.add(warning);
        }

        @Override
        public OXException[] getWarnings() {
            return warnings.isEmpty() ? null : warnings.toArray(new OXException[warnings.size()]);
        }

        @Override
        public boolean hasWarnings() {
            return !warnings.isEmpty();
        }
    }

    @Override
    public int getFolderId() {
        return 0;
    }

    @Override
    public LdapServer getLdapServer() {
        return null;
    }

    private String generateOrder(final int order_field, final Order order, final SuperCollator collation) {
    	if (order_field <= 0 || order_field == Contact.SPECIAL_SORTING || order.equals(Order.NO_ORDER)) {
            return null;
        }

    	final boolean addCollation = collation != null && collation != SuperCollator.DEFAULT;

    	final StringBuilder sqlOrder = new StringBuilder();
        sqlOrder.append(" ORDER BY ");
        final int realOrderField = order_field == Contact.USE_COUNT_GLOBAL_FIRST ? Contact.USE_COUNT : order_field;
        if(addCollation) {
            sqlOrder.append(" CONVERT(");
        }
        sqlOrder.append(generateFieldPart(realOrderField));
        if(addCollation) {
            sqlOrder.append(" USING '")
        		.append(collation.getSqlCharset())
        		.append("') COLLATE '")
        		.append(collation.getSqlCollation())
        		.append('\'');
        }

        sqlOrder.append(' ');
        final String realOrderMechanism = order_field == Contact.USE_COUNT_GLOBAL_FIRST ? "DESC" : forSQLCommand(order);
        if (realOrderMechanism != null && realOrderMechanism.length() > 0) {
            sqlOrder.append(realOrderMechanism);
        } else {
            sqlOrder.append("ASC");
        }
        return sqlOrder.append(' ').toString();
	}

    private String generateFieldPart(final int fieldNum){
    	if(Contact.SPECIAL_SORTING != fieldNum) {
            return "co." + Contacts.mapping[fieldNum].getDBFieldName();
        }

    	final String prefix = "co.";
    	final StringBuffer buffy = new StringBuffer();
    	final List<ContactField> fieldsToCheck = java.util.Arrays.asList(ContactField.YOMI_LAST_NAME, ContactField.SUR_NAME, ContactField.DISPLAY_NAME, ContactField.YOMI_COMPANY, ContactField.COMPANY, ContactField.EMAIL1, ContactField.EMAIL2 );
    	for(final ContactField field: fieldsToCheck) {
            buffy.append("IFNULL(").append(prefix).append(field.getDbName()).append(',');
        }
    	buffy.append("NULL");
    	for(final ContactField field: fieldsToCheck) {
            buffy.append(')');
        }
    	return buffy.toString();
    }

	private String checkFolderRights(final ContactSearchtermSqlConverter conv, final ContactSql cs) throws OXException {

        if(! conv.hasFolders() ){
        	try {
                return cs.buildAllFolderSearchString(userId, memberInGroups, session).toString();
            } catch (final SearchIteratorException e) {
                throw new OXException(e);
            }
        }

        final OXFolderAccess oxfa = new OXFolderAccess(ctx);

        final List<String> folders = conv.getFolders();
        final int[] folders2 = new int[folders.size()]; int i = 0;
        final OXFolderAccess folderAccess = new OXFolderAccess(ctx);
        for (final String folderStr : folders) {
        	int folderId;
        	try {
        		folderId = Integer.parseInt(folderStr);
        	} catch(final NumberFormatException e){
        		throw ContactExceptionCodes.NON_CONTACT_FOLDER.create(folderStr, I(ctx.getContextId()), I(userId));
        	}
            final FolderObject contactFolder = folderAccess.getFolderObject(folderId);
            if (contactFolder.getModule() != FolderObject.CONTACT) {
                throw ContactExceptionCodes.NON_CONTACT_FOLDER.create(I(folderId), I(ctx.getContextId()), I(userId));
            }
            final EffectivePermission oclPerm = oxfa.getFolderPermission(folderId, userId, userConfiguration);
            if (oclPerm.getFolderPermission() <= OCLPermission.NO_PERMISSIONS) {
                throw ContactExceptionCodes.NO_ACCESS_PERMISSION.create(I(folderId), I(ctx.getContextId()), I(userId));
            }
            if (!oclPerm.canReadOwnObjects()) {
                throw ContactExceptionCodes.NO_ACCESS_PERMISSION.create(I(folderId), I(ctx.getContextId()), I(userId));
            }
            folders2[i++] = folderId;
        }
        return cs.buildFolderSearch(userId, memberInGroups, folders2, session);
	}

}
