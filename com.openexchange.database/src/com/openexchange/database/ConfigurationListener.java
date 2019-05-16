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

package com.openexchange.database;

import com.openexchange.database.internal.Configuration;

/**
 * {@link ConfigurationListener}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.1
 */
public interface ConfigurationListener extends Comparable<ConfigurationListener> {

    /**
     * Notifies this listener about an updated configuration for JDBC
     *
     * @param configuration The new {@link Configuration}
     */
    void notify(Configuration configuration);

    /**
     * Gets the identifier of the pool to notify
     *
     * @return The pool ID
     */
    int getPoolId();

    /**
     * Gets the priority.
     * <ul>
     * <li><code>1<code></code> means highest, <b>reserved for configDB, do not use!</b></li>
     * <li><code>100</code> means lowest and is the default value</li>
     * </ul>
     *
     * @return The priority
     */
    default int getPriority() {
        return 100;
    }

    @Override
    default int compareTo(ConfigurationListener o) {
        int otherPriority = o.getPriority();
        int thisPriority = getPriority();
        return (thisPriority < otherPriority) ? -1 : ((otherPriority == thisPriority) ? 0 : 1);
    }

    /**
     * {@link ConfigDBListener} - Marker interface to avoid unnecessary reloading of user DBs
     *
     * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
     * @since v7.10.1
     */
    interface ConfigDBListener extends ConfigurationListener {
        // Marker interface to avoid unnecessary reloading of user DBs
    }
}
