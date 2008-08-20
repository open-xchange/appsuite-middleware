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
import com.openexchange.api2.FolderSQLInterface;
import com.openexchange.api2.OXConcurrentModificationException;
import com.openexchange.api2.RdbFolderSQLInterface;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.userconfiguration.UserConfigurationStorage;
import com.openexchange.session.Session;
import com.openexchange.tools.oxfolder.OXFolderAccess;
import com.openexchange.webdav.xml.DataParser;
import com.openexchange.webdav.xml.FolderParser;
import com.openexchange.webdav.xml.FolderWriter;
import com.openexchange.webdav.xml.XmlServlet;

/**
 * folders
 * 
 * @author <a href="mailto:sebastian.kauss@netline-is.de">Sebastian Kauss</a>
 */

public final class folders extends XmlServlet {

	private static final long serialVersionUID = 40888896545602450L;

	private static final String _invalidMethodError = "invalid method!";

	private static final transient Log LOG = LogFactory.getLog(folders.class);

	private final Queue<QueuedFolder> pendingInvocations;

	/**
	 * Initializes a new {@link folders}
	 */
	public folders() {
		super();
		pendingInvocations = new LinkedList<QueuedFolder>();
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
					if (object_id == OXFolderTools.getCalendarDefaultFolder(session.getUserId(), ctx)) {
						folderobject.removeFolderName();
					} else if (object_id == OXFolderTools.getContactDefaultFolder(session.getUserId(), ctx)) {
						folderobject.removeFolderName();
					} else if (object_id == OXFolderTools.getTaskDefaultFolder(session.getUserId(), ctx)) {
						folderobject.removeFolderName();
					}
					*/
				} else {
					folderobject.setParentFolderID(inFolder);
				}

				pendingInvocations.add(new QueuedFolder(folderobject, folderparser, method, lastModified, inFolder));
				break;
			case DataParser.DELETE:
				pendingInvocations.add(new QueuedFolder(folderobject, folderparser, method, lastModified, inFolder));
				break;
			default:
				if (LOG.isDebugEnabled()) {
					LOG.debug(_invalidMethodError);
				}
			}
		} else {
			parser.next();
		}
	}

	@Override
	protected void performActions(final OutputStream os, final Session session) throws Exception {
		final FolderSQLInterface foldersql = new RdbFolderSQLInterface(session, ContextStorage.getInstance()
				.getContext(session.getContextId()));
		while (!pendingInvocations.isEmpty()) {
			final QueuedFolder qfld = pendingInvocations.poll();
			if (null != qfld) {
				qfld.actionPerformed(foldersql, os, session.getUserId());
			}
		}
	}

	@Override
	protected void startWriter(final Session sessionObj, final Context ctx, final int objectId, final int folderId,
			final OutputStream os) throws Exception {
		final FolderWriter folderwriter = new FolderWriter(sessionObj, ctx);
		folderwriter.startWriter(objectId, os);
	}

	@Override
	protected void startWriter(final Session sessionObj, final Context ctx, final int folderId, final boolean modified,
			final boolean deleted, final Date lastsync, final OutputStream os) throws Exception {
		startWriter(sessionObj, ctx, folderId, modified, deleted, false, lastsync, os);
	}

	@Override
	protected void startWriter(final Session sessionObj, final Context ctx, final int folderId, final boolean modified,
			final boolean deleted, final boolean bList, final Date lastsync, final OutputStream os) throws Exception {
		final FolderWriter folderwriter = new FolderWriter(sessionObj, ctx);
		folderwriter.startWriter(modified, deleted, bList, lastsync, os);
	}

	@Override
	protected boolean hasModulePermission(final Session sessionObj, final Context ctx) {
		return UserConfigurationStorage.getInstance().getUserConfigurationSafe(sessionObj.getUserId(), ctx)
				.hasWebDAVXML();
	}

	private final class QueuedFolder {

		private final FolderObject folderObject;

		private final FolderParser folderParser;

		private final int action;

		private final Date lastModified;

		private final int inFolder;

		/**
		 * Initializes a new {@link QueuedTask}
		 * 
		 * @param folderObject
		 *            The folder object
		 * @param folderParser
		 *            The folder's parser
		 * @param action
		 *            The desired action
		 * @param lastModified
		 *            The last-modified date
		 * @param inFolder
		 *            The contact's folder
		 */
		public QueuedFolder(final FolderObject folderObject, final FolderParser folderParser, final int action,
				final Date lastModified, final int inFolder) {
			super();
			this.folderObject = folderObject;
			this.folderParser = folderParser;
			this.action = action;
			this.lastModified = lastModified;
			this.inFolder = inFolder;
		}

		public void actionPerformed(final FolderSQLInterface foldersSQL, final OutputStream os, final int user)
				throws Exception {

			final XMLOutputter xo = new XMLOutputter();
			final String client_id = folderParser.getClientID();

			try {
				switch (action) {
				case DataParser.SAVE:
					if (folderObject.getModule() == FolderObject.UNBOUND) {
						writeResponse(folderObject, HttpServletResponse.SC_CONFLICT, USER_INPUT_EXCEPTION, client_id,
								os, xo);
						return;
					}

					/* folderObject = */
					foldersSQL.saveFolderObject(folderObject, lastModified);
					break;
				case DataParser.DELETE:
					if (lastModified == null) {
						throw new OXMandatoryFieldException("missing field last_modified");
					}

					foldersSQL.deleteFolderObject(folderObject, lastModified);
					break;
				default:
					throw new OXConflictException(_invalidMethodError);
				}

				writeResponse(folderObject, HttpServletResponse.SC_OK, OK, client_id, os, xo);
			} catch (final OXMandatoryFieldException exc) {
				LOG.debug(_parsePropChilds, exc);
				writeResponse(folderObject, HttpServletResponse.SC_CONFLICT, getErrorMessage(exc,
						MANDATORY_FIELD_EXCEPTION), client_id, os, xo);
			} catch (final OXPermissionException exc) {
				LOG.debug(_parsePropChilds, exc);
				writeResponse(folderObject, HttpServletResponse.SC_FORBIDDEN,
						getErrorMessage(exc, PERMISSION_EXCEPTION), client_id, os, xo);
			} catch (final OXConflictException exc) {
				LOG.debug(_parsePropChilds, exc);
				writeResponse(folderObject, HttpServletResponse.SC_CONFLICT, getErrorMessage(exc, CONFLICT_EXCEPTION),
						client_id, os, xo);
			} catch (final OXObjectNotFoundException exc) {
				LOG.debug(_parsePropChilds, exc);
				writeResponse(folderObject, HttpServletResponse.SC_NOT_FOUND, OBJECT_NOT_FOUND_EXCEPTION, client_id,
						os, xo);
			} catch (final OXConcurrentModificationException exc) {
				LOG.debug(_parsePropChilds, exc);
				writeResponse(folderObject, HttpServletResponse.SC_CONFLICT, MODIFICATION_EXCEPTION, client_id, os, xo);
			} catch (final XmlPullParserException exc) {
				LOG.debug(_parsePropChilds, exc);
				writeResponse(folderObject, HttpServletResponse.SC_BAD_REQUEST, BAD_REQUEST_EXCEPTION, client_id, os,
						xo);
			} catch (final Exception exc) {
				LOG.error(_parsePropChilds, exc);
				writeResponse(folderObject, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, getErrorMessage(
						SERVER_ERROR_EXCEPTION, "undefinied error")
						+ exc.toString(), client_id, os, xo);
			}
		}

	}
}
