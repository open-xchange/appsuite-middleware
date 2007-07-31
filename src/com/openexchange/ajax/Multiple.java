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

import java.io.IOException;
import java.io.Writer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.parser.DataParser;
import com.openexchange.ajax.request.AppointmentRequest;
import com.openexchange.ajax.request.ContactRequest;
import com.openexchange.ajax.request.FolderRequest;
import com.openexchange.ajax.request.GroupRequest;
import com.openexchange.ajax.request.InfostoreRequest;
import com.openexchange.ajax.request.JSONSimpleRequest;
import com.openexchange.ajax.request.MailRequest;
import com.openexchange.ajax.request.ReminderRequest;
import com.openexchange.ajax.request.ResourceRequest;
import com.openexchange.ajax.request.TaskRequest;
import com.openexchange.api.OXConflictException;
import com.openexchange.api.OXMandatoryFieldException;
import com.openexchange.api.OXObjectNotFoundException;
import com.openexchange.api.OXPermissionException;
import com.openexchange.api2.MailInterface;
import com.openexchange.api2.MailInterfaceImpl;
import com.openexchange.api2.OXConcurrentModificationException;
import com.openexchange.api2.OXException;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.AbstractOXException.Category;
import com.openexchange.groupware.ldap.LdapException;
import com.openexchange.json.OXJSONWriter;
import com.openexchange.sessiond.SessionObject;
import com.openexchange.tools.iterator.SearchIteratorException;
import com.openexchange.tools.oxfolder.OXFolderException;
import com.openexchange.tools.oxfolder.OXFolderNotFoundException;
import com.openexchange.tools.servlet.AjaxException;
import com.openexchange.tools.servlet.OXJSONException;
import com.openexchange.tools.servlet.http.Tools;

public class Multiple extends SessionServlet {

	private static final String STR_EMPTY = "";

	private static final long serialVersionUID = 3029074251138469122L;

	protected static final String MODULE = "module";

	protected static final String MODULE_CALENDAR = "calendar";

	protected static final String MODULE_TASK = "tasks";

	protected static final String MODULE_CONTACT = "contacts";

	protected static final String MODULE_GROUP = "group";

	protected static final String MODULE_REMINDER = "reminder";

	protected static final String MODULE_RESOURCE = "resource";

	protected static final String MODULE_INFOSTORE = "infostore";

	protected static final String MODULE_FOLDER = "folder";

	private static final String ATTRIBUTE_MAIL_INTERFACE = "mi";

	private static final String ATTRIBUTE_MAIL_REQUEST = "mr";

	private static final transient Log LOG = LogFactory.getLog(Multiple.class);

	@Override
	protected void doPut(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
		final Response response = new Response();
		JSONArray jsonArray = null;
		final String data = getBody(req);

		try {
			jsonArray = new JSONArray(data);
		} catch (JSONException e) {
            final AbstractOXException exc = new OXJSONException(OXJSONException
                .Code.JSON_READ_ERROR, e, data);
            LOG.error(exc.getMessage() + Tools.logHeaderForError(req), exc);
            response.setException(exc);
            jsonArray = new JSONArray();
		}

		
		try {
			final JSONArray respArr = new JSONArray();

			for (int a = 0; a < jsonArray.length(); a++) {
				parseActionElement(respArr, jsonArray, a, getSessionObject(req), req);
			}

			if (req.getAttribute(ATTRIBUTE_MAIL_REQUEST) != null) {
				/*
				 * Write withheld mail request
				 */
				try {
					writeMailRequest((MailRequest) req.getAttribute(ATTRIBUTE_MAIL_REQUEST), (MailInterface) req
							.getAttribute(ATTRIBUTE_MAIL_INTERFACE), respArr);
				} finally {
					/*
					 * Remove mail request object
					 */
					req.setAttribute(ATTRIBUTE_MAIL_REQUEST, null);
				}
			}

			response.setData(respArr);
		} catch (JSONException e) {
			log(RESPONSE_ERROR, e);
			sendError(resp);
		} catch (AjaxException e) {
			log(RESPONSE_ERROR, e);
			sendError(resp);
		} catch (OXException e) {
			log(RESPONSE_ERROR, e);
			sendError(resp);
		} finally {
			Object tmp = req.getAttribute(ATTRIBUTE_MAIL_INTERFACE);
			if (tmp != null) {
				final MailInterface mi = (MailInterface) tmp;
				tmp = null;
				try {
					mi.close(true);
				} catch (OXException e) {
					LOG.error(e.getMessage(), e);
				}
			}
		}
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.setContentType(CONTENTTYPE_JAVASCRIPT);
        final Writer writer = resp.getWriter();
        writer.write(response.getData().toString());
        writer.flush();
	}

