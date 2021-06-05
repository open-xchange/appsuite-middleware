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

package com.openexchange.messaging.json;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;


/**
 * {@link UnitTests}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    MessagingAccountParserTest.class,
    MessagingAccountWriterTest.class,
    MessagingServiceWriterTest.class,
    MessagingMessageWriterTest.class,
    MessagingMessageParserTest.class,

    ContentTypeWriterTest.class,
    ContentTypeParserTest.class,
    AddressHeaderWriterTest.class,
    AddressHeaderParserTest.class,

    com.openexchange.messaging.json.actions.accounts.AllTest.class,
    com.openexchange.messaging.json.actions.accounts.DeleteTest.class,
    com.openexchange.messaging.json.actions.accounts.GetTest.class,
    com.openexchange.messaging.json.actions.accounts.NewTest.class,
    com.openexchange.messaging.json.actions.accounts.UpdateTest.class,

    com.openexchange.messaging.json.actions.services.AllActionTest.class,
    com.openexchange.messaging.json.actions.services.GetActionTest.class,

    com.openexchange.messaging.json.actions.messages.AllTest.class,
    com.openexchange.messaging.json.actions.messages.GetTest.class,
    com.openexchange.messaging.json.actions.messages.ListTest.class,
    com.openexchange.messaging.json.actions.messages.PerformTest.class,
    com.openexchange.messaging.json.actions.messages.SendTest.class,
    com.openexchange.messaging.json.actions.messages.MessagingRequestDataTest.class,
})
public class UnitTests {
}
