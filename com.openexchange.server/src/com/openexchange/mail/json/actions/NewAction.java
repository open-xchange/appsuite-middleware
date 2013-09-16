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

import java.util.ArrayList;
import java.util.List;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import org.apache.commons.logging.Log;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONValue;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.Mail;
import com.openexchange.ajax.fields.DataFields;
import com.openexchange.ajax.fields.FolderChildFields;
import com.openexchange.ajax.helper.ParamContainer;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestDataTools;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.documentation.RequestMethod;
import com.openexchange.documentation.annotations.Action;
import com.openexchange.documentation.annotations.Parameter;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.upload.impl.UploadEvent;
import com.openexchange.java.Charsets;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.MailJSONField;
import com.openexchange.mail.MailServletInterface;
import com.openexchange.mail.api.IMailFolderStorage;
import com.openexchange.mail.api.IMailMessageStorage;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.cache.MailMessageCache;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.dataobjects.compose.ComposeType;
import com.openexchange.mail.dataobjects.compose.ComposedMailMessage;
import com.openexchange.mail.dataobjects.compose.ContentAwareComposedMailMessage;
import com.openexchange.mail.json.MailRequest;
import com.openexchange.mail.json.parser.MessageParser;
import com.openexchange.mail.mime.MessageHeaders;
import com.openexchange.mail.mime.MimeDefaultSession;
import com.openexchange.mail.mime.MimeMailException;
import com.openexchange.mail.mime.QuotedInternetAddress;
import com.openexchange.mail.mime.converters.MimeMessageConverter;
import com.openexchange.mail.mime.dataobjects.MimeMailMessage;
import com.openexchange.mail.transport.MailTransport;
import com.openexchange.mail.utils.MailFolderUtility;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.preferences.ServerUserSetting;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.stream.UnsynchronizedByteArrayInputStream;


