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

package com.openexchange.reseller.data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

/**
 *
 * {@link ResellerAdmin} - A reseller administrator.
 * <pre>
 * ResellerAdmin ra = ResellerAdmin.builder()
 * .id(123)
 * .name("admin")
 * ...
 * .build();
 * </pre>
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.3
 */
public class ResellerAdmin implements PasswordMechObject, Cloneable, Serializable {

    private static final long serialVersionUID = -1123636946744408546L;

    /**
     * Creates a new builder instance.
     *
     * @return The new builder instance
     */
    public static ResellerAdminBuilder builder() {
        return new ResellerAdminBuilder();
    }

    /** The builder for an instance of <code>ResellerAdmin</code> */
    public static class ResellerAdminBuilder {

        private Integer id;
        private Integer parentId;
        private String name;
        private String password;
        private String passwordMech;
        private String salt;
        private String displayname;
        private List<Restriction> restrictions;
        private Set<ResellerCapability> capabilities;
        private Set<ResellerTaxonomy> taxonomies;
        private Map<String, ResellerConfigProperty> configuration;

        ResellerAdminBuilder() {
            super();
        }

        public ResellerAdminBuilder id(Integer id) {
            this.id = id;
            return this;
        }

        public ResellerAdminBuilder parentId(Integer id) {
            parentId = id;
            return this;
        }

        public ResellerAdminBuilder name(String name) {
            this.name = name;
            return this;
        }

        public ResellerAdminBuilder password(String password) {
            this.password = password;
            return this;
        }

        /**
         * Sets the password encryption mechanism, value is a password mechanism.
         * <p>
         * Currently supported mechanisms are <code>"{CRYPT}"</code> and <code>"{SHA}"</code>.
         *
         * @param passwordMech The passwordMech to set
         */
        public ResellerAdminBuilder passwordMech(String passwordMech) {
            this.passwordMech = passwordMech;
            return this;
        }

        public ResellerAdminBuilder salt(String salt) {
            this.salt = salt;
            return this;
        }

        public ResellerAdminBuilder displayname(String displayname) {
            this.displayname = displayname;
            return this;
        }

        public ResellerAdminBuilder restrictions(List<Restriction> restrictions) {
            this.restrictions = restrictions;
            return this;
        }

        public ResellerAdminBuilder capabilities(Set<ResellerCapability> capabilities) {
            this.capabilities = capabilities;
            return this;
        }

        public ResellerAdminBuilder taxonomies(Set<ResellerTaxonomy> taxonomies) {
            this.taxonomies = taxonomies;
            return this;
        }

        public ResellerAdminBuilder configuration(Map<String, ResellerConfigProperty> configuration) {
            this.configuration = configuration;
            return this;
        }

        /**
         * Creates the {@code ResellerAdmin} instance from this builder's attributes.
         *
         * @return The {@code ResellerAdmin} instance
         */
        public ResellerAdmin build() {
            return new ResellerAdmin(id, parentId, name, password, passwordMech, salt, displayname, restrictions, capabilities, configuration, taxonomies);
        }
    }

    // -------------------------------------------------------------------------------------------------------

    private final Integer id;
    private final boolean idset;

    private final Integer parentId;
    private final boolean parentIdset;

    private final String name;
    private final boolean nameset;

    private final String password;
    private final boolean passwordset;

    private final String passwordMech;
    private final boolean passwordMechset;

    private final String salt;
    private final boolean saltSet;

    private final String displayname;
    private final boolean displaynameset;

    private final List<Restriction> restrictions;
    private final boolean restrictionsset;

    private final Set<ResellerCapability> capabilities;
    private final boolean capabilitiesSet;

    private final Set<ResellerTaxonomy> taxonomies;
    private final boolean taxonomiesSet;

    private final Map<String, ResellerConfigProperty> configuration;
    private final boolean configurationSet;

    private volatile Integer hash; // For lazy hash-code computation

