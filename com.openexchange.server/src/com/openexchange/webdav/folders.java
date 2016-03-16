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
import com.openexchange.api2.FolderSQLInterface;
import com.openexchange.api2.RdbFolderSQLInterface;
import com.openexchange.exception.Category;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.userconfiguration.UserConfigurationStorage;
import com.openexchange.login.Interface;
import com.openexchange.monitoring.MonitoringInfo;
import com.openexchange.session.Session;
import com.openexchange.tools.oxfolder.OXFolderAccess;
import com.openexchange.tools.session.ServerSessionAdapter;
import com.openexchange.webdav.tasks.QueuedTask;
import com.openexchange.webdav.xml.DataParser;
import com.openexchange.webdav.xml.FolderParser;
import com.openexchange.webdav.xml.FolderWriter;
import com.openexchange.webdav.xml.XmlServlet;
import com.openexchange.webdav.xml.fields.DataFields;

/**
 * folders
 *
 * @author <a href="mailto:sebastian.kauss@open-xchange.com">Sebastian Kauss</a>
 */

public final class folders extends XmlServlet<FolderSQLInterface> {

    private static final long serialVersionUID = 40888896545602450L;

    private static final String _invalidMethodError = "invalid method!";

    static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(folders.class);

    /**
     * Initializes a new {@link folders}
     */
    public folders() {
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
            final XmlPullParser parser, final PendingInvocations<FolderSQLInterface> pendingInvocations)
            throws XmlPullParserException, IOException, OXException {
        final Session session = getSession(req);
        if (isTag(parser, "prop", "DAV:")) {
            /*
             * Adjust parser
             */
            parser.nextTag();

            final FolderObject folderobject = new FolderObject();

            final FolderParser folderparser = new FolderParser(session);
            folderparser.parse(parser, folderobject);

            final int method = folderparser.getMethod();

            final Date lastModified = folderobject.getLastModified();
            folderobject.removeLastModified();

            final int inFolder = folderparser.getFolder();

            /*
             * Prepare folder for being queued
             */
            switch (method) {
            case DataParser.SAVE:
                if (folderobject.containsObjectID()) {
                    final int object_id = folderobject.getObjectID();

                    final Context ctx = ContextStorage.getInstance().getContext(session.getContextId());
                    if (new OXFolderAccess(ctx).isDefaultFolder(object_id)) {
                        /*
                         * No default folder rename
                         */
                        folderobject.removeFolderName();
                    }
                    /*
                     * if (object_id ==
                     * OXFolderTools.getCalendarDefaultFolder(session
                     * .getUserId(), ctx)) { folderobject.removeFolderName(); }
                     * else if (object_id ==
                     * OXFolderTools.getContactDefaultFolder
                     * (session.getUserId(), ctx)) {
                     * folderobject.removeFolderName(); } else if (object_id ==
                     * OXFolderTools.getTaskDefaultFolder(session.getUserId(),
                     * ctx)) { folderobject.removeFolderName(); }
                     */
                } else {
                    folderobject.setParentFolderID(inFolder);
                }

                pendingInvocations.add(new QueuedFolder(folderobject, folderparser.getClientID(), method, lastModified));
                break;
            case DataParser.DELETE:
                pendingInvocations.add(new QueuedFolder(folderobject, folderparser.getClientID(), method, lastModified));
                break;
            case DataParser.CLEAR:
                pendingInvocations.add(new QueuedFolder(folderobject, folderparser.getClientID(), method, lastModified));
                break;
            default:
                LOG.debug(_invalidMethodError);
            }
        } else {
            parser.next();
        }
    }

    @Override
    protected void performActions(final OutputStream os, final Session session,
            final PendingInvocations<FolderSQLInterface> pendingInvocations) throws IOException, OXException {
        final FolderSQLInterface foldersql = new RdbFolderSQLInterface(ServerSessionAdapter.valueOf(session));
        while (!pendingInvocations.isEmpty()) {
            final QueuedFolder qfld = (QueuedFolder) pendingInvocations.poll();
            if (null != qfld) {
                qfld.setLastModifiedCache(pendingInvocations.getLastModifiedCache());
                qfld.actionPerformed(foldersql, os, session.getUserId());
            }
        }
    }

    @Override
    protected void startWriter(final Session sessionObj, final Context ctx, final int objectId, final int folderId,
            final OutputStream os) throws Exception {
        new FolderWriter(sessionObj, ctx).startWriter(objectId, os);
    }

    @Override
    protected void startWriter(final Session sessionObj, final Context ctx, final int folderId, final boolean modified,
            final boolean deleted, final Date lastsync, final OutputStream os) throws Exception {
        startWriter(sessionObj, ctx, folderId, modified, deleted, false, lastsync, os);
    }

