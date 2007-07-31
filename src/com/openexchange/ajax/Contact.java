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

package com.openexchange.ajax;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.fields.ContactFields;
import com.openexchange.ajax.parser.ContactParser;
import com.openexchange.ajax.parser.DataParser;
import com.openexchange.ajax.request.ContactRequest;
import com.openexchange.api.OXMandatoryFieldException;
import com.openexchange.api2.ContactSQLInterface;
import com.openexchange.api2.OXException;
import com.openexchange.api2.RdbContactSQLInterface;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.container.ContactObject;
import com.openexchange.groupware.upload.UploadEvent;
import com.openexchange.groupware.upload.UploadFile;
import com.openexchange.json.OXJSONWriter;
import com.openexchange.sessiond.SessionObject;
import com.openexchange.tools.Logging;
import com.openexchange.tools.servlet.AjaxException;
import com.openexchange.tools.servlet.OXJSONException;
import com.openexchange.tools.servlet.http.Tools;

public class Contact extends DataServlet {
	
	/**
	 * For serialization.
	 */
	private static final long serialVersionUID = 1635881627528234660L;
	
	private static final Log LOG = LogFactory.getLog(Contact.class);
	
	@Override
	protected void doGet(final HttpServletRequest httpServletRequest, final HttpServletResponse httpServletResponse) throws ServletException, IOException {
		final Response response = new Response();
		try {
			final String action = parseMandatoryStringParameter(httpServletRequest, PARAMETER_ACTION);
			final SessionObject sessionObj = getSessionObject(httpServletRequest);
			
			JSONObject jsonObj = null;
			try {
				jsonObj = convertParameter2JSONObject(httpServletRequest);
			} catch (final JSONException e) {
				LOG.error(_doGet, e);
				response.setException(new OXJSONException(OXJSONException.Code.JSON_BUILD_ERROR, e));
				writeResponse(response, httpServletResponse);
				return;
			}
			
			if (action.equals(ACTION_IMAGE)) {
				final int id = DataParser.checkInt(jsonObj, AJAXServlet.PARAMETER_ID);
				final int inFolder = DataParser.checkInt(jsonObj, AJAXServlet.PARAMETER_INFOLDER);
				
				OutputStream os = null;
				
				final ContactSQLInterface sqlinterface = new RdbContactSQLInterface(sessionObj);
				
				try {
					final ContactObject contactObj = sqlinterface.getObjectById(id, inFolder);
					final String imageContentType = contactObj.getImageContentType();
					if (imageContentType != null) {
						httpServletResponse.setContentType(imageContentType);
					/*
					 * Reset response header values since we are going to directly
					 * write into servlet's output stream and then some browsers do
					 * not allow header "Pragma"
					 */
						Tools.removeCachingHeader(httpServletResponse);
						os = httpServletResponse.getOutputStream();
						if (contactObj.getImage1() != null) {
							os.write(contactObj.getImage1());
						}
					} else {
						LOG.warn("image doesn't contain a content type: object_id=" + id);
					}
				} catch (final OXException e) {
					LOG.error("actionImage", e);
					httpServletResponse.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "");
				}
				
				if (os != null) {
					os.flush();
				}
				
				return;
			}
			final OXJSONWriter sw = new OXJSONWriter();
			final ContactRequest contactRequest = new ContactRequest(sessionObj, sw);
			contactRequest.action(action, jsonObj);
			response.setTimestamp(contactRequest.getTimestamp());
			response.setData(sw.getObject());
//			try {
//				response.setData(new JSONArray(sw.toString()));
//			} catch (final JSONException e) {
//				response.setData(new JSONObject(sw.toString()));
//			}
		} catch (final JSONException e) {
			final OXJSONException oje = new OXJSONException(OXJSONException.Code
					.JSON_WRITE_ERROR, e);
			LOG.error(oje.getMessage(), oje);
			response.setException(oje);
		} catch (final AbstractOXException e) {
			Logging.log(LOG, e);
			response.setException(e);
		}
		
