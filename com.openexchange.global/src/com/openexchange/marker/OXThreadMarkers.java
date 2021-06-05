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

package com.openexchange.marker;

import java.io.Closeable;
import com.openexchange.startup.impl.ThreadLocalCloseableControl;

/**
 * {@link OXThreadMarkers} - Utility class for {@link OXThreadMarker}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public class OXThreadMarkers {

    /**
     * Initializes a new {@link OXThreadMarkers}.
     */
    private OXThreadMarkers() {
        super();
    }

    /**
     * Checks if current thread is processing an HTTP request.
     *
     * @return <code>true</code> if is processing an HTTP request; otherwise <code>false</code>
     */
    public static boolean isHttpRequestProcessing() {
        return isHttpRequestProcessing(Thread.currentThread());
    }

    /**
     * Checks if specified thread is processing an HTTP request.
     *
     * @param t The thread to check
     * @return <code>true</code> if is processing an HTTP request; otherwise <code>false</code>
     */
    public static boolean isHttpRequestProcessing(Thread t) {
        return ((t instanceof OXThreadMarker) && ((OXThreadMarker) t).isHttpRequestProcessing());
    }

    /**
     * Remembers specified {@code Closeable} instance.
     *
     * @param closeable The {@code Closeable} instance
     * @return <code>true</code> if successfully added; otherwise <code>false</code>
     */
    public static boolean rememberCloseable(Closeable closeable) {
        if (null == closeable) {
            return false;
        }

        Thread t = Thread.currentThread();
        if (t instanceof OXThreadMarker) {
            try {
                return ThreadLocalCloseableControl.getInstance().addCloseable(closeable);
            } catch (Exception e) {
                // Ignore
            }
        }

        return false;
    }

    /**
     * Remembers specified {@code Closeable} instance if current thread is processing an HTTP request.
     *
     * @param closeable The {@code Closeable} instance
     * @return <code>true</code> if successfully added; otherwise <code>false</code>
     */
    public static boolean rememberCloseableIfHttpRequestProcessing(Closeable closeable) {
        if (null == closeable) {
            return false;
        }

        Thread t = Thread.currentThread();
        if ((t instanceof OXThreadMarker) && ((OXThreadMarker) t).isHttpRequestProcessing()) {
            try {
                return ThreadLocalCloseableControl.getInstance().addCloseable(closeable);
            } catch (Exception e) {
                // Ignore
            }
        }

        return false;
    }

    /**
     * Un-Remembers specified {@code Closeable} instance.
     *
     * @param closeable The {@code Closeable} instance
     * @return <code>true</code> if successfully removed; otherwise <code>false</code>
     */
    public static boolean unrememberCloseable(Closeable closeable) {
        if (null == closeable) {
            return false;
        }

        Thread t = Thread.currentThread();
        if (t instanceof OXThreadMarker) {
            try {
                return ThreadLocalCloseableControl.getInstance().removeCloseable(closeable);
            } catch (Exception e) {
                // Ignore
            }
        }

        return false;
    }

}
