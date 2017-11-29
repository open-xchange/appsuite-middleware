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
 *    trademarks of the OX Software GmbH group of companies.
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
 *     Copyright (C) 2016-2020 OX Software GmbH
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

package com.openexchange.find.util;

import java.util.Locale;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.contact.ContactProperty;
import com.openexchange.exception.OXException;
import com.openexchange.find.contacts.ContactDisplayNameFormat;
import com.openexchange.find.facet.ComplexDisplayItem;
import com.openexchange.find.facet.DisplayItem;
import com.openexchange.find.osgi.Services;
import com.openexchange.groupware.contact.ContactUtil;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.i18n.I18nService;
import com.openexchange.i18n.I18nServiceRegistry;
import com.openexchange.image.ImageDataSource;
import com.openexchange.image.ImageLocation;
import com.openexchange.java.Strings;
import com.openexchange.java.util.Pair;

/**
 * A helper class to create {@link DisplayItem}s for common cases.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.6.1
 */
public class DisplayItems {

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(DisplayItems.class);

    /**
     * Converts the specified {@link Contact} result into a {@link ComplexDisplayItem}
     *
     * @param contact the {@link Contact} to convert
     * @param locale the user's {@link Locale}
     * @return The {@link ComplexDisplayItem}
     */
    public static ComplexDisplayItem convert(Contact contact, Locale locale) {
        String displayName = formatDisplayName(contact, locale);
        String primaryAddress = extractPrimaryMailAddress(contact);
        if (Strings.isEmpty(displayName)) {
            displayName = Strings.isEmpty(primaryAddress) ? "" : primaryAddress;
        }

        ComplexDisplayItem item = new ComplexDisplayItem(displayName, primaryAddress);
        Pair<ImageDataSource, ImageLocation> imageData = ContactUtil.prepareImageData(contact);
        if (imageData != null) {
            item.setImageData(imageData.getFirst(), imageData.getSecond());
        }

        return item;
    }

    /**
     * Formats the display name of the {@link ComplexDisplayItem}
     *
     * @param contact The {@link Contact}
     * @param locale The locale to use when formating the display name
     * @return the display name
     */
    private static String formatDisplayName(Contact contact, Locale locale) {
        String template = getTemplate(locale, hasDepartment(contact));
        String lastName = contact.getSurName();
        String firstName = contact.getGivenName();
        String department = Strings.isEmpty(contact.getDepartment()) ? "" : contact.getDepartment();
        if (Strings.isEmpty(lastName)) {
            if (!Strings.isEmpty(firstName)) {
                return String.format(locale, template, firstName, "", department);
            }
        } else {
            if (Strings.isEmpty(firstName)) {
                firstName = "";
            }
            return String.format(locale, template, firstName, lastName, department);
        }

        return String.format(locale, template, contact.getDisplayName(), "", department);
    }

    /**
     * Determines whether the specified {@link Contact} has the department set and is an entry in the GAB (Global Address Book)
     *
     * @param contact The {@link Contact}
     * @return <code>true</code> if it has the department set and is an entry in the GAB, <code>false</code> otherwise
     */
    private static boolean hasDepartment(Contact contact) {
        return contact.containsDepartment() && !Strings.isEmpty(contact.getDepartment()) && contact.getParentFolderID() == FolderObject.SYSTEM_LDAP_FOLDER_ID;
    }

    /**
     * Get the display name template to use
     *
     * @param locale The locale to use for the translation of the template
     * @param hasDepartment Whether the contact has the department field set
     * @return The display name template
     */
    private static String getTemplate(Locale locale, boolean hasDepartment) {
        String toLocalise = hasDepartment && showDepartments() ? ContactDisplayNameFormat.DISPLAY_NAME_FORMAT_WITH_DEPARTMENT : ContactDisplayNameFormat.DISPLAY_NAME_FORMAT_WITHOUT_DEPARTMENT;
        I18nServiceRegistry registry = Services.optService(I18nServiceRegistry.class);
        if (registry == null) {
            LOGGER.warn("No such service: {}. Returning default template '{}' for display name format", toLocalise, I18nServiceRegistry.class);
            return toLocalise;
        }
        try {
            I18nService i18nService = registry.getI18nService(locale);
            if (i18nService == null) {
                LOGGER.debug("No i18n service for locale {}.", locale);
                return toLocalise;
            }
            return i18nService.getLocalized(toLocalise);
        } catch (OXException e) {
            LOGGER.debug("An error occurred while translating the template '{}' using the locale '{}': {}", toLocalise, locale, e.getMessage(), e);
        }
        return toLocalise;
    }

    /**
     * Look-up the property
     *
     * @return <code>true</code> to show departments, <code>false</code> otherwise
     */
    private static boolean showDepartments() {
        LeanConfigurationService configService = Services.optService(LeanConfigurationService.class);
        if (null == configService) {
            boolean defaultValue = false;
            LOGGER.warn("No such service: {}. Assuming default value of '{}' for property '{}'", LeanConfigurationService.class.getName(), defaultValue, ContactProperty.showDepartment);
            return defaultValue;
        }
        return configService.getBooleanProperty(ContactProperty.showDepartment);
    }

    /**
     * Extracts the primary e-mail address of the specified {@link Contact}.
     *
     * @param contact The {@link Contact} to extract the e-mail address from
     * @return The primary e-mail address
     */
    private static String extractPrimaryMailAddress(Contact contact) {
        String address = contact.getEmail1();
        if (Strings.isEmpty(address)) {
            address = contact.getEmail2();
        }
        if (Strings.isEmpty(address)) {
            address = contact.getEmail3();
        }

        return address;
    }

}
