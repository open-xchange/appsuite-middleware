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

package com.openexchange.imageconverter.api;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * {@link ElementLock}
 *
 * @author <a href="mailto:kai.ahrens@open-xchange.com">Kai Ahrens</a>
 * @since v7.10.0
 */
@SuppressWarnings("serial")
public class ElementLock extends ReentrantLock {

    /**
     * {@link LockMode}
     *
     * @author <a href="mailto:kai.ahrens@open-xchange.com">Kai Ahrens</a>
     * @since v7.10.0
     */
    public enum LockMode {
        STANDARD,
        BEGIN_PROCESSING,
        WAIT_IF_PROCESSED,
        TRY_LOCK;
    }

    /**
     * {@link UnlockMode}
     *
     * @author <a href="mailto:kai.ahrens@open-xchange.com">Kai Ahrens</a>
     * @since v7.10.2
     */
    public enum UnlockMode {
        STANDARD,
        END_PROCESSING
    }

    /**
     * Initializes a new {@link ElementLock}.
     */
    public ElementLock() {
        super();
    }

    /**
     * @return
     */
    public int incrementUseCount() {
        return ++m_useCount;
    }

    /**
     * @return
     */
    public int decrementUseCount() {
        return --m_useCount;
    }

    /**
     * @return
     */
    public boolean lock(final LockMode lockMode) {
        if (LockMode.TRY_LOCK == lockMode) {
            return super.tryLock();
        }

        super.lock();

        if (LockMode.WAIT_IF_PROCESSED == lockMode) {
            while (m_isProcessing && !Thread.currentThread().isInterrupted()) {
                try {
                    m_processingFinishedCondition.await();
                } catch (@SuppressWarnings("unused") InterruptedException e) {
                    // interrupted
                }
            }
        }

        if (LockMode.BEGIN_PROCESSING == lockMode) {
            m_isProcessing = true;
        }

        return true;
    }

    /**
     * Calling this method needs to be synchronized by the caller
     *
     * @return
     */
    public void unlock(final UnlockMode unlockMode) {
        if (m_isProcessing && (UnlockMode.END_PROCESSING == unlockMode)) {
            m_isProcessing = false;
            m_processingFinishedCondition.signalAll();
        }

        super.unlock();
    }

    /**
     * @return
     */
    public boolean isProcessing() {
        return m_isProcessing;
    }

    // - Members ---------------------------------------------------------------

    final private Condition m_processingFinishedCondition = newCondition();

    volatile private int m_useCount = 1;

    volatile private boolean m_isProcessing = false;
}
