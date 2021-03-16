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

package com.openexchange.groupware.update;


/**
 * {@link UpdateProperty} - The properties for update task framework.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v8.0.0
 */
public enum UpdateProperty implements com.openexchange.config.lean.Property {

    /**
     * Specifies a comma-separated list of name-spaces of update tasks that are supposed to be excluded from automatic update procedure.
     */
    EXCLUDED_UPDATE_TASKS("excludedUpdateTasks", ""),
    /**
     * Whether pending update tasks are triggered for context-associated database schema when a context is loaded from database.
     */
    DENY_IMPLICIT_UPDATE_ON_CONTEXT_LOAD("denyImplicitUpdateOnContextLoad", Boolean.FALSE),
    /**
     * Specifies the interval in milliseconds when to refresh/update lock's last-touched time stamp.
     */
    REFRESH_INTERVAL_MILLIS("refreshIntervalMillis", Long.valueOf(20000L)),
    /**
     * Accepts the number of milliseconds specifying the allowed idle time for acquired lock for non-background update tasks.
     */
    BLOCKED_IDLE_MILLIS("locked.idleMillis", Long.valueOf(60000L)),
    /**
     * Accepts the number of milliseconds specifying the allowed idle time for acquired lock for background update tasks.
     */
    BACKGROUND_IDLE_MILLIS("background.idleMillis", Long.valueOf(0L)),

    ;

    private final String fqn;
    private final Object defaultValue;

    private UpdateProperty(String appendix, Object defaultValue) {
        fqn = "com.openexchange.groupware.update." + appendix;
        this.defaultValue = defaultValue;
    }

    @Override
    public String getFQPropertyName() {
        return fqn;
    }

    @Override
    public Object getDefaultValue() {
        return defaultValue;
    }

}
