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

package com.openexchange.chronos.provider.composition.impl;

import com.openexchange.capabilities.CapabilityChecker;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.exception.OXException;
import com.openexchange.session.Session;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;

/**
 * {@link CalendarPrintingCapabilityChecker}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.3
 */
public class CalendarPrintingCapabilityChecker implements CapabilityChecker {

    public static final String CAPABILITY_NAME = "calendar-printing";

    private final ConfigViewFactory configViewFactory;

    /**
     * Initializes a new {@link CalendarPrintingCapabilityChecker}.
     * 
     * @param configViewFactory A reference to the config view factory
     */
    public CalendarPrintingCapabilityChecker(ConfigViewFactory configViewFactory) {
        super();
        this.configViewFactory = configViewFactory;
    }

    @Override
    public boolean isEnabled(String capability, Session session) throws OXException {
        if (CAPABILITY_NAME.equals(capability)) {
            ServerSession serverSession = ServerSessionAdapter.valueOf(session);
            if (serverSession.isAnonymous() || false == serverSession.getUserPermissionBits().hasCalendar()) {
                return false;
            }
            /*
             * enabled if either absent, or explicitly true
             */
            ConfigView view = configViewFactory.getView(session.getUserId(), session.getContextId());
            return view.opt("com.openexchange.capability.calendar-printing", Boolean.class, Boolean.TRUE).booleanValue();
        }
        return true;
    }

}
