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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.subscribe.internal;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.subscribe.Subscription;


/**
 * {@link SimFolderUpdaterStrategy}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public class SimFolderUpdaterStrategy implements FolderUpdaterStrategy<String> {

    private Set<String> dataSet;
    
    private Set<String> savedElements = new HashSet<String>();
    private Map<String, String> updatedElements = new HashMap<String, String>();
    
    public boolean handles(FolderObject folder) {
        return true;
    }

    public void setDataSet(String...data) {
        dataSet = new HashSet<String>(Arrays.asList(data));
    }

    public boolean wasUpdated(String orig, String update) {
        if(!updatedElements.containsKey(orig)) {
            return false;
        }
        return updatedElements.get(orig).equals(update);
    }

    public boolean wasCreated(String string) {
        return savedElements.contains(string);
    }

    public int calculateSimilarityScore(String original, String candidate, Object session) throws AbstractOXException {
        int counter = 0;
        for (int i = 0, size = Math.min(original.length(), candidate.length()); i < size; i++) {
            int cO = original.charAt(i);
            int cC = candidate.charAt(i);
            if(cO == cC) {
                counter++;
            } else {
                return counter;
            }
        }
        return counter;
    }

    public void closeSession(Object session) throws AbstractOXException {
        
    }

    public Collection<String> getData(Subscription subscription, Object session) throws AbstractOXException {
        return dataSet;
    }

    public int getThreshhold(Object session) throws AbstractOXException {
        return 3;
    }

    public void save(String newElement, Object session) throws AbstractOXException {
        savedElements.add(newElement);
    }

    public Object startSession(Subscription subscription) throws AbstractOXException {
        return null;
    }

    public void update(String original, String update, Object session) throws AbstractOXException {
        updatedElements.put(original, update);
    }

}
