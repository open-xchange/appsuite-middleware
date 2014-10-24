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

import java.io.Serializable;
import java.util.Date;

/**
 * {@link Share}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.0
 */
public class Share implements Serializable {

    private static final long serialVersionUID = -8680039849393740702L;

    private int guest;
    private ShareTarget target;
    private Date created;
    private int createdBy;
    private Date modified;
    private int modifiedBy;

    /**
     * Initializes a new, empty {@link Share}.
     */
    public Share() {
        super();
    }

    /**
     * Gets the share target.
     *
     * @return The target
     */
    public ShareTarget getTarget() {
        return target;
    }

    /**
     * Sets the share target
     *
     * @param target The target to set
     */
    public void setTarget(ShareTarget target) {
        this.target = target;
    }

    /**
     * Gets the identifier of the user that shared the target.
     *
     * @return The identifier of the user that shared the target
     */
    public int getCreatedBy() {
        return createdBy;
    }

    /**
     * Sets the identifier of the user that shared the target.
     *
     * @param sharedBy The identifier of the user that shared the target
     */
    public void setCreatedBy(int sharedBy) {
        this.createdBy = sharedBy;
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

}
