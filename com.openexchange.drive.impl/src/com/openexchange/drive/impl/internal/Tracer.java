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

package com.openexchange.drive.impl.internal;

import java.util.Date;
import com.openexchange.drive.impl.DriveConstants;

/**
 * {@link Tracer}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class Tracer {

    /**
     * The maximum length of the stored trace log.
     */
    public static final int MAX_SIZE = 100000;

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(Tracer.class);

    private final StringBuilder traceLog;

    /**
     * Initializes a new {@link Tracer}.
     *
     * @param clientDiagnostics Whether to write a diagnostics log or not.
     */
    public Tracer(Boolean clientDiagnostics) {
        super();
        this.traceLog = null != clientDiagnostics && clientDiagnostics.booleanValue() ? new StringBuilder() : null;
    }

    /**
     * Appends a new line for the supplied message into the trace log.
     *
     * @param message The message to trace
     */
    public void trace(Object message) {
        if (isTraceEnabled()) {
            String msg = String.valueOf(message);
            LOG.trace(msg, System.lineSeparator());
            if (null != traceLog) {
                int remainingCapacity = MAX_SIZE - traceLog.length();
                if (0 < remainingCapacity) {
                    traceLog.append(DriveConstants.LOG_DATE_FORMAT.get().format(new Date()))
                        .append(" [").append(Thread.currentThread().getId()).append("] : ");
                    if (msg.length() <= remainingCapacity) {
                        traceLog.append(msg.trim()).append("\n\n");
                    } else {
                        traceLog.append(msg.substring(0, remainingCapacity)).append("\n... (truncated)");
                    }
                }
            }
        }
    }

    /**
     * Gets the recorded trace log.
     *
     * @return
     */
    public String getTraceLog() {
        return null != traceLog ? traceLog.toString() : null;
    }

    /**
     * Gets a value indicating whether tracing is enabled either in the named logger instance or the drive-internal diagnostics log
     * generator.
     *
     * @return <code>true</code> if tracing is enabled, <code>false</code>, otherwise
     */
    public boolean isTraceEnabled() {
        return LOG.isTraceEnabled() || null != traceLog;
    }

}
