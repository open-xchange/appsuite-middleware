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



package com.openexchange.custom.parallels.impl;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.apache.xmlrpc.client.XmlRpcCommonsTransportFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osgi.framework.ServiceException;
import org.slf4j.Logger;
import com.openexchange.config.ConfigurationService;
import com.openexchange.custom.parallels.osgi.Services;
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

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(ParallelsOpenApiServletRequest.class);

    private User user;
    private final ConfigurationService configservice;


    public static final String ACTION_ADD = "add"; // use this to add data to the specified type if list
    public static final String ACTION_DELETE = "delete"; // use this to delete data to the specified type if list
    public static final String ACTION_GET = "get"; // use this to get data for the specified type if list
    public static final String PARAMETER_DATA = "data"; // use this to send data

    public static final String MODULE_WHITELIST= "whitelist"; // whiteliste module
    public static final String MODULE_BLACKLIST = "blacklist"; // blacklist module

    public ParallelsOpenApiServletRequest(final Session sessionObj, final Context ctx)	throws OXException, ServiceException {
        try {
            this.user = UserStorage.getInstance().getUser(sessionObj.getUserId(), ctx);
        } catch (final OXException e) {
            LOG.error("", e);
            throw new OXException(e);
        }

        // init config
        this.configservice = Services.getService(ConfigurationService.class);
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


            final HashMap<String, Object> rpcargs = new HashMap<String, Object>();
            final String[] data = {jsonObject.getString("data")};
            rpcargs.put("items",data );
            rpcargs.put("list",list);


            // Bugfix ID#27047 - [L3] POA antispam shows errors when service user and primary email address not match
            // use user login (LoginInfo) instead of email address to identify user in POA as this is what
            // POA also does
            //rpcargs.put("login", this.user.getMail());
            rpcargs.put("login", this.user.getLoginInfo());
            final Map<?, ?> response = (HashMap<?, ?>) getRpcClient().execute(method,new Object[]{rpcargs});
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
    private void checkXMLRpcResponseForError(final Map<?, ?> response) throws OXException {
        if(response.containsKey("error_code") || response.containsKey("error_message")){
            throw ParallelsOpenApiServletExceptionCodes.OPENAPI_COMMUNICATION_ERROR.create(response.get("error_message")+" (OPEN_API_ERROR_CODE:"+response.get("error_code")+" )");
        }
    }

    private Object getItemsForList(final String list) throws OXException, JSONException {

        final JSONObject json_response = new JSONObject();

        try{



            final HashMap<String, String> rpcargs = new HashMap<String, String>();
            rpcargs.put("list",list);
            // Bugfix ID#27047 - [L3] POA antispam shows errors when service user and primary email address not match
            // use user login (LoginInfo) instead of email address to identify user in POA as this is what
            // POA also does
            //rpcargs.put("login", this.user.getMail());
            rpcargs.put("login", this.user.getLoginInfo());
            final HashMap<?, ?> response = (HashMap<?, ?>) getRpcClient().execute("pem.spam_assassin.getItems",new Object[]{rpcargs});
            checkXMLRpcResponseForError(response);

            final JSONArray retval = new JSONArray();

            if(response.containsKey("result")){
                final HashMap<?, ?> tmp_ = (HashMap<?, ?>)response.get("result");
                final Object[] tmp__ = (Object[])tmp_.get("list");
                for (final Object addy : tmp__){
                    retval.put(addy.toString());
                }
            }
            LOG.debug("Got the following items from openapi for list \"{}\" {}", list, retval);
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
        config.setUserAgent("Open-Xchange Paralles Plugin");
        if(isOpenAPIAuthEnabled()){
            LOG.debug("Using HTTP BASIC AUTH (Username: {}) for sending XML-RPC requests to OpenAPI...", getOpenAPIAuthID());
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
        return getFromConfig(ParallelsOptions.PROPERTY_OPENAPI_INTERFACE_URL);
    }

    private String getOpenAPIAuthID() throws ServiceException{
        return getFromConfig(ParallelsOptions.PROPERTY_OPENAPI_INTERFACE_AUTH_ID);
    }

    private String getOpenAPIAuthPassword() throws ServiceException{
        return getFromConfig(ParallelsOptions.PROPERTY_OPENAPI_INTERFACE_AUTH_PASSWORD);
    }

    private boolean isOpenAPIAuthEnabled() throws ServiceException{
        return this.configservice.getBoolProperty(ParallelsOptions.PROPERTY_OPENAPI_INTERFACE_AUTH_ENABLED, false);
    }


    private String getFromConfig(final String key) throws ServiceException{
        return this.configservice.getProperty(key);
    }





}
