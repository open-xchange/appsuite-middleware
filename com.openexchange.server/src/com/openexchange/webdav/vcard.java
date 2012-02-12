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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.contact.ContactInterface;
import com.openexchange.groupware.contact.ContactInterfaceDiscoveryService;
import com.openexchange.groupware.container.CommonObject;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.DataObject;
import com.openexchange.groupware.container.FolderChildObject;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.impl.IDGenerator;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.groupware.userconfiguration.UserConfigurationStorage;
import com.openexchange.login.Interface;
import com.openexchange.server.impl.DBPool;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.oxfolder.OXFolderAccess;
import com.openexchange.tools.sql.DBUtils;
import com.openexchange.tools.versit.Property;
import com.openexchange.tools.versit.Versit;
import com.openexchange.tools.versit.VersitDefinition;
import com.openexchange.tools.versit.VersitObject;
import com.openexchange.tools.versit.converter.OXContainerConverter;

/**
 * vcard
 *
 * @author <a href="mailto:sebastian.kauss@open-xchange.com">Sebastian Kauss</a>
 */
public final class vcard extends PermissionServlet {

    private static final long serialVersionUID = 1043665340444383184L;

    private final static int[] _contactFields = {
        DataObject.OBJECT_ID, DataObject.CREATED_BY, DataObject.CREATION_DATE, DataObject.LAST_MODIFIED, DataObject.MODIFIED_BY,
        FolderChildObject.FOLDER_ID, CommonObject.PRIVATE_FLAG, CommonObject.CATEGORIES, Contact.GIVEN_NAME, Contact.SUR_NAME,
        Contact.ANNIVERSARY, Contact.ASSISTANT_NAME, Contact.BIRTHDAY, Contact.BRANCHES,
        Contact.BUSINESS_CATEGORY, Contact.CATEGORIES, Contact.CELLULAR_TELEPHONE1, Contact.CELLULAR_TELEPHONE2,
        Contact.CITY_BUSINESS, Contact.CITY_HOME, Contact.CITY_OTHER, Contact.COMMERCIAL_REGISTER,
        Contact.COMPANY, Contact.COUNTRY_BUSINESS, Contact.COUNTRY_HOME, Contact.COUNTRY_OTHER,
        Contact.DEPARTMENT, Contact.DISPLAY_NAME, Contact.EMAIL1, Contact.EMAIL2, Contact.EMAIL3,
        Contact.EMPLOYEE_TYPE, Contact.FAX_BUSINESS, Contact.FAX_HOME, Contact.FAX_OTHER, Contact.FILE_AS,
        Contact.FOLDER_ID, Contact.GIVEN_NAME, Contact.INFO, Contact.INSTANT_MESSENGER1,
        Contact.INSTANT_MESSENGER2, Contact.MANAGER_NAME, Contact.MARITAL_STATUS, Contact.MIDDLE_NAME,
        Contact.NICKNAME, Contact.NOTE, Contact.NUMBER_OF_CHILDREN, Contact.NUMBER_OF_EMPLOYEE,
        Contact.POSITION, Contact.POSTAL_CODE_BUSINESS, Contact.POSTAL_CODE_HOME, Contact.POSTAL_CODE_OTHER,
        Contact.PRIVATE_FLAG, Contact.PROFESSION, Contact.ROOM_NUMBER, Contact.SALES_VOLUME,
        Contact.SPOUSE_NAME, Contact.STATE_BUSINESS, Contact.STATE_HOME, Contact.STATE_OTHER,
        Contact.STREET_BUSINESS, Contact.STREET_HOME, Contact.STREET_OTHER, Contact.SUFFIX, Contact.TAX_ID,
        Contact.TELEPHONE_ASSISTANT, Contact.TELEPHONE_BUSINESS1, Contact.TELEPHONE_BUSINESS2,
        Contact.TELEPHONE_CALLBACK, Contact.TELEPHONE_CAR, Contact.TELEPHONE_COMPANY, Contact.TELEPHONE_HOME1,
        Contact.TELEPHONE_HOME2, Contact.TELEPHONE_IP, Contact.TELEPHONE_ISDN, Contact.TELEPHONE_OTHER,
        Contact.TELEPHONE_PAGER, Contact.TELEPHONE_PRIMARY, Contact.TELEPHONE_RADIO, Contact.TELEPHONE_TELEX,
        Contact.TELEPHONE_TTYTDD, Contact.TITLE, Contact.URL, Contact.DEFAULT_ADDRESS };

    private static final String STR_USER_AGENT = "user-agent";

    private static final String CONTACTFOLDER = "contactfolder";

    private static final String ENABLEDELETE = "enabledelete";

    private static String SQL_PRINCIPAL_SELECT = "SELECT object_id, contactfolder FROM vcard_principal WHERE cid = ? AND principal = ?";

    private static String SQL_PRINCIPAL_INSERT = "INSERT INTO vcard_principal (object_id, cid, principal, contactfolder) VALUES (?, ?, ?, ?)";

    private static String SQL_PRINCIPAL_UPDATE = "UPDATE vcard_principal SET contactfolder = ? WHERE object_id = ?";

    private static String SQL_ENTRIES_LOAD = "SELECT object_id, client_id, target_object_id FROM vcard_ids WHERE cid = ? AND principal_id = ?";

