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

package com.openexchange.webdav;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.contact.ContactService;
import com.openexchange.contact.internal.VCardUtil;
import com.openexchange.contact.vcard.VCardImport;
import com.openexchange.contact.vcard.VCardService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.impl.IDGenerator;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.groupware.userconfiguration.UserConfigurationStorage;
import com.openexchange.java.Streams;
import com.openexchange.login.Interface;
import com.openexchange.server.impl.DBPool;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIterators;
import com.openexchange.tools.oxfolder.OXFolderAccess;
import com.openexchange.tools.sql.DBUtils;

/**
 * vcard
 *
 * @author <a href="mailto:sebastian.kauss@open-xchange.com">Sebastian Kauss</a>
 */
public final class vcard extends PermissionServlet {

    private static final long serialVersionUID = 1043665340444383184L;

    private final static ContactField[] _contactFields = {
        ContactField.OBJECT_ID, ContactField.CREATED_BY, ContactField.CREATION_DATE, ContactField.LAST_MODIFIED, ContactField.MODIFIED_BY,
        ContactField.FOLDER_ID, ContactField.PRIVATE_FLAG, ContactField.CATEGORIES, ContactField.GIVEN_NAME, ContactField.SUR_NAME,
        ContactField.ANNIVERSARY, ContactField.ASSISTANT_NAME, ContactField.BIRTHDAY, ContactField.BRANCHES,
        ContactField.BUSINESS_CATEGORY, ContactField.CATEGORIES, ContactField.CELLULAR_TELEPHONE1, ContactField.CELLULAR_TELEPHONE2,
        ContactField.CITY_BUSINESS, ContactField.CITY_HOME, ContactField.CITY_OTHER, ContactField.COMMERCIAL_REGISTER,
        ContactField.COMPANY, ContactField.COUNTRY_BUSINESS, ContactField.COUNTRY_HOME, ContactField.COUNTRY_OTHER,
        ContactField.DEPARTMENT, ContactField.DISPLAY_NAME, ContactField.EMAIL1, ContactField.EMAIL2, ContactField.EMAIL3,
        ContactField.EMPLOYEE_TYPE, ContactField.FAX_BUSINESS, ContactField.FAX_HOME, ContactField.FAX_OTHER, ContactField.FILE_AS,
        ContactField.FOLDER_ID, ContactField.GIVEN_NAME, ContactField.INFO, ContactField.INSTANT_MESSENGER1,
        ContactField.INSTANT_MESSENGER2, ContactField.MANAGER_NAME, ContactField.MARITAL_STATUS, ContactField.MIDDLE_NAME,
        ContactField.NICKNAME, ContactField.NOTE, ContactField.NUMBER_OF_CHILDREN, ContactField.NUMBER_OF_EMPLOYEE,
        ContactField.POSITION, ContactField.POSTAL_CODE_BUSINESS, ContactField.POSTAL_CODE_HOME, ContactField.POSTAL_CODE_OTHER,
        ContactField.PRIVATE_FLAG, ContactField.PROFESSION, ContactField.ROOM_NUMBER, ContactField.SALES_VOLUME,
        ContactField.SPOUSE_NAME, ContactField.STATE_BUSINESS, ContactField.STATE_HOME, ContactField.STATE_OTHER,
        ContactField.STREET_BUSINESS, ContactField.STREET_HOME, ContactField.STREET_OTHER, ContactField.SUFFIX, ContactField.TAX_ID,
        ContactField.TELEPHONE_ASSISTANT, ContactField.TELEPHONE_BUSINESS1, ContactField.TELEPHONE_BUSINESS2,
        ContactField.TELEPHONE_CALLBACK, ContactField.TELEPHONE_CAR, ContactField.TELEPHONE_COMPANY, ContactField.TELEPHONE_HOME1,
        ContactField.TELEPHONE_HOME2, ContactField.TELEPHONE_IP, ContactField.TELEPHONE_ISDN, ContactField.TELEPHONE_OTHER,
        ContactField.TELEPHONE_PAGER, ContactField.TELEPHONE_PRIMARY, ContactField.TELEPHONE_RADIO, ContactField.TELEPHONE_TELEX,
        ContactField.TELEPHONE_TTYTDD, ContactField.TITLE, ContactField.URL, ContactField.DEFAULT_ADDRESS, ContactField.VCARD_ID };

