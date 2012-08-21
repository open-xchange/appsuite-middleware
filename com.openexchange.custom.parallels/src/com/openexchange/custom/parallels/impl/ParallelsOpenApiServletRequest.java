

package com.openexchange.custom.parallels.impl;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.apache.xmlrpc.client.XmlRpcCommonsTransportFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osgi.framework.ServiceException;
import com.openexchange.config.ConfigurationService;
import com.openexchange.custom.parallels.osgi.ParallelsServiceRegistry;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.session.Session;
import com.openexchange.tools.servlet.AjaxExceptionCodes;

/**
 * 
 * Handles requests to the OpenAPI interface of POA.
 * 
 * Currently the management of Black/White Lists is implemented
 * 
 */
public final class ParallelsOpenApiServletRequest  {

    private static final Log LOG = com.openexchange.log.Log.loggerFor(ParallelsOpenApiServletRequest.class);
    private User user;
    private final ConfigurationService configservice;


    public static final String ACTION_ADD = "add"; // use this to add data to the specified type if list
    public static final String ACTION_DELETE = "delete"; // use this to delete data to the specified type if list
    public static final String ACTION_GET = "get"; // use this to get data for the specified type if list
    public static final String PARAMETER_DATA = "data"; // use this to send data

    public static final String MODULE_WHITELIST= "whitelist"; // whiteliste module
    public static final String MODULE_BLACKLIST = "blacklist"; // blacklist module

    public static final String PROPERTY_OPENAPI_INTERFACE_URL = "com.openexchange.custom.parallels.openapi.interface_url";
    public static final String PROPERTY_OPENAPI_INTERFACE_AUTH_ID = "com.openexchange.custom.parallels.openapi.auth_id";
    public static final String PROPERTY_OPENAPI_INTERFACE_AUTH_PASSWORD = "com.openexchange.custom.parallels.openapi.auth_password";
    public static final String PROPERTY_OPENAPI_INTERFACE_AUTH_ENABLED = "com.openexchange.custom.parallels.openapi.auth_enabled";

    public ParallelsOpenApiServletRequest(final Session sessionObj, final Context ctx)	throws OXException, ServiceException {
        try {
            this.user = UserStorage.getInstance().getUser(sessionObj.getUserId(), ctx);
        } catch (final OXException e) {
            LOG.error(e.getMessage(), e);
            throw new OXException(e);
        }

        // init config
        this.configservice = ParallelsServiceRegistry.getServiceRegistry().getService(ConfigurationService.class,true);
    }

    public Object action(final String action,final String module, final JSONObject jsonObject) throws OXException, JSONException {
        Object retval = null;

        if (module.equalsIgnoreCase(MODULE_WHITELIST)) {
            if(action.equalsIgnoreCase(ACTION_ADD)){
                retval = addOrDeleteFromList("pem.spam_assassin.addItems", "white", jsonObject);
            }else if (action.equalsIgnoreCase(ACTION_DELETE)){
                retval = addOrDeleteFromList("pem.spam_assassin.deleteItems", "white", jsonObject);
            }else if (action.equalsIgnoreCase(ACTION_GET)){
                retval = getItemsForList("white");
            }else{
                throw AjaxExceptionCodes.UNKNOWN_ACTION.create(module);
            }
        }else if(module.equalsIgnoreCase(MODULE_BLACKLIST)){
            if(action.equalsIgnoreCase(ACTION_ADD)){
                retval = addOrDeleteFromList("pem.spam_assassin.addItems", "black", jsonObject);
            }else if (action.equalsIgnoreCase(ACTION_DELETE)){
                retval = addOrDeleteFromList("pem.spam_assassin.deleteItems", "black", jsonObject);
            }else if (action.equalsIgnoreCase(ACTION_GET)){
                retval = getItemsForList("black");
            }else{
                throw AjaxExceptionCodes.UNKNOWN_ACTION.create(module);
            }
        }else{
            throw AjaxExceptionCodes.UNKNOWN_MODULE.create(module);
        }
        return retval;
    }




