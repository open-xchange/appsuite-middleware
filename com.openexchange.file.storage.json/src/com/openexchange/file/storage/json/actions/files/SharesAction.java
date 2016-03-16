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

package com.openexchange.file.storage.json.actions.files;

import java.util.ArrayList;
import java.util.List;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.documentation.RequestMethod;
import com.openexchange.documentation.annotations.Action;
import com.openexchange.documentation.annotations.Parameter;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.File.Field;
import com.openexchange.file.storage.FileStorageFileAccess.SortDirection;
import com.openexchange.file.storage.composition.IDBasedFileAccess;
import com.openexchange.tools.iterator.SearchIterator;

/**
 * {@link SharesAction}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
@Action(method = RequestMethod.GET, name = "shares", description = "Get documents of the user shared to other entities", parameters = {
    @Parameter(name = "session", description = "A session ID previously obtained from the login module."),
    @Parameter(name = "columns", description = "The requested fields as per tables Common object data and Detailed infoitem data."),
    @Parameter(name = "sort", optional = true, description = "The identifier of a column which determines the sort order of the response. If this parameter is specified, then the parameter order must be also specified."),
    @Parameter(name = "order", optional = true, description = "\"asc\" if the response entires should be sorted in the ascending order, \"desc\" if the response entries should be sorted in the descending order. If this parameter is specified, then the parameter sort must be also specified."),
}, responseDescription = "Response: An array with infoitem data. Each array element describes one infoitem and is itself an array. The elements of each array contain the information specified by the corresponding identifiers in the columns parameter.")
public class SharesAction extends AbstractListingAction {

    @Override
    public AJAXRequestResult handle(InfostoreRequest request) throws OXException {
        IDBasedFileAccess fileAccess = request.getFileAccess();
        Field sortingField = request.getSortingField();
        SortDirection sortDirection = request.getSortingOrder();

        List<Field> columns = request.getFieldsToLoad();
        boolean copy = false;
        if(!columns.contains(File.Field.FOLDER_ID)) {
            columns = new ArrayList<File.Field>(columns);
            columns.add(File.Field.FOLDER_ID);
            copy = true;
        }
        if(!columns.contains(File.Field.ID)) {
            if(!copy) {
                columns = new ArrayList<File.Field>(columns);
                copy = true;
            }
            columns.add(File.Field.ID);
        }

        SearchIterator<File> searchIterator = fileAccess.getUserSharedDocuments(columns, sortingField, sortDirection);
        if (Field.CREATED_BY.equals(sortingField)) {
            searchIterator = CreatedByComparator.resort(request.getSession(), searchIterator, sortDirection);
        }
        return results(searchIterator, request);
    }

}
