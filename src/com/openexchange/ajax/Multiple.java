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
		JSONArray jsonArray = null;
		final String data = getBody(req);

		try {
			jsonArray = new JSONArray(data);
		} catch (final JSONException e) {
			final AbstractOXException exc = new OXJSONException(OXJSONException.Code.JSON_READ_ERROR, e, data);
			LOG.error(exc.getMessage() + Tools.logHeaderForError(req), exc);
			jsonArray = new JSONArray();
		}
		
		final JSONArray respArr = new JSONArray();
		try {

			for (int a = 0; a < jsonArray.length(); a++) {
				parseActionElement(respArr, jsonArray, a, getSessionObject(req), req);
			}
			/*
			 * Don't forget to write mail request
			 */
			writeMailRequest(req);
		} catch (final JSONException e) {
			log(RESPONSE_ERROR, e);
			sendError(resp);
		} catch (final AjaxException e) {
			log(RESPONSE_ERROR, e);
			sendError(resp);
		} catch (final AbstractOXException e) {
			log(RESPONSE_ERROR, e);
			sendError(resp);
		} finally {
			final MailInterface mi = (MailInterface) req.getAttribute(ATTRIBUTE_MAIL_INTERFACE);
			if (mi != null) {
				try {
					mi.close(true);
				} catch (final OXException e) {
					LOG.error(e.getMessage(), e);
				}
			}
		}
		resp.setStatus(HttpServletResponse.SC_OK);
		resp.setContentType(CONTENTTYPE_JAVASCRIPT);
		final Writer writer = resp.getWriter();
		writer.write(respArr.toString());
		writer.flush();
	}

	protected static final void parseActionElement(final JSONArray respArr, final JSONArray jsonArray, final int pos,
			final SessionObject sessionObj, final HttpServletRequest req) throws JSONException, AjaxException,
			OXException {
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

		final OXJSONWriter jWriter = new OXJSONWriter(respArr);

		doAction(module, action, jsonObj, sessionObj, req, jWriter);
	}

	private static final void writeMailRequest(final HttpServletRequest req) throws JSONException {
		final MailRequest mailReq = (MailRequest) req.getAttribute(ATTRIBUTE_MAIL_REQUEST);
		if (mailReq != null) {
			try {
				/*
				 * Write withheld mail response first
				 */
				mailReq.performMultiple((MailInterface) req.getAttribute(ATTRIBUTE_MAIL_INTERFACE));
			} finally {
				/*
				 * Remove mail request object
				 */
				req.setAttribute(ATTRIBUTE_MAIL_REQUEST, null);
			}
		}
	}

	protected static final void doAction(final String module, final String action, final JSONObject jsonObj,
			final SessionObject sessionObj, final HttpServletRequest req, final OXJSONWriter jsonWriter)
			throws AjaxException {
		try {
			if (module.equals(MODULE_CALENDAR)) {
				writeMailRequest(req);
				final AppointmentRequest appointmentRequest = new AppointmentRequest(sessionObj);
				jsonWriter.object();
				try {
					jsonWriter.key(Response.DATA);
					jsonWriter.value(appointmentRequest.action(action, jsonObj));
					if (jsonWriter.isExpectingValue()) {
						jsonWriter.value("");
					}
					if (null != appointmentRequest.getTimestamp()) {
						jsonWriter.key(Response.TIMESTAMP).value(appointmentRequest.getTimestamp().getTime());
					}
				} catch (final OXMandatoryFieldException e) {
					LOG.error(e.getMessage(), e);
					if (jsonWriter.isExpectingValue()) {
						jsonWriter.value("");
					}
					Response.writeException(e, jsonWriter);
				} catch (final OXConflictException e) {
					LOG.error(e.getMessage(), e);
					if (jsonWriter.isExpectingValue()) {
						jsonWriter.value("");
					}
					Response.writeException(e, jsonWriter);
				} catch (final OXException e) {
					if (e.getCategory() == Category.USER_INPUT) {
						LOG.debug(e.getMessage(), e);
					} else {
						LOG.error(e.getMessage(), e);
					}
					if (jsonWriter.isExpectingValue()) {
						jsonWriter.value("");
					}
					Response.writeException(e, jsonWriter);
				} catch (final SearchIteratorException e) {
					LOG.error(e.getMessage(), e);
					if (jsonWriter.isExpectingValue()) {
						jsonWriter.value("");
					}
					Response.writeException(e, jsonWriter);
				} catch (final AjaxException e) {
					LOG.error(e.getMessage(), e);
					if (jsonWriter.isExpectingValue()) {
						jsonWriter.value("");
					}
					Response.writeException(e, jsonWriter);
				} catch (final JSONException e) {
					final OXJSONException oje = new OXJSONException(OXJSONException.Code.JSON_WRITE_ERROR, e);
					LOG.error(oje.getMessage(), oje);
					if (jsonWriter.isExpectingValue()) {
						jsonWriter.value("");
					}
					Response.writeException(oje, jsonWriter);
				} catch (final OXJSONException e) {
					LOG.error(e.getMessage(), e);
					if (jsonWriter.isExpectingValue()) {
						jsonWriter.value("");
					}
					Response.writeException(e, jsonWriter);
				} finally {
					jsonWriter.endObject();
				}
			} else if (module.equals(MODULE_CONTACT)) {
				writeMailRequest(req);
				final ContactRequest contactRequest = new ContactRequest(sessionObj);
				jsonWriter.object();
				try {
					jsonWriter.key(Response.DATA);
					jsonWriter.value(contactRequest.action(action, jsonObj));
					if (jsonWriter.isExpectingValue()) {
						jsonWriter.value("");
					}
					if (null != contactRequest.getTimestamp()) {
						jsonWriter.key(Response.TIMESTAMP).value(contactRequest.getTimestamp().getTime());
					}
				} catch (final OXMandatoryFieldException e) {
					LOG.error(e.getMessage(), e);
					if (jsonWriter.isExpectingValue()) {
						jsonWriter.value("");
					}
					Response.writeException(e, jsonWriter);
				} catch (final OXConcurrentModificationException e) {
					LOG.error(e.getMessage(), e);
					if (jsonWriter.isExpectingValue()) {
						jsonWriter.value("");
					}
					Response.writeException(e, jsonWriter);
				} catch (final SearchIteratorException e) {
					LOG.error(e.getMessage(), e);
					if (jsonWriter.isExpectingValue()) {
						jsonWriter.value("");
					}
					Response.writeException(e, jsonWriter);
				} catch (final AjaxException e) {
					LOG.error(e.getMessage(), e);
					if (jsonWriter.isExpectingValue()) {
						jsonWriter.value("");
					}
					Response.writeException(e, jsonWriter);
				} catch (final OXException e) {
					LOG.error(e.getMessage(), e);
					if (jsonWriter.isExpectingValue()) {
						jsonWriter.value("");
					}
					Response.writeException(e, jsonWriter);
				} catch (final OXJSONException exc) {
					LOG.error(exc.getMessage(), exc);
					if (jsonWriter.isExpectingValue()) {
						jsonWriter.value("");
					}
					Response.writeException(exc, jsonWriter);
				} catch (final JSONException e) {
					final OXJSONException oje = new OXJSONException(OXJSONException.Code.JSON_WRITE_ERROR, e);
					LOG.error(oje.getMessage(), oje);
					if (jsonWriter.isExpectingValue()) {
						jsonWriter.value("");
					}
					Response.writeException(oje, jsonWriter);
				} finally {
					jsonWriter.endObject();
				}
			} else if (module.equals(MODULE_GROUP)) {
				writeMailRequest(req);
				final GroupRequest groupRequest = new GroupRequest(sessionObj);
				jsonWriter.object();
				try {
					jsonWriter.key(Response.DATA);
					jsonWriter.value(groupRequest.action(action, jsonObj));
					if (jsonWriter.isExpectingValue()) {
						jsonWriter.value("");
					}
					if (null != groupRequest.getTimestamp()) {
						jsonWriter.key(Response.TIMESTAMP).value(groupRequest.getTimestamp().getTime());
					}
				} catch (final OXMandatoryFieldException e) {
					LOG.error(e.getMessage(), e);
					if (jsonWriter.isExpectingValue()) {
						jsonWriter.value("");
					}
					Response.writeException(e, jsonWriter);
				} catch (final LdapException e) {
					LOG.error(e.getMessage(), e);
					if (jsonWriter.isExpectingValue()) {
						jsonWriter.value("");
					}
					Response.writeException(e, jsonWriter);
				} catch (final SearchIteratorException e) {
					LOG.error(e.getMessage(), e);
					if (jsonWriter.isExpectingValue()) {
						jsonWriter.value("");
					}
					Response.writeException(e, jsonWriter);
				} catch (final AjaxException e) {
					LOG.error(e.getMessage(), e);
					if (jsonWriter.isExpectingValue()) {
						jsonWriter.value("");
					}
					Response.writeException(e, jsonWriter);
				} catch (final OXJSONException exc) {
					LOG.error(exc.getMessage(), exc);
					if (jsonWriter.isExpectingValue()) {
						jsonWriter.value("");
					}
					Response.writeException(exc, jsonWriter);
				} catch (final JSONException e) {
					final OXJSONException oje = new OXJSONException(OXJSONException.Code.JSON_WRITE_ERROR, e);
					LOG.error(oje.getMessage(), oje);
					if (jsonWriter.isExpectingValue()) {
						jsonWriter.value("");
					}
					Response.writeException(oje, jsonWriter);
				} finally {
					jsonWriter.endObject();
				}
			} else if (module.equals(MODULE_REMINDER)) {
				writeMailRequest(req);
				final ReminderRequest reminderRequest = new ReminderRequest(sessionObj);
				jsonWriter.object();
				try {
					jsonWriter.key(Response.DATA);
					jsonWriter.value(reminderRequest.action(action, jsonObj));
					if (jsonWriter.isExpectingValue()) {
						jsonWriter.value("");
					}
					if (null != reminderRequest.getTimestamp()) {
						jsonWriter.key(Response.TIMESTAMP).value(reminderRequest.getTimestamp().getTime());
					}
				} catch (final OXMandatoryFieldException e) {
					LOG.error(e.getMessage(), e);
					if (jsonWriter.isExpectingValue()) {
						jsonWriter.value("");
					}
					Response.writeException(e, jsonWriter);
				} catch (final OXException e) {
					LOG.error(e.getMessage(), e);
					if (jsonWriter.isExpectingValue()) {
						jsonWriter.value("");
					}
					Response.writeException(e, jsonWriter);
				} catch (final SearchIteratorException e) {
					LOG.error(e.getMessage(), e);
					if (jsonWriter.isExpectingValue()) {
						jsonWriter.value("");
					}
					Response.writeException(e, jsonWriter);
				} catch (final AjaxException e) {
					LOG.error(e.getMessage(), e);
					if (jsonWriter.isExpectingValue()) {
						jsonWriter.value("");
					}
					Response.writeException(e, jsonWriter);
				} catch (final OXJSONException exc) {
					LOG.error(exc.getMessage(), exc);
					if (jsonWriter.isExpectingValue()) {
						jsonWriter.value("");
					}
					Response.writeException(exc, jsonWriter);
				} catch (final JSONException e) {
					final OXJSONException oje = new OXJSONException(OXJSONException.Code.JSON_WRITE_ERROR, e);
					LOG.error(oje.getMessage(), oje);
					if (jsonWriter.isExpectingValue()) {
						jsonWriter.value("");
					}
					Response.writeException(oje, jsonWriter);
				} finally {
					jsonWriter.endObject();
				}
			} else if (module.equals(MODULE_RESOURCE)) {
				writeMailRequest(req);
				final ResourceRequest resourceRequest = new ResourceRequest(sessionObj);
				jsonWriter.object();
				try {
					jsonWriter.key(Response.DATA);
					jsonWriter.value(resourceRequest.action(action, jsonObj));
					if (jsonWriter.isExpectingValue()) {
						jsonWriter.value("");
					}
					if (null != resourceRequest.getTimestamp()) {
						jsonWriter.key(Response.TIMESTAMP).value(resourceRequest.getTimestamp().getTime());
					}
				} catch (final OXMandatoryFieldException e) {
					LOG.error(e.getMessage(), e);
					if (jsonWriter.isExpectingValue()) {
						jsonWriter.value("");
					}
					Response.writeException(e, jsonWriter);
				} catch (final LdapException e) {
					LOG.error(e.getMessage(), e);
					if (jsonWriter.isExpectingValue()) {
						jsonWriter.value("");
					}
					Response.writeException(e, jsonWriter);
				} catch (final SearchIteratorException e) {
					LOG.error(e.getMessage(), e);
					if (jsonWriter.isExpectingValue()) {
						jsonWriter.value("");
					}
					Response.writeException(e, jsonWriter);
				} catch (final AjaxException e) {
					LOG.error(e.getMessage(), e);
					if (jsonWriter.isExpectingValue()) {
						jsonWriter.value("");
					}
					Response.writeException(e, jsonWriter);
				} catch (final OXJSONException exc) {
					LOG.error(exc.getMessage(), exc);
					if (jsonWriter.isExpectingValue()) {
						jsonWriter.value("");
					}
					Response.writeException(exc, jsonWriter);
				} catch (final JSONException e) {
					final OXJSONException oje = new OXJSONException(OXJSONException.Code.JSON_WRITE_ERROR, e);
					LOG.error(oje.getMessage(), oje);
					if (jsonWriter.isExpectingValue()) {
						jsonWriter.value("");
					}
					Response.writeException(oje, jsonWriter);
				} finally {
					jsonWriter.endObject();
				}
			} else if (module.equals(MODULE_TASK)) {
				writeMailRequest(req);
				final TaskRequest taskRequest = new TaskRequest(sessionObj);
				jsonWriter.object();
				try {
					jsonWriter.key(Response.DATA);
					jsonWriter.value(taskRequest.action(action, jsonObj));
					if (null != taskRequest.getTimestamp()) {
						jsonWriter.key(Response.TIMESTAMP).value(taskRequest.getTimestamp().getTime());
					}
				} catch (final OXFolderNotFoundException e) {
					LOG.error(e.getMessage(), e);
					if (jsonWriter.isExpectingValue()) {
						jsonWriter.value("");
					}
					Response.writeException(e, jsonWriter);
				} catch (final OXMandatoryFieldException e) {
					LOG.error(e.getMessage(), e);
					if (jsonWriter.isExpectingValue()) {
						jsonWriter.value("");
					}
					Response.writeException(e, jsonWriter);
				} catch (final OXObjectNotFoundException e) {
					LOG.error(e.getMessage(), e);
					if (jsonWriter.isExpectingValue()) {
						jsonWriter.value("");
					}
					Response.writeException(e, jsonWriter);
				} catch (final OXConflictException e) {
					LOG.error(e.getMessage(), e);
					if (jsonWriter.isExpectingValue()) {
						jsonWriter.value("");
					}
					Response.writeException(e, jsonWriter);
				} catch (final OXPermissionException e) {
					LOG.error(e.getMessage(), e);
					if (jsonWriter.isExpectingValue()) {
						jsonWriter.value("");
					}
					Response.writeException(e, jsonWriter);
				} catch (final SearchIteratorException e) {
					LOG.error(e.getMessage(), e);
					if (jsonWriter.isExpectingValue()) {
						jsonWriter.value("");
					}
					Response.writeException(e, jsonWriter);
				} catch (final AjaxException e) {
					LOG.error(e.getMessage(), e);
					if (jsonWriter.isExpectingValue()) {
						jsonWriter.value("");
					}
					Response.writeException(e, jsonWriter);
				} catch (final OXException e) {
					LOG.error(e.getMessage(), e);
					if (jsonWriter.isExpectingValue()) {
						jsonWriter.value("");
					}
					Response.writeException(e, jsonWriter);
				} catch (final JSONException e) {
					final OXJSONException oje = new OXJSONException(OXJSONException.Code.JSON_WRITE_ERROR, e);
					LOG.error(oje.getMessage(), oje);
					if (jsonWriter.isExpectingValue()) {
						jsonWriter.value("");
					}
					Response.writeException(oje, jsonWriter);
				} catch (final OXJSONException e) {
					LOG.error(e.getMessage(), e);
					if (jsonWriter.isExpectingValue()) {
						jsonWriter.value("");
					}
					Response.writeException(e, jsonWriter);
				} finally {
					jsonWriter.endObject();
				}
			} else if (module.equals(MODULE_INFOSTORE)) {
				writeMailRequest(req);
				final InfostoreRequest infoRequest = new InfostoreRequest(sessionObj, jsonWriter);
				try {
					infoRequest.action(action, new JSONSimpleRequest(jsonObj));
				} catch (final OXPermissionException e) {
					jsonWriter.object();
					Response.writeException(e, jsonWriter);
					jsonWriter.endObject();
				}
			} else if (module.equals(MODULE_FOLDER)) {
				writeMailRequest(req);
				final FolderRequest folderequest = new FolderRequest(sessionObj, jsonWriter);
				try {
					folderequest.action(action, jsonObj);
				} catch (final OXFolderException e) {
					LOG.error(e.getMessage(), e);
					jsonWriter.object();
					Response.writeException(e, jsonWriter);
					jsonWriter.endObject();
				} catch (final JSONException e) {
					final OXJSONException oje = new OXJSONException(OXJSONException.Code.JSON_WRITE_ERROR, e);
					LOG.error(oje.getMessage(), oje);
					jsonWriter.object();
					Response.writeException(oje, jsonWriter);
					jsonWriter.endObject();
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
					if (mailrequest.isContinuousCollect()) {
						/*
						 * Put into attributes to further collect move/copy
						 * requests and return a null reference to avoid writing
						 * response object
						 */
						if (storeMailRequest) {
							req.setAttribute(ATTRIBUTE_MAIL_REQUEST, mailrequest);
						}
						return;
					}
				} catch (final OXException e) {
					LOG.error(e.getMessage(), e);
					jsonWriter.object();
					Response.writeException(e, jsonWriter);
					jsonWriter.endObject();
				} catch (final SearchIteratorException e) {
					LOG.error(e.getMessage(), e);
					jsonWriter.object();
					Response.writeException(e, jsonWriter);
					jsonWriter.endObject();
				} catch (final JSONException e) {
					final OXJSONException oje = new OXJSONException(OXJSONException.Code.JSON_WRITE_ERROR, e);
					LOG.error(oje.getMessage(), oje);
					jsonWriter.object();
					Response.writeException(oje, jsonWriter);
					jsonWriter.endObject();
				}
			} else {
				throw new AjaxException(AjaxException.Code.UnknownAction, action);
			}
		} catch (final JSONException e) {
			/*
			 * Cannot occur
			 */
			LOG.error(e.getLocalizedMessage(), e);
		}
	}
}
