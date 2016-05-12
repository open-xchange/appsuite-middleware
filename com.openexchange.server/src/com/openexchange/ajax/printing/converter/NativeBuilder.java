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

package com.openexchange.ajax.printing.converter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;


/**
 * {@link NativeBuilder}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class NativeBuilder {
    private Stack<Object> stack;
    
    private Map<String, Object> currentMap;
    private List<Object> currentList;
    
    private Object current;
    private Object initial;
    
    private String key;
    
    public NativeBuilder list() {
        if (current != null) {
            stack.push(current);            
        }
        List<Object> newList = new ArrayList<Object>();

        if ( !isNil()) {
            value(newList);
        }
        current = currentList = newList;
        currentMap = null;

        if (initial == null) {
            initial = current;
        }
        return this;
    }
    
    public NativeBuilder map() {
        if (current != null) {
            stack.push(current);            
        }
        Map<String, Object> newMap = new HashMap<String, Object>();

        if ( !isNil() ) {
            value(newMap);
        }
        
        current = currentMap = newMap;
        currentList = null;
        
        if (initial == null) {
            initial = current;
        }
        
        return this;
    }
    
    public NativeBuilder key(String key) {
        if ( isNil() ) {
            throw new IllegalStateException("Please start with either #map() or #list()");    
        }
        
        if ( isList() ) {
            throw new IllegalStateException("Lists can only have values");
        }
        
        this.key = key;
        
        return this;
    }
    
    public NativeBuilder value(Object o) {
        if ( isNil() ) {
            throw new IllegalStateException("Please start with either #map() or #list()");
        }
        
        if ( isList() ) {
            currentList.add(o);
        } else {
            if (! hasKey()) {
                throw new IllegalStateException("Please provide a key for every value");
            }
            currentMap.put(key, o);
            key = null;
        }
        return this;
    }
    
    public NativeBuilder end() {
        if (stack.isEmpty()) {
            return this;
        }
        Object previous = stack.pop();
        current = null;
        if (Map.class.isInstance(previous)) {
            current = currentMap = (Map<String, Object>) previous;
            currentList = null;
        }
        
        if (List.class.isInstance(previous)) {
            current = currentList = (List<Object>) previous;
            currentMap = null;
        }
        return this;
    }
    
    public Map<String, Object> getMap() {
        
        return (Map<String, Object>) initial;
    }
    
    public List getList() {

        return (List<Object>) initial;
    }
    
    private boolean isNil() {
        return current == null;
    }
    
    private boolean isList() {
        return currentList == null;
    }
    
    private boolean isMap() {
        return currentMap == null;
    }
    
    private boolean hasKey() {
        return key != null;
    }
    
    
    
}
