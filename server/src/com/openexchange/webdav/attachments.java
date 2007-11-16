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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.output.XMLOutputter;

import com.openexchange.api.OXConflictException;
import com.openexchange.api.OXObjectNotFoundException;
import com.openexchange.api2.OXException;
import com.openexchange.groupware.AbstractOXException.Category;
import com.openexchange.groupware.attach.AttachmentBase;
import com.openexchange.groupware.attach.AttachmentMetadata;
import com.openexchange.groupware.attach.Attachments;
import com.openexchange.groupware.attach.impl.AttachmentImpl;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.tx.TransactionException;
import com.openexchange.groupware.userconfiguration.UserConfigurationStorage;
import com.openexchange.sessiond.Session;
import com.openexchange.tools.webdav.OXServlet;
import com.openexchange.webdav.xml.XmlServlet;
import com.openexchange.webdav.xml.fields.DataFields;

/**
 * attachments
 * @author <a href="mailto:sebastian.kauss@netline-is.de">Sebastian Kauss</a>
 */

public final class attachments extends OXServlet {
	
	public static final String LAST_MODIFIED = "last_modified";
	
	public static final String MODULE = "module";
	
	public static final String FILENAME = "filename";
	
	public static final String MIME_TYPE = "mimetype";
	
	public static final String TARGET_ID = "target_id";
	
	public static final String TARGET_FOLDER_ID = "target_folder_id";
	
	public static final String RTF_FLAG = "rtf_flag";
	
	private static final String DAV = "DAV:";
	
	public static final String NAMESPACE = XmlServlet.NAMESPACE;
	public static final String PREFIX = XmlServlet.PREFIX;
	
	private static AttachmentBase attachmentBase = Attachments.getInstance();
	
	static {
		attachmentBase.setTransactional(true);
	}
	
	private static final Log LOG = LogFactory.getLog(attachments.class);
	
