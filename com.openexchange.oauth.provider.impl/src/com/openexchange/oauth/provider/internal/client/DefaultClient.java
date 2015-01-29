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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.oauth.provider.internal.client;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import com.openexchange.oauth.provider.Client;
import com.openexchange.oauth.provider.Icon;
import com.openexchange.oauth.provider.internal.URIValidator;

/**
 * {@link DefaultClient} - The default {@link Client} implementation.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
public class DefaultClient implements Client {

    private static final long serialVersionUID = 8478439580309382857L;

    private String id;
    private String description;
    private String name;
    private String secret;
    private final List<String> redirectURIs;
    private String owner;
    private String contactAddress;
    private Icon icon;
    private Date registrationDate;

    /**
     * Initializes a new {@link DefaultClient}.
     */
    public DefaultClient() {
        super();
        redirectURIs = new LinkedList<>();
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getSecret() {
        return secret;
    }

    @Override
    public boolean hasRedirectURI(String uri) {
        if (!URIValidator.isValidRedirectURI(uri)) {
            return false;
        }

        for (String storedURI : redirectURIs) {
            if (URIValidator.urisEqual(storedURI, uri)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Sets the identifier
     *
     * @param id The identifier to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Sets the description
     *
     * @param description The description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Sets the name
     *
     * @param name The name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Sets the secret
     *
     * @param secret The secret to set
     */
    public void setSecret(String secret) {
        this.secret = secret;
    }

    public void addRedirectURI(String uri) {
        redirectURIs.add(uri);
    }

    @Override
    public String getOwner() {
        return owner;
    }

    /**
     * Sets the owner
     *
     * @param owner The owner to set
     */
    public void setOwner(String owner) {
        this.owner = owner;
    }

    @Override
    public String getContactAddress() {
        return contactAddress;
    }

    /**
     * Sets the contact address
     *
     * @param contactAddress The contact address to set
     */
    public void setContactAddress(String contactAddress) {
        this.contactAddress = contactAddress;
    }

    @Override
    public List<String> getRedirectURIs() {
        return redirectURIs;
    }

    @Override
    public Icon getIcon() {
        return icon;
    }


    /**
     * Sets the icon
     *
     * @param icon The icon to set
     */
    public void setIcon(Icon icon) {
        this.icon = icon;
    }

    @Override
    public Date getRegistrationDate() {
        return registrationDate;
    }


    /**
     * Sets the registrationDate
     *
     * @param registrationDate The registrationDate to set
     */
    public void setRegistrationDate(Date registrationDate) {
        this.registrationDate = registrationDate;
    }

}
