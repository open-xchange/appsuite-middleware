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

package com.openexchange.chronos;

import java.util.EnumSet;
import java.util.List;

/**
 * {@link Conference}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.4
 * @see <a href="https://tools.ietf.org/html/rfc7986#section-5.11">RFC 7986, section 5.11</a>
 */
public class Conference {

    private int id;
    private String uri;
    private String label;
    private List<String> features;
    private List<ExtendedPropertyParameter> extendedParameters;

    private final EnumSet<ConferenceField> setFields;

    /**
     * Initializes a new {@link Conference}.
     */
    public Conference() {
        super();
        this.setFields = EnumSet.noneOf(ConferenceField.class);
    }

    /**
     * Gets a value indicating whether a specific property is set in the conference or not.
     *
     * @param field The field to check
     * @return <code>true</code> if the field is set, <code>false</code>, otherwise
     */
    public boolean isSet(ConferenceField field) {
        return setFields.contains(field);
    }

    /**
     * Gets the internal identifier of the conference.
     *
     * @return The internal identifier
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the internal identifier of the conference.
     *
     * @param value The internal identifier to set
     */
    public void setId(int value) {
        id = value;
        setFields.add(ConferenceField.ID);
    }

    /**
     * Removes the internal identifier of the conference.
     */
    public void removeId() {
        id = 0;
        setFields.remove(ConferenceField.ID);
    }

    /**
     * Gets a value indicating whether the internal identifier of the conference has been set or not.
     *
     * @return <code>true</code> if the internal identifier is set, <code>false</code>, otherwise
     */
    public boolean containsId() {
        return isSet(ConferenceField.ID);
    }

    /**
     * Gets the URI to access the conference.
     *
     * @return The URI
     */
    public String getUri() {
        return uri;
    }

    /**
     * Sets the URI to access the conference.
     *
     * @param value The URI to set
     */
    public void setUri(String value) {
        uri = value;
        setFields.add(ConferenceField.URI);
    }

    /**
     * Removes the URI to access the conference.
     */
    public void removeUri() {
        uri = null;
        setFields.remove(ConferenceField.URI);
    }

    /**
     * Gets a value indicating whether the URI to access the conference has been set or not.
     *
     * @return <code>true</code> if the URI is set, <code>false</code>, otherwise
     */
    public boolean containsUri() {
        return isSet(ConferenceField.URI);
    }

    /**
     * Gets label used to convey additional details on the use of the URI.
     *
     * @return The label
     */
    public String getLabel() {
        return label;
    }

    /**
     * Sets the label used to convey additional details on the use of the URI.
     *
     * @param value The label to set
     */
    public void setLabel(String value) {
        label = value;
        setFields.add(ConferenceField.LABEL);
    }

    /**
     * Removes the label used to convey additional details on the use of the URI.
     */
    public void removeLabel() {
        label = null;
        setFields.remove(ConferenceField.LABEL);
    }

    /**
     * Gets a value indicating whether the label used to convey additional details on the use of the URI has been set or not.
     *
     * @return <code>true</code> if the label is set, <code>false</code>, otherwise
     */
    public boolean containsLabel() {
        return isSet(ConferenceField.LABEL);
    }

    /**
     * Gets the features describing the key capabilities of the conference system.
     *
     * @return The features
     */
    public List<String> getFeatures() {
        return features;
    }

    /**
     * Sets the features describing the key capabilities of the conference system.
     *
     * @param value The features to set
     */
    public void setFeatures(List<String> value) {
        features = value;
        setFields.add(ConferenceField.FEATURES);
    }

    /**
     * Removes features describing the key capabilities of the conference system.
     */
    public void removeFeatures() {
        features = null;
        setFields.remove(ConferenceField.FEATURES);
    }

    /**
     * Gets a value indicating whether the features describing the key capabilities of the conference system have been set or not.
     *
     * @return <code>true</code> if the features are set, <code>false</code>, otherwise
     */
    public boolean containsFeatures() {
        return setFields.contains(ConferenceField.FEATURES);
    }

    /**
     * Gets the extended parameters of the conference.
     *
     * @return The extended parameters
     */
    public List<ExtendedPropertyParameter> getExtendedParameters() {
        return extendedParameters;
    }

    /**
     * Sets the extended parameters of the conference.
     *
     * @param value The extended parameters to set
     */
    public void setExtendedParameters(List<ExtendedPropertyParameter> value) {
        extendedParameters = value;
        setFields.add(ConferenceField.EXTENDED_PARAMETERS);
    }

    /**
     * Removes the extended parameters of the conference.
     */
    public void removeExtendedParameters() {
        extendedParameters = null;
        setFields.remove(ConferenceField.EXTENDED_PARAMETERS);
    }

    /**
     * Gets a value indicating whether extended parameters of the conference have been set or not.
     *
     * @return <code>true</code> if extended parameters are set, <code>false</code>, otherwise
     */
    public boolean containsExtendedParameters() {
        return setFields.contains(ConferenceField.EXTENDED_PARAMETERS);
    }

    @Override
    public String toString() {
        return "Conference [id=" + id + ", uri=" + uri + ", label=" + label + "]";
    }

}
