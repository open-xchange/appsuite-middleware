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

package com.openexchange.mail.json.actions;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringOutputStream;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.Mail;
import com.openexchange.ajax.container.ThresholdFileHolder;
import com.openexchange.ajax.helper.DownloadUtility;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.documentation.RequestMethod;
import com.openexchange.documentation.annotations.Action;
import com.openexchange.documentation.annotations.Parameter;
import com.openexchange.exception.OXException;
import com.openexchange.java.CharsetDetector;
import com.openexchange.java.Streams;
import com.openexchange.java.StringAllocator;
import com.openexchange.log.Log;
import com.openexchange.log.LogProperties;
import com.openexchange.log.Props;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.MailServletInterface;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.json.MailRequest;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.mail.mime.MimeDefaultSession;
import com.openexchange.mail.mime.MimeFilter;
import com.openexchange.mail.mime.MimeMailException;
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
    @Parameter(name = "unseen", optional=true, description = "\"1\" or \"true\" to leave an unseen mail as unseen although its content is requested")
}, responseDescription = "(not IMAP: with timestamp): An JSON object containing all data of the requested mail. The fields of the object are listed in Detailed mail data. The fields id and attachment are not included. NOTE: Of course response is not a JSON object if either parameter hdr or parameter src are set to \"1\". Then the response contains plain text. Moreover if optional parameter save is set to \"1\" the complete message source is going to be directly written to output stream to open browser's save dialog.")
public final class GetAction extends AbstractMailAction {

