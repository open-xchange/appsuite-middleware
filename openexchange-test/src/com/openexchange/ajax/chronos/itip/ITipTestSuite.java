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

package com.openexchange.ajax.chronos.itip;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import com.openexchange.ajax.chronos.itip.bugs.Bug65533Test;
import com.openexchange.ajax.chronos.itip.bugs.MWB263Test;
import com.openexchange.ajax.chronos.itip.bugs.ReplyBugsTest;

/**
 * {@link ITipTestSuite}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.0
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({ // @formatter:off
    ITipRequestTests.class,
    ITipReplyTest.class,
    ITipAnalyzeChangesTest.class,
    ITipSeriesTest.class,
    ITipSeriesExceptionTest.class,
    ITipOnBehalfTest.class,
    ITipCancelTest.class,
    //    InternalNotificationTest.class, No such notifications yet

    ReplyBugsTest.class, 
    Bug65533Test.class, 
    MWB263Test.class,
    SingleInstanceTest.class,
}) // @formatter:on
public class ITipTestSuite {

}
