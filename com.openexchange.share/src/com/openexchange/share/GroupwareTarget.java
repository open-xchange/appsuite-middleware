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


/**
 * {@link GroupwareTarget}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.0
 */
public class GroupwareTarget implements Serializable {

    protected int module;
    protected String folder;
    protected String item;

    /**
     * Initializes a new, empty {@link GroupwareTarget}.
     */
    public GroupwareTarget() {
        super();
    }

    /**
     * Initializes a new {@link GroupwareTarget}, pointing to a folder of a specific groupware module.
     *
     * @param module The groupware module of the share's target folder
     * @param folder The identifier of the share's folder
     */
    public GroupwareTarget(int module, String folder) {
        this(module, folder, null);
    }

    /**
     * Initializes a new {@link GroupwareTarget}, pointing to an item located in a parent folder of a specific groupware module.
     *
     * @param module The groupware module of the share's target folder
     * @param folder The identifier of the share's folder
     * @param item The identifier of the share's item
     */
    public GroupwareTarget(int module, String folder, String item) {
        super();
        this.module = module;
        this.folder = folder;
        this.item = item;
    }

    /**
     * Gets the groupware module of the share's target folder.
     *
     * @return The module
     */
    public int getModule() {
        return module;
    }

    /**
     * Gets the identifier of the share's folder.
     *
     * @return The folder ID
     */
    public String getFolder() {
        return folder;
    }

    /**
     * Gets the identifier of the share's item in case the share is not a folder share.
     *
     * @return The item ID, or <code>null</code> if the share references a folder
     */
    public String getItem() {
        return item;
    }

    /**
     * Gets a value indicating whether the share points to a folder or a single item.
     *
     * @return <code>true</code> if the share points to a folder, <code>false</code>, otherwise
     */
    public boolean isFolder() {
        return null == item;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((folder == null) ? 0 : folder.hashCode());
        result = prime * result + ((item == null) ? 0 : item.hashCode());
        result = prime * result + module;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof GroupwareTarget)) {
            return false;
        }
        GroupwareTarget other = (GroupwareTarget) obj;
        if (folder == null) {
            if (other.folder != null) {
                return false;
            }
        } else if (!folder.equals(other.folder)) {
            return false;
        }
        if (item == null) {
            if (other.item != null) {
                return false;
            }
        } else if (!item.equals(other.item)) {
            return false;
        }
        if (module != other.module) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "GroupwareTarget [module=" + module + ", folder=" + folder + ", item=" + item + "]";
    }

}
