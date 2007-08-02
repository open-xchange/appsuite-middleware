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

import static com.openexchange.tools.oxfolder.OXFolderManagerImpl.getFolderName;
import static com.openexchange.tools.oxfolder.OXFolderManagerImpl.getUserName;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.activation.MimetypesFileTypeMap;
import javax.mail.Header;
import javax.mail.Message;
import javax.mail.internet.MimeUtility;
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
import com.openexchange.ajax.writer.MailWriter;
import com.openexchange.ajax.writer.MailWriter.MailFieldWriter;
import com.openexchange.api.OXMandatoryFieldException;
import com.openexchange.api.OXPermissionException;
import com.openexchange.api2.MailInterface;
import com.openexchange.api2.MailInterfaceImpl;
import com.openexchange.api2.OXException;
import com.openexchange.cache.FolderCacheManager;
import com.openexchange.cache.MessageCacheManager;
import com.openexchange.configuration.ServerConfig;
import com.openexchange.configuration.ServerConfig.Property;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.Component;
import com.openexchange.groupware.AbstractOXException.Category;
import com.openexchange.groupware.container.CommonObject;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.container.mail.JSONMessageAttachmentObject;
import com.openexchange.groupware.container.mail.JSONMessageObject;
import com.openexchange.groupware.container.mail.MessageCacheObject;
import com.openexchange.groupware.container.mail.parser.MessageUtils;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.infostore.InfostoreFacade;
import com.openexchange.groupware.infostore.utils.Metadata;
import com.openexchange.groupware.upload.UploadEvent;
import com.openexchange.groupware.upload.UploadException;
import com.openexchange.groupware.upload.UploadFile;
import com.openexchange.groupware.upload.UploadListener;
import com.openexchange.groupware.upload.UploadRegistry;
import com.openexchange.imap.IMAPException;
import com.openexchange.imap.IMAPProperties;
import com.openexchange.imap.OXMailException;
import com.openexchange.imap.UserSettingMail;
import com.openexchange.imap.OXMailException.MailCode;
import com.openexchange.imap.threadsort.ThreadSortMessage;
import com.openexchange.json.OXJSONWriter;
import com.openexchange.server.EffectivePermission;
import com.openexchange.sessiond.SessionObject;
import com.openexchange.tools.encoding.Helper;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorException;
import com.openexchange.tools.mail.ContentType;
import com.openexchange.tools.oxfolder.OXFolderException;
import com.openexchange.tools.oxfolder.OXFolderException.FolderCode;
import com.openexchange.tools.servlet.UploadServletException;
import com.openexchange.tools.servlet.http.Tools;

