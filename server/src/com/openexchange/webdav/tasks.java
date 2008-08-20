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

package com.openexchange.webdav;

import java.io.OutputStream;
import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.output.XMLOutputter;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import com.openexchange.api.OXConflictException;
import com.openexchange.api.OXMandatoryFieldException;
import com.openexchange.api.OXObjectNotFoundException;
import com.openexchange.api.OXPermissionException;
import com.openexchange.api2.OXConcurrentModificationException;
import com.openexchange.api2.OXException;
import com.openexchange.api2.TasksSQLInterface;
import com.openexchange.groupware.AbstractOXException.Category;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.groupware.tasks.TasksSQLInterfaceImpl;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.groupware.userconfiguration.UserConfigurationStorage;
import com.openexchange.session.Session;
import com.openexchange.webdav.xml.DataParser;
import com.openexchange.webdav.xml.TaskParser;
import com.openexchange.webdav.xml.TaskWriter;
import com.openexchange.webdav.xml.XmlServlet;

/**
 * tasks
 * 
 * @author <a href="mailto:sebastian.kauss@netline-is.de">Sebastian Kauss</a>
 */

public final class tasks extends XmlServlet {

	private static final long serialVersionUID = 1750720959626156342L;

	private static final transient Log LOG = LogFactory.getLog(tasks.class);

	private final Queue<QueuedTask> pendingInvocations;

	/**
	 * Initializes a new {@link tasks}
	 */
	public tasks() {
		super();
		pendingInvocations = new LinkedList<QueuedTask>();
	}

	@Override
	protected void parsePropChilds(final HttpServletRequest req, final HttpServletResponse resp,
			final XmlPullParser parser) throws Exception {
		final Session session = getSession(req);
		if (isTag(parser, "prop", "DAV:")) {
			/*
			 * Adjust parser
			 */
			parser.nextTag();

			final Task taskobject = new Task();

			final TaskParser taskparser = new TaskParser(session);
			taskparser.parse(parser, taskobject);

			final int method = taskparser.getMethod();

			final Date lastModified = taskobject.getLastModified();
			taskobject.removeLastModified();

			final int inFolder = taskparser.getFolder();

			/*
			 * Prepare task for being queued
			 */
			switch (method) {
			case DataParser.SAVE:
				if (taskobject.containsObjectID()) {
					if (!taskobject.getAlarmFlag()) {
						taskobject.setAlarm(null);
					}

					pendingInvocations.add(new QueuedTask(taskobject, taskparser, DataParser.SAVE, lastModified,
							inFolder));
				} else {
					if (!taskobject.getAlarmFlag()) {
						taskobject.removeAlarm();
					}

					taskobject.setParentFolderID(inFolder);

					pendingInvocations.add(new QueuedTask(taskobject, taskparser, DataParser.SAVE, lastModified,
							inFolder));
				}
				break;
			case DataParser.DELETE:
				pendingInvocations
						.add(new QueuedTask(taskobject, taskparser, DataParser.DELETE, lastModified, inFolder));
				break;
			case DataParser.CONFIRM:
				pendingInvocations.add(new QueuedTask(taskobject, taskparser, DataParser.CONFIRM, lastModified,
						inFolder));
				break;
			default:
				if (LOG.isDebugEnabled()) {
					LOG.debug("invalid method: " + method);
				}
			}
		} else {
			parser.next();
		}
	}

	@Override
	protected void performActions(final OutputStream os, final Session session) throws Exception {
		final TasksSQLInterface tasksql = new TasksSQLInterfaceImpl(session);
		while (!pendingInvocations.isEmpty()) {
			final QueuedTask qtask = pendingInvocations.poll();
			if (null != qtask) {
				qtask.actionPerformed(tasksql, os, session.getUserId());
			}
		}
	}

	@Override
	protected void startWriter(final Session sessionObj, final Context ctx, final int objectId, final int folderId,
			final OutputStream os) throws Exception {
		final User userObj = UserStorage.getStorageUser(sessionObj.getUserId(), ctx);
		final TaskWriter taskwriter = new TaskWriter(userObj, ctx, sessionObj);
		taskwriter.startWriter(objectId, folderId, os);
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
		final User userObj = UserStorage.getStorageUser(sessionObj.getUserId(), ctx);
		final TaskWriter taskwriter = new TaskWriter(userObj, ctx, sessionObj);
		taskwriter.startWriter(bModified, bDelete, bList, folderId, lastsync, os);
	}

	@Override
	protected boolean hasModulePermission(final Session sessionObj, final Context ctx) {
		final UserConfiguration uc = UserConfigurationStorage.getInstance().getUserConfigurationSafe(
				sessionObj.getUserId(), ctx);
		return (uc.hasWebDAVXML() && uc.hasTask());
	}