/**
 * {@link NewAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@Action(method = RequestMethod.PUT, name = "new", description = "Send/Save mail as MIME data block (RFC822) (added in SP5)", parameters = {
    @Parameter(name = "session", description = "A session ID previously obtained from the login module."),
    @Parameter(name = "folder", optional=true, description = "In case the mail should not be sent out, but saved in a specific folder, the \"folder\" parameter can be used. If the mail should be sent out to the recipient, the \"folder\" parameter must not be included and the mail is stored in the folder \"Sent Items\". Example \"folder=default.INBOX/Testfolder\""),
    @Parameter(name = "flags", optional=true, description = "In case the mail should be stored with status \"read\" (e.g. mail has been read already in the client inbox), the parameter \"flags\" has to be included. If no \"folder\" parameter is specified, this parameter must not be included. For infos about mail flags see Detailed mail data spec.")
}, requestBody = "The MIME Data Block.",
responseDescription = "Object ID of the newly created/moved mail.")
public final class NewAction extends AbstractMailAction {

    private static final Log LOG = com.openexchange.log.Log.loggerFor(NewAction.class);
    private static final boolean DEBUG = LOG.isDebugEnabled();

    private static final String FLAGS = MailJSONField.FLAGS.getKey();
    private static final String FROM = MailJSONField.FROM.getKey();
    private static final String ATTACHMENTS = MailJSONField.ATTACHMENTS.getKey();
    private static final String CONTENT = MailJSONField.CONTENT.getKey();
    private static final String UPLOAD_FORMFIELD_MAIL = AJAXServlet.UPLOAD_FORMFIELD_MAIL;

    /**
     * Initializes a new {@link NewAction}.
     * @param services
     */
    public NewAction(final ServiceLookup services) {
        super(services);
    }

    @Override
    protected AJAXRequestResult perform(final MailRequest req) throws OXException {
        final AJAXRequestData request = req.getRequest();
        final List<OXException> warnings = new ArrayList<OXException>();
        try {
            if (request.hasUploads() || request.getParameter(UPLOAD_FORMFIELD_MAIL) != null) {
                return performWithUploads(req, request, warnings);
            }
            return performWithoutUploads(req, warnings);
        } catch (final JSONException e) {
            throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
        } catch (final MessagingException e) {
            throw MimeMailException.handleMessagingException(e);
        } catch (final RuntimeException e) {
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    private AJAXRequestResult performWithUploads(final MailRequest req, final AJAXRequestData request, final List<OXException> warnings) throws OXException, JSONException {
        final ServerSession session = req.getSession();
        final UploadEvent uploadEvent = request.getUploadEvent();
        String msgIdentifier = null;
        {
            final JSONObject jMail;
            {
                final String json0 = uploadEvent.getFormField(UPLOAD_FORMFIELD_MAIL);
                if (json0 == null || json0.trim().length() == 0) {
                    throw MailExceptionCode.PROCESSING_ERROR.create(MailExceptionCode.MISSING_PARAM.create(UPLOAD_FORMFIELD_MAIL), new Object[0]);
                }
                jMail = new JSONObject(json0);
            }
            /*-
             * Parse
             *
             * Resolve "From" to proper mail account to select right transport server
             */
            final InternetAddress from;
            try {
                from = MessageParser.parseAddressKey(MailJSONField.FROM.getKey(), jMail, true)[0];
            } catch (final AddressException e) {
                throw MimeMailException.handleMessagingException(e);
            }
            int accountId;
            try {
                accountId = resolveFrom2Account(session, from, true, true);
            } catch (final OXException e) {
                if (MailExceptionCode.NO_TRANSPORT_SUPPORT.equals(e) || MailExceptionCode.INVALID_SENDER.equals(e)) {
                    // Re-throw
                    throw e;
                }
                LOG.warn(new com.openexchange.java.StringAllocator(128).append(e.getMessage()).append(". Using default account's transport.").toString());
                // Send with default account's transport provider
                accountId = MailAccount.DEFAULT_ID;
            }
            final MailServletInterface mailInterface = getMailInterface(req);
            if ((jMail.optInt(FLAGS, 0) & MailMessage.FLAG_DRAFT) > 0) {
                /*
                 * ... and save draft
                 */
                final ComposedMailMessage composedMail = MessageParser.parse4Draft(jMail, uploadEvent, session, accountId, warnings);
                final ComposeType sendType = jMail.hasAndNotNull(Mail.PARAMETER_SEND_TYPE) ? ComposeType.getType(jMail.getInt(Mail.PARAMETER_SEND_TYPE)) : null;
                if (null != sendType) {
                    composedMail.setSendType(sendType);
                }
                msgIdentifier = mailInterface.saveDraft(composedMail, false, accountId);
                if (msgIdentifier == null) {
                    throw MailExceptionCode.DRAFT_FAILED_UNKNOWN.create();
                }
                warnings.addAll(mailInterface.getWarnings());
            } else {
                /*
                 * ... and send message
                 */
                final String protocol = request.isSecure() ? "https://" : "http://";
                final ComposedMailMessage[] composedMails = MessageParser.parse4Transport(jMail, uploadEvent, session, accountId, protocol, request.getHostname(), warnings);
                ComposeType sendType = jMail.hasAndNotNull(Mail.PARAMETER_SEND_TYPE) ? ComposeType.getType(jMail.getInt(Mail.PARAMETER_SEND_TYPE)) : ComposeType.NEW;
                if (ComposeType.DRAFT.equals(sendType)) {
                    final boolean deleteDraftOnTransport = jMail.optBoolean("deleteDraftOnTransport", false);
                    if (deleteDraftOnTransport || AJAXRequestDataTools.parseBoolParameter("deleteDraftOnTransport", request)) {
                        sendType = ComposeType.DRAFT_DELETE_ON_TRANSPORT;
                    }
                }
                for (final ComposedMailMessage cm : composedMails) {
                    if (null != cm) {
                        cm.setSendType(sendType);
                    }
                }
                /*
                 * Check
                 */
                msgIdentifier = mailInterface.sendMessage(composedMails[0], sendType, accountId);
                for (int i = 1; i < composedMails.length; i++) {
                    final ComposedMailMessage cm = composedMails[i];
                    if (null != cm) {
                        mailInterface.sendMessage(cm, sendType, accountId);
                    }
                }
                warnings.addAll(mailInterface.getWarnings());
                /*
                 * Trigger contact collector
                 */
                try {
                    final ServerUserSetting setting = ServerUserSetting.getInstance();
                    final int contextId = session.getContextId();
                    final int userId = session.getUserId();
                    if (setting.isContactCollectionEnabled(contextId, userId).booleanValue() && setting.isContactCollectOnMailTransport(
                        contextId,
                        userId).booleanValue()) {
                        triggerContactCollector(session, composedMails[0]);
                    }
                } catch (final Exception e) {
                    LOG.warn("Contact collector could not be triggered.", e);
                }
            }
        }
        if (msgIdentifier == null) {
            if (warnings.isEmpty()) {
                throw MailExceptionCode.SEND_FAILED_UNKNOWN.create();
            }
            final AJAXRequestResult result = new AJAXRequestResult(JSONObject.NULL, "json");
            result.addWarnings(warnings);
            return result;
        }
        /*
         * Create JSON response object
         */
        final AJAXRequestResult result = new AJAXRequestResult(msgIdentifier, "string");
        result.addWarnings(warnings);
        return result;
    }

    private AJAXRequestResult performWithoutUploads(final MailRequest req, final List<OXException> warnings) throws OXException, MessagingException, JSONException {
        /*
         * Non-POST
         */
        final ServerSession session = req.getSession();
        /*
         * Read in parameters
         */
        final String folder = req.getParameter(AJAXServlet.PARAMETER_FOLDERID);
        final int flags;
        {
            final int i = req.optInt(Mail.PARAMETER_FLAGS);
            flags = MailRequest.NOT_FOUND == i ? 0 : i;
        }
        final boolean force;
        {
            final String tmp = req.getParameter("force");
            if (null == tmp) {
                force = false;
            } else {
                force = AJAXRequestDataTools.parseBoolParameter(tmp);
            }
        }
        // Get rfc822 bytes and create corresponding mail message
        final QuotedInternetAddress defaultSendAddr = new QuotedInternetAddress(getDefaultSendAddress(session), false);
        final PutNewMailData data;
        {
            final MimeMessage message = new MimeMessage(MimeDefaultSession.getDefaultSession(), new UnsynchronizedByteArrayInputStream(Charsets.toAsciiBytes((String) req.getRequest().requireData())));
            message.removeHeader("x-original-headers");
            final String fromAddr = message.getHeader(MessageHeaders.HDR_FROM, null);
            final InternetAddress fromAddress;
            final MailMessage mail;
            if (isEmpty(fromAddr)) {
                // Add from address
                fromAddress = defaultSendAddr;
                message.setFrom(fromAddress);
                mail = MimeMessageConverter.convertMessage(message);
            } else {
                fromAddress = new QuotedInternetAddress(fromAddr, true);
                mail = MimeMessageConverter.convertMessage(message);
            }
            data = new PutNewMailData() {

                @Override
                public MailMessage getMail() {
                    return mail;
                }

                @Override
                public InternetAddress getFromAddress() {
                    return fromAddress;
                }
            };
        }
        // Check if "folder" element is present which indicates to save given message as a draft or append to denoted folder
        final JSONValue responseData;
        if (folder == null) {
            responseData = appendDraft(session, flags, force, data.getFromAddress(), data.getMail());
        } else {
            final String[] ids;
            final MailServletInterface mailInterface = MailServletInterface.getInstance(session);
            try {
                ids = mailInterface.appendMessages(folder, new MailMessage[] { data.getMail() }, force);
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
        final AJAXRequestResult result = new AJAXRequestResult(responseData, "json");
        result.addWarnings(warnings);
        return result;
    }

    private interface PutNewMailData {

        InternetAddress getFromAddress();

        MailMessage getMail();
    }

    private JSONObject appendDraft(final ServerSession session, final int flags, final boolean force, final InternetAddress from, final MailMessage m) throws OXException, JSONException {
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
                if (!force && MailExceptionCode.INVALID_SENDER.equals(e)) {
                    throw e;
                }
                LOG.warn(new com.openexchange.java.StringAllocator(128).append(e.getMessage()).append(". Using default account's transport.").toString());
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
            final MailMessage sentMail;
            if (m instanceof MimeMailMessage) {
                sentMail = transport.sendMailMessage(new ContentAwareComposedMailMessage(((MimeMailMessage) m).getMimeMessage(), session, null), ComposeType.NEW);
            } else {
                sentMail = transport.sendRawMessage(m.getSourceBytes());
            }
            JSONObject responseData = null;
            if (!session.getUserSettingMail().isNoCopyIntoStandardSentFolder()) {
                /*
                 * Copy in sent folder allowed
                 */
                MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess = null;
                try {
                    mailAccess = MailAccess.getInstance(session, accountId);
                    mailAccess.connect();
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
                            LOG.error(e.getMessage(), e);
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
                } finally {
                    if (null != mailAccess) {
                        mailAccess.close(true);
                    }
                }
            }
            return responseData;
        } finally {
            transport.close();
        }
    }

}
