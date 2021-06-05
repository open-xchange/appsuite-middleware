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

package com.openexchange.ajax.appointment.bugtests;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.test.common.groupware.calendar.TimeTools.D;
import static org.junit.Assert.assertFalse;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.appointment.action.DeleteRequest;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.framework.CommonDeleteResponse;
import com.openexchange.ajax.framework.MultipleRequest;
import com.openexchange.ajax.framework.MultipleResponse;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.FolderObject;

/**
 * {@link Bug26350Test}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class Bug26350Test extends AbstractAJAXSession {

    private final int cycles = 3;

    private final int chunkSize = 20;

    private List<List<Integer>> ids;

    private FolderObject folder;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ids = new ArrayList<List<Integer>>();

        folder = ftm.generatePrivateFolder("Bug26350 Folder" + UUID.randomUUID().toString(), FolderObject.CALENDAR, getClient().getValues().getPrivateAppointmentFolder(), getClient().getValues().getUserId());
        ftm.insertFolderOnServer(folder);
    }

    @Test
    public void testBug26350() throws Exception {
        for (int i = 0; i < cycles; i++) {
            List<Integer> chunkIds = new ArrayList<Integer>();
            for (int j = 0; j < chunkSize; j++) {
                Appointment app = new Appointment();
                app.setTitle("Bug 26350 Test " + System.currentTimeMillis());
                app.setStartDate(D("13.06.2013 08:00"));
                app.setEndDate(D("13.06.2013 09:00"));
                app.setParentFolderID(folder.getObjectID());
                app.setIgnoreConflicts(true);
                Appointment insert = catm.insert(app);
                chunkIds.add(I(insert.getObjectID()));
            }
            ids.add(chunkIds);
        }

        for (List<Integer> chunkIds : ids) {
            DeleteRequest[] requests = new DeleteRequest[chunkIds.size()];

            for (int j = 0; j < chunkIds.size(); j++) {
                requests[j] = new DeleteRequest(chunkIds.get(j).intValue(), folder.getObjectID(), new Date(Long.MAX_VALUE));
            }

            MultipleResponse<CommonDeleteResponse> response = getClient().execute(MultipleRequest.create(requests));
            for (CommonDeleteResponse deleteResponse : response) {
                assertFalse("Delete Response should not have an error.", deleteResponse.hasError());
            }
        }

    }
}
