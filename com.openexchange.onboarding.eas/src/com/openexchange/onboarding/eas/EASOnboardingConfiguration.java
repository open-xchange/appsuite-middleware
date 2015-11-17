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

package com.openexchange.onboarding.eas;

import static com.openexchange.datatypes.genericonf.FormElement.custom;
import static com.openexchange.onboarding.OnboardingUtility.isIPad;
import static com.openexchange.onboarding.OnboardingUtility.isIPhone;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import com.openexchange.ajax.container.ThresholdFileHolder;
import com.openexchange.ajax.fileholder.IFileHolder;
import com.openexchange.capabilities.CapabilityService;
import com.openexchange.capabilities.CapabilitySet;
import com.openexchange.config.cascade.ComposedConfigProperty;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.datatypes.genericonf.DynamicFormDescription;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.mail.dataobjects.compose.ComposeType;
import com.openexchange.mail.dataobjects.compose.ComposedMailMessage;
import com.openexchange.mail.transport.MailTransport;
import com.openexchange.mail.transport.TransportProvider;
import com.openexchange.mail.transport.TransportProviderRegistry;
import com.openexchange.notification.mail.MailData;
import com.openexchange.notification.mail.NotificationMailFactory;
import com.openexchange.onboarding.ClientInfo;
import com.openexchange.onboarding.CommonEntity;
import com.openexchange.onboarding.CommonFormDescription;
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
import com.openexchange.onboarding.OnboardingType;
import com.openexchange.onboarding.OnboardingUtility;
import com.openexchange.onboarding.Platform;
import com.openexchange.onboarding.Result;
import com.openexchange.onboarding.notification.mail.OnboardingProfileCreatedNotificationMail;
import com.openexchange.onboarding.plist.PListDict;
import com.openexchange.onboarding.plist.xml.StaxUtils;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;

/**
 * {@link EASOnboardingConfiguration}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.8.1
 */
public class EASOnboardingConfiguration implements OnboardingConfiguration {

    private final ServiceLookup services;
    private final String id;
    private final Map<String, OnboardingExecutor> executors;

