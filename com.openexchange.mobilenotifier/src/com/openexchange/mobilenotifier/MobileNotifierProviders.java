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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.mobilenotifier;


/**
 * {@link MobileNotifierProviders} - Enumeration of values for the provider
 *
 * @author <a href="mailto:lars.hoogestraat@open-xchange.com">Lars Hoogestraat</a>
 */
public enum MobileNotifierProviders {

    /**
     * Enum field for mail provider
     */
    MAIL("mail", "io.ox/mail", "Mail", "Mail.tmpl", 1),

    /**
     * Enum field for appointment provider
     */
    APPOINTMENT("appointment", "io.ox/calendar", "Calendar", "Appointment.tmpl", 2),

    /**
     * Enum field for task provider
     */
    TASKS("tasks", "io.ox/tasks", "Tasks", "Task.tmpl", 3),

    /**
     * Enum field for reminder provider
     */
    REMINDER("reminder", "io.ox/reminder", "Reminder", "Reminder.tmpl", 4),

    /**
     * Enum field for drive provider
     */
    DRIVE("drive", "io.ox/drive", "Drive", "Drive.tmpl", 5);

    private final String providerName;

    private final String frontendName;

    private final String title;

    private final String templateFileName;

    private final int index;

    private MobileNotifierProviders(final String providerName, final String frontendName, final String title, final String templateFileName, final int index) {
        this.providerName = providerName;
        this.frontendName = frontendName;
        this.title = title;
        this.templateFileName = templateFileName;
        this.index = index;
    }

    public String getProviderName() {
        return providerName;
    }

    public String getFrontendName() {
        return frontendName;
    }

    public String getTemplateFileName() {
        return templateFileName;
    }

    public String getTitle() {
        return title;
    }

    public int getIndex() {
        return index;
    }

    /**
     * Parses the provider from the parameter name
     *
     * @param providerName
     * @return the provider or <code>null</code> if not found
     */
    public static MobileNotifierProviders parseProviderFromParam(String providerName) {
        for(MobileNotifierProviders p : MobileNotifierProviders.values())
            if(providerName.equals(p.getProviderName())) {
                return p;
            }
        return null;
    }
}
