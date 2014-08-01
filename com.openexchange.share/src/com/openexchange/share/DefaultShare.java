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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.share;

import java.util.Date;
import com.openexchange.groupware.modules.Module;

/**
 * {@link DefaultShare}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.6.1
 */
public class DefaultShare implements Share {

    private String token;
    private int contextID;
    private int module;
    private String folder;
    private String item;
    private Date created;
    private int createdBy;
    private Date lastModified;
    private int modifiedBy;
    private Date expires;
    private int guest;
    private int authentication;

    /**
     * Initializes a new {@link DefaultShare}.
     */
    public DefaultShare() {
        super();
    }

    @Override
    public String getToken() {
        return token;
    }

    @Override
    public int getContextID() {
        return contextID;
    }

    @Override
    public Module getModule() {
        return Module.getForFolderConstant(module);
    }

    @Override
    public String getFolder() {
        return folder;
    }

    @Override
    public boolean isFolder() {
        return item == null;
    }


    @Override
    public String getItem() {
        return item;
    }

    @Override
    public Date getCreated() {
        return created;
    }

    @Override
    public int getCreatedBy() {
        return createdBy;
    }

    @Override
    public Date getLastModified() {
        return lastModified;
    }

    @Override
    public int getModifiedBy() {
        return modifiedBy;
    }

    @Override
    public Date getExpires() {
        return expires;
    }

    @Override
    public boolean isExpired() {
        return expires != null && new Date().after(expires);
    }

    @Override
    public int getGuest() {
        return guest;
    }

    @Override
    public AuthenticationMode getAuthentication() {
        return AuthenticationMode.fromID(authentication);
    }

    /**
     * Sets the token
     *
     * @param token The token to set
     */
    public void setToken(String token) {
        this.token = token;
    }

    /**
     * Sets the contextID
     *
     * @param contextID The contextID to set
     */
    public void setContextID(int contextID) {
        this.contextID = contextID;
    }

    /**
     * Sets the module
     *
     * @param module The module to set
     */
    public void setModule(int module) {
        this.module = module;
    }

    /**
     * Sets the folder
     *
     * @param folder The folder to set
     */
    public void setFolder(String folder) {
        this.folder = folder;
    }

    /**
     * Sets the item
     *
     * @param item The item to set
     */
    public void setItem(String item) {
        this.item = item;
    }

    /**
     * Sets the created
     *
     * @param created The created to set
     */
    public void setCreated(Date created) {
        this.created = created;
    }

    /**
     * Sets the createdBy
     *
     * @param createdBy The createdBy to set
     */
    public void setCreatedBy(int createdBy) {
        this.createdBy = createdBy;
    }

    /**
     * Sets the lastModified
     *
     * @param lastModified The lastModified to set
     */
    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    /**
     * Sets the modifiedBy
     *
     * @param modifiedBy The modifiedBy to set
     */
    public void setModifiedBy(int modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    /**
     * Sets the expires
     *
     * @param expires The expires to set
     */
    public void setExpires(Date expires) {
        this.expires = expires;
    }

    /**
     * Sets the guest
     *
     * @param guest The guest to set
     */
    public void setGuest(int guest) {
        this.guest = guest;
    }

    /**
     * Sets the authentication
     *
     * @param authentication The authentication to set
     */
    public void setAuthentication(int authentication) {
        this.authentication = authentication;
    }

    @Override
    public String toString() {
        return "DefaultShare [token=" + token + ", contextID=" + contextID + ", module=" + module + ", folder=" + folder + ", item=" + item + "]";
    }

}
