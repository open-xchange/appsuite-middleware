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
 *    trademarks of the OX Software GmbH. group of companies.
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
