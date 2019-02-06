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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.mail.compose.impl;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.util.UUIDs.getUnformattedString;
import static com.openexchange.mail.mime.utils.MimeMessageUtility.parseAddressList;
import static com.openexchange.mail.mime.utils.MimeMessageUtility.unfold;
import static com.openexchange.mail.text.TextProcessing.performLineFolding;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.activation.DataHandler;
import javax.mail.BodyPart;
import javax.mail.Flags;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.InternetHeaders;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;
import javax.mail.internet.idn.IDNA;
import javax.servlet.http.HttpServletRequest;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import com.google.common.base.CharMatcher;
import com.google.common.collect.ImmutableMap;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.AJAXUtility;
import com.openexchange.ajax.container.ThresholdFileHolder;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestDataTools;
import com.openexchange.ajax.requesthandler.crypto.CryptographicServiceAuthenticationFactory;
import com.openexchange.capabilities.CapabilityService;
import com.openexchange.config.ConfigurationService;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.upload.StreamedUploadFile;
import com.openexchange.groupware.upload.StreamedUploadFileIterator;
import com.openexchange.html.HtmlService;
import com.openexchange.i18n.LocaleTools;
import com.openexchange.image.ImageActionFactory;
import com.openexchange.image.ImageDataSource;
import com.openexchange.image.ImageLocation;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
import com.openexchange.java.util.UUIDs;
import com.openexchange.mail.FullnameArgument;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.MailJSONField;
import com.openexchange.mail.MailPath;
import com.openexchange.mail.MailServletInterface;
import com.openexchange.mail.MailSessionCache;
import com.openexchange.mail.MailSessionParameterNames;
import com.openexchange.mail.api.FromAddressProvider;
import com.openexchange.mail.api.IMailFolderStorage;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.compose.Address;
import com.openexchange.mail.compose.Attachment;
import com.openexchange.mail.compose.AttachmentDescription;
import com.openexchange.mail.compose.AttachmentOrigin;
import com.openexchange.mail.compose.AttachmentStorage;
import com.openexchange.mail.compose.AttachmentStorageService;
import com.openexchange.mail.compose.CompositionSpace;
import com.openexchange.mail.compose.CompositionSpaceDescription;
import com.openexchange.mail.compose.CompositionSpaceErrorCode;
import com.openexchange.mail.compose.CompositionSpaceService;
import com.openexchange.mail.compose.CompositionSpaceStorageService;
import com.openexchange.mail.compose.CompositonSpaces;
import com.openexchange.mail.compose.Message;
import com.openexchange.mail.compose.Message.Priority;
import com.openexchange.mail.compose.MessageDescription;
import com.openexchange.mail.compose.MessageField;
import com.openexchange.mail.compose.Meta;
import com.openexchange.mail.compose.Meta.MetaType;
import com.openexchange.mail.compose.OpenCompositionSpaceParameters;
import com.openexchange.mail.compose.Security;
import com.openexchange.mail.compose.SharedAttachmentsInfo;
import com.openexchange.mail.compose.Type;
import com.openexchange.mail.compose.VCardAndFileName;
import com.openexchange.mail.compose.impl.attachment.AttachmentComparator;
import com.openexchange.mail.compose.impl.attachment.AttachmentImageDataSource;
import com.openexchange.mail.config.MailProperties;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.dataobjects.SecuritySettings;
import com.openexchange.mail.dataobjects.compose.ComposeType;
import com.openexchange.mail.dataobjects.compose.ComposedMailMessage;
import com.openexchange.mail.dataobjects.compose.ContentAwareComposedMailMessage;
import com.openexchange.mail.dataobjects.compose.TextBodyMailPart;
import com.openexchange.mail.json.compose.ComposeHandler;
import com.openexchange.mail.json.compose.ComposeHandlerRegistry;
import com.openexchange.mail.json.compose.ComposeRequest;
import com.openexchange.mail.json.compose.ComposeTransportResult;
import com.openexchange.mail.mime.ContentDisposition;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.mail.mime.MessageHeaders;
import com.openexchange.mail.mime.MimeDefaultSession;
import com.openexchange.mail.mime.MimeMailException;
import com.openexchange.mail.mime.MimeSmilFixer;
import com.openexchange.mail.mime.MimeType2ExtMap;
import com.openexchange.mail.mime.MimeTypes;
import com.openexchange.mail.mime.QuotedInternetAddress;
import com.openexchange.mail.mime.datasource.MessageDataSource;
import com.openexchange.mail.mime.filler.MimeMessageFiller;
import com.openexchange.mail.mime.processing.MimeProcessingUtility;
import com.openexchange.mail.mime.processing.TextAndContentType;
import com.openexchange.mail.mime.utils.MimeMessageUtility;
import com.openexchange.mail.parser.MailMessageParser;
import com.openexchange.mail.parser.handlers.InlineContentHandler;
import com.openexchange.mail.parser.handlers.NonInlineForwardPartHandler;
import com.openexchange.mail.text.HtmlProcessing;
import com.openexchange.mail.transport.MtaStatusInfo;
import com.openexchange.mail.transport.TransportProvider;
import com.openexchange.mail.transport.TransportProviderRegistry;
import com.openexchange.mail.usersetting.UserSettingMail;
import com.openexchange.mail.utils.ContactCollectorUtility;
import com.openexchange.mail.utils.MailPasswordUtil;
import com.openexchange.mail.utils.MessageUtility;
import com.openexchange.mail.utils.StorageUtility;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.mailaccount.UnifiedInboxManagement;
import com.openexchange.mailaccount.UnifiedInboxUID;
import com.openexchange.preferences.ServerUserSetting;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;

/**
 * {@link CompositionSpaceServiceImpl} - The composition space service implementation.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.2
 */
public class CompositionSpaceServiceImpl implements CompositionSpaceService {

    /** Simple class to delay initialization until needed */
    private static class LoggerHolder {

        static final Logger LOG = org.slf4j.LoggerFactory.getLogger(CompositionSpaceServiceImpl.class);
    }

    private static final com.openexchange.mail.compose.Message.ContentType TEXT_PLAIN = com.openexchange.mail.compose.Message.ContentType.TEXT_PLAIN;
    private static final com.openexchange.mail.compose.Message.ContentType TEXT_HTML = com.openexchange.mail.compose.Message.ContentType.TEXT_HTML;

    private static final com.openexchange.mail.compose.Attachment.ContentDisposition ATTACHMENT = com.openexchange.mail.compose.Attachment.ContentDisposition.ATTACHMENT;
    private static final com.openexchange.mail.compose.Attachment.ContentDisposition INLINE = com.openexchange.mail.compose.Attachment.ContentDisposition.INLINE;

    private static final String HEADER_X_OX_SHARED_ATTACHMENTS = MessageHeaders.HDR_X_OX_SHARED_ATTACHMENTS;
    private static final String HEADER_X_OX_SECURITY = MessageHeaders.HDR_X_OX_SECURITY;
    private static final String HEADER_X_OX_META = MessageHeaders.HDR_X_OX_META;
    private static final String HEADER_X_OX_READ_RECEIPT = MessageHeaders.HDR_X_OX_READ_RECEIPT;

    private final ServiceLookup services;
    private final CompositionSpaceStorageService storageService;
    private final AttachmentStorageService attachmentStorageService;
    private volatile Set<String> octetExtensions;

    /**
     * Initializes a new {@link CompositionSpaceServiceImpl}.
     *
     * @param storageService The storage service
     * @param attachmentStorageService The attachment storage service
     * @param services The service look-up
     */
    public CompositionSpaceServiceImpl(CompositionSpaceStorageService storageService, AttachmentStorageService attachmentStorageService, ServiceLookup services) {
        super();
        if (null == storageService) {
            throw new IllegalArgumentException("Storage service must not be null");
        }
        if (null == attachmentStorageService) {
            throw new IllegalArgumentException("Attachment storage service must not be null");
        }
        if (null == services) {
            throw new IllegalArgumentException("Service registry must not be null");
        }
        this.storageService = storageService;
        this.attachmentStorageService = attachmentStorageService;
        this.services = services;
    }

    private CompositionSpaceStorageService getStorageService() {
        return storageService;
    }

    /**
     * Gets the attachment storage for given session.
     *
     * @return The composition space service
     * @throws OXException If composition space service cannot be returned
     */
    private AttachmentStorage getAttachmentStorage(Session session) throws OXException {
        return attachmentStorageService.getAttachmentStorageFor(session);
    }

    private Set<String> octetExtensions() {
        Set<String> tmp = octetExtensions;
        if (null == tmp) {
            synchronized (CompositionSpaceServiceImpl.class) {
                tmp = octetExtensions;
                if (null == tmp) {
                    String defaultValue = "pgp";
                    ConfigurationService service = services.getService(ConfigurationService.class);
                    if (null == service) {
                        return new HashSet<String>(Arrays.asList(defaultValue));
                    }
                    String csv = service.getProperty("com.openexchange.mail.octetExtensions", defaultValue);
                    tmp = new HashSet<String>(Arrays.asList(Strings.splitByComma(csv)));
                    octetExtensions = tmp;
                }
            }
        }
        return tmp;
    }

    @Override
    public MailPath transportCompositionSpace(UUID compositionSpaceId, UserSettingMail mailSettings, AJAXRequestData request, List<OXException> warnings, boolean deleteAfterTransport, Session ses) throws OXException {
        CompositionSpace compositionSpace = getCompositionSpace(compositionSpaceId, ses);

        Message m = compositionSpace.getMessage();
        if (null == m) {
            return null;
        }

        // Check if attachments are supposed to be shared
        SharedAttachmentsInfo sharedAttachmentsInfo = m.getSharedAttachments();
        if (null != sharedAttachmentsInfo && sharedAttachmentsInfo.isEnabled() && false == mayShareAttachments(ses)) {
            // User wants to share attachments, but is not allowed to do so
            throw MailExceptionCode.SHARING_NOT_POSSIBLE.create(I(ses.getUserId()), I(ses.getContextId()));
        }

        // Yield server session
        ServerSession session = ServerSessionAdapter.valueOf(ses);

        // Determine the account identifier by From address
        int accountId;
        try {
            InternetAddress fromAddresss = toMimeAddress(m.getFrom());
            accountId = MimeMessageFiller.resolveFrom2Account(session, fromAddresss, true, true);
        } catch (final OXException e) {
            if (MailExceptionCode.NO_TRANSPORT_SUPPORT.equals(e) || MailExceptionCode.INVALID_SENDER.equals(e)) {
                // Re-throw
                throw e;
            }
            LoggerHolder.LOG.warn("{}. Using default account's transport.", e.getMessage());
            // Send with default account's transport provider
            accountId = MailAccount.DEFAULT_ID;
        }

        // Prepare text content
        List<Attachment> attachments = m.getAttachments();
        String content = m.getContent();
        if (null == content) {
            LoggerHolder.LOG.warn("Missing content in composition space {}. Using empty text instead.", getUnformattedString(compositionSpaceId));
            content = "";
        }
        if (TEXT_HTML == m.getContentType()) {
            // An HTML message...
            if (attachments != null && !attachments.isEmpty()) {
                // ... with attachments
                Map<UUID, Attachment> fileAttachments = new LinkedHashMap<>(attachments.size());
                for (Attachment attachment : attachments) {
                    fileAttachments.put(attachment.getId(), attachment);
                }

                if (Strings.isNotEmpty(content)) {
                    // Replace image URLs with src="cid:1234"
                    int numOfAttachments = fileAttachments.size();
                    Map<String, Attachment> contentId2InlineAttachment = new HashMap<>(numOfAttachments);
                    Map<String, Attachment> attachmentId2inlineAttachments = new HashMap<>(numOfAttachments);

                    for (Attachment attachment : fileAttachments.values()) {
                        if (INLINE == attachment.getContentDisposition() && null != attachment.getContentId() && new ContentType(attachment.getMimeType()).startsWith("image/")) {
                            attachmentId2inlineAttachments.put(getUnformattedString(attachment.getId()), attachment);
                            contentId2InlineAttachment.put(attachment.getContentId(), attachment);
                        }
                    }
                    content = replaceLinkedInlineImages(content, attachmentId2inlineAttachments, contentId2InlineAttachment, fileAttachments, session);
                }
            }
        }

        // Create a new compose message
        TransportProvider provider = TransportProviderRegistry.getTransportProviderBySession(session, accountId);
        ComposedMailMessage sourceMessage = provider.getNewComposedMailMessage(session, session.getContext());
        sourceMessage.setAccountId(accountId);

        // From
        {
            Address from = m.getFrom();
            if (null == from) {
                throw MailExceptionCode.MISSING_FIELD.create(MailJSONField.FROM.getKey());
            }
            sourceMessage.addFrom(toMimeAddress(from));
        }

        // Recipients
        {
            boolean anyRecipientSet = false;
            List<Address> to = m.getTo();
            if (null != to) {
                sourceMessage.addTo(toMimeAddresses(to));
                anyRecipientSet = true;
            }


            List<Address> cc = m.getCc();
            if (null != cc) {
                sourceMessage.addCc(toMimeAddresses(cc));
                anyRecipientSet = true;
            }


            List<Address> bcc = m.getBcc();
            if (null != bcc) {
                sourceMessage.addBcc(toMimeAddresses(bcc));
                anyRecipientSet = true;
            }

            if (false == anyRecipientSet) {
                throw MailExceptionCode.MISSING_FIELD.create("To");
            }
        }

        // Subject
        {
            String subject = m.getSubject();
            if (null != subject) {
                sourceMessage.setSubject(subject, true);
            }
        }

        // Read receipt
        if (m.isRequestReadReceipt()) {
            sourceMessage.setDispositionNotification(toMimeAddress(m.getFrom()));
        }

        // Priority
        {
            Priority priority = m.getPriority();
            sourceMessage.setHeader(MessageHeaders.HDR_X_PRIORITY, String.valueOf(priority.getLevel()));
            if (Priority.NORMAL == priority) {
                sourceMessage.setHeader(MessageHeaders.HDR_IMPORTANCE, "Medium");
            } else if (Priority.LOW == priority) {
                sourceMessage.setHeader(MessageHeaders.HDR_IMPORTANCE, "Low");
            } else {
                sourceMessage.setHeader(MessageHeaders.HDR_IMPORTANCE, "High");
            }
        }


        // Security
        {
            CryptographicServiceAuthenticationFactory authenticationFactory = null == services ? null : services.getOptionalService(CryptographicServiceAuthenticationFactory.class);
            String authentication = null;
            if (authenticationFactory != null) {
                authentication = authenticationFactory.createAuthenticationFrom(request);
            }

            Security security = m.getSecurity();
            if (null != security && false == security.isDisabled()) {
                SecuritySettings settings = SecuritySettings.builder()
                    .encrypt(security.isEncrypt())
                    .pgpInline(security.isPgpInline())
                    .sign(security.isSign())
                    .authentication(authentication)
                    .guestLanguage(security.getLanguage())
                    .guestMessage(security.getMessage())
                    .pin(security.getPin())
                    .build();
                if (settings.anythingSet()) {
                    sourceMessage.setSecuritySettings(settings);
                }
            }
        }

        // Create a new text part instance
        TextBodyMailPart textPart = provider.getNewTextBodyPart(content);
        textPart.setContentType(m.getContentType().getId());
        if (TEXT_PLAIN == m.getContentType()) {
            textPart.setPlainText(content);
        }

        // Apply content type to compose message as well
        sourceMessage.setContentType(textPart.getContentType());
        // sourceMessage.setBodyPart(textPart); --> Happens in 'c.o.mail.json.compose.AbstractComposeHandler.doCreateTransportResult()'

        // Collect parts
        List<MailPart> parts;
        if (attachments != null && !attachments.isEmpty()) {
            parts = new ArrayList<MailPart>(attachments.size());
            for (Attachment attachment : attachments) {
                parts.add(new AttachmentMailPart(attachment));
            }
        } else {
            parts = Collections.emptyList();
        }

        // Check for shared attachments
        Map<String, Object> params = Collections.emptyMap();
        if (null != sharedAttachmentsInfo && sharedAttachmentsInfo.isEnabled()) {
            ImmutableMap.Builder<String, Object> parameters = ImmutableMap.builder();

            try {
                JSONObject jShareAttachmentOptions = new JSONObject(6);
                jShareAttachmentOptions.put("enable", sharedAttachmentsInfo.isEnabled());
                jShareAttachmentOptions.put("autodelete", sharedAttachmentsInfo.isAutoDelete());
                String password = sharedAttachmentsInfo.getPassword();
                if (null != password) {
                    jShareAttachmentOptions.put("password", password);
                }
                Date expiryDate = sharedAttachmentsInfo.getExpiryDate();
                if (null != expiryDate) {
                    jShareAttachmentOptions.put("expiry_date", expiryDate.getTime());
                }

                parameters.put("share_attachments", jShareAttachmentOptions);
            } catch (JSONException e) {
                throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
            }

            params = parameters.build();
        }

        // Create compose request to process
        ComposeRequest composeRequest = new ComposeRequest(accountId, sourceMessage, textPart, parts, params, request, warnings);

        // Determine appropriate compose handler
        ComposeHandlerRegistry handlerRegistry = services.getService(ComposeHandlerRegistry.class);
        ComposeHandler composeHandler = handlerRegistry.getComposeHandlerFor(composeRequest);

        MailPath sentMailPath = null;
        MailServletInterface mailInterface = null;
        ComposeTransportResult transportResult = null;
        try {
            boolean newMessageId = AJAXRequestDataTools.parseBoolParameter(AJAXServlet.ACTION_NEW, request);

            // As new/transport message
            transportResult = composeHandler.createTransportResult(composeRequest);
            List<? extends ComposedMailMessage> composedMails = transportResult.getTransportMessages();
            ComposedMailMessage sentMessage = transportResult.getSentMessage();
            boolean transportEqualToSent = transportResult.isTransportEqualToSent();

            if (newMessageId) {
                for (ComposedMailMessage composedMail : composedMails) {
                    if (null != composedMail) {
                        composedMail.removeHeader("Message-ID");
                        composedMail.removeMessageId();
                    }
                }
                sentMessage.removeHeader("Message-ID");
                sentMessage.removeMessageId();
            }

            for (ComposedMailMessage cm : composedMails) {
                if (null != cm) {
                    cm.setSendType(ComposeType.NEW);
                }
            }

            // User settings
            UserSettingMail usm = session.getUserSettingMail();
            usm.setNoSave(true);
            {
                final String paramName = "copy2Sent";
                if (request.containsParameter(paramName)) { // Provided as URL parameter
                    String sCopy2Sent = request.getParameter(paramName);
                    if (null != sCopy2Sent) {
                        if (AJAXRequestDataTools.parseBoolParameter(sCopy2Sent)) {
                            usm.setNoCopyIntoStandardSentFolder(false);
                        } else if (Boolean.FALSE.equals(AJAXRequestDataTools.parseFalseBoolParameter(sCopy2Sent))) {
                            // Explicitly deny copy to sent folder
                            usm.setNoCopyIntoStandardSentFolder(true);
                        }
                    }
                }
            }
            checkAndApplyLineWrapAfter(request, usm);
            for (ComposedMailMessage cm : composedMails) {
                if (null != cm) {
                    cm.setMailSettings(usm);
                }
            }
            if (null != sentMessage) {
                sentMessage.setMailSettings(usm);
            }

            mailInterface = MailServletInterface.getInstance(session);

            // Reply or (inline) forward?
            Meta meta = m.getMeta();
            {
                MetaType metaType = meta.getType();
                if (metaType == MetaType.REPLY || metaType == MetaType.REPLY_ALL) {
                    MailMessage originalMail = requireMailMessage(meta.getReplyFor(), mailInterface);
                    setReplyHeaders(originalMail, sourceMessage);
                } else if (metaType == MetaType.FORWARD_INLINE) {
                    MailMessage originalMail = requireMailMessage(meta.getForwardsFor().get(0), mailInterface);
                    setReplyHeaders(originalMail, sourceMessage);
                }
            }

            // Do the transport...
            HttpServletRequest servletRequest = request.optHttpServletRequest();
            String remoteAddress = null == servletRequest ? request.getRemoteAddress() : servletRequest.getRemoteAddr();
            List<String> ids =  mailInterface.sendMessages(composedMails, sentMessage, transportEqualToSent, ComposeType.NEW, accountId, usm, new MtaStatusInfo(), remoteAddress);
            if (null != ids && !ids.isEmpty()) {
                String msgIdentifier = ids.get(0);
                try {
                    sentMailPath = MailPath.getMailPathFor(msgIdentifier);
                } catch (Exception x) {
                    LoggerHolder.LOG.warn("Failed toi parse mail path from {}", msgIdentifier, x);
                }
            }

            // Commit results as actual transport was executed
            {
                transportResult.commit();
                transportResult.finish();
                transportResult = null;
            }

            // Check if original mails needs to be marked or removed
            {
                MetaType metaType = meta.getType();
                if (metaType == MetaType.REPLY || metaType == MetaType.REPLY_ALL) {
                    MailPath replyFor = meta.getReplyFor();
                    try {
                        mailInterface.updateMessageFlags(replyFor.getFolderArgument(), new String[] { replyFor.getMailID() }, MailMessage.FLAG_ANSWERED, null, true);
                    } catch (Exception e) {
                        LoggerHolder.LOG.warn("Failed to mark original mail '{}' as answered", replyFor, e);
                    }
                } else if (metaType == MetaType.FORWARD_INLINE) {
                    MailPath forwardFor = meta.getForwardsFor().get(0);
                    try {
                        mailInterface.updateMessageFlags(forwardFor.getFolderArgument(), new String[] { forwardFor.getMailID() }, MailMessage.FLAG_FORWARDED, null, true);
                    } catch (Exception e) {
                        LoggerHolder.LOG.warn("Failed to mark original mail '{}' as forwarded", forwardFor, e);
                    }
                }
                MailPath editFor = meta.getEditFor();
                if (null != editFor) {
                    try {
                        mailInterface.deleteMessages(editFor.getFolderArgument(), new String[] { editFor.getMailID() }, true);
                    } catch (Exception e) {
                        LoggerHolder.LOG.warn("Failed to delete edited draft mail '{}'", editFor, e);
                    }
                }
            }

            warnings.addAll(mailInterface.getWarnings());

            // Trigger contact collector
            try {
                boolean memorizeAddresses = ServerUserSetting.getInstance().isContactCollectOnMailTransport(session.getContextId(), session.getUserId()).booleanValue();
                ContactCollectorUtility.triggerContactCollector(session, composedMails, memorizeAddresses, true);
            } catch (Exception e) {
                LoggerHolder.LOG.warn("Contact collector could not be triggered.", e);
            }

        } finally {
            if (transportResult != null) {
                transportResult.rollback();
                transportResult.finish();
                transportResult = null;
            }
            if (null != mailInterface) {
                mailInterface.close();
            }
        }

        if (deleteAfterTransport) {
            closeCompositionSpace(compositionSpaceId, session);
        }

        return sentMailPath;
    }

