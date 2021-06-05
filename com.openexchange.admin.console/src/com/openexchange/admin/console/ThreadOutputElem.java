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

package com.openexchange.admin.console;

public class ThreadOutputElem {

    private final long threadId;
    private final String threadName;
    private final long allocatedBytes;
    private final long cpuTime;
    private final long userTime;
    private StackTraceElement[] stackTrace;

    public ThreadOutputElem(final long threadId, final String threadName, final long allocatedBytes, final long cpuTime, final long userTime) {
        this.threadId = threadId;
        this.threadName = threadName;
        this.allocatedBytes = allocatedBytes;
        this.cpuTime = cpuTime;
        this.userTime = userTime;
    }

    public ThreadOutputElem(final long threadId, final String threadName, final long allocatedBytes, final long cpuTime, final long userTime, final StackTraceElement[] stackTrace) {
        this.threadId = threadId;
        this.threadName = threadName;
        this.allocatedBytes = allocatedBytes;
        this.cpuTime = cpuTime;
        this.userTime = userTime;
        this.stackTrace = stackTrace;
    }

    public long getThreadId() {
        return threadId;
    }

    public String getThreadName() {
        return threadName;
    }

    public long getAllocatedBytes() {
        return allocatedBytes;
    }

    public long getCpuTime() {
        return cpuTime;
    }

    public long getUserTime() {
        return userTime;
    }

    public StackTraceElement[] getStackTrace() {
        return stackTrace;
    }

    public void setStackTrace(final StackTraceElement[] stackTrace) {
        this.stackTrace = stackTrace;
    }
}
