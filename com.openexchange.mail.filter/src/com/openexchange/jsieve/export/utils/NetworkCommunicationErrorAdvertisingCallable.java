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

package com.openexchange.jsieve.export.utils;

import static com.openexchange.exception.ExceptionUtils.isEitherOf;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Callable;
import com.google.common.collect.ImmutableList;


/**
 * {@link NetworkCommunicationErrorAdvertisingCallable} - A special {@link Callable} calling an I/O operation, which only throws such
 * {@link IOException} that are considered as network communication error. Otherwise an instance of {@link IOResult} is returned that might
 * advertise an {@link IOException} as well.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.3
 */
public abstract class NetworkCommunicationErrorAdvertisingCallable<V> implements Callable<IOResult<V>> {

    private static final List<Class<? extends Exception>> NETWORK_COMMUNICATION_ERRORS = ImmutableList.of(
        java.net.SocketTimeoutException.class,
        java.io.EOFException.class);

    /**
     * Initializes a new {@link NetworkCommunicationErrorAdvertisingCallable}.
     */
    protected NetworkCommunicationErrorAdvertisingCallable() {
        super();
    }

    @Override
    public final IOResult<V> call() throws Exception {
        try {
            return IOResult.resultFor(performIOOperation());
        } catch (IOException e) {
            if (isEitherOf(e, NETWORK_COMMUNICATION_ERRORS)) {
                throw e;
            }
            return IOResult.errorFor(e);
        }
    }

    /**
     * Performs the I/O operation
     *
     * @return The result
     * @throws IOException An I/O error
     */
    protected abstract V performIOOperation() throws IOException;

}