    private void checkAndApplyLineWrapAfter(AJAXRequestData request, UserSettingMail usm) throws OXException {
        String paramName = "lineWrapAfter";
        if (request.containsParameter(paramName)) { // Provided as URL parameter
            String sLineWrapAfter = request.getParameter(paramName);
            if (null != sLineWrapAfter) {
                try {
                    int lineWrapAfter = Integer.parseInt(sLineWrapAfter.trim());
                    usm.setAutoLinebreak(lineWrapAfter <= 0 ? 0 : lineWrapAfter);
                } catch (NumberFormatException nfe) {
                    throw AjaxExceptionCodes.INVALID_PARAMETER_VALUE.create(nfe, paramName, sLineWrapAfter);
                }
            }
        }  else {
            // Disable by default
            usm.setAutoLinebreak(0);
        }
    }

    @Override
    public MailPath saveCompositionSpaceToDraftMail(UUID compositionSpaceId, boolean deleteAfterSave, Session session) throws OXException {
        CompositionSpace compositionSpace = getCompositionSpace(compositionSpaceId, session);

        Message m = compositionSpace.getMessage();

        MailServletInterface mailInterface = null;
        try {
            MimeMessage mimeMessage = new MimeMessage(MimeDefaultSession.getDefaultSession());
            int accountId = MailAccount.DEFAULT_ID;
            {
                Address from = m.getFrom();
                if (null != from) {
                    InternetAddress fromAddress = toMimeAddress(from);
                    mimeMessage.setFrom(fromAddress);

                    // Determine the account identifier by From address
                    try {
                        InternetAddress fromAddresss = toMimeAddress(m.getFrom());
                        accountId = MimeMessageFiller.resolveFrom2Account(ServerSessionAdapter.valueOf(session), fromAddresss, true, true);
                    } catch (final OXException e) {
                        if (MailExceptionCode.NO_TRANSPORT_SUPPORT.equals(e) || MailExceptionCode.INVALID_SENDER.equals(e)) {
                            // Re-throw
                            throw e;
                        }
                        LoggerHolder.LOG.warn("{}. Using default account's transport.", e.getMessage());
                        // Send with default account's transport provider
                        accountId = MailAccount.DEFAULT_ID;
                    }
                }

                Address sender = m.getSender();
                if (null != sender) {
                    mimeMessage.setSender(toMimeAddress(sender));
                }
            }
            {
                List<Address> to = m.getTo();
                if (null != to && !to.isEmpty()) {
                    mimeMessage.setRecipients(MimeMessage.RecipientType.TO, toMimeAddresses(to));
                }
            }
            {
                List<Address> cc = m.getCc();
                if (null != cc && !cc.isEmpty()) {
                    mimeMessage.setRecipients(MimeMessage.RecipientType.CC, toMimeAddresses(cc));
                }
            }
            {
                List<Address> bcc = m.getBcc();
                if (null != bcc && !bcc.isEmpty()) {
                    mimeMessage.setRecipients(MimeMessage.RecipientType.BCC, toMimeAddresses(bcc));
                }
            }

            String subject = m.getSubject();
            if (null != subject) {
                mimeMessage.setSubject(subject, "UTF-8");
            }

            Flags msgFlags = new Flags();
            msgFlags.add(Flags.Flag.DRAFT);
            mimeMessage.setFlags(msgFlags, true);

            if (m.isRequestReadReceipt() && null != m.getFrom()) {
                mimeMessage.setHeader(MessageHeaders.HDR_X_OX_NOTIFICATION, toMimeAddress(m.getFrom()).toString());
            }

            Priority priority = m.getPriority();
            mimeMessage.setHeader(MessageHeaders.HDR_X_PRIORITY, String.valueOf(priority.getLevel()));
            if (Priority.NORMAL == priority) {
                mimeMessage.setHeader(MessageHeaders.HDR_IMPORTANCE, "Medium");
            } else if (Priority.LOW == priority) {
                mimeMessage.setHeader(MessageHeaders.HDR_IMPORTANCE, "Low");
            } else {
                mimeMessage.setHeader(MessageHeaders.HDR_IMPORTANCE, "High");
            }

            // Encode state to headers
            {
                mimeMessage.setHeader(HEADER_X_OX_META, encodeHeaderValue(11, meta2HeaderValue(m.getMeta())));
                mimeMessage.setHeader(HEADER_X_OX_SECURITY,encodeHeaderValue(15, security2HeaderValue(m.getSecurity())));
                mimeMessage.setHeader(HEADER_X_OX_SHARED_ATTACHMENTS, encodeHeaderValue(25, sharedAttachments2HeaderValue(m.getSharedAttachments())));
                if (m.isRequestReadReceipt()) {
                    mimeMessage.setHeader(HEADER_X_OX_READ_RECEIPT, encodeHeaderValue(19, "true"));
                }
            }

            // Build MIME body
            String charset = MailProperties.getInstance().getDefaultMimeCharset();
            List<Attachment> attachments = m.getAttachments();
            boolean isHtml = TEXT_HTML == m.getContentType();

            if (attachments == null || attachments.isEmpty()) {
                // No attachments
                fillMessageWithoutAttachments(m, mimeMessage, charset, isHtml);
            } else {
                // With attachments
                Map<UUID, Attachment> attachmentId2inlineAttachments = new LinkedHashMap<>(attachments.size());
                for (Attachment attachment : attachments) {
                    attachmentId2inlineAttachments.put(attachment.getId(), attachment);
                }
                fillMessageWithAttachments(m, mimeMessage, attachmentId2inlineAttachments, charset, isHtml, session);
            }

            ContentAwareComposedMailMessage mailMessage = new ContentAwareComposedMailMessage(mimeMessage, session, session.getContextId());

            mailInterface = MailServletInterface.getInstance(session);
            MailPath draftPath = mailInterface.saveDraft(mailMessage, false, accountId);

            // Check if original mails needs to be removed
            {
                MailPath editFor = m.getMeta().getEditFor();
                if (null != editFor) {
                    try {
                        mailInterface.deleteMessages(editFor.getFolderArgument(), new String[] { editFor.getMailID() }, true);
                    } catch (Exception e) {
                        LoggerHolder.LOG.warn("Failed to delete edited draft mail '{}'", editFor, e);
                    }
                }
            }

            // Close mail resources
            mailInterface.close();
            mailInterface = null;

            if (deleteAfterSave) {
                closeCompositionSpace(compositionSpaceId, session);
            }

            return draftPath;
        } catch (MessagingException e) {
            throw MimeMailException.handleMessagingException(e);
        } finally {
            if (null != mailInterface) {
                mailInterface.close();
            }
        }
    }

    private void fillMessageWithoutAttachments(Message m, MimeMessage mimeMessage, String charset, boolean isHtml) throws OXException, MessagingException {
        String content = m.getContent();
        if (Strings.isEmpty(content)) {
            if (isHtml) {
                HtmlService htmlService = services.getService(HtmlService.class);
                content = htmlService.getConformHTML(HTML_SPACE, charset).replace(HTML_SPACE, "");
            } else {
                content = "";
            }
        } else {
            if (isHtml) {
                HtmlService htmlService = services.getService(HtmlService.class);
                content = htmlService.getConformHTML(content, charset);
            } else {
                content = performLineFolding(content, 0);
            }
        }

        MessageUtility.setText(content, charset, isHtml ? "html" : "plain", mimeMessage);
        mimeMessage.setHeader(MessageHeaders.HDR_MIME_VERSION, "1.0");
        mimeMessage.setHeader(MessageHeaders.HDR_CONTENT_TYPE, new StringBuilder(24).append("text/").append(isHtml ? "html" : "plain").append("; charset=").append(charset).toString());
        if (CharMatcher.ascii().matchesAllOf(content)) {
            mimeMessage.setHeader(MessageHeaders.HDR_CONTENT_TRANSFER_ENC, "7bit");
        }
    }

    private void fillMessageWithAttachments(Message m, MimeMessage mimeMessage, Map<UUID, Attachment> fileAttachments, String charset, boolean isHtml, Session session) throws OXException, MessagingException {
        if (isHtml) {
            // An HTML message
            Map<String, Attachment> contentId2InlineAttachment;

            String content = m.getContent();
            if (Strings.isEmpty(content)) {
                contentId2InlineAttachment = Collections.emptyMap();

                HtmlService htmlService = services.getService(HtmlService.class);
                content = htmlService.getConformHTML(HTML_SPACE, charset).replace(HTML_SPACE, "");
            } else {
                int numOfAttachments = fileAttachments.size();
                contentId2InlineAttachment = new HashMap<>(numOfAttachments);
                Map<String, Attachment> attachmentId2inlineAttachments = new HashMap<>(numOfAttachments);

                for (Attachment attachment : fileAttachments.values()) {
                    if (INLINE == attachment.getContentDisposition() && null != attachment.getContentId() && new ContentType(attachment.getMimeType()).startsWith("image/")) {
                        attachmentId2inlineAttachments.put(getUnformattedString(attachment.getId()), attachment);
                        contentId2InlineAttachment.put(attachment.getContentId(), attachment);
                    }
                }
                content = replaceLinkedInlineImages(content, attachmentId2inlineAttachments, contentId2InlineAttachment, fileAttachments, session);
                HtmlService htmlService = services.getService(HtmlService.class);
                content = htmlService.getConformHTML(content, charset);
            }

            Multipart primaryMultipart;
            if (contentId2InlineAttachment.isEmpty()) {
                // No inline images. A simple multipart message
                primaryMultipart = new MimeMultipart();

                // Add text part
                primaryMultipart.addBodyPart(createHtmlBodyPart(content, charset));

                // Add attachments
                for (Attachment attachment : fileAttachments.values()) {
                    addAttachment(attachment, primaryMultipart, session);
                }
            } else {
                if (fileAttachments.isEmpty()) {
                    // Only inline images
                    primaryMultipart = createMultipartRelated(content, charset, contentId2InlineAttachment, session);
                } else {
                    // Both - file attachments and inline images
                    primaryMultipart = new MimeMultipart();

                    // Add multipart/related
                    BodyPart altBodyPart = new MimeBodyPart();
                    MessageUtility.setContent(createMultipartRelated(content, charset, contentId2InlineAttachment, session), altBodyPart);
                    primaryMultipart.addBodyPart(altBodyPart);

                    // Add remaining file attachments
                    for (Attachment fileAttachment : fileAttachments.values()) {
                        addAttachment(fileAttachment, primaryMultipart, session);
                    }
                }
            }

            mimeMessage.setContent(primaryMultipart);
        } else {
            // A plain-text message
            Multipart primaryMultipart = new MimeMultipart();

            // Add text part
            primaryMultipart.addBodyPart(createTextBodyPart(Strings.isEmpty(m.getContent()) ? "" : performLineFolding(m.getContent(), 0), charset, false));

            // Add attachments
            for (Attachment attachment : fileAttachments.values()) {
                addAttachment(attachment, primaryMultipart, session);
            }

            mimeMessage.setContent(primaryMultipart);
        }
    }

    private Multipart createMultipartRelated(String wellFormedHTMLContent, String charset, Map<String, Attachment> contentId2InlineAttachment, Session session) throws MessagingException, OXException {
        Multipart relatedMultipart = new MimeMultipart("related");

        relatedMultipart.addBodyPart(createHtmlBodyPart(wellFormedHTMLContent, charset), 0);

        for (Attachment inlineImage : contentId2InlineAttachment.values()) {
            addAttachment(inlineImage, relatedMultipart, session);
        }

        return relatedMultipart;
    }

