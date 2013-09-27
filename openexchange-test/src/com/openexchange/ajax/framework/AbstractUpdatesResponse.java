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

package com.openexchange.ajax.framework;

import java.util.HashSet;
import java.util.Set;
import com.openexchange.ajax.container.Response;
import com.openexchange.groupware.container.DataObject;


/**
 * {@link AbstractUpdatesResponse} - 
 *
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public class AbstractUpdatesResponse extends AbstractColumnsResponse {

    private Set<Integer> newOrModifiedIds;
    private Set<Integer> deletedIds;

    /**
     * Initializes a new {@link AbstractUpdatesResponse}.
     * @param response
     */
    protected AbstractUpdatesResponse(Response response) {
        super(response);
    }

    @Override
    void setArray(final Object[][] array) {
        super.setArray(array);
        initUpdatedIds(array);
    }
    
    /**
     * Get a collection of object ids that were modified(new or updated) during the request. 
     * @return a collection of object ids that were modified during the request.
     */
    public Set<Integer> getNewOrModifiedIds() {
        return newOrModifiedIds;
    }

    /**
     * Get a collection of object ids that were deleted during the request. 
     * @return a collection of object ids that were deleted during the request.
     */
    public Set<Integer> getDeletedIds() {
        return deletedIds;
    }

    /*
     * Deleted Objects are represented as String ids on the toplevel of the response array
     * 
     * New or modified Objects are represented as array on the toplevel of the response array
     * [
     *  31279,
     *  35,
     *  "UpdatedTask 4",
     *  null,
     *  null,
     *  null,
     *  null,
     *  [
     *    {
     *      "type": 1,
     *      "confirmation": 0,
     *      "id": 5
     *    }
     *  ]
     * ]
     */
    private void initUpdatedIds(Object[][] responseData) {
        newOrModifiedIds = new HashSet<Integer>(responseData.length);
        deletedIds = new HashSet<Integer>(responseData.length);

        int idPosition = getColumnPos(DataObject.OBJECT_ID);

        for(Object[] objectArray : responseData) {
            if(objectArray.length == 1) {
                Integer objectId = (Integer)objectArray[0];
                deletedIds.add(objectId);
            } else {
                Integer objectId = (Integer) objectArray[idPosition];
                newOrModifiedIds.add(objectId);
            }
        }
    }

}
