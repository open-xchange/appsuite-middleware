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

package com.openexchange.ews.internal;

import java.util.Arrays;
import java.util.List;
import javax.xml.ws.Holder;
import com.microsoft.schemas.exchange.services._2006.messages.CreateItemResponseType;
import com.microsoft.schemas.exchange.services._2006.messages.CreateItemType;
import com.microsoft.schemas.exchange.services._2006.messages.DeleteItemResponseType;
import com.microsoft.schemas.exchange.services._2006.messages.DeleteItemType;
import com.microsoft.schemas.exchange.services._2006.messages.ExchangeServicePortType;
import com.microsoft.schemas.exchange.services._2006.messages.FindItemResponseMessageType;
import com.microsoft.schemas.exchange.services._2006.messages.FindItemResponseType;
import com.microsoft.schemas.exchange.services._2006.messages.FindItemType;
import com.microsoft.schemas.exchange.services._2006.messages.ResponseMessageType;
import com.microsoft.schemas.exchange.services._2006.types.BaseFolderIdType;
import com.microsoft.schemas.exchange.services._2006.types.DefaultShapeNamesType;
import com.microsoft.schemas.exchange.services._2006.types.DisposalType;
import com.microsoft.schemas.exchange.services._2006.types.FolderIdType;
import com.microsoft.schemas.exchange.services._2006.types.ItemIdType;
import com.microsoft.schemas.exchange.services._2006.types.ItemQueryTraversalType;
import com.microsoft.schemas.exchange.services._2006.types.ItemResponseShapeType;
import com.microsoft.schemas.exchange.services._2006.types.ItemType;
import com.microsoft.schemas.exchange.services._2006.types.MessageDispositionType;
import com.microsoft.schemas.exchange.services._2006.types.NonEmptyArrayOfAllItemsType;
import com.microsoft.schemas.exchange.services._2006.types.NonEmptyArrayOfBaseFolderIdsType;
import com.microsoft.schemas.exchange.services._2006.types.NonEmptyArrayOfBaseItemIdsType;
import com.microsoft.schemas.exchange.services._2006.types.RestrictionType;
import com.microsoft.schemas.exchange.services._2006.types.TargetFolderIdType;
import com.microsoft.schemas.exchange.services._2006.types.UnindexedFieldURIType;
import com.openexchange.ews.EWSExceptionCodes;
import com.openexchange.ews.ExchangeWebService;
import com.openexchange.ews.Items;
import com.openexchange.exception.OXException;

