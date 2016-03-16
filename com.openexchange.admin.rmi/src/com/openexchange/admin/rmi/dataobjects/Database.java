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

/**
 * This class represents a database.
 *
 * @author <a href="mailto:manuel.kraft@open-xchange.com">Manuel Kraft</a>
 * @author <a href="mailto:carsten.hoeger@open-xchange.com">Carsten Hoeger</a>
 * @author <a href="mailto:dennis.sieben@open-xchange.com">Dennis Sieben</a>
 *
 */
public class Database extends EnforceableDataObject implements NameAndIdObject {
    /**
     * For serialization
     */
    private static final long serialVersionUID = -3068828009821317094L;

    private Integer id;

    private boolean idset;

    private Integer read_id;

    private boolean read_idset;

    private String url;

    private boolean urlset;

    private String login;

    private boolean loginset;

    private String password;

    private boolean passwordset;

    private String name;

    private boolean nameset;

    private String driver;

    private boolean driverset;

    private String scheme;

    private boolean schemeset;

    private Integer clusterWeight;

    private boolean clusterWeightset;

    private Integer maxUnits;

    private boolean maxUnitsset;

    private Integer poolHardLimit;

    private boolean poolHardLimitset;

    private Integer poolInitial;

    private boolean poolInitialset;

    private Integer poolMax;

    private boolean poolMaxset;

    private Integer masterId;

    private boolean masterIdset;

    private Integer currentUnits;

    private boolean currentUnitsset;

    private Boolean master;

    private boolean masterset;

    /**
     * @param id
     */
    public Database(final int id) {
        super();
        init();
        this.id = Integer.valueOf(id);
    }

    /**
     * @param id
     * @param schema
     */
    public Database(final int id, final String schema) {
        super();
        init();
        this.id = Integer.valueOf(id);
        this.scheme = schema;
    }

    public Database() {
        super();
        init();
    }

    // //////////////////////////////////////////////
    // Getter and Setter
    //
    @Override
    public Integer getId() {
        return this.id;
    }

    @Override
    public void setId(final Integer val) {
        this.id = val;
        this.idset = true;
    }

    public String getUrl() {
        return this.url;
    }

    public void setUrl(final String val) {
        this.url = val;
        this.urlset = true;
    }

    public String getLogin() {
        return this.login;
    }

