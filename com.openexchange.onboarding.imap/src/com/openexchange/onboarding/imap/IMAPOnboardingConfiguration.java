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

package com.openexchange.onboarding.imap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.openexchange.capabilities.CapabilityService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.userconfiguration.Permission;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.api.MailConfig;
import com.openexchange.mail.api.MailConfig.PasswordSource;
import com.openexchange.mail.config.MailProperties;
import com.openexchange.mail.service.MailService;
import com.openexchange.mail.transport.config.TransportConfig;
import com.openexchange.onboarding.ClientInfo;
import com.openexchange.onboarding.CommonEntity;
import com.openexchange.onboarding.DefaultEntity;
import com.openexchange.onboarding.DefaultEntityPath;
import com.openexchange.onboarding.DefaultOnboardingSelection;
import com.openexchange.onboarding.IdEntity;
import com.openexchange.onboarding.EntityPath;
import com.openexchange.onboarding.Icon;
import com.openexchange.onboarding.OnboardingConfiguration;
import com.openexchange.onboarding.OnboardingRequest;
import com.openexchange.onboarding.OnboardingSelection;
import com.openexchange.onboarding.OnboardingType;
import com.openexchange.onboarding.OnboardingUtility;
import com.openexchange.onboarding.Platform;
import com.openexchange.onboarding.Result;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.smtp.config.SMTPConfig;

