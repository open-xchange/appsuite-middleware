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

package com.openexchange.admin.reseller.rmi.dataobjects;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import com.openexchange.admin.rmi.dataobjects.EnforceableDataObject;
import com.openexchange.admin.rmi.dataobjects.PasswordMechObject;

/**
 * {@link ResellerAdmin}
 * 
 * @author <a href="mailto:carsten.hoeger@open-xchange.com">Carsten Hoeger</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class ResellerAdmin extends EnforceableDataObject implements PasswordMechObject, Cloneable {

    private static final long serialVersionUID = 7212339205350666355L;

    private Integer id;
    private boolean idset = false;
    private Integer parentId;
    private boolean parentIdset = false;
    private String name;
    private boolean nameset = false;
    private String password;
    private boolean passwordset = false;
    private String passwordMech;
    private boolean passwordMechset = false;
    private byte[] salt;
    private boolean saltSet = false;
    private String displayname;
    private boolean displaynameset = false;
    private Restriction[] restrictions;
    private boolean restrictionsset = false;
    private String parentName;
    private boolean parentNameset = false;

    private Set<String> capabilities;
    private Set<String> capabilitiesToAdd;
    private boolean capasToAddSet = false;
    private Set<String> capabilitiesToRemove;
    private boolean capasToRemoveSet = false;
    private Set<String> capabilitiesToDrop;
    private boolean capasToDropSet = false;

    private Set<String> taxonomies;
    private Set<String> taxonomiesToAdd;
    private boolean taxonomiesToAddSet = false;
    private Set<String> taxonomiesToRemove;
    private boolean taxonomiesToRemoveSet = false;

    private Map<String, String> configuration;
    private Map<String, String> configurationToAdd;
    private boolean configurationToAddSet = false;
    private Set<String> configurationToRemove;
    private boolean configurationToRemoveSet = false;

    public ResellerAdmin() {
        super();
        init();
    }

    /**
     * @param id
     */
    public ResellerAdmin(final int id) {
        super();
        init();
        setId(Integer.valueOf(id));
    }

    /**
     * @param name
     */
    public ResellerAdmin(final String name) {
        super();
        init();
        setName(name);
    }

    /**
     * Initializes a new {@link ResellerAdmin}.
     *
     * @param id
     * @param name
     */
    public ResellerAdmin(Integer id, String name) {
        super();
        setId(id);
        setName(name);
    }

    /**
     * @param name
     * @param password
     */
    public ResellerAdmin(final String name, final String password) {
        super();
        init();
        setName(name);
        setPassword(password);
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
    public byte[] getSalt() {
        return salt;
    }

    /**
     * @return the pid
     */
    public Integer getParentId() {
        return parentId;
    }

    private void init() {
        id = null;
        parentId = null;
        name = null;
        password = null;
        displayname = null;
        passwordMech = null;
        salt = null;
        restrictions = null;
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
     * @return the saltSet
     */
    public boolean isSaltSet() {
        return saltSet;
    }

    /**
     * @return the passwordset
     */
    public boolean isPasswordset() {
        return passwordset;
    }

    /**
     * @return the pidset
     */
    public boolean isParentIdset() {
        return parentIdset;
    }

    /**
     * @param display_name the display_name to set
     */
    public void setDisplayname(final String displayname) {
        displaynameset = true;
        this.displayname = displayname;
    }

    /**
     * @param id the id to set
     */
    public void setId(final Integer id) {
        idset = true;
        this.id = id;
    }

    /**
     * @param name the name to set
     */
    public void setName(final String name) {
        nameset = true;
        this.name = name;
    }

    /**
     * @param password the password to set
     */
    public void setPassword(final String password) {
        passwordset = true;
        this.password = password;
    }

    @Override
    public void setPasswordMech(final String passwordMech) {
        passwordMechset = true;
        this.passwordMech = passwordMech;
    }

    /**
     * @param salt the salt to set
     */
    public void setSalt(final byte[] salt) {
        saltSet = true;
        this.salt = salt;
    }

    /**
     * This parameter is currently not used
     *
     * @param pid the pid to set
     */
    public void setParentId(final Integer pid) {
        parentIdset = true;
        parentId = pid;
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
        capasToAddSet = true;
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
        capasToRemoveSet = true;
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
        capasToDropSet = true;
        this.capabilitiesToDrop = capabilitiesToDrop;
    }

    /**
     * Gets the capasToAddSet
     *
     * @return The capasToAddSet
     */
    public boolean isCapabilitiesToAddSet() {
        return capasToAddSet;
    }

    /**
     * Gets the capasToRemoveSet
     *
     * @return The capasToRemoveSet
     */
    public boolean isCapabilitiesToRemoveSet() {
        return capasToRemoveSet;
    }

    /**
     * Gets the capasToDropSet
     *
     * @return The capasToDropSet
     */
    public boolean isCapabilitiesToDropSet() {
        return capasToDropSet;
    }

    /**
     * Gets the taxonomies to add
     *
     * @return The taxonomies to add
     */
    public Set<String> getTaxonomiesToAdd() {
        return taxonomiesToAdd;
    }

    /**
     * Sets the taxonomies
     *
     * @param taxonomies The taxonomies to set
     */
    public void setTaxonomiesToAdd(Set<String> taxonomies) {
        taxonomiesToAddSet = true;
        taxonomiesToAdd = taxonomies;
    }

    /**
     * Gets the taxonomiesSet
     *
     * @return The taxonomiesSet
     */
    public boolean isTaxonomiesToAddSet() {
        return taxonomiesToAddSet;
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
        taxonomiesToRemoveSet = true;
        this.taxonomiesToRemove = taxonomiesToRemove;
    }

    /**
     * Gets the taxonomiesToRemoveSet
     *
     * @return The taxonomiesToRemoveSet
     */
    public boolean isTaxonomiesToRemoveSet() {
        return taxonomiesToRemoveSet;
    }

    /**
     * Gets the configurationToAdd
     *
     * @return The configurationToAdd
     */
    public Map<String, String> getConfigurationToAdd() {
        return configurationToAdd;
    }

    /**
     * Sets the configurationToAdd
     *
     * @param configurationToAdd The configurationToAdd to set
     */
    public void setConfigurationToAdd(Map<String, String> configurationToAdd) {
        configurationToAddSet = true;
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
        configurationToRemoveSet = true;
        this.configurationToRemove = configurationToRemove;
    }

    /**
     * Gets the configurationToAddSet
     *
     * @return The configurationToAddSet
     */
    public boolean isConfigurationToAddSet() {
        return configurationToAddSet;
    }

    /**
     * Gets the configurationToRemoveSet
     *
     * @return The configurationToRemoveSet
     */
    public boolean isConfigurationToRemoveSet() {
        return configurationToRemoveSet;
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
     * Gets the configuration
     *
     * @return The configuration
     */
    public Map<String, String> getConfiguration() {
        return configuration;
    }

    /**
     * Sets the configuration
     *
     * @param configuration The configuration to set
     */
    public void setConfiguration(Map<String, String> configuration) {
        this.configuration = configuration;
    }

    /**
     * @return the restrictions
     */
    public final Restriction[] getRestrictions() {
        return restrictions;
    }

    /**
     * @param restrictions the restrictions to set
     */
    public final void setRestrictions(final Restriction[] restrictions) {
        restrictionsset = true;
        this.restrictions = restrictions;
    }

    public final boolean isRestrictionsset() {
        return restrictionsset;
    }

    public final String getParentName() {
        return parentName;
    }

    public void setParentName(final String parentName) {
        parentNameset = true;
        this.parentName = parentName;
    }

    public final boolean isParentNameset() {
        return parentNameset;
    }

    @Override
    public final String toString() {
        StringBuilder ret = new StringBuilder();
        ret.append("[ \n");
        for (final Field f : this.getClass().getDeclaredFields()) {
            try {
                Object ob = f.get(this);
                String tname = f.getName();
                if (ob != null && !tname.equals("serialVersionUID")) {
                    ret.append("  ");
                    ret.append(tname);
                    ret.append(": ");
                    ret.append(ob);
                    ret.append("\n");
                }
            } catch (IllegalArgumentException e) {
                ret.append("IllegalArgument\n");
            } catch (IllegalAccessException e) {
                ret.append("IllegalAccessException\n");
            }
        }
        ret.append("]");
        return ret.toString();
    }

    @Override
    public String[] getMandatoryMembersChange() {
        return null;
    }

    @Override
    public String[] getMandatoryMembersCreate() {
        return new String[] { "displayname", "name", "password" };
    }

    @Override
    public String[] getMandatoryMembersDelete() {
        return null;
    }

    @Override
    public String[] getMandatoryMembersRegister() {
        return null;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((capabilities == null) ? 0 : capabilities.hashCode());
        result = prime * result + ((capabilitiesToAdd == null) ? 0 : capabilitiesToAdd.hashCode());
        result = prime * result + ((capabilitiesToDrop == null) ? 0 : capabilitiesToDrop.hashCode());
        result = prime * result + ((capabilitiesToRemove == null) ? 0 : capabilitiesToRemove.hashCode());
        result = prime * result + (capasToAddSet ? 1231 : 1237);
        result = prime * result + (capasToDropSet ? 1231 : 1237);
        result = prime * result + (capasToRemoveSet ? 1231 : 1237);
        result = prime * result + ((configuration == null) ? 0 : configuration.hashCode());
        result = prime * result + ((configurationToAdd == null) ? 0 : configurationToAdd.hashCode());
        result = prime * result + (configurationToAddSet ? 1231 : 1237);
        result = prime * result + ((configurationToRemove == null) ? 0 : configurationToRemove.hashCode());
        result = prime * result + (configurationToRemoveSet ? 1231 : 1237);
        result = prime * result + ((displayname == null) ? 0 : displayname.hashCode());
        result = prime * result + (displaynameset ? 1231 : 1237);
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + (idset ? 1231 : 1237);
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + (nameset ? 1231 : 1237);
        result = prime * result + ((parentId == null) ? 0 : parentId.hashCode());
        result = prime * result + (parentIdset ? 1231 : 1237);
        result = prime * result + ((parentName == null) ? 0 : parentName.hashCode());
        result = prime * result + (parentNameset ? 1231 : 1237);
        result = prime * result + ((password == null) ? 0 : password.hashCode());
        result = prime * result + ((passwordMech == null) ? 0 : passwordMech.hashCode());
        result = prime * result + (passwordMechset ? 1231 : 1237);
        result = prime * result + (passwordset ? 1231 : 1237);
        result = prime * result + Arrays.hashCode(restrictions);
        result = prime * result + (restrictionsset ? 1231 : 1237);
        result = prime * result + Arrays.hashCode(salt);
        result = prime * result + (saltSet ? 1231 : 1237);
        result = prime * result + ((taxonomies == null) ? 0 : taxonomies.hashCode());
        result = prime * result + ((taxonomiesToAdd == null) ? 0 : taxonomiesToAdd.hashCode());
        result = prime * result + (taxonomiesToAddSet ? 1231 : 1237);
        result = prime * result + ((taxonomiesToRemove == null) ? 0 : taxonomiesToRemove.hashCode());
        result = prime * result + (taxonomiesToRemoveSet ? 1231 : 1237);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
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
        if (capabilitiesToAdd == null) {
            if (other.capabilitiesToAdd != null) {
                return false;
            }
        } else if (!capabilitiesToAdd.equals(other.capabilitiesToAdd)) {
            return false;
        }
        if (capabilitiesToDrop == null) {
            if (other.capabilitiesToDrop != null) {
                return false;
            }
        } else if (!capabilitiesToDrop.equals(other.capabilitiesToDrop)) {
            return false;
        }
        if (capabilitiesToRemove == null) {
            if (other.capabilitiesToRemove != null) {
                return false;
            }
        } else if (!capabilitiesToRemove.equals(other.capabilitiesToRemove)) {
            return false;
        }
        if (capasToAddSet != other.capasToAddSet) {
            return false;
        }
        if (capasToDropSet != other.capasToDropSet) {
            return false;
        }
        if (capasToRemoveSet != other.capasToRemoveSet) {
            return false;
        }
        if (configuration == null) {
            if (other.configuration != null) {
                return false;
            }
        } else if (!configuration.equals(other.configuration)) {
            return false;
        }
        if (configurationToAdd == null) {
            if (other.configurationToAdd != null) {
                return false;
            }
        } else if (!configurationToAdd.equals(other.configurationToAdd)) {
            return false;
        }
        if (configurationToAddSet != other.configurationToAddSet) {
            return false;
        }
        if (configurationToRemove == null) {
            if (other.configurationToRemove != null) {
                return false;
            }
        } else if (!configurationToRemove.equals(other.configurationToRemove)) {
            return false;
        }
        if (configurationToRemoveSet != other.configurationToRemoveSet) {
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
        if (parentName == null) {
            if (other.parentName != null) {
                return false;
            }
        } else if (!parentName.equals(other.parentName)) {
            return false;
        }
        if (parentNameset != other.parentNameset) {
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
        if (!Arrays.equals(restrictions, other.restrictions)) {
            return false;
        }
        if (restrictionsset != other.restrictionsset) {
            return false;
        }
        if (!Arrays.equals(salt, other.salt)) {
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
        if (taxonomiesToAdd == null) {
            if (other.taxonomiesToAdd != null) {
                return false;
            }
        } else if (!taxonomiesToAdd.equals(other.taxonomiesToAdd)) {
            return false;
        }
        if (taxonomiesToAddSet != other.taxonomiesToAddSet) {
            return false;
        }
        if (taxonomiesToRemove == null) {
            if (other.taxonomiesToRemove != null) {
                return false;
            }
        } else if (!taxonomiesToRemove.equals(other.taxonomiesToRemove)) {
            return false;
        }
        if (taxonomiesToRemoveSet != other.taxonomiesToRemoveSet) {
            return false;
        }
        return true;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    /**
     * Prints out the reseller data
     *
     * @return The reseller data as string
     */
    public String printOut() {
        StringBuilder ret = new StringBuilder();
        ret.append("[ \n");
        for (Field f : this.getClass().getDeclaredFields()) {
            try {
                Object ob = f.get(this);
                String tname = f.getName();
                if (tname.equals("restrictions") && restrictions != null) {
                    ret.append("  ");
                    ret.append(tname);
                    ret.append(": ");
                    for (Restriction r : restrictions) {
                        ret.append("{");
                        ret.append(r).append("}, ");
                    }
                    ret.setLength(ret.length() - 2);
                    ret.append("\n");
                } else if (ob != null && (!tname.equals("serialVersionUID") && !tname.equals("password") && !tname.equals("salt"))) {
                    ret.append("  ");
                    ret.append(tname);
                    ret.append(": ");
                    ret.append(ob);
                    ret.append("\n");
                }
            } catch (IllegalArgumentException | IllegalAccessException e) {
                ret.append(e.getClass().getSimpleName()).append("\n");
            }
        }
        ret.append("]");
        return ret.toString();
    }
}