    private void addAttachment(Attachment attachment, Multipart mp, Session session) throws MessagingException, OXException {
        ContentType ct = new ContentType(attachment.getMimeType());
        if (ct.startsWith(MimeTypes.MIME_MESSAGE_RFC822)) {
            addNestedMessage(attachment, mp);
            return;
        }

        // A non-message attachment
        String fileName = attachment.getName();
        if (fileName != null && (ct.startsWith(MimeTypes.MIME_APPL_OCTET) || ct.startsWith(MimeTypes.MIME_MULTIPART_OCTET))) {
            // Only "allowed" for certain files
            if (!octetExtensions().contains(extensionFor(fileName))) {
                // Try to determine MIME type
                String ct2 = MimeType2ExtMap.getContentType(fileName);
                int pos = ct2.indexOf('/');
                ct.setPrimaryType(ct2.substring(0, pos));
                ct.setSubType(ct2.substring(pos + 1));
            }
        }

        // Create MIME body part and set its content
        MimeBodyPart messageBodyPart = new MimeBodyPart();
        messageBodyPart.setDataHandler(new DataHandler(new AttachmentDataSource(attachment)));

        if (fileName != null && !ct.containsNameParameter()) {
            ct.setNameParameter(fileName);
        }
        messageBodyPart.setHeader(MessageHeaders.HDR_CONTENT_TYPE, MimeMessageUtility.foldContentType(ct.toString()));

        if (INLINE != attachment.getContentDisposition()) {
            // Force base64 encoding to keep data as it is
            messageBodyPart.setHeader(MessageHeaders.HDR_CONTENT_TRANSFER_ENC, "base64");
        }

        // Disposition
        String disposition = messageBodyPart.getHeader(MessageHeaders.HDR_CONTENT_DISPOSITION, null);
        ContentDisposition cd;
        if (disposition == null) {
            cd = new ContentDisposition(attachment.getContentDisposition().getId());
        } else {
            cd = new ContentDisposition(disposition);
            cd.setDisposition(attachment.getContentDisposition().getId());
        }
        if (fileName != null && !cd.containsFilenameParameter()) {
            cd.setFilenameParameter(fileName);
        }
        messageBodyPart.setHeader(MessageHeaders.HDR_CONTENT_DISPOSITION, MimeMessageUtility.foldContentDisposition(cd.toString()));

        // Content-ID
        String contentId = attachment.getContentId();
        if (contentId != null) {
            if (contentId.charAt(0) == '<') {
                messageBodyPart.setContentID(contentId);
            } else {
                messageBodyPart.setContentID(new StringBuilder(contentId.length() + 2).append('<').append(contentId).append('>').toString());
            }
        }

        // vCard
        if (AttachmentOrigin.VCARD == attachment.getOrigin()) {
            messageBodyPart.setHeader(MessageHeaders.HDR_X_OX_VCARD, new StringBuilder(16).append(session.getUserId()).append('@').append(session.getContextId()).toString());
        }

        // Add to parental multipart
        mp.addBodyPart(messageBodyPart);
    }

    private void addNestedMessage(Attachment attachment, Multipart mp) throws MessagingException, OXException {
        String fn;
        if (null == attachment.getName()) {
            InputStream data = attachment.getData();
            try {
                String subject = MimeMessageUtility.checkNonAscii(new InternetHeaders(data).getHeader(MessageHeaders.HDR_SUBJECT, null));
                if (null == subject || subject.length() == 0) {
                    fn = "part.eml";
                } else {
                    subject = MimeMessageUtility.decodeMultiEncodedHeader(MimeMessageUtility.unfold(subject));
                    fn = subject.replaceAll("\\p{Blank}+", "_") + ".eml";
                }
            } finally {
                Streams.close(data);
            }
        } else {
            fn = attachment.getName();
        }

        //Create MIME body part and set its content
        MimeBodyPart origMsgPart = new MimeBodyPart();
        origMsgPart.setDataHandler(new DataHandler(new AttachmentDataSource(attachment, MimeTypes.MIME_MESSAGE_RFC822)));

        // Content-Type
        ContentType ct = new ContentType(MimeTypes.MIME_MESSAGE_RFC822);
        if (null != fn) {
            ct.setNameParameter(fn);
        }
        origMsgPart.setHeader(MessageHeaders.HDR_CONTENT_TYPE, MimeMessageUtility.foldContentType(ct.toString()));

        // Content-Disposition
        String disposition = origMsgPart.getHeader(MessageHeaders.HDR_CONTENT_DISPOSITION, null);
        final ContentDisposition cd;
        if (disposition == null) {
            cd = new ContentDisposition(attachment.getContentDisposition().getId());
        } else {
            cd = new ContentDisposition(disposition);
            cd.setDisposition(attachment.getContentDisposition().getId());
        }
        if (null != fn && !cd.containsFilenameParameter()) {
            cd.setFilenameParameter(fn);
        }
        origMsgPart.setHeader(MessageHeaders.HDR_CONTENT_DISPOSITION, MimeMessageUtility.foldContentDisposition(cd.toString()));

        // Add to parental multipart
        mp.addBodyPart(origMsgPart);
    }

    private static final String HTML_SPACE = "&#160;";

    /**
     * Creates a body part of type <code>text/html</code> from given HTML content
     *
     * @param wellFormedHTMLContent The well-formed HTML content
     * @param charset The charset
     * @return A body part of type <code>text/html</code> from given HTML content
     * @throws MessagingException If a messaging error occurs
     * @throws OXException If a processing error occurs
     */
    private BodyPart createHtmlBodyPart(final String wellFormedHTMLContent, final String charset) throws MessagingException, OXException {
        try {
            final String contentType = new StringBuilder("text/html; charset=").append(charset).toString();
            final MimeBodyPart html = new MimeBodyPart();
            if (Strings.isEmpty(wellFormedHTMLContent)) {
                HtmlService htmlService = services.getService(HtmlService.class);
                String htmlContent = htmlService.getConformHTML(HTML_SPACE, charset).replace(HTML_SPACE, "");
                html.setDataHandler(new DataHandler(new MessageDataSource(htmlContent, contentType)));
            } else {
                html.setDataHandler(new DataHandler(new MessageDataSource(wellFormedHTMLContent, contentType)));
            }
            html.setHeader(MessageHeaders.HDR_MIME_VERSION, "1.0");
            html.setHeader(MessageHeaders.HDR_CONTENT_TYPE, contentType);
            return html;
        } catch (final UnsupportedEncodingException e) {
            throw new MessagingException("Unsupported encoding.", e);
        }
    }

    /**
     * Creates a body part of type <code>text/plain</code> for given content
     *
     * @param content The content
     * @param charset The character encoding
     * @param isHtml Whether provided content is HTML or not
     * @return A body part of type <code>text/plain</code>
     * @throws MessagingException If a messaging error occurs
     */
    private BodyPart createTextBodyPart(String content, String charset, boolean isHtml) throws MessagingException {
        /*
         * Convert HTML content to regular text. First: Create a body part for text content
         */
        MimeBodyPart text = new MimeBodyPart();
        /*
         * Define text content
         */
        String textContent;
        {
            if (content == null || content.length() == 0) {
                textContent = "";
            } else if (isHtml) {
                HtmlService htmlService = services.getService(HtmlService.class);
                textContent = performLineFolding(htmlService.html2text(content, false), 0);
            } else {
                textContent = performLineFolding(content, 0);
            }
        }
        MessageUtility.setText(textContent, charset, text);
        text.setHeader(MessageHeaders.HDR_MIME_VERSION, "1.0");
        text.setHeader(MessageHeaders.HDR_CONTENT_TYPE, new StringBuilder("text/plain; charset=").append(charset).toString());
        if (CharMatcher.ascii().matchesAllOf(textContent)) {
            text.setHeader(MessageHeaders.HDR_CONTENT_TRANSFER_ENC, "7bit");
        }
        return text;
    }

    private static String extensionFor(String fileName) {
        if (null == fileName) {
            return null;
        }

        int pos = fileName.lastIndexOf('.');
        return Strings.asciiLowerCase(pos > 0 ? fileName.substring(pos + 1) : fileName);
    }

    private static final String PREFIX_FWD = "Fwd: ";
    private static final String PREFIX_RE = "Re: ";

