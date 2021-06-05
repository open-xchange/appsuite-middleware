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

package com.openexchange.groupware.update.internal;

import com.openexchange.groupware.update.ProgressState;

/**
 * {@link ProgressStatusImpl}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class ProgressStatusImpl implements ProgressState {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ProgressStatusImpl.class);

    /**
     * Log every 10 seconds the state.
     */
    private static final long logTimeDistance = 10000;

    private final String taskName;

    private final String schema;

    private int total = -1;

    private int state = 0;

    private long lastLogTime;

    /**
     * Initializes a new {@link ProgressStatusImpl}.
     */
    public ProgressStatusImpl(String taskName, String schema) {
        super();
        this.taskName = taskName;
        this.schema = schema;
    }

    @Override
    public void setTotal(int total) {
        this.total = total;
        lastLogTime = System.currentTimeMillis();
    }

    @Override
    public void setState(int state) {
        this.state = state;
        logState();
    }

    @Override
    public void incrementState() {
        state++;
        logState();
    }

    private void logState() {
        if (total > 0) {
            long now = System.currentTimeMillis();
            if (now > lastLogTime + logTimeDistance) {
                lastLogTime = now;
                LOG.info("Update task {} finished {}% on schema {}.", taskName, Integer.valueOf((state * 100 / total)), schema);
            }
        }
    }
}