/**
 * Mail - the servlet to handle mail requests
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public class Mail extends PermissionServlet implements UploadListener {

	private static final String ENC_Q = "Q";

	private static final String MIME_MULTIPART = "multipart/";

	private static final String PARAMETER_PATTERN = "pattern";

	private static final String PARAMETER_COL = "col";

	private static final String MIME_TEXT_HTML_CHARSET_UTF_8 = "text/html; charset=UTF-8";

	private static final String STR_INLINE_FILENAME = "inline; filename=\"";

	private static final String STR_NAME = "name";

	private static final String MIME_APPLICATION_OCTET_STREAM = "application/octet-stream";

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
		return new AbstractOXException(Component.EMAIL, Category.INTERNAL_ERROR, 9999, cause.getMessage(), cause);
	}

	public static final char SEPERATOR = '/';

	private static final String UPLOAD_PARAM_MAILINTERFACE = "mi";

	private static final String UPLOAD_PARAM_WRITER = "w";

	private static final String UPLOAD_PARAM_SESSION = "s";

	private static final String STR_CHARSET = "charset";

	private static final String STR_UTF8 = "UTF-8";

	private static final String JSON_KEY_DATA = "data";

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
		} catch (Exception e) {
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
		} catch (Exception e) {
			LOG.error("doGet", e);
			writeError(e.toString(), new JSONWriter(resp.getWriter()));
		}
	}

	private final static void writeError(final String error, final JSONWriter jsonWriter) {
		try {
			startResponse(jsonWriter);
			jsonWriter.value(STR_EMPTY);
			endResponse(jsonWriter, null, error);
		} catch (Exception exc) {
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

	public void actionGetUpdates(final SessionObject sessionObj, final JSONWriter writer, final JSONObject requestObj,
			final MailInterface mi) throws JSONException {
		Response.write(actionGetUpdates(sessionObj, ParamContainer.getInstance(requestObj, Component.EMAIL), mi),
				writer);
	}

	private final void actionGetUpdates(final HttpServletRequest req, final HttpServletResponse resp)
			throws IOException, ServletException {
		try {
			Response.write(actionGetUpdates(getSessionObject(req), ParamContainer.getInstance(req, Component.EMAIL,
					resp), null), resp.getWriter());
		} catch (JSONException e) {
			sendErrorAsJS(resp, RESPONSE_ERROR);
		}
	}
	
	private final transient static JSONArray EMPTY_JSON_ARR = new JSONArray();

	private final Response actionGetUpdates(final SessionObject sessionObj, final ParamContainer paramContainer,
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

	public void actionGetMailCount(final SessionObject sessionObj, final JSONWriter writer,
			final JSONObject requestObj, final MailInterface mi) throws JSONException {
		Response.write(actionGetMailCount(sessionObj, ParamContainer.getInstance(requestObj, Component.EMAIL), mi),
				writer);
	}

	private final void actionGetMailCount(final HttpServletRequest req, final HttpServletResponse resp)
			throws IOException, ServletException {
		try {
			Response.write(actionGetMailCount(getSessionObject(req), ParamContainer.getInstance(req, Component.EMAIL,
					resp), null), resp.getWriter());
		} catch (JSONException e) {
			sendErrorAsJS(resp, RESPONSE_ERROR);
		}
	}

	private final Response actionGetMailCount(final SessionObject sessionObj, final ParamContainer paramContainer,
			final MailInterface mailInterfaceArg) {
		/*
		 * Some variables
		 */
		final Response response = new Response();
		/*
		 * Start response
		 */
		Object retval = JSONObject.NULL;
		try {
			final String folderId = paramContainer.checkStringParam(PARAMETER_MAILFOLDER);
			MailInterface mailInterface = mailInterfaceArg;
			boolean closeMailInterface = false;
			try {
				if (mailInterface == null) {
					mailInterface = MailInterfaceImpl.getInstance(sessionObj);
					closeMailInterface = true;
				}
				retval = Integer.valueOf(mailInterface.getAllMessageCount(folderId)[0]);
			} finally {
				if (closeMailInterface && mailInterface != null) {
					mailInterface.close(true);
				}
				mailInterface = null;
			}
		} catch (OXMailException e) {
			LOG.error(e.getMessage(), e);
			if (!e.getCategory().equals(Category.USER_CONFIGURATION)) {
				response.setException(e);
			}
		} catch (AbstractOXException e) {
			response.setException(e);
		} catch (Exception e) {
			LOG.error("actionGetMailCount", e);
			response.setException(getWrappingOXException(e));
		}
		/*
		 * Close response and flush print writer
		 */
		response.setData(retval);
		response.setTimestamp(null);
		return response;
	}

	public void actionGetAllMails(final SessionObject sessionObj, final JSONWriter writer, final JSONObject requestObj,
			final MailInterface mi) throws SearchIteratorException, JSONException {
		Response.write(actionGetAllMails(sessionObj, ParamContainer.getInstance(requestObj, Component.EMAIL), mi),
				writer);
	}

	private final void actionGetAllMails(final HttpServletRequest req, final HttpServletResponse resp)
			throws IOException, ServletException {
		try {
			Response.write(actionGetAllMails(getSessionObject(req), ParamContainer.getInstance(req, Component.EMAIL,
					resp), null), resp.getWriter());
		} catch (JSONException e) {
			sendErrorAsJS(resp, RESPONSE_ERROR);
		} catch (SearchIteratorException e) {
			sendErrorAsJS(resp, RESPONSE_ERROR);
		}
	}

	private static final String STR_ASC = "asc";

	private static final String STR_DESC = "desc";

	private final Response actionGetAllMails(final SessionObject sessionObj, final ParamContainer paramContainer,
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
		SearchIterator it = null;
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
				throw new OXMailException(MailCode.MISSING_PARAM, PARAMETER_ORDER);
			}
			/*
			 * Get all mails
			 */
			MailInterface mailInterface = mailInterfaceArg;
			boolean closeMailInterface = false;
			MailWriter mailWriter = null;
			try {
				if (mailInterface == null) {
					mailInterface = MailInterfaceImpl.getInstance(sessionObj);
					closeMailInterface = true;
				}
				mailWriter = new MailWriter(jsonWriter, sessionObj);
				/*
				 * Pre-Select field writers
				 */
				final MailFieldWriter[] writers = mailWriter.getMailFieldWriters(columns);
				/*
				 * Clear cache
				 */
				if (MessageCacheManager.getInstance().containsUserMessages(sessionObj)) {
					MessageCacheManager.getInstance().clearUserMessages(sessionObj);
				}
				/*
				 * Receive message iterator
				 */
				if (threadSort) {
					it = mailInterface.getAllThreadedMessages(folderId, columns);
					final int size = it.size();
					final boolean useCache = ((size < IMAPProperties.getMessageFetchLimit()));
					for (int i = 0; i < size; i++) {
						final MessageCacheObject msg = (MessageCacheObject) it.next();
						if (useCache) {
							/*
							 * Put into cache
							 */
							MessageCacheManager.getInstance().putMessage(sessionObj, msg.getUid(), msg);
						}
						jsonWriter.array();
						try {
							for (int j = 0; j < writers.length; j++) {
								writers[j].writeField(jsonWriter, msg, msg.getThreadLevel(), false);
							}
						} finally {
							jsonWriter.endArray();
						}
					}
				} else {
					final int sortCol = sort == null ? JSONMessageObject.FIELD_RECEIVED_DATE : Integer.parseInt(sort);
					int orderDir = MailInterface.ORDER_ASC;
					if (order != null) {
						if (order.equalsIgnoreCase(STR_ASC)) {
							orderDir = MailInterface.ORDER_ASC;
						} else if (order.equalsIgnoreCase(STR_DESC)) {
							orderDir = MailInterface.ORDER_DESC;
						} else {
							throw new OXMailException(MailCode.INVALID_INT_VALUE, PARAMETER_ORDER);
						}
					}
					/*
					 * Get iterator
					 */
					it = mailInterface.getAllMessages(folderId, sortCol, orderDir, columns);
					final int size = it.size();
					/*
					 * Cache must not be used if number of contained messages
					 * exceeds max. fetch limit. If it does, slim message
					 * objects of type MessageCacheObject are going to be
					 * returned on fetch request which are only filled with
					 * fields necessary to fullfill client request and sorting.
					 * Thus upcoming client's PUT request would end up in an
					 * exception telling that field is not present in message
					 * object.
					 */
					final boolean useCache = ((size < IMAPProperties.getMessageFetchLimit()));
					for (int i = 0; i < size; i++) {
						final MessageCacheObject msg = (MessageCacheObject) it.next();
						if (useCache) {
							/*
							 * Put into cache
							 */
							MessageCacheManager.getInstance().putMessage(sessionObj, msg.getUid(), msg);
						}
						/*
						 * Write message
						 */
						jsonWriter.array();
						try {
							for (int j = 0; j < writers.length; j++) {
								writers[j].writeField(jsonWriter, msg, 0, false);
							}
						} finally {
							jsonWriter.endArray();
						}
					}
				}
			} finally {
				if (closeMailInterface && mailInterface != null) {
					mailInterface.close(true);
				}
				mailInterface = null;
			}
		} catch (OXMailException e) {
			LOG.error(e.getMessage(), e);
			if (!e.getCategory().equals(Category.USER_CONFIGURATION)) {
				response.setException(e);
			}
		} catch (AbstractOXException e) {
			LOG.error(e.getMessage(), e);
			response.setException(e);
		} catch (Exception e) {
			LOG.error("actionGetAllMails", e);
			response.setException(getWrappingOXException(e));
		} finally {
			if (it != null) {
				it.close();
			}
			it = null;
		}
		/*
		 * Close response and flush print writer
		 */
		jsonWriter.endArray();
		response.setData(jsonWriter.getObject());
		response.setTimestamp(null);
		return response;
		// Response.write(response, writer);
	}

	public void actionGetReply(final SessionObject sessionObj, final JSONWriter writer, final JSONObject jo,
			final boolean reply2all, final MailInterface mailInterface) throws JSONException {
		Response.write(actionGetReply(sessionObj, reply2all, ParamContainer.getInstance(jo, Component.EMAIL),
				mailInterface), writer);
	}

	private final void actionGetReply(final HttpServletRequest req, final HttpServletResponse resp,
			final boolean reply2all) throws IOException, ServletException {
		try {
			Response.write(actionGetReply(getSessionObject(req), reply2all, ParamContainer.getInstance(req,
					Component.EMAIL, resp), null), resp.getWriter());
		} catch (JSONException e) {
			sendErrorAsJS(resp, RESPONSE_ERROR);
		}
	}

	private final Response actionGetReply(final SessionObject sessionObj, final boolean reply2all,
			final ParamContainer paramContainer, final MailInterface mailInterfaceArg) {
		/*
		 * final Some variables
		 */
		final Response response = new Response();
		final OXJSONWriter jsonWriter = new OXJSONWriter();
		/*
		 * Start response
		 */
		boolean valueWritten = false;
		try {
			/*
			 * Read in parameters
			 */
			final MailIdentifier mailIdentifier = new MailIdentifier(paramContainer.checkStringParam(PARAMETER_ID));
			/*
			 * Get reply message
			 */
			MailInterface mailInterface = mailInterfaceArg;
			boolean closeMailInterface = false;
			MailWriter mailWriter = null;
			try {
				if (mailInterfaceArg == null) {
					mailInterface = MailInterfaceImpl.getInstance(sessionObj);
					closeMailInterface = true;
				}
				mailWriter = new MailWriter(jsonWriter, sessionObj);
				mailWriter.writeJSONMessageObject(mailInterface.getReplyMessageForDisplay(mailIdentifier.folder,
						mailIdentifier.msgUID, reply2all));
				valueWritten = true;
			} finally {
				if (closeMailInterface && mailInterface != null) {
					mailInterface.close(true);
					mailInterface = null;
				}
			}
		} catch (OXMailException e) {
			LOG.error(e.getMessage(), e);
			if (!e.getCategory().equals(Category.USER_CONFIGURATION)) {
				response.setException(e);
			}
		} catch (AbstractOXException e) {
			LOG.error(e.getMessage(), e);
			response.setException(e);
		} catch (Exception e) {
			LOG.error("actionGetReply", e);
			response.setException(getWrappingOXException(e));
		}
		/*
		 * Close response and flush print writer
		 */
		response.setData(valueWritten ? jsonWriter.getObject() : JSONObject.NULL);
		response.setTimestamp(null);
		return response;
	}

	public void actionGetForward(final SessionObject sessionObj, final JSONWriter writer, final JSONObject requestObj,
			final MailInterface mailInterface) throws JSONException {
		Response.write(actionGetForward(sessionObj, ParamContainer.getInstance(requestObj, Component.EMAIL),
				mailInterface), writer);
	}

	private final void actionGetForward(final HttpServletRequest req, final HttpServletResponse resp)
			throws IOException, ServletException {
		try {
			Response.write(actionGetForward(getSessionObject(req), ParamContainer.getInstance(req, Component.EMAIL,
					resp), null), resp.getWriter());
		} catch (JSONException e) {
			sendErrorAsJS(resp, RESPONSE_ERROR);
		}
	}

	private final Response actionGetForward(final SessionObject sessionObj, final ParamContainer paramContainer,
			final MailInterface mailInterfaceArg) {
		/*
		 * Some variables
		 */
		final Response response = new Response();
		final OXJSONWriter jsonWriter = new OXJSONWriter();
		/*
		 * Start response
		 */
		boolean valueWritten = false;
		try {
			/*
			 * Read in parameters
			 */
			final MailIdentifier mailIdentifier = new MailIdentifier(paramContainer.checkStringParam(PARAMETER_ID));
			/*
			 * Get forward message
			 */
			MailInterface mailInterface = mailInterfaceArg;
			boolean closeMailInterface = false;
			MailWriter mailWriter = null;
			try {
				if (mailInterface == null) {
					mailInterface = MailInterfaceImpl.getInstance(sessionObj);
					closeMailInterface = true;
				}
				mailWriter = new MailWriter(jsonWriter, sessionObj);
				mailWriter.writeJSONMessageObject(mailInterface.getForwardMessageForDisplay(mailIdentifier.folder,
						mailIdentifier.msgUID));
				valueWritten = true;
			} finally {
				if (closeMailInterface && mailInterface != null) {
					mailInterface.close(true);
					mailInterface = null;
				}
			}
		} catch (OXMailException e) {
			LOG.error(e.getMessage(), e);
			if (!e.getCategory().equals(Category.USER_CONFIGURATION)) {
				response.setException(e);
			}
		} catch (AbstractOXException e) {
			LOG.error(e.getMessage(), e);
			response.setException(e);
		} catch (Exception e) {
			LOG.error("actionGetForward", e);
			response.setException(getWrappingOXException(e));
		}
		/*
		 * Close response and flush print writer
		 */
		response.setData(valueWritten ? jsonWriter.getObject() : JSONObject.NULL);
		response.setTimestamp(null);
		return response;
	}

	public void actionGetMessage(final SessionObject sessionObj, final JSONWriter writer, final JSONObject requestObj,
			final MailInterface mi) throws JSONException {
		Response.write(actionGetMessage(sessionObj, ParamContainer.getInstance(requestObj, Component.EMAIL), mi),
				writer);
	}

	private final void actionGetMessage(final HttpServletRequest req, final HttpServletResponse resp)
			throws IOException, ServletException {
		try {
			Response.write(actionGetMessage(getSessionObject(req), ParamContainer.getInstance(req, Component.EMAIL,
					resp), null), resp.getWriter());
		} catch (JSONException e) {
			sendErrorAsJS(resp, RESPONSE_ERROR);
		}
	}

	@SuppressWarnings("unchecked")
	private final Response actionGetMessage(final SessionObject sessionObj, final ParamContainer paramContainer,
			final MailInterface mailInterfaceArg) {
		/*
		 * Some variables
		 */
		final Response response = new Response();
		final OXJSONWriter jsonWriter = new OXJSONWriter();
		Object data = null;
		/*
		 * Start response
		 */
		boolean valueWritten = false;
		try {
			/*
			 * Read in parameters
			 */
			final MailIdentifier mailIdentifier = new MailIdentifier(paramContainer.checkStringParam(PARAMETER_ID));
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
					mailInterface = MailInterfaceImpl.getInstance(sessionObj);
					closeMailInterface = true;
				}
				final Message msg = mailInterface.getMessage(mailIdentifier.folder, mailIdentifier.msgUID);
				if (msg == null) {
					throw new OXMailException(MailCode.MESSAGE_NOT_FOUND, Long.valueOf(mailIdentifier.msgUID),
							mailIdentifier.folder);
				}
				if (showMessageSource) {
					final ByteArrayOutputStream baos = new ByteArrayOutputStream();
					msg.writeTo(baos);
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
						final String fileName = new StringBuilder(msg.getSubject().replaceAll(" ", "_")).append(".eml")
								.toString();
						paramContainer.getHttpServletResponse().setHeader(
								STR_CONTENT_DISPOSITION,
								new StringBuilder(50).append(STR_ATTACHMENT_FILENAME).append(
										getSaveAsFileName(fileName, internetExplorer)).append('"').toString());
						paramContainer.getHttpServletResponse().setContentType(contentType.toString());
						final OutputStream out = paramContainer.getHttpServletResponse().getOutputStream();
						out.write(baos.toByteArray());
						/*
						 * ... and return
						 */
						return null;
					}
					final ContentType ct = new ContentType(msg.getContentType());
					data = new String(baos.toByteArray(), ct.containsParameter(STR_CHARSET) ? ct
							.getParameter(STR_CHARSET) : STR_UTF8);
				} else if (showMessageHeaders) {
					data = formatMessageHeaders(msg.getAllHeaders());
				} else {
					new MailWriter(jsonWriter, sessionObj).writeMessageAsJSONObject(msg, !editDraft);
					data = jsonWriter.getObject();
				}
				valueWritten = true;
			} finally {
				if (closeMailInterface && mailInterface != null) {
					mailInterface.close(true);
					mailInterface = null;
				}
			}
		} catch (OXMailException e) {
			LOG.error(e.getMessage(), e);
			if (!e.getCategory().equals(Category.USER_CONFIGURATION)) {
				response.setException(e);
			}
		} catch (AbstractOXException e) {
			LOG.error(e.getMessage(), e);
			response.setException(e);
		} catch (Exception e) {
			LOG.error("actionGetMessage", e);
			response.setException(getWrappingOXException(e));
		}
		/*
		 * Close response and flush print writer
		 */
		response.setData(valueWritten ? data : JSONObject.NULL);
		response.setTimestamp(null);
		return response;
	}

	private static final String formatMessageHeaders(final Enumeration<Header> e) {
		final StringBuilder sb = new StringBuilder(500);
		while (e.hasMoreElements()) {
			final Header hdr = e.nextElement();
			sb.append(hdr.getName()).append(STR_DELIM).append(hdr.getValue()).append(STR_CRLF);
		}
		return sb.toString();
	}

	public void actionGetNew(final SessionObject sessionObj, final JSONWriter writer, final JSONObject requestObj,
			final MailInterface mi) throws SearchIteratorException, JSONException {
		Response.write(actionGetNew(sessionObj, ParamContainer.getInstance(requestObj, Component.EMAIL), mi), writer);
	}

	private final void actionGetNew(final HttpServletRequest req, final HttpServletResponse resp) throws IOException,
			ServletException {
		try {
			Response.write(actionGetNew(getSessionObject(req), ParamContainer.getInstance(req, Component.EMAIL, resp),
					null), resp.getWriter());
		} catch (JSONException e) {
			sendErrorAsJS(resp, RESPONSE_ERROR);
		} catch (SearchIteratorException e) {
			sendErrorAsJS(resp, RESPONSE_ERROR);
		}
	}

	private final Response actionGetNew(final SessionObject sessionObj, final ParamContainer paramContainer,
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
		SearchIterator it = null;
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
			MailWriter mailWriter = null;
			try {
				if (mailInterface == null) {
					mailInterface = MailInterfaceImpl.getInstance(sessionObj);
					closeMailInterface = true;
				}
				mailWriter = new MailWriter(jsonWriter, sessionObj);
				/*
				 * Receive message iterator
				 */
				final int sortCol = sort == null ? JSONMessageObject.FIELD_SENT_DATE : Integer.parseInt(sort);
				int orderDir = MailInterface.ORDER_ASC;
				if (order != null) {
					if (order.equalsIgnoreCase(STR_ASC)) {
						orderDir = MailInterface.ORDER_ASC;
					} else if (order.equalsIgnoreCase(STR_DESC)) {
						orderDir = MailInterface.ORDER_DESC;
					} else {
						throw new OXMailException(MailCode.INVALID_INT_VALUE, PARAMETER_ORDER);
					}
				}
				/*
				 * Pre-Select field writers
				 */
				final MailFieldWriter[] writers = mailWriter.getMailFieldWriters(columns);
				it = mailInterface.getNewMessages(folderId, sortCol, orderDir, columns,
						limit == ParamContainer.NOT_FOUND ? -1 : limit);
				final int size = it.size();
				for (int i = 0; i < size; i++) {
					final Message msg = (Message) it.next();
					jsonWriter.array();
					try {
						for (int j = 0; j < writers.length; j++) {
							writers[j].writeField(jsonWriter, msg, 0, false);
						}
					} finally {
						jsonWriter.endArray();
					}
				}
			} finally {
				if (closeMailInterface && mailInterface != null) {
					mailInterface.close(true);
					mailInterface = null;
				}
			}
		} catch (OXMailException e) {
			LOG.error(e.getMessage(), e);
			if (!e.getCategory().equals(Category.USER_CONFIGURATION)) {
				response.setException(e);
			}
		} catch (AbstractOXException e) {
			LOG.error(e.getMessage(), e);
			response.setException(e);
		} catch (Exception e) {
			LOG.error("actionGetNew", e);
			response.setException(getWrappingOXException(e));
		} finally {
			if (it != null) {
				it.close();
				it = null;
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

	public void actionGetSaveVersit(final SessionObject sessionObj, final Writer writer, final JSONObject requestObj,
			final MailInterface mi) throws Exception {
		actionGetSaveVersit(sessionObj, writer, ParamContainer.getInstance(requestObj, Component.EMAIL), mi);
	}

	private final void actionGetSaveVersit(final HttpServletRequest req, final HttpServletResponse resp)
			throws IOException, ServletException {
		try {
			actionGetSaveVersit(getSessionObject(req), resp.getWriter(), ParamContainer.getInstance(req,
					Component.EMAIL, resp), null);
		} catch (JSONException e) {
			sendErrorAsJS(resp, RESPONSE_ERROR);
		}
	}

	private final void actionGetSaveVersit(final SessionObject sessionObj, final Writer writer,
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
				final MailIdentifier mailIdentifier = new MailIdentifier(msgUID);
				if (mailInterface == null) {
					mailInterface = MailInterfaceImpl.getInstance(sessionObj);
					closeMailInterface = true;
				}
				final CommonObject[] insertedObjs = mailInterface.saveVersitAttachment(mailIdentifier.folder,
						mailIdentifier.msgUID, partIdentifier);
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
					mailInterface = null;
				}
			}
		} catch (OXMailException e) {
			LOG.error(e.getMessage(), e);
			if (!e.getCategory().equals(Category.USER_CONFIGURATION)) {
				response.setException(e);
			}
		} catch (AbstractOXException e) {
			LOG.error(e.getMessage(), e);
			response.setException(e);
		} catch (Exception e) {
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

	public void actionGetAttachment() throws OXMailException {
		throw new OXMailException(MailCode.UNSUPPORTED_ACTION, ACTION_MATTACH, "Multiple servlet");
	}

	/**
	 * Looks up a mail attachment and writes its content directly into response
	 * output stream. This method is not accessible via Mutliple servlet
	 */
	private final void actionGetAttachment(final HttpServletRequest req, final HttpServletResponse resp)
			throws IOException {
		/*
		 * Some variables
		 */
		final SessionObject sessionObj = getSessionObject(req);
		boolean outSelected = false;
		boolean saveToDisk = false;
		/*
		 * Start response
		 */
		try {
			/*
			 * Read in parameters
			 */
			final MailIdentifier mailIdentifier = new MailIdentifier(checkStringParam(req, PARAMETER_ID));
			final String attachmentIdentifier = req.getParameter(PARAMETER_MAILATTCHMENT);
			final String imageContentId = req.getParameter(PARAMETER_MAILCID);
			String saveIdentifier = req.getParameter(PARAMETER_SAVE);
			/*
			 * Get attachment
			 */
			MailInterface mailInterface = null;
			try {
				mailInterface = MailInterfaceImpl.getInstance(sessionObj);
				if (attachmentIdentifier == null && imageContentId == null) {
					throw new OXMailException(MailCode.MISSING_PARAM, new StringBuilder().append(
							PARAMETER_MAILATTCHMENT).append(" | ").append(PARAMETER_MAILCID).toString());
				}
				final JSONMessageAttachmentObject mao;
				if (imageContentId == null) {
					mao = mailInterface.getMessageAttachment(mailIdentifier.folder, mailIdentifier.msgUID,
							attachmentIdentifier);
					if (mao == null) {
						throw new OXMailException(MailCode.NO_ATTACHMENT_FOUND, attachmentIdentifier,
								mailIdentifier.str);
					}
				} else {
					mao = mailInterface.getMessageImage(mailIdentifier.folder, mailIdentifier.msgUID, imageContentId);
					if (mao == null) {
						throw new OXMailException(MailCode.NO_ATTACHMENT_FOUND, attachmentIdentifier,
								mailIdentifier.str);
					}
				}
				if (saveIdentifier == null || saveIdentifier.length() == 0) {
					saveIdentifier = "0";
				}
				saveToDisk = ((Integer.parseInt(saveIdentifier)) > 0);
				/*
				 * Write to response
				 */
				final Object content = mao.getContent();
				if (mao.getContentID() == JSONMessageAttachmentObject.CONTENT_INPUT_STREAM) {
					final String userAgent = req.getHeader(STR_USER_AGENT).toLowerCase(Locale.ENGLISH);
					final boolean internetExplorer = (userAgent != null && userAgent.indexOf(STR_MSIE) > -1 && userAgent
							.indexOf(STR_WINDOWS) > -1);
					final ContentType contentType;
					if (saveToDisk) {
						contentType = new ContentType();
						contentType.setPrimaryType(STR_APPLICATION);
						contentType.setSubType(STR_OCTET_STREAM);
						resp.setHeader(STR_CONTENT_DISPOSITION, new StringBuilder(50).append(STR_ATTACHMENT_FILENAME)
								.append(getSaveAsFileName(mao.getFileName(), internetExplorer)).append('"').toString());
					} else {
						final String fileName = getSaveAsFileName(mao.getFileName(), internetExplorer);
						contentType = new ContentType(mao.getContentType());
						if (contentType.getBaseType().equalsIgnoreCase(MIME_APPLICATION_OCTET_STREAM)) {
							/*
							 * Try to determine MIME type via JAF
							 */
							final String ct = MimetypesFileTypeMap.getDefaultFileTypeMap().getContentType(fileName);
							final int pos = ct.indexOf('/');
							contentType.setPrimaryType(ct.substring(0, pos));
							contentType.setSubType(ct.substring(pos + 1));
						}
						contentType.addParameter(STR_NAME, fileName);
						resp.setHeader(STR_CONTENT_DISPOSITION, new StringBuilder(50).append(STR_INLINE_FILENAME)
								.append(fileName).append('"').toString());
					}
					resp.setContentType(contentType.toString());
					/*
					 * Write from content's input stream to response output
					 * stream
					 */
					InputStream contentInputStream = null;
					/*
					 * Reset response header values since we are going to
					 * directly write into servlet's output stream and then some
					 * browsers do not allow header "Pragma"
					 */
					Tools.removeCachingHeader(resp);
					final OutputStream out = resp.getOutputStream();
					outSelected = true;
					try {
						contentInputStream = (InputStream) content;
						final byte[] buffer = new byte[0xFFFF];
						for (int len; (len = contentInputStream.read(buffer)) != -1;) {
							out.write(buffer, 0, len);
						}
					} finally {
						if (contentInputStream != null) {
							contentInputStream.close();
							contentInputStream = null;
						}
					}
				} else if (mao.getContentID() == JSONMessageAttachmentObject.CONTENT_BYTE_ARRAY) {
					final ContentType contentType = new ContentType(mao.getContentType());
					if (contentType.containsParameter(STR_CHARSET)) {
						contentType.addParameter(STR_CHARSET, STR_UTF8);
					}
					final String contentTypeStr = contentType.toString();
					resp.setContentType(contentTypeStr);
					final byte[] contentBytes = (byte[]) content;
					/*
					 * Reset response header values since we are going to
					 * directly write into servlet's output stream and then some
					 * browsers do not allow header "Pragma"
					 */
					Tools.removeCachingHeader(resp);
					final OutputStream out = resp.getOutputStream();
					outSelected = true;
					out.write(contentBytes);
				}
			} finally {
				if (mailInterface != null) {
					mailInterface.close(true);
					mailInterface = null;
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
			return;
		}
		/*
		 * flush output stream
		 */
		resp.getOutputStream().flush();
	}

	private static final Pattern PART_FILENAME_PATTERN = Pattern.compile("(part )([0-9]+)(\\.)([0-9]+)",
			Pattern.CASE_INSENSITIVE);

	private static final String DEFAULT_FILENAME = "file.dat";

	public static final String getSaveAsFileName(final String fileName, final boolean internetExplorer) {
		if (null == fileName) {
			return DEFAULT_FILENAME;
		}
		final Matcher m = PART_FILENAME_PATTERN.matcher(fileName);
		if (m.matches()) {
			return fileName.replaceAll(" ", "_");
		}
		try {
			return new String(Helper.encodeFilename(fileName, STR_UTF8, internetExplorer).getBytes("US-ASCII"));
		} catch (UnsupportedEncodingException e) {
			LOG.error("Unsupported encoding in a message detected and monitored.", e);
			MailInterfaceImpl.mailInterfaceMonitor.addUnsupportedEncodingExceptions(e.getMessage());
			return fileName;
		}
	}

	public void actionPutClear(final SessionObject session, final JSONWriter writer, final JSONObject jsonObj,
			final MailInterface mi) throws JSONException {
		Response.write(actionPutClear(session, jsonObj.getString(JSON_KEY_DATA), ParamContainer.getInstance(jsonObj,
				Component.EMAIL), mi), writer);
	}

	private final void actionPutClear(final HttpServletRequest req, final HttpServletResponse resp) throws IOException,
			ServletException {
		try {
			Response.write(actionPutClear(getSessionObject(req), getBody(req), ParamContainer.getInstance(req,
					Component.EMAIL, resp), null), resp.getWriter());
		} catch (JSONException e) {
			sendErrorAsJS(resp, RESPONSE_ERROR);
		}
	}

	private final Response actionPutClear(final SessionObject sessionObj, final String body,
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
						mailInterface = MailInterfaceImpl.getInstance(sessionObj);
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
					mailInterface = null;
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

	public void actionPutMailSearch(final SessionObject session, final JSONWriter writer, final JSONObject jsonObj,
			final MailInterface mi) throws JSONException, SearchIteratorException {
		Response.write(actionPutMailSearch(session, jsonObj.getString(JSON_KEY_DATA), ParamContainer.getInstance(
				jsonObj, Component.EMAIL), mi), writer);
	}

	private final void actionPutMailSearch(final HttpServletRequest req, final HttpServletResponse resp)
			throws IOException, ServletException {
		try {
			Response.write(actionPutMailSearch(getSessionObject(req), getBody(req), ParamContainer.getInstance(req,
					Component.EMAIL, resp), null), resp.getWriter());
		} catch (JSONException e) {
			sendErrorAsJS(resp, RESPONSE_ERROR);
		} catch (SearchIteratorException e) {
			sendErrorAsJS(resp, RESPONSE_ERROR);
		}
	}

	private final Response actionPutMailSearch(final SessionObject sessionObj, final String body,
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
		SearchIterator it = null;
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
				throw new OXMailException(MailCode.MISSING_PARAM, PARAMETER_ORDER);
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
				MailWriter mailWriter = null;
				try {
					if (mailInterface == null) {
						mailInterface = MailInterfaceImpl.getInstance(sessionObj);
						closeMailInterface = true;
					}
					mailWriter = new MailWriter(jsonWriter, sessionObj);
					/*
					 * Pre-Select field writers
					 */
					final MailFieldWriter[] writers = mailWriter.getMailFieldWriters(columns);
					/*
					 * Receive message iterator
					 */
					if (threadSort) {
						it = mailInterface.getThreadedMessages(folderId, null, searchCols, searchPats, true, columns);
						final int size = it.size();
						for (int i = 0; i < size; i++) {
							final ThreadSortMessage threadSortedMsgObj = (ThreadSortMessage) it.next();
							jsonWriter.array();
							try {
								for (int j = 0; j < writers.length; j++) {
									writers[j].writeField(jsonWriter, threadSortedMsgObj.getMsg(), threadSortedMsgObj
											.getThreadLevel(), false);
								}
							} finally {
								jsonWriter.endArray();
							}
						}
					} else {
						final int sortCol = sort == null ? JSONMessageObject.FIELD_RECEIVED_DATE : Integer
								.parseInt(sort);
						int orderDir = MailInterface.ORDER_ASC;
						if (order != null) {
							if (order.equalsIgnoreCase(STR_ASC)) {
								orderDir = MailInterface.ORDER_ASC;
							} else if (order.equalsIgnoreCase(STR_DESC)) {
								orderDir = MailInterface.ORDER_DESC;
							} else {
								throw new OXMailException(MailCode.INVALID_INT_VALUE, PARAMETER_ORDER);
							}
						}
						it = mailInterface.getMessages(folderId, null, sortCol, orderDir, searchCols, searchPats, true,
								columns);
						final int size = it.size();
						for (int i = 0; i < size; i++) {
							final Message msg = (Message) it.next();
							jsonWriter.array();
							try {
								for (int j = 0; j < writers.length; j++) {
									writers[j].writeField(jsonWriter, msg, 0, false);
								}
							} finally {
								jsonWriter.endArray();
							}
						}
					}
				} finally {
					if (closeMailInterface && mailInterface != null) {
						mailInterface.close(true);
					}
					mailInterface = null;
				}
			}
		} catch (OXMailException e) {
			LOG.error(e.getMessage(), e);
			if (!e.getCategory().equals(Category.USER_CONFIGURATION)) {
				response.setException(e);
			}
		} catch (AbstractOXException e) {
			LOG.error(e.getMessage(), e);
			response.setException(e);
		} catch (Exception e) {
			LOG.error("actionPutMailList", e);
			response.setException(getWrappingOXException(e));
		} finally {
			if (it != null) {
				it.close();
			}
			it = null;
		}
		/*
		 * Close response and flush print writer
		 */
		jsonWriter.endArray();
		response.setData(jsonWriter.getObject());
		response.setTimestamp(null);
		return response;
	}

	public void actionPutMailList(final SessionObject session, final JSONWriter writer, final JSONObject jsonObj,
			final MailInterface mi) throws JSONException {
		Response.write(actionPutMailList(session, jsonObj.getString(JSON_KEY_DATA), ParamContainer.getInstance(jsonObj,
				Component.EMAIL), mi), writer);
	}

	private final void actionPutMailList(final HttpServletRequest req, final HttpServletResponse resp)
			throws IOException, ServletException {
		try {
			Response.write(actionPutMailList(getSessionObject(req), getBody(req), ParamContainer.getInstance(req,
					Component.EMAIL, resp), null), resp.getWriter());
		} catch (JSONException e) {
			sendErrorAsJS(resp, RESPONSE_ERROR);
		}
	}

	private final Response actionPutMailList(final SessionObject sessionObj, final String body,
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
				final MailWriter mailWriter = new MailWriter(jsonWriter, sessionObj);
				/*
				 * Pre-Select field writers
				 */
				final MailFieldWriter[] writers = mailWriter.getMailFieldWriters(columns);
				final Map<String, SmartLongArray> idMap = new HashMap<String, SmartLongArray>();
				fillMap(idMap, body, length);
				final int size = idMap.size();
				final Iterator<Map.Entry<String, SmartLongArray>> iter = idMap.entrySet().iterator();
				boolean tryWithNewCon = true;
				try {
					UseCache: if (MessageCacheManager.getInstance().containsUserMessages(sessionObj)) {
						/*
						 * Fetch from cache
						 */
						for (int k = 0; k < size; k++) {
							final Map.Entry<String, SmartLongArray> entry = iter.next();
							/*
							 * Get message list from cache
							 */
							final Message[] msgs = MessageCacheManager.getInstance().getMessages(sessionObj,
									entry.getValue().toArray(), entry.getKey());
							if (msgs == null || msgs.length == 0) {
								break UseCache;
							}
							tryWithNewCon = false;
							for (int i = 0; i < msgs.length; i++) {
								if (msgs[i] != null) {
									jsonWriter.array();
									try {
										for (int j = 0; j < writers.length; j++) {
											writers[j].writeField(jsonWriter, msgs[i], 0, false);
										}
									} finally {
										jsonWriter.endArray();
									}
								}
							}
						}
					}
				} catch (AbstractOXException ao) {
					LOG.error(ao.getMessage(), ao);
					tryWithNewCon = true;
				}
				if (tryWithNewCon) {
					MailInterface mailInterface = mailInterfaceArg;
					boolean closeMailInterface = false;
					try {
						if (mailInterface == null) {
							mailInterface = MailInterfaceImpl.getInstance(sessionObj);
							closeMailInterface = true;
						}
						for (int k = 0; k < size; k++) {
							final Map.Entry<String, SmartLongArray> entry = iter.next();
							/*
							 * Get message list
							 */
							final Message[] msgs = mailInterface.getMessageList(entry.getKey(), entry.getValue()
									.toArray(), columns);
							for (int i = 0; i < msgs.length; i++) {
								if (msgs[i] != null) {
									jsonWriter.array();
									try {
										for (int j = 0; j < writers.length; j++) {
											writers[j].writeField(jsonWriter, msgs[i], 0, false);
										}
									} finally {
										jsonWriter.endArray();
									}
								}
							}
						}
					} finally {
						if (closeMailInterface && mailInterface != null) {
							mailInterface.close(true);
							mailInterface = null;
						}
					}
				}
			}
		} catch (OXMailException e) {
			LOG.error(e.getMessage(), e);
			if (!e.getCategory().equals(Category.USER_CONFIGURATION)) {
				response.setException(e);
			}
		} catch (AbstractOXException e) {
			LOG.error(e.getMessage(), e);
			response.setException(e);
		} catch (Exception e) {
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

	public void actionPutDeleteMails(final SessionObject sessionObj, final JSONWriter writer, final JSONObject jsonObj,
			final MailInterface mi) throws JSONException {
		Response.write(actionPutDeleteMails(sessionObj, jsonObj.getString(JSON_KEY_DATA), ParamContainer.getInstance(
				jsonObj, Component.EMAIL), mi), writer);
	}

	private final void actionPutDeleteMails(final HttpServletRequest req, final HttpServletResponse resp)
			throws IOException, ServletException {
		try {
			Response.write(actionPutDeleteMails(getSessionObject(req), getBody(req), ParamContainer.getInstance(req,
					Component.EMAIL, resp), null), resp.getWriter());
		} catch (JSONException e) {
			sendErrorAsJS(resp, RESPONSE_ERROR);
		}
	}

	private final Response actionPutDeleteMails(final SessionObject sessionObj, final String body,
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
					mailInterface = MailInterfaceImpl.getInstance(sessionObj);
					closeMailInterface = true;
				}
				boolean isJSONObject = true;
				final MailIdentifier mailIdentifier = new MailIdentifier();
				final int length = jsonIDs.length();
				if (length > 0) {
					final List<MailIdentifier> l = new ArrayList<MailIdentifier>(length);
					for (int i = 0; i < length; i++) {
						if (isJSONObject) {
							try {
								mailIdentifier.setMailIdentifierString(jsonIDs.getJSONObject(i).getString(
										FolderFields.ID));
							} catch (JSONException e) {
								mailIdentifier.setMailIdentifierString(jsonIDs.getString(i));
								isJSONObject = false;
							}
						} else {
							mailIdentifier.setMailIdentifierString(jsonIDs.getString(i));
						}
						l.add((MailIdentifier) mailIdentifier.clone());
					}
					Collections.sort(l, MailIdentifier.getMailIdentifierComparator());
					String lastFld = l.get(0).folder;
					final SmartLongArray arr = new SmartLongArray(length);
					for (int i = 0; i < length; i++) {
						final MailIdentifier current = l.get(i);
						if (!lastFld.equals(current.folder)) {
							/*
							 * Delete all collected UIDs til here and reset
							 */
							mailInterface.deleteMessages(lastFld, arr.toArray(), hardDelete);
							arr.reset();
							lastFld = current.folder;
						}
						arr.append(current.msgUID);
					}
					if (arr.size() > 0) {
						mailInterface.deleteMessages(lastFld, arr.toArray(), hardDelete);
					}
				}
			} finally {
				if (closeMailInterface && mailInterface != null) {
					mailInterface.close(true);
					mailInterface = null;
				}
			}
		} catch (OXMailException e) {
			LOG.error(e.getMessage(), e);
			if (!e.getCategory().equals(Category.USER_CONFIGURATION)) {
				response.setException(e);
			}
		} catch (AbstractOXException e) {
			LOG.error(e.getMessage(), e);
			response.setException(e);
		} catch (Exception e) {
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

	public void actionPutUpdateMail(final SessionObject sessionObj, final JSONWriter writer, final JSONObject jsonObj,
			final MailInterface mailInterface) throws JSONException {
		Response.write(actionPutUpdateMail(sessionObj, jsonObj.getString(JSON_KEY_DATA), ParamContainer.getInstance(
				jsonObj, Component.EMAIL), mailInterface), writer);
	}

	private final void actionPutUpdateMail(final HttpServletRequest req, final HttpServletResponse resp)
			throws IOException, ServletException {
		try {
			Response.write(actionPutUpdateMail(getSessionObject(req), getBody(req), ParamContainer.getInstance(req,
					Component.EMAIL, resp), null), resp.getWriter());
		} catch (JSONException e) {
			sendErrorAsJS(resp, RESPONSE_ERROR);
		}
	}

	private final Response actionPutUpdateMail(final SessionObject sessionObj, final String body,
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
			final MailIdentifier mailIdentifier = new MailIdentifier(paramContainer.checkStringParam(PARAMETER_ID));
			final String sourceFolder = paramContainer.checkStringParam(PARAMETER_FOLDERID);
			final JSONObject bodyObj = new JSONObject(body);
			final String destFolder = bodyObj.has(FolderFields.FOLDER_ID) && !bodyObj.isNull(FolderFields.FOLDER_ID) ? bodyObj
					.getString(FolderFields.FOLDER_ID)
					: null;
			final Integer colorLabel = bodyObj.has(CommonFields.COLORLABEL) && !bodyObj.isNull(CommonFields.COLORLABEL) ? Integer
					.valueOf(bodyObj.getInt(CommonFields.COLORLABEL))
					: null;
			final Integer flagBits = bodyObj.has(JSONMessageObject.JSON_FLAGS)
					&& !bodyObj.isNull(JSONMessageObject.JSON_FLAGS) ? Integer.valueOf(bodyObj
					.getInt(JSONMessageObject.JSON_FLAGS)) : null;
			boolean flagVal = false;
			if (flagBits != null) {
				/*
				 * Look for boolean value
				 */
				flagVal = (bodyObj.has(JSONMessageObject.JSON_VALUE) && !bodyObj.isNull(JSONMessageObject.JSON_VALUE) ? bodyObj
						.getBoolean(JSONMessageObject.JSON_VALUE)
						: false);
			}
			MailInterface mailInterface = mailIntefaceArg;
			boolean closeMailInterface = false;
			try {
				if (mailInterface == null) {
					mailInterface = MailInterfaceImpl.getInstance(sessionObj);
					closeMailInterface = true;
				}
				if (destFolder != null) {
					/*
					 * Perform move operation
					 */
					mailInterface.copyMessages(sourceFolder, destFolder, new long[] { mailIdentifier.msgUID }, true);
				}
				if (colorLabel != null) {
					/*
					 * Update color label
					 */
					final MessageCacheObject msg = (MessageCacheObject) mailInterface.updateMessageColorLabel(
							sourceFolder, new long[] { mailIdentifier.msgUID }, colorLabel.intValue())[0];
					if (msg != null && MessageCacheManager.getInstance().containsUserMessages(sessionObj)) {
						/*
						 * Update cache entry
						 */
						MessageCacheManager.getInstance().putMessage(sessionObj, mailIdentifier.msgUID, msg);
					}
				}
				if (flagBits != null) {
					/*
					 * Update system flags which are allowed to be altered by
					 * client
					 */
					final Message[] msgs = mailInterface.updateMessageFlags(sourceFolder,
							new long[] { mailIdentifier.msgUID }, flagBits.intValue(), flagVal);
					if (msgs != null && MessageCacheManager.getInstance().containsUserMessages(sessionObj)) {
						/*
						 * Update cache entry
						 */
						for (int i = 0; i < msgs.length; i++) {
							final MessageCacheObject mco = (MessageCacheObject) msgs[i];
							if (mco.isExpunged()) {
								MessageCacheManager.getInstance().removeMessage(sessionObj, mco.getUid(),
										mailIdentifier.folder);
							} else {
								MessageCacheManager.getInstance().putMessage(sessionObj, mco.getUid(), mco);
							}
						}
					}
				}
			} finally {
				if (closeMailInterface && mailInterface != null) {
					mailInterface.close(true);
					mailInterface = null;
				}
			}
		} catch (OXMailException e) {
			LOG.error(e.getMessage(), e);
			if (!e.getCategory().equals(Category.USER_CONFIGURATION)) {
				response.setException(e);
			}
		} catch (AbstractOXException e) {
			LOG.error(e.getMessage(), e);
			response.setException(e);
		} catch (Exception e) {
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

	public void actionPutCopyMail(final SessionObject sessionObj, final JSONWriter writer, final JSONObject jsonObj,
			final MailInterface mailInterface) throws JSONException {
		Response.write(actionPutCopyMail(sessionObj, jsonObj.getString(JSON_KEY_DATA), ParamContainer.getInstance(
				jsonObj, Component.EMAIL), mailInterface), writer);
	}

	private final void actionPutCopyMail(final HttpServletRequest req, final HttpServletResponse resp)
			throws IOException, ServletException {
		try {
			Response.write(actionPutCopyMail(getSessionObject(req), getBody(req), ParamContainer.getInstance(req,
					Component.EMAIL, resp), null), resp.getWriter());
		} catch (JSONException e) {
			sendErrorAsJS(resp, RESPONSE_ERROR);
		}
	}

	private final Response actionPutCopyMail(final SessionObject sessionObj, final String body,
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
			final MailIdentifier mailIdentifier = new MailIdentifier(paramContainer.checkStringParam(PARAMETER_ID));
			final String sourceFolder = paramContainer.checkStringParam(PARAMETER_FOLDERID);
			final String destFolder = new JSONObject(body).getString(FolderFields.FOLDER_ID);
			MailInterface mailInterface = mailInterfaceArg;
			boolean closeMailInterface = false;
			try {
				if (mailInterface == null) {
					mailInterface = MailInterfaceImpl.getInstance(sessionObj);
					closeMailInterface = true;
				}
				final long[] msgUIDs = mailInterface.copyMessages(sourceFolder, destFolder,
						new long[] { mailIdentifier.msgUID }, false);
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
					mailInterface = null;
				}
			}
		} catch (OXMailException e) {
			LOG.error(e.getMessage(), e);
			if (!e.getCategory().equals(Category.USER_CONFIGURATION)) {
				response.setException(e);
			}
		} catch (AbstractOXException e) {
			LOG.error(e.getMessage(), e);
			response.setException(e);
		} catch (Exception e) {
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

	public final void actionPutMoveMailMultiple(final SessionObject sessionObj, final JSONWriter writer,
			final String[] mailIDs, final String sourceFolder, final String destFolder, final MailInterface mailInteface)
			throws JSONException {
		actionPutMailMultiple(sessionObj, writer, mailIDs, sourceFolder, destFolder, true, mailInteface);
	}

	public final void actionPutCopyMailMultiple(final SessionObject sessionObj, final JSONWriter writer,
			final String[] mailIDs, final String srcFolder, final String destFolder, final MailInterface mailInterface)
			throws JSONException {
		actionPutMailMultiple(sessionObj, writer, mailIDs, srcFolder, destFolder, false, mailInterface);
	}

	public final void actionPutMailMultiple(final SessionObject sessionObj, final JSONWriter writer,
			final String[] mailIDs, final String srcFolder, final String destFolder, final boolean move,
			final MailInterface mailInterfaceArg) throws JSONException {
		try {
			MailInterface mailInterface = mailInterfaceArg;
			boolean closeMailInterface = false;
			try {
				if (mailInterface == null) {
					mailInterface = MailInterfaceImpl.getInstance(sessionObj);
					closeMailInterface = true;
				}
				final long[] msgUIDs = mailInterface.copyMessages(srcFolder, destFolder, MailIdentifier
						.getUIDs(mailIDs), move);
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
					mailInterface = null;
				}
			}
		} catch (OXMailException e) {
			LOG.error(e.getMessage(), e);
			final Response response = new Response();
			if (!e.getCategory().equals(Category.USER_CONFIGURATION)) {
				response.setException(e);
			}
			response.setData(JSONObject.NULL);
			response.setTimestamp(null);
			Response.write(response, writer);
		} catch (AbstractOXException e) {
			LOG.error(e.getMessage(), e);
			final Response response = new Response();
			response.setException(e);
			response.setData(JSONObject.NULL);
			response.setTimestamp(null);
			Response.write(response, writer);
		} catch (Exception e) {
			LOG.error("actionPutMailMultiple", e);
			final Response response = new Response();
			response.setException(getWrappingOXException(e));
			response.setData(JSONObject.NULL);
			response.setTimestamp(null);
			Response.write(response, writer);
		}
	}

	public void actionPutStoreFlagsMultiple(final SessionObject sessionObj, final JSONWriter writer,
			final String[] mailIDs, final String folder, final int flagsBits, final boolean flagValue,
			final MailInterface mailInterfaceArg) throws JSONException {
		try {
			MailInterface mailInterface = mailInterfaceArg;
			boolean closeMailInterface = false;
			try {
				if (mailInterface == null) {
					mailInterface = MailInterfaceImpl.getInstance(sessionObj);
					closeMailInterface = true;
				}
				final Message[] msgs = mailInterface.updateMessageFlags(folder, MailIdentifier.getUIDs(mailIDs),
						flagsBits, flagValue);
				if (msgs != null && MessageCacheManager.getInstance().containsUserMessages(sessionObj)) {
					/*
					 * Update cache entry
					 */
					for (int i = 0; i < msgs.length; i++) {
						final MessageCacheObject mco = (MessageCacheObject) msgs[i];
						if (mco.isExpunged()) {
							MessageCacheManager.getInstance().removeMessage(sessionObj, mco.getUid(), folder);
						} else {
							MessageCacheManager.getInstance().putMessage(sessionObj, mco.getUid(), mco);
						}
					}
				}

			} finally {
				if (closeMailInterface && mailInterface != null) {
					mailInterface.close(true);
					mailInterface = null;
				}
			}
		} catch (OXMailException e) {
			LOG.error(e.getMessage(), e);
			final Response response = new Response();
			if (!e.getCategory().equals(Category.USER_CONFIGURATION)) {
				response.setException(e);
			}
			response.setData(JSONObject.NULL);
			response.setTimestamp(null);
			Response.write(response, writer);
		} catch (AbstractOXException e) {
			LOG.error(e.getMessage(), e);
			final Response response = new Response();
			response.setException(e);
			response.setData(JSONObject.NULL);
			response.setTimestamp(null);
			Response.write(response, writer);
		} catch (Exception e) {
			LOG.error("actionPutStoreFlagsMultiple", e);
			final Response response = new Response();
			response.setException(getWrappingOXException(e));
			response.setData(JSONObject.NULL);
			response.setTimestamp(null);
			Response.write(response, writer);
		}
	}

	public void actionPutColorLabelMultiple(final SessionObject sessionObj, final JSONWriter writer,
			final String[] mailIDs, final String folder, final int colorLabel, final MailInterface mailInterfaceArg)
			throws JSONException {
		try {
			MailInterface mailInterface = mailInterfaceArg;
			boolean closeMailInterface = false;
			try {
				if (mailInterface == null) {
					mailInterface = MailInterfaceImpl.getInstance(sessionObj);
					closeMailInterface = true;
				}
				final Message[] msgs = mailInterface.updateMessageColorLabel(folder, MailIdentifier.getUIDs(mailIDs),
						colorLabel);
				if (msgs != null && MessageCacheManager.getInstance().containsUserMessages(sessionObj)) {
					/*
					 * Update cache entry
					 */
					for (int i = 0; i < msgs.length; i++) {
						final MessageCacheObject mco = (MessageCacheObject) msgs[i];
						if (mco.isExpunged()) {
							MessageCacheManager.getInstance().removeMessage(sessionObj, mco.getUid(), folder);
						} else {
							MessageCacheManager.getInstance().putMessage(sessionObj, mco.getUid(), mco);
						}
					}
				}

			} finally {
				if (closeMailInterface && mailInterface != null) {
					mailInterface.close(true);
					mailInterface = null;
				}
			}
		} catch (OXMailException e) {
			LOG.error(e.getMessage(), e);
			final Response response = new Response();
			if (!e.getCategory().equals(Category.USER_CONFIGURATION)) {
				response.setException(e);
			}
			response.setData(JSONObject.NULL);
			response.setTimestamp(null);
			Response.write(response, writer);
		} catch (AbstractOXException e) {
			LOG.error(e.getMessage(), e);
			final Response response = new Response();
			response.setException(e);
			response.setData(JSONObject.NULL);
			response.setTimestamp(null);
			Response.write(response, writer);
		} catch (Exception e) {
			LOG.error("actionPutColorLabelMultiple", e);
			final Response response = new Response();
			response.setException(getWrappingOXException(e));
			response.setData(JSONObject.NULL);
			response.setTimestamp(null);
			Response.write(response, writer);
		}
	}

	public void actionPutAttachment(final SessionObject sessionObj, final JSONWriter writer, final JSONObject jsonObj,
			final MailInterface mi) throws JSONException {
		Response.write(actionPutAttachment(sessionObj, jsonObj.getString(JSON_KEY_DATA), ParamContainer.getInstance(
				jsonObj, Component.EMAIL), mi), writer);
	}

	private final void actionPutAttachment(final HttpServletRequest req, final HttpServletResponse resp)
			throws IOException, ServletException {
		try {
			Response.write(actionPutAttachment(getSessionObject(req), getBody(req), ParamContainer.getInstance(req,
					Component.EMAIL, resp), null), resp.getWriter());
		} catch (JSONException e) {
			sendErrorAsJS(resp, RESPONSE_ERROR);
		}
	}

	private final Response actionPutAttachment(final SessionObject sessionObj, final String body,
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
			final MailIdentifier mailIdentifier = new MailIdentifier(paramContainer.checkStringParam(PARAMETER_ID));
			final String attachmentIdentifier = paramContainer.checkStringParam(PARAMETER_MAILATTCHMENT);
			final String destFolderIdentifier = paramContainer.checkStringParam(PARAMETER_DESTINATION_FOLDER);
			MailInterface mailInterface = mailInterfaceArg;
			boolean closeMailInterface = false;
			JSONMessageAttachmentObject mao = null;
			final InfostoreFacade db = Infostore.FACADE;
			try {
				if (!sessionObj.getUserConfiguration().hasInfostore()) {
					throw new OXPermissionException(new OXMailException(MailCode.NO_MAIL_MODULE_ACCESS, sessionObj
							.getUserlogin()));
				}
				if (mailInterface == null) {
					mailInterface = MailInterfaceImpl.getInstance(sessionObj);
					closeMailInterface = true;
				}
				mao = mailInterface.getMessageAttachment(mailIdentifier.folder, mailIdentifier.msgUID,
						attachmentIdentifier);
				if (mao == null) {
					throw new OXMailException(MailCode.NO_ATTACHMENT_FOUND, attachmentIdentifier, mailIdentifier
							.toString());
				}
				if (mao.getContent() == null) {
					throw new Exception("No content found in attachment with id \"" + attachmentIdentifier
							+ "\" in mail " + mailIdentifier);
				}
				final int destFolderID = Integer.parseInt(destFolderIdentifier);
				final FolderObject folderObj;
				if (FolderCacheManager.isEnabled()) {
					folderObj = FolderCacheManager.getInstance().getFolderObject(destFolderID, true,
							sessionObj.getContext(), null);
				} else {
					folderObj = FolderObject.loadFolderObjectFromDB(destFolderID, sessionObj.getContext());
				}
				final EffectivePermission p = folderObj.getEffectiveUserPermission(sessionObj.getUserObject().getId(),
						sessionObj.getUserConfiguration());
				if (!p.isFolderVisible()) {
					throw new OXFolderException(FolderCode.NOT_VISIBLE, getFolderName(folderObj),
							getUserName(sessionObj), Integer.valueOf(sessionObj.getContext().getContextId()));
				}
				if (!p.canWriteOwnObjects()) {
					throw new OXFolderException(FolderCode.NO_WRITE_PERMISSION, getUserName(sessionObj),
							getFolderName(folderObj), Integer.valueOf(sessionObj.getContext().getContextId()));
				}
				/*
				 * Create document's meta data
				 */
				final InfostoreParser parser = new InfostoreParser();
				final DocumentMetadata docMetaData = parser.getDocumentMetadata(body);
				final Set<Metadata> metSet = new HashSet<Metadata>(Arrays.asList(parser.findPresentFields(body)));
				if (!metSet.contains(Metadata.FILENAME_LITERAL)) {
					docMetaData.setFileName(mao.getFileName());
				}
				docMetaData.setFileMIMEType(mao.getContentType());
				docMetaData.setFileSize(mao.getSize());
				if (!metSet.contains(Metadata.TITLE_LITERAL)) {
					docMetaData.setTitle(mao.getFileName());
				}
				docMetaData.setFolderId(destFolderID);
				/*
				 * Start writing to infostore folder
				 */
				db.startTransaction();
				final InputStream in;
				if (mao.getContentID() == JSONMessageAttachmentObject.CONTENT_INPUT_STREAM) {
					in = (InputStream) mao.getContent();
				} else if (mao.getContentID() == JSONMessageAttachmentObject.CONTENT_BYTE_ARRAY) {
					final byte[] bytes = (byte[]) mao.getContent();
					in = new ByteArrayInputStream(bytes);
				} else if (mao.getContentID() == JSONMessageAttachmentObject.CONTENT_STRING) {
					final String contentStr = (String) mao.getContent();
					in = new ByteArrayInputStream(contentStr.getBytes(STR_UTF8));
				} else {
					in = new ByteArrayInputStream(STR_EMPTY.getBytes(STR_UTF8));
				}
				db.saveDocument(docMetaData, in, System.currentTimeMillis(), sessionObj);
				db.commit();
			} catch (Exception e) {
				db.rollback();
				throw e;
			} finally {
				if (closeMailInterface && mailInterface != null) {
					mailInterface.close(true);
					mailInterface = null;
				}
				if (db != null) {
					db.finish();
				}
			}
		} catch (OXMailException e) {
			LOG.error(e.getMessage(), e);
			if (!e.getCategory().equals(Category.USER_CONFIGURATION)) {
				response.setException(e);
			}
		} catch (AbstractOXException e) {
			LOG.error(e.getMessage(), e);
			response.setException(e);
		} catch (Exception e) {
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

	public void actionPutReceiptAck(final SessionObject sessionObj, final JSONWriter writer, final JSONObject jsonObj,
			final MailInterface mi) throws JSONException {
		Response.write(actionPutReceiptAck(sessionObj, jsonObj.getString(JSON_KEY_DATA), ParamContainer.getInstance(
				jsonObj, Component.EMAIL), mi), writer);
	}

	private final void actionPutReceiptAck(final HttpServletRequest req, final HttpServletResponse resp)
			throws IOException, ServletException {
		try {
			Response.write(actionPutReceiptAck(getSessionObject(req), getBody(req), ParamContainer.getInstance(req,
					Component.EMAIL, resp), null), resp.getWriter());
		} catch (JSONException e) {
			sendErrorAsJS(resp, RESPONSE_ERROR);
		}
	}

	private final Response actionPutReceiptAck(final SessionObject sessionObj, final String body,
			final ParamContainer paramContainer, final MailInterface mailInterfaceArg) {
		/*
		 * Some variables
		 */
		final Response response = new Response();
		/*
		 * Start response
		 */
		try {
			final MailIdentifier mailIdentifier = new MailIdentifier(paramContainer.checkStringParam(PARAMETER_ID));
			final JSONObject bodyObj = new JSONObject(body);
			final String fromAddr = bodyObj.has(JSONMessageObject.JSON_FROM)
					&& !bodyObj.isNull(JSONMessageObject.JSON_FROM) ? bodyObj.getString(JSONMessageObject.JSON_FROM)
					: null;
			MailInterface mailInterface = mailInterfaceArg;
			boolean closeMailInterface = false;
			try {
				if (mailInterface == null) {
					mailInterface = MailInterfaceImpl.getInstance(sessionObj);
					closeMailInterface = true;
				}
				mailInterface.sendReceiptAck(mailIdentifier.folder, mailIdentifier.msgUID, fromAddr);
			} finally {
				if (closeMailInterface && mailInterface != null) {
					mailInterface.close(true);
					mailInterface = null;
				}
			}
		} catch (OXMailException e) {
			LOG.error(e.getMessage(), e);
			if (!e.getCategory().equals(Category.USER_CONFIGURATION)) {
				response.setException(e);
			}
		} catch (AbstractOXException e) {
			LOG.error(e.getMessage(), e);
			response.setException(e);
		} catch (Exception e) {
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
			throw new OXMandatoryFieldException(Component.EMAIL, MailCode.MISSING_PARAM.getCategory(),
					MailCode.MISSING_PARAM.getNumber(), null, paramName);
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
		SessionObject sessionObj = null;
		/*
		 * The magic spell to disable caching
		 */
		Tools.disableCaching(resp);
		try {
			MailInterface mailInterface = null;
			try {
				sessionObj = getSessionObject(req);
				mailInterface = MailInterfaceImpl.getInstance(sessionObj);
				if (req.getContentType().toLowerCase().startsWith(MIME_MULTIPART)) {
					/*
					 * Set response headers according to html spec
					 */
					resp.setContentType(MIME_TEXT_HTML_CHARSET_UTF_8);
					final String actionStr = req.getParameter(PARAMETER_ACTION);
					/*
					 * Append UploadListener instances
					 */
					((UploadListener) this).getRegistry().addUploadListener(
							new UploadQuotaChecker(sessionObj.getUserConfiguration().getUserSettingMail(), resp,
									actionStr));
					((UploadListener) this).getRegistry().addUploadListener(new Mail());
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
						mailInterface = null;
					} catch (Exception e) {
						LOG.error(e.getMessage(), e);
					}
				}
			}
		} catch (UploadException e) {
			LOG.error(e.getMessage(), e);
			JSONObject responseObj = null;
			try {
				final Response response = new Response();
				response.setException(e);
				responseObj = response.getJSON();
			} catch (JSONException e1) {
				LOG.error(e1.getMessage(), e1);
			}
			throw new UploadServletException(resp, JS_FRAGMENT.replaceFirst(JS_FRAGMENT_JSON,
					responseObj == null ? STR_NULL : Matcher.quoteReplacement(responseObj.toString())).replaceFirst(
					JS_FRAGMENT_ACTION, e.getAction() == null ? STR_NULL : e.getAction()), e.getMessage(), e);
		} catch (OXException e) {
			LOG.error(e.getMessage(), e);
			sendErrorAsJS(resp, e.getMessage());
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
			final MailInterface mailInterface = (MailInterface) uploadEvent.getParameter(UPLOAD_PARAM_MAILINTERFACE);
			final PrintWriter writer = (PrintWriter) uploadEvent.getParameter(UPLOAD_PARAM_WRITER);
			final SessionObject sessionObj = (SessionObject) uploadEvent.getParameter(UPLOAD_PARAM_SESSION);
			final String actionStr = (String) uploadEvent.getParameter(PARAMETER_ACTION);
			String mailStr = uploadEvent.getFormField(UPLOAD_FORMFIELD_MAIL);
			if (mailStr == null) {
				throw new OXMailException(MailCode.MISSING_PARAM, UPLOAD_FORMFIELD_MAIL);
			}
			final JSONObject jsonMailObj = new JSONObject(mailStr);
			mailStr = null;
			final int sendType = jsonMailObj.has(PARAMETER_SEND_TYPE) && !jsonMailObj.isNull(PARAMETER_SEND_TYPE) ? jsonMailObj
					.getInt(PARAMETER_SEND_TYPE)
					: MailInterfaceImpl.SENDTYPE_NEW;
			int[] infostoreDocIDs = null;
			if (jsonMailObj.has(JSONMessageObject.JSON_INFOSTORE_IDS)) {
				final JSONArray ja = jsonMailObj.getJSONArray(JSONMessageObject.JSON_INFOSTORE_IDS);
				infostoreDocIDs = new int[ja.length()];
				final int length = ja.length();
				for (int i = 0; i < length; i++) {
					infostoreDocIDs[i] = ja.getInt(i);
				}
			}
			try {
				final JSONMessageObject msgObj = new JSONMessageObject(sessionObj.getUserConfiguration()
						.getUserSettingMail(), TimeZone.getTimeZone(sessionObj.getUserObject().getTimeZone()))
						.parseJSONObject(jsonMailObj);
				if (uploadEvent.getAction().equals(ACTION_NEW)) {
					/*
					 * Add file & infostore attachments to message
					 */
					addUploadFilesAsAttachments(msgObj, uploadEvent, 0);
					addInfostoreDocumentsAsAttachments(msgObj, infostoreDocIDs, 0, sessionObj);
					String msgIdentifier = null;
					/*
					 * Send message
					 */
					msgIdentifier = mailInterface.sendMessage(msgObj, uploadEvent, sendType);
					if (msgIdentifier == null) {
						throw new OXMailException(MailCode.SEND_FAILED_UNKNOWN);
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
			} catch (final OXException e) {
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
			throw new OXMailException(MailCode.JSON_ERROR, e, e.getMessage());
		}
	}

	private static final String UPLOAD_FILE_ATTACHMENT_PREFIX = "file_";

	private static final void addUploadFilesAsAttachments(final JSONMessageObject msgObj,
			final UploadEvent uploadEvent, final int level) {
		int nextAttachmentNum = msgObj.getMsgAttachments() == null || msgObj.getMsgAttachments().isEmpty() ? 0 : msgObj
				.getMsgAttachments().size();
		final int numOfUploadFiles = uploadEvent.getNumberOfUploadFiles();
		int attachmentCounter = 0;
		int addedAttachments = 0;
		final JSONMessageAttachmentObject mao = new JSONMessageAttachmentObject();
		/*
		 * Try 5 times
		 */
		final int[] indices = new int[2];
		indices[0] = level;
		while (addedAttachments < numOfUploadFiles) {
			final UploadFile uf = uploadEvent.getUploadFileByFieldName(getFieldName(attachmentCounter++));
			if (uf != null) {
				indices[1] = ++nextAttachmentNum;
				mao.setPositionInMail(MessageUtils.getIdentifier(indices));
				mao.setSize(uf.getSize());
				mao.setContent(null);
				mao.setContentID(JSONMessageAttachmentObject.CONTENT_NONE);
				mao.setContentType(uf.getContentType());
				try {
					mao.setFileName(MimeUtility.encodeText(uf.getFileName(), IMAPProperties.getDefaultMimeCharset(),
							ENC_Q));
				} catch (UnsupportedEncodingException e) {
					mao.setFileName(uf.getFileName());
				} catch (IMAPException e) {
					mao.setFileName(uf.getFileName());
				}
				mao.setUniqueDiskFileName(uf.getTmpFile());
				msgObj.addMessageAttachment((JSONMessageAttachmentObject) mao.clone());
				mao.reset();
				addedAttachments++;
			}
		}
	}

	private static final String getFieldName(final int num) {
		return new StringBuilder(10).append(UPLOAD_FILE_ATTACHMENT_PREFIX).append(num).toString();
	}

	private static final void addInfostoreDocumentsAsAttachments(final JSONMessageObject msgObj,
			final int[] documentIDs, final int level, final SessionObject session) {
		if (documentIDs == null || documentIDs.length == 0) {
			return;
		}
		int nextAttachmentNum = msgObj.getMsgAttachments() == null || msgObj.getMsgAttachments().isEmpty() ? 0 : msgObj
				.getMsgAttachments().size();
		final InfostoreFacade db = Infostore.FACADE;
		final JSONMessageAttachmentObject mao = new JSONMessageAttachmentObject();
		final int[] indices = new int[2];
		indices[0] = level;
		NextDoc: for (int i = 0; i < documentIDs.length; i++) {
			try {
				indices[1] = ++nextAttachmentNum;
				mao.setPositionInMail(MessageUtils.getIdentifier(indices));
				final int docID = documentIDs[i];
				final DocumentMetadata docMeta = db.getDocumentMetadata(docID, InfostoreFacade.CURRENT_VERSION, session
						.getContext(), session.getUserObject(), session.getUserConfiguration());
				final InputStream docInputSream = db.getDocument(docID, InfostoreFacade.CURRENT_VERSION, session
						.getContext(), session.getUserObject(), session.getUserConfiguration());
				mao.setSize(docMeta.getFileSize());
				mao.setContent(null);
				mao.setContentID(JSONMessageAttachmentObject.CONTENT_NONE);
				final String docMIMEType = docMeta.getFileMIMEType();
				mao.setContentType(docMIMEType == null || docMIMEType.length() == 0 ? MIME_APPLICATION_OCTET_STREAM
						: docMeta.getFileMIMEType());
				try {
					mao.setFileName(MimeUtility.encodeText(docMeta.getFileName(), IMAPProperties
							.getDefaultMimeCharset(), ENC_Q));
				} catch (UnsupportedEncodingException e) {
					mao.setFileName(docMeta.getFileName());
				} catch (IMAPException e) {
					mao.setFileName(docMeta.getFileName());
				}
				mao.setUniqueDiskFileName(null);
				mao.setInfostoreDocumentInputStream(docInputSream);
				msgObj.addMessageAttachment((JSONMessageAttachmentObject) mao.clone());
				mao.reset();
			} catch (Exception e) {
				LOG.error(e, e);
				continue NextDoc;
			}
		}
	}

	public UploadRegistry getRegistry() {
		return this;
	}

	@Override
	protected boolean hasModulePermission(final SessionObject sessionObj) {
		return sessionObj.getUserConfiguration().hasWebMail();
	}

	public static class MailIdentifier implements Cloneable {

		private static final Pattern DELIM_PATTERN = Pattern.compile(new StringBuilder(15).append("(.+)(").append(
				Mail.SEPERATOR).append(")([0-9]+)").toString());

		public static final MailIdentifier[] getMailIdentifiers(final String[] mailIDs) throws OXException {
			final MailIdentifier[] retval = new MailIdentifier[mailIDs.length];
			for (int i = 0; i < mailIDs.length; i++) {
				retval[i] = new MailIdentifier(mailIDs[i]);
			}
			return retval;
		}

		public static final long[] getUIDs(final String[] mailIDs) throws OXException {
			final long[] retval = new long[mailIDs.length];
			for (int i = 0; i < mailIDs.length; i++) {
				retval[i] = new MailIdentifier(mailIDs[i]).msgUID;
			}
			return retval;
		}

		private String folder;

		private long msgUID;

		private String str;

		public MailIdentifier() {
			super();
		}

		public MailIdentifier(final String mailIdentifier) throws OXException {
			final Matcher m = DELIM_PATTERN.matcher(mailIdentifier);
			if (!m.matches()) {
				throw new OXMailException(MailCode.INVALID_MAIL_IDENTIFIER, mailIdentifier);
			}
			msgUID = Long.parseLong(m.group(3));
			folder = m.group(1);
			str = mailIdentifier;
		}

		public MailIdentifier(String folder, long msgUID) {
			this.folder = folder;
			this.msgUID = msgUID;
			str = new StringBuilder().append(folder).append(SEPERATOR).append(msgUID).toString();
		}

		public MailIdentifier setMailIdentifierString(final String mailIdentifier) throws OXException {
			final Matcher m = DELIM_PATTERN.matcher(mailIdentifier);
			if (!m.matches()) {
				throw new OXMailException(MailCode.INVALID_MAIL_IDENTIFIER, mailIdentifier);
			}
			msgUID = Long.parseLong(m.group(3));
			folder = m.group(1);
			str = mailIdentifier;
			return this;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#clone()
		 */
		@Override
		public Object clone() {
			try {
				return super.clone();
			} catch (CloneNotSupportedException e) {
				/*
				 * Cannot occur since Cloneable is implemented
				 */
				return null;
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return str;
		}

		public String getFolder() {
			return folder;
		}

		public long getMsgUID() {
			return msgUID;
		}

		public String getStr() {
			return str;
		}

		public static final Comparator<MailIdentifier> getMailIdentifierComparator() {
			return new Comparator<MailIdentifier>() {
				public int compare(MailIdentifier mi1, MailIdentifier mi2) {
					final int res = mi1.folder.compareTo(mi2.folder);
					if (res != 0) {
						return res;
					}
					return Long.valueOf(mi1.msgUID).compareTo(Long.valueOf(mi2.msgUID));
				}
			};
		}

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
			} catch (CloneNotSupportedException e) {
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
				this.uploadQuota = ServerConfig.getInteger(Property.MAX_UPLOAD_SIZE);
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
					final OXMailException oxme = new OXMailException(MailCode.UPLOAD_QUOTA_EXCEEDED_FOR_FILE, Long
							.valueOf(uploadQuotaPerFile), uploadFile.getFileName(), Long.valueOf(uploadFile.getSize()));
					JSONObject responseObj = null;
					try {
						final Response response = new Response();
						response.setException(oxme);
						responseObj = response.getJSON();
					} catch (JSONException e) {
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
					final OXMailException oxme = new OXMailException(MailCode.UPLOAD_QUOTA_EXCEEDED, Long
							.valueOf(uploadQuota));
					JSONObject responseObj = null;
					try {
						final Response response = new Response();
						response.setException(oxme);
						responseObj = response.getJSON();
					} catch (JSONException e) {
						LOG.error(e.getMessage(), e);
					}
					throw new UploadServletException(resp, JS_FRAGMENT.replaceFirst(JS_FRAGMENT_JSON,
							responseObj == null ? STR_NULL : Matcher.quoteReplacement(responseObj.toString()))
							.replaceFirst(JS_FRAGMENT_ACTION, actionStr), oxme.getMessage(), oxme);
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
