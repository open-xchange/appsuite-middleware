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

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import com.openexchange.onboarding.Device;
import com.openexchange.onboarding.DisplayResult;
import com.openexchange.onboarding.Icon;
import com.openexchange.onboarding.ObjectResult;
import com.openexchange.onboarding.OnboardingAction;
import com.openexchange.onboarding.OnboardingProvider;
import com.openexchange.onboarding.OnboardingExceptionCodes;
import com.openexchange.onboarding.OnboardingRequest;
import com.openexchange.onboarding.OnboardingStrings;
import com.openexchange.onboarding.OnboardingUtility;
import com.openexchange.onboarding.Result;
import com.openexchange.onboarding.Scenario;
import com.openexchange.onboarding.notification.mail.OnboardingProfileCreatedNotificationMail;
import com.openexchange.onboarding.plist.PListDict;
import com.openexchange.onboarding.plist.PListWriter;
import com.openexchange.onboarding.plist.xml.StaxUtils;
import com.openexchange.onboarding.signature.PListSigner;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;

/**
 * {@link CalDAVOnboardingProvider}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public class CalDAVOnboardingProvider implements OnboardingProvider {

    private final ServiceLookup services;
    private final String identifier;
    private final EnumSet<Device> supportedDevices;

    /**
     * Initializes a new {@link CalDAVOnboardingProvider}.
     */
    public CalDAVOnboardingProvider(ServiceLookup services) {
        super();
        this.services = services;
        identifier = "caldav";
        supportedDevices = EnumSet.of(Device.APPLE_IPAD, Device.APPLE_IPHONE, Device.APPLE_MAC);
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
            throw OnboardingExceptionCodes.UNSUPPORTED_ACTION.create(identifier, request.getAction().getId(), scenario.getType().getId());
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

    private Result doExecutePlist(OnboardingRequest request, Result previousResult, Session session) {


    }

    private Result doExecuteManual(OnboardingRequest request, Result previousResult, Session session) throws OXException {
        return displayResult(request, previousResult, session);
    }

    // --------------------------------------------- Display utils --------------------------------------------------------------


    private final static String CALDAV_LOGIN_FIELD = "login";
    private final static String CALDAV_PASSWORD_FIELD = "password";
    private final static String CALDAV_HOST_FIELD = "hostName";

    private Result displayResult(OnboardingRequest request, Result previousResult, Session session) throws OXException {
        Map<String, Object> configuration = null == previousResult ? new HashMap<String, Object>() : ((DisplayResult) previousResult).getConfiguration();
        configuration.put(CALDAV_LOGIN_FIELD, session.getLogin());
        configuration.put(CALDAV_PASSWORD_FIELD, session.getPassword());
        configuration.put(CALDAV_HOST_FIELD, getCalDAVUrl(request, session));
        return new DisplayResult(configuration);
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
            fileHolder.setDisposition("attachment; filename=caldav.mobileconfig");
            fileHolder.setName("caldav.mobileconfig");
            fileHolder.setContentType("application/x-apple-aspen-config; charset=UTF-8; name=caldav.mobileconfig");// Or application/x-plist ?
            XMLStreamWriter writer = StaxUtils.createXMLStreamWriter(fileHolder.asOutputStream());
            pListWriter.write(pListDict, writer);
            PListSigner signer = new PListSigner(fileHolder);
            fileHolder = signer.signPList();
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

    private static final String PROFILE_CALDAV_DEFAULT_UUID = "c454c731-b93d-428e-8b7f-d158db3726ef";
    private static final String PROFILE_CALDAV_DEFAULT_CONTENT_UUID = "6af5eca3-4249-4e2c-8eba-4ae7c8ed204b";

    PListDict generatePList(OnboardingRequest request, Session session) throws OXException {
        PListDict payloadContent = new PListDict();
        payloadContent.setPayloadType("com.apple.caldav.account");
        payloadContent.setPayloadUUID(OnboardingUtility.getValueFromProperty("com.openexchange.onboarding.caldav.plist.payloadContentUUID", PROFILE_CALDAV_DEFAULT_CONTENT_UUID, session));
        payloadContent.setPayloadIdentifier(OnboardingUtility.getValueFromProperty("com.openexchange.onboarding.caldav.plist.payloadContentIdentifier", "com.open-xchange.caldav", session));
        payloadContent.setPayloadVersion(1);
        payloadContent.addStringValue("PayloadOrganization", "OX");
        payloadContent.addStringValue("CalDAVUsername", session.getLogin());
        payloadContent.addStringValue("CalDAVPassword", session.getPassword());
        payloadContent.addStringValue("CalDAVHostName", getCalDAVUrl(request, session));
        payloadContent.addBooleanValue("CalDAVUseSSL", false);
        payloadContent.addStringValue("CalDAVAccountDescription", OnboardingUtility.getTranslationFromProperty("com.openexchange.onboarding.caldav.plist.accountDescription", CalDAVOnboardingStrings.CALDAV_ACCOUNT_DESCRIPTION, true, session));

        PListDict pListDict = new PListDict();
        pListDict.setPayloadIdentifier(OnboardingUtility.getValueFromProperty("com.openexchange.onboarding.caldav.plist.payloadIdentifier", "com.open-xchange.caldav", session));
        pListDict.setPayloadType("Configuration");
        pListDict.setPayloadUUID(OnboardingUtility.getValueFromProperty("com.openexchange.onboarding.caldav.plist.payloadUUID", PROFILE_CALDAV_DEFAULT_UUID, session));
        pListDict.setPayloadVersion(1);
        pListDict.setPayloadContent(payloadContent);
        pListDict.setPayloadDisplayName(CalDAVOnboardingStrings.CALDAV_DISPLAY_NAME);

        return pListDict;
    }

    Result generatePListResult(OnboardingRequest request, Session session) throws OXException {
        try {
            PListDict pListDict = generatePList(request, session);
            PListWriter pListWriter = new PListWriter();
            ThresholdFileHolder fileHolder = new ThresholdFileHolder();
            fileHolder.setDisposition("attachment");
            fileHolder.setName("caldav.mobileconfig");
            fileHolder.setContentType("application/x-apple-aspen-config");// Or application/x-plist ?
            fileHolder.setDelivery("download");
            XMLStreamWriter writer = StaxUtils.createXMLStreamWriter(fileHolder.asOutputStream());
            pListWriter.write(pListDict, writer);
            PListSigner signer = new PListSigner(fileHolder);
            fileHolder = signer.signPList();
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
