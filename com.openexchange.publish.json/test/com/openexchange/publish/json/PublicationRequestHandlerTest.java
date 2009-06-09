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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.publish.json;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.publish.Publication;
import com.openexchange.publish.PublicationException;
import com.openexchange.publish.PublicationTarget;
import com.openexchange.publish.SimPublicationService;
import junit.framework.TestCase;

/**
 * {@link PublicationRequestHandlerTest}
 * 
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class PublicationRequestHandlerTest extends TestCase {

    private PublicationTarget target;

    private SimPublicationService publicationService;

    public void setUp() {
        this.target = new PublicationTarget();
        target.setId("com.openexchange.publish.test");
        publicationService = new SimPublicationService();
        publicationService.setNewId(23);
        publicationService.setTarget(target);
        target.setPublicationService(publicationService);

    }

    public void testCreate() throws PublicationException, PublicationJSONException, JSONException {

        SimPublicationRequest req = new SimPublicationRequest();

        req.setAction(PublicationRequest.Action.NEW);

        Publication publication = new Publication();
        publication.setId(-1);
        publication.setTarget(target);

        req.setPublication(publication);

        PublicationResponse response = new PublicationRequestHandler().handle(req);

        assertNotNull(response);

        assertEquals("Didn't receive new id as data segment", 23, response.getJSONData());

    }

    public void testUpdate() throws PublicationException, PublicationJSONException, JSONException {
        SimPublicationRequest req = new SimPublicationRequest();

        req.setAction(PublicationRequest.Action.UPDATE);

        Publication publication = new Publication();
        publication.setId(12);
        publication.setTarget(target);

        req.setPublication(publication);

        PublicationResponse response = new PublicationRequestHandler().handle(req);

        assertNotNull(response);

        assertSuccess(response);
        assertEquals("Expected id 12 to be saved", 12, publicationService.getUpdatedId());
    }

    private void assertSuccess(PublicationResponse response) {
        assertEquals("Didn't receive success result as data segment", 1, response.getJSONData());
    }

    public void testDelete() throws PublicationException, PublicationJSONException, JSONException {
        SimPublicationRequest req = new SimPublicationRequest();

        req.setAction(PublicationRequest.Action.DELETE);
        req.setPublications(getPublicationsWithIDs(12, 13, 14, 15));

        PublicationResponse response = new PublicationRequestHandler().handle(req);

        assertNotNull(response);
        assertSuccess(response);

        assertDeleted(12, 13, 14, 15);
    }

    private List<Publication> getPublicationsWithIDs(int... ids) {
        ArrayList<Publication> list = new ArrayList<Publication>(ids.length);
        for (int id : ids) {
            Publication publication = new Publication();
            publication.setId(id);
            publication.setTarget(target);
            list.add(publication);
        }
        return list;
    }

    private void assertDeleted(int... ids) {
        Set<Integer> deleted = publicationService.getDeletedIDs();
        for (int id : ids) {
            assertTrue("Did not delete " + id, deleted.remove(id));
        }
    }

    public void testGet() throws PublicationException, JSONException, PublicationJSONException {
        SimPublicationRequest req = new SimPublicationRequest();

        req.setAction(PublicationRequest.Action.GET);

        Publication publication = new Publication();
        publication.setId(12);
        publication.setTarget(target);

        req.setPublication(publication);

        PublicationResponse response = new PublicationRequestHandler().handle(req);

        assertNotNull(response);

        JSONObject publicationAsJSONObject = (JSONObject) response.getJSONData();

        assertEquals("expected (at least) the id of the publication", 12, publicationAsJSONObject.get("id"));
        // Rest is assured by writer test
    }

    public void testAll() throws PublicationException, PublicationJSONException, JSONException {
        SimPublicationRequest req = new SimPublicationRequest();

        req.setAction(PublicationRequest.Action.ALL);
        req.setPublications(getPublicationsWithIDs(12, 13, 14, 15));
        req.setColumns("id");

        PublicationResponse response = new PublicationRequestHandler().handle(req);

        assertNotNull(response);

        JSONArray rows = (JSONArray) response.getJSONData();
        
        assertEquals(4, rows.length());
        // Rest is assured by writer test
    }

    public void testList() throws PublicationException, PublicationJSONException, JSONException {
        SimPublicationRequest req = new SimPublicationRequest();

        req.setAction(PublicationRequest.Action.LIST);
        req.setPublications(getPublicationsWithIDs(12, 13, 14, 15));
        req.setColumns("id");

        PublicationResponse response = new PublicationRequestHandler().handle(req);

        assertNotNull(response);

        JSONArray rows = (JSONArray) response.getJSONData();
        
        assertEquals(4, rows.length());
        // Rest is assured by writer test
        
    }

}
