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

package com.openexchange.mail.json.compose.share;

import java.util.ArrayList;
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.mail.json.compose.Utilities;
import com.openexchange.mail.json.compose.share.spi.EnabledChecker;
import com.openexchange.session.Session;


/**
 * {@link DefaultEnabledChecker} - The default checker testing for <code>"drive"</code> and <code>"share_links"</code> capabilities.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.2
 */
public class DefaultEnabledChecker implements EnabledChecker {

    private static final DefaultEnabledChecker INSTANCE = new DefaultEnabledChecker();

    /**
     * Gets the instance
     *
     * @return The instance
     */
    public static DefaultEnabledChecker getInstance() {
        return INSTANCE;
    }

    // ----------------------------------------------------------------------------------------------------------------------------------

    /**
     * Initializes a new {@link DefaultEnabledChecker}.
     */
    protected DefaultEnabledChecker() {
        super();
    }

    @Override
    public boolean isEnabled(Session session) throws OXException {
        List<String> capsToCheck = new ArrayList<String>(4);
        capsToCheck.add("infostore");
        capsToCheck.add("share_links");
        capsToCheck = modifyCapabilitiesToCheck(capsToCheck, session);

        if (null == capsToCheck) {
            return true;
        }

        String[] caps = capsToCheck.toArray(new String[capsToCheck.size()]);
        return Utilities.hasCapabilities(session, caps);
    }

    /**
     * Modifies the specified capabilities (remove existing, add new ones) that are supposed to checked.
     *
     * @param capabilities The capabilities to modify
     * @param session The session providing user data
     * @return The possible modified capabilities
     */
    protected List<String> modifyCapabilitiesToCheck(List<String> capabilities, Session session) {
        // Return unchanged for default checker
        return capabilities;
    }

}
