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
        final Collection<T> dataInFolder = strategy.getData(target, session);
        try {
            while (data.hasNext()) {
                T element = data.next();
                if (null != element) {
                    final T bestMatch = findBestMatch(element, dataInFolder, session);
                    if(bestMatch == null) {
                        strategy.save(element, session, errors);
                    } else {
                        strategy.update(bestMatch, element, session);
                    }
                }
                element = null;
            }
        } catch (final OXException x) {
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
            if(currentScore > maxScore) {
                maxElement = elementInFolder;
                maxScore = currentScore;
            }
        }
        if(maxScore > strategy.getThreshold(session)) {
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