    private static final String STR_USER_AGENT = "user-agent";

    private static final String CONTACTFOLDER = "contactfolder";

    private static final String ENABLEDELETE = "enabledelete";

    private static String SQL_PRINCIPAL_SELECT = "SELECT object_id, contactfolder FROM vcard_principal WHERE cid = ? AND principal = ?";

    private static String SQL_PRINCIPAL_INSERT = "INSERT INTO vcard_principal (object_id, cid, principal, contactfolder) VALUES (?, ?, ?, ?)";

    private static String SQL_PRINCIPAL_UPDATE = "UPDATE vcard_principal SET contactfolder = ? WHERE object_id = ?";

    private static String SQL_ENTRIES_LOAD = "SELECT object_id, client_id, target_object_id FROM vcard_ids WHERE cid = ? AND principal_id = ?";

    private static String SQL_ENTRY_INSERT = "INSERT INTO vcard_ids (object_id, cid, principal_id, client_id, target_object_id) VALUES (?, ?, ?, ? ,?)";

    private static String SQL_ENTRY_DELETE = "DELETE FROM vcard_ids WHERE target_object_id = ? AND principal_id = ?";

    private static transient final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(vcard.class);

    public void oxinit() {
        // Nothing to do
    }

    @Override
    protected Interface getInterface() {
        return Interface.WEBDAV_VCARD;
    }

    @Override
    public void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
        LOG.debug("GET");

        final OutputStream os = resp.getOutputStream();

        String user_agent = null;
        String principal = null;

        final Session sessionObj = getSession(req);

