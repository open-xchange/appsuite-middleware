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
            LOG.trace(msg);
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