	protected static final void parseActionElement(final JSONArray respArr, final JSONArray jsonArray, final int pos,
			final SessionObject sessionObj, final HttpServletRequest req)
			throws JSONException, AjaxException, OXException {
		final JSONObject jsonObj = jsonArray.getJSONObject(pos);

		final String module;
		final String action;

		if (jsonObj.has(MODULE)) {
			module = DataParser.checkString(jsonObj, MODULE);
		} else {
			throw new AjaxException(AjaxException.Code.NoField, MODULE);
		}

		if (jsonObj.has(PARAMETER_ACTION)) {
			action = DataParser.checkString(jsonObj, PARAMETER_ACTION);
		} else {
			throw new AjaxException(AjaxException.Code.NoField, PARAMETER_ACTION);
		}

		final Response response = doAction(module, action, jsonObj, sessionObj, req);
		if (response != null) {
			if (req.getAttribute(ATTRIBUTE_MAIL_REQUEST) != null) {
				/*
				 * Write withheld mail request first
				 */
				try {
					writeMailRequest((MailRequest) req.getAttribute(ATTRIBUTE_MAIL_REQUEST), (MailInterface) req
							.getAttribute(ATTRIBUTE_MAIL_INTERFACE), respArr);
				} finally {
					/*
					 * Remove mail request object
					 */
					req.setAttribute(ATTRIBUTE_MAIL_REQUEST, null);
				}
			}
			respArr.put(response.getJSON());
		}
	}

	private static final void writeMailRequest(final MailRequest mailReq, final MailInterface mailInterface,
			final JSONArray respArr) throws JSONException {
		/*
		 * Write withheld mail response first
		 */
		mailReq.performMultiple(mailInterface);
		if (mailReq.getContent().equals(STR_EMPTY)) {
			final Response mailResp = new Response();
			mailResp.setData(STR_EMPTY);
			respArr.put(mailResp.getJSON());
			//Response.write(mailResp, w);
		} else {
			final JSONArray ja = mailReq.getContent();
			final int len = ja.length();
			for (int i = 0; i < len; i++) {
				respArr.put(ja.getJSONObject(i));
			}
			/*try {
				w.write(mailReq.getContent());
			} catch (IOException e) {
				LOG.error(e.getMessage(), e);
			}*/
		}
	}

