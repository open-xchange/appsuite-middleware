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



package com.openexchange.webdav.xml;

import com.openexchange.webdav.xml.fields.CalendarFields;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.output.XMLOutputter;
import com.openexchange.api.OXConflictException;
import com.openexchange.api.OXPermissionException;
import com.openexchange.groupware.attach.AttachmentBase;
import com.openexchange.groupware.attach.Attachments;
import com.openexchange.groupware.container.AppointmentObject;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.container.DataObject;
import com.openexchange.server.Version;
import com.openexchange.sessiond.SessionObject;
import com.openexchange.webdav.PermissionServlet;
import com.openexchange.webdav.xml.fields.CommonFields;
import com.openexchange.webdav.xml.fields.DataFields;
import java.io.OutputStream;
import java.util.Date;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.kxml2.io.KXmlParser;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/**
 * XmlHandler
 *
 * @author <a href="mailto:sebastian.kauss@netline-is.de">Sebastian Kauss</a>
 */

public abstract class XmlServlet extends PermissionServlet {
	
	public static AttachmentBase attachmentBase = Attachments.getInstance();
	
	public static final int MODIFICATION_STATUS = 1000;
	
	public static final int OBJECT_NOT_FOUND_STATUS = 1001;
	
	public static final int PERMISSION_STATUS = 1002;
	
	public static final int CONFLICT_STATUS = 1003;
	
	public static final int MANDATORY_FIELD_STATUS = 1004;
	
	public static final int APPOINTMENT_CONFLICT_STATUS = 1006;
	
	public static final int USER_INPUT_STATUS = 1100;
	
	public static final int BAD_REQUEST_STATUS = 1400;
	
	public static final int SERVER_ERROR_STATUS = 1500;
	
	public static final int OK_STATUS = 1200;
	
	public static final String MODIFICATION_EXCEPTION = "[" + MODIFICATION_STATUS + "] This object was modified on the server";
	
	public static final String OBJECT_NOT_FOUND_EXCEPTION = "[" + OBJECT_NOT_FOUND_STATUS + "] Object not found";
	
	public static final String PERMISSION_EXCEPTION = "[" + PERMISSION_STATUS + "] No permission";
	
	public static final String CONFLICT_EXCEPTION = "[" + CONFLICT_STATUS + "] Conflict";
	
	public static final String USER_INPUT_EXCEPTION = "[" + USER_INPUT_STATUS + "] invalid user input";
	
	public static final String APPOINTMENT_CONFLICT_EXCEPTION = "[" + APPOINTMENT_CONFLICT_STATUS + "] Appointments Conflicted";
	
	public static final String MANDATORY_FIELD_EXCEPTION = "[" + MANDATORY_FIELD_STATUS + "] Missing field";
	
	public static final String BAD_REQUEST_EXCEPTION = "[" + BAD_REQUEST_STATUS + "] bad xml request";
	
	public static final String SERVER_ERROR_EXCEPTION = "[" + SERVER_ERROR_STATUS + "] Server Error - ";
	
	public static final String OK = "[" + OK_STATUS + "] OK";
	
	public static final String _contentType = "text/xml; charset=UTF-8";
	
	public static final String NAMESPACE = "http://www.open-xchange.org";
	
	public static final String PREFIX = "ox";
	
	protected static final String _parsePropChilds = "parsePropChilds";
	
	public static final Namespace NS = Namespace.getNamespace(PREFIX, NAMESPACE);
	
	private static final String prop = "prop";
	
	private static final String davUri = "DAV:";
	
	private static final Namespace dav = Namespace.getNamespace("D", davUri);
	
	private static transient final Log LOG = LogFactory.getLog(XmlServlet.class);
	
	public void oxinit() throws ServletException {
		
	}
	
	@Override
	public void doPut(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
		doPropPatch(req, resp);
	}
	