	@Override
	public void doPut(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
		log("PUT");
		
		String client_id = null;
		
		int objectId = 0;
		
		final XMLOutputter xo = new XMLOutputter();
		
		Session sessionObj = null;
		
		try {
			sessionObj = getSession(req);
			
			client_id = req.getHeader("client_id");
			
			String filename = req.getHeader(FILENAME);
			
			if (filename != null) {
				filename = URLDecoder.decode(filename, "UTF-8");
			} 
			
			final int module = req.getIntHeader(MODULE);
			final int targetId = req.getIntHeader(TARGET_ID);
			objectId = req.getIntHeader(DataFields.OBJECT_ID);
			final int target_folder_id = req.getIntHeader(TARGET_FOLDER_ID);
			final String sRtfFlag = req.getHeader("rtf_flag");
			final boolean rtfFlag = (sRtfFlag != null) ? Boolean.valueOf(sRtfFlag) : false;
			final int fileSize = req.getContentLength();
			final String mimeType = req.getContentType();
			
			final InputStream is = req.getInputStream();
			
			final AttachmentMetadata attachmentMeta = new AttachmentImpl();
			
			attachmentMeta.setAttachedId(targetId);
			attachmentMeta.setFolderId(target_folder_id);
			attachmentMeta.setRtfFlag(rtfFlag);
			attachmentMeta.setModuleId(module);
			attachmentMeta.setFilename(filename);
			attachmentMeta.setFileMIMEType(mimeType);
			attachmentMeta.setFilesize(fileSize);
			attachmentMeta.setId((req.getHeader(DataFields.OBJECT_ID) != null) ? objectId : AttachmentBase.NEW);
			
			attachmentBase.startTransaction();
			attachmentBase.attachToObject(attachmentMeta, is, sessionObj.getContext(), UserStorage.getUser(sessionObj.getUserId(), sessionObj.getContext()), UserConfigurationStorage.getInstance().getUserConfigurationSafe(sessionObj.getUserId(), sessionObj.getContext()));
			attachmentBase.commit();
			
			objectId = attachmentMeta.getId();
			final Date lastModified = attachmentMeta.getCreationDate();
			
			final Element e_multistatus = new Element("multistatus", "D", DAV);
			final Document output_doc = new Document(e_multistatus);
			
			final Element e_response = new Element("response", "D", DAV);
			e_response.addNamespaceDeclaration(Namespace.getNamespace(PREFIX, NAMESPACE));
			
			final Element e_href = new Element("href", "D", DAV);
			
			e_href.addContent(String.valueOf(objectId));
			
			e_response.addContent(e_href);
			
			final Element e_prop = new Element("prop", Namespace.getNamespace("D", DAV));
			final Element e_propstat = new Element("propstat", "D", DAV);
			
			final Element e_object_id = new Element(DataFields.OBJECT_ID, PREFIX, NAMESPACE);
			e_object_id.addContent(String.valueOf(objectId));
			
			e_prop.addContent(e_object_id);
			
			final Element e_lastmodified = new Element(LAST_MODIFIED, PREFIX, NAMESPACE);
			e_lastmodified.addContent(String.valueOf(lastModified.getTime()));
			
			e_prop.addContent(e_lastmodified);
			
			
			if (client_id != null && client_id.length() > 0) {
				final Element e_client_id = new Element("client_id", PREFIX, NAMESPACE);
				e_client_id.addContent(client_id);
				
				e_prop.addContent(e_client_id);
			}
			e_propstat.addContent(e_prop);
			
			final Element e_status = new Element("status", "D", DAV);
			e_status.addContent(String.valueOf(HttpServletResponse.SC_OK));
			
			e_propstat.addContent(e_status);
			
			final Element e_descr = new Element("responsedescription", "D", DAV);
			e_descr.addContent(XmlServlet.OK);
			
			e_propstat.addContent(e_descr);
			e_response.addContent(e_propstat);
			
			e_multistatus.addContent(e_response);
			
			resp.setStatus(SC_MULTISTATUS);
			resp.setContentType(XmlServlet._contentType);
			
			xo.output(output_doc, resp.getOutputStream());
		} catch (TransactionException exc) {
			doError(req, resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, exc.toString());
			LOG.error("doPut", exc);
			exc.printStarterTrace();
			rollbackTransaction();
		} catch (OXException exc) {
			if (exc.getCategory() == Category.PERMISSION) {
				LOG.debug("doPut", exc);
			} else {
				LOG.error("doPut", exc);
			}
			
			doError(req, resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, exc.toString());
			rollbackTransaction();
		} catch (Exception exc) {
			doError(req, resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, exc.toString());
			LOG.error("doPut", exc);
			rollbackTransaction();
		} finally {
			finishTransaction();
		}
	}
	
	@Override
	public void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
		log("GET");
		
		int target_id = 0;
		int object_id = 0;
		int folder_id = 0;
		
		int module = 0;
		
		OutputStream os = null;
		
