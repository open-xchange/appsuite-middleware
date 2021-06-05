/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.gdpr.dataexport.impl.notification;

import static com.openexchange.notification.FullNameBuilder.buildFullName;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.gdpr.dataexport.DataExportExceptionCode;
import com.openexchange.gdpr.dataexport.HostInfo;
import com.openexchange.gdpr.dataexport.impl.osgi.Services;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.notify.hostname.HostnameService;
import com.openexchange.i18n.Translator;
import com.openexchange.i18n.TranslatorFactory;
import com.openexchange.mail.mime.QuotedInternetAddress;
import com.openexchange.notification.mail.MailData;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.serverconfig.ServerConfig;
import com.openexchange.serverconfig.ServerConfigService;
import com.openexchange.tools.TimeZoneUtils;
import com.openexchange.user.User;
import com.openexchange.user.UserService;


/**
 * {@link DataExportNotificationMail} - Utility class for creating {@link MailData} instances ready to be used to create an
 * appropriate notification mail for the on-boarding module.
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.8.1
 */
public class DataExportNotificationMail {

    private static final String VARIABLE_SALUTATION = "salutation";
    private static final String VARIABLE_CONTENT = "content";
    private static final String VARIABLE_VIEW_ARCHIVES_LINK = "view_archives_link";
    private static final String VARIABLE_VIEW_ARCHIVES_LABEL = "view_archives_label";
    private static final String VARIABLE_INFO = "info";

    private static final String fallbackHostname;
    static {
        String fbHostname;
        try {
            fbHostname = InetAddress.getLocalHost().getCanonicalHostName();
        } catch (final UnknownHostException e) {
            fbHostname = "localhost";
        }
        fallbackHostname = fbHostname;
    }

    /**
     * Creates a new {@link MailData} instance ready to be used to create an appropriate notification mail for delivering a profile via E-Mail.
     *
     * @param reason The notification reason
     * @param creationDate The date when the data export has been created/requested
     * @param expiryDate The expiry date (only expected if reason is set to {@link Reason#SUCCESS})
     * @param hostInfo The basic host information (only expected if reason is set to {@link Reason#SUCCESS})
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return A new {@code MailData} instance representing the profile delivery mail
     * @throws OXException If {@code MailData} instance cannot be returned
     */
    public static MailData createNotificationMail(Reason reason, Date creationDate, Date expiryDate, HostInfo hostInfo, int userId, int contextId) throws OXException {
        String hostName = determineHostName(userId, contextId);
        return createNotificationMail(hostName, reason, creationDate, expiryDate, hostInfo, userId, contextId);
    }

