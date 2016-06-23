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

package com.openexchange.mail.json.actions;

import static com.openexchange.java.Strings.toLowerCase;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import javax.mail.MessageRemovedException;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringOutputStream;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.Mail;
import com.openexchange.ajax.SessionServlet;
import com.openexchange.ajax.container.ThresholdFileHolder;
import com.openexchange.ajax.fileholder.IFileHolder;
import com.openexchange.ajax.helper.DownloadUtility;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestDataTools;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.documentation.RequestMethod;
import com.openexchange.documentation.annotations.Action;
import com.openexchange.documentation.annotations.Parameter;
import com.openexchange.exception.OXException;
import com.openexchange.java.CharsetDetector;
import com.openexchange.java.Charsets;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
import com.openexchange.log.LogProperties;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.MailServletInterface;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.json.MailRequest;
import com.openexchange.mail.json.converters.MailConverter;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.mail.mime.MimeDefaultSession;
import com.openexchange.mail.mime.MimeFilter;
import com.openexchange.mail.mime.MimeMailException;
import com.openexchange.mail.mime.converters.MimeMessageConverter;
import com.openexchange.preferences.ServerUserSetting;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link GetAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@Action(method = RequestMethod.GET, name = "get", description = "Get a mail.", parameters = {
    @Parameter(name = "session", description = "A session ID previously obtained from the login module."),
    @Parameter(name = "id", description = "Object ID of the requested mail."),
    @Parameter(name = "message_id", description = "(Preliminary) The value of \"Message-Id\" header of the requested mail. This parameter is a substitute for \"id\" parameter."),
    @Parameter(name = "folder", description = "Object ID of the mail's folder."),
    @Parameter(name = "edit", optional=true, description = "1 indicates that this request should fill the message compose dialog to edit a message and thus display-specific date is going to be withheld."),
    @Parameter(name = "hdr", optional=true, description = "1 to let the response contain only the (formatted) message headers as plain text"),
    @Parameter(name = "src", optional=true, description = "1 to let the response contain the complete message source as plain text"),
    @Parameter(name = "save", optional=true, description = "1 to write the complete message source to output stream. NOTE: This parameter will only be used if parameter src is set to 1."),
    @Parameter(name = "view", optional=true, description = "(available with SP4) \"raw\" returns the content as it is, meaning no preparation are performed and thus no guarantee for safe contents is given (available with SP6 v6.10). \"text\" forces the server to deliver a text-only version of the requested mail's body, even if content is HTML. \"textNoHtmlAttach\" is the same as \"text\", but does not deliver the HTML part as attachment in case of multipart/alternative content. \"html\" to allow a possible HTML mail body being transferred as it is (but white-list filter applied). \"noimg\" to allow a possible HTML content being transferred but without original image src attributes which references external images: Can be used to prevent loading external linked images (spam privacy protection). NOTE: if set, the corresponding gui config setting will be ignored."),
    @Parameter(name = "unseen", optional = true, description = "\"1\" or \"true\" to leave an unseen mail as unseen although its content is requested"),
    @Parameter(name = "max_size", optional = true, description = "Maximum size of the returned mail content")
}, responseDescription = "(not IMAP: with timestamp): An JSON object containing all data of the requested mail. The fields of the object are listed in Detailed mail data. The fields id and attachment are not included. NOTE: Of course response is not a JSON object if either parameter hdr or parameter src are set to \"1\". Then the response contains plain text. Moreover if optional parameter save is set to \"1\" the complete message source is going to be directly written to output stream to open browser's save dialog.")
public final class GetAction extends AbstractMailAction {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(GetAction.class);

    private static final byte[] CHUNK1 = "{\"data\":\"".getBytes();
    private static final byte[] CHUNK2 = "\"}".getBytes();

    /**
     * Initializes a new {@link GetAction}.
     *
     * @param services
     */
    public GetAction(final ServiceLookup services) {
        super(services);
    }

