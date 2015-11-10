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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.openexchange.onboarding.caldav;

import static com.openexchange.datatypes.genericonf.FormElement.custom;
import static com.openexchange.onboarding.OnboardingUtility.isIPad;
import static com.openexchange.onboarding.OnboardingUtility.isIPhone;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.activation.DataHandler;
import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMessage.RecipientType;
import javax.mail.internet.MimeMultipart;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import com.openexchange.ajax.container.ThresholdFileHolder;
import com.openexchange.config.cascade.ComposedConfigProperty;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.datatypes.genericonf.DynamicFormDescription;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.java.UnsynchronizedStringWriter;
import com.openexchange.mail.dataobjects.compose.ComposeType;
import com.openexchange.mail.dataobjects.compose.ComposedMailMessage;
import com.openexchange.mail.dataobjects.compose.ContentAwareComposedMailMessage;
import com.openexchange.mail.mime.MessageHeaders;
import com.openexchange.mail.mime.MimeDefaultSession;
import com.openexchange.mail.mime.MimeMailException;
import com.openexchange.mail.mime.QuotedInternetAddress;
import com.openexchange.mail.mime.datasource.MessageDataSource;
import com.openexchange.mail.mime.utils.MimeMessageUtility;
import com.openexchange.mail.transport.MailTransport;
import com.openexchange.mail.transport.TransportProvider;
import com.openexchange.mail.transport.TransportProviderRegistry;
import com.openexchange.mail.usersetting.UserSettingMail;
import com.openexchange.mail.usersetting.UserSettingMailStorage;
import com.openexchange.mail.utils.MessageUtility;
import com.openexchange.onboarding.ClientInfo;
import com.openexchange.onboarding.CommonEntity;
import com.openexchange.onboarding.DefaultEntity;
import com.openexchange.onboarding.DefaultEntityPath;
import com.openexchange.onboarding.DefaultOnboardingSelection;
import com.openexchange.onboarding.Entity;
import com.openexchange.onboarding.EntityPath;
import com.openexchange.onboarding.Icon;
import com.openexchange.onboarding.OnboardingConfiguration;
import com.openexchange.onboarding.OnboardingExceptionCodes;
import com.openexchange.onboarding.OnboardingExecutor;
import com.openexchange.onboarding.OnboardingRequest;
import com.openexchange.onboarding.OnboardingSelection;
import com.openexchange.onboarding.OnboardingStrings;
import com.openexchange.onboarding.OnboardingUtility;
import com.openexchange.onboarding.Platform;
import com.openexchange.onboarding.Result;
import com.openexchange.onboarding.plist.PListDict;
import com.openexchange.onboarding.plist.xml.StaxUtils;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link CalDAVOnboardingConfiguration}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public class CalDAVOnboardingConfiguration implements OnboardingConfiguration {

    private final ServiceLookup services;
    private final String id;
    private final Map<String, OnboardingExecutor> executors;

    /**
     * Initializes a new {@link CalDAVOnboardingConfiguration}.
     */
    public CalDAVOnboardingConfiguration(ServiceLookup services) {
        super();
        this.services = services;
        id = "com.openexchange.onboarding.caldav";
        executors = new HashMap<String, OnboardingExecutor>(8);

        executors.put("apple.ios.ipad.caldav.download", new OnboardingExecutor() {

            @Override
            public Result execute(OnboardingRequest request, Session session) throws OXException {
                if (isIPad(request.getClientInfo())) {
                    return generatePListResult(request, session);
                }
                throw OnboardingExceptionCodes.CONFIGURATION_NOT_SUPPORTED.create(request.getSelectionId());
            }
        });

        executors.put("apple.ios.iphone.caldav.download", new OnboardingExecutor() {

            @Override
            public Result execute(OnboardingRequest request, Session session) throws OXException {
                if (isIPad(request.getClientInfo())) {
                    return generatePListResult(request, session);
                }
                throw OnboardingExceptionCodes.CONFIGURATION_NOT_SUPPORTED.create(request.getSelectionId());
            }
        });

        executors.put("apple.ios.ipad.caldav.email", new OnboardingExecutor() {

            @Override
            public Result execute(OnboardingRequest request, Session session) throws OXException {
                return sendEmailResult(request, session);
            }
        });

        executors.put("apple.ios.iphone.caldav.email", new OnboardingExecutor() {

            @Override
            public Result execute(OnboardingRequest request, Session session) throws OXException {
                return sendEmailResult(request, session);
            }
        });

        executors.put("apple.ios.ipad.caldav.display", new OnboardingExecutor() {

            @Override
            public Result execute(OnboardingRequest request, Session session) throws OXException {
                return displayResult(request, session);
            }
        });

        executors.put("apple.ios.iphone.caldav.display", new OnboardingExecutor() {

            @Override
            public Result execute(OnboardingRequest request, Session session) throws OXException {
                return displayResult(request, session);
            }
        });
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getDisplayName(Session session) throws OXException {
        return OnboardingUtility.getTranslationFromProperty(id + ".displayName", CalDAVOnboardingStrings.CALDAV_DISPLAY_NAME, true, session);
    }

    @Override
    public Icon getIcon(Session session) throws OXException {
        return OnboardingUtility.loadIconImageFromProperty(id + ".iconName", session);
    }

    @Override
    public String getDescription(Session session) throws OXException {
        return OnboardingUtility.getTranslationFromProperty(id + ".description", CalDAVOnboardingStrings.CALDAV_ACCOUNT_DESCRIPTION, true, session);
    }

    @Override
    public boolean isEnabled(Session session) throws OXException {
        ConfigViewFactory viewFactory = services.getService(ConfigViewFactory.class);
        ConfigView view = viewFactory.getView(session.getUserId(), session.getContextId());
        ComposedConfigProperty<Boolean> property = view.property("com.openexchange.caldav.enabled", boolean.class);
        return null != property && property.isDefined() && property.get().booleanValue();
    }

    @Override
    public List<EntityPath> getEntityPaths(Session session) throws OXException {
        List<EntityPath> paths = new ArrayList<EntityPath>(6);
        {
            List<Entity> path = new ArrayList<Entity>(4);
            path.add(CommonEntity.APPLE_IOS);
            path.add(CommonEntity.APPLE_IOS_IPAD);
            path.add(DefaultEntity.newInstance(CommonEntity.APPLE_IOS_IPAD.getId() + ".caldav", "com.openexchange.onboarding.caldav.", true));
            paths.add(new DefaultEntityPath(Platform.APPLE, path));
        }
        {
            List<Entity> path = new ArrayList<Entity>(4);
            path.add(CommonEntity.APPLE_IOS);
            path.add(CommonEntity.APPLE_IOS_IPHONE);
            path.add(DefaultEntity.newInstance(CommonEntity.APPLE_IOS_IPHONE.getId() + ".caldav", "com.openexchange.onboarding.caldav.", true));
            paths.add(new DefaultEntityPath(Platform.APPLE, path));
        }
        return paths;
    }

    @Override
    public List<OnboardingSelection> getSelections(String lastEntityId, ClientInfo clientInfo, Session session) throws OXException {
        if ((CommonEntity.APPLE_IOS_IPAD.getId() + ".caldav").equals(lastEntityId)) {
            List<OnboardingSelection> selections = new ArrayList<OnboardingSelection>(2);

            // Via download or eMail
            if (isIPad(clientInfo)) {
                // The download selection
                selections.add(DefaultOnboardingSelection.newInstance(CommonEntity.APPLE_IOS_IPAD.getId() + ".caldav.download", id, "com.openexchange.onboarding.caldav.download."));
            }

            // The eMail selection
            selections.add(DefaultOnboardingSelection.newInstance(CommonEntity.APPLE_IOS_IPAD.getId() + ".caldav.email", id, "com.openexchange.onboarding.caldav.email."));

            // The display settings selection
            selections.add(DefaultOnboardingSelection.newInstance(CommonEntity.APPLE_IOS_IPAD.getId() + ".caldav.display", id, "com.openexchange.onboarding.caldav.display."));

            return selections;
        } else if ((CommonEntity.APPLE_IOS_IPHONE.getId() + ".caldav").equals(lastEntityId)) {
            List<OnboardingSelection> selections = new ArrayList<OnboardingSelection>(3);

            // Via download, SMS or eMail
            if (isIPhone(clientInfo)) {
                // The download selection
                selections.add(DefaultOnboardingSelection.newInstance(CommonEntity.APPLE_IOS_IPHONE.getId() + ".caldav.download", id, "com.openexchange.onboarding.caldav.download."));

                // The SMS selection
                selections.add(DefaultOnboardingSelection.newInstance(CommonEntity.APPLE_IOS_IPHONE.getId() + ".caldav.sms", id, "com.openexchange.onboarding.caldav.sms."));
            }

            // The eMail selection
            selections.add(DefaultOnboardingSelection.newInstance(CommonEntity.APPLE_IOS_IPHONE.getId() + ".caldav.email", id, "com.openexchange.onboarding.caldav.email."));

            // The display settings selection
            selections.add(DefaultOnboardingSelection.newInstance(CommonEntity.APPLE_IOS_IPHONE.getId() + ".caldav.display", id, "com.openexchange.onboarding.caldav.display."));

            return selections;
        }

        throw OnboardingExceptionCodes.ENTITY_NOT_SUPPORTED.create(lastEntityId);
    }

    @Override
    public Result execute(OnboardingRequest request, Session session) throws OXException {
        String selectionId = request.getSelectionId();
        if (null == selectionId) {
            throw OnboardingExceptionCodes.CONFIGURATION_ID_MISSING.create();
        }

        OnboardingExecutor onboardingExecutor = executors.get(selectionId);
        if (null == onboardingExecutor) {
            throw OnboardingExceptionCodes.CONFIGURATION_NOT_SUPPORTED.create(selectionId);
        }

        return onboardingExecutor.execute(request, session);
    }

    // --------------------------------------------- Display utils --------------------------------------------------------------

    private final static String CALDAV_LOGIN_FIELD = "login";
    private final static String CALDAV_PASSWORD_FIELD = "password";
    private final static String CALDAV_HOST_FIELD = "hostName";

    Result displayResult(OnboardingRequest request, Session session) throws OXException {
        String resultText = OnboardingUtility.getTranslationFor(CalDAVOnboardingStrings.CALDAV_TEXT_SETTINGS, session);

        DynamicFormDescription form = new DynamicFormDescription();
        form.add(custom("text", CALDAV_LOGIN_FIELD, CalDAVFormDisplayNames.CALDAV_LOGIN_DISPLAY_NAME))
            .add(custom("text", CALDAV_PASSWORD_FIELD, CalDAVFormDisplayNames.CALDAV_PASSWORD_DISPLAY_NAME))
            .add(custom("text", CALDAV_HOST_FIELD, CalDAVFormDisplayNames.CALDAV_HOST_DISPLAY_NAME));

        Map<String, Object> formContent = new HashMap<String, Object>();
        formContent.put(CALDAV_LOGIN_FIELD, session.getLogin());
        formContent.put(CALDAV_PASSWORD_FIELD, session.getPassword());
        formContent.put(CALDAV_HOST_FIELD, getCalDAVUrl(request, session));

        return new Result(resultText, formContent, form);
    }


    // --------------------------------------------- E-Mail utils --------------------------------------------------------------

    private TransportProvider getTransportProvider() {
        return TransportProviderRegistry.getTransportProvider("smtp");
    }

    private UserSettingMail getUserSettingMail(Session session) throws OXException {
        if (session instanceof ServerSession) {
            return ((ServerSession) session).getUserSettingMail();
        }

        return UserSettingMailStorage.getInstance().getUserSettingMail(session);
    }

    Result sendEmailResult(OnboardingRequest request, Session session) throws OXException {
        MailTransport transport = getTransportProvider().createNewNoReplyTransport(session.getContextId());
        try {
            MimeMessage mimeMessage = new MimeMessage(MimeDefaultSession.getDefaultSession());

            {
                UserSettingMail userSettingMail = getUserSettingMail(session);
                mimeMessage.setRecipient(RecipientType.TO, new QuotedInternetAddress(userSettingMail.getSendAddr()));
                mimeMessage.setSubject(OnboardingUtility.getTranslationFor(CalDAVOnboardingStrings.CALDAV_TEXT_PROFILE, session), "UTF-8");
                mimeMessage.setHeader("Auto-Submitted", "auto-generated");
            }

            MimeMultipart mimeMultipart = new MimeMultipart();

            {
                MimeBodyPart textBodyPart = new MimeBodyPart();
                MessageUtility.setText(OnboardingUtility.getTranslationFor(CalDAVOnboardingStrings.CALDAV_TEXT_PROFILE, session), "UTF-8", textBodyPart);
                textBodyPart.setHeader(MessageHeaders.HDR_MIME_VERSION, "1.0");
                textBodyPart.setHeader(MessageHeaders.HDR_CONTENT_TYPE, MimeMessageUtility.foldContentType("text/plain; charset=UTF-8"));
                mimeMultipart.addBodyPart(textBodyPart);
            }

            try {
                MimeBodyPart plistBodyPart = new MimeBodyPart();
                UnsynchronizedStringWriter writer = new UnsynchronizedStringWriter(2048);
                PListDict pListDict = generatePList(request, session);
                pListDict.write(StaxUtils.createXMLStreamWriter(writer));
                plistBodyPart.setDataHandler(new DataHandler(new MessageDataSource(writer.toString(), "application/xml")));

                plistBodyPart.setHeader(MessageHeaders.HDR_MIME_VERSION, "1.0");
                plistBodyPart.setHeader(MessageHeaders.HDR_CONTENT_TYPE, MimeMessageUtility.foldContentType("application/xml; charset=UTF-8; name=caldav.plist"));
                plistBodyPart.setHeader(MessageHeaders.HDR_CONTENT_DISPOSITION, MimeMessageUtility.foldContentType("attachment; filename=caldav.plist"));
                mimeMultipart.addBodyPart(plistBodyPart);
            } catch (final UnsupportedEncodingException e) {
                throw new MessagingException("Unsupported encoding.", e);
            }

            mimeMessage.setContent(mimeMultipart);
            mimeMessage.saveChanges();

            ComposedMailMessage composedMailMessage = new ContentAwareComposedMailMessage(mimeMessage, session.getContextId());
            transport.sendMailMessage(composedMailMessage, ComposeType.NEW);
        } catch (MessagingException e) {
            throw MimeMailException.handleMessagingException(e);
        } catch (XMLStreamException e) {
            throw OnboardingExceptionCodes.XML_ERROR.create(e, e.getMessage());
        } finally {
            transport.close();
        }

        return new Result(OnboardingUtility.getTranslationFor(OnboardingStrings.RESULT_EMAIL_SENT, session));
    }

    // --------------------------------------------- PLIST utils --------------------------------------------------------------

    private static final String PROFILE_CALDAV_DEFAULT_UUID = "c454c731-b93d-428e-8b7f-d158db3726ef";
    private static final String PROFILE_CALDAV_DEFAULT_CONTENT_UUID = "6af5eca3-4249-4e2c-8eba-4ae7c8ed204b";

    PListDict generatePList(OnboardingRequest request, Session session) throws OXException {
        PListDict payloadContent = new PListDict();
        payloadContent.setPayloadType("com.apple.caldav.account");
        payloadContent.setPayloadUUID(OnboardingUtility.getValueFromProperty("com.openexchange.onboarding.caldav.plist.payloadContentUUID", PROFILE_CALDAV_DEFAULT_CONTENT_UUID, session));
        payloadContent.setPayloadIdentifier(OnboardingUtility.getValueFromProperty("com.openexchange.onboarding.caldav.plist.payloadContentIdentifier", "com.open-xchange.caldav", session));
        payloadContent.addStringValue("CalDAVUsername", session.getLogin());
        payloadContent.addStringValue("CalDAVPassword", session.getPassword());
        payloadContent.addStringValue("CalDAVHostname", getCalDAVUrl(request, session));
        payloadContent.addStringValue("CardDAVAccountDescription", OnboardingUtility.getTranslationFromProperty("com.openexchange.onboarding.caldav.plist.accountDescription", CalDAVOnboardingStrings.CALDAV_ACCOUNT_DESCRIPTION, true, session));

        PListDict pListDict = new PListDict();
        pListDict.setPayloadIdentifier(OnboardingUtility.getValueFromProperty("com.openexchange.onboarding.caldav.plist.payloadIdentifier", "com.open-xchange", session));
        pListDict.setPayloadType("Configuration");
        pListDict.setPayloadUUID(OnboardingUtility.getValueFromProperty("com.openexchange.onboarding.caldav.plist.payloadUUID", PROFILE_CALDAV_DEFAULT_UUID, session));
        pListDict.setPayloadVersion(1);
        pListDict.setPayloadContent(payloadContent);

        return pListDict;
    }

    Result generatePListResult(OnboardingRequest request, Session session) throws OXException {
        try {
            PListDict pListDict = generatePList(request, session);

            ThresholdFileHolder fileHolder = new ThresholdFileHolder();
            fileHolder.setDisposition("attachment");
            fileHolder.setName("caldav.mobileconfig");
            fileHolder.setContentType("application/xml"); // Or application/x-plist ?
            fileHolder.setDelivery("download");
            XMLStreamWriter writer = StaxUtils.createXMLStreamWriter(fileHolder.asOutputStream());
            pListDict.write(writer);
            return new Result(fileHolder, "file");
        } catch (XMLStreamException e) {
            throw OnboardingExceptionCodes.XML_ERROR.create(e, e.getMessage());
        }
    }

    private String getCalDAVUrl(OnboardingRequest request, Session session) throws OXException {
        ConfigViewFactory viewFactory = services.getService(ConfigViewFactory.class);
        ConfigView view = viewFactory.getView(session.getUserId(), session.getContextId());
        ComposedConfigProperty<String> property = view.property("com.openexchange.onboarding.caldav.url", String.class);
        if (null == property || !property.isDefined()) {
            return OnboardingUtility.constructURLWithParameters(request.getHostData(), null, "/caldav", false, null).toString();
        }

        String value = property.get();
        if (Strings.isEmpty(value)) {
            return OnboardingUtility.constructURLWithParameters(request.getHostData(), null, "/caldav", false, null).toString();
        }

        return value;
    }

}
