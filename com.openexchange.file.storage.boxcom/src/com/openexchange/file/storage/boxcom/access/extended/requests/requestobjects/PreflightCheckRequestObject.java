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

package com.openexchange.file.storage.boxcom.access.extended.requests.requestobjects;

import com.box.boxjavalibv2.dao.BoxFile;
import com.box.boxjavalibv2.dao.BoxFolder;
import com.box.boxjavalibv2.dao.BoxItem;
import com.box.boxjavalibv2.jsonentities.MapJSONStringEntity;
import com.box.restclientv2.requestsbase.BoxDefaultRequestObject;

/**
 * {@link PreflightCheckRequestObject}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class PreflightCheckRequestObject extends BoxDefaultRequestObject {

    /**
     * Initializes a new {@link PreflightCheckRequestObject}.
     */
    public PreflightCheckRequestObject(String name, String parentId, long size) {
        super();
        setName(name);
        setParent(parentId);
        setSize(size);
    }

    /**
     * Set the name of the file
     * 
     * @param name the name of the file
     * @return
     */
    private PreflightCheckRequestObject setName(String name) {
        put(BoxFile.FIELD_NAME, name);
        return this;
    }

    /**
     * Set the parent folder of the file.
     * 
     * @param parentId the identifier of the parent folder
     * @return
     */
    private PreflightCheckRequestObject setParent(String parentId) {
        MapJSONStringEntity entity = new MapJSONStringEntity();
        entity.put(BoxFolder.FIELD_ID, parentId);
        put(BoxItem.FIELD_PARENT, entity);
        return this;
    }

    /**
     * Set the size of the file in bytes
     * 
     * @param size the size of the file in bytes
     * @return
     */
    private PreflightCheckRequestObject setSize(long size) {
        put(BoxFile.FIELD_SIZE, size);
        return this;
    }
}
