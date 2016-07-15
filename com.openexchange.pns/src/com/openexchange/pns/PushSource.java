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

package com.openexchange.pns;

/**
 * {@link PushSource} - Enumeration of possible push sources.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public enum PushSource {

    /**
     * Enum field for mail-related push notifications
     */
    MAIL("mail", "io.ox/mail", "Mail"),
    /**
     * Enum field for appointment-related push notifications
     */
    APPOINTMENT("appointment", "io.ox/calendar", "Calendar"),
    /**
     * Enum field for task-related push notifications
     */
    TASKS("tasks", "io.ox/tasks", "Tasks"),
    /**
     * Enum field for reminder-related push notifications
     */
    REMINDER("reminder", "io.ox/reminder", "Reminder"),
    /**
     * Enum field for drive-related push notifications
     */
    DRIVE("drive", "io.ox/drive", "Drive"),

    ;

    private final String sourceName;
    private final String frontendName;
    private final String title;

    private PushSource(String providerName, String frontendName, String title) {
        this.sourceName = providerName;
        this.frontendName = frontendName;
        this.title = title;
    }

    /**
     * Gets the source name (acting as identifier)
     *
     * @return The source name
     */
    public String getSourceName() {
        return sourceName;
    }

    /**
     * Gets the front-end name
     *
     * @return The front-end name
     */
    public String getFrontendName() {
        return frontendName;
    }

    /**
     * Gets the title
     *
     * @return The title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Gets the index position
     *
     * @return The index position
     */
    public int getIndex() {
        return ordinal() + 1;
    }

    /**
     * Gets the source for specified name.
     *
     * @param sourceName The source name
     * @return The source or <code>null</code> if not found
     */
    public static PushSource sourceFor(String sourceName) {
        if (null != sourceName) {
            for (PushSource p : PushSource.values()) {
                if (sourceName.equals(p.getSourceName())) {
                    return p;
                }
            }
        }
        return null;
    }
}
