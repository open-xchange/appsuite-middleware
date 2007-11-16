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
import com.openexchange.api2.ContactSQLInterface;
import com.openexchange.api2.OXConcurrentModificationException;
import com.openexchange.api2.OXException;
import com.openexchange.api2.RdbContactSQLInterface;
import com.openexchange.groupware.AbstractOXException.Category;
import com.openexchange.groupware.container.ContactObject;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.groupware.userconfiguration.UserConfigurationStorage;
import com.openexchange.sessiond.Session;
import com.openexchange.webdav.xml.ContactParser;
import com.openexchange.webdav.xml.ContactWriter;
import com.openexchange.webdav.xml.DataParser;
import com.openexchange.webdav.xml.XmlServlet;

/**
 * contacts
 * @author <a href="mailto:sebastian.kauss@netline-is.de">Sebastian Kauss</a>
 */
public final class contacts extends XmlServlet {
	
	private static final Log LOG = LogFactory.getLog(contacts.class);
	
	protected void parsePropChilds(final HttpServletRequest req, final HttpServletResponse resp, final XmlPullParser parser) throws Exception {
		final OutputStream os = resp.getOutputStream();
		
		final Session sessionObj = getSession(req);
		
		final XMLOutputter xo = new XMLOutputter();
		
		final ContactParser contactparser = new ContactParser(sessionObj);
		final ContactSQLInterface contactsql = new RdbContactSQLInterface(sessionObj);
		
		if (isTag(parser, "prop", "DAV:")) {
			String client_id = null;
			
			int method = 0;
			
			ContactObject contactobject = null;
			
			try {
				contactobject = new ContactObject();
				parser.nextTag();
				contactparser.parse(parser, contactobject);
				
				method = contactparser.getMethod();
				
				final Date lastModified = contactobject.getLastModified();
				contactobject.removeLastModified();
				
				final int inFolder = contactparser.getFolder();
				client_id = contactparser.getClientID();
				
				switch (method) {
					case DataParser.SAVE:
						if (contactobject.containsObjectID()) {
							if (lastModified == null) {
								throw new OXMandatoryFieldException("missing field last_modified");
							}
							
							contactsql.updateContactObject(contactobject, inFolder, lastModified);
						} else {
							contactobject.setParentFolderID(inFolder);
							
							if (contactobject.containsImage1() && contactobject.getImage1() == null) {
								contactobject.removeImage1();
							}
							
							contactsql.insertContactObject(contactobject);
						}
						break;
					case DataParser.DELETE:
						if (lastModified == null) {
							throw new OXMandatoryFieldException("missing field last_modified");
						}
						
						contactsql.deleteContactObject(contactobject.getObjectID(), inFolder, lastModified);
						break;
					default:
						throw new OXConflictException("invalid method: " + method);
				}
				
				writeResponse(contactobject, HttpServletResponse.SC_OK, OK, client_id, os, xo);
			} catch (OXMandatoryFieldException exc) {
				LOG.debug(_parsePropChilds, exc);
				writeResponse(contactobject, HttpServletResponse.SC_CONFLICT, MANDATORY_FIELD_EXCEPTION, client_id, os, xo);
			} catch (OXPermissionException exc) {
				LOG.debug(_parsePropChilds, exc);
				writeResponse(contactobject, HttpServletResponse.SC_FORBIDDEN, PERMISSION_EXCEPTION, client_id, os, xo);
			} catch (OXConflictException exc) {
				LOG.debug(_parsePropChilds, exc);
				writeResponse(contactobject, HttpServletResponse.SC_CONFLICT, CONFLICT_EXCEPTION, client_id, os, xo);
			} catch (OXObjectNotFoundException exc) {
				LOG.debug(_parsePropChilds, exc);
				writeResponse(contactobject, HttpServletResponse.SC_NOT_FOUND, OBJECT_NOT_FOUND_EXCEPTION, client_id, os, xo);
			} catch (OXConcurrentModificationException exc) {
				LOG.debug(_parsePropChilds, exc);
				writeResponse(contactobject, HttpServletResponse.SC_CONFLICT, MODIFICATION_EXCEPTION, client_id, os, xo);
			} catch (XmlPullParserException exc) {
				LOG.debug(_parsePropChilds, exc);
				writeResponse(contactobject, HttpServletResponse.SC_BAD_REQUEST, BAD_REQUEST_EXCEPTION, client_id, os, xo);
			} catch (OXException exc) {
				if (exc.getCategory() == Category.USER_INPUT) {
					LOG.debug(_parsePropChilds, exc);
					writeResponse(contactobject, HttpServletResponse.SC_CONFLICT, USER_INPUT_EXCEPTION, client_id, os, xo);
				} else {
					LOG.error(_parsePropChilds, exc);
					writeResponse(contactobject, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, SERVER_ERROR_EXCEPTION + exc.toString(), client_id, os, xo);
				}
			} catch (Exception exc) {
				LOG.error(_parsePropChilds, exc);
				writeResponse(contactobject, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, SERVER_ERROR_EXCEPTION + exc.toString(), client_id, os, xo);
			}
		} else {
			parser.next();
		}
	}
	
	protected void startWriter(final Session sessionObj, final int objectId, final int folderId, final OutputStream os) throws Exception {
		final ContactWriter contactwriter = new ContactWriter(sessionObj);
		contactwriter.startWriter(objectId, folderId, os);
	}

	protected void startWriter(final Session sessionObj, final int folderId, final boolean bModified, final boolean bDelete, final Date lastsync, final OutputStream os) throws Exception {
		startWriter(sessionObj, folderId, bModified, bDelete, false, lastsync, os);
	}
	
	protected void startWriter(final Session sessionObj, final int folderId, final boolean bModified, final boolean bDelete, final boolean bList, final Date lastsync, final OutputStream os) throws Exception {
		final ContactWriter contactwriter = new ContactWriter(sessionObj);
		contactwriter.startWriter(bModified, bDelete, bList, folderId, lastsync, os);
	}
	
	protected boolean hasModulePermission(final Session sessionObj) {
		final UserConfiguration uc = UserConfigurationStorage.getInstance().getUserConfigurationSafe(sessionObj.getUserId(), sessionObj.getContext());
		return (uc.hasWebDAVXML() && uc.hasContact());
	}
}




