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

package com.openexchange.filestore;


/**
 * {@link StorageInfo} - Information for a file storage.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public class StorageInfo {

    private final int id;
    private final long quota;
    private final String name;
    private final int owner;

    /**
     * Initializes a new {@link StorageInfo}.
     *
     * @param id The file storage identifier
     * @param owner The owner of the file storage
     * @param name The entity-specific location inside the file storage
     * @param quota The file storage quota
     */
    public StorageInfo(int id, int owner, String name, long quota) {
        super();
        this.id = id;
        this.owner = owner;
        this.name = name;
        this.quota = quota;
    }

    /**
     * Gets the file storage quota
     *
     * @return The quota for the file storage or <code>0</code> if there is no quota.
     */
    public long getQuota() {
        return quota;
    }

    /**
     * Gets the file storage identifier
     *
     * @return The file storage identifier
     */
    public int getId() {
        return id;
    }

    /**
     * Gets the entity-specific location inside the file storage.
     *
     * @return The entity-specific location inside the file storage.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the owner of the file storage.
     * <p>
     * The owner determines to what 'filestore_usage' entry the quota gets accounted.
     *
     * @return The owner
     */
    public int getOwner() {
        return owner;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("StorageInfo [id=").append(id).append(", quota=").append(quota).append(", ");
        if (name != null) {
            builder.append("name=").append(name).append(", ");
        }
        builder.append("owner=").append(owner).append("]");
        return builder.toString();
    }

}
