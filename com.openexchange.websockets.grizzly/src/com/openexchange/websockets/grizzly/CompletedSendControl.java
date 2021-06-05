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

package com.openexchange.websockets.grizzly;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import com.openexchange.websockets.SendControl;


/**
 * {@link CompletedSendControl} - The send-control which already completed.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class CompletedSendControl implements SendControl {

    private static final CompletedSendControl INSTANCE = new CompletedSendControl();

    /**
     * Gets the instance
     *
     * @return The instance
     */
    public static CompletedSendControl getInstance() {
        return INSTANCE;
    }

    // -------------------------------------------------------------------------------------------

    /**
     * Initializes a new {@link CompletedSendControl}.
     */
    private CompletedSendControl() {
        super();
    }

    @Override
    public void awaitDone() throws InterruptedException {
        // Nothing
    }

    @Override
    public void awaitDone(long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
        // Nothing
    }

    @Override
    public boolean isDone() {
        return true;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return false;
    }

}
