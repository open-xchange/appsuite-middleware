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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

import com.openexchange.groupware.AbstractOXException;

/**
 * A single update task.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public interface UpdateTask {
	
	/**
     * Priorities for update tasks.
     * TODO remove the int value. this isn't used anywhere. enum itself have an
     * internal order.
	 */
    public static enum UpdateTaskPriority {
		HIGHEST(0),
		HIGH(1),
		NORMAL(3),
		LOW(4),
		LOWEST(5);
		
		public final int priority;
		
		private UpdateTaskPriority(final int priority) {
			this.priority = priority;
		}
	}

    /**
     * Returns the database schema version with that this update task was
     * introduced. This version is compared with the schema version of the
     * database. This update will only be applied if the database schema version
     * is lower than this version. The most actual version is defined here
     * {@link Version}.
     * @return the schema version with that this update task was introduced.
     */
    int addedWithVersion();
    
    /**
     * @return this update task's priority
     */
    int getPriority();

    /**
     * This method is called to apply the changes to the schema of the database.
     * Write the schema changing code in this method that it doesn't destroy any
     * information in the database if this changings already have been applied.
     * This ensures that update tasks can be executed twice if a task failed.
     * @param schema schema meta data
     * @param contextId this context identifier is used to fetch database
     * connections from the pool.
     * @throws AbstractOXException if applying the changes fails.
     */
    void perform(Schema schema, int contextId) throws AbstractOXException;

}
