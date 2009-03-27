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

import static com.openexchange.tools.oxfolder.OXFolderUtility.getFolderName;
import static com.openexchange.tools.oxfolder.OXFolderUtility.getUserName;
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
import java.util.Collection;
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
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONWriter;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.fields.CommonFields;
import com.openexchange.ajax.fields.DataFields;
import com.openexchange.ajax.fields.FolderChildFields;
import com.openexchange.ajax.fields.FolderFields;
import com.openexchange.ajax.fields.ResponseFields;
import com.openexchange.ajax.helper.ParamContainer;
import com.openexchange.ajax.parser.InfostoreParser;
import com.openexchange.ajax.writer.ResponseWriter;
import com.openexchange.api.OXMandatoryFieldException;
import com.openexchange.api.OXPermissionException;
import com.openexchange.api2.OXException;
import com.openexchange.cache.OXCachingException;
import com.openexchange.contactcollector.ContactCollectorService;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.EnumComponent;
import com.openexchange.groupware.AbstractOXException.Category;
import com.openexchange.groupware.container.CommonObject;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.infostore.InfostoreFacade;
import com.openexchange.groupware.infostore.utils.Metadata;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.upload.impl.UploadEvent;
import com.openexchange.groupware.upload.impl.UploadException;
import com.openexchange.groupware.upload.impl.UploadListener;
import com.openexchange.groupware.upload.impl.UploadRegistry;
import com.openexchange.json.OXJSONWriter;
import com.openexchange.mail.MailException;
import com.openexchange.mail.MailJSONField;
import com.openexchange.mail.MailListField;
import com.openexchange.mail.MailPath;
import com.openexchange.mail.MailServletInterface;
import com.openexchange.mail.OrderDirection;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.api.MailConfig;
import com.openexchange.mail.cache.MailMessageCache;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.dataobjects.compose.ComposeType;
import com.openexchange.mail.dataobjects.compose.ComposedMailMessage;
import com.openexchange.mail.json.parser.MessageParser;
import com.openexchange.mail.json.writer.MessageWriter;
import com.openexchange.mail.json.writer.MessageWriter.MailFieldWriter;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.mail.mime.MIMEMailException;
import com.openexchange.mail.mime.MIMEType2ExtMap;
import com.openexchange.mail.mime.MIMETypes;
import com.openexchange.mail.mime.converters.MIMEMessageConverter;
import com.openexchange.mail.text.HTMLProcessing;
import com.openexchange.mail.text.parser.HTMLParser;
import com.openexchange.mail.text.parser.handler.HTMLFilterHandler;
import com.openexchange.mail.transport.MailTransport;
import com.openexchange.mail.usersetting.UserSettingMail;
import com.openexchange.mail.utils.DisplayMode;
import com.openexchange.mail.utils.MailFolderUtility;
import com.openexchange.mail.utils.MessageUtility;
import com.openexchange.server.impl.EffectivePermission;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.tools.encoding.Helper;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorException;
import com.openexchange.tools.oxfolder.OXFolderAccess;
import com.openexchange.tools.oxfolder.OXFolderException;
import com.openexchange.tools.oxfolder.OXFolderException.FolderCode;
import com.openexchange.tools.servlet.OXJSONException;
import com.openexchange.tools.servlet.UploadServletException;
import com.openexchange.tools.servlet.http.Tools;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;
import com.openexchange.tools.stream.UnsynchronizedByteArrayInputStream;
import com.openexchange.tools.stream.UnsynchronizedByteArrayOutputStream;
import com.openexchange.tools.versit.utility.VersitUtility;

