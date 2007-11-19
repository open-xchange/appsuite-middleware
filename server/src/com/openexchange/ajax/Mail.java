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

import static com.openexchange.ajax.request.MailRequest.MAIL_SERVLET;
import static com.openexchange.tools.oxfolder.OXFolderManagerImpl.getFolderName;
import static com.openexchange.tools.oxfolder.OXFolderManagerImpl.getUserName;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONWriter;

import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.fields.CommonFields;
import com.openexchange.ajax.fields.FolderFields;
import com.openexchange.ajax.helper.ParamContainer;
import com.openexchange.ajax.parser.InfostoreParser;
import com.openexchange.api.OXMandatoryFieldException;
import com.openexchange.api.OXPermissionException;
import com.openexchange.api2.OXException;
import com.openexchange.configuration.ConfigurationException;
import com.openexchange.configuration.ServerConfig;
import com.openexchange.configuration.ServerConfig.Property;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.Component;
import com.openexchange.groupware.AbstractOXException.Category;
import com.openexchange.groupware.container.CommonObject;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.infostore.InfostoreFacade;
import com.openexchange.groupware.infostore.utils.Metadata;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.upload.impl.UploadEvent;
import com.openexchange.groupware.upload.impl.UploadException;
import com.openexchange.groupware.upload.impl.UploadFile;
import com.openexchange.groupware.upload.impl.UploadListener;
import com.openexchange.groupware.upload.impl.UploadRegistry;
import com.openexchange.groupware.userconfiguration.UserConfigurationStorage;
import com.openexchange.json.OXJSONWriter;
import com.openexchange.mail.MailException;
import com.openexchange.mail.MailInterface;
import com.openexchange.mail.MailInterfaceImpl;
import com.openexchange.mail.MailJSONField;
import com.openexchange.mail.MailListField;
import com.openexchange.mail.MailPath;
import com.openexchange.mail.MailStorageUtils.OrderDirection;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.dataobjects.TransportMailMessage;
import com.openexchange.mail.json.parser.MessageParser;
import com.openexchange.mail.json.writer.MessageWriter;
import com.openexchange.mail.json.writer.MessageWriter.MailFieldWriter;
import com.openexchange.mail.mime.MIMEType2ExtMap;
import com.openexchange.mail.transport.SendType;
import com.openexchange.mail.usersetting.UserSettingMail;
import com.openexchange.mail.usersetting.UserSettingMailStorage;
import com.openexchange.server.EffectivePermission;
import com.openexchange.session.Session;
import com.openexchange.tools.encoding.Helper;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorException;
import com.openexchange.tools.mail.ContentType;
import com.openexchange.tools.oxfolder.OXFolderAccess;
import com.openexchange.tools.oxfolder.OXFolderException;
import com.openexchange.tools.oxfolder.OXFolderException.FolderCode;
import com.openexchange.tools.servlet.UnsynchronizedByteArrayOutputStream;
import com.openexchange.tools.servlet.UploadServletException;
import com.openexchange.tools.servlet.http.Tools;

