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

import static com.openexchange.onboarding.OnboardingSelectionKey.keyFor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import com.openexchange.ajax.container.ThresholdFileHolder;
import com.openexchange.ajax.fileholder.IFileHolder;
import com.openexchange.capabilities.CapabilityService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.userconfiguration.Permission;
import com.openexchange.imap.config.IMAPConfig;
import com.openexchange.java.Strings;
import com.openexchange.mail.api.MailConfig;
import com.openexchange.mail.dataobjects.compose.ComposeType;
import com.openexchange.mail.dataobjects.compose.ComposedMailMessage;
import com.openexchange.mail.transport.MailTransport;
import com.openexchange.mail.transport.TransportProvider;
import com.openexchange.mail.transport.TransportProviderRegistry;
import com.openexchange.mail.transport.config.TransportConfig;
import com.openexchange.mail.usersetting.UserSettingMail;
import com.openexchange.mail.usersetting.UserSettingMailStorage;
import com.openexchange.notification.mail.MailData;
import com.openexchange.notification.mail.NotificationMailFactory;
import com.openexchange.onboarding.CommonForms;
import com.openexchange.onboarding.DefaultEntityPath;
import com.openexchange.onboarding.DefaultOnboardingSelection;
import com.openexchange.onboarding.Device;
import com.openexchange.onboarding.EntityPath;
import com.openexchange.onboarding.Icon;
import com.openexchange.onboarding.Module;
import com.openexchange.onboarding.OnboardingConfiguration;
import com.openexchange.onboarding.OnboardingExceptionCodes;
import com.openexchange.onboarding.OnboardingExecutor;
import com.openexchange.onboarding.OnboardingRequest;
import com.openexchange.onboarding.OnboardingSelection;
import com.openexchange.onboarding.OnboardingSelectionKey;
import com.openexchange.onboarding.OnboardingStrings;
import com.openexchange.onboarding.OnboardingType;
import com.openexchange.onboarding.OnboardingUtility;
import com.openexchange.onboarding.Result;
import com.openexchange.onboarding.notification.mail.OnboardingProfileCreatedNotificationMail;
import com.openexchange.onboarding.plist.PListDict;
import com.openexchange.onboarding.plist.xml.StaxUtils;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.smtp.config.SMTPConfig;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.user.UserService;

