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

package com.openexchange.drive.restricted.loader;

/**
 * {@link StringsProvider}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 * @since v7.10.2
 */
public interface StringsProvider {

    /**
     * Gets the name of the scope within the config-cascade, in which secrets and certificates should be defined.
     * <p>
     * This is useful for defining brand-specific data only for this brand.
     *
     * @return The scope name
     */
    default String getConfigCascadeScope() {
        return "server";
    }

    /**
     * Gets the name of the <code>.properties</code> file containing the properties:
     * <ul>
     * <li>{@link #getGCMSecretPropertyName()}</li>
     * <li>{@link #getAPNiOSKeystorePropertyName()}</li>
     * </ul>
     * <p>
     * Take care, that no other fragment uses the same filename as the fragment, that you're implementing. The class loader is not
     * able to distinguish files with the same name in different fragments.
     *
     * @return The name of the <code>.properties</code> file
     */
    String getPropertiesFilename();

    /**
     * Gets the name of the property containing the GCM secret key loaded from the file named {@link #getGCMPropertiesFilename()}. Returns
     * <code>null</code> if not GCM secret key should be loaded.
     *
     * @return The property name or <code>null</code>
     */
    default String getGCMSecretPropertyName() {
        return "gcmKey";
    }

    /**
     * Gets the name of the property specifying whether GCM is enabled or not.
     *
     * @return The property name
     */
    default String getGCMSecretEnabledPropertyName() {
        return "gcmEnabled";
    }

    /**
     * Gets the name of the property specifying whether iOS push via APN is enabled or not.
     *
     * @return The property name
     */
    default String getAPNiOSEnabledPropertyName() {
        return "iOSEnabled";
    }

    /**
     * Gets the name of the property for the keystore of iOS push.
     *
     * @return The property name
     */
    default String getAPNiOSKeystorePropertyName() {
        return "iOSKeystore";
    }

    /**
     * Gets the name of the property for the password of iOS push.
     *
     * @return The property name
     */
    default String getAPNiOSPasswordPropertyName() {
        return "iOSPassword";
    }

    /**
     * Gets the name of the property for the production flag of iOS push.
     *
     * @return The property name
     */
    default String getAPNiOSProductionPropertyName() {
        return "iOSProduction";
    }

    /**
     * Gets the name of the property specifying whether Mac OS push is enabled or not.
     *
     * @return The property name
     */
    default String getAPNmacOSEnabledPropertyName() {
        return "macOSEnabled";
    }

    /**
     * Gets the name of the property for the keystore of Mac OS push.
     *
     * @return The property name
     */
    default String getAPNmacOSKeystorePropertyName() {
        return "macOSKeystore";
    }

    /**
     * Gets the name of the property for the password of Mac OS push.
     *
     * @return The property name
     */
    default String getAPNmacOSPasswordPropertyName() {
        return "macOSPassword";
    }

    /**
     * Gets the name of the property for the production flag of Mac OS push.
     *
     * @return The property name
     */
    default String getAPNmacOSProductionPropertyName() {
        return "macOSProduction";
    }

}
