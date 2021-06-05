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

package com.openexchange.client.onboarding.notification.mail;

import static com.openexchange.notification.FullNameBuilder.buildFullName;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import com.openexchange.client.onboarding.OnboardingExceptionCodes;
import com.openexchange.client.onboarding.notification.OnboardingNotificationStrings;
import com.openexchange.client.onboarding.osgi.Services;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.i18n.Translator;
import com.openexchange.i18n.TranslatorFactory;
import com.openexchange.java.Strings;
import com.openexchange.mail.mime.QuotedInternetAddress;
import com.openexchange.notification.mail.MailData;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.serverconfig.ServerConfig;
import com.openexchange.serverconfig.ServerConfigService;
import com.openexchange.session.Session;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.user.User;
import com.openexchange.user.UserService;


/**
 * {@link OnboardingProfileCreatedNotificationMail} - Utility class for creating {@link MailData} instances ready to be used to create an
 * appropriate notification mail for the on-boarding module.
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.8.1
 */
public class OnboardingProfileCreatedNotificationMail {

    private static final String VARIABLE_SALUTATION = "salutation";
    private static final String VARIABLE_CONTENT = "content";

    /**
     * Creates a new {@link MailData} instance ready to be used to create an appropriate notification mail for delivering a profile via E-Mail.
     *
     * @param mailAddress The E-Mail address to use as recipient
     * @param hostName The associated host name
     * @param fileName The name of the file providing the configuration profile
     * @param session The session providing user data
     * @return A new {@code MailData} instance representing the profile delivery mail
     * @throws OXException If {@code MailData} instance cannot be returned
     */
    public static MailData createProfileNotificationMail(String mailAddress, String hostName, String fileName, Session session) throws OXException {
        Map<String, Object> vars = new HashMap<String, Object>(4);

        // Get translator
        TranslatorFactory factory = Services.getService(TranslatorFactory.class);
        if (null == factory) {
            throw ServiceExceptionCode.absentService(TranslatorFactory.class);
        }
        User user = getUser(session);
        Translator translator = factory.translatorFor(user.getLocale());

        // Salutation
        {
            String translated = translator.translate(OnboardingNotificationStrings.SALUTATION);
            translated = String.format(translated, buildFullName(user, translator));
            vars.put(VARIABLE_SALUTATION, translated);
        }

        // E-Mail content
        if (Strings.isEmpty(fileName)) {
            String translated = translator.translate(OnboardingNotificationStrings.CONTENT);
            vars.put(VARIABLE_CONTENT, translated);
        } else {
            String translated = translator.translate(OnboardingNotificationStrings.CONTENT_WITH_FILENAME);
            translated = String.format(translated, fileName);
            vars.put(VARIABLE_CONTENT, translated);
        }

        // E-Mail subject
        String subject = translator.translate(OnboardingNotificationStrings.SUBJECT);

        return createNotificationMail(mailAddress, hostName, session, "notify.onboarding.profile.mail.html.tmpl", subject, vars);
    }

    /**
     * Creates a new {@link MailData} instance ready to be used to create an appropriate notification mail as a result of a selected
     * on-boarding action; e.g. delivery of a profile via E-Mail.
     *
     * @param mailAddress The E-Mail address to use as recipient
     * @param hostName The associated host name
     * @param session The session providing user data
     * @param templateFileName The file name of the HTML template
     * @param subject The string for the mail's subject
     * @param vars The variables to insert
     * @return A new {@code MailData} instance
     * @throws OXException If {@code MailData} instance cannot be returned
     */
    private static MailData createNotificationMail(String mailAddress, String hostName, Session session, String templateFileName, String subject, Map<String, Object> vars) throws OXException {
        // Acquire needed services
        ServerConfigService serverConfigService = Services.getService(ServerConfigService.class);
        if (null == serverConfigService) {
            throw ServiceExceptionCode.absentService(ServerConfigService.class);
        }
        ContextService contextService = Services.getService(ContextService.class);
        if (null == contextService) {
            throw ServiceExceptionCode.absentService(ContextService.class);
        }

        // Build MailData instance
        try {
            InternetAddress recipient = new QuotedInternetAddress(mailAddress);
            Context ctx = contextService.getContext(session.getContextId());
            ServerConfig serverConfig = serverConfigService.getServerConfig(null == hostName ? "" : hostName, session.getUserId(), session.getContextId());

            return MailData.newBuilder()
                .setRecipient(recipient)
                .setSubject(subject)
                .setHtmlTemplate(templateFileName)
                .setTemplateVars((null == vars) ? Collections.<String, Object> emptyMap() : vars)
                .setMailConfig(serverConfig.getNotificationMailConfig())
                .setContext(ctx)
                .build();
        } catch (AddressException e) {
            throw OnboardingExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    private static User getUser(Session session) throws OXException {
        if (session instanceof ServerSession) {
            return ((ServerSession) session).getUser();
        }

        UserService userService = Services.getService(UserService.class);
        if (null == userService) {
            throw ServiceExceptionCode.absentService(UserService.class);
        }
        return userService.getUser(session.getUserId(), session.getContextId());
    }

}
