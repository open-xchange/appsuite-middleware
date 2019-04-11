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
        WAIT_IF_PROCESSED,
        WAIT_IF_PROCESSED_AND_BEGIN_PROCESSING,
        PROCESSING,
        TRY_LOCK;

        /**
         * @return
         */
        boolean needsWaiting() {
            return (PROCESSING != this);
        }

        /**
         * @return
         */
        boolean isBeginProcessing() {
            return (WAIT_IF_PROCESSED_AND_BEGIN_PROCESSING == this);
        }

        /**
         * @return
         */
        boolean isTryLock() {
            return TRY_LOCK == this;
        }
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
    public boolean lock(final LockMode lockMode) {
        if (lockMode.isTryLock()) {
            if (!super.tryLock()) {
                return false;
            }
        } else {
            super.lock();
        }

        if (lockMode.needsWaiting()) {
            while (m_isProcessing && !Thread.currentThread().isInterrupted()) {
                try {
                    m_processingFinishedCondition.await();
                } catch (@SuppressWarnings("unused") InterruptedException e) {
                    // interrupted
                }
            }
        }

        if (lockMode.isBeginProcessing()) {
            m_isProcessing = true;
        }

        return true;
    }

    /**
     * @return
     */
    public long unlock(final boolean finishProcessing) {
        if (finishProcessing && m_isProcessing) {
            m_isProcessing = false;
            m_processingFinishedCondition.signalAll();
        }

        super.unlock();

        return getHoldCount();
    }

    /**
     * @return
     */
    public boolean isProcessing() {
        return m_isProcessing;
    }

    // - Members ---------------------------------------------------------------

    final private Condition m_processingFinishedCondition = newCondition();

    volatile private boolean m_isProcessing = false;
}
