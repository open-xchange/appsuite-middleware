
package com.openexchange.spamsettings.generic.servlet;

import java.io.IOException;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.PermissionServlet;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.writer.ResponseWriter;
import com.openexchange.exception.OXException;
import com.openexchange.spamsettings.generic.SpamSettingsParser;
import com.openexchange.spamsettings.generic.SpamSettingsWriter;
import com.openexchange.spamsettings.generic.osgi.SpamSettingsServiceRegistry;
import com.openexchange.spamsettings.generic.service.SpamSettingService;
import com.openexchange.tools.servlet.OXJSONExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public final class SpamSettingsServlet extends PermissionServlet {

    /**
     * 
     */
    private static final long serialVersionUID = 2124511440962531967L;

    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(SpamSettingsServlet.class);

    /**
     * Initializes
     */
    public SpamSettingsServlet() {
        super();
    }

    @Override
    protected boolean hasModulePermission(final ServerSession session) {
        return true;
    }

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {

        final Response response = new Response();

        try {
            final String action = JSONUtility.checkStringParameter(req, "action");
            if ("get".equals(action)) {
                final JSONObject result = new JSONObject();
                result.put("formDescription", new SpamSettingsWriter().write(getSessionObject(req)));
                final SpamSettingService service = SpamSettingsServiceRegistry.getServiceRegistry().getService(SpamSettingService.class);
                result.put("value", getValue(getSessionObject(req), service));
                response.setData(result);
            }
        } catch (final OXException e) {
            LOG.error("Missing or wrong field action in JSON request", e);
            response.setException(e);
        } catch (final JSONException e) {
            LOG.error(e.getMessage(), e);
            response.setException(OXJSONExceptionCodes.JSON_WRITE_ERROR.create(e));
        }

        /*
         * Close response and flush print writer
         */
        try {
            ResponseWriter.write(response, resp.getWriter());
        } catch (final JSONException e) {
            LOG.error(e.getLocalizedMessage(), e);
        }
    }

    private JSONObject getValue(final ServerSession session, final SpamSettingService service) throws JSONException, OXException {
        final Map<String, Object> settings = service.getSettings(session);
        final JSONObject retval = new JSONObject();
        for (final Map.Entry<String, Object> entry : settings.entrySet()) {
            retval.put(entry.getKey(), entry.getValue());
        }
        return retval;
    }

    @Override
    protected void doPut(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {

        final Response response = new Response();

        final JSONObject obj = new JSONObject();

        try {
            final String action = JSONUtility.checkStringParameter(req, "action");
            if (action.equals("update")) {
                final String body = getBody(req);
                final JSONObject jsonObject = new JSONObject(body);
                final Map<String, Object> settings = new SpamSettingsParser().parse(getSessionObject(req), jsonObject);
                final SpamSettingService spamSettingService = SpamSettingsServiceRegistry.getServiceRegistry().getService(SpamSettingService.class);
                spamSettingService.writeSettings(getSessionObject(req), settings);
                obj.put("message", "Settings written");
            }
        } catch (final OXException e) {
            LOG.error("Missing or wrong field action in JSON request", e);
            response.setException(e);
        } catch (final JSONException e) {
            LOG.error(e.getLocalizedMessage(), e);
            response.setException(OXJSONExceptionCodes.JSON_READ_ERROR.create(e));
        }

        response.setData(obj);

        /*
         * Close response and flush print writer
         */
        try {
            ResponseWriter.write(response, resp.getWriter());
        } catch (final JSONException e) {
            LOG.error(e.getLocalizedMessage(), e);
        }
    }

}
