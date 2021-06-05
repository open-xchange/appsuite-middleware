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

package com.openexchange.imageconverter.api;

import java.util.concurrent.atomic.AtomicInteger;
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
        return m_useCount.incrementAndGet();
    }

    /**
     * @return
     */
    public int decrementUseCount() {
        return m_useCount.decrementAndGet();
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

    final private AtomicInteger m_useCount = new AtomicInteger(1);

    volatile private boolean m_isProcessing = false;
}
