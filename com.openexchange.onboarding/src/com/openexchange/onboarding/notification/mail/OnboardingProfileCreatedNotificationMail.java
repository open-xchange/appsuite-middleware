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
 *     Copyright (C) 2004-2015 Open-Xchange, Inc.
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

package com.openexchange.onboarding.notification.mail;

import java.util.HashMap;
import java.util.Map;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.i18n.Translator;
import com.openexchange.i18n.TranslatorFactory;
import com.openexchange.notification.mail.MailData;
import com.openexchange.notification.mail.MailData.Builder;
import com.openexchange.onboarding.OnboardingExceptionCodes;
import com.openexchange.onboarding.notification.OnboardingNotificationStrings;
import com.openexchange.onboarding.osgi.Services;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.serverconfig.ServerConfig;
import com.openexchange.serverconfig.ServerConfigService;
import com.openexchange.session.Session;
import com.openexchange.user.UserService;


/**
 * {@link OnboardingProfileCreatedNotificationMail}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.8.1
 */
public class OnboardingProfileCreatedNotificationMail {

    private static final String PROFILE_CREATED = "profile_created";

    public static MailData createNotificationMail(String mailAddress, String hostName, Session session) throws OXException {
        InternetAddress recipient = null;
        try {
            recipient = new InternetAddress(mailAddress);
        } catch (AddressException e) {
            throw OnboardingExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
        ServerConfigService serverConfigService = Services.getService(ServerConfigService.class);
        if (null == serverConfigService) {
            throw ServiceExceptionCode.absentService(ServerConfigService.class);
        }
        UserService userService = Services.getService(UserService.class);
        if (null == userService) {
            throw ServiceExceptionCode.absentService(UserService.class);
        }
        ContextService contextService = Services.getService(ContextService.class);
        if (null == contextService) {
            throw ServiceExceptionCode.absentService(ContextService.class);
        }
        Context ctx = contextService.getContext(session.getContextId());
        TranslatorFactory factory = Services.getService(TranslatorFactory.class);
        if (null == factory) {
            throw ServiceExceptionCode.absentService(TranslatorFactory.class);
        }
        Translator translator = factory.translatorFor(userService.getUser(session.getUserId(), session.getContextId()).getLocale());
        String notification = translator.translate(OnboardingNotificationStrings.PROFILE_CREATED);

        ServerConfig serverConfig = serverConfigService.getServerConfig(null == hostName ? "" : hostName, session.getUserId(), session.getContextId());

        Map<String, Object> vars = new HashMap<String, Object>();
        vars.put(PROFILE_CREATED, notification);
        Builder builder = MailData.newBuilder()
            .setRecipient(recipient)
            .setSubject(notification)
            .setHtmlTemplate("notify.onboarding.profile.mail.html.tmpl")
            .setTemplateVars(vars)
            .setMailConfig(serverConfig.getNotificationMailConfig())
            .setContext(ctx);
        return builder.build();
    }

}
