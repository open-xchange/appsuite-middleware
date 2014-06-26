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

package com.openexchange.groupware.update;

import com.openexchange.exception.OXException;

/**
 * {@link UpdateTask} - A single update task.
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @deprecated use {@link UpdateTaskV2} or more easy {@link UpdateTaskAdapter}.
 */
@Deprecated
public interface UpdateTask {

    /**
     * Priorities for update tasks.
     */
    public static enum UpdateTaskPriority {
        HIGHEST(0), HIGH(1), NORMAL(3), LOW(4), LOWEST(5);

        public final int priority;

        private UpdateTaskPriority(final int priority) {
            this.priority = priority;
        }

        public static UpdateTaskPriority getInstance(final int priority) {
            for (final UpdateTaskPriority test : values()) {
                if (test.priority == priority) {
                    return test;
                }
            }
            throw new IllegalArgumentException();
        }

        public boolean equalOrHigher(final UpdateTaskPriority otherPriority) {
            return this.ordinal() <= otherPriority.ordinal();
        }
    }

    /**
     * Returns the database schema version of this update task.
     * <p>
     * For statically added update tasks an even number is supposed to be used, whereby an uneven number is supposed to be used by
     * dynamically added update tasks. Thus version always increments by 2 for stable releases.
     * <p>
     * This version is compared with the schema version of the database. This update will only be applied if the database schema version is
     * lower than this version. Remember to register your update task in the configuration file for update tasks or to publish it as an OSGi
     * service.
     *
     * @return The schema version with which this update task was introduced.
     */
    int addedWithVersion();

    /**
     * Gets this update task's priority.
     * <p>
     * Returned value is supposed to be provided by one of the {@link UpdateTaskPriority} constants:
     *
     * <pre>
     * public int getPriority() {
     *     return UpdateTask.UpdateTaskPriority.NORMAL.priority;
     * }
     * </pre>
     *
     * @return The update task's priority
     */
    int getPriority();

    /**
     * This method is called to apply the changes to the database schema. Performed changes must not destroy any information in the
     * database. This ensures that update tasks can be executed twice if a task failed.
     *
     * @param schema The schema meta data
     * @param contextId The context ID which is used to fetch database connections from the pool.
     * @throws OXException if applying the changes fails.
     */
    void perform(Schema schema, int contextId) throws OXException;

}
