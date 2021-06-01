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

package com.openexchange.ajax.folder.api2;

import static org.junit.Assert.assertFalse;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.InsertRequest;
import com.openexchange.ajax.folder.actions.InsertResponse;
import com.openexchange.ajax.folder.actions.UpdateRequest;
import com.openexchange.groupware.container.FolderObject;

/**
 * {@link Bug44895Test}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.8.2
 */
public class Bug44895Test extends AbstractFolderTest {

    FolderObject calendarFolder;

    /**
     * Initializes a new {@link Bug44895Test}.
     *
     * @param name
     */
    public Bug44895Test() {
        super();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        calendarFolder = new FolderObject();
        calendarFolder.setFolderName("Bug44895Test");
        calendarFolder.setModule(FolderObject.CALENDAR);
        calendarFolder.setParentFolderID(client.getValues().getPrivateAppointmentFolder());
        InsertRequest req = new InsertRequest(EnumAPI.OX_NEW, calendarFolder);
        InsertResponse resp = client.execute(req);
        resp.fillObject(calendarFolder);
    }

    @Test
    public void testBug44895() throws Exception {
        calendarFolder.setFolderName("shouldNotFailInCalendarModule<>");
        UpdateRequest req = new UpdateRequest(EnumAPI.OX_NEW, calendarFolder, false);
        InsertResponse resp = client.execute(req);
        assertFalse(resp.hasError());
    }

}