    /**
     * Initializes a new {@link ResellerAdmin}.
     * 
     * @param taxonomies
     * @param configuration
     * @param capabilities
     */
    ResellerAdmin(Integer id, Integer parentId, String name, String password, String passwordMech, String salt, String displayname, List<Restriction> restrictions, Set<ResellerCapability> capabilities, Map<String, ResellerConfigProperty> configuration, Set<ResellerTaxonomy> taxonomies) {
        super();

        if (id != null) {
            this.id = id;
            idset = true;
        } else {
            this.id = null;
            idset = false;
        }

        if (parentId != null) {
            this.parentId = parentId;
            parentIdset = true;
        } else {
            this.parentId = null;
            parentIdset = false;
        }

        if (name != null) {
            this.name = name;
            nameset = true;
        } else {
            this.name = null;
            nameset = false;
        }

        if (password != null) {
            this.password = password;
            passwordset = true;
        } else {
            this.password = null;
            passwordset = false;
        }

        if (passwordMech != null) {
            this.passwordMech = passwordMech;
            passwordMechset = true;
        } else {
            this.passwordMech = null;
            passwordMechset = false;
        }

        if (salt != null) {
            this.salt = salt;
            saltSet = true;
        } else {
            this.salt = null;
            saltSet = false;
        }

        if (displayname != null) {
            this.displayname = displayname;
            displaynameset = true;
        } else {
            this.displayname = null;
            displaynameset = false;
        }

        if (restrictions != null) {
            this.restrictions = ImmutableList.copyOf(restrictions);
            restrictionsset = true;
        } else {
            this.restrictions = null;
            restrictionsset = false;
        }

        if (capabilities != null) {
            this.capabilities = ImmutableSet.copyOf(capabilities);
            capabilitiesSet = true;
        } else {
            this.capabilities = null;
            capabilitiesSet = false;
        }

        if (configuration != null) {
            this.configuration = ImmutableMap.copyOf(configuration);
            configurationSet = true;
        } else {
            this.configuration = null;
            configurationSet = false;
        }

        if (taxonomies != null) {
            this.taxonomies = ImmutableSet.copyOf(taxonomies);
            taxonomiesSet = true;
        } else {
            this.taxonomies = null;
            taxonomiesSet = false;
        }
    }

    /**
     * @return the display_name
     */
    public String getDisplayname() {
        return displayname;
    }