        try {
            final Context context = ContextStorage.getInstance().getContext(sessionObj.getContextId());

            resp.setStatus(HttpServletResponse.SC_OK);

            // get user-agent
            user_agent = getUserAgent(req);

            principal = user_agent + '_' + sessionObj.getUserId();

            int contactfolder_id = getContactFolderID(req);

            if (contactfolder_id == 0) {
                contactfolder_id = new OXFolderAccess(context).getDefaultFolderID(sessionObj.getUserId(), FolderObject.CONTACT);
            }

            int db_contactfolder_id = 0;

            int principal_id = 0;

            Map<String, String> entries_db = new HashMap<String, String>(0);
            final Set<String> entries = new HashSet<String>();

            Connection readCon = null;

            PreparedStatement principalStatement = null;

            ResultSet rs = null;

            boolean exists = false;
            try {
                readCon = DBPool.pickup(context);

                principalStatement = readCon.prepareStatement(SQL_PRINCIPAL_SELECT);
                principalStatement.setLong(1, context.getContextId());
                principalStatement.setString(2, principal);
                rs = principalStatement.executeQuery();

                exists = rs.next();

                if (exists) {
                    principal_id = rs.getInt(1);
                    db_contactfolder_id = rs.getInt(2);

                    entries_db = loadDBEntries(context, principal_id);
                }
            } finally {
                DBUtils.closeResources(rs, principalStatement, readCon, true, context);
            }

            SearchIterator<Contact> it = null;

            try {
                resp.setStatus(HttpServletResponse.SC_OK);
                resp.setContentType("text/vcard");

                final ContactService contactService = ServerServiceRegistry.getInstance().getService(ContactService.class);
                it = contactService.getAllContacts(sessionObj, Integer.toString(contactfolder_id), _contactFields);
                while (it.hasNext()) {
                    final Contact contactObject = it.next();

                    VCardUtil.exportContact(contactObject, sessionObj, os);

                    entries.add(Integer.toString(contactObject.getObjectID()));
                }
            } finally {
                if (it != null) {
                    it.close();
                }
            }

            Connection writeCon = null;

            PreparedStatement ps = null;
            try {
                writeCon = DBPool.pickupWriteable(context);
                if (exists) {
                    if (!(db_contactfolder_id == contactfolder_id)) {
                        ps = writeCon.prepareStatement(SQL_PRINCIPAL_UPDATE);
                        ps.setInt(1, principal_id);
                        ps.setInt(2, contactfolder_id);

                        ps.executeUpdate();
                        ps.close();
                    }
                } else {
                    writeCon.setAutoCommit(false);
                    principal_id = IDGenerator.getId(context, Types.ICAL, writeCon);
                    writeCon.commit();

                    ps = writeCon.prepareStatement(SQL_PRINCIPAL_INSERT);
                    ps.setInt(1, principal_id);
                    ps.setLong(2, context.getContextId());
                    ps.setString(3, principal);
                    ps.setInt(4, contactfolder_id);

                    ps.executeUpdate();
                    ps.close();
                }
            } finally {
                if (ps != null) {
                    ps.close();
                }

                if (writeCon != null) {
                    writeCon.setAutoCommit(true);
                    DBPool.pushWrite(context, writeCon);
                }
            }

            for (final Iterator<String> iterator = entries.iterator(); iterator.hasNext();) {
                final String s_object_id = iterator.next();

                if (!entries_db.containsKey(s_object_id)) {
                    addEntry(context, principal_id, Integer.parseInt(s_object_id), s_object_id);
                }
            }

            for (final Iterator<String> databaseIterator = entries_db.keySet().iterator(); databaseIterator.hasNext();) {
                final String s_object_id = entries_db.get(databaseIterator.next());

                if (!entries.contains(s_object_id)) {
                    deleteEntry(context, principal_id, Integer.parseInt(s_object_id));
                }
            }
        } catch (final OXException exc) {
            if (exc.isConflict()) {
                LOG.debug("doGet", exc);
                doError(resp, HttpServletResponse.SC_CONFLICT, exc.getMessage());
            } else {
                LOG.error("doGet", exc);
                doError(resp);
            }
        } catch (final Exception exc) {
            LOG.error("doGet", exc);
            doError(resp);
        }
    }

    @Override
    public void doPut(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
        LOG.debug("PUT");

        String content_type = null;

        String client_id = null;

        final InputStream is = req.getInputStream();

        String user_agent = null;
        String principal = null;

        final Session session = getSession(req);

        try {
            final Context context = ContextStorage.getInstance().getContext(session.getContextId());

            resp.setStatus(HttpServletResponse.SC_OK);

            user_agent = getUserAgent(req);
            content_type = req.getContentType();

            log("read vcard content_type: " + content_type);

            if (content_type == null) {
                content_type = "text/vcard";
            }

            if (user_agent == null) {
                throw WebdavExceptionCode.MISSING_HEADER_FIELD.create(STR_USER_AGENT);
            }

            principal = user_agent + '_' + session.getUserId();

            int contactfolder_id = getContactFolderID(req);

            if (contactfolder_id == 0) {
                contactfolder_id = new OXFolderAccess(context).getDefaultFolderID(session.getUserId(), FolderObject.CONTACT);
            }

            if (contactfolder_id == FolderObject.SYSTEM_LDAP_FOLDER_ID) {
                resp.setContentType("text/html");
                resp.setStatus(HttpServletResponse.SC_CONFLICT);

                final PrintWriter pw = resp.getWriter();
                pw.println("folder internal users is only readable!");
                pw.flush();

                return;
            }

            final Map<String, String> entries_db;
            final Set<String> entries = new HashSet<String>();

            final boolean enabledelete = getEnableDelete(req);

            boolean exists = false;

            int principal_id = 0;

            Connection readCon = null;

            PreparedStatement principalStatement = null;

            ResultSet rs = null;
            try {
                readCon = DBPool.pickup(context);
                principalStatement = readCon.prepareStatement(SQL_PRINCIPAL_SELECT);
                principalStatement.setLong(1, context.getContextId());
                principalStatement.setString(2, principal);
                rs = principalStatement.executeQuery();

                int db_contactfolder_id = 0;

                exists = rs.next();

                if (!exists) {
                    throw WebdavExceptionCode.NO_PRINCIPAL.create(principal);
                }
                principal_id = rs.getInt(1);
                db_contactfolder_id = rs.getInt(2);

                if (db_contactfolder_id != contactfolder_id) {
                    throw WebdavExceptionCode.NO_PRINCIPAL.create(principal);
                }

                entries_db = loadDBEntries(context, principal_id);
            } finally {
                DBUtils.closeResources(rs, principalStatement, readCon, true, context);
            }

            VCardService vCardService = ServerServiceRegistry.getServize(VCardService.class, true);
            SearchIterator<VCardImport> searchIterator = null;
            final Date timestamp = new Date();
            try {
                searchIterator = vCardService.importVCards(is, vCardService.createParameters(session));
                while (searchIterator.hasNext()) {
                    int object_id = 0;
                    VCardImport vCardImport = null;
                    try {
                        vCardImport = searchIterator.next();

                        final Contact contactObj = vCardImport.getContact();
                        client_id = contactObj.getUid();

                        if (contactObj.getObjectID() == 0) {
                            contactObj.setParentFolderID(contactfolder_id);
                        }

                        if (client_id != null && entries_db.containsKey(client_id)) {
                            try {
                                object_id = Integer.parseInt(entries_db.get(client_id));
                            } catch (final NumberFormatException exc) {
                                LOG.debug("object id is not an integer");
                            }

                            if (object_id > 0) {
                                contactObj.setObjectID(object_id);
                            }

                            contactObj.setParentFolderID(contactfolder_id);

                            final ContactService contactService = ServerServiceRegistry.getInstance().getService(ContactService.class);
                            if (contactObj.containsObjectID()) {
                                contactService.updateContact(session, Integer.toString(contactfolder_id), Integer.toString(object_id),
                                        contactObj, timestamp);
                            } else {
                                contactService.createContact(session, Integer.toString(contactfolder_id), contactObj);
                            }

                            entries.add(client_id);
                        } else {
                            contactObj.setParentFolderID(contactfolder_id);

                            final ContactService contactService = ServerServiceRegistry.getInstance().getService(ContactService.class);
                            if (contactObj.containsObjectID()) {
                                contactService.updateContact(session, Integer.toString(contactfolder_id),
                                        Integer.toString(contactObj.getObjectID()), contactObj, timestamp);
                            } else {
                                contactService.createContact(session, Integer.toString(contactfolder_id), contactObj);
                            }

                            if (client_id != null) {
                                entries.add(client_id);
                                addEntry(context, principal_id, contactObj.getObjectID(), client_id);
                            }
                        }
                        LOG.debug("STATUS: OK");
                    } catch (final OXException exc) {
                        if (exc.isNotFound()) {
                            LOG.debug("object was already deleted on server: {}", object_id, exc);
                        } else {
                            throw exc;
                        }
                    } finally {
                        Streams.close(vCardImport);
                    }
                }
            } finally {
                SearchIterators.close(searchIterator);
            }

            for (final Map.Entry<String, String> entry : entries_db.entrySet()) {
                final String tmp = entry.getKey();
                if (!entries.contains(tmp)) {
                    final int object_id = Integer.parseInt(entry.getValue());

                    deleteEntry(context, principal_id, object_id);

                    if (enabledelete) {
                    	final ContactService contactService = ServerServiceRegistry.getInstance().getService(ContactService.class);
                        try {
                        	contactService.deleteContact(session, Integer.toString(contactfolder_id), Integer.toString(object_id),
                        			timestamp);
                        } catch (final OXException exc) {
                            if (exc.isNotFound()) {
                                LOG.debug("object was already deleted on server: {}", object_id, exc);
                            } else {
                                throw exc;
                            }
                        }
                    }
                }
            }
        } catch (final OXException exc) {
            if (exc.isNoPermission()) {
                LOG.debug("doPut", exc);
                doError(resp, HttpServletResponse.SC_FORBIDDEN, exc.getMessage());
            } else if (exc.isConflict()) {
                LOG.debug("doPut", exc);
                doError(resp, HttpServletResponse.SC_CONFLICT, exc.getMessage());
            } else {
                LOG.error("doPut", exc);
                doError(resp);
            }
        }catch (final Exception exc) {
            LOG.error("doPut", exc);
            doError(resp);
        }
    }

    private void doError(final HttpServletResponse resp) {
        doError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Server Error");
    }

    private void doError(final HttpServletResponse resp, final int code, final String msg) {
        log("ERROR: " + code + ": " + msg);
        resp.setStatus(code);
        resp.setContentType("text/html");
    }

    private String getUserAgent(final HttpServletRequest req) throws OXException {
        final Enumeration<?> e = req.getHeaderNames();
        while (e.hasMoreElements()) {
            final String tmp = e.nextElement().toString().toLowerCase();
            if (STR_USER_AGENT.equals(tmp)) {
                return req.getHeader(STR_USER_AGENT);
            }
        }
        throw WebdavExceptionCode.MISSING_HEADER_FIELD.create(STR_USER_AGENT);
    }

    private int getContactFolderID(final HttpServletRequest req) throws OXException {
        if (req.getParameter(CONTACTFOLDER) != null) {
            try {
                return Integer.parseInt(req.getParameter(CONTACTFOLDER));
            } catch (final NumberFormatException exc) {
                throw WebdavExceptionCode.NOT_A_NUMBER.create(exc, CONTACTFOLDER);
            }
        }
        return 0;
    }

    private Map<String, String> loadDBEntries(final Context context, final int principal_object_id) throws OXException, SQLException {
        final Map<String, String> entries_db = new HashMap<String, String>();

        Connection readCon = null;

        PreparedStatement ps = null;

        ResultSet rs = null;

        try {
            readCon = DBPool.pickup(context);
            ps = readCon.prepareStatement(SQL_ENTRIES_LOAD);
            ps.setInt(1, principal_object_id);
            ps.setLong(2, context.getContextId());
            rs = ps.executeQuery();

            while (rs.next()) {
                entries_db.put(rs.getString(2), Integer.toString(rs.getInt(3)));
            }

            rs.close();
            ps.close();
        } finally {
            if (rs != null) {
                rs.close();
            }

            if (ps != null) {
                ps.close();
            }

            if (readCon != null) {
                DBPool.push(context, readCon);
            }
        }

        return entries_db;
    }

    private boolean getEnableDelete(final HttpServletRequest req) {
        if ((req.getParameter(ENABLEDELETE) != null) && (req.getParameter(ENABLEDELETE).toLowerCase().equals("yes"))) {
            return true;
        }

        return false;
    }

    private void addEntry(final Context context, final int principal_id, final int object_target_id, final String client_id) throws OXException, SQLException {
        Connection writeCon = null;

        PreparedStatement ps = null;

        try {
            writeCon = DBPool.pickupWriteable(context);
            writeCon.setAutoCommit(false);
            final int objectId = IDGenerator.getId(context, Types.ICAL, writeCon);
            writeCon.commit();

            ps = writeCon.prepareStatement(SQL_ENTRY_INSERT);
            ps.setInt(1, objectId);
            ps.setLong(2, context.getContextId());
            ps.setInt(3, principal_id);
            ps.setString(4, client_id);
            ps.setInt(5, object_target_id);

            ps.executeUpdate();
            ps.close();
        } finally {
            if (ps != null) {
                ps.close();
            }

            if (writeCon != null) {
                writeCon.setAutoCommit(true);
                DBPool.pushWrite(context, writeCon);
            }
        }
    }

    private void deleteEntry(final Context context, final int principal_id, final int object_target_id) throws OXException, SQLException {
        Connection writeCon = null;

        PreparedStatement ps = null;

        try {
            writeCon = DBPool.pickupWriteable(context);
            ps = writeCon.prepareStatement(SQL_ENTRY_DELETE);
            ps.setInt(1, object_target_id);
            ps.setInt(2, principal_id);

            ps.executeUpdate();
            ps.close();
        } finally {
            if (ps != null) {
                ps.close();
            }

            if (writeCon != null) {
                DBPool.pushWrite(context, writeCon);
            }
        }
    }

    @Override
    protected boolean hasModulePermission(final Session sessionObj, final Context ctx) {
        final UserConfiguration uc = UserConfigurationStorage.getInstance().getUserConfigurationSafe(sessionObj.getUserId(), ctx);
        return (uc.hasVCard() && uc.hasContact());
    }

    @Override
    protected void decrementRequests() {
        // Nothing to do
    }

    @Override
    protected void incrementRequests() {
        // Nothing to do
    }

}