    @Override
    public UUID openCompositionSpace(OpenCompositionSpaceParameters parameters, Session session) throws OXException {
        UUID uuid = null;

        Context context = getContext(session);
        UserSettingMail usm = parameters.getMailSettings();

        AttachmentStorage attachmentStorage = null;
        List<Attachment> attachments = null;
        try {
            Type type = parameters.getType();
            if (null == type) {
                type = Type.NEW;
            }

            if (parameters.isAppendOriginalAttachments() && (Type.REPLY != type && Type.REPLY_ALL != type)) {
                throw CompositionSpaceErrorCode.NO_REPLY_FOR.create();
            }

            // Generate composition space identifier
            uuid = UUID.randomUUID();

            // Compile message (draft) for the new composition space
            MessageDescription message = new MessageDescription();

            // Check for priority
            {
                Priority priority = parameters.getPriority();
                if (null != priority) {
                    message.setPriority(priority);
                }
            }

            // Check for Content-Type
            {
                com.openexchange.mail.compose.Message.ContentType contentType = parameters.getContentType();
                if (null != contentType) {
                    message.setContentType(contentType);
                }
            }

            // Check if a read receipt should be requested
            if (parameters.isRequestReadReceipt()) {
                message.setRequestReadReceipt(true);
            }

            // Determine the meta information for the message (draft)
            if (Type.NEW == type) {
                message.setMeta(Meta.META_NEW);
            } else {
                MailServletInterface mailInterface = null;
                try {
                    Meta.Builder metaBuilder = Meta.builder();
                    metaBuilder.withType(Meta.MetaType.typeFor(type));

                    MailMessage originalMail = null;
                    switch (type) {
                        case FORWARD: {
                            List<MailPath> forwardsFor = parameters.getReferencedMails();
                            metaBuilder.withForwardsFor(forwardsFor);
                            mailInterface = MailServletInterface.getInstance(session);
                            originalMail = requireMailMessage(forwardsFor.get(0), mailInterface);
                            metaBuilder.withDate(originalMail.getSentDate());

                            // Forward, pre-set subject
                            String origSubject = originalMail.getSubject();
                            if (Strings.isEmpty(origSubject)) {
                                message.setSubject(PREFIX_FWD);
                            } else {
                                if (origSubject.regionMatches(true, 0, PREFIX_FWD, 0, PREFIX_FWD.length())) {
                                    message.setSubject(origSubject);
                                } else {
                                    message.setSubject(new StringBuilder(PREFIX_FWD.length() + origSubject.length()).append(PREFIX_FWD).append(origSubject).toString());
                                }
                            }

                            // Determine "From"
                            {
                                FromAddressProvider fromAddressProvider = FromAddressProvider.byAccountId();
                                boolean fromSet = false;
                                if (null != fromAddressProvider) {
                                    if (fromAddressProvider.isDetectBy()) {
                                        InternetAddress from = MimeProcessingUtility.determinePossibleFrom(true, originalMail, forwardsFor.get(0).getAccountId(), session, context);
                                        if (null != from) {
                                            message.setFrom(toAddress(from));
                                            fromSet = true;
                                        }
                                    } else if (fromAddressProvider.isSpecified()) {
                                        InternetAddress from = fromAddressProvider.getFromAddress();
                                        if (null != from) {
                                            message.setFrom(toAddress(from));
                                            fromSet = true;
                                        }
                                    }
                                }
                                if (!fromSet) {
                                    String sendAddr = usm.getSendAddr();
                                    if (sendAddr != null) {
                                        message.setFrom(toAddress(new QuotedInternetAddress(sendAddr, false)));
                                    }
                                }
                            }

                            if (forwardsFor.size() == 1) {
                                String owner = MimeProcessingUtility.getFolderOwnerIfShared(forwardsFor.get(0).getFolder(), forwardsFor.get(0).getAccountId(), session);
                                if (null != owner) {
                                    final User[] users = UserStorage.getInstance().searchUserByMailLogin(owner, context);
                                    if (null != users && users.length > 0) {
                                        InternetAddress onBehalfOf = new QuotedInternetAddress(users[0].getMail(), false);
                                        message.setFrom(toAddress(onBehalfOf));
                                        QuotedInternetAddress sender = new QuotedInternetAddress(usm.getSendAddr(), false);
                                        message.setSender(toAddress(sender));
                                    }
                                }
                            }

                            if (usm.isForwardAsAttachment() || forwardsFor.size() > 1) {
                                metaBuilder.withType(Meta.MetaType.FORWARD_ATTACHMENT);
                                // Forward as attachment, add mail(s) as attachment(s)

                                // Obtain attachment storage (can only be null here)
                                attachmentStorage = getAttachmentStorage(session);

                                // Add each mail
                                attachments = new ArrayList<>(forwardsFor.size());
                                int i = 0;
                                for (MailPath forwardFor : forwardsFor) {
                                    ThresholdFileHolder sink = new ThresholdFileHolder();
                                    try {
                                        MailMessage forwardedMail = i == 0 ? originalMail : requireMailMessage(forwardFor, mailInterface);
                                        forwardedMail.writeTo(sink.asOutputStream());

                                        // Compile attachment
                                        AttachmentDescription attachment = new AttachmentDescription();
                                        attachment.setCompositionSpaceId(uuid);
                                        attachment.setContentDisposition(ATTACHMENT);
                                        attachment.setMimeType(MimeTypes.MIME_MESSAGE_RFC822);
                                        String fwdSubject = forwardedMail.getSubject();
                                        attachment.setName((Strings.isEmpty(fwdSubject) ? "mail" + Integer.toString(i + 1) : fwdSubject.replaceAll("\\p{Blank}+", "_")) + ".eml");
                                        attachment.setSize(sink.getLength());
                                        attachment.setOrigin(AttachmentOrigin.MAIL);
                                        Attachment emlAttachment = saveAttachment(sink.getStream(), attachment, session, attachmentStorage);
                                        attachments.add(emlAttachment);
                                    } finally {
                                        Streams.close(sink);
                                    }
                                    i++;
                                }
                            } else {
                                metaBuilder.withType(Meta.MetaType.FORWARD_INLINE);
                                // Forward inline

                                // Fix possible "application/smil" parts
                                MailMessage forwardedMail;
                                {
                                    ContentType contentType = originalMail.getContentType();
                                    if (contentType.startsWith("multipart/related") && ("application/smil".equals(contentType.getParameter(com.openexchange.java.Strings.toLowerCase("type"))))) {
                                        forwardedMail = MimeSmilFixer.getInstance().process(originalMail);
                                    } else {
                                        forwardedMail = originalMail;
                                    }
                                }

                                // Grab first seen text from original message and check for possible referenced inline images
                                boolean multipart = forwardedMail.getContentType().startsWith("multipart/");
                                List<String> contentIds = multipart ? new ArrayList<String>() : null;
                                {
                                    TextAndContentType textForForward = MimeProcessingUtility.getTextForForward(originalMail, usm.isDisplayHtmlInlineContent(), false, contentIds, session);
                                    if (null == textForForward) {
                                        message.setContent("");
                                        message.setContentType(usm.isDisplayHtmlInlineContent() ? TEXT_HTML : TEXT_PLAIN);
                                    } else {
                                        message.setContent(textForForward.getText());
                                        message.setContentType(textForForward.isHtml() ? TEXT_HTML : TEXT_PLAIN);
                                    }
                                }

                                // Check if original mail may contain attachments
                                if (multipart) {
                                    // Add mail's non-inline parts
                                    {
                                        NonInlineForwardPartHandler handler = new NonInlineForwardPartHandler();
                                        if (null != contentIds && !contentIds.isEmpty()) {
                                            handler.setImageContentIds(contentIds);
                                        }
                                        new MailMessageParser().setInlineDetectorBehavior(true).parseMailMessage(forwardedMail, handler);
                                        List<MailPart> nonInlineParts = handler.getNonInlineParts();
                                        if (null != nonInlineParts && !nonInlineParts.isEmpty()) {
                                            attachmentStorage = getAttachmentStorage(session);
                                            attachments = new ArrayList<>(nonInlineParts.size());
                                            int i = 0;
                                            for (MailPart mailPart : nonInlineParts) {
                                                // Compile & store attachment
                                                AttachmentDescription attachment = new AttachmentDescription();
                                                attachment.setCompositionSpaceId(uuid);
                                                attachment.setContentDisposition(ATTACHMENT);
                                                attachment.setMimeType(mailPart.getContentType().toString(true));
                                                String fileName = mailPart.getFileName();
                                                attachment.setName(Strings.isEmpty(fileName) ? MailMessageParser.generateFilename(Integer.toString(i + 1), mailPart.getContentType().getBaseType()) : fileName);
                                                attachment.setOrigin(hasVCardMarker(mailPart, session) ? AttachmentOrigin.VCARD : AttachmentOrigin.MAIL);
                                                Attachment partAttachment = saveAttachment(mailPart.getInputStream(), attachment, session, attachmentStorage);
                                                attachments.add(partAttachment);
                                                i++;
                                            }
                                        }
                                    }

                                    // Add mail's inline images
                                    if (TEXT_HTML == message.getContentType() && null != contentIds && !contentIds.isEmpty()) {
                                        InlineContentHandler inlineHandler = new InlineContentHandler(contentIds);
                                        new MailMessageParser().setInlineDetectorBehavior(true).parseMailMessage(originalMail, inlineHandler);
                                        Map<String, MailPart> inlineParts = inlineHandler.getInlineContents();
                                        if (null != inlineParts && !inlineParts.isEmpty()) {
                                            if (null == attachmentStorage) {
                                                attachmentStorage = getAttachmentStorage(session);
                                            }
                                            if (null == attachments) {
                                                attachments = new ArrayList<>(inlineParts.size());
                                            }

                                            Map<String, Attachment> inlineAttachments = new HashMap<String, Attachment>(inlineParts.size());
                                            int i = 0;
                                            for (Map.Entry<String, MailPart> inlineEntry : inlineParts.entrySet()) {
                                                // Compile & store attachment
                                                MailPart mailPart = inlineEntry.getValue();
                                                AttachmentDescription attachment = new AttachmentDescription();
                                                attachment.setCompositionSpaceId(uuid);
                                                attachment.setContentDisposition(INLINE);
                                                attachment.setContentId(inlineEntry.getKey());
                                                attachment.setMimeType(mailPart.getContentType().toString(true));
                                                String fileName = mailPart.getFileName();
                                                attachment.setName(Strings.isEmpty(fileName) ? MailMessageParser.generateFilename(Integer.toString(i + 1), mailPart.getContentType().getBaseType()) : fileName);
                                                attachment.setOrigin(AttachmentOrigin.MAIL);
                                                Attachment partAttachment = saveAttachment(mailPart.getInputStream(), attachment, session, attachmentStorage);
                                                attachments.add(partAttachment);
                                                inlineAttachments.put(inlineEntry.getKey(), partAttachment);
                                                i++;
                                            }

                                            message.setContent(replaceCidInlineImages(message.getContent(), inlineAttachments, session));
                                        }
                                    }
                                }
                            }
                        }
                            break;
                        case REPLY:
                            // fall-through
                        case REPLY_ALL: {
                            MailPath replyFor = parameters.getReferencedMails().get(0);
                            metaBuilder.withReplyFor(replyFor);
                            mailInterface = MailServletInterface.getInstance(session);
                            originalMail = requireMailMessage(replyFor, mailInterface);
                            metaBuilder.withDate(originalMail.getSentDate());

                            int accountId = replyFor.getAccountId();
                            boolean replyAll = (type == Type.REPLY_ALL);
                            boolean preferToAsRecipient = false;
                            {
                                String originalMailFolder = replyFor.getFolder();
                                String[] arr = MailSessionCache.getInstance(session).getParameter(accountId, MailSessionParameterNames.getParamDefaultFolderArray());
                                if (arr == null) {
                                    MailAccess<?, ?> mailAccess = mailInterface.getMailAccess();
                                    IMailFolderStorage folderStorage = mailAccess.getFolderStorage();
                                    preferToAsRecipient = originalMailFolder.equals(folderStorage.getSentFolder()) || originalMailFolder.equals(folderStorage.getDraftsFolder());
                                } else {
                                    preferToAsRecipient = originalMailFolder.equals(arr[StorageUtility.INDEX_SENT]) || originalMailFolder.equals(arr[StorageUtility.INDEX_DRAFTS]);
                                }
                            }

                            // Reply, pre-set subject
                            String origSubject = originalMail.getSubject();
                            if (Strings.isEmpty(origSubject)) {
                                message.setSubject(PREFIX_RE);
                            } else {
                                if (origSubject.regionMatches(true, 0, PREFIX_RE, 0, PREFIX_RE.length())) {
                                    message.setSubject(origSubject);
                                } else {
                                    message.setSubject(new StringBuilder(PREFIX_RE.length() + origSubject.length()).append(PREFIX_RE).append(origSubject).toString());
                                }
                            }

                            // Determine "From"
                            InternetAddress from = null;
                            {
                                FromAddressProvider fromAddressProvider = FromAddressProvider.byAccountId();
                                if (null != fromAddressProvider) {
                                    if (fromAddressProvider.isDetectBy()) {
                                        from = MimeProcessingUtility.determinePossibleFrom(false, originalMail, accountId, session, context);
                                        /*
                                         * Set if a "From" candidate applies
                                         */
                                        if (null != from) {
                                            message.setFrom(toAddress(from));
                                        }
                                    } else if (fromAddressProvider.isSpecified()) {
                                        from = fromAddressProvider.getFromAddress();
                                        if (null != from) {
                                            message.setFrom(toAddress(from));
                                        }
                                    }
                                }
                            }

                            /*
                             * Set the appropriate recipients. Taken from RFC 822 section 4.4.4: If the "Reply-To" field exists, then the reply should go to
                             * the addresses indicated in that field and not to the address(es) indicated in the "From" field.
                             */
                            InternetAddress[] recipientAddrs;
                            if (preferToAsRecipient) {
                                recipientAddrs = originalMail.getTo();
                            } else {
                                Set<InternetAddress> tmpSet = new LinkedHashSet<InternetAddress>(4);
                                boolean fromAdded;
                                {
                                    String[] replyTo = originalMail.getHeader(MessageHeaders.HDR_REPLY_TO);
                                    if (MimeMessageUtility.isEmptyHeader(replyTo)) {
                                        String owner = MimeProcessingUtility.getFolderOwnerIfShared(replyFor.getFolder(), replyFor.getAccountId(), session);
                                        if (null != owner) {
                                            final User[] users = UserStorage.getInstance().searchUserByMailLogin(owner, context);
                                            if (null != users && users.length > 0) {
                                                InternetAddress onBehalfOf = new QuotedInternetAddress(users[0].getMail(), false);
                                                message.setFrom(toAddress(onBehalfOf));
                                                QuotedInternetAddress sender = new QuotedInternetAddress(usm.getSendAddr(), false);
                                                message.setSender(toAddress(sender));
                                            }
                                        }
                                        /*
                                         * Set from as recipient
                                         */
                                        tmpSet.addAll(Arrays.asList(originalMail.getFrom()));
                                        fromAdded = true;
                                    } else {
                                        /*
                                         * Message holds header 'Reply-To'
                                         */
                                        tmpSet.addAll(Arrays.asList(MimeMessageUtility.getAddressHeader(unfold(replyTo[0]))));
                                        fromAdded = false;
                                    }
                                }
                                if (replyAll) {
                                    /*-
                                     * Check 'From' has been added
                                     */
                                    if (!fromAdded) {
                                        tmpSet.addAll(Arrays.asList(originalMail.getFrom()));
                                    }
                                }
                                recipientAddrs = tmpSet.toArray(new InternetAddress[tmpSet.size()]);
                            }

                            if (replyAll) {
                                /*
                                 * Create a filter which is used to sort out addresses before adding them to either field 'To' or 'Cc'
                                 */
                                Set<InternetAddress> filter = new HashSet<InternetAddress>();
                                if (null != from) {
                                    filter.add(from);
                                }
                                /*
                                 * Add user's address to filter
                                 */
                                if (accountId == MailAccount.DEFAULT_ID) {
                                    MimeProcessingUtility.addUserAliases(filter, session, context);
                                } else {
                                    // Check for Unified Mail account
                                    UnifiedInboxManagement management = services.getService(UnifiedInboxManagement.class);
                                    if ((null != management) && (accountId == management.getUnifiedINBOXAccountID(session))) {
                                        int realAccountId;
                                        try {
                                            UnifiedInboxUID uid = new UnifiedInboxUID(originalMail.getMailId());
                                            realAccountId = uid.getAccountId();
                                        } catch (OXException e) {
                                            // No Unified Mail identifier
                                            LoggerHolder.LOG.trace("", e);
                                            FullnameArgument fa = UnifiedInboxUID.parsePossibleNestedFullName(originalMail.getFolder());
                                            realAccountId = null == fa ? MailAccount.DEFAULT_ID : fa.getAccountId();
                                        }

                                        if (realAccountId == MailAccount.DEFAULT_ID) {
                                            MimeProcessingUtility.addUserAliases(filter, session, context);
                                        } else {
                                            MailAccountStorageService mass = services.getService(MailAccountStorageService.class);
                                            if (null == mass) {
                                                MimeProcessingUtility.addUserAliases(filter, session, context);
                                            } else {
                                                filter.add(new QuotedInternetAddress(mass.getMailAccount(realAccountId, session.getUserId(), session.getContextId()).getPrimaryAddress(), false));
                                            }
                                        }
                                    } else {
                                        MailAccountStorageService mass = services.getService(MailAccountStorageService.class);
                                        if (null == mass) {
                                            MimeProcessingUtility.addUserAliases(filter, session, context);
                                        } else {
                                            filter.add(new QuotedInternetAddress(mass.getMailAccount(accountId, session.getUserId(), session.getContextId()).getPrimaryAddress(), false));
                                        }
                                    }
                                }
                                /*
                                 * Determine if other original recipients should be added to 'Cc'.
                                 */
                                final boolean replyallcc = usm.isReplyAllCc();
                                /*
                                 * Filter the recipients of 'Reply-To'/'From' field
                                 */
                                final Set<InternetAddress> filteredAddrs = filter(filter, recipientAddrs);
                                /*
                                 * Add filtered recipients from 'To' field
                                 */
                                String hdrVal = originalMail.getHeader(MessageHeaders.HDR_TO, MessageHeaders.HDR_ADDR_DELIM);
                                InternetAddress[] toAddrs = null;
                                if (hdrVal != null) {
                                    filteredAddrs.addAll(filter(filter, (toAddrs = parseAddressList(hdrVal, true))));
                                }
                                /*
                                 * ... and add filtered addresses to either 'To' or 'Cc' field
                                 */
                                if (!filteredAddrs.isEmpty()) {
                                    if (replyallcc) {
                                        // Put original sender into 'To'
                                        message.setTo(toAddresses(recipientAddrs));
                                        // All other into 'Cc'
                                        filteredAddrs.removeAll(Arrays.asList(recipientAddrs));
                                        message.setCc(toAddresses(filteredAddrs.toArray(new InternetAddress[filteredAddrs.size()])));
                                    } else {
                                        message.setTo(toAddresses(filteredAddrs.toArray(new InternetAddress[filteredAddrs.size()])));
                                    }
                                } else if (toAddrs != null) {
                                    final Set<InternetAddress> tmpSet = new HashSet<InternetAddress>(Arrays.asList(recipientAddrs));
                                    tmpSet.removeAll(Arrays.asList(toAddrs));
                                    if (tmpSet.isEmpty()) {
                                        /*
                                         * The message was sent from the user to himself. In this special case allow user's own address in field 'To' to
                                         * avoid an empty 'To' field
                                         */
                                        message.setTo(toAddresses(recipientAddrs));
                                    }
                                }
                                /*
                                 * Filter recipients from 'Cc' field
                                 */
                                filteredAddrs.clear();
                                hdrVal = originalMail.getHeader(MessageHeaders.HDR_CC, MessageHeaders.HDR_ADDR_DELIM);
                                if (hdrVal != null) {
                                    filteredAddrs.addAll(filter(filter, parseAddressList(unfold(hdrVal), true)));
                                }
                                if (!filteredAddrs.isEmpty()) {
                                    message.setCc(toAddresses(filteredAddrs.toArray(new InternetAddress[filteredAddrs.size()])));
                                }
                                /*
                                 * Filter recipients from 'Bcc' field
                                 */
                                filteredAddrs.clear();
                                hdrVal = originalMail.getHeader(MessageHeaders.HDR_BCC, MessageHeaders.HDR_ADDR_DELIM);
                                if (hdrVal != null) {
                                    filteredAddrs.addAll(filter(filter, parseAddressList(unfold(hdrVal), true)));
                                }
                                if (!filteredAddrs.isEmpty()) {
                                    message.setBcc(toAddresses(filteredAddrs.toArray(new InternetAddress[filteredAddrs.size()])));
                                }
                            } else {
                                /*
                                 * Plain reply: Just put original sender into 'To' field
                                 */
                                message.setTo(toAddresses(recipientAddrs));
                            }

                            // Check whether to attach original message
                            if (usm.getAttachOriginalMessage() > 0) {
                                // Obtain attachment storage (can only be null here)
                                attachmentStorage = getAttachmentStorage(session);

                                ThresholdFileHolder sink = new ThresholdFileHolder();
                                try {
                                    originalMail.writeTo(sink.asOutputStream());

                                    // Compile attachment
                                    AttachmentDescription attachment = new AttachmentDescription();
                                    attachment.setCompositionSpaceId(uuid);
                                    attachment.setContentDisposition(ATTACHMENT);
                                    attachment.setMimeType(MimeTypes.MIME_MESSAGE_RFC822);
                                    attachment.setName((Strings.isEmpty(origSubject) ? "mail" : origSubject.replaceAll("\\p{Blank}+", "_")) + ".eml");
                                    attachment.setSize(sink.getLength());
                                    attachment.setOrigin(AttachmentOrigin.MAIL);
                                    Attachment emlAttachment = saveAttachment(sink.getStream(), attachment, session, attachmentStorage);
                                    attachments = new ArrayList<>(1);
                                    attachments.add(emlAttachment);
                                } finally {
                                    Streams.close(sink);
                                }
                            }

                            {
                                TextAndContentType textForReply = usm.isIgnoreOriginalMailTextOnReply() ? null : MimeProcessingUtility.getTextForReply(originalMail, usm.isDisplayHtmlInlineContent(), false, session);
                                if (null == textForReply) {
                                    message.setContent("");
                                    message.setContentType(usm.isDisplayHtmlInlineContent() ? TEXT_HTML : TEXT_PLAIN);
                                } else {
                                    message.setContent(textForReply.getText());
                                    message.setContentType(textForReply.isHtml() ? TEXT_HTML : TEXT_PLAIN);
                                }
                            }

                            // Add mail's inline images
                            List<String> contentIds = new ArrayList<String>();
                            if (TEXT_HTML == message.getContentType()) {
                                MimeProcessingUtility.getTextForForward(originalMail, true, false, contentIds, session);

                                if (!contentIds.isEmpty()) {
                                    InlineContentHandler inlineHandler = new InlineContentHandler(contentIds);
                                    new MailMessageParser().setInlineDetectorBehavior(true).parseMailMessage(originalMail, inlineHandler);
                                    Map<String, MailPart> inlineParts = inlineHandler.getInlineContents();
                                    if (null != inlineParts && !inlineParts.isEmpty()) {
                                        if (null == attachmentStorage) {
                                            attachmentStorage = getAttachmentStorage(session);
                                        }
                                        if (null == attachments) {
                                            attachments = new ArrayList<>(inlineParts.size());
                                        }

                                        Map<String, Attachment> inlineAttachments = new HashMap<String, Attachment>(inlineParts.size());
                                        int i = 0;
                                        for (Map.Entry<String, MailPart> inlineEntry : inlineParts.entrySet()) {
                                            MailPart mailPart = inlineEntry.getValue();
                                            // Compile & store attachment
                                            AttachmentDescription attachment = new AttachmentDescription();
                                            attachment.setCompositionSpaceId(uuid);
                                            attachment.setContentDisposition(INLINE);
                                            attachment.setContentId(inlineEntry.getKey());
                                            attachment.setMimeType(mailPart.getContentType().toString(true));
                                            String fileName = mailPart.getFileName();
                                            attachment.setName(Strings.isEmpty(fileName) ? MailMessageParser.generateFilename(Integer.toString(i + 1), mailPart.getContentType().getBaseType()) : fileName);
                                            attachment.setOrigin(AttachmentOrigin.MAIL);
                                            Attachment partAttachment = saveAttachment(mailPart.getInputStream(), attachment, session, attachmentStorage);
                                            attachments.add(partAttachment);

                                            inlineAttachments.put(inlineEntry.getKey(), partAttachment);
                                            i++;
                                        }

                                        message.setContent(replaceCidInlineImages(message.getContent(), inlineAttachments, session));
                                    }
                                }
                            }

                            if (parameters.isAppendOriginalAttachments()) {
                                // Add mail's non-inline parts
                                NonInlineForwardPartHandler handler = new NonInlineForwardPartHandler();
                                if (false == contentIds.isEmpty()) {
                                    handler.setImageContentIds(contentIds);
                                }
                                new MailMessageParser().setInlineDetectorBehavior(true).parseMailMessage(originalMail, handler);
                                List<MailPart> nonInlineParts = handler.getNonInlineParts();
                                if (null != nonInlineParts && !nonInlineParts.isEmpty()) {
                                    // Obtain attachment storage
                                    if (null == attachmentStorage) {
                                        attachmentStorage = getAttachmentStorage(session);
                                    }
                                    if (null == attachments) {
                                        attachments = new ArrayList<>(nonInlineParts.size());
                                    }

                                    int i = attachments.size();
                                    for (MailPart mailPart : nonInlineParts) {
                                        // Compile & store attachment
                                        AttachmentDescription attachment = new AttachmentDescription();
                                        attachment.setCompositionSpaceId(uuid);
                                        attachment.setContentDisposition(ATTACHMENT);
                                        attachment.setMimeType(mailPart.getContentType().toString(true));
                                        String fileName = mailPart.getFileName();
                                        attachment.setName(Strings.isEmpty(fileName) ? MailMessageParser.generateFilename(Integer.toString(i + 1), mailPart.getContentType().getBaseType()) : fileName);
                                        attachment.setOrigin(hasVCardMarker(mailPart, session) ? AttachmentOrigin.VCARD : AttachmentOrigin.MAIL);
                                        Attachment partAttachment = saveAttachment(mailPart.getInputStream(), attachment, session, attachmentStorage);
                                        attachments.add(partAttachment);
                                        i++;
                                    }
                                }
                            }
                        }
                            break;
                        case EDIT:
                            // fall-through
                        case COPY: {
                            MailPath editFor = parameters.getReferencedMails().get(0);
                            if (type == Type.EDIT) {
                                metaBuilder.withEditFor(editFor);
                            }
                            mailInterface = MailServletInterface.getInstance(session);
                            originalMail = requireMailMessage(editFor, mailInterface);
                            metaBuilder.withDate(originalMail.getSentDate());

                            // Restore meta, security, and shared attachments info from draft message
                            {
                                String headerValue = decodeHeaderValue(originalMail.getFirstHeader(HEADER_X_OX_META));
                                Meta parsedMeta = headerValue2Meta(headerValue);
                                metaBuilder.applyFromDraft(parsedMeta);

                                headerValue = decodeHeaderValue(originalMail.getFirstHeader(HEADER_X_OX_SECURITY));
                                Security parsedSecurity = headerValue2Security(headerValue);
                                message.setSecurity(parsedSecurity);

                                headerValue = decodeHeaderValue(originalMail.getFirstHeader(HEADER_X_OX_SHARED_ATTACHMENTS));
                                SharedAttachmentsInfo parsedSharedAttachments = headerValue2SharedAttachments(headerValue);
                                message.setsharedAttachmentsInfo(parsedSharedAttachments);

                                headerValue = decodeHeaderValue(originalMail.getFirstHeader(HEADER_X_OX_READ_RECEIPT));
                                if ("true".equalsIgnoreCase(headerValue)) {
                                    message.setRequestReadReceipt(true);
                                }
                            }

                            // Pre-set subject
                            String origSubject = originalMail.getSubject();
                            if (Strings.isNotEmpty(origSubject)) {
                                message.setSubject(origSubject);
                            }

                            // Set "From"
                            {
                                InternetAddress[] from = originalMail.getFrom();
                                if (null != from && from.length > 0) {
                                    message.setFrom(toAddress(from[0]));
                                }
                            }

                            // Pre-set recipients
                            {
                                InternetAddress[] recipients = originalMail.getTo();
                                if (null != recipients && recipients.length > 0) {
                                    message.setTo(toAddresses(recipients));
                                }
                                recipients = originalMail.getCc();
                                if (null != recipients && recipients.length > 0) {
                                    message.setCc(toAddresses(recipients));
                                }
                                recipients = originalMail.getBcc();
                                if (null != recipients && recipients.length > 0) {
                                    message.setBcc(toAddresses(recipients));
                                }
                            }

                            // Grab first seen text from original message and check for possible referenced inline images
                            boolean multipart = originalMail.getContentType().startsWith("multipart/");
                            List<String> contentIds = multipart ? new ArrayList<String>() : null;
                            {
                                TextAndContentType textForForward = MimeProcessingUtility.getTextForForward(originalMail, usm.isDisplayHtmlInlineContent(), false, contentIds, session);
                                if (null == textForForward) {
                                    message.setContent("");
                                    message.setContentType(usm.isDisplayHtmlInlineContent() ? TEXT_HTML : TEXT_PLAIN);
                                } else {
                                    message.setContent(textForForward.getText());
                                    message.setContentType(textForForward.isHtml() ? TEXT_HTML : TEXT_PLAIN);
                                }
                            }

                            // Check if original mail may contain attachments
                            if (multipart) {
                                // Add mail's non-inline parts
                                {
                                    NonInlineForwardPartHandler handler = new NonInlineForwardPartHandler();
                                    if (null != contentIds && !contentIds.isEmpty()) {
                                        handler.setImageContentIds(contentIds);
                                    }
                                    new MailMessageParser().setInlineDetectorBehavior(true).parseMailMessage(originalMail, handler);
                                    List<MailPart> nonInlineParts = handler.getNonInlineParts();
                                    if (null != nonInlineParts && !nonInlineParts.isEmpty()) {
                                        attachmentStorage = getAttachmentStorage(session);
                                        attachments = new ArrayList<>(nonInlineParts.size());
                                        int i = 0;
                                        for (MailPart mailPart : nonInlineParts) {
                                            // Compile & store attachment
                                            AttachmentDescription attachment = new AttachmentDescription();
                                            attachment.setCompositionSpaceId(uuid);
                                            attachment.setContentDisposition(ATTACHMENT);
                                            attachment.setMimeType(mailPart.getContentType().toString(true));
                                            String fileName = mailPart.getFileName();
                                            attachment.setName(Strings.isEmpty(fileName) ? MailMessageParser.generateFilename(Integer.toString(i + 1), mailPart.getContentType().getBaseType()) : fileName);
                                            attachment.setOrigin(hasVCardMarker(mailPart, session) ? AttachmentOrigin.VCARD : AttachmentOrigin.MAIL);
                                            Attachment partAttachment = saveAttachment(mailPart.getInputStream(), attachment, session, attachmentStorage);
                                            attachments.add(partAttachment);
                                            i++;
                                        }
                                    }
                                }

                                // Add mail's inline images
                                if (TEXT_HTML == message.getContentType() && null != contentIds && !contentIds.isEmpty()) {
                                    InlineContentHandler inlineHandler = new InlineContentHandler(contentIds);
                                    new MailMessageParser().setInlineDetectorBehavior(true).parseMailMessage(originalMail, inlineHandler);
                                    Map<String, MailPart> inlineParts = inlineHandler.getInlineContents();
                                    if (null != inlineParts && !inlineParts.isEmpty()) {
                                        if (null == attachmentStorage) {
                                            attachmentStorage = getAttachmentStorage(session);
                                        }
                                        if (null == attachments) {
                                            attachments = new ArrayList<>(inlineParts.size());
                                        }

                                        Map<String, Attachment> inlineAttachments = new HashMap<String, Attachment>(inlineParts.size());
                                        int i = 0;
                                        for (Map.Entry<String, MailPart> inlineEntry : inlineParts.entrySet()) {
                                            MailPart mailPart = inlineEntry.getValue();
                                            // Compile & store attachment
                                            AttachmentDescription attachment = new AttachmentDescription();
                                            attachment.setCompositionSpaceId(uuid);
                                            attachment.setContentDisposition(INLINE);
                                            attachment.setContentId(inlineEntry.getKey());
                                            attachment.setMimeType(mailPart.getContentType().toString(true));
                                            String fileName = mailPart.getFileName();
                                            attachment.setName(Strings.isEmpty(fileName) ? MailMessageParser.generateFilename(Integer.toString(i + 1), mailPart.getContentType().getBaseType()) : fileName);
                                            attachment.setOrigin(AttachmentOrigin.MAIL);
                                            Attachment partAttachment = saveAttachment(mailPart.getInputStream(), attachment, session, attachmentStorage);
                                            attachments.add(partAttachment);

                                            inlineAttachments.put(inlineEntry.getKey(), partAttachment);
                                            i++;
                                        }

                                        message.setContent(replaceCidInlineImages(message.getContent(), inlineAttachments, session));
                                    }
                                }
                            }
                        }
                            break;
                        case RESEND:{
                            MailPath resendFor = parameters.getReferencedMails().get(0);
                            mailInterface = MailServletInterface.getInstance(session);
                            originalMail = requireMailMessage(resendFor, mailInterface);
                            metaBuilder.withDate(originalMail.getSentDate());

                            // Pre-set subject
                            String origSubject = originalMail.getSubject();
                            if (Strings.isNotEmpty(origSubject)) {
                                message.setSubject(origSubject);
                            }

                            // Determine "From"
                            InternetAddress from = null;
                            {
                                FromAddressProvider fromAddressProvider = FromAddressProvider.byAccountId();
                                if (null != fromAddressProvider) {
                                    if (fromAddressProvider.isDetectBy()) {
                                        from = MimeProcessingUtility.determinePossibleFrom(false, originalMail, resendFor.getAccountId(), session, context);
                                        /*
                                         * Set if a "From" candidate applies
                                         */
                                        if (null != from) {
                                            message.setFrom(toAddress(from));
                                        }
                                    } else if (fromAddressProvider.isSpecified()) {
                                        from = fromAddressProvider.getFromAddress();
                                        if (null != from) {
                                            message.setFrom(toAddress(from));
                                        }
                                    }
                                }
                            }

                            // Pre-set recipients
                            {
                                InternetAddress[] recipients = originalMail.getTo();
                                if (null != recipients && recipients.length > 0) {
                                    message.setTo(toAddresses(recipients));
                                }
                                recipients = originalMail.getCc();
                                if (null != recipients && recipients.length > 0) {
                                    message.setCc(toAddresses(recipients));
                                }
                                recipients = originalMail.getBcc();
                                if (null != recipients && recipients.length > 0) {
                                    message.setBcc(toAddresses(recipients));
                                }
                            }

                            // Grab first seen text from original message and check for possible referenced inline images
                            boolean multipart = originalMail.getContentType().startsWith("multipart/");
                            List<String> contentIds = multipart ? new ArrayList<String>() : null;
                            {
                                TextAndContentType textForForward = MimeProcessingUtility.getTextForForward(originalMail, usm.isDisplayHtmlInlineContent(), false, contentIds, session);
                                if (null == textForForward) {
                                    message.setContent("");
                                    message.setContentType(usm.isDisplayHtmlInlineContent() ? TEXT_HTML : TEXT_PLAIN);
                                } else {
                                    message.setContent(textForForward.getText());
                                    message.setContentType(textForForward.isHtml() ? TEXT_HTML : TEXT_PLAIN);
                                }
                            }

                            // Check if original mail may contain attachments
                            if (multipart) {
                                // Add mail's non-inline parts
                                {
                                    NonInlineForwardPartHandler handler = new NonInlineForwardPartHandler();
                                    if (null != contentIds && !contentIds.isEmpty()) {
                                        handler.setImageContentIds(contentIds);
                                    }
                                    new MailMessageParser().setInlineDetectorBehavior(true).parseMailMessage(originalMail, handler);
                                    List<MailPart> nonInlineParts = handler.getNonInlineParts();
                                    if (null != nonInlineParts && !nonInlineParts.isEmpty()) {
                                        attachmentStorage = getAttachmentStorage(session);
                                        attachments = new ArrayList<>(nonInlineParts.size());
                                        int i = 0;
                                        for (MailPart mailPart : nonInlineParts) {
                                            // Compile & store attachment
                                            AttachmentDescription attachment = new AttachmentDescription();
                                            attachment.setCompositionSpaceId(uuid);
                                            attachment.setContentDisposition(ATTACHMENT);
                                            attachment.setMimeType(mailPart.getContentType().toString(true));
                                            String fileName = mailPart.getFileName();
                                            attachment.setName(Strings.isEmpty(fileName) ? MailMessageParser.generateFilename(Integer.toString(i + 1), mailPart.getContentType().getBaseType()) : fileName);
                                            attachment.setOrigin(hasVCardMarker(mailPart, session) ? AttachmentOrigin.VCARD : AttachmentOrigin.MAIL);
                                            Attachment partAttachment = saveAttachment(mailPart.getInputStream(), attachment, session, attachmentStorage);
                                            attachments.add(partAttachment);
                                            i++;
                                        }
                                    }
                                }

                                // Add mail's inline images
                                if (TEXT_HTML == message.getContentType() && null != contentIds && !contentIds.isEmpty()) {
                                    InlineContentHandler inlineHandler = new InlineContentHandler(contentIds);
                                    new MailMessageParser().setInlineDetectorBehavior(true).parseMailMessage(originalMail, inlineHandler);
                                    Map<String, MailPart> inlineParts = inlineHandler.getInlineContents();
                                    if (null != inlineParts && !inlineParts.isEmpty()) {
                                        if (null == attachmentStorage) {
                                            attachmentStorage = getAttachmentStorage(session);
                                        }
                                        if (null == attachments) {
                                            attachments = new ArrayList<>(inlineParts.size());
                                        }

                                        Map<String, Attachment> inlineAttachments = new HashMap<String, Attachment>(inlineParts.size());
                                        int i = 0;
                                        for (Map.Entry<String, MailPart> inlineEntry : inlineParts.entrySet()) {
                                            MailPart mailPart = inlineEntry.getValue();
                                            // Compile & store attachment
                                            AttachmentDescription attachment = new AttachmentDescription();
                                            attachment.setCompositionSpaceId(uuid);
                                            attachment.setContentDisposition(INLINE);
                                            attachment.setContentId(inlineEntry.getKey());
                                            attachment.setMimeType(mailPart.getContentType().toString(true));
                                            String fileName = mailPart.getFileName();
                                            attachment.setName(Strings.isEmpty(fileName) ? MailMessageParser.generateFilename(Integer.toString(i + 1), mailPart.getContentType().getBaseType()) : fileName);
                                            attachment.setOrigin(AttachmentOrigin.MAIL);
                                            Attachment partAttachment = saveAttachment(mailPart.getInputStream(), attachment, session, attachmentStorage);
                                            attachments.add(partAttachment);

                                            inlineAttachments.put(inlineEntry.getKey(), partAttachment);
                                            i++;
                                        }

                                        message.setContent(replaceCidInlineImages(message.getContent(), inlineAttachments, session));
                                    }
                                }
                            }
                        }
                            break;
                        default:
                            break;
                    }

                    message.setMeta(metaBuilder.build());
                } catch (MessagingException e) {
                    throw MimeMailException.handleMessagingException(e);
                } finally {
                    if (null != mailInterface) {
                        mailInterface.close(true);
                    }
                }
            }

            // Check if vCard of session-associated user is supposed to be attached
            if (parameters.isAppendVCard()) {
                // Obtain attachment storage
                if (null == attachmentStorage) {
                    attachmentStorage = getAttachmentStorage(session);
                }

                // Create VCard
                VCardAndFileName userVCard = getUserVCard(session);
                byte[] vcard = userVCard.getVcard();

                // Compile attachment
                AttachmentDescription attachment = new AttachmentDescription();
                attachment.setCompositionSpaceId(uuid);
                attachment.setContentDisposition(ATTACHMENT);
                attachment.setMimeType(MimeTypes.MIME_TEXT_VCARD + "; charset=\"UTF-8\"");
                attachment.setName(userVCard.getFileName());
                attachment.setSize(vcard.length);
                attachment.setOrigin(AttachmentOrigin.VCARD);
                Attachment vcardAttachment = saveAttachment(Streams.newByteArrayInputStream(vcard), attachment, session, attachmentStorage);
                if (null == attachments) {
                    attachments = new ArrayList<>(1);
                }
                attachments.add(vcardAttachment);
            }

            if (null != attachments) {
                Collections.sort(attachments, AttachmentComparator.getInstance());
                message.setAttachments(attachments);
            }

            UUID compositionSpaceId = getStorageService().openCompositionSpace(session, new CompositionSpaceDescription().setUuid(uuid).setMessage(message));
            attachments = null; // Avoid premature deletion
            return compositionSpaceId;
        } finally {
            if (null != attachments && null != attachmentStorage) {
                for (Attachment deleteMe : attachments) {
                    deleteAttachmentSafe(deleteMe, attachmentStorage, session);
                }
            }
        }
    }

