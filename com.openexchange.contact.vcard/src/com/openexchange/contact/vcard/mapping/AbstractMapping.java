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

package com.openexchange.contact.vcard.mapping;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import com.openexchange.contact.vcard.internal.VCardExceptionCodes;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import ezvcard.VCard;
import ezvcard.parameter.VCardParameter;
import ezvcard.parameter.VCardParameters;
import ezvcard.property.RawProperty;
import ezvcard.property.VCardProperty;

/**
 * {@link AbstractMapping}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public abstract class AbstractMapping implements VCardMapping {

    protected static boolean addConversionWarning(com.openexchange.contact.vcard.VCardParameters parameters, Throwable cause, String propertyName, String message) {
        return addWarning(parameters, VCardExceptionCodes.CONVERSION_FAILED.create(cause, propertyName, message));
    }

    protected static boolean addConversionWarning(com.openexchange.contact.vcard.VCardParameters parameters, String propertyName, String message) {
        return addWarning(parameters, VCardExceptionCodes.CONVERSION_FAILED.create(propertyName, message));
    }

    protected static boolean addWarning(com.openexchange.contact.vcard.VCardParameters parameters, OXException warning) {
        if (null != parameters && null != parameters.getWarnings()) {
            return parameters.getWarnings().add(warning);
        }
        return false;
    }

    protected static boolean hasAll(Contact contact, int...fields) {
        for (int field : fields) {
            if (false == has(contact, field)) {
                return false;
            }
        }
        return true;
    }

    protected static boolean hasOneOf(Contact contact, int...fields) {
        for (int field : fields) {
            if (has(contact, field)) {
                return true;
            }
        }
        return false;
    }

    protected static boolean has(Contact contact, int field) {
        return null != contact.get(field);
    }

    protected static <T extends VCardProperty> T getFirstProperty(List<T> properties) {
        T matchingProperty = null;
        if (null != properties && 0 < properties.size()) {
            for (T property : properties) {
                VCardParameters parameters = property.getParameters();
                if (null != parameters) {
                    Set<String> types = parameters.getTypes();
                    if (null != types && types.contains("PREF")) {
                        return property;
                    }
                }
                if (null == matchingProperty) {
                    matchingProperty = property;
                }
            }
        }
        return matchingProperty;
    }

    protected static boolean addTypeIfMissing(VCardProperty property, String type) {
        VCardParameters parameters = property.getParameters();
        if (null == parameters) {
            property.addParameter(VCardParameters.TYPE, type);
            return true;
        }
        Set<String> types = parameters.getTypes();
        if (null != types) {
            for (String existingType : types) {
                if (type.equalsIgnoreCase(existingType)) {
                    return false;
                }
            }
        }
        parameters.addType(type);
        return true;
    }

    protected static String[] getTypeValues(VCardParameter...types) {
        String[] values = new String[types.length];
        for (int i = 0; i < types.length; i++) {
            values[i] = types[i].getValue();
        }
        return values;
    }

    protected static Collection<String> getParameterValues(Collection<? extends VCardParameter> parameters) {
        if (null != parameters && 0 < parameters.size()) {
            List<String> values = new ArrayList<String>();
            for (VCardParameter parameter : parameters) {
                values.add(parameter.getValue());
            }
            return values;
        }
        return Collections.emptyList();
    }

    protected static <T extends VCardProperty> T getPropertyWithTypes(List<T> properties, VCardParameter...types) {
        return getPropertyWithTypes(properties, getTypeValues(types));
    }

    protected static <T extends VCardProperty> T getPropertyWithTypes(List<T> properties, String...types) {
        T matchingProperty = null;
        if (null != properties && 0 < properties.size()) {
            for (T property : properties) {
                VCardParameters parameters = property.getParameters();
                if (null != parameters) {
                    if (containsIgnoreCase(parameters.getTypes(), types)) {
                        if (containsIgnoreCase(parameters.getTypes(), "pref")) {
                            /*
                             * prefer the "preferred" property
                             */
                            return property;
                        }
                        if (null == matchingProperty) {
                            /*
                             * remember the first matching property
                             */
                            matchingProperty = property;
                        }
                    }
                }
            }
        }
        return matchingProperty;
    }

    protected static <T extends VCardProperty> List<T> getPropertiesWithTypes(List<T> properties, String...types) {
        /*
         * get matching properties, storing the preferred ones separately
         */
        List<T> matchingProperties = new ArrayList<T>();
        List<T> preferredMatchingProperties = new ArrayList<T>();
        if (null != properties && 0 < properties.size()) {
            for (T property : properties) {
                VCardParameters parameters = property.getParameters();
                if (null != parameters) {
                    if (containsIgnoreCase(parameters.getTypes(), types)) {
                        if (containsIgnoreCase(parameters.getTypes(), "pref")) {
                            preferredMatchingProperties.add(property);
                        } else {
                            matchingProperties.add(property);
                        }
                    }
                }
            }
        }
        /*
         * return all matching properties, but the preferred onex first
         */
        preferredMatchingProperties.addAll(matchingProperties);
        return preferredMatchingProperties;
    }

    protected static <T extends VCardProperty> void sort(List<T> properties) {
        /*
         * sort based on "pref" type parameter
         */
        if (1 < properties.size()) {
            Collections.sort(properties, new Comparator<T>() {

                @Override
                public int compare(T property1, T property2) {
                    VCardParameters parameters1 = property1.getParameters();
                    VCardParameters parameters2 = property2.getParameters();
                    if (null != parameters1 && containsIgnoreCase(parameters1.getTypes(), "pref")) {
                        return null != parameters2 && containsIgnoreCase(parameters2.getTypes(), "pref") ? 0 : -1;
                    }
                    return containsIgnoreCase(parameters2.getTypes(), "pref") ? 1 : 0;
                }
            });
        }
    }

    /**
     * Gets a property whose type parameters do not contain any of the specified type values, ignoring case.
     *
     * @param properties The properties to check
     * @param index The 0-based index in the list of all matching candidates to use
     * @param types The types that should not be present in the property
     * @return The property, or <code>null</code> if not matching property was found
     */
    protected static <T extends VCardProperty> T getPropertyWithoutTypes(List<T> properties, int index, String...types) {
        int matches = 0;
        if (null != properties && 0 < properties.size()) {
            for (T property : properties) {
                VCardParameters parameters = property.getParameters();
                if (null == parameters || false == containsAnyIgnoreCase(parameters.getTypes(), types)) {
                    if (matches++ == index) {
                        return property;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Gets a value indicating whether a collection contains all of the supplied items, ignoring case.
     *
     * @param collection The collection to check
     * @param items The items to lookup
     * @return <code>true</code> if all items are contained in the collection, <code>false</code>, otherwise
     */
    protected static boolean containsIgnoreCase(Collection<String> collection, String...items) {
        for (String item : items) {
            if (false == containsIgnoreCase(collection, item)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Gets a value indicating whether a collection contains at least one of the supplied items, ignoring case.
     *
     * @param collection The collection to check
     * @param items The items to lookup
     * @return <code>true</code> if at least one item is contained in the collection, <code>false</code>, otherwise
     */
    protected static boolean containsAnyIgnoreCase(Collection<String> collection, String...items) {
        for (String item : items) {
            if (containsIgnoreCase(collection, item)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets a value indicating whether a collection contains at a specific item, ignoring case.
     *
     * @param collection The collection to check
     * @param item The item to lookup
     * @return <code>true</code> if the item is contained in the collection, <code>false</code>, otherwise
     */
    protected static boolean containsIgnoreCase(Collection<String> collection, String item) {
        if (null != collection) {
            for (String v : collection) {
                if (item.equalsIgnoreCase(v)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Gets a value indicating whether the vCard represents a legacy OX distribution list.
     *
     * @param vCard The vCard to check
     * @return <code>true</code> if the vCard represents a legacy OX distribution list, <code>false</code>, otherwise
     */
    protected static boolean isLegacyDistributionList(VCard vCard) {
        RawProperty property = vCard.getExtendedProperty("X-OPEN-XCHANGE-CTYPE");
        return null != property && "dlist".equalsIgnoreCase(property.getValue());
    }

}
