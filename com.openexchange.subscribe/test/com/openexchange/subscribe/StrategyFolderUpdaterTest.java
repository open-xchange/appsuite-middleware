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

package com.openexchange.subscribe;

import static org.junit.Assert.assertTrue;
import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.subscribe.internal.SimFolderUpdaterStrategy;
import com.openexchange.subscribe.internal.StrategyFolderUpdaterService;
import com.openexchange.tools.iterator.SearchIteratorDelegator;

/**
 * {@link ContactFolderUpdater}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public class StrategyFolderUpdaterTest {

    private SimFolderUpdaterStrategy simStrategy;
    private StrategyFolderUpdaterService<String> updater;

    @Before
    public void setUp() {
        simStrategy = new SimFolderUpdaterStrategy();
        simStrategy.setDataSet("aaaac", "baaaa");
        updater = new StrategyFolderUpdaterService<String>(simStrategy);
    }

    @Test
    public void testMerging() throws Exception {
        List<String> list = Arrays.asList("aaaab", "baaaa", "new");
        updater.save(new SearchIteratorDelegator<String>(list), null);
        assertTrue("Expected update to aaaac", simStrategy.wasUpdated("aaaac", "aaaab"));
        assertTrue("Expected creation of 'new'", simStrategy.wasCreated("new"));
    }

}