    /**
     * Checks if specified mail part has the vCard marker.
     *
     * @param mailPart The mail part to check
     * @param session The session
     * @return <code>true</code> if vCard marker is present; otherwise <code>false</code>
     */
    private boolean hasVCardMarker(MailPart mailPart, Session session) {
        String userId = new StringBuilder(16).append(session.getUserId()).append('@').append(session.getContextId()).toString();
        return userId.equals(mailPart.getFirstHeader(MessageHeaders.HDR_X_OX_VCARD));
    }

    private Context getContext(Session session) throws OXException {
        return session instanceof ServerSession ? ((ServerSession) session).getContext() : services.getService(ContextService.class).getContext(session.getContextId());
    }

    private void deleteAttachmentSafe(Attachment attachmentToDelete, AttachmentStorage attachmentStorage, Session session) {
        try {
            attachmentStorage.deleteAttachment(attachmentToDelete.getId(), session);
        } catch (Exception e) {
            LoggerHolder.LOG.error("Failed to delete attachment with ID " + getUnformattedString(attachmentToDelete.getId()) + " from storage " + attachmentStorage.getClass().getName(), e);
        }
    }

    /**
     * Filters given address array against given filter set. All addresses currently contained in filter set are removed from specified
     * <code>addrs</code> and all addresses not contained in filter set are added to filter set for future invocations.
     *
     * @param filter The current address filter
     * @param addrs The address list to filter
     * @return The filtered set of addresses
     */
    private static Set<InternetAddress> filter(final Set<InternetAddress> filter, final InternetAddress[] addrs) {
        if (addrs == null) {
            return new HashSet<InternetAddress>(0);
        }
        final Set<InternetAddress> set = new LinkedHashSet<InternetAddress>(Arrays.asList(addrs));
        /*
         * Remove all addresses from set which are contained in filter
         */
        set.removeAll(filter);
        /*
         * Add new addresses to filter
         */
        filter.addAll(set);
        return set;
    }

