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

package com.openexchange.chronos.provider;

import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import com.openexchange.chronos.ExtendedProperties;
import com.openexchange.exception.OXException;

/**
 * {@link DefaultCalendarFolder}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class DefaultCalendarFolder implements CalendarFolder {

    private String id;
    private String name;
    private boolean subscribed;
    private Date lastModified;
    private List<CalendarPermission> permissions;
    private ExtendedProperties extendedProperties;
    private EnumSet<CalendarCapability> supportedCapabilites;
    private OXException accountError;

    /**
     * Initializes a new {@link DefaultCalendarFolder}.
     */
    public DefaultCalendarFolder() {
        super();
        subscribed = true;
    }

    /**
     * Initializes a new {@link DefaultCalendarFolder}, taking over the properties from another folder.
     *
     * @param folder The folder to copy the properties from
     */
    public DefaultCalendarFolder(CalendarFolder folder) {
        this();
        id = folder.getId();
        name = folder.getName();
        lastModified = folder.getLastModified();
        permissions = folder.getPermissions();
        extendedProperties = folder.getExtendedProperties();
        supportedCapabilites = folder.getSupportedCapabilites();
        subscribed = folder.isSubscribed();
    }

    /**
     * Initializes a new {@link DefaultCalendarFolder}.
     *
     * @param id The folder identifier
     * @param name The folder name
     */
    public DefaultCalendarFolder(String id, String name) {
        this();
        this.id = id;
        this.name = name;
    }

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean isSubscribed() {
        return subscribed;
    }

    public void setSubscribed(boolean subscribed) {
        this.subscribed = subscribed;
    }

    @Override
    public List<CalendarPermission> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<CalendarPermission> permissions) {
        this.permissions = permissions;
    }

    @Override
    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    @Override
    public ExtendedProperties getExtendedProperties() {
        return extendedProperties;
    }

    public void setExtendedProperties(ExtendedProperties extendedProperties) {
        this.extendedProperties = extendedProperties;
    }

    @Override
    public EnumSet<CalendarCapability> getSupportedCapabilites() {
        return supportedCapabilites;
    }

    public void setSupportedCapabilites(EnumSet<CalendarCapability> supportedCapabilites) {
        this.supportedCapabilites = supportedCapabilites;
    }

    @Override
    public OXException getAccountError() {
        return accountError;
    }

    public void setAccountError(OXException accountError) {
        this.accountError = accountError;
    }

    @Override
    public String toString() {
        return "DefaultCalendarFolder [id=" + id + ", name=" + name + "]";
    }

}
