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

package com.openexchange.admin.soap.reseller.service.reseller.rmi.dataobjects;

import java.util.List;
import java.util.Set;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import com.openexchange.admin.soap.reseller.service.soap.dataobjects.SOAPStringMap;

/**
 * <p>SOAP mapping class for ResellerAdmin complex type.
 *
 * @author <a href="mailto:marcus@open-xchange.com">Marcus Klein</a>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ResellerAdmin", propOrder = {
    "name",
    "passwordMech",
    "password",
    "parentId",
    "restrictions",
    "displayName",
    "id",
    "capabilities",
    "capabilitiesToAdd",
    "capabilitiesToRemove",
    "capabilitiesToDrop",
    "taxonomies",
    "taxonomiesToAdd",
    "taxonomiesToRemove",
    "configuration",
    "configurationToAdd",
    "configurationToRemove"
})
public class ResellerAdmin {

    @XmlElement(nillable = true)
    protected Integer id;

    @XmlElement(nillable = true)
    protected Integer parentId;

    @XmlElement(nillable = true)
    protected String name;

    @XmlElement(nillable = true)
    protected String password;

    @XmlElement(nillable = true)
    protected String passwordMech;

    @XmlElement(name = "displayname", nillable = true)
    protected String displayName;

    @XmlElement(nillable = true)
    protected List<Restriction> restrictions;

    @XmlElement(nillable = true)
    protected Set<String> capabilities;

    @XmlElement(nillable = true)
    protected Set<String> capabilitiesToAdd;

    @XmlElement(nillable = true)
    protected Set<String> capabilitiesToRemove;

    @XmlElement(nillable = true)
    protected Set<String> capabilitiesToDrop;

    @XmlElement(nillable = true)
    protected Set<String> taxonomies;

    @XmlElement(nillable = true)
    protected Set<String> taxonomiesToAdd;

    @XmlElement(nillable = true)
    protected Set<String> taxonomiesToRemove;

    @XmlElement(nillable = true)
    protected SOAPStringMap configuration;

    @XmlElement(nillable = true)
    protected SOAPStringMap configurationToAdd;

    @XmlElement(nillable = true)
    protected Set<String> configurationToRemove;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getParentId() {
        return parentId;
    }

    public void setParentId(Integer parentId) {
        this.parentId = parentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPasswordMech() {
        return passwordMech;
    }

    public void setPasswordMech(String passwordMech) {
        this.passwordMech = passwordMech;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public List<Restriction> getRestrictions() {
        return restrictions;
    }

    public void setRestrictions(List<Restriction> restrictions) {
        this.restrictions = restrictions;
    }

    /**
     * Gets the capabilities
     *
     * @return The capabilities
     */
    public Set<String> getCapabilities() {
        return capabilities;
    }

    /**
     * Sets the capabilities
     *
     * @param capabilities The capabilities to set
     */
    public void setCapabilities(Set<String> capabilities) {
        this.capabilities = capabilities;
    }

    /**
     * Gets the capabilitiesToAdd
     *
     * @return The capabilitiesToAdd
     */
    public Set<String> getCapabilitiesToAdd() {
        return capabilitiesToAdd;
    }

    /**
     * Sets the capabilitiesToAdd
     *
     * @param capabilitiesToAdd The capabilitiesToAdd to set
     */
    public void setCapabilitiesToAdd(Set<String> capabilitiesToAdd) {
        this.capabilitiesToAdd = capabilitiesToAdd;
    }

    /**
     * Gets the capabilitiesToRemove
     *
     * @return The capabilitiesToRemove
     */
    public Set<String> getCapabilitiesToRemove() {
        return capabilitiesToRemove;
    }

    /**
     * Sets the capabilitiesToRemove
     *
     * @param capabilitiesToRemove The capabilitiesToRemove to set
     */
    public void setCapabilitiesToRemove(Set<String> capabilitiesToRemove) {
        this.capabilitiesToRemove = capabilitiesToRemove;
    }

    /**
     * Gets the capabilitiesToDrop
     *
     * @return The capabilitiesToDrop
     */
    public Set<String> getCapabilitiesToDrop() {
        return capabilitiesToDrop;
    }

    /**
     * Sets the capabilitiesToDrop
     *
     * @param capabilitiesToDrop The capabilitiesToDrop to set
     */
    public void setCapabilitiesToDrop(Set<String> capabilitiesToDrop) {
        this.capabilitiesToDrop = capabilitiesToDrop;
    }

    /**
     * Gets the taxonomies
     *
     * @return The taxonomies
     */
    public Set<String> getTaxonomies() {
        return taxonomies;
    }

    /**
     * Sets the taxonomies
     *
     * @param taxonomies The taxonomies to set
     */
    public void setTaxonomies(Set<String> taxonomies) {
        this.taxonomies = taxonomies;
    }

    /**
     * Gets the taxonomiesToAdd
     *
     * @return The taxonomiesToAdd
     */
    public Set<String> getTaxonomiesToAdd() {
        return taxonomiesToAdd;
    }

    /**
     * Sets the taxonomiesToAdd
     *
     * @param taxonomiesToAdd The taxonomiesToAdd to set
     */
    public void setTaxonomiesToAdd(Set<String> taxonomiesToAdd) {
        this.taxonomiesToAdd = taxonomiesToAdd;
    }

    /**
     * Gets the taxonomiesToRemove
     *
     * @return The taxonomiesToRemove
     */
    public Set<String> getTaxonomiesToRemove() {
        return taxonomiesToRemove;
    }

    /**
     * Sets the taxonomiesToRemove
     *
     * @param taxonomiesToRemove The taxonomiesToRemove to set
     */
    public void setTaxonomiesToRemove(Set<String> taxonomiesToRemove) {
        this.taxonomiesToRemove = taxonomiesToRemove;
    }

    /**
     * Gets the configuration
     *
     * @return The configuration
     */
    public SOAPStringMap getConfiguration() {
        return configuration;
    }

    /**
     * Sets the configuration
     *
     * @param configuration The configuration to set
     */
    public void setConfiguration(SOAPStringMap configuration) {
        this.configuration = configuration;
    }

    /**
     * Gets the configurationToAdd
     *
     * @return The configurationToAdd
     */
    public SOAPStringMap getConfigurationToAdd() {
        return configurationToAdd;
    }

    /**
     * Sets the configurationToAdd
     *
     * @param configurationToAdd The configurationToAdd to set
     */
    public void setConfigurationToAdd(SOAPStringMap configurationToAdd) {
        this.configurationToAdd = configurationToAdd;
    }

    /**
     * Gets the configurationToRemove
     *
     * @return The configurationToRemove
     */
    public Set<String> getConfigurationToRemove() {
        return configurationToRemove;
    }

    /**
     * Sets the configurationToRemove
     *
     * @param configurationToRemove The configurationToRemove to set
     */
    public void setConfigurationToRemove(Set<String> configurationToRemove) {
        this.configurationToRemove = configurationToRemove;
    }
}
