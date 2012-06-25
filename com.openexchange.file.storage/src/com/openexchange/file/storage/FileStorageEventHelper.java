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

package com.openexchange.file.storage;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Set;
import org.osgi.service.event.Event;
import com.openexchange.exception.OXException;
import com.openexchange.session.Session;


/**
 * {@link FileStorageEventHelper}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class FileStorageEventHelper {

    public static Event buildUpdateEvent(Session session, String service, String accountId, String folderId, String objectId) {
        Event event = new Event(FileStorageEventConstants.UPDATE_TOPIC, buildProperties(session, service, accountId, folderId, objectId));        
        return event;
    }
    
    public static Event buildCreateEvent(Session session, String service, String accountId, String folderId, String objectId) {        
        Event event = new Event(FileStorageEventConstants.CREATE_TOPIC, buildProperties(session, service, accountId, folderId, objectId));        
        return event;
    }   
    
    public static Event buildDeleteEvent(Session session, String service, String accountId, String folderId, String objectId, Set<Integer> versions) {
        Dictionary<String,Object> properties = buildProperties(session, service, accountId, folderId, objectId);
        /*
         * version may be null to indicate a complete deletion of a document.
         */
        if (versions != null) {
            properties.put(FileStorageEventConstants.VERSIONS, versions);
        }
        
        Event event = new Event(FileStorageEventConstants.DELETE_TOPIC, properties);        
        return event;
    }
    
    private static Dictionary<String, Object> buildProperties(Session session, String service, String accountId, String folderId, String objectId) {
        Dictionary<String, Object> ht = new Hashtable<String, Object>();
        ht.put(FileStorageEventConstants.SESSION, session);
        ht.put(FileStorageEventConstants.SERVICE, service);
        ht.put(FileStorageEventConstants.ACCOUNT_ID, accountId);           
        ht.put(FileStorageEventConstants.OBJECT_ID, objectId); 
        ht.put(FileStorageEventConstants.FOLDER_ID, folderId);
        
        return ht;
    }
    
    public static boolean isCreateEvent(Event event) {
        return event.getTopic().equals(FileStorageEventConstants.CREATE_TOPIC);
    }
    
    public static boolean isUpdateEvent(Event event) {
        return event.getTopic().equals(FileStorageEventConstants.UPDATE_TOPIC);
    }
    
    public static boolean isDeleteEvent(Event event) {
        return event.getTopic().equals(FileStorageEventConstants.DELETE_TOPIC);
    }
    
    public static boolean isInfostoreEvent(Event event) {
        return "com.openexchange.infostore".equals(event.getProperty(FileStorageEventConstants.SERVICE));
    }

    public static Session extractSession(Event event) throws OXException {
        Object sessionObj = event.getProperty(FileStorageEventConstants.SESSION);
        if (sessionObj == null) {
            throw FileStorageExceptionCodes.MISSING_PARAMETER.create(FileStorageEventConstants.SESSION);
        }
        
        if (sessionObj instanceof Session) {
            return (Session) sessionObj;
        } else {
            throw FileStorageExceptionCodes.INVALID_PARAMETER.create(FileStorageEventConstants.SESSION, sessionObj.getClass().getName());
        }
    }
    
    public static String extractObjectId(Event event) throws OXException {
        return extractValue(event, FileStorageEventConstants.OBJECT_ID);
    }

    public static String extractFolderId(Event event) throws OXException {
        return extractValue(event, FileStorageEventConstants.FOLDER_ID);
    }
    
    public static String extractAccountId(Event event) throws OXException {
        return extractValue(event, FileStorageEventConstants.ACCOUNT_ID);
    }
    
    public static String extractService(Event event) throws OXException {
        return extractValue(event, FileStorageEventConstants.SERVICE);
    }
    
    public static Set<Integer> extractVersions(Event event) {
        Object versionsObj = event.getProperty(FileStorageEventConstants.VERSIONS);
        if (versionsObj == null || !(versionsObj instanceof Set<?>)) {
            return null;
        }
        
        return (Set<Integer>) versionsObj;
    }
    
    public static String createDebugMessage(String eventName, Event event) {
        StringBuilder sb = new StringBuilder("Received ");
        sb.append(eventName);
        sb.append(": ");
        sb.append(event.toString());
        for (String key : event.getPropertyNames()) {
            Object value = event.getProperty(key);            
            sb.append("\n    ");
            sb.append(key);
            sb.append(": ");
            sb.append(value.toString());            
        }
        
        return sb.toString();
    }
    
    private static String extractValue(Event event, String key) throws OXException {
        Object obj = event.getProperty(key);
        if (obj == null) {
            throw FileStorageExceptionCodes.MISSING_PARAMETER.create(key);
        }
        
        if (obj instanceof String) {
            return (String) obj;
        }
        
        throw FileStorageExceptionCodes.INVALID_PARAMETER.create(key, obj.getClass().getName(), String.class.getName());
    }
}
