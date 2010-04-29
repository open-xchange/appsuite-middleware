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
 *     Copyright (C) 2004-2010 Open-Xchange, Inc.
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

package com.openexchange.messaging.json.cacheing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.openexchange.caching.Cache;
import com.openexchange.caching.CacheException;
import com.openexchange.messaging.IndexRange;
import com.openexchange.messaging.MessagingContent;
import com.openexchange.messaging.MessagingException;
import com.openexchange.messaging.MessagingField;
import com.openexchange.messaging.MessagingMessage;
import com.openexchange.messaging.MessagingMessageAccess;
import com.openexchange.messaging.MessagingPart;
import com.openexchange.messaging.OrderDirection;
import com.openexchange.messaging.SearchTerm;
import com.openexchange.session.Session;

/**
 * {@link CacheingMessageAccess}
 * 
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class CacheingMessageAccess implements MessagingMessageAccess {

    private final MessagingMessageAccess delegate;
    private final Cache cache;
    private final String folderPrefix;
    private final Session session;

    public CacheingMessageAccess(MessagingMessageAccess delegate, Cache cache, String folderPrefix, Session session) {
        this.delegate = delegate;
        this.cache = cache;
        this.folderPrefix = folderPrefix;
        this.session = session;
    }

    public MessagingPart getAttachment(String folder, String messageId, String sectionId) throws MessagingException {
        return delegate.getAttachment(folder, messageId, sectionId);
    }

    public void appendMessages(String folder, MessagingMessage[] messages) throws MessagingException {
        delegate.appendMessages(folder, messages);
    }

    public List<String> copyMessages(String sourceFolder, String destFolder, String[] messageIds, boolean fast) throws MessagingException {
        return delegate.copyMessages(sourceFolder, destFolder, messageIds, fast);
    }

    public void deleteMessages(String folder, String[] messageIds, boolean hardDelete) throws MessagingException {
        delegate.deleteMessages(folder, messageIds, hardDelete);
    }

    public List<MessagingMessage> getAllMessages(String folder, IndexRange indexRange, MessagingField sortField, OrderDirection order, MessagingField... fields) throws MessagingException {
        clear(folder);
        return remember(delegate.getAllMessages(folder, indexRange, sortField, order, addDefaultFields(fields)));
    }

    private MessagingField[] addDefaultFields(MessagingField[] fields) {
        Set<MessagingField> allFields = new HashSet<MessagingField>(Arrays.asList(fields));
        allFields.add(MessagingField.FOLDER_ID);
        allFields.add(MessagingField.ID);
        allFields.add(MessagingField.SUBJECT);
        allFields.add(MessagingField.FROM);
        allFields.add(MessagingField.RECEIVED_DATE);
        allFields.add(MessagingField.BODY);
        allFields.add(MessagingField.HEADERS);
        allFields.add(MessagingField.PICTURE);
        return allFields.toArray(new MessagingField[allFields.size()]);
    }

    public MessagingMessage getMessage(String folder, String id, boolean peek) throws MessagingException {
        MessagingMessage msg = get(folder, id);
        if (msg != null) {
            return msg;
        }

        return remember(delegate.getMessage(folder, id, peek));
    }

    public List<MessagingMessage> getMessages(String folder, String[] messageIds, MessagingField[] fields) throws MessagingException {
        Map<String, MessagingMessage> allMessages = new HashMap<String, MessagingMessage>();
        List<String> idsToLoad = new LinkedList<String>();
        
        for (String id : messageIds) {
            MessagingMessage cached = get(folder, id);
            if(cached != null) {
                allMessages.put(id, cached);
            } else {
                idsToLoad.add(id);
            }
        }
        
        if(!idsToLoad.isEmpty()) {
            List<MessagingMessage> messages = delegate.getMessages(folder, idsToLoad.toArray(new String[idsToLoad.size()]), fields);
            remember(messages);
            if(allMessages.isEmpty()) {
                return remember(messages);
            }
            for (MessagingMessage messagingMessage : messages) {
                allMessages.put(messagingMessage.getId(), messagingMessage);
            }
            
        }
        
        List<MessagingMessage> messages = new ArrayList<MessagingMessage>(messageIds.length);
        for (String id : messageIds) {
            messages.add(allMessages.get(id));
        }
        
        return remember(messages);
    }

    private List<MessagingMessage> remember(List<MessagingMessage> messages) throws MessagingException {
        for (MessagingMessage messagingMessage : messages) {
            remember(messagingMessage);
        }
        return messages;
    }

    public List<String> moveMessages(String sourceFolder, String destFolder, String[] messageIds, boolean fast) throws MessagingException {
        return delegate.moveMessages(sourceFolder, destFolder, messageIds, fast);
    }

    public MessagingMessage perform(MessagingMessage message, String action) throws MessagingException {
        return delegate.perform(message, action);
    }

    public MessagingMessage perform(String folder, String id, String action) throws MessagingException {
        return delegate.perform(folder, id, action);
    }

    public MessagingMessage perform(String action) throws MessagingException {
        return delegate.perform(action);
    }

    public List<MessagingMessage> searchMessages(String folder, IndexRange indexRange, MessagingField sortField, OrderDirection order, SearchTerm<?> searchTerm, MessagingField[] fields) throws MessagingException {
        return delegate.searchMessages(folder, indexRange, sortField, order, searchTerm, fields);
    }

    public void updateMessage(MessagingMessage message, MessagingField[] fields) throws MessagingException {
        delegate.updateMessage(message, fields);
    }

    protected MessagingMessage get(String folder, String id) {
        return (MessagingMessage) cache.getFromGroup(id, getGroupName(folder));
    }

    protected MessagingMessage remember(MessagingMessage message) throws MessagingException {
        String groupName = getGroupName(message.getFolder());
        String key = message.getId();
        
        try {
            if(key != null) {
                cache.putInGroup(key, groupName, message);
            }
        } catch (CacheException e) {
            throw new MessagingException(e);
        }
        
        return message;
    }

    protected void clear(String folderId) {
        cache.invalidateGroup(getGroupName(folderId));
        
    }

    protected String getGroupName(String folderId) {
        return session.getContextId()+"/"+folderPrefix+"/"+folderId;
    }

    public MessagingContent resolveContent(String folder, String id, String referenceId) throws MessagingException {
        return delegate.resolveContent(folder, id, referenceId);
    }

}
