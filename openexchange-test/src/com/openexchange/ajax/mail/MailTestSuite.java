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

package com.openexchange.ajax.mail;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import com.openexchange.test.concurrent.ParallelSuite;

/**
 * {@link MailTestSuite}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@RunWith(ParallelSuite.class)
@Suite.SuiteClasses({
    AllTest.class,
    AttachmentTest.class,
    ClearTest.class,
    CopyMailTest.class,
    CountMailTest.class,
    ForwardMailTest.class,
    GetTest.class,
    ListTest.class,
    MailSearchTest.class,
    MoveMailTest.class,
    NewMailTest.class,
    MultipleAttachmentTest.class,
    ReplyAllTest.class,
    ReplyTest.class,
    SearchTest.class,
    SendTest.class,
    Send2Test.class,
    UpdateMailTest.class,
    ViewTest.class,
    AllAliasTest.class,
    ListAliasTest.class,
    GetStructureTest.class,
    Base64Test.class,
    AllSeenMailTest.class,

    /* AlwaysTest.class, */

    Bug12409Test.class,
    Bug14234Test.class,
    Bug15608Test.class,
    Bug15777Test.class,
    Bug15901Test.class,
    Bug16087Test.class,
    Bug16141Test.class,
    Bug29865Test.class,
    Bug19696Test.class,
    Bug30903Test.class,
    Bug31855Test.class,
    Bug32355Test.class,
//    Bug27708Test.class,
    Bug28913Test.class,
    Bug29437Test.class,
    Bug34254Test.class,
    Bug36333Test.class,
    Bug37247Test.class,

    TestEstimateLength.class,

    MaxMailSizeTest.class,
//    MSISDNAddressTest.class, msisdn not supported by mail infrastructure
    ExamineTest.class,

})
public final class MailTestSuite  {

}
