
package com.openexchange.publish.json;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.PermissionServlet;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.writer.ResponseWriter;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.publish.Path;
import com.openexchange.publish.Publication;
import com.openexchange.publish.PublicationService;
import com.openexchange.publish.Site;
import com.openexchange.session.Session;
import com.openexchange.tools.servlet.http.Tools;

public class PublishJSONServlet extends PermissionServlet {

    private static final long serialVersionUID = 12L;

    private static final Log LOG = LogFactory.getLog(PublishJSONServlet.class);

    private static final String STANDARD_SITE = "public";

    private static final int POST = 0;

    private static final int GET = 1;

    private static final int PUT = 2;

    private static PublicationService publicationService;

    @Override
    protected boolean hasModulePermission(Session session, Context ctx) {
        return true;
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
        
        switch(method) {
        case PUT: case POST: return writeAction(req);
        default: return readAction(req);
        }
        
    }
    
    private Response readAction(HttpServletRequest req)  {
        Response response = new Response();
        
        String action = req.getParameter("action");
        
        Session session = getSessionObject(req);
        Publication publication = getPublicationFromParameters(req, session);
        
        if(action.equals("exists")) {
            response.setData( exists(publication) );
        }
        
        return response;
    }
    
    private Response writeAction(HttpServletRequest req) throws JSONException, IOException {
        Response response = new Response();
        
        JSONObject objectToPublish = new JSONObject(getBody(req));
        Session session = getSessionObject(req);
        
        Publication publication = getPublication( objectToPublish, session );
        
        
        String action = req.getParameter("action");
        if (action.equals("publish")) {
        
            publish( publication );
        
        } else if (action.equals("unpublish")) {
            
            unpublish( publication );
                
        }
        
        response.setData(1);
        return response;
    }
    
    private Publication getPublication(JSONObject objectToPublish, Session session) throws JSONException {
        Path path = new Path(session.getUserId(), session.getContextId(), STANDARD_SITE);
        Site site = new Site(); site.setPath(path);
        
        Publication publication = new Publication();
        publication.setSite(site);
        publication.setObjectID(objectToPublish.getInt("id"));
        publication.setFolderId(objectToPublish.getInt("folder"));
        publication.setType(type4module(objectToPublish.getString("module")));
        
        return publication;
        
    }
    
    private Publication getPublicationFromParameters(HttpServletRequest req, Session session) {
        Path path = new Path(session.getUserId(), session.getContextId(), STANDARD_SITE);
        Site site = new Site(); site.setPath(path);
        
        Publication publication = new Publication();
        publication.setSite(site);
        publication.setObjectID(Integer.valueOf(req.getParameter("id")));
        publication.setFolderId(Integer.valueOf(req.getParameter("folder")));
        publication.setType(type4module(req.getParameter("module")));
        
        return publication;
        
    }

    private int type4module(String string) {
        return Types.CONTACT;
    }

    private void publish(Publication publication) {
        publicationService.publish( publication );
    }
    
    private void unpublish(Publication publication) {
        publicationService.unpublish(publication);
    }

    private boolean exists(Publication publication) {
        return publicationService.exists( publication );
    }
    public static void setPublicationService(PublicationService service) {
        publicationService = service;
    }

}
