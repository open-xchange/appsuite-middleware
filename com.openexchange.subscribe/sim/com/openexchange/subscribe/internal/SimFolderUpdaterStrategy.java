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

package com.openexchange.subscribe.internal;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.generic.TargetFolderDefinition;


/**
 * {@link SimFolderUpdaterStrategy}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public class SimFolderUpdaterStrategy implements FolderUpdaterStrategy<String> {

    private Set<String> dataSet;

    private final Set<String> savedElements = new HashSet<String>();
    private final Map<String, String> updatedElements = new HashMap<String, String>();

    @Override
    public boolean handles(final FolderObject folder) {
        return true;
    }

    public void setDataSet(final String...data) {
        dataSet = new HashSet<String>(Arrays.asList(data));
    }

    public boolean wasUpdated(final String orig, final String update) {
        if(!updatedElements.containsKey(orig)) {
            return false;
        }
        return updatedElements.get(orig).equals(update);
    }

    public boolean wasCreated(final String string) {
        return savedElements.contains(string);
    }

    @Override
    public int calculateSimilarityScore(final String original, final String candidate, final Object session) throws OXException {
        int counter = 0;
        for (int i = 0, size = Math.min(original.length(), candidate.length()); i < size; i++) {
            final int cO = original.charAt(i);
            final int cC = candidate.charAt(i);
            if(cO == cC) {
                counter++;
            } else {
                return counter;
            }
        }
        return counter;
    }

    @Override
    public void closeSession(final Object session) throws OXException {

    }

    @Override
    public Collection<String> getData(final TargetFolderDefinition target, final Object session) throws OXException {
        return dataSet;
    }

    @Override
    public int getThreshold(final Object session) throws OXException {
        return 3;
    }

    @Override
    public void save(final String newElement, final Object session, Collection<OXException> errors) throws OXException {
        savedElements.add(newElement);
    }

    @Override
    public Object startSession(final TargetFolderDefinition target) throws OXException {
        return null;
    }

    @Override
    public void update(final String original, final String update, final Object session) throws OXException {
        updatedElements.put(original, update);
    }

}
