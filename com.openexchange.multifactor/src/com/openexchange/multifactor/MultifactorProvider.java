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

package com.openexchange.multifactor;

import java.util.Collection;
import java.util.Optional;
import com.openexchange.exception.OXException;

/**
 * {@link MultifactorProvider} - provider which manages devices for multi-factor/OTP authentication.
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.2
 */
public interface MultifactorProvider {

    /**
     * Gets the name of the provider
     *
     * @return The provider's name
     */
    String getName();

    /**
     * Returns if a multi-factor provider is enabled for a given user.
     * <br>
     * <br>
     * Only providers which are "enabled" can be used by a user.
     *
     * @param multifactorRequest The {@link MultifactorRequest}
     * @return <code>true</code>, if the provider is enabled for the given user, <code>false</code> otherwise
     */
    boolean isEnabled(MultifactorRequest multifactorRequest);

    /**
     * Returns <code>true</code> if this provider can be used as a backup for another multifactor method
     *
     * @return <code>true</code> if this provider can be used as a backup for another multifactor method, <code>false</code> otherwise
     */
    default boolean isBackupProvider() {
        return false;
    }

    /**
     * Returns <code>true</code> if this provider is ONLY used for backing up others
     *
     * @return <code>true</code> if this provider is ONLY used for backing up others, <code>false</code> otherwise
     */
    default boolean isBackupOnlyProvider() {
        return false;
    }

    /**
     * Returns if this provider can handles devices which are related to a "Trusted Application".
     *
     * A "trusted application" is a client which performs multi-factor authentication on behalf of the user automatically.
     *
     * @return <code>true</code>, if the provider can return devices which belongs to a "trusted application", <code>false</code> otherwise.
     */
    default boolean isTrustedApplicationProvider() {
        return false;
    }

    /**
     * Returns a collection of registered devices for the given session
     *
     * @param multifactorRequest The {@link MultifactorRequest}
     * @return A collection of registered devices, or an empty collection if no devices are registered for the given session
     */
    Collection<? extends MultifactorDevice> getDevices(MultifactorRequest multifactorRequest) throws OXException;

    /**
     * Returns a collection of enabled devices for the given session
     *
     * @param multifactorRequest The {@link MultifactorRequest}
     * @return A collection of registered devices which are enabled, or an empty collection if no devices are registered and enabled for the given session
     * @throws OXException
     */
    Collection<? extends MultifactorDevice> getEnabledDevices(MultifactorRequest multifactorRequest) throws OXException;

    /**
     * Returns a registered device by ID
     *
     * @param multifactorRequest The {@link MultifactorRequest}
     * @param deviceId The ID of the device to get
     * @return The {@link MultifactorDevice}
     */
    Optional<? extends MultifactorDevice> getDevice(MultifactorRequest multifactorRequest, String deviceId) throws OXException;

    /**
     * Starts registering a multi-factor authentication for a new device
     *
     * @param multifactorRequest The {@link MultifactorRequest}
     * @param device A new {@link MultifactorDevice} to start the registration process for
     * @return The {@link RegistrationChallenge}
     */
    RegistrationChallenge startRegistration(MultifactorRequest multifactorRequest, MultifactorDevice device) throws OXException;

    /**
     * Completes the registration for a given device
     *
     * @param multifactorRequest The {@link MultifactorRequest}
     * @param deviceId The id of the device to finish the registration for
     * @param answer The answer to a previously send {@link Challenge}
     * @return The registered {@link MultifactorDevice}
     */
    MultifactorDevice finishRegistration(MultifactorRequest multifactorRequest, String deviceId, ChallengeAnswer answer) throws OXException;

    /**
     * Unregisters a multi-factor authentication device
     *
     * @param multifactorRequest The {@link MultifactorRequest} to unregister the multi-factor auth
     * @param deviceId The id of the device to unregister
     * @throws OXException in case the device coulnd't be unregistered
     */
    void deleteRegistration(MultifactorRequest multifactorRequest, String deviceId) throws OXException;

    /**
     * Unregisters ALL multi-factor authentication devices for a given user
     *
     * @param contextId Context of the user
     * @param userId Id of the user
     * @throws OXException in case the devices coulnd't be unregistered
     */
    void deleteRegistrations(int contextId, int userId) throws OXException;

    /**
     * Deletes ALL multi-factor devices within a given context
     *
     * @param contextId  The id of the context
     * @throws OXException in case the devices coulnd't be unregistered
     */
    void deleteRegistrations(int contextId) throws OXException;

    /**
     * Initiates and authentication against a multifactor device
     * Generates a response for the UI, possibly with a challenge
     *
     * @param multifactorRequest The {@link MultifactorRequest}
     * @param deviceId The id of the device to begin the authentication for
     * @return The {@link Challenge}
     * @throws OXException
     */
    Challenge beginAuthentication(MultifactorRequest multifactorRequest, String deviceId) throws OXException;

    /**
     * Do an authentication against a device
     *
     * @param multifactorRequest The {@link MultifactorRequest}
     * @param deviceId The id of the device to authenticate against
     * @param answer The answer to the {@link Challenge}
     * @throws OXException If authentication failed
     */
    void doAuthentication(MultifactorRequest multifactorRequest, String deviceId, ChallengeAnswer answer) throws OXException;

    /**
     * Changes the name of a multifactor device
     *
     * @param multifactorRequest The {@link MultifactorRequest}
     * @param device The device containing the id and the new name
     * @return The changed {@link MultifactorDevice}
     * @throws OXException
     */
    MultifactorDevice renameDevice(MultifactorRequest multifactorRequest, MultifactorDevice device) throws OXException;
}
