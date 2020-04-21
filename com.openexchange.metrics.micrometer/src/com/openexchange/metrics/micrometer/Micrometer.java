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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.metrics.micrometer;

import java.util.function.ToDoubleFunction;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.lang.Nullable;

/**
 * Provides common utility methods for Micrometer metrics.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.10.4
 */
public class Micrometer {

    /**
     * Registers and returns a Gauge with given arguments at the given registry. If a Gauge with equal name and tags already exists,
     * it is removed from the registry first. Effectively an existing Gauge gets overridden within the registry. This is desired in
     * cases, where the observed instance {@code obj} gets replaced during application life time.
     *
     * @param registry The registry
     * @param name The name
     * @param tags The tags
     * @param description The description
     * @param baseUnit The base unit
     * @param obj The source object
     * @param f The value conversion function
     * @return The newly registered Gauge
     */
    public static <T> Gauge registerOrUpdateGauge(MeterRegistry registry, String name, Tags tags, @Nullable String description, @Nullable String baseUnit, @Nullable T obj, ToDoubleFunction<T> f) {
        Gauge oldGauge = registry.find(name).tags(tags).gauge();
        if (oldGauge != null) {
            registry.remove(oldGauge);
        }
        return Gauge.builder(name, obj, f)
            .description(description)
            .tags(tags)
            .baseUnit(baseUnit)
            .register(registry);
    }

}