		Session sessionObj = null;
		
		
		try {
			sessionObj = getSession(req);
			
			os = resp.getOutputStream();
			
			if (req.getHeader(MODULE) == null) {
				throw new OXConflictException(MODULE + " is missing");
			} else {
				try {
					module = Integer.parseInt(req.getHeader(MODULE));
				} catch (NumberFormatException exc) {
					throw new OXConflictException(MODULE + "is not a integer", exc);
				}
			}
			
			if (req.getHeader(TARGET_ID) == null) {
				throw new OXConflictException(TARGET_ID + " is missing");
			} else {
				try {
					target_id = Integer.parseInt(req.getHeader(TARGET_ID));
				} catch (NumberFormatException exc) {
					throw new OXConflictException(TARGET_ID + "is not a long", exc);
				}
			}
			
			if (req.getHeader(DataFields.OBJECT_ID) == null) {
				throw new OXConflictException(DataFields.OBJECT_ID + " is missing");
			} else {
				try {
					object_id = Integer.parseInt(req.getHeader(DataFields.OBJECT_ID));
				} catch (NumberFormatException exc) {
					throw new OXConflictException(DataFields.OBJECT_ID + "is not a integer", exc);
				}
			}
			
			if (req.getHeader(TARGET_FOLDER_ID) != null) {
				try {
					folder_id = Integer.parseInt(req.getHeader(TARGET_FOLDER_ID));
				} catch (NumberFormatException exc) {
					throw new OXConflictException(TARGET_FOLDER_ID + " is not an integer", exc);
				}
			}
			
			attachmentBase.startTransaction();
			final User u = UserStorage.getUser(sessionObj.getUserId(), sessionObj.getContext());
			final AttachmentMetadata attachmentMeta = attachmentBase.getAttachment(folder_id, target_id, module, object_id, sessionObj.getContext(), u, UserConfigurationStorage.getInstance().getUserConfigurationSafe(sessionObj.getUserId(), sessionObj.getContext()));
			final InputStream is = attachmentBase.getAttachedFile(folder_id, target_id, module, object_id, sessionObj.getContext(), u, UserConfigurationStorage.getInstance().getUserConfigurationSafe(sessionObj.getUserId(), sessionObj.getContext()));
			attachmentBase.commit();
			resp.setContentType(attachmentMeta.getFileMIMEType());
			
			
			final byte b[] = new byte[8192];
			int i = is.read(b);			
			while (i != -1) {
				os.write(b, 0, i);
				i = is.read(b);
			}
		} catch (OXConflictException exc) {
			doError(req, resp, HttpServletResponse.SC_CONFLICT, exc.toString());
			rollbackTransaction();
		} catch (Exception exc) {
			doError(req, resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Server Error: " + exc.toString());
			LOG.error("doGet", exc);
			rollbackTransaction();
		} finally {
			finishTransaction();
		}
	}
	
	@Override
	public void doDelete(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
		log("DELETE");
		

		Session sessionObj = null;
		
		try {
			sessionObj = getSession(req);
			
			final int objectId = req.getIntHeader(DataFields.OBJECT_ID);
			final int module = req.getIntHeader(MODULE);
			final int targetId = req.getIntHeader(TARGET_ID);
			final int folderId = req.getIntHeader(TARGET_FOLDER_ID);
			
			attachmentBase.startTransaction();
			attachmentBase.detachFromObject(folderId, targetId, module, new int[] { objectId }, sessionObj.getContext(), UserStorage.getUser(sessionObj.getUserId(), sessionObj.getContext()), UserConfigurationStorage.getInstance().getUserConfigurationSafe(sessionObj.getUserId(), sessionObj.getContext()));
			attachmentBase.commit();
			resp.setStatus(HttpServletResponse.SC_OK);
		} catch (OXConflictException exc) {
			doError(req, resp, HttpServletResponse.SC_CONFLICT, exc.toString());
			LOG.error("doDelete", exc);
		} catch (OXObjectNotFoundException exc) {
			doError(req, resp, HttpServletResponse.SC_NOT_FOUND, exc.toString());
			rollbackTransaction();
		} catch (Exception exc) {
			doError(req, resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Server Error: " + exc.toString());
			LOG.error("doDelete", exc);
			rollbackTransaction();
		} finally {
			finishTransaction();
		}
	}
	
	protected void doError(final HttpServletRequest req, final HttpServletResponse resp, final int errorcode, final String errormessage) {
		resp.setStatus(errorcode);
		log(errorcode + ": " + errormessage);
	}
	
	private void finishTransaction() {
		try {
			attachmentBase.finish();
		} catch (TransactionException exc) {
			LOG.error("finishTransaction", exc);
		}
	}

	private void rollbackTransaction() {
		try {
			attachmentBase.rollback();
		} catch (TransactionException exc) {
			LOG.error("finishTransaction", exc);
		}
	}
}





