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
