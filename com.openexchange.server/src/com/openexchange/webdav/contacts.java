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
import java.io.OutputStream;
import java.util.Date;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jdom2.output.XMLOutputter;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import com.openexchange.contact.ContactService;
import com.openexchange.exception.Category;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.groupware.userconfiguration.UserConfigurationStorage;
import com.openexchange.login.Interface;
import com.openexchange.monitoring.MonitoringInfo;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.webdav.tasks.QueuedTask;
import com.openexchange.webdav.xml.ContactParser;
import com.openexchange.webdav.xml.ContactWriter;
import com.openexchange.webdav.xml.DataParser;
import com.openexchange.webdav.xml.XmlServlet;
import com.openexchange.webdav.xml.fields.DataFields;

/**
 * contacts
 *
 * @author <a href="mailto:sebastian.kauss@open-xchange.com">Sebastian Kauss</a>
 */
public final class contacts extends XmlServlet<ContactService> {

    private static final long serialVersionUID = -3731372041610025543L;

    static final transient org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(contacts.class);

    /**
     * Initializes a new {@link contacts}.
     */
    public contacts() {
        super();
    }

    @Override
    protected Interface getInterface() {
        return Interface.WEBDAV_XML;
    }

    @Override
    protected boolean isServletDisabled() {
        return true;
    }

    @Override
    protected void parsePropChilds(final HttpServletRequest req, final HttpServletResponse resp,
            final XmlPullParser parser, final PendingInvocations<ContactService> pendingInvocations)
            throws XmlPullParserException, IOException, OXException {
        final Session session = getSession(req);

        if (isTag(parser, "prop", "DAV:")) {
            /*
             * Adjust parser
             */
            parser.nextTag();

            final Contact contactobject = new Contact();

            final ContactParser contactparser = new ContactParser(session);
            contactparser.parse(parser, contactobject);

            final int method = contactparser.getMethod();

            final Date lastModified = contactobject.getLastModified();

            final int inFolder = contactparser.getFolder();

            /*
             * Prepare contact for being queued
             */
            switch (method) {
            case DataParser.SAVE:
                if (contactobject.containsObjectID()) {
                    pendingInvocations.add(new QueuedContact(contactobject, contactparser.getClientID(), method,
                            lastModified, inFolder, session));
                } else {
                    contactobject.setParentFolderID(inFolder);

                    if (contactobject.containsImage1() && contactobject.getImage1() == null) {
                        contactobject.removeImage1();
                    }

                    pendingInvocations.add(new QueuedContact(contactobject, contactparser.getClientID(), method,
                            lastModified, inFolder, session));
                }
                break;
            case DataParser.DELETE:
                pendingInvocations.add(new QueuedContact(contactobject, contactparser.getClientID(), method,
                        lastModified, inFolder, session));
                break;
            default:
                LOG.debug("invalid method: {}", method);
            }
        } else {
            parser.next();
        }
    }

    @Override
    protected void performActions(final OutputStream os, final Session session,
            final PendingInvocations<ContactService> pendingInvocations) throws IOException {
        final ContactService contactService = ServerServiceRegistry.getInstance().getService(ContactService.class);
        while (!pendingInvocations.isEmpty()) {
            final QueuedContact qcon = (QueuedContact) pendingInvocations.poll();
            if (null != qcon) {
                qcon.setLastModifiedCache(pendingInvocations.getLastModifiedCache());
                qcon.actionPerformed(contactService, os, session.getUserId());
            }
        }
    }

    @Override
    protected void startWriter(final Session sessionObj, final Context ctx, final int objectId, final int folderId,
            final OutputStream os) throws Exception {
        final User userObj = UserStorage.getInstance().getUser(sessionObj.getUserId(), ctx);
        final ContactWriter contactwriter = new ContactWriter(userObj, ctx, sessionObj);
        contactwriter.startWriter(objectId, folderId, os);
    }

    @Override
    protected void startWriter(final Session sessionObj, final Context ctx, final int folderId,
            final boolean bModified, final boolean bDelete, final Date lastsync, final OutputStream os)
            throws Exception {
        startWriter(sessionObj, ctx, folderId, bModified, bDelete, false, lastsync, os);
    }

    @Override
    protected void startWriter(final Session sessionObj, final Context ctx, final int folderId,
            final boolean bModified, final boolean bDelete, final boolean bList, final Date lastsync,
            final OutputStream os) throws Exception {
        final User userObj = UserStorage.getInstance().getUser(sessionObj.getUserId(), ctx);
        final ContactWriter contactwriter = new ContactWriter(userObj, ctx, sessionObj);
        contactwriter.startWriter(bModified, bDelete, bList, folderId, lastsync, os);
    }

    @Override
    protected boolean hasModulePermission(final Session sessionObj, final Context ctx) {
        final UserConfiguration uc = UserConfigurationStorage.getInstance().getUserConfigurationSafe(
                sessionObj.getUserId(), ctx);
        return (uc.hasWebDAVXML() && uc.hasContact());
    }

    private final class QueuedContact implements QueuedAction<ContactService> {

