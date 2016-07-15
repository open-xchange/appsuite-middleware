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

package com.openexchange.contact.vcard.impl.mapping;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import com.openexchange.contact.vcard.impl.internal.VCardExceptionCodes;
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

    /** The extended <code>x-other</code> type parameter*/
    static final String TYPE_OTHER = "x-other";

    /** The value used in the custom <code>X-ABLabel</code> property to indicate the "Other" label in Apple clients */
    static final String ABLABEL_OTHER = "_$!<Other>!$_";

    /**
     * Initializes and adds a new conversion warning to the warnings collection in the supplied vCard parameters reference.
     *
     * @param warnings A reference to the warnings collection, or <code>null</code> if not used
     * @param cause The underlying exception
     * @param propertyName The vCard property name where the warning occurred
     * @param message The warning message
     * @return <code>true</code> if the warning was added, <code>false</code>, otherwise
     */
    protected static boolean addConversionWarning(List<OXException> warnings, Throwable cause, String propertyName, String message) {
        return addWarning(warnings, VCardExceptionCodes.CONVERSION_FAILED.create(cause, propertyName, message));
    }

    /**
     * Initializes and adds a new conversion warning to the warnings collection in the supplied vCard parameters reference.
     *
     * @param warnings A reference to the warnings collection, or <code>null</code> if not used
     * @param propertyName The vCard property name where the warning occurred
     * @param message The warning message
     * @return <code>true</code> if the warning was added, <code>false</code>, otherwise
     */
    protected static boolean addConversionWarning(List<OXException> warnings, String propertyName, String message) {
        return addWarning(warnings, VCardExceptionCodes.CONVERSION_FAILED.create(propertyName, message));
    }

    /**
     * Adds a conversion warning to the supplied warnings collection.
     *
     * @param warnings A reference to the warnings collection, or <code>null</code> if not used
     * @param warning The warning to add
     * @return <code>true</code> if the warning was added, <code>false</code>, otherwise
     */
    protected static boolean addWarning(List<OXException> warnings, OXException warning) {
        if (null != warnings) {
            return warnings.add(warning);
        }
        return false;
    }

    /**
     * Gets a value indicating whether a contact has defined values for all of the supplied contact fields.
     *
     * @param contact The contact to check
     * @param fields The contact fields / column identifiers to check
     * @return <code>true</code> if all fields are set in the supplied contact, <code>false</code>, otherwise
     */
    protected static boolean hasAll(Contact contact, int...fields) {
        for (int field : fields) {
            if (false == has(contact, field)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Gets a value indicating whether a contact has at least one defined value for any the supplied contact fields.
     *
     * @param contact The contact to check
     * @param fields The contact fields / column identifiers to check
     * @return <code>true</code> if at least one field is set in the supplied contact, <code>false</code>, otherwise
     */
    protected static boolean hasOneOf(Contact contact, int...fields) {
        for (int field : fields) {
            if (has(contact, field)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets a value indicating whether a contact has a defined value for the supplied contact field, i.e. it has a value other than
     * <code>null</code>.
     *
     * @param contact The contact to check
     * @param fields The contact field / column identifier to check
     * @return <code>true</code> if the field is set in the supplied contact, <code>false</code>, otherwise
     */
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

    protected static boolean addTypesIfMissing(VCardProperty property, String...types) {
        boolean added = false;
        for (String type : types) {
            added |= addTypeIfMissing(property, type);
        }
        return added;
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
     * @return The property, or <code>null</code> if no matching property was found
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
     * Gets those properties whose type parameters do not contain any of the specified type values, ignoring case.
     *
     * @param properties The properties to check
     * @param types The types that should not be present in the property
     * @return The properties, or an empty list if no matching property was found
     */
    protected static <T extends VCardProperty> List<T> getPropertiesWithoutTypes(List<T> properties, String...types) {
        List<T> matchingProperties = new ArrayList<T>();
        if (null != properties && 0 < properties.size()) {
            for (T property : properties) {
                VCardParameters parameters = property.getParameters();
                if (null == parameters || false == containsAnyIgnoreCase(parameters.getTypes(), types)) {
                    matchingProperties.add(property);
                }
            }
        }
        return matchingProperties;
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



    /**
     * If found, gets the value of an <code>X-ABLabel</code> grouped with the supplied property which is set by Apple clients to indicate
     * the kind of a property, e.g. <code>_$!<Other>!$_</code>.
     *
     * @param vCard The vCard
     * @param property The property to get the AB label for
     * @return The value of the associated <code>X-ABLabel</code> property, or <code>null</code> if not defined
     */
    protected static String getABLabel(VCard vCard, VCardProperty property) {
        if (null != property && null != property.getGroup()) {
            for (RawProperty labelProperty : vCard.getExtendedProperties("X-ABLabel")) {
                if (property.getGroup().equals(labelProperty.getGroup())) {
                    return labelProperty.getValue();
                }
            }
        }
        return null;
    }

    /**
     * Gets a property that is associated with a matching <code>X-ABLabel</code> property.
     *
     * @param vCard The vCard
     * @param properties The properties to check
     * @param abLabel The <code>X-ABLabel</code> value to match
     * @return The property, or <code>null</code> if not found
     */
    protected static <T extends VCardProperty> T getPropertyWithABLabel(VCard vCard, List<T> properties, String abLabel) {
        if (null != properties && 0 < properties.size()) {
            for (T property : properties) {
                if (abLabel.equals(getABLabel(vCard, property))) {
                    return property;
                }
            }
        }
        return null;
    }

    private final String[] propertyNames;

    /**
     * Initializes a new {@link AbstractMapping}.
     *
     * @param propertyNames The affected vCard property names
     */
    protected AbstractMapping(String...propertyNames) {
        super();
        this.propertyNames = propertyNames;
    }

    @Override
    public String[] getPropertyNames() {
        return propertyNames;
    }

}
