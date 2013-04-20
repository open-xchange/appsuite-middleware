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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;
import java.util.regex.Pattern;
import javax.mail.MessagingException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONValue;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.Mail;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestDataTools;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.Converter;
import com.openexchange.ajax.requesthandler.ResultConverter;
import com.openexchange.exception.OXException;
import com.openexchange.json.OXJSONWriter;
import com.openexchange.json.cache.JsonCacheService;
import com.openexchange.json.cache.JsonCaches;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.MailListField;
import com.openexchange.mail.MailServletInterface;
import com.openexchange.mail.config.MailProperties;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.dataobjects.ThreadedStructure;
import com.openexchange.mail.json.MailActionConstants;
import com.openexchange.mail.json.MailRequest;
import com.openexchange.mail.json.MailRequestSha1Calculator;
import com.openexchange.mail.json.actions.AbstractMailAction;
import com.openexchange.mail.json.writer.MessageWriter;
import com.openexchange.mail.json.writer.MessageWriter.MailFieldWriter;
import com.openexchange.mail.mime.MimeFilter;
import com.openexchange.mail.mime.MimeMailException;
import com.openexchange.mail.usersetting.UserSettingMail;
import com.openexchange.mail.utils.DisplayMode;
import com.openexchange.preferences.ServerUserSetting;
import com.openexchange.tools.TimeZoneUtils;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link MailConverter}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MailConverter implements ResultConverter, MailActionConstants {

    private static final org.apache.commons.logging.Log LOG =
        com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(MailConverter.class));

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
                if (LOG.isWarnEnabled()) {
                    LOG.warn("Result object is null.");
                }
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
            } else {
                @SuppressWarnings("unchecked") final Collection<MailMessage> mails = (Collection<MailMessage>) resultObject;
                if (AJAXServlet.ACTION_ALL.equalsIgnoreCase(action)) {
                    convertMultiple4All(mails, requestData, result, session);
                } else if (AJAXServlet.ACTION_LIST.equalsIgnoreCase(action)) {
                    convertMultiple4List(mails, requestData, result, session);
                } else {
                    // throw AjaxExceptionCodes.UNKNOWN_ACTION.create(action);
                    convertMultiple4List(mails, requestData, result, session);
                }
            }
        } catch (final JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        }
    }

    private void convertThreadStructure(final ThreadedStructure structure, final AJAXRequestData requestData, final AJAXRequestResult result, final ServerSession session) throws OXException, JSONException {
        /*-
         * The data UI needs looks like this:
         *
         * [{ id: 1234, folder_id: 'default0/INBOX', thread: [{ id: 1234, folder_id: 'default0/INBOX'}, {id: 4711, folder_id: 'default0/INBOX' }, ...]
         */
        /*
         * Read in parameters
         */
        final int[] columns = requestData.checkIntArray(AJAXServlet.PARAMETER_COLUMNS);
        final String[] headers = requestData.getParameterValues(Mail.PARAMETER_HEADERS);
        /*
         * Pre-Select field writers
         */
        final MailFieldWriter[] writers = MessageWriter.getMailFieldWriter(MailListField.getFields(columns));
        final MailFieldWriter[] headerWriters = null == headers ? null : MessageWriter.getHeaderFieldWriter(headers);
        /*
         * Get mail interface
         */
        final OXJSONWriter jsonWriter = new OXJSONWriter();
        /*
         * Start response
         */
        final boolean writeThreadAsObjects = AJAXRequestDataTools.parseBoolParameter("konfetti", requestData);
        final boolean containsMultipleFolders = containsMultipleFolders(structure, new HashSet<String>(2));
        jsonWriter.array();
        try {
            final int userId = session.getUserId();
            final int contextId = session.getContextId();
            for (final List<MailMessage> mails : structure.getMails()) {
                if (mails != null && !mails.isEmpty()) {
                    final JSONObject jo = new JSONObject(32);
                    writeThreadSortedMail(mails, jo, writers, headerWriters, containsMultipleFolders, writeThreadAsObjects, userId, contextId);
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

    private static final MailFieldWriter[] WRITER_IDS = MessageWriter.getMailFieldWriter(new MailListField[] {
        MailListField.ID, MailListField.FOLDER_ID });

    private void writeThreadSortedMail(final List<MailMessage> mails, final JSONObject jMail, final MailFieldWriter[] writers, final MailFieldWriter[] headerWriters, final boolean containsMultipleFolders, final boolean writeThreadAsObjects, final int userId, final int contextId) throws OXException, JSONException {
        final MailMessage rootMessage = mails.get(0);
        int accountID = rootMessage.getAccountId();
        for (int j = 0; j < writers.length; j++) {
            writers[j].writeField(jMail, rootMessage, 0, true, accountID, userId, contextId);
        }
        if (null != headerWriters) {
            for (int j = 0; j < headerWriters.length; j++) {
                headerWriters[j].writeField(jMail, rootMessage, 0, true, accountID, userId, contextId);
            }
        }
        int unreadCount = 0;
        // Add child nodes
        final JSONArray jChildMessages = new JSONArray(mails.size());
        if (writeThreadAsObjects) {
            for (final MailMessage child : mails) {
                final JSONObject jChild = new JSONObject();
                accountID = child.getAccountId();
                for (int j = 0; j < writers.length; j++) {
                    writers[j].writeField(jChild, child, 0, true, accountID, userId, contextId);
                }
                if (null != headerWriters) {
                    for (int j = 0; j < headerWriters.length; j++) {
                        headerWriters[j].writeField(jChild, child, 0, true, accountID, userId, contextId);
                    }
                }
                /*-
                 *
                for (final MailFieldWriter w : WRITER_IDS) {
                    w.writeField(jChild, child, 0, true, accountID, userId, contextId);
                }
                 *
                 */
                jChildMessages.put(jChild);
                /*
                 * Count unread messages in this thread structure
                 */
                if (!child.isSeen()) {
                    unreadCount++;
                }
            }
        } else {
            if (containsMultipleFolders) {
                final StringBuilder sb = new StringBuilder(16);
                final char defaultSeparator = MailProperties.getInstance().getDefaultSeparator();
                for (final MailMessage child : mails) {
                    sb.setLength(0);
                    jChildMessages.put(sb.append(child.getFolder()).append(defaultSeparator).append(child.getMailId()).toString());
                    /*
                     * Count unread messages in this thread structure
                     */
                    if (!child.isSeen()) {
                        unreadCount++;
                    }
                }
            } else {
                for (final MailMessage child : mails) {
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
        jMail.put("thread", jChildMessages);
        jMail.put("unreadCount", unreadCount);
    }

    private void convertMultiple4List(final Collection<MailMessage> mails, final AJAXRequestData requestData, final AJAXRequestResult result, final ServerSession session) throws OXException, JSONException {
        /*
         * Read in parameters
         */
        final int[] columns = requestData.checkIntArray(AJAXServlet.PARAMETER_COLUMNS);
        final String[] headers = requestData.getParameterValues(Mail.PARAMETER_HEADERS);
        /*
         * Pre-Select field writers
         */
        final MailFieldWriter[] writers = MessageWriter.getMailFieldWriter(MailListField.getFields(columns));
        final MailFieldWriter[] headerWriters = null == headers ? null : MessageWriter.getHeaderFieldWriter(headers);
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
                if (mail != null) {
                    final JSONArray ja = new JSONArray();
                    final int accountID = mail.getAccountId();
                    for (int j = 0; j < writers.length; j++) {
                        writers[j].writeField(ja, mail, 0, false, accountID, userId, contextId);
                    }
                    if (null != headerWriters) {
                        for (int j = 0; j < headerWriters.length; j++) {
                            headerWriters[j].writeField(ja, mail, 0, false, accountID, userId, contextId);
                        }
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
        final int[] columns = requestData.checkIntArray(AJAXServlet.PARAMETER_COLUMNS);
        final String sort = requestData.getParameter(AJAXServlet.PARAMETER_SORT);
        /*
         * Get mail interface
         */
        final MailServletInterface mailInterface = getMailInterface(requestData, session);
        /*
         * Pre-Select field writers
         */
        final MailFieldWriter[] writers = MessageWriter.getMailFieldWriter(MailListField.getFields(columns));
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
                    final JSONArray ja = new JSONArray();
                    if (mail != null) {
                        final int accountId = mail.getAccountId();
                        for (final MailFieldWriter writer : writers) {
                            writer.writeField(ja, mail, mail.getThreadLevel(), false, accountId, userId, contextId);
                        }

                    }
                    jsonWriter.value(ja);
                }
            } else {
                /*
                 * Get iterator
                 */
                for (final MailMessage mail : mails) {
                    final JSONArray ja = new JSONArray();
                    if (mail != null) {
                        final int accountId = mail.getAccountId();
                        for (final MailFieldWriter writer : writers) {
                            writer.writeField(ja, mail, 0, false, accountId, userId, contextId);
                        }
                    }
                    jsonWriter.value(ja);
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

    private static final Pattern SPLIT = Pattern.compile(" *, *");

    private void convertSingle4Get(final MailMessage mail, final AJAXRequestData requestData, final AJAXRequestResult result, final ServerSession session) throws OXException {
        String tmp = requestData.getParameter(Mail.PARAMETER_EDIT_DRAFT);
        final boolean editDraft = ("1".equals(tmp) || Boolean.parseBoolean(tmp));
        tmp = requestData.getParameter(Mail.PARAMETER_VIEW);
        final String view = null == tmp ? null : tmp.toLowerCase(Locale.ENGLISH);
        tmp = requestData.getParameter(Mail.PARAMETER_UNSEEN);
        final boolean unseen = (tmp != null && ("1".equals(tmp) || Boolean.parseBoolean(tmp)));
        tmp = requestData.getParameter(Mail.PARAMETER_TIMEZONE);
        final TimeZone timeZone = isEmpty(tmp) ? null : TimeZoneUtils.getTimeZone(tmp.trim());
        tmp = requestData.getParameter("token");
        final boolean token = (tmp != null && ("1".equals(tmp) || Boolean.parseBoolean(tmp)));
        tmp = requestData.getParameter("ttlMillis");
        int ttlMillis;
        try {
            ttlMillis = (tmp == null ? -1 : Integer.parseInt(tmp.trim()));
        } catch (final NumberFormatException e) {
            ttlMillis = -1;
        }
        tmp = requestData.getParameter("embedded");
        final boolean embedded = (tmp != null && ("1".equals(tmp) || Boolean.parseBoolean(tmp)));
        tmp = requestData.getParameter("ignorable");
        final MimeFilter mimeFilter;
        if (isEmpty(tmp)) {
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
        final UserSettingMail usmNoSave = (UserSettingMail) session.getUserSettingMail().clone();
        /*
         * Deny saving for this request-specific settings
         */
        usmNoSave.setNoSave(true);
        /*
         * Overwrite settings with request's parameters
         */
        final DisplayMode displayMode = AbstractMailAction.detectDisplayMode(editDraft, view, usmNoSave);
        final String folderPath = requestData.checkParameter(AJAXServlet.PARAMETER_FOLDERID);
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
        final MailServletInterface mailInterface = getMailInterface(requestData, session);
        final List<OXException> warnings = new ArrayList<OXException>(2);
        final JSONObject jMail;
        try {
            jMail = MessageWriter.writeMailMessage(
                mail.getAccountId(),
                mail,
                displayMode,
                embedded,
                session,
                usmNoSave,
                warnings,
                token,
                ttlMillis,
                mimeFilter,
                timeZone);
        } catch (final OXException e) {
            if (MailExceptionCode.MESSAGING_ERROR.equals(e)) {
                final Throwable cause = e.getCause();
                if (cause instanceof javax.mail.MessageRemovedException) {
                    throw MailExceptionCode.MAIL_NOT_FOUND.create(cause, mail.getMailId(), mail.getFolder());
                } else if (cause instanceof javax.mail.MessagingException) {
                    throw MimeMailException.handleMessagingException((MessagingException) cause, null, session);
                }
            }
            // Handle others
            else if (MailExceptionCode.MAIL_NOT_FOUND.equals(e)) {
                LOG.warn(
                    new StringBuilder("Requested mail could not be found. ").append(
                        "Most likely this is caused by concurrent access of multiple clients ").append(
                        "while one performed a delete on affected mail.").toString(),
                    e);
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
        result.setResultObject(jMail, "json");
        if (doUnseen) {
            /*-
             * Leave mail as unseen
             *
             * Determine mail identifier
             */
            final String uid;
            {
                String tmp2 = requestData.getParameter(AJAXServlet.PARAMETER_ID);
                if (null == tmp2) {
                    tmp2 = requestData.getParameter(Mail.PARAMETER_MESSAGE_ID);
                    if (null == tmp2) {
                        throw AjaxExceptionCodes.MISSING_PARAMETER.create(AJAXServlet.PARAMETER_ID);
                    }
                    uid = mailInterface.getMailIDByMessageID(folderPath, tmp2);
                } else {
                    uid = tmp2;
                }
            }
            mailInterface.updateMessageFlags(folderPath, new String[] { uid }, MailMessage.FLAG_SEEN, false);
        } else if (wasUnseen) {
            try {
                final ServerUserSetting setting = ServerUserSetting.getInstance();
                final int contextId = session.getContextId();
                final int userId = session.getUserId();
                if (setting.isContactCollectionEnabled(contextId, userId).booleanValue() && setting.isContactCollectOnMailAccess(
                    contextId,
                    userId).booleanValue()) {
                    AbstractMailAction.triggerContactCollector(session, mail);
                }
            } catch (final OXException e) {
                LOG.warn("Contact collector could not be triggered.", e);
            }
        }
    }

    private void convertSingle(final MailMessage mail, final AJAXRequestData requestData, final AJAXRequestResult result, final ServerSession session) throws OXException {
        String view = requestData.getParameter(Mail.PARAMETER_VIEW);
        view = null == view ? null : view.toLowerCase(Locale.US);
        String tmp = requestData.getParameter("embedded");
        final boolean embedded = (tmp != null && ("1".equals(tmp) || Boolean.parseBoolean(tmp)));
        tmp = null;
        final UserSettingMail usmNoSave = (UserSettingMail) session.getUserSettingMail().clone();
        /*
         * Deny saving for this request-specific settings
         */
        usmNoSave.setNoSave(true);
        /*
         * Overwrite settings with request's parameters
         */
        final DisplayMode displayMode = AbstractMailAction.detectDisplayMode(true, view, usmNoSave);
        final List<OXException> warnings = new ArrayList<OXException>(2);
        final JSONObject jsonObject =
            MessageWriter.writeMailMessage(mail.getAccountId(), mail, displayMode, embedded, session, usmNoSave, warnings, false, -1);
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

    private static boolean isEmpty(final String string) {
        if (null == string) {
            return true;
        }
        final int len = string.length();
        boolean isWhitespace = true;
        for (int i = 0; isWhitespace && i < len; i++) {
            isWhitespace = Character.isWhitespace(string.charAt(i));
        }
        return isWhitespace;
    }

}
