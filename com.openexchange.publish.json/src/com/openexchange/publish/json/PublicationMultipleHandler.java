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

import static com.openexchange.publish.json.MultipleHandlerTools.wrapThrowable;
import static com.openexchange.publish.json.PublicationJSONErrorMessage.MISSING_PARAMETER;
import static com.openexchange.publish.json.PublicationJSONErrorMessage.UNKNOWN_ACTION;
import static com.openexchange.publish.json.PublicationJSONErrorMessage.UNKOWN_ENTITY_MODULE;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.fields.ResponseFields;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.multiple.MultipleHandler;
import com.openexchange.publish.Publication;
import com.openexchange.publish.PublicationException;
import com.openexchange.publish.PublicationService;
import com.openexchange.publish.PublicationTarget;
import com.openexchange.publish.PublicationTargetDiscoveryService;
import com.openexchange.tools.QueryStringPositionComparator;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link PublicationMultipleHandler}
 * 
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class PublicationMultipleHandler implements MultipleHandler {

    private PublicationTargetDiscoveryService discovery;

    private Map<String, EntityType> entities;

    public PublicationMultipleHandler(PublicationTargetDiscoveryService discovery, Map<String, EntityType> entities) {
        this.discovery = discovery;
        this.entities = entities;
    }

    public void close() {

    }

    public Date getTimestamp() {
        return null;
    }
    
    public static final Set<String> ACTIONS_REQUIRING_BODY = new HashSet<String>() {{
        
        add("new");
        add("update");
        add("delete");
        add("list");
        
    }};


    public Object performRequest(String action, JSONObject request, ServerSession session) throws AbstractOXException, JSONException {
        try {
            if (null == action) {
                throw MISSING_PARAMETER.create("action");
            } else if (action.equals("new")) {
                return createPublication(request, session);
            } else if (action.equals("update")) {
                return updatePublication(request, session);
            } else if (action.equals("delete")) {
                return deletePublication(request, session);
            } else if (action.equals("get")) {
                return loadPublication(request, session);
            } else if (action.equals("all")) {
                return loadAllPublicationsForEntity(request, session);
            } else if (action.equals("list")) {
                return listPublications(request, session);
            } else {
                throw UNKNOWN_ACTION.create(action);
            }
        } catch (AbstractOXException x) {
            throw x;
        } catch (JSONException x) {
            throw x;
        } catch (Throwable t) {
            throw wrapThrowable(t);
        }
    }

    private Object listPublications(JSONObject request, ServerSession session) throws JSONException, PublicationException, PublicationJSONException {
        JSONArray ids = request.getJSONArray(ResponseFields.DATA);
        Context context = session.getContext();
        List<Publication> publications = new ArrayList<Publication>(ids.length());
        for (int i = 0, size = ids.length(); i < size; i++) {
            int id = ids.getInt(i);
            PublicationService publicationService = discovery.getTarget(context, id).getPublicationService();
            Publication publication = publicationService.load(context, id);
            if (publication != null) {
                publications.add(publication);
            }
        }
        String[] basicColumns = getBasicColumns(request);
        Map<String, String[]> dynamicColumns = getDynamicColumns(request);
        List<String> dynamicColumnOrder = getDynamicColumnOrder(request);

        return createList(publications, basicColumns, dynamicColumns, dynamicColumnOrder);
    }

    private Object loadAllPublicationsForEntity(JSONObject request, ServerSession session) throws PublicationJSONException, JSONException, PublicationException {
        if (!request.has("entityModule")) {
            throw MISSING_PARAMETER.create("entityModule");
        }
        String module = request.optString("entityModule");
        EntityType entityType = entities.get(module);
        if (null == entityType) {
            throw UNKOWN_ENTITY_MODULE.create(module);
        }
        String entityId = entityType.toEntityID(request);
        Context context = session.getContext();
        List<Publication> publications = loadAllPublicationsForEntity(context, entityId, module);

        String[] basicColumns = getBasicColumns(request);
        Map<String, String[]> dynamicColumns = getDynamicColumns(request);
        List<String> dynamicColumnOrder = getDynamicColumnOrder(request);

        return createList(publications, basicColumns, dynamicColumns, dynamicColumnOrder);
    }

    private static final Set<String> KNOWN_PARAMS = new HashSet<String>() {

        {
            add("entityModule");
            add("columns");
            add("session");
            add("action");
        }
    };


    private Map<String, String[]> getDynamicColumns(JSONObject request) throws JSONException {
        List<String> identifiers = getDynamicColumnOrder(request);
        Map<String, String[]> dynamicColumns = new HashMap<String, String[]>();
        for (String identifier : identifiers) {
            String columns = request.optString(identifier);
            if (columns != null && ! columns.equals("")) {
                dynamicColumns.put(identifier, columns.split("\\s*,\\s*"));
            }
        }
        return dynamicColumns;
    }

    private List<String> getDynamicColumnOrder(JSONObject request) throws JSONException {
        if (request.has("dynamicColumnPlugins")) {
            return Arrays.asList(request.getString("dynamicColumnPlugins").split("\\s*,\\s*"));
        }

        List<String> dynamicColumnIdentifiers = new ArrayList<String>();
        for (String paramName : request.keySet()) {
            if (!KNOWN_PARAMS.contains(paramName) && paramName.contains(".")) {
                dynamicColumnIdentifiers.add(paramName);
            }
        }
        String order = request.optString("__query");
        Collections.sort(dynamicColumnIdentifiers, new QueryStringPositionComparator(order));
        return dynamicColumnIdentifiers;
    }

    private String[] getBasicColumns(JSONObject request) throws JSONException {
        String columns = request.optString("columns");
        if (columns == null || columns.equals("")) {
            return new String[] { "id", "entityId", "entityModule", "target" };
        }
        return columns.split("\\s*,\\s*");
    }

    private List<Publication> loadAllPublicationsForEntity(Context context, String entityId, String module) throws PublicationException {
        List<Publication> publications = new LinkedList<Publication>();
        Collection<PublicationTarget> targetsForEntityType = discovery.getTargetsForEntityType(module);
        for (PublicationTarget target : targetsForEntityType) {
            if (target.isResponsibleFor(module)) {
                PublicationService publicationService = target.getPublicationService();
                Collection<Publication> allPublicationsForEntity = publicationService.getAllPublications(context, entityId);
                if (allPublicationsForEntity != null) {
                    publications.addAll(allPublicationsForEntity);
                }
            }
        }
        return publications;
    }

    private Object loadPublication(JSONObject request, ServerSession session) throws JSONException, AbstractOXException {
        int id = request.getInt("id");
        String target = request.optString("target");
        Context context = session.getContext();
        Publication publication = loadPublication(id, context, target);
        return createResponse(publication);
    }

    private Publication loadPublication(int id, Context context, String target) throws AbstractOXException {
        PublicationService service = null;
        if (target != null && !target.equals("")) {
            service = discovery.getTarget(target).getPublicationService();
        } else {
            service = discovery.getTarget(context, id).getPublicationService();
        }
        return service.load(context, id);
    }

    private Object deletePublication(JSONObject request, ServerSession session) throws PublicationException, JSONException {
        JSONArray ids = request.getJSONArray(ResponseFields.DATA);
        Context context = session.getContext();
        for (int i = 0, size = ids.length(); i < size; i++) {
            int id = ids.getInt(i);
            PublicationService publisher = discovery.getTarget(context, id).getPublicationService();
            Publication publication = new Publication();
            publication.setContext(context);
            publication.setId(id);
            publication.setUserId(session.getUserId());
            publisher.delete(publication);
        }
        return 1;
    }

    private Object updatePublication(JSONObject request, ServerSession session) throws JSONException, PublicationException, PublicationJSONException {
        Publication publication = getPublication(request, session);

        publication.update();

        return 1;

    }

    private Object createPublication(JSONObject request, ServerSession session) throws PublicationException, PublicationJSONException, JSONException {
        Publication publication = getPublication(request, session);
        publication.setId(-1);

        publication.create();

        return publication.getId();

    }

    private Object createList(List<Publication> publications, String[] basicColumns, Map<String, String[]> dynamicColumns, List<String> dynamicColumnOrder) throws PublicationJSONException, JSONException {
        JSONArray rows = new JSONArray();
        PublicationWriter writer = new PublicationWriter();
        for (Publication publication : publications) {
            JSONArray row = writer.writeArray(
                publication,
                basicColumns,
                dynamicColumns,
                dynamicColumnOrder,
                publication.getTarget().getFormDescription());
            rows.put(row);
        }
        return rows;
    }

    private Object createResponse(Publication publication) throws JSONException, PublicationJSONException {
        JSONObject asJson = new PublicationWriter().write(publication);
        return asJson;
    }

    private Publication getPublication(JSONObject request, ServerSession session) throws JSONException, PublicationException, PublicationJSONException {
        JSONObject object = request.getJSONObject(ResponseFields.DATA);
        Publication publication = new PublicationParser(discovery).parse(object);
        publication.setUserId(session.getUserId());
        publication.setContext(session.getContext());
        if (publication.getTarget() == null && publication.getId() > 0) {
            PublicationTarget target = discovery.getTarget(publication.getContext(), publication.getId());
            publication.setTarget(target);
        }
        return publication;
    }

}
