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
import com.openexchange.groupware.userconfiguration.UserConfigurationStorage;
import com.openexchange.session.Session;
import com.openexchange.tools.oxfolder.OXFolderTools;
import com.openexchange.webdav.xml.DataParser;
import com.openexchange.webdav.xml.FolderParser;
import com.openexchange.webdav.xml.FolderWriter;
import com.openexchange.webdav.xml.XmlServlet;

/**
 * folders
 * @author <a href="mailto:sebastian.kauss@netline-is.de">Sebastian Kauss</a>
 */

public final class folders extends XmlServlet {
	
	private static final String _invalidMethodError = "invalid method!";
	
	private static final Log LOG = LogFactory.getLog(folders.class);
	
	protected void parsePropChilds(final HttpServletRequest req, final HttpServletResponse resp, final XmlPullParser parser) throws Exception {
		final OutputStream os = resp.getOutputStream();
		
		final Session sessionObj = getSession(req);
		
		final XMLOutputter xo = new XMLOutputter();
		
		final FolderParser folderparser = new FolderParser(sessionObj);
		final FolderSQLInterface foldersql = new RdbFolderSQLInterface(sessionObj);
		
		if (isTag(parser, "prop", "DAV:")) {
			String client_id = null;
			
			int method = 0;
			
			FolderObject folderobject = null;
			
			try {
				folderobject = new FolderObject();
				parser.nextTag();
				folderparser.parse(parser, folderobject);
				
				method = folderparser.getMethod();
				
				final Date lastModified = folderobject.getLastModified();
				folderobject.removeLastModified();
				
				final int inFolder = folderparser.getFolder();
				client_id = folderparser.getClientID();
				
				switch (method) {
					case DataParser.SAVE:
						if (folderobject.containsObjectID()) {
							final int object_id = folderobject.getObjectID();
							
							if (object_id == OXFolderTools.getCalendarDefaultFolder(sessionObj.getUserId(), sessionObj.getContext())) {
								folderobject.removeFolderName();
							} else if (object_id == OXFolderTools.getContactDefaultFolder(sessionObj.getUserId(), sessionObj.getContext())) {
								folderobject.removeFolderName();
							} else if (object_id == OXFolderTools.getTaskDefaultFolder(sessionObj.getUserId(), sessionObj.getContext())) {
								folderobject.removeFolderName();
							}
						} else {
							folderobject.setParentFolderID(inFolder);
						}
						
						if (folderobject.getModule() == folderobject.UNBOUND) {
							writeResponse(folderobject, HttpServletResponse.SC_CONFLICT, USER_INPUT_EXCEPTION, client_id, os, xo);
							return;
						}
						
						folderobject = foldersql.saveFolderObject(folderobject, lastModified);
						break;
					case DataParser.DELETE:
						if (lastModified == null) {
							throw new OXMandatoryFieldException("missing field last_modified");
						}
						
						foldersql.deleteFolderObject(folderobject, lastModified);
						break;
					default:
						throw new OXConflictException(_invalidMethodError);
				}
				
				writeResponse(folderobject, HttpServletResponse.SC_OK, OK, client_id, os, xo);
			} catch (OXMandatoryFieldException exc) {
				LOG.debug(_parsePropChilds, exc);
				writeResponse(folderobject, HttpServletResponse.SC_CONFLICT, MANDATORY_FIELD_EXCEPTION, client_id, os, xo);
			} catch (OXPermissionException exc) {
				LOG.debug(_parsePropChilds, exc);
				writeResponse(folderobject, HttpServletResponse.SC_FORBIDDEN, PERMISSION_EXCEPTION, client_id, os, xo);
			} catch (OXConflictException exc) {
				LOG.debug(_parsePropChilds, exc);
				writeResponse(folderobject, HttpServletResponse.SC_CONFLICT, CONFLICT_EXCEPTION, client_id, os, xo);
			} catch (OXObjectNotFoundException exc) {
				LOG.debug(_parsePropChilds, exc);
				writeResponse(folderobject, HttpServletResponse.SC_NOT_FOUND, OBJECT_NOT_FOUND_EXCEPTION, client_id, os, xo);
			} catch (OXConcurrentModificationException exc) {
				LOG.debug(_parsePropChilds, exc);
				writeResponse(folderobject, HttpServletResponse.SC_CONFLICT, MODIFICATION_EXCEPTION, client_id, os, xo);
			} catch (XmlPullParserException exc) {
				LOG.debug(_parsePropChilds, exc);
				writeResponse(folderobject, HttpServletResponse.SC_BAD_REQUEST, BAD_REQUEST_EXCEPTION, client_id, os, xo);
			} catch (Exception exc) {
				LOG.error(_parsePropChilds, exc);
				writeResponse(folderobject, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, SERVER_ERROR_EXCEPTION + exc.toString(), client_id, os, xo);
			}
		} else {
			parser.next();
		}
	}
	
	@Override
	protected void startWriter(final Session sessionObj, final int objectId, final int folderId, final OutputStream os) throws Exception {
		final FolderWriter folderwriter = new FolderWriter(sessionObj);
		folderwriter.startWriter(objectId, os);
	}

	@Override
	protected void startWriter(final Session sessionObj, final int folderId, final boolean modified, final boolean deleted, final Date lastsync, final OutputStream os) throws Exception {
		startWriter(sessionObj, folderId, modified, deleted, false, lastsync, os);
	}
	
	@Override
	protected void startWriter(final Session sessionObj, final int folderId, final boolean modified, final boolean deleted, final boolean bList, final Date lastsync, final OutputStream os) throws Exception {
		final FolderWriter folderwriter = new FolderWriter(sessionObj);
		folderwriter.startWriter(modified, deleted, bList, lastsync, os);
	}
	
	@Override
	protected boolean hasModulePermission(final Session sessionObj) {
		return UserConfigurationStorage.getInstance().getUserConfigurationSafe(sessionObj.getUserId(), sessionObj.getContext()).hasWebDAVXML();
	}
}




