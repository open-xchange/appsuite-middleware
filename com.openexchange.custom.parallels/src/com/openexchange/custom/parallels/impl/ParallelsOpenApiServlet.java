

package com.openexchange.custom.parallels.impl;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.DataServlet;
import com.openexchange.ajax.container.Response;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.session.Session;
import com.openexchange.tools.servlet.OXJSONExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * 
 * Servlet which manages the POA Black/White Lists for an OX User.
 * 
 * 
 * @author <a href="mailto:manuel.kraft@open-xchange.com">Manuel Kraft</a>
 * 
 */
public final class ParallelsOpenApiServlet extends DataServlet {

    /**
     * 
     */
    private static final long serialVersionUID = 7650360590998502303L;
    private static final org.apache.commons.logging.Log LOG = com.openexchange.log.Log.loggerFor(ParallelsOpenApiServlet.class);

    public ParallelsOpenApiServlet() {
        super();
    }

    @Override
    protected boolean hasModulePermission(final ServerSession session) {
        return true;
    }


    @Override
    protected void doPost(final HttpServletRequest req, final HttpServletResponse resp)
    throws ServletException, IOException {
        super.doPost(req, resp);
        doGet(req,resp);
    }

    @Override
    protected void doGet(final HttpServletRequest req,
        final HttpServletResponse resp) throws ServletException,
        IOException {

        final Response response = new Response();

        try {

            final String action = parseMandatoryStringParameter(req,PARAMETER_ACTION);
            final String module = parseMandatoryStringParameter(req,PARAMETER_MODULE);
            final Session session = getSessionObject(req);
            JSONObject jsonObj;

            try {
                jsonObj = convertParameter2JSONObject(req);
            } catch (final JSONException e) {
                LOG.error(e.getMessage(), e);
                response.setException(OXJSONExceptionCodes.JSON_BUILD_ERROR.create(e));
                writeResponse(response, resp);
                return;
            }
            final Context ctx = ContextStorage.getInstance().getContext(session);
            final ParallelsOpenApiServletRequest proRequest = new ParallelsOpenApiServletRequest(session, ctx);
            final Object responseObj = proRequest.action(action,module, jsonObj);
            response.setData(responseObj);

        } catch (final OXException e) {
            LOG.error(e.getMessage(), e);
            response.setException(e);
        } catch (final JSONException e) {
            final OXException oje = OXJSONExceptionCodes.JSON_WRITE_ERROR.create(e);
            LOG.error(oje.getMessage(), oje);
            response.setException(oje);
        }

        writeResponse(response, resp);

    }

}
