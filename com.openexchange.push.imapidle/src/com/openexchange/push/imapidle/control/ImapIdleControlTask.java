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

package com.openexchange.push.imapidle.control;

import java.util.List;
import com.openexchange.threadpool.Task;
import com.openexchange.threadpool.ThreadRenamer;

/**
 * {@link ImapIdleControlTask} - Responsible for interrupting IMAP IDLE of expired threads.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public class ImapIdleControlTask extends AbstractImapIdleControlTask implements Task<Void> {

    private volatile boolean keepOn;

    /**
     * Initializes a new {@link ImapIdleControlTask}.
     */
    public ImapIdleControlTask(ImapIdleControl control) {
        super(control);
        keepOn = true;
    }

    @Override
    public Void call() {
        try {
            while (keepOn) {
                try {
                    List<ImapIdleRegistration> expired = control.awaitExpired();
                    for (ImapIdleRegistration registration : expired) {
                        // Idl'ing for too long
                        handleExpired(registration);
                    }
                } catch (InterruptedException e) {
                    throw e;
                } catch (Exception e) {
                    // Ignore
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return null;
    }

    /**
     * Signals to cancel this task.
     */
    public void cancel() {
        keepOn = false;
    }

    @Override
    public void afterExecute(Throwable throwable) {
        // NOP
    }

    @Override
    public void beforeExecute(Thread thread) {
        // NOP
    }

    @Override
    public void setThreadName(ThreadRenamer threadRenamer) {
        threadRenamer.renamePrefix("ImapIdleControlTask");
    }

}