    @Override
    public Attachment addAttachmentToCompositionSpace(UUID compositionSpaceId, AttachmentDescription attachmentDesc, InputStream data, Session session) throws OXException {
        try {
            CompositionSpace compositionSpace = getCompositionSpace(compositionSpaceId, session);
            // Obtain attachment storage
            AttachmentStorage attachmentStorage = getAttachmentStorage(session);
            Attachment newAttachment = null;
            try {
                attachmentDesc.setCompositionSpaceId(compositionSpaceId);
                newAttachment = saveAttachment(data, attachmentDesc, session, attachmentStorage);

                boolean retry = true;
                int retryCount = 0;
                do {
                    try {
                        // Add new attachments to message
                        List<Attachment> attachments = new ArrayList<Attachment>(compositionSpace.getMessage().getAttachments());
                        attachments.add(newAttachment);
                        Collections.sort(attachments, AttachmentComparator.getInstance());

                        // Add new attachments to composition space
                        MessageDescription md = new MessageDescription();
                        md.setAttachments(attachments);
                        getStorageService().updateCompositionSpace(session, new CompositionSpaceDescription().setUuid(compositionSpaceId).setMessage(md).setLastModifiedDate(new Date(compositionSpace.getLastModified())));
                        retry = false;
                    } catch (OXException e) {
                        if (!CompositionSpaceErrorCode.CONCURRENT_UPDATE.equals(e)) {
                            throw e;
                        }

                        // Exponential back-off
                        exponentialBackoffWait(++retryCount, 1000L);

                        // Reload & retry
                        compositionSpace = getCompositionSpace(compositionSpaceId, session);
                    }
                } while (retry);

                // Everything went fine
                Attachment retval = newAttachment;
                newAttachment = null;
                return retval;
            } finally {
                if (null != newAttachment) {
                    attachmentStorage.deleteAttachment(newAttachment.getId(), session);
                }
            }
        } finally {
            Streams.close(data);
        }
    }

    @Override
    public Attachment replaceAttachmentInCompositionSpace(UUID compositionSpaceId, UUID attachmentId, StreamedUploadFileIterator uploadedAttachments, String disposition, Session session) throws OXException {
        CompositionSpace compositionSpace = getCompositionSpace(compositionSpaceId, session);

        // Check attachment existence
        {
            List<Attachment> attachments = compositionSpace.getMessage().getAttachments();
            if (null == attachments || attachments.isEmpty()) {
                String sAttachmentId = getUnformattedString(attachmentId);
                String sCompositionSpaceId = getUnformattedString(compositionSpaceId);
                LoggerHolder.LOG.debug("No such attachment {} in compositon space {}. Available attachments are: []", sAttachmentId, sCompositionSpaceId);
                throw CompositionSpaceErrorCode.NO_SUCH_ATTACHMENT_IN_COMPOSITION_SPACE.create(sAttachmentId, sCompositionSpaceId);
            }

            Attachment toReplace = null;
            for (Iterator<Attachment> it = attachments.iterator(); null == toReplace && it.hasNext();) {
                Attachment a = it.next();
                if (attachmentId.equals(a.getId())) {
                    toReplace = a;
                }
            }
            if (null == toReplace) {
                // No such attachment
                String sCompositionSpaceId = getUnformattedString(compositionSpaceId);
                String sAttachmentId = getUnformattedString(attachmentId);
                if (LoggerHolder.LOG.isDebugEnabled()) {
                    LoggerHolder.LOG.debug("No such attachment {} in compositon space {}. Available attachments are: {}", sAttachmentId, sCompositionSpaceId, generateAttachmentIdListing(compositionSpace.getMessage().getAttachments()));
                }
                throw CompositionSpaceErrorCode.NO_SUCH_ATTACHMENT_IN_COMPOSITION_SPACE.create(sAttachmentId, sCompositionSpaceId);
            }
        }

        // Obtain attachment storage
        AttachmentStorage attachmentStorage = getAttachmentStorage(session);

        List<Attachment> newAttachments = new LinkedList<Attachment>();
        try {
            if (uploadedAttachments.hasNext()) {
                StreamedUploadFile uploadFile = uploadedAttachments.next();

                AttachmentDescription attachment = new AttachmentDescription();
                attachment.setCompositionSpaceId(compositionSpaceId);
                attachment.setContentDisposition(com.openexchange.mail.compose.Attachment.ContentDisposition.dispositionFor(disposition));
                ContentType contentType = new ContentType(uploadFile.getContentType());
                attachment.setMimeType(contentType.toString());
                if (INLINE == attachment.getContentDisposition() && contentType.startsWith("image/")) {
                    // Set a Content-Id for inline image, too
                    attachment.setContentId(UUIDs.getUnformattedStringFromRandom() + "@Open-Xchange");
                }
                attachment.setName(uploadFile.getPreparedFileName());
                attachment.setOrigin(AttachmentOrigin.UPLOAD);
                newAttachments.add(saveAttachment(uploadFile.getStream(), attachment, session, attachmentStorage));
            }

            if (newAttachments.isEmpty()) {
                // Nothing added
                return null;
            }

            Attachment newAttachment = newAttachments.get(0);
            boolean retry = true;
            int retryCount = 0;
            do {
                try {
                    // Replace new attachment in message
                    List<Attachment> attachments = new ArrayList<Attachment>(compositionSpace.getMessage().getAttachments());

                    int index = 0;
                    boolean found = false;
                    for (Iterator<Attachment> it = attachments.iterator(); !found && it.hasNext();) {
                        Attachment a = it.next();
                        if (attachmentId.equals(a.getId())) {
                            found = true;
                        } else {
                            index++;
                        }
                    }

                    if (!found) {
                        // No such attachment
                        String sCompositionSpaceId = getUnformattedString(compositionSpaceId);
                        String sAttachmentId = getUnformattedString(attachmentId);
                        if (LoggerHolder.LOG.isDebugEnabled()) {
                            LoggerHolder.LOG.debug("No such attachment {} in compositon space {}. Available attachments are: {}", sAttachmentId, sCompositionSpaceId, generateAttachmentIdListing(compositionSpace.getMessage().getAttachments()));
                        }
                        throw CompositionSpaceErrorCode.NO_SUCH_ATTACHMENT_IN_COMPOSITION_SPACE.create(sAttachmentId, sCompositionSpaceId);
                    }

                    attachments.set(index, newAttachment);

                    // Replace attachment in composition space
                    MessageDescription md = new MessageDescription();
                    md.setAttachments(attachments);
                    getStorageService().updateCompositionSpace(session, new CompositionSpaceDescription().setUuid(compositionSpaceId).setMessage(md).setLastModifiedDate(new Date(compositionSpace.getLastModified())));
                    retry = false;
                } catch (OXException e) {
                    if (!CompositionSpaceErrorCode.CONCURRENT_UPDATE.equals(e)) {
                        throw e;
                    }

                    // Exponential back-off
                    exponentialBackoffWait(++retryCount, 1000L);

                    // Reload & retry
                    compositionSpace = getCompositionSpace(compositionSpaceId, session);
                }
            } while (retry);

            // Everything went fine
            newAttachments = null;
            return newAttachment;
        } catch (IOException e) {
            throw AjaxExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } finally {
            if (null != newAttachments) {
                for (Attachment attachment : newAttachments) {
                    attachmentStorage.deleteAttachment(attachment.getId(), session);
                }
            }
        }
    }

    @Override
    public List<Attachment> addAttachmentToCompositionSpace(UUID compositionSpaceId, StreamedUploadFileIterator uploadedAttachments, String disposition, Session session) throws OXException {
        CompositionSpace compositionSpace = getCompositionSpace(compositionSpaceId, session);

        // Obtain attachment storage
        AttachmentStorage attachmentStorage = getAttachmentStorage(session);

        List<Attachment> newAttachments = new LinkedList<Attachment>();
        try {
            while (uploadedAttachments.hasNext()) {
                StreamedUploadFile uploadFile = uploadedAttachments.next();

                AttachmentDescription attachment = new AttachmentDescription();
                attachment.setCompositionSpaceId(compositionSpaceId);
                attachment.setContentDisposition(com.openexchange.mail.compose.Attachment.ContentDisposition.dispositionFor(disposition));
                ContentType contentType = new ContentType(uploadFile.getContentType());
                attachment.setMimeType(contentType.toString());
                if (INLINE == attachment.getContentDisposition() && contentType.startsWith("image/")) {
                    // Set a Content-Id for inline image, too
                    attachment.setContentId(UUIDs.getUnformattedStringFromRandom() + "@Open-Xchange");
                }
                attachment.setName(uploadFile.getPreparedFileName());
                attachment.setOrigin(AttachmentOrigin.UPLOAD);
                newAttachments.add(saveAttachment(uploadFile.getStream(), attachment, session, attachmentStorage));
            }

            boolean retry = true;
            int retryCount = 0;
            do {
                try {
                    // Add new attachments to message
                    List<Attachment> attachments = new ArrayList<Attachment>(compositionSpace.getMessage().getAttachments());
                    for (Attachment attachment : newAttachments) {
                        attachments.add(attachment);
                    }
                    Collections.sort(attachments, AttachmentComparator.getInstance());

                    // Add new attachments to composition space
                    MessageDescription md = new MessageDescription();
                    md.setAttachments(attachments);
                    getStorageService().updateCompositionSpace(session, new CompositionSpaceDescription().setUuid(compositionSpaceId).setMessage(md).setLastModifiedDate(new Date(compositionSpace.getLastModified())));
                    retry = false;
                } catch (OXException e) {
                    if (!CompositionSpaceErrorCode.CONCURRENT_UPDATE.equals(e)) {
                        throw e;
                    }

                    // Exponential back-off
                    exponentialBackoffWait(++retryCount, 1000L);

                    // Reload & retry
                    compositionSpace = getCompositionSpace(compositionSpaceId, session);
                }
            } while (retry);

            // Everything went fine
            List<Attachment> retval = newAttachments;
            newAttachments = null;
            return retval;
        } catch (IOException e) {
            throw AjaxExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } finally {
            if (null != newAttachments) {
                for (Attachment attachment : newAttachments) {
                    attachmentStorage.deleteAttachment(attachment.getId(), session);
                }
            }
        }
    }

    @Override
    public Attachment addVCardToCompositionSpace(UUID compositionSpaceId, Session session) throws OXException {
        CompositionSpace compositionSpace = getCompositionSpace(compositionSpaceId, session);
        for (Attachment existingAttachment : compositionSpace.getMessage().getAttachments()) {
            if (AttachmentOrigin.VCARD == existingAttachment.getOrigin()) {
                // vCard already contained
                return existingAttachment;
            }
        }

        AttachmentStorage attachmentStorage = getAttachmentStorage(session);

        // Compile & save vCard attachment
        Attachment vcardAttachment;
        {
            // Create VCard
            VCardAndFileName userVCard = getUserVCard(session);
            byte[] vcard = userVCard.getVcard();

            AttachmentDescription attachment = new AttachmentDescription();
            attachment.setCompositionSpaceId(compositionSpaceId);
            attachment.setContentDisposition(ATTACHMENT);
            attachment.setMimeType(MimeTypes.MIME_TEXT_VCARD + "; charset=\"UTF-8\"");
            attachment.setName(userVCard.getFileName());
            attachment.setSize(vcard.length);
            attachment.setOrigin(AttachmentOrigin.VCARD);
            vcardAttachment = saveAttachment(Streams.newByteArrayInputStream(vcard), attachment, session, attachmentStorage);
        }

        try {
            boolean retry = true;
            int retryCount = 0;
            do {
                try {
                    // Add new attachment to message
                    List<Attachment> attachments = new ArrayList<Attachment>(compositionSpace.getMessage().getAttachments());
                    attachments.add(vcardAttachment);
                    Collections.sort(attachments, AttachmentComparator.getInstance());

                    // Add new attachments to composition space
                    MessageDescription md = new MessageDescription().setAttachments(attachments);
                    getStorageService().updateCompositionSpace(session, new CompositionSpaceDescription().setUuid(compositionSpaceId).setMessage(md).setLastModifiedDate(new Date(compositionSpace.getLastModified())));
                    retry = false;
                } catch (OXException e) {
                    if (!CompositionSpaceErrorCode.CONCURRENT_UPDATE.equals(e)) {
                        throw e;
                    }

                    // Exponential back-off
                    exponentialBackoffWait(++retryCount, 1000L);

                    // Reload & retry
                    compositionSpace = getCompositionSpace(compositionSpaceId, session);
                }
            } while (retry);

            Attachment retval = vcardAttachment;
            vcardAttachment = null;
            return retval;
        } finally {
            if (null != vcardAttachment) {
                deleteAttachmentSafe(vcardAttachment, attachmentStorage, session);
            }
        }
    }

    @Override
    public Attachment addContactVCardToCompositionSpace(UUID compositionSpaceId, String contactId, String folderId, Session session) throws OXException {
        CompositionSpace compositionSpace = getCompositionSpace(compositionSpaceId, session);
        AttachmentStorage attachmentStorage = getAttachmentStorage(session);

        // Compile & save vCard attachment
        Attachment vcardAttachment;
        {
            // Create VCard
            VCardAndFileName contactVCard = CompositonSpaces.getContactVCard(contactId, folderId, session);
            byte[] vcard = contactVCard.getVcard();

            AttachmentDescription attachment = new AttachmentDescription();
            attachment.setCompositionSpaceId(compositionSpaceId);
            attachment.setContentDisposition(ATTACHMENT);
            attachment.setMimeType(MimeTypes.MIME_TEXT_VCARD + "; charset=\"UTF-8\"");
            attachment.setName(contactVCard.getFileName());
            attachment.setSize(vcard.length);
            attachment.setOrigin(AttachmentOrigin.CONTACT);
            vcardAttachment = saveAttachment(Streams.newByteArrayInputStream(vcard), attachment, session, attachmentStorage);
        }

        try {
            boolean retry = true;
            int retryCount = 0;
            do {
                try {
                    // Add new attachment to message
                    List<Attachment> attachments = new ArrayList<Attachment>(compositionSpace.getMessage().getAttachments());
                    attachments.add(vcardAttachment);
                    Collections.sort(attachments, AttachmentComparator.getInstance());

                    // Add new attachments to composition space
                    MessageDescription md = new MessageDescription().setAttachments(attachments);
                    getStorageService().updateCompositionSpace(session, new CompositionSpaceDescription().setUuid(compositionSpaceId).setMessage(md).setLastModifiedDate(new Date(compositionSpace.getLastModified())));
                    retry = false;
                } catch (OXException e) {
                    if (!CompositionSpaceErrorCode.CONCURRENT_UPDATE.equals(e)) {
                        throw e;
                    }

                    // Exponential back-off
                    exponentialBackoffWait(++retryCount, 1000L);

                    // Reload & retry
                    compositionSpace = getCompositionSpace(compositionSpaceId, session);
                }
            } while (retry);

            Attachment retval = vcardAttachment;
            vcardAttachment = null;
            return retval;
        } finally {
            if (null != vcardAttachment) {
                deleteAttachmentSafe(vcardAttachment, attachmentStorage, session);
            }
        }
    }

    @Override
    public CompositionSpace getCompositionSpace(UUID compositionSpaceId, Session session) throws OXException {
        CompositionSpace compositionSpace = getStorageService().getCompositionSpace(session, compositionSpaceId);
        if (null == compositionSpace) {
            throw CompositionSpaceErrorCode.NO_SUCH_COMPOSITION_SPACE.create(getUnformattedString(compositionSpaceId));
        }
        return compositionSpace;
    }

    @Override
    public List<CompositionSpace> getCompositionSpaces(MessageField[] fields, Session session) throws OXException {
        return getStorageService().getCompositionSpaces(session, fields);
    }

    @Override
    public CompositionSpace updateCompositionSpace(UUID compositionSpaceId, MessageDescription md, Session session) throws OXException {
        CompositionSpace compositionSpace = null;

        boolean retry = true;
        int retryCount = 0;
        do {
            try {
                compositionSpace = getCompositionSpace(compositionSpaceId, session);

                // Check if attachment identifiers are about to be changed
                Set<UUID> oldAttachmentIds = null;
                if (md.containsAttachments()) {

                    List<Attachment> oldAttachments = compositionSpace.getMessage().getAttachments();
                    oldAttachmentIds = new HashSet<UUID>(oldAttachments.size());
                    for (Attachment attachment : oldAttachments) {
                        oldAttachmentIds.add(attachment.getId());
                    }
                }

                // Perform update
                compositionSpace = getStorageService().updateCompositionSpace(session, new CompositionSpaceDescription().setUuid(compositionSpaceId).setMessage(md).setLastModifiedDate(new Date(compositionSpace.getLastModified())));
                retry = false;

                // Update successfully performed... Check for orphaned attachments
                if (null != oldAttachmentIds) {
                    AttachmentStorage attachmentStorage = getAttachmentStorage(session);

                    List<UUID> toDelete = new ArrayList<UUID>(oldAttachmentIds.size());
                    for (Attachment attachment : md.getAttachments()) {
                        UUID attachmentId = attachment.getId();
                        if (false == oldAttachmentIds.contains(attachmentId)) {
                            toDelete.add(attachmentId);
                        }
                    }

                    attachmentStorage.deleteAttachments(toDelete, session);
                }
            } catch (OXException e) {
                if (!CompositionSpaceErrorCode.CONCURRENT_UPDATE.equals(e)) {
                    throw e;
                }

                // Exponential back-off
                exponentialBackoffWait(++retryCount, 1000L);

                // Reload & retry
            }
        } while (retry);

        if (null == compositionSpace) {
            throw CompositionSpaceErrorCode.NO_SUCH_COMPOSITION_SPACE.create(getUnformattedString(compositionSpaceId));
        }
        return compositionSpace;
    }

