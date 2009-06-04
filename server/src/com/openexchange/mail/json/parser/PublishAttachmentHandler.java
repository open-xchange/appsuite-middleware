
package com.openexchange.mail.json.parser;

import static com.openexchange.groupware.upload.impl.UploadUtility.getSize;
import static com.openexchange.mail.mime.converters.MIMEMessageConverter.convertPart;
import static com.openexchange.mail.mime.utils.MIMEMessageUtility.fold;
import static com.openexchange.mail.text.HTMLProcessing.getConformHTML;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import com.openexchange.ajax.Infostore;
import com.openexchange.api2.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextException;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.infostore.InfostoreException;
import com.openexchange.groupware.infostore.InfostoreFacade;
import com.openexchange.groupware.infostore.database.impl.DocumentMetadataImpl;
import com.openexchange.groupware.ldap.LdapException;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserException;
import com.openexchange.groupware.tx.TransactionException;
import com.openexchange.groupware.upload.impl.UploadUtility;
import com.openexchange.mail.MailException;
import com.openexchange.mail.MailSessionParameterNames;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.dataobjects.compose.ComposedMailMessage;
import com.openexchange.mail.dataobjects.compose.TextBodyMailPart;
import com.openexchange.mail.mime.MIMEMailException;
import com.openexchange.mail.mime.MessageHeaders;
import com.openexchange.mail.transport.TransportProvider;
import com.openexchange.mail.transport.config.TransportProperties;
import com.openexchange.publish.Publication;
import com.openexchange.publish.PublicationException;
import com.openexchange.publish.PublicationService;
import com.openexchange.publish.PublicationTarget;
import com.openexchange.publish.PublicationTargetDiscoveryService;
import com.openexchange.server.ServiceException;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;
import com.openexchange.user.UserService;

