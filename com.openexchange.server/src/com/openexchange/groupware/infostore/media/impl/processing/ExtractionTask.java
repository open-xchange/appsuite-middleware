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

package com.openexchange.groupware.infostore.media.impl.processing;

import com.openexchange.session.UserAndContext;

/**
 * {@link ExtractionTask}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.2
 */
public class ExtractionTask implements Runnable {

    private final String key;
    private final Runnable task;
    private final UserAndContext stripeKey;
    private volatile Thread worker;

    /**
     * Initializes a new {@link ExtractionTask}.
     *
     * @param key The task's key
     * @param task The task to execute
     * @param stripeKey The stripe key
     */
    public ExtractionTask(String key, Runnable task, UserAndContext stripeKey) {
        super();
        this.key = key;
        this.task = task;
        this.stripeKey = stripeKey;
    }

    /**
     * Gets the key
     *
     * @return The key
     */
    public String getKey() {
        return key;
    }

    /**
     * Gets the stripe key, which is the user/context pair.
     *
     * @return The strip key
     */
    public UserAndContext getStripeKey() {
        return stripeKey;
    }

    @Override
    public void run() {
        worker = Thread.currentThread();
        try {
            task.run();
        } finally {
            worker = null;
        }
    }

    /**
     * Interrupts this task.
     */
    public void interrupt() {
        Thread worker = this.worker;
        if (null != worker) {
            worker.interrupt();
        }

    }

}
