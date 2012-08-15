

package com.openexchange.custom.parallels.impl;

import java.io.IOException;
import java.util.Arrays;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONException;
import org.json.JSONObject;
import org.osgi.framework.ServiceException;
import com.openexchange.ajax.PermissionServlet;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.writer.ResponseWriter;
import com.openexchange.config.ConfigurationService;
import com.openexchange.custom.parallels.osgi.ParallelsServiceRegistry;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.session.Session;
import com.openexchange.tools.session.ServerSession;

/**
 * 
 * Servlet which returns needed Data for the Parallels direct links manipulation
 * and also returns some data like username,password etc.
 * 
 * Also does jobs for the other GUI Plugins
 * 
 * @author <a href="mailto:manuel.kraft@open-xchange.com">Manuel Kraft</a>
 * 
 */
public final class ParallelsInfoServlet extends PermissionServlet {



    /**
     * 
     */
    private static final long serialVersionUID = -6454818806420432111L;

    private static final org.apache.commons.logging.Log LOG = com.openexchange.log.Log.loggerFor(ParallelsInfoServlet.class);



    /**
     * Initializes
     */
    public ParallelsInfoServlet() {
        super();
    }

    @Override
    protected boolean hasModulePermission(final ServerSession session) {
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
     * name,context-id,branding-url. Can then be used in IFRAME Plugins and
     * for manipulating direct urls.
     * 
     * obj.put("login"
		obj.put("password",
		obj.put("context_id"
		obj.put("context_name"
		obj.put("mail"
		obj.put("branding_url"
     * 
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
        String mail = null;
        String branded_url = null;
        try {

            final Context ctx = ContextStorage.getInstance().getContext(contextid);
            final User userobject = getUserObjectFromSession(session);
            context_name = ctx.getName();

            // resolve the branding URL
            branded_url = getBrandingURLFromLoginMappings(ctx);

            mail = userobject.getMail();
        } catch (final OXException e) {
            LOG.error(e.getMessage(), e);
        } catch (final ServiceException e) {
            LOG.error("Error resolving branded url for context "+contextid);
        }

        final JSONObject obj = new JSONObject();

        obj.put("login", login);
        obj.put("password", password);
        obj.put("context_id", contextid);
        obj.put("context_name", context_name);
        obj.put("mail", mail);
        obj.put("branding_url", branded_url);

        response.setData(obj);

        /*
         * Close response and flush print writer
         */
        ResponseWriter.write(response, resp.getWriter());
    }

    public static User getUserObjectFromSession(final Session session) throws OXException{

        final int contextid = session.getContextId();
        final Context ctx = ContextStorage.getInstance().getContext(contextid);

        return UserStorage.getInstance().getUser(session.getUserId(),ctx);
    }



    private String getBrandingURLFromLoginMappings(final Context ctx) throws OXException {

        final String[] login_mappings = ctx.getLoginInfo();
        final ConfigurationService configservice = ParallelsServiceRegistry.getServiceRegistry().getService(ConfigurationService.class,true);

        // load fallback url first, and if context is branded, we switch this url to the branded from database
        String branded_url = configservice.getProperty("com.openexchange.custom.parallels.branding.fallbackurl");

        // load suffix for branding string dynamically in loginmappings
        final String suffix_branded = configservice.getProperty("com.openexchange.custom.parallels.branding.suffix");
        // for debugging purposes
        if(LOG.isDebugEnabled()){
            LOG.debug("Loaded loginmappings "+Arrays.toString(login_mappings)+" for context "+ctx.getContextId());
        }
        for (final String login_mapping : login_mappings) {
            if(login_mapping.startsWith(suffix_branded)){
                /**
                 * 
                 *  We found our mapping which contains the branded URL!
                 * 
                 *  Now split up the string to get the URL part
                 * 
                 */
                final String[] URL_ = login_mapping.split("\\|\\|"); // perhaps replace with substring(start,end) if would be faster
                if(URL_.length!=3){
                    LOG.fatal("Could not split up branded URL "+login_mapping+" login mapping for context "+ctx.getContextId());
                }else{
                    branded_url = URL_[2];
                    if(LOG.isDebugEnabled()){
                        LOG.debug("Successfully resolved HOST to "+branded_url+" for branded context "+ctx.getContextId());
                    }
                }
            }
        }


        return branded_url;

    }





}
