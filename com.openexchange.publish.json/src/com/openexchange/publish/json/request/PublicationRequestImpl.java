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

package com.openexchange.publish.json.request;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.publish.Publication;
import com.openexchange.publish.PublicationException;
import com.openexchange.publish.PublicationService;
import com.openexchange.publish.PublicationTarget;
import com.openexchange.publish.PublicationTargetDiscoveryService;
import com.openexchange.publish.json.EntityType;
import com.openexchange.publish.json.PublicationJSONErrorMessage;
import com.openexchange.publish.json.PublicationJSONException;
import com.openexchange.publish.json.PublicationParser;
import com.openexchange.publish.json.PublicationRequest;
import com.openexchange.publish.json.types.EntityMap;
import com.openexchange.tools.QueryStringPositionComparator;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link PublicationRequestImpl}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public class PublicationRequestImpl implements PublicationRequest {

    private static final EnumSet<Action> BODY_ACTIONS = EnumSet.of(Action.NEW, Action.UPDATE, Action.LIST, Action.DELETE);
    private static final EnumSet<Action> CONTAINS_PUBLICATION_BODY = EnumSet.of(Action.NEW, Action.UPDATE);
    
    
    private Action action;
    private PublicationTargetDiscoveryService discovery;
    private String body;
    private ServerSession session;
    
    private static Map<String, EntityType> entities = new EntityMap();
    
    private Map<String, Object> args = new HashMap<String, Object>();

    public PublicationRequestImpl(String action, JSONObject request, ServerSession session, PublicationTargetDiscoveryService discovery) throws PublicationJSONException, JSONException {
        try {
            this.action = Action.valueOf(action.toUpperCase());
        } catch (IllegalArgumentException x) {
            throw PublicationJSONErrorMessage.UNKNOWN_ACTION.create(action);
        }
        this.discovery = discovery;
        this.session = session;
        
        if(expectBody()) {
            this.body = request.getString("body");
        }
        
        parseArguments(request);
    }

    private void parseArguments(JSONObject request) throws JSONException {
        switch(action) {
        case GET: {
            args.put("id", request.getInt("id"));
            return;
        }
        case ALL: {
            if(request.has("entityModule")) {
                String module = request.getString("entityModule");
                EntityType entityType = entities.get(module);
                args.put("module", module);
                args.put("entityId", entityType.toEntityID(request));
            }
        }
        case LIST: {
            // Expects fall-through from ALL
            if(request.has("columns")) {
                args.put("basicColumns", request.getString("columns").split("\\s*,\\s*"));
            }
            Map<String, String[]> dynamicColumns = new HashMap<String, String[]>();
            for(String key : request.keySet()) {
                if(looksDynamic(key)) {
                    dynamicColumns.put(key, request.getString(key).split("\\s*,\\s*"));
                }
            }
            args.put("dynamicColumns", dynamicColumns);
            ArrayList<String> dynColOrder = new ArrayList<String>(dynamicColumns.keySet());
            sort(dynColOrder, request);
            args.put("dynamicColumnOrder", dynColOrder);
            return;
        }
        }
    }

    private void sort(ArrayList<String> dynColOrder, JSONObject request) throws JSONException {
        if(request.has("_query") || request.has("dynamicColumnOrder")) {
            String query = request.has("_query")  ? request.getString("_query") : request.getString("dynamicColumnOrder");
            QueryStringPositionComparator comparator = new QueryStringPositionComparator(query);
            Collections.sort(dynColOrder, comparator);
            return;
        }
        Collections.sort(dynColOrder);
    }

    private boolean looksDynamic(String key) {
        return key.contains(".");
    }

    private boolean expectBody() {
        return BODY_ACTIONS.contains(action);
    }

    public Action getAction() {
        return action;
    }

    public String[] getBasicColumns() {
        return (String[]) args.get("basicColumns");
    }

    public List<String> getDynamicColumnOrder() {
        return (List<String>) args.get("dynamicColumnOrder");
    }

    public Map<String, String[]> getDynamicColumns() {
        return (Map<String, String[]>) args.get("dynamicColumns");
    }

    public Publication getPublication() throws PublicationException, PublicationJSONException, JSONException {
        if(containsPublicationBody()) {
            return parsePublication();
        }
        return loadPublication();
    }


    private Publication parsePublication() throws PublicationException, PublicationJSONException, JSONException {
        PublicationParser parser = new PublicationParser(discovery);
        Publication publication = parser.parse(new JSONObject(body));
        publication.setContext(session.getContext());
        publication.setUserId(session.getUserId());
        if(publication.getTarget() == null && publication.getId() > 0) {
            PublicationTarget target = discovery.getTarget(publication.getContext(), publication.getId());
            publication.setTarget(target);
        }
        return publication;
    }

    private boolean containsPublicationBody() {
        return CONTAINS_PUBLICATION_BODY.contains(action);
    }

    public List<Publication> getPublications() throws JSONException, PublicationException {
        if(action == Action.ALL) {
            return loadForEntity();
        } else {
            JSONArray ids = new JSONArray(body);
            ArrayList<Publication> publications = new ArrayList<Publication>();
            for(int i = 0, size = ids.length(); i < size; i++) {
                Publication publication = loadPublication(ids.getInt(i));
                if(null != publication) {
                    publications.add(publication);
                }
            }
            return publications;
        }
    }

    private List<Publication> loadForEntity() throws PublicationException {
        String module = (String) args.get("module");
        Context context = session.getContext();
        String entityId = (String) args.get("entityId");
        List<Publication> publications = new LinkedList<Publication>();
        Collection<PublicationTarget> targetsForEntityType = discovery.getTargetsForEntityType(module);
        for(PublicationTarget target : targetsForEntityType) {
            if(target.isResponsibleFor(module)) {
                PublicationService publicationService = target.getPublicationService();
                Collection<Publication> allPublicationsForEntity = publicationService.getAllPublications(context, entityId);
                if(allPublicationsForEntity != null) {
                    publications.addAll(allPublicationsForEntity);
                }
            }
        }
        return publications;
    }

    private Publication loadPublication(int publicationId) throws PublicationException {
        Context context = session.getContext();
        PublicationTarget target = discovery.getTarget(context, publicationId);
        if(action == Action.DELETE) {
            Publication publication = new Publication();
            publication.setContext(context);
            publication.setTarget(target);
            publication.setId(publicationId);
            return publication;
        }
        return target.getPublicationService().load(context, publicationId);
    }
    
    private Publication loadPublication() throws PublicationException {
        int publicationId = (Integer) args.get("id");
        return loadPublication(publicationId);
    }


}