/**
 * {@link IMAPOnboardingConfiguration}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public class IMAPOnboardingConfiguration implements OnboardingConfiguration {

    private final static String IMAP_LOGIN_FIELD = "imapLogin";
    private final static String IMAP_PASSWORD_FIELD = "imapPassword";
    private final static String IMAP_SERVER_FIELD = "imapServer";
    private final static String IMAP_PORT_FIELD = "imapPort";
    private final static String IMAP_SECURE_FIELD = "imapSecure";
    private final static String SMTP_LOGIN_FIELD = "smtpLogin";
    private final static String SMTP_PASSWORD_FIELD = "smtpPassword";
    private final static String SMTP_SERVER_FIELD = "smtpServer";
    private final static String SMTP_PORT_FIELD = "smtpPort";
    private final static String SMTP_SECURE_FIELD = "smtpSecure";

    private final ServiceLookup services;
    private final String id;

    /**
     * Initializes a new {@link IMAPOnboardingConfiguration}.
     */
    public IMAPOnboardingConfiguration(ServiceLookup services) {
        super();
        id = "com.openexchange.onboarding.imap";
        this.services = services;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getDisplayName(Session session) throws OXException {
        return OnboardingUtility.getTranslationFromProperty(id + ".displayName", session);
    }

    @Override
    public Icon getIcon(Session session) throws OXException {
        return OnboardingUtility.loadIconImageFromProperty(id + ".iconName", session);
    }

    @Override
    public String getDescription(Session session) throws OXException {
        return OnboardingUtility.getTranslationFromProperty(id + ".description", session);
    }

    @Override
    public boolean isEnabled(Session session) throws OXException {
        CapabilityService capabilityService = services.getOptionalService(CapabilityService.class);
        if (null == capabilityService) {
            throw ServiceExceptionCode.absentService(CapabilityService.class);
        }

        if (false == capabilityService.getCapabilities(session).contains(Permission.WEBMAIL.getCapabilityName())) {
            return false;
        }

        return OnboardingUtility.getBoolValue("com.openexchange.onboarding.imap.enabled", true, session);
    }

    @Override
    public List<EntityPath> getEntityPaths(Session session) throws OXException {
        List<EntityPath> paths = new ArrayList<EntityPath>(6);
        if (OnboardingUtility.getBoolValue("com.openexchange.onboarding.imap.ipad.enabled", true, session)) {
            List<IdEntity> path = new ArrayList<IdEntity>(3);
            path.add(CommonEntity.APPLE_IOS);
            path.add(CommonEntity.APPLE_IOS_IPAD);
            path.add(DefaultEntity.newInstance((CommonEntity.APPLE_IOS_IPAD.getId() + ".imap"), "com.openexchange.onboarding.imap.", true));
            paths.add(new DefaultEntityPath(path, Platform.APPLE));
        }
        if (OnboardingUtility.getBoolValue("com.openexchange.onboarding.imap.iphone.enabled", true, session)) {
            List<IdEntity> path = new ArrayList<IdEntity>(3);
            path.add(CommonEntity.APPLE_IOS);
            path.add(CommonEntity.APPLE_IOS_IPHONE);
            path.add(DefaultEntity.newInstance((CommonEntity.APPLE_IOS_IPHONE.getId() + ".imap"), "com.openexchange.onboarding.imap.", true));
            paths.add(new DefaultEntityPath(path, Platform.APPLE));
        }
        if (OnboardingUtility.getBoolValue("com.openexchange.onboarding.imap.osx.enabled", true, session)) {
            List<IdEntity> path = new ArrayList<IdEntity>(2);
            path.add(CommonEntity.APPLE_OSX);
            path.add(DefaultEntity.newInstance(CommonEntity.APPLE_OSX.getId() + ".imap", "com.openexchange.onboarding.imap.", true));
            paths.add(new DefaultEntityPath(path, Platform.APPLE));
        }

        if (OnboardingUtility.getBoolValue("com.openexchange.onboarding.imap.android.tablet.enabled", true, session)) {
            List<IdEntity> path = new ArrayList<IdEntity>(2);
            path.add(CommonEntity.ANDROID_TABLET);
            path.add(DefaultEntity.newInstance(CommonEntity.ANDROID_TABLET.getId() + ".imap", "com.openexchange.onboarding.imap.", true));
            paths.add(new DefaultEntityPath(path, Platform.ANDROID_GOOGLE));
        }
        if (OnboardingUtility.getBoolValue("com.openexchange.onboarding.imap.android.phone.enabled", true, session)) {
            List<IdEntity> path = new ArrayList<IdEntity>(2);
            path.add(CommonEntity.ANDROID_PHONE);
            path.add(DefaultEntity.newInstance(CommonEntity.ANDROID_PHONE.getId() + ".imap", "com.openexchange.onboarding.imap.", true));
            paths.add(new DefaultEntityPath(path, Platform.ANDROID_GOOGLE));
        }

        if (OnboardingUtility.getBoolValue("com.openexchange.onboarding.imap.windows.enabled", true, session)) {
            List<IdEntity> path = new ArrayList<IdEntity>(2);
            path.add(CommonEntity.WINDOWS_DESKTOP_8_10);
            path.add(DefaultEntity.newInstance(CommonEntity.WINDOWS_DESKTOP_8_10.getId() + ".imap", "com.openexchange.onboarding.imap.", true));
            paths.add(new DefaultEntityPath(path, Platform.WINDOWS));
        }
        return paths;
    }

    @Override
    public List<OnboardingSelection> getSelections(String lastEntityId, ClientInfo clientInfo, Session session) throws OXException {
        List<OnboardingSelection> selections = new ArrayList<OnboardingSelection>(1);
        selections.add(DefaultOnboardingSelection.newInstance("imap.display", id, "com.openexchange.onboarding.imap.display.", OnboardingType.DISPLAY));
        return selections;
    }

    @Override
    public Result execute(OnboardingRequest request, Session session) throws OXException {
        MailService mailService = services.getService(MailService.class);
        SMTPConfig smtpConfig = new SMTPConfig();
        TransportConfig.getTransportConfig(smtpConfig, session, 0);
        MailAccess<?, ?> access = mailService.getMailAccess(session, 0);
        try {
            access.connect(false);
            MailConfig config = access.getMailConfig();
            String imapServer = config.getServer();
            int imapPort = config.getPort();
            String imapLogin = config.getLogin();
            String imapPassword;
            PasswordSource source = MailProperties.getInstance().getPasswordSource();
            if (PasswordSource.GLOBAL.equals(source)) {
                imapPassword = MailProperties.getInstance().getMasterPassword();
            } else {
                imapPassword = session.getPassword();
            }
            boolean imapSecure = config.isSecure();

            String smtpServer = smtpConfig.getServer();
            int smtpPort = smtpConfig.getPort();
            String smtpLogin = smtpConfig.getLogin();
            String smtpPassword = smtpConfig.getPassword();
            boolean smtpSecure = smtpConfig.isSecure();

            Map<String, Object> formContent = new HashMap<String, Object>();
            formContent.put(IMAP_LOGIN_FIELD, imapLogin);
            formContent.put(IMAP_PASSWORD_FIELD, imapPassword);
            formContent.put(IMAP_SERVER_FIELD, imapServer);
            formContent.put(IMAP_PORT_FIELD, new Integer(imapPort));
            formContent.put(IMAP_SECURE_FIELD, new Boolean(imapSecure));
            formContent.put(SMTP_LOGIN_FIELD, smtpLogin);
            formContent.put(SMTP_PASSWORD_FIELD, smtpPassword);
            formContent.put(SMTP_SERVER_FIELD, smtpServer);
            formContent.put(SMTP_PORT_FIELD, new Integer(smtpPort));
            formContent.put(SMTP_SECURE_FIELD, new Boolean(smtpSecure));

            Result result = new Result("Result", formContent);
            return result;
        } finally {
            access.close();
        }
    }

}
