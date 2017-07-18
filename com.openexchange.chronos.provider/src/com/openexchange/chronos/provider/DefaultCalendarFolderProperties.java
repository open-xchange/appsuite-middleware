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

/**
 * {@link DefaultCalendarFolderProperties}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.0
 */
public enum DefaultCalendarFolderProperties {

    /** Subscribe flag */
    SUBSCRIBED(DefaultCalendarFolderProperties.SUBSCRIBED_FLAG, Boolean.FALSE),

    /** Sync flag */
    SYNCED(DefaultCalendarFolderProperties.SYNC_FLAG, Boolean.FALSE),

    /** Potential own display name of the folder */
    DISPLAYNAME(DefaultCalendarFolderProperties.OWN_DISPLAYNAME, ""),

    /** Potential own description for the folder */
    DESCRIPTION(DefaultCalendarFolderProperties.OWN_DESCRIPTION, "")

    ;

    public static final String SUBSCRIBED_FLAG = "subscribed";
    public static final String SYNC_FLAG = "sync";
    public static final String OWN_DISPLAYNAME = "ownDisplayname";
    public static final String OWN_DESCRIPTION = "ownDescription";

    private final String pName;
    private final Object defaultValue;

    /**
     * Initializes a new {@link DefaultCalendarFolderProperties}.
     */
    private DefaultCalendarFolderProperties(String pName, Object defaultValue) {
        this.pName = pName;
        this.defaultValue = defaultValue;
    }

    /**
     * Get the property represented by its synonym
     * 
     * @return The property
     */
    public CalendarFolderProperty getProperty() {
        return new CalendarFolderProperty(pName, defaultValue);
    }

    /**
     * The name of the property
     * 
     * @return The name
     */
    public String getPropertyName() {
        return pName;
    }

    /**
     * The default value of the property
     * 
     * @return The default value
     */
    public Object getDefaultValue() {
        return defaultValue;
    }
}
