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

package com.openexchange.client.onboarding.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import com.openexchange.client.onboarding.OnboardingProvider;

/**
 * {@link RemoteOnboardingService}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public interface RemoteOnboardingService extends Remote {

    /**
     * RMI name to be used in the naming lookup.
     */
    public static final String RMI_NAME = RemoteOnboardingService.class.getSimpleName();

    /**
     * Gets the sorted identifiers of all currently registered {@link OnboardingProvider providers}.
     *
     * @return The sorted data of all providers
     * @throws RemoteOnboardingServiceException If providers cannot be returned
     * @throws RemoteException If a communication-related error occurs
     */
    List<List<String>> getAllProviders() throws RemoteOnboardingServiceException, RemoteException;
}
