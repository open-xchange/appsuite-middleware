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
