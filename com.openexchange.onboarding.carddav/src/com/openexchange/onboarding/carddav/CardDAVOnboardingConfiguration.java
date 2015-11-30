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

package com.openexchange.onboarding.carddav;

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
import com.openexchange.config.cascade.ComposedConfigProperty;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.userconfiguration.Permission;
import com.openexchange.java.Strings;
import com.openexchange.mail.dataobjects.compose.ComposeType;
import com.openexchange.mail.dataobjects.compose.ComposedMailMessage;
import com.openexchange.mail.transport.MailTransport;
import com.openexchange.mail.transport.TransportProvider;
import com.openexchange.mail.transport.TransportProviderRegistry;
import com.openexchange.notification.mail.MailData;
import com.openexchange.notification.mail.NotificationMailFactory;
import com.openexchange.onboarding.CommonForms;
import com.openexchange.onboarding.DefaultEntityPath;
import com.openexchange.onboarding.DefaultOnboardingSelection;
import com.openexchange.onboarding.Device;
import com.openexchange.onboarding.Module;
import com.openexchange.onboarding.OnboardingAction;
import com.openexchange.onboarding.EntityPath;
import com.openexchange.onboarding.Icon;
import com.openexchange.onboarding.OnboardingConfiguration;
import com.openexchange.onboarding.OnboardingExceptionCodes;
import com.openexchange.onboarding.OnboardingExecutor;
import com.openexchange.onboarding.OnboardingRequest;
import com.openexchange.onboarding.OnboardingSelection;
import com.openexchange.onboarding.OnboardingSelectionKey;
import com.openexchange.onboarding.OnboardingStrings;
import com.openexchange.onboarding.OnboardingUtility;
import com.openexchange.onboarding.Result;
import com.openexchange.onboarding.notification.mail.OnboardingProfileCreatedNotificationMail;
import com.openexchange.onboarding.plist.PListDict;
import com.openexchange.onboarding.plist.PListWriter;
import com.openexchange.onboarding.plist.xml.StaxUtils;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;


