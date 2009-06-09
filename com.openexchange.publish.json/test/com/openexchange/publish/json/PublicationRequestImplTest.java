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

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.publish.Publication;
import com.openexchange.publish.PublicationException;
import com.openexchange.publish.PublicationTarget;
import com.openexchange.publish.SimPublicationService;
import com.openexchange.publish.SimPublicationTargetDiscoveryService;
import com.openexchange.publish.json.request.PublicationRequestImpl;
import com.openexchange.tools.session.SimServerSession;
import junit.framework.TestCase;

/**
 * {@link PublicationRequestImplTest}
 * 
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class PublicationRequestImplTest extends TestCase {

    private SimServerSession session;

    private SimPublicationTargetDiscoveryService discoveryService;

    @Override
    protected void setUp() throws Exception {
        session = new SimServerSession(1337, 23);

        discoveryService = new SimPublicationTargetDiscoveryService();

        PublicationTarget target = new PublicationTarget();
        target.setModule("contacts");
        target.setId("com.openexchange.publish.test");

        SimPublicationService publicationService = new SimPublicationService() {

            @Override
            public boolean knows(Context ctx, int publicationId) {
                return publicationId == 12;
            }

            @Override
            public Publication load(Context ctx, int publicationId) {
                Publication publication = new Publication();
                publication.setContext(ctx);
                publication.setId(publicationId);
                publication.getConfiguration().put("loaded", true);
                return publication;
            }
            
            @Override
            public Collection<Publication> getAllPublications(Context ctx, String entityId) {
                Publication publication = new Publication();
                publication.setId(23);
                publication.setContext(ctx);
                publication.getConfiguration().put("loaded", true);
                return Arrays.asList(publication);
            }
        };

        target.setPublicationService(publicationService);
        publicationService.setTarget(target);
        discoveryService.addTarget(target);

    }

    public void testGetPublicationFromJSONObject() throws JSONException, PublicationJSONException, PublicationException {
        JSONObject request = new JSONObject();
        request.put("action", "new");

        JSONObject publication = getPublicationJSONObject();
        request.put("body", publication);

        PublicationRequest req = new PublicationRequestImpl("new", request, session, discoveryService);

        assertEquals("Expected action new", PublicationRequest.Action.NEW, req.getAction());

        Publication pub = req.getPublication();

        assertNotNull("Publication was null!", pub);
        assertEquals("Wrong target", "com.openexchange.publish.test", pub.getTarget().getId());
        assertEquals("Wrong context", 1337, pub.getContext().getContextId());
        assertEquals("Wrong user", 23, pub.getUserId());

    }

    public void testFallBackToDiscoveredTarget() throws JSONException, PublicationJSONException, PublicationException {
        JSONObject request = new JSONObject();
        request.put("action", "update");

        JSONObject publication = getPublicationJSONObject();
        publication.put("id", 12);
        publication.remove("target");
        request.put("body", publication);

        PublicationRequest req = new PublicationRequestImpl("update", request, session, discoveryService);
        Publication pub = req.getPublication();

        assertEquals("Target was not discovered", "com.openexchange.publish.test", pub.getTarget().getId());
    }

    public void testLoadPublication() throws JSONException, PublicationJSONException, PublicationException {
        JSONObject request = new JSONObject();
        request.put("action", "get");
        request.put("id", 12);

        PublicationRequest req = new PublicationRequestImpl("get", request, session, discoveryService);

        Publication pub = req.getPublication();

        assertEquals("Wrong context", 1337, pub.getContext().getContextId());
        assertEquals("Wrong id", 12, pub.getId());

    }

    public void testLoadPublicationList() throws JSONException, PublicationJSONException, PublicationException {
        JSONObject request = new JSONObject();
        request.put("body", "[12]");
    
        PublicationRequest req = new PublicationRequestImpl("list", request, session, discoveryService);
        
        List<Publication> publications = req.getPublications();
        
        assertNotNull(publications);
        
        assertEquals(1, publications.size());
        Publication pub = publications.get(0);
        assertEquals("Wrong context", 1337, pub.getContext().getContextId());
        assertEquals("Wrong id", 12, pub.getId());
        assertTrue("Was not loaded", (Boolean) pub.getConfiguration().get("loaded"));
    }

    public void testParsePublicationList() throws JSONException, PublicationJSONException, PublicationException {
        JSONObject request = new JSONObject();
        request.put("body", "[12]");
    
        PublicationRequest req = new PublicationRequestImpl("delete", request, session, discoveryService);
        
        List<Publication> publications = req.getPublications();
        
        assertNotNull(publications);
        
        assertEquals(1, publications.size());
        Publication pub = publications.get(0);
        assertEquals("Wrong context", 1337, pub.getContext().getContextId());
        assertEquals("Wrong id", 12, pub.getId());
        assertFalse("Was loaded", (Boolean) pub.getConfiguration().containsKey("loaded"));
    }

    public void testLoadEntityPublicationList() throws JSONException, PublicationJSONException, PublicationException {
        JSONObject request = new JSONObject();
        request.put("entityModule", "contacts");
        request.put("folder", 13);
        
        PublicationRequest req = new PublicationRequestImpl("all", request, session, discoveryService);
        
        List<Publication> publications = req.getPublications();
        
        assertNotNull(publications);
        
        assertEquals(1, publications.size());
        Publication pub = publications.get(0);
        assertEquals("Wrong context", 1337, pub.getContext().getContextId());
        assertEquals("Wrong id", 23, pub.getId());
        assertTrue("Was not loaded", (Boolean) pub.getConfiguration().get("loaded"));
    }
    
    public void testColumnParameters() throws JSONException, PublicationJSONException {
        JSONObject request = new JSONObject();
        request.put("columns", "id,entityModule,entity");
        
        PublicationRequest req = new PublicationRequestImpl("all", request, session, discoveryService);
        
        String[] basicColumns = req.getBasicColumns();
        assertNotNull(basicColumns);
        assertEquals(3, basicColumns.length);
        assertEquals("id", basicColumns[0]);
        assertEquals("entityModule", basicColumns[1]);
        assertEquals("entity", basicColumns[2]);
        
    }
    
    public void testDynamicColumns() throws JSONException, PublicationJSONException {
        JSONObject request = new JSONObject();
        request.put("com.openexchange.plugin1", "bla,blupp,gnoehoe");
        request.put("com.openexchange.plugin2", "foo,bar,baz");
        
        PublicationRequest req = new PublicationRequestImpl("all", request, session, discoveryService);
        
        Map<String, String[]> dynamicColumns = req.getDynamicColumns();
        assertNotNull(dynamicColumns);
        
        assertEquals(2, dynamicColumns.size());
        
        String[] plugin1 = dynamicColumns.get("com.openexchange.plugin1");
        assertNotNull(plugin1);
        assertEquals(3, plugin1.length);
        assertEquals("bla", plugin1[0]);
        assertEquals("blupp", plugin1[1]);
        assertEquals("gnoehoe", plugin1[2]);
        
        String[] plugin2 = dynamicColumns.get("com.openexchange.plugin2");
        assertNotNull(plugin2);
        assertEquals(3, plugin2.length);
        assertEquals("foo", plugin2[0]);
        assertEquals("bar", plugin2[1]);
        assertEquals("baz", plugin2[2]);
        
    }
    
    public void testAlphabeticalOrder() throws PublicationJSONException, JSONException {
        JSONObject request = new JSONObject();
        request.put("com.openexchange.plugin1", "bla,blupp,gnoehoe");
        request.put("com.openexchange.plugin2", "foo,bar,baz");
        
        PublicationRequest req = new PublicationRequestImpl("all", request, session, discoveryService);
        
        List<String> dynamicColumnOrder = req.getDynamicColumnOrder();
        
        assertNotNull(dynamicColumnOrder);
        assertEquals("com.openexchange.plugin1", dynamicColumnOrder.get(0));
        assertEquals("com.openexchange.plugin2", dynamicColumnOrder.get(1));
        
    }
    
    public void testQueryStringOrder() throws PublicationJSONException, JSONException {
        JSONObject request = new JSONObject();
        request.put("com.openexchange.plugin1", "bla,blupp,gnoehoe");
        request.put("com.openexchange.plugin2", "foo,bar,baz");
        request.put("_query", "?session=2&com.openexchange.plugin2=foo,bar,baz&com.openexchange.plugin1=bla,blupp,gnoehoe");
        
        PublicationRequest req = new PublicationRequestImpl("all", request, session, discoveryService);
        
        List<String> dynamicColumnOrder = req.getDynamicColumnOrder();
        
        assertNotNull(dynamicColumnOrder);
        assertEquals("com.openexchange.plugin2", dynamicColumnOrder.get(0));
        assertEquals("com.openexchange.plugin1", dynamicColumnOrder.get(1));
        
    }
    
    public void testExplicitOrder() throws PublicationJSONException, JSONException {
        JSONObject request = new JSONObject();
        request.put("com.openexchange.plugin1", "bla,blupp,gnoehoe");
        request.put("com.openexchange.plugin2", "foo,bar,baz");
        request.put("dynamicColumnOrder", "com.openexchange.plugin2,com.openexchange.plugin1");
        
        PublicationRequest req = new PublicationRequestImpl("all", request, session, discoveryService);
        
        List<String> dynamicColumnOrder = req.getDynamicColumnOrder();
        
        assertNotNull(dynamicColumnOrder);
        assertEquals("com.openexchange.plugin2", dynamicColumnOrder.get(0));
        assertEquals("com.openexchange.plugin1", dynamicColumnOrder.get(1));
        
    }

    private JSONObject getPublicationJSONObject() throws JSONException {
        JSONObject publication = new JSONObject();
        publication.put("entityModule", "contacts");

        JSONObject entity = new JSONObject();
        entity.put("folder", 12);
        publication.put("entity", entity);

        publication.put("target", "com.openexchange.publish.test");
        return publication;
    }
}
