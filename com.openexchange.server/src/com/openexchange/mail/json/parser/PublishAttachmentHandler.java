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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.mail.json.parser;

import static com.openexchange.groupware.upload.impl.UploadUtility.getSize;
import static com.openexchange.mail.mime.converters.MimeMessageConverter.convertPart;
import static com.openexchange.mail.text.HtmlProcessing.getConformHTML;
import static com.openexchange.mail.text.HtmlProcessing.htmlFormat;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.idn.IDNA;
import com.openexchange.config.ConfigurationService;
import com.openexchange.configuration.ServerConfig;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.i18n.MailStrings;
import com.openexchange.groupware.ldap.LdapExceptionCode;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.i18n.tools.StringHelper;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.attachment.storage.DefaultMailAttachmentStorageRegistry;
import com.openexchange.mail.attachment.storage.DownloadUri;
import com.openexchange.mail.attachment.storage.MailAttachmentStorage;
import com.openexchange.mail.attachment.storage.StoreOperation;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.dataobjects.compose.ComposedMailMessage;
import com.openexchange.mail.dataobjects.compose.TextBodyMailPart;
import com.openexchange.mail.mime.MessageHeaders;
import com.openexchange.mail.mime.MimeMailException;
import com.openexchange.mail.mime.utils.MimeMessageUtility;
import com.openexchange.mail.transport.TransportProvider;
import com.openexchange.mail.transport.config.TransportProperties;
import com.openexchange.mail.utils.MessageUtility;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.tools.servlet.http.Cookies;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tx.TransactionException;
import com.openexchange.user.UserService;

