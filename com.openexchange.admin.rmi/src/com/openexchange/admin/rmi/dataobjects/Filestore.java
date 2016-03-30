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

import java.io.Serializable;

/**
 * This class represents a filestore.
 *
 * @author <a href="mailto:manuel.kraft@open-xchange.com">Manuel Kraft</a>
 * @author <a href="mailto:carsten.hoeger@open-xchange.com">Carsten Hoeger</a>
 * @author <a href="mailto:dennis.sieben@open-xchange.com">Dennis Sieben</a>
 */
public class Filestore implements Serializable {

    private static final long serialVersionUID = -6970026864761440793L;

    private Integer id;

    private boolean idset;

    private String url;

    private boolean urlset;

    /**
     * Size in mega bytes (MB).
     */
    private Long size;

    private boolean sizeset;

    /**
     * Used space in mega bytes (MB)
     */
    private Long used;

    private boolean usedset;

    /**
     * Reserved space in mega bytes (MB)
     */
    private Long reserved;

    private boolean reservedset;

    private Integer maxContexts;

    private boolean maxContextsset;

    private Integer currentContexts;

    private boolean currentContextsset;

    /**
     * Initiates an empty filestore object
     */
    public Filestore() {
        super();
    }

    /**
     * Initiates a filestore object with given id set
     *
     * @param id An {@link Integer} containing the id
     */
    public Filestore(Integer id) {
        super();
        this.id = id;
    }

    /**
     * Returns the id of this filestore object
     *
     * @return An {@link Integer} containing the id
     */
    public Integer getId() {
        return this.id;
    }

    /**
     * Sets the id for this filestore object
     *
     * @param id An {@link Integer} containing the id
     */
    public void setId(final Integer id) {
        this.id = id;
        this.idset = true;
    }

    /**
     * Returns the url of this filestore object
     *
     * @return A {@link String} containing the url
     */
    public String getUrl() {
        return this.url;
    }

    /**
     * Sets the url for this filestore object
     *
     * @param url A {@link String} containing the url
     */
    public void setUrl(final String url) {
        this.url = url;
        this.urlset = true;
    }

    /**
     * Returns the size of this filestore object (in MB)
     *
     * @return A {@link Long} containing the size (in MB)
     */
    public Long getSize() {
        return this.size;
    }

    /**
     * @param Size (in MB)
     */
    /**
     * Sets the size for this filestore object (in MB)
     *
     * @param size A {@link Long} containing the size (in MB)
     */
    public void setSize(final Long size) {
        this.size = size;
        this.sizeset = true;
    }

    /**
     * Returns the maximum amount of contexts/users of this filestore object
     *
     * @return An {@link Integer} containing the maximum number of contexts/users
     */
    public Integer getMaxContexts() {
        return this.maxContexts;
    }

    /**
     * Sets the maximum amount of contexts/users for this filestore object
     *
     * @param maxContexts The maximum number of contexts/users
     */
    public void setMaxContexts(final Integer maxContexts) {
        this.maxContexts = maxContexts;
        this.maxContextsset = true;
    }

    /**
     * Returns the current amount of contexts of this filestore object
     *
     * @return An {@link Integer} containing the current
     */
    public Integer getCurrentContexts() {
        return this.currentContexts;
    }

    /**
     * Sets the current amount of contexts for this filestore object
     *
     * @param currentContexts An {@link Integer} containing the current amount of contexts
     */
    public void setCurrentContexts(final Integer currentContexts) {
        this.currentContexts = currentContexts;
        this.currentContextsset = true;
    }

    /**
     * Returns the currently used size of this filestore object (in MB)
     *
     * @return A {@link Long} containing the currently used size (in MB)
     */
    public Long getUsed() {
        return this.used;
    }

    /**
     * Sets the currently used size for this filestore object (in MB)
     *
     * @param quota_used A {@link Long} containing the currently used size
     */
    public void setUsed(final Long quota_used) {
        this.used = quota_used;
        this.usedset = true;
    }

