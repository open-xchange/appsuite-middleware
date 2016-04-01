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

package com.openexchange.oauth.provider.authorizationserver.client;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import com.openexchange.oauth.provider.resourceserver.scope.Scope;
import com.openexchange.oauth.provider.tools.URIValidator;

/**
 * The default {@link Client} implementation.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
public class DefaultClient implements Client {

    private static final long serialVersionUID = 8478439580309382857L;

    private String id;
    private String secret;
    private String name;
    private String description;
    private Icon icon;
    private String website;
    private String contactAddress;
    private final List<String> redirectURIs;
    private Scope defaultScope;
    private Date registrationDate;
    private boolean enabled;

    /**
     * Initializes a new {@link DefaultClient}.
     */
    public DefaultClient() {
        super();
        redirectURIs = new LinkedList<>();
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
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public Icon getIcon() {
        return icon;
    }

    @Override
    public String getWebsite() {
        return website;
    }

    @Override
    public String getContactAddress() {
        return contactAddress;
    }

    @Override
    public List<String> getRedirectURIs() {
        return redirectURIs;
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

    @Override
    public Scope getDefaultScope() {
        return defaultScope;
    }

    @Override
    public Date getRegistrationDate() {
        return registrationDate;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setIcon(Icon icon) {
        this.icon = icon;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public void setContactAddress(String contactAddress) {
        this.contactAddress = contactAddress;
    }

    public void addRedirectURI(String uri) {
        redirectURIs.add(uri);
    }

    public void setDefaultScope(Scope defaultScope) {
        this.defaultScope = defaultScope;
    }

    public void setRegistrationDate(Date registrationDate) {
        this.registrationDate = registrationDate;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("DefaultClient [");
        if (getId() != null) {
            builder.append("id=").append(getId()).append(", ");
        }
        if (getSecret() != null) {
            builder.append("secret=").append(getSecret()).append(", ");
        }
        if (getName() != null) {
            builder.append("name=").append(getName()).append(", ");
        }
        if (getDescription() != null) {
            builder.append("description=").append(getDescription()).append(", ");
        }
        if (getIcon() != null) {
            builder.append("icon=").append(getIcon()).append(", ");
        }
        if (getWebsite() != null) {
            builder.append("website=").append(getWebsite()).append(", ");
        }
        if (getContactAddress() != null) {
            builder.append("contact-address=").append(getContactAddress()).append(", ");
        }
        if (getRedirectURIs() != null) {
            builder.append("redirect-URIs=").append(getRedirectURIs()).append(", ");
        }
        if (getDefaultScope() != null) {
            builder.append("default-scope=").append(getDefaultScope()).append(", ");
        }
        if (getRegistrationDate() != null) {
            builder.append("registration-date=").append(getRegistrationDate()).append(", ");
        }
        builder.append("enabled=").append(isEnabled()).append("]");
        return builder.toString();
    }

}
