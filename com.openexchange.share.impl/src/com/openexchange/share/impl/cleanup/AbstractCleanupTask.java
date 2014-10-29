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

package com.openexchange.share.impl.cleanup;

import com.openexchange.server.ServiceLookup;
import com.openexchange.share.impl.ConnectionHelper;
import com.openexchange.threadpool.AbstractTask;

/**
 * {@link AbstractCleanupTask}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.0
 */
public abstract class AbstractCleanupTask<V> extends AbstractTask<V> implements Comparable<AbstractCleanupTask<V>> {

    protected final int priority;
    protected final ServiceLookup services;
    protected final int contextID;
    protected final ConnectionHelper connectionHelper;

    /**
     * Initializes a new {@link AbstractCleanupTask}.
     *
     * @param priority The priority of the task
     * @param services A service lookup reference
     * @param connectionHelper A (started) connection helper, or <code>null</code> if not supplied
     * @param contextID The context ID
     */
    protected AbstractCleanupTask(int priority, ServiceLookup services, ConnectionHelper connectionHelper, int contextID) {
        super();
        this.priority = priority;
        this.services = services;
        this.connectionHelper = connectionHelper;
        this.contextID = contextID;
    }

    @Override
    public int compareTo(AbstractCleanupTask<V> o) {
        if (priority != o.priority) {
            return priority - o.priority;
        }
        if (null == connectionHelper) {
            return null == o.connectionHelper ? 0 : 1;
        }
        return null != o.connectionHelper ? 0 : -1;
    }

}