    private static String SQL_ENTRY_INSERT = "INSERT INTO vcard_ids (object_id, cid, principal_id, client_id, target_object_id) VALUES (?, ?, ?, ? ,?)";

    private static String SQL_ENTRY_DELETE = "DELETE FROM vcard_ids WHERE target_object_id = ? AND principal_id = ?";

    private static transient final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(vcard.class));

    public void oxinit() {
        // Nothing to do
    }

    @Override
    protected Interface getInterface() {
        return Interface.WEBDAV_VCARD;
    }

    @Override
    public void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("GET");
        }

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
                contactfolder_id = new OXFolderAccess(context).getDefaultFolder(sessionObj.getUserId(), FolderObject.CONTACT).getObjectID();
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

            final VersitDefinition def = Versit.getDefinition("text/vcard");
            final VersitDefinition.Writer w = def.getWriter(os, "UTF-8");
            final OXContainerConverter oxc = new OXContainerConverter(sessionObj);

            SearchIterator<Contact> it = null;

            try {
                resp.setStatus(HttpServletResponse.SC_OK);
                resp.setContentType("text/vcard");

                final ContactInterface contactInterface = ServerServiceRegistry.getInstance().getService(
                    ContactInterfaceDiscoveryService.class).newContactInterface(contactfolder_id, sessionObj);

                //final ContactSQLInterface contactInterface = new RdbContactSQLInterface(sessionObj);
                it = contactInterface.getModifiedContactsInFolder(contactfolder_id, _contactFields, new Date(0));

                while (it.hasNext()) {
                    final Contact contactObject = it.next();

                    final VersitObject vo = oxc.convertContact(contactObject, "3.0");
                    def.write(w, vo);

                    entries.add(Integer.toString(contactObject.getObjectID()));
                }
            } finally {
                w.flush();
                w.close();
                oxc.close();

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
        if (LOG.isDebugEnabled()) {
            LOG.debug("PUT");
        }

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
                contactfolder_id = new OXFolderAccess(context).getDefaultFolder(session.getUserId(), FolderObject.CONTACT).getObjectID();
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

            final VersitDefinition def = Versit.getDefinition(content_type);

            final OXContainerConverter oxc = new OXContainerConverter(session);
            //final ContactSQLInterface contactInterface = new RdbContactSQLInterface(session);

            final Date timestamp = new Date();
            try {
                final VersitDefinition.Reader r = def.getReader(is, "UTF-8");

                while (true) {
                    final VersitObject vo = def.parse(r);
                    if (vo == null) {
                        break;
                    }

                    final Property property = vo.getProperty("UID");

                    client_id = null;
                    int object_id = 0;

                    if (property != null) {
                        client_id = property.getValue().toString();
                    }

                    final Contact contactObj = oxc.convertContact(vo);

                    if (contactObj.getObjectID() == 0) {
                        contactObj.setParentFolderID(contactfolder_id);
                    }

                    try {
                        if (client_id != null && entries_db.containsKey(client_id)) {
                            try {
                                object_id = Integer.parseInt(entries_db.get(client_id));
                            } catch (final NumberFormatException exc) {
                                if (LOG.isDebugEnabled()) {
                                    LOG.debug("object id is not an integer");
                                }
                            }

                            if (object_id > 0) {
                                contactObj.setObjectID(object_id);
                            }

                            contactObj.setParentFolderID(contactfolder_id);

                            final ContactInterface contactInterface = ServerServiceRegistry.getInstance().getService(
                                ContactInterfaceDiscoveryService.class).newContactInterface(contactfolder_id, session);
                            if (contactObj.containsObjectID()) {
                                contactInterface.updateContactObject(contactObj, contactfolder_id, timestamp);
                            } else {
                                contactInterface.insertContactObject(contactObj);
                            }

                            entries.add(client_id);
                        } else {
                            contactObj.setParentFolderID(contactfolder_id);

                            final ContactInterface contactInterface = ServerServiceRegistry.getInstance().getService(
                                ContactInterfaceDiscoveryService.class).newContactInterface(contactfolder_id, session);
                            if (contactObj.containsObjectID()) {
                                contactInterface.updateContactObject(contactObj, contactfolder_id, timestamp);
                            } else {
                                contactInterface.insertContactObject(contactObj);
                            }

                            if (client_id != null) {
                                entries.add(client_id);
                                addEntry(context, principal_id, contactObj.getObjectID(), client_id);
                            }
                        }
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("STATUS: OK");
                        }
                    } catch (final OXException exc) {
                        if (exc.isNotFound()) {
                            LOG.debug("object was already deleted on server: " + object_id, exc);
                        } else {
                            throw exc;
                        }
                    }
                }
            } finally {
                oxc.close();
            }

            for (final Map.Entry<String, String> entry : entries_db.entrySet()) {
                final String tmp = entry.getKey();
                if (!entries.contains(tmp)) {
                    final int object_id = Integer.parseInt(entry.getValue());

                    deleteEntry(context, principal_id, object_id);

                    if (enabledelete) {
                        final ContactInterface contactInterface = ServerServiceRegistry.getInstance().getService(
                            ContactInterfaceDiscoveryService.class).newContactInterface(contactfolder_id, session);
                        try {
                            contactInterface.deleteContactObject(object_id, contactfolder_id, timestamp);
                        } catch (final OXException exc) {
                            if (exc.isNotFound()) {
                                LOG.debug("object was already deleted on server: " + object_id, exc);
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