/**
 * Mail - the servlet to handle mail requests
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public class Mail extends PermissionServlet implements UploadListener {

	private static final String MIME_MULTIPART = "multipart/";

	private static final String PARAMETER_PATTERN = "pattern";

	private static final String PARAMETER_COL = "col";

	private static final String MIME_TEXT_HTML_CHARSET_UTF_8 = "text/html; charset=UTF-8";

	private static final String STR_INLINE_FILENAME = "inline; filename=\"";

	private static final String STR_NAME = "name";

	private static final String MIME_APPLICATION_OCTET_STREAM = "application/octet-stream";

	private static final String MIME_TEXT_PLAIN = "text/plain";

	private static final String MIME_TEXT_HTML = "text/htm";

	private static final String STR_ATTACHMENT_FILENAME = "attachment; filename=\"";

	private static final String STR_CONTENT_DISPOSITION = "Content-disposition";

	private static final String STR_OCTET_STREAM = "octet-stream";

	private static final String STR_APPLICATION = "application";

	private static final String STR_WINDOWS = "windows";

	private static final String STR_MSIE = "msie";

	private static final String STR_USER_AGENT = "user-agent";

	private static final String STR_DELIM = ": ";

	private static final String STR_CRLF = "\r\n";

	private static final String STR_THREAD = "thread";

	private static final long serialVersionUID = 1980226522220313667L;

	private static final transient Log LOG = LogFactory.getLog(Mail.class);

	/**
	 * Error message if writing the response fails.
	 */
	private static final String RESPONSE_ERROR = "Error while writing response object.";

	private static final AbstractOXException getWrappingOXException(final Throwable cause) {
		return new AbstractOXException(Component.MAIL, Category.INTERNAL_ERROR, 9999, cause.getMessage(), cause);
	}

	public static final char SEPERATOR = '/';

	private static final String UPLOAD_PARAM_MAILINTERFACE = "mi";

	private static final String UPLOAD_PARAM_WRITER = "w";

	private static final String UPLOAD_PARAM_SESSION = "s";

	private static final String STR_CHARSET = "charset";

	private static final String STR_UTF8 = "UTF-8";

	private static final String STR_1 = "1";

	private static final String STR_EMPTY = "";

	private static final String STR_NULL = "null";

	/**
	 * The parameter 'folder' contains the folder's id whose contents are
	 * queried.
	 */
	public static final String PARAMETER_MAILFOLDER = "folder";

	public static final String PARAMETER_MAILATTCHMENT = "attachment";

	public static final String PARAMETER_DESTINATION_FOLDER = "dest_folder";

	public static final String PARAMETER_MAILCID = "cid";

	public static final String PARAMETER_SAVE = "save";

	public static final String PARAMETER_SHOW_SRC = "src";

	public static final String PARAMETER_SHOW_HEADER = "hdr";

	public static final String PARAMETER_EDIT_DRAFT = "edit";

	public static final String PARAMETER_SEND_TYPE = "sendtype";

	@Override
	protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
		resp.setContentType(CONTENTTYPE_JAVASCRIPT);
		/*
		 * The magic spell to disable caching
		 */
		Tools.disableCaching(resp);
		try {
			actionGet(req, resp);
		} catch (final Exception e) {
			LOG.error("doGet", e);
			writeError(e.toString(), new JSONWriter(resp.getWriter()));
		}
	}

	@Override
	protected void doPut(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
		resp.setContentType(CONTENTTYPE_JAVASCRIPT);
		/*
		 * The magic spell to disable caching
		 */
		Tools.disableCaching(resp);
		try {
			actionPut(req, resp);
		} catch (final Exception e) {
			LOG.error("doGet", e);
			writeError(e.toString(), new JSONWriter(resp.getWriter()));
		}
	}

	private final static void writeError(final String error, final JSONWriter jsonWriter) {
		try {
			startResponse(jsonWriter);
			jsonWriter.value(STR_EMPTY);
			endResponse(jsonWriter, null, error);
		} catch (final Exception exc) {
			LOG.error("writeError", exc);
		}
	}

	private final void actionGet(final HttpServletRequest req, final HttpServletResponse resp) throws Exception {
		final String actionStr = checkStringParam(req, PARAMETER_ACTION);
		if (actionStr.equalsIgnoreCase(ACTION_ALL)) {
			actionGetAllMails(req, resp);
		} else if (actionStr.equalsIgnoreCase(ACTION_COUNT)) {
			actionGetMailCount(req, resp);
		} else if (actionStr.equalsIgnoreCase(ACTION_UPDATES)) {
			actionGetUpdates(req, resp);
		} else if (actionStr.equalsIgnoreCase(ACTION_REPLY) || actionStr.equalsIgnoreCase(ACTION_REPLYALL)) {
			actionGetReply(req, resp, (actionStr.equalsIgnoreCase(ACTION_REPLYALL)));
		} else if (actionStr.equalsIgnoreCase(ACTION_FORWARD)) {
			actionGetForward(req, resp);
		} else if (actionStr.equalsIgnoreCase(ACTION_GET)) {
			actionGetMessage(req, resp);
		} else if (actionStr.equalsIgnoreCase(ACTION_MATTACH)) {
			actionGetAttachment(req, resp);
		} else if (actionStr.equalsIgnoreCase(ACTION_NEW_MSGS)) {
			actionGetNew(req, resp);
		} else if (actionStr.equalsIgnoreCase(ACTION_SAVE_VERSIT)) {
			actionGetSaveVersit(req, resp);
		} else {
			throw new Exception("Unknown value in parameter " + PARAMETER_ACTION + " through GET command");
		}
	}

	private final void actionPut(final HttpServletRequest req, final HttpServletResponse resp) throws Exception {
		final String actionStr = checkStringParam(req, PARAMETER_ACTION);
		if (actionStr.equalsIgnoreCase(ACTION_LIST)) {
			actionPutMailList(req, resp);
		} else if (actionStr.equalsIgnoreCase(ACTION_DELETE)) {
			actionPutDeleteMails(req, resp);
		} else if (actionStr.equalsIgnoreCase(ACTION_UPDATE)) {
			actionPutUpdateMail(req, resp);
		} else if (actionStr.equalsIgnoreCase(ACTION_COPY)) {
			actionPutCopyMail(req, resp);
		} else if (actionStr.equalsIgnoreCase(ACTION_MATTACH)) {
			actionPutAttachment(req, resp);
		} else if (actionStr.equalsIgnoreCase(ACTION_MAIL_RECEIPT_ACK)) {
			actionPutReceiptAck(req, resp);
		} else if (actionStr.equalsIgnoreCase(ACTION_SEARCH)) {
			actionPutMailSearch(req, resp);
		} else if (actionStr.equalsIgnoreCase(ACTION_CLEAR)) {
			actionPutClear(req, resp);
		} else {
			throw new Exception("Unknown value in parameter " + PARAMETER_ACTION + " through PUT command");
		}
	}

	public void actionGetUpdates(final Session sessionObj, final JSONWriter writer, final JSONObject requestObj,
			final MailInterface mi) throws JSONException {
		Response
				.write(actionGetUpdates(sessionObj, ParamContainer.getInstance(requestObj, Component.MAIL), mi), writer);
	}

	private final void actionGetUpdates(final HttpServletRequest req, final HttpServletResponse resp)
			throws IOException, ServletException {
		try {
			Response.write(actionGetUpdates(getSessionObject(req), ParamContainer
					.getInstance(req, Component.MAIL, resp), null), resp.getWriter());
		} catch (final JSONException e) {
			sendErrorAsJS(resp, RESPONSE_ERROR);
		}
	}

	private final transient static JSONArray EMPTY_JSON_ARR = new JSONArray();

	private final Response actionGetUpdates(final Session sessionObj, final ParamContainer paramContainer,
			final MailInterface mailInterfaceArg) throws JSONException {
		/*
		 * Send an empty array cause ACTION=UPDATES is not supported for IMAP
		 * messages
		 */
		final Response response = new Response();
		/*
		 * Close response and flush print writer
		 */
		response.setData(EMPTY_JSON_ARR);
		response.setTimestamp(null);
		return response;
	}

	public void actionGetMailCount(final Session sessionObj, final JSONWriter writer,
			final JSONObject requestObj, final MailInterface mi) throws JSONException {
		Response.write(actionGetMailCount(sessionObj, ParamContainer.getInstance(requestObj, Component.MAIL), mi),
				writer);
	}

	private final void actionGetMailCount(final HttpServletRequest req, final HttpServletResponse resp)
			throws IOException, ServletException {
		try {
			Response.write(actionGetMailCount(getSessionObject(req), ParamContainer.getInstance(req, Component.MAIL,
					resp), null), resp.getWriter());
		} catch (final JSONException e) {
			sendErrorAsJS(resp, RESPONSE_ERROR);
		}
	}

	private final Response actionGetMailCount(final Session sessionObj, final ParamContainer paramContainer,
			final MailInterface mailInterfaceArg) {
		/*
		 * Some variables
		 */
		final Response response = new Response();
		/*
		 * Start response
		 */
		Object data = JSONObject.NULL;
		try {
			final String folderId = paramContainer.checkStringParam(PARAMETER_MAILFOLDER);
			MailInterface mailInterface = mailInterfaceArg;
			boolean closeMailInterface = false;
			try {
				if (mailInterface == null) {
					mailInterface = MailInterface.getInstance(sessionObj);
					closeMailInterface = true;
				}
				data = Integer.valueOf(mailInterface.getAllMessageCount(folderId)[0]);
			} finally {
				if (closeMailInterface && mailInterface != null) {
					mailInterface.close(true);
				}
			}
		} catch (final MailException e) {
			LOG.error(e.getMessage(), e);
			if (!e.getCategory().equals(Category.USER_CONFIGURATION)) {
				response.setException(e);
			}
		} catch (final AbstractOXException e) {
			response.setException(e);
		} catch (final Exception e) {
			LOG.error("actionGetMailCount", e);
			response.setException(getWrappingOXException(e));
		}
		/*
		 * Close response and flush print writer
		 */
		response.setData(data);
		response.setTimestamp(null);
		return response;
	}

	public void actionGetAllMails(final Session sessionObj, final JSONWriter writer, final JSONObject requestObj,
			final MailInterface mi) throws SearchIteratorException, JSONException {
		Response.write(actionGetAllMails(sessionObj, ParamContainer.getInstance(requestObj, Component.MAIL), mi),
				writer);
	}

	private final void actionGetAllMails(final HttpServletRequest req, final HttpServletResponse resp)
			throws IOException, ServletException {
		try {
			Response.write(actionGetAllMails(getSessionObject(req), ParamContainer.getInstance(req, Component.MAIL,
					resp), null), resp.getWriter());
		} catch (final JSONException e) {
			sendErrorAsJS(resp, RESPONSE_ERROR);
		} catch (final SearchIteratorException e) {
			sendErrorAsJS(resp, RESPONSE_ERROR);
		}
	}

	private static final String STR_ASC = "asc";

	private static final String STR_DESC = "desc";

	private final Response actionGetAllMails(final Session sessionObj, final ParamContainer paramContainer,
			final MailInterface mailInterfaceArg) throws JSONException, SearchIteratorException {
		/*
		 * Some variables
		 */
		final Response response = new Response();
		final OXJSONWriter jsonWriter = new OXJSONWriter();
		/*
		 * Start response
		 */
		jsonWriter.array();
		SearchIterator<?> it = null;
		try {
			/*
			 * Read in parameters
			 */
			final String folderId = paramContainer.checkStringParam(PARAMETER_MAILFOLDER);
			final int[] columns = paramContainer.checkIntArrayParam(PARAMETER_COLUMNS);
			final String sort = paramContainer.getStringParam(PARAMETER_SORT);
			final String order = paramContainer.getStringParam(PARAMETER_ORDER);
			final boolean threadSort = (STR_THREAD.equalsIgnoreCase(sort));
			if (sort != null && !threadSort && order == null) {
				throw new MailException(MailException.Code.MISSING_PARAM, PARAMETER_ORDER);
			}
			/*
			 * Get all mails
			 */
			MailInterface mailInterface = mailInterfaceArg;
			boolean closeMailInterface = false;
			try {
				if (mailInterface == null) {
					mailInterface = MailInterface.getInstance(sessionObj);
					closeMailInterface = true;
				}
				/*
				 * Pre-Select field writers
				 */
				final MailFieldWriter[] writers = MessageWriter.getMailFieldWriter(MailListField.getFields(columns),
						sessionObj);
				/*
				 * Receive message iterator
				 */
				if (threadSort) {
					it = mailInterface.getAllThreadedMessages(folderId, columns);
					final int size = it.size();
					for (int i = 0; i < size; i++) {
						final MailMessage mail = (MailMessage) it.next();
						final JSONArray ja = new JSONArray();
						for (final MailFieldWriter writer : writers) {
							writer.writeField(ja, mail, mail.getThreadLevel(), false);
						}
						jsonWriter.value(ja);
					}
				} else {
					final int sortCol = sort == null ? MailListField.RECEIVED_DATE.getField() : Integer.parseInt(sort);
					int orderDir = OrderDirection.ASC.getOrder();
					if (order != null) {
						if (order.equalsIgnoreCase(STR_ASC)) {
							orderDir = OrderDirection.ASC.getOrder();
						} else if (order.equalsIgnoreCase(STR_DESC)) {
							orderDir = OrderDirection.DESC.getOrder();
						} else {
							throw new MailException(MailException.Code.INVALID_INT_VALUE, PARAMETER_ORDER);
						}
					}
					/*
					 * Get iterator
					 */
					it = mailInterface.getAllMessages(folderId, sortCol, orderDir, columns);
					final int size = it.size();
					for (int i = 0; i < size; i++) {
						final MailMessage mail = (MailMessage) it.next();
						final JSONArray ja = new JSONArray();
						for (final MailFieldWriter writer : writers) {
							writer.writeField(ja, mail, 0, false);
						}
						jsonWriter.value(ja);
					}
				}
			} finally {
				if (closeMailInterface && mailInterface != null) {
					mailInterface.close(true);
				}
			}
		} catch (final MailException e) {
			LOG.error(e.getMessage(), e);
			response.setException(e);
		} catch (final AbstractOXException e) {
			LOG.error(e.getMessage(), e);
			response.setException(e);
		} catch (final Exception e) {
			LOG.error("actionGetAllMails", e);
			response.setException(getWrappingOXException(e));
		} finally {
			if (it != null) {
				it.close();
			}
		}
		/*
		 * Close response and flush print writer
		 */
		jsonWriter.endArray();
		response.setData(jsonWriter.getObject());
		response.setTimestamp(null);
		return response;
	}

	public void actionGetReply(final Session sessionObj, final JSONWriter writer, final JSONObject jo,
			final boolean reply2all, final MailInterface mailInterface) throws JSONException {
		Response.write(actionGetReply(sessionObj, reply2all, ParamContainer.getInstance(jo, Component.MAIL),
				mailInterface), writer);
	}

	private final void actionGetReply(final HttpServletRequest req, final HttpServletResponse resp,
			final boolean reply2all) throws IOException, ServletException {
		try {
			Response.write(actionGetReply(getSessionObject(req), reply2all, ParamContainer.getInstance(req,
					Component.MAIL, resp), null), resp.getWriter());
		} catch (final JSONException e) {
			sendErrorAsJS(resp, RESPONSE_ERROR);
		}
	}

	private final Response actionGetReply(final Session sessionObj, final boolean reply2all,
			final ParamContainer paramContainer, final MailInterface mailInterfaceArg) {
		/*
		 * final Some variables
		 */
		final Response response = new Response();
		Object data = JSONObject.NULL;
		/*
		 * Start response
		 */
		try {
			/*
			 * Read in parameters
			 */
			final MailPath mailPath = new MailPath(paramContainer.checkStringParam(PARAMETER_ID));
			/*
			 * Get reply message
			 */
			MailInterface mailInterface = mailInterfaceArg;
			boolean closeMailInterface = false;
			try {
				if (mailInterfaceArg == null) {
					mailInterface = MailInterface.getInstance(sessionObj);
					closeMailInterface = true;
				}
				data = MessageWriter.writeMailMessage(mailInterface.getReplyMessageForDisplay(mailPath.getFolder(),
						mailPath.getUid(), reply2all), false, sessionObj);
			} finally {
				if (closeMailInterface && mailInterface != null) {
					mailInterface.close(true);
				}
			}
		} catch (final MailException e) {
			LOG.error(e.getMessage(), e);
			response.setException(e);
		} catch (final AbstractOXException e) {
			LOG.error(e.getMessage(), e);
			response.setException(e);
		} catch (final Exception e) {
			LOG.error("actionGetReply", e);
			response.setException(getWrappingOXException(e));
		}
		/*
		 * Close response and flush print writer
		 */
		response.setData(data);
		response.setTimestamp(null);
		return response;
	}

	public void actionGetForward(final Session sessionObj, final JSONWriter writer, final JSONObject requestObj,
			final MailInterface mailInterface) throws JSONException {
		Response.write(actionGetForward(sessionObj, ParamContainer.getInstance(requestObj, Component.MAIL),
				mailInterface), writer);
	}

	private final void actionGetForward(final HttpServletRequest req, final HttpServletResponse resp)
			throws IOException, ServletException {
		try {
			Response.write(actionGetForward(getSessionObject(req), ParamContainer
					.getInstance(req, Component.MAIL, resp), null), resp.getWriter());
		} catch (final JSONException e) {
			sendErrorAsJS(resp, RESPONSE_ERROR);
		}
	}

	private final Response actionGetForward(final Session sessionObj, final ParamContainer paramContainer,
			final MailInterface mailInterfaceArg) {
		/*
		 * Some variables
		 */
		final Response response = new Response();
		Object data = JSONObject.NULL;
		/*
		 * Start response
		 */
		try {
			/*
			 * Read in parameters
			 */
			final MailPath mailPath = new MailPath(paramContainer.checkStringParam(PARAMETER_ID));
			/*
			 * Get forward message
			 */
			MailInterface mailInterface = mailInterfaceArg;
			boolean closeMailInterface = false;
			try {
				if (mailInterface == null) {
					mailInterface = MailInterface.getInstance(sessionObj);
					closeMailInterface = true;
				}
				data = MessageWriter.writeMailMessage(mailInterface.getForwardMessageForDisplay(mailPath.getFolder(),
						mailPath.getUid()), false, sessionObj);
			} finally {
				if (closeMailInterface && mailInterface != null) {
					mailInterface.close(true);
				}
			}
		} catch (final MailException e) {
			LOG.error(e.getMessage(), e);
			response.setException(e);
		} catch (final AbstractOXException e) {
			LOG.error(e.getMessage(), e);
			response.setException(e);
		} catch (final Exception e) {
			LOG.error("actionGetForward", e);
			response.setException(getWrappingOXException(e));
		}
		/*
		 * Close response and flush print writer
		 */
		response.setData(data);
		response.setTimestamp(null);
		return response;
	}

	public void actionGetMessage(final Session sessionObj, final JSONWriter writer, final JSONObject requestObj,
			final MailInterface mi) throws JSONException {
		Response
				.write(actionGetMessage(sessionObj, ParamContainer.getInstance(requestObj, Component.MAIL), mi), writer);
	}

	private final void actionGetMessage(final HttpServletRequest req, final HttpServletResponse resp)
			throws IOException, ServletException {
		try {
			Response.write(actionGetMessage(getSessionObject(req), ParamContainer
					.getInstance(req, Component.MAIL, resp), null), resp.getWriter());
		} catch (final JSONException e) {
			sendErrorAsJS(resp, RESPONSE_ERROR);
		}
	}

	@SuppressWarnings("unchecked")
	private final Response actionGetMessage(final Session sessionObj, final ParamContainer paramContainer,
			final MailInterface mailInterfaceArg) {
		/*
		 * Some variables
		 */
		final Response response = new Response();
		Object data = JSONObject.NULL;
		/*
		 * Start response
		 */
		try {
			/*
			 * Read in parameters
			 */
			final MailPath mailPath = new MailPath(paramContainer.checkStringParam(PARAMETER_ID));
			String tmp = paramContainer.getStringParam(PARAMETER_SHOW_SRC);
			final boolean showMessageSource = (STR_1.equals(tmp) || Boolean.parseBoolean(tmp));
			tmp = paramContainer.getStringParam(PARAMETER_EDIT_DRAFT);
			final boolean editDraft = (STR_1.equals(tmp) || Boolean.parseBoolean(tmp));
			tmp = paramContainer.getStringParam(PARAMETER_SHOW_HEADER);
			final boolean showMessageHeaders = (STR_1.equals(tmp) || Boolean.parseBoolean(tmp));
			tmp = paramContainer.getStringParam(PARAMETER_SAVE);
			final boolean saveToDisk = (tmp != null && tmp.length() > 0 && Integer.parseInt(tmp) > 0);
			tmp = null;
			/*
			 * Get message
			 */
			MailInterface mailInterface = mailInterfaceArg;
			boolean closeMailInterface = false;
			try {
				if (mailInterface == null) {
					mailInterface = MailInterface.getInstance(sessionObj);
					closeMailInterface = true;
				}
				final MailMessage mail = mailInterface.getMessage(mailPath.getFolder(), mailPath.getUid());
				if (mail == null) {
					throw new MailException(MailException.Code.MAIL_NOT_FOUND, Long.valueOf(mailPath.getUid()),
							mailPath.getFolder());
				}
				if (showMessageSource) {
					final UnsynchronizedByteArrayOutputStream baos = new UnsynchronizedByteArrayOutputStream();
					mail.writeTo(baos);
					if (saveToDisk) {
						/*
						 * Write message source to output stream...
						 */
						final String userAgent = paramContainer.getHeader(STR_USER_AGENT).toLowerCase(Locale.ENGLISH);
						final boolean internetExplorer = (userAgent != null && userAgent.indexOf(STR_MSIE) > -1 && userAgent
								.indexOf(STR_WINDOWS) > -1);
						final ContentType contentType = new ContentType();
						contentType.setPrimaryType(STR_APPLICATION);
						contentType.setSubType(STR_OCTET_STREAM);
						final String fileName = new StringBuilder(mail.getSubject().replaceAll(" ", "_"))
								.append(".eml").toString();
						paramContainer.getHttpServletResponse().setHeader(
								STR_CONTENT_DISPOSITION,
								new StringBuilder(50).append(STR_ATTACHMENT_FILENAME).append(
										getSaveAsFileName(fileName, internetExplorer, null)).append('"').toString());
						paramContainer.getHttpServletResponse().setContentType(contentType.toString());
						final OutputStream out = paramContainer.getHttpServletResponse().getOutputStream();
						out.write(baos.toByteArray());
						/*
						 * ... and return
						 */
						return null;
					}
					final ContentType ct = mail.getContentType();
					data = new String(baos.toByteArray(), ct.containsParameter(STR_CHARSET) ? ct
							.getParameter(STR_CHARSET) : STR_UTF8);
				} else if (showMessageHeaders) {
					data = formatMessageHeaders(mail.getHeadersIterator());
				} else {
					data = MessageWriter.writeMailMessage(mail, !editDraft, sessionObj);
				}
			} finally {
				if (closeMailInterface && mailInterface != null) {
					mailInterface.close(true);
				}
			}
		} catch (final MailException e) {
			LOG.error(e.getMessage(), e);
			response.setException(e);
		} catch (final AbstractOXException e) {
			LOG.error(e.getMessage(), e);
			response.setException(e);
		} catch (final Exception e) {
			LOG.error("actionGetMessage", e);
			response.setException(getWrappingOXException(e));
		}
		/*
		 * Close response and flush print writer
		 */
		response.setData(data);
		response.setTimestamp(null);
		return response;
	}

	private static final String formatMessageHeaders(final Iterator<Map.Entry<String, String>> iter) {
		final StringBuilder sb = new StringBuilder(1024);
		while (iter.hasNext()) {
			final Map.Entry<String, String> entry = iter.next();
			sb.append(entry.getKey()).append(STR_DELIM).append(entry.getValue()).append(STR_CRLF);
		}
		return sb.toString();
	}

	public void actionGetNew(final Session sessionObj, final JSONWriter writer, final JSONObject requestObj,
			final MailInterface mi) throws SearchIteratorException, JSONException {
		Response.write(actionGetNew(sessionObj, ParamContainer.getInstance(requestObj, Component.MAIL), mi), writer);
	}

	private final void actionGetNew(final HttpServletRequest req, final HttpServletResponse resp) throws IOException,
			ServletException {
		try {
			Response.write(actionGetNew(getSessionObject(req), ParamContainer.getInstance(req, Component.MAIL, resp),
					null), resp.getWriter());
		} catch (final JSONException e) {
			sendErrorAsJS(resp, RESPONSE_ERROR);
		} catch (final SearchIteratorException e) {
			sendErrorAsJS(resp, RESPONSE_ERROR);
		}
	}

	private final Response actionGetNew(final Session sessionObj, final ParamContainer paramContainer,
			final MailInterface mailInterfaceArg) throws JSONException, SearchIteratorException {
		/*
		 * Some variables
		 */
		final Response response = new Response();
		final OXJSONWriter jsonWriter = new OXJSONWriter();
		/*
		 * Start response
		 */
		jsonWriter.array();
		SearchIterator<?> it = null;
		try {
			/*
			 * Read in parameters
			 */
			final String folderId = paramContainer.checkStringParam(PARAMETER_MAILFOLDER);
			final int[] columns = paramContainer.checkIntArrayParam(PARAMETER_COLUMNS);
			final String sort = paramContainer.getStringParam(PARAMETER_SORT);
			final String order = paramContainer.getStringParam(PARAMETER_ORDER);
			final int limit = paramContainer.getIntParam(PARAMETER_LIMIT);
			/*
			 * Get new mails
			 */
			MailInterface mailInterface = mailInterfaceArg;
			boolean closeMailInterface = false;
			try {
				if (mailInterface == null) {
					mailInterface = MailInterface.getInstance(sessionObj);
					closeMailInterface = true;
				}
				/*
				 * Receive message iterator
				 */
				final int sortCol = sort == null ? MailListField.RECEIVED_DATE.getField() : Integer.parseInt(sort);
				int orderDir = OrderDirection.ASC.getOrder();
				if (order != null) {
					if (order.equalsIgnoreCase(STR_ASC)) {
						orderDir = OrderDirection.ASC.getOrder();
					} else if (order.equalsIgnoreCase(STR_DESC)) {
						orderDir = OrderDirection.DESC.getOrder();
					} else {
						throw new MailException(MailException.Code.INVALID_INT_VALUE, PARAMETER_ORDER);
					}
				}
				/*
				 * Pre-Select field writers
				 */
				final MailFieldWriter[] writers = MessageWriter.getMailFieldWriter(MailListField.getFields(columns),
						sessionObj);
				it = mailInterface.getNewMessages(folderId, sortCol, orderDir, columns,
						limit == ParamContainer.NOT_FOUND ? -1 : limit);
				final int size = it.size();
				for (int i = 0; i < size; i++) {
					final MailMessage mail = (MailMessage) it.next();
					final JSONArray ja = new JSONArray();
					for (final MailFieldWriter writer : writers) {
						writer.writeField(ja, mail, 0, false);
					}
					jsonWriter.value(ja);
				}
			} finally {
				if (closeMailInterface && mailInterface != null) {
					mailInterface.close(true);
				}
			}
		} catch (final MailException e) {
			LOG.error(e.getMessage(), e);
			response.setException(e);
		} catch (final AbstractOXException e) {
			LOG.error(e.getMessage(), e);
			response.setException(e);
		} catch (final Exception e) {
			LOG.error("actionGetNew", e);
			response.setException(getWrappingOXException(e));
		} finally {
			if (it != null) {
				it.close();
			}
		}
		/*
		 * Close response and flush print writer
		 */
		jsonWriter.endArray();
		response.setData(jsonWriter.getObject());
		response.setTimestamp(null);
		return response;
	}

	public void actionGetSaveVersit(final Session sessionObj, final Writer writer, final JSONObject requestObj,
			final MailInterface mi) throws Exception {
		actionGetSaveVersit(sessionObj, writer, ParamContainer.getInstance(requestObj, Component.MAIL), mi);
	}

	private final void actionGetSaveVersit(final HttpServletRequest req, final HttpServletResponse resp)
			throws IOException, ServletException {
		try {
			actionGetSaveVersit(getSessionObject(req), resp.getWriter(), ParamContainer.getInstance(req,
					Component.MAIL, resp), null);
		} catch (final JSONException e) {
			sendErrorAsJS(resp, RESPONSE_ERROR);
		}
	}

	private final void actionGetSaveVersit(final Session sessionObj, final Writer writer,
			final ParamContainer paramContainer, final MailInterface mailInterfaceArg) throws JSONException {
		/*
		 * Some variables
		 */
		final Response response = new Response();
		final OXJSONWriter jsonWriter = new OXJSONWriter();
		/*
		 * Start response
		 */
		jsonWriter.array();
		try {
			/*
			 * Read in parameters
			 */
			final String msgUID = paramContainer.checkStringParam(PARAMETER_ID);
			final String partIdentifier = paramContainer.checkStringParam(PARAMETER_MAILATTCHMENT);
			/*
			 * Get new mails
			 */
			MailInterface mailInterface = mailInterfaceArg;
			boolean closeMailInterface = false;
			try {
				final MailPath mailPath = new MailPath(msgUID);
				if (mailInterface == null) {
					mailInterface = MailInterface.getInstance(sessionObj);
					closeMailInterface = true;
				}
				final CommonObject[] insertedObjs = mailInterface.saveVersitAttachment(mailPath.getFolder(), mailPath
						.getUid(), partIdentifier);
				final JSONObject jo = new JSONObject();
				for (int i = 0; i < insertedObjs.length; i++) {
					final CommonObject current = insertedObjs[i];
					jo.reset();
					jo.put(CommonFields.ID, current.getObjectID());
					jo.put(CommonFields.FOLDER_ID, current.getParentFolderID());
					jsonWriter.value(jo);
				}
			} finally {
				if (closeMailInterface && mailInterface != null) {
					mailInterface.close(true);
				}
			}
		} catch (final MailException e) {
			LOG.error(e.getMessage(), e);
			response.setException(e);
		} catch (final AbstractOXException e) {
			LOG.error(e.getMessage(), e);
			response.setException(e);
		} catch (final Exception e) {
			LOG.error("actionGetSaveVersit", e);
			response.setException(getWrappingOXException(e));
		}
		/*
		 * Close response and flush print writer
		 */
		jsonWriter.endArray();
		response.setData(jsonWriter.getObject());
		response.setTimestamp(null);
		Response.write(response, writer);
	}

	public void actionGetAttachment() throws MailException {
		throw new MailException(MailException.Code.UNSUPPORTED_ACTION, ACTION_MATTACH, "Multiple servlet");
	}

	/**
	 * Looks up a mail attachment and writes its content directly into response
	 * output stream. This method is not accessible via Mutliple servlet
	 */
	private final void actionGetAttachment(final HttpServletRequest req, final HttpServletResponse resp) {
		/*
		 * Some variables
		 */
		final Session sessionObj = getSessionObject(req);
		boolean outSelected = false;
		boolean saveToDisk = false;
		/*
		 * Start response
		 */
		try {
			/*
			 * Read in parameters
			 */
			final MailPath mailPath = new MailPath(checkStringParam(req, PARAMETER_ID));
			final String sequenceId = req.getParameter(PARAMETER_MAILATTCHMENT);
			final String imageContentId = req.getParameter(PARAMETER_MAILCID);
			String saveIdentifier = req.getParameter(PARAMETER_SAVE);
			saveToDisk = ((saveIdentifier == null || saveIdentifier.length() == 0) ? false : ((Integer
					.parseInt(saveIdentifier)) > 0));
			saveIdentifier = null;
			/*
			 * Get attachment
			 */
			final MailInterface mailInterface = MailInterface.getInstance(sessionObj);
			try {
				if (sequenceId == null && imageContentId == null) {
					throw new MailException(MailException.Code.MISSING_PARAM, new StringBuilder().append(
							PARAMETER_MAILATTCHMENT).append(" | ").append(PARAMETER_MAILCID).toString());
				}
				final MailPart mailPart;
				if (imageContentId == null) {
					mailPart = mailInterface.getMessageAttachment(mailPath.getFolder(), mailPath.getUid(), sequenceId,
							!saveToDisk);
					if (mailPart == null) {
						throw new MailException(MailException.Code.NO_ATTACHMENT_FOUND, sequenceId, mailPath.toString());
					}
				} else {
					mailPart = mailInterface.getMessageImage(mailPath.getFolder(), mailPath.getUid(), imageContentId);
					if (mailPart == null) {
						throw new MailException(MailException.Code.NO_ATTACHMENT_FOUND, sequenceId, mailPath.toString());
					}
				}
				/*
				 * Write to response
				 */
				final String userAgent = req.getHeader(STR_USER_AGENT) == null ? null : req.getHeader(STR_USER_AGENT)
						.toLowerCase(Locale.ENGLISH);
				final boolean internetExplorer = (userAgent != null && userAgent.indexOf(STR_MSIE) > -1 && userAgent
						.indexOf(STR_WINDOWS) > -1);
				final ContentType contentType;
				if (saveToDisk) {
					contentType = new ContentType();
					contentType.setPrimaryType(STR_APPLICATION);
					contentType.setSubType(STR_OCTET_STREAM);
					resp.setHeader(STR_CONTENT_DISPOSITION, new StringBuilder(50).append(STR_ATTACHMENT_FILENAME)
							.append(
									getSaveAsFileName(mailPart.getFileName(), internetExplorer, mailPart
											.getContentType().toString())).append('"').toString());
				} else {
					final String fileName = getSaveAsFileName(mailPart.getFileName(), internetExplorer, mailPart
							.getContentType().toString());
					contentType = mailPart.getContentType();
					if (contentType.isMimeType(MIME_APPLICATION_OCTET_STREAM)) {
						/*
						 * Try to determine MIME type
						 */
						final String ct = MIMEType2ExtMap.getContentType(fileName);
						final int pos = ct.indexOf('/');
						contentType.setPrimaryType(ct.substring(0, pos));
						contentType.setSubType(ct.substring(pos + 1));
					}
					contentType.addParameter(STR_NAME, fileName);
					resp.setHeader(STR_CONTENT_DISPOSITION, new StringBuilder(50).append(STR_INLINE_FILENAME).append(
							fileName).append('"').toString());
				}
				resp.setContentType(contentType.toString());
				/*
				 * Write from content's input stream to response output stream
				 */
				InputStream contentInputStream = null;
				/*
				 * Reset response header values since we are going to directly
				 * write into servlet's output stream and then some browsers do
				 * not allow header "Pragma"
				 */
				Tools.removeCachingHeader(resp);
				final OutputStream out = resp.getOutputStream();
				outSelected = true;
				try {
					contentInputStream = mailPart.getInputStream();
					final byte[] buffer = new byte[0xFFFF];
					for (int len; (len = contentInputStream.read(buffer)) != -1;) {
						out.write(buffer, 0, len);
					}
					out.flush();
				} finally {
					if (contentInputStream != null) {
						contentInputStream.close();
					}
				}
			} finally {
				if (mailInterface != null) {
					mailInterface.close(true);
				}
			}
		} catch (final Exception e) {
			LOG.error("actionGetAttachment", e);
			try {
				resp.setContentType(MIME_TEXT_HTML_CHARSET_UTF_8);
				final Writer writer;
				if (outSelected) {
					/*
					 * Output stream has already been selected
					 */
					Tools.disableCaching(resp);
					writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(resp.getOutputStream(), resp
							.getCharacterEncoding())), true);
				} else {
					writer = resp.getWriter();
				}
				resp.setHeader(STR_CONTENT_DISPOSITION, null);
				final Response response = new Response();
				response.setException(e instanceof AbstractOXException ? (AbstractOXException) e
						: getWrappingOXException(e));
				final String callback = saveToDisk ? JS_FRAGMENT : JS_FRAGMENT_POPUP;
				writer.write(callback.replaceFirst(JS_FRAGMENT_JSON, response.getJSON().toString()).replaceFirst(
						JS_FRAGMENT_ACTION, "error"));
				writer.flush();
			} catch (final UnsupportedEncodingException uee) {
				LOG.error(uee.getLocalizedMessage(), uee);
			} catch (final IOException ioe) {
				LOG.error(ioe.getLocalizedMessage(), ioe);
			} catch (final IllegalStateException ise) {
				LOG.error(ise.getLocalizedMessage(), ise);
			} catch (final JSONException je) {
				LOG.error(je.getLocalizedMessage(), je);
			}
		}
	}

	private static final Pattern PART_FILENAME_PATTERN = Pattern.compile("(part )([0-9]+)(?:(\\.)([0-9]+))*",
			Pattern.CASE_INSENSITIVE);

	private static final String DEFAULT_FILENAME = "file.dat";

	public static final String getSaveAsFileName(final String fileName, final boolean internetExplorer,
			final String baseCT) {
		if (null == fileName) {
			return DEFAULT_FILENAME;
		}
		final StringBuilder tmp = new StringBuilder(32);
		final Matcher m = PART_FILENAME_PATTERN.matcher(fileName);
		if (m.matches()) {
			tmp.append(fileName.replaceAll(" ", "_"));
		} else {
			try {
				tmp.append(Helper.encodeFilename(fileName, STR_UTF8, internetExplorer));
			} catch (final UnsupportedEncodingException e) {
				LOG.error("Unsupported encoding in a message detected and monitored.", e);
				MailInterfaceImpl.mailInterfaceMonitor.addUnsupportedEncodingExceptions(e.getMessage());
				return fileName;
			}
		}
		if (null != baseCT) {
			if (baseCT.regionMatches(true, 0, MIME_TEXT_PLAIN, 0, MIME_TEXT_PLAIN.length())) {
				if (!fileName.toLowerCase(Locale.ENGLISH).endsWith(".txt")) {
					tmp.append(".txt");
				}
			} else if (baseCT.regionMatches(true, 0, MIME_TEXT_HTML, 0, MIME_TEXT_HTML.length())) {
				if (!fileName.toLowerCase(Locale.ENGLISH).endsWith(".htm")
						&& !fileName.toLowerCase(Locale.ENGLISH).endsWith(".html")) {
					tmp.append(".html");
				}
			}
		}
		return tmp.toString();
	}

	public void actionPutClear(final Session session, final JSONWriter writer, final JSONObject jsonObj,
			final MailInterface mi) throws JSONException {
		Response.write(actionPutClear(session, jsonObj.getString(Response.DATA), ParamContainer.getInstance(jsonObj,
				Component.MAIL), mi), writer);
	}

	private final void actionPutClear(final HttpServletRequest req, final HttpServletResponse resp) throws IOException,
			ServletException {
		try {
			Response.write(actionPutClear(getSessionObject(req), getBody(req), ParamContainer.getInstance(req,
					Component.MAIL, resp), null), resp.getWriter());
		} catch (final JSONException e) {
			sendErrorAsJS(resp, RESPONSE_ERROR);
		}
	}

	private final Response actionPutClear(final Session sessionObj, final String body,
			final ParamContainer paramContainer, final MailInterface mailInterfaceArg) throws JSONException {
		/*
		 * Some variables
		 */
		final Response response = new Response();
		final OXJSONWriter jsonWriter = new OXJSONWriter();
		/*
		 * Start response
		 */
		jsonWriter.array();
		try {
			/*
			 * Parse body
			 */
			final JSONArray ja = new JSONArray(body);
			final int length = ja.length();
			if (length > 0) {
				MailInterface mailInterface = mailInterfaceArg;
				boolean closeMailInterface = false;
				try {
					if (mailInterface == null) {
						mailInterface = MailInterface.getInstance(sessionObj);
						closeMailInterface = true;
					}
					/*
					 * Clear folder sequentially
					 */
					for (int i = 0; i < length; i++) {
						final String folderId = ja.getString(i);
						if (!mailInterface.clearFolder(folderId)) {
							/*
							 * Something went wrong
							 */
							jsonWriter.value(folderId);
						}
					}
				} finally {
					if (closeMailInterface && mailInterface != null) {
						mailInterface.close(true);
					}
				}
			}
		} catch (final AbstractOXException e) {
			LOG.error(e.getMessage(), e);
			response.setException(e);
		} catch (final Exception e) {
			LOG.error("actionPutClear", e);
			response.setException(getWrappingOXException(e));
		}
		/*
		 * Close response and flush print writer
		 */
		jsonWriter.endArray();
		response.setData(jsonWriter.getObject());
		response.setTimestamp(null);
		return response;
	}

	public void actionPutMailSearch(final Session session, final JSONWriter writer, final JSONObject jsonObj,
			final MailInterface mi) throws JSONException, SearchIteratorException {
		Response.write(actionPutMailSearch(session, jsonObj.getString(Response.DATA), ParamContainer.getInstance(
				jsonObj, Component.MAIL), mi), writer);
	}

	private final void actionPutMailSearch(final HttpServletRequest req, final HttpServletResponse resp)
			throws IOException, ServletException {
		try {
			Response.write(actionPutMailSearch(getSessionObject(req), getBody(req), ParamContainer.getInstance(req,
					Component.MAIL, resp), null), resp.getWriter());
		} catch (final JSONException e) {
			sendErrorAsJS(resp, RESPONSE_ERROR);
		} catch (final SearchIteratorException e) {
			sendErrorAsJS(resp, RESPONSE_ERROR);
		}
	}

	private final Response actionPutMailSearch(final Session sessionObj, final String body,
			final ParamContainer paramContainer, final MailInterface mailInterfaceArg) throws JSONException,
			SearchIteratorException {
		/*
		 * Some variables
		 */
		final Response response = new Response();
		final OXJSONWriter jsonWriter = new OXJSONWriter();
		/*
		 * Start response
		 */
		jsonWriter.array();
		SearchIterator<?> it = null;
		try {
			/*
			 * Read in parameters
			 */
			final String folderId = paramContainer.checkStringParam(PARAMETER_MAILFOLDER);
			final int[] columns = paramContainer.checkIntArrayParam(PARAMETER_COLUMNS);
			final String sort = paramContainer.getStringParam(PARAMETER_SORT);
			final String order = paramContainer.getStringParam(PARAMETER_ORDER);
			final boolean threadSort = (STR_THREAD.equalsIgnoreCase(sort));
			if (sort != null && !threadSort && order == null) {
				throw new MailException(MailException.Code.MISSING_PARAM, PARAMETER_ORDER);
			}
			/*
			 * Parse body into a JSON array
			 */
			final JSONArray ja = new JSONArray(body);
			final int length = ja.length();
			if (length > 0) {
				final int[] searchCols = new int[length];
				final String[] searchPats = new String[length];
				for (int i = 0; i < length; i++) {
					final JSONObject tmp = ja.getJSONObject(i);
					searchCols[i] = tmp.getInt(PARAMETER_COL);
					searchPats[i] = tmp.getString(PARAMETER_PATTERN);
				}
				/*
				 * Search mails
				 */
				MailInterface mailInterface = mailInterfaceArg;
				boolean closeMailInterface = false;
				try {
					if (mailInterface == null) {
						mailInterface = MailInterface.getInstance(sessionObj);
						closeMailInterface = true;
					}
					/*
					 * Pre-Select field writers
					 */
					/*
					 * Pre-Select field writers
					 */
					final MailFieldWriter[] writers = MessageWriter.getMailFieldWriter(
							MailListField.getFields(columns), sessionObj);
					/*
					 * Receive message iterator
					 */
					if (threadSort) {
						it = mailInterface.getThreadedMessages(folderId, null, searchCols, searchPats, true, columns);
						final int size = it.size();
						for (int i = 0; i < size; i++) {
							final MailMessage mail = (MailMessage) it.next();
							final JSONArray arr = new JSONArray();
							for (final MailFieldWriter writer : writers) {
								writer.writeField(arr, mail, 0, false);
							}
							jsonWriter.value(arr);
						}
					} else {
						final int sortCol = sort == null ? MailListField.RECEIVED_DATE.getField() : Integer
								.parseInt(sort);
						int orderDir = OrderDirection.ASC.getOrder();
						if (order != null) {
							if (order.equalsIgnoreCase(STR_ASC)) {
								orderDir = OrderDirection.ASC.getOrder();
							} else if (order.equalsIgnoreCase(STR_DESC)) {
								orderDir = OrderDirection.DESC.getOrder();
							} else {
								throw new MailException(MailException.Code.INVALID_INT_VALUE, PARAMETER_ORDER);
							}
						}
						it = mailInterface.getMessages(folderId, null, sortCol, orderDir, searchCols, searchPats, true,
								columns);
						final int size = it.size();
						for (int i = 0; i < size; i++) {
							final MailMessage mail = (MailMessage) it.next();
							final JSONArray arr = new JSONArray();
							for (final MailFieldWriter writer : writers) {
								writer.writeField(arr, mail, 0, false);
							}
							jsonWriter.value(arr);
						}
					}
				} finally {
					if (closeMailInterface && mailInterface != null) {
						mailInterface.close(true);
					}
				}
			}
		} catch (final MailException e) {
			LOG.error(e.getMessage(), e);
			response.setException(e);
		} catch (final AbstractOXException e) {
			LOG.error(e.getMessage(), e);
			response.setException(e);
		} catch (final Exception e) {
			LOG.error("actionPutMailList", e);
			response.setException(getWrappingOXException(e));
		} finally {
			if (it != null) {
				it.close();
			}
		}
		/*
		 * Close response and flush print writer
		 */
		jsonWriter.endArray();
		response.setData(jsonWriter.getObject());
		response.setTimestamp(null);
		return response;
	}

	public void actionPutMailList(final Session session, final JSONWriter writer, final JSONObject jsonObj,
			final MailInterface mi) throws JSONException {
		Response.write(actionPutMailList(session, jsonObj.getString(Response.DATA), ParamContainer.getInstance(jsonObj,
				Component.MAIL), mi), writer);
	}

	private final void actionPutMailList(final HttpServletRequest req, final HttpServletResponse resp)
			throws IOException, ServletException {
		try {
			Response.write(actionPutMailList(getSessionObject(req), getBody(req), ParamContainer.getInstance(req,
					Component.MAIL, resp), null), resp.getWriter());
		} catch (final JSONException e) {
			sendErrorAsJS(resp, RESPONSE_ERROR);
		}
	}

	private final Response actionPutMailList(final Session sessionObj, final String body,
			final ParamContainer paramContainer, final MailInterface mailInterfaceArg) throws JSONException {
		/*
		 * Some variables
		 */
		final Response response = new Response();
		final OXJSONWriter jsonWriter = new OXJSONWriter();
		/*
		 * Start response
		 */
		jsonWriter.array();
		try {
			final int[] columns = paramContainer.checkIntArrayParam(PARAMETER_COLUMNS);
			final JSONArray jsonIDs = new JSONArray(body);
			final int length = jsonIDs.length();
			if (length > 0) {
				/*
				 * Pre-Select field writers
				 */
				final MailFieldWriter[] writers = MessageWriter.getMailFieldWriter(MailListField.getFields(columns),
						sessionObj);
				final Map<String, SmartLongArray> idMap = new HashMap<String, SmartLongArray>();
				fillMap(idMap, body, length);
				final int size = idMap.size();
				MailInterface mailInterface = mailInterfaceArg;
				boolean closeMailInterface = false;
				try {
					if (mailInterface == null) {
						mailInterface = MailInterface.getInstance(sessionObj);
						closeMailInterface = true;
					}
					final Iterator<Map.Entry<String, SmartLongArray>> iter = idMap.entrySet().iterator();
					for (int k = 0; k < size; k++) {
						final Map.Entry<String, SmartLongArray> entry = iter.next();
						/*
						 * Get message list
						 */
						final MailMessage[] mails = mailInterface.getMessageList(entry.getKey(), entry.getValue()
								.toArray(), columns);
						for (int i = 0; i < mails.length; i++) {
							if (mails[i] != null) {
								final JSONArray ja = new JSONArray();
								for (int j = 0; j < writers.length; j++) {
									writers[j].writeField(ja, mails[i], 0, false);
								}
								jsonWriter.value(ja);
							}
						}
					}
				} finally {
					if (closeMailInterface && mailInterface != null) {
						mailInterface.close(true);
					}
				}
			}
		} catch (final MailException e) {
			LOG.error(e.getMessage(), e);
			response.setException(e);
		} catch (final AbstractOXException e) {
			LOG.error(e.getMessage(), e);
			response.setException(e);
		} catch (final Exception e) {
			LOG.error("actionPutMailList", e);
			response.setException(getWrappingOXException(e));
		}
		/*
		 * Close response and flush print writer
		 */
		jsonWriter.endArray();
		response.setData(jsonWriter.getObject());
		response.setTimestamp(null);
		return response;
	}

	private static final Pattern PATTERN_IDS = Pattern.compile("\"id\":\".+?/([0-9]+?)\"\\s*,\\s*\"folder\":\"(.+?)\"");

	@SuppressWarnings("null")
	private static final void fillMap(final Map<String, SmartLongArray> idMap, final String requestBody,
			final int length) {
		final Matcher m = PATTERN_IDS.matcher(requestBody);
		String folder = null;
		SmartLongArray sla = null;
		while (m.find()) {
			boolean found = false;
			do {
				if (folder == null || !folder.equals(m.group(2))) {
					folder = m.group(2);
					sla = new SmartLongArray(length);
					idMap.put(folder, sla);
				}
				sla.append(Long.parseLong(m.group(1)));
				found = m.find();
			} while (found && folder.equals(m.group(2)));
			if (found) {
				folder = m.group(2);
				sla = new SmartLongArray(length);
				sla.append(Long.parseLong(m.group(1)));
				idMap.put(folder, sla);
			}
		}
	}

	public void actionPutDeleteMails(final Session sessionObj, final JSONWriter writer, final JSONObject jsonObj,
			final MailInterface mi) throws JSONException {
		Response.write(actionPutDeleteMails(sessionObj, jsonObj.getString(Response.DATA), ParamContainer.getInstance(
				jsonObj, Component.MAIL), mi), writer);
	}

	private final void actionPutDeleteMails(final HttpServletRequest req, final HttpServletResponse resp)
			throws IOException, ServletException {
		try {
			Response.write(actionPutDeleteMails(getSessionObject(req), getBody(req), ParamContainer.getInstance(req,
					Component.MAIL, resp), null), resp.getWriter());
		} catch (final JSONException e) {
			sendErrorAsJS(resp, RESPONSE_ERROR);
		}
	}

	private final Response actionPutDeleteMails(final Session sessionObj, final String body,
			final ParamContainer paramContainer, final MailInterface mailInterfaceArg) throws JSONException {
		/*
		 * Some variables
		 */
		final Response response = new Response();
		final OXJSONWriter jsonWriter = new OXJSONWriter();
		/*
		 * Start response
		 */
		jsonWriter.array();
		try {
			final boolean hardDelete = STR_1.equals(paramContainer.getStringParam(PARAMETER_HARDDELETE));
			final JSONArray jsonIDs = new JSONArray(body);
			MailInterface mailInterface = mailInterfaceArg;
			boolean closeMailInterface = false;
			try {
				if (mailInterface == null) {
					mailInterface = MailInterface.getInstance(sessionObj);
					closeMailInterface = true;
				}
				boolean isJSONObject = true;
				final MailPath mailIdentifier = new MailPath();
				final int length = jsonIDs.length();
				if (length > 0) {
					final List<MailPath> l = new ArrayList<MailPath>(length);
					for (int i = 0; i < length; i++) {
						if (isJSONObject) {
							try {
								mailIdentifier.setMailIdentifierString(jsonIDs.getJSONObject(i).getString(
										FolderFields.ID));
							} catch (final JSONException e) {
								mailIdentifier.setMailIdentifierString(jsonIDs.getString(i));
								isJSONObject = false;
							}
						} else {
							mailIdentifier.setMailIdentifierString(jsonIDs.getString(i));
						}
						l.add((MailPath) mailIdentifier.clone());
					}
					Collections.sort(l, MailPath.getMailPathComparator());
					String lastFld = l.get(0).getFolder();
					final SmartLongArray arr = new SmartLongArray(length);
					for (int i = 0; i < length; i++) {
						final MailPath current = l.get(i);
						if (!lastFld.equals(current.getFolder())) {
							/*
							 * Delete all collected UIDs til here and reset
							 */
							final long[] uids = arr.toArray();
							mailInterface.deleteMessages(lastFld, uids, hardDelete);
							arr.reset();
							lastFld = current.getFolder();
						}
						arr.append(current.getUid());
					}
					if (arr.size() > 0) {
						final long[] uids = arr.toArray();
						mailInterface.deleteMessages(lastFld, uids, hardDelete);
					}
				}
			} finally {
				if (closeMailInterface && mailInterface != null) {
					mailInterface.close(true);
				}
			}
		} catch (final MailException e) {
			LOG.error(e.getMessage(), e);
			response.setException(e);
		} catch (final AbstractOXException e) {
			LOG.error(e.getMessage(), e);
			response.setException(e);
		} catch (final Exception e) {
			LOG.error("actionPutDeleteMails", e);
			response.setException(getWrappingOXException(e));
		}
		/*
		 * Close response and flush print writer
		 */
		jsonWriter.endArray();
		response.setData(jsonWriter.getObject());
		response.setTimestamp(null);
		return response;
	}

	public void actionPutUpdateMail(final Session sessionObj, final JSONWriter writer, final JSONObject jsonObj,
			final MailInterface mailInterface) throws JSONException {
		Response.write(actionPutUpdateMail(sessionObj, jsonObj.getString(Response.DATA), ParamContainer.getInstance(
				jsonObj, Component.MAIL), mailInterface), writer);
	}

	private final void actionPutUpdateMail(final HttpServletRequest req, final HttpServletResponse resp)
			throws IOException, ServletException {
		try {
			Response.write(actionPutUpdateMail(getSessionObject(req), getBody(req), ParamContainer.getInstance(req,
					Component.MAIL, resp), null), resp.getWriter());
		} catch (final JSONException e) {
			sendErrorAsJS(resp, RESPONSE_ERROR);
		}
	}

	private final Response actionPutUpdateMail(final Session sessionObj, final String body,
			final ParamContainer paramContainer, final MailInterface mailIntefaceArg) throws JSONException {
		/*
		 * Some variables
		 */
		final Response response = new Response();
		final OXJSONWriter jsonWriter = new OXJSONWriter();
		/*
		 * Start response
		 */
		jsonWriter.array();
		try {
			final MailPath mailPath = new MailPath(paramContainer.checkStringParam(PARAMETER_ID));
			final String sourceFolder = paramContainer.checkStringParam(PARAMETER_FOLDERID);
			final JSONObject bodyObj = new JSONObject(body);
			final String destFolder = bodyObj.has(FolderFields.FOLDER_ID) && !bodyObj.isNull(FolderFields.FOLDER_ID) ? bodyObj
					.getString(FolderFields.FOLDER_ID)
					: null;
			final Integer colorLabel = bodyObj.has(CommonFields.COLORLABEL) && !bodyObj.isNull(CommonFields.COLORLABEL) ? Integer
					.valueOf(bodyObj.getInt(CommonFields.COLORLABEL))
					: null;
			final Integer flagBits = bodyObj.has(MailJSONField.FLAGS.getKey())
					&& !bodyObj.isNull(MailJSONField.FLAGS.getKey()) ? Integer.valueOf(bodyObj
					.getInt(MailJSONField.FLAGS.getKey())) : null;
			boolean flagVal = false;
			if (flagBits != null) {
				/*
				 * Look for boolean value
				 */
				flagVal = (bodyObj.has(MailJSONField.VALUE.getKey()) && !bodyObj.isNull(MailJSONField.VALUE.getKey()) ? bodyObj
						.getBoolean(MailJSONField.VALUE.getKey())
						: false);
			}
			MailInterface mailInterface = mailIntefaceArg;
			boolean closeMailInterface = false;
			try {
				if (mailInterface == null) {
					mailInterface = MailInterface.getInstance(sessionObj);
					closeMailInterface = true;
				}
				if (destFolder != null) {
					/*
					 * Perform move operation
					 */
					mailInterface.copyMessages(sourceFolder, destFolder, new long[] { mailPath.getUid() }, true);
				}
				if (colorLabel != null) {
					/*
					 * Update color label
					 */
					mailInterface.updateMessageColorLabel(sourceFolder, new long[] { mailPath.getUid() }, colorLabel
							.intValue());
				}
				if (flagBits != null) {
					/*
					 * Update system flags which are allowed to be altered by
					 * client
					 */
					mailInterface.updateMessageFlags(sourceFolder, new long[] { mailPath.getUid() }, flagBits
							.intValue(), flagVal);
				}
			} finally {
				if (closeMailInterface && mailInterface != null) {
					mailInterface.close(true);
				}
			}
		} catch (final MailException e) {
			LOG.error(e.getMessage(), e);
			response.setException(e);
		} catch (final AbstractOXException e) {
			LOG.error(e.getMessage(), e);
			response.setException(e);
		} catch (final Exception e) {
			LOG.error("actionPutUpdateMail", e);
			response.setException(getWrappingOXException(e));
		}
		/*
		 * Close response and flush print writer
		 */
		jsonWriter.endArray();
		response.setData(jsonWriter.getObject());
		response.setTimestamp(null);
		return response;
	}

	public void actionPutCopyMail(final Session sessionObj, final JSONWriter writer, final JSONObject jsonObj,
			final MailInterface mailInterface) throws JSONException {
		Response.write(actionPutCopyMail(sessionObj, jsonObj.getString(Response.DATA), ParamContainer.getInstance(
				jsonObj, Component.MAIL), mailInterface), writer);
	}

	private final void actionPutCopyMail(final HttpServletRequest req, final HttpServletResponse resp)
			throws IOException, ServletException {
		try {
			Response.write(actionPutCopyMail(getSessionObject(req), getBody(req), ParamContainer.getInstance(req,
					Component.MAIL, resp), null), resp.getWriter());
		} catch (final JSONException e) {
			sendErrorAsJS(resp, RESPONSE_ERROR);
		}
	}

	private final Response actionPutCopyMail(final Session sessionObj, final String body,
			final ParamContainer paramContainer, final MailInterface mailInterfaceArg) throws JSONException {
		/*
		 * Some variables
		 */
		final Response response = new Response();
		final OXJSONWriter jsonWriter = new OXJSONWriter();
		/*
		 * Start response
		 */
		jsonWriter.array();
		try {
			final MailPath mailPath = new MailPath(paramContainer.checkStringParam(PARAMETER_ID));
			final String sourceFolder = paramContainer.checkStringParam(PARAMETER_FOLDERID);
			final String destFolder = new JSONObject(body).getString(FolderFields.FOLDER_ID);
			MailInterface mailInterface = mailInterfaceArg;
			boolean closeMailInterface = false;
			try {
				if (mailInterface == null) {
					mailInterface = MailInterface.getInstance(sessionObj);
					closeMailInterface = true;
				}
				final long[] msgUIDs = mailInterface.copyMessages(sourceFolder, destFolder, new long[] { mailPath
						.getUid() }, false);
				if (msgUIDs.length == 1) {
					jsonWriter.value(new StringBuilder(destFolder).append(SEPERATOR).append(msgUIDs[0]).toString());
				} else if (msgUIDs.length > 1) {
					jsonWriter.array();
					try {
						for (int i = 0; i < msgUIDs.length; i++) {
							jsonWriter.value(msgUIDs[i]);
						}
					} finally {
						jsonWriter.endArray();
					}
				} else {
					jsonWriter.value(JSONObject.NULL);
				}
			} finally {
				if (closeMailInterface && mailInterface != null) {
					mailInterface.close(true);
				}
			}
		} catch (final MailException e) {
			LOG.error(e.getMessage(), e);
			response.setException(e);
		} catch (final AbstractOXException e) {
			LOG.error(e.getMessage(), e);
			response.setException(e);
		} catch (final Exception e) {
			LOG.error("actionPutCopyMail", e);
			response.setException(getWrappingOXException(e));
		}
		/*
		 * Close response and flush print writer
		 */
		jsonWriter.endArray();
		response.setData(jsonWriter.getObject());
		response.setTimestamp(null);
		return response;
	}

	public final void actionPutMoveMailMultiple(final Session sessionObj, final JSONWriter writer,
			final String[] mailIDs, final String sourceFolder, final String destFolder, final MailInterface mailInteface)
			throws JSONException {
		actionPutMailMultiple(sessionObj, writer, mailIDs, sourceFolder, destFolder, true, mailInteface);
	}

	public final void actionPutCopyMailMultiple(final Session sessionObj, final JSONWriter writer,
			final String[] mailIDs, final String srcFolder, final String destFolder, final MailInterface mailInterface)
			throws JSONException {
		actionPutMailMultiple(sessionObj, writer, mailIDs, srcFolder, destFolder, false, mailInterface);
	}

	public final void actionPutMailMultiple(final Session sessionObj, final JSONWriter writer,
			final String[] mailIDs, final String srcFolder, final String destFolder, final boolean move,
			final MailInterface mailInterfaceArg) throws JSONException {
		try {
			MailInterface mailInterface = mailInterfaceArg;
			boolean closeMailInterface = false;
			try {
				if (mailInterface == null) {
					mailInterface = MailInterface.getInstance(sessionObj);
					closeMailInterface = true;
				}
				final long[] msgUIDs = mailInterface.copyMessages(srcFolder, destFolder, MailPath.getUIDs(mailIDs),
						move);
				if (msgUIDs.length > 0) {
					final StringBuilder sb = new StringBuilder();
					final Response response = new Response();
					for (int k = 0; k < msgUIDs.length; k++) {
						response.reset();
						sb.setLength(0);
						final JSONArray jsonArr = new JSONArray();
						jsonArr.put(sb.append(destFolder).append(SEPERATOR).append(msgUIDs[k]).toString());
						response.setData(jsonArr);
						response.setTimestamp(null);
						Response.write(response, writer);
					}
				} else {
					final Response response = new Response();
					response.setData(JSONObject.NULL);
					response.setTimestamp(null);
					Response.write(response, writer);
				}
			} finally {
				if (closeMailInterface && mailInterface != null) {
					mailInterface.close(true);
				}
			}
		} catch (final AbstractOXException e) {
			LOG.error(e.getMessage(), e);
			final Response response = new Response();
			response.setException(e);
			response.setData(JSONObject.NULL);
			response.setTimestamp(null);
			Response.write(response, writer);
		} catch (final Exception e) {
			LOG.error("actionPutMailMultiple", e);
			final Response response = new Response();
			response.setException(getWrappingOXException(e));
			response.setData(JSONObject.NULL);
			response.setTimestamp(null);
			Response.write(response, writer);
		}
	}

	public void actionPutStoreFlagsMultiple(final Session sessionObj, final JSONWriter writer,
			final String[] mailIDs, final String folder, final int flagsBits, final boolean flagValue,
			final MailInterface mailInterfaceArg) throws JSONException {
		try {
			MailInterface mailInterface = mailInterfaceArg;
			boolean closeMailInterface = false;
			try {
				if (mailInterface == null) {
					mailInterface = MailInterface.getInstance(sessionObj);
					closeMailInterface = true;
				}
				mailInterface.updateMessageFlags(folder, MailPath.getUIDs(mailIDs), flagsBits, flagValue);
			} finally {
				if (closeMailInterface && mailInterface != null) {
					mailInterface.close(true);
				}
			}
		} catch (final AbstractOXException e) {
			LOG.error(e.getMessage(), e);
			final Response response = new Response();
			response.setException(e);
			response.setData(JSONObject.NULL);
			response.setTimestamp(null);
			Response.write(response, writer);
		} catch (final Exception e) {
			LOG.error("actionPutStoreFlagsMultiple", e);
			final Response response = new Response();
			response.setException(getWrappingOXException(e));
			response.setData(JSONObject.NULL);
			response.setTimestamp(null);
			Response.write(response, writer);
		}
	}

	public void actionPutColorLabelMultiple(final Session sessionObj, final JSONWriter writer,
			final String[] mailIDs, final String folder, final int colorLabel, final MailInterface mailInterfaceArg)
			throws JSONException {
		try {
			MailInterface mailInterface = mailInterfaceArg;
			boolean closeMailInterface = false;
			try {
				if (mailInterface == null) {
					mailInterface = MailInterface.getInstance(sessionObj);
					closeMailInterface = true;
				}
				mailInterface.updateMessageColorLabel(folder, MailPath.getUIDs(mailIDs), colorLabel);
			} finally {
				if (closeMailInterface && mailInterface != null) {
					mailInterface.close(true);
				}
			}
		} catch (final AbstractOXException e) {
			LOG.error(e.getMessage(), e);
			final Response response = new Response();
			response.setException(e);
			response.setData(JSONObject.NULL);
			response.setTimestamp(null);
			Response.write(response, writer);
		} catch (final Exception e) {
			LOG.error("actionPutColorLabelMultiple", e);
			final Response response = new Response();
			response.setException(getWrappingOXException(e));
			response.setData(JSONObject.NULL);
			response.setTimestamp(null);
			Response.write(response, writer);
		}
	}

	public void actionPutAttachment(final Session sessionObj, final JSONWriter writer, final JSONObject jsonObj,
			final MailInterface mi) throws JSONException {
		Response.write(actionPutAttachment(sessionObj, jsonObj.getString(Response.DATA), ParamContainer.getInstance(
				jsonObj, Component.MAIL), mi), writer);
	}

	private final void actionPutAttachment(final HttpServletRequest req, final HttpServletResponse resp)
			throws IOException, ServletException {
		try {
			Response.write(actionPutAttachment(getSessionObject(req), getBody(req), ParamContainer.getInstance(req,
					Component.MAIL, resp), null), resp.getWriter());
		} catch (final JSONException e) {
			sendErrorAsJS(resp, RESPONSE_ERROR);
		}
	}

	private final Response actionPutAttachment(final Session sessionObj, final String body,
			final ParamContainer paramContainer, final MailInterface mailInterfaceArg) throws JSONException {
		/*
		 * Some variables
		 */
		final Response response = new Response();
		final OXJSONWriter jsonWriter = new OXJSONWriter();
		/*
		 * Start response
		 */
		jsonWriter.array();
		try {
			final MailPath mailPath = new MailPath(paramContainer.checkStringParam(PARAMETER_ID));
			final String sequenceId = paramContainer.checkStringParam(PARAMETER_MAILATTCHMENT);
			final String destFolderIdentifier = paramContainer.checkStringParam(PARAMETER_DESTINATION_FOLDER);
			MailInterface mailInterface = mailInterfaceArg;
			boolean closeMailInterface = false;
			final InfostoreFacade db = Infostore.FACADE;
			try {
				if (!UserConfigurationStorage.getInstance().getUserConfigurationSafe(sessionObj.getUserId(),
						sessionObj.getContext()).hasInfostore()) {
					throw new OXPermissionException(new MailException(MailException.Code.NO_MAIL_ACCESS));
				}
				if (mailInterface == null) {
					mailInterface = MailInterface.getInstance(sessionObj);
					closeMailInterface = true;
				}
				final MailPart mailPart = mailInterface.getMessageAttachment(mailPath.getFolder(), mailPath.getUid(),
						sequenceId, false);
				if (mailPart == null) {
					throw new MailException(MailException.Code.NO_ATTACHMENT_FOUND, sequenceId, mailPath.toString());
				}
				final int destFolderID = Integer.parseInt(destFolderIdentifier);
				{
					final FolderObject folderObj = new OXFolderAccess(sessionObj.getContext())
							.getFolderObject(destFolderID);
					final EffectivePermission p = folderObj.getEffectiveUserPermission(sessionObj.getUserId(),
							UserConfigurationStorage.getInstance().getUserConfigurationSafe(sessionObj.getUserId(),
									sessionObj.getContext()));
					if (!p.isFolderVisible()) {
						throw new OXFolderException(FolderCode.NOT_VISIBLE, getFolderName(folderObj), getUserName(
								sessionObj, UserStorage.getStorageUser(sessionObj.getUserId(), sessionObj.getContext())),
								Integer.valueOf(sessionObj.getContext().getContextId()));
					}
					if (!p.canWriteOwnObjects()) {
						throw new OXFolderException(FolderCode.NO_WRITE_PERMISSION, getUserName(sessionObj, UserStorage
								.getStorageUser(sessionObj.getUserId(), sessionObj.getContext())), getFolderName(folderObj),
								Integer.valueOf(sessionObj.getContext().getContextId()));
					}
				}
				/*
				 * Create document's meta data
				 */
				final InfostoreParser parser = new InfostoreParser();
				final DocumentMetadata docMetaData = parser.getDocumentMetadata(body);
				final Set<Metadata> metSet = new HashSet<Metadata>(Arrays.asList(parser.findPresentFields(body)));
				if (!metSet.contains(Metadata.FILENAME_LITERAL)) {
					docMetaData.setFileName(mailPart.getFileName());
				}
				docMetaData.setFileMIMEType(mailPart.getContentType().toString());
				/*
				 * Since file's size given from IMAP server is just an
				 * estimation and therefore does not exactly match the file's
				 * size a future file access via webdav can fail because of the
				 * size mismatch. Thus set the file size to 0 to make the
				 * infostore measure the size.
				 */
				docMetaData.setFileSize(0);
				if (!metSet.contains(Metadata.TITLE_LITERAL)) {
					docMetaData.setTitle(mailPart.getFileName());
				}
				docMetaData.setFolderId(destFolderID);
				/*
				 * Start writing to infostore folder
				 */
				db.startTransaction();
				db.saveDocument(docMetaData, mailPart.getInputStream(), System.currentTimeMillis(), sessionObj);
				db.commit();
			} catch (final Exception e) {
				db.rollback();
				throw e;
			} finally {
				if (closeMailInterface && mailInterface != null) {
					mailInterface.close(true);
				}
				if (db != null) {
					db.finish();
				}
			}
		} catch (final MailException e) {
			LOG.error(e.getMessage(), e);
			response.setException(e);
		} catch (final AbstractOXException e) {
			LOG.error(e.getMessage(), e);
			response.setException(e);
		} catch (final Exception e) {
			LOG.error("actionPutAttachment", e);
			response.setException(getWrappingOXException(e));
		}
		/*
		 * Close response and flush print writer
		 */
		jsonWriter.endArray();
		response.setData(jsonWriter.getObject());
		response.setTimestamp(null);
		return response;
	}

	public void actionPutReceiptAck(final Session sessionObj, final JSONWriter writer, final JSONObject jsonObj,
			final MailInterface mi) throws JSONException {
		Response.write(actionPutReceiptAck(sessionObj, jsonObj.getString(Response.DATA), ParamContainer.getInstance(
				jsonObj, Component.MAIL), mi), writer);
	}

	private final void actionPutReceiptAck(final HttpServletRequest req, final HttpServletResponse resp)
			throws IOException, ServletException {
		try {
			Response.write(actionPutReceiptAck(getSessionObject(req), getBody(req), ParamContainer.getInstance(req,
					Component.MAIL, resp), null), resp.getWriter());
		} catch (final JSONException e) {
			sendErrorAsJS(resp, RESPONSE_ERROR);
		}
	}

	private final Response actionPutReceiptAck(final Session sessionObj, final String body,
			final ParamContainer paramContainer, final MailInterface mailInterfaceArg) {
		/*
		 * Some variables
		 */
		final Response response = new Response();
		/*
		 * Start response
		 */
		try {
			final MailPath mailPath = new MailPath(paramContainer.checkStringParam(PARAMETER_ID));
			final JSONObject bodyObj = new JSONObject(body);
			final String fromAddr = bodyObj.has(MailJSONField.FROM.getKey())
					&& !bodyObj.isNull(MailJSONField.FROM.getKey()) ? bodyObj.getString(MailJSONField.FROM.getKey())
					: null;
			MailInterface mailInterface = mailInterfaceArg;
			boolean closeMailInterface = false;
			try {
				if (mailInterface == null) {
					mailInterface = MailInterface.getInstance(sessionObj);
					closeMailInterface = true;
				}
				mailInterface.sendReceiptAck(mailPath.getFolder(), mailPath.getUid(), fromAddr);
			} finally {
				if (closeMailInterface && mailInterface != null) {
					mailInterface.close(true);
				}
			}
		} catch (final MailException e) {
			LOG.error(e.getMessage(), e);
			response.setException(e);
		} catch (final AbstractOXException e) {
			LOG.error(e.getMessage(), e);
			response.setException(e);
		} catch (final Exception e) {
			LOG.error("actionPutReceiptAck", e);
			response.setException(getWrappingOXException(e));
		}
		/*
		 * Close response and flush print writer
		 */
		response.setData(JSONObject.NULL);
		response.setTimestamp(null);
		return response;
	}

	private static String checkStringParam(final HttpServletRequest req, final String paramName)
			throws OXMandatoryFieldException {
		final String paramVal = req.getParameter(paramName);
		if (paramVal == null || paramVal.length() == 0 || STR_NULL.equals(paramVal)) {
			throw new OXMandatoryFieldException(Component.MAIL, MailException.Code.MISSING_PARAM.getCategory(),
					MailException.Code.MISSING_PARAM.getNumber(), null, paramName);
		}
		return paramVal;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException,
			IOException {
		final Session sessionObj = getSessionObject(req);
		/*
		 * The magic spell to disable caching
		 */
		Tools.disableCaching(resp);
		final String actionStr = req.getParameter(PARAMETER_ACTION);
		try {
			final MailInterface mailInterface = MailInterface.getInstance(sessionObj);
			try {
				if (req.getContentType().toLowerCase().startsWith(MIME_MULTIPART)) {
					/*
					 * Set response headers according to html spec
					 */
					resp.setContentType(MIME_TEXT_HTML_CHARSET_UTF_8);
					/*
					 * Append UploadListener instances
					 */
					((UploadListener) this).getRegistry().addUploadListener(
							new UploadQuotaChecker(UserSettingMailStorage.getInstance().getUserSettingMail(
									sessionObj.getUserId(), sessionObj.getContext()), resp, actionStr));
					((UploadListener) this).getRegistry().addUploadListener(MAIL_SERVLET);
					/*
					 * Create and fire upload event
					 */
					final UploadEvent uploadEvent = ((UploadListener) this).getRegistry().processUpload(req);
					uploadEvent.setParameter(UPLOAD_PARAM_MAILINTERFACE, mailInterface);
					uploadEvent.setParameter(UPLOAD_PARAM_WRITER, resp.getWriter());
					uploadEvent.setParameter(UPLOAD_PARAM_SESSION, sessionObj);
					uploadEvent.setParameter(PARAMETER_ACTION, actionStr);
					((UploadListener) this).getRegistry().fireUploadEvent(uploadEvent);
				}
			} finally {
				if (mailInterface != null) {
					try {
						mailInterface.close(true);
					} catch (final Exception e) {
						LOG.error(e.getMessage(), e);
					}
				}
			}
		} catch (final UploadException e) {
			LOG.error(e.getMessage(), e);
			JSONObject responseObj = null;
			try {
				final Response response = new Response();
				response.setException(e);
				responseObj = response.getJSON();
			} catch (final JSONException e1) {
				LOG.error(e1.getMessage(), e1);
			}
			throw new UploadServletException(resp, JS_FRAGMENT.replaceFirst(JS_FRAGMENT_JSON,
					responseObj == null ? STR_NULL : Matcher.quoteReplacement(responseObj.toString())).replaceFirst(
					JS_FRAGMENT_ACTION, e.getAction() == null ? STR_NULL : e.getAction()), e.getMessage(), e);
		} catch (final MailException e) {
			LOG.error(e.getMessage(), e);
			JSONObject responseObj = null;
			try {
				final Response response = new Response();
				response.setException(e);
				responseObj = response.getJSON();
			} catch (final JSONException e1) {
				LOG.error(e1.getMessage(), e1);
			}
			throw new UploadServletException(resp, JS_FRAGMENT.replaceFirst(JS_FRAGMENT_JSON,
					responseObj == null ? STR_NULL : Matcher.quoteReplacement(responseObj.toString())).replaceFirst(
					JS_FRAGMENT_ACTION, actionStr == null ? STR_NULL : actionStr), e.getMessage(), e);
		}
	}

	protected boolean sendMessage(final HttpServletRequest req) {
		return req.getParameter(PARAMETER_ACTION) != null
				&& req.getParameter(PARAMETER_ACTION).equalsIgnoreCase(ACTION_SEND);
	}

	protected boolean appendMessage(final HttpServletRequest req) {
		return req.getParameter(PARAMETER_ACTION) != null
				&& req.getParameter(PARAMETER_ACTION).equalsIgnoreCase(ACTION_APPEND);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.groupware.upload.UploadListener#action(com.openexchange.groupware.upload.UploadEvent)
	 */
	public boolean action(final UploadEvent uploadEvent) throws OXException {
		if (uploadEvent.getAffiliationId() != UploadEvent.MAIL_UPLOAD) {
			return false;
		}
		try {
			final PrintWriter writer = (PrintWriter) uploadEvent.getParameter(UPLOAD_PARAM_WRITER);
			final String actionStr = (String) uploadEvent.getParameter(PARAMETER_ACTION);
			try {
				if (uploadEvent.getAction().equals(ACTION_NEW)) {
					String msgIdentifier = null;
					{
						final JSONObject jsonMailObj = new JSONObject(uploadEvent.getFormField(UPLOAD_FORMFIELD_MAIL));
						/*
						 * Parse
						 */
						final TransportMailMessage transportMessage = MessageParser.parse(jsonMailObj, uploadEvent,
								(Session) uploadEvent.getParameter(UPLOAD_PARAM_SESSION));
						/*
						 * ... and send message
						 */
						final SendType sendType = jsonMailObj.has(PARAMETER_SEND_TYPE)
								&& !jsonMailObj.isNull(PARAMETER_SEND_TYPE) ? SendType.getSendType(jsonMailObj
								.getInt(PARAMETER_SEND_TYPE)) : SendType.NEW;
						msgIdentifier = ((MailInterface) uploadEvent.getParameter(UPLOAD_PARAM_MAILINTERFACE))
								.sendMessage(transportMessage, sendType);
					}
					if (msgIdentifier == null) {
						throw new MailException(MailException.Code.SEND_FAILED_UNKNOWN);
					}
					/*
					 * Create JSON response object
					 */
					final Response response = new Response();
					response.setData(msgIdentifier);
					final String jsResponse = JS_FRAGMENT.replaceFirst(JS_FRAGMENT_JSON,
							Matcher.quoteReplacement(response.getJSON().toString())).replaceFirst(JS_FRAGMENT_ACTION,
							actionStr);
					writer.write(jsResponse);
					writer.flush();
					return true;
				} else if (uploadEvent.getAction().equals(ACTION_APPEND)) {
					// TODO: Editing mail
					throw new UnsupportedOperationException("APPEND NOT SUPPORTED, YET!");
				}
			} catch (final MailException e) {
				/*
				 * Message could not be sent
				 */
				LOG.error(e.getMessage(), e);
				final Response response = new Response();
				response.setException(e);
				final String jsResponse = JS_FRAGMENT.replaceFirst(JS_FRAGMENT_JSON,
						Matcher.quoteReplacement(response.getJSON().toString())).replaceFirst(JS_FRAGMENT_ACTION,
						actionStr);
				writer.write(jsResponse);
				writer.flush();
				return true;
			}
			return false;
		} catch (final JSONException e) {
			throw new OXException(new MailException(MailException.Code.JSON_ERROR, e, e.getMessage()));
		}
	}

	public UploadRegistry getRegistry() {
		return this;
	}

	@Override
	protected boolean hasModulePermission(final Session sessionObj) {
		return UserConfigurationStorage.getInstance().getUserConfigurationSafe(sessionObj.getUserId(),
				sessionObj.getContext()).hasWebMail();
	}

	private static class SmartLongArray implements Cloneable {
		/**
		 * Pointer to keep track of position in the array
		 */
		private int pointer;

		private long[] array;

		private final int growthSize;

		private long[] trimmedArray;

		public SmartLongArray() {
			this(1024);
		}

		public SmartLongArray(final int initialSize) {
			this(initialSize, (initialSize / 4));
		}

		public SmartLongArray(final int initialSize, final int growthSize) {
			this.growthSize = growthSize;
			array = new long[initialSize];
		}

		public void reset() {
			pointer = 0;
			// Arrays.fill(array, 0);
			trimmedArray = null;
		}

		public int size() {
			return pointer;
		}

		public SmartLongArray append(final long l) {
			trimmedArray = null;
			if (pointer >= array.length) {
				/*
				 * time to grow!
				 */
				final long[] tmpArray = new long[array.length + growthSize];
				System.arraycopy(array, 0, tmpArray, 0, array.length);
				array = tmpArray;
			}
			array[pointer++] = l;
			return this;
		}

		public long[] toArray() {
			if (trimmedArray == null) {
				trimmedArray = new long[pointer];
				System.arraycopy(array, 0, trimmedArray, 0, trimmedArray.length);
			}
			return trimmedArray;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return Arrays.toString(toArray());
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#clone()
		 */
		@Override
		public Object clone() {
			SmartLongArray clone;
			try {
				clone = (SmartLongArray) super.clone();
				clone.array = new long[this.array.length];
				System.arraycopy(array, 0, clone.array, 0, array.length);
				if (trimmedArray != null) {
					clone.trimmedArray = new long[this.trimmedArray.length];
					System.arraycopy(trimmedArray, 0, clone.trimmedArray, 0, trimmedArray.length);
				}
				return clone;
			} catch (final CloneNotSupportedException e) {
				throw new InternalError(e.getMessage());
			}
		}
	}

	private class UploadQuotaChecker implements UploadListener {

		private static final String WARN01 = "Upload Quota is less than zero."
				+ " Using global server property \"MAX_UPLOAD_SIZE\" instead.";

		private final long uploadQuota;

		private final long uploadQuotaPerFile;

		private final HttpServletResponse resp;

		private final String actionStr;

		private final boolean doAction;

		public UploadQuotaChecker(final long uploadQuota, final long uploadQuotaPerFile,
				final HttpServletResponse resp, final String actionStr) {
			this.uploadQuota = uploadQuota;
			this.uploadQuotaPerFile = uploadQuotaPerFile;
			this.resp = resp;
			this.actionStr = actionStr;
			doAction = ((uploadQuotaPerFile > 0) || (uploadQuota > 0));
		}

		public UploadQuotaChecker(final UserSettingMail usm, final HttpServletResponse resp, final String actionStr) {
			if (usm.getUploadQuota() >= 0) {
				this.uploadQuota = usm.getUploadQuota();
			} else {
				if (LOG.isWarnEnabled()) {
					LOG.warn(WARN01);
				}
				long tmp;
				try {
					tmp = ServerConfig.getInteger(Property.MAX_UPLOAD_SIZE);
				} catch (final ConfigurationException e) {
					LOG.error(e.getLocalizedMessage(), e);
					tmp = 0;
				}
				this.uploadQuota = tmp;
			}
			this.uploadQuotaPerFile = usm.getUploadQuotaPerFile();
			this.resp = resp;
			this.actionStr = actionStr;
			doAction = ((uploadQuotaPerFile > 0) || (uploadQuota > 0));
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.openexchange.groupware.upload.UploadListener#action(com.openexchange.groupware.upload.UploadEvent)
		 */
		public boolean action(final UploadEvent uploadEvent) throws UploadServletException {
			if (!doAction) {
				return true;
			} else if (uploadEvent.getAffiliationId() != UploadEvent.MAIL_UPLOAD) {
				return false;
			}
			long totalSize = 0;
			final int numOfUploadFiles = uploadEvent.getNumberOfUploadFiles();
			final Iterator<UploadFile> iter = uploadEvent.getUploadFilesIterator();
			for (int i = 0; i < numOfUploadFiles; i++) {
				final UploadFile uploadFile = iter.next();
				if (uploadQuotaPerFile > 0 && uploadFile.getSize() > uploadQuotaPerFile) {
					final MailException oxme = new MailException(MailException.Code.UPLOAD_QUOTA_EXCEEDED_FOR_FILE,
							Long.valueOf(uploadQuotaPerFile), uploadFile.getFileName(), Long.valueOf(uploadFile
									.getSize()));
					JSONObject responseObj = null;
					try {
						final Response response = new Response();
						response.setException(oxme);
						responseObj = response.getJSON();
					} catch (final JSONException e) {
						LOG.error(e.getMessage(), e);
					}
					throw new UploadServletException(resp, JS_FRAGMENT.replaceFirst(JS_FRAGMENT_JSON,
							responseObj == null ? STR_NULL : Matcher.quoteReplacement(responseObj.toString()))
							.replaceFirst(JS_FRAGMENT_ACTION, actionStr), oxme.getMessage(), oxme);
				}
				/*
				 * Add current file size
				 */
				totalSize += uploadFile.getSize();
				if (uploadQuota > 0 && totalSize > uploadQuota) {
					final MailException me = new MailException(MailException.Code.UPLOAD_QUOTA_EXCEEDED, Long
							.valueOf(uploadQuota));
					JSONObject responseObj = null;
					try {
						final Response response = new Response();
						response.setException(me);
						responseObj = response.getJSON();
					} catch (final JSONException e) {
						LOG.error(e.getMessage(), e);
					}
					throw new UploadServletException(resp, JS_FRAGMENT.replaceFirst(JS_FRAGMENT_JSON,
							responseObj == null ? STR_NULL : Matcher.quoteReplacement(responseObj.toString()))
							.replaceFirst(JS_FRAGMENT_ACTION, actionStr), me.getMessage(), me);
				}
			}
			return true;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.openexchange.groupware.upload.UploadListener#getRegistry()
		 */
		public UploadRegistry getRegistry() {
			return Mail.this.getRegistry();
		}

		/**
		 * @return upload quota
		 */
		public long getUploadQuota() {
			return uploadQuota;
		}

		/**
		 * @return upload quota per file
		 */
		public long getUploadQuotaPerFile() {
			return uploadQuotaPerFile;
		}

	}

}
