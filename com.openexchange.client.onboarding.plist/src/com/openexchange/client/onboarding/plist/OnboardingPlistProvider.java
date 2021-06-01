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

package com.openexchange.client.onboarding.plist;

import com.openexchange.client.onboarding.OnboardingProvider;
import com.openexchange.client.onboarding.Scenario;
import com.openexchange.exception.OXException;
import com.openexchange.plist.PListDict;


/**
 * {@link OnboardingPlistProvider}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.1
 */
public interface OnboardingPlistProvider extends OnboardingProvider {

    /**
     * Retrieves the PLIST dictionary for the given user and scenario.
     *
     * @param optPrevPListDict The optional previous PLIST dictionary; or <code>null</code>
     * @param scenario The scenario
     * @param hostName The host name to use
     * @param userId The user id
     * @param contextId The context id
     * @return The associated PLIST dictionary
     * @throws OXException If returning PLIST dictionary fails
     */
    PListDict getPlist(PListDict optPrevPListDict, Scenario scenario, String hostName, int userId, int contextId) throws OXException;

}
