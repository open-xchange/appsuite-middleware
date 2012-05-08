
package com.openexchange.custom.dynamicnet.impl;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.PermissionServlet;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.writer.ResponseWriter;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.session.Session;
import com.openexchange.tools.session.ServerSession;

/**
 * 
 * Servlet which returns needed Data for the Dynamic Net Iframe Plugin to
 * redirect to an external GUI
 * 
 * @author <a href="mailto:manuel.kraft@open-xchange.com">Manuel Kraft</a>
 * 
 */
public final class DynamicNetIFrameServlet extends PermissionServlet {

    /**
     * 
     */
    private static final long serialVersionUID = 8541427953784271108L;

    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(DynamicNetIFrameServlet.class);



    /**
     * Initializes
     */
    public DynamicNetIFrameServlet() {
        super();
    }

    @Override
    protected boolean hasModulePermission(final ServerSession sessionObj) {
        return true;
    }

    @Override
    protected void doGet(final HttpServletRequest req,
        final HttpServletResponse resp) throws ServletException,
        IOException {

        try {
            actionGetData(req, resp);
        } catch (final JSONException e) {
            LOG.error(e.getLocalizedMessage(), e);
        }

    }

    /**
     * Performs the GET request!
     * 
     * This will return a Json Object with username,password,context
     * name,context-id. Can then be used in IFRAME Plugins to login/redirect
     * into other GUIs!
     * 
     * @param req
     *            The servlet request
     * @param resp
     *            The servlet response
     * @throws JSONException
     *             If JSON data cannot be composed
     * @throws IOException
     *             If an I/O error occurs
     */
    private void actionGetData(final HttpServletRequest req,
        final HttpServletResponse resp) throws JSONException, IOException {
        /*
         * Some variables we need here to handle the request
         */
        final Response response = new Response();
        final Session session = getSessionObject(req);

        String login = session.getLogin();
        final String[] tmp = login.split("@");
        login = tmp[0];
        final String password = session.getPassword();
        final int contextid = session.getContextId();
        String context_name = "UNRESOLVED";
        try {
            context_name = ContextStorage.getInstance().getContext(contextid).getName();
        } catch (final OXException e) {
            LOG.error("Error resolving context mappings/name!",e);
        }

        final JSONObject obj = new JSONObject();

        obj.put("login", login);
        obj.put("password", password);
        obj.put("context_id", contextid);
        obj.put("context_name", context_name);

        response.setData(obj);

        /*
         * Close response and flush print writer
         */
        ResponseWriter.write(response, resp.getWriter());
    }


}
