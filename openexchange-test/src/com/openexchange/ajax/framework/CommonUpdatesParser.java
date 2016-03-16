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

package com.openexchange.ajax.framework;

import java.util.HashSet;
import java.util.Set;
import org.json.JSONException;
import com.openexchange.ajax.container.Response;
import com.openexchange.groupware.container.DataObject;
import com.openexchange.groupware.container.FolderObject;

/**
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public class CommonUpdatesParser<T extends CommonUpdatesResponse> extends AbstractColumnsParser<T> {

    protected CommonUpdatesParser(final boolean failOnError, final int[] columns) {
        super(failOnError, columns);
    }

    /**
     * This method must be overwritten if some more detailed response class should be used instead of the common updates response class.
     * @param response the general response object containing methods and data for handling the general JSON response object.
     * @return a detailed response object corresponding to the request and NEVER <code>null</code>.
     */
    @SuppressWarnings("unchecked")
    @Override
    protected T instantiateResponse(final Response response) {
        // I don't quite get this.
        return (T) new CommonUpdatesResponse(response);
    }

    @Override
    protected T createResponse(final Response response) throws JSONException {
        T updateResponse = super.createResponse(response);
        initUpdatedIds(updateResponse);
        return updateResponse;
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
    protected void initUpdatedIds(T updatesResponse) {
        Object[][] responseData = updatesResponse.getArray();
        int idPosition = updatesResponse.getColumnPos(DataObject.OBJECT_ID);
        Set<Integer> newOrModifiedIds = new HashSet<Integer>(responseData.length);
        Set<Integer> deletedIds = new HashSet<Integer>(responseData.length);
        if (idPosition > -1) {
            for (Object[] objectArray : responseData) {
                if (objectArray.length == 1) {
                    deletedIds.add(getIdFromObject(objectArray[0]));
                } else {
                    newOrModifiedIds.add(getIdFromObject(objectArray[idPosition]));
                }
            }
        }
        updatesResponse.setNewOrModifiedIds(newOrModifiedIds);
        updatesResponse.setDeletedIds(deletedIds);
    }

    private int getIdFromObject(Object object) {
        int id = -1;
        if (object instanceof String) {
            String s = (String) object;
            if (s.startsWith(FolderObject.SHARED_PREFIX)) {
                id = Integer.parseInt(s.substring(FolderObject.SHARED_PREFIX.length()));
            } else {
                id = Integer.parseInt((String) object);
            }
        } else {
            id = ((Integer) object).intValue();
        }
        return id;
    }

}
