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

package com.openexchange.mail.json.converters;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;
import java.util.regex.Pattern;
import javax.mail.MessagingException;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONValue;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.Mail;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.helper.ParamContainer;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestDataTools;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.AJAXRequestResult.ResultType;
import com.openexchange.ajax.requesthandler.Converter;
import com.openexchange.ajax.requesthandler.ResultConverter;
import com.openexchange.exception.OXException;
import com.openexchange.json.OXJSONWriter;
import com.openexchange.json.cache.JsonCacheService;
import com.openexchange.json.cache.JsonCaches;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.MailField;
import com.openexchange.mail.MailFields;
import com.openexchange.mail.MailJSONField;
import com.openexchange.mail.MailListField;
import com.openexchange.mail.MailServletInterface;
import com.openexchange.mail.config.MailProperties;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.dataobjects.MailThread;
import com.openexchange.mail.dataobjects.MailThreads;
import com.openexchange.mail.dataobjects.ThreadedStructure;
import com.openexchange.mail.json.MailActionConstants;
import com.openexchange.mail.json.MailRequest;
import com.openexchange.mail.json.MailRequestSha1Calculator;
import com.openexchange.mail.json.actions.AbstractMailAction;
import com.openexchange.mail.json.utils.Column;
import com.openexchange.mail.json.writer.MessageWriter;
import com.openexchange.mail.json.writer.MessageWriterParams;
import com.openexchange.mail.json.writer.MessageWriter.MailFieldWriter;
import com.openexchange.mail.mime.MimeFilter;
import com.openexchange.mail.mime.MimeMailException;
import com.openexchange.mail.usersetting.UserSettingMail;
import com.openexchange.mail.utils.DisplayMode;
import com.openexchange.tools.TimeZoneUtils;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link MailConverter}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MailConverter implements ResultConverter, MailActionConstants {

    private static final org.slf4j.Logger LOG =
        org.slf4j.LoggerFactory.getLogger(MailConverter.class);

    private static final MailConverter INSTANCE = new MailConverter();

    /**
     * Gets the instance
     *
     * @return The instance
     */
    public static MailConverter getInstance() {
        return INSTANCE;
    }

    /**
     * Initializes a new {@link MailConverter}.
     */
    private MailConverter() {
        super();
    }

    @Override
    public String getInputFormat() {
        return "mail";
    }

    @Override
    public String getOutputFormat() {
        return "apiResponse";
    }

    @Override
    public Quality getQuality() {
        return Quality.GOOD;
    }

    @Override
    public void convert(final AJAXRequestData requestData, final AJAXRequestResult result, final ServerSession session, final Converter converter) throws OXException {
        convert2JSON(requestData, result, session);
        final Response response = new Response(session);
        response.setData(result.getResultObject());
        response.setTimestamp(result.getTimestamp());
        response.setProperties(result.getResponseProperties());
        final Collection<OXException> warnings = result.getWarnings();
        if (null != warnings && !warnings.isEmpty()) {
            for (final OXException warning : warnings) {
                response.addWarning(warning);
            }
        }
        result.setResultObject(response);
    }

    /**
     * Converts to JSON output format.
     *
     * @param requestData The AJAX request data
     * @param result The AJAX result
     * @param session The associated session
     * @throws OXException If an error occurs
     */
    public void convert2JSON(final AJAXRequestData requestData, final AJAXRequestResult result, final ServerSession session) throws OXException {
        try {
            final Object resultObject = result.getResultObject();
            if (null == resultObject) {
                LOG.warn("Result object is null.");
                result.setResultObject(JSONObject.NULL, "json");
                return;
            }
            final String action = requestData.getParameter("action");
            if (resultObject instanceof MailMessage) {
                final MailMessage mail = (MailMessage) resultObject;
                if (AJAXServlet.ACTION_GET.equals(action)) {
                    convertSingle4Get(mail, requestData, result, session);
                } else {
                    convertSingle(mail, requestData, result, session);
                }
            } else if (resultObject instanceof ThreadedStructure) {
                convertThreadStructure((ThreadedStructure) resultObject, requestData, result, session);
            } else if (resultObject instanceof MailThreads) {
                convertMailThreads((MailThreads) resultObject, requestData, result, session);
            } else {
                @SuppressWarnings("unchecked") final Collection<MailMessage> mails = (Collection<MailMessage>) resultObject;
                if (AJAXServlet.ACTION_ALL.equalsIgnoreCase(action)) {
                    convertMultiple4All(mails, requestData, result, session);
                } else if (AJAXServlet.ACTION_LIST.equalsIgnoreCase(action)) {
                    convertMultiple4List(mails, requestData, result, session);
                } else {
                    convertMultiple4List(mails, requestData, result, session);
                }
            }
        } catch (final JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        }
    }

    private void convertMailThreads(final MailThreads mailThreads, final AJAXRequestData requestData, final AJAXRequestResult result, final ServerSession session) throws OXException, JSONException {
        List<Column> columns = MailRequest.requireColumnsAndHeaders(requestData).getColumns();
        String tmp = requestData.getParameter(Mail.PARAMETER_TIMEZONE);
        TimeZone timeZone = com.openexchange.java.Strings.isEmpty(tmp) ? TimeZoneUtils.getTimeZone(session.getUser().getTimeZone()) : TimeZoneUtils.getTimeZone(tmp.trim());
        tmp = null;

        MailFields mailFields = new MailFields();
        List<MailFieldWriter> writers = new ArrayList<MailFieldWriter>(columns.size());
        for (Column column : columns) {
            MailFieldWriter fieldWriter;
            if (column.getField() > 0) {
                fieldWriter = MessageWriter.getMailFieldWriter(MailListField.getField(column.getField()));
                MailField mailField = MailField.getField(column.getField());
                if (null != mailField) {
                    mailFields.add(mailField);
                }
            } else {
                fieldWriter = MessageWriter.getHeaderFieldWriter(column.getHeader());
            }
            writers.add(fieldWriter);
        }

        OXJSONWriter jsonWriter = new OXJSONWriter();
        jsonWriter.array();
        try {
            final int userId = session.getUserId();
            final int contextId = session.getContextId();
            for (MailThread mailThread : mailThreads.getMailThreads()) {
                JSONObject jo = new JSONObject(32);
                writeMailThread(mailThread, jo, writers, mailFields, userId, contextId, timeZone);
                jsonWriter.value(jo);
            }
        } finally {
            jsonWriter.endArray();
        }
        final JSONValue newJsonValue = jsonWriter.getObject();
        result.setResultObject(newJsonValue, "json");
    }

    private void writeMailThread(MailThread mailThread, JSONObject jMail, List<MailFieldWriter> writers, MailFields mailFields, int userId, int contextId, TimeZone optTimeZone) throws OXException, JSONException {
        MailMessage rootMessage = mailThread.getParent();
        int accountId = rootMessage.getAccountId();
        for (MailFieldWriter writer : writers) {
            writer.writeField(jMail, rootMessage, 0, true, accountId, userId, contextId, optTimeZone);
        }
        // Add child nodes
        List<MailThread> subthreads = mailThread.getChildren();
        JSONArray jChildMessages = new JSONArray(subthreads.size());
        writeSubThreads(subthreads, accountId, jChildMessages, writers, mailFields, userId, contextId, optTimeZone);
        jMail.put("thread", jChildMessages);
    }

    private void writeSubThreads(List<MailThread> subthreads, int accountId, JSONArray jChildMessages, List<MailFieldWriter> writers, MailFields mailFields, int userId, int contextId, TimeZone optTimeZone) throws OXException {
        for (MailThread subthread : subthreads) {
            MailMessage mail = subthread.getParent();
            if (seemsValid(mail, mailFields)) {
                JSONObject jChild = new JSONObject(writers.size());
                for (MailFieldWriter writer : writers) {
                    writer.writeField(jChild, mail, 0, true, accountId, userId, contextId, optTimeZone);
                }
                jChildMessages.put(jChild);
            }
            List<MailThread> subSubThreads = subthread.getChildren();
            if (null != subSubThreads && !subSubThreads.isEmpty()) {
                writeSubThreads(subSubThreads, accountId, jChildMessages, writers, mailFields, userId, contextId, optTimeZone);
            }
        }
    }

    private void convertThreadStructure(final ThreadedStructure structure, final AJAXRequestData requestData, final AJAXRequestResult result, final ServerSession session) throws OXException, JSONException {
        /*-
         * The data UI needs looks like this:
         *
         * [{ id: 1234, folder_id: 'default0/INBOX', thread: [{ id: 1234, folder_id: 'default0/INBOX'}, {id: 4711, folder_id: 'default0/INBOX' }, ...]
         */
        List<Column> columns = MailRequest.requireColumnsAndHeaders(requestData).getColumns();
        String tmp = requestData.getParameter(Mail.PARAMETER_TIMEZONE);
        final TimeZone timeZone = com.openexchange.java.Strings.isEmpty(tmp) ? TimeZoneUtils.getTimeZone(session.getUser().getTimeZone()) : TimeZoneUtils.getTimeZone(tmp.trim());
        tmp = null;
        /*
         * Pre-Select field writers
         */
        MailFields mailFields = new MailFields();
        List<MailFieldWriter> writers = new ArrayList<MailFieldWriter>(columns.size());
        for (Column column : columns) {
            MailFieldWriter fieldWriter;
            if (column.getField() > 0) {
                fieldWriter = MessageWriter.getMailFieldWriter(MailListField.getField(column.getField()));
                MailField mailField = MailField.getField(column.getField());
                if (null != mailField) {
                    mailFields.add(mailField);
                }
            } else {
                fieldWriter = MessageWriter.getHeaderFieldWriter(column.getHeader());
            }
            writers.add(fieldWriter);
        }
        /*
         * Get mail interface
         */
        final OXJSONWriter jsonWriter = new OXJSONWriter();
        /*
         * Start response
         */
        final boolean writeThreadAsObjects = !requestData.isSet("writeThreadAsObjects") || AJAXRequestDataTools.parseBoolParameter("writeThreadAsObjects", requestData);
        final boolean containsMultipleFolders = containsMultipleFolders(structure, new HashSet<String>(2));
        jsonWriter.array();
        try {
            final int userId = session.getUserId();
            final int contextId = session.getContextId();
            for (final List<MailMessage> mails : structure.getMails()) {
                if (mails != null && !mails.isEmpty()) {
                    final JSONObject jo = new JSONObject(32);
                    writeThreadSortedMail(mails, jo, writers, mailFields, containsMultipleFolders, writeThreadAsObjects, userId, contextId, timeZone);
                    jsonWriter.value(jo);
                }
            }
        } finally {
            jsonWriter.endArray();
        }
        final JSONValue newJsonValue = jsonWriter.getObject();
        result.setResultObject(newJsonValue, "json");
        /*
         * Put to cache if differs
         */
        final MailRequest req = new MailRequest(requestData, session);
        final boolean cache = req.optBool("cache", false);
        if (cache) {
            final JsonCacheService jsonCache = JsonCaches.getCache();
            final MailRequestSha1Calculator sha1Calculator = req.getRequest().getProperty("mail.sha1calc");
            if (null != jsonCache && null != sha1Calculator) {
                final String sha1Sum = sha1Calculator.getSha1For(req);
                final String id = "com.openexchange.mail." + sha1Sum;
                final JSONValue jsonValue = requestData.getProperty(id);
                if (!JsonCaches.areEqual(jsonValue, newJsonValue)) {
                    final ServerSession ses = req.getSession();
                    if (null == jsonValue) {
                        jsonCache.setIfDifferent(id, newJsonValue, result.getDuration(), ses.getUserId(), ses.getContextId());
                    } else {
                        jsonCache.set(id, newJsonValue, result.getDuration(), ses.getUserId(), ses.getContextId());
                    }
                }
            }
        }
    }

    private static boolean containsMultipleFolders(final ThreadedStructure structure, final Set<String> fullNames) {
        for (final List<MailMessage> mails : structure.getMails()) {
            for (final MailMessage mailMessage : mails) {
                if (fullNames.add(mailMessage.getFolder()) && fullNames.size() > 1) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Enforces to write thread as JSON objects.
     */
    private static boolean writeThreadAsObjects() {
        return false;
    }

    private static final MailFieldWriter[] WRITER_IDS = MessageWriter.getMailFieldWriters(new MailListField[] { MailListField.ID, MailListField.FOLDER_ID });

    private void writeThreadSortedMail(List<MailMessage> mails, JSONObject jMail, List<MailFieldWriter> writers, MailFields mailFields, boolean containsMultipleFolders, boolean writeThreadAsObjects, int userId, int contextId, TimeZone optTimeZone) throws OXException, JSONException {
        final MailMessage rootMessage = mails.get(0);
        int accountId = rootMessage.getAccountId();
        for (MailFieldWriter writer : writers) {
            writer.writeField(jMail, rootMessage, 0, true, accountId, userId, contextId, optTimeZone);
        }
        int unreadCount = 0;
        // Add child nodes
        final JSONArray jChildMessages = new JSONArray(mails.size());
        if (writeThreadAsObjects) {
            for (final MailMessage child : mails) {
                if (seemsValid(child, mailFields)) {
                    JSONObject jChild = new JSONObject(writers.size());
                    accountId = child.getAccountId();
                    for (MailFieldWriter writer : writers) {
                        writer.writeField(jChild, child, 0, true, accountId, userId, contextId, optTimeZone);
                    }
                    jChildMessages.put(jChild);
                    /*
                     * Count unread messages in this thread structure
                     */
                    if (!child.isSeen()) {
                        unreadCount++;
                    }
                }
            }
        } else {
            if (containsMultipleFolders) {
                final StringBuilder sb = new StringBuilder(16);
                final char defaultSeparator = MailProperties.getInstance().getDefaultSeparator();
                for (final MailMessage child : mails) {
                    if (seemsValid(child, mailFields)) {
                        sb.setLength(0);
                        jChildMessages.put(sb.append(child.getFolder()).append(defaultSeparator).append(child.getMailId()).toString());
                        /*
                         * Count unread messages in this thread structure
                         */
                        if (!child.isSeen()) {
                            unreadCount++;
                        }
                    }
                }
            } else {
                for (final MailMessage child : mails) {
                    if (seemsValid(child, mailFields)) {
                        jChildMessages.put(child.getMailId());
                        /*
                         * Count unread messages in this thread structure
                         */
                        if (!child.isSeen()) {
                            unreadCount++;
                        }
                    }
                }
            }
        }
        jMail.put("thread", jChildMessages);
        jMail.put("unreadCount", unreadCount);
    }

    private void convertMultiple4List(final Collection<MailMessage> mails, final AJAXRequestData requestData, final AJAXRequestResult result, final ServerSession session) throws OXException, JSONException {
        List<Column> columns = MailRequest.requireColumnsAndHeaders(requestData).getColumns();
        String tmp = requestData.getParameter(Mail.PARAMETER_TIMEZONE);
        TimeZone timeZone = com.openexchange.java.Strings.isEmpty(tmp) ? TimeZoneUtils.getTimeZone(session.getUser().getTimeZone()) : TimeZoneUtils.getTimeZone(tmp.trim());
        tmp = null;
        /*
         * Pre-Select field writers
         */
        MailFields mailFields = new MailFields();
        List<MailFieldWriter> writers = new ArrayList<MailFieldWriter>(columns.size());
        for (Column column : columns) {
            MailFieldWriter fieldWriter;
            if (column.getField() > 0) {
                fieldWriter = MessageWriter.getMailFieldWriter(MailListField.getField(column.getField()));
                MailField mailField = MailField.getField(column.getField());
                if (null != mailField) {
                    mailFields.add(mailField);
                }
            } else {
                fieldWriter = MessageWriter.getHeaderFieldWriter(column.getHeader());
            }
            writers.add(fieldWriter);
        }
        /*
         * Get mail interface
         */
        final OXJSONWriter jsonWriter = new OXJSONWriter();
        /*
         * Start response
         */
        jsonWriter.array();
        try {
            final int userId = session.getUserId();
            final int contextId = session.getContextId();
            for (final MailMessage mail : mails) {
                if (seemsValid(mail, mailFields)) {
                    JSONArray ja = new JSONArray(writers.size());
                    int accountId = mail.getAccountId();
                    for (MailFieldWriter writer : writers) {
                        writer.writeField(ja, mail, 0, false, accountId, userId, contextId, timeZone);
                    }
                    jsonWriter.value(ja);
                }
            }
        } finally {
            jsonWriter.endArray();
        }
        result.setResultObject(jsonWriter.getObject(), "json");
    }

    private void convertMultiple4All(final Collection<MailMessage> mails, final AJAXRequestData requestData, final AJAXRequestResult result, final ServerSession session) throws OXException, JSONException {
        List<Column> columns = MailRequest.requireColumnsAndHeaders(requestData).getColumns();
        String sort = requestData.getParameter(AJAXServlet.PARAMETER_SORT);
        String tmp = requestData.getParameter(Mail.PARAMETER_TIMEZONE);
        TimeZone timeZone = com.openexchange.java.Strings.isEmpty(tmp) ? TimeZoneUtils.getTimeZone(session.getUser().getTimeZone()) : TimeZoneUtils.getTimeZone(tmp.trim());
        tmp = null;
        getMailInterface(requestData, session);
        /*
         * Pre-Select field writers
         */
        MailFields mailFields = new MailFields();
        List<MailFieldWriter> writers = new ArrayList<MailFieldWriter>(columns.size());
        for (Column column : columns) {
            MailFieldWriter fieldWriter;
            if (column.getField() > 0) {
                fieldWriter = MessageWriter.getMailFieldWriter(MailListField.getField(column.getField()));
                MailField mailField = MailField.getField(column.getField());
                if (null != mailField) {
                    mailFields.add(mailField);
                }
            } else {
                fieldWriter = MessageWriter.getHeaderFieldWriter(column.getHeader());
            }
            writers.add(fieldWriter);
        }
        final int userId = session.getUserId();
        final int contextId = session.getContextId();
        final OXJSONWriter jsonWriter = new OXJSONWriter();
        /*
         * Start response
         */
        jsonWriter.array();
        try {
            /*
             * Check for thread-sort
             */
            if (("thread".equalsIgnoreCase(sort))) {
                for (final MailMessage mail : mails) {
                    if (seemsValid(mail, mailFields)) {
                        final JSONArray ja = new JSONArray(writers.size());
                        final int accountId = mail.getAccountId();
                        for (final MailFieldWriter writer : writers) {
                            writer.writeField(ja, mail, mail.getThreadLevel(), false, accountId, userId, contextId, timeZone);
                        }
                        jsonWriter.value(ja);
                    }
                }
            } else {
                /*
                 * Get iterator
                 */
                for (final MailMessage mail : mails) {
                    if (seemsValid(mail, mailFields)) {
                        final JSONArray ja = new JSONArray(writers.size());
                        final int accountId = mail.getAccountId();
                        for (final MailFieldWriter writer : writers) {
                            writer.writeField(ja, mail, 0, false, accountId, userId, contextId, timeZone);
                        }
                        jsonWriter.value(ja);
                    }
                }
            }
        } finally {
            jsonWriter.endArray();
        }
        final JSONValue newJsonValue = jsonWriter.getObject();
        result.setResultObject(newJsonValue, "json");
        /*
         * Put to cache if differs
         */
        final MailRequest req = new MailRequest(requestData, session);
        final boolean cache = req.optBool("cache", false);
        if (cache) {
            final JsonCacheService jsonCache = JsonCaches.getCache();
            final MailRequestSha1Calculator sha1Calculator = req.getRequest().getProperty("mail.sha1calc");
            if (null != jsonCache && null != sha1Calculator) {
                final String sha1Sum = sha1Calculator.getSha1For(req);
                final String id = "com.openexchange.mail." + sha1Sum;
                final JSONValue jsonValue = requestData.getProperty(id);
                if (!JsonCaches.areEqual(jsonValue, newJsonValue)) {
                    final ServerSession ses = req.getSession();
                    if (null == jsonValue) {
                        jsonCache.setIfDifferent(id, newJsonValue, result.getDuration(), ses.getUserId(), ses.getContextId());
                    } else {
                        jsonCache.set(id, newJsonValue, result.getDuration(), ses.getUserId(), ses.getContextId());
                    }
                }
            }
        }
    }

    private void convertSingle4Get(MailMessage mail, AJAXRequestData requestData, AJAXRequestResult result, ServerSession session) throws OXException {
        JSONObject jMail = convertSingle4Get(mail, ParamContainer.getInstance(requestData), session, getMailInterface(requestData, session));
        if (null == jMail) {
            result.setResultObject(null, "native");
            result.setType(ResultType.DIRECT);
        } else {
            result.setResultObject(jMail, "json");
        }
    }

    private static final Pattern SPLIT = Pattern.compile(" *, *");

    /**
     * Converts given mail
     *
     * @param mail The mail
     * @param paramContainer The parameter container
     * @param session The associated session
     * @param mailInterface The mail interface
     * @throws OXException If operation fails
     */
    public JSONObject convertSingle4Get(MailMessage mail, ParamContainer paramContainer, ServerSession session, MailServletInterface mailInterface) throws OXException {
        String tmp = paramContainer.getStringParam(Mail.PARAMETER_EDIT_DRAFT);
        final boolean editDraft = ("1".equals(tmp) || Boolean.parseBoolean(tmp));
        tmp = paramContainer.getStringParam(Mail.PARAMETER_VIEW);
        final String view = null == tmp ? null : tmp.toLowerCase(Locale.ENGLISH);
        tmp = paramContainer.getStringParam(Mail.PARAMETER_UNSEEN);
        final boolean unseen = (tmp != null && ("1".equals(tmp) || Boolean.parseBoolean(tmp)));
        tmp = paramContainer.getStringParam(Mail.PARAMETER_TIMEZONE);
        final TimeZone timeZone = com.openexchange.java.Strings.isEmpty(tmp) ? TimeZoneUtils.getTimeZone(session.getUser().getTimeZone()) : TimeZoneUtils.getTimeZone(tmp.trim());
        tmp = paramContainer.getStringParam("token");
        final boolean token = (tmp != null && ("1".equals(tmp) || Boolean.parseBoolean(tmp)));
        tmp = paramContainer.getStringParam("ttlMillis");
        int ttlMillis;
        try {
            ttlMillis = (tmp == null ? -1 : Integer.parseInt(tmp.trim()));
        } catch (final NumberFormatException e) {
            ttlMillis = -1;
        }
        tmp = paramContainer.getStringParam("embedded");
        final boolean embedded = (tmp != null && ("1".equals(tmp) || Boolean.parseBoolean(tmp)));
        tmp = paramContainer.getStringParam("includePlainText");
        final boolean includePlainText = (tmp != null && ("1".equals(tmp) || Boolean.parseBoolean(tmp)));
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
        final UserSettingMail usmNoSave = session.getUserSettingMail().clone();
        /*
         * Deny saving for this request-specific settings
         */
        usmNoSave.setNoSave(true);
        /*
         * Overwrite settings with request's parameters
         */
        final DisplayMode displayMode = AbstractMailAction.detectDisplayMode(editDraft, view, usmNoSave);
        final String folderPath = paramContainer.checkStringParam(AJAXServlet.PARAMETER_FOLDERID);
        /*
         * Check for possible unseen action
         */
        final boolean wasUnseen = (mail.containsPrevSeen() && !mail.isPrevSeen());
        final boolean doUnseen = (unseen && wasUnseen);
        if (doUnseen) {
            mail.setFlag(MailMessage.FLAG_SEEN, false);
            final int unreadMsgs = mail.getUnreadMessages();
            mail.setUnreadMessages(unreadMsgs < 0 ? 0 : unreadMsgs + 1);
        }
        List<OXException> warnings = new ArrayList<OXException>(2);
        int maxContentSize = AJAXRequestDataTools.parseIntParameter(paramContainer.getStringParam(Mail.PARAMETER_MAX_SIZE), -1);
        boolean allowNestedMessages;
        {
            String str = paramContainer.getStringParam(Mail.PARAMETER_ALLOW_NESTED_MESSAGES);
            allowNestedMessages = null == str ? true : AJAXRequestDataTools.parseBoolParameter(str);
        }
        boolean exactLength = AJAXRequestDataTools.parseBoolParameter(paramContainer.getStringParam("exact_length"));
        JSONObject jMail;
        try {
            MessageWriterParams params = MessageWriterParams.builder(mail.getAccountId(), mail, session)
                                                            .setDisplayMode(displayMode)
                                                            .setEmbedded(embedded)
                                                            .setExactLength(exactLength)
                                                            .setIncludePlainText(includePlainText)
                                                            .setMaxContentSize(maxContentSize)
                                                            .setMaxNestedMessageLevels(allowNestedMessages ? -1 : 1)
                                                            .setMimeFilter(mimeFilter)
                                                            .setOptTimeZone(timeZone)
                                                            .setSettings(usmNoSave)
                                                            .setToken(token)
                                                            .setTokenTimeout(ttlMillis)
                                                            .setWarnings(warnings)
                                                            .build();
            jMail = MessageWriter.writeMailMessage(params);
        } catch (final OXException e) {
            if (MailExceptionCode.MESSAGING_ERROR.equals(e)) {
                final Throwable cause = e.getCause();
                if (cause instanceof javax.mail.MessageRemovedException) {
                    throw MailExceptionCode.MAIL_NOT_FOUND.create(cause, mail.getMailId(), mail.getFolder());
                } else if (cause instanceof javax.mail.MessagingException) {
                    throw MimeMailException.handleMessagingException((MessagingException) cause, null, session);
                }
            } else if (MailExceptionCode.MAIL_NOT_FOUND.equals(e) || MailExceptionCode.MAIL_NOT_FOUND_SIMPLE.equals(e)) {
                LOG.warn("Requested mail could not be found  (folder={}, id={}, user={}, context={}). Most likely this is caused by concurrent access of multiple clients while one performed a delete on affected mail.", paramContainer.getStringParam(AJAXServlet.PARAMETER_FOLDERID), paramContainer.getStringParam(AJAXServlet.PARAMETER_ID), session.getUserId(), session.getContextId(), e);
            }
            throw e;
        }
        if (mail.containsPrevSeen()) {
            try {
                jMail.put("unseen", wasUnseen);
            } catch (final JSONException e) {
                LOG.warn("Couldn't set \"unseen\" field in JSON mail representation.", e);
            }
        }

        // Check for special view=document
        if (DisplayMode.DOCUMENT.isIncluded(displayMode)) {
            HttpServletResponse resp = paramContainer.getHttpServletResponse();
            if (resp != null) {
                try {
                    String htmlContent = jMail.getJSONArray(MailJSONField.ATTACHMENTS.getKey()).getJSONObject(0).getString(MailJSONField.CONTENT.getKey());

                    resp.setContentType("text/html; charset=UTF-8");
                    resp.setHeader("Content-Disposition", "inline");
                    PrintWriter writer = resp.getWriter();
                    writer.write(htmlContent);
                    writer.flush();
                    jMail = null;
                } catch (JSONException e) {
                    throw AjaxExceptionCodes.JSON_ERROR.create(e, e.getMessage());
                } catch (IOException e) {
                    throw AjaxExceptionCodes.IO_ERROR.create(e, e.getMessage());
                }
            }
        }

        if (doUnseen) {
            /*-
             * Leave mail as unseen
             *
             * Determine mail identifier
             */
            final String uid;
            {
                String tmp2 = paramContainer.getStringParam(AJAXServlet.PARAMETER_ID);
                if (null == tmp2) {
                    tmp2 = paramContainer.getStringParam(Mail.PARAMETER_MESSAGE_ID);
                    if (null == tmp2) {
                        throw AjaxExceptionCodes.MISSING_PARAMETER.create(AJAXServlet.PARAMETER_ID);
                    }
                    uid = mailInterface.getMailIDByMessageID(folderPath, tmp2);
                } else {
                    uid = tmp2;
                }
            }
            mailInterface.updateMessageFlags(folderPath, new String[] { uid }, MailMessage.FLAG_SEEN, false);
        }

        // Common handling
        return jMail;
    }

    private void convertSingle(final MailMessage mail, final AJAXRequestData requestData, final AJAXRequestResult result, final ServerSession session) throws OXException {
        String view = requestData.getParameter(Mail.PARAMETER_VIEW);
        view = null == view ? null : view.toLowerCase(Locale.US);
        String tmp = requestData.getParameter("embedded");
        final boolean embedded = (tmp != null && ("1".equals(tmp) || Boolean.parseBoolean(tmp)));
        tmp = null;
        final UserSettingMail usmNoSave = session.getUserSettingMail().clone();
        /*
         * Deny saving for this request-specific settings
         */
        usmNoSave.setNoSave(true);
        /*
         * Overwrite settings with request's parameters
         */
        DisplayMode displayMode = AbstractMailAction.detectDisplayMode(true, view, usmNoSave);
        int maxContentSize = AJAXRequestDataTools.parseIntParameter(requestData.getParameter(Mail.PARAMETER_MAX_SIZE), -1);
        boolean allowNestedMessages = AJAXRequestDataTools.parseBoolParameter(Mail.PARAMETER_ALLOW_NESTED_MESSAGES, requestData, true);
        List<OXException> warnings = new ArrayList<OXException>(2);
        JSONObject jsonObject = MessageWriter.writeMailMessage(mail.getAccountId(), mail, displayMode, embedded, session, usmNoSave, warnings, false, -1, null, null, false, maxContentSize, allowNestedMessages ? -1 : 1);

        {
            String csid = (String) result.getParameter("csid");
            if (null != csid) {
                try {
                    jsonObject.put("csid", csid);
                } catch (JSONException e) {
                    throw AjaxExceptionCodes.JSON_ERROR.create(e, e.getMessage());
                }
            }
        }

        result.addWarnings(warnings);
        result.setResultObject(jsonObject, "json");
    }

    private MailServletInterface getMailInterface(final AJAXRequestData request, final ServerSession session) throws OXException {
        /*
         * Get mail interface
         */
        MailServletInterface mailInterface = request.getState().optProperty(PROPERTY_MAIL_IFACE);
        if (mailInterface == null) {
            final MailServletInterface newMailInterface = MailServletInterface.getInstance(session);
            mailInterface = request.getState().putProperty(PROPERTY_MAIL_IFACE, newMailInterface);
            if (null == mailInterface) {
                mailInterface = newMailInterface;
            } else {
                newMailInterface.close(true);
            }
        }
        return mailInterface;
    }

    private boolean seemsValid(MailMessage mail, MailFields mailFields) {
        if (null == mail) {
            return false;
        }

        boolean valid = true;
        if (mailFields.contains(MailField.SUBJECT)) {
            if (mail.containsSubject()) {
                return true;
            }
            valid = false; // pessimistic
        }
        if (mailFields.contains(MailField.FROM)) {
            if (mail.containsFrom()) {
                return true;
            }
            valid = false; // pessimistic
        }
        if (mailFields.contains(MailField.TO)) {
            if (mail.containsTo()) {
                return true;
            }
            valid = false; // pessimistic
        }
        if (mailFields.contains(MailField.SIZE)) {
            if (mail.containsSize()) {
                return true;
            }
            valid = false; // pessimistic
        }
        if (mailFields.contains(MailField.SENT_DATE)) {
            if (mail.containsSentDate()) {
                return true;
            }
            valid = false; // pessimistic
        }

        return valid;
    }
}
