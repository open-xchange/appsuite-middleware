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

package com.openexchange.onboarding.mail;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.userconfiguration.Permission;
import com.openexchange.mail.api.MailConfig;
import com.openexchange.mail.service.MailService;
import com.openexchange.mail.transport.config.TransportConfig;
import com.openexchange.mail.usersetting.UserSettingMail;
import com.openexchange.mail.usersetting.UserSettingMailStorage;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.onboarding.Device;
import com.openexchange.onboarding.DisplayResult;
import com.openexchange.onboarding.OnboardingProvider;
import com.openexchange.onboarding.OnboardingExceptionCodes;
import com.openexchange.onboarding.OnboardingRequest;
import com.openexchange.onboarding.OnboardingUtility;
import com.openexchange.onboarding.Result;
import com.openexchange.onboarding.ResultReply;
import com.openexchange.onboarding.Scenario;
import com.openexchange.onboarding.plist.PlistResult;
import com.openexchange.plist.PListDict;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.user.UserService;

/**
 * {@link MailOnboardingProvider}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public class MailOnboardingProvider implements OnboardingProvider {

    private final ServiceLookup services;
    private final String identifier;
    private final EnumSet<Device> supportedDevices;

    /**
     * Initializes a new {@link MailOnboardingProvider}.
     */
    public MailOnboardingProvider(ServiceLookup services) {
        super();
        this.services = services;
        identifier = "imap";
        supportedDevices = EnumSet.allOf(Device.class);
    }

    @Override
    public boolean isAvailable(Session session) throws OXException {
        if (!OnboardingUtility.hasCapability(Permission.WEBMAIL.getCapabilityName(), session)) {
            return false;
        }
        MailAccount mailAccount = services.getService(MailAccountStorageService.class).getDefaultMailAccount(session.getUserId(), session.getContextId());
        return (mailAccount.getMailProtocol().startsWith("imap") && mailAccount.getTransportProtocol().startsWith("smtp"));
    }

    @Override
    public String getId() {
        return identifier;
    }

    @Override
    public Set<Device> getSupportedDevices() {
        return Collections.unmodifiableSet(supportedDevices);
    }

    @Override
    public Result execute(OnboardingRequest request, Result previousResult, Session session) throws OXException {
        Device device = request.getDevice();
        if (!supportedDevices.contains(device)) {
            throw OnboardingExceptionCodes.UNSUPPORTED_DEVICE.create(identifier, device.getId());
        }

        Scenario scenario = request.getScenario();
        if (!Device.getActionsFor(device, scenario.getType(), session).contains(request.getAction())) {
            throw OnboardingExceptionCodes.UNSUPPORTED_ACTION.create(request.getAction().getId());
        }

        switch(scenario.getType()) {
            case LINK:
                throw OnboardingExceptionCodes.UNSUPPORTED_TYPE.create(identifier, scenario.getType().getId());
            case MANUAL:
                return doExecuteManual(request, previousResult, session);
            case PLIST:
                return doExecutePlist(request, previousResult, session);
            default:
                throw OnboardingExceptionCodes.UNSUPPORTED_TYPE.create(identifier, scenario.getType().getId());
        }
    }

    private Configurations getEffectiveConfigurations(Session session) throws OXException {
        // IMAP
        Configuration imapConfiguration;
        {
            MailConfig imapConfig = services.getService(MailService.class).getMailConfig(session, MailAccount.DEFAULT_ID);
            MailConfig.getConfig(imapConfig, session, 0);
            String imapServer = OnboardingUtility.getValueFromProperty("com.openexchange.onboarding.mail.imap.host", null, session);
            if (null == imapServer) {
                imapServer = imapConfig.getServer();
            }
            Integer imapPort = OnboardingUtility.getIntFromProperty("com.openexchange.onboarding.mail.imap.port", null, session);
            if (null == imapPort) {
                imapPort = Integer.valueOf(imapConfig.getPort());
            }
            Boolean imapSecure = OnboardingUtility.getBoolFromProperty("com.openexchange.onboarding.mail.imap.secure", null, session);
            if (null == imapSecure) {
                imapSecure = Boolean.valueOf(imapConfig.isSecure());
            }
            String imapLogin = imapConfig.getLogin();
            String imapPassword = imapConfig.getPassword();
            imapConfiguration = new Configuration(imapServer, imapPort.intValue(), imapSecure.booleanValue(), imapLogin, imapPassword);
        }

        // SMTP
        Configuration smtpConfiguration;
        {
            TransportConfig smtpConfig = services.getService(MailService.class).getTransportConfig(session, MailAccount.DEFAULT_ID);
            TransportConfig.getTransportConfig(smtpConfig, session, MailAccount.DEFAULT_ID);
            String smtpServer = OnboardingUtility.getValueFromProperty("com.openexchange.onboarding.mail.smtp.host", null, session);
            if (null == smtpServer) {
                smtpServer = smtpConfig.getServer();
            }
            Integer smtpPort = OnboardingUtility.getIntFromProperty("com.openexchange.onboarding.mail.smtp.port", null, session);
            if (null == smtpPort) {
                smtpPort = Integer.valueOf(smtpConfig.getPort());
            }
            Boolean smtpSecure = OnboardingUtility.getBoolFromProperty("com.openexchange.onboarding.mail.smtp.secure", null, session);
            if (null == smtpSecure) {
                smtpSecure = Boolean.valueOf(smtpConfig.isSecure());
            }
            String smtpLogin = smtpConfig.getLogin();
            String smtpPassword = smtpConfig.getPassword();
            smtpConfiguration = new Configuration(smtpServer, smtpPort.intValue(), smtpSecure.booleanValue(), smtpLogin, smtpPassword);
        }

        // Return configurations
        return new Configurations(imapConfiguration, smtpConfiguration);
    }

    private Result doExecutePlist(OnboardingRequest request, Result previousResult, Session session) throws OXException {
        return plistResult(request, previousResult, session);
    }

    private Result doExecuteManual(OnboardingRequest request, Result previousResult, Session session) throws OXException {
        return displayResult(request, previousResult, session);
    }

    // --------------------------------------------------------------------------------------------------------------------------

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

    private Result displayResult(OnboardingRequest request, Result previousResult, Session session) throws OXException {
        Configurations configurations = getEffectiveConfigurations(session);

        Map<String, Object> configuration = null == previousResult ? new HashMap<String, Object>(8) : ((DisplayResult) previousResult).getConfiguration();

        configuration.put(IMAP_LOGIN_FIELD, configurations.imapConfig.login);
        configuration.put(IMAP_PASSWORD_FIELD, configurations.imapConfig.password);
        configuration.put(IMAP_SERVER_FIELD, configurations.imapConfig.host);
        configuration.put(IMAP_PORT_FIELD, new Integer(configurations.imapConfig.port));
        configuration.put(IMAP_SECURE_FIELD, new Boolean(configurations.imapConfig.secure));

        configuration.put(SMTP_LOGIN_FIELD, configurations.smtpConfig.login);
        configuration.put(SMTP_PASSWORD_FIELD, configurations.smtpConfig.password);
        configuration.put(SMTP_SERVER_FIELD, configurations.smtpConfig.host);
        configuration.put(SMTP_PORT_FIELD, new Integer(configurations.smtpConfig.port));
        configuration.put(SMTP_SECURE_FIELD, new Boolean(configurations.smtpConfig.secure));

        return new DisplayResult(configuration);
    }

    // --------------------------------------------- PLIST utils --------------------------------------------------------------

    private UserSettingMail getUserSettingMail(Session session) throws OXException {
        if (session instanceof ServerSession) {
            return ((ServerSession) session).getUserSettingMail();
        }

        return UserSettingMailStorage.getInstance().getUserSettingMail(session);
    }

    private User getUser(Session session) throws OXException {
        if (session instanceof ServerSession) {
            return ((ServerSession) session).getUser();
        }

        return services.getService(UserService.class).getUser(session.getUserId(), session.getContextId());
    }

    private Result plistResult(OnboardingRequest request, Result previousResult, Session session) throws OXException {
        Scenario scenario = request.getScenario();

        Configurations configurations = getEffectiveConfigurations(session);

        // Get the PListDict to contribute to
        PListDict pListDict;
        if (null == previousResult) {
            pListDict = new PListDict();
            pListDict.setPayloadIdentifier("com.open-xchange." + scenario.getId());
            pListDict.setPayloadType("Configuration");
            pListDict.setPayloadUUID(OnboardingUtility.craftUUIDFrom(scenario.getId(), session).toString());
            pListDict.setPayloadVersion(1);
            pListDict.setPayloadDisplayName(scenario.getDisplayName(session));
        } else {
            pListDict = ((PlistResult) previousResult).getPListDict();
        }

        // Generate content
        PListDict payloadContent = new PListDict();
        payloadContent.setPayloadType("com.apple.mail.managed");
        payloadContent.setPayloadUUID(OnboardingUtility.craftUUIDFrom(identifier, session).toString());
        payloadContent.setPayloadIdentifier("com.open-xchange.mail");
        payloadContent.setPayloadVersion(1);

        // A user-visible description of the email account, shown in the Mail and Settings applications.
        payloadContent.addStringValue("EmailAccountDescription", OnboardingUtility.getTranslationFor(MailOnboardingStrings.IMAP_ACCOUNT_DESCRIPTION, session));

        // The full user name for the account. This is the user name in sent messages, etc.
        payloadContent.addStringValue("EmailAccountName", getUser(session).getDisplayName());

        // Allowed values are EmailTypePOP and EmailTypeIMAP. Defines the protocol to be used for that account.
        payloadContent.addStringValue("EmailAccountType", "EmailTypeIMAP");

        // Designates the full email address for the account. If not present in the payload, the device prompts for this string during profile installation.
        payloadContent.addStringValue("EmailAddress", getUserSettingMail(session).getSendAddr());


        // Designates the authentication scheme for incoming mail. Allowed values are EmailAuthPassword and EmailAuthNone.
        payloadContent.addStringValue("IncomingMailServerAuthentication", "EmailAuthPassword");

        // Designates the incoming mail server host name (or IP address).
        payloadContent.addStringValue("IncomingMailServerHostName", configurations.imapConfig.host);

        // Designates the incoming mail server port number. If no port number is specified, the default port for a given protocol is used.
        payloadContent.addIntegerValue("IncomingMailServerPortNumber", configurations.imapConfig.port);

        // Designates whether the incoming mail server uses SSL for authentication. Default false.
        payloadContent.addBooleanValue("IncomingMailServerUseSSL", configurations.imapConfig.secure);

        // Designates the user name for the email account, usually the same as the email address up to the @ character.
        // If not present in the payload, and the account is set up to require authentication for incoming email, the device will prompt for this string during profile installation.
        payloadContent.addStringValue("IncomingMailServerUsername", configurations.imapConfig.login);

        // Password for the Incoming Mail Server. Use only with encrypted profiles.
        payloadContent.addStringValue("IncomingPassword", configurations.imapConfig.password);


        // Password for the Outgoing Mail Server. Use only with encrypted profiles.
        payloadContent.addStringValue("OutgoingPassword", configurations.smtpConfig.password);

        // If set, the user will be prompted for the password only once and it will be used for both outgoing and incoming mail.
        payloadContent.addBooleanValue("OutgoingPasswordSameAsIncomingPassword", configurations.imapConfig.password.equals(configurations.smtpConfig.password));

        // Designates the authentication scheme for outgoing mail. Allowed values are EmailAuthPassword and EmailAuthNone.
        payloadContent.addStringValue("OutgoingMailServerAuthentication", "EmailAuthPassword");

        // Designates the outgoing mail server host name (or IP address).
        payloadContent.addStringValue("OutgoingMailServerHostName", configurations.smtpConfig.host);

        // Designates the outgoing mail server port number. If no port number is specified, ports 25, 587 and 465 are used, in this order.
        payloadContent.addIntegerValue("OutgoingMailServerPortNumber", configurations.smtpConfig.port);

        // Designates whether the outgoing mail server uses SSL for authentication. Default false.
        payloadContent.addBooleanValue("OutgoingMailServerUseSSL", configurations.smtpConfig.secure);

        // Designates the user name for the email account, usually the same as the email address up to the @ character.
        // If not present in the payload, and the account is set up to require authentication for outgoing email, the device prompts for this string during profile installation.
        payloadContent.addStringValue("OutgoingMailServerUsername", configurations.smtpConfig.login);

        // Further options (currently not used)

        // PreventMove - Boolean - Optional. Default false.
        // If true, messages may not be moved out of this email account into another account. Also prevents forwarding or replying from a different account than the message was originated from.
        // Availability: Available only in iOS 5.0 and later.

        // PreventAppSheet - Boolean - Optional. Default false.
        // If true, this account is not available for sending mail in any app other than the Apple Mail app.
        // Availability: Available only in iOS 5.0 and later.

        // SMIMEEnabled - Boolean - Optional. Default false.
        // If true, this account supports S/MIME.
        // Availability: Available only in iOS 5.0 and later.

        // SMIMESigningCertificateUUID - String - Optional.
        // The PayloadUUID of the identity certificate used to sign messages sent from this account.
        // Availability: Available only in iOS 5.0 and later.

        // SMIMEEncryptionCertificateUUID - String - Optional.
        // The PayloadUUID of the identity certificate used to decrypt messages sent to this account.
        // Availability: Available only in iOS 5.0 and later.

        // SMIMEEnablePerMessageSwitch _ Boolean - Optional.
        // If set to true, enable the per-message signing and encryption switch. Defaults to true.
        // Availability: Available only in iOS 8.0 and later.

        // disableMailRecentsSyncing - Boolean - Default false.
        // If true, this account is excluded from address Recents syncing. This defaults to false.
        // Availability: Available only in iOS 6.0 and later.

        // allowMailDrop - Boolean - Default false.
        // If true, this account is allowed to use Mail Drop. The default is false.
        // Availability: Available only in iOS 9.0 and later.

        // disableMailDrop - Boolean - Default false.
        // If true, this account is excluded from using Mail Drop. The default is false.

        // Add payload content dictionary to top-level dictionary
        pListDict.addPayloadContent(payloadContent);

        // Return result
        return new PlistResult(pListDict, ResultReply.NEUTRAL);
    }

}
