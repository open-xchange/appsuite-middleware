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

package com.openexchange.publish.json;

import static com.openexchange.java.Autoboxing.L;
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
import java.util.TimeZone;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.fields.ResponseFields;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.multiple.MultipleHandler;
import com.openexchange.publish.Publication;
import com.openexchange.publish.PublicationErrorMessage;
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

    private static final String PROPERTY_USE_OTHER_DOMAIN = "com.openexchange.publish.domain";
    private static final String PROPERTY_USE_OTHER_SUBDOMAIN = "com.openexchange.publish.subdomain";

    private final PublicationTargetDiscoveryService discovery;
    private final Map<String, EntityType> entities;
    private final ConfigurationService config;

    public PublicationMultipleHandler(final PublicationTargetDiscoveryService discovery, final Map<String, EntityType> entities, final ConfigurationService config) {
        super();
        this.discovery = discovery;
        this.entities = entities;
        this.config = config;
    }

    @Override
    public void close() {
        // Nothing to do.
    }

    @Override
    public Date getTimestamp() {
        return null;
    }

    public static final Set<String> ACTIONS_REQUIRING_BODY = new HashSet<String>() {
        private static final long serialVersionUID = -4485493200664773739L;
        {

            add("new");
            add("update");
            add("delete");
            add("list");

        }
    };

    @Override
    public Object performRequest(final String action, final JSONObject request, final ServerSession session, final boolean secure) throws JSONException, OXException {
        try {
            if (null == action) {
                throw MISSING_PARAMETER.create("action");
            } else if (action.equals("new")) {
                if (!config.getBoolProperty("com.openexchange.publish.createModifyEnabled", false)) {
                    throw PublicationJSONErrorMessage.FORBIDDEN_CREATE_MODIFY.create();
                }
                return createPublication(request, session);
            } else if (action.equals("update")) {
                if (!config.getBoolProperty("com.openexchange.publish.createModifyEnabled", false)) {
                    throw PublicationJSONErrorMessage.FORBIDDEN_CREATE_MODIFY.create();
                }
                return updatePublication(request, session);
            } else if (action.equals("delete")) {
                return deletePublication(request, session);
            } else if (action.equals("get")) {
                return loadPublication(request, session);
            } else if (action.equals("all")) {
                return loadAllPublications(request, session);
            } else if (action.equals("list")) {
                return listPublications(request, session);
            } else {
                throw UNKNOWN_ACTION.create(action);
            }
        } catch (final JSONException x) {
            throw x;
        } catch (final OXException x) {
            throw x;
        } catch (final Throwable t) {
            throw wrapThrowable(t);
        }
    }

    private Object listPublications(final JSONObject request, final ServerSession session) throws JSONException, OXException, OXException {
        final JSONArray ids = request.getJSONArray(ResponseFields.DATA);
        final Context context = session.getContext();
        final List<Publication> publications = new ArrayList<Publication>(ids.length());
        for (int i = 0, size = ids.length(); i < size; i++) {
            final int id = ids.getInt(i);
            final PublicationTarget target = discovery.getTarget(context, id);
            if(target != null) {
                final PublicationService publicationService = target.getPublicationService();
                final Publication publication = publicationService.load(context, id);
                if (publication != null) {
                    publications.add(publication);
                }
            }

        }
        final String[] basicColumns = getBasicColumns(request);
        final Map<String, String[]> dynamicColumns = getDynamicColumns(request);
        final List<String> dynamicColumnOrder = getDynamicColumnOrder(request);

        TimeZone tz = null;
        String sTimeZone = request.optString("timezone");
        if (sTimeZone != null) {
            tz = TimeZone.getTimeZone(sTimeZone);
        } else {
            tz = TimeZone.getTimeZone(session.getUser().getTimeZone());
        }

        return createList(publications, basicColumns, dynamicColumns, dynamicColumnOrder, tz);
    }

    private Object loadAllPublications(final JSONObject request, final ServerSession session) throws OXException, JSONException, OXException {
        final Context context = session.getContext();
        final int userId = session.getUserId();
        boolean containsFolderOrId = false;

        if (request.has("folder") || request.has("id")) {
            if (!request.has("entityModule")) {
                throw MISSING_PARAMETER.create("entityModule");
            }
            containsFolderOrId = true;
        }

        String module = null;
        if (request.has("entityModule")) {
            module = request.optString("entityModule");
        }

        // Check if request contains folder attribute. If not assume a request for all publications of the session user.
        // If module is set in this case, fetch all publications of a user in that module.
        final List<Publication> publications;
        if (containsFolderOrId) {
            final EntityType entityType = entities.get(module);
            if (null == entityType) {
                throw UNKOWN_ENTITY_MODULE.create(module);
            }
            final String entityId = entityType.toEntityID(request);
            publications = loadAllPublicationsForEntity(context, entityId, module);
        } else {
            publications = loadAllPublicationsForUser(context, userId, module);
        }

        final String[] basicColumns = getBasicColumns(request);
        final Map<String, String[]> dynamicColumns = getDynamicColumns(request);
        final List<String> dynamicColumnOrder = getDynamicColumnOrder(request);

        TimeZone tz = null;
        String sTimeZone = request.optString("timezone");
        if (sTimeZone != null) {
            tz = TimeZone.getTimeZone(sTimeZone);
        } else {
            tz = TimeZone.getTimeZone(session.getUser().getTimeZone());
        }

        return createList(publications, basicColumns, dynamicColumns, dynamicColumnOrder, tz);
    }

    private static final Set<String> KNOWN_PARAMS = new HashSet<String>() {
        private static final long serialVersionUID = -6947818649378328911L;
        {
            add("entityModule");
            add("columns");
            add("session");
            add("action");
        }
    };


    private Map<String, String[]> getDynamicColumns(final JSONObject request) throws JSONException {
        final List<String> identifiers = getDynamicColumnOrder(request);
        final Map<String, String[]> dynamicColumns = new HashMap<String, String[]>();
        for (final String identifier : identifiers) {
            final String columns = request.optString(identifier);
            if (columns != null && ! columns.equals("")) {
                dynamicColumns.put(identifier, columns.split("\\s*,\\s*"));
            }
        }
        return dynamicColumns;
    }

    private List<String> getDynamicColumnOrder(final JSONObject request) throws JSONException {
        if (request.has("dynamicColumnPlugins")) {
            return Arrays.asList(request.getString("dynamicColumnPlugins").split("\\s*,\\s*"));
        }

        final List<String> dynamicColumnIdentifiers = new ArrayList<String>();
        for (final String paramName : request.keySet()) {
            if (!KNOWN_PARAMS.contains(paramName) && paramName.indexOf('.') >= 0) {
                dynamicColumnIdentifiers.add(paramName);
            }
        }
        final String order = request.optString("__query");
        Collections.sort(dynamicColumnIdentifiers, new QueryStringPositionComparator(order));
        return dynamicColumnIdentifiers;
    }

    private String[] getBasicColumns(final JSONObject request) {
        final String columns = request.optString("columns");
        if (columns == null || columns.equals("")) {
            return new String[] { "id", "entity", "entityModule", "target", "enabled" };
        }
        return columns.split("\\s*,\\s*");
    }

    private List<Publication> loadAllPublicationsForUser(final Context context, final int userId, final String module) throws OXException {
        final List<Publication> publications = new LinkedList<Publication>();
        final Collection<PublicationTarget> targets = discovery.listTargets();

        for (final PublicationTarget target : targets) {
            Collection<Publication> allPublicationsForUser = null;

            if (module == null) {
                final PublicationService publicationService = target.getPublicationService();
                allPublicationsForUser = publicationService.getAllPublications(context, userId, target.getModule());
            } else {
                if (target.isResponsibleFor(module)) {
                    final PublicationService publicationService = target.getPublicationService();
                    allPublicationsForUser = publicationService.getAllPublications(context, userId, module);
                }
            }
            if (allPublicationsForUser != null) {
                publications.addAll(allPublicationsForUser);
            }
        }

        return publications;
    }

    private List<Publication> loadAllPublicationsForEntity(final Context context, final String entityId, final String module) throws OXException {
        final List<Publication> publications = new LinkedList<Publication>();
        final Collection<PublicationTarget> targetsForEntityType = discovery.getTargetsForEntityType(module);
        for (final PublicationTarget target : targetsForEntityType) {
            if (target.isResponsibleFor(module)) {
                final PublicationService publicationService = target.getPublicationService();
                final Collection<Publication> allPublicationsForEntity = publicationService.getAllPublications(context, entityId);
                if (allPublicationsForEntity != null) {
                    publications.addAll(allPublicationsForEntity);
                }
            }
        }
        return publications;
    }

    private Object loadPublication(final JSONObject request, final ServerSession session) throws JSONException, OXException {
        final int id = request.getInt("id");
        final String target = request.optString("target");
        final Context context = session.getContext();
        final Publication publication = loadPublication(id, context, target);

        if (null == publication) {
            throw PublicationErrorMessage.PUBLICATION_NOT_FOUND_EXCEPTION.create();
        }

        String sTimeZone = request.optString("timezone");
        TimeZone tz = null;
        if (sTimeZone != null) {
            tz = TimeZone.getTimeZone(sTimeZone);
        } else {
            tz = TimeZone.getTimeZone(session.getUser().getTimeZone());
        }

        return createResponse(publication, getURLPrefix(request, publication), tz);
    }

    private String getURLPrefix(final JSONObject request, final Publication publication) {
        String hostname = Hostname.getInstance().getHostname(publication);
        String serverURL = request.optString("__serverURL");
        String protocol = "https://";

        if (hostname != null) {
            if (serverURL == null || serverURL.startsWith("https")) {
                protocol = "https://";
            } else {
                protocol = "http://";
            }
            serverURL = new StringBuilder(protocol).append(hostname).toString();
        } else if (serverURL != null) {
            hostname = serverURL.substring(serverURL.indexOf("://") + 3);
            if (serverURL.startsWith("https")) {
                protocol = "https://";
            } else {
                protocol = "http://";
            }
        }

        final String otherDomain = config.getProperty(PROPERTY_USE_OTHER_DOMAIN);
        final String separateSubdomain = config.getProperty(PROPERTY_USE_OTHER_SUBDOMAIN);

        if (otherDomain != null) {
            return protocol + otherDomain;
        }

        if (separateSubdomain != null) {
            return new StringBuilder(protocol).append(separateSubdomain).append('.').append(hostname).toString();
        }

        return serverURL;
    }

    private Publication loadPublication(final int id, final Context context, final String target) throws OXException {
        PublicationService service = null;
        if (target != null && !target.equals("")) {
            PublicationTarget t = discovery.getTarget(target);
            if (t == null) {
                return null;
            }
            service = t.getPublicationService();
        } else {
            PublicationTarget t = discovery.getTarget(context, id);
            if (t == null) {
                return null;
            }
            service = t.getPublicationService();
        }
        if(service == null) {
            return null;
        }
        return service.load(context, id);
    }

    private Object deletePublication(final JSONObject request, final ServerSession session) throws OXException, JSONException {
        final JSONArray ids = request.getJSONArray(ResponseFields.DATA);
        final Context context = session.getContext();
        for (int i = 0, size = ids.length(); i < size; i++) {
            final int id = ids.getInt(i);
            final PublicationTarget target = discovery.getTarget(context, id);
            if (target == null) {
                throw PublicationErrorMessage.PUBLICATION_NOT_FOUND_EXCEPTION.create();
            }
            final PublicationService publisher = target.getPublicationService();
            final Publication publication = new Publication();
            publication.setContext(context);
            publication.setId(id);
            publication.setUserId(session.getUserId());
            publisher.delete(publication);
        }
        return L(1);
    }

    private Object updatePublication(final JSONObject request, final ServerSession session) throws JSONException, OXException, OXException {
        final Publication publication = getPublication(request, session);
        publication.update();
        return L(1);
    }

    private Object createPublication(final JSONObject request, final ServerSession session) throws OXException, OXException, JSONException {
        final Publication publication = getPublication(request, session);
        publication.setId(-1);
        publication.create();
        return L(publication.getId());
    }

    private Object createList(final List<Publication> publications, final String[] basicColumns, final Map<String, String[]> dynamicColumns, final List<String> dynamicColumnOrder, TimeZone tz) throws OXException, JSONException {
        final JSONArray rows = new JSONArray();
        final PublicationWriter writer = new PublicationWriter();
        for (final Publication publication : publications) {
            final JSONArray row = writer.writeArray(
                publication,
                basicColumns,
                dynamicColumns,
                dynamicColumnOrder,
                publication.getTarget().getFormDescription(), tz);
            rows.put(row);
        }
        return rows;
    }

    private Object createResponse(final Publication publication, final String urlPrefix, TimeZone tz) throws JSONException, OXException {
        final JSONObject asJson = new PublicationWriter().write(publication, urlPrefix, tz);
        return asJson;
    }

    private Publication getPublication(final JSONObject request, final ServerSession session) throws JSONException, OXException, OXException {
        final JSONObject object = request.getJSONObject(ResponseFields.DATA);
        final Publication publication = new PublicationParser(discovery).parse(object);
        publication.setUserId(session.getUserId());
        publication.setContext(session.getContext());
        if (publication.getTarget() == null && publication.getId() > 0) {
            final PublicationTarget target = discovery.getTarget(publication.getContext(), publication.getId());
            publication.setTarget(target);
        }
        return publication;
    }

    @Override
    public Collection<OXException> getWarnings() {
        return Collections.<OXException> emptySet();
    }

}