/**
 * {@link PublishAttachmentHandler} - An {@link IAttachmentHandler attachment handler} that publishes attachments on exceeded quota (either
 * overall or per-file quota).
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
final class PublishAttachmentHandler extends AbstractAttachmentHandler {

    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(PublishAttachmentHandler.class);

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
     * @throws MailException If initialization fails
     */
    public PublishAttachmentHandler(final Session session, final TransportProvider transportProvider, final String protocol, final String hostName) throws MailException {
        super(session);
        this.protocol = protocol;
        this.hostName = hostName;
        this.transportProvider = transportProvider;
        this.session = session;
    }

    public void setTextPart(final TextBodyMailPart textPart) {
        this.textPart = textPart;
    }

    public void addAttachment(final MailPart attachment) throws MailException {
        if (doAction && !exceeded) {
            final long size = attachment.getSize();
            if (size <= 0 && LOG.isDebugEnabled()) {
                LOG.debug(new StringBuilder("Missing size: ").append(size).toString(), new Throwable());
            }
            if (uploadQuotaPerFile > 0 && size > uploadQuotaPerFile) {
                if (LOG.isDebugEnabled()) {
                    final String fileName = attachment.getFileName();
                    final MailException e = new MailException(MailException.Code.UPLOAD_QUOTA_EXCEEDED_FOR_FILE, UploadUtility.getSize(
                        uploadQuotaPerFile,
                        2,
                        false,
                        true), null == fileName ? "" : fileName, UploadUtility.getSize(size, 2, false, true));
                    LOG.debug(new StringBuilder(64).append("Per-file quota (").append(getSize(uploadQuotaPerFile, 2, false, true)).append(
                        ") exceeded. Message is going to be sent with links to publishing infostore folder.").toString(), e);
                }
                exceeded = true;
            } else {
                /*
                 * Add current file size
                 */
                consumed += size;
                if (uploadQuota > 0 && consumed > uploadQuota) {
                    if (LOG.isDebugEnabled()) {
                        final MailException e = new MailException(MailException.Code.UPLOAD_QUOTA_EXCEEDED, UploadUtility.getSize(
                            uploadQuota,
                            2,
                            false,
                            true));
                        LOG.debug(new StringBuilder(64).append("Overall quota (").append(getSize(uploadQuota, 2, false, true)).append(
                            ") exceeded. Message is going to be sent with links to publishing infostore folder.").toString(), e);
                    }
                    exceeded = true;
                }
            }
        }
        attachments.add(attachment);
    }

    public ComposedMailMessage[] generateComposedMails(final ComposedMailMessage source) throws MailException {
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
            throw new MailException(MailException.Code.SEND_FAILED_UNKNOWN, t, new Object[0]);
        }
        final int folderId = ((Integer) session.getParameter(key)).intValue();
        final Context ctx = getContext();
        final PublicationTarget target;
        final PublicationService publisher;
        try {
            /*
             * Get discovery service
             */
            final PublicationTargetDiscoveryService discoveryService = ServerServiceRegistry.getInstance().getService(
                PublicationTargetDiscoveryService.class,
                true);
            /*
             * Get discovery service's target
             */
            target = discoveryService.getTarget("com.openexchange.publish.online.infostore.document");
            if (null == target) {
                final Throwable t = new Throwable("Missing publication target for ID: com.openexchange.publish.online.infostore.document");
                throw new MailException(MailException.Code.SEND_FAILED_UNKNOWN, t, new Object[0]);
            }
            /*
             * ... and in turn target's publication service
             */
            publisher = target.getPublicationService();
        } catch (final ServiceException e) {
            throw new MailException(e);
        } catch (final PublicationException e) {
            throw new MailException(e);
        }
        try {
            return generateComposedMails0(source, publications, folderId, target, publisher, ctx);
        } catch (final MailException e) {
            /*
             * Rollback of publications
             */
            rollbackPublications(publications, publisher, ctx);
            /*
             * Re-throw exception
             */
            throw e;
        }
    }

    private ComposedMailMessage[] generateComposedMails0(final ComposedMailMessage source, final List<PublicationAndInfostoreID> publications, final int folderId, final PublicationTarget target, final PublicationService publisher, final Context ctx) throws MailException {
        final List<LinkAndNamePair> links = new ArrayList<LinkAndNamePair>(attachments.size());
        try {
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
                links.add(new LinkAndNamePair(attachment.getFileName(), linkBuilder.append(protocol).append("://").append(hostName).append(
                    path).toString()));
            }
        } catch (final PublicationException e) {
            throw new MailException(e);
        } catch (final TransactionException e) {
            throw new MailException(e);
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
        final UserService userService = ServerServiceRegistry.getInstance().getService(UserService.class);
        final List<ComposedMailMessage> mails = new ArrayList<ComposedMailMessage>(2);
        ComposedMailMessage internal = null;
        ComposedMailMessage external = null;
        for (final InternetAddress address : addresses) {
            User user = null;
            try {
                user = userService.searchUser(address.getAddress(), ctx);
            } catch (final UserException e) {
                /*
                 * Unfortunately UserService.searchUser() throws an exception if no user could be found matching given email address.
                 * Therefore check for this special error code and throw an exception if it is not equal.
                 */
                if (LdapException.Code.NO_USER_BY_MAIL.getDetailNumber() != e.getDetailNumber()) {
                    throw new MailException(e);
                }
            }
            if (null == user) {
                // External
                if (null == external) {
                    external = generateExternalVersion(source, ctx, links, TransportProperties.getInstance().isProvideLinksInAttachment());
                    mails.add(external);
                }
                external.addRecipient(address);
            } else {
                // Internal
                if (null == internal) {
                    internal = generateInternalVersion(source, ctx, links, TransportProperties.getInstance().isProvideLinksInAttachment());
                    mails.add(internal);
                }
                internal.addRecipient(address);
            }
        }
        /*
         * Return mail versions
         */
        return mails.toArray(new ComposedMailMessage[mails.size()]);
    }

    private Context getContext() throws MailException {
        if (session instanceof ServerSession) {
            return ((ServerSession) session).getContext();
        }
        try {
            return ContextStorage.getStorageContext(session.getContextId());
        } catch (final ContextException e) {
            throw new MailException(e);
        }
    }

    private ComposedMailMessage generateInternalVersion(final ComposedMailMessage source, final Context ctx, final List<LinkAndNamePair> links, final boolean appendLinksAsAttachment) throws MailException {
        final ComposedMailMessage internalVersion = copyOf(source, ctx);
        final TextBodyMailPart textPart = this.textPart.copy();
        if (appendLinksAsAttachment) {
            // Apply text part as it is
            internalVersion.setBodyPart(textPart);
            // Generate text for attachment
            final StringBuilder textBuilder = new StringBuilder(256 * links.size());
            textBuilder.append("Save file(s) on your machine. It may not be available to view next time.<br />");
            appendLinks(links, textBuilder);
            internalVersion.addEnclosedPart(createLinksAttachment(textBuilder.toString()));
        } else {
            final String text = (String) textPart.getContent();
            final StringBuilder textBuilder = new StringBuilder(text.length() + 512);
            textBuilder.append("Save file(s) on your machine. It may not be available to view next time.<br />");
            appendLinks(links, textBuilder);
            textBuilder.append("Find original text below:<br /><br />");
            textBuilder.append(text);
            textPart.setText(textBuilder.toString());
            internalVersion.setBodyPart(textPart);
        }
        return internalVersion;
    }

    private ComposedMailMessage generateExternalVersion(final ComposedMailMessage source, final Context ctx, final List<LinkAndNamePair> links, final boolean appendLinksAsAttachment) throws MailException {
        final ComposedMailMessage externalVersion = copyOf(source, ctx);
        final TextBodyMailPart textPart = this.textPart.copy();
        if (TransportProperties.getInstance().isSendAttachmentToExternalRecipients()) {
            externalVersion.setBodyPart(textPart);
            for (final MailPart attachment : attachments) {
                externalVersion.addEnclosedPart(attachment);
            }
        } else {
            if (appendLinksAsAttachment) {
                // Apply text part as it is
                externalVersion.setBodyPart(textPart);
                // Generate text for attachment
                final StringBuilder textBuilder = new StringBuilder(256 * links.size());
                textBuilder.append("Save file(s) on your machine. It may not be available to view next time.<br />");
                appendLinks(links, textBuilder);
                externalVersion.addEnclosedPart(createLinksAttachment(textBuilder.toString()));
            } else {
                final String text = (String) textPart.getContent();
                final StringBuilder textBuilder = new StringBuilder(text.length() + 512);
                textBuilder.append("Save file(s) on your machine. It may not be available to view next time.<br />");
                appendLinks(links, textBuilder);
                textBuilder.append("Find original text below:<br /><br />");
                textBuilder.append(text);
                textPart.setText(textBuilder.toString());
                externalVersion.setBodyPart(textPart);
            }
        }
        return externalVersion;
    }

    private ComposedMailMessage copyOf(final ComposedMailMessage source, final Context ctx) throws MailException {
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

    private MailPart createLinksAttachment(final String text) throws MailException, MIMEMailException {
        try {
            final MimeBodyPart bodyPart = new MimeBodyPart();
            bodyPart.setText(getConformHTML(text, "UTF-8"), "UTF-8", "html");
            bodyPart.setHeader(MessageHeaders.HDR_MIME_VERSION, "1.0");
            bodyPart.setHeader(MessageHeaders.HDR_CONTENT_TYPE, fold(14, "text/html; charset=UTF-8; name=links.html"));
            bodyPart.setHeader(MessageHeaders.HDR_CONTENT_TRANSFER_ENC, "base64");
            bodyPart.setHeader(MessageHeaders.HDR_CONTENT_DISPOSITION, fold(21, "attachment; filename=links.html"));
            return convertPart(bodyPart, false);
        } catch (final MessagingException e) {
            throw MIMEMailException.handleMessagingException(e);
        }
    } // End of createLinksAttachment()

    private String publishAttachmentAndGetPath(final MailPart attachment, final int folderId, final Context ctx, final List<PublicationAndInfostoreID> publications, final PublicationTarget target, final PublicationService publisher) throws MailException, TransactionException, PublicationException {
        /*
         * Create document meta data for current attachment
         */
        final String name = attachment.getFileName();
        final DocumentMetadata documentMetadata = new DocumentMetadataImpl();
        documentMetadata.setId(InfostoreFacade.NEW);
        documentMetadata.setFolderId(folderId);
        documentMetadata.setFileName(name);
        documentMetadata.setFileMIMEType(attachment.getContentType().toString());
        documentMetadata.setTitle(name);
        /*
         * Put attachment's document to dedicated infostore folder
         */
        final InfostoreFacade infostoreFacade = Infostore.FACADE;
        final ServerSession serverSession;
        if (session instanceof ServerSession) {
            serverSession = (ServerSession) session;
        } else {
            serverSession = new ServerSessionAdapter(session, ctx);
        }
        boolean retry = true;
        int count = 1;
        final StringBuilder hlp = new StringBuilder(16);
        while (retry) {
            infostoreFacade.startTransaction();
            try {
                infostoreFacade.saveDocument(documentMetadata, attachment.getInputStream(), Long.MAX_VALUE, serverSession);
                infostoreFacade.commit();
                retry = false;
            } catch (final InfostoreException x) {
                infostoreFacade.rollback();
                if (441 == x.getDetailNumber()) {
                    /*
                     * Duplicate document name, thus retry with a new name
                     */
                    hlp.setLength(0);
                    final String newName = hlp.append(name).append(" (").append(++count).append(')').toString();
                    documentMetadata.setFileName(newName);
                    documentMetadata.setTitle(newName);
                } else {
                    throw new MailException(x);
                }
            } catch (final OXException x) {
                infostoreFacade.rollback();
                throw new MailException(x);
            } finally {
                infostoreFacade.finish();
            }
        }
        /*
         * Generate publication for current attachment
         */
        final Publication publication = new Publication();
        publication.setModule("infostore/object");
        publication.setEntityId(String.valueOf(documentMetadata.getId()));
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
        publications.add(new PublicationAndInfostoreID(publication, documentMetadata.getId()));
        /*
         * Return URL
         */
        return (String) publication.getConfiguration().get("url");
    } // End of publishAttachmentAndGetPath()

    private void rollbackPublications(final List<PublicationAndInfostoreID> publications, final PublicationService publisher, final Context ctx) {
        /*
         * Remove publication one-by-one
         */
        final InfostoreFacade infostoreFacade = Infostore.FACADE;
        final ServerSession serverSession;
        if (session instanceof ServerSession) {
            serverSession = (ServerSession) session;
        } else {
            serverSession = new ServerSessionAdapter(session, ctx);
        }
        final long timestamp = System.currentTimeMillis();
        final int[] arr = new int[1];
        for (final PublicationAndInfostoreID publication : publications) {
            try {
                publisher.delete(publication.publication);
            } catch (final PublicationException e) {
                LOG.error(new StringBuilder("Publication with ID \"").append(publication.publication.getId()).append(
                    " could not be roll-backed.").toString(), e);
            }
            try {
                infostoreFacade.startTransaction();
                try {
                    arr[0] = publication.infostoreId;
                    infostoreFacade.removeDocument(arr, timestamp, serverSession);
                    infostoreFacade.commit();
                } catch (final OXException x) {
                    infostoreFacade.rollback();
                    throw x;
                } finally {
                    infostoreFacade.finish();
                }
            } catch (final TransactionException e) {
                LOG.error(new StringBuilder("Transaction error while deleting infostore document with ID \"").append(
                    publication.infostoreId).append("\" failed.").toString(), e);
            } catch (final OXException e) {
                LOG.error(
                    new StringBuilder("Deleting infostore document with ID \"").append(publication.infostoreId).append("\" failed.").toString(),
                    e);
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
                textBuilder.append(name).append("</a><br />");
            } else {
                textBuilder.append(link).append("</a><br />");
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
            if (obj == null) {
                return false;
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

        final int infostoreId;

        public PublicationAndInfostoreID(final Publication publication, final int infostoreId) {
            super();
            this.publication = publication;
            this.infostoreId = infostoreId;
        }

    } // End of PublicationAndInfostoreID

}
