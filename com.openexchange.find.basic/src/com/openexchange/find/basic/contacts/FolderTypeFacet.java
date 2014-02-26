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

package com.openexchange.find.basic.contacts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.openexchange.contact.ContactFieldOperand;
import com.openexchange.exception.OXException;
import com.openexchange.find.FindExceptionCode;
import com.openexchange.find.basic.Services;
import com.openexchange.find.common.CommonFacetType;
import com.openexchange.find.common.CommonStrings;
import com.openexchange.find.common.FolderTypeDisplayItem;
import com.openexchange.find.contacts.ContactsFacetType;
import com.openexchange.find.facet.FacetValue;
import com.openexchange.find.facet.Filter;
import com.openexchange.folderstorage.FolderResponse;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.folderstorage.Type;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.folderstorage.database.contentType.ContactContentType;
import com.openexchange.folderstorage.type.PrivateType;
import com.openexchange.folderstorage.type.PublicType;
import com.openexchange.folderstorage.type.SharedType;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.search.CompositeSearchTerm;
import com.openexchange.search.CompositeSearchTerm.CompositeOperation;
import com.openexchange.search.SearchTerm;
import com.openexchange.search.SingleSearchTerm;
import com.openexchange.search.SingleSearchTerm.SingleOperation;
import com.openexchange.search.internal.operands.ConstantOperand;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link FolderTypeFacet}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class FolderTypeFacet extends ContactSearchFacet {

    private static final long serialVersionUID = -9031103652463933032L;

    private static final String ID = "folder_type";

    public FolderTypeFacet() {
        super(ContactsFacetType.FOLDERS, getFacetValues());
    }

    private static List<FacetValue> getFacetValues() {
        List<FacetValue> facetValues = new ArrayList<FacetValue>(3);
        facetValues.add(new FacetValue(FolderTypeDisplayItem.Type.PRIVATE.getIdentifier(), new FolderTypeDisplayItem(
            CommonStrings.FOLDER_TYPE_PRIVATE, FolderTypeDisplayItem.Type.PRIVATE), FacetValue.UNKNOWN_COUNT, new Filter(
            Collections.singletonList(ID), FolderTypeDisplayItem.Type.PRIVATE.getIdentifier())));
        facetValues.add(new FacetValue(FolderTypeDisplayItem.Type.PUBLIC.getIdentifier(), new FolderTypeDisplayItem(
            CommonStrings.FOLDER_TYPE_PUBLIC, FolderTypeDisplayItem.Type.PUBLIC), FacetValue.UNKNOWN_COUNT, new Filter(
                Collections.singletonList(ID), FolderTypeDisplayItem.Type.PUBLIC.getIdentifier())));
        facetValues.add(new FacetValue(FolderTypeDisplayItem.Type.SHARED.getIdentifier(), new FolderTypeDisplayItem(
            CommonStrings.FOLDER_TYPE_SHARED, FolderTypeDisplayItem.Type.SHARED), FacetValue.UNKNOWN_COUNT, new Filter(
                Collections.singletonList(ID), FolderTypeDisplayItem.Type.SHARED.getIdentifier())));
        return facetValues;
    }

    @Override
    public String getID() {
        return ID;
    }

    @Override
    public SearchTerm<?> getSearchTerm(ServerSession session, String query) throws OXException {
        Type type;
        if (FolderTypeDisplayItem.Type.PRIVATE.getIdentifier().equals(query)) {
            type = PrivateType.getInstance();
        } else if (FolderTypeDisplayItem.Type.PUBLIC.getIdentifier().equals(query)) {
            type = PublicType.getInstance();
        } else if (FolderTypeDisplayItem.Type.SHARED.getIdentifier().equals(query)) {
            type = SharedType.getInstance();
        } else {
            throw FindExceptionCode.UNSUPPORTED_FILTER_QUERY.create(query, CommonFacetType.FOLDER_TYPE.getId());
        }
        FolderResponse<UserizedFolder[]> visibleFolders = Services.getFolderService().getVisibleFolders(
            FolderStorage.REAL_TREE_ID, ContactContentType.getInstance(), type, false, session, null);
        UserizedFolder[] folders = visibleFolders.getResponse();
        if (null != folders && 0 < folders.length) {
            if (1 == folders.length) {
                return getFolderIDEqualsTerm(folders[0].getID());
            } else {
                CompositeSearchTerm orTerm = new CompositeSearchTerm(CompositeOperation.OR);
                for (UserizedFolder folder : folders) {
                    orTerm.addSearchTerm(getFolderIDEqualsTerm(folder.getID()));
                }
                return orTerm;
            }
        }
        /*
         * no folders found, no results
         */
        return null;
    }

    private static SingleSearchTerm getFolderIDEqualsTerm(String folderID) {
        SingleSearchTerm searchTerm = new SingleSearchTerm(SingleOperation.EQUALS);
        searchTerm.addOperand(new ContactFieldOperand(ContactField.FOLDER_ID));
        searchTerm.addOperand(new ConstantOperand<String>(folderID));
        return searchTerm;
    }

}
