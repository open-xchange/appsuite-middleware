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

import java.util.Map;

/**
 * {@link MultifactorDevice} - represents a device for performing multi-factor authentication
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.2
 */
public interface MultifactorDevice {

    /**
     * Gets the ID of the device
     *
     * @return The ID
     */
    String getId();

    /**
     * Sets the ID of the device
     *
     * @param id The ID of the device
     */
    void setId(String id);

    /**
     * Gets the user friendly name of the device
     *
     * @return The name of the device
     */
    String getName();

    /**
     * Sets the device name
     *
     * @param name the new name
     */
    void setName(String name);

    /**
     * Returns the name of the device's provider
     *
     * @return The name of the device provider
     */
    String getProviderName();

    /**
     * Sets the name of the device's provider
     *
     * @param name The name of the provider
     */
    void setProviderName(String name);

    /**
     * Returns the state of the device
     *
     * @return Returns <code>true</code>, if this device is enabled, <code>false</code> if disabled
     */
    Boolean isEnabled();

    /**
     * Enables or disables the device
     *
     * @param enable <code>true</code>, to enable the device, <code>false</code> to disable, null to reset the state
     */
    void enable(Boolean enable);

    /**
     * Returns true if device registered as backup device
     *
     * @return <code>true</code> if this device is a backup device, <code>false</code> otherwise
     */
    boolean isBackup();

    /**
     * Sets backup status of the device
     *
     * @param backup The new backup status of the device
     */
    void setBackup(boolean backup);

    /**
     * Contains method for {@link #isBackup()} field
     *
     * @return <code>true</code>, if {@link #isBackup()} is set, <code>false</code> otherwise
     */
    boolean containsBackup();

    /**
     * Remove method for {@link #isBackup()} field
     */
    void removeBackup();

    /**
     * Returns true if the device is related to a "trusted application" which performs multi-factor authentication automatically on behalf of the user
     *
     * @return <code>true</code>, if the device is related to a "trusted application", <code>false<code>if not, null if the state is not set
     */
    Boolean isTrustedApplicationDevice();

    /**
     * Sets whether or not this device is related to a "trusted application" which performs multi-factor authentication automatically on behalf of the user
     *
     * @param isTrustedApplicationDevice whether this device is related to a "trusted application" or not
     */
    void setIsTrustedApplicationDevice(boolean isTrustedApplicationDevice);

    /**
     * Contains method for {@link #isTrustedApplicationDevice()}
     *
     * @return <code>true</code>, if {@link #isTrustedApplicationDevice()} is set, <code>false</code> otherwise
     */
    boolean containsIsTrustedApplcationDevice();

    /**
     * Remove method for {@link #isTrustedApplicationDevice()}
     */
    void removeIsTrustedApplicationDevice();

    /**
     * Gets the specific, parameters required to perform registration or authentication, like a secret_token, a signed challenge, etc
     *
     * @return The additional parameters required to perform authentication or registration of the device
     */
    Map<String, Object> getParameters();

    /**
     * Gets whether this device contains specific parameters
     *
     * @return <code>true</code> if the device contains additional parameters, <code>false</code> otherwise
     */
    boolean containsParameters();

    /**
     * Sets some specific, parameters required to perform registration or authentication, like a secret_token, a signed challenge, etc
     *
     * @param parameters Additional parameters required to perform authentication or registration of the device
     */
    void setParameters(Map<String, Object> parameters);

    /**
     * Convenience method to remove all specific/additional, parameters from the device
     */
    void removeParameters();
}