    @Override
    public boolean closeCompositionSpace(UUID compositionSpaceId, Session session) throws OXException {
        AttachmentStorage attachmentStorage = getAttachmentStorage(session);

        boolean closed = getStorageService().closeCompositionSpace(session, compositionSpaceId);
        if (closed) {
            attachmentStorage.deleteAttachmentsByCompositionSpace(compositionSpaceId, session);
        }
        return closed;
    }

    @Override
    public void closeExpiredCompositionSpaces(long maxIdleTimeMillis, Session session) throws OXException {
        List<UUID> deleted = getStorageService().deleteExpiredCompositionSpaces(session, maxIdleTimeMillis);
        if (null != deleted && !deleted.isEmpty()) {
            AttachmentStorage attachmentStorage = getAttachmentStorage(session);
            for (UUID compositionSpaceId : deleted) {
                try {
                    attachmentStorage.deleteAttachmentsByCompositionSpace(compositionSpaceId, session);
                } catch (Exception e) {
                    LoggerHolder.LOG.warn("Failed to delete attachments associated with composition space {}", getUnformattedString(compositionSpaceId), e);
                }
            }
        }
    }

    @Override
    public List<Attachment> addOriginalAttachmentsToCompositionSpace(UUID compositionSpaceId, Session session) throws OXException {
        CompositionSpace compositionSpace = getCompositionSpace(compositionSpaceId, session);

        // Acquire meta information and determine the "replyFor" path
        Meta meta = compositionSpace.getMessage().getMeta();
        MailPath replyFor = meta.getReplyFor();
        if (null == replyFor) {
            throw CompositionSpaceErrorCode.NO_REPLY_FOR.create();
        }

        // Obtain attachment storage
        AttachmentStorage attachmentStorage = getAttachmentStorage(session);

        List<Attachment> newAttachments = null;
        MailServletInterface mailInterface = null;
        try {
            mailInterface = MailServletInterface.getInstance(session);
            MailMessage originalMail = requireMailMessage(replyFor, mailInterface);

            if (!originalMail.getContentType().startsWith("multipart/")) {
                return Collections.emptyList();
            }

            // Grab first seen text from original message and check for possible referenced inline images
            List<String> contentIds = new ArrayList<String>();
            MimeProcessingUtility.getTextForForward(originalMail, true, false, contentIds, session);

            // Add mail's non-inline parts
            NonInlineForwardPartHandler handler = new NonInlineForwardPartHandler();
            if (false == contentIds.isEmpty()) {
                handler.setImageContentIds(contentIds);
            }
            new MailMessageParser().setInlineDetectorBehavior(true).parseMailMessage(originalMail, handler);
            List<MailPart> nonInlineParts = handler.getNonInlineParts();
            if (null == nonInlineParts || nonInlineParts.isEmpty()) {
                return Collections.emptyList();
            }

            newAttachments = new ArrayList<>(nonInlineParts.size());
            int i = 0;
            for (MailPart mailPart : nonInlineParts) {
                // Compile & store attachment
                AttachmentDescription attachment = new AttachmentDescription();
                attachment.setCompositionSpaceId(compositionSpaceId);
                attachment.setContentDisposition(ATTACHMENT);
                attachment.setMimeType(mailPart.getContentType().toString(true));
                String fileName = mailPart.getFileName();
                attachment.setName(Strings.isEmpty(fileName) ? MailMessageParser.generateFilename(Integer.toString(i+1), mailPart.getContentType().getBaseType()) : fileName);
                attachment.setOrigin(hasVCardMarker(mailPart, session) ? AttachmentOrigin.VCARD : AttachmentOrigin.MAIL);
                Attachment partAttachment = saveAttachment(mailPart.getInputStream(), attachment, session, attachmentStorage);
                newAttachments.add(partAttachment);
                i++;
            }

            boolean retry = true;
            int retryCount = 0;
            do {
                try {
                    // Add new attachments to message
                    List<Attachment> attachments = new ArrayList<Attachment>(compositionSpace.getMessage().getAttachments());
                    for (Attachment attachment : newAttachments) {
                        attachments.add(attachment);
                    }
                    Collections.sort(attachments, AttachmentComparator.getInstance());

                    // Add new attachments to composition space
                    MessageDescription md = new MessageDescription();
                    md.setAttachments(attachments);
                    getStorageService().updateCompositionSpace(session, new CompositionSpaceDescription().setUuid(compositionSpaceId).setMessage(md).setLastModifiedDate(new Date(compositionSpace.getLastModified())));
                    retry = false;
                } catch (OXException e) {
                    if (!CompositionSpaceErrorCode.CONCURRENT_UPDATE.equals(e)) {
                        throw e;
                    }

                    // Exponential back-off
                    exponentialBackoffWait(++retryCount, 1000L);

                    // Reload & retry
                    compositionSpace = getCompositionSpace(compositionSpaceId, session);
                }
            } while (retry);

            // Everything went fine
            List<Attachment> retval = newAttachments;
            newAttachments = null;
            return retval;
        } finally {
            if (null != mailInterface) {
                mailInterface.close(true);
            }
            if (null != newAttachments) {
                for (Attachment attachment : newAttachments) {
                    attachmentStorage.deleteAttachment(attachment.getId(), session);
                }
            }
        }
    }

    @Override
    public Attachment getAttachment(UUID compositionSpaceId, UUID attachmentId, Session session) throws OXException {
        CompositionSpace compositionSpace = getCompositionSpace(compositionSpaceId, session);

        // Obtain attachment storage
        AttachmentStorage attachmentStorage = getAttachmentStorage(session);

        // Find the attachment to return in composition space
        {
            List<Attachment> attachments = compositionSpace.getMessage().getAttachments();
            if (null == attachments) {
                String sAttachmentId = getUnformattedString(attachmentId);
                String sCompositionSpaceId = getUnformattedString(compositionSpaceId);
                LoggerHolder.LOG.debug("No such attachment {} in compositon space {}. Available attachments are: []", sAttachmentId, sCompositionSpaceId);
                throw CompositionSpaceErrorCode.NO_SUCH_ATTACHMENT_IN_COMPOSITION_SPACE.create(sAttachmentId, sCompositionSpaceId);
            }

            Attachment toReturn = null;
            for (Iterator<Attachment> it = attachments.iterator(); null == toReturn && it.hasNext();) {
                Attachment a = it.next();
                if (attachmentId.equals(a.getId())) {
                    toReturn = a;
                }
            }
            if (null == toReturn) {
                // No such attachment
                String sCompositionSpaceId = getUnformattedString(compositionSpaceId);
                String sAttachmentId = getUnformattedString(attachmentId);
                if (LoggerHolder.LOG.isDebugEnabled()) {
                    LoggerHolder.LOG.debug("No such attachment {} in compositon space {}. Available attachments are: {}", sAttachmentId, sCompositionSpaceId, generateAttachmentIdListing(compositionSpace.getMessage().getAttachments()));
                }
                throw CompositionSpaceErrorCode.NO_SUCH_ATTACHMENT_IN_COMPOSITION_SPACE.create(sAttachmentId, sCompositionSpaceId);
            }
        }

        // Look-up attachment in attachment storage
        Attachment attachment = attachmentStorage.getAttachment(attachmentId, session);
        if (null == attachment) {
            // No such attachment. Delete non-existent attachment from composition space's references
            boolean retry = true;
            int retryCount = 0;
            do {
                try {
                    List<Attachment> existingAttachments = compositionSpace.getMessage().getAttachments();
                    List<Attachment> attachments = new ArrayList<Attachment>(existingAttachments.size());
                    for (Iterator<Attachment> it = existingAttachments.iterator(); it.hasNext();) {
                        Attachment a = it.next();
                        if (!attachmentId.equals(a.getId())) {
                            attachments.add(a);
                        }
                    }
                    Collections.sort(attachments, AttachmentComparator.getInstance());

                    MessageDescription md = new MessageDescription().setAttachments(attachments);
                    getStorageService().updateCompositionSpace(session, new CompositionSpaceDescription().setUuid(compositionSpaceId).setMessage(md).setLastModifiedDate(new Date(compositionSpace.getLastModified())));
                    retry = false;
                } catch (OXException e) {
                    if (CompositionSpaceErrorCode.CONCURRENT_UPDATE.equals(e)) {
                        // Exponential back-off
                        exponentialBackoffWait(++retryCount, 1000L);

                        // Reload & retry
                        compositionSpace = getCompositionSpace(compositionSpaceId, session);
                    } else {
                        LoggerHolder.LOG.warn("Failed to delete non-existent attachment {} from composition space {}", getUnformattedString(attachmentId), getUnformattedString(compositionSpaceId), e);
                        retry = false;
                    }
                } catch (Exception e) {
                    LoggerHolder.LOG.warn("Failed to delete non-existent attachment {} from composition space {}", getUnformattedString(attachmentId), getUnformattedString(compositionSpaceId), e);
                    retry = false;
                }
            } while (retry);

            throw CompositionSpaceErrorCode.NO_SUCH_ATTACHMENT_RESOURCE.create(getUnformattedString(attachmentId));
        }

        return attachment;
    }

    @Override
    public void deleteAttachment(UUID compositionSpaceId, UUID attachmentId, Session session) throws OXException {
        CompositionSpace compositionSpace = getCompositionSpace(compositionSpaceId, session);

        // Obtain attachment storage
        AttachmentStorage attachmentStorage = getAttachmentStorage(session);

        boolean retry = true;
        int retryCount = 0;
        do {
            try {
                // Find the attachment to delete
                List<Attachment> attachments;
                {
                    List<Attachment> existingAttachments = compositionSpace.getMessage().getAttachments();
                    if (null == existingAttachments) {
                        String sAttachmentId = getUnformattedString(attachmentId);
                        String sCompositionSpaceId = getUnformattedString(compositionSpaceId);
                        LoggerHolder.LOG.debug("No such attachment {} in compositon space {}. Available attachments are: []", sAttachmentId, sCompositionSpaceId);
                        throw CompositionSpaceErrorCode.NO_SUCH_ATTACHMENT_IN_COMPOSITION_SPACE.create(sAttachmentId, sCompositionSpaceId);
                    }

                    attachments = new ArrayList<Attachment>(existingAttachments);
                }

                Attachment toDelete = null;
                for (Iterator<Attachment> it = attachments.iterator(); toDelete == null && it.hasNext();) {
                    Attachment attachment = it.next();
                    if (attachmentId.equals(attachment.getId())) {
                        toDelete = attachment;
                        it.remove();
                    }
                }

                if (null == toDelete) {
                    // No such attachment
                    String sAttachmentId = getUnformattedString(attachmentId);
                    String sCompositionSpaceId = getUnformattedString(compositionSpaceId);
                    if (LoggerHolder.LOG.isDebugEnabled()) {
                        LoggerHolder.LOG.debug("No such attachment {} in compositon space {}. Available attachments are: {}", sAttachmentId, sCompositionSpaceId, generateAttachmentIdListing(compositionSpace.getMessage().getAttachments()));
                    }
                    throw CompositionSpaceErrorCode.NO_SUCH_ATTACHMENT_IN_COMPOSITION_SPACE.create(sAttachmentId, sCompositionSpaceId);
                }
                if (!attachments.isEmpty()) {
                    Collections.sort(attachments, AttachmentComparator.getInstance());
                }

                // Update composition space
                MessageDescription md = new MessageDescription().setAttachments(attachments);
                getStorageService().updateCompositionSpace(session, new CompositionSpaceDescription().setUuid(compositionSpaceId).setMessage(md).setLastModifiedDate(new Date(compositionSpace.getLastModified())));
                retry = false;
            } catch (OXException e) {
                if (!CompositionSpaceErrorCode.CONCURRENT_UPDATE.equals(e)) {
                    throw e;
                }

                // Exponential back-off
                exponentialBackoffWait(++retryCount, 1000L);

                // Reload & retry
                compositionSpace = getCompositionSpace(compositionSpaceId, session);
            }
        } while (retry);

        // Delete the denoted attachment
        attachmentStorage.deleteAttachment(attachmentId, session);
    }

    /**
     * Saves the specified attachment binary data and meta data using given storage instance.
     *
     * @param input The input stream providing binary data
     * @param attachment The attachment providing meta data
     * @param session The session providing user information
     * @param attachmentStorage The storage instance to use
     * @return The resulting attachment
     * @throws OXException If saving attachment fails
     */
    private static Attachment saveAttachment(InputStream input, AttachmentDescription attachmentDesc, Session session, AttachmentStorage attachmentStorage) throws OXException {
        try {
            return attachmentStorage.saveAttachment(input, attachmentDesc, null, session);
        } finally {
            Streams.close(input);
        }
    }

    /**
     * Gets the session user's vCard.
     *
     * @param session The session
     * @return The vCard as byte array
     * @throws OXException
     */
    private VCardAndFileName getUserVCard(Session session) throws OXException {
        return CompositonSpaces.getUserVCard(session);
    }

    private static final String HDR_MESSAGE_ID = MessageHeaders.HDR_MESSAGE_ID;

    private static final String HDR_REFERENCES = MessageHeaders.HDR_REFERENCES;

    private static final String HDR_IN_REPLY_TO = MessageHeaders.HDR_IN_REPLY_TO;

    /**
     * Sets the appropriate headers <code>In-Reply-To</code> and <code>References</code> in specified MIME message.
     *
     * @param referencedMail The referenced mail
     * @param message The message to set in
     */
    private static void setReplyHeaders(MailMessage referencedMail, ComposedMailMessage message) {
        if (null == referencedMail) {
            /*
             * Obviously referenced mail does no more exist; cancel setting reply headers Message-Id, In-Reply-To, and References.
             */
            return;
        }
        final String pMsgId = referencedMail.getFirstHeader(HDR_MESSAGE_ID);
        if (pMsgId != null) {
            message.setHeader(HDR_IN_REPLY_TO, pMsgId);
        }
        /*
         * Set References header field
         */
        final String pReferences = referencedMail.getFirstHeader(HDR_REFERENCES);
        final String pInReplyTo = referencedMail.getFirstHeader(HDR_IN_REPLY_TO);
        final StringBuilder refBuilder = new StringBuilder();
        if (pReferences != null) {
            /*
             * The "References:" field will contain the contents of the parent's "References:" field (if any) followed by the contents of
             * the parent's "Message-ID:" field (if any).
             */
            refBuilder.append(pReferences);
        } else if (pInReplyTo != null) {
            /*
             * If the parent message does not contain a "References:" field but does have an "In-Reply-To:" field containing a single
             * message identifier, then the "References:" field will contain the contents of the parent's "In-Reply-To:" field followed by
             * the contents of the parent's "Message-ID:" field (if any).
             */
            refBuilder.append(pInReplyTo);
        }
        if (pMsgId != null) {
            if (refBuilder.length() > 0) {
                refBuilder.append(' ');
            }
            refBuilder.append(pMsgId);
            /*
             * If the parent has none of the "References:", "In-Reply-To:", or "Message-ID:" fields, then the new message will have no
             * "References:" field.
             */
            message.setReferences(refBuilder.toString());
        } else if (refBuilder.length() > 0) {
            /*
             * If the parent has none of the "References:", "In-Reply-To:", or "Message-ID:" fields, then the new message will have no
             * "References:" field.
             */
            message.setReferences(refBuilder.toString());
        }
    }

    /**
     * Converts given (internet) address to an {@link Address} instance
     *
     * @param addr The address to convert
     * @return The resulting {@code Address} instance
     */
    private static Address toAddress(QuotedInternetAddress addr) {
        return null == addr ? null : new Address(addr.getPersonal(), addr.getUnicodeAddress());
    }

    /**
     * Converts given (internet) address to an {@link Address} instance
     *
     * @param addr The address to convert
     * @return The resulting {@code Address} instance
     */
    private static Address toAddress(InternetAddress addr) {
        return null == addr ? null : new Address(addr.getPersonal(), IDNA.toIDN(addr.getAddress()));
    }

    /**
     * Converts given (internet) addresses to an {@link Address} instances
     *
     * @param addrs The addresses to convert
     * @return The resulting {@code Address} instances
     */
    private static List<Address> toAddresses(InternetAddress[] addrs) {
        if (null == addrs || 0 == addrs.length) {
            return Collections.emptyList();
        }

        List<Address> addresses = new ArrayList<Address>(addrs.length);
        for (InternetAddress addr : addrs) {
            Address address = toAddress(addr);
            if (null != address) {
                addresses.add(address);
            }
        }
        return addresses;
    }

