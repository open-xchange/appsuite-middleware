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

package com.openexchange.ews;

import java.util.List;
import com.microsoft.schemas.exchange.services._2006.types.BaseFolderIdType;
import com.microsoft.schemas.exchange.services._2006.types.DefaultShapeNamesType;
import com.microsoft.schemas.exchange.services._2006.types.DisposalType;
import com.microsoft.schemas.exchange.services._2006.types.FolderIdType;
import com.microsoft.schemas.exchange.services._2006.types.ItemIdType;
import com.microsoft.schemas.exchange.services._2006.types.ItemQueryTraversalType;
import com.microsoft.schemas.exchange.services._2006.types.ItemType;
import com.microsoft.schemas.exchange.services._2006.types.MessageDispositionType;
import com.openexchange.exception.OXException;

/**
 * {@link Items}
 *
 * Simplifies access to items using EWS.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public interface Items {

    /**
     * Searches for items by their subject.
     *
     * @param parentFolderID The parent folder ID
     * @param name The item's subject
     * @param traversal The traversal type
     * @param shape The item shape
     * @return The found items
     * @throws OXException
     */
    List<ItemType> findItemsBySubject(BaseFolderIdType parentFolderID, String subject, ItemQueryTraversalType traversal, DefaultShapeNamesType shape) throws OXException;

    List<ItemType> findItemsBySubject(BaseFolderIdType parentFolderID, List<String> subjects, ItemQueryTraversalType traversal, DefaultShapeNamesType shape) throws OXException;

    /**
     * Searches for an item by it's subject.
     *
     * @param parentFolderID The parent folder ID
     * @param name The item's subject
     * @param traversal The traversal type
     * @param shape The item shape
     * @return The found item
     * @throws OXException
     */
    ItemType findItemBySubject(BaseFolderIdType parentFolderID, String subject, ItemQueryTraversalType traversal, DefaultShapeNamesType shape) throws OXException;

    void deleteItems(List<ItemIdType> itemIds, DisposalType disposal) throws OXException;

    void deleteItem(ItemIdType itemId, DisposalType disposal) throws OXException;

    void createItems(FolderIdType targetFolderID, List<? extends ItemType> items, MessageDispositionType messageDisposition) throws OXException;

    void createItem(FolderIdType targetFolderID, ItemType item, MessageDispositionType messageDisposition) throws OXException;
}