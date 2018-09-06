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

package com.openexchange.health.check;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import com.openexchange.health.DefaultHealthCheck;
import com.openexchange.health.DefaultHealthCheckResponse;
import com.openexchange.health.DefaultHealthCheckResponseBuilder;
import com.openexchange.server.ServiceLookup;


/**
 * {@link JVMHeapHealthCheck}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.10.1
 */
public class JVMHeapHealthCheck extends DefaultHealthCheck {

    private static final String NAME = "jvmHeap";

    private final ServiceLookup services;

    public JVMHeapHealthCheck(ServiceLookup services) {
        super(NAME);
        this.services = services;
    }

    @Override
    public DefaultHealthCheckResponse execute() {
        MemoryMXBean bean = ManagementFactory.getMemoryMXBean();
        MemoryUsage usage = bean.getHeapMemoryUsage();
        boolean status = true;

        Map<String, String> data = new HashMap<>();
        data.put("init", String.valueOf(usage.getInit()));
        data.put("max", String.valueOf(usage.getMax()));
        data.put("used", String.valueOf(usage.getUsed()));
        data.put("commited", String.valueOf(usage.getCommitted()));

        Properties systemProperties = System.getProperties();
        if (null != systemProperties && systemProperties.contains("__lastOOM")) {
            status = false;
            data.put("lastOOM", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss,SSSZ").format(systemProperties.getProperty("__lastOOM")));
        }

        DefaultHealthCheckResponseBuilder builder = new DefaultHealthCheckResponseBuilder();
        builder.name(NAME).state(status);
        for (String key : data.keySet()) {
            builder.withData(key, data.get(key));
        }
        return builder.build();
    }

}
