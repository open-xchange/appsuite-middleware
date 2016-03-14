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

package com.openexchange.mail.json.parser;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.mail.internet.InternetAddress;
import com.openexchange.config.ConfigurationService;
import com.openexchange.configuration.ServerConfig;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.mail.attachment.storage.DefaultMailAttachmentStorageRegistry;
import com.openexchange.mail.attachment.storage.DownloadUri;
import com.openexchange.mail.attachment.storage.MailAttachmentStorage;
import com.openexchange.mail.attachment.storage.StoreOperation;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.dataobjects.compose.ComposedMailMessage;
import com.openexchange.mail.transport.TransportProvider;
import com.openexchange.mail.transport.config.TransportProperties;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.tools.servlet.http.Cookies;
import com.openexchange.tx.TransactionException;

/**
 * {@link PublishAttachmentHandler} - An {@link IAttachmentHandler attachment handler} that publishes attachments on exceeded quota (either
 * overall or per-file quota).
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class PublishAttachmentHandler extends DefaultAttachmentHandler {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(PublishAttachmentHandler.class);

    private final MailAttachmentStorage attachmentStorage;
    private List<LinkedPublication> createdPublications;

    public PublishAttachmentHandler(Session session, TransportProvider transportProvider, String protocol, String hostName) throws OXException {
        super(session, transportProvider, protocol, hostName);
        this.attachmentStorage = DefaultMailAttachmentStorageRegistry.getInstance().getMailAttachmentStorage();
    }

    @Override
    protected List<LinkedAttachment> publishAttachments(ComposedMailMessage source, List<OXException> warnings) throws OXException {

        // Get attachment storage
        MailAttachmentStorage attachmentStorage = DefaultMailAttachmentStorageRegistry.getInstance().getMailAttachmentStorage();

        List<LinkedAttachment> links = new ArrayList<LinkedAttachment>(attachments.size());
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
        for (MailPart attachment : attachments) {
            /*
             * Generate publish URL: "/publications/infostore/documents/12abead21498754abcfde" & add to list
             */
            LinkedPublication linkedPublication = publishAttachmentAndGetPath(storeProps, attachment, attachmentStorage);
            if (null != createdPublications) {
                createdPublications.add(linkedPublication);
            }
            links.add(linkedPublication);
        }
        return links;
    }

    private LinkedPublication publishAttachmentAndGetPath(Map<String, Object> storeProps, MailPart attachment, MailAttachmentStorage attachmentStorage) throws OXException, TransactionException, OXException {
        // Store attachment
        storeProps.put("externalLocale", TransportProperties.getInstance().getExternalRecipientsLocale());
        String attachmentId = attachmentStorage.storeAttachment(attachment, StoreOperation.PUBLISH_STORE, storeProps, session);

        // Get its download URI
        DownloadUri downloadUri = attachmentStorage.getDownloadUri(attachmentId, session);

        // Remember information
        return new LinkedPublication(protocol, hostName, attachment.getFileName(), downloadUri, attachmentId);
    } // End of publishAttachmentAndGetPath()

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

    @Override
    public void startTransaction() throws OXException {
        createdPublications = new ArrayList<LinkedPublication>();
    }

    @Override
    public void commit() throws OXException {
        // no
    }

    @Override
    public void rollback() throws OXException {
        if (null != createdPublications && 0 < createdPublications.size()) {
            for (final LinkedPublication publication : createdPublications) {
                try {
                    attachmentStorage.discard(publication.getAttachmentID(), publication.getDownloadUri(), session);
                } catch (final OXException e) {
                    LOG.error("Error while deleting stored attachment with ID \"{}\".", publication.getAttachmentID(), e);
                }
            }
        }
    }

    @Override
    public void finish() throws OXException {
        // no
    }

    private static final class LinkedPublication implements LinkedAttachment {

        private final String protocol;
        private final String hostName;
        private final String name;
        private final DownloadUri downloadUri;
        private final String attachmentID;

        public LinkedPublication(String protocol, String hostname, String name, DownloadUri downloadUri, String attachmentID) {
            super();
            this.protocol = protocol;
            this.hostName = hostname;
            this.name = name;
            this.downloadUri = downloadUri;;
            this.attachmentID = attachmentID;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getLink(InternetAddress recipient) {
            if (downloadUri.getDownloadUri().startsWith("http")) {
                return downloadUri.getDownloadUri();
            } else {
                return new StringBuilder().append(Strings.isEmpty(protocol) ? (forcedSecure(hostName) ? "https://" : "http://") : saneProtocol(protocol))
                    .append(hostName).append(downloadUri.getDownloadUri()).toString();
            }
        }

        public String getAttachmentID() {
            return attachmentID;
        }

        public DownloadUri getDownloadUri() {
            return downloadUri;
        }

    }

}