    @Override
    protected void startWriter(final Session sessionObj, final Context ctx, final int folderId, final boolean modified,
            final boolean deleted, final boolean bList, final Date lastsync, final OutputStream os) throws Exception {
        new FolderWriter(sessionObj, ctx).startWriter(modified, deleted, bList, lastsync, os);
    }

    @Override
    protected boolean hasModulePermission(final Session sessionObj, final Context ctx) {
        return UserConfigurationStorage.getInstance().getUserConfigurationSafe(sessionObj.getUserId(), ctx)
                .hasWebDAVXML();
    }

    private final class QueuedFolder implements QueuedAction<FolderSQLInterface> {

        private final FolderObject folderObject;

        private final String clientId;

        private final int action;

        private final Date lastModified;

        private LastModifiedCache lastModifiedCache;

        /**
         * Initializes a new {@link QueuedTask}
         *
         * @param folderObject The folder object
         * @param clientId The client ID
         * @param action The desired action
         * @param lastModified The last-modified date
         * @param inFolder The contact's folder
         */
        public QueuedFolder(final FolderObject folderObject, final String clientId, final int action,
                final Date lastModified) {
            super();
            this.folderObject = folderObject;
            this.clientId = clientId;
            this.action = action;
            this.lastModified = lastModified;
            this.lastModifiedCache = new LastModifiedCache();
        }

        @Override
        public void actionPerformed(final FolderSQLInterface foldersSQL, final OutputStream os, final int user)
                throws IOException {

            final XMLOutputter xo = new XMLOutputter();

            try {
                switch (action) {
                case DataParser.SAVE:
                    if (folderObject.getModule() == FolderObject.UNBOUND) {
                        writeResponse(folderObject, HttpServletResponse.SC_CONFLICT, USER_INPUT_EXCEPTION, clientId, os, xo);
                        return;
                    }

                    final Date currentLastModified = lastModifiedCache.getLastModified(folderObject.getObjectID(), lastModified);
                    lastModifiedCache.update(folderObject.getObjectID(), 0, lastModified);
                    foldersSQL.saveFolderObject(folderObject, currentLastModified);
                    lastModifiedCache.update(folderObject.getObjectID(), 0, folderObject.getLastModified());
                    break;
                case DataParser.DELETE:
                    if (lastModified == null) {
                        throw WebdavExceptionCode.MISSING_FIELD.create(DataFields.LAST_MODIFIED);
                    }

                    foldersSQL.deleteFolderObject(folderObject, lastModified);
                    break;
                case DataParser.CLEAR:
                    if (lastModified == null) {
                        throw WebdavExceptionCode.MISSING_FIELD.create(DataFields.LAST_MODIFIED);
                    }

                    foldersSQL.clearFolder(folderObject, lastModified);
                    break;
                default:
                    throw WebdavExceptionCode.INVALID_ACTION.create(Integer.valueOf(action));
                }

                writeResponse(folderObject, HttpServletResponse.SC_OK, OK, clientId, os, xo);
            } catch (final OXException exc) {
                if (exc.isMandatory()) {
                    LOG.debug(_parsePropChilds, exc);
                    writeResponse(folderObject, HttpServletResponse.SC_CONFLICT, getErrorMessage(exc,
                            MANDATORY_FIELD_EXCEPTION), clientId, os, xo);
                } else if (exc.isNoPermission()) {
                    LOG.debug(_parsePropChilds, exc);
                    writeResponse(folderObject, HttpServletResponse.SC_FORBIDDEN, getErrorMessage(exc,
                            PERMISSION_EXCEPTION), clientId, os, xo);
                } else if (exc.isConflict()) {
                    LOG.debug(_parsePropChilds, exc);
                    writeResponse(folderObject, HttpServletResponse.SC_CONFLICT, getErrorMessage(exc,
                            CONFLICT_EXCEPTION), clientId, os, xo);
                } else if (exc.isNotFound()) {
                    LOG.debug(_parsePropChilds, exc);
                    writeResponse(folderObject, HttpServletResponse.SC_NOT_FOUND, OBJECT_NOT_FOUND_EXCEPTION,
                            clientId, os, xo);
                } else {
                    if (exc.getCategory() == Category.CATEGORY_TRUNCATED) {
                        LOG.debug(_parsePropChilds, exc);
                        writeResponse(folderObject, HttpServletResponse.SC_CONFLICT, getErrorMessage(exc,
                                USER_INPUT_EXCEPTION), clientId, os, xo);
                    } else {
                        LOG.error(_parsePropChilds, exc);
                        writeResponse(folderObject, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, getErrorMessage(exc,
                                SERVER_ERROR_EXCEPTION)
                                + exc.toString(), clientId, os, xo);
                    }
                }
            } catch (final Exception exc) {
                LOG.error(_parsePropChilds, exc);
                writeResponse(folderObject, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, getErrorMessage(
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