/**
 * {@link ItemsImpl}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class ItemsImpl extends Common implements Items {

    public ItemsImpl(ExchangeWebService service, ExchangeServicePortType port) {
        super(service, port);
    }

    @Override
    public void deleteItems(List<ItemIdType> itemIds, DisposalType disposal) throws OXException {
        NonEmptyArrayOfBaseItemIdsType itemIdsArray = new NonEmptyArrayOfBaseItemIdsType();
        itemIdsArray.getItemIdOrOccurrenceItemIdOrRecurringMasterItemId().addAll(itemIds);
        DeleteItemType request = new DeleteItemType();
        request.setDeleteType(disposal);
        request.setItemIds(itemIdsArray);
        Holder<DeleteItemResponseType> responseHolder = new Holder<DeleteItemResponseType>();
        port.deleteItem(request, getRequestVersion(), responseHolder, getVersionHolder());
        check(getResponseMessages(responseHolder));
    }

    @Override
    public void deleteItem(ItemIdType itemId, DisposalType disposal) throws OXException {
        NonEmptyArrayOfBaseItemIdsType itemIdsArray = new NonEmptyArrayOfBaseItemIdsType();
        itemIdsArray.getItemIdOrOccurrenceItemIdOrRecurringMasterItemId().add(itemId);
        DeleteItemType request = new DeleteItemType();
        request.setDeleteType(disposal);
        request.setItemIds(itemIdsArray);
        Holder<DeleteItemResponseType> responseHolder = new Holder<DeleteItemResponseType>();
        port.deleteItem(request, getRequestVersion(), responseHolder, getVersionHolder());
        ResponseMessageType responseMessage = getResponseMessage(responseHolder);
        check(responseMessage);
    }

    @Override
    public void createItems(FolderIdType targetFolderID, List<? extends ItemType> items, MessageDispositionType messageDisposition) throws OXException {
        NonEmptyArrayOfAllItemsType itemsArray = new NonEmptyArrayOfAllItemsType();
        itemsArray.getItemOrMessageOrCalendarItem().addAll(items);
        TargetFolderIdType targetFolder = new TargetFolderIdType();
        targetFolder.setFolderId(targetFolderID);
        CreateItemType request = new CreateItemType();
        request.setItems(itemsArray);
        request.setSavedItemFolderId(targetFolder);
        request.setMessageDisposition(messageDisposition);
        Holder<CreateItemResponseType> responseHolder = new Holder<CreateItemResponseType>();
        port.createItem(request, getRequestVersion(), responseHolder, getVersionHolder());
        check(getResponseMessages(responseHolder));
    }

    @Override
    public void createItem(FolderIdType targetFolderID, ItemType item, MessageDispositionType messageDisposition) throws OXException {
        this.createItems(targetFolderID, Arrays.asList(new ItemType[] { item }), messageDisposition);
    }

    @Override
    public List<ItemType> findItemsBySubject(BaseFolderIdType parentFolderID, String subject, ItemQueryTraversalType traversal, DefaultShapeNamesType shape) throws OXException {
        RestrictionType restriction = getIsEqualRestriction(UnindexedFieldURIType.ITEM_SUBJECT, subject);
        return findItems(parentFolderID, restriction, traversal, shape);
    }

    @Override
    public List<ItemType> findItemsBySubject(BaseFolderIdType parentFolderID, List<String> subjects, ItemQueryTraversalType traversal, DefaultShapeNamesType shape) throws OXException {
        RestrictionType restriction = getIsEqualRestriction(UnindexedFieldURIType.ITEM_SUBJECT, subjects);
        return findItems(parentFolderID, restriction, traversal, shape);
    }

    @Override
    public ItemType findItemBySubject(BaseFolderIdType parentFolderID, String subject, ItemQueryTraversalType traversal, DefaultShapeNamesType shape) throws OXException {
        RestrictionType restriction = getIsEqualRestriction(UnindexedFieldURIType.ITEM_SUBJECT, subject);
        List<ItemType> items = findItems(parentFolderID, restriction, traversal, shape);
        if (null == items || 0 == items.size()) {
            throw EWSExceptionCodes.NOT_FOUND.create(subject);
        } else if (1 != items.size()) {
            throw EWSExceptionCodes.AMBIGUOUS_NAME.create(subject);
        } else {
            return items.get(0);
        }
    }

    private List<ItemType> findItems(BaseFolderIdType parentFolderID, RestrictionType restriction, ItemQueryTraversalType traversal, DefaultShapeNamesType shape) throws OXException {
        NonEmptyArrayOfBaseFolderIdsType parentFolderIDs = new NonEmptyArrayOfBaseFolderIdsType();
        parentFolderIDs.getFolderIdOrDistinguishedFolderId().add(parentFolderID);
        ItemResponseShapeType itemShape = new ItemResponseShapeType();
        itemShape.setBaseShape(shape);
        FindItemType request = new FindItemType();
        request.setItemShape(itemShape);
        request.setTraversal(traversal);
        request.setRestriction(restriction);
        request.setParentFolderIds(parentFolderIDs);
        Holder<FindItemResponseType> responseHolder = new Holder<FindItemResponseType>();
        port.findItem(request, getRequestVersion(), responseHolder, getVersionHolder());
        FindItemResponseMessageType responseMessage = (FindItemResponseMessageType)getResponseMessage(responseHolder);
        check(responseMessage);
        return null != responseMessage.getRootFolder() && null != responseMessage.getRootFolder().getItems() ? responseMessage.getRootFolder().getItems().getItemOrMessageOrCalendarItem() : null;
    }

}