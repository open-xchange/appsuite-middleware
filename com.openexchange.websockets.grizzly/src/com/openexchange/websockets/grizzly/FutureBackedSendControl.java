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

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.glassfish.grizzly.websockets.DataFrame;
import com.openexchange.exception.ExceptionUtils;
import com.openexchange.websockets.SendControl;


/**
 * {@link FutureBackedSendControl} - The send-control backed by a {@link Future} instance.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class FutureBackedSendControl implements SendControl {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(FutureBackedSendControl.class);

    private final Future<DataFrame> future;

    /**
     * Initializes a new {@link FutureBackedSendControl}.
     */
    public FutureBackedSendControl(Future<DataFrame> future) {
        super();
        this.future = future;
    }

    @Override
    public void awaitDone() throws InterruptedException {
        try {
            future.get();
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            ExceptionUtils.handleThrowable(cause);
            LOG.error("Web Socket message could not be sent", cause);
        }
    }

    @Override
    public void awaitDone(long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
        try {
            future.get(timeout, unit);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            ExceptionUtils.handleThrowable(cause);
            LOG.error("Web Socket message could not be sent", cause);
        }
    }

    @Override
    public boolean isDone() {
        return future.isDone();
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return future.cancel(mayInterruptIfRunning);
    }

}
