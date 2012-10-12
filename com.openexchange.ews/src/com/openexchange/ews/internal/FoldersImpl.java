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

package com.openexchange.ews.internal;

import java.util.List;
import javax.xml.ws.Holder;
import com.microsoft.schemas.exchange.services._2006.messages.ExchangeServicePortType;
import com.microsoft.schemas.exchange.services._2006.messages.FindFolderResponseType;
import com.microsoft.schemas.exchange.services._2006.messages.FindFolderType;
import com.microsoft.schemas.exchange.services._2006.messages.FolderInfoResponseMessageType;
import com.microsoft.schemas.exchange.services._2006.messages.GetFolderResponseType;
import com.microsoft.schemas.exchange.services._2006.messages.GetFolderType;
import com.microsoft.schemas.exchange.services._2006.types.BaseFolderIdType;
import com.microsoft.schemas.exchange.services._2006.types.BaseFolderType;
import com.microsoft.schemas.exchange.services._2006.types.DefaultShapeNamesType;
import com.microsoft.schemas.exchange.services._2006.types.FolderQueryTraversalType;
import com.microsoft.schemas.exchange.services._2006.types.FolderResponseShapeType;
import com.microsoft.schemas.exchange.services._2006.types.NonEmptyArrayOfBaseFolderIdsType;
import com.microsoft.schemas.exchange.services._2006.types.RestrictionType;
import com.openexchange.ews.ExchangeWebService;
import com.openexchange.ews.Folders;

/**
 * {@link FoldersImpl}
 * 
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class FoldersImpl extends Common implements Folders {
    
    public FoldersImpl(ExchangeWebService service, ExchangeServicePortType port) {
        super(service, port);
    }
    
    @Override
    public BaseFolderType getFolder(BaseFolderIdType folderID) {
        return getFolder(folderID, DefaultShapeNamesType.ALL_PROPERTIES);
    }
    
    @Override
    public BaseFolderType getFolder(BaseFolderIdType folderID, DefaultShapeNamesType shape) {
        NonEmptyArrayOfBaseFolderIdsType folderIds = new NonEmptyArrayOfBaseFolderIdsType();
        folderIds.getFolderIdOrDistinguishedFolderId().add(folderID);
        FolderResponseShapeType folderShape = new FolderResponseShapeType();
        folderShape.setBaseShape(shape);
        GetFolderType request = new GetFolderType();
        request.setFolderShape(folderShape);
        request.setFolderIds(folderIds);        
        Holder<GetFolderResponseType> responseHolder = new Holder<GetFolderResponseType>();
        port.getFolder(request, getRequestVersion(), responseHolder, getVersionHolder());
        FolderInfoResponseMessageType responseMessage = (FolderInfoResponseMessageType)getResponseMessage(responseHolder);
        if (null != responseMessage.getFolders() && null != responseMessage.getFolders().getFolderOrCalendarFolderOrContactsFolder() &&
            1 == responseMessage.getFolders().getFolderOrCalendarFolderOrContactsFolder().size()) {
            return responseMessage.getFolders().getFolderOrCalendarFolderOrContactsFolder().get(0);
        } else {
            return null;
        }
    }

    @Override
    public List<BaseFolderType> findFolders(BaseFolderIdType parentFolderID, RestrictionType restriction, FolderQueryTraversalType traversal, DefaultShapeNamesType shape) {
        NonEmptyArrayOfBaseFolderIdsType parentFolderIDs = new NonEmptyArrayOfBaseFolderIdsType();
        parentFolderIDs.getFolderIdOrDistinguishedFolderId().add(parentFolderID);
        FolderResponseShapeType folderShape = new FolderResponseShapeType();
        folderShape.setBaseShape(shape);
        FindFolderType request = new FindFolderType();
        request.setFolderShape(folderShape);
        request.setTraversal(traversal);
        request.setRestriction(restriction);
        request.setParentFolderIds(parentFolderIDs);
        Holder<FindFolderResponseType> responseHolder = new Holder<FindFolderResponseType>();
        port.findFolder(request, getRequestVersion(), responseHolder, getVersionHolder());
        FolderInfoResponseMessageType responseMessage = (FolderInfoResponseMessageType)getResponseMessage(responseHolder);
        return null != responseMessage.getFolders() ? responseMessage.getFolders().getFolderOrCalendarFolderOrContactsFolder() : null;
    }
    

}