/**
 * {@link CardDAVOnboardingConfiguration}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public class CardDAVOnboardingConfiguration implements OnboardingConfiguration {

    private final ServiceLookup services;
    private final String propertyPrefix;
    private final String identifier;
    private final Map<OnboardingSelectionKey, OnboardingExecutor> executors;

    /**
     * Initializes a new {@link CardDAVOnboardingConfiguration}.
     */
    public CardDAVOnboardingConfiguration(ServiceLookup services) {
        super();
        this.services = services;
        propertyPrefix = "com.openexchange.onboarding.carddav";
        identifier = "carddav";
        executors = new HashMap<OnboardingSelectionKey, OnboardingExecutor>(8);

        {
            OnboardingExecutor downloadExecutor = new OnboardingExecutor() {

                @Override
                public Result execute(OnboardingRequest request, Session session) throws OXException {
                    return generatePListResult(request, session);
                }
            };

            executors.put(keyFor(new DefaultEntityPath(this, Device.APPLE_IPAD, Module.CONTACTS), OnboardingAction.DOWNLOAD), downloadExecutor);
            executors.put(keyFor(new DefaultEntityPath(this, Device.APPLE_IPHONE, Module.CONTACTS), OnboardingAction.DOWNLOAD), downloadExecutor);
            executors.put(keyFor(new DefaultEntityPath(this, Device.APPLE_MAC, Module.CONTACTS), OnboardingAction.DOWNLOAD), downloadExecutor);
        }

        {
            OnboardingExecutor emailExecutor = new OnboardingExecutor() {

                @Override
                public Result execute(OnboardingRequest request, Session session) throws OXException {
                    return sendEmailResult(request, session);
                }
            };

            executors.put(keyFor(new DefaultEntityPath(this, Device.APPLE_IPAD, Module.CONTACTS), OnboardingAction.EMAIL), emailExecutor);
            executors.put(keyFor(new DefaultEntityPath(this, Device.APPLE_IPHONE, Module.CONTACTS), OnboardingAction.EMAIL), emailExecutor);
            executors.put(keyFor(new DefaultEntityPath(this, Device.APPLE_MAC, Module.CONTACTS), OnboardingAction.EMAIL), emailExecutor);
        }

        {
            OnboardingExecutor displayExecutor = new OnboardingExecutor() {

                @Override
                public Result execute(OnboardingRequest request, Session session) throws OXException {
                    return displayResult(request, session);
                }
            };

            executors.put(keyFor(new DefaultEntityPath(this, Device.APPLE_IPAD, Module.CONTACTS), OnboardingAction.DISPLAY), displayExecutor);
            executors.put(keyFor(new DefaultEntityPath(this, Device.APPLE_IPHONE, Module.CONTACTS), OnboardingAction.DISPLAY), displayExecutor);
            executors.put(keyFor(new DefaultEntityPath(this, Device.APPLE_MAC, Module.CONTACTS), OnboardingAction.DISPLAY), displayExecutor);
        }
    }

    @Override
    public String getId() {
        return identifier;
    }

    @Override
    public String getDisplayName(Session session) throws OXException {
        return OnboardingUtility.getTranslationFromProperty(propertyPrefix + ".displayName", CardDAVOnboardingStrings.CARDDAV_DISPLAY_NAME, true, session);
    }

    @Override
    public Icon getIcon(Session session) throws OXException {
        return OnboardingUtility.loadIconImageFromProperty(propertyPrefix + ".iconName", session);
    }

    @Override
    public String getDescription(Session session) throws OXException {
        return OnboardingUtility.getTranslationFromProperty(propertyPrefix + ".description", CardDAVOnboardingStrings.CARDDAV_ACCOUNT_DESCRIPTION, true, session);
    }

    @Override
    public boolean isEnabled(Session session) throws OXException {
        CapabilityService capabilityService = services.getOptionalService(CapabilityService.class);
        if (null == capabilityService) {
            throw ServiceExceptionCode.absentService(CapabilityService.class);
        }

        if (false == capabilityService.getCapabilities(session).contains(Permission.CARDDAV.getCapabilityName())) {
            return false;
        }

        return OnboardingUtility.getBoolValue(propertyPrefix + ".enabled", true, session);
    }

    @Override
    public List<EntityPath> getEntityPaths(Session session) throws OXException {
        List<EntityPath> paths = new ArrayList<EntityPath>(4);
        paths.add(new DefaultEntityPath(this, Device.APPLE_IPAD, Module.CONTACTS));
        paths.add(new DefaultEntityPath(this, Device.APPLE_IPHONE, Module.CONTACTS));
        paths.add(new DefaultEntityPath(this, Device.APPLE_MAC, Module.CONTACTS));
        return paths;
    }

    @Override
    public List<OnboardingSelection> getSelections(EntityPath entityPath, Session session) throws OXException {
        if (entityPath.matches(Device.APPLE_IPAD, Module.CONTACTS, identifier)) {
            List<OnboardingSelection> selections = new ArrayList<OnboardingSelection>(4);

            // The download selection
            selections.add(DefaultOnboardingSelection.newInstance(entityPath, OnboardingAction.DOWNLOAD));

            // The eMail selection
            selections.add(DefaultOnboardingSelection.newInstance(entityPath, OnboardingAction.EMAIL));

            // The display settings selection
            selections.add(DefaultOnboardingSelection.newInstance(entityPath, OnboardingAction.DISPLAY));

            return selections;
        } else if (entityPath.matches(Device.APPLE_IPHONE, Module.CONTACTS, identifier)) {
            List<OnboardingSelection> selections = new ArrayList<OnboardingSelection>(4);

            // The download selection
            selections.add(DefaultOnboardingSelection.newInstance(entityPath, OnboardingAction.DOWNLOAD));

            // The download selection
            selections.add(DefaultOnboardingSelection.newInstance(entityPath, OnboardingAction.SMS));

            // The eMail selection
            selections.add(DefaultOnboardingSelection.newInstance(entityPath, OnboardingAction.EMAIL));

            // The display settings selection
            selections.add(DefaultOnboardingSelection.newInstance(entityPath, OnboardingAction.DISPLAY));

            return selections;

        } else if (entityPath.matches(Device.APPLE_MAC, Module.CONTACTS, identifier)) {
            List<OnboardingSelection> selections = new ArrayList<OnboardingSelection>(4);

            // The download selection
            selections.add(DefaultOnboardingSelection.newInstance(entityPath, OnboardingAction.DOWNLOAD));

            // The eMail selection
            selections.add(DefaultOnboardingSelection.newInstance(entityPath, OnboardingAction.EMAIL));

            // The display settings selection
            selections.add(DefaultOnboardingSelection.newInstance(entityPath, OnboardingAction.DISPLAY));

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

    // --------------------------------------------- Display utils --------------------------------------------------------------

    private final static String CARDDAV_LOGIN_FIELD = "login";
    private final static String CARDDAV_PASSWORD_FIELD = "password";
    private final static String CARDDAV_HOST_FIELD = "hostName";

    Result displayResult(OnboardingRequest request, Session session) throws OXException {
        String resultText = OnboardingUtility.getTranslationFor(CardDAVOnboardingStrings.CARDDAV_TEXT_SETTINGS, session);

        Map<String, Object> formContent = new HashMap<String, Object>();
        formContent.put(CARDDAV_LOGIN_FIELD, session.getLogin());
        formContent.put(CARDDAV_PASSWORD_FIELD, session.getPassword());
        formContent.put(CARDDAV_HOST_FIELD, getCardDAVUrl(request, session));

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
            PListWriter pListWriter = new PListWriter();
            ThresholdFileHolder fileHolder = new ThresholdFileHolder();
            fileHolder.setDisposition("attachment; filename=carddav.mobileconfig");
            fileHolder.setName("carddav.mobileconfig");
            fileHolder.setContentType("application/x-apple-aspen-config; charset=UTF-8; name=carddav.mobileconfig"); // Or application/x-plist ?
            XMLStreamWriter writer = StaxUtils.createXMLStreamWriter(fileHolder.asOutputStream());
            pListWriter.write(pListDict, writer);
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

    private static final String PROFILE_CARDDAV_DEFAULT_UUID = "f264c731-b93d-428e-8b7f-d158db3726ef";
    private static final String PROFILE_CARDDAV_DEFAULT_CONTENT_UUID = "a6f5eca3-4249-4e2c-8eba-4ae7c8ed204b";

    PListDict generatePList(OnboardingRequest request, Session session) throws OXException {
        PListDict payloadContent = new PListDict();
        payloadContent.setPayloadType("com.apple.carddav.account");
        payloadContent.setPayloadUUID(OnboardingUtility.getValueFromProperty("com.openexchange.onboarding.carddav.plist.payloadContentUUID", PROFILE_CARDDAV_DEFAULT_CONTENT_UUID, session));
        payloadContent.setPayloadIdentifier(OnboardingUtility.getValueFromProperty("com.openexchange.onboarding.carddav.plist.payloadContentIdentifier", "com.open-xchange.carddav", session));
        payloadContent.setPayloadVersion(1);
        payloadContent.addStringValue("PayloadOrganization", "OX");
        payloadContent.addStringValue("CardDAVUsername", session.getLogin());
        payloadContent.addStringValue("CardDAVPassword", session.getPassword());
        payloadContent.addStringValue("CardDAVHostName", getCardDAVUrl(request, session));
        payloadContent.addBooleanValue("CardDAVUseSSL", false);
        payloadContent.addStringValue("CardDAVAccountDescription", OnboardingUtility.getTranslationFromProperty("com.openexchange.onboarding.carddav.plist.accountDescription", CardDAVOnboardingStrings.CARDDAV_ACCOUNT_DESCRIPTION, true, session));

        PListDict pListDict = new PListDict();
        pListDict.setPayloadIdentifier(OnboardingUtility.getValueFromProperty("com.openexchange.onboarding.carddav.plist.payloadIdentifier", "com.open-xchange.carddav", session));
        pListDict.setPayloadType("Configuration");
        pListDict.setPayloadUUID(OnboardingUtility.getValueFromProperty("com.openexchange.onboarding.carddav.plist.payloadUUID", PROFILE_CARDDAV_DEFAULT_UUID, session));
        pListDict.setPayloadVersion(1);
        pListDict.setPayloadContent(payloadContent);
        pListDict.setPayloadDisplayName(CardDAVOnboardingStrings.CARDDAV_DISPLAY_NAME);

        return pListDict;
    }

    Result generatePListResult(OnboardingRequest request, Session session) throws OXException {
        try {
            PListDict pListDict = generatePList(request, session);
            PListWriter pListWriter = new PListWriter();

            ThresholdFileHolder fileHolder = new ThresholdFileHolder();
            fileHolder.setDisposition("attachment");
            fileHolder.setName("carddav.mobileconfig");
            fileHolder.setContentType("application/x-apple-aspen-config"); // Or application/x-plist ?
            fileHolder.setDelivery("download");
            XMLStreamWriter writer = StaxUtils.createXMLStreamWriter(fileHolder.asOutputStream());
            pListWriter.write(pListDict, writer);
            return new Result(fileHolder, "file");
        } catch (XMLStreamException e) {
            throw OnboardingExceptionCodes.XML_ERROR.create(e, e.getMessage());
        }
    }

    private String getCardDAVUrl(OnboardingRequest request, Session session) throws OXException {
        ConfigViewFactory viewFactory = services.getService(ConfigViewFactory.class);
        ConfigView view = viewFactory.getView(session.getUserId(), session.getContextId());
        ComposedConfigProperty<String> property = view.property("com.openexchange.onboarding.carddav.url", String.class);
        if (null == property || !property.isDefined()) {
            return OnboardingUtility.constructURLWithParameters(request.getHostData(), null, "/carddav", false, null).toString();
        }

        String value = property.get();
        if (Strings.isEmpty(value)) {
            return OnboardingUtility.constructURLWithParameters(request.getHostData(), null, "/carddav", false, null).toString();
        }

        return value;
    }

}
