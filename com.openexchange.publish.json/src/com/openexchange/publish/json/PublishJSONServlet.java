
package com.openexchange.publish.json;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.PermissionServlet;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.writer.ResponseWriter;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.publish.Path;
import com.openexchange.publish.PublicationService;
import com.openexchange.publish.Site;
import com.openexchange.session.Session;
import com.openexchange.tools.servlet.http.Tools;
import com.openexchange.tools.session.ServerSession;

/**
 * @author <a href="mailto:martin.herfurth@open-xchange.org">Martin Herfurth</a>
 */
public class PublishJSONServlet extends PermissionServlet {

    private static final long serialVersionUID = 12L;

    private static final Log LOG = LogFactory.getLog(PublishJSONServlet.class);

    private static final String PUBLISH_ACTION = "publish";

    private static final String UNPUBLISH_ACTION = "unpublish";

    private static final String LIST_ACTION = "list";

    private static final int POST = 0;

    private static final int GET = 1;

    private static final int PUT = 2;

    private static PublicationService publicationService;
    
    
    public static void setPublicationService(PublicationService service) {
        publicationService = service;
    }

    protected void doPost(HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        perform(req, resp, POST);
    }

    protected void doGet(HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        perform(req, resp, GET);
    }

    @Override
    protected void doPut(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        perform(req, resp, PUT);
    }

    private void perform(HttpServletRequest req, HttpServletResponse resp, int method) throws ServletException, IOException {
        resp.setContentType(CONTENTTYPE_JAVASCRIPT);
        /*
         * The magic spell to disable caching
         */
        Tools.disableCaching(resp);
        try {
            final Response response = doAction(req, method);
            ResponseWriter.write(response, resp.getWriter());
        } catch (final AbstractOXException e) {
            LOG.error("perform", e);
            final Response response = new Response();
            response.setException(e);
            final PrintWriter writer = resp.getWriter();
            try {
                ResponseWriter.write(response, writer);
            } catch (final JSONException e1) {
                final ServletException se = new ServletException(e1);
                se.initCause(e1);
                throw se;
            }
            writer.flush();
        } catch (final JSONException e) {
            LOG.error("perform", e);
        }
    }

    private Response doAction(HttpServletRequest req, int method) throws AbstractOXException, JSONException, IOException {

        switch (method) {
        case PUT:
        case POST:
            return writeAction(req);
        default:
            return readAction(req);
        }

    }

    private Response readAction(HttpServletRequest req) throws JSONException {
        final Response response = new Response();
        final String action = req.getParameter("action");
        final Session session = getSessionObject(req);

        if (action.equals(LIST_ACTION)) {
            response.setData(list(session));
        }

        return response;
    }

    private JSONArray list(Session session) throws JSONException {
        JSONArray retval = new JSONArray();

        //Collection<Site> sites = publicationService.getSites(session.getContextId(), session.getUserId());
        /*if(sites == null) {
            return retval;
        }
        for (Site site : sites) {
            retval.put(getJSONObject(site));
        }*/

        return retval;
    }

    private JSONObject getJSONObject(Site site) throws JSONException {
        JSONObject retval = new JSONObject();

        retval.put("name", site.getPath().getSiteName());
        retval.put("folder", site.getFolderId());

        return retval;
    }

    private Response writeAction(HttpServletRequest req) throws JSONException, IOException {
        final Response response = new Response();
        final String action = req.getParameter("action");
        final Session session = getSessionObject(req);

        if (action.equals(PUBLISH_ACTION)) {
            JSONObject siteToPublish = new JSONObject(getBody(req));
            Site site = getSite(siteToPublish, session);
            unpublish(site);
            publish(site);
        } else if (action.equals(UNPUBLISH_ACTION)) {
            JSONObject siteToUnpublish = new JSONObject(getBody(req));
            Site site = getSite(siteToUnpublish, session);
            unpublish(site);
        }

        response.setData(1);
        return response;
    }

    private void unpublish(Site site) {
        //publicationService.delete(site);
    }

    private void publish(Site site) {
        //publicationService.create(site);
    }

    private Site getSite(JSONObject siteToPublish, Session session) throws JSONException {
        Site site = new Site();
        Path path = new Path();
        path.setContextId(session.getContextId());
        path.setOwnerId(session.getUserId());
        path.setSiteName(siteToPublish.optString("name"));
        site.setPath(path);
        if (siteToPublish.has("folder")) {
            site.setFolderId(siteToPublish.getInt("folder"));
        }
        return site;
    }

    @Override
    protected boolean hasModulePermission(ServerSession session) {
        // TODO Auto-generated method stub
        return false;
    }
}
