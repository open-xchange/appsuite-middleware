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

package com.openexchange.mail.json.parser;

import static com.openexchange.groupware.upload.impl.UploadUtility.getSize;
import static com.openexchange.mail.mime.converters.MimeMessageConverter.convertPart;
import static com.openexchange.mail.text.HtmlProcessing.getConformHTML;
import static com.openexchange.mail.text.HtmlProcessing.htmlFormat;
import java.io.InputStream;
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
import com.openexchange.file.storage.DefaultFile;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.FileStorageFileAccess;
import com.openexchange.file.storage.composition.IDBasedFileAccess;
import com.openexchange.file.storage.composition.IDBasedFileAccessFactory;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.i18n.MailStrings;
import com.openexchange.groupware.ldap.LdapExceptionCode;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.i18n.tools.StringHelper;
import com.openexchange.java.Streams;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.MailSessionParameterNames;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.dataobjects.compose.ComposedMailMessage;
import com.openexchange.mail.dataobjects.compose.TextBodyMailPart;
import com.openexchange.mail.mime.MessageHeaders;
import com.openexchange.mail.mime.MimeMailException;
import com.openexchange.mail.mime.utils.MimeMessageUtility;
import com.openexchange.mail.transport.TransportProvider;
import com.openexchange.mail.transport.config.TransportProperties;
import com.openexchange.mail.utils.MessageUtility;
import com.openexchange.publish.Publication;
import com.openexchange.publish.PublicationService;
import com.openexchange.publish.PublicationTarget;
import com.openexchange.publish.PublicationTargetDiscoveryService;
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

    private static final org.apache.commons.logging.Log LOG = com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(PublishAttachmentHandler.class));

    private final Session session;

    private final TransportProvider transportProvider;

    private final String protocol;

    private final String hostName;

    private final IDBasedFileAccessFactory fileAccessFactory;

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
        fileAccessFactory = ServerServiceRegistry.getInstance().getService(IDBasedFileAccessFactory.class, true);
    }

    @Override
    public void setTextPart(final TextBodyMailPart textPart) {
        this.textPart = textPart;
    }

    @Override
    public void addAttachment(final MailPart attachment) throws OXException {
        if (doAction && !exceeded) {
            final long size = attachment.getSize();
            if (size <= 0 && LOG.isDebugEnabled()) {
                LOG.debug(new com.openexchange.java.StringAllocator("Missing size: ").append(size).toString(), new Throwable());
            }
            if (uploadQuotaPerFile > 0 && size > uploadQuotaPerFile) {
                if (LOG.isDebugEnabled()) {
                    final String fileName = attachment.getFileName();
                    final OXException e =
                        MailExceptionCode.UPLOAD_QUOTA_EXCEEDED_FOR_FILE.create(
                            Long.valueOf(uploadQuotaPerFile),
                            null == fileName ? "" : fileName,
                            Long.valueOf(size));
                    LOG.debug(
                        new com.openexchange.java.StringAllocator(64).append("Per-file quota (").append(getSize(uploadQuotaPerFile, 2, false, true)).append(
                            ") exceeded. Message is going to be sent with links to publishing infostore folder.").toString(),
                        e);
                }
                exceeded = true;
            }
            /*
             * Add current file size
             */
            consumed += size;
            if (uploadQuota > 0 && consumed > uploadQuota) {
                if (LOG.isDebugEnabled()) {
                    final OXException e = MailExceptionCode.UPLOAD_QUOTA_EXCEEDED.create(Long.valueOf(uploadQuota));
                    LOG.debug(
                        new com.openexchange.java.StringAllocator(64).append("Overall quota (").append(getSize(uploadQuota, 2, false, true)).append(
                            ") exceeded. Message is going to be sent with links to publishing infostore folder.").toString(),
                        e);
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
        /*
         * Handle exceeded quota through generating appropriate publication links
         */
        final List<PublicationAndInfostoreID> publications = new ArrayList<PublicationAndInfostoreID>(attachments.size());
        /*
         * Check for folder ID
         */
        final String key = MailSessionParameterNames.getParamPublishingInfostoreFolderID();
        if (!session.containsParameter(key)) {
            final Throwable t = new Throwable("Missing folder ID of publishing infostore folder.");
            throw MailExceptionCode.SEND_FAILED_UNKNOWN.create(t, new Object[0]);
        }
        final int folderId = ((Integer) session.getParameter(key)).intValue();
        final Context ctx = getContext();
        final PublicationTarget target;
        final PublicationService publisher;
        /*
         * Get discovery service
         */
        final PublicationTargetDiscoveryService discoveryService =
            ServerServiceRegistry.getInstance().getService(PublicationTargetDiscoveryService.class, true);
        /*
         * Get discovery service's target
         */
        target = discoveryService.getTarget("com.openexchange.publish.online.infostore.document");
        if (null == target) {
            LOG.warn("Missing publication target for ID \"com.openexchange.publish.online.infostore.document\".\nThrowing quota-exceeded exception instead.");
            throw MailExceptionCode.UPLOAD_QUOTA_EXCEEDED.create(Long.valueOf(uploadQuota));
        }
        /*
         * ... and in turn target's publication service
         */
        publisher = target.getPublicationService();
        try {
        	warnings.add(MailExceptionCode.USED_PUBLISHING_FEATURE.create());
            return generateComposedMails0(source, publications, folderId, target, publisher, ctx);
        } catch (final OXException e) {
            /*
             * Rollback of publications
             */
            rollbackPublications(publications, publisher);
            /*
             * Re-throw exception
             */
            throw e;
        }
    }

    private ComposedMailMessage[] generateComposedMails0(final ComposedMailMessage source, final List<PublicationAndInfostoreID> publications, final int folderId, final PublicationTarget target, final PublicationService publisher, final Context ctx) throws OXException {
        final List<LinkAndNamePair> links = new ArrayList<LinkAndNamePair>(attachments.size());
        /*
         * Generate publication link for each attachment
         */
        final StringBuilder linkBuilder = new StringBuilder(256);
        for (final MailPart attachment : attachments) {
            /*
             * Generate publish URL: "/publications/infostore/documents/12abead21498754abcfde"
             */
            final String path = publishAttachmentAndGetPath(attachment, folderId, ctx, publications, target, publisher);
            /*
             * Add to list
             */
            linkBuilder.setLength(0);
            linkBuilder.append(isEmpty(protocol) ? (forcedSecure(hostName) ? "https://" : "http://") : saneProtocol(protocol)).append(hostName).append(path);
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
            elapsedDate = new Date(System.currentTimeMillis() + TransportProperties.getInstance().getPublishedDocumentTimeToLive());
        }
        final UserService userService = ServerServiceRegistry.getInstance().getService(UserService.class);
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
                    externalMessage =
                        generateExternalVersion(
                            source,
                            ctx,
                            links,
                            TransportProperties.getInstance().isProvideLinksInAttachment(),
                            elapsedDate);
                }
                externalMessage.addRecipient(address);
            } else {
                // Internal user
                final Locale locale = user.getLocale();
                ComposedMailMessage localedMessage = internalMessages.get(locale);
                if (null == localedMessage) {
                    localedMessage =
                        generateInternalVersion(
                            source,
                            ctx,
                            links,
                            TransportProperties.getInstance().isProvideLinksInAttachment(),
                            elapsedDate,
                            locale);
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
            mails.add(generateInternalVersion(
                source,
                ctx,
                links,
                TransportProperties.getInstance().isProvideLinksInAttachment(),
                elapsedDate,
                getSessionUser().getLocale()));
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
            final com.openexchange.java.StringAllocator textBuilder = new com.openexchange.java.StringAllocator(256 * links.size());
            textBuilder.append(htmlFormat(stringHelper.getString(MailStrings.PUBLISHED_ATTACHMENTS_PREFIX))).append("<br>");
            appendLinks(links, textBuilder);
            internalVersion.addEnclosedPart(createLinksAttachment(textBuilder.toString()));
        } else {
            final String text = (String) textPart.getContent();
            final com.openexchange.java.StringAllocator textBuilder = new com.openexchange.java.StringAllocator(text.length() + 512);
            textBuilder.append(htmlFormat(stringHelper.getString(MailStrings.PUBLISHED_ATTACHMENTS_PREFIX))).append("<br>");
            appendLinks(links, textBuilder);
            if (elapsedDate != null) {
                textBuilder.append(
                    htmlFormat(PATTERN_DATE.matcher(stringHelper.getString(MailStrings.PUBLISHED_ATTACHMENTS_APPENDIX)).replaceFirst(
                        DateFormat.getDateInstance(DateFormat.LONG, locale).format(elapsedDate)))).append("<br><br>");
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
                final com.openexchange.java.StringAllocator textBuilder = new com.openexchange.java.StringAllocator(256 * links.size());
                textBuilder.append(htmlFormat(stringHelper.getString(MailStrings.PUBLISHED_ATTACHMENTS_PREFIX))).append("<br>");
                appendLinks(links, textBuilder);
                externalVersion.addEnclosedPart(createLinksAttachment(textBuilder.toString()));
            } else {
                final String text = (String) textPart.getContent();
                final com.openexchange.java.StringAllocator textBuilder = new com.openexchange.java.StringAllocator(text.length() + 512);
                textBuilder.append(htmlFormat(stringHelper.getString(MailStrings.PUBLISHED_ATTACHMENTS_PREFIX))).append("<br>");
                appendLinks(links, textBuilder);
                if (elapsedDate != null) {
                    textBuilder.append(
                        htmlFormat(PATTERN_DATE.matcher(stringHelper.getString(MailStrings.PUBLISHED_ATTACHMENTS_APPENDIX)).replaceFirst(
                            DateFormat.getDateInstance(DateFormat.LONG, locale).format(elapsedDate)))).append("<br><br>");
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
        return UserStorage.getStorageUser(session.getUserId(), context).getLocale();
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
            bodyPart.setHeader(
                MessageHeaders.HDR_CONTENT_TYPE,
                MimeMessageUtility.foldContentType("text/html; charset=UTF-8; name=links.html"));
            bodyPart.setHeader(MessageHeaders.HDR_CONTENT_TRANSFER_ENC, "base64");
            bodyPart.setHeader(
                MessageHeaders.HDR_CONTENT_DISPOSITION,
                MimeMessageUtility.foldContentDisposition("attachment; filename=links.html"));
            return convertPart(bodyPart, false);
        } catch (final MessagingException e) {
            throw MimeMailException.handleMessagingException(e);
        }
    } // End of createLinksAttachment()

    private String publishAttachmentAndGetPath(final MailPart attachment, final int folderId, final Context ctx, final List<PublicationAndInfostoreID> publications, final PublicationTarget target, final PublicationService publisher) throws OXException, TransactionException, OXException {
        /*
         * Create document meta data for current attachment
         */
        String name = attachment.getFileName();
        if (name == null) {
            name = "attachment";
        }
        final File file = new DefaultFile();
        file.setId(FileStorageFileAccess.NEW);
        file.setFolderId(String.valueOf(folderId));
        file.setFileName(name);
        file.setFileMIMEType(attachment.getContentType().toString());
        file.setTitle(name);
        /*
         * Put attachment's document to dedicated infostore folder
         */
        final IDBasedFileAccess fileAccess = fileAccessFactory.createAccess(session);
        boolean retry = true;
        int count = 1;
        final StringBuilder hlp = new StringBuilder(16);
        while (retry) {
            /*
             * Get attachment's input stream
             */
            final InputStream in = attachment.getInputStream();
            try {
                /*
                 * Start InfoStore transaction
                 */
                fileAccess.startTransaction();
                try {
                    fileAccess.saveDocument(file, in, FileStorageFileAccess.DISTANT_FUTURE);
                    fileAccess.commit();
                    retry = false;
                } catch (final OXException x) {
                    fileAccess.rollback();
                    if (!x.isPrefix("IFO")) {
                        throw x;
                    }
                    if (441 != x.getCode()) {
                        throw x;
                    }
                    /*
                     * Duplicate document name, thus retry with a new name
                     */
                    hlp.setLength(0);
                    final int pos = name.lastIndexOf('.');
                    final String newName;
                    if (pos >= 0) {
                        newName =
                            hlp.append(name.substring(0, pos)).append("_(").append(++count).append(')').append(name.substring(pos)).toString();
                    } else {
                        newName = hlp.append(name).append("_(").append(++count).append(')').toString();
                    }
                    file.setFileName(newName);
                    file.setTitle(newName);
                } catch (final RuntimeException e) {
                    fileAccess.rollback();
                    throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
                } finally {
                    fileAccess.finish();
                }
            } finally {
                Streams.close(in);
            }
        }
        /*
         * Generate publication for current attachment
         */
        final Publication publication = new Publication();
        publication.setModule("infostore/object");
        publication.setEntityId(String.valueOf(file.getId()));
        publication.setContext(ctx);
        publication.setUserId(session.getUserId());
        /*
         * Set target
         */
        publication.setTarget(target);
        /*
         * ... and publish
         */
        publisher.create(publication);
        /*
         * Remember publication in provided list
         */
        publications.add(new PublicationAndInfostoreID(publication, file.getId()));
        /*
         * Return URL
         */
        return (String) publication.getConfiguration().get("url");
    } // End of publishAttachmentAndGetPath()

    private void rollbackPublications(final List<PublicationAndInfostoreID> publications, final PublicationService publisher) {
        final IDBasedFileAccess fileAccess = fileAccessFactory.createAccess(session);
        final long timestamp = System.currentTimeMillis();
        final List<String> arr = new ArrayList<String>(1);
        for (final PublicationAndInfostoreID publication : publications) {
            try {
                publisher.delete(publication.publication);
            } catch (final OXException e) {
                LOG.error(
                    new com.openexchange.java.StringAllocator("Publication with ID \"").append(publication.publication.getId()).append(" could not be roll-backed.").toString(),
                    e);
            }
            try {
                fileAccess.startTransaction();
                try {
                    arr.set(0, publication.infostoreId);
                    fileAccess.removeDocument(arr, timestamp);
                    fileAccess.commit();
                } catch (final OXException x) {
                    fileAccess.rollback();
                    throw x;
                } finally {
                    fileAccess.finish();
                }
            } catch (final OXException e) {
                LOG.error(
                    new com.openexchange.java.StringAllocator("Transaction error while deleting infostore document with ID \"").append(publication.infostoreId).append(
                        "\" failed.").toString(),
                    e);
            }
        }
    } // End of rollbackPublications()

    private static void appendLinks(final List<LinkAndNamePair> links, final com.openexchange.java.StringAllocator textBuilder) {
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

        final Publication publication;
        final String infostoreId;

        PublicationAndInfostoreID(final Publication publication, final String infostoreId) {
            super();
            this.publication = publication;
            this.infostoreId = infostoreId;
        }

    } // End of PublicationAndInfostoreID

    private static String saneProtocol(final String protocol) {
        if (protocol.endsWith("://")) {
            return protocol;
        }
        return new com.openexchange.java.StringAllocator(protocol).append("://").toString();
    }

    private static boolean forcedSecure(final String hostName) {
        final ConfigurationService configurationService = ServerServiceRegistry.getInstance().getService(ConfigurationService.class);
        return (configurationService != null && configurationService.getBoolProperty(ServerConfig.Property.FORCE_HTTPS.getPropertyName(), false) && !Cookies.isLocalLan(hostName));
    }

    private static boolean isEmpty(final String string) {
        if (null == string) {
            return true;
        }
        final int len = string.length();
        boolean isWhitespace = true;
        for (int i = 0; isWhitespace && i < len; i++) {
            isWhitespace = com.openexchange.java.Strings.isWhitespace(string.charAt(i));
        }
        return isWhitespace;
    }

}
