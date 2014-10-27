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
 *     Copyright (C) 2004-2013 Open-Xchange, Inc.
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

package com.openexchange.share.storage.mapping;

import java.util.Date;
import java.util.Map;
import com.openexchange.share.Share;
import com.openexchange.share.ShareTarget;

/**
 * {@link RdbShare}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class RdbShare {

    private int cid;
    private int guest;
    private int module;
    private String folder;
    private String item;
    private int owner;
    private Date expires;
    private Date created;
    private int createdBy;
    private Date modified;
    private int modifiedBy;
    private Map<String, Object> meta;

    /**
     * Initializes a new {@link RdbShare}.
     */
    public RdbShare() {
        super();
    }

    /**
     * Initializes a new {@link RdbShare}, inheriting the properties from the supplied share.
     *
     * @param contextID The context ID
     * @param share The share to inherit the properties from
     */
    public RdbShare(int contextID, Share share) {
        this();
        cid = contextID;
        guest = share.getGuest();
        if (null != share.getTarget()) {
            ShareTarget target = share.getTarget();
            module = target.getModule();
            folder = target.getFolder();
            item = target.getItem();
            owner = target.getOwnedBy();
            expires = target.getExpiryDate();
            meta = target.getMeta();
        }
        created = share.getCreated();
        createdBy = share.getCreatedBy();
        modified = share.getModified();
        modifiedBy = share.getModifiedBy();
    }

    /**
     * Converts this database share to a plain share object, taking over all properties.
     *
     * @return The share
     */
    public Share toShare() {
        Share share = new Share();
        share.setCreated(created);
        share.setCreatedBy(createdBy);
        share.setGuest(guest);
        share.setModified(modified);
        share.setModifiedBy(modifiedBy);
        ShareTarget target = new ShareTarget(module, folder, item);
        target.setExpiryDate(expires);
        target.setOwnedBy(owner);
        target.setMeta(meta);
        share.setTarget(target);
        return share;
    }

    /**
     * Gets the cid
     *
     * @return The cid
     */
    public int getCid() {
        return cid;
    }

    /**
     * Sets the cid
     *
     * @param cid The cid to set
     */
    public void setCid(int cid) {
        this.cid = cid;
    }

    /**
     * Gets the guest
     *
     * @return The guest
     */
    public int getGuest() {
        return guest;
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
     * Gets the module
     *
     * @return The module
     */
    public int getModule() {
        return module;
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
     * Gets the folder
     *
     * @return The folder
     */
    public String getFolder() {
        return folder;
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
     * Gets the item
     *
     * @return The item
     */
    public String getItem() {
        return item;
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
     * Gets the owner
     *
     * @return The owner
     */
    public int getOwner() {
        return owner;
    }

    /**
     * Sets the owner
     *
     * @param owner The owner to set
     */
    public void setOwner(int owner) {
        this.owner = owner;
    }

    /**
     * Gets the expires
     *
     * @return The expires
     */
    public Date getExpires() {
        return expires;
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
     * Gets the created
     *
     * @return The created
     */
    public Date getCreated() {
        return created;
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
     * Gets the createdBy
     *
     * @return The createdBy
     */
    public int getCreatedBy() {
        return createdBy;
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
     * Gets the modified
     *
     * @return The modified
     */
    public Date getModified() {
        return modified;
    }

    /**
     * Sets the modified
     *
     * @param modified The modified to set
     */
    public void setModified(Date modified) {
        this.modified = modified;
    }

    /**
     * Gets the modifiedBy
     *
     * @return The modifiedBy
     */
    public int getModifiedBy() {
        return modifiedBy;
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
     * Gets the meta
     *
     * @return The meta
     */
    public Map<String, Object> getMeta() {
        return meta;
    }

    /**
     * Sets the meta
     *
     * @param meta The meta to set
     */
    public void setMeta(Map<String, Object> meta) {
        this.meta = meta;
    }

}

