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
import com.openexchange.ajax.fields.DataFields;
import com.openexchange.api2.TasksSQLInterface;
import com.openexchange.exception.Category;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.groupware.tasks.TasksSQLImpl;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.groupware.userconfiguration.UserConfigurationStorage;
import com.openexchange.login.Interface;
import com.openexchange.monitoring.MonitoringInfo;
import com.openexchange.session.Session;
import com.openexchange.webdav.xml.DataParser;
import com.openexchange.webdav.xml.TaskParser;
import com.openexchange.webdav.xml.TaskWriter;
import com.openexchange.webdav.xml.XmlServlet;

/**
 * {@link tasks} - The WebDAV/XML servlet for task module.
 *
 * @author <a href="mailto:sebastian.kauss@open-xchange.com">Sebastian Kauss</a>
 */
public final class tasks extends XmlServlet<TasksSQLInterface> {

    private static final long serialVersionUID = 1750720959626156342L;

    static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(tasks.class);

    public tasks() {
        super();
    }

    @Override
    protected Interface getInterface() {
        return Interface.WEBDAV_ICAL;
    }

    @Override
    protected boolean isServletDisabled() {
        return true;
    }

    @Override
    protected void parsePropChilds(final HttpServletRequest req, final HttpServletResponse resp, final XmlPullParser parser, final PendingInvocations<TasksSQLInterface> pendingInvocations) throws OXException, XmlPullParserException, IOException {
        final Session session = getSession(req);
        if (isTag(parser, "prop", "DAV:")) {
            /*
             * Adjust parser
             */
            parser.nextTag();

            final Task task = new Task();

            final TaskParser taskparser = new TaskParser(session);
            taskparser.parse(parser, task);

            final int method = taskparser.getMethod();

            final Date lastModified = task.getLastModified();
            task.removeLastModified();

            final int inFolder = taskparser.getFolder();

            /*
             * Prepare task for being queued
             */
            switch (method) {
            case DataParser.SAVE:
                if (task.containsObjectID()) {
                    if (!task.getAlarmFlag()) {
                        task.setAlarm(null);
                    }
                    pendingInvocations.add(new QueuedTask(task, taskparser.getClientID(), DataParser.SAVE, lastModified, inFolder));
                } else {
                    if (!task.getAlarmFlag()) {
                        task.removeAlarm();
                    }
                    task.setParentFolderID(inFolder);
                    pendingInvocations.add(new QueuedTask(task, taskparser.getClientID(), DataParser.SAVE, lastModified, inFolder));
                }
                break;
            case DataParser.DELETE:
                pendingInvocations.add(new QueuedTask(task, taskparser.getClientID(), DataParser.DELETE, lastModified, inFolder));
                break;
            case DataParser.CONFIRM:
                pendingInvocations.add(new QueuedTask(task, taskparser.getClientID(), DataParser.CONFIRM, lastModified, inFolder));
                break;
            default:
                LOG.debug("invalid method: {}", method);
            }
        } else {
            parser.next();
        }
    }

    @Override
    protected void performActions(final OutputStream os, final Session session, final PendingInvocations<TasksSQLInterface> pendingInvocations) throws IOException {
        final TasksSQLInterface tasksql = new TasksSQLImpl(session);
        while (!pendingInvocations.isEmpty()) {
            final QueuedTask qtask = (QueuedTask) pendingInvocations.poll();
            if (null != qtask) {
                qtask.setLastModifiedCache(pendingInvocations.getLastModifiedCache());
                qtask.actionPerformed(tasksql, os, session.getUserId());
            }
        }
    }

    @Override
    protected void startWriter(final Session sessionObj, final Context ctx, final int objectId, final int folderId, final OutputStream os) throws Exception {
        final User userObj = UserStorage.getInstance().getUser(sessionObj.getUserId(), ctx);
        final TaskWriter taskwriter = new TaskWriter(userObj, ctx, sessionObj);
        taskwriter.startWriter(objectId, folderId, os);
    }

    @Override
    protected void startWriter(final Session sessionObj, final Context ctx, final int folderId, final boolean bModified, final boolean bDelete, final Date lastsync, final OutputStream os) throws Exception {
        startWriter(sessionObj, ctx, folderId, bModified, bDelete, false, lastsync, os);
    }

    @Override
    protected void startWriter(final Session sessionObj, final Context ctx, final int folderId, final boolean bModified, final boolean bDelete, final boolean bList, final Date lastsync, final OutputStream os) throws Exception {
        final User userObj = UserStorage.getInstance().getUser(sessionObj.getUserId(), ctx);
        final TaskWriter taskwriter = new TaskWriter(userObj, ctx, sessionObj);
        taskwriter.startWriter(bModified, bDelete, bList, folderId, lastsync, os);
    }

