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

package com.openexchange.groupware.reminder.internal;

import static com.openexchange.java.Autoboxing.I;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.reminder.ReminderExceptionCode;
import com.openexchange.groupware.reminder.TargetService;

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
