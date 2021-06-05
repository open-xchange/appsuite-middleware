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

package com.openexchange.mailfilter.json.osgi;

import com.openexchange.capabilities.CapabilityChecker;
import com.openexchange.exception.OXException;
import com.openexchange.session.Session;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;


/**
 * {@link MailFilterChecker}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class MailFilterChecker implements CapabilityChecker {

    public static final String CAPABILITY = "mailfilter";

    /**
     * Initializes a new {@link MailFilterChecker}.
     */
    public MailFilterChecker() {
        super();
    }

    @Override
    public boolean isEnabled(String capability, Session ses) throws OXException {
        if (CAPABILITY.equals(capability)) {
            final ServerSession session = ServerSessionAdapter.valueOf(ses);
            if (session.isAnonymous() || false == session.getUserPermissionBits().hasWebMail()) {
                return false;
            }
        }
        return true;
    }

}
