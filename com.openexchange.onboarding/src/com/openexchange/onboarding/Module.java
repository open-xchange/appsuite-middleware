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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.onboarding;

import com.openexchange.exception.OXException;
import com.openexchange.session.Session;

/**
 * {@link Module} - An enumeration for available on-boarding modules.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public enum Module implements IdEntity {

    /**
     * The E-Mail module for accessing/synchronizing E-Mails.
     */
    EMAIL("email", OnboardingStrings.MODULE_EMAIL_DISPLAY_NAME, OnboardingStrings.MODULE_EMAIL_DESCRIPTION, "module_icon_email.png"),
    /**
     * The contacts module for accessing/synchronizing contacts.
     */
    CONTACTS("contacts", OnboardingStrings.MODULE_CONTACTS_DISPLAY_NAME, OnboardingStrings.MODULE_CONTACTS_DESCRIPTION, "module_icon_contacts.png"),
    /**
     * The calendar module for accessing/synchronizing events.
     */
    CALENDAR("calendar", OnboardingStrings.MODULE_CALENDAR_DISPLAY_NAME, OnboardingStrings.MODULE_CALENDAR_DESCRIPTION, "module_icon_calendar.png"),
    /**
     * The Drive module for accessing/synchronizing files.
     */
    DRIVE("drive", OnboardingStrings.MODULE_DRIVE_DISPLAY_NAME, OnboardingStrings.MODULE_DRIVE_DESCRIPTION, "module_icon_drive.png"),
    ;

    private final String id;

    private final String enabledProperty;
    private final String displayNameProperty;
    private final String iconProperty;
    private final String descriptionProperty;

    private final String defaultDisplayName;
    private final String defaultIcon;
    private final String defaultDescription;

    private Module(String id, String defaultDisplayName, String defaultDescription, String defaultIcon) {
        this.id = id;

        String prefix = "com.openexchange.onboarding.module." + id;
        enabledProperty = prefix + ".enabled";
        displayNameProperty = prefix + ".displayName";
        iconProperty = prefix + ".icon";
        descriptionProperty = prefix + ".description";

        this.defaultDisplayName = defaultDisplayName;
        this.defaultIcon = defaultIcon;
        this.defaultDescription = defaultDescription;
    }

    @Override
    public boolean isEnabled(Session session) throws OXException {
        return OnboardingUtility.getBoolValue(enabledProperty, true, session);
    }

    @Override
    public String getDisplayName(Session session) throws OXException {
        return OnboardingUtility.getTranslationFromProperty(displayNameProperty, defaultDisplayName, true, session);
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Icon getIcon(Session session) throws OXException {
        return OnboardingUtility.loadIconImageFromProperty(iconProperty, defaultIcon, session);
    }

    @Override
    public String getDescription(Session session) throws OXException {
        return OnboardingUtility.getTranslationFromProperty(descriptionProperty, defaultDescription, true, session);
    }

    /**
     * Gets the module for specified identifier
     *
     * @param id The identifier to look-up
     * @return The associated module or <code>null</code>
     */
    public static Module moduleFor(String id) {
        if (null == id) {
            return null;
        }

        for (Module module : values()) {
            if (id.equals(module.getId())) {
                return module;
            }
        }
        return null;
    }

}
