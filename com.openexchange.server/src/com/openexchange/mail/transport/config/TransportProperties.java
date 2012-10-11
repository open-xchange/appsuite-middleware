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

package com.openexchange.mail.transport.config;

import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;
import com.openexchange.config.ConfigurationService;
import com.openexchange.i18n.LocaleTools;
import com.openexchange.server.services.ServerServiceRegistry;

/**
 * {@link TransportProperties}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class TransportProperties implements ITransportProperties {

    private static final org.apache.commons.logging.Log LOG = com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(TransportProperties.class));

    private static final TransportProperties instance = new TransportProperties();

    /**
     * Gets the singleton instance of {@link TransportProperties}
     *
     * @return The singleton instance of {@link TransportProperties}
     */
    public static TransportProperties getInstance() {
        return instance;
    }

    private final AtomicBoolean loaded;

    /*-
     * Fields for global properties
     */

    private int referencedPartLimit;

    private String defaultTransportProvider;

    private boolean publishOnExceededQuota;

    private String publishingInfostoreFolder;

    private boolean publishPrimaryAccountOnly;

    private boolean sendAttachmentToExternalRecipients;

    private boolean provideLinksInAttachment;

    private long publishedDocumentTimeToLive;

    private Locale externalRecipientsLocale;

    /**
     * Initializes a new {@link TransportProperties}
     */
    private TransportProperties() {
        super();
        loaded = new AtomicBoolean();
    }

    /**
     * Exclusively loads the global transport properties
     */
    void loadProperties() {
        if (!loaded.get()) {
            synchronized (loaded) {
                if (!loaded.get()) {
                    loadProperties0();
                    loaded.set(true);
                }
            }
        }
    }

    /**
     * Exclusively resets the global transport properties
     */
    void resetProperties() {
        if (loaded.get()) {
            synchronized (loaded) {
                if (loaded.get()) {
                    resetFields();
                    loaded.set(false);
                }
            }
        }
    }

    private void resetFields() {
        referencedPartLimit = 0;
        publishingInfostoreFolder = null;
        publishOnExceededQuota = false;
        publishPrimaryAccountOnly = true;
        sendAttachmentToExternalRecipients = false;
        provideLinksInAttachment = false;
        publishedDocumentTimeToLive = 604800000L;
        externalRecipientsLocale = null;
    }

    private void loadProperties0() {
        final StringBuilder logBuilder = new StringBuilder(1024);
        logBuilder.append("\nLoading global transport properties...\n");

        final ConfigurationService configuration = ServerServiceRegistry.getInstance().getService(ConfigurationService.class);

        {
            final String referencedPartLimitStr = configuration.getProperty(
                "com.openexchange.mail.transport.referencedPartLimit",
                "1048576").trim();
            try {
                referencedPartLimit = Integer.parseInt(referencedPartLimitStr);
                logBuilder.append("\tReferenced Part Limit: ").append(referencedPartLimit).append('\n');
            } catch (final NumberFormatException e) {
                referencedPartLimit = 1048576;
                logBuilder.append("\tReferenced Part Limit: Invalid value \"").append(referencedPartLimitStr).append(
                    "\". Setting to fallback ").append(referencedPartLimit).append('\n');

            }
        }

        {
            final String defaultTransProvStr = configuration.getProperty("com.openexchange.mail.defaultTransportProvider", "smtp").trim();
            defaultTransportProvider = defaultTransProvStr;
            logBuilder.append("\tDefault Transport Provider: ").append(defaultTransportProvider).append('\n');
        }

        {
            final String tmp = configuration.getProperty("com.openexchange.mail.transport.enablePublishOnExceededQuota", "false").trim();
            publishOnExceededQuota = Boolean.parseBoolean(tmp);
            logBuilder.append("\tPublish On Exceeded Quota: ").append(publishOnExceededQuota).append('\n');
        }

        {
            final String tmp = configuration.getProperty(
                "com.openexchange.mail.transport.publishingPublicInfostoreFolder",
                "Email attachments").trim();
            publishingInfostoreFolder = tmp;
            logBuilder.append("\tPublishing Infostore Folder Name: \"").append(publishingInfostoreFolder).append('"').append('\n');
        }

        {
            final String tmp = configuration.getProperty("com.openexchange.mail.transport.publishPrimaryAccountOnly", "true").trim();
            publishPrimaryAccountOnly = Boolean.parseBoolean(tmp);
            logBuilder.append("\tPublish Primary Account Only: ").append(publishPrimaryAccountOnly).append('\n');
        }

        {
            final String tmp = configuration.getProperty("com.openexchange.mail.transport.sendAttachmentToExternalRecipients", "false").trim();
            sendAttachmentToExternalRecipients = Boolean.parseBoolean(tmp);
            logBuilder.append("\tSend Attachment to External Recipients: ").append(sendAttachmentToExternalRecipients).append('\n');
        }

        {
            final String tmp = configuration.getProperty("com.openexchange.mail.transport.provideLinksInAttachment", "false").trim();
            provideLinksInAttachment = Boolean.parseBoolean(tmp);
            logBuilder.append("\tProvide Links In Attachment: ").append(provideLinksInAttachment).append('\n');
        }

        {
            final String tmp = configuration.getProperty("com.openexchange.mail.transport.publishedDocumentTimeToLive", "604800000").trim();
            try {
                publishedDocumentTimeToLive = Long.parseLong(tmp);
            } catch (final NumberFormatException e) {
                LOG.warn(
                    new StringBuilder("Value of property \"com.openexchange.mail.transport.publishedDocumentTimeToLive\" is not a number: ").append(
                        tmp).append(". Using fallback 604800000 instead.").toString(),
                    e);
                publishedDocumentTimeToLive = 604800000L;
            }
            logBuilder.append("\tPublished Document Time-to-Live: ").append(publishedDocumentTimeToLive).append('\n');
        }

        {
            final String tmp = configuration.getProperty("com.openexchange.mail.transport.externalRecipientsLocale", "user-defined").trim();
            if ("user-defined".equalsIgnoreCase(tmp)) {
                externalRecipientsLocale = null;
                logBuilder.append("\tExternal Recipients Locale: ").append("user-defined").append('\n');
            } else {
                try {
                    externalRecipientsLocale = LocaleTools.getLocale(tmp);
                } catch (final Exception e) {
                    LOG.warn(
                        new StringBuilder("Value of property \"com.openexchange.mail.transport.externalRecipientsLocale\"").append(
                            " is not a valid locale identifier (such as \"en_US\"): ").append(tmp).append(". Using fallback \"en\" instead.").toString(),
                        e);
                    externalRecipientsLocale = Locale.ENGLISH;
                }
                if (null == externalRecipientsLocale) {
                    LOG.warn(new StringBuilder("Value of property \"com.openexchange.mail.transport.externalRecipientsLocale\"").append(
                        " is not a valid locale identifier (such as \"en_US\"): ").append(tmp).append(". Using fallback \"en\" instead.").toString());
                    externalRecipientsLocale = Locale.ENGLISH;
                }
                logBuilder.append("\tExternal Recipients Locale: ").append(externalRecipientsLocale.toString()).append('\n');
            }
        }

        logBuilder.append("Global transport properties successfully loaded!");
        if (LOG.isInfoEnabled()) {
            LOG.info(logBuilder.toString());
        }
    }

    @Override
    public int getReferencedPartLimit() {
        return referencedPartLimit;
    }

    /**
     * Gets the default transport provider
     *
     * @return The default transport provider
     */
    public String getDefaultTransportProvider() {
        return defaultTransportProvider;
    }

    /**
     * Gets the name of the publishing infostore folder.
     *
     * @return The name of the publishing infostore folder
     */
    public String getPublishingInfostoreFolder() {
        return publishingInfostoreFolder;
    }

    /**
     * Checks if exceeded attachments shall be published rather than throwing an exceeded-quota exception.
     *
     * @return <code>true</code> if exceeded attachments shall be published rather than throwing an exceeded-quota exception; otherwise
     *         <code>false</code>
     */
    public boolean isPublishOnExceededQuota() {
        return publishOnExceededQuota;
    }

    /**
     * Checks if publishing of email attachments is only enabled for primary account.
     *
     * @return <code>true</code> if publishing of email attachments is only enabled for primary account; otherwise <code>false</code>
     */
    public boolean isPublishPrimaryAccountOnly() {
        return publishPrimaryAccountOnly;
    }

    /**
     * Checks if attachments shall be sent to external recipients although quota was exceeded.
     *
     * @return <code>true</code> if attachments shall be sent to external recipients although quota was exceeded; othjerwise
     *         <code>false</code>
     */
    public boolean isSendAttachmentToExternalRecipients() {
        return sendAttachmentToExternalRecipients;
    }

    /**
     * Checks if publication links shall be provided in "text/html" file attachment named "links.html".
     *
     * @return <code>true</code> if publication links shall be provided in "text/html" file attachment; otherwise <code>false</code>
     */
    public boolean isProvideLinksInAttachment() {
        return provideLinksInAttachment;
    }

    /**
     * Gets the time-to-live in milliseconds for published documents.
     *
     * @return The time-to-live in milliseconds for published documents
     */
    public long getPublishedDocumentTimeToLive() {
        return publishedDocumentTimeToLive;
    }

    /**
     * Determines if published documents should expire.
     */
    public boolean publishedDocumentsExpire() {
        return publishedDocumentTimeToLive > 0;
    }

    /**
     * Gets the locale to use when composing text sent to external recipients.
     *
     * @return The locale to use when composing text sent to external recipients or <code>null</code> to select user's locale
     */
    public Locale getExternalRecipientsLocale() {
        return externalRecipientsLocale;
    }

}
