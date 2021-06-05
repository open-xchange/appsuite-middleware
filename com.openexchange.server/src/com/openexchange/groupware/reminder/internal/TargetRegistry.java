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

package com.openexchange.groupware.reminder.internal;

import static com.openexchange.java.Autoboxing.I;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.reminder.ReminderExceptionCode;
import com.openexchange.groupware.reminder.TargetService;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;

/**
 * Registry for the {@link TargetService} instances.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class TargetRegistry {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(TargetRegistry.class);
    private static final TargetRegistry SINGLETON = new TargetRegistry();

    private final TIntObjectMap<TargetService> registry = new TIntObjectHashMap<TargetService>();

    private TargetRegistry() {
        super();
    }

    public static final TargetRegistry getInstance() {
        return SINGLETON;
    }

    public TargetService getService(final int module) throws OXException {
        final TargetService retval = registry.get(module);
        if (null == retval) {
            throw ReminderExceptionCode.NO_TARGET_SERVICE.create(I(module));
        }
        return retval;
    }

    public void addService(final int module, final TargetService targetService) {
        final TargetService previous = registry.putIfAbsent(module, targetService);
        if (null == previous) {
            return;
        }
        LOG.error("Duplicate registration of a reminder target service for module {} with implementation {}.", module, targetService.getClass().getName());
    }

    public void removeService(final int module) {
        registry.remove(module);
    }
}
