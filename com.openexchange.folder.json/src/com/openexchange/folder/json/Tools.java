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

package com.openexchange.folder.json;

import java.util.TimeZone;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

/**
 * {@link Tools} - A utility class for folder storage processing.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class Tools {

    /**
     * Initializes a new {@link Tools}.
     */
    private Tools() {
        super();
    }

    private static final ConcurrentMap<String, Future<TimeZone>> TZ_MAP = new ConcurrentHashMap<String, Future<TimeZone>>();

    /**
     * Gets the <code>TimeZone</code> for the given ID.
     *
     * @param timeZoneID The ID for a <code>TimeZone</code>, either an abbreviation such as "PST", a full name such as
     *            "America/Los_Angeles", or a custom ID such as "GMT-8:00".
     * @return The specified <code>TimeZone</code>, or the GMT zone if the given ID cannot be understood.
     * @throws IllegalStateException If this method fails to return an appropriate time zone
     */
    public static TimeZone getTimeZone(final String timeZoneID) {
        if (null == timeZoneID) {
            return null;
        }
        Future<TimeZone> future = TZ_MAP.get(timeZoneID);
        if (null == future) {
            final FutureTask<TimeZone> ft = new FutureTask<TimeZone>(new Callable<TimeZone>() {

                @Override
                public TimeZone call() throws Exception {
                    return TimeZone.getTimeZone(timeZoneID);
                }
            });
            future = TZ_MAP.putIfAbsent(timeZoneID, ft);
            if (null == future) {
                future = ft;
                ft.run();
            }
        }
        try {
            return future.get();
        } catch (InterruptedException e) {
            // Keep interrupted status
            Thread.currentThread().interrupt();
            final IllegalStateException ise = new IllegalStateException(e.getMessage());
            ise.initCause(e);
            throw ise;
        } catch (CancellationException e) {
            final IllegalStateException ise = new IllegalStateException(e.getMessage());
            ise.initCause(e);
            throw ise;
        } catch (ExecutionException e) {
            final Throwable cause = e.getCause();
            if (cause instanceof Exception) {
                final IllegalStateException ise = new IllegalStateException(e.getMessage());
                ise.initCause(e);
                throw ise;
            }
            if (cause instanceof Error) {
                throw (Error) cause;
            }
            throw new IllegalStateException("Not unchecked", cause);
        }
    }
}
