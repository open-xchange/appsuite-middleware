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

package com.openexchange.dav.caldav.bugs;

import org.apache.jackrabbit.webdav.client.methods.DeleteMethod;
import org.junit.Assert;
import org.junit.Test;
import com.openexchange.dav.Config;
import com.openexchange.dav.caldav.CalDAVTest;

/**
 * {@link Bug44304Test}
 *
 * NPE at c.o.webdav.protocol.helpers.AbstractWebdavFactory.mixin
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.1
 */
public class Bug44304Test extends CalDAVTest {

    @Test
    public void testDeleteScheduleInboxResource() throws Exception {
        DeleteMethod delete = null;
        try {
            String href = "/caldav/schedule-inbox/BD9B8F1B-78ED-4768-BE8C-3AB687AC2A90.ics";
            delete = new DeleteMethod(getBaseUri() + Config.getPathPrefix() + href);
            Assert.assertEquals("response code wrong", 204, getWebDAVClient().executeMethod(delete));
        } finally {
            release(delete);
        }
    }

}
