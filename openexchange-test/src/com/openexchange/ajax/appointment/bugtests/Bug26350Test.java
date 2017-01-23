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

package com.openexchange.ajax.appointment.bugtests;

import static com.openexchange.groupware.calendar.TimeTools.D;
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
                chunkIds.add(insert.getObjectID());
            }
            ids.add(chunkIds);
        }

        for (List<Integer> chunkIds : ids) {
            DeleteRequest[] requests = new DeleteRequest[chunkIds.size()];

            for (int j = 0; j < chunkIds.size(); j++) {
                requests[j] = new DeleteRequest(chunkIds.get(j), folder.getObjectID(), new Date(Long.MAX_VALUE));
            }

            MultipleResponse<CommonDeleteResponse> response = getClient().execute(MultipleRequest.create(requests));
            for (CommonDeleteResponse deleteResponse : response) {
                assertFalse("Delete Response should not have an error.", deleteResponse.hasError());
            }
        }

    }
}