    @Override
    protected boolean hasModulePermission(final Session sessionObj, final Context ctx) {
        final UserConfiguration uc = UserConfigurationStorage.getInstance().getUserConfigurationSafe(sessionObj.getUserId(), ctx);
        return (uc.hasWebDAVXML() && uc.hasTask());
    }

    public final class QueuedTask implements QueuedAction<TasksSQLInterface> {

        private final Task task;

        private final String clientId;

        private final int action;

        private final Date lastModified;

        private final int inFolder;

        private LastModifiedCache lastModifiedCache;

        /**
         * Initializes a new {@link QueuedTask}
         *
         * @param task The task object
         * @param clientId The client ID
         * @param confirm The confirm status
         * @param action The desired action
         * @param lastModified The last-modified date
         * @param inFolder The task's folder
         */
        public QueuedTask(final Task task, final String clientId, final int action, final Date lastModified, final int inFolder) {
            super();
            this.task = task;
            this.clientId = clientId;
            this.action = action;
            this.lastModified = lastModified;
            this.inFolder = inFolder;
            this.lastModifiedCache = new LastModifiedCache();
        }

        /**
         * Performs this queued task's action
         *
         * @param tasksSQL The task SQL interface
         * @param os The output stream
         * @param user The user ID
         * @throws IOException If writing response fails
         */
        @Override
        public void actionPerformed(final TasksSQLInterface tasksSQL, final OutputStream os, final int user) throws IOException {
            final XMLOutputter xo = new XMLOutputter();
            try {
                if (action == DataParser.SAVE) {
                    if (task.containsObjectID()) {
                        if (lastModified == null) {
                            throw WebdavExceptionCode.MISSING_FIELD.create(DataFields.LAST_MODIFIED);
                        }
                        final Date currentLastModified = lastModifiedCache.getLastModified(task.getObjectID(), lastModified);
                        lastModifiedCache.update(task.getObjectID(), 0, lastModified);
                        tasksSQL.updateTaskObject(task, inFolder, currentLastModified);
                        lastModifiedCache.update(task.getObjectID(), 0, task.getLastModified());
                    } else {
                        tasksSQL.insertTaskObject(task);
                        lastModifiedCache.update(task.getObjectID(), 0, task.getLastModified());
                    }
                } else if (action == DataParser.DELETE) {
                    if (lastModified == null) {
                        throw WebdavExceptionCode.MISSING_FIELD.create(DataFields.LAST_MODIFIED);
                    }
                    tasksSQL.deleteTaskObject(task.getObjectID(), inFolder, lastModified);
                } else if (action == DataParser.CONFIRM) {
                    tasksSQL.setUserConfirmation(task.getObjectID(), user, task.getConfirm(), task.getConfirmMessage());
                } else {
                    throw WebdavExceptionCode.INVALID_ACTION.create(Integer.valueOf(action));
                }
                writeResponse(task, HttpServletResponse.SC_OK, OK, clientId, os, xo);
            } catch (final OXException exc) {
                if (exc.isMandatory()) {
                    LOG.debug(_parsePropChilds, exc);
                    writeResponse(task, HttpServletResponse.SC_CONFLICT, getErrorMessage(exc,
                            MANDATORY_FIELD_EXCEPTION), clientId, os, xo);
                } else if (exc.isNoPermission()) {
                    LOG.debug(_parsePropChilds, exc);
                    writeResponse(task, HttpServletResponse.SC_FORBIDDEN, getErrorMessage(exc,
                            PERMISSION_EXCEPTION), clientId, os, xo);
                } else if (exc.isConflict() || exc.getCategories().contains(Category.CATEGORY_CONFLICT)) {
                    LOG.debug(_parsePropChilds, exc);
                    writeResponse(task, HttpServletResponse.SC_CONFLICT, MODIFICATION_EXCEPTION, clientId, os, xo);
                } else if (exc.isNotFound()) {
                    LOG.debug(_parsePropChilds, exc);
                    writeResponse(task, HttpServletResponse.SC_NOT_FOUND, OBJECT_NOT_FOUND_EXCEPTION,
                            clientId, os, xo);
                } else {
                    if (exc.getCategory() == Category.CATEGORY_TRUNCATED) {
                        LOG.debug(_parsePropChilds, exc);
                        writeResponse(task, HttpServletResponse.SC_CONFLICT, getErrorMessage(exc,
                                USER_INPUT_EXCEPTION), clientId, os, xo);
                    } else {
                        LOG.error(_parsePropChilds, exc);
                        writeResponse(task, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, getErrorMessage(exc,
                                SERVER_ERROR_EXCEPTION)
                                + exc.toString(), clientId, os, xo);
                    }
                }
            } catch (final Exception e) {
                LOG.error(_parsePropChilds, e);
                writeResponse(task, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, getErrorMessage(
                    SERVER_ERROR_EXCEPTION,
                    "undefinied error") + e.toString(), clientId, os, xo);
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
