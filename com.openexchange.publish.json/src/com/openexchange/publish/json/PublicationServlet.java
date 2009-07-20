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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.publish.Publication;
import com.openexchange.publish.PublicationException;
import com.openexchange.publish.PublicationService;
import com.openexchange.publish.PublicationTarget;
import com.openexchange.publish.PublicationTargetDiscoveryService;
import com.openexchange.publish.json.types.EntityMap;
import com.openexchange.tools.QueryStringPositionComparator;
import com.openexchange.tools.exceptions.LoggingLogic;
import com.openexchange.tools.session.ServerSession;

import static com.openexchange.publish.json.PublicationJSONErrorMessage.*;

/**
 * {@link PublicationServlet}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public class PublicationServlet extends AbstractPublicationServlet{

    private static final Log LOG = LogFactory.getLog(PublicationServlet.class);
    private static final LoggingLogic LL = LoggingLogic.getLoggingLogic(PublicationServlet.class, LOG);

    private static PublicationTargetDiscoveryService discovery = null;
    
    private static final Map<String, EntityType> entities = new EntityMap();
    
    public static void setPublicationTargetDiscoveryService(PublicationTargetDiscoveryService service) {
        discovery = service;
    }
    
    
    @Override
    protected Log getLog() {
        return LOG;
    }

    @Override
    protected LoggingLogic getLoggingLogic() {
        return LL;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        handleRequest(req, resp);
    }
    
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        handleRequest(req, resp);
    }
    
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        handleRequest(req, resp);
    }
    
    protected void handleRequest(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            String action = req.getParameter("action");
            if(null == action) {
                throw MISSING_PARAMETER.create("action");
            } else if (action.equals("new")) {
                createPublication(req, resp);
            } else if (action.equals("update")) {
                updatePublication(req, resp);
            } else if (action.equals("delete")) {
                deletePublication(req, resp);
            } else if (action.equals("get")) {
                loadPublication(req, resp);
            } else if (action.equals("all")) {
                loadAllPublicationsForEntity(req, resp);
            } else if (action.equals("list")) {
                listPublications(req, resp);
            } else {
                throw UNKNOWN_ACTION.create(action);
            }
        } catch (AbstractOXException x) {
            writeOXException(x, resp);
        } catch (Throwable t) {
            writeOXException(wrapThrowable(t), resp);
        }
    }

    private void createPublication(HttpServletRequest req, HttpServletResponse resp) throws JSONException, IOException, PublicationException, PublicationJSONException {
        ServerSession session = getSessionObject(req);
        Publication publication = getPublication(req, session);
        publication.setId(-1);
        
        publication.create();
        
        writeData(publication.getId(), resp);
        
    }

    private Publication getPublication(HttpServletRequest req, ServerSession session) throws JSONException, IOException, PublicationException, PublicationJSONException {
        JSONObject object = new JSONObject(getBody(req));
        Publication publication = new PublicationParser(discovery).parse(object);
        publication.setUserId(session.getUserId());
        publication.setContext(session.getContext());
        if(publication.getTarget() == null && publication.getId() > 0) {
            PublicationTarget target = discovery.getTarget(publication.getContext(), publication.getId());
            publication.setTarget(target);
        }
        return publication;
    }


    private void updatePublication(HttpServletRequest req, HttpServletResponse resp) throws JSONException, IOException, PublicationException, PublicationJSONException {
        ServerSession session = getSessionObject(req);
        Publication publication = getPublication(req, session);
        
        publication.update();

        writeData(1, resp);
    }


    private void deletePublication(HttpServletRequest req, HttpServletResponse resp) throws JSONException, IOException, PublicationException {
        JSONArray ids = new JSONArray(getBody(req));
        ServerSession session = getSessionObject(req);
        
        Context context = session.getContext();
        int userId = session.session.getUserId();
        
        for(int i = 0, size = ids.length(); i < size; i++) {
            int id = ids.getInt(i);
            PublicationService publisher = discovery.getTarget(context, id).getPublicationService();
            Publication publication = new Publication();
            publication.setContext(context);
            publication.setId(id);
            publication.setUserId(userId);
            publisher.delete(publication);
        }
        writeData(1, resp);

    }


    private void loadPublication(HttpServletRequest req, HttpServletResponse resp) throws AbstractOXException, JSONException {
        int id = Integer.parseInt(req.getParameter("id"));
        String target = req.getParameter("target");
        Context context = getSessionObject(req).getContext();
        Publication publication = loadPublication(id, context, target);
        writePublication(publication, resp);
    }


    private void writePublication(Publication publication, HttpServletResponse resp) throws JSONException, PublicationJSONException {
        JSONObject object = new PublicationWriter().write(publication);
        writeData(object, resp);
    }

    private Publication loadPublication(int id, Context context, String target) throws AbstractOXException {
        PublicationService service = null;
        if(target != null) {
            service = discovery.getTarget(target).getPublicationService();
        } else {
            service = discovery.getTarget(context, id).getPublicationService();
        }
        return service.load(context, id);
    }


    // Robustness!
    private void loadAllPublicationsForEntity(HttpServletRequest req, HttpServletResponse resp) throws PublicationJSONException, PublicationException, JSONException {
        if(null == req.getParameter("entityModule")) {
            throw MISSING_PARAMETER.create("entityModule");
        }
        String module = req.getParameter("entityModule");
        EntityType entityType = entities.get(module);
        if(null == entityType) {
            throw UNKOWN_ENTITY_MODULE.create(module);
        }
        String entityId = entityType.toEntityID(req);
        Context context = getSessionObject(req).getContext();
        List<Publication> publications = loadAllPublicationsForEntity(context, entityId, module);
    
        String[] basicColumns = getBasicColumns(req);
        Map<String, String[]> dynamicColumns = getDynamicColumns(req);
        List<String> dynamicColumnOrder = getDynamicColumnOrder(req);
        
        writePublications(publications, basicColumns, dynamicColumns, dynamicColumnOrder, resp);
    }


    private List<Publication> loadAllPublicationsForEntity(Context context, String entityId, String module) throws PublicationException {
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


    private void listPublications(HttpServletRequest req, HttpServletResponse resp) throws JSONException, IOException, PublicationException, PublicationJSONException {
        JSONArray ids = new JSONArray(getBody(req));
        Context context = getSessionObject(req).getContext();
        List<Publication> publications = new ArrayList<Publication>(ids.length());
        for(int i = 0, size = ids.length(); i < size; i++) {
            int id = ids.getInt(i);
            PublicationService publicationService = discovery.getTarget(context, id).getPublicationService();
            Publication publication = publicationService.load(context, id);
            if(publication != null) {
                publications.add(publication);
            }
        }
        String[] basicColumns = getBasicColumns(req);
        Map<String, String[]> dynamicColumns = getDynamicColumns(req);
        List<String> dynamicColumnOrder = getDynamicColumnOrder(req);
        
        writePublications(publications, basicColumns, dynamicColumns, dynamicColumnOrder, resp);
    }

    private void writePublications(List<Publication> allPublications, String[] basicColumns, Map<String, String[]> dynamicColumns, List<String> dynamicColumnOrder, HttpServletResponse resp) throws PublicationJSONException, JSONException {
        JSONArray rows = new JSONArray();
        PublicationWriter writer = new PublicationWriter();
        for (Publication publication : allPublications) {
            JSONArray row = writer.writeArray(publication, basicColumns, dynamicColumns, dynamicColumnOrder, publication.getTarget().getFormDescription());
            rows.put(row);
        }
        writeData(rows, resp);
    }

    private Map<String, String[]> getDynamicColumns(HttpServletRequest req) {
        List<String> identifiers = getDynamicColumnOrder(req);
        Map<String, String[]> dynamicColumns = new HashMap<String, String[]>();
        for(String identifier : identifiers) {
            String columns = req.getParameter(identifier);
            dynamicColumns.put(identifier, columns.split("\\s*,\\s*"));
        }
        return dynamicColumns;
    }
    
    private static final Set<String> KNOWN_PARAMS = new HashSet<String>() {{
        add("entityModule");
        add("columns");
        add("session");
        add("action");
    }};
    
    private List<String> getDynamicColumnOrder(HttpServletRequest req) {
        Enumeration parameterNames = req.getParameterNames();
        List<String> dynamicColumnIdentifiers = new ArrayList<String>();
        while(parameterNames.hasMoreElements()) {
            String paramName = (String) parameterNames.nextElement();
            if(!KNOWN_PARAMS.contains(paramName) && paramName.contains(".")) {
                dynamicColumnIdentifiers.add(paramName);
            }
        }
        Collections.sort(dynamicColumnIdentifiers, new QueryStringPositionComparator(req.getQueryString()));
        return dynamicColumnIdentifiers;
    }

    private String[] getBasicColumns(HttpServletRequest req) {
        String columns = req.getParameter("columns");
        if(columns == null) {
            return new String[]{"id", "entityId", "entityModule", "target"};
        }
        return columns.split("\\s*,\\s*");
    }
    
}
