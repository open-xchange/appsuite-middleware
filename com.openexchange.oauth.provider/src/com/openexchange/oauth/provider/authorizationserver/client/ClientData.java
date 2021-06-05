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

package com.openexchange.oauth.provider.authorizationserver.client;

import java.io.Serializable;
import java.util.Set;

/**
 * DTO class for the client management API.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class ClientData implements Serializable {

    private static final long serialVersionUID = 8228091377828967016L;

    private Icon icon;
    private boolean bIcon;

    private String name;
    private boolean bName;

    private String description;
    private boolean bDescription;

    private String defaultScope;
    private boolean bDefaultScope;

    private Set<String> redirectURIs;
    private boolean bRedirectURIs;

    private String contactAddress;
    private boolean bContactAddress;

    private String website;
    private boolean bWebsite;


    public ClientData() {
        super();
    }

    public Icon getIcon() {
        return icon;
    }

    public void setIcon(Icon icon) {
        this.icon = icon;
        bIcon = true;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        bName = true;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
        bDescription = true;
    }

    public String getDefaultScope() {
        return defaultScope;
    }

    public void setDefaultScope(String defaultScope) {
        this.defaultScope = defaultScope;
        bDefaultScope = true;
    }

    public Set<String> getRedirectURIs() {
        return redirectURIs;
    }

    public void setRedirectURIs(Set<String> redirectURIs) {
        this.redirectURIs = redirectURIs;
        bRedirectURIs = true;
    }

    public String getContactAddress() {
        return contactAddress;
    }

    public void setContactAddress(String contactAddress) {
        this.contactAddress = contactAddress;
        bContactAddress = true;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
        bWebsite = true;
    }

    public boolean containsIcon() {
        return bIcon;
    }

    public boolean containsName() {
        return bName;
    }

    public boolean containsDescription() {
        return bDescription;
    }

    public boolean containsDefaultScope() {
        return bDefaultScope;
    }

    public boolean containsRedirectURIs() {
        return bRedirectURIs;
    }

    public boolean containsContactAddress() {
        return bContactAddress;
    }

    public boolean containsWebsite() {
        return bWebsite;
    }
}
