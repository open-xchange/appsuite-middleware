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

package com.openexchange.admin.plugin.hosting.monitoring;

import java.util.concurrent.atomic.AtomicLong;
import javax.management.NotCompliantMBeanException;
import javax.management.StandardMBean;

/**
 *
 * @author cutmasta
 */
public final class Monitor extends StandardMBean implements MonitorMBean {

    private final AtomicLong createResourceCalls = new AtomicLong(0);

    private final AtomicLong createContextCalls = new AtomicLong(0);

    private final AtomicLong createUserCalls = new AtomicLong(0);

    private final AtomicLong createGroupCalls = new AtomicLong(0);

    public Monitor() throws NotCompliantMBeanException {
        super(MonitorMBean.class);
    }

    public void incrementNumberOfCreateResourceCalled() {
        createResourceCalls.incrementAndGet();
    }

    public void incrementNumberOfCreateContextCalled() {
        createContextCalls.incrementAndGet();
    }

    public void incrementNumberOfCreateUserCalled() {
        createUserCalls.incrementAndGet();
    }

    public void incrementNumberOfCreateGroupCalled() {
        createGroupCalls.incrementAndGet();
    }

    @Override
    public long getNumberOfCreateResourceCalled() {
        return createResourceCalls.get();
    }

    @Override
    public long getNumberOfCreateContextCalled() {
        return createContextCalls.get();
    }

    @Override
    public long getNumberOfCreateUserCalled() {
        return createUserCalls.get();
    }

    @Override
    public long getNumberOfCreateGroupCalled() {
        return createGroupCalls.get();
    }
}