/**
 * {@link Mail} - The servlet to handle mail requests
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class Mail extends PermissionServlet implements UploadListener {

    private static final transient org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(Mail.class);

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

    private static final AbstractOXException getWrappingOXException(final Exception cause) {
        if (LOG.isWarnEnabled()) {
            final StringBuilder warnBuilder = new StringBuilder(140);
            warnBuilder.append("An unexpected exception occurred, which is going to be wrapped for proper display.\n");
            warnBuilder.append("For safety reason its original content is display here.");
            LOG.warn(warnBuilder.toString(), cause);
        }
        final String message = cause.getMessage();
        return new AbstractOXException(
            EnumComponent.MAIL,
            Category.INTERNAL_ERROR,
            9999,
            null == message ? "[Not available]" : message,
            cause);
    }

    private static final String UPLOAD_PARAM_MAILINTERFACE = "mi";

    private static final String UPLOAD_PARAM_WRITER = "w";

    private static final String UPLOAD_PARAM_SESSION = "s";

    private static final String STR_CHARSET = "charset";

    private static final String STR_UTF8 = "UTF-8";

    private static final String STR_1 = "1";

    private static final String STR_EMPTY = "";

    private static final String STR_NULL = "null";

    /**
     * The parameter 'folder' contains the folder's id whose contents are queried.
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

    public static final String PARAMETER_VIEW = "view";

    public static final String PARAMETER_SRC = "src";

    public static final String PARAMETER_FLAGS = "flags";

    public static final String PARAMETER_UNSEEN = "unseen";

    public static final String PARAMETER_FILTER = "filter";

    private static final String VIEW_TEXT = "text";

    private static final String VIEW_HTML = "html";

    private static final String VIEW_HTML_BLOCKED_IMAGES = "noimg";

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
        } else if (actionStr.equalsIgnoreCase(ACTION_AUTOSAVE)) {
            actionPutAutosave(req, resp);
        } else if (actionStr.equalsIgnoreCase(ACTION_FORWARD)) {
            actionPutForwardMultiple(req, resp);
        } else if (actionStr.equalsIgnoreCase(ACTION_REPLY) || actionStr.equalsIgnoreCase(ACTION_REPLYALL)) {
            actionPutReply(req, resp, (actionStr.equalsIgnoreCase(ACTION_REPLYALL)));
        } else if (actionStr.equalsIgnoreCase(ACTION_GET)) {
            actionPutGet(req, resp);
        } else if (actionStr.equalsIgnoreCase(ACTION_NEW)) {
            actionPutNewMail(req, resp);
        } else {
            throw new Exception("Unknown value in parameter " + PARAMETER_ACTION + " through PUT command");
        }
    }

    public void actionGetUpdates(final Session session, final JSONWriter writer, final JSONObject requestObj, final MailServletInterface mi) throws JSONException {
        ResponseWriter.write(actionGetUpdates(session, ParamContainer.getInstance(requestObj, EnumComponent.MAIL), mi), writer);
    }

    private final void actionGetUpdates(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
        try {
            ResponseWriter.write(
                actionGetUpdates(getSessionObject(req), ParamContainer.getInstance(req, EnumComponent.MAIL, resp), null),
                resp.getWriter());
        } catch (final JSONException e) {
            final OXJSONException oxe = new OXJSONException(OXJSONException.Code.JSON_WRITE_ERROR, e, new Object[0]);
            LOG.error(oxe.getMessage(), oxe);
            final Response response = new Response();
            response.setException(oxe);
            try {
                ResponseWriter.write(response, resp.getWriter());
            } catch (final JSONException e1) {
                LOG.error(RESPONSE_ERROR, e1);
                sendError(resp);
            }
        }
    }

    private final transient static JSONArray EMPTY_JSON_ARR = new JSONArray();

    private final Response actionGetUpdates(final Session session, final ParamContainer paramContainer, final MailServletInterface mailInterfaceArg) throws JSONException {
        /*
         * Send an empty array cause ACTION=UPDATES is not supported for messages
         */
        final Response response = new Response();
        /*
         * Close response and flush print writer
         */
        response.setData(EMPTY_JSON_ARR);
        response.setTimestamp(null);
        return response;
    }

    public void actionGetMailCount(final Session session, final JSONWriter writer, final JSONObject requestObj, final MailServletInterface mi) throws JSONException {
        ResponseWriter.write(actionGetMailCount(session, ParamContainer.getInstance(requestObj, EnumComponent.MAIL), mi), writer);
    }

    private final void actionGetMailCount(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
        try {
            ResponseWriter.write(
                actionGetMailCount(getSessionObject(req), ParamContainer.getInstance(req, EnumComponent.MAIL, resp), null),
                resp.getWriter());
        } catch (final JSONException e) {
            final OXJSONException oxe = new OXJSONException(OXJSONException.Code.JSON_WRITE_ERROR, e, new Object[0]);
            LOG.error(oxe.getMessage(), oxe);
            final Response response = new Response();
            response.setException(oxe);
            try {
                ResponseWriter.write(response, resp.getWriter());
            } catch (final JSONException e1) {
                LOG.error(RESPONSE_ERROR, e1);
                sendError(resp);
            }
        }
    }

    private final Response actionGetMailCount(final Session session, final ParamContainer paramContainer, final MailServletInterface mailInterfaceArg) {
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
            MailServletInterface mailInterface = mailInterfaceArg;
            boolean closeMailInterface = false;
            try {
                if (mailInterface == null) {
                    mailInterface = MailServletInterface.getInstance(session);
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
            final AbstractOXException wrapper = getWrappingOXException(e);
            LOG.error(wrapper.getMessage(), wrapper);
            response.setException(wrapper);
        }
        /*
         * Close response and flush print writer
         */
        response.setData(data);
        response.setTimestamp(null);
        return response;
    }

    public void actionGetAllMails(final ServerSession session, final JSONWriter writer, final JSONObject requestObj, final MailServletInterface mi) throws SearchIteratorException, JSONException {
        ResponseWriter.write(actionGetAllMails(session, ParamContainer.getInstance(requestObj, EnumComponent.MAIL), mi), writer);
    }

    private final void actionGetAllMails(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
        try {
            ResponseWriter.write(
                actionGetAllMails(getSessionObject(req), ParamContainer.getInstance(req, EnumComponent.MAIL, resp), null),
                resp.getWriter());
        } catch (final JSONException e) {
            final OXJSONException oxe = new OXJSONException(OXJSONException.Code.JSON_WRITE_ERROR, e, new Object[0]);
            LOG.error(oxe.getMessage(), oxe);
            final Response response = new Response();
            response.setException(oxe);
            try {
                ResponseWriter.write(response, resp.getWriter());
            } catch (final JSONException e1) {
                LOG.error(RESPONSE_ERROR, e1);
                sendError(resp);
            }
        } catch (final SearchIteratorException e) {
            LOG.error(e.getMessage(), e);
            final Response response = new Response();
            response.setException(e);
            try {
                ResponseWriter.write(response, resp.getWriter());
            } catch (final JSONException e1) {
                LOG.error(RESPONSE_ERROR, e1);
                sendError(resp);
            }
        }
    }

    private static final String STR_ASC = "asc";

    private static final String STR_DESC = "desc";

    private final Response actionGetAllMails(final ServerSession session, final ParamContainer paramContainer, final MailServletInterface mailInterfaceArg) throws JSONException, SearchIteratorException {
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

            final int[] fromToIndices;
            {
                final int leftHandLimit = paramContainer.getIntParam(LEFT_HAND_LIMIT);
                final int rigthHandLimit = paramContainer.getIntParam(RIGHT_HAND_LIMIT);
                if (leftHandLimit == ParamContainer.NOT_FOUND || rigthHandLimit == ParamContainer.NOT_FOUND) {
                    fromToIndices = null;
                } else {
                    fromToIndices = new int[] { leftHandLimit, rigthHandLimit };
                }
            }

            /*
             * Get all mails
             */
            MailServletInterface mailInterface = mailInterfaceArg;
            boolean closeMailInterface = false;
            try {
                if (mailInterface == null) {
                    mailInterface = MailServletInterface.getInstance(session);
                    closeMailInterface = true;
                }
                /*
                 * Pre-Select field writers
                 */
                final MailFieldWriter[] writers = MessageWriter.getMailFieldWriter(MailListField.getFields(columns));
                /*
                 * Receive message iterator
                 */
                if (threadSort) {
                    it = mailInterface.getAllThreadedMessages(folderId, columns, fromToIndices);
                    final int size = it.size();
                    for (int i = 0; i < size; i++) {
                        final MailMessage mail = (MailMessage) it.next();
                        final JSONArray ja = new JSONArray();
                        if (mail == null) {
                            for (int j = 0; j < writers.length; j++) {
                                ja.put(JSONObject.NULL);
                            }
                        } else {
                            for (final MailFieldWriter writer : writers) {
                                writer.writeField(ja, mail, mail.getThreadLevel(), false, session.getUserId(), session.getContextId());
                            }
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
                    it = mailInterface.getAllMessages(folderId, sortCol, orderDir, columns, fromToIndices);
                    final int size = it.size();
                    for (int i = 0; i < size; i++) {
                        final MailMessage mail = (MailMessage) it.next();
                        final JSONArray ja = new JSONArray();
                        if (mail == null) {
                            for (int j = 0; j < writers.length; j++) {
                                ja.put(JSONObject.NULL);
                            }
                        } else {
                            for (final MailFieldWriter writer : writers) {
                                writer.writeField(ja, mail, 0, false, session.getUserId(), session.getContextId());
                            }
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
            final AbstractOXException wrapper = getWrappingOXException(e);
            LOG.error(wrapper.getMessage(), wrapper);
            response.setException(wrapper);
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

    public void actionGetReply(final ServerSession session, final JSONWriter writer, final JSONObject jo, final boolean reply2all, final MailServletInterface mailInterface) throws JSONException {
        ResponseWriter.write(actionGetReply(session, reply2all, ParamContainer.getInstance(jo, EnumComponent.MAIL), mailInterface), writer);
    }

    private final void actionGetReply(final HttpServletRequest req, final HttpServletResponse resp, final boolean reply2all) throws IOException {
        try {
            ResponseWriter.write(actionGetReply(
                getSessionObject(req),
                reply2all,
                ParamContainer.getInstance(req, EnumComponent.MAIL, resp),
                null), resp.getWriter());
        } catch (final JSONException e) {
            final OXJSONException oxe = new OXJSONException(OXJSONException.Code.JSON_WRITE_ERROR, e, new Object[0]);
            LOG.error(oxe.getMessage(), oxe);
            final Response response = new Response();
            response.setException(oxe);
            try {
                ResponseWriter.write(response, resp.getWriter());
            } catch (final JSONException e1) {
                LOG.error(RESPONSE_ERROR, e1);
                sendError(resp);
            }
        }
    }

    private final Response actionGetReply(final ServerSession session, final boolean reply2all, final ParamContainer paramContainer, final MailServletInterface mailInterfaceArg) {
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
            final String folderPath = paramContainer.checkStringParam(PARAMETER_FOLDERID);
            final long uid = Long.parseLong(paramContainer.checkStringParam(PARAMETER_ID));
            final String view = paramContainer.getStringParam(PARAMETER_VIEW);
            final UserSettingMail usmNoSave = (UserSettingMail) session.getUserSettingMail().clone();
            /*
             * Deny saving for this request-specific settings
             */
            usmNoSave.setNoSave(true);
            /*
             * Overwrite settings with request's parameters
             */
            if (null != view) {
                if (VIEW_TEXT.equals(view)) {
                    usmNoSave.setDisplayHtmlInlineContent(false);
                } else if (VIEW_HTML.equals(view)) {
                    usmNoSave.setDisplayHtmlInlineContent(true);
                    usmNoSave.setAllowHTMLImages(true);
                } else {
                    LOG.warn(new StringBuilder(64).append("Unknown value in parameter ").append(PARAMETER_VIEW).append(": ").append(view).append(
                        ". Using user's mail settings as fallback."));
                }
            }
            /*
             * Get reply message
             */
            MailServletInterface mailInterface = mailInterfaceArg;
            boolean closeMailInterface = false;
            try {
                if (mailInterfaceArg == null) {
                    mailInterface = MailServletInterface.getInstance(session);
                    closeMailInterface = true;
                }
                data = MessageWriter.writeMailMessage(
                    mailInterface.getReplyMessageForDisplay(folderPath, uid, reply2all, usmNoSave),
                    DisplayMode.MODIFYABLE,
                    session,
                    usmNoSave);
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
            final AbstractOXException wrapper = getWrappingOXException(e);
            LOG.error(wrapper.getMessage(), wrapper);
            response.setException(wrapper);
        }
        /*
         * Close response and flush print writer
         */
        response.setData(data);
        response.setTimestamp(null);
        return response;
    }

    public void actionGetForward(final ServerSession session, final JSONWriter writer, final JSONObject requestObj, final MailServletInterface mailInterface) throws JSONException {
        ResponseWriter.write(actionGetForward(session, ParamContainer.getInstance(requestObj, EnumComponent.MAIL), mailInterface), writer);
    }

    private final void actionGetForward(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
        try {
            ResponseWriter.write(
                actionGetForward(getSessionObject(req), ParamContainer.getInstance(req, EnumComponent.MAIL, resp), null),
                resp.getWriter());
        } catch (final JSONException e) {
            final OXJSONException oxe = new OXJSONException(OXJSONException.Code.JSON_WRITE_ERROR, e, new Object[0]);
            LOG.error(oxe.getMessage(), oxe);
            final Response response = new Response();
            response.setException(oxe);
            try {
                ResponseWriter.write(response, resp.getWriter());
            } catch (final JSONException e1) {
                LOG.error(RESPONSE_ERROR, e1);
                sendError(resp);
            }
        }
    }

    private final Response actionGetForward(final ServerSession session, final ParamContainer paramContainer, final MailServletInterface mailInterfaceArg) {
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
            final String folderPath = paramContainer.checkStringParam(PARAMETER_FOLDERID);
            final long uid = Long.parseLong(paramContainer.checkStringParam(PARAMETER_ID));
            final String view = paramContainer.getStringParam(PARAMETER_VIEW);
            final UserSettingMail usmNoSave = (UserSettingMail) session.getUserSettingMail().clone();
            /*
             * Deny saving for this request-specific settings
             */
            usmNoSave.setNoSave(true);
            /*
             * Overwrite settings with request's parameters
             */
            if (null != view) {
                if (VIEW_TEXT.equals(view)) {
                    usmNoSave.setDisplayHtmlInlineContent(false);
                } else if (VIEW_HTML.equals(view)) {
                    usmNoSave.setDisplayHtmlInlineContent(true);
                    usmNoSave.setAllowHTMLImages(true);
                } else {
                    LOG.warn(new StringBuilder(64).append("Unknown value in parameter ").append(PARAMETER_VIEW).append(": ").append(view).append(
                        ". Using user's mail settings as fallback."));
                }
            }
            /*
             * Get forward message
             */
            MailServletInterface mailInterface = mailInterfaceArg;
            boolean closeMailInterface = false;
            try {
                if (mailInterface == null) {
                    mailInterface = MailServletInterface.getInstance(session);
                    closeMailInterface = true;
                }
                data = MessageWriter.writeMailMessage(mailInterface.getForwardMessageForDisplay(
                    new String[] { folderPath },
                    new long[] { uid },
                    usmNoSave), DisplayMode.MODIFYABLE, session, usmNoSave);
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
            final AbstractOXException wrapper = getWrappingOXException(e);
            LOG.error(wrapper.getMessage(), wrapper);
            response.setException(wrapper);
        }
        /*
         * Close response and flush print writer
         */
        response.setData(data);
        response.setTimestamp(null);
        return response;
    }

    public void actionGetMessage(final ServerSession session, final JSONWriter writer, final JSONObject requestObj, final MailServletInterface mi) throws JSONException {
        ResponseWriter.write(actionGetMessage(session, ParamContainer.getInstance(requestObj, EnumComponent.MAIL), mi), writer);
    }

    private final void actionGetMessage(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
        try {
            ResponseWriter.write(
                actionGetMessage(getSessionObject(req), ParamContainer.getInstance(req, EnumComponent.MAIL, resp), null),
                resp.getWriter());
        } catch (final JSONException e) {
            final OXJSONException oxe = new OXJSONException(OXJSONException.Code.JSON_WRITE_ERROR, e, new Object[0]);
            LOG.error(oxe.getMessage(), oxe);
            final Response response = new Response();
            response.setException(oxe);
            try {
                ResponseWriter.write(response, resp.getWriter());
            } catch (final JSONException e1) {
                LOG.error(RESPONSE_ERROR, e1);
                sendError(resp);
            }
        }
    }

    private final Response actionGetMessage(final ServerSession session, final ParamContainer paramContainer, final MailServletInterface mailInterfaceArg) {
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
            final String folderPath = paramContainer.checkStringParam(PARAMETER_FOLDERID);
            final long uid = Long.parseLong(paramContainer.checkStringParam(PARAMETER_ID));
            String tmp = paramContainer.getStringParam(PARAMETER_SHOW_SRC);
            final boolean showMessageSource = (STR_1.equals(tmp) || Boolean.parseBoolean(tmp));
            tmp = paramContainer.getStringParam(PARAMETER_EDIT_DRAFT);
            final boolean editDraft = (STR_1.equals(tmp) || Boolean.parseBoolean(tmp));
            tmp = paramContainer.getStringParam(PARAMETER_SHOW_HEADER);
            final boolean showMessageHeaders = (STR_1.equals(tmp) || Boolean.parseBoolean(tmp));
            tmp = paramContainer.getStringParam(PARAMETER_SAVE);
            final boolean saveToDisk = (tmp != null && tmp.length() > 0 && Integer.parseInt(tmp) > 0);
            tmp = paramContainer.getStringParam(PARAMETER_VIEW);
            final String view = null == tmp ? null : tmp.toLowerCase(Locale.ENGLISH);
            tmp = paramContainer.getStringParam(PARAMETER_UNSEEN);
            final boolean unseen = (tmp != null && (STR_1.equals(tmp) || Boolean.parseBoolean(tmp)));
            tmp = null;
            /*
             * Get message
             */
            MailServletInterface mailInterface = mailInterfaceArg;
            boolean closeMailInterface = false;
            try {
                if (mailInterface == null) {
                    mailInterface = MailServletInterface.getInstance(session);
                    closeMailInterface = true;
                }
                /*
                 * Get \Seen state
                 */
                final MailMessage mail = mailInterface.getMessage(folderPath, uid);
                if (mail == null) {
                    throw new MailException(MailException.Code.MAIL_NOT_FOUND, Long.valueOf(uid), folderPath);
                }
                if (showMessageSource) {
                    final UnsynchronizedByteArrayOutputStream baos = new UnsynchronizedByteArrayOutputStream();
                    mail.writeTo(baos);
                    if (saveToDisk) {
                        /*
                         * Write message source to output stream...
                         */
                        final String userAgent = paramContainer.getHeader(STR_USER_AGENT).toLowerCase(Locale.ENGLISH);
                        final boolean internetExplorer = (userAgent != null && userAgent.indexOf(STR_MSIE) > -1 && userAgent.indexOf(STR_WINDOWS) > -1);
                        final ContentType contentType = new ContentType();
                        contentType.setPrimaryType(STR_APPLICATION);
                        contentType.setSubType(STR_OCTET_STREAM);
                        final String fileName = new StringBuilder(mail.getSubject().replaceAll(" ", "_")).append(".eml").toString();
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
                    final boolean wasUnseen = (mail.containsPrevSeen() && !mail.isPrevSeen());
                    final boolean doUnseen = (unseen && wasUnseen);
                    if (doUnseen) {
                        mail.setFlag(MailMessage.FLAG_SEEN, false);
                        final int unreadMsgs = mail.getUnreadMessages();
                        mail.setUnreadMessages(unreadMsgs < 0 ? 0 : unreadMsgs + 1);
                    }
                    data = new String(baos.toByteArray(), ct.containsParameter(STR_CHARSET) ? ct.getParameter(STR_CHARSET) : STR_UTF8);
                    if (doUnseen) {
                        /*
                         * Leave mail as unseen
                         */
                        mailInterface.updateMessageFlags(folderPath, new long[] { uid }, MailMessage.FLAG_SEEN, false);
                    } else if (wasUnseen) {
                        triggerContactCollector(session, mail);
                    }
                } else if (showMessageHeaders) {
                    final boolean wasUnseen = (mail.containsPrevSeen() && !mail.isPrevSeen());
                    final boolean doUnseen = (unseen && wasUnseen);
                    if (doUnseen) {
                        mail.setFlag(MailMessage.FLAG_SEEN, false);
                        final int unreadMsgs = mail.getUnreadMessages();
                        mail.setUnreadMessages(unreadMsgs < 0 ? 0 : unreadMsgs + 1);
                    }
                    data = formatMessageHeaders(mail.getHeadersIterator());
                    if (doUnseen) {
                        /*
                         * Leave mail as unseen
                         */
                        mailInterface.updateMessageFlags(folderPath, new long[] { uid }, MailMessage.FLAG_SEEN, false);
                    } else if (wasUnseen) {
                        triggerContactCollector(session, mail);
                    }
                } else {
                    final UserSettingMail usmNoSave = (UserSettingMail) session.getUserSettingMail().clone();
                    /*
                     * Deny saving for this request-specific settings
                     */
                    usmNoSave.setNoSave(true);
                    /*
                     * Overwrite settings with request's parameters
                     */
                    if (null != view) {
                        if (VIEW_TEXT.equals(view)) {
                            usmNoSave.setDisplayHtmlInlineContent(false);
                        } else if (VIEW_HTML.equals(view)) {
                            usmNoSave.setDisplayHtmlInlineContent(true);
                            usmNoSave.setAllowHTMLImages(true);
                        } else if (VIEW_HTML_BLOCKED_IMAGES.equals(view)) {
                            usmNoSave.setDisplayHtmlInlineContent(true);
                            usmNoSave.setAllowHTMLImages(false);
                        } else {
                            LOG.warn(new StringBuilder(64).append("Unknown value in parameter ").append(PARAMETER_VIEW).append(": ").append(
                                view).append(". Using user's mail settings as fallback."));
                        }
                    }
                    final boolean wasUnseen = (mail.containsPrevSeen() && !mail.isPrevSeen());
                    final boolean doUnseen = (unseen && wasUnseen);
                    if (doUnseen) {
                        mail.setFlag(MailMessage.FLAG_SEEN, false);
                        final int unreadMsgs = mail.getUnreadMessages();
                        mail.setUnreadMessages(unreadMsgs < 0 ? 0 : unreadMsgs + 1);
                    }
                    data = MessageWriter.writeMailMessage(
                        mail,
                        editDraft ? DisplayMode.MODIFYABLE : DisplayMode.DISPLAY,
                        session,
                        usmNoSave);
                    if (doUnseen) {
                        /*
                         * Leave mail as unseen
                         */
                        mailInterface.updateMessageFlags(folderPath, new long[] { uid }, MailMessage.FLAG_SEEN, false);
                    } else if (wasUnseen) {
                        triggerContactCollector(session, mail);
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
            final AbstractOXException wrapper = getWrappingOXException(e);
            LOG.error(wrapper.getMessage(), wrapper);
            response.setException(wrapper);
        }
        /*
         * Close response and flush print writer
         */
        response.setData(data);
        response.setTimestamp(null);
        return response;
    }

    private static void triggerContactCollector(final ServerSession session, final MailMessage mail) {
        final ContactCollectorService ccs = ServerServiceRegistry.getInstance().getService(ContactCollectorService.class);
        if (null != ccs) {
            final Set<InternetAddress> addrs = new HashSet<InternetAddress>();
            addrs.addAll(Arrays.asList(mail.getFrom()));
            addrs.addAll(Arrays.asList(mail.getTo()));
            addrs.addAll(Arrays.asList(mail.getCc()));
            addrs.addAll(Arrays.asList(mail.getBcc()));
            // Strip by aliases
            try {
                final Set<InternetAddress> validAddrs = new HashSet<InternetAddress>(4);
                final UserSettingMail usm = session.getUserSettingMail();
                if (usm.getSendAddr() != null && usm.getSendAddr().length() > 0) {
                    validAddrs.add(new InternetAddress(usm.getSendAddr()));
                }
                final User user = UserStorage.getStorageUser(session.getUserId(), session.getContextId());
                validAddrs.add(new InternetAddress(user.getMail()));
                final String[] aliases = user.getAliases();
                for (final String alias : aliases) {
                    validAddrs.add(new InternetAddress(alias));
                }
                addrs.removeAll(validAddrs);
            } catch (final AddressException e) {
                LOG.warn("Collected contacts could not be stripped by user's email aliases: " + e.getMessage(), e);

            }
            if (!addrs.isEmpty()) {
                // Add addresses
                ccs.memorizeAddresses(new ArrayList<InternetAddress>(addrs), session);
            }
        }
    }

    private static final String formatMessageHeaders(final Iterator<Map.Entry<String, String>> iter) {
        final StringBuilder sb = new StringBuilder(1024);
        while (iter.hasNext()) {
            final Map.Entry<String, String> entry = iter.next();
            sb.append(entry.getKey()).append(STR_DELIM).append(entry.getValue()).append(STR_CRLF);
        }
        return sb.toString();
    }

    public void actionGetNew(final ServerSession session, final JSONWriter writer, final JSONObject requestObj, final MailServletInterface mi) throws SearchIteratorException, JSONException {
        ResponseWriter.write(actionGetNew(session, ParamContainer.getInstance(requestObj, EnumComponent.MAIL), mi), writer);
    }

    private final void actionGetNew(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
        try {
            ResponseWriter.write(
                actionGetNew(getSessionObject(req), ParamContainer.getInstance(req, EnumComponent.MAIL, resp), null),
                resp.getWriter());
        } catch (final JSONException e) {
            final OXJSONException oxe = new OXJSONException(OXJSONException.Code.JSON_WRITE_ERROR, e, new Object[0]);
            LOG.error(oxe.getMessage(), oxe);
            final Response response = new Response();
            response.setException(oxe);
            try {
                ResponseWriter.write(response, resp.getWriter());
            } catch (final JSONException e1) {
                LOG.error(RESPONSE_ERROR, e1);
                sendError(resp);
            }
        } catch (final SearchIteratorException e) {
            LOG.error(e.getMessage(), e);
            final Response response = new Response();
            response.setException(e);
            try {
                ResponseWriter.write(response, resp.getWriter());
            } catch (final JSONException e1) {
                LOG.error(RESPONSE_ERROR, e1);
                sendError(resp);
            }
        }
    }

    private final Response actionGetNew(final ServerSession session, final ParamContainer paramContainer, final MailServletInterface mailInterfaceArg) throws JSONException, SearchIteratorException {
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
            MailServletInterface mailInterface = mailInterfaceArg;
            boolean closeMailInterface = false;
            try {
                if (mailInterface == null) {
                    mailInterface = MailServletInterface.getInstance(session);
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
                final MailFieldWriter[] writers = MessageWriter.getMailFieldWriter(MailListField.getFields(columns));
                it = mailInterface.getNewMessages(folderId, sortCol, orderDir, columns, limit == ParamContainer.NOT_FOUND ? -1 : limit);
                final int size = it.size();
                for (int i = 0; i < size; i++) {
                    final MailMessage mail = (MailMessage) it.next();
                    final JSONArray ja = new JSONArray();
                    for (final MailFieldWriter writer : writers) {
                        writer.writeField(ja, mail, 0, false, session.getUserId(), session.getContextId());
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
            final AbstractOXException wrapper = getWrappingOXException(e);
            LOG.error(wrapper.getMessage(), wrapper);
            response.setException(wrapper);
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

    public void actionGetSaveVersit(final ServerSession session, final Writer writer, final JSONObject requestObj, final MailServletInterface mi) throws JSONException, IOException {
        actionGetSaveVersit(session, writer, ParamContainer.getInstance(requestObj, EnumComponent.MAIL), mi);
    }

    private final void actionGetSaveVersit(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
        try {
            actionGetSaveVersit(getSessionObject(req), resp.getWriter(), ParamContainer.getInstance(req, EnumComponent.MAIL, resp), null);
        } catch (final JSONException e) {
            final OXJSONException oxe = new OXJSONException(OXJSONException.Code.JSON_WRITE_ERROR, e, new Object[0]);
            LOG.error(oxe.getMessage(), oxe);
            final Response response = new Response();
            response.setException(oxe);
            try {
                ResponseWriter.write(response, resp.getWriter());
            } catch (final JSONException e1) {
                LOG.error(RESPONSE_ERROR, e1);
                sendError(resp);
            }
        }
    }

    private final void actionGetSaveVersit(final ServerSession session, final Writer writer, final ParamContainer paramContainer, final MailServletInterface mailInterfaceArg) throws JSONException, IOException {
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
            final String folderPath = paramContainer.checkStringParam(PARAMETER_FOLDERID);
            final long uid = Long.parseLong(paramContainer.checkStringParam(PARAMETER_ID));
            // final String msgUID =
            // paramContainer.checkStringParam(PARAMETER_ID);
            final String partIdentifier = paramContainer.checkStringParam(PARAMETER_MAILATTCHMENT);
            /*
             * Get new mails
             */
            MailServletInterface mailInterface = mailInterfaceArg;
            boolean closeMailInterface = false;
            try {
                if (mailInterface == null) {
                    mailInterface = MailServletInterface.getInstance(session);
                    closeMailInterface = true;
                }
                final CommonObject[] insertedObjs;
                {
                    final MailPart versitPart = mailInterface.getMessageAttachment(folderPath, uid, partIdentifier, false);
                    /*
                     * Save dependent on content type
                     */
                    final Context ctx = ContextStorage.getStorageContext(session.getContextId());
                    final List<CommonObject> retvalList = new ArrayList<CommonObject>();
                    if (versitPart.getContentType().isMimeType(MIMETypes.MIME_TEXT_X_VCARD) || versitPart.getContentType().isMimeType(
                        MIMETypes.MIME_TEXT_VCARD)) {
                        /*
                         * Save VCard
                         */
                        VersitUtility.saveVCard(
                            versitPart.getInputStream(),
                            versitPart.getContentType().getBaseType(),
                            versitPart.getContentType().containsCharsetParameter() ? versitPart.getContentType().getCharsetParameter() : MailConfig.getDefaultMimeCharset(),
                            retvalList,
                            session,
                            ctx);
                    } else if (versitPart.getContentType().isMimeType(MIMETypes.MIME_TEXT_X_VCALENDAR) || versitPart.getContentType().isMimeType(
                        MIMETypes.MIME_TEXT_CALENDAR)) {
                        /*
                         * Save ICalendar
                         */
                        VersitUtility.saveICal(
                            versitPart.getInputStream(),
                            versitPart.getContentType().getBaseType(),
                            versitPart.getContentType().containsCharsetParameter() ? versitPart.getContentType().getCharsetParameter() : MailConfig.getDefaultMimeCharset(),
                            retvalList,
                            session,
                            ctx);
                    } else {
                        throw new MailException(MailException.Code.UNSUPPORTED_VERSIT_ATTACHMENT, versitPart.getContentType());
                    }
                    insertedObjs = retvalList.toArray(new CommonObject[retvalList.size()]);
                }
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
            final AbstractOXException wrapper = getWrappingOXException(e);
            LOG.error(wrapper.getMessage(), wrapper);
            response.setException(wrapper);
        }
        /*
         * Close response and flush print writer
         */
        jsonWriter.endArray();
        response.setData(jsonWriter.getObject());
        response.setTimestamp(null);
        ResponseWriter.write(response, writer);
    }

    public void actionGetAttachment() throws MailException {
        throw new MailException(MailException.Code.UNSUPPORTED_ACTION, ACTION_MATTACH, "Multiple servlet");
    }

    /**
     * Looks up a mail attachment and writes its content directly into response output stream. This method is not accessible via Multiple
     * servlet
     */
    private final void actionGetAttachment(final HttpServletRequest req, final HttpServletResponse resp) {
        /*
         * Some variables
         */
        final ServerSession session = getSessionObject(req);
        boolean outSelected = false;
        boolean saveToDisk = false;
        /*
         * Start response
         */
        try {
            /*
             * Read in parameters
             */
            final String folderPath = checkStringParam(req, PARAMETER_FOLDERID);
            final long uid = Long.parseLong(checkStringParam(req, PARAMETER_ID));
            final String sequenceId = req.getParameter(PARAMETER_MAILATTCHMENT);
            final String imageContentId = req.getParameter(PARAMETER_MAILCID);
            {
                final String saveParam = req.getParameter(PARAMETER_SAVE);
                saveToDisk = ((saveParam == null || saveParam.length() == 0) ? false : ((Integer.parseInt(saveParam)) > 0));
            }
            final boolean filter;
            {
                final String filterParam = req.getParameter(PARAMETER_FILTER);
                filter = Boolean.parseBoolean(filterParam) || STR_1.equals(filterParam);
            }
            /*
             * Get attachment
             */
            final MailServletInterface mailInterface = MailServletInterface.getInstance(session);
            try {
                if (sequenceId == null && imageContentId == null) {
                    throw new MailException(MailException.Code.MISSING_PARAM, new StringBuilder().append(PARAMETER_MAILATTCHMENT).append(
                        " | ").append(PARAMETER_MAILCID).toString());
                }
                final MailPart mailPart;
                final InputStream attachmentInputStream;
                if (imageContentId == null) {
                    mailPart = mailInterface.getMessageAttachment(folderPath, uid, sequenceId, !saveToDisk);
                    if (mailPart == null) {
                        throw new MailException(MailException.Code.NO_ATTACHMENT_FOUND, sequenceId);
                    }
                    if (filter && !saveToDisk && mailPart.getContentType().isMimeType(MIMETypes.MIME_TEXT_HTM_ALL)) {
                        /*
                         * Apply filter
                         */
                        final ContentType contentType = mailPart.getContentType();
                        final String cs = contentType.containsCharsetParameter() ? contentType.getCharsetParameter() : MailConfig.getDefaultMimeCharset();
                        final String htmlContent = MessageUtility.readMailPart(mailPart, cs);
                        final HTMLFilterHandler filterHandler = new HTMLFilterHandler(htmlContent.length());
                        HTMLParser.parse(HTMLProcessing.getConformHTML(htmlContent, contentType), filterHandler);
                        attachmentInputStream = new UnsynchronizedByteArrayInputStream(filterHandler.getHTML().getBytes(cs));
                    } else {
                        attachmentInputStream = mailPart.getInputStream();
                    }
                    /*-
                     * TODO: Does not work, yet.
                     * 
                     * if (!saveToDisk &amp;&amp; mailPart.getContentType().isMimeType(MIMETypes.MIME_MESSAGE_RFC822)) {
                     *     // Treat as a mail get
                     *     final MailMessage mail = (MailMessage) mailPart.getContent();
                     *     final Response response = new Response();
                     *     response.setData(MessageWriter.writeMailMessage(mail, true, session));
                     *     response.setTimestamp(null);
                     *     ResponseWriter.write(response, resp.getWriter());
                     *     return;
                     * }
                     */
                } else {
                    mailPart = mailInterface.getMessageImage(folderPath, uid, imageContentId);
                    if (mailPart == null) {
                        throw new MailException(MailException.Code.NO_ATTACHMENT_FOUND, sequenceId);
                    }
                    attachmentInputStream = mailPart.getInputStream();
                }
                /*
                 * Write to response
                 */
                final String userAgent = req.getHeader(STR_USER_AGENT) == null ? null : req.getHeader(STR_USER_AGENT).toLowerCase(
                    Locale.ENGLISH);
                final boolean internetExplorer = (userAgent != null && userAgent.indexOf(STR_MSIE) > -1 && userAgent.indexOf(STR_WINDOWS) > -1);
                final ContentType contentType;
                if (saveToDisk) {
                    contentType = new ContentType();
                    contentType.setPrimaryType(STR_APPLICATION);
                    contentType.setSubType(STR_OCTET_STREAM);
                    resp.setHeader(
                        STR_CONTENT_DISPOSITION,
                        new StringBuilder(64).append(STR_ATTACHMENT_FILENAME).append(
                            getSaveAsFileName(mailPart.getFileName(), internetExplorer, mailPart.getContentType().toString())).append('"').toString());
                } else {
                    final String fileName = getSaveAsFileName(
                        mailPart.getFileName(),
                        internetExplorer,
                        mailPart.getContentType().toString());
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
                    contentType.setParameter(STR_NAME, fileName);
                    resp.setHeader(
                        STR_CONTENT_DISPOSITION,
                        new StringBuilder(50).append(STR_INLINE_FILENAME).append(fileName).append('"').toString());
                }
                resp.setContentType(contentType.toString());
                /*
                 * Reset response header values since we are going to directly write into servlet's output stream and then some browsers do
                 * not allow header "Pragma"
                 */
                Tools.removeCachingHeader(resp);
                final OutputStream out = resp.getOutputStream();
                outSelected = true;
                /*
                 * Write from content's input stream to response output stream
                 */
                try {
                    final byte[] buffer = new byte[0xFFFF];
                    for (int len; (len = attachmentInputStream.read(buffer, 0, buffer.length)) != -1;) {
                        out.write(buffer, 0, len);
                    }
                    out.flush();
                } finally {
                    attachmentInputStream.close();
                }
            } finally {
                if (mailInterface != null) {
                    mailInterface.close(true);
                }
            }
        } catch (final AbstractOXException e) {
            LOG.error(e.getMessage(), e);
            try {
                resp.setContentType(MIME_TEXT_HTML_CHARSET_UTF_8);
                final Writer writer;
                if (outSelected) {
                    /*
                     * Output stream has already been selected
                     */
                    Tools.disableCaching(resp);
                    writer = new PrintWriter(
                        new BufferedWriter(new OutputStreamWriter(resp.getOutputStream(), resp.getCharacterEncoding())),
                        true);
                } else {
                    writer = resp.getWriter();
                }
                resp.setHeader(STR_CONTENT_DISPOSITION, null);
                final Response response = new Response();
                response.setException(e);
                final String callback = saveToDisk ? JS_FRAGMENT : JS_FRAGMENT_POPUP;
                writer.write(callback.replaceFirst(JS_FRAGMENT_JSON, Matcher.quoteReplacement(ResponseWriter.getJSON(response).toString())).replaceFirst(
                    JS_FRAGMENT_ACTION,
                    "error"));
                writer.flush();
            } catch (final UnsupportedEncodingException uee) {
                uee.initCause(e);
                LOG.error(uee.getMessage(), uee);
            } catch (final IOException ioe) {
                ioe.initCause(e);
                LOG.error(ioe.getMessage(), ioe);
            } catch (final IllegalStateException ise) {
                ise.initCause(e);
                LOG.error(ise.getMessage(), ise);
            } catch (final JSONException je) {
                je.initCause(e);
                LOG.error(je.getMessage(), je);
            }
        } catch (final Exception e) {
            try {
                resp.setContentType(MIME_TEXT_HTML_CHARSET_UTF_8);
                final Writer writer;
                if (outSelected) {
                    /*
                     * Output stream has already been selected
                     */
                    Tools.disableCaching(resp);
                    writer = new PrintWriter(
                        new BufferedWriter(new OutputStreamWriter(resp.getOutputStream(), resp.getCharacterEncoding())),
                        true);
                } else {
                    writer = resp.getWriter();
                }
                resp.setHeader(STR_CONTENT_DISPOSITION, null);
                final Response response = new Response();
                final AbstractOXException exc = getWrappingOXException(e);
                LOG.error(exc.getMessage(), e);
                response.setException(exc);
                final String callback = saveToDisk ? JS_FRAGMENT : JS_FRAGMENT_POPUP;
                writer.write(callback.replaceFirst(JS_FRAGMENT_JSON, Matcher.quoteReplacement(ResponseWriter.getJSON(response).toString())).replaceFirst(
                    JS_FRAGMENT_ACTION,
                    "error"));
                writer.flush();
            } catch (final UnsupportedEncodingException uee) {
                uee.initCause(e);
                LOG.error(uee.getMessage(), uee);
            } catch (final IOException ioe) {
                ioe.initCause(e);
                LOG.error(ioe.getMessage(), ioe);
            } catch (final IllegalStateException ise) {
                ise.initCause(e);
                LOG.error(ise.getMessage(), ise);
            } catch (final JSONException je) {
                je.initCause(e);
                LOG.error(je.getMessage(), je);
            }
        }
    }

    private static final Pattern PART_FILENAME_PATTERN = Pattern.compile("(part )([0-9]+)(?:(\\.)([0-9]+))*", Pattern.CASE_INSENSITIVE);

    private static final String DEFAULT_FILENAME = "file.dat";

    public static final String getSaveAsFileName(final String fileName, final boolean internetExplorer, final String baseCT) {
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
                LOG.error("Unsupported encoding in a message detected and monitored: \"" + STR_UTF8 + '"', e);
                MailServletInterface.mailInterfaceMonitor.addUnsupportedEncodingExceptions(STR_UTF8);
                return fileName;
            }
        }
        if (null != baseCT) {
            if (baseCT.regionMatches(true, 0, MIME_TEXT_PLAIN, 0, MIME_TEXT_PLAIN.length())) {
                if (!fileName.toLowerCase(Locale.ENGLISH).endsWith(".txt")) {
                    tmp.append(".txt");
                }
            } else if (baseCT.regionMatches(true, 0, MIME_TEXT_HTML, 0, MIME_TEXT_HTML.length())) {
                if (!fileName.toLowerCase(Locale.ENGLISH).endsWith(".htm") && !fileName.toLowerCase(Locale.ENGLISH).endsWith(".html")) {
                    tmp.append(".html");
                }
            }
        }
        return tmp.toString();
    }

    public void actionPutForwardMultiple(final ServerSession session, final JSONWriter writer, final JSONObject jsonObj, final MailServletInterface mi) throws JSONException {
        ResponseWriter.write(actionPutForwardMultiple(session, jsonObj.getString(ResponseFields.DATA), ParamContainer.getInstance(
            jsonObj,
            EnumComponent.MAIL), mi), writer);
    }

    private final void actionPutForwardMultiple(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
        try {
            ResponseWriter.write(actionPutForwardMultiple(getSessionObject(req), getBody(req), ParamContainer.getInstance(
                req,
                EnumComponent.MAIL,
                resp), null), resp.getWriter());
        } catch (final JSONException e) {
            final OXJSONException oxe = new OXJSONException(OXJSONException.Code.JSON_WRITE_ERROR, e, new Object[0]);
            LOG.error(oxe.getMessage(), oxe);
            final Response response = new Response();
            response.setException(oxe);
            try {
                ResponseWriter.write(response, resp.getWriter());
            } catch (final JSONException e1) {
                LOG.error(RESPONSE_ERROR, e1);
                sendError(resp);
            }
        }
    }

    private final Response actionPutForwardMultiple(final ServerSession session, final String body, final ParamContainer paramContainer, final MailServletInterface mailInterfaceArg) throws JSONException {
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
            final JSONArray paths = new JSONArray(body);
            final String[] folders = new String[paths.length()];
            final long[] ids = new long[paths.length()];
            for (int i = 0; i < folders.length; i++) {
                final JSONObject folderAndID = paths.getJSONObject(i);
                folders[i] = folderAndID.getString(PARAMETER_FOLDERID);
                ids[i] = Long.parseLong(folderAndID.get(PARAMETER_ID).toString());
            }
            final String view = paramContainer.getStringParam(PARAMETER_VIEW);
            final UserSettingMail usmNoSave = (UserSettingMail) session.getUserSettingMail().clone();
            /*
             * Deny saving for this request-specific settings
             */
            usmNoSave.setNoSave(true);
            /*
             * Overwrite settings with request's parameters
             */
            if (null != view) {
                if (VIEW_TEXT.equals(view)) {
                    usmNoSave.setDisplayHtmlInlineContent(false);
                } else if (VIEW_HTML.equals(view)) {
                    usmNoSave.setDisplayHtmlInlineContent(true);
                    usmNoSave.setAllowHTMLImages(true);
                } else {
                    LOG.warn(new StringBuilder(64).append("Unknown value in parameter ").append(PARAMETER_VIEW).append(": ").append(view).append(
                        ". Using user's mail settings as fallback."));
                }
            }
            /*
             * Get forward message
             */
            MailServletInterface mailInterface = mailInterfaceArg;
            boolean closeMailInterface = false;
            try {
                if (mailInterface == null) {
                    mailInterface = MailServletInterface.getInstance(session);
                    closeMailInterface = true;
                }
                data = MessageWriter.writeMailMessage(
                    mailInterface.getForwardMessageForDisplay(folders, ids, usmNoSave),
                    DisplayMode.MODIFYABLE,
                    session,
                    usmNoSave);
            } finally {
                if (closeMailInterface && mailInterface != null) {
                    mailInterface.close(true);
                }
            }
        } catch (final AbstractOXException e) {
            LOG.error(e.getMessage(), e);
            response.setException(e);
        } catch (final Exception e) {
            final AbstractOXException wrapper = getWrappingOXException(e);
            LOG.error(wrapper.getMessage(), wrapper);
            response.setException(wrapper);
        }
        /*
         * Close response and flush print writer
         */
        response.setData(data);
        response.setTimestamp(null);
        return response;
    }

    public void actionPutReply(final ServerSession session, final boolean replyAll, final JSONWriter writer, final JSONObject jsonObj, final MailServletInterface mi) throws JSONException {
        ResponseWriter.write(actionPutReply(session, jsonObj.getString(ResponseFields.DATA), ParamContainer.getInstance(
            jsonObj,
            EnumComponent.MAIL), replyAll, mi), writer);
    }

    private final void actionPutReply(final HttpServletRequest req, final HttpServletResponse resp, final boolean replyAll) throws IOException {
        try {
            ResponseWriter.write(actionPutReply(getSessionObject(req), getBody(req), ParamContainer.getInstance(
                req,
                EnumComponent.MAIL,
                resp), replyAll, null), resp.getWriter());
        } catch (final JSONException e) {
            final OXJSONException oxe = new OXJSONException(OXJSONException.Code.JSON_WRITE_ERROR, e, new Object[0]);
            LOG.error(oxe.getMessage(), oxe);
            final Response response = new Response();
            response.setException(oxe);
            try {
                ResponseWriter.write(response, resp.getWriter());
            } catch (final JSONException e1) {
                LOG.error(RESPONSE_ERROR, e1);
                sendError(resp);
            }
        }
    }

    private final Response actionPutReply(final ServerSession session, final String body, final ParamContainer paramContainer, final boolean replyAll, final MailServletInterface mailInterfaceArg) throws JSONException {
        /*
         * Create new parameter container from body data...
         */
        final JSONArray paths = new JSONArray(body);
        final int length = paths.length();
        if (length != 1) {
            throw new IllegalArgumentException("JSON array's length is not 1");
        }
        final Map<String, String> map = new HashMap<String, String>(2);
        for (int i = 0; i < length; i++) {
            final JSONObject folderAndID = paths.getJSONObject(i);
            map.put(PARAMETER_FOLDERID, folderAndID.getString(PARAMETER_FOLDERID));
            map.put(PARAMETER_ID, folderAndID.get(PARAMETER_ID).toString());
        }
        /*
         * ... and fake a GET request
         */
        return actionGetReply(session, replyAll, ParamContainer.getInstance(map, EnumComponent.MAIL), mailInterfaceArg);
    }

    public void actionPutGet(final ServerSession session, final JSONWriter writer, final JSONObject jsonObj, final MailServletInterface mi) throws JSONException {
        ResponseWriter.write(actionPutGet(session, jsonObj.getString(ResponseFields.DATA), ParamContainer.getInstance(
            jsonObj,
            EnumComponent.MAIL), mi), writer);
    }

    private final void actionPutGet(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
        try {
            ResponseWriter.write(actionPutGet(
                getSessionObject(req),
                getBody(req),
                ParamContainer.getInstance(req, EnumComponent.MAIL, resp),
                null), resp.getWriter());
        } catch (final JSONException e) {
            final OXJSONException oxe = new OXJSONException(OXJSONException.Code.JSON_WRITE_ERROR, e, new Object[0]);
            LOG.error(oxe.getMessage(), oxe);
            final Response response = new Response();
            response.setException(oxe);
            try {
                ResponseWriter.write(response, resp.getWriter());
            } catch (final JSONException e1) {
                LOG.error(RESPONSE_ERROR, e1);
                sendError(resp);
            }
        }
    }

    private final Response actionPutGet(final ServerSession session, final String body, final ParamContainer paramContainer, final MailServletInterface mailInterfaceArg) throws JSONException {
        /*
         * Create new parameter container from body data...
         */
        final JSONArray paths = new JSONArray(body);
        final int length = paths.length();
        if (length != 1) {
            throw new IllegalArgumentException("JSON array's length is not 1");
        }
        final Map<String, String> map = new HashMap<String, String>(2);
        for (int i = 0; i < length; i++) {
            final JSONObject folderAndID = paths.getJSONObject(i);
            map.put(PARAMETER_FOLDERID, folderAndID.getString(PARAMETER_FOLDERID));
            map.put(PARAMETER_ID, folderAndID.get(PARAMETER_ID).toString());
        }
        try {
            String tmp = paramContainer.getStringParam(PARAMETER_SHOW_SRC);
            if (STR_1.equals(tmp) || Boolean.parseBoolean(tmp)) { // showMessageSource
                map.put(PARAMETER_SHOW_SRC, tmp);
            }
            tmp = paramContainer.getStringParam(PARAMETER_EDIT_DRAFT);
            if (STR_1.equals(tmp) || Boolean.parseBoolean(tmp)) { // editDraft
                map.put(PARAMETER_EDIT_DRAFT, tmp);
            }
            tmp = paramContainer.getStringParam(PARAMETER_SHOW_HEADER);
            if (STR_1.equals(tmp) || Boolean.parseBoolean(tmp)) { // showMessageHeaders
                map.put(PARAMETER_SHOW_HEADER, tmp);
            }
            tmp = paramContainer.getStringParam(PARAMETER_SAVE);
            if (tmp != null && tmp.length() > 0 && Integer.parseInt(tmp) > 0) { // saveToDisk
                map.put(PARAMETER_SAVE, tmp);
            }
            tmp = paramContainer.getStringParam(PARAMETER_VIEW);
            if (tmp != null) { // view
                map.put(PARAMETER_VIEW, tmp);
            }
            tmp = paramContainer.getStringParam(PARAMETER_UNSEEN);
            if (tmp != null) { // unseen
                map.put(PARAMETER_UNSEEN, tmp);
            }
            tmp = null;
        } catch (final AbstractOXException e) {
            final Response response = new Response();
            response.setException(e);
            return response;
        }
        /*
         * ... and fake a GET request
         */
        return actionGetMessage(session, ParamContainer.getInstance(map, EnumComponent.MAIL), mailInterfaceArg);
    }

    public void actionPutAutosave(final ServerSession session, final JSONWriter writer, final JSONObject jsonObj, final MailServletInterface mi) throws JSONException {
        ResponseWriter.write(actionPutAutosave(session, jsonObj.getString(ResponseFields.DATA), ParamContainer.getInstance(
            jsonObj,
            EnumComponent.MAIL), mi), writer);
    }

    private final void actionPutAutosave(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
        try {
            ResponseWriter.write(actionPutAutosave(getSessionObject(req), getBody(req), ParamContainer.getInstance(
                req,
                EnumComponent.MAIL,
                resp), null), resp.getWriter());
        } catch (final JSONException e) {
            final OXJSONException oxe = new OXJSONException(OXJSONException.Code.JSON_WRITE_ERROR, e, new Object[0]);
            LOG.error(oxe.getMessage(), oxe);
            final Response response = new Response();
            response.setException(oxe);
            try {
                ResponseWriter.write(response, resp.getWriter());
            } catch (final JSONException e1) {
                LOG.error(RESPONSE_ERROR, e1);
                sendError(resp);
            }
        }
    }

    private final Response actionPutAutosave(final ServerSession session, final String body, final ParamContainer paramContainer, final MailServletInterface mailInterfaceArg) throws JSONException {
        /*
         * Some variables
         */
        final Response response = new Response();
        try {
            /*
             * Autosave draft
             */
            MailServletInterface mailInterface = mailInterfaceArg;
            boolean closeMailInterface = false;
            try {
                if (mailInterface == null) {
                    mailInterface = MailServletInterface.getInstance(session);
                    closeMailInterface = true;
                }
                String msgIdentifier = null;
                {
                    final JSONObject jsonMailObj = new JSONObject(body);
                    /*
                     * Parse
                     */
                    final ComposedMailMessage composedMail = MessageParser.parse(jsonMailObj, (UploadEvent) null, session);
                    if ((composedMail.getFlags() & MailMessage.FLAG_DRAFT) == 0) {
                        LOG.warn("Missing \\Draft flag on action=autosave in JSON message object", new Throwable());
                        composedMail.setFlag(MailMessage.FLAG_DRAFT, true);
                    }
                    if ((composedMail.getFlags() & MailMessage.FLAG_DRAFT) == MailMessage.FLAG_DRAFT) {
                        /*
                         * ... and autosave draft
                         */
                        msgIdentifier = mailInterface.saveDraft(composedMail, true);
                    } else {
                        throw new MailException(MailException.Code.UNEXPECTED_ERROR, "No new message on action=edit");
                    }
                }
                if (msgIdentifier == null) {
                    throw new MailException(MailException.Code.SEND_FAILED_UNKNOWN);
                }
                /*
                 * Fill JSON response object
                 */
                response.setData(msgIdentifier);
            } finally {
                if (closeMailInterface && mailInterface != null) {
                    mailInterface.close(true);
                }
            }
        } catch (final AbstractOXException e) {
            LOG.error(e.getMessage(), e);
            response.setException(e);
        } catch (final Exception e) {
            final AbstractOXException wrapper = getWrappingOXException(e);
            LOG.error(wrapper.getMessage(), wrapper);
            response.setException(wrapper);
        }
        /*
         * Close response and flush print writer
         */
        response.setTimestamp(null);
        return response;
    }

    public void actionPutClear(final ServerSession session, final JSONWriter writer, final JSONObject jsonObj, final MailServletInterface mi) throws JSONException {
        ResponseWriter.write(actionPutClear(session, jsonObj.getString(ResponseFields.DATA), ParamContainer.getInstance(
            jsonObj,
            EnumComponent.MAIL), mi), writer);
    }

    private final void actionPutClear(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
        try {
            ResponseWriter.write(actionPutClear(getSessionObject(req), getBody(req), ParamContainer.getInstance(
                req,
                EnumComponent.MAIL,
                resp), null), resp.getWriter());
        } catch (final JSONException e) {
            final OXJSONException oxe = new OXJSONException(OXJSONException.Code.JSON_WRITE_ERROR, e, new Object[0]);
            LOG.error(oxe.getMessage(), oxe);
            final Response response = new Response();
            response.setException(oxe);
            try {
                ResponseWriter.write(response, resp.getWriter());
            } catch (final JSONException e1) {
                LOG.error(RESPONSE_ERROR, e1);
                sendError(resp);
            }
        }
    }

    private final Response actionPutClear(final ServerSession session, final String body, final ParamContainer paramContainer, final MailServletInterface mailInterfaceArg) throws JSONException {
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
                MailServletInterface mailInterface = mailInterfaceArg;
                boolean closeMailInterface = false;
                try {
                    if (mailInterface == null) {
                        mailInterface = MailServletInterface.getInstance(session);
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
            final AbstractOXException wrapper = getWrappingOXException(e);
            LOG.error(wrapper.getMessage(), wrapper);
            response.setException(wrapper);
        }
        /*
         * Close response and flush print writer
         */
        jsonWriter.endArray();
        response.setData(jsonWriter.getObject());
        response.setTimestamp(null);
        return response;
    }

    public void actionPutMailSearch(final ServerSession session, final JSONWriter writer, final JSONObject jsonObj, final MailServletInterface mi) throws JSONException, SearchIteratorException {
        ResponseWriter.write(actionPutMailSearch(session, jsonObj.getString(ResponseFields.DATA), ParamContainer.getInstance(
            jsonObj,
            EnumComponent.MAIL), mi), writer);
    }

    private final void actionPutMailSearch(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
        try {
            ResponseWriter.write(actionPutMailSearch(getSessionObject(req), getBody(req), ParamContainer.getInstance(
                req,
                EnumComponent.MAIL,
                resp), null), resp.getWriter());
        } catch (final JSONException e) {
            final OXJSONException oxe = new OXJSONException(OXJSONException.Code.JSON_WRITE_ERROR, e, new Object[0]);
            LOG.error(oxe.getMessage(), oxe);
            final Response response = new Response();
            response.setException(oxe);
            try {
                ResponseWriter.write(response, resp.getWriter());
            } catch (final JSONException e1) {
                LOG.error(RESPONSE_ERROR, e1);
                sendError(resp);
            }
        } catch (final SearchIteratorException e) {
            LOG.error(e.getMessage(), e);
            final Response response = new Response();
            response.setException(e);
            try {
                ResponseWriter.write(response, resp.getWriter());
            } catch (final JSONException e1) {
                LOG.error(RESPONSE_ERROR, e1);
                sendError(resp);
            }
        }
    }

    private final Response actionPutMailSearch(final ServerSession session, final String body, final ParamContainer paramContainer, final MailServletInterface mailInterfaceArg) throws JSONException, SearchIteratorException {
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
                    searchPats[i] = tmp.getString(PARAMETER_SEARCHPATTERN);
                }
                /*
                 * Search mails
                 */
                MailServletInterface mailInterface = mailInterfaceArg;
                boolean closeMailInterface = false;
                try {
                    if (mailInterface == null) {
                        mailInterface = MailServletInterface.getInstance(session);
                        closeMailInterface = true;
                    }
                    /*
                     * Pre-Select field writers
                     */
                    /*
                     * Pre-Select field writers
                     */
                    final MailFieldWriter[] writers = MessageWriter.getMailFieldWriter(MailListField.getFields(columns));
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
                                writer.writeField(arr, mail, 0, false, session.getUserId(), session.getContextId());
                            }
                            jsonWriter.value(arr);
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
                        it = mailInterface.getMessages(folderId, null, sortCol, orderDir, searchCols, searchPats, true, columns);
                        final int size = it.size();
                        for (int i = 0; i < size; i++) {
                            final MailMessage mail = (MailMessage) it.next();
                            final JSONArray arr = new JSONArray();
                            for (final MailFieldWriter writer : writers) {
                                writer.writeField(arr, mail, 0, false, session.getUserId(), session.getContextId());
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
            final AbstractOXException wrapper = getWrappingOXException(e);
            LOG.error(wrapper.getMessage(), wrapper);
            response.setException(wrapper);
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

    public void actionPutMailList(final ServerSession session, final JSONWriter writer, final JSONObject jsonObj, final MailServletInterface mi) throws JSONException {
        ResponseWriter.write(actionPutMailList(session, jsonObj.getString(ResponseFields.DATA), ParamContainer.getInstance(
            jsonObj,
            EnumComponent.MAIL), mi), writer);
    }

    private final void actionPutMailList(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
        try {
            ResponseWriter.write(actionPutMailList(getSessionObject(req), getBody(req), ParamContainer.getInstance(
                req,
                EnumComponent.MAIL,
                resp), null), resp.getWriter());
        } catch (final JSONException e) {
            final OXJSONException oxe = new OXJSONException(OXJSONException.Code.JSON_WRITE_ERROR, e, new Object[0]);
            LOG.error(oxe.getMessage(), oxe);
            final Response response = new Response();
            response.setException(oxe);
            try {
                ResponseWriter.write(response, resp.getWriter());
            } catch (final JSONException e1) {
                LOG.error(RESPONSE_ERROR, e1);
                sendError(resp);
            }
        }
    }

    private final Response actionPutMailList(final ServerSession session, final String body, final ParamContainer paramContainer, final MailServletInterface mailInterfaceArg) throws JSONException {
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
                final MailFieldWriter[] writers = MessageWriter.getMailFieldWriter(MailListField.getFields(columns));
                final Map<String, SmartLongArray> idMap = new HashMap<String, SmartLongArray>(4);
                fillMapByArray(idMap, jsonIDs, length);
                final int size = idMap.size();
                if (size == 0) {
                    /*
                     * Must not be zero since JSON array's length is greater than zero.
                     */
                    if (LOG.isWarnEnabled()) {
                        final String jsonIDsStr = jsonIDs.toString();
                        LOG.warn(new StringBuilder(jsonIDsStr.length() + 64).append("Parsing of folder-and-ID-pairs failed:\n").append(
                            jsonIDsStr).toString(), new Throwable());
                    }
                    final Response r = new Response();
                    r.setData(EMPTY_JSON_ARR);
                    return r;
                }
                MailServletInterface mailInterface = mailInterfaceArg;
                boolean closeMailInterface = false;
                try {
                    if (mailInterface == null) {
                        mailInterface = MailServletInterface.getInstance(session);
                        closeMailInterface = true;
                    }
                    final Iterator<Map.Entry<String, SmartLongArray>> iter = idMap.entrySet().iterator();
                    for (int k = 0; k < size; k++) {
                        final Map.Entry<String, SmartLongArray> entry = iter.next();
                        /*
                         * Get message list
                         */
                        final MailMessage[] mails = mailInterface.getMessageList(entry.getKey(), entry.getValue().toArray(), columns);
                        for (int i = 0; i < mails.length; i++) {
                            if (mails[i] != null) {
                                final JSONArray ja = new JSONArray();
                                for (int j = 0; j < writers.length; j++) {
                                    writers[j].writeField(ja, mails[i], 0, false, session.getUserId(), session.getContextId());
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
            final AbstractOXException wrapper = getWrappingOXException(e);
            LOG.error(wrapper.getMessage(), wrapper);
            response.setException(wrapper);
        }
        /*
         * Close response and flush print writer
         */
        jsonWriter.endArray();
        response.setData(jsonWriter.getObject());
        response.setTimestamp(null);
        return response;
    }

    private static final void fillMapByArray(final Map<String, SmartLongArray> idMap, final JSONArray idArray, final int length) throws JSONException {
        String folder = null;
        SmartLongArray sla = null;
        for (int i = 0; i < length; i++) {
            final JSONObject idObject = idArray.getJSONObject(i);
            final String fld = idObject.getString(PARAMETER_FOLDERID);
            if (folder == null || !folder.equals(fld)) {
                folder = fld;
                final SmartLongArray tmp = idMap.get(folder);
                if (tmp == null) {
                    sla = new SmartLongArray(length);
                    idMap.put(folder, sla);
                } else {
                    sla = tmp;
                }
            }
            sla.append(Long.parseLong(idObject.getString(PARAMETER_ID)));
        }
    }

    public void actionPutDeleteMails(final ServerSession session, final JSONWriter writer, final JSONObject jsonObj, final MailServletInterface mi) throws JSONException {
        ResponseWriter.write(actionPutDeleteMails(session, jsonObj.getString(ResponseFields.DATA), ParamContainer.getInstance(
            jsonObj,
            EnumComponent.MAIL), mi), writer);
    }

    private final void actionPutDeleteMails(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
        try {
            ResponseWriter.write(actionPutDeleteMails(getSessionObject(req), getBody(req), ParamContainer.getInstance(
                req,
                EnumComponent.MAIL,
                resp), null), resp.getWriter());
        } catch (final JSONException e) {
            final OXJSONException oxe = new OXJSONException(OXJSONException.Code.JSON_WRITE_ERROR, e, new Object[0]);
            LOG.error(oxe.getMessage(), oxe);
            final Response response = new Response();
            response.setException(oxe);
            try {
                ResponseWriter.write(response, resp.getWriter());
            } catch (final JSONException e1) {
                LOG.error(RESPONSE_ERROR, e1);
                sendError(resp);
            }
        }
    }

    private final Response actionPutDeleteMails(final ServerSession session, final String body, final ParamContainer paramContainer, final MailServletInterface mailInterfaceArg) throws JSONException {
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
            MailServletInterface mailInterface = mailInterfaceArg;
            boolean closeMailInterface = false;
            try {
                if (mailInterface == null) {
                    mailInterface = MailServletInterface.getInstance(session);
                    closeMailInterface = true;
                }
                final int length = jsonIDs.length();
                if (length > 0) {
                    final List<MailPath> l = new ArrayList<MailPath>(length);
                    for (int i = 0; i < length; i++) {
                        final JSONObject obj = jsonIDs.getJSONObject(i);
                        l.add(new MailPath(obj.getString(PARAMETER_FOLDERID), obj.getLong(PARAMETER_ID)));
                    }
                    Collections.sort(l, MailPath.COMPARATOR);
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
            final AbstractOXException wrapper = getWrappingOXException(e);
            LOG.error(wrapper.getMessage(), wrapper);
            response.setException(wrapper);
        }
        /*
         * Close response and flush print writer
         */
        jsonWriter.endArray();
        response.setData(jsonWriter.getObject());
        response.setTimestamp(null);
        return response;
    }

    public void actionPutUpdateMail(final ServerSession session, final JSONWriter writer, final JSONObject jsonObj, final MailServletInterface mailInterface) throws JSONException {
        ResponseWriter.write(actionPutUpdateMail(session, jsonObj.getString(ResponseFields.DATA), ParamContainer.getInstance(
            jsonObj,
            EnumComponent.MAIL), mailInterface), writer);
    }

    private final void actionPutUpdateMail(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
        try {
            ResponseWriter.write(actionPutUpdateMail(getSessionObject(req), getBody(req), ParamContainer.getInstance(
                req,
                EnumComponent.MAIL,
                resp), null), resp.getWriter());
        } catch (final JSONException e) {
            final OXJSONException oxe = new OXJSONException(OXJSONException.Code.JSON_WRITE_ERROR, e, new Object[0]);
            LOG.error(oxe.getMessage(), oxe);
            final Response response = new Response();
            response.setException(oxe);
            try {
                ResponseWriter.write(response, resp.getWriter());
            } catch (final JSONException e1) {
                LOG.error(RESPONSE_ERROR, e1);
                sendError(resp);
            }
        }
    }

    private final Response actionPutUpdateMail(final ServerSession session, final String body, final ParamContainer paramContainer, final MailServletInterface mailIntefaceArg) throws JSONException {
        /*
         * Some variables
         */
        final Response response = new Response();
        final OXJSONWriter jsonWriter = new OXJSONWriter();
        /*
         * Start response
         */
        jsonWriter.object();
        try {
            final long uid = paramContainer.checkIntParam(PARAMETER_ID);
            final String sourceFolder = paramContainer.checkStringParam(PARAMETER_FOLDERID);
            final JSONObject bodyObj = new JSONObject(body);
            final String destFolder = bodyObj.has(FolderFields.FOLDER_ID) && !bodyObj.isNull(FolderFields.FOLDER_ID) ? bodyObj.getString(FolderFields.FOLDER_ID) : null;
            final Integer colorLabel = bodyObj.has(CommonFields.COLORLABEL) && !bodyObj.isNull(CommonFields.COLORLABEL) ? Integer.valueOf(bodyObj.getInt(CommonFields.COLORLABEL)) : null;
            final Integer flagBits = bodyObj.has(MailJSONField.FLAGS.getKey()) && !bodyObj.isNull(MailJSONField.FLAGS.getKey()) ? Integer.valueOf(bodyObj.getInt(MailJSONField.FLAGS.getKey())) : null;
            boolean flagVal = false;
            if (flagBits != null) {
                /*
                 * Look for boolean value
                 */
                flagVal = (bodyObj.has(MailJSONField.VALUE.getKey()) && !bodyObj.isNull(MailJSONField.VALUE.getKey()) ? bodyObj.getBoolean(MailJSONField.VALUE.getKey()) : false);
            }
            MailServletInterface mailInterface = mailIntefaceArg;
            boolean closeMailInterface = false;
            try {
                if (mailInterface == null) {
                    mailInterface = MailServletInterface.getInstance(session);
                    closeMailInterface = true;
                }
                if (destFolder != null) {
                    /*
                     * Perform move operation
                     */
                    final long id = mailInterface.copyMessages(sourceFolder, destFolder, new long[] { uid }, true)[0];
                    jsonWriter.key(FolderChildFields.FOLDER_ID).value(destFolder);
                    jsonWriter.key(DataFields.ID).value(id);
                }
                if (colorLabel != null) {
                    /*
                     * Update color label
                     */
                    mailInterface.updateMessageColorLabel(sourceFolder, new long[] { uid }, colorLabel.intValue());
                    jsonWriter.key(FolderChildFields.FOLDER_ID).value(sourceFolder);
                    jsonWriter.key(DataFields.ID).value(uid);
                }
                if (flagBits != null) {
                    /*
                     * Update system flags which are allowed to be altered by client
                     */
                    mailInterface.updateMessageFlags(sourceFolder, new long[] { uid }, flagBits.intValue(), flagVal);
                    jsonWriter.key(FolderChildFields.FOLDER_ID).value(sourceFolder);
                    jsonWriter.key(DataFields.ID).value(uid);
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
            final AbstractOXException wrapper = getWrappingOXException(e);
            LOG.error(wrapper.getMessage(), wrapper);
            response.setException(wrapper);
        }
        /*
         * Close response and flush print writer
         */
        jsonWriter.endObject();
        response.setData(jsonWriter.getObject());
        response.setTimestamp(null);
        return response;
    }

    public void actionPutNewMail(final ServerSession session, final JSONWriter writer, final JSONObject jsonObj) throws JSONException {
        ResponseWriter.write(actionPutNewMail(session, jsonObj.getString(ResponseFields.DATA), ParamContainer.getInstance(
            jsonObj,
            EnumComponent.MAIL)), writer);
    }

    private final void actionPutNewMail(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
        try {
            ResponseWriter.write(actionPutNewMail(getSessionObject(req), getBody(req), ParamContainer.getInstance(
                req,
                EnumComponent.MAIL,
                resp)), resp.getWriter());
        } catch (final JSONException e) {
            final OXJSONException oxe = new OXJSONException(OXJSONException.Code.JSON_WRITE_ERROR, e, new Object[0]);
            LOG.error(oxe.getMessage(), oxe);
            final Response response = new Response();
            response.setException(oxe);
            try {
                ResponseWriter.write(response, resp.getWriter());
            } catch (final JSONException e1) {
                LOG.error(RESPONSE_ERROR, e1);
                sendError(resp);
            }
        }
    }

    private final Response actionPutNewMail(final ServerSession session, final String body, final ParamContainer paramContainer) {
        /*
         * Some variables
         */
        final Response response = new Response();
        /*
         * Start response
         */
        JSONObject responseData = null;
        try {
            final String src = paramContainer.checkStringParam(PARAMETER_SRC);
            if (!(STR_1.equals(src) || Boolean.parseBoolean(src))) {
                throw new MailException(MailException.Code.MISSING_PARAMETER, PARAMETER_SRC);
            }
            final String folder = paramContainer.getStringParam(PARAMETER_FOLDERID);
            if (body == null || body.length() == 0) {
                throw new MailException(MailException.Code.MISSING_PARAMETER, PARAMETER_DATA);
            }
            final int flags = paramContainer.getIntParam(PARAMETER_FLAGS);
            /*
             * Get rfc822 bytes and create corresponding mail message
             */
            final byte[] rfc822 = body.getBytes("US-ASCII");
            final MailMessage m = MIMEMessageConverter.convertMessage(rfc822);
            /*
             * Check for valid from address
             */
            try {
                final Set<InternetAddress> validAddrs = new HashSet<InternetAddress>(4);
                final UserSettingMail usm = session.getUserSettingMail();
                if (usm.getSendAddr() != null && usm.getSendAddr().length() > 0) {
                    validAddrs.add(new InternetAddress(usm.getSendAddr()));
                }
                final User user = session.getUser();
                validAddrs.add(new InternetAddress(user.getMail()));
                final String[] aliases = user.getAliases();
                for (final String alias : aliases) {
                    validAddrs.add(new InternetAddress(alias));
                }
                final List<InternetAddress> from = Arrays.asList(m.getFrom());
                if (!validAddrs.containsAll(from)) {
                    throw new MailException(
                        MailException.Code.INVALID_SENDER,
                        from.size() == 1 ? from.get(0).toString() : Arrays.toString(m.getFrom()));
                }
            } catch (final AddressException e) {
                throw MIMEMailException.handleMessagingException(e);
            }
            /*
             * Check if "folder" element is present which indicates to save given message as a draft or append to denoted folder
             */
            if (folder == null) {
                /*
                 * Missing "folder" element indicates to send given message
                 */
                final MailTransport transport = MailTransport.getInstance(session);
                try {
                    /*
                     * Send raw message source
                     */
                    final MailMessage sentMail = transport.sendRawMessage(rfc822);
                    if (!session.getUserSettingMail().isNoCopyIntoStandardSentFolder()) {
                        /*
                         * Copy in sent folder allowed
                         */
                        final MailAccess<?, ?> mailAccess = MailAccess.getInstance(session);
                        mailAccess.connect();
                        try {
                            final String sentFullname = MailFolderUtility.prepareMailFolderParam(mailAccess.getFolderStorage().getSentFolder());
                            final long[] uidArr;
                            try {
                                /*
                                 * Append to default "sent" folder
                                 */
                                if (flags != ParamContainer.NOT_FOUND) {
                                    sentMail.setFlags(flags);
                                }
                                uidArr = mailAccess.getMessageStorage().appendMessages(sentFullname, new MailMessage[] { sentMail });
                                try {
                                    /*
                                     * Update cache
                                     */
                                    MailMessageCache.getInstance().removeFolderMessages(
                                        sentFullname,
                                        session.getUserId(),
                                        session.getContext());
                                } catch (final OXCachingException e) {
                                    LOG.error(e.getMessage(), e);
                                }
                            } catch (final MailException e) {
                                if (e.getMessage().indexOf("quota") != -1) {
                                    throw new MailException(MailException.Code.COPY_TO_SENT_FOLDER_FAILED_QUOTA, e, new Object[0]);
                                }
                                throw new MailException(MailException.Code.COPY_TO_SENT_FOLDER_FAILED, e, new Object[0]);
                            }
                            if ((uidArr != null) && (uidArr[0] != -1)) {
                                /*
                                 * Mark appended sent mail as seen
                                 */
                                mailAccess.getMessageStorage().updateMessageFlags(sentFullname, uidArr, MailMessage.FLAG_SEEN, true);
                            }
                            /*
                             * Compose JSON object
                             */
                            responseData = new JSONObject();
                            responseData.put(FolderChildFields.FOLDER_ID, MailFolderUtility.prepareFullname(sentFullname));
                            responseData.put(DataFields.ID, uidArr[0]);
                        } finally {
                            mailAccess.close(true);
                        }
                    }
                } finally {
                    transport.close();
                }
            } else {
                /*
                 * Append message to denoted folder
                 */
                final MailAccess<?, ?> mailAccess = MailAccess.getInstance(session);
                mailAccess.connect();
                try {
                    if (flags != ParamContainer.NOT_FOUND) {
                        m.setFlags(flags);
                    }
                    if (mailAccess.getFolderStorage().getDraftsFolder().equals(folder)) {
                        m.setFlag(MailMessage.FLAG_DRAFT, true);
                    }
                    final long id = mailAccess.getMessageStorage().appendMessages(
                        MailFolderUtility.prepareMailFolderParam(folder),
                        new MailMessage[] { m })[0];
                    responseData = new JSONObject();
                    responseData.put(FolderChildFields.FOLDER_ID, folder);
                    responseData.put(DataFields.ID, id);
                } finally {
                    mailAccess.close(true);
                }
            }
        } catch (final MailException e) {
            LOG.error(e.getMessage(), e);
            response.setException(e);
        } catch (final AbstractOXException e) {
            LOG.error(e.getMessage(), e);
            response.setException(e);
        } catch (final Exception e) {
            final AbstractOXException wrapper = getWrappingOXException(e);
            LOG.error(wrapper.getMessage(), wrapper);
            response.setException(wrapper);
        }
        /*
         * Close response and flush print writer
         */
        response.setData(responseData == null ? JSONObject.NULL : responseData);
        response.setTimestamp(null);
        return response;
    }

    public void actionPutCopyMail(final ServerSession session, final JSONWriter writer, final JSONObject jsonObj, final MailServletInterface mailInterface) throws JSONException {
        ResponseWriter.write(actionPutCopyMail(session, jsonObj.getString(ResponseFields.DATA), ParamContainer.getInstance(
            jsonObj,
            EnumComponent.MAIL), mailInterface), writer);
    }

    private final void actionPutCopyMail(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
        try {
            ResponseWriter.write(actionPutCopyMail(getSessionObject(req), getBody(req), ParamContainer.getInstance(
                req,
                EnumComponent.MAIL,
                resp), null), resp.getWriter());
        } catch (final JSONException e) {
            final OXJSONException oxe = new OXJSONException(OXJSONException.Code.JSON_WRITE_ERROR, e, new Object[0]);
            LOG.error(oxe.getMessage(), oxe);
            final Response response = new Response();
            response.setException(oxe);
            try {
                ResponseWriter.write(response, resp.getWriter());
            } catch (final JSONException e1) {
                LOG.error(RESPONSE_ERROR, e1);
                sendError(resp);
            }
        }
    }

    private final Response actionPutCopyMail(final ServerSession session, final String body, final ParamContainer paramContainer, final MailServletInterface mailInterfaceArg) throws JSONException {
        /*
         * Some variables
         */
        final Response response = new Response();
        final OXJSONWriter jsonWriter = new OXJSONWriter();
        /*
         * Start response
         */
        jsonWriter.object();
        try {
            final long uid = paramContainer.checkIntParam(PARAMETER_ID);
            final String sourceFolder = paramContainer.checkStringParam(PARAMETER_FOLDERID);
            final String destFolder = new JSONObject(body).getString(FolderFields.FOLDER_ID);
            MailServletInterface mailInterface = mailInterfaceArg;
            boolean closeMailInterface = false;
            try {
                if (mailInterface == null) {
                    mailInterface = MailServletInterface.getInstance(session);
                    closeMailInterface = true;
                }
                final long msgUID = mailInterface.copyMessages(sourceFolder, destFolder, new long[] { uid }, false)[0];
                jsonWriter.key(FolderChildFields.FOLDER_ID).value(destFolder);
                jsonWriter.key(DataFields.ID).value(msgUID);
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
            final AbstractOXException wrapper = getWrappingOXException(e);
            LOG.error(wrapper.getMessage(), wrapper);
            response.setException(wrapper);
        }
        /*
         * Close response and flush print writer
         */
        jsonWriter.endObject();
        response.setData(jsonWriter.getObject());
        response.setTimestamp(null);
        return response;
    }

    public final void actionPutMoveMailMultiple(final ServerSession session, final JSONWriter writer, final long[] mailIDs, final String sourceFolder, final String destFolder, final MailServletInterface mailInteface) throws JSONException {
        actionPutMailMultiple(session, writer, mailIDs, sourceFolder, destFolder, true, mailInteface);
    }

    public final void actionPutCopyMailMultiple(final ServerSession session, final JSONWriter writer, final long[] mailIDs, final String srcFolder, final String destFolder, final MailServletInterface mailInterface) throws JSONException {
        actionPutMailMultiple(session, writer, mailIDs, srcFolder, destFolder, false, mailInterface);
    }

    public final void actionPutMailMultiple(final ServerSession session, final JSONWriter writer, final long[] mailIDs, final String srcFolder, final String destFolder, final boolean move, final MailServletInterface mailInterfaceArg) throws JSONException {
        try {
            MailServletInterface mailInterface = mailInterfaceArg;
            boolean closeMailInterface = false;
            try {
                if (mailInterface == null) {
                    mailInterface = MailServletInterface.getInstance(session);
                    closeMailInterface = true;
                }
                final long[] msgUIDs = mailInterface.copyMessages(srcFolder, destFolder, mailIDs, move);
                if (msgUIDs.length > 0) {
                    final Response response = new Response();
                    for (int k = 0; k < msgUIDs.length; k++) {
                        response.reset();
                        final JSONObject jsonObj = new JSONObject();
                        // DataFields.ID | FolderChildFields.FOLDER_ID
                        jsonObj.put(FolderChildFields.FOLDER_ID, destFolder);
                        jsonObj.put(DataFields.ID, msgUIDs[k]);
                        response.setData(jsonObj);
                        response.setTimestamp(null);
                        ResponseWriter.write(response, writer);
                    }
                } else {
                    final Response response = new Response();
                    response.setData(JSONObject.NULL);
                    response.setTimestamp(null);
                    ResponseWriter.write(response, writer);
                }
            } finally {
                if (closeMailInterface && mailInterface != null) {
                    mailInterface.close(true);
                }
            }
        } catch (final AbstractOXException e) {
            LOG.error(e.getMessage(), e);
            final Response response = new Response();
            for (int k = 0; k < mailIDs.length; k++) {
                response.reset();
                response.setException(e);
                response.setData(JSONObject.NULL);
                response.setTimestamp(null);
                ResponseWriter.write(response, writer);
            }
        } catch (final Exception e) {
            final AbstractOXException wrapper = getWrappingOXException(e);
            LOG.error(wrapper.getMessage(), wrapper);
            final Response response = new Response();
            for (int k = 0; k < mailIDs.length; k++) {
                response.reset();
                response.setException(wrapper);
                response.setData(JSONObject.NULL);
                response.setTimestamp(null);
                ResponseWriter.write(response, writer);
            }
        }
    }

    public void actionPutStoreFlagsMultiple(final ServerSession session, final JSONWriter writer, final long[] mailIDs, final String folder, final int flagsBits, final boolean flagValue, final MailServletInterface mailInterfaceArg) throws JSONException {
        try {
            MailServletInterface mailInterface = mailInterfaceArg;
            boolean closeMailInterface = false;
            try {
                if (mailInterface == null) {
                    mailInterface = MailServletInterface.getInstance(session);
                    closeMailInterface = true;
                }
                mailInterface.updateMessageFlags(folder, mailIDs, flagsBits, flagValue);
                final Response response = new Response();
                for (int i = 0; i < mailIDs.length; i++) {
                    response.reset();
                    final JSONObject jsonObj = new JSONObject();
                    // DataFields.ID | FolderChildFields.FOLDER_ID
                    jsonObj.put(FolderChildFields.FOLDER_ID, folder);
                    jsonObj.put(DataFields.ID, mailIDs[i]);
                    response.setData(jsonObj);
                    response.setTimestamp(null);
                    ResponseWriter.write(response, writer);
                }
            } finally {
                if (closeMailInterface && mailInterface != null) {
                    mailInterface.close(true);
                }
            }
        } catch (final AbstractOXException e) {
            LOG.error(e.getMessage(), e);
            final Response response = new Response();
            for (int i = 0; i < mailIDs.length; i++) {
                response.reset();
                response.setException(e);
                response.setData(JSONObject.NULL);
                response.setTimestamp(null);
                ResponseWriter.write(response, writer);
            }
        } catch (final Exception e) {
            final AbstractOXException wrapper = getWrappingOXException(e);
            LOG.error(wrapper.getMessage(), wrapper);
            final Response response = new Response();
            for (int i = 0; i < mailIDs.length; i++) {
                response.reset();
                response.setException(wrapper);
                response.setData(JSONObject.NULL);
                response.setTimestamp(null);
                ResponseWriter.write(response, writer);
            }
        }
    }

    public void actionPutColorLabelMultiple(final ServerSession session, final JSONWriter writer, final long[] mailIDs, final String folder, final int colorLabel, final MailServletInterface mailInterfaceArg) throws JSONException {
        try {
            MailServletInterface mailInterface = mailInterfaceArg;
            boolean closeMailInterface = false;
            try {
                if (mailInterface == null) {
                    mailInterface = MailServletInterface.getInstance(session);
                    closeMailInterface = true;
                }
                mailInterface.updateMessageColorLabel(folder, mailIDs, colorLabel);
                final Response response = new Response();
                for (int i = 0; i < mailIDs.length; i++) {
                    response.reset();
                    final JSONObject jsonObj = new JSONObject();
                    // DataFields.ID | FolderChildFields.FOLDER_ID
                    jsonObj.put(FolderChildFields.FOLDER_ID, folder);
                    jsonObj.put(DataFields.ID, mailIDs[i]);
                    response.setData(jsonObj);
                    response.setTimestamp(null);
                    ResponseWriter.write(response, writer);
                }
            } finally {
                if (closeMailInterface && mailInterface != null) {
                    mailInterface.close(true);
                }
            }
        } catch (final AbstractOXException e) {
            LOG.error(e.getMessage(), e);
            final Response response = new Response();
            for (int i = 0; i < mailIDs.length; i++) {
                response.reset();
                response.setException(e);
                response.setData(JSONObject.NULL);
                response.setTimestamp(null);
                ResponseWriter.write(response, writer);
            }
        } catch (final Exception e) {
            final AbstractOXException wrapper = getWrappingOXException(e);
            LOG.error(wrapper.getMessage(), wrapper);
            final Response response = new Response();
            for (int i = 0; i < mailIDs.length; i++) {
                response.reset();
                response.setException(wrapper);
                response.setData(JSONObject.NULL);
                response.setTimestamp(null);
                ResponseWriter.write(response, writer);
            }
        }
    }

    public void actionPutAttachment(final ServerSession session, final JSONWriter writer, final JSONObject jsonObj, final MailServletInterface mi) throws JSONException {
        ResponseWriter.write(actionPutAttachment(session, jsonObj.getString(ResponseFields.DATA), ParamContainer.getInstance(
            jsonObj,
            EnumComponent.MAIL), mi), writer);
    }

    private final void actionPutAttachment(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
        try {
            ResponseWriter.write(actionPutAttachment(getSessionObject(req), getBody(req), ParamContainer.getInstance(
                req,
                EnumComponent.MAIL,
                resp), null), resp.getWriter());
        } catch (final JSONException e) {
            final OXJSONException oxe = new OXJSONException(OXJSONException.Code.JSON_WRITE_ERROR, e, new Object[0]);
            LOG.error(oxe.getMessage(), oxe);
            final Response response = new Response();
            response.setException(oxe);
            try {
                ResponseWriter.write(response, resp.getWriter());
            } catch (final JSONException e1) {
                LOG.error(RESPONSE_ERROR, e1);
                sendError(resp);
            }
        }
    }

    private final Response actionPutAttachment(final ServerSession session, final String body, final ParamContainer paramContainer, final MailServletInterface mailInterfaceArg) throws JSONException {
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
            final String folderPath = paramContainer.checkStringParam(PARAMETER_FOLDERID);
            final long uid = Long.parseLong(paramContainer.checkStringParam(PARAMETER_ID));
            final String sequenceId = paramContainer.checkStringParam(PARAMETER_MAILATTCHMENT);
            final String destFolderIdentifier = paramContainer.checkStringParam(PARAMETER_DESTINATION_FOLDER);
            MailServletInterface mailInterface = mailInterfaceArg;
            boolean closeMailInterface = false;
            final InfostoreFacade db = Infostore.FACADE;
            boolean performRollback = false;
            try {
                final Context ctx = session.getContext();
                if (!session.getUserConfiguration().hasInfostore()) {
                    throw new OXPermissionException(new MailException(MailException.Code.NO_MAIL_ACCESS));
                }
                if (mailInterface == null) {
                    mailInterface = MailServletInterface.getInstance(session);
                    closeMailInterface = true;
                }
                final MailPart mailPart = mailInterface.getMessageAttachment(folderPath, uid, sequenceId, false);
                if (mailPart == null) {
                    throw new MailException(MailException.Code.NO_ATTACHMENT_FOUND, sequenceId);
                }
                final int destFolderID = Integer.parseInt(destFolderIdentifier);
                {
                    final FolderObject folderObj = new OXFolderAccess(ctx).getFolderObject(destFolderID);
                    final EffectivePermission p = folderObj.getEffectiveUserPermission(session.getUserId(), session.getUserConfiguration());
                    if (!p.isFolderVisible()) {
                        throw new OXFolderException(
                            FolderCode.NOT_VISIBLE,
                            getFolderName(folderObj),
                            getUserName(session),
                            Integer.valueOf(ctx.getContextId()));
                    }
                    if (!p.canWriteOwnObjects()) {
                        throw new OXFolderException(
                            FolderCode.NO_WRITE_PERMISSION,
                            getUserName(session),
                            getFolderName(folderObj),
                            Integer.valueOf(ctx.getContextId()));
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
                 * Since file's size given from IMAP server is just an estimation and therefore does not exactly match the file's size a
                 * future file access via webdav can fail because of the size mismatch. Thus set the file size to 0 to make the infostore
                 * measure the size.
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
                performRollback = true;
                db.saveDocument(docMetaData, mailPart.getInputStream(), System.currentTimeMillis(), new ServerSessionAdapter(session, ctx));
                db.commit();
            } catch (final Exception e) {
                if (performRollback) {
                    db.rollback();
                }
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
            final AbstractOXException wrapper = getWrappingOXException(e);
            LOG.error(wrapper.getMessage(), wrapper);
            response.setException(wrapper);
        }
        /*
         * Close response and flush print writer
         */
        jsonWriter.endArray();
        response.setData(jsonWriter.getObject());
        response.setTimestamp(null);
        return response;
    }

    public void actionPutReceiptAck(final ServerSession session, final JSONWriter writer, final JSONObject jsonObj, final MailServletInterface mi) throws JSONException {
        ResponseWriter.write(actionPutReceiptAck(session, jsonObj.getString(ResponseFields.DATA), ParamContainer.getInstance(
            jsonObj,
            EnumComponent.MAIL), mi), writer);
    }

    private final void actionPutReceiptAck(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
        try {
            ResponseWriter.write(actionPutReceiptAck(getSessionObject(req), getBody(req), ParamContainer.getInstance(
                req,
                EnumComponent.MAIL,
                resp), null), resp.getWriter());
        } catch (final JSONException e) {
            final OXJSONException oxe = new OXJSONException(OXJSONException.Code.JSON_WRITE_ERROR, e, new Object[0]);
            LOG.error(oxe.getMessage(), oxe);
            final Response response = new Response();
            response.setException(oxe);
            try {
                ResponseWriter.write(response, resp.getWriter());
            } catch (final JSONException e1) {
                LOG.error(RESPONSE_ERROR, e1);
                sendError(resp);
            }
        }
    }

    private final Response actionPutReceiptAck(final ServerSession session, final String body, final ParamContainer paramContainer, final MailServletInterface mailInterfaceArg) {
        /*
         * Some variables
         */
        final Response response = new Response();
        /*
         * Start response
         */
        try {
            final JSONObject bodyObj = new JSONObject(body);
            final String folderPath = bodyObj.has(PARAMETER_FOLDERID) ? bodyObj.getString(PARAMETER_FOLDERID) : null;
            if (null == folderPath) {
                throw new MailException(MailException.Code.MISSING_PARAM, PARAMETER_FOLDERID);
            }
            final long uid = bodyObj.has(PARAMETER_ID) ? Long.parseLong(bodyObj.getString(PARAMETER_ID)) : -1L;
            if (-1 == uid) {
                throw new MailException(MailException.Code.MISSING_PARAM, PARAMETER_ID);
            }
            final String fromAddr = bodyObj.has(MailJSONField.FROM.getKey()) && !bodyObj.isNull(MailJSONField.FROM.getKey()) ? bodyObj.getString(MailJSONField.FROM.getKey()) : null;
            MailServletInterface mailInterface = mailInterfaceArg;
            boolean closeMailInterface = false;
            try {
                if (mailInterface == null) {
                    mailInterface = MailServletInterface.getInstance(session);
                    closeMailInterface = true;
                }
                mailInterface.sendReceiptAck(folderPath, uid, fromAddr);
            } finally {
                if (closeMailInterface && mailInterface != null) {
                    mailInterface.close(true);
                }
            }
        } catch (final MailException e) {
            LOG.error(e.getMessage(), e);
            response.setException(e);
        } catch (final Exception e) {
            final AbstractOXException wrapper = getWrappingOXException(e);
            LOG.error(wrapper.getMessage(), wrapper);
            response.setException(wrapper);
        }
        /*
         * Close response and flush print writer
         */
        response.setData(JSONObject.NULL);
        response.setTimestamp(null);
        return response;
    }

    private static String checkStringParam(final HttpServletRequest req, final String paramName) throws OXMandatoryFieldException {
        final String paramVal = req.getParameter(paramName);
        if (paramVal == null || paramVal.length() == 0 || STR_NULL.equals(paramVal)) {
            throw new OXMandatoryFieldException(
                EnumComponent.MAIL,
                MailException.Code.MISSING_PARAM.getCategory(),
                MailException.Code.MISSING_PARAM.getNumber(),
                null,
                paramName);
        }
        return paramVal;
    }

    /*
     * (non-Javadoc)
     * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest , javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        final ServerSession session = getSessionObject(req);
        /*
         * The magic spell to disable caching
         */
        Tools.disableCaching(resp);
        final String actionStr = req.getParameter(PARAMETER_ACTION);
        try {
            final MailServletInterface mailInterface = MailServletInterface.getInstance(session);
            try {
                /*
                 * Set response headers according to html spec
                 */
                resp.setContentType(MIME_TEXT_HTML_CHARSET_UTF_8);
                /*
                 * Append UploadListener instances
                 */
                final Collection<UploadListener> listeners = new ArrayList<UploadListener>(1);
                listeners.add(this);
                /*
                 * Create and fire upload event
                 */
                final UploadEvent uploadEvent = processUpload(req);
                uploadEvent.setParameter(UPLOAD_PARAM_MAILINTERFACE, mailInterface);
                uploadEvent.setParameter(UPLOAD_PARAM_WRITER, resp.getWriter());
                uploadEvent.setParameter(UPLOAD_PARAM_SESSION, session);
                uploadEvent.setParameter(PARAMETER_ACTION, actionStr);
                fireUploadEvent(uploadEvent, listeners);
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
                responseObj = ResponseWriter.getJSON(response);
            } catch (final JSONException e1) {
                LOG.error(e1.getMessage(), e1);
            }
            throw new UploadServletException(resp, JS_FRAGMENT.replaceFirst(
                JS_FRAGMENT_JSON,
                responseObj == null ? STR_NULL : Matcher.quoteReplacement(responseObj.toString())).replaceFirst(
                JS_FRAGMENT_ACTION,
                e.getAction() == null ? STR_NULL : e.getAction()), e.getMessage(), e);
        } catch (final AbstractOXException e) {
            LOG.error(e.getMessage(), e);
            JSONObject responseObj = null;
            try {
                final Response response = new Response();
                response.setException(e);
                responseObj = ResponseWriter.getJSON(response);
            } catch (final JSONException e1) {
                LOG.error(e1.getMessage(), e1);
            }
            throw new UploadServletException(resp, JS_FRAGMENT.replaceFirst(
                JS_FRAGMENT_JSON,
                responseObj == null ? STR_NULL : Matcher.quoteReplacement(responseObj.toString())).replaceFirst(
                JS_FRAGMENT_ACTION,
                actionStr == null ? STR_NULL : actionStr), e.getMessage(), e);
        } catch (final Exception e) {
            final AbstractOXException wrapper = getWrappingOXException(e);
            LOG.error(wrapper.getMessage(), wrapper);
            JSONObject responseObj = null;
            try {
                final Response response = new Response();
                response.setException(wrapper);
                responseObj = ResponseWriter.getJSON(response);
            } catch (final JSONException e1) {
                LOG.error(e1.getMessage(), e1);
            }
            throw new UploadServletException(resp, JS_FRAGMENT.replaceFirst(
                JS_FRAGMENT_JSON,
                responseObj == null ? STR_NULL : Matcher.quoteReplacement(responseObj.toString())).replaceFirst(
                JS_FRAGMENT_ACTION,
                actionStr == null ? STR_NULL : actionStr), wrapper.getMessage(), wrapper);
        }
    }

    protected boolean sendMessage(final HttpServletRequest req) {
        return req.getParameter(PARAMETER_ACTION) != null && req.getParameter(PARAMETER_ACTION).equalsIgnoreCase(ACTION_SEND);
    }

    protected boolean appendMessage(final HttpServletRequest req) {
        return req.getParameter(PARAMETER_ACTION) != null && req.getParameter(PARAMETER_ACTION).equalsIgnoreCase(ACTION_APPEND);
    }

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
                        final JSONObject jsonMailObj;
                        {
                            final String json0 = uploadEvent.getFormField(UPLOAD_FORMFIELD_MAIL);
                            if (json0 == null || json0.trim().length() == 0) {
                                throw new MailException(MailException.Code.MISSING_PARAM, UPLOAD_FORMFIELD_MAIL);
                            }
                            jsonMailObj = new JSONObject(json0);
                        }
                        /*
                         * Parse
                         */
                        final ServerSession session = (ServerSession) uploadEvent.getParameter(UPLOAD_PARAM_SESSION);
                        final ComposedMailMessage composedMail = MessageParser.parse(jsonMailObj, uploadEvent, session);
                        if ((composedMail.getFlags() & MailMessage.FLAG_DRAFT) == MailMessage.FLAG_DRAFT) {
                            /*
                             * ... and save draft
                             */
                            msgIdentifier = ((MailServletInterface) uploadEvent.getParameter(UPLOAD_PARAM_MAILINTERFACE)).saveDraft(
                                composedMail,
                                false);
                        } else {
                            /*
                             * ... and send message
                             */
                            final ComposeType sendType = jsonMailObj.has(PARAMETER_SEND_TYPE) && !jsonMailObj.isNull(PARAMETER_SEND_TYPE) ? ComposeType.getType(jsonMailObj.getInt(PARAMETER_SEND_TYPE)) : ComposeType.NEW;
                            msgIdentifier = ((MailServletInterface) uploadEvent.getParameter(UPLOAD_PARAM_MAILINTERFACE)).sendMessage(
                                composedMail,
                                sendType);
                            /*
                             * Trigger contact collector
                             */
                            triggerContactCollector(session, composedMail);
                        }
                    }
                    if (msgIdentifier == null) {
                        throw new MailException(MailException.Code.SEND_FAILED_UNKNOWN);
                    }
                    /*
                     * Create JSON response object
                     */
                    final Response response = new Response();
                    response.setData(msgIdentifier);
                    final String jsResponse = JS_FRAGMENT.replaceFirst(
                        JS_FRAGMENT_JSON,
                        Matcher.quoteReplacement(ResponseWriter.getJSON(response).toString())).replaceFirst(JS_FRAGMENT_ACTION, actionStr);
                    writer.write(jsResponse);
                    writer.flush();
                    return true;
                } else if (uploadEvent.getAction().equals(ACTION_EDIT)) {
                    /*
                     * Edit draft
                     */
                    String msgIdentifier = null;
                    {
                        final JSONObject jsonMailObj = new JSONObject(uploadEvent.getFormField(UPLOAD_FORMFIELD_MAIL));
                        /*
                         * Parse
                         */
                        final ComposedMailMessage composedMail = MessageParser.parse(
                            jsonMailObj,
                            uploadEvent,
                            (Session) uploadEvent.getParameter(UPLOAD_PARAM_SESSION));
                        if ((composedMail.getFlags() & MailMessage.FLAG_DRAFT) == MailMessage.FLAG_DRAFT && (composedMail.getMsgref() != null)) {
                            /*
                             * ... and edit draft
                             */
                            msgIdentifier = ((MailServletInterface) uploadEvent.getParameter(UPLOAD_PARAM_MAILINTERFACE)).saveDraft(
                                composedMail,
                                false);
                        } else {
                            throw new MailException(MailException.Code.UNEXPECTED_ERROR, "No new message on action=edit");
                        }
                    }
                    if (msgIdentifier == null) {
                        throw new MailException(MailException.Code.SEND_FAILED_UNKNOWN);
                    }
                    /*
                     * Create JSON response object
                     */
                    final Response response = new Response();
                    response.setData(msgIdentifier);
                    final String jsResponse = JS_FRAGMENT.replaceFirst(
                        JS_FRAGMENT_JSON,
                        Matcher.quoteReplacement(ResponseWriter.getJSON(response).toString())).replaceFirst(JS_FRAGMENT_ACTION, actionStr);
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
                final String jsResponse = JS_FRAGMENT.replaceFirst(
                    JS_FRAGMENT_JSON,
                    Matcher.quoteReplacement(ResponseWriter.getJSON(response).toString())).replaceFirst(JS_FRAGMENT_ACTION, actionStr);
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
    protected boolean hasModulePermission(final ServerSession session) {
        return session.getUserConfiguration().hasWebMail();
    }

    private static class SmartLongArray implements Cloneable {

        /**
         * Pointer to keep track of position in the array
         */
        private int pointer;

        private long[] array;

        private final int growthSize;

        public SmartLongArray() {
            this(1024);
        }

        public SmartLongArray(final int initialSize) {
            this(initialSize, (initialSize >> 2));
        }

        public SmartLongArray(final int initialSize, final int growthSize) {
            this.growthSize = growthSize;
            array = new long[initialSize];
        }

        public void reset() {
            pointer = 0;
        }

        public int size() {
            return pointer;
        }

        public SmartLongArray append(final long l) {
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
            final long[] trimmedArray = new long[pointer];
            System.arraycopy(array, 0, trimmedArray, 0, trimmedArray.length);
            return trimmedArray;
        }

        @Override
        public String toString() {
            return Arrays.toString(toArray());
        }

        @Override
        public Object clone() {
            SmartLongArray clone;
            try {
                clone = (SmartLongArray) super.clone();
                clone.array = new long[this.array.length];
                System.arraycopy(array, 0, clone.array, 0, array.length);
                return clone;
            } catch (final CloneNotSupportedException e) {
                throw new InternalError(e.getMessage());
            }
        }
    }

}