	private final class QueuedTask {

		private final Task task;

		private final TaskParser taskParser;

		private final int action;

		private final Date lastModified;

		private final int inFolder;

		/**
		 * Initializes a new {@link QueuedTask}
		 * 
		 * @param task
		 *            The task object
		 * @param taskParser
		 *            The task's parser
		 * @param action
		 *            The desired action
		 * @param lastModified
		 *            The last-modified date
		 * @param inFolder
		 *            The task's folder
		 */
		public QueuedTask(final Task task, final TaskParser taskParser, final int action, final Date lastModified,
				final int inFolder) {
			super();
			this.task = task;
			this.taskParser = taskParser;
			this.action = action;
			this.lastModified = lastModified;
			this.inFolder = inFolder;
		}

		/**
		 * Performs this queued task's action
		 * 
		 * @param tasksSQL
		 *            The task SQL interface
		 * @param os
		 *            The output stream
		 * @param user
		 *            The user ID
		 * @throws Exception
		 *             If task's action fails
		 */
		public void actionPerformed(final TasksSQLInterface tasksSQL, final OutputStream os, final int user)
				throws Exception {

			final String client_id = taskParser.getClientID();
			final XMLOutputter xo = new XMLOutputter();
			try {
				if (action == DataParser.SAVE) {
					if (task.containsObjectID()) {
						if (lastModified == null) {
							throw new OXMandatoryFieldException("missing field last_modified");
						}
						tasksSQL.updateTaskObject(task, inFolder, lastModified);
					} else {
						tasksSQL.insertTaskObject(task);
					}
				} else if (action == DataParser.DELETE) {
					if (lastModified == null) {
						throw new OXMandatoryFieldException("missing field last_modified");
					}
					tasksSQL.deleteTaskObject(task.getObjectID(), inFolder, lastModified);
				} else if (action == DataParser.CONFIRM) {
					tasksSQL.setUserConfirmation(task.getObjectID(), user, taskParser.getConfirm(), null);
				} else {
					throw new OXConflictException("invalid method: " + action);
				}
				writeResponse(task, HttpServletResponse.SC_OK, OK, client_id, os, xo);
			} catch (final OXMandatoryFieldException exc) {
				LOG.debug(_parsePropChilds, exc);
				writeResponse(task, HttpServletResponse.SC_CONFLICT, getErrorMessage(exc, MANDATORY_FIELD_EXCEPTION),
						client_id, os, xo);
			} catch (final OXPermissionException exc) {
				LOG.debug(_parsePropChilds, exc);
				writeResponse(task, HttpServletResponse.SC_FORBIDDEN, getErrorMessage(exc, PERMISSION_EXCEPTION),
						client_id, os, xo);
			} catch (final OXConflictException exc) {
				LOG.debug(_parsePropChilds, exc);
				writeResponse(task, HttpServletResponse.SC_CONFLICT, getErrorMessage(exc, CONFLICT_EXCEPTION),
						client_id, os, xo);
			} catch (final OXObjectNotFoundException exc) {
				LOG.debug(_parsePropChilds, exc);
				writeResponse(task, HttpServletResponse.SC_NOT_FOUND, OBJECT_NOT_FOUND_EXCEPTION, client_id, os, xo);
			} catch (final OXConcurrentModificationException exc) {
				LOG.debug(_parsePropChilds, exc);
				writeResponse(task, HttpServletResponse.SC_CONFLICT, MODIFICATION_EXCEPTION, client_id, os, xo);
			} catch (final XmlPullParserException exc) {
				LOG.debug(_parsePropChilds, exc);
				writeResponse(task, HttpServletResponse.SC_BAD_REQUEST, BAD_REQUEST_EXCEPTION, client_id, os, xo);
			} catch (final OXException exc) {
				if (exc.getCategory() == Category.USER_INPUT) {
					LOG.debug(_parsePropChilds, exc);
					writeResponse(task, HttpServletResponse.SC_CONFLICT, getErrorMessage(exc, USER_INPUT_EXCEPTION),
							client_id, os, xo);
				} else {
					LOG.error(_parsePropChilds, exc);
					writeResponse(task, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, getErrorMessage(exc,
							SERVER_ERROR_EXCEPTION)
							+ exc.toString(), client_id, os, xo);
				}
			} catch (final Exception exc) {
				LOG.error(_parsePropChilds, exc);
				writeResponse(task, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, getErrorMessage(
						SERVER_ERROR_EXCEPTION, "undefinied error")
						+ exc.toString(), client_id, os, xo);
			}
		}

	}
}