    /**
     * Gets referenced mail
     *
     * @param mailPath The mail path for the mail
     * @param mailInterface The service to use
     * @return The mail
     * @throws OXException If mail cannot be returned
     */
    private MailMessage requireMailMessage(MailPath mailPath, MailServletInterface mailInterface) throws OXException {
        MailMessage mailMessage = mailInterface.getMessage(mailPath.getFolderArgument(), mailPath.getMailID(), false);
        if (null == mailMessage) {
            throw MailExceptionCode.MAIL_NOT_FOUND.create(mailPath.getMailID(), mailPath.getFolderArgument());
        }
        return mailMessage;
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    private static final Pattern PATTERN_SRC = MimeMessageUtility.PATTERN_SRC;

    /**
     * Replaces &lt;img&gt; tags providing an inline image through exchanging <code>"src"</code> value appropriately.
     * <p>
     * <code>&lt;img src="cid:123456"&gt;</code> is converted to<br>
     * <code>&lt;img src="/ajax/image/mail/compose/image?uid=71ff23e06f424cc5bcb08a92e006838a"&gt;</code>
     *
     * @param htmlContent The HTML content to replace in
     * @param contentId2InlineAttachments The detected inline images
     * @param session The session providing user information
     * @return The (possibly) processed HTML content
     * @throws OXException If replacing &lt;img&gt; tags fails
     */
    private static String replaceCidInlineImages(String htmlContent, Map<String, Attachment> contentId2InlineAttachments, Session session) throws OXException {
        Matcher matcher = PATTERN_SRC.matcher(htmlContent);
        if (!matcher.find()) {
            return htmlContent;
        }

        ImageDataSource imageDataSource = AttachmentImageDataSource.getInstance();
        StringBuffer sb = new StringBuffer(htmlContent.length());
        do {
            String imageTag = matcher.group();
            String srcValue = matcher.group(1);
            if (srcValue.startsWith("cid:")) {
                String contentId = MimeMessageUtility.trimContentId(srcValue.substring(4));
                Attachment attachment = contentId2InlineAttachments.get(contentId);
                if (null == attachment) {
                    // No such inline image... Yield a blank "src" attribute for current <img> tag
                    LoggerHolder.LOG.warn("No such inline image found for Content-Id {}", contentId);
                    int st = matcher.start(1);
                    int end = matcher.end(1);
                    matcher.appendReplacement(sb, Matcher.quoteReplacement(imageTag.substring(0, st) + imageTag.substring(end)));
                } else {
                    ImageLocation imageLocation = new ImageLocation.Builder(getUnformattedString(attachment.getId())).optImageHost(HtmlProcessing.imageHost()).build();
                    String imageUrl = imageDataSource.generateUrl(imageLocation, session);
                    int st = matcher.start(1) - matcher.start();
                    int end = matcher.end(1) - matcher.start();
                    matcher.appendReplacement(sb, Matcher.quoteReplacement(imageTag.substring(0, st) + imageUrl + imageTag.substring(end)));
                }
            }
        } while (matcher.find());
        matcher.appendTail(sb);
        return sb.toString();
    }

    private static final Pattern PATTERN_IMAGE_ID = Pattern.compile("[?&]" + AJAXServlet.PARAMETER_UID + "=([^&]+)");

    private static final String UTF_8 = "UTF-8";

    /*
     * Something like "/ajax/image/mail/compose/image..."
     */
    private static final Pattern PATTERN_IMAGE_SRC_START_BY_IMAGE = Pattern.compile("[a-zA-Z_0-9&-.]+/(?:[a-zA-Z_0-9&-.]+/)*" + ImageActionFactory.ALIAS_APPENDIX + AttachmentImageDataSource.getInstance().getAlias(), Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    /*
     * Something like "/appsuite/api/mail/compose/5e9f9b6d15a94a31a8ba175489e5363a/attachments/8f119070e6af4143bd2f3c74bd8973a9..."
     */
    private static final Pattern PATTERN_IMAGE_SRC_START_BY_URL = Pattern.compile("[a-zA-Z_0-9&-.]+/(?:[a-zA-Z_0-9&-.]+/)*" + "mail/compose/" + "([a-fA-F0-9]+)" + "/attachments/" + "([a-fA-F0-9]+)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    /**
     * Replaces &lt;img&gt; tags providing an inline image through exchanging <code>"src"</code> value appropriately.
     * <p>
     * <code>&lt;img src="/ajax/image/mail/compose/image?uid=71ff23e06f424cc5bcb08a92e006838a"&gt;</code> is converted to<br>
     * <code>&lt;img src="cid:123456"&gt;</code>
     *
     * @param htmlContent The HTML content to replace in
     * @param attachmentId2inlineAttachments The detected inline images
     * @param contentId2InlineAttachment The map to fill with actually used inline attachments
     * @param fileAttachments The complete attachment mapping from which to remove actually used inline attachments
     * @param session The session providing user information
     * @return The (possibly) processed HTML content
     * @throws OXException If replacing &lt;img&gt; tags fails
     */
    private static String replaceLinkedInlineImages(String htmlContent, Map<String, Attachment> attachmentId2inlineAttachments, Map<String, Attachment> contentId2InlineAttachment, Map<UUID, Attachment> fileAttachments,  Session session) throws OXException {
        Matcher matcher = PATTERN_SRC.matcher(htmlContent);
        if (!matcher.find()) {
            return htmlContent;
        }

        StringBuffer sb = new StringBuffer(htmlContent.length());
        Matcher mailComposeUrlMatcher;
        do {
            String imageTag = matcher.group();
            String srcValue = matcher.group(1);
            if (PATTERN_IMAGE_SRC_START_BY_IMAGE.matcher(srcValue).find()) {
                Matcher attachmentIdMatcher = PATTERN_IMAGE_ID.matcher(srcValue);
                if (attachmentIdMatcher.find()) {
                    String attachmentId = AJAXUtility.decodeUrl(attachmentIdMatcher.group(1), UTF_8);
                    replaceLinkedInlineImage(attachmentId, imageTag, sb, matcher, attachmentId2inlineAttachments, contentId2InlineAttachment, fileAttachments);
                }
            } else if ((mailComposeUrlMatcher = PATTERN_IMAGE_SRC_START_BY_URL.matcher(srcValue)).find()) {
                String attachmentId = AJAXUtility.decodeUrl(mailComposeUrlMatcher.group(2), UTF_8);
                replaceLinkedInlineImage(attachmentId, imageTag, sb, matcher, attachmentId2inlineAttachments, contentId2InlineAttachment, fileAttachments);
            }
        } while (matcher.find());
        matcher.appendTail(sb);
        return sb.toString();
    }

    private static void replaceLinkedInlineImage(String attachmentId, String imageTag, StringBuffer sb, Matcher matcher, Map<String, Attachment> attachmentId2inlineAttachments, Map<String, Attachment> contentId2InlineAttachment, Map<UUID, Attachment> fileAttachments) {
        Attachment attachment = attachmentId2inlineAttachments.get(attachmentId);
        if (null == attachment) {
            // No such inline image... Yield a blank "src" attribute for current <img> tag
            LoggerHolder.LOG.warn("No such inline image found for attachment identifier {}", attachmentId);
            matcher.appendReplacement(sb, "");
        } else {
            String imageUrl = "cid:" + attachment.getContentId();
            int st = matcher.start(1) - matcher.start();
            int end = matcher.end(1) - matcher.start();
            matcher.appendReplacement(sb, Matcher.quoteReplacement(imageTag.substring(0, st) + imageUrl + imageTag.substring(end)));

            contentId2InlineAttachment.put(attachment.getContentId(), attachment);
            fileAttachments.remove(attachment.getId());
        }
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    private static final String CAPABILITY_SHARE_MAIL_ATTACHMENTS = "share_mail_attachments";

    private boolean mayShareAttachments(Session session) throws OXException {
        CapabilityService capabilityService = services.getOptionalService(CapabilityService.class);
        return null == capabilityService ? false : capabilityService.getCapabilities(session).contains(CAPABILITY_SHARE_MAIL_ATTACHMENTS);
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    private static InternetAddress[] toMimeAddresses(List<Address> addrs) throws OXException {
        if (null == addrs) {
            return null;
        }
        List<InternetAddress> mimeAddresses = new ArrayList<>(addrs.size());
        for (Address address : addrs) {
            InternetAddress mimeAddress = toMimeAddress(address);
            if (null != mimeAddress) {
                mimeAddresses.add(mimeAddress);
            }
        }
        return mimeAddresses.toArray(new InternetAddress[mimeAddresses.size()]);
    }

    private static InternetAddress toMimeAddress(Address a) throws OXException {
        if (null == a) {
            return null;
        }
        try {
            return new QuotedInternetAddress(a.getAddress(), a.getPersonal(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            // Nah...
            throw OXException.general("UTF-8 not available", e);
        } catch (MessagingException e) {
            throw MimeMailException.handleMessagingException(e);
        }
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    private static final String HEADER_PW = "open-xchange";

    private static String encodeHeaderValue(int used, String raw) {
        if (null == raw) {
            return null;
        }

        try {
            return MimeMessageUtility.forceFold(used, MailPasswordUtil.encrypt(raw, HEADER_PW));
        } catch (GeneralSecurityException x) {
            LoggerHolder.LOG.debug("Failed to encode header value", x);
            return MimeMessageUtility.forceFold(used, raw);
        }
    }

    private static String decodeHeaderValue(String encoded) {
        if (null == encoded) {
            return null;
        }

        try {
            return MailPasswordUtil.decrypt(MimeUtility.unfold(encoded), HEADER_PW);
        } catch (GeneralSecurityException x) {
            LoggerHolder.LOG.debug("Failed to decode header value", x);
            return MimeUtility.unfold(encoded);
        }
    }

    private static final String JSON_META_NEW = new JSONObject(4).putSafe("type", Type.NEW.getId()).toString();

    private static String meta2HeaderValue(Meta meta) {
        if (null == meta || MetaType.NEW == meta.getType()) {
            return JSON_META_NEW;
        }

        JSONObject jMeta = new JSONObject(8).putSafe("type", meta.getType().getId());
        {
            Date date = meta.getDate();
            if (null != date) {
                jMeta.putSafe("date", Long.valueOf(meta.getDate().getTime()));
            }
        }
        {
            MailPath replyFor = meta.getReplyFor();
            if (null != replyFor) {
                jMeta.putSafe("replyFor", replyFor.toString());
            }
        }
        {
            MailPath editFor = meta.getEditFor();
            if (null != editFor) {
                jMeta.putSafe("editFor", editFor.toString());
            }
        }
        {
            List<MailPath> forwardsFor = meta.getForwardsFor();
            if (null != forwardsFor) {
                JSONArray jForwardsFor = new JSONArray(forwardsFor.size());
                for (MailPath forwardFor : forwardsFor) {
                    jForwardsFor.put(forwardFor.toString());
                }
                jMeta.putSafe("forwardsFor", jForwardsFor);
            }
        }

        return jMeta.toString();
    }

    private static Meta headerValue2Meta(String headerValue) {
        if (Strings.isEmpty(headerValue)) {
            return Meta.META_NEW;
        }

        try {
            JSONObject jMeta = new JSONObject(headerValue);

            Meta.Builder meta = Meta.builder();
            meta.withType(MetaType.typeFor(jMeta.optString("type", Type.NEW.getId())));
            {
                long lDate = jMeta.optLong("date", -1L);
                meta.withDate(lDate < 0 ? null : new Date(lDate));
            }
            {
                String path = jMeta.optString("replyFor", null);
                meta.withReplyFor(Strings.isEmpty(path) ? null : new MailPath(path));
            }
            {
                String path = jMeta.optString("editFor", null);
                meta.withEditFor(Strings.isEmpty(path) ? null : new MailPath(path));
            }
            {
                JSONArray jPaths = jMeta.optJSONArray("forwardsFor");
                if (null != jPaths) {
                    List<MailPath> paths = new ArrayList<MailPath>(jPaths.length());
                    for (Object jPath : jPaths) {
                        paths.add(new MailPath(jPath.toString()));
                    }
                    meta.withForwardsFor(paths);
                }
            }
            return meta.build();
        } catch (Exception e) {
            LoggerHolder.LOG.warn("Header value cannot be parsed to meta information: {}", headerValue, e);
            return Meta.META_NEW;
        }
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    private static final String JSON_SHARED_ATTACHMENTS_DISABLED = new JSONObject(4).putSafe("enabled", Boolean.FALSE).toString();

    private static String sharedAttachments2HeaderValue(SharedAttachmentsInfo sharedAttachmentsInfo) {
        if (null == sharedAttachmentsInfo || sharedAttachmentsInfo.isDisabled()) {
            return JSON_SHARED_ATTACHMENTS_DISABLED;
        }

        return new JSONObject(8).putSafe("enabled", Boolean.valueOf(sharedAttachmentsInfo.isEnabled())).putSafe("language", sharedAttachmentsInfo.getLanguage() == null ? JSONObject.NULL : sharedAttachmentsInfo.getLanguage().toString()).putSafe("autoDelete", Boolean.valueOf(sharedAttachmentsInfo.isAutoDelete())).putSafe("expiryDate", sharedAttachmentsInfo.getExpiryDate() == null ? JSONObject.NULL : Long.valueOf(sharedAttachmentsInfo.getExpiryDate().getTime())).putSafe("password", sharedAttachmentsInfo.getPassword() == null ? JSONObject.NULL : sharedAttachmentsInfo.getPassword()).toString();
    }

    private static SharedAttachmentsInfo headerValue2SharedAttachments(String headerValue) {
        if (Strings.isEmpty(headerValue)) {
            return SharedAttachmentsInfo.DISABLED;
        }

        try {
            JSONObject jSharedAttachments = new JSONObject(headerValue);

            SharedAttachmentsInfo.Builder sharedAttachments = SharedAttachmentsInfo.builder();
            sharedAttachments.withEnabled(jSharedAttachments.optBoolean("enabled", false));
            {
                String language = jSharedAttachments.optString("language", null);
                sharedAttachments.withLanguage(Strings.isEmpty(language) ? null : LocaleTools.getLocale(language));
            }
            sharedAttachments.withAutoDelete(jSharedAttachments.optBoolean("autoDelete", false));
            {
                long lDate = jSharedAttachments.optLong("expiryDate", -1L);
                sharedAttachments.withExpiryDate(lDate < 0 ? null : new Date(lDate));
            }
            sharedAttachments.withPassword(jSharedAttachments.optString("password", null));
            return sharedAttachments.build();
        } catch (JSONException e) {
            LoggerHolder.LOG.warn("Header value cannot be parsed to shared-attachments settings: {}", headerValue, e);
            return SharedAttachmentsInfo.DISABLED;
        }
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    private static final String JSON_SECURITY_DISABLED = new JSONObject(4).putSafe("encrypt", Boolean.FALSE).putSafe("pgpInline", Boolean.FALSE).putSafe("sign", Boolean.FALSE).toString();

    private static String security2HeaderValue(Security security) {
        if (null == security || security.isDisabled()) {
            return JSON_SECURITY_DISABLED;
        }

        return new JSONObject(4)
            .putSafe("encrypt", Boolean.valueOf(security.isEncrypt()))
            .putSafe("pgpInline", Boolean.valueOf(security.isPgpInline()))
            .putSafe("sign", Boolean.valueOf(security.isSign()))
            .putSafe("language", security.getLanguage())
            .putSafe("message", security.getMessage())
            .putSafe("pin", security.getPin())
            .toString();
    }

    private static Security headerValue2Security(String headerValue) {
        if (Strings.isEmpty(headerValue)) {
            return Security.DISABLED;
        }

        try {
            JSONObject jSecurity = new JSONObject(headerValue);
            return Security.builder()
                .withEncrypt(jSecurity.optBoolean("encrypt"))
                .withPgpInline(jSecurity.optBoolean("pgpInline"))
                .withSign(jSecurity.optBoolean("sign"))
                .withLanguage(jSecurity.optString("language", null))
                .withMessage(jSecurity.optString("message", null))
                .withPin(jSecurity.optString("pin", null))
                .build();
        } catch (JSONException e) {
            LoggerHolder.LOG.warn("Header value cannot be parsed to security settings: {}", headerValue, e);
            return Security.DISABLED;
        }
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    private static String generateAttachmentIdListing(List<Attachment> existingAttachments) {
        if (null == existingAttachments) {
            return "null";
        }

        int size = existingAttachments.size();
        if (size <= 0) {
            return "[]";
        }

        StringBuilder sb = new StringBuilder(size << 4);
        sb.append('[');
        sb.append(getUnformattedString(existingAttachments.get(0).getId()));
        for (int i = 1; i < size; i++) {
            sb.append(", ").append(getUnformattedString(existingAttachments.get(i).getId()));
        }
        sb.append(']');
        return sb.toString();
    }

    /**
     * Performs a wait according to exponential back-off strategy.
     * <pre>
     * (retry-count * base-millis) + random-millis
     * </pre>
     *
     * @param retryCount The current number of retries
     * @param baseMillis The base milliseconds
     */
    private static void exponentialBackoffWait(int retryCount, long baseMillis) {
        long nanosToWait = TimeUnit.NANOSECONDS.convert((retryCount * baseMillis) + ((long) (Math.random() * baseMillis)), TimeUnit.MILLISECONDS);
        LockSupport.parkNanos(nanosToWait);
    }

}
