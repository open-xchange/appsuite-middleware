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

package com.openexchange.groupware.contact.helpers;

import java.util.Locale;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.contact.ContactProperty;
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
        String displayName = Strings.isEmpty(contact.getDisplayName()) ? "-" : contact.getDisplayName();
        return String.format(locale, template, displayName, "", department);
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
            i18nService = i18nServices.getI18nService(locale);
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
            Boolean defaultValue = (Boolean) ContactProperty.showDepartments.getDefaultValue();
            LOGGER.warn("No such service: {}. Assuming default value of '{}' for property '{}'", LeanConfigurationService.class.getName(), defaultValue, ContactProperty.showDepartments);
            return defaultValue.booleanValue();
        }
        return configService.getBooleanProperty(ContactProperty.showDepartments);
    }

}
