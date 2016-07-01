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
 *    trademarks of the OX Software GmbH group of companies.
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
 *     Copyright (C) 2016-2020 OX Software GmbH
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

import static com.openexchange.tools.Collections.newHashMap;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.idn.IDNA;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONValue;
import org.json.JSONWriter;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.fields.CommonFields;
import com.openexchange.ajax.fields.DataFields;
import com.openexchange.ajax.fields.FolderChildFields;
import com.openexchange.ajax.fields.ResponseFields;
import com.openexchange.ajax.fileholder.ByteArrayRandomAccess;
import com.openexchange.ajax.fileholder.InputStreamReadable;
import com.openexchange.ajax.fileholder.Readable;
import com.openexchange.ajax.helper.BrowserDetector;
import com.openexchange.ajax.helper.DownloadUtility;
import com.openexchange.ajax.helper.DownloadUtility.CheckedDownload;
import com.openexchange.ajax.helper.ParamContainer;
import com.openexchange.ajax.parser.SearchTermParser;
import com.openexchange.ajax.requesthandler.AJAXRequestDataTools;
import com.openexchange.ajax.writer.ResponseWriter;
import com.openexchange.configuration.ServerConfig;
import com.openexchange.configuration.ServerConfig.Property;
import com.openexchange.contact.internal.VCardUtil;
import com.openexchange.contactcollector.ContactCollectorService;
import com.openexchange.data.conversion.ical.internal.ICalUtil;
import com.openexchange.exception.Category;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.File.Field;
import com.openexchange.file.storage.composition.IDBasedFileAccess;
import com.openexchange.file.storage.composition.IDBasedFileAccessFactory;
import com.openexchange.file.storage.parse.FileMetadataParserService;
import com.openexchange.filemanagement.ManagedFile;
import com.openexchange.groupware.container.CommonObject;
import com.openexchange.groupware.i18n.MailStrings;
import com.openexchange.groupware.importexport.MailImportResult;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.upload.impl.UploadEvent;
import com.openexchange.html.HtmlService;
import com.openexchange.i18n.tools.StringHelper;
import com.openexchange.java.CharsetDetector;
import com.openexchange.java.Charsets;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
import com.openexchange.json.OXJSONWriter;
import com.openexchange.mail.FullnameArgument;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.MailField;
import com.openexchange.mail.MailJSONField;
import com.openexchange.mail.MailListField;
import com.openexchange.mail.MailPath;
import com.openexchange.mail.MailServletInterface;
import com.openexchange.mail.MailSortField;
import com.openexchange.mail.OrderDirection;
import com.openexchange.mail.api.IMailFolderStorage;
import com.openexchange.mail.api.IMailMessageStorage;
import com.openexchange.mail.api.IMailMessageStorageExt;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.attachment.AttachmentToken;
import com.openexchange.mail.attachment.AttachmentTokenConstants;
import com.openexchange.mail.attachment.AttachmentTokenService;
import com.openexchange.mail.cache.MailMessageCache;
import com.openexchange.mail.config.MailProperties;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.dataobjects.compose.ComposeType;
import com.openexchange.mail.dataobjects.compose.ComposedMailMessage;
import com.openexchange.mail.dataobjects.compose.ContentAwareComposedMailMessage;
import com.openexchange.mail.json.converters.MailConverter;
import com.openexchange.mail.json.parser.MessageParser;
import com.openexchange.mail.json.writer.MessageWriter;
import com.openexchange.mail.json.writer.MessageWriter.MailFieldWriter;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.mail.mime.ManagedMimeMessage;
import com.openexchange.mail.mime.MessageHeaders;
import com.openexchange.mail.mime.MimeDefaultSession;
import com.openexchange.mail.mime.MimeFilter;
import com.openexchange.mail.mime.MimeMailException;
import com.openexchange.mail.mime.MimeMailExceptionCode;
import com.openexchange.mail.mime.MimeTypes;
import com.openexchange.mail.mime.QuotedInternetAddress;
import com.openexchange.mail.mime.converters.MimeMessageConverter;
import com.openexchange.mail.mime.filler.MimeMessageFiller;
import com.openexchange.mail.structure.parser.MIMEStructureParser;
import com.openexchange.mail.transport.MailTransport;
import com.openexchange.mail.usersetting.UserSettingMail;
import com.openexchange.mail.utils.AddressUtility;
import com.openexchange.mail.utils.DisplayMode;
import com.openexchange.mail.utils.MailFolderUtility;
import com.openexchange.mail.utils.MessageUtility;
import com.openexchange.mail.utils.MsisdnUtility;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.mailaccount.MailAccountExceptionCodes;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.preferences.ServerUserSetting;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.threadpool.AbstractTask;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.threadpool.ThreadRenamer;
import com.openexchange.tools.TimeZoneUtils;
import com.openexchange.tools.encoding.Helper;
import com.openexchange.tools.encoding.URLCoder;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.servlet.OXJSONExceptionCodes;
import com.openexchange.tools.servlet.http.Tools;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.stream.UnsynchronizedByteArrayOutputStream;

