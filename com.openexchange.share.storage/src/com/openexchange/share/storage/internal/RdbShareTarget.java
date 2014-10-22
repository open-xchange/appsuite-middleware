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

package com.openexchange.share.storage.internal;

import java.util.Date;
import com.openexchange.share.ShareTarget;

/**
 * {@link RdbShareTarget}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class RdbShareTarget extends ShareTarget {

    private static final long serialVersionUID = 6291061207433984824L;

    private int contextID;
    private byte[] uuid;
    private String token;

    /**
     * Initializes a new {@link RdbShareTarget}.
     *
     * @param target The target to copy the values from
     */
    public RdbShareTarget(ShareTarget target) {
        super(target.getModule(), target.getFolder(), target.getItem());
        this.expiryDate = target.getExpiryDate();
    }

    /**
     * Initializes a new {@link RdbShareTarget}.
     */
    public RdbShareTarget() {
        super();
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
     * Sets the expiryDate
     *
     * @param expiryDate The expiryDate to set
     */
    @Override
    public void setExpiryDate(Date expiryDate) {
        this.expiryDate = expiryDate;
    }

    /**
     * Gets the contextID
     *
     * @return The contextID
     */
    public int getContextID() {
        return contextID;
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
     * Gets the uuid
     *
     * @return The uuid
     */
    public byte[] getUuid() {
        return uuid;
    }

    /**
     * Sets the uuid
     *
     * @param uuid The uuid to set
     */
    public void setUuid(byte[] uuid) {
        this.uuid = uuid;
    }

    /**
     * Gets the token
     *
     * @return The token
     */
    public String getToken() {
        return token;
    }

    /**
     * Sets the token
     *
     * @param token The token to set
     */
    public void setToken(String token) {
        this.token = token;
    }

}