/**
 * {@link IMAPOnboardingConfiguration}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public class IMAPOnboardingConfiguration implements OnboardingConfiguration {

    private final ServiceLookup services;
    private final String propertyPrefix;
    private final String identifier;
    private final Map<OnboardingSelectionKey, OnboardingExecutor> executors;

    /**
     * Initializes a new {@link IMAPOnboardingConfiguration}.
     */
    public IMAPOnboardingConfiguration(ServiceLookup services) {
        super();
        this.services = services;
        propertyPrefix = "com.openexchange.onboarding.imap";
        identifier = "imap";
        executors = new HashMap<OnboardingSelectionKey, OnboardingExecutor>(16);

        {
            OnboardingExecutor downloadExecutor = new OnboardingExecutor() {

                @Override
                public Result execute(OnboardingRequest request, Session session) throws OXException {
                    return generatePListResult(request, session);
                }
            };

            executors.put(keyFor(new DefaultEntityPath(this, Device.APPLE_IPAD, Module.EMAIL), OnboardingType.DOWNLOAD), downloadExecutor);
            executors.put(keyFor(new DefaultEntityPath(this, Device.APPLE_IPHONE, Module.EMAIL), OnboardingType.DOWNLOAD), downloadExecutor);
            executors.put(keyFor(new DefaultEntityPath(this, Device.APPLE_MAC, Module.EMAIL), OnboardingType.DOWNLOAD), downloadExecutor);
        }

        {
            OnboardingExecutor emailExecutor = new OnboardingExecutor() {

                @Override
                public Result execute(OnboardingRequest request, Session session) throws OXException {
                    return sendEmailResult(request, session);
                }
            };

            executors.put(keyFor(new DefaultEntityPath(this, Device.APPLE_IPAD, Module.EMAIL), OnboardingType.EMAIL), emailExecutor);
            executors.put(keyFor(new DefaultEntityPath(this, Device.APPLE_IPHONE, Module.EMAIL), OnboardingType.EMAIL), emailExecutor);
            executors.put(keyFor(new DefaultEntityPath(this, Device.APPLE_MAC, Module.EMAIL), OnboardingType.EMAIL), emailExecutor);
        }

        {
            OnboardingExecutor displayExecutor = new OnboardingExecutor() {

                @Override
                public Result execute(OnboardingRequest request, Session session) throws OXException {
                    return displayResult(request, session);
                }
            };

            executors.put(keyFor(new DefaultEntityPath(this, Device.APPLE_IPAD, Module.EMAIL), OnboardingType.DISPLAY), displayExecutor);
            executors.put(keyFor(new DefaultEntityPath(this, Device.APPLE_IPHONE, Module.EMAIL), OnboardingType.DISPLAY), displayExecutor);
            executors.put(keyFor(new DefaultEntityPath(this, Device.APPLE_MAC, Module.EMAIL), OnboardingType.DISPLAY), displayExecutor);
            executors.put(keyFor(new DefaultEntityPath(this, Device.ANDROID_PHONE, Module.EMAIL), OnboardingType.DISPLAY), displayExecutor);
            executors.put(keyFor(new DefaultEntityPath(this, Device.ANDROID_TABLET, Module.EMAIL), OnboardingType.DISPLAY), displayExecutor);
            executors.put(keyFor(new DefaultEntityPath(this, Device.WINDOWS_DESKTOP_8_10, Module.EMAIL), OnboardingType.DISPLAY), displayExecutor);
        }
    }

    @Override
    public String getId() {
        return identifier;
    }

    @Override
    public String getDisplayName(Session session) throws OXException {
        return OnboardingUtility.getTranslationFromProperty(propertyPrefix + ".displayName", session);
    }

    @Override
    public Icon getIcon(Session session) throws OXException {
        return OnboardingUtility.loadIconImageFromProperty(propertyPrefix + ".iconName", session);
    }

    @Override
    public String getDescription(Session session) throws OXException {
        return OnboardingUtility.getTranslationFromProperty(propertyPrefix + ".description", session);
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

        return OnboardingUtility.getBoolValue(propertyPrefix + ".enabled", true, session);
    }

    @Override
    public List<EntityPath> getEntityPaths(Session session) throws OXException {
        List<EntityPath> paths = new ArrayList<EntityPath>(6);
        paths.add(new DefaultEntityPath(this, Device.APPLE_IPAD, Module.EMAIL));
        paths.add(new DefaultEntityPath(this, Device.APPLE_IPHONE, Module.EMAIL));
        paths.add(new DefaultEntityPath(this, Device.APPLE_MAC, Module.EMAIL));
        paths.add(new DefaultEntityPath(this, Device.ANDROID_PHONE, Module.EMAIL));
        paths.add(new DefaultEntityPath(this, Device.ANDROID_TABLET, Module.EMAIL));
        paths.add(new DefaultEntityPath(this, Device.WINDOWS_DESKTOP_8_10, Module.EMAIL));
        return paths;
    }

    @Override
    public List<OnboardingSelection> getSelections(EntityPath entityPath, Session session) throws OXException {
        if (entityPath.matches(Device.APPLE_IPAD, Module.EMAIL, identifier)) {
            List<OnboardingSelection> selections = new ArrayList<OnboardingSelection>(4);

            // The download selection
            selections.add(DefaultOnboardingSelection.newInstance(entityPath, OnboardingType.DOWNLOAD));

            // The eMail selection
            selections.add(DefaultOnboardingSelection.newInstance(entityPath, OnboardingType.EMAIL));

            // The display settings selection
            selections.add(DefaultOnboardingSelection.newInstance(entityPath, OnboardingType.DISPLAY));

            return selections;
        } else if (entityPath.matches(Device.APPLE_IPHONE, Module.EMAIL, identifier)) {
            List<OnboardingSelection> selections = new ArrayList<OnboardingSelection>(4);

            // The download selection
            selections.add(DefaultOnboardingSelection.newInstance(entityPath, OnboardingType.DOWNLOAD));

            // The download selection
            selections.add(DefaultOnboardingSelection.newInstance(entityPath, OnboardingType.SMS));

            // The eMail selection
            selections.add(DefaultOnboardingSelection.newInstance(entityPath, OnboardingType.EMAIL));

            // The display settings selection
            selections.add(DefaultOnboardingSelection.newInstance(entityPath, OnboardingType.DISPLAY));

            return selections;

        } else if (entityPath.matches(Device.APPLE_MAC, Module.EMAIL, identifier)) {
            List<OnboardingSelection> selections = new ArrayList<OnboardingSelection>(4);

            // The download selection
            selections.add(DefaultOnboardingSelection.newInstance(entityPath, OnboardingType.DOWNLOAD));

            // The eMail selection
            selections.add(DefaultOnboardingSelection.newInstance(entityPath, OnboardingType.EMAIL));

            // The display settings selection
            selections.add(DefaultOnboardingSelection.newInstance(entityPath, OnboardingType.DISPLAY));

            return selections;
        } else if (entityPath.matches(Device.ANDROID_PHONE, Module.EMAIL, identifier)) {
            List<OnboardingSelection> selections = new ArrayList<OnboardingSelection>(4);

            // The display settings selection
            selections.add(DefaultOnboardingSelection.newInstance(entityPath, OnboardingType.DISPLAY));

            return selections;
        } else if (entityPath.matches(Device.ANDROID_TABLET, Module.EMAIL, identifier)) {
            List<OnboardingSelection> selections = new ArrayList<OnboardingSelection>(4);

            // The display settings selection
            selections.add(DefaultOnboardingSelection.newInstance(entityPath, OnboardingType.DISPLAY));

            return selections;
        } else if (entityPath.matches(Device.WINDOWS_DESKTOP_8_10, Module.EMAIL, identifier)) {
            List<OnboardingSelection> selections = new ArrayList<OnboardingSelection>(4);

            // The display settings selection
            selections.add(DefaultOnboardingSelection.newInstance(entityPath, OnboardingType.DISPLAY));

            return selections;
        }

        throw OnboardingExceptionCodes.ENTITY_NOT_SUPPORTED.create(entityPath.getCompositeId());
    }

    @Override
    public Result execute(OnboardingRequest request, Session session) throws OXException {
        OnboardingSelection selection = request.getSelection();
        if (!selection.isEnabled(session)) {
            throw OnboardingExceptionCodes.CONFIGURATION_NOT_SUPPORTED.create(selection.getCompositeId());
        }

        OnboardingExecutor onboardingExecutor = executors.get(new OnboardingSelectionKey(selection));
        if (null == onboardingExecutor) {
            throw OnboardingExceptionCodes.CONFIGURATION_NOT_SUPPORTED.create(selection.getCompositeId());
        }

        return onboardingExecutor.execute(request, session);
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

    Result displayResult(OnboardingRequest request, Session session) throws OXException {
        String resultText = OnboardingUtility.getTranslationFor(IMAPOnboardingStrings.IMAP_TEXT_SETTINGS, session);

        IMAPConfig imapConfig = new IMAPConfig(0);
        MailConfig.getConfig(imapConfig, session, 0);
        String imapServer = imapConfig.getServer();
        int imapPort = imapConfig.getPort();
        String imapLogin = imapConfig.getLogin();
        String imapPassword = imapConfig.getPassword();
        boolean imapSecure = imapConfig.isSecure();

        SMTPConfig smtpConfig = new SMTPConfig();
        TransportConfig.getTransportConfig(smtpConfig, session, 0);
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

        return new Result(resultText, formContent);
    }

    // --------------------------------------------- E-Mail utils --------------------------------------------------------------

    private TransportProvider getTransportProvider() {
        return TransportProviderRegistry.getTransportProvider("smtp");
    }

    Result sendEmailResult(OnboardingRequest request, Session session) throws OXException {
        Map<String, Object> formContent = request.getFormContent();
        if (null == formContent) {
            throw OnboardingExceptionCodes.MISSING_FORM_FIELD.create(CommonForms.EMAIL_ADDRESS.getFirstElementName());
        }

        String emailAddress = (String) formContent.get(CommonForms.EMAIL_ADDRESS.getFirstElementName());
        if (Strings.isEmpty(emailAddress)) {
            throw OnboardingExceptionCodes.MISSING_FORM_FIELD.create(CommonForms.EMAIL_ADDRESS.getFirstElementName());
        }

        MailTransport transport = getTransportProvider().createNewNoReplyTransport(session.getContextId());
        try {
            MailData data = OnboardingProfileCreatedNotificationMail.createProfileNotificationMail(emailAddress, request.getHostData().getHost(), session);

            PListDict pListDict = generatePList(request, session);
            ThresholdFileHolder fileHolder = new ThresholdFileHolder();
            fileHolder.setDisposition("attachment; filename=imap.mobileconfig");
            fileHolder.setName("imap.mobileconfig");
            fileHolder.setContentType("application/x-apple-aspen-config; charset=UTF-8; name=imap.mobileconfig"); // Or application/x-plist ?
            XMLStreamWriter writer = StaxUtils.createXMLStreamWriter(fileHolder.asOutputStream());
            pListDict.write(writer);
            NotificationMailFactory notify = services.getService(NotificationMailFactory.class);
            ComposedMailMessage message = notify.createMail(data, Collections.singleton((IFileHolder) fileHolder));
            transport.sendMailMessage(message, ComposeType.NEW);
        } catch (XMLStreamException e) {
            throw OnboardingExceptionCodes.XML_ERROR.create(e, e.getMessage());
        } finally {
            transport.close();
        }

        return new Result(OnboardingUtility.getTranslationFor(OnboardingStrings.RESULT_EMAIL_SENT, session));
    }

    // --------------------------------------------- PLIST utils --------------------------------------------------------------

    private static final String PROFILE_IMAP_DEFAULT_UUID = "b264c731-b93d-428e-8b7f-ae12db3726ef";
    private static final String PROFILE_IMAP_DEFAULT_CONTENT_UUID = "56f5eca3-4249-4e2c-8eba-34f6c8ed204b";

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

    PListDict generatePList(OnboardingRequest request, Session session) throws OXException {
        IMAPConfig imapConfig = new IMAPConfig(0);
        MailConfig.getConfig(imapConfig, session, 0);
        String imapServer = imapConfig.getServer();
        int imapPort = imapConfig.getPort();
        String imapLogin = imapConfig.getLogin();
        String imapPassword = imapConfig.getPassword();
        boolean imapSecure = imapConfig.isSecure();

        SMTPConfig smtpConfig = new SMTPConfig();
        TransportConfig.getTransportConfig(smtpConfig, session, 0);
        String smtpServer = smtpConfig.getServer();
        int smtpPort = smtpConfig.getPort();
        String smtpLogin = smtpConfig.getLogin();
        String smtpPassword = smtpConfig.getPassword();
        boolean smtpSecure = smtpConfig.isSecure();

        PListDict payloadContent = new PListDict();
        payloadContent.setPayloadType("com.apple.mail.managed");
        payloadContent.setPayloadUUID(OnboardingUtility.getValueFromProperty("com.openexchange.onboarding.imap.plist.payloadContentUUID", PROFILE_IMAP_DEFAULT_CONTENT_UUID, session));
        payloadContent.setPayloadIdentifier(OnboardingUtility.getValueFromProperty("com.openexchange.onboarding.imap.plist.payloadContentIdentifier", "com.open-xchange.imap", session));

        // A user-visible description of the email account, shown in the Mail and Settings applications.
        payloadContent.addStringValue("EmailAccountDescription", OnboardingUtility.getTranslationFromProperty("com.openexchange.onboarding.imap.plist.accountDescription", IMAPOnboardingStrings.IMAP_ACCOUNT_DESCRIPTION, true, session));

        // The full user name for the account. This is the user name in sent messages, etc.
        payloadContent.addStringValue("EmailAccountName", getUser(session).getDisplayName());

        // Allowed values are EmailTypePOP and EmailTypeIMAP. Defines the protocol to be used for that account.
        payloadContent.addStringValue("EmailAccountType", "EmailTypeIMAP");

        // Designates the full email address for the account. If not present in the payload, the device prompts for this string during profile installation.
        payloadContent.addStringValue("EmailAccountType", getUserSettingMail(session).getSendAddr());


        // Designates the authentication scheme for incoming mail. Allowed values are EmailAuthPassword and EmailAuthNone.
        payloadContent.addStringValue("IncomingMailServerAuthentication", "EmailAuthPassword");

        // Designates the incoming mail server host name (or IP address).
        payloadContent.addStringValue("IncomingMailServerHostName", imapServer);

        // Designates the incoming mail server port number. If no port number is specified, the default port for a given protocol is used.
        payloadContent.addIntegerValue("IncomingMailServerPortNumber", imapPort);

        // Designates whether the incoming mail server uses SSL for authentication. Default false.
        payloadContent.addBooleanValue("IncomingMailServerUseSSL", imapSecure);

        // Designates the user name for the email account, usually the same as the email address up to the @ character.
        // If not present in the payload, and the account is set up to require authentication for incoming email, the device will prompt for this string during profile installation.
        payloadContent.addStringValue("IncomingMailServerUsername", imapLogin);

        // Password for the Incoming Mail Server. Use only with encrypted profiles.
        payloadContent.addStringValue("IncomingPassword", imapPassword);


        // Password for the Outgoing Mail Server. Use only with encrypted profiles.
        payloadContent.addStringValue("OutgoingPassword", smtpPassword);

        // If set, the user will be prompted for the password only once and it will be used for both outgoing and incoming mail.
        payloadContent.addBooleanValue("OutgoingPasswordSameAsIncomingPassword", imapPassword.equals(smtpPassword));

        // Designates the authentication scheme for outgoing mail. Allowed values are EmailAuthPassword and EmailAuthNone.
        payloadContent.addStringValue("OutgoingMailServerAuthentication", "EmailAuthPassword");

        // Designates the outgoing mail server host name (or IP address).
        payloadContent.addStringValue("OutgoingMailServerHostName", smtpServer);

        // Designates the outgoing mail server port number. If no port number is specified, ports 25, 587 and 465 are used, in this order.
        payloadContent.addIntegerValue("OutgoingMailServerPortNumber", smtpPort);

        // Designates whether the outgoing mail server uses SSL for authentication. Default false.
        payloadContent.addBooleanValue("OutgoingMailServerUseSSL", smtpSecure);

        // Designates the user name for the email account, usually the same as the email address up to the @ character.
        // If not present in the payload, and the account is set up to require authentication for outgoing email, the device prompts for this string during profile installation.
        payloadContent.addStringValue("OutgoingMailServerUsername", smtpLogin);

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

        PListDict pListDict = new PListDict();
        pListDict.setPayloadIdentifier(OnboardingUtility.getValueFromProperty("com.openexchange.onboarding.imap.plist.payloadIdentifier", "com.open-xchange", session));
        pListDict.setPayloadType("Configuration");
        pListDict.setPayloadUUID(OnboardingUtility.getValueFromProperty("com.openexchange.onboarding.imap.plist.payloadUUID", PROFILE_IMAP_DEFAULT_UUID, session));
        pListDict.setPayloadVersion(1);
        pListDict.setPayloadContent(payloadContent);

        return pListDict;
    }

    Result generatePListResult(OnboardingRequest request, Session session) throws OXException {
        try {
            PListDict pListDict = generatePList(request, session);

            ThresholdFileHolder fileHolder = new ThresholdFileHolder();
            fileHolder.setDisposition("attachment");
            fileHolder.setName("imap.mobileconfig");
            fileHolder.setContentType("application/x-apple-aspen-config"); // Or application/x-plist ?
            fileHolder.setDelivery("download");
            XMLStreamWriter writer = StaxUtils.createXMLStreamWriter(fileHolder.asOutputStream());
            pListDict.write(writer);
            return new Result(fileHolder, "file");
        } catch (XMLStreamException e) {
            throw OnboardingExceptionCodes.XML_ERROR.create(e, e.getMessage());
        }
    }

}
