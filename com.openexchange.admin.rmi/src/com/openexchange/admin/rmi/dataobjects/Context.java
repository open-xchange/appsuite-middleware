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

package com.openexchange.admin.rmi.dataobjects;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * Class representing a context.
 *
 * @author <a href="mailto:manuel.kraft@open-xchange.com">Manuel Kraft</a>
 * @author <a href="mailto:carsten.hoeger@open-xchange.com">Carsten Hoeger</a>
 * @author <a href="mailto:dennis.sieben@open-xchange.com">Dennis Sieben</a>
 *
 */
public class Context extends ExtendableDataObject implements NameAndIdObject {
    /**
     *
     */
    private static final long serialVersionUID = -8939189372445990901L;

    private Integer id;

    private boolean idset;

    private Database readDatabase;

    private boolean readDatabaseset;

    private Database writeDatabase;

    private boolean writeDatabaseset;

    private Integer filestore_id;

    private boolean filestore_idset;

    private String filestore_name;

    private boolean filestore_nameset;

    private Long average_size;

    private boolean average_sizeset;

    private Long maxQuota;

    private boolean maxQuotaset;

    private Long usedQuota;

    private boolean usedQuotaset;

    private MaintenanceReason maintenanceReason;

    private boolean maintenanceReasonset;

    private Boolean enabled;

    private boolean enabledset;

    private String name;

    private boolean nameset;

    private HashSet<String> login_mappings;

    private Map<String, Map<String, String>> userAttributes = null;

    private boolean userAttribtuesset;

    private boolean listrun;

    private List<Object[]> quotas;

    public Context() {
        super();
        init();
    }

    /**
     * @param id
     */
    public Context(final Integer id) {
        super();
        init();
        setId(id);
    }

    /**
     * @param id
     * @param name
     */
    public Context(final int id, final String name) {
        super();
        this.id = id;
        this.name = name;
    }

    @Override
    public final Integer getId() {
        return this.id;
    }

    public final String getIdAsString() {
        return null == id ? null : id.toString();
    }

    @Override
    public final void setId(final Integer id) {
        this.id = id;
        this.idset = true;
    }

    @Override
    public final String getName() {
        return name;
    }

    @Override
    public final void setName(final String name) {
        this.name = name;
        this.nameset = true;
    }

    /*
     * Add login mappings.
     * Example:
     * If you add here  a HashSet containing "mydomain.org", then you can later
     * login with <username>@ mydomain.org OR   <username>@<context_id>
     *
     */
    public final void setLoginMappings(final HashSet<String> mappings) {
        this.login_mappings = mappings;
    }

    /*
     * Add a single login mapping entry.
     */
    public final void addLoginMapping(final String mapping) {
        if (null == mapping) {
            return;
        }
        if (this.login_mappings == null) {
            this.login_mappings = new HashSet<String>(4);
        }
        this.login_mappings.add(mapping);
    }

    public final void addLoginMappings(final Collection<String> mapping) {
        if (null == mapping) {
            return;
        }
        if (this.login_mappings == null) {
            this.login_mappings = new HashSet<String>(4);
        }
        for (final String sMapping : mapping) {
            if (null != sMapping) {
                this.login_mappings.add(sMapping);
            }
        }
    }

    /*
     * Remove a login mapping.
     */
    public final boolean removeLoginMapping(final String mapping) {
        if (null == mapping) {
            return false;
        }
        if (null != this.login_mappings) {
            return this.login_mappings.remove(mapping);
        } else {
            return false;
        }
    }

    public final boolean removeLoginMappings(final Collection<String> mapping) {
        if (null == mapping) {
            return false;
        }
        if (null != this.login_mappings) {
            return this.login_mappings.removeAll(mapping);
        } else {
            return false;
        }
    }

    public final HashSet<String> getLoginMappings() {
        return this.login_mappings;
    }

    public final Integer getFilestoreId() {
        return filestore_id;
    }

    public final void setFilestoreId(final Integer filestore_id) {
        this.filestore_id = filestore_id;
        this.filestore_idset = true;
    }

    /**
     * @return max Quota (in MB)
     */
    public final Long getMaxQuota() {
        return maxQuota;
    }

    /**
     *
     * @param maxQuota (in MB)
     */
    public final void setMaxQuota(final Long maxQuota) {
        this.maxQuota = maxQuota;
        this.maxQuotaset = true;
    }

    /**
     * @return used Quota (in MB)
     */
    public final Long getUsedQuota() {
        return usedQuota;
    }

    public final void setUsedQuota(final Long usedQuota) {
        this.usedQuota = usedQuota;
        this.usedQuotaset = true;
    }

    public final MaintenanceReason getMaintenanceReason() {
        return maintenanceReason;
    }

    public final void setMaintenanceReason(final MaintenanceReason maintenanceReason) {
        this.maintenanceReason = maintenanceReason;
        this.maintenanceReasonset = true;
    }

    public final Boolean isEnabled() {
        return enabled;
    }

    public final void setEnabled(final Boolean enabled) {
        this.enabled = enabled;
        this.enabledset = true;
    }

