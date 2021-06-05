/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
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
        if (!updatedElements.containsKey(orig)) {
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
            if (cO == cC) {
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