	@Override
	public void doPropPatch(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
		if (LOG.isDebugEnabled()) {
			LOG.debug("PROPPATCH");
		}
		
		XmlPullParser parser = null;
		
		try {
			parser = new KXmlParser();
			parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true);
			parser.setInput(req.getInputStream(), "UTF-8");
			
			resp.setStatus(SC_MULTISTATUS);
			resp.setContentType(_contentType);
			
			if (parser.getEventType() == XmlPullParser.START_DOCUMENT) {
				parser.next();
				if (isTag(parser, "multistatus", davUri)) {
					parser.nextTag();
					parser.require(XmlPullParser.START_TAG, davUri, "propertyupdate");
					
					parsePropertyUpdate(req, resp, parser);
				} else if (isTag(parser, "propertyupdate", davUri)) {
					parsePropertyUpdate(req, resp, parser);
				} else {
					doError(req, resp, HttpServletResponse.SC_BAD_REQUEST, "XML ERROR");
					return ;
				}
			} else {
				doError(req, resp, HttpServletResponse.SC_BAD_REQUEST, "XML ERROR");
				return ;
			}
		} catch (Exception exc) {
			LOG.error("doProppatch", exc);
			doError(req, resp);
		}
	}
	
	@Override
	public void doPropFind(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
		if (LOG.isDebugEnabled()) {
			LOG.debug("PROPFIND");
		}
		
		Document input_doc = null;
		
		OutputStream os = null;
		
		Date lastsync = null;
		int folder_id = 0;
		int object_id = 0;
		
		boolean bModified = true;
		boolean bDeleted = false;
		
		try {
			final SessionObject sessionObj = getSession(req);
			
			input_doc = getJDOMDocument(req);
			os = resp.getOutputStream();
			
			resp.setStatus(207);
			resp.setContentType("text/xml; charset=UTF-8");
			
			boolean hasLastSync = false;
			boolean hasObjectId = false;
			
			if (input_doc != null) {
				final Element el = input_doc.getRootElement();
				final Element eProp = el.getChild(prop, dav);
				
				if (eProp == null) {
					doError(req, resp, HttpServletResponse.SC_BAD_REQUEST, "expected element: prop");
					return;
				}
				
				final Element eLastSync = eProp.getChild("lastsync", Namespace.getNamespace(PREFIX, NAMESPACE));
				if (eLastSync != null) {
					hasLastSync = true;
				}
				
				final Element eObjectId = eProp.getChild(DataFields.OBJECT_ID, Namespace.getNamespace(PREFIX, NAMESPACE));
				if (eObjectId != null) {
					hasObjectId = true;
				}
				
				if (hasLastSync) {
					try {
						lastsync = new Date(Long.parseLong(eLastSync.getText()));
					} catch (NumberFormatException exc) {
						System.out.println("invalid value in element lastsync");
					}
					
					final Element eFolderId = eProp.getChild(CommonFields.FOLDER_ID, Namespace.getNamespace(PREFIX, NAMESPACE));
					if (eFolderId != null) {
						try {
							folder_id = Integer.parseInt(eFolderId.getText());
						} catch (NumberFormatException exc) {
							throw new OXConflictException("invalid value in element folder_id: " + eFolderId.getText(), exc);
						}
					}
					
					final Element eObjectMode = eProp.getChild("objectmode", Namespace.getNamespace(PREFIX, NAMESPACE));
					if (eObjectMode != null) {
						final String[] value = eObjectMode.getText().trim().toUpperCase().split(",");
						
						for (int a = 0; a < value.length; a++) {
							if (value[a].trim().equals("MODIFIED") || value[a].trim().equals("NEW_AND_MODIFIED")) {
								bModified = true;
							} else if (value[a].trim().equals("DELETED")) {
								bDeleted = true;
							} else {
								throw new OXConflictException("invalid value in element objectmode: " + value[a]);
							}
						}
					} else {
						bModified = true;
					}
				} else if (hasObjectId) {
					try {
						object_id = Integer.parseInt(eObjectId.getText());
					} catch (NumberFormatException exc) {
						throw new OXConflictException("invalid value in element object_id: " + eObjectId.getText());
					}
					
					final Element eFolderId = eProp.getChild(CommonFields.FOLDER_ID, Namespace.getNamespace(PREFIX, NAMESPACE));
					if (eFolderId != null) {
						try {
							folder_id = Integer.parseInt(eFolderId.getText());
						} catch (NumberFormatException exc) {
							throw new OXConflictException("invalid value in element folder_id: " + eFolderId.getText(), exc);
						}
					}
				} else {
					doError(req, resp, HttpServletResponse.SC_BAD_REQUEST, "expected element: object_id or lastsync");
					return;
				}
			}
			
			// SEND FIRST XML LINE
			os.write(("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n").getBytes());
			os.write(("<D:multistatus xmlns:D=\"DAV:\" buildnumber=\"" + Version.BUILDNUMBER + "\" buildname=\"" + Version.NAME + "\">").getBytes());
			
			if (hasLastSync) {
				if (lastsync == null) {
					lastsync = new Date(0);
				}
				startWriter(sessionObj, folder_id, bModified, bDeleted, lastsync, resp.getOutputStream());
			} else {
				startWriter(sessionObj, object_id, folder_id, resp.getOutputStream());
			}
			
			os.write(("</D:multistatus>").getBytes());
			os.flush();
		} catch (OXPermissionException opexc) {
			doError(req, resp, HttpServletResponse.SC_FORBIDDEN, opexc.getMessage());
		} catch (OXConflictException ocexc) {
			if (LOG.isErrorEnabled()) {
				LOG.error(ocexc.getMessage(), ocexc);
			}
			doError(req, resp, HttpServletResponse.SC_CONFLICT, "Conflict: " + ocexc.getMessage());
		} catch (org.jdom.JDOMException exc) {
			LOG.error("doPropFind", exc);
			doError(req, resp, HttpServletResponse.SC_BAD_REQUEST, "XML ERROR");
		} catch (Exception exc) {
			LOG.error("doPropFind", exc);
			doError(req, resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Server Error");
		}
	}
	
	public void doError(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException {
		doError(req, resp,HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Server Error");
	}
	
	public void doError(final HttpServletRequest req, final HttpServletResponse resp, final int code, final String msg) throws ServletException {
		try {
			if (LOG.isDebugEnabled()) {
				LOG.debug("STATUS: " + msg + ": (" + code + ')');
			}
			
			resp.sendError(code, msg);
			resp.setContentType("text/html");
		} catch (Exception exc) {
			LOG.error("doError", exc);
		}
	}
	
	protected void parsePropertyUpdate(final HttpServletRequest req, final HttpServletResponse resp, final XmlPullParser parser) throws Exception {
		final OutputStream os = resp.getOutputStream();
		
		os.write(("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n").getBytes());
		os.write(("<D:multistatus xmlns:D=\"DAV:\" buildnumber=\"" + Version.BUILDNUMBER + "\" buildname=\"" + Version.NAME + "\">").getBytes());
		
		while (parser.getEventType() != XmlPullParser.END_DOCUMENT) {
			if (isTag(parser, "set", davUri)) {
				openSet(req, resp, parser);
			} else {
				parser.next();
			}
		}
		
		os.write(("</D:multistatus>").getBytes());
	}
	
	protected void openSet(final HttpServletRequest req, final HttpServletResponse resp, final XmlPullParser parser) throws Exception {
		openProp(req, resp, parser);
	}
	
	protected void openProp(final HttpServletRequest req, final HttpServletResponse resp, final XmlPullParser parser) throws Exception {
		parser.nextTag();
		parser.require(XmlPullParser.START_TAG, davUri, prop);
		
		parsePropChilds(req, resp, parser);
		
		closeProp(parser);
	}
	
	protected void closeProp(final XmlPullParser parser) throws Exception {
		// parser.nextTag();
		if (!isTag(parser, prop, davUri)) {
			boolean isProp = true;
			
			while (isProp) {
				if (endTag(parser, prop, davUri) || DataParser.isEnd(parser)) {
					isProp = false;
					break;
				}
				
				parser.next();
			}
		}
		
		closeSet(parser);
	}
	
	protected void closeSet(final XmlPullParser parser) throws Exception {
		parser.nextTag();
		parser.require(XmlPullParser.END_TAG, davUri, "set");
	}
	
	public boolean isTag(final XmlPullParser parser, final String name, final String namespace) throws XmlPullParserException {
		return parser.getEventType() == XmlPullParser.START_TAG	&& (name == null || name.equals(parser.getName()));
	}
	
	public boolean endTag(final XmlPullParser parser, final String name, final String namespace) throws XmlPullParserException {
		return parser.getEventType() == XmlPullParser.END_TAG && (name == null || name.equals(parser.getName()));
	}
	
	protected void writeResponse(final DataObject dataobject, final int status, final String message, final String client_id, final OutputStream os, final XMLOutputter xo) throws Exception {
		writeResponse(dataobject, status, message, client_id, os, xo, null);
	}
	
	protected void writeResponse(final DataObject dataobject, final int status, final String message, final String client_id, final OutputStream os, final XMLOutputter xo, final AppointmentObject[] conflicts) throws Exception {
		if (LOG.isDebugEnabled()) {
			LOG.debug(message + ':' + status);
		}
		
		final Element e_response = new Element("response", dav);
		e_response.addNamespaceDeclaration(NS);
		
		final Element e_href = new Element("href", dav);
		e_href.addContent(String.valueOf(dataobject.getObjectID()));
		
		e_response.addContent(e_href);
		
		final Element e_propstat = new Element("propstat", dav);
		final Element e_prop = new Element(prop, dav);
		
		final Element e_object_id = new Element(DataFields.OBJECT_ID, NS);
		e_object_id.addContent(String.valueOf(dataobject.getObjectID()));
		e_prop.addContent(e_object_id);
		
		final Date lastModified = dataobject.getLastModified();
		if (lastModified != null) {
			final Element eLastModified = new Element(DataFields.LAST_MODIFIED, NS);
			eLastModified.addContent(String.valueOf(lastModified.getTime()));
			e_prop.addContent(eLastModified);
		}
		
		if (dataobject instanceof CalendarObject) {
			final Element e_recurrence_id = new Element(CalendarFields.RECURRENCE_ID, NS);
			e_recurrence_id.addContent(String.valueOf(((CalendarObject)dataobject).getRecurrenceID()));
			e_prop.addContent(e_recurrence_id);
		}
		
		if (client_id != null && client_id.length() > 0) {
			final Element e_client_id = new Element("client_id", NS);
			e_client_id.addContent(client_id);
			
			e_prop.addContent(e_client_id);
		}
		
		e_propstat.addContent(e_prop);
		
		final Element e_status = new Element("status", "D", davUri);
		e_status.addContent(String.valueOf(status));
		
		e_propstat.addContent(e_status);
		
		final Element e_descr = new Element("responsedescription", "D", davUri);
		e_descr.addContent(message);
		
		e_propstat.addContent(e_descr);
		
		if (conflicts != null) {
			final Element eConflictItems = new Element("conflictitems", Namespace.getNamespace("D", davUri));
			final StringBuilder textBuilder = new StringBuilder(50);
			for (int a = 0; a < conflicts.length; a++) {
				final Element eConflictItem = new Element("conflictitem", Namespace.getNamespace("D", davUri));
				if (conflicts[a].getTitle() == null) {
					eConflictItem.setAttribute("subject", "", NS);
				} else {
					eConflictItem.setAttribute("subject", conflicts[a].getTitle(), NS);
				}

				eConflictItem.setText(textBuilder.append(conflicts[a].getStartDate().getTime()).append(',').append(conflicts[a].getEndDate().getTime()).append(',').append(conflicts[a].getFullTime()).toString());
				textBuilder.setLength(0);
				
				eConflictItems.addContent(eConflictItem);
			}
			
			e_propstat.addContent(eConflictItems);
		}
		
		e_response.addContent(e_propstat);
		
		xo.output(e_response, os);
	}
	
	protected abstract void parsePropChilds(HttpServletRequest req, HttpServletResponse resp, XmlPullParser parser) throws Exception;
	
	protected abstract void startWriter(SessionObject sessionObj, int objectId, int folderId, OutputStream os) throws Exception;
	
	protected abstract void startWriter(SessionObject sessionObj, int folderId, boolean bModified, boolean bDelete, Date lastsync, OutputStream os) throws Exception;
}