    private static final org.apache.commons.logging.Log LOG = Log.valueOf(com.openexchange.log.LogFactory.getLog(GetAction.class));

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
    protected AJAXRequestResult perform(final MailRequest req) throws OXException {
        final JSONArray paths = (JSONArray) req.getRequest().getData();
        if (null == paths) {
            return performGet(req);
        }
        return performPut(req, paths);
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

    private AJAXRequestResult performGet(final MailRequest req) throws OXException {
        final Props logProperties = LogProperties.getLogProperties();
        try {
            final ServerSession session = req.getSession();
            /*
             * Read in parameters
             */
            final String folderPath = req.checkParameter(AJAXServlet.PARAMETER_FOLDERID);
            // final String uid = paramContainer.checkStringParam(PARAMETER_ID);
            String tmp = req.getParameter(Mail.PARAMETER_SHOW_SRC);
            final boolean showMessageSource = ("1".equals(tmp) || Boolean.parseBoolean(tmp));
            tmp = req.getParameter(Mail.PARAMETER_SHOW_HEADER);
            final boolean showMessageHeaders = ("1".equals(tmp) || Boolean.parseBoolean(tmp));
            tmp = req.getParameter(Mail.PARAMETER_SAVE);
            final boolean saveToDisk = (tmp != null && tmp.length() > 0 && Integer.parseInt(tmp) > 0);
            tmp = req.getParameter(Mail.PARAMETER_UNSEEN);
            final boolean unseen = (tmp != null && ("1".equals(tmp) || Boolean.parseBoolean(tmp)));
            tmp = req.getParameter("ignorable");
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
            /*
             * Warnings container
             */
            final List<OXException> warnings = new ArrayList<OXException>(2);
            /*
             * Get mail interface
             */
            final MailServletInterface mailInterface = getMailInterface(req);
            /*
             * Determine mail identifier
             */
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
            logProperties.put(LogProperties.Name.MAIL_MAIL_ID, uid);
            logProperties.put(LogProperties.Name.MAIL_FULL_NAME, folderPath);
            AJAXRequestResult data = getJSONNullResult();
            if (showMessageSource) {
                /*
                 * Get message
                 */
                final MailMessage mail = mailInterface.getMessage(folderPath, uid);
                if (mail == null) {
                    throw MailExceptionCode.MAIL_NOT_FOUND.create(uid, folderPath);
                }
                /*
                 * Direct response if possible
                 */
                if (null == mimeFilter) {
                    final AJAXRequestData requestData = req.getRequest();
                    final OutputStream directOutputStream = requestData.optOutputStream();
                    if (null != directOutputStream) {
                        try {
                            final boolean wasUnseen = (mail.containsPrevSeen() && !mail.isPrevSeen());
                            final boolean doUnseen = (unseen && wasUnseen);
                            if (saveToDisk) {
                                requestData.setResponseHeader("Content-Type", "application/octet-stream");
                                final StringAllocator sb = new StringAllocator(64).append("attachment");
                                {
                                    final String subject = mail.getSubject();
                                    final String fileName = isEmpty(subject) ? "mail.eml" : saneForFileName(subject) + ".eml";
                                    DownloadUtility.appendFilenameParameter(fileName, requestData.getUserAgent(), sb);
                                }
                                requestData.setResponseHeader("Content-Disposition",  sb.toString());
                                mail.writeTo(directOutputStream);
                                directOutputStream.flush();
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
                            }
                            final AJAXRequestResult requestResult = new AJAXRequestResult(AJAXRequestResult.DIRECT_OBJECT, "direct");
                            if (doUnseen) {
                                mailInterface.updateMessageFlags(folderPath, new String[] { uid }, MailMessage.FLAG_SEEN, false);
                            }
                            return requestResult;
                        } catch (final Exception e) {
                            LOG.debug(e.getMessage(), e);
                        }
                    }
                }
                /*-
                 * The regular way...
                 */
                @SuppressWarnings("resource")
                ThresholdFileHolder fileHolder = new ThresholdFileHolder();
                try {
                    mail.writeTo(fileHolder.asOutputStream());
                } catch (final OXException e) {
                    if (!MailExceptionCode.NO_CONTENT.equals(e)) {
                        throw e;
                    }
                    LOG.debug(e.getMessage(), e);
                    fileHolder = new ThresholdFileHolder();
                    fileHolder.write(new byte[0]);
                }
                // Filter
                if (null != mimeFilter) {
                    final InputStream is = fileHolder.getStream();
                    try {
                        MimeMessage mimeMessage = new MimeMessage(MimeDefaultSession.getDefaultSession(), is);
                        mimeMessage = mimeFilter.filter(mimeMessage);
                        fileHolder = new ThresholdFileHolder();
                        mimeMessage.writeTo(fileHolder.asOutputStream());
                    } finally {
                        Streams.close(is);
                    }
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
                        if (setting.isContactCollectionEnabled(contextId, userId).booleanValue() && setting.isContactCollectOnMailAccess(
                            contextId,
                            userId).booleanValue()) {
                            triggerContactCollector(session, mail);
                        }
                    } catch (final OXException e) {
                        LOG.warn("Contact collector could not be triggered.", e);
                    }
                }
                if (saveToDisk) {
                    /*
                     * Create appropriate file holder
                     */
                    req.getRequest().setFormat("file");
                    fileHolder.setContentType("application/octet-stream");
                    // Set file name
                    final String subject = mail.getSubject();
                    fileHolder.setName(new com.openexchange.java.StringAllocator(isEmpty(subject) ? "mail" : saneForFileName(subject)).append(".eml").toString());
                    return new AJAXRequestResult(fileHolder, "file");
                }
                final ContentType ct = mail.getContentType();
                if (ct.containsCharsetParameter() && CharsetDetector.isValid(ct.getCharsetParameter())) {
                    data = new AJAXRequestResult(new String(fileHolder.toByteArray(), ct.getCharsetParameter()), "string");
                } else {
                    data = new AJAXRequestResult(new String(fileHolder.toByteArray(), "UTF-8"), "string");
                }
                // final ContentType rct = new ContentType("text/plain");
                // if (ct.containsCharsetParameter() && CharsetDetector.isValid(ct.getCharsetParameter())) {
                // rct.setCharsetParameter(ct.getCharsetParameter());
                // } else {
                // rct.setCharsetParameter("UTF-8");
                // }
                // req.getRequest().setFormat("file");
                // final ByteArrayFileHolder fileHolder = new ByteArrayFileHolder(baos.toByteArray());
                // fileHolder.setContentType(rct.toString());
                // fileHolder.setName("msgsrc.txt");
                // data = new AJAXRequestResult(fileHolder, "file");
            } else if (showMessageHeaders) {
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
                final ContentType rct = new ContentType("text/plain");
                final ContentType ct = mail.getContentType();
                if (ct.containsCharsetParameter() && CharsetDetector.isValid(ct.getCharsetParameter())) {
                    rct.setCharsetParameter(ct.getCharsetParameter());
                } else {
                    rct.setCharsetParameter("UTF-8");
                }
                // req.getRequest().setFormat("file");
                // final String sHeaders = formatMessageHeaders(mail.getHeadersIterator());
                // final ByteArrayFileHolder fileHolder = new ByteArrayFileHolder(sHeaders.getBytes(rct.getCharsetParameter()));
                // fileHolder.setContentType(rct.toString());
                // data = new AJAXRequestResult(fileHolder, "file");
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
                        if (setting.isContactCollectionEnabled(contextId, userId).booleanValue() && setting.isContactCollectOnMailAccess(
                            contextId,
                            userId).booleanValue()) {
                            triggerContactCollector(session, mail);
                        }
                    } catch (final OXException e) {
                        LOG.warn("Contact collector could not be triggered.", e);
                    }
                }
            } else {
//                tmp = req.getParameter(Mail.PARAMETER_EDIT_DRAFT);
//                final boolean editDraft = ("1".equals(tmp) || Boolean.parseBoolean(tmp));
//                tmp = req.getParameter(Mail.PARAMETER_VIEW);
//                final String view = null == tmp ? null : tmp.toLowerCase(Locale.ENGLISH);
//                tmp = null;
//                final UserSettingMail usmNoSave = (UserSettingMail) session.getUserSettingMail().clone();
//                /*
//                 * Deny saving for this request-specific settings
//                 */
//                usmNoSave.setNoSave(true);
//                /*
//                 * Overwrite settings with request's parameters
//                 */
//                detectDisplayMode(editDraft, view, usmNoSave);
                /*
                 * Get message
                 */
                final MailMessage mail = mailInterface.getMessage(folderPath, uid);
                if (mail == null) {
                    throw MailExceptionCode.MAIL_NOT_FOUND.create(uid, folderPath);
                }
                if (!mail.containsAccountId()) {
                    mail.setAccountId(mailInterface.getAccountID());
                }
                data = new AJAXRequestResult(mail, "mail");
            }
            data.addWarnings(warnings);
            return data;
        } catch (final OXException e) {
            if (MailExceptionCode.MAIL_NOT_FOUND.equals(e)) {
                LOG.warn(
                    new com.openexchange.java.StringAllocator("Requested mail could not be found. ").append(
                        "Most likely this is caused by concurrent access of multiple clients ").append(
                        "while one performed a delete on affected mail.").toString(),
                    e);
                final Object[] args = e.getDisplayArgs();
                final String uid = null == args || 0 == args.length ? null : (null == args[0] ? null : args[0].toString());
                if ("undefined".equalsIgnoreCase(uid)) {
                    throw MailExceptionCode.PROCESSING_ERROR.create(e, new Object[0]);
                }
            } else {
                LOG.error(e.getMessage(), e);
            }
            throw e;
        } catch (final MessagingException e) {
            throw MimeMailException.handleMessagingException(e);
        } catch (final IOException e) {
            if ("com.sun.mail.util.MessageRemovedIOException".equals(e.getClass().getName())) {
                throw MailExceptionCode.MAIL_NOT_FOUND_SIMPLE.create(e);
            }
            throw MailExceptionCode.IO_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            logProperties.remove(LogProperties.Name.MAIL_MAIL_ID);
            logProperties.remove(LogProperties.Name.MAIL_FULL_NAME);
        }
    }

    private static final String formatMessageHeaders(final Iterator<Map.Entry<String, String>> iter) {
        final com.openexchange.java.StringAllocator sb = new com.openexchange.java.StringAllocator(1024);
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
        final com.openexchange.java.StringAllocator sb = new com.openexchange.java.StringAllocator(len);
        char prev = '\0';
        for (int i = 0; i < len; i++) {
            final char c = fileName.charAt(i);
            if (Character.isWhitespace(c)) {
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
