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

package com.openexchange.oauth.provider.rmi.client;

import java.io.Serializable;
import java.util.List;

/**
 * DTO class for client objects.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class ClientDto implements Serializable {

    private static final long serialVersionUID = -8656347951737285488L;

    private String id;

    private String secret;

    private String name;

    private String description;

    private IconDto icon;

    private String website;

    private String contactAddress;

    private String defaultScope;

    private long registrationDate;

    private boolean enabled;

    private List<String> redirectURIs;

    /**
     * Initializes a new {@link ClientDto}.
     */
    public ClientDto() {
        super();
    }

    /**
     * Gets the clients public identifier.
     *
     * @return The identifier
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the clients public identifier.
     *
     * @param id The identifier
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Gets the clients secret.
     *
     * @return The secret
     */
    public String getSecret() {
        return secret;
    }

    /**
     * Sets the clients secret.
     *
     * @param secret The secret
     */
    public void setSecret(String secret) {
        this.secret = secret;
    }

    /**
     * Gets the clients name.
     *
     * @return The name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the clients name.
     *
     * @param name The name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the clients description.
     *
     * @return The description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the clients description.
     *
     * @param description The description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Gets the URL of the clients website.
     *
     * @return The website
     */
    public String getWebsite() {
        return website;
    }

    /**
     * Sets the URL of the clients website.
     *
     * @param website The website
     */
    public void setWebsite(String website) {
        this.website = website;
    }

    /**
     * Gets the email address to contact the client vendor.
     *
     * @return The contact address
     */
    public String getContactAddress() {
        return contactAddress;
    }

    /**
     * Sets the email address to contact the client vendor.
     *
     * @param contactAddress The contact address
     */
    public void setContactAddress(String contactAddress) {
        this.contactAddress = contactAddress;
    }

    /**
     * Gets the clients icon.
     *
     * @return The icon
     */
    public IconDto getIcon() {
        return icon;
    }

    /**
     * Sets the clients icon.
     *
     * @param icon The icon
     */
    public void setIcon(IconDto icon) {
        this.icon = icon;
    }

    /**
     * Gets the default scope that should be applied when an authorization request does not
     * specify a certain scope.
     *
     * @return The default scope
     */
    public String getDefaultScope() {
        return defaultScope;
    }

    /**
     * Sets the default scope that should be applied when an authorization request does not
     * specify a certain scope.
     *
     * @param defaultScope The default scope
     */
    public void setDefaultScope(String defaultScope) {
        this.defaultScope = defaultScope;
    }

    /**
     * Gets the registration date of the client in milliseconds since January 1, 1970, 00:00:00 GMT.
     *
     * @return The registration date
     */
    public long getRegistrationDate() {
        return registrationDate;
    }

    /**
     * Sets the registration date of the client in milliseconds since January 1, 1970, 00:00:00 GMT.
     *
     * @param registrationDate The registration date
     */
    public void setRegistrationDate(long registrationDate) {
        this.registrationDate = registrationDate;
    }

    /**
     * Gets whether the client is enabled or not.
     *
     * @return <code>true</code> if the client is enabled, otherwise <code>false</code>
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Sets whether the client is enabled or not.
     *
     * @param enabled <code>true</code> if the client is enabled, otherwise <code>false</code>
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Gets the list of redirect URIs that are used to callback the client when a user grants access.
     *
     * @return The URIs
     */
    public List<String> getRedirectURIs() {
        return redirectURIs;
    }

    /**
     * Sets the list of redirect URIs that are used to callback the client when a user grants access.
     *
     * @param redirectURIs The URIs
     */
    public void setRedirectURIs(List<String> redirectURIs) {
        this.redirectURIs = redirectURIs;
    }

}