    public final Database getReadDatabase() {
        return readDatabase;
    }

    public final void setReadDatabase(final Database readDatabase) {
        this.readDatabase = readDatabase;
        this.readDatabaseset = true;
    }

    public final Database getWriteDatabase() {
        return writeDatabase;
    }

    public final void setWriteDatabase(final Database writeDatabase) {
        this.writeDatabase = writeDatabase;
        this.writeDatabaseset = true;
    }

    /**
     * @return configured average size (in MB)
     */
    public final Long getAverage_size() {
        return average_size;
    }

    /**
     * The context average size can only be configured in AdminDaemon.properties
     */
    public final void setAverage_size(final Long average_size) {
        this.average_size = average_size;
        this.average_sizeset = true;
    }

    public final String getFilestore_name() {
        return filestore_name;
    }

    public final void setFilestore_name(final String filestore_name) {
        this.filestore_name = filestore_name;
        this.filestore_nameset = true;
    }

    /*
     * DO NOT make this final because others might want to extend this
     * (non-Javadoc)
     * @see com.openexchange.admin.rmi.dataobjects.ExtendableDataObject#toString()
     */
    @Override
    public String toString() {
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

    private void init() {
        initExtendable();
        this.id = null;
        this.name = null;
        this.enabled = false;
        this.filestore_id = null;
        this.average_size = null;
        this.maintenanceReason = null;
        this.maxQuota = null;
        this.usedQuota = null;
        this.readDatabase = null;
        this.writeDatabase = null;
        this.login_mappings = null;
        this.listrun = false;
        this.quotas = Collections.emptyList();
    }

    /**
     * At the moment no fields are defined here
     */
    @Override
    public String[] getMandatoryMembersChange() {
        return null;
    }

    /**
     * At the moment {@link #setId} and {@link #setMaxQuota} are defined here
     */
    @Override
    public String[] getMandatoryMembersCreate() {
        return new String[]{ "id", "maxQuota" };
    }

    /**
     * At the moment no fields are defined here
     */
    @Override
    public String[] getMandatoryMembersDelete() {
        return null;
    }

    /**
     * At the moment no fields are defined here
     */
    @Override
    public String[] getMandatoryMembersRegister() {
        return null;
    }

    /**
     * Gets the quotas
     * <pre>
     *  (["calendar", 1000000L], ["contact", 1000000L], ...)
     * </pre>
     *
     * @return The quotas
     */
    public List<Object[]> getQuotas() {
        return Collections.unmodifiableList(quotas);
    }

    /**
     * Sets the quotas
     * <pre>
     *  (["calendar", 1000000L], ["contact", 1000000L], ...)
     * </pre>
     *
     * @param quotas The quotas to set
     */
    public void setQuotas(List<Object[]> quotas) {
        if (null == quotas) {
            this.quotas = Collections.emptyList();
        } else {
            this.quotas = new ArrayList<Object[]>(quotas);
        }
    }

    /**
     * @return the average_sizeset
     */
    public boolean isAverage_sizeset() {
        return average_sizeset;
    }

    /**
     * @return the enabledset
     */
    public boolean isEnabledset() {
        return enabledset;
    }

    /**
     * @return the filestore_idset
     */
    public boolean isFilestore_idset() {
        return filestore_idset;
    }

    /**
     * @return the filestore_nameset
     */
    public boolean isFilestore_nameset() {
        return filestore_nameset;
    }

    /**
     * @return the idset
     */
    public boolean isIdset() {
        return idset;
    }

    /**
     * @return the maintenanceReasonset
     */
    public boolean isMaintenanceReasonset() {
        return maintenanceReasonset;
    }

    /**
     * @return the maxQuotaset
     */
    public boolean isMaxQuotaset() {
        return maxQuotaset;
    }

    /**
     * @return the nameset
     */
    public boolean isNameset() {
        return nameset;
    }

    /**
     * @return the readDatabaseset
     */
    public boolean isReadDatabaseset() {
        return readDatabaseset;
    }

    /**
     * @return the usedQuotaset
     */
    public boolean isUsedQuotaset() {
        return usedQuotaset;
    }

    /**
     * @return the writeDatabaseset
     */
    public boolean isWriteDatabaseset() {
        return writeDatabaseset;
    }

    /**
     * Sets a generic user attribute
     */
    public void setUserAttribute(String namespace, String name, String value) {
        getNamespace(namespace).put(name, value);
        userAttribtuesset = true;
    }

    /**
     * Read a generic user attribute
     */
    public String getUserAttribute(String namespace, String name) {
        return getNamespace(namespace).get(name);
    }

    public Map<String, Map<String, String>> getUserAttributes() {
        if(userAttributes == null) {
            userAttributes = new HashMap<String, Map<String, String>>();
        }
        return userAttributes;
    }

    public void setUserAttributes(Map<String, Map<String, String>> userAttributes) {
        this.userAttribtuesset = true;
        this.userAttributes = userAttributes;
    }

    public Map<String, String> getNamespace(String namespace) {
        if(userAttributes == null) {
            userAttributes = new HashMap<String, Map<String, String>>();
        }
        Map<String, String> ns = userAttributes.get(namespace);
        if(ns == null) {
            ns = new HashMap<String, String>();
            userAttributes.put(namespace, ns);
        }
        return ns;
    }

    /**
     * Used to check if the user attributes have been modified
     */
    public boolean isUserAttributesset() {
        return userAttribtuesset;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((average_size == null) ? 0 : average_size.hashCode());
        result = prime * result + (average_sizeset ? 1231 : 1237);
        result = prime * result + ((enabled == null) ? 0 : enabled.hashCode());
        result = prime * result + (enabledset ? 1231 : 1237);
        result = prime * result + ((filestore_id == null) ? 0 : filestore_id.hashCode());
        result = prime * result + (filestore_idset ? 1231 : 1237);
        result = prime * result + ((filestore_name == null) ? 0 : filestore_name.hashCode());
        result = prime * result + (filestore_nameset ? 1231 : 1237);
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + (idset ? 1231 : 1237);
        result = prime * result + ((login_mappings == null) ? 0 : login_mappings.hashCode());
        result = prime * result + ((maintenanceReason == null) ? 0 : maintenanceReason.hashCode());
        result = prime * result + (maintenanceReasonset ? 1231 : 1237);
        result = prime * result + ((maxQuota == null) ? 0 : maxQuota.hashCode());
        result = prime * result + (maxQuotaset ? 1231 : 1237);
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + (nameset ? 1231 : 1237);
        result = prime * result + ((readDatabase == null) ? 0 : readDatabase.hashCode());
        result = prime * result + (readDatabaseset ? 1231 : 1237);
        result = prime * result + ((usedQuota == null) ? 0 : usedQuota.hashCode());
        result = prime * result + (usedQuotaset ? 1231 : 1237);
        result = prime * result + ((writeDatabase == null) ? 0 : writeDatabase.hashCode());
        result = prime * result + (writeDatabaseset ? 1231 : 1237);
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (!(obj instanceof Context)) {
            return false;
        }
        final Context other = (Context) obj;
        if (average_size == null) {
            if (other.average_size != null) {
                return false;
            }
        } else if (!average_size.equals(other.average_size)) {
            return false;
        }
        if (average_sizeset != other.average_sizeset) {
            return false;
        }
        if (enabled == null) {
            if (other.enabled != null) {
                return false;
            }
        } else if (!enabled.equals(other.enabled)) {
            return false;
        }
        if (enabledset != other.enabledset) {
            return false;
        }
        if (filestore_id == null) {
            if (other.filestore_id != null) {
                return false;
            }
        } else if (!filestore_id.equals(other.filestore_id)) {
            return false;
        }
        if (filestore_idset != other.filestore_idset) {
            return false;
        }
        if (filestore_name == null) {
            if (other.filestore_name != null) {
                return false;
            }
        } else if (!filestore_name.equals(other.filestore_name)) {
            return false;
        }
        if (filestore_nameset != other.filestore_nameset) {
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
        if (login_mappings == null) {
            if (other.login_mappings != null) {
                return false;
            }
        } else if (!login_mappings.equals(other.login_mappings)) {
            return false;
        }
        if (maintenanceReason == null) {
            if (other.maintenanceReason != null) {
                return false;
            }
        } else if (!maintenanceReason.equals(other.maintenanceReason)) {
            return false;
        }
        if (maintenanceReasonset != other.maintenanceReasonset) {
            return false;
        }
        if (maxQuota == null) {
            if (other.maxQuota != null) {
                return false;
            }
        } else if (!maxQuota.equals(other.maxQuota)) {
            return false;
        }
        if (maxQuotaset != other.maxQuotaset) {
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
        if (readDatabase == null) {
            if (other.readDatabase != null) {
                return false;
            }
        } else if (!readDatabase.equals(other.readDatabase)) {
            return false;
        }
        if (readDatabaseset != other.readDatabaseset) {
            return false;
        }
        if (usedQuota == null) {
            if (other.usedQuota != null) {
                return false;
            }
        } else if (!usedQuota.equals(other.usedQuota)) {
            return false;
        }
        if (usedQuotaset != other.usedQuotaset) {
            return false;
        }
        if (writeDatabase == null) {
            if (other.writeDatabase != null) {
                return false;
            }
        } else if (!writeDatabase.equals(other.writeDatabase)) {
            return false;
        }
        if (writeDatabaseset != other.writeDatabaseset) {
            return false;
        }
        return true;
    }

    public Boolean getEnabled() {
        return enabled;
    }


    /**
     * This settings are only used internally, don't manipulate the setting here or rely on this methods existence
     *
     * @return
     */
    public final boolean isListrun() {
        return listrun;
    }


    /**
     * This settings are only used internally, don't manipulate the setting here or rely on this methods existence
     *
     * @param listrun
     */
    public final void setListrun(boolean listrun) {
        this.listrun = listrun;
    }
}
