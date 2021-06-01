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

package com.openexchange.ajax.find.calendar;

import java.util.List;
import java.util.Map;
import java.util.Random;
import org.junit.Before;
import com.openexchange.ajax.find.AbstractFindTest;
import com.openexchange.ajax.find.PropDocument;
import com.openexchange.find.Module;
import com.openexchange.find.facet.ActiveFacet;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.test.CalendarTestManager;
import com.openexchange.test.common.groupware.calendar.TimeTools;

/**
 * {@link CalendarFindTest}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class CalendarFindTest extends AbstractFindTest {

    protected CalendarTestManager manager;
    @SuppressWarnings("hiding")
    protected Random random;
    protected CalendarTestManager manager2;

    /**
     * Initializes a new {@link CalendarFindTest}.
     *
     * @param name The test name
     */
    public CalendarFindTest() {
        super();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        random = new Random();
        manager = new CalendarTestManager(getClient());
        manager2 = new CalendarTestManager(client2);
    }

    /**
     * Creates a new, random appointment instance containing some basic random data, with the folder ID being set to the private folder ID.
     * The appointment is not created at the server automatically.
     *
     * @return The appointment
     */
    protected Appointment randomPrivateAppointment() throws Exception {
        Appointment app = new Appointment();
        app.setTitle(randomUID());
        app.setLocation(randomUID());
        app.setNote(randomUID());
        app.setStartDate(TimeTools.D("Next friday at 10:15"));
        app.setEndDate(TimeTools.D("Next friday at 11:30"));
        app.setParentFolderID(getClient().getValues().getPrivateAppointmentFolder());
        app.setIgnoreConflicts(true);
        return app;
    }

    /**
     * Creates a new, random appointment instance containing some basic random data, with the folder ID being set to the given one.
     * The appointment is not created at the server automatically.
     *
     * @param parentFolder The parent folder id
     * @return The appointment
     */
    protected Appointment randomAppointment(int parentFolder) {
        Appointment app = new Appointment();
        app.setTitle(randomUID());
        app.setLocation(randomUID());
        app.setNote(randomUID());
        app.setStartDate(TimeTools.D("Next friday at 10:15"));
        app.setEndDate(TimeTools.D("Next friday at 11:30"));
        app.setParentFolderID(parentFolder);
        app.setIgnoreConflicts(true);
        return app;
    }

    /**
     * Performs a query request using the supplied active facets.
     *
     * @param facets The active facets
     * @return The found documents
     * @throws Exception
     */
    protected List<PropDocument> query(List<ActiveFacet> facets) throws Exception {
        return query(Module.CALENDAR, facets);
    }

    /**
     * Performs a query request using the supplied active facets.
     *
     * @param facets The active facets
     * @param options The options
     * @return The found documents
     * @throws Exception
     */
    protected List<PropDocument> query(List<ActiveFacet> facets, Map<String, String> options) throws Exception {
        return query(Module.CALENDAR, facets, options);
    }
}
