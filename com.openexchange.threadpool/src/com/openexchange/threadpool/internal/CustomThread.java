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

package com.openexchange.threadpool.internal;

import com.openexchange.threadpool.ThreadRenamer;

/**
 * {@link CustomThread} - Enhances {@link Thread} class by a setter/getter method for a thread's original name.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class CustomThread extends Thread implements ThreadRenamer {

    private volatile String originalName;
    private volatile String appendix;
    private volatile boolean changed;

    /**
     * Initializes a new {@link CustomThread}.
     */
    public CustomThread() {
        super();
    }

    /**
     * Initializes a new {@link CustomThread}.
     *
     * @param target The object whose run method is called
     */
    public CustomThread(final Runnable target) {
        super(target);
    }

    /**
     * Initializes a new {@link CustomThread}.
     *
     * @param name The name of the new thread which is also used as original name
     */
    public CustomThread(final String name) {
        super(name);
        applyName(name);
    }

    /**
     * Initializes a new {@link CustomThread}.
     *
     * @param group The thread group
     * @param target The object whose run method is called
     */
    public CustomThread(final ThreadGroup group, final Runnable target) {
        super(group, target);
    }

    /**
     * Initializes a new {@link CustomThread}.
     *
     * @param group The thread group
     * @param name The name of the new thread which is also set used its original name
     */
    public CustomThread(final ThreadGroup group, final String name) {
        super(group, name);
        applyName(name);
    }

    /**
     * Initializes a new {@link CustomThread}.
     *
     * @param target The object whose run method is called
     * @param name The name of the new thread which is also used as original name
     */
    public CustomThread(final Runnable target, final String name) {
        super(target, name);
        applyName(name);
    }

    /**
     * Initializes a new {@link CustomThread}.
     *
     * @param group The thread group
     * @param target The object whose run method is called
     * @param name The name of the new thread which is also used as original name
     */
    public CustomThread(final ThreadGroup group, final Runnable target, final String name) {
        super(group, target, name);
        applyName(name);
    }

    /**
     * Initializes a new {@link CustomThread}.
     *
     * @param group The thread group
     * @param target The object whose run method is called
     * @param name The name of the new thread which is also used as original name
     * @param stackSize The desired stack size for the new thread, or zero to indicate that this parameter is to be ignored
     */
    public CustomThread(final ThreadGroup group, final Runnable target, final String name, final long stackSize) {
        super(group, target, name, stackSize);
        applyName(name);
    }

    private void applyName(final String name) {
        originalName = name;
        final int pos = originalName.indexOf('-');
        if (pos > 0) {
            appendix = name.substring(pos);
        } else {
            appendix = null;
        }
    }

    /**
     * Gets the original name.
     *
     * @return The original name
     */
    public String getOriginalName() {
        return originalName;
    }

    /**
     * Restores the original name.
     */
    public void restoreName() {
        if (!changed) {
            return;
        }
        setName(originalName);
    }

    @Override
    public void rename(final String newName) {
        setName(newName);
        changed = true;
    }

    @Override
    public void renamePrefix(final String newPrefix) {
        if (null == appendix) {
            setName(newPrefix);
        } else {
            setName(new com.openexchange.java.StringAllocator(16).append(newPrefix).append(appendix).toString());
        }
        changed = true;
    }

}