	protected static final Response doAction(final String module, final String action, final JSONObject jsonObj,
			final SessionObject sessionObj, final HttpServletRequest req) throws AjaxException {
		Response response = new Response();
		final OXJSONWriter jsonWriter = new OXJSONWriter();
		if (module.equals(MODULE_CALENDAR)) {
			final AppointmentRequest appointmentRequest = new AppointmentRequest(sessionObj, jsonWriter);
			try {
				appointmentRequest.action(action, jsonObj);
				response.setTimestamp(appointmentRequest.getTimestamp());
				response.setData(jsonWriter.isEmpty() ? STR_EMPTY : jsonWriter.getObject());
				/*if (sw.toString().equals(STR_EMPTY)) {
					response.setData(STR_EMPTY);
				} else {
					try {
						response.setData(new JSONArray(sw.toString()));
					} catch (JSONException e) {
						response.setData(new JSONObject(sw.toString()));
					}
				}*/
			} catch (OXMandatoryFieldException e) {
				LOG.error(e.getMessage(), e);
				response.setException(e);
			} catch (OXConflictException e) {
				LOG.error(e.getMessage(), e);
				response.setException(e);
			} catch (OXException e) {
				if (e.getCategory() == Category.USER_INPUT) {
					LOG.debug(e.getMessage(), e);
				} else {
					LOG.error(e.getMessage(), e);
				}

				response.setException(e);
			} catch (SearchIteratorException e) {
				LOG.error(e.getMessage(), e);
				response.setException(e);
			} catch (AjaxException e) {
				LOG.error(e.getMessage(), e);
				response.setException(e);
			} catch (JSONException e) {
				final OXJSONException oje = new OXJSONException(OXJSONException.Code.JSON_WRITE_ERROR, e);
				LOG.error(oje.getMessage(), oje);
				response.setException(oje);
            } catch (OXJSONException e) {
                LOG.error(e.getMessage(), e);
                response.setException(e);
			}
		} else if (module.equals(MODULE_CONTACT)) {
			final ContactRequest contactRequest = new ContactRequest(sessionObj, jsonWriter);
			try {
				contactRequest.action(action, jsonObj);
				response.setTimestamp(contactRequest.getTimestamp());
				response.setData(jsonWriter.isEmpty() ? STR_EMPTY : jsonWriter.getObject());
			} catch (OXMandatoryFieldException e) {
				LOG.error(e.getMessage(), e);
				response.setException(e);
			} catch (OXConcurrentModificationException e) {
				LOG.error(e.getMessage(), e);
				response.setException(e);
			} catch (SearchIteratorException e) {
				LOG.error(e.getMessage(), e);
				response.setException(e);
			} catch (AjaxException e) {
				LOG.error(e.getMessage(), e);
				response.setException(e);
			} catch (OXException e) {
				LOG.error(e.getMessage(), e);
				response.setException(e);
			} catch (JSONException e) {
				final OXJSONException oje = new OXJSONException(OXJSONException.Code.JSON_WRITE_ERROR, e);
				LOG.error(oje.getMessage(), oje);
				response.setException(oje);
			}
		} else if (module.equals(MODULE_GROUP)) {
			final GroupRequest groupRequest = new GroupRequest(sessionObj, jsonWriter);
			try {
				groupRequest.action(action, jsonObj);
				response.setTimestamp(groupRequest.getTimestamp());
				response.setData(jsonWriter.isEmpty() ? STR_EMPTY : jsonWriter.getObject());
			} catch (OXMandatoryFieldException e) {
				LOG.error(e.getMessage(), e);
				response.setException(e);
			} catch (LdapException e) {
				LOG.error(e.getMessage(), e);
				response.setException(e);
			} catch (SearchIteratorException e) {
				LOG.error(e.getMessage(), e);
				response.setException(e);
			} catch (AjaxException e) {
				LOG.error(e.getMessage(), e);
				response.setException(e);
			} catch (JSONException e) {
				final OXJSONException oje = new OXJSONException(OXJSONException.Code.JSON_WRITE_ERROR, e);
				LOG.error(oje.getMessage(), oje);
				response.setException(oje);
			}
		} else if (module.equals(MODULE_REMINDER)) {
			final ReminderRequest reminderRequest = new ReminderRequest(sessionObj, jsonWriter);
			try {
				reminderRequest.action(action, jsonObj);
				response.setTimestamp(reminderRequest.getTimestamp());
				response.setData(jsonWriter.isEmpty() ? STR_EMPTY : jsonWriter.getObject());
			} catch (OXMandatoryFieldException e) {
				LOG.error(e.getMessage(), e);
				response.setException(e);
			} catch (OXException e) {
				LOG.error(e.getMessage(), e);
				response.setException(e);
			} catch (SearchIteratorException e) {
				LOG.error(e.getMessage(), e);
				response.setException(e);
			} catch (AjaxException e) {
				LOG.error(e.getMessage(), e);
				response.setException(e);
			} catch (JSONException e) {
				final OXJSONException oje = new OXJSONException(OXJSONException.Code.JSON_WRITE_ERROR, e);
				LOG.error(oje.getMessage(), oje);
				response.setException(oje);
			}
		} else if (module.equals(MODULE_RESOURCE)) {
			final ResourceRequest resourceRequest = new ResourceRequest(sessionObj, jsonWriter);
			try {
				resourceRequest.action(action, jsonObj);
				response.setTimestamp(resourceRequest.getTimestamp());
				response.setData(jsonWriter.isEmpty() ? STR_EMPTY : jsonWriter.getObject());
			} catch (OXMandatoryFieldException e) {
				LOG.error(e.getMessage(), e);
				response.setException(e);
			} catch (LdapException e) {
				LOG.error(e.getMessage(), e);
				response.setException(e);
			} catch (SearchIteratorException e) {
				LOG.error(e.getMessage(), e);
				response.setException(e);
			} catch (AjaxException e) {
				LOG.error(e.getMessage(), e);
				response.setException(e);
			} catch (JSONException e) {
				final OXJSONException oje = new OXJSONException(OXJSONException.Code.JSON_WRITE_ERROR, e);
				LOG.error(oje.getMessage(), oje);
				response.setException(oje);
			}
		} else if (module.equals(MODULE_TASK)) {
			final TaskRequest taskRequest = new TaskRequest(sessionObj, jsonWriter);
			int retval;
			try {
				retval = taskRequest.action(action, jsonObj);
				response.setTimestamp(taskRequest.getTimestamp());
				if (retval != -1) {
					response.setData(Integer.valueOf(retval));
				} else if (jsonWriter.isEmpty()) {
					response.setData(STR_EMPTY);
				} else {
					response.setData(jsonWriter.getObject());
				}
				/*if (retval != -1) {
					response.setData(Integer.valueOf(retval));
				} else if (sw.toString().equals(STR_EMPTY)) {
					response.setData(STR_EMPTY);
				} else {
					try {
						response.setData(new JSONArray(sw.toString()));
					} catch (JSONException e) {
						response.setData(new JSONObject(sw.toString()));
					}
				}*/
			} catch (OXFolderNotFoundException e) {
				LOG.error(e.getMessage(), e);
				response.setException(e);
			} catch (OXMandatoryFieldException e) {
				LOG.error(e.getMessage(), e);
				response.setException(e);
			} catch (OXObjectNotFoundException e) {
				LOG.error(e.getMessage(), e);
				response.setException(e);
			} catch (OXConflictException e) {
				LOG.error(e.getMessage(), e);
				response.setException(e);
			} catch (OXPermissionException e) {
				LOG.error(e.getMessage(), e);
				response.setException(e);
			} catch (SearchIteratorException e) {
				LOG.error(e.getMessage(), e);
				response.setException(e);
			} catch (AjaxException e) {
				LOG.error(e.getMessage(), e);
				response.setException(e);
			} catch (OXException e) {
				LOG.error(e.getMessage(), e);
				response.setException(e);
			} catch (JSONException e) {
				final OXJSONException oje = new OXJSONException(OXJSONException.Code.JSON_WRITE_ERROR, e);
				LOG.error(oje.getMessage(), oje);
				response.setException(oje);
            } catch (OXJSONException e) {
                LOG.error(e.getMessage(), e);
                response.setException(e);
			}
		} else if (module.equals(MODULE_INFOSTORE)) {
			final InfostoreRequest infoRequest = new InfostoreRequest(sessionObj, jsonWriter);
			infoRequest.action(action, new JSONSimpleRequest(jsonObj));
			
			
			if (jsonWriter.isEmpty()) {
				response.setData(STR_EMPTY);
			} else {
				response = new Response((JSONObject) jsonWriter.getObject());
			}
		} else if (module.equals(MODULE_FOLDER)) {
			final FolderRequest folderequest = new FolderRequest(sessionObj, jsonWriter);
			try {
				folderequest.action(action, jsonObj);
				if (jsonWriter.isEmpty()) {
					response.setData(STR_EMPTY);
				} else {
					response = new Response((JSONObject) jsonWriter.getObject());
				}
			} catch (OXFolderException e) {
				LOG.error(e.getMessage(), e);
				response.setException(e);
			} catch (JSONException e) {
				final OXJSONException oje = new OXJSONException(OXJSONException.Code.JSON_WRITE_ERROR, e);
				LOG.error(oje.getMessage(), oje);
				response.setException(oje);
			}
		} else if (module.equals(MODULE_MAIL)) {
			try {
				/*
				 * Fetch or create mail request object
				 */
				final boolean storeMailRequest;
				final MailRequest mailrequest;
				Object tmp = req.getAttribute(ATTRIBUTE_MAIL_REQUEST);
				if (tmp == null) {
					mailrequest = new MailRequest(sessionObj, jsonWriter);
					storeMailRequest = true;
				} else {
					mailrequest = (MailRequest) tmp;
					storeMailRequest = false;
				}
				/*
				 * Fetch or create mail interface object
				 */
				final MailInterface mi;
				tmp = req.getAttribute(ATTRIBUTE_MAIL_INTERFACE);
				if (tmp == null) {
					mi = MailInterfaceImpl.getInstance(sessionObj);
					req.setAttribute(ATTRIBUTE_MAIL_INTERFACE, mi);
				} else {
					mi = ((MailInterface) tmp);
				}
				mailrequest.action(action, jsonObj, mi);
				if (mailrequest.hasMultiple()) {
					/*
					 * Put into attributes to further collect move/copy requests
					 * and return a null reference to avoid writing response
					 * object
					 */
					if (storeMailRequest) {
						req.setAttribute(ATTRIBUTE_MAIL_REQUEST, mailrequest);
					}
					return null;
				}
				final JSONArray ja = mailrequest.getContent();
				final int len = ja.length();
				if (len == 0) {
					response.setData(STR_EMPTY);
				} else if (len == 1) {
					response = new Response(ja.getJSONObject(0));
				} else {
					LOG.error("Non-Continguous mail request with more than one response");
				}
				/*if (mailrequest.getContent().equals(STR_EMPTY)) {
					response.setData(STR_EMPTY);
				} else {
					response = Response.parse(mailrequest.getContent());
				}*/
			} catch (OXException e) {
				LOG.error(e.getMessage(), e);
				response.setException(e);
			} catch (SearchIteratorException e) {
				LOG.error(e.getMessage(), e);
				response.setException(e);
			} catch (JSONException e) {
				final OXJSONException oje = new OXJSONException(OXJSONException.Code.JSON_WRITE_ERROR, e);
				LOG.error(oje.getMessage(), oje);
				response.setException(oje);
			}
		} else {
			throw new AjaxException(AjaxException.Code.UnknownAction, action);
		}
		return response;
	}
}