    @Override
    protected AJAXRequestResult perform(final MailRequest req) throws OXException, JSONException {
        Object data = req.getRequest().getData();
        if (null == data) {
            return performGet(req);
        }
        if (!(data instanceof JSONArray)) {
            throw AjaxExceptionCodes.INVALID_JSON_REQUEST_BODY.create();
        }
        return performPut(req, (JSONArray) data);
    }

    private AJAXRequestResult performPut(final MailRequest req, final JSONArray paths) throws OXException {
        try {
            final int length = paths.length();
            if (length != 1) {
                throw new IllegalArgumentException("JSON array's length is not 1");
            }
            final AJAXRequestData requestData = req.getRequest();
            for (int i = 0; i < length; i++) {
                final JSONObject folderAndID = paths.getJSONObject(i);
                requestData.putParameter(AJAXServlet.PARAMETER_FOLDERID, folderAndID.getString(AJAXServlet.PARAMETER_FOLDERID));
                requestData.putParameter(AJAXServlet.PARAMETER_ID, folderAndID.get(AJAXServlet.PARAMETER_ID).toString());
            }
            /*
             * ... and fake a GET request
             */
            return performGet(new MailRequest(requestData, req.getSession()));
        } catch (final JSONException e) {
            throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
        }
    }

    private static final Pattern SPLIT = Pattern.compile(" *, *");

