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

package com.openexchange.health.impl.checks;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import com.openexchange.exception.ExceptionUtils;
import com.openexchange.health.MWHealthCheck;
import com.openexchange.health.MWHealthCheckResponse;
import com.openexchange.health.impl.MWHealthCheckResponseImpl;


/**
 * {@link JVMHeapCheck}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.10.1
 */
public class JVMHeapCheck implements MWHealthCheck {

    private static final String NAME = "jvmHeap";
    private static final long TIMEOUT = 5000L;

    public JVMHeapCheck() {
        super();
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public long getTimeout() {
        return TIMEOUT;
    }

    @Override
    public MWHealthCheckResponse call() {
        MemoryMXBean bean = ManagementFactory.getMemoryMXBean();
        MemoryUsage usage = bean.getHeapMemoryUsage();
        boolean status = true;

        Map<String, Object> data = new HashMap<>();
        data.put("init", String.valueOf(usage.getInit()));
        data.put("max", String.valueOf(usage.getMax()));
        data.put("used", String.valueOf(usage.getUsed()));
        data.put("commited", String.valueOf(usage.getCommitted()));

        Date lastOOM = ExceptionUtils.getLastOOM();
        if (null != lastOOM) {
            status = false;
            data.put("lastOOM", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss,SSSZ").format(lastOOM));
        }

        return new MWHealthCheckResponseImpl(NAME, data, status);
    }

}
