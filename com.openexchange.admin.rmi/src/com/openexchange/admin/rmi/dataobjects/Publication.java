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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

import com.openexchange.admin.rmi.dataobjects.Context;

/**
 * {@link Publication}
 *
 * @author <a href="mailto:felix.marx@open-xchange.com">Felix Marx</a>
 */
public class Publication extends ExtendableDataObject implements NameAndIdObject, java.io.Serializable {

    /**
     * For serialization
     */
    private static final long serialVersionUID = -1272376727507395566L;
    
    private int userId;
    
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

    private Boolean enabled = null;

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(final String entityId) {
        if (null == entityId){
            this.entityIdSet = true;
        }
        this.entityId = entityId;
    }

    public String getModule() {
        return module;
    }

    public void setModule(final String module) {
        if (null == module){
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





}