/**
 * {@link Mail} - The servlet to handle mail requests.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class Mail extends PermissionServlet {

    private static final transient org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(Mail.class);

    private static final long ZERO = 0L;

    private static final String MIME_TEXT_HTML_CHARSET_UTF_8 = "text/html; charset=UTF-8";

    private static final String MIME_TEXT_PLAIN = "text/plain";

    private static final String MIME_TEXT_HTML = "text/htm";

    private static final String STR_CONTENT_DISPOSITION = "Content-disposition";

    private static final String STR_USER_AGENT = "user-agent";

    private static final String STR_DELIM = ": ";

    private static final String STR_CRLF = "\r\n";

    private static final String STR_THREAD = "thread";

    private static final long serialVersionUID = 1980226522220313667L;

    /**
     * Generates a wrapping {@link AbstractOXException} for specified exception.
     *
     * @param cause The exception to wrap
     * @return The wrapping {@link AbstractOXException}
     */
    protected static final OXException getWrappingOXException(final Exception cause) {
        final String message = cause.getMessage();
        final String lineSeparator = System.getProperty("line.separator");
        LOG.warn(
            "An unexpected exception occurred, which is going to be wrapped for proper display.{}For safety reason its original content is displayed here.{}{}",
            lineSeparator,
            lineSeparator,
            (null == message ? "[Not available]" : message),
            cause);
        return MailExceptionCode.UNEXPECTED_ERROR.create(cause, null == message ? "[Not available]" : message);
    }

    private static void appendStackTrace(final StackTraceElement[] trace, final StringBuilder sb, final String lineSeparator) {
        if (null == trace) {
            sb.append("<missing stack trace>").append(lineSeparator);
            return;
        }
        for (final StackTraceElement ste : trace) {
            final String className = ste.getClassName();
            if (null != className) {
                sb.append("    at ").append(className).append('.').append(ste.getMethodName());
                if (ste.isNativeMethod()) {
                    sb.append("(Native Method)");
                } else {
                    final String fileName = ste.getFileName();
                    if (null == fileName) {
                        sb.append("(Unknown Source)");
                    } else {
                        final int lineNumber = ste.getLineNumber();
                        sb.append('(').append(fileName);
                        if (lineNumber >= 0) {
                            sb.append(':').append(lineNumber);
                        }
                        sb.append(')');
                    }
                }
                sb.append(lineSeparator);
            }
        }
    }

    private static PrintWriter writerFrom(final HttpServletResponse resp) throws IOException {
        try {
            return resp.getWriter();
        } catch (final IllegalStateException ise) {
            // The getOutputStream() method has already been called for given HttpServletResponse
            return new PrintWriter(resp.getOutputStream());
        }
    }

    private static final String UPLOAD_PARAM_MAILINTERFACE = "msint";

    private static final String UPLOAD_PARAM_WRITER = "writer";

    private static final String UPLOAD_PARAM_SESSION = "sess";

    private static final String UPLOAD_PARAM_HOSTNAME = "hostn";

    private static final String UPLOAD_PARAM_PROTOCOL = "proto";

    private static final String UPLOAD_PARAM_GID = "gid";

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

    /**
     * Parameter to define the maximum desired content length (in bytes) returned for the requested mail.
     */
    public static final String PARAMETER_MAX_SIZE = "max_size";

    public static final String PARAMETER_ALLOW_NESTED_MESSAGES = "allow_nested_messages";

    public static final String PARAMETER_SAVE = "save";

    public static final String PARAMETER_SHOW_SRC = "src";

    public static final String PARAMETER_SHOW_HEADER = "hdr";

    public static final String PARAMETER_EDIT_DRAFT = "edit";

    public static final String PARAMETER_SEND_TYPE = "sendtype";

    public static final String PARAMETER_VIEW = "view";

    public static final String PARAMETER_SRC = "src";

    public static final String PARAMETER_FLAGS = "flags";

    public static final String PARAMETER_UNSEEN = "unseen";

    public static final String PARAMETER_PREPARE = "prepare";

    public static final String PARAMETER_FILTER = "filter";

    public static final String PARAMETER_COL = "col";

    public static final String PARAMETER_MESSAGE_ID = "message_id";

    public static final String PARAMETER_HEADERS = "headers";

    private static final String VIEW_RAW = "raw";

    private static final String VIEW_TEXT = "text";

    private static final String VIEW_TEXT_NO_HTML_ATTACHMENT = "textNoHtmlAttach";

    private static final String VIEW_HTML = "html";

    private static final String VIEW_HTML_BLOCKED_IMAGES = "noimg";


    /**
     * Initializes a new {@link Mail}.
     */
    public Mail() {
        super();
    }

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
            writeError(e.toString(), new JSONWriter(writerFrom(resp)));
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
            LOG.error("doPut", e);
            writeError(e.toString(), new JSONWriter(writerFrom(resp)));
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
        } else if (actionStr.equalsIgnoreCase(ACTION_GET_STRUCTURE)) {
            actionGetStructure(req, resp);
        } else if (actionStr.equalsIgnoreCase(ACTION_MATTACH)) {
            actionGetAttachment(req, resp);
        } else if (actionStr.equalsIgnoreCase("attachmentToken")) {
            actionGetAttachmentToken(req, resp);
        } else if (actionStr.equalsIgnoreCase(ACTION_ZIP_MATTACH)) {
            actionGetMultipleAttachments(req, resp);
        } else if (actionStr.equalsIgnoreCase(ACTION_ZIP_MESSAGES)) {
            actionGetMultipleMessages(req, resp);
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
        } else if (actionStr.equalsIgnoreCase("transport")) {
            actionPutTransportMail(req, resp);
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

    public void actionGetUpdates(final ServerSession session, final JSONWriter writer, final JSONObject requestObj, final MailServletInterface mi) throws JSONException {
        ResponseWriter.write(
            actionGetUpdates(session, ParamContainer.getInstance(requestObj), mi),
            writer,
            session.getUser().getLocale());
    }

    private final void actionGetUpdates(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
        final ServerSession session = getSessionObject(req);
        try {
            ResponseWriter.write(
                actionGetUpdates(session, ParamContainer.getInstance(req, resp), null),
                resp.getWriter(),
                localeFrom(session));
        } catch (final JSONException e) {
            final OXException oxe = OXJSONExceptionCodes.JSON_WRITE_ERROR.create(e, new Object[0]);
            LOG.error("", oxe);
            final Response response = new Response(session);
            response.setException(oxe);
            try {
                ResponseWriter.write(response, resp.getWriter(), localeFrom(session));
            } catch (final JSONException e1) {
                LOG.error(RESPONSE_ERROR, e1);
                sendError(resp);
            }
        }
    }

    private final transient static JSONArray EMPTY_JSON_ARR = new JSONArray();

    private final transient MailFieldWriter WRITER_ID = MessageWriter.getMailFieldWriters(new MailListField[] { MailListField.ID })[0];

    private final Response actionGetUpdates(final ServerSession session, final ParamContainer paramContainer, final MailServletInterface mailInterfaceArg) throws JSONException {
        /*
         * Some variables
         */
        final Response response = new Response(session);
        final OXJSONWriter jsonWriter = new OXJSONWriter();
        jsonWriter.array();
        try {
            final String folderId = paramContainer.checkStringParam(PARAMETER_MAILFOLDER);
            final String ignore = paramContainer.getStringParam(PARAMETER_IGNORE);
            String tmp = paramContainer.getStringParam(Mail.PARAMETER_TIMEZONE);
            final TimeZone timeZone = com.openexchange.java.Strings.isEmpty(tmp) ? null : TimeZoneUtils.getTimeZone(tmp.trim());
            tmp = null;
            boolean bIgnoreDelete = false;
            boolean bIgnoreModified = false;
            if (ignore != null && ignore.indexOf("deleted") != -1) {
                bIgnoreDelete = true;
            }
            if (ignore != null && ignore.indexOf("changed") != -1) {
                bIgnoreModified = true;
            }
            if (!bIgnoreModified || !bIgnoreDelete) {
                final int[] columns = paramContainer.checkIntArrayParam(PARAMETER_COLUMNS);
                final int userId = session.getUserId();
                final int contextId = session.getContextId();
                MailServletInterface mailInterface = mailInterfaceArg;
                boolean closeMailInterface = false;
                try {
                    if (mailInterface == null) {
                        mailInterface = MailServletInterface.getInstance(session);
                        closeMailInterface = true;
                    }
                    if (!bIgnoreModified) {
                        final MailMessage[] modified = mailInterface.getUpdatedMessages(folderId, columns);
                        final MailFieldWriter[] writers = MessageWriter.getMailFieldWriters(MailListField.getFields(columns));
                        for (final MailMessage mail : modified) {
                            if (mail != null) {
                                final JSONArray ja = new JSONArray();
                                for (final MailFieldWriter writer : writers) {
                                    writer.writeField(ja, mail, 0, false, mailInterface.getAccountID(), userId, contextId, timeZone);
                                }
                                jsonWriter.value(ja);
                            }
                        }
                    }
                    if (!bIgnoreDelete) {
                        final MailMessage[] deleted = mailInterface.getDeletedMessages(folderId, columns);
                        for (final MailMessage mail : deleted) {
                            final JSONArray ja = new JSONArray();
                            WRITER_ID.writeField(ja, mail, 0, false, mailInterface.getAccountID(), userId, contextId, timeZone);
                            jsonWriter.value(ja);
                        }
                    }
                } finally {
                    if (closeMailInterface && mailInterface != null) {
                        mailInterface.close(true);
                    }
                }
            }
            // final FullnameArgument fa = MailFolderUtility.prepareMailFolderParam(folderId);
            /*
             * Clean session caches
             */
            // SessionMailCache.getInstance(session, fa.getAccountId()).clear();
            /*
             * Clean message cache
             */
            // MailMessageCache.getInstance().removeFolderMessages(fa.getAccountId(), fa.getFullname(), session.getUserId(),
            // session.getContext());
        } catch (final OXException e) {
            LOG.error("", e);
            response.setException(e);
        } catch (final Exception e) {
            final OXException wrapper = getWrappingOXException(e);
            LOG.error("", wrapper);
            response.setException(wrapper);
        }
        jsonWriter.endArray();
        /*
         * Close response and flush print writer
         */
        response.setData(jsonWriter.getObject());
        response.setTimestamp(null);
        return response;
    }

    public void actionGetMailCount(final Session session, final JSONWriter writer, final JSONObject requestObj, final MailServletInterface mi) throws JSONException {
        ResponseWriter.write(
            actionGetMailCount(session, ParamContainer.getInstance(requestObj), mi),
            writer,
            localeFrom(session));
    }

    private final void actionGetMailCount(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
        final ServerSession session = getSessionObject(req);
        try {
            ResponseWriter.write(
                actionGetMailCount(session, ParamContainer.getInstance(req, resp), null),
                resp.getWriter(),
                localeFrom(session));
        } catch (final JSONException e) {
            final OXException oxe = OXJSONExceptionCodes.JSON_WRITE_ERROR.create(e, new Object[0]);
            LOG.error("", oxe);
            final Response response = new Response(session);
            response.setException(oxe);
            try {
                ResponseWriter.write(response, resp.getWriter(), localeFrom(session));
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
        final Response response;
        try {
            response = new Response(session);
        } catch (final OXException e) {
            return new Response().setException(e);
        }
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
        } catch (final OXException e) {
            LOG.error("", e);
            if (!e.getCategory().equals(Category.CATEGORY_PERMISSION_DENIED)) {
                response.setException(e);
            }
        } catch (final Exception e) {
            final OXException wrapper = getWrappingOXException(e);
            LOG.error("", wrapper);
            response.setException(wrapper);
        }
        /*
         * Close response and flush print writer
         */
        response.setData(data);
        response.setTimestamp(null);
        return response;
    }

    public void actionGetAllMails(final ServerSession session, final JSONWriter writer, final JSONObject requestObj, final MailServletInterface mi) throws OXException, JSONException {
        ResponseWriter.write(
            actionGetAllMails(session, ParamContainer.getInstance(requestObj), mi),
            writer,
            session.getUser().getLocale());
    }

    private final void actionGetAllMails(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
        final ServerSession session = getSessionObject(req);
        try {
            ResponseWriter.write(
                actionGetAllMails(session, ParamContainer.getInstance(req, resp), null),
                resp.getWriter(),
                localeFrom(session));
        } catch (final JSONException e) {
            final OXException oxe = OXJSONExceptionCodes.JSON_WRITE_ERROR.create(e, new Object[0]);
            LOG.error("", oxe);
            final Response response = new Response(session);
            response.setException(oxe);
            try {
                ResponseWriter.write(response, resp.getWriter(), localeFrom(session));
            } catch (final JSONException e1) {
                LOG.error(RESPONSE_ERROR, e1);
                sendError(resp);
            }
        } catch (final OXException e) {
            LOG.error("", e);
            final Response response = new Response(session);
            response.setException(e);
            try {
                ResponseWriter.write(response, resp.getWriter(), localeFrom(session));
            } catch (final JSONException e1) {
                LOG.error(RESPONSE_ERROR, e1);
                sendError(resp);
            }
        }
    }

    private static final String STR_ASC = "asc";

    private static final String STR_DESC = "desc";

    private final Response actionGetAllMails(final ServerSession session, final ParamContainer paramContainer, final MailServletInterface mailInterfaceArg) throws JSONException, OXException {
        /*
         * Some variables
         */
        final Response response = new Response(session);
        final OXJSONWriter jsonWriter = new OXJSONWriter();
        /*
         * Start response
         */
        jsonWriter.array();
        SearchIterator<MailMessage> it = null;
        try {
            /*
             * Read in parameters
             */
            final String folderId = paramContainer.checkStringParam(PARAMETER_MAILFOLDER);
            final int[] columns = paramContainer.checkIntArrayParam(PARAMETER_COLUMNS);
            final String sort = paramContainer.getStringParam(PARAMETER_SORT);
            final String order = paramContainer.getStringParam(PARAMETER_ORDER);
            if (sort != null && order == null) {
                throw MailExceptionCode.MISSING_PARAM.create(PARAMETER_ORDER);
            }
            String tmp = paramContainer.getStringParam(Mail.PARAMETER_TIMEZONE);
            final TimeZone timeZone = com.openexchange.java.Strings.isEmpty(tmp) ? null : TimeZoneUtils.getTimeZone(tmp.trim());
            tmp = null;

            final int[] fromToIndices;
            {
                final int leftHandLimit = paramContainer.getIntParam(LEFT_HAND_LIMIT);
                final int rightHandLimit = paramContainer.getIntParam(RIGHT_HAND_LIMIT);
                if (leftHandLimit == ParamContainer.NOT_FOUND || rightHandLimit == ParamContainer.NOT_FOUND) {
                    fromToIndices = null;
                } else {
                    fromToIndices = new int[] { leftHandLimit, rightHandLimit };
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
                final MailFieldWriter[] writers = MessageWriter.getMailFieldWriters(MailListField.getFields(columns));
                final int userId = session.getUserId();
                final int contextId = session.getContextId();
                int orderDir = OrderDirection.ASC.getOrder();
                if (order != null) {
                    if (order.equalsIgnoreCase(STR_ASC)) {
                        orderDir = OrderDirection.ASC.getOrder();
                    } else if (order.equalsIgnoreCase(STR_DESC)) {
                        orderDir = OrderDirection.DESC.getOrder();
                    } else {
                        throw MailExceptionCode.INVALID_INT_VALUE.create(PARAMETER_ORDER);
                    }
                }
                /*
                 * Check for thread-sort
                 */
                if ((STR_THREAD.equalsIgnoreCase(sort))) {
                    it = mailInterface.getAllThreadedMessages(
                        folderId,
                        MailSortField.RECEIVED_DATE.getField(),
                        orderDir,
                        columns,
                        fromToIndices);
                    final int size = it.size();
                    for (int i = 0; i < size; i++) {
                        final MailMessage mail = it.next();
                        if (mail != null && !mail.isDeleted()) {
                            final JSONArray ja = new JSONArray();
                            for (final MailFieldWriter writer : writers) {
                                writer.writeField(
                                    ja,
                                    mail,
                                    mail.getThreadLevel(),
                                    false,
                                    mailInterface.getAccountID(),
                                    userId,
                                    contextId,
                                    timeZone);
                            }
                            jsonWriter.value(ja);
                        }
                    }
                } else {
                    final int sortCol = sort == null ? MailListField.RECEIVED_DATE.getField() : Integer.parseInt(sort);
                    /*
                     * Get iterator
                     */
                    it = mailInterface.getAllMessages(folderId, sortCol, orderDir, columns, fromToIndices, false);
                    final int size = it.size();
                    for (int i = 0; i < size; i++) {
                        final MailMessage mail = it.next();
                        if (mail != null && !mail.isDeleted()) {
                            final JSONArray ja = new JSONArray();
                            for (final MailFieldWriter writer : writers) {
                                writer.writeField(ja, mail, 0, false, mailInterface.getAccountID(), userId, contextId, timeZone);
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
        } catch (final OXException e) {
            LOG.error("", e);
            response.setException(e);
        } catch (final Exception e) {
            final OXException wrapper = getWrappingOXException(e);
            LOG.error("", wrapper);
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
        ResponseWriter.write(
            actionGetReply(session, reply2all, ParamContainer.getInstance(jo), mailInterface),
            writer,
            session.getUser().getLocale());
    }

    private final void actionGetReply(final HttpServletRequest req, final HttpServletResponse resp, final boolean reply2all) throws IOException {
        final ServerSession session = getSessionObject(req);
        try {
            ResponseWriter.write(
                actionGetReply(session, reply2all, ParamContainer.getInstance(req, resp), null),
                resp.getWriter(),
                localeFrom(session));
        } catch (final JSONException e) {
            final OXException oxe = OXJSONExceptionCodes.JSON_WRITE_ERROR.create(e, new Object[0]);
            LOG.error("", oxe);
            final Response response = new Response(session);
            response.setException(oxe);
            try {
                ResponseWriter.write(response, resp.getWriter(), localeFrom(session));
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
        final Response response = new Response(session);
        Object data = JSONObject.NULL;
        final List<OXException> warnings = new ArrayList<OXException>(2);
        /*
         * Start response
         */
        try {
            /*
             * Read in parameters
             */
            final String folderPath = paramContainer.checkStringParam(PARAMETER_FOLDERID);
            final String uid = paramContainer.checkStringParam(PARAMETER_ID);
            final String view = paramContainer.getStringParam(PARAMETER_VIEW);
            final UserSettingMail usmNoSave = session.getUserSettingMail().clone();
            /*
             * Deny saving for this request-specific settings
             */
            usmNoSave.setNoSave(true);
            /*
             * Overwrite settings with request's parameters
             */
            final DisplayMode displayMode = detectDisplayMode(true, view, usmNoSave);
            /*
             * Get reply message
             */
            MailServletInterface mailInterface = mailInterfaceArg;
            boolean closeMailInterface = false;
            try {
                if (mailInterface == null) {
                    mailInterface = MailServletInterface.getInstance(session);
                    closeMailInterface = true;
                }
                data = MessageWriter.writeMailMessage(
                    mailInterface.getAccountID(),
                    mailInterface.getReplyMessageForDisplay(folderPath, uid, reply2all, usmNoSave, false),
                    displayMode,
                    false,
                    session,
                    usmNoSave,
                    warnings,
                    false,
                    -1);
            } finally {
                if (closeMailInterface && mailInterface != null) {
                    mailInterface.close(true);
                }
            }
        } catch (final OXException e) {
            LOG.error("", e);
            response.setException(e);
        } catch (final Exception e) {
            final OXException wrapper = getWrappingOXException(e);
            LOG.error("", wrapper);
            response.setException(wrapper);
        }
        /*
         * Close response and flush print writer
         */
        response.setData(data);
        response.setTimestamp(null);
        if (!warnings.isEmpty()) {
            response.addWarning(warnings.get(0));
        }
        return response;
    }

    public void actionGetForward(final ServerSession session, final JSONWriter writer, final JSONObject requestObj, final MailServletInterface mailInterface) throws JSONException {
        ResponseWriter.write(
            actionGetForward(session, ParamContainer.getInstance(requestObj), mailInterface),
            writer,
            localeFrom(session));
    }

    private final void actionGetForward(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
        final ServerSession session = getSessionObject(req);
        try {
            ResponseWriter.write(
                actionGetForward(session, ParamContainer.getInstance(req, resp), null),
                resp.getWriter(),
                localeFrom(session));
        } catch (final JSONException e) {
            final OXException oxe = OXJSONExceptionCodes.JSON_WRITE_ERROR.create(e, new Object[0]);
            LOG.error("", oxe);
            final Response response = new Response(session);
            response.setException(oxe);
            try {
                ResponseWriter.write(response, resp.getWriter(), localeFrom(session));
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
        final Response response = new Response(session);
        Object data = JSONObject.NULL;
        final List<OXException> warnings = new ArrayList<OXException>(2);
        /*
         * Start response
         */
        try {
            /*
             * Read in parameters
             */
            final String folderPath = paramContainer.checkStringParam(PARAMETER_FOLDERID);
            final String uid = paramContainer.checkStringParam(PARAMETER_ID);
            final String view = paramContainer.getStringParam(PARAMETER_VIEW);
            final UserSettingMail usmNoSave = session.getUserSettingMail().clone();
            boolean setFrom = AJAXRequestDataTools.parseBoolParameter(paramContainer.getStringParam("setFrom"));
            /*
             * Deny saving for this request-specific settings
             */
            usmNoSave.setNoSave(true);
            /*
             * Overwrite settings with request's parameters
             */
            final DisplayMode displayMode = detectDisplayMode(true, view, usmNoSave);
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
                    mailInterface.getAccountID(),
                    mailInterface.getForwardMessageForDisplay(new String[] { folderPath }, new String[] { uid }, usmNoSave, setFrom),
                    displayMode,
                    false,
                    session,
                    usmNoSave,
                    warnings,
                    false,
                    -1);
            } finally {
                if (closeMailInterface && mailInterface != null) {
                    mailInterface.close(true);
                }
            }
        } catch (final OXException e) {
            LOG.error("", e);
            response.setException(e);
        } catch (final Exception e) {
            final OXException wrapper = getWrappingOXException(e);
            LOG.error("", wrapper);
            response.setException(wrapper);
        }
        /*
         * Close response and flush print writer
         */
        response.setData(data);
        response.setTimestamp(null);
        if (!warnings.isEmpty()) {
            response.addWarning(warnings.get(0));
        }
        return response;
    }

    public void actionGetStructure(final ServerSession session, final JSONWriter writer, final JSONObject requestObj, final MailServletInterface mi) throws JSONException {
        final Response response = actionGetStructure(session, ParamContainer.getInstance(requestObj), mi);
        if (null != response) {
            ResponseWriter.write(response, writer, localeFrom(session));
        }
    }

    private final void actionGetStructure(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
        final ServerSession session = getSessionObject(req);
        try {
            final Response response = actionGetStructure(session, ParamContainer.getInstance(req, resp), null);
            if (null != response) {
                ResponseWriter.write(response, resp.getWriter(), localeFrom(session));
            }
        } catch (final JSONException e) {
            final OXException oxe = OXJSONExceptionCodes.JSON_WRITE_ERROR.create(e, new Object[0]);
            LOG.error("", oxe);
            final Response response = new Response(session);
            response.setException(oxe);
            try {
                ResponseWriter.write(response, resp.getWriter(), localeFrom(session));
            } catch (final JSONException e1) {
                LOG.error(RESPONSE_ERROR, e1);
                sendError(resp);
            }
        }
    }

    private final Response actionGetStructure(final ServerSession session, final ParamContainer paramContainer, final MailServletInterface mailInterfaceArg) {
        /*
         * Some variables
         */
        final Response response = new Response(session);
        Object data = JSONObject.NULL;
        /*
         * Start response
         */
        try {
            /*
             * Read in parameters
             */
            final String folderPath = paramContainer.checkStringParam(PARAMETER_FOLDERID);
            // final String uid = paramContainer.checkStringParam(PARAMETER_ID);
            final boolean unseen;
            {
                final String tmp = paramContainer.getStringParam(PARAMETER_UNSEEN);
                unseen = (STR_1.equals(tmp) || Boolean.parseBoolean(tmp));
            }
            final long maxSize;
            {
                final String tmp = paramContainer.getStringParam("max_size");
                if (null == tmp) {
                    maxSize = -1;
                } else {
                    long l = -1;
                    try {
                        l = Long.parseLong(tmp.trim());
                    } catch (final NumberFormatException e) {
                        l = -1;
                    }
                    maxSize = l;
                }
            }
            MailServletInterface mailInterface = mailInterfaceArg;
            boolean closeMailInterface = false;
            try {
                if (mailInterface == null) {
                    mailInterface = MailServletInterface.getInstance(session);
                    closeMailInterface = true;
                }

                final String uid;
                {
                    String tmp2 = paramContainer.getStringParam(PARAMETER_ID);
                    if (null == tmp2) {
                        tmp2 = paramContainer.getStringParam(PARAMETER_MESSAGE_ID);
                        if (null == tmp2) {
                            throw AjaxExceptionCodes.MISSING_PARAMETER.create(PARAMETER_ID);
                        }
                        uid = mailInterface.getMailIDByMessageID(folderPath, tmp2);
                    } else {
                        uid = tmp2;
                    }
                }

                /*
                 * Get message
                 */
                final MailMessage mail = mailInterface.getMessage(folderPath, uid);
                if (mail == null) {
                    throw MailExceptionCode.MAIL_NOT_FOUND.create(uid, folderPath);
                }
                final boolean wasUnseen = (mail.containsPrevSeen() && !mail.isPrevSeen());
                final boolean doUnseen = (unseen && wasUnseen);
                if (doUnseen) {
                    mail.setFlag(MailMessage.FLAG_SEEN, false);
                    final int unreadMsgs = mail.getUnreadMessages();
                    mail.setUnreadMessages(unreadMsgs < 0 ? 0 : unreadMsgs + 1);
                }
                data = MessageWriter.writeStructure(mailInterface.getAccountID(), mail, maxSize);
                if (doUnseen) {
                    /*
                     * Leave mail as unseen
                     */
                    mailInterface.updateMessageFlags(folderPath, new String[] { uid }, MailMessage.FLAG_SEEN, false);
                } else if (wasUnseen) {
                    try {
                        final ServerUserSetting setting = ServerUserSetting.getInstance();
                        final int contextId = session.getContextId();
                        final int userId = session.getUserId();
                        if (setting.isContactCollectOnMailAccess(
                            contextId,
                            userId).booleanValue()) {
                            triggerContactCollector(session, mail, false);
                        }
                        //                        countObjectUse(session, mail);
                    } catch (final OXException e) {
                        LOG.warn("Contact collector could not be triggered.", e);
                    }
                }

            } finally {
                if (closeMailInterface && mailInterface != null) {
                    mailInterface.close(true);
                }
            }

        } catch (final OXException e) {
            LOG.error("", e);
            response.setException(e);
        } catch (final Exception e) {
            final OXException wrapper = getWrappingOXException(e);
            LOG.error("", wrapper);
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
        final Response response = actionGetMessage(session, ParamContainer.getInstance(requestObj), mi);
        if (null != response) {
            ResponseWriter.write(response, writer, localeFrom(session));
        }
    }

    private final void actionGetMessage(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
        final ServerSession session = getSessionObject(req);
        try {
            final Response response = actionGetMessage(session, ParamContainer.getInstance(req, resp), null);
            if (null != response) {
                ResponseWriter.write(response, resp.getWriter(), localeFrom(session));
            }
        } catch (final JSONException e) {
            final OXException oxe = OXJSONExceptionCodes.JSON_WRITE_ERROR.create(e, new Object[0]);
            LOG.error("", oxe);
            final Response response = new Response(session);
            response.setException(oxe);
            try {
                ResponseWriter.write(response, resp.getWriter(), localeFrom(session));
            } catch (final JSONException e1) {
                LOG.error(RESPONSE_ERROR, e1);
                sendError(resp);
            }
        }
    }

    private final Response actionGetMessage(final ServerSession session, final ParamContainer paramContainer, final MailServletInterface mailInterfaceArg) throws JSONException {
        /*
         * Some variables
         */
        final Response response = new Response(session);
        Object data = JSONObject.NULL;
        final List<OXException> warnings = new ArrayList<OXException>(2);
        /*
         * Start response
         */
        boolean errorAsCallback = false;
        try {
            /*
             * Read in parameters
             */
            final String folderPath = paramContainer.checkStringParam(PARAMETER_FOLDERID);
            String tmp = paramContainer.getStringParam(PARAMETER_SAVE);
            final boolean saveToDisk = (tmp != null && tmp.length() > 0 && Integer.parseInt(tmp) > 0);
            tmp = null;
            errorAsCallback = saveToDisk;
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

                String uid;
                {
                    String tmp2 = paramContainer.getStringParam(PARAMETER_ID);
                    if (null == tmp2) {
                        tmp2 = paramContainer.getStringParam(PARAMETER_MESSAGE_ID);
                        if (null == tmp2) {
                            throw AjaxExceptionCodes.MISSING_PARAMETER.create(PARAMETER_ID);
                        }
                        uid = mailInterface.getMailIDByMessageID(folderPath, tmp2);
                    } else {
                        uid = tmp2;
                    }
                }

                MailMessage mail = mailInterface.getMessage(folderPath, uid);
                if (mail == null) {
                    throw MailExceptionCode.MAIL_NOT_FOUND.create(uid, folderPath);
                }

                return actionGetMessage(session, paramContainer, mail, uid, mailInterface);
            } finally {
                if (closeMailInterface && mailInterface != null) {
                    mailInterface.close(true);
                }
            }
        } catch (final OXException e) {
            if (MailExceptionCode.MAIL_NOT_FOUND.equals(e)) {
                LOG.warn(
                    "Requested mail could not be found. Most likely this is caused by concurrent access of multiple clients while one performed a delete on affected mail.",
                    e);
            } else {
                LOG.error("", e);
            }
            response.setException(e);
            if (errorAsCallback) {
                try {
                    final HttpServletResponse resp = paramContainer.getHttpServletResponse();
                    resp.setContentType(MIME_TEXT_HTML_CHARSET_UTF_8);
                    final String jsResponse = substituteJS(ResponseWriter.getJSON(response).toString(), ACTION_GET);
                    final PrintWriter writer = resp.getWriter();
                    writer.write(jsResponse);
                    writer.flush();
                    return null;
                } catch (final Exception exc) {
                    throw new JSONException(exc);
                }
            }
        } catch (final Exception e) {
            final OXException wrapper = getWrappingOXException(e);
            LOG.error("", wrapper);
            response.setException(wrapper);
            if (errorAsCallback) {
                try {
                    final HttpServletResponse resp = paramContainer.getHttpServletResponse();
                    resp.setContentType(MIME_TEXT_HTML_CHARSET_UTF_8);
                    final String jsResponse = substituteJS(ResponseWriter.getJSON(response).toString(), ACTION_GET);
                    final PrintWriter writer = resp.getWriter();
                    writer.write(jsResponse);
                    writer.flush();
                    return null;
                } catch (final Exception exc) {
                    throw new JSONException(exc);
                }
            }
        }
        /*
         * Close response and flush print writer
         */
        response.setData(data);
        response.setTimestamp(null);
        if (!warnings.isEmpty()) {
            response.addWarning(warnings.get(0));
        }
        return response;
    }

    private final Response actionGetMessage(ServerSession session, ParamContainer paramContainer, MailMessage mail, String uid, MailServletInterface mailInterface) throws JSONException {
        /*
         * Some variables
         */
        final Response response = new Response(session);
        Object data = JSONObject.NULL;
        final List<OXException> warnings = new ArrayList<OXException>(2);
        /*
         * Start response
         */
        boolean errorAsCallback = false;
        try {
            /*
             * Read in parameters
             */
            final String folderPath = paramContainer.checkStringParam(PARAMETER_FOLDERID);
            String tmp = paramContainer.getStringParam(PARAMETER_SHOW_SRC);
            final boolean showMessageSource = (STR_1.equals(tmp) || Boolean.parseBoolean(tmp));
            tmp = paramContainer.getStringParam(PARAMETER_SHOW_HEADER);
            final boolean showMessageHeaders = (STR_1.equals(tmp) || Boolean.parseBoolean(tmp));
            tmp = paramContainer.getStringParam(PARAMETER_SAVE);
            final boolean saveToDisk = (tmp != null && tmp.length() > 0 && Integer.parseInt(tmp) > 0);
            tmp = paramContainer.getStringParam(PARAMETER_UNSEEN);
            final boolean unseen = (tmp != null && (STR_1.equals(tmp) || Boolean.parseBoolean(tmp)));
            tmp = paramContainer.getStringParam("ignorable");
            final MimeFilter mimeFilter;
            if (com.openexchange.java.Strings.isEmpty(tmp)) {
                mimeFilter = null;
            } else {
                final String[] strings = SPLIT.split(tmp, 0);
                final int length = strings.length;
                MimeFilter mf;
                if (1 == length && (mf = MimeFilter.filterFor(strings[0])) != null) {
                    mimeFilter = mf;
                } else {
                    final List<String> ignorableContentTypes = new ArrayList<String>(length);
                    for (int i = 0; i < length; i++) {
                        final String cts = strings[i];
                        if ("ics".equalsIgnoreCase(cts)) {
                            ignorableContentTypes.add("text/calendar");
                            ignorableContentTypes.add("application/ics");
                        } else {
                            ignorableContentTypes.add(cts);
                        }
                    }
                    mimeFilter = MimeFilter.filterFor(ignorableContentTypes);
                }
            }
            tmp = null;
            errorAsCallback = saveToDisk;

            if (showMessageSource) {
                final ByteArrayOutputStream baos = new UnsynchronizedByteArrayOutputStream();
                try {
                    mail.writeTo(baos);
                } catch (final OXException e) {
                    if (!MailExceptionCode.NO_CONTENT.equals(e)) {
                        throw e;
                    }
                    LOG.debug("", e);
                    baos.reset();
                }
                // Filter
                if (null != mimeFilter) {
                    MimeMessage mimeMessage = new MimeMessage(
                        MimeDefaultSession.getDefaultSession(),
                        Streams.newByteArrayInputStream(baos.toByteArray()));
                    mimeMessage = mimeFilter.filter(mimeMessage);
                    baos.reset();
                    mimeMessage.writeTo(baos);
                }
                // Proceed
                final boolean wasUnseen = (mail.containsPrevSeen() && !mail.isPrevSeen());
                final boolean doUnseen = (unseen && wasUnseen);
                if (doUnseen) {
                    mail.setFlag(MailMessage.FLAG_SEEN, false);
                    final int unreadMsgs = mail.getUnreadMessages();
                    mail.setUnreadMessages(unreadMsgs < 0 ? 0 : unreadMsgs + 1);
                }
                if (doUnseen) {
                    /*
                     * Leave mail as unseen
                     */
                    mailInterface.updateMessageFlags(folderPath, new String[] { uid }, MailMessage.FLAG_SEEN, false);
                } else if (wasUnseen) {
                    /*
                     * Trigger contact collector
                     */
                    try {
                        final ServerUserSetting setting = ServerUserSetting.getInstance();
                        final int contextId = session.getContextId();
                        final int userId = session.getUserId();
                        if (setting.isContactCollectOnMailAccess(
                            contextId,
                            userId).booleanValue()) {
                            triggerContactCollector(session, mail, false);
                        }
                        //                        countObjectUse(session, mail);
                    } catch (final OXException e) {
                        LOG.warn("Contact collector could not be triggered.", e);
                    }
                }
                if (saveToDisk) {
                    /*
                     * Write message source to output stream...
                     */
                    final ContentType contentType = new ContentType();
                    contentType.setPrimaryType("application");
                    contentType.setSubType("octet-stream");
                    final HttpServletResponse httpResponse = paramContainer.getHttpServletResponse();
                    httpResponse.setContentType(contentType.toString());

                    String subject = mail.getSubject();
                    if (subject == null) { // in case no subject was set
                        subject = StringHelper.valueOf(session.getUser().getLocale()).getString(MailStrings.DEFAULT_SUBJECT);
                    }

                    httpResponse.setHeader(
                        "Content-disposition",
                        getAttachmentDispositionValue(
                            new StringBuilder(subject).append(".eml").toString(),
                            null,
                            paramContainer.getHeader("user-agent")));
                    Tools.removeCachingHeader(httpResponse);
                    // Write output stream in max. 8K chunks
                    final OutputStream out = httpResponse.getOutputStream();
                    final byte[] bytes = baos.toByteArray();
                    int offset = 0;
                    while (offset < bytes.length) {
                        final int len = Math.min(0xFFFF, bytes.length - offset);
                        out.write(bytes, offset, len);
                        offset += len;
                    }
                    out.flush();
                    /*
                     * ... and return
                     */
                    return null;
                }
                final ContentType ct = mail.getContentType();
                if (ct.containsCharsetParameter() && CharsetDetector.isValid(ct.getCharsetParameter())) {
                    data = new String(baos.toByteArray(), Charsets.forName(ct.getCharsetParameter()));
                } else {
                    data = new String(baos.toByteArray(), Charsets.UTF_8);
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
                    mailInterface.updateMessageFlags(folderPath, new String[] { uid }, MailMessage.FLAG_SEEN, false);
                } else if (wasUnseen) {
                    try {
                        final ServerUserSetting setting = ServerUserSetting.getInstance();
                        final int contextId = session.getContextId();
                        final int userId = session.getUserId();
                        if (setting.isContactCollectOnMailAccess(
                            contextId,
                            userId).booleanValue()) {
                            triggerContactCollector(session, mail, false);
                        }
                    } catch (final OXException e) {
                        LOG.warn("Contact collector could not be triggered.", e);
                    }
                }
            } else {
                data = MailConverter.getInstance().convertSingle4Get(mail, paramContainer, session, mailInterface);
            }
        } catch (final OXException e) {
            if (MailExceptionCode.MAIL_NOT_FOUND.equals(e)) {
                LOG.warn(
                    "Requested mail could not be found. Most likely this is caused by concurrent access of multiple clients while one performed a delete on affected mail.",
                    e);
            } else {
                LOG.error("", e);
            }
            response.setException(e);
            if (errorAsCallback) {
                try {
                    final HttpServletResponse resp = paramContainer.getHttpServletResponse();
                    resp.setContentType(MIME_TEXT_HTML_CHARSET_UTF_8);
                    final String jsResponse = substituteJS(ResponseWriter.getJSON(response).toString(), ACTION_GET);
                    final PrintWriter writer = resp.getWriter();
                    writer.write(jsResponse);
                    writer.flush();
                    return null;
                } catch (final Exception exc) {
                    throw new JSONException(exc);
                }
            }
        } catch (final Exception e) {
            final OXException wrapper = getWrappingOXException(e);
            LOG.error("", wrapper);
            response.setException(wrapper);
            if (errorAsCallback) {
                try {
                    final HttpServletResponse resp = paramContainer.getHttpServletResponse();
                    resp.setContentType(MIME_TEXT_HTML_CHARSET_UTF_8);
                    final String jsResponse = substituteJS(ResponseWriter.getJSON(response).toString(), ACTION_GET);
                    final PrintWriter writer = resp.getWriter();
                    writer.write(jsResponse);
                    writer.flush();
                    return null;
                } catch (final Exception exc) {
                    throw new JSONException(exc);
                }
            }
        }
        /*
         * Close response and flush print writer
         */
        response.setData(data);
        response.setTimestamp(null);
        if (!warnings.isEmpty()) {
            response.addWarning(warnings.get(0));
        }
        return response;
    }

    private static DisplayMode detectDisplayMode(final boolean modifyable, final String view, final UserSettingMail usmNoSave) {
        final DisplayMode displayMode;
        if (null != view) {
            if (VIEW_RAW.equals(view)) {
                displayMode = DisplayMode.RAW;
            } else if (VIEW_TEXT_NO_HTML_ATTACHMENT.equals(view)) {
                usmNoSave.setDisplayHtmlInlineContent(false);
                usmNoSave.setSuppressHTMLAlternativePart(true);
                displayMode = modifyable ? DisplayMode.MODIFYABLE : DisplayMode.DISPLAY;
            } else if (VIEW_TEXT.equals(view)) {
                usmNoSave.setDisplayHtmlInlineContent(false);
                displayMode = modifyable ? DisplayMode.MODIFYABLE : DisplayMode.DISPLAY;
            } else if (VIEW_HTML.equals(view)) {
                usmNoSave.setDisplayHtmlInlineContent(true);
                usmNoSave.setAllowHTMLImages(true);
                displayMode = modifyable ? DisplayMode.MODIFYABLE : DisplayMode.DISPLAY;
            } else if (VIEW_HTML_BLOCKED_IMAGES.equals(view)) {
                usmNoSave.setDisplayHtmlInlineContent(true);
                usmNoSave.setAllowHTMLImages(false);
                displayMode = modifyable ? DisplayMode.MODIFYABLE : DisplayMode.DISPLAY;
            } else {
                LOG.warn("Unknown value in parameter {}: {}. Using user's mail settings as fallback.", PARAMETER_VIEW, view);
                displayMode = modifyable ? DisplayMode.MODIFYABLE : DisplayMode.DISPLAY;
            }
        } else {
            displayMode = modifyable ? DisplayMode.MODIFYABLE : DisplayMode.DISPLAY;
        }
        return displayMode;
    }

    private static void triggerContactCollector(ServerSession session, MailMessage mail, boolean incrementUseCount) throws OXException {
        final ContactCollectorService ccs = ServerServiceRegistry.getInstance().getService(ContactCollectorService.class);
        if (null != ccs) {
            Set<InternetAddress> addrs = AddressUtility.getAddresses(mail, session);

            if (!addrs.isEmpty()) {
                ccs.memorizeAddresses(new ArrayList<InternetAddress>(addrs), incrementUseCount, session);
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

    public void actionGetNew(final ServerSession session, final JSONWriter writer, final JSONObject requestObj, final MailServletInterface mi) throws OXException, JSONException {
        ResponseWriter.write(
            actionGetNew(session, ParamContainer.getInstance(requestObj), mi),
            writer,
            localeFrom(session));
    }

    private final void actionGetNew(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
        final ServerSession session = getSessionObject(req);
        try {
            ResponseWriter.write(
                actionGetNew(session, ParamContainer.getInstance(req, resp), null),
                resp.getWriter(),
                localeFrom(session));
        } catch (final JSONException e) {
            final OXException oxe = OXJSONExceptionCodes.JSON_WRITE_ERROR.create(e, new Object[0]);
            LOG.error("", oxe);
            final Response response = new Response(session);
            response.setException(oxe);
            try {
                ResponseWriter.write(response, resp.getWriter(), localeFrom(session));
            } catch (final JSONException e1) {
                LOG.error(RESPONSE_ERROR, e1);
                sendError(resp);
            }
        } catch (final OXException e) {
            LOG.error("", e);
            final Response response = new Response(session);
            response.setException(e);
            try {
                ResponseWriter.write(response, resp.getWriter(), localeFrom(session));
            } catch (final JSONException e1) {
                LOG.error(RESPONSE_ERROR, e1);
                sendError(resp);
            }
        }
    }

    private final Response actionGetNew(final ServerSession session, final ParamContainer paramContainer, final MailServletInterface mailInterfaceArg) throws JSONException, OXException {
        /*
         * Some variables
         */
        final Response response = new Response(session);
        final OXJSONWriter jsonWriter = new OXJSONWriter();
        /*
         * Start response
         */
        jsonWriter.array();
        SearchIterator<MailMessage> it = null;
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
                        throw MailExceptionCode.INVALID_INT_VALUE.create(PARAMETER_ORDER);
                    }
                }
                String tmp = paramContainer.getStringParam(Mail.PARAMETER_TIMEZONE);
                final TimeZone timeZone = com.openexchange.java.Strings.isEmpty(tmp) ? null : TimeZoneUtils.getTimeZone(tmp.trim());
                tmp = null;
                /*
                 * Pre-Select field writers
                 */
                final MailFieldWriter[] writers = MessageWriter.getMailFieldWriters(MailListField.getFields(columns));
                it = mailInterface.getNewMessages(folderId, sortCol, orderDir, columns, limit == ParamContainer.NOT_FOUND ? -1 : limit);
                final int size = it.size();
                final int userId = session.getUserId();
                final int contextId = session.getContextId();
                for (int i = 0; i < size; i++) {
                    final MailMessage mail = it.next();
                    if (mail != null && !mail.isDeleted()) {
                        final JSONArray ja = new JSONArray();
                        for (final MailFieldWriter writer : writers) {
                            writer.writeField(ja, mail, 0, false, mailInterface.getAccountID(), userId, contextId, timeZone);
                        }
                        jsonWriter.value(ja);
                    }
                }
            } finally {
                if (closeMailInterface && mailInterface != null) {
                    mailInterface.close(true);
                }
            }
        } catch (final OXException e) {
            LOG.error("", e);
            response.setException(e);
        } catch (final Exception e) {
            final OXException wrapper = getWrappingOXException(e);
            LOG.error("", wrapper);
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
        actionGetSaveVersit(session, writer, ParamContainer.getInstance(requestObj), mi);
    }

    private final void actionGetSaveVersit(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
        final ServerSession session = getSessionObject(req);
        try {
            actionGetSaveVersit(session, resp.getWriter(), ParamContainer.getInstance(req, resp), null);
        } catch (final JSONException e) {
            final OXException oxe = OXJSONExceptionCodes.JSON_WRITE_ERROR.create(e, new Object[0]);
            LOG.error("", oxe);
            final Response response = new Response(session);
            response.setException(oxe);
            try {
                ResponseWriter.write(response, resp.getWriter(), localeFrom(session));
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
        final Response response = new Response(session);
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
            final String uid = paramContainer.checkStringParam(PARAMETER_ID);
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
                    final List<CommonObject> retvalList = new ArrayList<CommonObject>();
                    if (versitPart.getContentType().isMimeType(MimeTypes.MIME_TEXT_X_VCARD) || versitPart.getContentType().isMimeType(
                        MimeTypes.MIME_TEXT_VCARD)) {
                        /*
                         * Save VCard
                         */
                        retvalList.add(VCardUtil.importContactToDefaultFolder(versitPart.getInputStream(), session));
                    } else if (versitPart.getContentType().isMimeType(MimeTypes.MIME_TEXT_X_VCALENDAR) || versitPart.getContentType().isMimeType(
                        MimeTypes.MIME_TEXT_CALENDAR)) {
                        /*
                         * Save ICalendar
                         */
                        retvalList.addAll(ICalUtil.importToDefaultFolder(versitPart.getInputStream(), session));
                    } else {
                        throw MailExceptionCode.UNSUPPORTED_VERSIT_ATTACHMENT.create(versitPart.getContentType());
                    }
                    insertedObjs = retvalList.toArray(new CommonObject[retvalList.size()]);
                }
                final JSONObject jo = new JSONObject();
                for (final CommonObject current : insertedObjs) {
                    jo.reset();
                    jo.put(DataFields.ID, current.getObjectID());
                    jo.put(FolderChildFields.FOLDER_ID, current.getParentFolderID());
                    jsonWriter.value(jo);
                }
            } finally {
                if (closeMailInterface && mailInterface != null) {
                    mailInterface.close(true);
                }
            }
        } catch (final OXException e) {
            LOG.error("", e);
            response.setException(e);
        } catch (final Exception e) {
            final OXException wrapper = getWrappingOXException(e);
            LOG.error("", wrapper);
            response.setException(wrapper);
        }
        /*
         * Close response and flush print writer
         */
        jsonWriter.endArray();
        response.setData(jsonWriter.getObject());
        response.setTimestamp(null);
        ResponseWriter.write(response, writer, localeFrom(session));
    }

    public void actionGetGetMultipleAttachments() throws OXException {
        throw MailExceptionCode.UNSUPPORTED_ACTION.create(ACTION_ZIP_MATTACH, "Multiple servlet");
    }

    private final void actionGetMultipleAttachments(final HttpServletRequest req, final HttpServletResponse resp) {
        /*
         * Some variables
         */
        final ServerSession session = getSessionObject(req);
        boolean outSelected = false;
        /*
         * Start response
         */
        try {
            /*
             * Read in parameters
             */
            final String folderPath = checkStringParam(req, PARAMETER_FOLDERID);
            final String uid = checkStringParam(req, PARAMETER_ID);
            final String[] sequenceIds = checkStringArrayParam(req, PARAMETER_MAILATTCHMENT);
            /*
             * Get attachment
             */
            final MailServletInterface mailInterface = MailServletInterface.getInstance(session);
            ManagedFile mf = null;
            try {
                mf = mailInterface.getMessageAttachments(folderPath, uid, sequenceIds);
                /*
                 * Set Content-Type and Content-Disposition header
                 */
                final String fileName;
                {
                    String subject = mailInterface.getMessage(folderPath, uid).getSubject();
                    if (subject == null) { // in case no subject was set
                        subject = StringHelper.valueOf(localeFrom(getSessionObject(req))).getString(MailStrings.DEFAULT_SUBJECT);
                    }
                    fileName = new StringBuilder(subject).append(".zip").toString();
                }
                /*
                 * We are supposed to offer attachment for download. Therefore enforce application/octet-stream and attachment disposition.
                 */
                final ContentType contentType = new ContentType();
                contentType.setPrimaryType("application");
                contentType.setSubType("octet-stream");
                resp.setContentType(contentType.toString());
                resp.setHeader("Content-disposition", getAttachmentDispositionValue(fileName, null, req.getHeader("user-agent")));
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
                final InputStream zipInputStream = mf.getInputStream();
                try {
                    final byte[] buffer = new byte[0xFFFF];
                    for (int len; (len = zipInputStream.read(buffer, 0, buffer.length)) > 0;) {
                        out.write(buffer, 0, len);
                    }
                    out.flush();
                } finally {
                    zipInputStream.close();
                }
            } finally {
                if (mailInterface != null) {
                    mailInterface.close(true);
                }
                if (null != mf) {
                    mf.delete();
                    mf = null;
                }
            }
        } catch (final OXException e) {
            LOG.error("", e);
            callbackError(resp, outSelected, true, session, e);
        } catch (final Exception e) {
            final OXException exc = getWrappingOXException(e);
            LOG.error("", exc);
            callbackError(resp, outSelected, true, session, exc);
        }
    }

    public void actionGetGetMultipleMessages() throws OXException {
        throw MailExceptionCode.UNSUPPORTED_ACTION.create(ACTION_ZIP_MESSAGES, "Multiple servlet");
    }

    private final void actionGetMultipleMessages(final HttpServletRequest req, final HttpServletResponse resp) {
        /*
         * Some variables
         */
        final ServerSession session = getSessionObject(req);
        boolean outSelected = false;
        /*
         * Start response
         */
        try {
            /*
             * Read in parameters
             */
            final String folderPath = checkStringParam(req, PARAMETER_FOLDERID);
            final String[] ids = checkStringArrayParam(req, PARAMETER_ID);
            /*
             * Get attachment
             */
            final MailServletInterface mailInterface = MailServletInterface.getInstance(session);
            ManagedFile mf = null;
            try {
                mf = mailInterface.getMessages(folderPath, ids);
                /*
                 * Set Content-Type and Content-Disposition header
                 */
                final String fileName = "mails.zip";
                /*
                 * We are supposed to offer attachment for download. Therefore enforce application/octet-stream and attachment disposition.
                 */
                final ContentType contentType = new ContentType();
                contentType.setPrimaryType("application");
                contentType.setSubType("octet-stream");
                resp.setContentType(contentType.toString());
                resp.setHeader("Content-disposition", getAttachmentDispositionValue(fileName, null, req.getHeader("user-agent")));
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
                final InputStream zipInputStream = mf.getInputStream();
                try {
                    final byte[] buffer = new byte[0xFFFF];
                    for (int len; (len = zipInputStream.read(buffer, 0, buffer.length)) > 0;) {
                        out.write(buffer, 0, len);
                    }
                    out.flush();
                } finally {
                    zipInputStream.close();
                }
            } finally {
                if (mailInterface != null) {
                    mailInterface.close(true);
                }
                if (null != mf) {
                    mf.delete();
                    mf = null;
                }
            }
        } catch (final OXException e) {
            LOG.error("", e);
            callbackError(resp, outSelected, true, session, e);
        } catch (final Exception e) {
            final OXException exc = getWrappingOXException(e);
            LOG.error("", exc);
            callbackError(resp, outSelected, true, session, exc);
        }
    }

    public void actionGetAttachmentToken(final ServerSession session, final JSONWriter writer, final JSONObject requestObj, final MailServletInterface mi) throws OXException, JSONException {
        ResponseWriter.write(
            actionGetAttachmentToken(session, ParamContainer.getInstance(requestObj), mi),
            writer,
            localeFrom(session));
    }

    private final void actionGetAttachmentToken(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
        final ServerSession session = getSessionObject(req);
        try {
            ResponseWriter.write(
                actionGetAttachmentToken(session, ParamContainer.getInstance(req, resp), null),
                resp.getWriter(),
                localeFrom(session));
        } catch (final JSONException e) {
            final OXException oxe = OXJSONExceptionCodes.JSON_WRITE_ERROR.create(e, new Object[0]);
            LOG.error("", oxe);
            final Response response = new Response();
            response.setException(oxe);
            try {
                ResponseWriter.write(response, resp.getWriter(), localeFrom(session));
            } catch (final JSONException e1) {
                LOG.error(RESPONSE_ERROR, e1);
                sendError(resp);
            }
        }
    }

    private final Response actionGetAttachmentToken(final ServerSession session, final ParamContainer paramContainer, final MailServletInterface mailInterfaceArg) throws JSONException {
        /*
         * Some variables
         */
        final Response response = new Response(session);
        Object data = JSONObject.NULL;
        final List<OXException> warnings = new ArrayList<OXException>(2);
        /*
         * Start response
         */
        try {
            /*
             * Read in parameters
             */
            final String folderPath = paramContainer.checkStringParam(PARAMETER_FOLDERID);
            final String uid = paramContainer.checkStringParam(PARAMETER_ID);
            final String sequenceId = paramContainer.getStringParam(PARAMETER_MAILATTCHMENT);
            final String imageContentId = paramContainer.getStringParam(PARAMETER_MAILCID);
            if (sequenceId == null && imageContentId == null) {
                throw MailExceptionCode.MISSING_PARAM.create(new StringBuilder().append(PARAMETER_MAILATTCHMENT).append(" | ").append(
                    PARAMETER_MAILCID).toString());
            }
            int ttlMillis;
            {
                final String tmp = paramContainer.getStringParam("ttlMillis");
                try {
                    ttlMillis = (tmp == null ? -1 : Integer.parseInt(tmp.trim()));
                } catch (final NumberFormatException e) {
                    ttlMillis = -1;
                }
            }
            /*
             * Generate attachment token
             */
            MailServletInterface mailInterface = mailInterfaceArg;
            boolean closeMailInterface = false;
            try {
                if (mailInterface == null) {
                    mailInterface = MailServletInterface.getInstance(session);
                    closeMailInterface = true;
                }
                /*
                 * Get mail part
                 */
                final MailPart mailPart = mailInterface.getMessageAttachment(folderPath, uid, sequenceId, true);
                if (mailPart == null) {
                    throw MailExceptionCode.NO_ATTACHMENT_FOUND.create(sequenceId);
                }
                final AttachmentToken token = new AttachmentToken(ttlMillis <= 0 ? AttachmentTokenConstants.DEFAULT_TIMEOUT : ttlMillis);
                token.setAccessInfo(mailInterface.getAccountID(), session);
                token.setAttachmentInfo(folderPath, uid, sequenceId);
                AttachmentTokenService service = ServerServiceRegistry.getInstance().getService(AttachmentTokenService.class, true);
                service.putToken(token.setOneTime(true), session);
                final JSONObject attachmentObject = new JSONObject();
                attachmentObject.put("id", token.getId());
                attachmentObject.put("jsessionid", token.getJSessionId());
                data = attachmentObject;
                warnings.addAll(mailInterface.getWarnings());
            } finally {
                if (closeMailInterface && mailInterface != null) {
                    mailInterface.close(true);
                }
            }
        } catch (final OXException e) {
            LOG.error("", e);
            response.setException(e);
        } catch (final RuntimeException e) {
            final OXException wrapper = getWrappingOXException(e);
            LOG.error("", wrapper);
            response.setException(wrapper);
        }
        /*
         * Close response and flush print writer
         */
        response.setData(data);
        response.setTimestamp(null);
        if (!warnings.isEmpty()) {
            response.addWarning(warnings.get(0));
        }
        return response;
    }

    public void actionGetAttachment() throws OXException {
        throw MailExceptionCode.UNSUPPORTED_ACTION.create(ACTION_MATTACH, "Multiple servlet");
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
            final String uid = checkStringParam(req, PARAMETER_ID);
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
                    throw MailExceptionCode.MISSING_PARAM.create(new StringBuilder().append(PARAMETER_MAILATTCHMENT).append(" | ").append(
                        PARAMETER_MAILCID).toString());
                }
                final MailPart mailPart;
                Readable attachmentInputStream;
                if (imageContentId == null) {
                    mailPart = mailInterface.getMessageAttachment(folderPath, uid, sequenceId, !saveToDisk);
                    if (mailPart == null) {
                        throw MailExceptionCode.NO_ATTACHMENT_FOUND.create(sequenceId);
                    }
                    if (filter && !saveToDisk && mailPart.getContentType().isMimeType(MimeTypes.MIME_TEXT_HTM_ALL)) {
                        /*
                         * Apply filter
                         */
                        final ContentType contentType = mailPart.getContentType();
                        final String cs = contentType.containsCharsetParameter() ? contentType.getCharsetParameter() : MailProperties.getInstance().getDefaultMimeCharset();
                        String htmlContent = MessageUtility.readMailPart(mailPart, cs);
                        htmlContent = MessageUtility.simpleHtmlDuplicateRemoval(htmlContent);
                        final HtmlService htmlService = ServerServiceRegistry.getInstance().getService(HtmlService.class);
                        attachmentInputStream = new ByteArrayRandomAccess(sanitizeHtml(htmlContent, htmlService).getBytes(Charsets.forName(cs)));
                    } else {
                        attachmentInputStream = new InputStreamReadable(mailPart.getInputStream());
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
                        throw MailExceptionCode.NO_ATTACHMENT_FOUND.create(sequenceId);
                    }
                    attachmentInputStream = new InputStreamReadable(mailPart.getInputStream());
                }
                /*
                 * Set Content-Type and Content-Disposition header
                 */
                final String fileName = mailPart.getFileName();
                if (saveToDisk) {
                    /*
                     * We are supposed to offer attachment for download. Therefore enforce application/octet-stream and attachment
                     * disposition.
                     */
                    resp.setContentType("application/octet-stream");
                    resp.setHeader(
                        "Content-Disposition",
                        getAttachmentDispositionValue(fileName, mailPart.getContentType().getBaseType(), req.getHeader("user-agent")));
                } else {
                    final CheckedDownload checkedDownload = DownloadUtility.checkInlineDownload(
                        attachmentInputStream,
                        fileName,
                        mailPart.getContentType().toString(),
                        req.getHeader(STR_USER_AGENT),
                        session);
                    resp.setContentType(checkedDownload.getContentType());
                    resp.setHeader("Content-Disposition", checkedDownload.getContentDisposition());
                    attachmentInputStream = checkedDownload.getInputStream();
                }
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
                    final int buflen = 0xFFFF;
                    final byte[] buffer = new byte[buflen];
                    for (int len; (len = attachmentInputStream.read(buffer, 0, buflen)) > 0;) {
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
        } catch (final OXException e) {
            LOG.error("", e);
            callbackError(resp, outSelected, saveToDisk, session, e);
        } catch (final Exception e) {
            final OXException exc = getWrappingOXException(e);
            LOG.error("", exc);
            callbackError(resp, outSelected, saveToDisk, session, exc);
        }
    }

    private static String sanitizeHtml(final String htmlContent, final HtmlService htmlService) {
        return htmlService.sanitize(htmlContent, null, false, null, null);
    }

    private static void callbackError(final HttpServletResponse resp, final boolean outSelected, final boolean saveToDisk, final ServerSession session, final OXException e) {
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
            final Response response = null == session ? new Response() : new Response(session);
            response.setException(e);
            writer.write(substituteJS(ResponseWriter.getJSON(response).toString(), "error"));
            writer.flush();
        } catch (final UnsupportedEncodingException uee) {
            uee.initCause(e);
            LOG.error("", uee);
        } catch (final IOException ioe) {
            ioe.initCause(e);
            LOG.error("", ioe);
        } catch (final IllegalStateException ise) {
            ise.initCause(e);
            LOG.error("", ise);
        } catch (final JSONException je) {
            je.initCause(e);
            LOG.error("", je);
        }
    }

    private static boolean isMSIEOnWindows(final String userAgent) {
        final BrowserDetector browserDetector = BrowserDetector.detectorFor(userAgent);
        return (browserDetector.isMSIE() && browserDetector.isWindows());
    }

    private static final Pattern PAT_BSLASH = Pattern.compile("\\\\");

    private static final Pattern PAT_QUOTE = Pattern.compile("\"");

    private static String escapeBackslashAndQuote(final String str) {
        return PAT_QUOTE.matcher(PAT_BSLASH.matcher(str).replaceAll("\\\\\\\\")).replaceAll("\\\\\\\"");
    }

    private static final Pattern PART_FILENAME_PATTERN = Pattern.compile("(part )([0-9]+)(?:(\\.)([0-9]+))*", Pattern.CASE_INSENSITIVE);

    private static final String DEFAULT_FILENAME = "file.dat";

    @Deprecated
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
                LOG.error("Unsupported encoding in a message detected and monitored: \"{}{}", STR_UTF8, '"', e);
                MailServletInterface.mailInterfaceMonitor.addUnsupportedEncodingExceptions(STR_UTF8);
                return fileName;
            }
        }
        if ((null != baseCT) && (null == getFileExtension(fileName))) {
            if (baseCT.regionMatches(true, 0, MIME_TEXT_PLAIN, 0, MIME_TEXT_PLAIN.length()) && !fileName.toLowerCase(Locale.US).endsWith(
                ".txt")) {
                tmp.append(".txt");
            } else if (baseCT.regionMatches(true, 0, MIME_TEXT_HTML, 0, MIME_TEXT_HTML.length()) && !fileName.toLowerCase(Locale.US).endsWith(
                ".html")) {
                tmp.append(".html");
            }
        }
        return escapeBackslashAndQuote(tmp.toString());
    }

    public static String getAttachmentDispositionValue(final String fileName, final String baseCT, final String userAgent) {
        if (null == fileName) {
            return new StringBuilder("attachment; filename=\"").append(DEFAULT_FILENAME).append('"').toString();
        }
        final Matcher m = PART_FILENAME_PATTERN.matcher(fileName);
        if (m.matches()) {
            return new StringBuilder("attachment; filename=\"").append(escapeBackslashAndQuote(fileName.replaceAll(" ", "_"))).append('"').toString();
        }
        String fn = fileName;
        if ((null != baseCT) && (null == getFileExtension(fn))) {
            if (baseCT.regionMatches(true, 0, MIME_TEXT_PLAIN, 0, MIME_TEXT_PLAIN.length()) && !fileName.toLowerCase(Locale.US).endsWith(
                ".txt")) {
                fn += ".txt";
            } else if (baseCT.regionMatches(true, 0, MIME_TEXT_HTML, 0, MIME_TEXT_HTML.length()) && !fileName.toLowerCase(Locale.US).endsWith(
                ".html")) {
                fn += ".html";
            }
        }
        fn = escapeBackslashAndQuote(fn);
        if (null != userAgent && BrowserDetector.detectorFor(userAgent).isMSIE()) {
            // InternetExplorer
            return new StringBuilder("attachment; filename=\"").append(Helper.encodeFilenameForIE(fn, Charsets.UTF_8)).append('"').toString();
        }
        /*-
         * On socket layer characters are casted to byte values.
         *
         * sink.write((byte) chars[i]);
         *
         * Therefore ensure we have a one-character-per-byte charset, as it is with ISO-8859-1
         */
        final String foo = new String(fn.getBytes(com.openexchange.java.Charsets.UTF_8), com.openexchange.java.Charsets.ISO_8859_1);
        return new StringBuilder("attachment; filename*=UTF-8''").append(URLCoder.encode(fn)).append("; filename=\"").append(foo).append(
            '"').toString();
    }

    private static final Pattern P = Pattern.compile("^[\\w\\d\\:\\/\\.]+(\\.\\w{3,4})$");

    /**
     * Checks if specified file name has a trailing file extension.
     *
     * @param fileName The file name
     * @return The extension (e.g. <code>".txt"</code>) or <code>null</code>
     */
    public static String getFileExtension(final String fileName) {
        if (null == fileName || fileName.indexOf('.') <= 0) {
            return null;
        }
        final Matcher m = P.matcher(fileName);
        return m.matches() ? m.group(1).toLowerCase(Locale.ENGLISH) : null;
    }

    public void actionPutForwardMultiple(final ServerSession session, final JSONWriter writer, final JSONObject jsonObj, final MailServletInterface mi) throws JSONException {
        ResponseWriter.write(
            actionPutForwardMultiple(
                session,
                jsonObj.getString(ResponseFields.DATA),
                ParamContainer.getInstance(jsonObj),
                mi),
                writer,
                localeFrom(session));
    }

    private final void actionPutForwardMultiple(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
        final ServerSession session = getSessionObject(req);
        try {
            ResponseWriter.write(
                actionPutForwardMultiple(session, getBody(req), ParamContainer.getInstance(req, resp), null),
                resp.getWriter(),
                localeFrom(session));
        } catch (final JSONException e) {
            final OXException oxe = OXJSONExceptionCodes.JSON_WRITE_ERROR.create(e, new Object[0]);
            LOG.error("", oxe);
            final Response response = new Response(session);
            response.setException(oxe);
            try {
                ResponseWriter.write(response, resp.getWriter(), localeFrom(session));
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
        final Response response = new Response(session);
        Object data = JSONObject.NULL;
        final List<OXException> warnings = new ArrayList<OXException>(2);
        /*
         * Start response
         */
        try {
            /*
             * Read in parameters
             */
            final JSONArray paths = new JSONArray(body);
            final String[] folders = new String[paths.length()];
            final String[] ids = new String[paths.length()];
            for (int i = 0; i < folders.length; i++) {
                final JSONObject folderAndID = paths.getJSONObject(i);
                folders[i] = folderAndID.getString(PARAMETER_FOLDERID);
                ids[i] = folderAndID.getString(PARAMETER_ID);
            }
            final String view = paramContainer.getStringParam(PARAMETER_VIEW);
            final UserSettingMail usmNoSave = session.getUserSettingMail().clone();
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
                } else if (VIEW_TEXT_NO_HTML_ATTACHMENT.equals(view)) {
                    usmNoSave.setDisplayHtmlInlineContent(false);
                    usmNoSave.setSuppressHTMLAlternativePart(true);
                } else if (VIEW_HTML.equals(view)) {
                    usmNoSave.setDisplayHtmlInlineContent(true);
                    usmNoSave.setAllowHTMLImages(true);
                } else if (VIEW_HTML_BLOCKED_IMAGES.equals(view)) {
                    usmNoSave.setDisplayHtmlInlineContent(true);
                    usmNoSave.setAllowHTMLImages(false);
                } else {
                    LOG.warn("Unknown value in parameter {}: {}. Using user's mail settings as fallback.", PARAMETER_VIEW, view);
                }
            }
            boolean setFrom = AJAXRequestDataTools.parseBoolParameter(paramContainer.getStringParam("setFrom"));
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
                    mailInterface.getAccountID(),
                    mailInterface.getForwardMessageForDisplay(folders, ids, usmNoSave, setFrom),
                    DisplayMode.MODIFYABLE,
                    false,
                    session,
                    usmNoSave,
                    warnings,
                    false,
                    -1);
            } finally {
                if (closeMailInterface && mailInterface != null) {
                    mailInterface.close(true);
                }
            }
        } catch (final OXException e) {
            LOG.error("", e);
            response.setException(e);
        } catch (final Exception e) {
            final OXException wrapper = getWrappingOXException(e);
            LOG.error("", wrapper);
            response.setException(wrapper);
        }
        /*
         * Close response and flush print writer
         */
        response.setData(data);
        response.setTimestamp(null);
        if (!warnings.isEmpty()) {
            response.addWarning(warnings.get(0));
        }
        return response;
    }

    public void actionPutReply(final ServerSession session, final boolean replyAll, final JSONWriter writer, final JSONObject jsonObj, final MailServletInterface mi) throws JSONException {
        ResponseWriter.write(
            actionPutReply(
                session,
                jsonObj.getString(ResponseFields.DATA),
                ParamContainer.getInstance(jsonObj),
                replyAll,
                mi),
                writer,
                localeFrom(session));
    }

    private final void actionPutReply(final HttpServletRequest req, final HttpServletResponse resp, final boolean replyAll) throws IOException {
        final ServerSession session = getSessionObject(req);
        try {
            ResponseWriter.write(
                actionPutReply(session, getBody(req), ParamContainer.getInstance(req, resp), replyAll, null),
                resp.getWriter(),
                localeFrom(session));
        } catch (final JSONException e) {
            final OXException oxe = OXJSONExceptionCodes.JSON_WRITE_ERROR.create(e, new Object[0]);
            LOG.error("", oxe);
            final Response response = new Response(session);
            response.setException(oxe);
            try {
                ResponseWriter.write(response, resp.getWriter(), localeFrom(session));
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
        final Map<String, String> map = newHashMap(8);
        for (final String name : paramContainer.getParameterNames()) {
            try {
                map.put(name, paramContainer.getStringParam(name));
            } catch (final OXException e) {
                LOG.warn("", e);
            }
        }
        for (int i = 0; i < length; i++) {
            final JSONObject folderAndID = paths.getJSONObject(i);
            map.put(PARAMETER_FOLDERID, folderAndID.getString(PARAMETER_FOLDERID));
            map.put(PARAMETER_ID, folderAndID.get(PARAMETER_ID).toString());
        }
        /*
         * ... and fake a GET request
         */
        return actionGetReply(session, replyAll, ParamContainer.getInstance(map), mailInterfaceArg);
    }

    public void actionPutGet(final ServerSession session, final JSONWriter writer, final JSONObject jsonObj, final MailServletInterface mi) throws JSONException {
        ResponseWriter.write(
            actionPutGet(session, jsonObj.getString(ResponseFields.DATA), ParamContainer.getInstance(jsonObj), mi),
            writer,
            localeFrom(session));
    }

    private final void actionPutGet(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
        final ServerSession session = getSessionObject(req);
        try {
            ResponseWriter.write(
                actionPutGet(session, getBody(req), ParamContainer.getInstance(req, resp), null),
                resp.getWriter(),
                localeFrom(session));
        } catch (final JSONException e) {
            final OXException oxe = OXJSONExceptionCodes.JSON_WRITE_ERROR.create(e, new Object[0]);
            LOG.error("", oxe);
            final Response response = new Response(session);
            response.setException(oxe);
            try {
                ResponseWriter.write(response, resp.getWriter(), localeFrom(session));
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
        final Map<String, String> map = newHashMap(2);
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
        } catch (final OXException e) {
            final Response response = new Response(session);
            response.setException(e);
            return response;
        }
        /*
         * ... and fake a GET request
         */
        return actionGetMessage(session, ParamContainer.getInstance(map), mailInterfaceArg);
    }

    public void actionPutAutosave(final ServerSession session, final JSONWriter writer, final JSONObject jsonObj, final MailServletInterface mi) throws JSONException {
        ResponseWriter.write(
            actionPutAutosave(session, jsonObj.getString(ResponseFields.DATA), ParamContainer.getInstance(jsonObj), mi),
            writer,
            localeFrom(session));
    }

    private final void actionPutAutosave(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
        final ServerSession session = getSessionObject(req);
        try {
            ResponseWriter.write(
                actionPutAutosave(session, getBody(req), ParamContainer.getInstance(req, resp), null),
                resp.getWriter(),
                localeFrom(session));
        } catch (final JSONException e) {
            final OXException oxe = OXJSONExceptionCodes.JSON_WRITE_ERROR.create(e, new Object[0]);
            LOG.error("", oxe);
            final Response response = new Response(session);
            response.setException(oxe);
            try {
                ResponseWriter.write(response, resp.getWriter(), localeFrom(session));
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
        final Response response = new Response(session);
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
                     * Parse with default account's transport provider
                     */
                    final List<OXException> warnings = new ArrayList<OXException>();
                    final ComposedMailMessage composedMail = MessageParser.parse4Draft(
                        jsonMailObj,
                        (UploadEvent) null,
                        session,
                        MailAccount.DEFAULT_ID,
                        warnings);
                    response.addWarnings(warnings);
                    if ((composedMail.getFlags() & MailMessage.FLAG_DRAFT) == 0) {
                        LOG.warn("Missing \\Draft flag on action=autosave in JSON message object", new Throwable());
                        composedMail.setFlag(MailMessage.FLAG_DRAFT, true);
                    }
                    if ((composedMail.getFlags() & MailMessage.FLAG_DRAFT) == MailMessage.FLAG_DRAFT) {
                        /*
                         * ... and autosave draft
                         */
                        int accountId;
                        if (composedMail.containsFrom()) {
                            accountId = resolveFrom2Account(session, composedMail.getFrom()[0], false, true);
                        } else {
                            accountId = MailAccount.DEFAULT_ID;
                        }
                        /*
                         * Check if detected account has a drafts folder
                         */
                        if (mailInterface.getDraftsFolder(accountId) == null) {
                            if (MailAccount.DEFAULT_ID == accountId) {
                                // Huh... No drafts folder in default account
                                throw MailExceptionCode.FOLDER_NOT_FOUND.create("Drafts");
                            }
                            LOG.warn(
                                "Mail account {} for user {} in context {} has no drafts folder. Saving draft to default account's draft folder.",
                                accountId,
                                session.getUserId(),
                                session.getContextId());
                            // No drafts folder in detected mail account; auto-save to default account
                            accountId = MailAccount.DEFAULT_ID;
                            composedMail.setFolder(mailInterface.getDraftsFolder(accountId));
                        }
                        msgIdentifier = mailInterface.saveDraft(composedMail, true, accountId).toString();
                    } else {
                        throw MailExceptionCode.UNEXPECTED_ERROR.create("No new message on action=edit");
                    }
                }
                if (msgIdentifier == null) {
                    throw MailExceptionCode.SEND_FAILED_UNKNOWN.create();
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
        } catch (final OXException e) {
            if (MimeMailExceptionCode.INVALID_EMAIL_ADDRESS.equals(e)) {
                e.setCategory(Category.CATEGORY_USER_INPUT);
                LOG.warn("", e);
            } else {
                LOG.error("", e);
            }
            response.setException(e);
        } catch (final Exception e) {
            final OXException wrapper = getWrappingOXException(e);
            LOG.error("", wrapper);
            response.setException(wrapper);
        }
        /*
         * Close response and flush print writer
         */
        response.setTimestamp(null);
        return response;
    }

    public void actionPutClear(final ServerSession session, final JSONWriter writer, final JSONObject jsonObj, final MailServletInterface mi) throws JSONException {
        ResponseWriter.write(
            actionPutClear(session, jsonObj.getString(ResponseFields.DATA), ParamContainer.getInstance(jsonObj), mi),
            writer,
            localeFrom(session));
    }

    private final void actionPutClear(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
        final ServerSession session = getSessionObject(req);
        try {
            ResponseWriter.write(
                actionPutClear(session, getBody(req), ParamContainer.getInstance(req, resp), null),
                resp.getWriter(),
                localeFrom(session));
        } catch (final JSONException e) {
            final OXException oxe = OXJSONExceptionCodes.JSON_WRITE_ERROR.create(e, new Object[0]);
            LOG.error("", oxe);
            final Response response = new Response(session);
            response.setException(oxe);
            try {
                ResponseWriter.write(response, resp.getWriter(), localeFrom(session));
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
        final Response response = new Response(session);
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
        } catch (final OXException e) {
            LOG.error("", e);
            response.setException(e);
        } catch (final Exception e) {
            final OXException wrapper = getWrappingOXException(e);
            LOG.error("", wrapper);
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

    public void actionPutMailSearch(final ServerSession session, final JSONWriter writer, final JSONObject jsonObj, final MailServletInterface mi) throws JSONException, OXException {
        ResponseWriter.write(
            actionPutMailSearch(
                session,
                jsonObj.getString(ResponseFields.DATA),
                ParamContainer.getInstance(jsonObj),
                mi),
                writer,
                localeFrom(session));
    }

    private final void actionPutMailSearch(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
        final ServerSession session = getSessionObject(req);
        try {
            ResponseWriter.write(
                actionPutMailSearch(session, getBody(req), ParamContainer.getInstance(req, resp), null),
                resp.getWriter(),
                localeFrom(session));
        } catch (final JSONException e) {
            final OXException oxe = OXJSONExceptionCodes.JSON_WRITE_ERROR.create(e, new Object[0]);
            LOG.error("", oxe);
            final Response response = new Response(session);
            response.setException(oxe);
            try {
                ResponseWriter.write(response, resp.getWriter(), localeFrom(session));
            } catch (final JSONException e1) {
                LOG.error(RESPONSE_ERROR, e1);
                sendError(resp);
            }
        } catch (final OXException e) {
            LOG.error("", e);
            final Response response = new Response(session);
            response.setException(e);
            try {
                ResponseWriter.write(response, resp.getWriter(), localeFrom(session));
            } catch (final JSONException e1) {
                LOG.error(RESPONSE_ERROR, e1);
                sendError(resp);
            }
        }
    }

    private final Response actionPutMailSearch(final ServerSession session, final String body, final ParamContainer paramContainer, final MailServletInterface mailInterfaceArg) throws JSONException, OXException {
        /*
         * Some variables
         */
        final Response response = new Response(session);
        final OXJSONWriter jsonWriter = new OXJSONWriter();
        /*
         * Start response
         */
        jsonWriter.array();
        SearchIterator<MailMessage> it = null;
        try {
            /*
             * Read in parameters
             */
            final String folderId = paramContainer.checkStringParam(PARAMETER_MAILFOLDER);
            final int[] columns = paramContainer.checkIntArrayParam(PARAMETER_COLUMNS);
            final String sort = paramContainer.getStringParam(PARAMETER_SORT);
            final String order = paramContainer.getStringParam(PARAMETER_ORDER);
            if (sort != null && order == null) {
                throw MailExceptionCode.MISSING_PARAM.create(PARAMETER_ORDER);
            }
            final JSONValue searchValue;
            if (startsWith('[', body, true)) {
                searchValue = new JSONArray(body);
            } else if (startsWith('{', body, true)) {
                searchValue = new JSONObject(body);
            } else {
                throw new JSONException(MessageFormat.format("Request body is not a JSON value: {0}", body));
            }
            String s = paramContainer.getStringParam(Mail.PARAMETER_TIMEZONE);
            final TimeZone timeZone = com.openexchange.java.Strings.isEmpty(s) ? null : TimeZoneUtils.getTimeZone(s.trim());
            s = null;
            /*
             * Perform search dependent on passed JSON value
             */
            if (searchValue.isArray()) {
                /*
                 * Parse body into a JSON array
                 */
                final JSONArray ja = searchValue.toArray();
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
                        final MailFieldWriter[] writers = MessageWriter.getMailFieldWriters(MailListField.getFields(columns));
                        final int userId = session.getUserId();
                        final int contextId = session.getContextId();
                        int orderDir = OrderDirection.ASC.getOrder();
                        if (order != null) {
                            if (order.equalsIgnoreCase(STR_ASC)) {
                                orderDir = OrderDirection.ASC.getOrder();
                            } else if (order.equalsIgnoreCase(STR_DESC)) {
                                orderDir = OrderDirection.DESC.getOrder();
                            } else {
                                throw MailExceptionCode.INVALID_INT_VALUE.create(PARAMETER_ORDER);
                            }
                        }
                        if ((STR_THREAD.equalsIgnoreCase(sort))) {
                            it = mailInterface.getThreadedMessages(
                                folderId,
                                null,
                                MailSortField.RECEIVED_DATE.getField(),
                                orderDir,
                                searchCols,
                                searchPats,
                                true,
                                columns);
                            final int size = it.size();
                            for (int i = 0; i < size; i++) {
                                final MailMessage mail = it.next();
                                if (mail != null && !mail.isDeleted()) {
                                    final JSONArray arr = new JSONArray();
                                    for (final MailFieldWriter writer : writers) {
                                        writer.writeField(arr, mail, 0, false, mailInterface.getAccountID(), userId, contextId, timeZone);
                                    }
                                    jsonWriter.value(arr);
                                }
                            }
                        } else {
                            final int sortCol = sort == null ? MailListField.RECEIVED_DATE.getField() : Integer.parseInt(sort);
                            it = mailInterface.getMessages(folderId, null, sortCol, orderDir, searchCols, searchPats, true, columns, false);
                            final int size = it.size();
                            for (int i = 0; i < size; i++) {
                                final MailMessage mail = it.next();
                                if (mail != null && !mail.isDeleted()) {
                                    final JSONArray arr = new JSONArray();
                                    for (final MailFieldWriter writer : writers) {
                                        writer.writeField(arr, mail, 0, false, mailInterface.getAccountID(), userId, contextId, timeZone);
                                    }
                                    jsonWriter.value(arr);
                                }
                            }
                        }
                    } finally {
                        if (closeMailInterface && mailInterface != null) {
                            mailInterface.close(true);
                        }
                    }
                }
            } else {
                final JSONArray searchArray = searchValue.toObject().getJSONArray(PARAMETER_FILTER);
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
                    final MailFieldWriter[] writers = MessageWriter.getMailFieldWriters(MailListField.getFields(columns));
                    final int userId = session.getUserId();
                    final int contextId = session.getContextId();
                    int orderDir = OrderDirection.ASC.getOrder();
                    if (order != null) {
                        if (order.equalsIgnoreCase(STR_ASC)) {
                            orderDir = OrderDirection.ASC.getOrder();
                        } else if (order.equalsIgnoreCase(STR_DESC)) {
                            orderDir = OrderDirection.DESC.getOrder();
                        } else {
                            throw MailExceptionCode.INVALID_INT_VALUE.create(PARAMETER_ORDER);
                        }
                    }
                    final int sortCol = sort == null ? MailListField.RECEIVED_DATE.getField() : Integer.parseInt(sort);
                    it = mailInterface.getMessages(folderId, null, sortCol, orderDir, SearchTermParser.parse(searchArray), true, columns, false);
                    final int size = it.size();
                    for (int i = 0; i < size; i++) {
                        final MailMessage mail = it.next();
                        final JSONArray arr = new JSONArray();
                        for (final MailFieldWriter writer : writers) {
                            writer.writeField(arr, mail, 0, false, mailInterface.getAccountID(), userId, contextId, timeZone);
                        }
                        jsonWriter.value(arr);
                    }
                } finally {
                    if (closeMailInterface && mailInterface != null) {
                        mailInterface.close(true);
                    }
                }
            }
        } catch (final OXException e) {
            LOG.error("", e);
            response.setException(e);
        } catch (final Exception e) {
            final OXException wrapper = getWrappingOXException(e);
            LOG.error("", wrapper);
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
        ResponseWriter.write(
            actionPutMailList(session, jsonObj.getString(ResponseFields.DATA), ParamContainer.getInstance(jsonObj), mi),
            writer,
            localeFrom(session));
    }

    private final void actionPutMailList(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
        final ServerSession session = getSessionObject(req);
        try {
            ResponseWriter.write(
                actionPutMailList(session, getBody(req), ParamContainer.getInstance(req, resp), null),
                resp.getWriter(),
                localeFrom(session));
        } catch (final JSONException e) {
            final OXException oxe = OXJSONExceptionCodes.JSON_WRITE_ERROR.create(e, new Object[0]);
            LOG.error("", oxe);
            final Response response = new Response(session);
            response.setException(oxe);
            try {
                ResponseWriter.write(response, resp.getWriter(), localeFrom(session));
            } catch (final JSONException e1) {
                LOG.error(RESPONSE_ERROR, e1);
                sendError(resp);
            }
        }
    }

    private static final Pattern SPLIT = Pattern.compile(" *, *");

    private final Response actionPutMailList(final ServerSession session, final String body, final ParamContainer paramContainer, final MailServletInterface mailInterfaceArg) throws JSONException {
        /*
         * Some variables
         */
        final Response response = new Response(session);
        final OXJSONWriter jsonWriter = new OXJSONWriter();
        /*
         * Start response
         */
        jsonWriter.array();
        try {
            final int[] columns = paramContainer.checkIntArrayParam(PARAMETER_COLUMNS);
            final String[] headers;
            {
                final String tmp = paramContainer.getStringParam(PARAMETER_HEADERS);
                headers = null == tmp ? null : SPLIT.split(tmp, 0);
            }
            String tmp = paramContainer.getStringParam(Mail.PARAMETER_TIMEZONE);
            final TimeZone timeZone = com.openexchange.java.Strings.isEmpty(tmp) ? null : TimeZoneUtils.getTimeZone(tmp.trim());
            tmp = null;
            /*
             * Pre-Select field writers
             */
            final MailFieldWriter[] writers = MessageWriter.getMailFieldWriters(MailListField.getFields(columns));
            final MailFieldWriter[] headerWriters = null == headers ? null : MessageWriter.getHeaderFieldWriters(headers);
            /*
             * Get map
             */
            final Map<String, List<String>> idMap = fillMapByArray(new JSONArray(body));
            if (idMap.isEmpty()) {
                /*
                 * Request body is an empty JSON array
                 */
                LOG.debug("Empty JSON array detected in request body.", new Throwable());
                final Response r = new Response(session);
                r.setData(EMPTY_JSON_ARR);
                return r;
            }
            /*
             * Proceed
             */
            MailServletInterface mailInterface = mailInterfaceArg;
            boolean closeMailInterface = false;
            try {
                if (mailInterface == null) {
                    mailInterface = MailServletInterface.getInstance(session);
                    closeMailInterface = true;
                }
                final int userId = session.getUserId();
                final int contextId = session.getContextId();
                for (final Map.Entry<String, List<String>> entry : idMap.entrySet()) {
                    final MailMessage[] mails = mailInterface.getMessageList(entry.getKey(), toArray(entry.getValue()), columns, headers);
                    final int accountID = mailInterface.getAccountID();
                    for (final MailMessage mail : mails) {
                        if (mail != null) {
                            final JSONArray ja = new JSONArray();
                            for (MailFieldWriter writer : writers) {
                                writer.writeField(ja, mail, 0, false, accountID, userId, contextId, timeZone);
                            }
                            if (null != headerWriters) {
                                for (MailFieldWriter headerWriter : headerWriters) {
                                    headerWriter.writeField(ja, mail, 0, false, accountID, userId, contextId, timeZone);
                                }
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
        } catch (final OXException e) {
            LOG.error("", e);
            response.setException(e);
        } catch (final Exception e) {
            final OXException wrapper = getWrappingOXException(e);
            LOG.error("", wrapper);
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

    private static String[] toArray(final Collection<String> c) {
        return c.toArray(new String[c.size()]);
    }

    private static final Map<String, List<String>> fillMapByArray(final JSONArray idArray) throws JSONException, OXException {
        final int length = idArray.length();
        if (length <= 0) {
            return Collections.emptyMap();
        }
        final Map<String, List<String>> idMap = newHashMap(4);
        final String parameterFolderId = PARAMETER_FOLDERID;
        final String parameterId = PARAMETER_ID;
        String folder;
        List<String> list;
        {
            final JSONObject idObject = idArray.getJSONObject(0);
            folder = ensureString(parameterFolderId, idObject);
            list = new ArrayList<String>(length);
            idMap.put(folder, list);
            list.add(ensureString(parameterId, idObject));
        }
        for (int i = 1; i < length; i++) {
            final JSONObject idObject = idArray.getJSONObject(i);
            final String fld = ensureString(parameterFolderId, idObject);
            if (!folder.equals(fld)) {
                folder = fld;
                final List<String> tmp = idMap.get(folder);
                if (tmp == null) {
                    list = new ArrayList<String>(length);
                    idMap.put(folder, list);
                } else {
                    list = tmp;
                }
            }
            list.add(ensureString(parameterId, idObject));
        }
        return idMap;
    }

    private static String ensureString(final String key, final JSONObject jo) throws OXException {
        if (!jo.hasAndNotNull(key)) {
            throw MailExceptionCode.MISSING_PARAMETER.create(key);
        }
        return jo.optString(key);
    }

    public void actionPutDeleteMails(final ServerSession session, final JSONWriter writer, final JSONObject jsonObj, final MailServletInterface mi) throws JSONException {
        ResponseWriter.write(
            actionPutDeleteMails(
                session,
                jsonObj.getString(ResponseFields.DATA),
                ParamContainer.getInstance(jsonObj),
                mi),
                writer,
                localeFrom(session));
    }

    private final void actionPutDeleteMails(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
        final ServerSession session = getSessionObject(req);
        try {
            ResponseWriter.write(
                actionPutDeleteMails(session, getBody(req), ParamContainer.getInstance(req, resp), null),
                resp.getWriter(),
                localeFrom(session));
        } catch (final JSONException e) {
            final OXException oxe = OXJSONExceptionCodes.JSON_WRITE_ERROR.create(e, new Object[0]);
            LOG.error("", oxe);
            final Response response = new Response(session);
            response.setException(oxe);
            try {
                ResponseWriter.write(response, resp.getWriter(), localeFrom(session));
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
        final Response response = new Response(session);
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
                        final FullnameArgument fa = MailFolderUtility.prepareMailFolderParam(obj.getString(PARAMETER_FOLDERID));
                        l.add(new MailPath(fa.getAccountId(), fa.getFullname(), obj.getString(PARAMETER_ID)));
                    }
                    Collections.sort(l, MailPath.COMPARATOR);
                    String lastFldArg = l.get(0).getFolderArgument();
                    final List<String> arr = new ArrayList<String>(length);
                    for (int i = 0; i < length; i++) {
                        final MailPath current = l.get(i);
                        final String folderArgument = current.getFolderArgument();
                        if (!lastFldArg.equals(folderArgument)) {
                            /*
                             * Delete all collected UIDs til here and reset
                             */
                            final String[] uids = arr.toArray(new String[arr.size()]);
                            mailInterface.deleteMessages(lastFldArg, uids, hardDelete);
                            arr.clear();
                            lastFldArg = folderArgument;
                        }
                        arr.add(current.getMailID());
                    }
                    if (arr.size() > 0) {
                        final String[] uids = arr.toArray(new String[arr.size()]);
                        mailInterface.deleteMessages(lastFldArg, uids, hardDelete);
                    }
                }
            } finally {
                if (closeMailInterface && mailInterface != null) {
                    mailInterface.close(true);
                }
            }
        } catch (final OXException e) {
            LOG.error("", e);
            response.setException(e);
        } catch (final Exception e) {
            final OXException wrapper = getWrappingOXException(e);
            LOG.error("", wrapper);
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
        ResponseWriter.write(
            actionPutUpdateMail(
                session,
                jsonObj.getString(ResponseFields.DATA),
                ParamContainer.getInstance(jsonObj),
                mailInterface),
                writer,
                localeFrom(session));
    }

    private final void actionPutUpdateMail(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
        final ServerSession session = getSessionObject(req);
        try {
            ResponseWriter.write(
                actionPutUpdateMail(session, getBody(req), ParamContainer.getInstance(req, resp), null),
                resp.getWriter(),
                localeFrom(session));
        } catch (final JSONException e) {
            final OXException oxe = OXJSONExceptionCodes.JSON_WRITE_ERROR.create(e, new Object[0]);
            LOG.error("", oxe);
            final Response response = new Response(session);
            response.setException(oxe);
            try {
                ResponseWriter.write(response, resp.getWriter(), localeFrom(session));
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
        final Response response = new Response(session);
        final OXJSONWriter jsonWriter = new OXJSONWriter();
        /*
         * Start response
         */
        jsonWriter.object();
        try {
            final String sourceFolder = paramContainer.checkStringParam(PARAMETER_FOLDERID);
            final JSONObject bodyObj = new JSONObject(body);
            final String destFolder = bodyObj.hasAndNotNull(FolderChildFields.FOLDER_ID) ? bodyObj.getString(FolderChildFields.FOLDER_ID) : null;
            final Integer colorLabel = bodyObj.hasAndNotNull(CommonFields.COLORLABEL) ? Integer.valueOf(bodyObj.getInt(CommonFields.COLORLABEL)) : null;
            final Integer flagBits = bodyObj.hasAndNotNull(MailJSONField.FLAGS.getKey()) ? Integer.valueOf(bodyObj.getInt(MailJSONField.FLAGS.getKey())) : null;
            boolean flagVal = false;
            if (flagBits != null) {
                /*
                 * Look for boolean value
                 */
                flagVal = (bodyObj.has(MailJSONField.VALUE.getKey()) && !bodyObj.isNull(MailJSONField.VALUE.getKey()) ? bodyObj.getBoolean(MailJSONField.VALUE.getKey()) : false);
            }

            final Integer setFlags = bodyObj.hasAndNotNull("set_flags") ? Integer.valueOf(bodyObj.getInt("set_flags")) : null;
            final Integer clearFlags = bodyObj.hasAndNotNull("clear_flags") ? Integer.valueOf(bodyObj.getInt("clear_flags")) : null;

            MailServletInterface mailInterface = mailIntefaceArg;
            boolean closeMailInterface = false;
            try {
                if (mailInterface == null) {
                    mailInterface = MailServletInterface.getInstance(session);
                    closeMailInterface = true;
                }

                final String uid;
                {
                    String tmp = paramContainer.getStringParam(PARAMETER_ID);
                    if (null == tmp) {
                        tmp = paramContainer.getStringParam(PARAMETER_MESSAGE_ID);
                        if (null == tmp) {
                            uid = null;
                        } else {
                            uid = mailInterface.getMailIDByMessageID(sourceFolder, tmp);
                        }
                    } else {
                        uid = tmp;
                    }
                }

                String folderId = sourceFolder;
                String mailId = uid;
                if (colorLabel != null) {
                    /*
                     * Update color label
                     */
                    mailInterface.updateMessageColorLabel(sourceFolder, uid == null ? null : new String[] { uid }, colorLabel.intValue());
                }
                if (flagBits != null) {
                    /*
                     * Update system flags which are allowed to be altered by client
                     */
                    mailInterface.updateMessageFlags(sourceFolder, uid == null ? null : new String[] { uid }, flagBits.intValue(), flagVal);
                }
                if (setFlags != null) {
                    /*
                     * Add system flags which are allowed to be altered by client
                     */
                    mailInterface.updateMessageFlags(sourceFolder, uid == null ? null : new String[] { uid }, setFlags.intValue(), true);
                }
                if (clearFlags != null) {
                    /*
                     * Remove system flags which are allowed to be altered by client
                     */
                    mailInterface.updateMessageFlags(sourceFolder, uid == null ? null : new String[] { uid }, clearFlags.intValue(), false);
                }
                if (destFolder != null) {
                    /*
                     * Perform move operation
                     */
                    mailId = mailInterface.copyMessages(sourceFolder, destFolder, new String[] { uid }, true)[0];
                    folderId = destFolder;
                }
                jsonWriter.key(FolderChildFields.FOLDER_ID).value(folderId);
                jsonWriter.key(DataFields.ID).value(mailId);
            } finally {
                if (closeMailInterface && mailInterface != null) {
                    mailInterface.close(true);
                }
            }
        } catch (final OXException e) {
            LOG.error("", e);
            response.setException(e);
        } catch (final Exception e) {
            final OXException wrapper = getWrappingOXException(e);
            LOG.error("", wrapper);
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

    private final void actionPutNewMail(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
        final ServerSession session = getSessionObject(req);
        try {
            ResponseWriter.write(
                actionPutNewMail(session, req, ParamContainer.getInstance(req, resp)),
                resp.getWriter(),
                localeFrom(session));
        } catch (final JSONException e) {
            final OXException oxe = OXJSONExceptionCodes.JSON_WRITE_ERROR.create(e, new Object[0]);
            LOG.error("", oxe);
            final Response response = new Response(session);
            response.setException(oxe);
            try {
                ResponseWriter.write(response, resp.getWriter(), localeFrom(session));
            } catch (final JSONException e1) {
                LOG.error(RESPONSE_ERROR, e1);
                sendError(resp);
            }
        }
    }

    private final void actionPostImportMail(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
        Tools.disableCaching(resp);
        resp.setContentType(CONTENTTYPE_JAVASCRIPT);
        final ServerSession session = getSessionObject(req);
        final Response response = actionPostImportMail(session, req, ParamContainer.getInstance(req, resp));
        try {
            ResponseWriter.write(response, resp.getWriter(), localeFrom(session));
        } catch (final JSONException e) {
            final OXException oxe = OXJSONExceptionCodes.JSON_WRITE_ERROR.create(e, new Object[0]);
            LOG.error("", oxe);
            final Response response1 = new Response(session);
            response1.setException(oxe);
            try {
                ResponseWriter.write(response1, resp.getWriter(), localeFrom(session));
            } catch (final JSONException e1) {
                LOG.error(RESPONSE_ERROR, e1);
                sendError(resp);
            }
        }
    }

    private interface PutNewMailData {

        InternetAddress getFromAddress();

        MimeMessage getMail();
    }

    private final Response actionPutNewMail(final ServerSession session, final HttpServletRequest req, final ParamContainer paramContainer) {
        final Response response = new Response(session);
        JSONValue responseData = null;
        final ManagedMimeMessage managedMimeMessage = null;
        try {
            final String folder = paramContainer.getStringParam(PARAMETER_FOLDERID);
            final int flags;
            {
                final int i = paramContainer.getIntParam(PARAMETER_FLAGS);
                flags = ParamContainer.NOT_FOUND == i ? 0 : i;
            }
            final boolean force;
            {
                String tmp = paramContainer.getStringParam("force");
                if (null == tmp) {
                    force = false;
                } else {
                    tmp = tmp.trim();
                    force = "1".equals(tmp) || Boolean.parseBoolean(tmp);
                }
            }
            // Get rfc822 bytes and create corresponding mail message
            final QuotedInternetAddress defaultSendAddr = new QuotedInternetAddress(getDefaultSendAddress(session), false);
            final PutNewMailData data;
            {
                final MimeMessage message;
                {
                    InputStream in = null;
                    try {
                        in = req.getInputStream();
                        message = new MimeMessage(MimeDefaultSession.getDefaultSession(), in);
                    } finally {
                        if (null != in) {
                            try {
                                in.close();
                            } catch (final Exception e) {
                                LOG.error("Closing stream failed.", e);
                            }
                        }
                    }
                }
                /*
                 * Drop special "x-original-headers" header
                 */
                message.removeHeader("x-original-headers");
                new MimeMessageFiller(session, session.getContext()).setCommonHeaders(message);
                /*
                 * Proceed...
                 */
                final String fromAddr = message.getHeader(MessageHeaders.HDR_FROM, null);
                final InternetAddress fromAddress;
                if (com.openexchange.java.Strings.isEmpty(fromAddr)) {
                    // Add from address
                    fromAddress = defaultSendAddr;
                    message.setFrom(fromAddress);
                } else {
                    fromAddress = new QuotedInternetAddress(fromAddr, true);
                }
                data = new PutNewMailData() {

                    @Override
                    public MimeMessage getMail() {
                        return message;
                    }

                    @Override
                    public InternetAddress getFromAddress() {
                        return fromAddress;
                    }
                };
            }
            // Check if "folder" element is present which indicates to save given message as a draft or append to denoted folder
            if (folder == null) {
                responseData = appendDraft(session, flags, force, data.getFromAddress(), data.getMail());
            } else {
                final String[] ids;
                final MailServletInterface mailInterface = MailServletInterface.getInstance(session);
                try {
                    ids = mailInterface.appendMessages(
                        folder,
                        new MailMessage[] { MimeMessageConverter.convertMessage(data.getMail()) },
                        force);
                    if (flags > 0) {
                        mailInterface.updateMessageFlags(folder, ids, flags, true);
                    }
                } finally {
                    mailInterface.close(true);
                }
                final JSONObject responseObj = new JSONObject();
                responseObj.put(FolderChildFields.FOLDER_ID, folder);
                responseObj.put(DataFields.ID, ids[0]);
                responseData = responseObj;
            }
        } catch (final OXException e) {
            LOG.error("", e);
            response.setException(e);
        } catch (final Exception e) {
            final OXException wrapper = getWrappingOXException(e);
            LOG.error("", wrapper);
            response.setException(wrapper);
        } finally {
            if (null != managedMimeMessage) {
                try {
                    managedMimeMessage.cleanUp();
                } catch (final Exception e) {
                    // Ignore
                }
            }
        }
        // Close response and flush print writer
        response.setData(responseData == null ? JSONObject.NULL : responseData);
        response.setTimestamp(null);
        return response;
    }

    public void actionPutTransportMail(final ServerSession session, final JSONWriter writer, final JSONObject jsonObj, final MailServletInterface mailInterface) throws JSONException {
        ResponseWriter.write(
            actionPutTransportMail(
                session,
                jsonObj.getString(ResponseFields.DATA),
                ParamContainer.getInstance(jsonObj),
                mailInterface),
                writer,
                localeFrom(session));
    }

    private final void actionPutTransportMail(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
        final ServerSession session = getSessionObject(req);
        try {
            ResponseWriter.write(
                actionPutTransportMail(session, getBody(req), ParamContainer.getInstance(req, resp), null),
                resp.getWriter(),
                localeFrom(session));
        } catch (final JSONException e) {
            final OXException oxe = OXJSONExceptionCodes.JSON_WRITE_ERROR.create(e, new Object[0]);
            LOG.error("", oxe);
            final Response response = new Response(session);
            response.setException(oxe);
            try {
                ResponseWriter.write(response, resp.getWriter(), localeFrom(session));
            } catch (final JSONException e1) {
                LOG.error(RESPONSE_ERROR, e1);
                sendError(resp);
            }
        }
    }

    private final Response actionPutTransportMail(final ServerSession session, final String body, final ParamContainer paramContainer, final MailServletInterface mailIntefaceArg) {
        final Response response = new Response(session);
        JSONValue responseData = null;
        try {
            final InternetAddress[] recipients;
            {
                final String recipientsStr = paramContainer.getStringParam("recipients");
                recipients = null == recipientsStr ? null : QuotedInternetAddress.parseHeader(recipientsStr, false);
            }
            /*
             * Parse structured JSON mail object
             */
            final ComposedMailMessage composedMail = MIMEStructureParser.parseStructure(new JSONObject(body), session);
            if (recipients != null && recipients.length > 0) {
                composedMail.addRecipients(recipients);
            }
            /*
             * Transport mail
             */
            MailServletInterface mailInterface = mailIntefaceArg;
            boolean closeMailInterface = false;
            try {
                if (mailInterface == null) {
                    mailInterface = MailServletInterface.getInstance(session);
                    closeMailInterface = true;
                }
                /*
                 * Determine account
                 */
                int accountId;
                try {
                    final InternetAddress[] fromAddrs = composedMail.getFrom();
                    accountId = resolveFrom2Account(session, fromAddrs != null && fromAddrs.length > 0 ? fromAddrs[0] : null, true, true);
                } catch (final OXException e) {
                    if (MailExceptionCode.NO_TRANSPORT_SUPPORT.equals(e)) {
                        // Re-throw
                        throw e;
                    }
                    LOG.warn("{}. Using default account's transport.", e.getMessage());
                    // Send with default account's transport provider
                    accountId = MailAccount.DEFAULT_ID;
                }
                /*
                 * Transport mail
                 */
                final String id = mailInterface.sendMessage(composedMail, ComposeType.NEW, accountId);
                final int pos = id.lastIndexOf(MailPath.SEPERATOR);
                if (-1 == pos) {
                    throw MailExceptionCode.INVALID_MAIL_IDENTIFIER.create(id);
                }
                final JSONObject responseObj = new JSONObject();
                responseObj.put(FolderChildFields.FOLDER_ID, id.substring(0, pos));
                responseObj.put(DataFields.ID, id.substring(pos + 1));
                responseData = responseObj;
                /*
                 * Trigger contact collector
                 */
                try {
                    final ServerUserSetting setting = ServerUserSetting.getInstance();
                    final int contextId = session.getContextId();
                    final int userId = session.getUserId();
                    if (setting.isContactCollectOnMailTransport(
                        contextId,
                        userId).booleanValue()) {
                        triggerContactCollector(session, composedMail, true);
                    }
                } catch (final OXException e) {
                    LOG.warn("Contact collector could not be triggered.", e);
                }
            } finally {
                if (closeMailInterface && mailInterface != null) {
                    mailInterface.close(true);
                }
            }
        } catch (final OXException e) {
            LOG.error("", e);
            response.setException(e);
        } catch (final Exception e) {
            final OXException wrapper = getWrappingOXException(e);
            LOG.error("", wrapper);
            response.setException(wrapper);
        }
        // Close response and flush print writer
        response.setData(responseData == null ? JSONObject.NULL : responseData);
        response.setTimestamp(null);
        return response;
    }

    /**
     * The poison element to quit message import immediately.
     */
    protected static final MimeMessage POISON = new MimeMessage(MimeDefaultSession.getDefaultSession());

    private static final class AppenderTask extends AbstractTask<Object> {

        private final AtomicBoolean keepgoing;

        private final MailServletInterface mailInterface;

        private final String folder;

        private final boolean force;

        private final int flags;

        private final BlockingQueue<MimeMessage> queue;

        private OXException exception;

        protected AppenderTask(final MailServletInterface mailInterface, final String folder, final boolean force, final int flags, final BlockingQueue<MimeMessage> queue) {
            super();
            keepgoing = new AtomicBoolean(true);
            this.mailInterface = mailInterface;
            this.folder = folder;
            this.force = force;
            this.flags = flags;
            this.queue = queue;
        }

        protected void stop() throws OXException {
            keepgoing.set(false);
            /*
             * Feed poison element to enforce quit
             */
            try {
                queue.put(POISON);
            } catch (final InterruptedException e) {
                /*
                 * Cannot occur, but keep interrupted state
                 */
                Thread.currentThread().interrupt();
                throw MailExceptionCode.INTERRUPT_ERROR.create(e);
            }
        }

        @Override
        public Object call() throws Exception {
            final List<String> idList = new ArrayList<String>();
            try {
                final List<MimeMessage> messages = new ArrayList<MimeMessage>(16);
                final List<MailMessage> mails = new ArrayList<MailMessage>(16);
                while (keepgoing.get() || !queue.isEmpty()) {
                    if (queue.isEmpty()) {
                        // Blocking wait for at least 1 message to arrive.
                        final MimeMessage msg = queue.take();
                        if (POISON == msg) {
                            return null;
                        }
                        messages.add(msg);
                    }
                    queue.drainTo(messages);
                    final boolean quit = messages.remove(POISON);
                    for (final MimeMessage message : messages) {
                        message.getHeader("Date", null);
                        final MailMessage mm = MimeMessageConverter.convertMessage(message);
                        mails.add(mm);
                    }
                    messages.clear();
                    final String[] ids = mailInterface.importMessages(folder, mails.toArray(new MailMessage[mails.size()]), force);
                    mails.clear();
                    idList.addAll(Arrays.asList(ids));
                    if (flags > 0) {
                        mailInterface.updateMessageFlags(folder, ids, flags, true);
                    }
                    if (quit) {
                        return null;
                    }
                }
            } catch (final OXException e) {
                exception = e;
                throw e;
            } catch (final MessagingException e) {
                exception = MimeMailException.handleMessagingException(e);
                throw exception;
            } catch (final InterruptedException e) {
                // Restore the interrupted status; see http://www.ibm.com/developerworks/java/library/j-jtp05236/index.html
                Thread.currentThread().interrupt();
                exception = getWrappingOXException(e);
                throw exception;
            } finally {
                mailInterface.close(true);
            }
            return null;
        }

        @Override
        public void setThreadName(final ThreadRenamer threadRenamer) {
            threadRenamer.rename("Mail Import Thread");
        }
    }

    private final Response actionPostImportMail(final ServerSession session, final HttpServletRequest req, final ParamContainer paramContainer) {
        final Response response = new Response(session);
        JSONValue responseData = null;
        AppenderTask task;
        try {
            final String folder = paramContainer.checkStringParam(PARAMETER_FOLDERID);
            final int flags;
            {
                final int i = paramContainer.getIntParam(PARAMETER_FLAGS);
                flags = ParamContainer.NOT_FOUND == i ? 0 : i;
            }
            final boolean force;
            {
                String tmp = paramContainer.getStringParam("force");
                if (null == tmp) {
                    force = false;
                } else {
                    tmp = tmp.trim();
                    force = STR_1.equals(tmp) || Boolean.parseBoolean(tmp);
                }
            }
            final QuotedInternetAddress defaultSendAddr = new QuotedInternetAddress(getDefaultSendAddress(session), false);
            MailServletInterface mailInterface = MailServletInterface.getInstance(session);
            final BlockingQueue<MimeMessage> queue = new ArrayBlockingQueue<MimeMessage>(100);
            Future<Object> future = null;
            {
                if (!ServletFileUpload.isMultipartContent(req)) {
                    throw MailExceptionCode.UNSUPPORTED_MIME_TYPE.create(req.getContentType());
                }
                final ThreadPoolService service = ServerServiceRegistry.getInstance().getService(ThreadPoolService.class, true);
                task = new AppenderTask(mailInterface, folder, force, flags, queue);
                try {
                    FileItemIterator iter;
                    {
                        UserSettingMail usm = session.getUserSettingMail();
                        long maxFileSize = usm.getUploadQuotaPerFile();
                        if (maxFileSize <= 0) {
                            maxFileSize = -1L;
                        }
                        long maxSize = usm.getUploadQuota();
                        if (maxSize <= 0) {
                            maxSize = -1L;
                        }
                        ServletFileUpload upload = newFileUploadBase(maxFileSize, maxSize);

                        // Check request's character encoding
                        if (null == req.getCharacterEncoding()) {
                            String defaultEnc = ServerConfig.getProperty(Property.DefaultEncoding);
                            try {
                                req.setCharacterEncoding(defaultEnc);
                            } catch (final Exception e) {
                                // Ignore
                            }
                            upload.setHeaderEncoding(defaultEnc);
                        }

                        iter = upload.getItemIterator(req);
                    }

                    if (iter.hasNext()) {
                        future = service.submit(task);
                    }
                    boolean keepgoing = true;
                    while (keepgoing && iter.hasNext()) {
                        final FileItemStream item = iter.next();
                        final InputStream is = item.openStream();
                        final MimeMessage message;
                        try {
                            message = new MimeMessage(MimeDefaultSession.getDefaultSession(), is);
                        } finally {
                            try {
                                is.close();
                            } catch (final Exception e) {
                                LOG.error("Closing file item stream failed.", e);
                            }
                        }
                        final String fromAddr = message.getHeader(MessageHeaders.HDR_FROM, null);
                        if (com.openexchange.java.Strings.isEmpty(fromAddr)) {
                            // Add from address
                            message.setFrom(defaultSendAddr);
                        }
                        while (keepgoing && !queue.offer(message, 1, TimeUnit.SECONDS)) {
                            keepgoing = !future.isDone();
                        }
                    }
                } finally {
                    task.stop();
                }
            }

            final MailImportResult[] mirs;
            if (null == future) {
                mirs = new MailImportResult[0];
            } else {
                /*
                 * Ensure release from BlockingQueue.take();
                 */
                try {
                    future.get(10, TimeUnit.SECONDS);
                } catch (final TimeoutException e) {
                    // Wait time elapsed; enforce cancelation
                    future.cancel(true);
                }
                final MailImportResult[] alreadyImportedOnes = mailInterface.getMailImportResults();
                /*
                 * Still some in queue?
                 */
                if (queue.isEmpty()) {
                    mirs = alreadyImportedOnes;
                } else {
                    final List<MimeMessage> messages = new ArrayList<MimeMessage>(16);
                    queue.drainTo(messages);
                    messages.remove(POISON);
                    final List<MailMessage> mails = new ArrayList<MailMessage>(messages.size());
                    for (final MimeMessage message : messages) {
                        message.getHeader("Date", null);
                        final MailMessage mm = MimeMessageConverter.convertMessage(message);
                        mails.add(mm);
                    }
                    messages.clear();
                    mailInterface = MailServletInterface.getInstance(session);
                    try {
                        final String[] ids = mailInterface.importMessages(folder, mails.toArray(new MailMessage[mails.size()]), force);
                        mails.clear();
                        if (flags > 0) {
                            mailInterface.updateMessageFlags(folder, ids, flags, true);
                        }
                    } finally {
                        mailInterface.close(true);
                    }
                    final MailImportResult[] byCaller = mailInterface.getMailImportResults();
                    mirs = new MailImportResult[alreadyImportedOnes.length + byCaller.length];
                    System.arraycopy(alreadyImportedOnes, 0, mirs, 0, alreadyImportedOnes.length);
                    System.arraycopy(byCaller, 0, mirs, alreadyImportedOnes.length, byCaller.length);
                }
            }
            final JSONArray respArray = new JSONArray();
            for (final MailImportResult m : mirs) {
                if (m.hasError()) {
                    final JSONObject responseObj = new JSONObject();
                    responseObj.put(FolderChildFields.FOLDER_ID, folder);
                    responseObj.put(MailImportResult.FILENAME, m.getMail().getFileName());
                    responseObj.put(MailImportResult.ERROR, m.getException().getMessage());
                    respArray.put(responseObj);
                } else {
                    final JSONObject responseObj = new JSONObject();
                    responseObj.put(FolderChildFields.FOLDER_ID, folder);
                    responseObj.put(DataFields.ID, m.getId());
                    respArray.put(responseObj);
                }
            }
            responseData = respArray;

        } catch (final OXException e) {
            LOG.error("", e);
            response.setException(e);
        } catch (final Exception e) {
            final OXException wrapper = getWrappingOXException(e);
            LOG.error("", wrapper);
            response.setException(wrapper);
        }
        // Close response and flush print writer
        response.setData(responseData == null ? JSONObject.NULL : responseData);
        response.setTimestamp(null);
        return response;
    }

    private JSONObject appendDraft(final ServerSession session, final int flags, final boolean force, final InternetAddress from, final MimeMessage m) throws OXException, OXException, JSONException {
        /*
         * Determine the account to transport with
         */
        final int accountId;
        {
            int accId;
            try {
                accId = resolveFrom2Account(session, from, true, !force);
            } catch (final OXException e) {
                if (MailExceptionCode.NO_TRANSPORT_SUPPORT.equals(e)) {
                    // Re-throw
                    throw e;
                }
                LOG.warn("{}. Using default account's transport.", e.getMessage());
                // Send with default account's transport provider
                accId = MailAccount.DEFAULT_ID;
            }
            accountId = accId;
        }
        /*
         * Missing "folder" element indicates to send given message via default mail account
         */
        final MailTransport transport = MailTransport.getInstance(session, accountId);
        try {
            /*
             * Send raw message source
             */
            if (MailProperties.getInstance().isAddClientIPAddress()) {
                MimeMessageFiller.addClientIPAddress(m, session);
            }
            /*
             * Get message bytes
             */
            MailMessage sentMail;
            OXException oxError = null;
            try {
                sentMail = transport.sendMailMessage(new ContentAwareComposedMailMessage(m, session, null), ComposeType.NEW);
            } catch (OXException e) {
                if (!MimeMailExceptionCode.SEND_FAILED_EXT.equals(e) && !MimeMailExceptionCode.SEND_FAILED_MSG_ERROR.equals(e)) {
                    throw e;
                }

                MailMessage ma = (MailMessage) e.getArgument("sent_message");
                if (null == ma) {
                    throw e;
                }

                sentMail = ma;
                oxError = e;
            }
            JSONObject responseData = null;
            /*
             * Set \Answered flag (if appropriate) & append to sent folder
             */
            MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess = null;
            try {
                mailAccess = MailAccess.getInstance(session, accountId);
                mailAccess.connect();
                /*
                 * Manually detect&set \Answered flag
                 */
                if (mailAccess.getMessageStorage() instanceof IMailMessageStorageExt) {
                    final List<String> lst = new ArrayList<String>(2);
                    {
                        final String inReplyTo = sentMail.getFirstHeader("In-Reply-To");
                        String references = sentMail.getFirstHeader("References");
                        if (equals(inReplyTo, references)) {
                            references = null;
                        }
                        if (null != inReplyTo) {
                            lst.add(inReplyTo);
                        }
                        if (null != references) {
                            lst.add(references);
                        }
                    }
                    if (!lst.isEmpty()) {
                        final IMailMessageStorageExt messageStorageExt = (IMailMessageStorageExt) mailAccess.getMessageStorage();
                        final MailMessage[] mails = messageStorageExt.getMessagesByMessageID(lst.toArray(new String[lst.size()]));
                        for (final MailMessage mail : mails) {
                            if (null != mail) {
                                setFlagReply(new MailPath(accountId, mail.getFolder(), mail.getMailId()), mailAccess);
                            }
                        }
                    }
                }
                /*
                 * Append to sent folder
                 */
                if (!session.getUserSettingMail().isNoCopyIntoStandardSentFolder()) {
                    /*
                     * Copy in sent folder allowed
                     */
                    final String sentFullname = MailFolderUtility.prepareMailFolderParam(mailAccess.getFolderStorage().getSentFolder()).getFullname();
                    final String[] uidArr;
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
                                accountId,
                                sentFullname,
                                session.getUserId(),
                                session.getContext().getContextId());
                        } catch (final OXException e) {
                            LOG.error("", e);
                        }
                    } catch (final OXException e) {
                        if (e.getMessage().indexOf("quota") != -1) {
                            throw MailExceptionCode.COPY_TO_SENT_FOLDER_FAILED_QUOTA.create(e, new Object[0]);
                        }
                        throw MailExceptionCode.COPY_TO_SENT_FOLDER_FAILED.create(e, new Object[0]);
                    }
                    if ((uidArr != null) && (uidArr[0] != null)) {
                        /*
                         * Mark appended sent mail as seen
                         */
                        mailAccess.getMessageStorage().updateMessageFlags(sentFullname, uidArr, MailMessage.FLAG_SEEN, true);
                    }
                    /*
                     * Compose JSON object
                     */
                    responseData = new JSONObject();
                    responseData.put(FolderChildFields.FOLDER_ID, MailFolderUtility.prepareFullname(MailAccount.DEFAULT_ID, sentFullname));
                    responseData.put(DataFields.ID, uidArr[0]);
                }
            } finally {
                if (null != mailAccess) {
                    mailAccess.close(true);
                }
            }
            if (null != oxError) {
                throw oxError;
            }
            return responseData;
        } catch (final MessagingException e) {
            throw MimeMailException.handleMessagingException(e);
        } catch (final RuntimeException e) {
            if ("com.sun.mail.util.MessageRemovedIOException".equals(e.getClass().getName())) {
                throw MailExceptionCode.MAIL_NOT_FOUND_SIMPLE.create(e);
            }
            throw MailExceptionCode.IO_ERROR.create(e, e.getMessage());
        } finally {
            transport.close();
        }
    }

    private static boolean equals(final String s1, final String s2) {
        if (null == s1) {
            if (null != s2) {
                return false;
            }
        } else if (!s1.equals(s2)) {
            return false;
        }
        return true;
    }

    private static final MailListField[] FIELDS_FLAGS = new MailListField[] { MailListField.FLAGS };

    private void setFlagReply(final MailPath path, final MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess) throws OXException {
        if (null == path) {
            LOG.warn("Missing msgref on reply. Corresponding mail cannot be marked as answered.", new Throwable());
            return;
        }
        /*
         * Mark referenced mail as answered
         */
        final String fullname = path.getFolder();
        final String[] uids = new String[] { path.getMailID() };
        mailAccess.getMessageStorage().updateMessageFlags(fullname, uids, MailMessage.FLAG_ANSWERED, true);
        try {
            /*
             * Update JSON cache
             */
            final Session session = mailAccess.getSession();
            final int userId = session.getUserId();
            final int contextId = session.getContextId();
            if (MailMessageCache.getInstance().containsFolderMessages(mailAccess.getAccountId(), fullname, userId, contextId)) {
                /*
                 * Update cache entries
                 */
                MailMessageCache.getInstance().updateCachedMessages(
                    uids,
                    mailAccess.getAccountId(),
                    fullname,
                    userId,
                    contextId,
                    FIELDS_FLAGS,
                    new Object[] { Integer.valueOf(MailMessage.FLAG_ANSWERED) });
            }
        } catch (final OXException e) {
            LOG.error("", e);
        }
    }

    public void actionPutCopyMail(final ServerSession session, final JSONWriter writer, final JSONObject jsonObj, final MailServletInterface mailInterface) throws JSONException {
        ResponseWriter.write(
            actionPutCopyMail(
                session,
                jsonObj.getString(ResponseFields.DATA),
                ParamContainer.getInstance(jsonObj),
                mailInterface),
                writer,
                localeFrom(session));
    }

    private final void actionPutCopyMail(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
        final ServerSession session = getSessionObject(req);
        try {
            ResponseWriter.write(
                actionPutCopyMail(session, getBody(req), ParamContainer.getInstance(req, resp), null),
                resp.getWriter(),
                localeFrom(session));
        } catch (final JSONException e) {
            final OXException oxe = OXJSONExceptionCodes.JSON_WRITE_ERROR.create(e, new Object[0]);
            LOG.error("", oxe);
            final Response response = new Response(session);
            response.setException(oxe);
            try {
                ResponseWriter.write(response, resp.getWriter(), localeFrom(session));
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
        final Response response = new Response(session);
        final OXJSONWriter jsonWriter = new OXJSONWriter();
        /*
         * Start response
         */
        jsonWriter.object();
        try {
            final String uid = paramContainer.checkStringParam(PARAMETER_ID);
            final String sourceFolder = paramContainer.checkStringParam(PARAMETER_FOLDERID);
            final String destFolder = new JSONObject(body).getString(FolderChildFields.FOLDER_ID);
            MailServletInterface mailInterface = mailInterfaceArg;
            boolean closeMailInterface = false;
            try {
                if (mailInterface == null) {
                    mailInterface = MailServletInterface.getInstance(session);
                    closeMailInterface = true;
                }
                final String msgUID = mailInterface.copyMessages(sourceFolder, destFolder, new String[] { uid }, false)[0];
                jsonWriter.key(FolderChildFields.FOLDER_ID).value(destFolder);
                jsonWriter.key(DataFields.ID).value(msgUID);
            } finally {
                if (closeMailInterface && mailInterface != null) {
                    mailInterface.close(true);
                }
            }
        } catch (final OXException e) {
            LOG.error("", e);
            response.setException(e);
        } catch (final Exception e) {
            final OXException wrapper = getWrappingOXException(e);
            LOG.error("", wrapper);
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

    public final void actionPutMoveMailMultiple(final ServerSession session, final JSONWriter writer, final String[] mailIDs, final String sourceFolder, final String destFolder, final MailServletInterface mailInteface) throws JSONException {
        actionPutMailMultiple(session, writer, mailIDs, sourceFolder, destFolder, true, mailInteface);
    }

    public final void actionPutCopyMailMultiple(final ServerSession session, final JSONWriter writer, final String[] mailIDs, final String srcFolder, final String destFolder, final MailServletInterface mailInterface) throws JSONException {
        actionPutMailMultiple(session, writer, mailIDs, srcFolder, destFolder, false, mailInterface);
    }

    public final void actionPutMailMultiple(final ServerSession session, final JSONWriter writer, final String[] mailIDs, final String srcFolder, final String destFolder, final boolean move, final MailServletInterface mailInterfaceArg) throws JSONException {
        try {
            MailServletInterface mailInterface = mailInterfaceArg;
            boolean closeMailInterface = false;
            try {
                if (mailInterface == null) {
                    mailInterface = MailServletInterface.getInstance(session);
                    closeMailInterface = true;
                }
                final String[] msgUIDs = mailInterface.copyMessages(srcFolder, destFolder, mailIDs, move);
                if (msgUIDs.length > 0) {
                    final Response response = new Response(session);
                    for (String msgUID : msgUIDs) {
                        response.reset();
                        final JSONObject jsonObj = new JSONObject();
                        // DataFields.ID | FolderChildFields.FOLDER_ID
                        jsonObj.put(FolderChildFields.FOLDER_ID, destFolder);
                        jsonObj.put(DataFields.ID, msgUID);
                        response.setData(jsonObj);
                        response.setTimestamp(null);
                        ResponseWriter.write(response, writer, localeFrom(session));
                    }
                } else {
                    final Response response = new Response(session);
                    response.setData(JSONObject.NULL);
                    response.setTimestamp(null);
                    ResponseWriter.write(response, writer, localeFrom(session));
                }
            } finally {
                if (closeMailInterface && mailInterface != null) {
                    mailInterface.close(true);
                }
            }
        } catch (final OXException e) {
            LOG.error("", e);
            final Response response = new Response(session);
            for (String mailID : mailIDs) {
                response.reset();
                response.setException(e);
                response.setData(JSONObject.NULL);
                response.setTimestamp(null);
                ResponseWriter.write(response, writer, localeFrom(session));
            }
        } catch (final Exception e) {
            final OXException wrapper = getWrappingOXException(e);
            LOG.error("", wrapper);
            final Response response = new Response(session);
            for (String mailID : mailIDs) {
                response.reset();
                response.setException(wrapper);
                response.setData(JSONObject.NULL);
                response.setTimestamp(null);
                ResponseWriter.write(response, writer, localeFrom(session));
            }
        }
    }

    public void actionGetGetMessageMultiple(ServerSession session, JSONWriter writer, String[] mailIDs, ParamContainer[] containers, String folder, MailServletInterface mailInterfaceArg) throws JSONException {
        try {
            MailServletInterface mailInterface = mailInterfaceArg;
            boolean closeMailInterface = false;
            try {
                if (mailInterface == null) {
                    mailInterface = MailServletInterface.getInstance(session);
                    closeMailInterface = true;
                }
                mailInterface.openFor(folder);
                MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess = mailInterface.getMailAccess();
                FullnameArgument fa = MailFolderUtility.prepareMailFolderParam(folder);
                MailMessage[] messages = mailAccess.getMessageStorage().getMessages(fa.getFullName(), mailIDs, new MailField[] { MailField.FULL});

                int length = messages.length;
                for (int i = 0; i < length; i++) {
                    MailMessage m = messages[i];
                    Response response;
                    if (null == m) {
                        response = new Response(session);
                        response.setException(MailExceptionCode.MAIL_NOT_FOUND.create(mailIDs[i], folder));
                    } else {
                        JSONObject jMail = MailConverter.getInstance().convertSingle4Get(m, containers[i], session, mailInterface);
                        response = new Response(session);
                        response.setData(jMail);
                    }
                    response.setTimestamp(null);
                    ResponseWriter.write(response, writer, localeFrom(session));
                }
            } finally {
                if (closeMailInterface && mailInterface != null) {
                    mailInterface.close(true);
                }
            }
        } catch (final OXException e) {
            LOG.error("", e);
            final Response response = new Response(session);
            for (String mailID : mailIDs) {
                response.reset();
                response.setException(e);
                response.setData(JSONObject.NULL);
                response.setTimestamp(null);
                ResponseWriter.write(response, writer, localeFrom(session));
            }
        } catch (final Exception e) {
            final OXException wrapper = getWrappingOXException(e);
            LOG.error("", wrapper);
            final Response response = new Response(session);
            for (String mailID : mailIDs) {
                response.reset();
                response.setException(wrapper);
                response.setData(JSONObject.NULL);
                response.setTimestamp(null);
                ResponseWriter.write(response, writer, localeFrom(session));
            }
        }
    }

    public void actionPutStoreFlagsMultiple(final ServerSession session, final JSONWriter writer, final String[] mailIDs, final String folder, final int flagsBits, final boolean flagValue, final MailServletInterface mailInterfaceArg) throws JSONException {
        try {
            MailServletInterface mailInterface = mailInterfaceArg;
            boolean closeMailInterface = false;
            try {
                if (mailInterface == null) {
                    mailInterface = MailServletInterface.getInstance(session);
                    closeMailInterface = true;
                }
                mailInterface.updateMessageFlags(folder, mailIDs, flagsBits, flagValue);
                final Response response = new Response(session);
                for (String mailID : mailIDs) {
                    response.reset();
                    final JSONObject jsonObj = new JSONObject();
                    // DataFields.ID | FolderChildFields.FOLDER_ID
                    jsonObj.put(FolderChildFields.FOLDER_ID, folder);
                    jsonObj.put(DataFields.ID, mailID);
                    response.setData(jsonObj);
                    response.setTimestamp(null);
                    ResponseWriter.write(response, writer, localeFrom(session));
                }
            } finally {
                if (closeMailInterface && mailInterface != null) {
                    mailInterface.close(true);
                }
            }
        } catch (final OXException e) {
            LOG.error("", e);
            final Response response = new Response(session);
            for (String mailID : mailIDs) {
                response.reset();
                response.setException(e);
                response.setData(JSONObject.NULL);
                response.setTimestamp(null);
                ResponseWriter.write(response, writer, localeFrom(session));
            }
        } catch (final Exception e) {
            final OXException wrapper = getWrappingOXException(e);
            LOG.error("", wrapper);
            final Response response = new Response(session);
            for (String mailID : mailIDs) {
                response.reset();
                response.setException(wrapper);
                response.setData(JSONObject.NULL);
                response.setTimestamp(null);
                ResponseWriter.write(response, writer, localeFrom(session));
            }
        }
    }

    public void actionPutColorLabelMultiple(final ServerSession session, final JSONWriter writer, final String[] mailIDs, final String folder, final int colorLabel, final MailServletInterface mailInterfaceArg) throws JSONException {
        try {
            MailServletInterface mailInterface = mailInterfaceArg;
            boolean closeMailInterface = false;
            try {
                if (mailInterface == null) {
                    mailInterface = MailServletInterface.getInstance(session);
                    closeMailInterface = true;
                }
                mailInterface.updateMessageColorLabel(folder, mailIDs, colorLabel);
                final Response response = new Response(session);
                for (String mailID : mailIDs) {
                    response.reset();
                    final JSONObject jsonObj = new JSONObject();
                    // DataFields.ID | FolderChildFields.FOLDER_ID
                    jsonObj.put(FolderChildFields.FOLDER_ID, folder);
                    jsonObj.put(DataFields.ID, mailID);
                    response.setData(jsonObj);
                    response.setTimestamp(null);
                    ResponseWriter.write(response, writer, localeFrom(session));
                }
            } finally {
                if (closeMailInterface && mailInterface != null) {
                    mailInterface.close(true);
                }
            }
        } catch (final OXException e) {
            LOG.error("", e);
            final Response response = new Response(session);
            for (String mailID : mailIDs) {
                response.reset();
                response.setException(e);
                response.setData(JSONObject.NULL);
                response.setTimestamp(null);
                ResponseWriter.write(response, writer, localeFrom(session));
            }
        } catch (final Exception e) {
            final OXException wrapper = getWrappingOXException(e);
            LOG.error("", wrapper);
            final Response response = new Response(session);
            for (String mailID : mailIDs) {
                response.reset();
                response.setException(wrapper);
                response.setData(JSONObject.NULL);
                response.setTimestamp(null);
                ResponseWriter.write(response, writer, localeFrom(session));
            }
        }
    }

    public void actionPutAttachment(final ServerSession session, final JSONWriter writer, final JSONObject jsonObj, final MailServletInterface mi) throws JSONException {
        ResponseWriter.write(
            actionPutAttachment(
                session,
                jsonObj.getString(ResponseFields.DATA),
                ParamContainer.getInstance(jsonObj),
                mi),
                writer,
                localeFrom(session));
    }

    private final void actionPutAttachment(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
        final ServerSession session = getSessionObject(req);
        try {
            ResponseWriter.write(
                actionPutAttachment(session, getBody(req), ParamContainer.getInstance(req, resp), null),
                resp.getWriter(),
                localeFrom(session));
        } catch (final JSONException e) {
            final OXException oxe = OXJSONExceptionCodes.JSON_WRITE_ERROR.create(e, new Object[0]);
            LOG.error("", oxe);
            final Response response = new Response(session);
            response.setException(oxe);
            try {
                ResponseWriter.write(response, resp.getWriter(), localeFrom(session));
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
        final Response response = new Response(session);
        final OXJSONWriter jsonWriter = new OXJSONWriter();
        /*
         * Start response
         */
        jsonWriter.array();
        try {
            String folderPath = paramContainer.checkStringParam(PARAMETER_FOLDERID);
            String uid = paramContainer.checkStringParam(PARAMETER_ID);
            String sequenceId = paramContainer.checkStringParam(PARAMETER_MAILATTCHMENT);
            String destFolderIdentifier = paramContainer.checkStringParam(PARAMETER_DESTINATION_FOLDER);
            MailServletInterface mailInterface = mailInterfaceArg;
            boolean closeMailInterface = false;
            ServerServiceRegistry serviceRegistry = ServerServiceRegistry.getInstance();
            IDBasedFileAccess fileAccess = serviceRegistry.getService(IDBasedFileAccessFactory.class).createAccess(session);
            boolean performRollback = false;
            try {
                if (!session.getUserPermissionBits().hasInfostore()) {
                    throw MailExceptionCode.NO_MAIL_ACCESS.create();
                }
                if (mailInterface == null) {
                    mailInterface = MailServletInterface.getInstance(session);
                    closeMailInterface = true;
                }
                final MailPart mailPart = mailInterface.getMessageAttachment(folderPath, uid, sequenceId, false);
                if (mailPart == null) {
                    throw MailExceptionCode.NO_ATTACHMENT_FOUND.create(sequenceId);
                }
                final String destFolderID = destFolderIdentifier;
                /*
                 * Create document's meta data
                 */
                final FileMetadataParserService parser = serviceRegistry.getService(FileMetadataParserService.class, true);
                final JSONObject jsonFileObject = new JSONObject(body);
                final File file = parser.parse(jsonFileObject);
                final List<Field> fields = parser.getFields(jsonFileObject);
                final Set<Field> set = EnumSet.copyOf(fields);
                if (!set.contains(Field.FILENAME)) {
                    file.setFileName(mailPart.getFileName());
                }
                file.setFileMIMEType(mailPart.getContentType().getBaseType());
                /*
                 * Since file's size given from IMAP server is just an estimation and therefore does not exactly match the file's size a
                 * future file access via webdav can fail because of the size mismatch. Thus set the file size to 0 to make the infostore
                 * measure the size.
                 */
                file.setFileSize(0);
                if (!set.contains(Field.TITLE)) {
                    file.setTitle(mailPart.getFileName());
                }
                file.setFolderId(destFolderID);
                /*
                 * Start writing to infostore folder
                 */
                fileAccess.startTransaction();
                performRollback = true;
                fileAccess.saveDocument(file, mailPart.getInputStream(), System.currentTimeMillis(), fields);
                fileAccess.commit();
            } catch (final Exception e) {
                if (performRollback) {
                    fileAccess.rollback();
                }
                throw e;
            } finally {
                if (closeMailInterface && mailInterface != null) {
                    mailInterface.close(true);
                }
                if (fileAccess != null) {
                    fileAccess.finish();
                }
            }
        } catch (final OXException e) {
            LOG.error("", e);
            response.setException(e);
        } catch (final Exception e) {
            final OXException wrapper = getWrappingOXException(e);
            LOG.error("", wrapper);
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
        ResponseWriter.write(
            actionPutReceiptAck(
                session,
                jsonObj.getString(ResponseFields.DATA),
                ParamContainer.getInstance(jsonObj),
                mi),
                writer,
                localeFrom(session));
    }

    private final void actionPutReceiptAck(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
        final ServerSession session = getSessionObject(req);
        try {
            ResponseWriter.write(
                actionPutReceiptAck(session, getBody(req), ParamContainer.getInstance(req, resp), null),
                resp.getWriter(),
                localeFrom(session));
        } catch (final JSONException e) {
            final OXException oxe = OXJSONExceptionCodes.JSON_WRITE_ERROR.create(e, new Object[0]);
            LOG.error("", oxe);
            final Response response = new Response(session);
            response.setException(oxe);
            try {
                ResponseWriter.write(response, resp.getWriter(), localeFrom(session));
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
        final Response response = new Response(session);
        /*
         * Start response
         */
        try {
            final JSONObject bodyObj = new JSONObject(body);
            final String folderPath = bodyObj.has(PARAMETER_FOLDERID) ? bodyObj.getString(PARAMETER_FOLDERID) : null;
            if (null == folderPath) {
                throw MailExceptionCode.MISSING_PARAM.create(PARAMETER_FOLDERID);
            }
            final String uid = bodyObj.has(PARAMETER_ID) ? bodyObj.getString(PARAMETER_ID) : null;
            if (null == uid) {
                throw MailExceptionCode.MISSING_PARAM.create(PARAMETER_ID);
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
        } catch (final OXException e) {
            LOG.error("", e);
            response.setException(e);
        } catch (final Exception e) {
            final OXException wrapper = getWrappingOXException(e);
            LOG.error("", wrapper);
            response.setException(wrapper);
        }
        /*
         * Close response and flush print writer
         */
        response.setData(JSONObject.NULL);
        response.setTimestamp(null);
        return response;
    }

    private static String checkStringParam(final HttpServletRequest req, final String paramName) throws OXException {
        final String paramVal = req.getParameter(paramName);
        if (paramVal == null || paramVal.length() == 0 || STR_NULL.equals(paramVal)) {
            throw MailExceptionCode.MISSING_FIELD.create(paramName);
        }
        return paramVal;
    }

    private static String[] checkStringArrayParam(final HttpServletRequest req, final String paramName) throws OXException {
        final String tmp = req.getParameter(paramName);
        if (tmp == null || tmp.length() == 0 || STR_NULL.equals(tmp)) {
            throw MailExceptionCode.MISSING_FIELD.create(paramName);
        }
        return SPLIT.split(tmp, 0);
    }

    protected boolean sendMessage(final HttpServletRequest req) {
        return req.getParameter(PARAMETER_ACTION) != null && req.getParameter(PARAMETER_ACTION).equalsIgnoreCase(ACTION_SEND);
    }

    protected boolean appendMessage(final HttpServletRequest req) {
        return req.getParameter(PARAMETER_ACTION) != null && req.getParameter(PARAMETER_ACTION).equalsIgnoreCase(ACTION_APPEND);
    }

    @Override
    protected boolean hasModulePermission(final ServerSession session) {
        return session.getUserPermissionBits().hasWebMail();
    }

    private static String getDefaultSendAddress(final ServerSession session) throws OXException {
        final MailAccountStorageService storageService = ServerServiceRegistry.getInstance().getService(
            MailAccountStorageService.class,
            true);
        return storageService.getDefaultMailAccount(session.getUserId(), session.getContextId()).getPrimaryAddress();
    }

    private static int resolveFrom2Account(final ServerSession session, final InternetAddress from, final boolean checkTransportSupport, final boolean checkFrom) throws OXException, OXException {
        /*
         * Resolve "From" to proper mail account to select right transport server
         */
        int accountId;
        {
            final MailAccountStorageService storageService = ServerServiceRegistry.getInstance().getService(
                MailAccountStorageService.class,
                true);
            final int user = session.getUserId();
            final int cid = session.getContextId();
            if (null == from) {
                accountId = MailAccount.DEFAULT_ID;
            } else {
                accountId = storageService.getByPrimaryAddress(from.getAddress(), user, cid);
                if (accountId == -1) {
                    // Retry with IDN representation
                    accountId = storageService.getByPrimaryAddress(IDNA.toIDN(from.getAddress()), user, cid);
                }
            }
            if (accountId != -1) {
                if (!session.getUserPermissionBits().isMultipleMailAccounts() && accountId != MailAccount.DEFAULT_ID) {
                    throw MailAccountExceptionCodes.NOT_ENABLED.create(Integer.valueOf(user), Integer.valueOf(cid));
                }
                if (checkTransportSupport) {
                    final MailAccount account = storageService.getMailAccount(accountId, user, cid);
                    // Check if determined account supports mail transport
                    if (null == account.getTransportServer()) {
                        // Account does not support mail transport
                        throw MailExceptionCode.NO_TRANSPORT_SUPPORT.create(account.getName(), Integer.valueOf(accountId));
                    }
                }
            }
        }
        if (accountId == -1) {
            if (checkFrom && null != from) {
                /*
                 * Check aliases
                 */
                try {
                    final Set<InternetAddress> validAddrs = new HashSet<InternetAddress>(4);
                    final User user = session.getUser();
                    final String[] aliases = user.getAliases();
                    for (final String alias : aliases) {
                        validAddrs.add(new QuotedInternetAddress(alias));
                    }
                    if (MailProperties.getInstance().isSupportMsisdnAddresses()) {
                        MsisdnUtility.addMsisdnAddress(validAddrs, session);
                        final String address = from.getAddress();
                        final int pos = address.indexOf('/');
                        if (pos > 0) {
                            from.setAddress(address.substring(0, pos));
                        }
                    }
                    if (!validAddrs.contains(from)) {
                        throw MailExceptionCode.INVALID_SENDER.create(from.toString());
                    }
                } catch (final AddressException e) {
                    throw MimeMailException.handleMessagingException(e);
                }
            }
            accountId = MailAccount.DEFAULT_ID;
        }
        return accountId;
    }

    private static interface StringProvider {

        public String getString() throws IOException;

        public boolean isEmpty();
    }

    private static final class SimpleStringProvider implements StringProvider {

        private final String string;

        public SimpleStringProvider(final String string) {
            super();
            this.string = string;
        }

        @Override
        public String getString() throws IOException {
            return string;
        }

        @Override
        public boolean isEmpty() {
            return null == string || 0 == string.length();
        }

    }

    // getBody
    private static final class HTTPRequestStringProvider implements StringProvider {

        private final HttpServletRequest req;

        // private String string;

        public HTTPRequestStringProvider(final HttpServletRequest req) {
            super();
            this.req = req;
        }

        @Override
        public String getString() throws IOException {
            return AJAXServlet.getBody(req);
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

    }

    private static boolean startsWith(final char startingChar, final String toCheck, final boolean ignoreHeadingWhitespaces) {
        if (null == toCheck) {
            return false;
        }
        final int len = toCheck.length();
        if (len <= 0) {
            return false;
        }
        if (!ignoreHeadingWhitespaces) {
            return startingChar == toCheck.charAt(0);
        }
        int i = 0;
        while (i < len && Strings.isWhitespace(toCheck.charAt(i))) {
            i++;
        }
        if (i >= len) {
            return false;
        }
        return startingChar == toCheck.charAt(i);
    }

    private static JSONArray toJSONArray(final String toCheck) {
        if (!startsWith('[', toCheck, true)) {
            return null;
        }
        try {
            return new JSONArray(toCheck);
        } catch (final JSONException e) {
            return null;
        }
    }

    private static String getSimpleName(final String fullname) {
        if (null == fullname) {
            return null;
        }
        final int len = fullname.length();
        int pos = fullname.lastIndexOf('.');
        if (pos >= 0 && pos < len - 1) {
            return fullname.substring(pos + 1);
        }
        pos = fullname.lastIndexOf('/');
        if (pos >= 0 && pos < len - 1) {
            return fullname.substring(pos + 1);
        }
        return fullname;
    }

}
