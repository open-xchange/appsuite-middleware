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

package com.openexchange.oauth.provider.rmi.client;

import java.io.Serializable;
import java.util.List;

/**
 * DTO class for client data objects.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class ClientDataDto implements Serializable {

    private static final long serialVersionUID = 8228091377828967016L;

    private String name;
    private boolean bName;

    private String description;
    private boolean bDescription;

    private String website;
    private boolean bWebsite;

    private String contactAddress;
    private boolean bContactAddress;

    private IconDto icon;
    private boolean bIcon;

    private String defaultScope;
    private boolean bDefaultScope;

    private List<String> redirectURIs;
    private boolean bRedirectURIs;

    /**
     * Initializes a new {@link ClientDataDto}.
     */
    public ClientDataDto() {
        super();
    }

    /**
     * Gets whether a name was set.
     *
     * @return <code>true</code> if so, otherwise <code>false</code>
     */
    public boolean containsName() {
        return bName;
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
        bName = true;
    }

    /**
     * Gets whether a description was set.
     *
     * @return <code>true</code> if so, otherwise <code>false</code>
     */
    public boolean containsDescription() {
        return bDescription;
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
        bDescription = true;
    }

    /**
     * Gets whether a website was set.
     *
     * @return <code>true</code> if so, otherwise <code>false</code>
     */
    public boolean containsWebsite() {
        return bWebsite;
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
        bWebsite = true;
    }

    /**
     * Gets whether a contact address was set.
     *
     * @return <code>true</code> if so, otherwise <code>false</code>
     */
    public boolean containsContactAddress() {
        return bContactAddress;
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
        bContactAddress = true;
    }

    /**
     * Gets whether an icon was set.
     *
     * @return <code>true</code> if so, otherwise <code>false</code>
     */
    public boolean containsIcon() {
        return bIcon;
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
        bIcon = true;
    }

    /**
     * Gets whether a default scope was set.
     *
     * @return <code>true</code> if so, otherwise <code>false</code>
     */
    public boolean containsDefaultScope() {
        return bDefaultScope;
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
        bDefaultScope = true;
    }

    /**
     * Gets whether the redirect URIs were set.
     *
     * @return <code>true</code> if so, otherwise <code>false</code>
     */
    public boolean containsRedirectURIs() {
        return bRedirectURIs;
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
        bRedirectURIs = true;
    }

}