    /**
     * Creates a new {@link MailData} instance ready to be used to create an appropriate notification mail for delivering a profile via E-Mail.
     *
     * @param hostName The associated host name
     * @param reason The notification reason
     * @param creationDate The date when the data export has been created/requested
     * @param expiryDate The expiry date (only expected if reason is set to {@link Reason#SUCCESS})
     * @param hostInfo The basic host information (only expected if reason is set to {@link Reason#SUCCESS})
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return A new {@code MailData} instance representing the profile delivery mail
     * @throws OXException If {@code MailData} instance cannot be returned
     */
    public static MailData createNotificationMail(String hostName, Reason reason, Date creationDate, Date expiryDate, HostInfo hostInfo, int userId, int contextId) throws OXException {
        Map<String, Object> vars = new HashMap<String, Object>(4);

        // Get translator
        TranslatorFactory factory = Services.optService(TranslatorFactory.class);
        if (null == factory) {
            throw ServiceExceptionCode.absentService(TranslatorFactory.class);
        }
        User user = getUser(userId, contextId);
        String mailAddress = user.getMail();
        Translator translator = factory.translatorFor(user.getLocale());

        // Salutation
        {
            String translated = translator.translate(DataExportNotificationStrings.SALUTATION);
            translated = String.format(translated, buildFullName(user, translator));
            vars.put(VARIABLE_SALUTATION, translated);
        }

        // Determine E-Mail subject &  content dependent on reason
        String subject;
        String content;
        switch (reason) {
            case SUCCESS:
                // Link to settings
                String settingsLink = generateSettingsLink(hostInfo);
                vars.put(VARIABLE_VIEW_ARCHIVES_LINK, settingsLink);
                vars.put(VARIABLE_VIEW_ARCHIVES_LABEL, translator.translate(DataExportNotificationStrings.VIEW_ARCHIVES));
                // Inject expiration date
                if (expiryDate != null) {
                    // Translate content
                    content = translator.translate(DataExportNotificationStrings.CONTENT_SUCCESS_WITH_EXPIRATION);
                    // Inject creation & expiration date
                    DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM, user.getLocale());
                    Date localCreationDate = new Date(creationDate.getTime() + TimeZoneUtils.getTimeZone(user.getTimeZone()).getOffset(creationDate.getTime()));
                    Date localExpirationDate = new Date(expiryDate.getTime() + TimeZoneUtils.getTimeZone(user.getTimeZone()).getOffset(expiryDate.getTime()));
                    content = String.format(content, dateFormat.format(localCreationDate), dateFormat.format(localExpirationDate));
                } else {
                    // Translate content
                    content = translator.translate(DataExportNotificationStrings.CONTENT_SUCCESS_WITHOUT_EXPIRATION);
                    // Inject creation date
                    DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM, user.getLocale());
                    Date localCreationDate = new Date(creationDate.getTime() + TimeZoneUtils.getTimeZone(user.getTimeZone()).getOffset(creationDate.getTime()));
                    content = String.format(content, dateFormat.format(localCreationDate));
                }
                subject = translator.translate(DataExportNotificationStrings.SUBJECT_SUCCESS);
                break;
            case FAILED:
                content = translator.translate(DataExportNotificationStrings.CONTENT_FAILURE);
                // Inject creation date
                {
                    DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM, user.getLocale());
                    Date localCreationDate = new Date(creationDate.getTime() + TimeZoneUtils.getTimeZone(user.getTimeZone()).getOffset(creationDate.getTime()));
                    content = String.format(content, dateFormat.format(localCreationDate));
                }
                subject = translator.translate(DataExportNotificationStrings.SUBJECT_FAILURE);
                break;
            case ABORTED:
                content = translator.translate(DataExportNotificationStrings.CONTENT_ABORTED);
                // Inject creation date
                {
                    DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM, user.getLocale());
                    Date localCreationDate = new Date(creationDate.getTime() + TimeZoneUtils.getTimeZone(user.getTimeZone()).getOffset(creationDate.getTime()));
                    content = String.format(content, dateFormat.format(localCreationDate));
                }
                subject = translator.translate(DataExportNotificationStrings.SUBJECT_ABORTED);
                break;
            default:
                throw new IllegalArgumentException("Unknown reason: " + reason);
        }

        vars.put(VARIABLE_CONTENT, content);

        vars.put(VARIABLE_INFO, translator.translate(DataExportNotificationStrings.INFO));

        String noReplyPersonal = translator.translate(DataExportNotificationStrings.NO_REPLY_PERSONAL);

        return createNotificationMail(mailAddress, hostName, userId, contextId, "notify.gdpr.dataexport.mail.html.tmpl", subject, noReplyPersonal, vars);
    }

    private static String generateSettingsLink(HostInfo hostInfo) throws OXException {
        try {
            String fragment = "!!&app=io.ox/settings&folder=virtual/settings/personaldata";
            return new URI(hostInfo.isSecure() ? "https" : "http", null, hostInfo.getHost(), -1, "/appsuite/", null, fragment).toString();
        } catch (URISyntaxException e) {
            throw OXException.general("Building URI failed", e);
        }
    }

    /**
     * Creates a new {@link MailData} instance ready to be used to create an appropriate notification mail.
     *
     * @param mailAddress The E-Mail address to use as recipient
     * @param hostName The associated host name
     * @param userId The user identifier
     * @param contextId The context identifier
     * @param templateFileName The file name of the HTML template
     * @param subject The string for the mail's subject
     * @param noReplyPersonal The no-reply personal
     * @param vars The variables to insert
     * @return A new {@code MailData} instance
     * @throws OXException If {@code MailData} instance cannot be returned
     */
    private static MailData createNotificationMail(String mailAddress, String hostName, int userId, int contextId, String templateFileName, String subject, String noReplyPersonal, Map<String, Object> vars) throws OXException {
        // Acquire needed services
        ServerConfigService serverConfigService = Services.optService(ServerConfigService.class);
        if (null == serverConfigService) {
            throw ServiceExceptionCode.absentService(ServerConfigService.class);
        }
        ContextService contextService = Services.optService(ContextService.class);
        if (null == contextService) {
            throw ServiceExceptionCode.absentService(ContextService.class);
        }

        // Build MailData instance
        try {
            InternetAddress recipient = new QuotedInternetAddress(mailAddress);
            Context ctx = contextService.getContext(contextId);
            ServerConfig serverConfig = serverConfigService.getServerConfig(null == hostName ? "" : hostName, userId, contextId);

            return MailData.newBuilder()
                .setRecipient(recipient)
                .setSubject(subject)
                .setHtmlTemplate(templateFileName)
                .setTemplateVars((null == vars) ? Collections.<String, Object> emptyMap() : vars)
                .setMailConfig(serverConfig.getNotificationMailConfig())
                .setContext(ctx)
                .setNoReplyAddressPersonal(noReplyPersonal)
                .build();
        } catch (AddressException e) {
            throw DataExportExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Gets the user for given identifier.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return The user
     * @throws OXException If user cannot be returned
     */
    public static User getUser(int userId, int contextId) throws OXException {
        UserService userService = Services.optService(UserService.class);
        if (null == userService) {
            throw ServiceExceptionCode.absentService(UserService.class);
        }
        return userService.getUser(userId, contextId);
    }

    private static String determineHostName(int userId, int contextId) {
        String hostname = null;

        HostnameService hostnameService = Services.optService(HostnameService.class);
        if (hostnameService != null) {
            hostname = hostnameService.getHostname(userId, contextId);
        }

        if (hostname == null) {
            hostname = fallbackHostname;
        }

        return hostname;
    }

}