    private Object addOrDeleteFromList(final String method,final String list,final JSONObject jsonObject) throws OXException, JSONException {

        // we need the data parameter here
        checkForMissingParameter(jsonObject);

        try{


            final Map bla = new HashMap();
            final String[] data = {jsonObject.getString("data")};
            bla.put("items",data );
            bla.put("list",list);


            bla.put("login", this.user.getMail());
            final Map response = (HashMap) getRpcClient().execute(method,new Object[]{bla});
            checkXMLRpcResponseForError(response);
        } catch (final XmlRpcException e) {
            LOG.error("xml-rpc error detected while communicating with openapi interface");
            throw ParallelsOpenApiServletExceptionCodes.HTTP_COMMUNICATION_ERROR.create(e.getMessage());
        } catch (final MalformedURLException e) {
            LOG.error("IO error occured while communicating with openapi interface");
            throw ParallelsOpenApiServletExceptionCodes.HTTP_COMMUNICATION_ERROR.create(e.getMessage());
        } catch (final ServiceException e) {
            LOG.error("internal configuration error occured");
            throw ParallelsOpenApiServletExceptionCodes.OPENAPI_COMMUNICATION_ERROR.create(e.getMessage());
        }finally{
        }

        return null;
    }

    /**
     * checks if response contains error field
     * @param response
     * @throws ParallelsOpenApiServletExceptionCodes
     */
    private void checkXMLRpcResponseForError(final Map response) throws OXException {
        if(response.containsKey("error_code")){
            throw ParallelsOpenApiServletExceptionCodes.OPENAPI_COMMUNICATION_ERROR.create(response.get("error_message")+" (OPEN_API_ERROR_CODE:"+response.get("error_code")+" )");
        }
    }

    private Object getItemsForList(final String list) throws OXException, JSONException {

        final JSONObject json_response = new JSONObject();

        try{



            final Map bla = new HashMap();
            bla.put("list",list);
            bla.put("login", this.user.getMail());
            final Map response = (HashMap) getRpcClient().execute("pem.spam_assassin.getItems",new Object[]{bla});
            checkXMLRpcResponseForError(response);

            final JSONArray retval = new JSONArray();

            if(response.containsKey("result")){
                final HashMap tmp_ = (HashMap)response.get("result");
                final Object[] tmp__ = (Object[])tmp_.get("list");
                for (final Object addy : tmp__){
                    retval.put(addy.toString());
                }
            }
            if(LOG.isDebugEnabled()){
                LOG.debug("Got the following items from openapi for list \""+list+"\" "+retval.toString());
            }
            json_response.put("items", retval);
            return json_response;
        } catch (final XmlRpcException e) {
            LOG.error("xml-rpc error detected while communicating with openapi interface");
            throw ParallelsOpenApiServletExceptionCodes.HTTP_COMMUNICATION_ERROR.create(e.getMessage());
        } catch (final MalformedURLException e) {
            LOG.error("IO error occured while communicating with openapi interface");
            throw ParallelsOpenApiServletExceptionCodes.HTTP_COMMUNICATION_ERROR.create(e.getMessage());
        } catch (final ServiceException e) {
            LOG.error("internal configuration error occured");
            throw ParallelsOpenApiServletExceptionCodes.OPENAPI_COMMUNICATION_ERROR.create(e.getMessage());
        }finally{
        }


    }

    private XmlRpcClient getRpcClient() throws MalformedURLException, ServiceException{
        final XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
        config.setServerURL(new URL(getOpenAPIInterfaceURL()));
        config.setUserAgent("OX-HE POA PLUGIN");
        if(isOpenAPIAuthEnabled()){
            if(LOG.isDebugEnabled()){
                LOG.debug("Using HTTP BASIC AUTH (Username: "+getOpenAPIAuthID()+") for sending XML-RPC requests to OpenAPI...");
            }
            config.setBasicUserName(getOpenAPIAuthID());
            config.setBasicPassword(getOpenAPIAuthPassword());
        }
        final XmlRpcClient client = new XmlRpcClient();
        client.setTransportFactory(new XmlRpcCommonsTransportFactory(client));
        client.setConfig(config);
        return client;
    }

    private void checkForMissingParameter(final JSONObject jsonObject) throws OXException{
        if(!jsonObject.has(PARAMETER_DATA)){
            throw AjaxExceptionCodes.MISSING_PARAMETER.create("data");
        }
    }


    private String getOpenAPIInterfaceURL() throws ServiceException{
        return getFromConfig(PROPERTY_OPENAPI_INTERFACE_URL);
    }

    private String getOpenAPIAuthID() throws ServiceException{
        return getFromConfig(PROPERTY_OPENAPI_INTERFACE_AUTH_ID);
    }

    private String getOpenAPIAuthPassword() throws ServiceException{
        return getFromConfig(PROPERTY_OPENAPI_INTERFACE_AUTH_PASSWORD);
    }

    private boolean isOpenAPIAuthEnabled() throws ServiceException{
        return this.configservice.getBoolProperty(PROPERTY_OPENAPI_INTERFACE_AUTH_ENABLED, false);
    }


    private String getFromConfig(final String key) throws ServiceException{
        return this.configservice.getProperty(key);
    }





}
