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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.ajax.find.calendar;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import com.openexchange.ajax.find.AbstractFindTest;
import com.openexchange.ajax.find.PropDocument;
import com.openexchange.ajax.find.actions.QueryRequest;
import com.openexchange.ajax.find.actions.QueryResponse;
import com.openexchange.find.Document;
import com.openexchange.find.Module;
import com.openexchange.find.SearchResult;
import com.openexchange.find.facet.ActiveFacet;
import com.openexchange.groupware.calendar.TimeTools;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.test.CalendarTestManager;

/**
 * {@link CalendarFindTest}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class CalendarFindTest extends AbstractFindTest {

    protected CalendarTestManager manager;
    protected Random random;

    /**
     * Initializes a new {@link CalendarFindTest}.
     *
     * @param name The test name
     */
    public CalendarFindTest(String name) {
        super(name);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        random = new Random();
        manager = new CalendarTestManager(client);
    }

    @Override
    public void tearDown() throws Exception {
        manager.cleanUp();
        super.tearDown();
    }

    /**
     * Creates a new, random UUID string.
     *
     * @return The UUID string
     */
    protected static String randomUID() {
        return UUID.randomUUID().toString();
    }

    /**
     * Creates a new, random appointment instance containing some basic random data, with the folder ID being set to the private folder ID.
     * The appointment is not created at the server automatically.
     *
     * @return The appointment
     */
    protected Appointment randomAppointment() throws Exception {
        Appointment app = new Appointment();
        app.setTitle(randomUID());
        app.setLocation(randomUID());
        app.setNote(randomUID());
        app.setStartDate(TimeTools.D("Next friday at 10:15"));
        app.setEndDate(TimeTools.D("Next friday at 11:30"));
        app.setParentFolderID(client.getValues().getPrivateAppointmentFolder());
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
        QueryRequest queryRequest = new QueryRequest(0, Integer.MAX_VALUE, facets, Module.CALENDAR.getIdentifier());
        QueryResponse queryResponse = client.execute(queryRequest);
        SearchResult result = queryResponse.getSearchResult();
        List<PropDocument> propDocuments = new ArrayList<PropDocument>();
        List<Document> documents = result.getDocuments();
        for (Document document : documents) {
            propDocuments.add((PropDocument) document);
        }
        return propDocuments;
    }

    /**
     * Searches the supplied list of property documents matching the supplied value in one of its properties.
     *
     * @param documents The documents to check
     * @param property The property name to check
     * @param value The value to check
     * @return The found document, or <code>null</code> if not found
     */
    protected static PropDocument findByProperty(List<PropDocument> documents, String property, String value) {
        for (PropDocument propDocument : documents) {
            if (value.equals(propDocument.getProps().get(property))) {
                return propDocument;
            }
        }
        return null;
    }

    /**
     * Gets a random substring of the supplied value, using a minimum length of 4.
     *
     * @param value The value to get the substring from
     * @return The substring
     */
    protected String randomSubstring(String value) {
        return randomSubstring(value, 4);
    }

    /**
     * Gets a random substring of the supplied value.
     *
     * @param value The value to get the substring from
     * @return The substring
     */
    protected String randomSubstring(String value, int minLength) {
        if (minLength >= value.length()) {
            fail(value + " is too short to get a substring from");
        }
        int start = random.nextInt(value.length() - minLength);
        int stop = start + minLength + random.nextInt(value.length() - start - minLength);
        return value.substring(start, stop);
    }

}
