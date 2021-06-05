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

package com.openexchange.client.onboarding;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import com.openexchange.client.onboarding.download.DownloadLinkProvider;
import com.openexchange.client.onboarding.osgi.Services;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.session.Session;
import com.openexchange.sms.SMSServiceSPI;

/**
 * {@link Device} - An enumeration for available on-boarding devices.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public enum Device implements Entity {

    /**
     * The device for an Apple Mac; <code>"apple.mac"</code>
     */
    APPLE_MAC(Platform.APPLE.getId() + ".mac", OnboardingStrings.DEVICE_APPLE_MAC_DISPLAY_NAME, "fa-laptop", Platform.APPLE),
    /**
     * The device for an Apple iPad; <code>"apple.ipad"</code>
     */
    APPLE_IPAD(Platform.APPLE.getId() + ".ipad", OnboardingStrings.DEVICE_APPLE_IPAD_DISPLAY_NAME, "fa-tablet", Platform.APPLE),
    /**
     * The device for an Apple iPhone; <code>"apple.iphone"</code>
     */
    APPLE_IPHONE(Platform.APPLE.getId() + ".iphone", OnboardingStrings.DEVICE_APPLE_IPHONE_DISPLAY_NAME, "fa-mobile", Platform.APPLE),

    /**
     * The device for an Android/Google tablet; <code>"android.tablet"</code>
     */
    ANDROID_TABLET(Platform.ANDROID_GOOGLE.getId() + ".tablet", OnboardingStrings.DEVICE_ANDROID_TABLET_DISPLAY_NAME, "fa-tablet", Platform.ANDROID_GOOGLE),
    /**
     * The device for an Android/Google phone; <code>"android.phone"</code>
     */
    ANDROID_PHONE(Platform.ANDROID_GOOGLE.getId() + ".phone", OnboardingStrings.DEVICE_ANDROID_PHONE_DISPLAY_NAME, "fa-mobile", Platform.ANDROID_GOOGLE),

    /**
     * The device for a Windows Desktop 8 + 10; <code>"windows.desktop"</code>
     */
    WINDOWS_DESKTOP_8_10(Platform.WINDOWS.getId() + ".desktop", OnboardingStrings.DEVICE_WINDOWS_DESKTOP_DISPLAY_NAME, "fa-laptop", Platform.WINDOWS),

    ;

    private final String id;
    private final FontAwesomeIcon icon;

    private final String enabledProperty;
    private final String displayNameProperty;
    private final String scenariosProperty;

    private final String defaultDisplayName;

    private final Platform platform;

    private Device(String id, String defaultDisplayName, String fontAwesomeName, Platform platform) {
        this.id = id;
        this.platform = platform;
        icon = new FontAwesomeIcon(fontAwesomeName);

        String prefix = "com.openexchange.client.onboarding." + id;
        enabledProperty = prefix + ".enabled";
        displayNameProperty = prefix + ".displayName";
        scenariosProperty = prefix + ".scenarios";

        this.defaultDisplayName = defaultDisplayName;
    }

    @Override
    public boolean isEnabled(Session session) throws OXException {
        return OnboardingUtility.getBoolValue(enabledProperty, true, session);
    }

    @Override
    public boolean isEnabled(int userId, int contextId) throws OXException {
        return OnboardingUtility.getBoolValue(enabledProperty, true, userId, contextId);
    }

    /**
     * Gets the platform associated with this entity
     *
     * @return The platform
     */
    public Platform getPlatform() {
        return platform;
    }

    /**
     * Checks if there are any user-associated scenarios for this device.
     *
     * @param session The session providing user data
     * @return <code>true</code> if there is at least one scenario; otherwise <code>false</code> if there is none
     * @throws OXException If scenarios cannot be checked
     */
    public boolean hasScenarios(Session session) throws OXException {
        String proprSceanrios = OnboardingUtility.getValueFromProperty(scenariosProperty, null, session);
        return (Strings.isNotEmpty(proprSceanrios));
    }

    /**
     * Gets the identifiers for the user-associated scenarios for this device.
     *
     * @param session The session providing user data
     * @return The identifiers for the user-associated scenarios
     * @throws OXException If scenarios cannot be returned
     */
    public List<String> getScenarios(Session session) throws OXException {
        String scenarioIds = OnboardingUtility.getValueFromProperty(scenariosProperty, null, session);
        if (Strings.isEmpty(scenarioIds)) {
            return Collections.emptyList();
        }
        return new ArrayList<String>(Arrays.asList(Strings.splitByComma(scenarioIds)));
    }

    @Override
    public String getDisplayName(Session session) throws OXException {
        return OnboardingUtility.getTranslationFromProperty(displayNameProperty, defaultDisplayName, true, session);
    }

    @Override
    public String getDisplayName(int userId, int contextId) throws OXException {
        return OnboardingUtility.getTranslationFromProperty(displayNameProperty, defaultDisplayName, true, userId, contextId);
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Icon getIcon(Session session) throws OXException {
        return icon;
    }

    @Override
    public String getDescription(Session session) throws OXException {
        return null;
    }

    /**
     * Gets the device for specified identifier
     *
     * @param id The identifier to look-up
     * @return The associated device or <code>null</code>
     */
    public static Device deviceFor(String id) {
        if (null == id) {
            return null;
        }

        for (Device device : values()) {
            if (id.equals(device.getId())) {
                return device;
            }
        }
        return null;
    }

    /**
     * Gets the available action for given device and type for session-associated user.
     *
     * @param clientDevice The client device, which is the target for the on-boarding action
     * @param device The device
     * @param type The type
     * @param session The session
     * @return The available actions
     * @throws OXException If actions cannot be returned
     */
    public static List<OnboardingAction> getActionsFor(ClientDevice clientDevice, Device device, OnboardingType type, Session session) throws OXException {
        return getActionsFor(clientDevice, device, type, session.getUserId(), session.getContextId());
    }

    /**
     * Gets the available action for given device and type for session-associated user.
     *
     * @param clientDevice The client device, which is the target for the on-boarding action
     * @param device The device
     * @param type The type
     * @param session The session
     * @return The available actions
     * @throws OXException If actions cannot be returned
     */
    public static List<OnboardingAction> getActionsFor(ClientDevice clientDevice, Device device, OnboardingType type, int userId, int contextId) throws OXException {
        if (null == device || null == type) {
            return Collections.emptyList();
        }

        // Only one action possible for types LINK and MANUAL
        switch (type) {
            case LINK:
                return Arrays.asList(OnboardingAction.LINK);
            case MANUAL:
                return Arrays.asList(OnboardingAction.DISPLAY);
            default:
                break;
        }

        // Check for other types
        List<OnboardingAction> actions = getConfiguredActionsFor(clientDevice, device, type, userId, contextId);
        if (null == actions) {
            // No actions available for specified device and type
            return Collections.emptyList();
        }

        for (Iterator<OnboardingAction> iter = actions.iterator(); iter.hasNext();) {
            OnboardingAction action = iter.next();
            switch (action) {
                case SMS:
                    // Check availability of needed services
                    if (null == Services.optService(SMSServiceSPI.class) || null == Services.optService(DownloadLinkProvider.class)) {
                        iter.remove();
                    }
                    break;
                case EMAIL:
                    if (!OnboardingUtility.hasNoReplyTransport(contextId)) {
                        iter.remove();
                    }
                    break;
                default:
                    // Nothing to do...
                    break;
            }
        }
        return actions;
    }

    private static List<OnboardingAction> getConfiguredActionsFor(ClientDevice clientDevice, Device device, OnboardingType type, int userId, int contextId) throws OXException {
        switch (device) {
            case ANDROID_PHONE:
                {
                    switch (type) {
                        case PLIST:
                            return null;
                        default:
                            throw new IllegalArgumentException("Unknown type: " + type.getId());
                    }
                }
            case ANDROID_TABLET:
                {
                    switch (type) {
                        case PLIST:
                            return null;
                        default:
                            throw new IllegalArgumentException("Unknown type: " + type.getId());
                    }
                }
            case APPLE_IPAD:
                {
                    switch (type) {
                        case PLIST:
                            {
                                if (null != clientDevice && clientDevice.matches(device)) {
                                    return queryActionsFor(device, type, Arrays.asList(OnboardingAction.DOWNLOAD), userId, contextId);
                                }
                                return queryActionsFor(device, type, Arrays.asList(OnboardingAction.EMAIL, OnboardingAction.DOWNLOAD), userId, contextId);
                            }
                        default:
                            throw new IllegalArgumentException("Unknown type: " + type.getId());
                    }
                }
            case APPLE_IPHONE:
                {
                    switch (type) {
                        case PLIST:
                            {
                                if (null != clientDevice && clientDevice.matches(device)) {
                                    return queryActionsFor(device, type, Arrays.asList(OnboardingAction.DOWNLOAD), userId, contextId);
                                }
                                return queryActionsFor(device, type, Arrays.asList(OnboardingAction.SMS, OnboardingAction.EMAIL), userId, contextId);
                            }
                        default:
                            throw new IllegalArgumentException("Unknown type: " + type.getId());
                    }
                }
            case APPLE_MAC:
                {
                    switch (type) {
                        case PLIST:
                            {
                                if (null != clientDevice && clientDevice.matches(device)) {
                                    return queryActionsFor(device, type, Arrays.asList(OnboardingAction.DOWNLOAD), userId, contextId);
                                }
                                return queryActionsFor(device, type, Arrays.asList(OnboardingAction.DOWNLOAD, OnboardingAction.EMAIL), userId, contextId);
                            }
                        default:
                            throw new IllegalArgumentException("Unknown type: " + type.getId());
                    }
                }
            case WINDOWS_DESKTOP_8_10:
                {
                    switch (type) {
                        case PLIST:
                            return null;
                        default:
                            throw new IllegalArgumentException("Unknown type: " + type.getId());
                    }
                }
            default:
                throw new IllegalArgumentException("Unknown device: " + device.id);
        }
    }

    private static List<OnboardingAction> queryActionsFor(Device device, OnboardingType type, List<OnboardingAction> defaultActions, int userId, int contextId) throws OXException {
        // Build property name
        String propName = new StringBuilder("com.openexchange.client.onboarding.").append(device.getId()).append('.').append(type.getId()).append(".actions").toString();

        // Query property value
        String propValue = OnboardingUtility.getValueFromProperty(propName, null, userId, contextId);
        if (Strings.isEmpty(propValue)) {
            return null == defaultActions ? null : new ArrayList<OnboardingAction>(defaultActions);
        }

        // Parse to actions
        String[] sActions = Strings.splitByComma(propValue);
        List<OnboardingAction> actions = new ArrayList<OnboardingAction>(sActions.length);
        for (String sAction : sActions) {
            sAction = Strings.asciiLowerCase(sAction);

            if ("none".equals(sAction)) {
                // Explicitly disabled
                return null;
            }

            OnboardingAction action = OnboardingAction.actionFor(sAction);
            if (null != action) {
                actions.add(action);
            }
        }

        return actions.isEmpty() ? (null == defaultActions ? null : new ArrayList<OnboardingAction>(defaultActions)) : actions;
    }

}
