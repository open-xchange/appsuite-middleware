/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.logging;

/**
 * {@link MDCEnabledThreadGroup} - A special thread group to signal that {@link org.slf4j.MDC MDC} is supposed to be enabled for threads
 * belonging to that group.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.2
 * @deprecated The according <code>MDCEnablerTurboFilter</code> class has been removed, thus this thread group is nowhere used and is
 *             subject for being removed
 */
@Deprecated
public class MDCEnabledThreadGroup extends ThreadGroup {

    /**
     * Constructs a new MDC-enabled thread group with default name <code>"MDCEnabledThreadGroup"</code>.
     * The parent of this new group is the thread group of the currently running thread.
     * <p>
     * The <code>checkAccess</code> method of the parent thread group is
     * called with no arguments; this may result in a security exception.
     *
     * @throws SecurityException If the current thread cannot create a thread in the specified thread group.
     * @see java.lang.ThreadGroup#checkAccess()
     */
    public MDCEnabledThreadGroup() {
        super("MDCEnabledThreadGroup");
    }

    /**
     * Constructs a new MDC-enabled thread group. The parent of this new group is
     * the thread group of the currently running thread.
     * <p>
     * The <code>checkAccess</code> method of the parent thread group is
     * called with no arguments; this may result in a security exception.
     *
     * @param name The name of the new thread group.
     * @throws SecurityException If the current thread cannot create a thread in the specified thread group.
     * @see java.lang.ThreadGroup#checkAccess()
     */
    public MDCEnabledThreadGroup(String name) {
        super(name);
    }

    /**
     * Creates a new MDC-enabled thread group with default name <code>"MDCEnabledThreadGroup"</code>.
     * The parent of this new group is the specified thread group.
     * <p>
     * The <code>checkAccess</code> method of the parent thread group is
     * called with no arguments; this may result in a security exception.
     *
     * @param parent The parent thread group.
     * @throws NullPointerException If the thread group argument is <code>null</code>.
     * @throws SecurityException If the current thread cannot create a thread in the specified thread group.
     * @see java.lang.SecurityException
     * @see java.lang.ThreadGroup#checkAccess()
     */
    public MDCEnabledThreadGroup(ThreadGroup parent) {
        super(parent, "MDCEnabledThreadGroup");
    }

    /**
     * Creates a new thread group. The parent of this new group is the specified thread group.
     * <p>
     * The <code>checkAccess</code> method of the parent thread group is
     * called with no arguments; this may result in a security exception.
     *
     * @param parent The parent thread group.
     * @param name The name of the new thread group.
     * @throws NullPointerException If the thread group argument is <code>null</code>.
     * @throws SecurityException If the current thread cannot create a thread in the specified thread group.
     * @see java.lang.SecurityException
     * @see java.lang.ThreadGroup#checkAccess()
     */
    public MDCEnabledThreadGroup(ThreadGroup parent, String name) {
        super(parent, name);
    }

}