    /**
     * Constructs a <code>String</code> with all attributes in name = value
     * format.
     *
     * @return a <code>String</code> representation of this object.
     */
    @Override
    public String toString() {
        final StringBuilder retValue = new StringBuilder();
        retValue.append("Filestore (id=");
        retValue.append(id);
        retValue.append(" url=");
        retValue.append(url);
        retValue.append(" size=");
        retValue.append(size);
        retValue.append(" used=");
        retValue.append(used);
        retValue.append(" reserved=");
        retValue.append(reserved);
        retValue.append(" maxCtx=");
        retValue.append(maxContexts);
        retValue.append(" curCtx=");
        retValue.append(currentContexts);
        retValue.append(")");
        return retValue.toString();
    }

    /**
     * Gets the reserved file store space in mega bytes (MB). <b>Applies only to context-associated file storages!</b>
     * <p>
     * &lt;average-filestore-space&gt; * &lt;number-of-filestore-contexts&gt;
     *
     * @return The reserved space in MB
     */
    public final Long getReserved() {
        return reserved;
    }

    /**
     * Sets the reserved file store space in mega bytes (MB). <b>Applies only to context-associated file storages!</b>
     * <p>
     * &lt;average-filestore-space&gt; * &lt;number-of-filestore-contexts&gt;
     *
     * @param reserved the reserved space in MB to set
     */
    public final void setReserved(Long reserved) {
        this.reserved = reserved;
        reservedset = true;
    }

    /**
     * @return the currentContextsset
     */
    public boolean isCurrentContextsset() {
        return currentContextsset;
    }

    /**
     * @return the idset
     */
    public boolean isIdset() {
        return idset;
    }

    /**
     * @return the maxContextsset
     */
    public boolean isMaxContextsset() {
        return maxContextsset;
    }

    /**
     * @return the reservedset
     */
    public boolean isReservedset() {
        return reservedset;
    }

    /**
     * @return the sizeset
     */
    public boolean isSizeset() {
        return sizeset;
    }

    /**
     * @return the urlset
     */
    public boolean isUrlset() {
        return urlset;
    }

    /**
     * @return the usedset
     */
    public boolean isUsedset() {
        return usedset;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((currentContexts == null) ? 0 : currentContexts.hashCode());
        result = prime * result + (currentContextsset ? 1231 : 1237);
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + (idset ? 1231 : 1237);
        result = prime * result + ((maxContexts == null) ? 0 : maxContexts.hashCode());
        result = prime * result + (maxContextsset ? 1231 : 1237);
        result = prime * result + ((reserved == null) ? 0 : reserved.hashCode());
        result = prime * result + (reservedset ? 1231 : 1237);
        result = prime * result + ((size == null) ? 0 : size.hashCode());
        result = prime * result + (sizeset ? 1231 : 1237);
        result = prime * result + ((url == null) ? 0 : url.hashCode());
        result = prime * result + (urlset ? 1231 : 1237);
        result = prime * result + ((used == null) ? 0 : used.hashCode());
        result = prime * result + (usedset ? 1231 : 1237);
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
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Filestore)) {
            return false;
        }
        final Filestore other = (Filestore) obj;
        if (currentContexts == null) {
            if (other.currentContexts != null) {
                return false;
            }
        } else if (!currentContexts.equals(other.currentContexts)) {
            return false;
        }
        if (currentContextsset != other.currentContextsset) {
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
        if (maxContexts == null) {
            if (other.maxContexts != null) {
                return false;
            }
        } else if (!maxContexts.equals(other.maxContexts)) {
            return false;
        }
        if (maxContextsset != other.maxContextsset) {
            return false;
        }
        if (reserved == null) {
            if (other.reserved != null) {
                return false;
            }
        } else if (!reserved.equals(other.reserved)) {
            return false;
        }
        if (reservedset != other.reservedset) {
            return false;
        }
        if (size == null) {
            if (other.size != null) {
                return false;
            }
        } else if (!size.equals(other.size)) {
            return false;
        }
        if (sizeset != other.sizeset) {
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
        if (used == null) {
            if (other.used != null) {
                return false;
            }
        } else if (!used.equals(other.used)) {
            return false;
        }
        if (usedset != other.usedset) {
            return false;
        }
        return true;
    }
}
