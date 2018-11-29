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

package com.openexchange.groupware.contact.helpers;

import java.util.Locale;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.contact.ContactProperty;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.i18n.ContactDisplayNameFormat;
import com.openexchange.i18n.I18nService;
import com.openexchange.i18n.I18nServiceRegistry;
import com.openexchange.java.Strings;
import com.openexchange.server.services.ServerServiceRegistry;

/**
 * {@link ContactDisplayNameHelper}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @author <a href="mailto:tobias.friedrich@open-xchange.org">Tobias Friedrich</a>
 *
 * @since v7.10.0
 */
public class ContactDisplayNameHelper {

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(ContactDisplayNameHelper.class);

    /**
     * Formats the display name of the {@link ComplexDisplayItem}
     *
     * @param i18nServices A reference to the i18n service registry, or <code>null</code> if not available
     * @param contact The {@link Contact}
     * @param locale The locale to use when formating the display name
     * @return The display name
     */
    public static String formatDisplayName(I18nServiceRegistry i18nServices, Contact contact, Locale locale) {
        String template = getTemplate(i18nServices, locale, hasDepartment(contact));
        return formatDisplayName(contact, template, locale);
    }

    /**
     * Formats the display name of the {@link ComplexDisplayItem}
     *
     * @param i18nService The i18n service to use for localization
     * @param contact The {@link Contact}
     * @param locale The locale to use when formating the display name
     * @return The display name
     */
    public static String formatDisplayName(I18nService i18nService, Contact contact, Locale locale) {
        String template = getTemplate(i18nService, hasDepartment(contact));
        return formatDisplayName(contact, template, locale);
    }

    /**
     * Formats the display name of the {@link ComplexDisplayItem}
     *
     * @param contact The {@link Contact}
     * @param template The template to use
     * @param locale The locale to use when formating the display name
     * @return The display name
     */
    private static String formatDisplayName(Contact contact, String template, Locale locale) {
        String lastName = contact.getSurName();
        String firstName = contact.getGivenName();
        String department = Strings.isEmpty(contact.getDepartment()) ? "" : contact.getDepartment();
        if (Strings.isEmpty(lastName)) {
            if (Strings.isNotEmpty(firstName)) {
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
        return contact.containsDepartment() && Strings.isNotEmpty(contact.getDepartment()) && contact.getParentFolderID() == FolderObject.SYSTEM_LDAP_FOLDER_ID;
    }

    /**
     * Get the display name template to use
     *
     * @param i18nServices A reference to the i18n service registry
     * @param locale The locale to use for the translation of the template
     * @param hasDepartment Whether the contact has the department field set
     * @return The display name template
     */
    private static String getTemplate(I18nServiceRegistry i18nServices, Locale locale, boolean hasDepartment) {
        I18nService i18nService = null;
        if (null != i18nServices) {
            try {
                i18nService = i18nServices.getI18nService(locale);
            } catch (OXException e) {
                LOGGER.debug("An error occurred while gettting the i18n service for locale '{}': {}", locale, e.getMessage(), e);
            }
        } else {
            LOGGER.debug("No such service: {}. Falling back default template for display name format", I18nServiceRegistry.class);
        }
        return getTemplate(i18nService, hasDepartment);
    }

    /**
     * Get the display name template to use
     *
     * @param i18nService The i18n service to use for localization, or <code>null</code> if not available
     * @param hasDepartment Whether the contact has the department field set
     * @return The display name template
     */
    private static String getTemplate(I18nService i18nService, boolean hasDepartment) {
        String toLocalise = hasDepartment && showDepartments() ? ContactDisplayNameFormat.DISPLAY_NAME_FORMAT_WITH_DEPARTMENT : ContactDisplayNameFormat.DISPLAY_NAME_FORMAT_WITHOUT_DEPARTMENT;
        if (i18nService == null) {
            LOGGER.debug("No i18n service available {}, falling back to default template.");
            return toLocalise;
        }
        return i18nService.getLocalized(toLocalise);
    }

    /**
     * Look-up the property
     *
     * @return <code>true</code> to show departments, <code>false</code> otherwise
     */
    private static boolean showDepartments() {
        LeanConfigurationService configService = ServerServiceRegistry.getServize(LeanConfigurationService.class);
        if (null == configService) {
            boolean defaultValue = false;
            LOGGER.warn("No such service: {}. Assuming default value of '{}' for property '{}'", LeanConfigurationService.class.getName(), defaultValue, ContactProperty.showDepartments);
            return defaultValue;
        }
        return configService.getBooleanProperty(ContactProperty.showDepartments);
    }

}