    private AJAXRequestResult performGet(MailRequest req) throws OXException, JSONException {
        try {
            ServerSession session = req.getSession();
            /*
             * Read in parameters
             */
            String folderPath = req.checkParameter(AJAXServlet.PARAMETER_FOLDERID);
            String tmp = req.getParameter(Mail.PARAMETER_SHOW_SRC);
            boolean showMessageSource = ("1".equals(tmp) || Boolean.parseBoolean(tmp));
            tmp = req.getParameter(Mail.PARAMETER_SHOW_HEADER);
            boolean showMessageHeaders = ("1".equals(tmp) || Boolean.parseBoolean(tmp));
            boolean saveToDisk;
            {
                String saveParam = req.getParameter(Mail.PARAMETER_SAVE);
                saveToDisk = AJAXRequestDataTools.parseBoolParameter(saveParam) || "download".equals(toLowerCase(req.getParameter(AJAXServlet.PARAMETER_DELIVERY)));
            }
            tmp = req.getParameter(Mail.PARAMETER_UNSEEN);
            boolean unseen = (tmp != null && ("1".equals(tmp) || Boolean.parseBoolean(tmp)));

            // Check for possible MIME filter
            tmp = req.getParameter("ignorable");
            MimeFilter mimeFilter;
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
            tmp = req.getParameter("attach_src");
            final boolean attachMessageSource = ("1".equals(tmp) || Boolean.parseBoolean(tmp));
            tmp = null;

            // Warnings container
            List<OXException> warnings = new ArrayList<OXException>(2);

            // Get mail interface
            MailServletInterface mailInterface = getMailInterface(req);

            // Determine mail identifier
            final String uid;
            {
                String tmp2 = req.getParameter(AJAXServlet.PARAMETER_ID);
                if (null == tmp2) {
                    tmp2 = req.getParameter(Mail.PARAMETER_MESSAGE_ID);
                    if (null == tmp2) {
                        throw AjaxExceptionCodes.MISSING_PARAMETER.create(AJAXServlet.PARAMETER_ID);
                    }
                    uid = mailInterface.getMailIDByMessageID(folderPath, tmp2);
                } else {
                    uid = tmp2;
                }
            }

            LogProperties.put(LogProperties.Name.MAIL_MAIL_ID, uid);
            LogProperties.put(LogProperties.Name.MAIL_FULL_NAME, folderPath);
            AJAXRequestResult data = getJSONNullResult();
            if (showMessageSource) {
                // Get message
                final MailMessage mail = mailInterface.getMessage(folderPath, uid, !unseen);
                if (mail == null) {
                    throw MailExceptionCode.MAIL_NOT_FOUND.create(uid, folderPath);
                }

                // Direct response if possible
                if (null == mimeFilter) {
                    final AJAXRequestData requestData = req.getRequest();
                    final OutputStream directOutputStream = requestData.optOutputStream();
                    if (null != directOutputStream) {
                        if (saveToDisk) {
                            if (requestData.setResponseHeader("Content-Type", "application/octet-stream")) {
                                final StringBuilder sb = new StringBuilder(64).append("attachment");
                                {
                                    final String subject = mail.getSubject();
                                    final String fileName = isEmpty(subject) ? "mail.eml" : saneForFileName(subject) + ".eml";
                                    DownloadUtility.appendFilenameParameter(fileName, requestData.getUserAgent(), sb);
                                }
                                requestData.setResponseHeader("Content-Disposition",  sb.toString());
                                requestData.removeCachingHeader();
                                mail.writeTo(directOutputStream);
                                directOutputStream.flush();
                                return new AJAXRequestResult(AJAXRequestResult.DIRECT_OBJECT, "direct");
                            }
                        } else {
                            // As JSON response: {"data":"..."}
                            directOutputStream.write(CHUNK1); // {"data":"...
                            {
                                final JSONStringOutputStream jsonStringOutputStream = new JSONStringOutputStream(directOutputStream);
                                mail.writeTo(jsonStringOutputStream);
                                jsonStringOutputStream.flush();
                            }
                            directOutputStream.write(CHUNK2); // ..."}
                            directOutputStream.flush();
                            return new AJAXRequestResult(AJAXRequestResult.DIRECT_OBJECT, "direct");
                        }
                    }
                }

                // The regular way...
                ThresholdFileHolder fileHolder = getMimeSource(mail, mimeFilter);
                try {
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
                            if (setting.isContactCollectOnMailAccess(contextId, userId).booleanValue()) {
                                triggerContactCollector(session, mail, false);
                            }
                        } catch (final OXException e) {
                            LOG.warn("Contact collector could not be triggered.", e);
                        }
                    }
                    if (saveToDisk) {
                        /*
                         * Create appropriate file holder
                         */
                        final AJAXRequestData requestData = req.getRequest();
                        if (requestData.setResponseHeader("Content-Type", "application/octet-stream")) {
                            final OutputStream directOutputStream = requestData.optOutputStream();
                            if (null != directOutputStream) {
                                // Direct output
                                final StringBuilder sb = new StringBuilder(64).append("attachment");
                                {
                                    final String subject = mail.getSubject();
                                    final String fileName = isEmpty(subject) ? "mail.eml" : saneForFileName(subject) + ".eml";
                                    DownloadUtility.appendFilenameParameter(fileName, requestData.getUserAgent(), sb);
                                }
                                requestData.setResponseHeader("Content-Disposition",  sb.toString());
                                requestData.removeCachingHeader();
                                final InputStream is = fileHolder.getStream();
                                try {
                                    final int len = 2048;
                                    final byte[] buf = new byte[len];
                                    for (int read; (read = is.read(buf, 0, len)) > 0;) {
                                        directOutputStream.write(buf, 0, read);
                                    }
                                } finally {
                                    Streams.close(is);
                                }
                                directOutputStream.flush();
                                return new AJAXRequestResult(AJAXRequestResult.DIRECT_OBJECT, "direct");
                            }
                        }
                        // As file holder
                        requestData.setFormat("file");
                        fileHolder.setContentType("application/octet-stream");
                        fileHolder.setDelivery("download");
                        // Set file name
                        final String subject = mail.getSubject();
                        fileHolder.setName(new StringBuilder(isEmpty(subject) ? "mail" : saneForFileName(subject)).append(".eml").toString());
                        IFileHolder temp = fileHolder;
                        fileHolder = null; // Avoid premature closing
                        return new AJAXRequestResult(temp, "file");
                    }
                    final ContentType ct = mail.getContentType();
                    if (ct.containsCharsetParameter() && CharsetDetector.isValid(ct.getCharsetParameter())) {
                        data = new AJAXRequestResult(new String(fileHolder.toByteArray(), ct.getCharsetParameter()), "string");
                    } else {
                        data = new AJAXRequestResult(new String(fileHolder.toByteArray(), "UTF-8"), "string");
                    }
                } finally {
                    Streams.close(fileHolder);
                }
            } else if (showMessageHeaders) {
                /*
                 * Get message
                 */
                final MailMessage mail = mailInterface.getMessage(folderPath, uid, !unseen);
                if (mail == null) {
                    throw MailExceptionCode.MAIL_NOT_FOUND.create(uid, folderPath);
                }
                final boolean wasUnseen = (mail.containsPrevSeen() && !mail.isPrevSeen());
                final boolean doUnseen = (unseen && wasUnseen);
                final ContentType rct = new ContentType("text/plain");
                final ContentType ct = mail.getContentType();
                if (ct.containsCharsetParameter() && CharsetDetector.isValid(ct.getCharsetParameter())) {
                    rct.setCharsetParameter(ct.getCharsetParameter());
                } else {
                    rct.setCharsetParameter("UTF-8");
                }
                data = new AJAXRequestResult(formatMessageHeaders(mail.getHeadersIterator()), "string");
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
                        if (setting.isContactCollectOnMailAccess(contextId, userId).booleanValue()) {
                            triggerContactCollector(session, mail, false);
                        }
                    } catch (final OXException e) {
                        LOG.warn("Contact collector could not be triggered.", e);
                    }
                }
            } else {
                /*
                 * Get & check message
                 */
                MailMessage mail = mailInterface.getMessage(folderPath, uid, !unseen);
                if (mail == null) {
                    OXException oxe = MailExceptionCode.MAIL_NOT_FOUND.create(uid, folderPath);
                    if (VIEW_DOCUMENT.equals(req.getParameter(Mail.PARAMETER_VIEW))) {
                        HttpServletResponse resp = req.getRequest().optHttpServletResponse();
                        if (resp != null) {
                            SessionServlet.writeErrorPage(HttpServletResponse.SC_NOT_FOUND, oxe.getDisplayMessage(session.getUser().getLocale()), resp);
                            return new AJAXRequestResult(AJAXRequestResult.DIRECT_OBJECT, "direct").setType(AJAXRequestResult.ResultType.DIRECT);
                        }
                    }
                    throw oxe;
                }
                /*
                 * Mail found...
                 */
                if (!mail.containsAccountId()) {
                    mail.setAccountId(mailInterface.getAccountID());
                }
                /*
                 * Check if source shall be attached
                 */
                ThresholdFileHolder fileHolder = null;
                try {
                    if (attachMessageSource) {
                        fileHolder = getMimeSource(mail, mimeFilter);
                    }
                    /*
                     * Check whether to trigger contact collector
                     */
                    if (!unseen && (mail.containsPrevSeen() && !mail.isPrevSeen())) {
                        try {
                            final ServerUserSetting setting = ServerUserSetting.getInstance();
                            final int contextId = session.getContextId();
                            final int userId = session.getUserId();
                            if (setting.isContactCollectOnMailAccess(contextId, userId).booleanValue()) {
                                triggerContactCollector(session, mail, false);
                            }
                        } catch (final OXException e) {
                            LOG.warn("Contact collector could not be triggered.", e);
                        }
                    }
                    /*
                     * Create result dependent on "attachMessageSource" flag
                     */
                    if (null == fileHolder) {
                        data = new AJAXRequestResult(mail, "mail");
                    } else {
                        AJAXRequestResult requestResult = new AJAXRequestResult(mail, "mail");
                        MailConverter.getInstance().convert2JSON(req.getRequest(), requestResult, session);
                        JSONObject jMail = (JSONObject) requestResult.getResultObject();
                        if (null != jMail) {
                            if (fileHolder.isInMemory()) {
                                jMail.put("source", new String(fileHolder.toByteArray(), Charsets.UTF_8));
                            } else {
                                jMail.put("source", new InputStreamReader(fileHolder.getClosingStream(), Charsets.UTF_8));
                                fileHolder = null; // Avoid preliminary closing
                            }
                        }
                        data = requestResult;
                    }
                } finally {
                    Streams.close(fileHolder);
                }
            }
            data.addWarnings(warnings);
            return data;
        } catch (final OXException e) {
            if (MailExceptionCode.MAIL_NOT_FOUND.equals(e)) {
                LOG.debug("Requested mail could not be found. Most likely this is caused by concurrent access of multiple clients while one performed a delete on affected mail.", e);
                final String uid = getUidFromException(e);
                if ("undefined".equalsIgnoreCase(uid)) {
                    throw MailExceptionCode.PROCESSING_ERROR.create(e, new Object[0]);
                }
            } else {
                LOG.error("", e);
            }
            throw e;
        } catch (final MessagingException e) {
            throw MimeMailException.handleMessagingException(e);
        } catch (final IOException e) {
            if ("com.sun.mail.util.MessageRemovedIOException".equals(e.getClass().getName()) || (e.getCause() instanceof MessageRemovedException)) {
                throw MailExceptionCode.MAIL_NOT_FOUND_SIMPLE.create(e);
            }
            throw MailExceptionCode.IO_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    private ThresholdFileHolder getMimeSource(final MailMessage mail, final MimeFilter mimeFilter) throws OXException, MessagingException, IOException {
        ThresholdFileHolder fileHolder = new ThresholdFileHolder();
        try {
            mail.writeTo(fileHolder.asOutputStream());
        } catch (final OXException e) {
            if (!MailExceptionCode.NO_CONTENT.equals(e)) {
                throw e;
            }
            LOG.debug("", e);
            fileHolder = new ThresholdFileHolder();
            fileHolder.write(new byte[0]);
        }
        // Filter
        if (null != mimeFilter) {
            InputStream is = fileHolder.getStream();
            try {
                // Store to MIME message
                MimeMessage mimeMessage = new MimeMessage(MimeDefaultSession.getDefaultSession(), is);
                // Clean-up
                Streams.close(is);
                is = null;
                fileHolder.close();
                // Filter MIME message
                MimeMessageConverter.saveChanges(mimeMessage);
                mimeMessage = mimeFilter.filter(mimeMessage);
                fileHolder = new ThresholdFileHolder();
                mimeMessage.writeTo(fileHolder.asOutputStream());
            } finally {
                Streams.close(is);
            }
        }
        return fileHolder;
    }

    private static final String formatMessageHeaders(final Iterator<Map.Entry<String, String>> iter) {
        final StringBuilder sb = new StringBuilder(1024);
        final String delim = ": ";
        final String crlf = "\r\n";
        while (iter.hasNext()) {
            final Map.Entry<String, String> entry = iter.next();
            sb.append(entry.getKey()).append(delim).append(entry.getValue()).append(crlf);
        }
        return sb.toString();
    }

    private static String saneForFileName(final String fileName) {
        if (isEmpty(fileName)) {
            return fileName;
        }
        final int len = fileName.length();
        final StringBuilder sb = new StringBuilder(len);
        char prev = '\0';
        for (int i = 0; i < len; i++) {
            final char c = fileName.charAt(i);
            if (Strings.isWhitespace(c)) {
                if (prev != '_') {
                    prev = '_';
                    sb.append(prev);
                }
            } else if ('/' == c) {
                if (prev != '_') {
                    prev = '_';
                    sb.append(prev);
                }
            } else if ('\\' == c) {
                if (prev != '_') {
                    prev = '_';
                    sb.append(prev);
                }
            } else {
                prev = '\0';
                sb.append(c);
            }
        }
        return sb.toString();
    }

}