    /**
     * @return the id
     */
    public Integer getId() {
        return id;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getPasswordMech() {
        return passwordMech;
    }

    @Override
    public String getSalt() {
        return salt;
    }

    /**
     * @return the pid
     */
    public Integer getParentId() {
        return parentId;
    }

    /**
     * @return the display_nameset
     */
    public boolean isDisplaynameset() {
        return displaynameset;
    }

    /**
     * @return the idset
     */
    public boolean isIdset() {
        return idset;
    }

    /**
     * @return the nameset
     */
    public boolean isNameset() {
        return nameset;
    }

    /**
     * @return the passwordMechset
     */
    public boolean isPasswordMechset() {
        return passwordMechset;
    }

    /**
     * @return the passwordset
     */
    public boolean isPasswordset() {
        return passwordset;
    }

    /**
     * @return the saltSet
     */
    public boolean isSaltSet() {
        return saltSet;
    }

    /**
     * @return the pidset
     */
    public boolean isParentIdset() {
        return parentIdset;
    }

    /**
     * @return the restrictions
     */
    public final List<Restriction> getRestrictions() {
        return restrictions;
    }

    public final boolean isRestrictionsset() {
        return restrictionsset;
    }

    /**
     * Gets the capabilities
     *
     * @return The capabilities
     */
    public Set<ResellerCapability> getCapabilities() {
        return capabilities;
    }

    /**
     * Gets the capabilitiesSet
     *
     * @return The capabilitiesSet
     */
    public boolean isCapabilitiesSet() {
        return capabilitiesSet;
    }

    /**
     * Gets the taxonomies
     *
     * @return The taxonomies
     */
    public Set<ResellerTaxonomy> getTaxonomies() {
        return taxonomies;
    }

    /**
     * Gets the taxonomiesSet
     *
     * @return The taxonomiesSet
     */
    public boolean isTaxonomiesSet() {
        return taxonomiesSet;
    }

    /**
     * Gets the configuration
     *
     * @return The configuration
     */
    public Map<String, ResellerConfigProperty> getConfiguration() {
        return configuration;
    }

    /**
     * Gets the configurationSet
     *
     * @return The configurationSet
     */
    public boolean isConfigurationSet() {
        return configurationSet;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(64);
        builder.append("{");
        if (id != null) {
            builder.append("id=").append(id).append(", ");
        }
        if (parentId != null) {
            builder.append("parentId=").append(parentId).append(", ");
        }
        if (name != null) {
            builder.append("name=").append(name).append(", ");
        }
        if (password != null) {
            builder.append("password=").append(password).append(", ");
        }
        if (passwordMech != null) {
            builder.append("passwordMech=").append(passwordMech).append(", ");
        }
        if (displayname != null) {
            builder.append("displayname=").append(displayname).append(", ");
        }
        if (restrictions != null) {
            builder.append("restrictions=").append(restrictions);
        }
        if (capabilities != null) {
            builder.append("capabilities=").append(capabilities);
        }
        if (configuration != null) {
            builder.append("configuration=").append(configuration);
        }
        if (taxonomies != null) {
            builder.append("taxonomies=").append(taxonomies);
        }
        builder.append("}");
        return builder.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((capabilities == null) ? 0 : capabilities.hashCode());
        result = prime * result + (capabilitiesSet ? 1231 : 1237);
        result = prime * result + ((configuration == null) ? 0 : configuration.hashCode());
        result = prime * result + (configurationSet ? 1231 : 1237);
        result = prime * result + ((displayname == null) ? 0 : displayname.hashCode());
        result = prime * result + (displaynameset ? 1231 : 1237);
        result = prime * result + ((hash == null) ? 0 : hash.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + (idset ? 1231 : 1237);
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + (nameset ? 1231 : 1237);
        result = prime * result + ((parentId == null) ? 0 : parentId.hashCode());
        result = prime * result + (parentIdset ? 1231 : 1237);
        result = prime * result + ((password == null) ? 0 : password.hashCode());
        result = prime * result + ((passwordMech == null) ? 0 : passwordMech.hashCode());
        result = prime * result + (passwordMechset ? 1231 : 1237);
        result = prime * result + (passwordset ? 1231 : 1237);
        result = prime * result + ((restrictions == null) ? 0 : restrictions.hashCode());
        result = prime * result + (restrictionsset ? 1231 : 1237);
        result = prime * result + ((salt == null) ? 0 : salt.hashCode());
        result = prime * result + (saltSet ? 1231 : 1237);
        result = prime * result + ((taxonomies == null) ? 0 : taxonomies.hashCode());
        result = prime * result + (taxonomiesSet ? 1231 : 1237);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ResellerAdmin other = (ResellerAdmin) obj;
        if (capabilities == null) {
            if (other.capabilities != null) {
                return false;
            }
        } else if (!capabilities.equals(other.capabilities)) {
            return false;
        }
        if (capabilitiesSet != other.capabilitiesSet) {
            return false;
        }
        if (configuration == null) {
            if (other.configuration != null) {
                return false;
            }
        } else if (!configuration.equals(other.configuration)) {
            return false;
        }
        if (configurationSet != other.configurationSet) {
            return false;
        }
        if (displayname == null) {
            if (other.displayname != null) {
                return false;
            }
        } else if (!displayname.equals(other.displayname)) {
            return false;
        }
        if (displaynameset != other.displaynameset) {
            return false;
        }
        if (hash == null) {
            if (other.hash != null) {
                return false;
            }
        } else if (!hash.equals(other.hash)) {
            return false;
        }
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        if (idset != other.idset) {
            return false;
        }
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (nameset != other.nameset) {
            return false;
        }
        if (parentId == null) {
            if (other.parentId != null) {
                return false;
            }
        } else if (!parentId.equals(other.parentId)) {
            return false;
        }
        if (parentIdset != other.parentIdset) {
            return false;
        }
        if (password == null) {
            if (other.password != null) {
                return false;
            }
        } else if (!password.equals(other.password)) {
            return false;
        }
        if (passwordMech == null) {
            if (other.passwordMech != null) {
                return false;
            }
        } else if (!passwordMech.equals(other.passwordMech)) {
            return false;
        }
        if (passwordMechset != other.passwordMechset) {
            return false;
        }
        if (passwordset != other.passwordset) {
            return false;
        }
        if (restrictions == null) {
            if (other.restrictions != null) {
                return false;
            }
        } else if (!restrictions.equals(other.restrictions)) {
            return false;
        }
        if (restrictionsset != other.restrictionsset) {
            return false;
        }
        if (salt == null) {
            if (other.salt != null) {
                return false;
            }
        } else if (!salt.equals(other.salt)) {
            return false;
        }
        if (saltSet != other.saltSet) {
            return false;
        }
        if (taxonomies == null) {
            if (other.taxonomies != null) {
                return false;
            }
        } else if (!taxonomies.equals(other.taxonomies)) {
            return false;
        }
        if (taxonomiesSet != other.taxonomiesSet) {
            return false;
        }
        return true;
    }

    @Override
    public ResellerAdmin clone() {
        try {
            return (ResellerAdmin) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError("CloneNotSupportedException although Cloneable is implemented.", e);
        }
    }

}
