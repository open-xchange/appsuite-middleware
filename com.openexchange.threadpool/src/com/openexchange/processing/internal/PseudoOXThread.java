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

package com.openexchange.processing.internal;

import com.openexchange.marker.OXThreadMarker;

/**
 * {@link PseudoOXThread} - A pseudo OX thread to keep MDC during logging.
 * <p>
 * See <code>com.openexchange.logging.filter.MDCEnablerTurboFilter</code>.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.2
 */
public class PseudoOXThread extends Thread implements OXThreadMarker {

    /**
     * Initializes a new {@link PseudoOXThread}.
     */
    public PseudoOXThread() {
        super();
    }

    /**
     * Initializes a new {@link PseudoOXThread}.
     *
     * @param target
     *            the object whose {@code run} method is invoked when this thread
     *            is started. If {@code null}, this classes {@code run} method does
     *            nothing.
     */
    public PseudoOXThread(Runnable target) {
        super(target);
    }

    /**
     * Initializes a new {@link PseudoOXThread}.
     *
     * @param name
     *            the name of the new thread
     */
    public PseudoOXThread(String name) {
        super(name);
    }

    /**
     * Initializes a new {@link PseudoOXThread}.
     *
     * @param group
     *            the thread group. If {@code null} and there is a security
     *            manager, the group is determined by {@linkplain
     *            SecurityManager#getThreadGroup SecurityManager.getThreadGroup()}.
     *            If there is not a security manager or {@code
     *         SecurityManager.getThreadGroup()} returns {@code null}, the group
     *            is set to the current thread's thread group.
     *
     * @param target
     *            the object whose {@code run} method is invoked when this thread
     *            is started. If {@code null}, this thread's run method is invoked.
     */
    public PseudoOXThread(ThreadGroup group, Runnable target) {
        super(group, target);
    }

    /**
     * Initializes a new {@link PseudoOXThread}.
     *
     * @param group
     *            the thread group. If {@code null} and there is a security
     *            manager, the group is determined by {@linkplain
     *            SecurityManager#getThreadGroup SecurityManager.getThreadGroup()}.
     *            If there is not a security manager or {@code
     *         SecurityManager.getThreadGroup()} returns {@code null}, the group
     *            is set to the current thread's thread group.
     *
     * @param name
     *            the name of the new thread
     */
    public PseudoOXThread(ThreadGroup group, String name) {
        super(group, name);
    }

    /**
     * Initializes a new {@link PseudoOXThread}.
     *
     * @param target
     *            the object whose {@code run} method is invoked when this thread
     *            is started. If {@code null}, this thread's run method is invoked.
     *
     * @param name
     *            the name of the new thread
     */
    public PseudoOXThread(Runnable target, String name) {
        super(target, name);
    }

    /**
     * Initializes a new {@link PseudoOXThread}.
     *
     * @param group
     *            the thread group. If {@code null} and there is a security
     *            manager, the group is determined by {@linkplain
     *            SecurityManager#getThreadGroup SecurityManager.getThreadGroup()}.
     *            If there is not a security manager or {@code
     *         SecurityManager.getThreadGroup()} returns {@code null}, the group
     *            is set to the current thread's thread group.
     *
     * @param target
     *            the object whose {@code run} method is invoked when this thread
     *            is started. If {@code null}, this thread's run method is invoked.
     *
     * @param name
     *            the name of the new thread
     */
    public PseudoOXThread(ThreadGroup group, Runnable target, String name) {
        super(group, target, name);
    }

    /**
     * Initializes a new {@link PseudoOXThread}.
     *
     * @param group
     *            the thread group. If {@code null} and there is a security
     *            manager, the group is determined by {@linkplain
     *            SecurityManager#getThreadGroup SecurityManager.getThreadGroup()}.
     *            If there is not a security manager or {@code
     *         SecurityManager.getThreadGroup()} returns {@code null}, the group
     *            is set to the current thread's thread group.
     *
     * @param target
     *            the object whose {@code run} method is invoked when this thread
     *            is started. If {@code null}, this thread's run method is invoked.
     *
     * @param name
     *            the name of the new thread
     *
     * @param stackSize
     *            the desired stack size for the new thread, or zero to indicate
     *            that this parameter is to be ignored.
     */
    public PseudoOXThread(ThreadGroup group, Runnable target, String name, long stackSize) {
        super(group, target, name, stackSize);
    }

    @Override
    public boolean isHttpRequestProcessing() {
        return false;
    }

    @Override
    public void setHttpRequestProcessing(boolean httpProcessing) {
        // Do nothing
    }

}
