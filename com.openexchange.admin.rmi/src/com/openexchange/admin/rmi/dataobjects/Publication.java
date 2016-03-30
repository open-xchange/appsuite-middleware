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

/**
 * {@link Publication}
 *
 * @author <a href="mailto:felix.marx@open-xchange.com">Felix Marx</a>
 */
public class Publication extends ExtendableDataObject implements NameAndIdObject, Comparable<Publication>, java.io.Serializable {

    /**
     * For serialization
     */
    private static final long serialVersionUID = -1272376727507395566L;

    private Integer userId;
    private boolean userIdSet = false;

    private Context context;
    private boolean contextSet = false;

    private Integer id;
    private boolean idSet = false;

    private String entityId;
    private boolean entityIdSet = false;

    private String module;
    private boolean moduleSet = false;

    private String name;
    private boolean nameSet = false;

    private String description;
    private boolean descriptionSet = false;

    private String url;

    public String getEntityId() {
        return entityId;
    }

    /**
     * Gets the url
     *
     * @return The url
     */
    public String getUrl() {
        return url;
    }

    /**
     * Sets the url
     *
     * @param url The url to set
     */
    public void setUrl(String url) {
        this.url = url;
    }

    public void setEntityId(final String entityId) {
        if (null == entityId) {
            this.entityIdSet = true;
        }
        this.entityId = entityId;
    }

    public String getModule() {
        return module;
    }

    public void setModule(final String module) {
        if (null == module) {
            this.moduleSet = true;
        }
        this.module = module;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(final Context context) {
        if (null == context) {
            this.contextSet = true;
        }
        this.context = context;
    }

    /**
     * Sets the numeric publication id
     *
     * @param id An {@link Integer} containing the user id
     */
    @Override
    final public void setId(final Integer id) {
        if (null == id) {
            this.idSet = true;
        }
        this.id = id;
    }

    /**
     * Returns the id of the publication
     *
     * @return Returns the id of the user as a long.
     */
    @Override
    final public Integer getId() {
        return id;
    }

    /**
     * Sets the numeric user id
     *
     * @param userid An {@link Integer} containing the user id
     */
    final public void setUserId(final Integer userid) {
        if (null == userid) {
            this.userIdSet = true;
        }
        this.userId = userid;
    }

    /**
     * Returns the id of the publication
     *
     * @return Returns the id of the user as a long.
     */
    final public Integer getUserId() {
        return userId;
    }

    @Override
    final public String getName() {
        return name;
    }

    /**
     * Sets the symbolic publication identifier
     *
     * @param name A {@link String} containing the publication name
     */
    @Override
    final public void setName(final String name) {
        if (null == name) {
            this.nameSet = true;
        }
        this.name = name;
    }

    @Override
    public String[] getMandatoryMembersCreate() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String[] getMandatoryMembersChange() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String[] getMandatoryMembersDelete() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String[] getMandatoryMembersRegister() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * Gets the description
     *
     * @return The description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description
     *
     * @param description The description to set
     */
    public void setDescription(String description) {
        if (null == description) {
            this.descriptionSet = true;
        }
        this.description = description;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((context == null) ? 0 : context.hashCode());
        result = prime * result + (contextSet ? 1231 : 1237);
        result = prime * result + ((description == null) ? 0 : description.hashCode());
        result = prime * result + (descriptionSet ? 1231 : 1237);
        result = prime * result + ((entityId == null) ? 0 : entityId.hashCode());
        result = prime * result + (entityIdSet ? 1231 : 1237);
        result = prime * result + ((module == null) ? 0 : module.hashCode());
        result = prime * result + (moduleSet ? 1231 : 1237);
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + (idSet ? 1231 : 1237);
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + (nameSet ? 1231 : 1237);
        result = prime * result + ((userId == null) ? 0 : userId.hashCode());
        result = prime * result + (userIdSet ? 1231 : 1237);
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
        if (!(obj instanceof Publication)) {
            return false;
        }
        final Publication other = (Publication) obj;
        if (context == null) {
            if (other.context != null) {
                return false;
            }
        } else if (!context.equals(other.context)) {
            return false;
        }
        if (contextSet != other.contextSet) {
            return false;
        }
        if (description == null) {
            if (other.description != null) {
                return false;
            }
        } else if (!description.equals(other.description)) {
            return false;
        }
        if (descriptionSet != other.descriptionSet) {
            return false;
        }
        if (entityId == null) {
            if (other.entityId != null) {
                return false;
            }
        } else if (!entityId.equals(other.entityId)) {
            return false;
        }
        if (entityIdSet != other.entityIdSet) {
            return false;
        }
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        if (idSet != other.idSet) {
            return false;
        }
        if (module == null) {
            if (other.module != null) {
                return false;
            }
        } else if (!module.equals(other.module)) {
            return false;
        }
        if (moduleSet != other.moduleSet) {
            return false;
        }
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (nameSet != other.nameSet) {
            return false;
        }
        if (userId == null) {
            if (other.userId != null) {
                return false;
            }
        } else if (!userId.equals(other.userId)) {
            return false;
        }
        if (userIdSet != other.userIdSet) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(256);
        builder.append("Publication [");
        if (userId != null) {
            builder.append("userId=").append(userId).append(", ");
        }
        if (context != null) {
            builder.append("context=").append(context).append(", ");
        }
        if (id != null) {
            builder.append("id=").append(id).append(", ");
        }
        if (entityId != null) {
            builder.append("entityId=").append(entityId).append(", ");
        }
        if (module != null) {
            builder.append("module=").append(module).append(", ");
        }
        if (name != null) {
            builder.append("name=").append(name).append(", ");
        }
        if (description != null) {
            builder.append("description=").append(description).append(", ");
        }
        if (url != null) {
            builder.append("url=").append(url);
        }
        builder.append("]");
        return builder.toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(Publication o) {
        if (o.getUserId() > getUserId()) {
            return -1;
        } else if (o.getUserId() < getUserId()) {
            return 1;
        } else {
            return 0;
        }
    }

}