        private final Contact contactObject;

        private final String clientId;

        private final int action;

        private final Date lastModified;

        private final int inFolder;

        private LastModifiedCache lastModifiedCache;

        private final Session session;

        /**
         * Initializes a new {@link QueuedTask}
         *
         * @param contactObject The contact object
         * @param clientId The client ID
         * @param action The desired action
         * @param lastModified The last-modified date
         * @param inFolder The contact's folder
         * @param session The session
         */
        public QueuedContact(final Contact contactObject, final String clientId, final int action,
                final Date lastModified, final int inFolder, final Session session) {
            super();
            this.contactObject = contactObject;
            this.clientId = clientId;
            this.action = action;
            this.lastModified = lastModified;
            this.inFolder = inFolder;
            this.lastModifiedCache = new LastModifiedCache();
            this.session = session;
        }

        @Override
        public void actionPerformed(final ContactService contactService, final OutputStream os, final int user)
                throws IOException {

            final XMLOutputter xo = new XMLOutputter();
            if (contactObject.getLastModified() == null) {
            	contactObject.setLastModified(lastModified);
            }
            try {
                switch (action) {
                case DataParser.SAVE:
                    if (contactObject.containsObjectID()) {
                        if (lastModified == null) {
                            throw WebdavExceptionCode.MISSING_FIELD.create(DataFields.LAST_MODIFIED);
                        }

                        final Date currentLastModified = lastModifiedCache.getLastModified(contactObject.getObjectID(), lastModified);
                        lastModifiedCache.update(contactObject.getObjectID(), 0, lastModified);
                        contactService.updateContact(session, Integer.toString(inFolder), Integer.toString(contactObject.getObjectID()),
                        		contactObject, currentLastModified);
                        lastModifiedCache.update(contactObject.getObjectID(), 0, contactObject.getLastModified());
                    } else {
                        contactService.createContact(session, Integer.toString(inFolder), contactObject);
                        lastModifiedCache.update(contactObject.getObjectID(), 0, contactObject.getLastModified());
                    }
                    break;
                case DataParser.DELETE:
                    if (lastModified == null) {
                        throw WebdavExceptionCode.MISSING_FIELD.create(DataFields.LAST_MODIFIED);
                    }
                    contactService.deleteContact(session, Integer.toString(inFolder), Integer.toString(contactObject.getObjectID()),
                    		contactObject.getLastModified());
                    break;
                default:
                    throw WebdavExceptionCode.INVALID_ACTION.create(Integer.valueOf(action));
                }
                writeResponse(contactObject, HttpServletResponse.SC_OK, OK, clientId, os, xo);
            } catch (final OXException exc) {
                if (exc.isMandatory()) {
                    LOG.debug(_parsePropChilds, exc);
                    writeResponse(contactObject, HttpServletResponse.SC_CONFLICT, getErrorMessage(exc,
                            MANDATORY_FIELD_EXCEPTION), clientId, os, xo);
                } else if (exc.isNoPermission()) {
                    LOG.debug(_parsePropChilds, exc);
                    writeResponse(contactObject, HttpServletResponse.SC_FORBIDDEN, getErrorMessage(exc,
                            PERMISSION_EXCEPTION), clientId, os, xo);
                } else if (exc.isConflict()) {
                    LOG.debug(_parsePropChilds, exc);
                    writeResponse(contactObject, HttpServletResponse.SC_CONFLICT, MODIFICATION_EXCEPTION, clientId, os, xo);
                } else if (exc.isNotFound()) {
                    LOG.debug(_parsePropChilds, exc);
                    writeResponse(contactObject, HttpServletResponse.SC_NOT_FOUND, OBJECT_NOT_FOUND_EXCEPTION,
                            clientId, os, xo);
                } else {
                    if (exc.getCategory() == Category.CATEGORY_TRUNCATED) {
                        LOG.debug(_parsePropChilds, exc);
                        writeResponse(contactObject, HttpServletResponse.SC_CONFLICT, getErrorMessage(exc,
                                USER_INPUT_EXCEPTION), clientId, os, xo);
                    } else {
                        LOG.error(_parsePropChilds, exc);
                        writeResponse(contactObject, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, getErrorMessage(exc,
                                SERVER_ERROR_EXCEPTION)
                                + exc.toString(), clientId, os, xo);
                    }
                }
            } catch (final Exception exc) {
                LOG.error(_parsePropChilds, exc);
                writeResponse(contactObject, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, getErrorMessage(
                        SERVER_ERROR_EXCEPTION, "undefinied error")
                        + exc.toString(), clientId, os, xo);
            }

        }

        public void setLastModifiedCache(final LastModifiedCache lastModifiedCache) {
            this.lastModifiedCache = lastModifiedCache;
        }
    }

    @Override
    protected void decrementRequests() {
        MonitoringInfo.decrementNumberOfConnections(MonitoringInfo.OUTLOOK);
    }

    @Override
    protected void incrementRequests() {
        MonitoringInfo.incrementNumberOfConnections(MonitoringInfo.OUTLOOK);
    }
}
