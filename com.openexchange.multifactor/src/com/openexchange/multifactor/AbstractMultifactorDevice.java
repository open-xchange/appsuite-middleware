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
       return Boolean.valueOf(this.enabled != null && this.enabled.booleanValue());
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
        return isTrustedApplicationDevice ? Boolean.TRUE : Boolean.FALSE;
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
