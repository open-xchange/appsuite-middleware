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

import java.util.HashMap;
import java.util.Map;

/**
 * {@link AbstractMultifactorDevice} - a base class for describing a multi-factor authentication device
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.2
 */
public abstract class AbstractMultifactorDevice implements MultifactorDevice {

    private String  id;
    private String  name;
    private Boolean enabled = null;
    private boolean isBackup = false;
    private boolean isTrustedApplicationDevice = false;
    private String providerName;
    private Map<String, Object> parameters = null;

    private boolean b_isBackup = false;
    private boolean b_isTrustedApplicationDevice = false;


    /**
     * Initializes a new {@link AbstractMultifactorDevice}.
     *
     * @param id A unique ID of the device
     * @param providerName The name of the device's provider
     */
    public AbstractMultifactorDevice(String id, String providerName) {
        this(id, providerName, null, new HashMap<>());
    }

    /**
     * Initializes a new {@link AbstractMultifactorDevice}.
     *
     * @param id A unique ID of the device
     * @param providerName The name of the device's provider
     * @param name A user friendly name which describes the device
     * @param backup Whether this device is a backup device or not
     */
    public AbstractMultifactorDevice(String id, String providerName, String name) {
        this(id, providerName, name, new HashMap<>());
    }

    /**
     * Initializes a new {@link AbstractMultifactorDevice}.
     *
     * @param id A unique ID of the device
     * @param providerName The name of the device's provider
     * @param name A user friendly name which describes the device
     * @param parameters Additional parameters
     */
    public AbstractMultifactorDevice(String id, String providerName, String name, Map<String, Object> parameters) {
        this.id = id;
        this.providerName = providerName;
        this.name = name;
        this.parameters = parameters;
    }

    /**
     * Convenience method to checks if the device contains a parameter
     *
     * @param parameterName The name of the parameter to check
     * @return True, if the device contains a parameter with the given name, flase otherwise
     */
    protected boolean containsParameter(String parameterName) {
        return parameters != null && parameters.containsKey(parameterName);
    }

    /**
     * Convenience method to set a device specific parameter
     *
     * @param name The name of the parameter to set
     * @param value The value of the parameter to set
     */
    protected void setParameter(String name, Object value) {
        parameters.put(name, value);
    }

    /**
     * Convenience method to remove the parameter with the given name if present
     *
     * @param name The name of the parameter to remove
     */
    protected void removeParamter(String name) {
        if (parameters != null) {
            parameters.remove(name);
        }
    }

    /**
     * Convenience method to get a specific device parameter
     *
     * @param name The name of the parameter
     * @return The parameter with the given name, or null if no such parameter is present
     */
    @SuppressWarnings("unchecked")
    protected <T> T getParameter(String name) {
        return parameters != null ? (T) parameters.get(name) : null;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getProviderName() {
        return this.providerName;
    }

    @Override
    public void setProviderName(String name) {
        this.providerName = name;
    }

    @Override
    public Boolean isEnabled() {
       return Boolean.TRUE.equals(this.enabled);
    }

    @Override
    public void enable(Boolean enable){
       this.enabled = enable;
    }

    @Override
    public boolean isBackup() {
        return this.isBackup;
    }

    @Override
    public void setBackup(boolean backup) {
        this.isBackup = backup;
        this.b_isBackup = true;
    }

    @Override
    public boolean containsBackup() {
        return b_isBackup;
    }

    @Override
    public void removeBackup() {
       isBackup = false;
       b_isBackup = false;
    }

    @Override
    public Boolean isTrustedApplicationDevice() {
        return isTrustedApplicationDevice;
    }

    @Override
    public void setIsTrustedApplicationDevice(boolean isTrustedApplicationDevice) {
        this.isTrustedApplicationDevice = isTrustedApplicationDevice;
        this.b_isTrustedApplicationDevice = true;
    }

    @Override
    public boolean containsIsTrustedApplcationDevice() {
        return b_isTrustedApplicationDevice;
    }

    @Override
    public void removeIsTrustedApplicationDevice() {
       isTrustedApplicationDevice = false;
       b_isTrustedApplicationDevice = false;
    }

    @Override
    public Map<String, Object> getParameters() {
        return parameters;
    }

    @Override
    public boolean containsParameters() {
        return parameters != null && parameters.size() > 0;
    }

    @Override
    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }

    @Override
    public void removeParameters() {
        this.parameters = null;
    }
}
