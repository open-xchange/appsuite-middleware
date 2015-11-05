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
import java.util.List;
import com.openexchange.config.cascade.ComposedConfigProperty;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.exception.OXException;
import com.openexchange.onboarding.ClientInfo;
import com.openexchange.onboarding.DefaultEntity;
import com.openexchange.onboarding.DefaultEntityPath;
import com.openexchange.onboarding.DefaultOnboardingSelection;
import com.openexchange.onboarding.Entity;
import com.openexchange.onboarding.EntityPath;
import com.openexchange.onboarding.Icon;
import com.openexchange.onboarding.OnboardingConfiguration;
import com.openexchange.onboarding.OnboardingExceptionCodes;
import com.openexchange.onboarding.OnboardingRequest;
import com.openexchange.onboarding.OnboardingSelection;
import com.openexchange.onboarding.OnboardingUtility;
import com.openexchange.onboarding.Platform;
import com.openexchange.onboarding.Result;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.uadetector.UserAgentParser;
import net.sf.uadetector.OperatingSystem;
import net.sf.uadetector.OperatingSystemFamily;
import net.sf.uadetector.ReadableDeviceCategory;
import net.sf.uadetector.ReadableDeviceCategory.Category;
import net.sf.uadetector.ReadableUserAgent;


/**
 * {@link CalDAVOnboardingConfiguration}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public class CalDAVOnboardingConfiguration implements OnboardingConfiguration {

    private final ServiceLookup services;

    /**
     * Initializes a new {@link CalDAVOnboardingConfiguration}.
     */
    public CalDAVOnboardingConfiguration(ServiceLookup services) {
        super();
        this.services = services;
    }

    @Override
    public String getId() {
        return "com.openexchange.onboarding.caldav";
    }

    @Override
    public String getDisplayName(Session session) throws OXException {
        return OnboardingUtility.getTranslationFromProperty("com.openexchange.onboarding.caldav.displayName", session);
    }

    @Override
    public Icon getIcon(Session session) throws OXException {
        return OnboardingUtility.loadIconImageFromProperty("com.openexchange.onboarding.caldav.iconName", session);
    }

    @Override
    public String getDescription(Session session) throws OXException {
        return OnboardingUtility.getTranslationFromProperty("com.openexchange.onboarding.caldav.description", session);
    }

    @Override
    public boolean isEnabled(Session session) throws OXException {
        ConfigViewFactory viewFactory = services.getService(ConfigViewFactory.class);
        ConfigView view = viewFactory.getView(session.getUserId(), session.getContextId());
        ComposedConfigProperty<Boolean> property = view.property("com.openexchange.carddav.enabled", boolean.class);
        return null != property && property.isDefined() && property.get().booleanValue();
    }

    @Override
    public Platform getPlatform() throws OXException {
        return Platform.APPLE;
    }

    @Override
    public List<EntityPath> getEntityPaths(Session session) throws OXException {
        List<EntityPath> paths = new ArrayList<EntityPath>(6);
        {
            List<Entity> path = new ArrayList<Entity>(4);
            path.add(DefaultEntity.newInstance("apple.ios", "com.openexchange.onboarding.ios.", false));
            path.add(DefaultEntity.newInstance("apple.ios.ipad", "com.openexchange.onboarding.ipad.", false));
            path.add(DefaultEntity.newInstance("apple.ios.ipad.caldav", "com.openexchange.onboarding.caldav.", true));
            paths.add(new DefaultEntityPath(path));
        }
        {
            List<Entity> path = new ArrayList<Entity>(4);
            path.add(DefaultEntity.newInstance("apple.ios", "com.openexchange.onboarding.ios.", false));
            path.add(DefaultEntity.newInstance("apple.ios.iphone", "com.openexchange.onboarding.iphone.", false));
            path.add(DefaultEntity.newInstance("apple.ios.iphone.caldav", "com.openexchange.onboarding.caldav.", true));
            paths.add(new DefaultEntityPath(path));
        }
        return paths;
    }

    @Override
    public List<OnboardingSelection> getSelections(String lastEntityId, ClientInfo clientInfo, Session session) throws OXException {
        if ("apple.ios.ipad.caldav".equals(lastEntityId)) {
            List<OnboardingSelection> selections = new ArrayList<OnboardingSelection>(2);

            // Via download or eMail
            if (isIPad(clientInfo)) {
                // The download selection
                selections.add(DefaultOnboardingSelection.newInstance("apple.ios.ipad.caldav.download", "com.openexchange.onboarding.caldav.download."));
            }

            // The eMail selection
            selections.add(DefaultOnboardingSelection.newInstance("apple.ios.ipad.caldav.email", "com.openexchange.onboarding.caldav.email."));

            return selections;
        } else if ("apple.ios.iphone.caldav".equals(lastEntityId)) {
            List<OnboardingSelection> selections = new ArrayList<OnboardingSelection>(3);

            // Via download, SMS or eMail
            if (isIPhone(clientInfo)) {
                // The download selection
                selections.add(DefaultOnboardingSelection.newInstance("apple.ios.iphone.caldav.download", "com.openexchange.onboarding.caldav.download."));

                // The SMS selection
                selections.add(DefaultOnboardingSelection.newInstance("apple.ios.iphone.caldav.sms", "com.openexchange.onboarding.caldav.sms."));
            }

            // The eMail selection
            selections.add(DefaultOnboardingSelection.newInstance("apple.ios.iphone.caldav.email", "com.openexchange.onboarding.caldav.email."));

            return selections;
        }

        throw OnboardingExceptionCodes.ENTITY_NOT_SUPPORTED.create(lastEntityId);
    }

    @Override
    public Result execute(OnboardingRequest selection, Session session) throws OXException {
        String entityId = selection.getSelectionId();
        if (null == entityId) {
            throw OnboardingExceptionCodes.ENTITY_ID_MISSING.create();
        }

        if ("apple.ios.ipad.caldav".equals(entityId)) {
            if (isIPad(selection.getClientInfo())) {

            }
        } else if ("apple.ios.iphone.caldav".equals(entityId)) {
            if (isIPhone(selection.getClientInfo())) {

            }
        }

        return null;
    }

    // --------------------------------------------- User-Agent parsing --------------------------------------------------------------

    private boolean isIPad(ClientInfo clientInfo) {
        String userAgent = clientInfo.getUserAgent();
        if (null == userAgent) {
            return false;
        }

        UserAgentParser userAgentParser = services.getService(UserAgentParser.class);
        ReadableUserAgent agent = userAgentParser.parse(userAgent);

        OperatingSystem operatingSystem = agent.getOperatingSystem();
        if (!OperatingSystemFamily.IOS.equals(operatingSystem.getFamily())) {
            return false;
        }

        ReadableDeviceCategory deviceCategory = agent.getDeviceCategory();
        return Category.TABLET.equals(deviceCategory.getCategory());
    }

    private boolean isIPhone(ClientInfo clientInfo) {
        String userAgent = clientInfo.getUserAgent();
        if (null == userAgent) {
            return false;
        }

        UserAgentParser userAgentParser = services.getService(UserAgentParser.class);
        ReadableUserAgent agent = userAgentParser.parse(userAgent);

        OperatingSystem operatingSystem = agent.getOperatingSystem();
        if (!OperatingSystemFamily.IOS.equals(operatingSystem.getFamily())) {
            return false;
        }

        ReadableDeviceCategory deviceCategory = agent.getDeviceCategory();
        return Category.SMARTPHONE.equals(deviceCategory.getCategory());
    }

}
