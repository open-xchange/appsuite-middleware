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

package com.openexchange.admin.soap.multifactor.soap;

import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlType;

/**
 * {@link MultifactorDeviceResult}
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.2
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MultifactorDeviceResult", namespace = "http://dataobjects.rmi.admin.openexchange.com/xsd")
public class MultifactorDeviceResult {

    @XmlElement(required = true)
    protected String id;
    @XmlElement(required = true)
    protected String providerName;
    @XmlElement(required = true)
    protected String name;
    @XmlElement(required = true)
    protected boolean isEnabled;
    @XmlElement(required = true)
    protected boolean isBackupDevice;

    /**
     * Gets the ID of the device
     *
     * @return The ID of the device
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the ID of the device
     *
     * @param id The ID of the device
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Gets the name of the device
     *
     * @return The device name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the device's name
     *
     * @param name The new name of the device
     * @return this
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the name of the provider
     *
     * @return the provider name
     */
    public String getProviderName() {
        return providerName;
    }

    /**
     * Sets the name of the provider
     *
     * @param providerName The provider name
     */
    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }

    /**
     * Gets whether or not the device is enabled
     *
     * @return True, if the device is enabled, false otherwise
     */
    public boolean isEnabled() {
        return isEnabled;
    }

    /**
     * Sets the enabled state
     *
     * @param isEnabled true, if the device is enabled, false otherwise
     */
    public void setEnabled(boolean isEnabled) {
        this.isEnabled = isEnabled;
    }

    /**
     * Gets whether or not the device is a backup device
     *
     * @return True, if the device is a backup device, false otherwise
     */
    public boolean isBackupDevice() {
        return isBackupDevice;
    }

    /**
     * Sets whether or not the device is acting as a backup device
     *
     * @param isBackupDevice True if the device is a backup device, false otherwise
     */
    public void setBackupDevice(boolean isBackupDevice) {
        this.isBackupDevice = isBackupDevice;
    }
}