    public void setLogin(final String val) {
        this.login = val;
        this.loginset = true;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(final String val) {
        this.password = val;
        this.passwordset = true;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void setName(final String val) {
        this.name = val;
        this.nameset = true;
    }

    public String getDriver() {
        return this.driver;
    }

    public void setDriver(final String val) {
        this.driver = val;
        this.driverset = true;
    }

    public String getScheme() {
        return this.scheme;
    }

    public void setScheme(final String scheme) {
        this.scheme = scheme;
        this.schemeset = true;
    }

    public Integer getClusterWeight() {
        return this.clusterWeight;
    }

    /**
     * The system weight factor of this database in percent, value is Integer
     * This value defines how contexts will be distributed over multiple
     * databases/db pools.
     */
    public void setClusterWeight(final Integer clusterWeight) {
        this.clusterWeight = clusterWeight;
        this.clusterWeightset = true;
    }

    public Integer getMaxUnits() {
        return this.maxUnits;
    }

    public void setMaxUnits(final Integer maxunits) {
        this.maxUnits = maxunits;
        this.maxUnitsset = true;
    }

    public Integer getPoolInitial() {
        return this.poolInitial;
    }

    public void setPoolInitial(final Integer poolInitial) {
        this.poolInitial = poolInitial;
        this.poolInitialset = true;
    }

    public Integer getPoolMax() {
        return this.poolMax;
    }

    public void setPoolMax(final Integer poolMax) {
        this.poolMax = poolMax;
        this.poolMaxset = true;
    }

    public Integer getMasterId() {
        return this.masterId;
    }

    public void setMasterId(final Integer masterId) {
        this.masterId = masterId;
        this.masterIdset = true;
    }

    public Boolean isMaster() {
        return this.master;
    }

    public void setMaster(final Boolean master) {
        this.master = master;
        this.masterset = true;
    }

    public Integer getRead_id() {
        return this.read_id;
    }

    public void setRead_id(final Integer read_id) {
        this.read_id = read_id;
        this.read_idset = true;
    }

    public Integer getCurrentUnits() {
        return this.currentUnits;
    }

    public void setCurrentUnits(final Integer units) {
        this.currentUnits = units;
        this.currentUnitsset = true;
    }

    public Integer getPoolHardLimit() {
        return this.poolHardLimit;
    }

    public void setPoolHardLimit(final Integer poolHardLimit) {
        this.poolHardLimit = poolHardLimit;
        this.poolHardLimitset = true;
    }

    @Override
    public String toString() {
        final StringBuilder ret = new StringBuilder();
        ret.append("[ \n");
        for (final Field f : this.getClass().getDeclaredFields()) {
            try {
                final Object ob = f.get(this);
                final String tname = f.getName();
                if (ob != null && !tname.equals("serialVersionUID")) {
                    ret.append("  ");
                    ret.append(tname);
                    ret.append(": ");
                    ret.append(ob);
                    ret.append("\n");
                }
            } catch (final IllegalArgumentException e) {
                ret.append("IllegalArgument\n");
            } catch (final IllegalAccessException e) {
                ret.append("IllegalAccessException\n");
            }
        }
        ret.append(" ]");
        return ret.toString();
    }

    /**
     * Initializes all members to the default values
     */
    private void init() {
        this.id = null;
        this.read_id = null;
        this.clusterWeight = null;
        this.url = null;
        this.login = null;
        this.password = null;
        this.name = null;
        this.driver = null;
        this.scheme = null;
        this.maxUnits = null;
        this.poolHardLimit = null;
        this.poolInitial = null;
        this.poolMax = null;
        this.masterId = null;
        this.master = null;
        this.currentUnits = null;
    }

    /**
     * At the moment no fields are defined here
     */
    @Override
    public String[] getMandatoryMembersChange() {
        return null;
    }

    /**
     * At the moment {@link #setId}, {@link #setDriver}, {@link #setUrl} and {@link #setScheme} are defined here
     */
    @Override
    public String[] getMandatoryMembersCreate() {
        return new String[] { "id", "driver", "url", "scheme" };
    }

    /**
     * At the moment {@link #setDriver}, {@link #setUrl}, {@link #setScheme}, {@link #setPassword} and {@link #setLogin} are defined here
     */
    @Override
    public String[] getMandatoryMembersDelete() {
        return new String[] { "driver", "url", "scheme", "password", "login" };
    }

    /**
     * At the moment {@link #setPassword}, {@link #setName} and {@link #setMaster} are defined here
     */
    @Override
    public String[] getMandatoryMembersRegister() {
        return new String[] { "password", "name", "master" };
    }

    /**
     * @return the clusterWeightset
     */
    public boolean isClusterWeightset() {
        return clusterWeightset;
    }

    /**
     * @return the currentUnitsset
     */
    public boolean isCurrentUnitsset() {
        return currentUnitsset;
    }

    /**
     * @return the driverset
     */
    public boolean isDriverset() {
        return driverset;
    }

    /**
     * @return the idset
     */
    public boolean isIdset() {
        return idset;
    }

    /**
     * @return the loginset
     */
    public boolean isLoginset() {
        return loginset;
    }

    /**
     * @return the masterIdset
     */
    public boolean isMasterIdset() {
        return masterIdset;
    }

    /**
     * @return the masterset
     */
    public boolean isMasterset() {
        return masterset;
    }

    /**
     * @return the maxUnitsset
     */
    public boolean isMaxUnitsset() {
        return maxUnitsset;
    }

    /**
     * @return the nameset
     */
    public boolean isNameset() {
        return nameset;
    }

    /**
     * @return the passwordset
     */
    public boolean isPasswordset() {
        return passwordset;
    }

    /**
     * @return the poolHardLimitset
     */
    public boolean isPoolHardLimitset() {
        return poolHardLimitset;
    }

    /**
     * @return the poolInitialset
     */
    public boolean isPoolInitialset() {
        return poolInitialset;
    }

    /**
     * @return the poolMaxset
     */
    public boolean isPoolMaxset() {
        return poolMaxset;
    }

    /**
     * @return the read_idset
     */
    public boolean isRead_idset() {
        return read_idset;
    }

    /**
     * @return the schemeset
     */
    public boolean isSchemeset() {
        return schemeset;
    }

    /**
     * @return the urlset
     */
    public boolean isUrlset() {
        return urlset;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((clusterWeight == null) ? 0 : clusterWeight.hashCode());
        result = prime * result + (clusterWeightset ? 1231 : 1237);
        result = prime * result + ((currentUnits == null) ? 0 : currentUnits.hashCode());
        result = prime * result + (currentUnitsset ? 1231 : 1237);
        result = prime * result + ((driver == null) ? 0 : driver.hashCode());
        result = prime * result + (driverset ? 1231 : 1237);
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + (idset ? 1231 : 1237);
        result = prime * result + ((login == null) ? 0 : login.hashCode());
        result = prime * result + (loginset ? 1231 : 1237);
        result = prime * result + ((master == null) ? 0 : master.hashCode());
        result = prime * result + ((masterId == null) ? 0 : masterId.hashCode());
        result = prime * result + (masterIdset ? 1231 : 1237);
        result = prime * result + (masterset ? 1231 : 1237);
        result = prime * result + ((maxUnits == null) ? 0 : maxUnits.hashCode());
        result = prime * result + (maxUnitsset ? 1231 : 1237);
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + (nameset ? 1231 : 1237);
        result = prime * result + ((password == null) ? 0 : password.hashCode());
        result = prime * result + (passwordset ? 1231 : 1237);
        result = prime * result + ((poolHardLimit == null) ? 0 : poolHardLimit.hashCode());
        result = prime * result + (poolHardLimitset ? 1231 : 1237);
        result = prime * result + ((poolInitial == null) ? 0 : poolInitial.hashCode());
        result = prime * result + (poolInitialset ? 1231 : 1237);
        result = prime * result + ((poolMax == null) ? 0 : poolMax.hashCode());
        result = prime * result + (poolMaxset ? 1231 : 1237);
        result = prime * result + ((read_id == null) ? 0 : read_id.hashCode());
        result = prime * result + (read_idset ? 1231 : 1237);
        result = prime * result + ((scheme == null) ? 0 : scheme.hashCode());
        result = prime * result + (schemeset ? 1231 : 1237);
        result = prime * result + ((url == null) ? 0 : url.hashCode());
        result = prime * result + (urlset ? 1231 : 1237);
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
        if (!(obj instanceof Database)) {
            return false;
        }
        final Database other = (Database) obj;
        if (clusterWeight == null) {
            if (other.clusterWeight != null) {
                return false;
            }
        } else if (!clusterWeight.equals(other.clusterWeight)) {
            return false;
        }
        if (clusterWeightset != other.clusterWeightset) {
            return false;
        }
        if (currentUnits == null) {
            if (other.currentUnits != null) {
                return false;
            }
        } else if (!currentUnits.equals(other.currentUnits)) {
            return false;
        }
        if (currentUnitsset != other.currentUnitsset) {
            return false;
        }
        if (driver == null) {
            if (other.driver != null) {
                return false;
            }
        } else if (!driver.equals(other.driver)) {
            return false;
        }
        if (driverset != other.driverset) {
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
        if (login == null) {
            if (other.login != null) {
                return false;
            }
        } else if (!login.equals(other.login)) {
            return false;
        }
        if (loginset != other.loginset) {
            return false;
        }
        if (master == null) {
            if (other.master != null) {
                return false;
            }
        } else if (!master.equals(other.master)) {
            return false;
        }
        if (masterId == null) {
            if (other.masterId != null) {
                return false;
            }
        } else if (!masterId.equals(other.masterId)) {
            return false;
        }
        if (masterIdset != other.masterIdset) {
            return false;
        }
        if (masterset != other.masterset) {
            return false;
        }
        if (maxUnits == null) {
            if (other.maxUnits != null) {
                return false;
            }
        } else if (!maxUnits.equals(other.maxUnits)) {
            return false;
        }
        if (maxUnitsset != other.maxUnitsset) {
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
        if (password == null) {
            if (other.password != null) {
                return false;
            }
        } else if (!password.equals(other.password)) {
            return false;
        }
        if (passwordset != other.passwordset) {
            return false;
        }
        if (poolHardLimit == null) {
            if (other.poolHardLimit != null) {
                return false;
            }
        } else if (!poolHardLimit.equals(other.poolHardLimit)) {
            return false;
        }
        if (poolHardLimitset != other.poolHardLimitset) {
            return false;
        }
        if (poolInitial == null) {
            if (other.poolInitial != null) {
                return false;
            }
        } else if (!poolInitial.equals(other.poolInitial)) {
            return false;
        }
        if (poolInitialset != other.poolInitialset) {
            return false;
        }
        if (poolMax == null) {
            if (other.poolMax != null) {
                return false;
            }
        } else if (!poolMax.equals(other.poolMax)) {
            return false;
        }
        if (poolMaxset != other.poolMaxset) {
            return false;
        }
        if (read_id == null) {
            if (other.read_id != null) {
                return false;
            }
        } else if (!read_id.equals(other.read_id)) {
            return false;
        }
        if (read_idset != other.read_idset) {
            return false;
        }
        if (scheme == null) {
            if (other.scheme != null) {
                return false;
            }
        } else if (!scheme.equals(other.scheme)) {
            return false;
        }
        if (schemeset != other.schemeset) {
            return false;
        }
        if (url == null) {
            if (other.url != null) {
                return false;
            }
        } else if (!url.equals(other.url)) {
            return false;
        }
        if (urlset != other.urlset) {
            return false;
        }
        return true;
    }

    public Boolean getMaster() {
        return master;
    }
}
