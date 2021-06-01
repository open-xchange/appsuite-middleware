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

package com.openexchange.ajax.task;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import com.openexchange.test.concurrent.ParallelSuite;

/**
 * Suite for all task tests.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
@RunWith(ParallelSuite.class)
@Suite.SuiteClasses({
    TasksTest.class,
    TaskAttachmentTests.class,

    // Now several single function tests.
    InsertTest.class,
    CharsetTest.class,
    TruncationTest.class,
    FloatTest.class,
    AllTest.class,
    ListTest.class,
    UpdatesTest.class,
    TaskRecurrenceTest.class,
    ConfirmTest.class,
    AllAliasTest.class,
    ListAliasTest.class,
    TaskDurationAndCostsTest.class,
    DeleteMultipleTaskTest.class,
    DateTimeTest.class,

    // Nodes
    LastModifiedUTCTest.class,

    // And finally bug tests.
    Bug6335Test.class,
    Bug7276Test.class,
    Bug7380Test.class,
    Bug8935Test.class,
    Bug9252Test.class,
    Bug10071Test.class,
    Bug10119Test.class,
    Bug10400Test.class,
    Bug11075Test.class,
    Bug11190Test.class,
    Bug11195Test.class,
    Bug11397Test.class,
    Bug11619Test.class,
    Bug11650Test.class,
    Bug11659Test.class,
    Bug11848Test.class,
    Bug12364Test.class,
    Bug12727Test.class,
    Bug12926Test.class,
    Bug13173Test.class,
    Bug14002Test.class,
    Bug15291Test.class,
    Bug15580Test.class,
    Bug15897Test.class,
    Bug15937Test.class,
    Bug16006Test.class,
    Bug18204Test.class,
    Bug20008Test.class,
    Bug21026Test.class,
    Bug22305Test.class,
    Bug23444Test.class,
    Bug26217Test.class,
    Bug27840Test.class,
    Bug28089Test.class,
    Bug30015Test.class,
    Bug32044Test.class,
    Bug33258Test.class,
    Bug35992Test.class,
    Bug37002Test.class,
    Bug37424Test.class,
    Bug37927Test.class,
    Bug38782Test.class,
    Bug50739Test.class,
    Bug58023Test.class,
    Bug65799Test.class,
})
public final class TaskTestSuite {
}