		writeResponse(response, httpServletResponse);
		
	}
	
	protected void doPut(final HttpServletRequest httpServletRequest, final HttpServletResponse httpServletResponse) throws ServletException, IOException {
		final Response response = new Response();
		try {
			final String action = parseMandatoryStringParameter(httpServletRequest, PARAMETER_ACTION);
			final SessionObject sessionObj = getSessionObject(httpServletRequest);
			
			final String data = getBody(httpServletRequest);
			if (data.length() > 0) {
				final JSONObject jsonObj;
				final OXJSONWriter sw = new OXJSONWriter();
				
				try {
					jsonObj = convertParameter2JSONObject(httpServletRequest);
				} catch (final JSONException e) {
					LOG.error(e.getMessage(), e);
					response.setException(new OXJSONException(OXJSONException.Code.JSON_BUILD_ERROR, e));
					writeResponse(response, httpServletResponse);
					return;
				}
				
				final ContactRequest contactRequest = new ContactRequest(sessionObj, sw);
				
				if (data.charAt(0) == '[') {
					final JSONArray jsonDataArray = new JSONArray(data);
					jsonObj.put(AJAXServlet.PARAMETER_DATA, jsonDataArray);
					
					contactRequest.action(action, jsonObj);
					response.setTimestamp(contactRequest.getTimestamp());
					response.setData(sw.getObject());
					/*try {
						response.setData(new JSONArray(sw.toString()));
					} catch (final JSONException e) {
						response.setData(new JSONObject(sw.toString()));
					}*/
				} else if (data.charAt(0) == '{') {
					final JSONObject jsonDataObj = new JSONObject(data);
					jsonObj.put(AJAXServlet.PARAMETER_DATA, jsonDataObj);
					
					contactRequest.action(action, jsonObj);
					response.setTimestamp(contactRequest.getTimestamp());
					response.setData(sw.getObject());
					/*try {
						response.setData(new JSONArray(sw.toString()));
					} catch (final JSONException e) {
						response.setData(new JSONObject(sw.toString()));
					}*/
				} else {
					httpServletResponse.sendError(HttpServletResponse.SC_BAD_REQUEST, "invalid json object");
				}
			} else {
				httpServletResponse.sendError(HttpServletResponse.SC_BAD_REQUEST, "no data found");
			}
		} catch (final JSONException e) {
			final OXJSONException oje = new OXJSONException(OXJSONException.Code
					.JSON_WRITE_ERROR, e);
			LOG.error(oje.getMessage(), oje);
			response.setException(oje);
		} catch (final AbstractOXException e) {
			Logging.log(LOG, e);
			response.setException(e);
		}
		
		writeResponse(response, httpServletResponse);
		
	}
	
	@Override
			protected void doPost(final HttpServletRequest httpServletRequest, final HttpServletResponse httpServletResponse) throws ServletException, IOException {
		httpServletResponse.setContentType("text/html");
		String callbackSite = null;
		final Response response = new Response();
		String action = ACTION_ERROR;
		try {
			final SessionObject sessionObj = getSessionObject(httpServletRequest);
			action = parseMandatoryStringParameter(httpServletRequest, PARAMETER_ACTION);
			if (action.equals(ACTION_NEW)) {
				UploadEvent upload = null;
				try {
					upload = processUpload(httpServletRequest);
					final UploadFile uploadFile = upload.getUploadFileByFieldName(AJAXServlet.PARAMETER_FILE);
					
					if (uploadFile == null) {
						throw new AjaxException(AjaxException.Code.NoUploadImage);
					}
					
					final String obj = upload.getFormField(AJAXServlet.PARAMETER_JSON);
					if(obj == null) {
						throw new AjaxException(AjaxException.Code.NoField, "form");
					}
					
					final ContactObject contactobject = new ContactObject();
					final JSONObject jsonobject = new JSONObject(obj);
					
					final ContactParser contactparser = new ContactParser(sessionObj);
					contactparser.parse(contactobject, jsonobject);
					
					if (!contactobject.containsParentFolderID()) {
						throw new OXMandatoryFieldException("missing folder");
					}
					
					final byte[] b = new byte[(int)uploadFile.getSize()];
					final FileInputStream fis = new FileInputStream(uploadFile.getTmpFile());
					fis.read(b);
					contactobject.setImageContentType(uploadFile.getContentType());
					contactobject.setImage1(b);
					
					final ContactSQLInterface contactsql = new RdbContactSQLInterface(sessionObj);
					contactsql.insertContactObject(contactobject);
					
					final JSONObject jData = new JSONObject();
					jData.put(ContactFields.ID, contactobject.getObjectID());
					
					response.setData(jData);
				} finally {
					if (upload != null) {
						upload.cleanUp();
					}
				}
			} else if (action.equals(ACTION_UPDATE)) {
				final int id = parseMandatoryIntParameter(httpServletRequest, AJAXServlet.PARAMETER_ID);
				final int inFolder = parseMandatoryIntParameter(httpServletRequest, AJAXServlet.PARAMETER_INFOLDER);
				final Date timestamp = parseMandatoryDateParameter(httpServletRequest, AJAXServlet.PARAMETER_TIMESTAMP);
				
				UploadEvent upload = null;
				try {
					upload = processUpload(httpServletRequest);
					final UploadFile uploadFile = upload.getUploadFileByFieldName(AJAXServlet.PARAMETER_FILE);
					
					final String obj = upload.getFormField(AJAXServlet.PARAMETER_JSON);
					if(obj == null) {
						throw new AjaxException(AjaxException.Code.NoField, "form");
					}
					
					final ContactObject contactobject = new ContactObject();
					final JSONObject jsonobject = new JSONObject(obj);
					
					final ContactParser contactparser = new ContactParser(sessionObj);
					contactparser.parse(contactobject, jsonobject);
					
					contactobject.setObjectID(id);
					
					if (null == uploadFile) {
						contactobject.setImage1(null);
					} else {
						final byte[] b = new byte[(int)uploadFile.getSize()];
						final FileInputStream fis = new FileInputStream(uploadFile.getTmpFile());
						fis.read(b);
						contactobject.setImageContentType(uploadFile.getContentType());
						contactobject.setImage1(b);
					}
					
					final ContactSQLInterface contactsql = new RdbContactSQLInterface(sessionObj);
					contactsql.updateContactObject(contactobject, inFolder, timestamp);
				} finally {
					if (upload != null) {
						upload.cleanUp();
					}
				}
			} else {
				throw new AjaxException(AjaxException.Code.UnknownAction, action);
			}
		} catch (final JSONException e) {
			final OXJSONException oje = new OXJSONException(OXJSONException.Code
					.JSON_WRITE_ERROR, e);
			LOG.error(oje.getMessage(), oje);
			response.setException(oje);
		} catch (final AbstractOXException e) {
			Logging.log(LOG, e);
			response.setException(e);
		}
		try {
			// Replaces every 2+2x parameter through the 3+2x parameter in the first parameter string
			callbackSite = AJAXServlet.substitute(AJAXServlet.JS_FRAGMENT, AJAXServlet.PARAMETER_JSON, response.getJSON().toString(), AJAXServlet.PARAMETER_ACTION, action);
			final PrintWriter pw = httpServletResponse.getWriter();
			pw.print(callbackSite);
		} catch (final JSONException e) {
			log(RESPONSE_ERROR, e);
			sendError(httpServletResponse);
		}
		
	}
	
	protected boolean hasModulePermission(final SessionObject sessionObj) {
		return sessionObj.getUserConfiguration().hasContact();
	}
}
