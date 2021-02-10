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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.groupware;

import java.io.Serializable;
import java.util.Date;

/**
 * {@link LinkEntityInfo}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.5
 */
public class LinkEntityInfo extends EntityInfo implements Serializable, Cloneable {

    private static final long serialVersionUID = 1907071021467621978L;

    private final String shareUrl;
    private final String password;
    private final Date expiryDate;
    private final boolean includeSubfolders;

    /**
     * Initializes a new {@link LinkEntityInfo}.
     * 
     * @param entityInfo The entity info to use for initialization
     * @param shareUrl The URL of the share link
     * @param password The optional password for the share link
     * @param expiryDate The optional expiration date for the share link
     * @param includeSubfolders A value indicating whether subfolders are included in the share or not
     */
    public LinkEntityInfo(EntityInfo entityInfo, String shareUrl, String password, Date expiryDate, boolean includeSubfolders) {
        super(entityInfo);
        this.shareUrl = shareUrl;
        this.password = password;
        this.expiryDate = expiryDate;
        this.includeSubfolders = includeSubfolders;
    }

    /**
     * Gets the URL of the share link.
     * 
     * @return The share URL
     */
    public String getShareUrl() {
        return shareUrl;
    }

    /**
     * Gets the optional password for the share link.
     * 
     * @return The password, or <code>null</code> if not set
     */
    public String getPassword() {
        return password;
    }

    /**
     * Gets the optional expiration date for the share link.
     * 
     * @return The expiration date, or <code>null</code> if not set
     */
    public Date getExpiryDate() {
        return expiryDate;
    }

    /**
     * Gets a value indicating whether subfolders are included in the share or not.
     * 
     * @return <code>true</code> if subfolders are included, <code>false</code> otherwise
     */
    public boolean isIncludeSubfolders() {
        return includeSubfolders;
    }

    @Override
    public Object clone() {
        Date expiryDate = null != this.expiryDate ? new Date(this.expiryDate.getTime()) : null;
        return new LinkEntityInfo(this, shareUrl, password, expiryDate, includeSubfolders);
    }

}
