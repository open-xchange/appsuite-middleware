
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
        
        /*switch(method) {
        case PUT: case POST: return writeAction(req);
        default: return readAction(req);
        }*/
        return null;
        
    }
    
    

}