/**
 * {@link PublishAttachmentHandler} - An {@link IAttachmentHandler attachment handler} that publishes attachments on exceeded quota (either
 * overall or per-file quota).
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class PublishAttachmentHandler extends AbstractAttachmentHandler {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(PublishAttachmentHandler.class);

    private final Session session;
    private final TransportProvider transportProvider;
    private final String protocol;
    private final String hostName;

    private boolean exceeded;
    private TextBodyMailPart textPart;
    private long consumed;

    /**
     * Initializes a new {@link PublishAttachmentHandler}.
     *
     * @param session The session providing needed user information
     * @param transportProvider The transport provider
     * @param protocol The server's protocol
     * @param hostName The server's host name
     * @throws OXException If initialization fails
     */
    public PublishAttachmentHandler(final Session session, final TransportProvider transportProvider, final String protocol, final String hostName) throws OXException {
        super(session);
        this.protocol = protocol;
        this.hostName = hostName;
        this.transportProvider = transportProvider;
        this.session = session;
    }

    @Override
    public void setTextPart(final TextBodyMailPart textPart) {
        this.textPart = textPart;
    }

    @Override
    public void addAttachment(final MailPart attachment) throws OXException {
        if (doAction && !exceeded) {
            final long size = attachment.getSize();
            if (size <= 0) {
                LOG.debug("Missing size: {}", Long.valueOf(size), new Throwable());
            }
            if (uploadQuotaPerFile > 0 && size > uploadQuotaPerFile) {
                if (LOG.isDebugEnabled()) {
                    final String fileName = attachment.getFileName();
                    final OXException e = MailExceptionCode.UPLOAD_QUOTA_EXCEEDED_FOR_FILE.create(getSize(uploadQuotaPerFile), null == fileName ? "" : fileName, getSize(size));
                    LOG.debug("Per-file quota ({}) exceeded. Message is going to be sent with links to publishing infostore folder.", getSize(uploadQuotaPerFile), e);
                }
                exceeded = true;
            }
            /*
             * Add current file size
             */
            consumed += size;
            if (uploadQuota > 0 && consumed > uploadQuota) {
                if (LOG.isDebugEnabled()) {
                    final OXException e = MailExceptionCode.UPLOAD_QUOTA_EXCEEDED.create(getSize(uploadQuota));
                    LOG.debug("Overall quota ({}) exceeded. Message is going to be sent with links to publishing infostore folder.", getSize(uploadQuota), e);
                }
                exceeded = true;
            }
        }
        attachments.add(attachment);
    }

    @Override
    public ComposedMailMessage[] generateComposedMails(final ComposedMailMessage source, List<OXException> warnings) throws OXException {
        if (!exceeded) {
            /*
             * No quota exceeded, return prepared source
             */
            source.setBodyPart(textPart);
            for (final MailPart attachment : attachments) {
                source.addEnclosedPart(attachment);
            }
            return new ComposedMailMessage[] { source };
        }

        // Handle exceeded quota through generating appropriate publication links
        final List<PublicationAndInfostoreID> publications = new ArrayList<PublicationAndInfostoreID>(attachments.size());

        // Get attachment storage
        MailAttachmentStorage attachmentStorage = DefaultMailAttachmentStorageRegistry.getInstance().getMailAttachmentStorage();

        try {
            warnings.add(MailExceptionCode.USED_PUBLISHING_FEATURE.create());
            return generateComposedMails0(source, publications, attachmentStorage);
        } catch (final OXException e) {
            /*
             * Rollback of publications
             */
            rollbackPublications(publications, attachmentStorage);
            /*
             * Re-throw exception
             */
            throw e;
        }
    }

    private ComposedMailMessage[] generateComposedMails0(ComposedMailMessage source, List<PublicationAndInfostoreID> publications, MailAttachmentStorage attachmentStorage) throws OXException {
        final List<LinkAndNamePair> links = new ArrayList<LinkAndNamePair>(attachments.size());
        /*
         * Message information
         */
        final long now = System.currentTimeMillis();
        Map<String, Object> storeProps = new HashMap<String, Object>(8);
        storeProps.put("subject", source.getSubject());
        storeProps.put("date", new Date(now));
        storeProps.put("to", source.getTo());
        /*
         * Generate publication link for each attachment
         */
        final StringBuilder linkBuilder = new StringBuilder(256);
        for (final MailPart attachment : attachments) {
            /*
             * Generate publish URL: "/publications/infostore/documents/12abead21498754abcfde"
             */
            final String path = publishAttachmentAndGetPath(storeProps, attachment, publications, attachmentStorage);
            /*
             * Add to list
             */
            linkBuilder.setLength(0);
            if (path.startsWith("http")) {
                linkBuilder.append(path);
            } else {
                linkBuilder.append(com.openexchange.java.Strings.isEmpty(protocol) ? (forcedSecure(hostName) ? "https://" : "http://") : saneProtocol(protocol)).append(hostName).append(path);
            }
            links.add(new LinkAndNamePair(attachment.getFileName(), linkBuilder.toString()));
        }
        /*
         * Get recipients
         */
        final Set<InternetAddress> addresses = new HashSet<InternetAddress>();
        addresses.addAll(Arrays.asList(source.getTo()));
        addresses.addAll(Arrays.asList(source.getCc()));
        addresses.addAll(Arrays.asList(source.getBcc()));
        /*
         * Iterate recipients and split them to internal vs. external recipients
         */
        Date elapsedDate = null;
        if (TransportProperties.getInstance().publishedDocumentsExpire()) {
            elapsedDate = new Date(now + TransportProperties.getInstance().getPublishedDocumentTimeToLive());
        }
        final UserService userService = ServerServiceRegistry.getInstance().getService(UserService.class);
        Context ctx = getContext();
        final Map<Locale, ComposedMailMessage> internalMessages = new HashMap<Locale, ComposedMailMessage>(addresses.size());
        ComposedMailMessage externalMessage = null;
        for (final InternetAddress address : addresses) {
            User user = null;
            try {
                user = userService.searchUser(IDNA.toIDN(address.getAddress()), ctx);
            } catch (final OXException e) {
                /*
                 * Unfortunately UserService.searchUser() throws an exception if no user could be found matching given email address.
                 * Therefore check for this special error code and throw an exception if it is not equal.
                 */
                if (!LdapExceptionCode.NO_USER_BY_MAIL.equals(e)) {
                    throw e;
                }
                /*
                 * Retry
                 */
                try {
                    user = userService.searchUser(IDNA.toIDN(address.getAddress()), ctx);
                } catch (final OXException inner) {
                    if (!LdapExceptionCode.NO_USER_BY_MAIL.equals(inner)) {
                        throw inner;
                    }
                }
            }
            if (null == user) {
                // External user
                if (null == externalMessage) {
                    externalMessage = generateExternalVersion(source, ctx, links, TransportProperties.getInstance().isProvideLinksInAttachment(), elapsedDate);
                }
                externalMessage.addRecipient(address);
            } else {
                // Internal user
                final Locale locale = user.getLocale();
                ComposedMailMessage localedMessage = internalMessages.get(locale);
                if (null == localedMessage) {
                    localedMessage = generateInternalVersion(source, ctx, links, TransportProperties.getInstance().isProvideLinksInAttachment(), elapsedDate, locale);
                    internalMessages.put(locale, localedMessage);
                }
                localedMessage.addRecipient(address);
            }
        }
        /*
         * Return mail versions
         */
        final List<ComposedMailMessage> mails = new ArrayList<ComposedMailMessage>(internalMessages.size() + 1);
        mails.addAll(internalMessages.values());
        if (null != externalMessage) {
            mails.add(externalMessage);
        }
        /*
         * Any version available?
         */
        if (mails.isEmpty()) {
            mails.add(generateInternalVersion(source, ctx, links, TransportProperties.getInstance().isProvideLinksInAttachment(), elapsedDate, getSessionUser().getLocale()));
        }
        return mails.toArray(new ComposedMailMessage[mails.size()]);
    }

    private Context getContext() throws OXException {
        if (session instanceof ServerSession) {
            return ((ServerSession) session).getContext();
        }
        return ContextStorage.getStorageContext(session.getContextId());
    }

    private User getSessionUser() throws OXException {
        if (session instanceof ServerSession) {
            return ((ServerSession) session).getUser();
        }
        return UserStorage.getInstance().getUser(session.getUserId(), getContext());
    }

    private static final Pattern PATTERN_DATE = Pattern.compile(Pattern.quote("#DATE#"));

    private ComposedMailMessage generateInternalVersion(final ComposedMailMessage source, final Context ctx, final List<LinkAndNamePair> links, final boolean appendLinksAsAttachment, final Date elapsedDate, final Locale locale) throws OXException {
        final ComposedMailMessage internalVersion = copyOf(source, ctx);
        final TextBodyMailPart textPart = this.textPart.copy();
        textPart.setPlainText(null);
        final StringHelper stringHelper = StringHelper.valueOf(locale);
        if (appendLinksAsAttachment) {
            // Apply text part as it is
            internalVersion.setBodyPart(textPart);
            // Generate text for attachment
            final StringBuilder textBuilder = new StringBuilder(256 * links.size());
            textBuilder.append(htmlFormat(stringHelper.getString(MailStrings.PUBLISHED_ATTACHMENTS_PREFIX))).append("<br>");
            appendLinks(links, textBuilder);
            internalVersion.addEnclosedPart(createLinksAttachment(textBuilder.toString()));
        } else {
            final String text = (String) textPart.getContent();
            final StringBuilder textBuilder = new StringBuilder(text.length() + 512);
            textBuilder.append(htmlFormat(stringHelper.getString(MailStrings.PUBLISHED_ATTACHMENTS_PREFIX))).append("<br>");
            appendLinks(links, textBuilder);
            if (elapsedDate != null) {
                textBuilder.append(htmlFormat(PATTERN_DATE.matcher(stringHelper.getString(MailStrings.PUBLISHED_ATTACHMENTS_APPENDIX)).replaceFirst(DateFormat.getDateInstance(DateFormat.LONG, locale).format(elapsedDate)))).append("<br><br>");
            }
            textBuilder.append(text);
            textPart.setText(textBuilder.toString());
            internalVersion.setBodyPart(textPart);
        }
        return internalVersion;
    }

    private ComposedMailMessage generateExternalVersion(final ComposedMailMessage source, final Context ctx, final List<LinkAndNamePair> links, final boolean appendLinksAsAttachment, final Date elapsedDate) throws OXException {
        final ComposedMailMessage externalVersion = copyOf(source, ctx);
        final TextBodyMailPart textPart = this.textPart.copy();
        if (TransportProperties.getInstance().isSendAttachmentToExternalRecipients()) {
            externalVersion.setBodyPart(textPart);
            for (final MailPart attachment : attachments) {
                externalVersion.addEnclosedPart(attachment);
            }
        } else {
            textPart.setPlainText(null);
            Locale locale = TransportProperties.getInstance().getExternalRecipientsLocale();
            if (null == locale) {
                locale = getSessionUserLocale();
            }
            final StringHelper stringHelper = StringHelper.valueOf(locale);
            if (appendLinksAsAttachment) {
                // Apply text part as it is
                externalVersion.setBodyPart(textPart);
                // Generate text for attachment
                final StringBuilder textBuilder = new StringBuilder(256 * links.size());
                textBuilder.append(htmlFormat(stringHelper.getString(MailStrings.PUBLISHED_ATTACHMENTS_PREFIX))).append("<br>");
                appendLinks(links, textBuilder);
                externalVersion.addEnclosedPart(createLinksAttachment(textBuilder.toString()));
            } else {
                final String text = (String) textPart.getContent();
                final StringBuilder textBuilder = new StringBuilder(text.length() + 512);
                textBuilder.append(htmlFormat(stringHelper.getString(MailStrings.PUBLISHED_ATTACHMENTS_PREFIX))).append("<br>");
                appendLinks(links, textBuilder);
                if (elapsedDate != null) {
                    textBuilder.append(htmlFormat(PATTERN_DATE.matcher(stringHelper.getString(MailStrings.PUBLISHED_ATTACHMENTS_APPENDIX)).replaceFirst(DateFormat.getDateInstance(DateFormat.LONG, locale).format(elapsedDate)))).append("<br><br>");
                }
                textBuilder.append(text);
                textPart.setText(textBuilder.toString());
                externalVersion.setBodyPart(textPart);
            }
        }
        return externalVersion;
    }

    private Locale getSessionUserLocale() throws OXException {
        if (session instanceof ServerSession) {
            return ((ServerSession) session).getUser().getLocale();
        }
        final Context context = ContextStorage.getStorageContext(session.getContextId());
        return UserStorage.getInstance().getUser(session.getUserId(), context).getLocale();
    }

    private ComposedMailMessage copyOf(final ComposedMailMessage source, final Context ctx) throws OXException {
        final ComposedMailMessage composedMail = transportProvider.getNewComposedMailMessage(session, ctx);
        if (source.containsFlags()) {
            composedMail.setFlags(source.getFlags());
        }
        if (source.containsThreadLevel()) {
            composedMail.setThreadLevel(source.getThreadLevel());
        }
        if (source.containsUserFlags()) {
            composedMail.addUserFlags(source.getUserFlags());
        }
        if (source.containsUserFlags()) {
            composedMail.addUserFlags(source.getUserFlags());
        }
        if (source.containsHeaders()) {
            composedMail.addHeaders(source.getHeaders());
        }
        if (source.containsFrom()) {
            composedMail.addFrom(source.getFrom());
        }
        if (source.containsTo()) {
            composedMail.addTo(source.getTo());
        }
        if (source.containsCc()) {
            composedMail.addCc(source.getCc());
        }
        if (source.containsBcc()) {
            composedMail.addBcc(source.getBcc());
        }
        if (source.containsReplyTo()) {
            composedMail.addReplyTo(source.getReplyTo());
        }
        if (source.containsDispositionNotification()) {
            composedMail.setDispositionNotification(source.getDispositionNotification());
        }
        if (source.containsDispositionNotification()) {
            composedMail.setDispositionNotification(source.getDispositionNotification());
        }
        if (source.containsPriority()) {
            composedMail.setPriority(source.getPriority());
        }
        if (source.containsColorLabel()) {
            composedMail.setColorLabel(source.getColorLabel());
        }
        if (source.containsAppendVCard()) {
            composedMail.setAppendVCard(source.isAppendVCard());
        }
        if (source.containsMsgref()) {
            composedMail.setMsgref(source.getMsgref());
        }
        if (source.containsSubject()) {
            composedMail.setSubject(source.getSubject());
        }
        if (source.containsSize()) {
            composedMail.setSize(source.getSize());
        }
        if (source.containsSentDate()) {
            composedMail.setSentDate(source.getSentDate());
        }
        if (source.containsReceivedDate()) {
            composedMail.setReceivedDate(source.getReceivedDate());
        }
        if (source.containsContentType()) {
            composedMail.setContentType(source.getContentType());
        }
        return composedMail;
    } // End of copyOf()

    private MailPart createLinksAttachment(final String text) throws OXException, OXException {
        try {
            final MimeBodyPart bodyPart = new MimeBodyPart();
            MessageUtility.setText(getConformHTML(text, "UTF-8"), "UTF-8", "html", bodyPart);
            // bodyPart.setText(getConformHTML(text, "UTF-8"), "UTF-8", "html");
            bodyPart.setHeader(MessageHeaders.HDR_MIME_VERSION, "1.0");
            bodyPart.setHeader(MessageHeaders.HDR_CONTENT_TYPE, MimeMessageUtility.foldContentType("text/html; charset=UTF-8; name=links.html"));
            bodyPart.setHeader(MessageHeaders.HDR_CONTENT_TRANSFER_ENC, "base64");
            bodyPart.setHeader(MessageHeaders.HDR_CONTENT_DISPOSITION, MimeMessageUtility.foldContentDisposition("attachment; filename=links.html"));
            return convertPart(bodyPart, false);
        } catch (final MessagingException e) {
            throw MimeMailException.handleMessagingException(e);
        }
    } // End of createLinksAttachment()

    private String publishAttachmentAndGetPath(Map<String, Object> storeProps, MailPart attachment, List<PublicationAndInfostoreID> publications, MailAttachmentStorage attachmentStorage) throws OXException, TransactionException, OXException {
        // Store attachment
        storeProps.put("externalLocale", TransportProperties.getInstance().getExternalRecipientsLocale());
        String attachmentId = attachmentStorage.storeAttachment(attachment, StoreOperation.PUBLISH_STORE, storeProps, session);

        // Get its download URI
        DownloadUri downloadUri = attachmentStorage.getDownloadUri(attachmentId, session);

        // Remember information
        publications.add(new PublicationAndInfostoreID(downloadUri, attachmentId));
        /*
         * Return URL
         */
        return downloadUri.getDownloadUri();
    } // End of publishAttachmentAndGetPath()

    private void rollbackPublications(List<PublicationAndInfostoreID> publications, MailAttachmentStorage attachmentStorage) {
        for (final PublicationAndInfostoreID publication : publications) {
            try {
                attachmentStorage.discard(publication.attachmentId, publication.downloadUri, session);
            } catch (final OXException e) {
                LOG.error("Error while deleting stored attachment with ID \"{}\".", publication.attachmentId, e);
            }
        }
    } // End of rollbackPublications()

    private static void appendLinks(final List<LinkAndNamePair> links, final StringBuilder textBuilder) {
        for (final LinkAndNamePair pair : links) {
            final String link = pair.link;
            final char quot;
            if (link.indexOf('"') < 0) {
                quot = '"';
            } else {
                quot = '\'';
            }
            textBuilder.append("<a href=").append(quot).append(link).append(quot).append('>');
            final String name = pair.name;
            if (null != name && name.length() > 0) {
                textBuilder.append(name).append("</a><br>");
            } else {
                textBuilder.append(link).append("</a><br>");
            }
        }
    } // End of appendLinks()

    private static final class LinkAndNamePair {

        final String name;

        final String link;

        public LinkAndNamePair(final String name, final String link) {
            super();
            this.name = name;
            this.link = link;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((link == null) ? 0 : link.hashCode());
            result = prime * result + ((name == null) ? 0 : name.hashCode());
            return result;
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof LinkAndNamePair)) {
                return false;
            }
            final LinkAndNamePair other = (LinkAndNamePair) obj;
            if (link == null) {
                if (other.link != null) {
                    return false;
                }
            } else if (!link.equals(other.link)) {
                return false;
            }
            if (name == null) {
                if (other.name != null) {
                    return false;
                }
            } else if (!name.equals(other.name)) {
                return false;
            }
            return true;
        }

    } // End of LinkAndNamePair

    private static final class PublicationAndInfostoreID {

        final DownloadUri downloadUri;
        final String attachmentId;

        PublicationAndInfostoreID(DownloadUri downloadUri, String attachmentId) {
            super();
            this.downloadUri = downloadUri;
            this.attachmentId = attachmentId;
        }

    } // End of PublicationAndInfostoreID

    private static String saneProtocol(final String protocol) {
        if (protocol.endsWith("://")) {
            return protocol;
        }
        return new StringBuilder(protocol).append("://").toString();
    }

    private static boolean forcedSecure(final String hostName) {
        final ConfigurationService configurationService = ServerServiceRegistry.getInstance().getService(ConfigurationService.class);
        return (configurationService != null && configurationService.getBoolProperty(ServerConfig.Property.FORCE_HTTPS.getPropertyName(), false) && !Cookies.isLocalLan(hostName));
    }
}