    /**
     * Initializes a new {@link EASOnboardingConfiguration}.
     */
    public EASOnboardingConfiguration(ServiceLookup services) {
        super();
        this.services = services;
        id = "com.openexchange.onboarding.eas";
        executors = new HashMap<String, OnboardingExecutor>();

        executors.put("apple.ios.ipad.eas.download", new OnboardingExecutor() {

            @Override
            public Result execute(OnboardingRequest request, Session session) throws OXException {
                if (isIPad(request.getClientInfo())) {
                    return generatePListResult(request, session);
                }
                throw OnboardingExceptionCodes.CONFIGURATION_NOT_SUPPORTED.create(request.getSelectionId());
            }
        });

        executors.put("apple.ios.ipad.eas.email", new OnboardingExecutor() {

            @Override
            public Result execute(OnboardingRequest request, Session session) throws OXException {
                return sendEmailResult(request, session);
            }
        });

        executors.put("apple.ios.ipad.eas.display", new OnboardingExecutor() {

            @Override
            public Result execute(OnboardingRequest request, Session session) throws OXException {
                return displayResult(request, session);
            }
        });

        executors.put("apple.ios.iphone.eas.download", new OnboardingExecutor() {

            @Override
            public Result execute(OnboardingRequest request, Session session) throws OXException {
                if (isIPhone(request.getClientInfo())) {
                    return generatePListResult(request, session);
                }
                throw OnboardingExceptionCodes.CONFIGURATION_NOT_SUPPORTED.create(request.getSelectionId());
            }
        });

        executors.put("apple.ios.iphone.eas.email", new OnboardingExecutor() {

            @Override
            public Result execute(OnboardingRequest request, Session session) throws OXException {
                return sendEmailResult(request, session);
            }
        });

        executors.put("apple.ios.iphone.eas.display", new OnboardingExecutor() {

            @Override
            public Result execute(OnboardingRequest request, Session session) throws OXException {
                return displayResult(request, session);
            }
        });

        executors.put("apple.osx.eas.download", new OnboardingExecutor() {

            @Override
            public Result execute(OnboardingRequest request, Session session) throws OXException {
                return generatePListResult(request, session);
            }
        });

        executors.put("apple.osx.eas.email", new OnboardingExecutor() {

            @Override
            public Result execute(OnboardingRequest request, Session session) throws OXException {
                return sendEmailResult(request, session);
            }
        });

        executors.put("apple.osx.eas.display", new OnboardingExecutor() {

            @Override
            public Result execute(OnboardingRequest request, Session session) throws OXException {
                return displayResult(request, session);
            }
        });

        executors.put("android.phone.eas.display", new OnboardingExecutor() {

            @Override
            public Result execute(OnboardingRequest request, Session session) throws OXException {
                return displayResult(request, session);
            }
        });

        executors.put("android.tablet.eas.display", new OnboardingExecutor() {

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
        return OnboardingUtility.getTranslationFromProperty(id + ".displayName", EASOnboardingStrings.EAS_DISPLAY_NAME, true, session);
    }

    @Override
    public Icon getIcon(Session session) throws OXException {
        return OnboardingUtility.loadIconImageFromProperty("com.openexchange.onboarding.eas.icon", session);
    }

    @Override
    public boolean isEnabled(Session session) throws OXException {
        CapabilityService capabilityService = services.getService(CapabilityService.class);
        CapabilitySet caps = capabilityService.getCapabilities(session);
        return OnboardingUtility.getBoolValue("com.openexchange.onboarding.eas.enabled", true, session) && caps.contains("active_sync");
    }

    @Override
    public List<EntityPath> getEntityPaths(Session session) {
        List<EntityPath> paths = new ArrayList<EntityPath>(6);
        {
            List<Entity> path = new ArrayList<Entity>(4);
            path.add(CommonEntity.APPLE_IOS);
            path.add(CommonEntity.APPLE_IOS_IPAD);
            path.add(DefaultEntity.newInstance(CommonEntity.APPLE_IOS_IPAD.getId() + ".eas", "com.openexchange.onboarding.eas.", true));
            paths.add(new DefaultEntityPath(Platform.APPLE, path));
        }
        {
            List<Entity> path = new ArrayList<Entity>(4);
            path.add(CommonEntity.APPLE_IOS);
            path.add(CommonEntity.APPLE_IOS_IPHONE);
            path.add(DefaultEntity.newInstance(CommonEntity.APPLE_IOS_IPHONE.getId() + ".eas", "com.openexchange.onboarding.eas.", true));
            paths.add(new DefaultEntityPath(Platform.APPLE, path));
        }
        {
            List<Entity> path = new ArrayList<Entity>(4);
            path.add(CommonEntity.APPLE_OSX);
            path.add(DefaultEntity.newInstance(CommonEntity.APPLE_OSX.getId() + ".eas", "com.openexchange.onboarding.eas.", true));
            paths.add(new DefaultEntityPath(Platform.APPLE, path));
        }
        {
            List<Entity> path = new ArrayList<Entity>(4);
            path.add(CommonEntity.ANDROID_PHONE);
            path.add(DefaultEntity.newInstance(CommonEntity.ANDROID_PHONE.getId() + ".eas", "com.openexchange.onboarding.eas.", true));
            paths.add(new DefaultEntityPath(Platform.ANDROID_GOOGLE, path));
        }
        {
            List<Entity> path = new ArrayList<Entity>(4);
            path.add(CommonEntity.ANDROID_TABLET);
            path.add(DefaultEntity.newInstance(CommonEntity.ANDROID_TABLET.getId() + ".eas", "com.openexchange.onboarding.eas.", true));
            paths.add(new DefaultEntityPath(Platform.ANDROID_GOOGLE, path));
        }
        return paths;
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

    @Override
    public String getDescription(Session session) throws OXException {
        return OnboardingUtility.getTranslationFromProperty(id + ".description", EASOnboardingStrings.EAS_ACCOUNT_DESCRIPTION, true, session);
    }

    @Override
    public List<OnboardingSelection> getSelections(String lastEntityId, ClientInfo clientInfo, Session session) throws OXException {
        if ((CommonEntity.APPLE_IOS_IPAD.getId() + ".eas").equals(lastEntityId)) {
            List<OnboardingSelection> selections = new ArrayList<OnboardingSelection>(2);

            // Via download or eMail
            if (isIPad(clientInfo)) {
                // The download selection
                selections.add(DefaultOnboardingSelection.newInstance(CommonEntity.APPLE_IOS_IPAD.getId() + ".eas.download", id, "com.openexchange.onboarding.eas.download.", OnboardingType.DOWNLOAD));
            }

            // The eMail selection
            selections.add(DefaultOnboardingSelection.newInstance(CommonEntity.APPLE_IOS_IPAD.getId() + ".eas.email", id, "com.openexchange.onboarding.eas.email.", OnboardingType.EMAIL));

            // The display settings selection
            selections.add(DefaultOnboardingSelection.newInstance(CommonEntity.APPLE_IOS_IPAD.getId() + ".eas.display", id, "com.openexchange.onboarding.eas.display.", OnboardingType.DISPLAY));

            return selections;
        } else if ((CommonEntity.APPLE_IOS_IPHONE.getId() + ".eas").equals(lastEntityId)) {
            List<OnboardingSelection> selections = new ArrayList<OnboardingSelection>(3);

            // Via download, SMS or eMail
            if (isIPhone(clientInfo)) {
                // The download selection
                selections.add(DefaultOnboardingSelection.newInstance(CommonEntity.APPLE_IOS_IPHONE.getId() + ".eas.download", id, "com.openexchange.onboarding.eas.download.", OnboardingType.DOWNLOAD));

                // The SMS selection
                selections.add(DefaultOnboardingSelection.newInstance(CommonEntity.APPLE_IOS_IPHONE.getId() + ".eas.sms", id, "com.openexchange.onboarding.eas.sms.", OnboardingType.SMS));
            }

            // The eMail selection
            selections.add(DefaultOnboardingSelection.newInstance(CommonEntity.APPLE_IOS_IPHONE.getId() + ".eas.email", id, "com.openexchange.onboarding.eas.email.", OnboardingType.EMAIL));

            // The display settings selection
            selections.add(DefaultOnboardingSelection.newInstance(CommonEntity.APPLE_IOS_IPHONE.getId() + ".eas.display", id, "com.openexchange.onboarding.eas.display.", OnboardingType.DISPLAY));

            return selections;
        } else if ((CommonEntity.ANDROID_PHONE.getId() + ".eas").equals(lastEntityId)) {
            List<OnboardingSelection> selections = new ArrayList<OnboardingSelection>(1);
            selections.add(DefaultOnboardingSelection.newInstance(CommonEntity.ANDROID_PHONE.getId() + ".eas.display", id, "com.openexchange.onboarding.eas.download.", OnboardingType.DOWNLOAD));
            return selections;
        } else if ((CommonEntity.ANDROID_TABLET.getId() + ".eas").equals(lastEntityId)) {
            List<OnboardingSelection> selections = new ArrayList<OnboardingSelection>(1);
            selections.add(DefaultOnboardingSelection.newInstance(CommonEntity.ANDROID_TABLET.getId() + ".eas.display", id, "com.openexchange.onboarding.eas.download.", OnboardingType.DOWNLOAD));
            return selections;
        }

        throw OnboardingExceptionCodes.ENTITY_NOT_SUPPORTED.create(lastEntityId);
    }

    Result sendEmailResult(OnboardingRequest request, Session session) throws OXException {
        Map<String, Object> formContent = request.getFormContent();
        if (null == formContent) {
            throw OnboardingExceptionCodes.MISSING_FORM_FIELD.create(CommonFormDescription.EMAIL_ADDRESS.getFirstFormElementName());
        }

        String emailAddress = (String) formContent.get(CommonFormDescription.EMAIL_ADDRESS.getFirstFormElementName());
        if (Strings.isEmpty(emailAddress)) {
            throw OnboardingExceptionCodes.MISSING_FORM_FIELD.create(CommonFormDescription.EMAIL_ADDRESS.getFirstFormElementName());
        }

        MailTransport transport = getTransportProvider().createNewNoReplyTransport(session.getContextId());
        try {
            MailData data = OnboardingProfileCreatedNotificationMail.createProfileNotificationMail(emailAddress, request.getHostData().getHost(), session);

            PListDict pListDict = generatePList(request, session);
            ThresholdFileHolder fileHolder = new ThresholdFileHolder();
            fileHolder.setDisposition("attachment; filename=eas.mobileconfig");
            fileHolder.setName("eas.mobileconfig");
            fileHolder.setContentType("application/x-apple-aspen-config; charset=UTF-8; name=eas.mobileconfig"); // Or application/x-plist ?
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

    Result generatePListResult(OnboardingRequest request, Session session) throws OXException {
        try {
            PListDict pListDict = generatePList(request, session);

            ThresholdFileHolder fileHolder = new ThresholdFileHolder();
            fileHolder.setDisposition("attachment");
            fileHolder.setName("eas.mobileconfig");
            fileHolder.setContentType("application/xml"); // Or application/x-plist ?
            fileHolder.setDelivery("download");
            XMLStreamWriter writer = StaxUtils.createXMLStreamWriter(fileHolder.asOutputStream());
            pListDict.write(writer);
            return new Result(fileHolder, "file");
        } catch (XMLStreamException e) {
            throw OnboardingExceptionCodes.XML_ERROR.create(e, e.getMessage());
        }
    }

    Result displayResult(OnboardingRequest request, Session session) throws OXException {
        String resultText = OnboardingUtility.getTranslationFor(EASOnboardingStrings.EAS_TEXT_SETTINGS, session);

        DynamicFormDescription form = new DynamicFormDescription();
        form.add(custom("text", EAS_LOGIN_FIELD, OnboardingUtility.getTranslationFor(EASDisplayNames.EAS_LOGIN, session)))
            .add(custom("text", EAS_PASSWORD_FIELD, OnboardingUtility.getTranslationFor(EASDisplayNames.EAS_PASSWORD, session)))
            .add(custom("text", EAS_HOST_FIELD, OnboardingUtility.getTranslationFor(EASDisplayNames.EAS_HOST, session)));

        Map<String, Object> formContent = new HashMap<String, Object>();
        formContent.put(EAS_LOGIN_FIELD, session.getLogin());
        formContent.put(EAS_PASSWORD_FIELD, session.getPassword());
        formContent.put(EAS_HOST_FIELD, getEASUrl(request, session));

        return new Result(resultText, formContent, form);
    }

    private final static String EAS_LOGIN_FIELD = "login";
    private final static String EAS_PASSWORD_FIELD = "password";
    private final static String EAS_HOST_FIELD = "hostName";

    private PListDict generatePList(OnboardingRequest request, Session session) throws OXException {
        PListDict payloadContent = new PListDict();
        payloadContent.setPayloadType("com.apple.eas.account");
        payloadContent.setPayloadUUID(UUID.randomUUID().toString());
        payloadContent.setPayloadIdentifier("com.open-xchange.eas");
        payloadContent.addStringValue("UserName", session.getLogin());
        payloadContent.addStringValue("Password", session.getPassword());
        payloadContent.addStringValue("Host", getEASUrl(request, session));

        PListDict pListDict = new PListDict();
        pListDict.setPayloadIdentifier("com.open-xchange");
        pListDict.setPayloadType("Configuration");
        pListDict.setPayloadUUID(UUID.randomUUID().toString());
        pListDict.setPayloadVersion(1);
        pListDict.setPayloadContent(payloadContent);

        return pListDict;
    }

    private String getEASUrl(OnboardingRequest request, Session session) throws OXException {
        ConfigViewFactory viewFactory = services.getService(ConfigViewFactory.class);
        ConfigView view = viewFactory.getView(session.getUserId(), session.getContextId());
        ComposedConfigProperty<String> property = view.property("com.openexchange.onboarding.eas.url", String.class);
        if (null == property || !property.isDefined()) {
            return OnboardingUtility.constructURLWithParameters(request.getHostData(), null, "/Microsoft-Server-ActiveSync", false, null).toString();
        }

        String value = property.get();
        if (Strings.isEmpty(value)) {
            return OnboardingUtility.constructURLWithParameters(request.getHostData(), null, "/Microsoft-Server-ActiveSync", false, null).toString();
        }

        return value;
    }

    private TransportProvider getTransportProvider() {
        return TransportProviderRegistry.getTransportProvider("smtp");
    }

}
