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

import java.util.Collection;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.generic.FolderUpdaterServiceV2;
import com.openexchange.groupware.generic.TargetFolderDefinition;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIterators;


/**
 * {@link StrategyFolderUpdaterService}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public class StrategyFolderUpdaterService<T> implements FolderUpdaterServiceV2<T> {

    private final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(StrategyFolderUpdaterService.class);

    private final FolderUpdaterStrategy<T> strategy;

    // a normal updater overwrites existing objects
    private boolean usesMultipleStrategy = false;

    public StrategyFolderUpdaterService(final FolderUpdaterStrategy<T> strategy) {
        this.strategy = strategy;
    }

    public StrategyFolderUpdaterService(final FolderUpdaterStrategy<T> strategy, final boolean usesMultipleStrategy) {
        this.strategy = strategy;
        this.usesMultipleStrategy = usesMultipleStrategy;
    }

    @Override
    public boolean handles(final FolderObject folder) {
        return strategy.handles(folder);
    }

    @Override
    public void save(final SearchIterator<T> data, final TargetFolderDefinition target) throws OXException {
        save(data, target, null);
    }

    @Override
    public void save(SearchIterator<T> data, TargetFolderDefinition target, Collection<OXException> errors) throws OXException {
        final Object session = strategy.startSession(target);
        try {
            final Collection<T> dataInFolder = strategy.getData(target, session);
            while (data.hasNext()) {
                T element = data.next();
                if (null != element) {
                    final T bestMatch = findBestMatch(element, dataInFolder, session);
                    if (bestMatch == null) {
                        strategy.save(element, session, errors);
                    } else {
                        strategy.update(bestMatch, element, session);
                    }
                }
                element = null;
            }
        } catch (OXException x) {
            if (null == errors) {
                LOG.error("", x);
            } else {
                errors.add(x);
            }
            throw x; //TODO: re-throw also in case of passed errors collection?
        } finally {
            SearchIterators.close(data);
            strategy.closeSession(session);
        }
    }

    private T findBestMatch(final T element, final Collection<T> dataInFolder, final Object session) throws OXException {
        // USM for the poor
        int maxScore = -1;
        T maxElement = null;
        for(final T elementInFolder : dataInFolder) {
            final int currentScore = strategy.calculateSimilarityScore(elementInFolder, element, session);
            if (currentScore > maxScore) {
                maxElement = elementInFolder;
                maxScore = currentScore;
            }
        }
        if (maxScore > strategy.getThreshold(session)) {
            return maxElement;
        }
       return null;
    }

    /**
     * This attribute defines whether the Updater should:
     * - overwrite existing objects, deleting fields not given by the update (the classic update: one subscription on one folder)
     * - only touch fields given in the updated object (the aggregating update: multiple subscriptions on one folder)
     */
    @Override
    public boolean usesMultipleStrategy() {
        return usesMultipleStrategy;
    }

}
