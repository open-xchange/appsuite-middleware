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
 *     Copyright (C) 2004-2015 Open-Xchange, Inc.
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

package com.openexchange.oauth.provider;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * {@link ClientData}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class ClientData {

    private String owner;
    private boolean bOwner;

    private String name;
    private boolean bName;

    private String description;
    private boolean bDescription;

    private String contactAddress;
    private boolean bContactAddress;

    private final Set<String> redirectURIs;
    private boolean bRedirectURIs;

    /**
     * Initializes a new {@link ClientData}.
     */
    public ClientData() {
        super();
        redirectURIs = new LinkedHashSet<String>();
    }

    /**
     * Gets the owner
     *
     * @return The owner
     */
    public String getOwner() {
        return owner;
    }

    /**
     * Sets the owner
     *
     * @param owner The owner
     */
    public void setOwner(String owner) {
        this.owner = owner;
        bOwner = true;
    }

    /**
     * Gets the name
     *
     * @return The name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name
     *
     * @param name The name
     */
    public void setName(String name) {
        this.name = name;
        bName = true;
    }

    /**
     * Gets the description
     *
     * @return The description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description
     *
     * @param The description
     */
    public void setDescription(String description) {
        this.description = description;
        bDescription = true;
    }

    /**
     * Gets the contact address
     *
     * @return The contact address
     */
    public String getContactAddress() {
        return contactAddress;
    }

    /**
     * Sets the contact address
     *
     * @param The contact address
     */
    public void setContactAddress(String contactAddress) {
        this.contactAddress = contactAddress;
        bContactAddress = true;
    }

    /**
     * Gets the redirectUris
     *
     * @return The redirectUris
     */
    public List<String> getRedirectURIs() {
        return new ArrayList<String>(redirectURIs);
    }

    /**
     * Adds given redirect URI
     *
     * @param uri The URI to add
     */
    public void addRedirectURI(String uri) {
        redirectURIs.add(uri);
        bRedirectURIs = true;
    }

    /**
     * Checks if this client data contains owner information
     *
     * @return <code>true</code> if this client data contains owner information; otherwise <code>false</code>
     */
    public boolean containsOwner() {
        return bOwner;
    }

    /**
     * Checks if this client data contains the name
     *
     * @return <code>true</code> if this client data contains the name; otherwise <code>false</code>
     */
    public boolean containsName() {
        return bName;
    }

    /**
     * Checks if this client data contains a description
     *
     * @return <code>true</code> if this client data contains a description; otherwise <code>false</code>
     */
    public boolean containsDescription() {
        return bDescription;
    }

    /**
     * Checks if this client data contains a contact address
     *
     * @return <code>true</code> if this client data contains a contact address; otherwise <code>false</code>
     */
    public boolean containsContactAddress() {
        return bContactAddress;
    }

    /**
     * Checks if this client data contains redirect URIs
     *
     * @return <code>true</code> if this client data contains redirect URIs; otherwise <code>false</code>
     */
    public boolean containsRedirectURIs() {
        return bRedirectURIs;
    }

}
