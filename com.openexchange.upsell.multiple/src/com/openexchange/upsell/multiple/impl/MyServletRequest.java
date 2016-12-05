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

package com.openexchange.upsell.multiple.impl;

import static com.openexchange.upsell.multiple.osgi.MyServiceRegistry.getServiceRegistry;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.httpclient.URIException;
import org.json.JSONException;
import org.json.JSONObject;
import org.osgi.framework.ServiceException;
import org.slf4j.Logger;
import com.openexchange.admin.rmi.OXContextInterface;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.NoSuchContextException;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.config.cascade.ConfigCascadeExceptionCodes;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.java.Streams;
import com.openexchange.mail.api.MailConfig;
import com.openexchange.mail.dataobjects.compose.ComposeType;
import com.openexchange.mail.dataobjects.compose.ContentAwareComposedMailMessage;
import com.openexchange.mail.mime.MessageHeaders;
import com.openexchange.mail.mime.MimeDefaultSession;
import com.openexchange.mail.mime.MimeMailException;
import com.openexchange.mail.mime.QuotedInternetAddress;
import com.openexchange.mail.transport.MailTransport;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.session.Session;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.stream.UnsynchronizedByteArrayOutputStream;
import com.openexchange.upsell.multiple.api.UpsellURLParametersMap;
import com.openexchange.upsell.multiple.api.UpsellURLService;

/**
 *
 * Servlet to trigger upsell actions like email or URL redirect.
 *
 */
public final class MyServletRequest  {

    private final Session sessionObj;
    private User user;
    private User admin;
    private final Context ctx;
    private final ConfigView configView;


    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(MyServletRequest.class);

    // HTTP API methods/parameters
    public static final String ACTION_GET_CONFIGURED_METHOD = "get_method"; // action to retrieve configured upsell method
    public static final String ACTION_GET_STATIC_REDIRECT_URL_METHOD = "get_static_redirect_url"; //
    public static final String ACTION_GET_EXTERNAL_REDIRECT_URL_METHOD = "get_external_redirect_url"; //
    public static final String ACTION_TRIGGER_UPSELL_EMAIL = "send_upsell_email"; //
    public static final String ACTION_DOWNGRADE = "change_context_permissions"; //


    // config options
    private static final String PROPERTY_METHOD_EXTERNAL_SHOP_API_URL = "com.openexchange.upsell.multiple.method.external.shop_api_url";
    private static final String PROPERTY_METHOD_STATIC_SHOP_REDIR_URL = "com.openexchange.upsell.multiple.method.static.shop_redir_url";
    private static final String PROPERTY_METHOD = "com.openexchange.upsell.multiple.method"; // one of: external, static, email, direct

    // email options
    private static final String PROPERTY_METHOD_EMAIL_ADDRESS = "com.openexchange.upsell.multiple.method.email.address";
    private static final String PROPERTY_METHOD_EMAIL_SUBJECT = "com.openexchange.upsell.multiple.method.email.subject";
    private static final String PROPERTY_METHOD_EMAIL_TEMPLATE = "com.openexchange.upsell.multiple.method.email.template";
    private static final String PROPERTY_METHOD_EMAIL_OXUSER_TEMPLATE = "com.openexchange.upsell.multiple.method.email.oxuser.template";
    private static final String PROPERTY_METHOD_EMAIL_OXUSER_SUBJECT_TEMPLATE = "com.openexchange.upsell.multiple.method.email.oxuser.template_subject";
    private static final String PROPERTY_METHOD_EMAIL_OXUSER_ENABLED = "com.openexchange.upsell.multiple.method.email.oxuser.enabled";

    // RMI API options
    //private static final String PROPERTY_RMI_HOST = "com.openexchange.upsell.multiple.rmi.host";
    private static final String PROPERTY_RMI_MASTERADMIN = "com.openexchange.upsell.multiple.rmi.masteradmin";
    private static final String PROPERTY_RMI_MASTERADMIN_PWD = "com.openexchange.upsell.multiple.rmi.masteradmin.pass";
    private static final String PROPERTY_RMI_DOWNGRADE_NAME = "com.openexchange.upsell.multiple.rmi.downgrade.accessname";

    public MyServletRequest(final Session sessionObj, final Context ctx) throws OXException {
        this.sessionObj = sessionObj;
        this.ctx = ctx;
        try {
            // load user for data
            this.user = UserStorage.getInstance().getUser(sessionObj.getUserId(), ctx);

            // load admin for custom data like redirect url
            this.admin = UserStorage.getInstance().getUser(this.ctx.getMailadmin(), ctx);

        } catch (final OXException e) {
            LOG.error("", e);
            throw e;
        }

        // init config
        final ConfigViewFactory configViewFactory = getServiceRegistry().getService(ConfigViewFactory.class,true);
        this.configView = configViewFactory.getView(sessionObj.getUserId(), sessionObj.getContextId());
    }

    public Object action(final String action, final JSONObject jsonObject, final HttpServletRequest http_request) throws OXException, JSONException {
        Object retval = null;

        // Host/UI URL from where the request came, needed for different types of shops per domain/branding
        final String request_src_hostname = http_request.getServerName();

        if(action.equalsIgnoreCase(ACTION_GET_CONFIGURED_METHOD)){
            // return configur\ufffded upsell method
            retval = actionGetUpsellMethod(jsonObject);
        }else if(action.equalsIgnoreCase(ACTION_GET_STATIC_REDIRECT_URL_METHOD)){
            // return static redirect URL containing all needed parameters
            retval = actionGetStaticRedirectURL(jsonObject,request_src_hostname);
        }else if(action.equalsIgnoreCase(ACTION_GET_EXTERNAL_REDIRECT_URL_METHOD)){
            // return the generated URL from the external system
            retval = actionGetExternalRedirectURL(jsonObject,request_src_hostname);
        }else if(action.equalsIgnoreCase(ACTION_DOWNGRADE)){
            // downgrade the context which is within this users session
            retval = actionUpDownGradeContext(jsonObject);
        }else if(action.equalsIgnoreCase(ACTION_TRIGGER_UPSELL_EMAIL)){
            // trigger and send email with all need params to configured email addy
            // UI must send feature, upsell package and hostname
            retval = actionTriggerEmailUpsell(jsonObject,request_src_hostname);
        }else {
            throw AjaxExceptionCodes.UNKNOWN_ACTION.create(action);
        }

        return retval;
    }

    private Object actionUpDownGradeContext(final JSONObject json) throws OXException {

        try {

            String upsell_plan = getFromConfig(PROPERTY_RMI_DOWNGRADE_NAME); // fallback if not set

            if(json.has("upsell_plan")){
                upsell_plan = json.getString("upsell_plan");
            }

            final OXContextInterface iface = getServiceRegistry().getService(OXContextInterface.class);
            if (null == iface) {
                throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(OXContextInterface.class.getName());
            }

            final com.openexchange.admin.rmi.dataobjects.Context bla = new com.openexchange.admin.rmi.dataobjects.Context(Integer.valueOf(this.sessionObj.getContextId()));

            final Credentials authcreds = new Credentials(getFromConfig(PROPERTY_RMI_MASTERADMIN),getFromConfig(PROPERTY_RMI_MASTERADMIN_PWD));

            iface.getAccessCombinationName(bla, authcreds);
            LOG.info("Current access combination name for context {}: {}", this.ctx.getContextId(), iface.getAccessCombinationName(bla, authcreds));

            // update the level of the context
            iface.changeModuleAccess(bla,upsell_plan, authcreds);

            // get updated level to debug if it was correctly set
            iface.getAccessCombinationName(bla, authcreds);
            LOG.info("Updated access combination name for context {} to: {}", this.ctx.getContextId(), iface.getAccessCombinationName(bla, authcreds));

        } catch (final RemoteException e) {
            LOG.error("Error changing context",e);
            throw MyServletExceptionCodes.API_COMMUNICATION_ERROR.create(e.getMessage());
        } catch (final StorageException e) {
            LOG.error("Error changing context",e);
            throw MyServletExceptionCodes.API_COMMUNICATION_ERROR.create(e.getMessage());
        } catch (final InvalidCredentialsException e) {
            LOG.error("Invalid credentials supplied for OX API",e);
            throw MyServletExceptionCodes.API_COMMUNICATION_ERROR.create(e.getMessage());
        } catch (final NoSuchContextException e) {
            LOG.error("Error changing context",e);
            throw MyServletExceptionCodes.API_COMMUNICATION_ERROR.create(e.getMessage());
        } catch (final InvalidDataException e) {
            LOG.error("Error changing context",e);
            throw MyServletExceptionCodes.API_COMMUNICATION_ERROR.create(e.getMessage());
        } catch (final JSONException e) {
            LOG.error("Error changing context",e);
            throw MyServletExceptionCodes.API_COMMUNICATION_ERROR.create(e.getMessage());
        } catch (final OXException e) {
            if (ConfigCascadeExceptionCodes.PREFIX.equals(e.getPrefix())) {
                LOG.error("Error changing context. Mandatory configuration option not found",e);
                throw MyServletExceptionCodes.API_COMMUNICATION_ERROR.create(e.getMessage());
            }
            throw e;
        }





        return null;
    }

    private Object actionGetExternalRedirectURL(final JSONObject jsonObject,final String request_src_hostname) {


        return null;
    }



    /**
     *
     * Send an upsell mail to configured email address with configured/parsed body and subject
     *
     * @param jsonObject
     * @param request_src_hostname
     * @return
     * @throws OXException
     */
    private Object actionTriggerEmailUpsell(final JSONObject jsonObject,final String request_src_hostname) throws OXException {

        try {

            final String email_addy_ox_user = this.user.getMail();
            final String email_addy_provider = getFromConfig(PROPERTY_METHOD_EMAIL_ADDRESS);
            String subject = getFromConfig(PROPERTY_METHOD_EMAIL_SUBJECT);


            // load mail body template if exists
            String mailbody_provider = getTemplateContent(getFromConfig(PROPERTY_METHOD_EMAIL_TEMPLATE),false);
            if(mailbody_provider==null){
                mailbody_provider = subject;
            }

            subject = parseText(subject, jsonObject, false); // replace stuff for easier processing at customer
            mailbody_provider = parseText(mailbody_provider, jsonObject, false); // replace stuff in mail template

            // send mail to provider email addy
            sendUpsellEmail(email_addy_provider, email_addy_ox_user, mailbody_provider, subject);


            // prepare/send email to enduser if configured
            if(getFromConfig(PROPERTY_METHOD_EMAIL_OXUSER_ENABLED)!=null &&
                getFromConfig(PROPERTY_METHOD_EMAIL_OXUSER_ENABLED).equalsIgnoreCase("true")){

                // first try to load i18n version, if not found, try to load generic one
                String oxuser_subject = getTemplateContent(getFromConfig(PROPERTY_METHOD_EMAIL_OXUSER_SUBJECT_TEMPLATE),true);


                if(oxuser_subject==null){
                    oxuser_subject = subject; // fallback to general subject
                }else{
                    oxuser_subject = parseText(oxuser_subject, jsonObject, false); // parse infos into the templates
                }


                String oxuser_body = getTemplateContent(getFromConfig(PROPERTY_METHOD_EMAIL_OXUSER_TEMPLATE),true);

                if(oxuser_body==null){
                    oxuser_body = mailbody_provider; // fallback to general mailbody
                }else{
                    oxuser_body = parseText(oxuser_body, jsonObject, false); // parse infos into the templates
                }

                sendUpsellEmail(email_addy_ox_user, email_addy_ox_user, oxuser_body, oxuser_subject);
                LOG.debug("Sent upsell request email to enduser with email address:{}", email_addy_ox_user);
            }

        } catch (final OXException e) {
            LOG.error("Error reading mandatory configuration parameters for sending upsell email",e);
            throw MyServletExceptionCodes.EMAIL_COMMUNICATION_ERROR.create(e.getMessage());
        } catch (final URIException e) {
            LOG.error("Error parsing upsell email text",e);
            throw MyServletExceptionCodes.EMAIL_COMMUNICATION_ERROR.create(e.getMessage());
        } catch (final UnsupportedEncodingException e) {
            LOG.error("Error parsing upsell email text",e);
            throw MyServletExceptionCodes.EMAIL_COMMUNICATION_ERROR.create(e.getMessage());
        } catch (final JSONException e) {
            LOG.error("Error processing upsell email text",e);
            throw MyServletExceptionCodes.EMAIL_COMMUNICATION_ERROR.create(e.getMessage());
        }

        return null;
    }


    private String getTemplateContent(final String fulltemplatepath,final boolean i18n){


        if (!i18n) {
            final File templateFile = new File(fulltemplatepath);
            if (templateFile.exists() && templateFile.canRead() && templateFile.isFile()) {
                LOG.debug("Found and now using the upsell mail template at {}", fulltemplatepath);
                return getFileContents(templateFile);
            } else {
                LOG.error("Could not find an upsell mail template at {}, using {} as mail body.", fulltemplatepath, PROPERTY_METHOD_EMAIL_SUBJECT);
                return null;
            }
        } else {
            // load with langcode extension
            final File templateFile_i18n = new File(fulltemplatepath + "_"+ this.user.getPreferredLanguage());
            final File templateFile = new File(fulltemplatepath);
            // first try to load i18n file, then the fallback file
            if (templateFile_i18n.exists() && templateFile_i18n.canRead()
                && templateFile_i18n.isFile()) {
                LOG.debug("Found and now using the i18n upsell mail template at {}", templateFile_i18n.getPath());
                return getFileContents(templateFile_i18n);
            } else if (templateFile.exists() && templateFile.canRead()
                && templateFile.isFile()) {
                LOG.debug("Found and now using the upsell mail template at {}", templateFile.getPath());
                return getFileContents(templateFile);
            } else {
                LOG.error("Could not find an i18n upsell mail template with base path {}", fulltemplatepath);
                return null;
            }
        }

    }



    /**
     *
     * Return the parsed URL to the UI to which it should redirect
     *
     * @param jsonObject
     * @return
     * @throws ServiceException
     * @throws JSONException
     */
    private Object actionGetStaticRedirectURL(final JSONObject jsonObject,final String request_src_hostname) throws OXException, JSONException {
        final JSONObject jsonResponseObject = new JSONObject();

        // Default implementation to generate the redirect URL
        // this checks for configured url in file or configured url in admin user attributes
        final String STATIC_URL_RAW = getRedirectURL();


        try {

            String url = parseText(STATIC_URL_RAW,jsonObject,true);


            // now check for custom implementations of the URL
            final UpsellURLService urlservice = getServiceRegistry().getService(UpsellURLService.class);
            final UpsellURLService provider = null;
            if (null != urlservice) {
                LOG.debug("Found URLGenerator service. Using it now to generate redirect Upsell URL instead of default.");
                // We have a special service providing login information, so we use that one...
                try {
                    // pass the parameters to the external implementation
                    url = urlservice.generateUrl(getParameterMap(jsonObject),this.sessionObj,this.user,this.admin,this.ctx);
                    LOG.debug("Using custom redirect URL from URLGenerator service. URL: {}", url);
                } catch (final OXException e) {
                    LOG.error("Fatal error occurred, generating redirect URL from custom implementation failed!", e);
                }
            }

            jsonResponseObject.put("upsell_static_redirect_url",url); // parsed url with all parameter
        } catch (final URIException e) {
            LOG.error("Error encoding static redirect URL", e);
        } catch (final UnsupportedEncodingException e) {
            LOG.error("Error encoding static redirect URL", e);
        }

        return jsonResponseObject;
    }

    /**
     * If context has special login mapping "UPSELL_DIRECT_URL||<URL>" we use this URL instead of configured one.
     * @return
     * @throws ServiceException
     */
    private String getRedirectURL() throws OXException{

        String STATIC_URL_RAW = getFromConfig(PROPERTY_METHOD_STATIC_SHOP_REDIR_URL);
        final int contextId = this.ctx.getContextId();

        LOG.debug("Admin user attributes for context {} : {}", contextId, this.admin.getAttributes());

        if(this.admin.getAttributes().containsKey("com.openexchange.upsell/url")){
            String url = this.admin.getAttributes().get("com.openexchange.upsell/url");
            STATIC_URL_RAW = url;
            STATIC_URL_RAW += "src=ox&user=_USER_&invite=_INVITE_&mail=_MAIL_&purchase_type=_PURCHASE_TYPE_&login=_LOGIN_&imaplogin=_IMAPLOGIN_&clicked_feat=_CLICKED_FEATURE_&upsell_plan=_UPSELL_PLAN_&cid=_CID_&lang=_LANG_";
            LOG.debug("Parsed UPSELL URL from context {} and admin user attributes: {}", contextId, STATIC_URL_RAW);
        }else{
            LOG.debug("Parsed UPSELL URL from configuration for context: {}", contextId);
        }

        return STATIC_URL_RAW;
    }

    private String parseText(String raw_text, final JSONObject json, final boolean url_encode_it) throws JSONException, URIException, UnsupportedEncodingException {
        final Map<UpsellURLParametersMap, String> bla = getParameterMap(json);

        // loop through all params and replace
        for (final Map.Entry<UpsellURLParametersMap, String> entry : bla.entrySet()) {
            final UpsellURLParametersMap map_key = entry.getKey();
            String map_val = entry.getValue();
            if (null != map_val) {
                if (url_encode_it) {
                    map_val = URLEncoder.encode(map_val, "UTF-8");
                }
                // replace the placeholder with values
                raw_text = raw_text.replaceAll(map_key.propertyName, map_val);
            }
        }

        return raw_text;
    }

    private static final Pattern P_RPL = Pattern.compile("<br/?>");

    private void sendUpsellEmail(final String to, final String from,final String text,final String subject) throws OXException{
        MailConfig mailConfig = null;
        try {
            /*
             * Compose rfc822 message
             */
            final MimeMessage mimeMessage;
            {
                mimeMessage = new MimeMessage(MimeDefaultSession.getDefaultSession());
                mimeMessage.setSubject(subject);
                mimeMessage.setFrom(new QuotedInternetAddress(from, false));
                mimeMessage.setRecipient(javax.mail.Message.RecipientType.TO, new QuotedInternetAddress(to));
                /*
                 * Set text content
                 */
                mimeMessage.setText(P_RPL.matcher(text).replaceAll("\n"), "UTF-8");
                mimeMessage.setHeader(MessageHeaders.HDR_MIME_VERSION, "1.0");
                mimeMessage.setHeader(MessageHeaders.HDR_CONTENT_TYPE, "text/plain; charset=UTF-8");
                /*
                 * Write bytes
                 */
                final ByteArrayOutputStream tmp = new UnsynchronizedByteArrayOutputStream(2048);
                mimeMessage.writeTo(tmp);
            }
            /*
             * Perform transport
             */
            final MailTransport transport = MailTransport.getInstance(this.sessionObj);
            try {
                mailConfig = transport.getTransportConfig();
                transport.sendMailMessage(new ContentAwareComposedMailMessage(mimeMessage, sessionObj, ctx), ComposeType.NEW);
                LOG.info("Upsell request from user {} (cid:{})  was sent to {}", this.sessionObj.getLogin(), this.ctx.getContextId(), to);
            } finally {
                transport.close();
            }
        } catch (final OXException e) {
            LOG.error("Couldn't send provisioning mail", e);
            throw MyServletExceptionCodes.EMAIL_COMMUNICATION_ERROR.create(e.getMessage());
        } catch (final AddressException e) {
            LOG.error("Target email address cannot be parsed",e);
            throw MyServletExceptionCodes.EMAIL_COMMUNICATION_ERROR.create(e.getMessage());
        } catch (final MessagingException e) {
            throw MimeMailException.handleMessagingException(e, mailConfig, sessionObj);
        } catch (final IOException e) {
            // Cannot occur
            throw MyServletExceptionCodes.EMAIL_COMMUNICATION_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Method for generating a map with all needed parameters
     *
     * @param jsondata - Data from UI to fill feature which was clicked and what upsell plan user wants to buy
     * @return
     * @throws JSONException
     */
    private Map<UpsellURLParametersMap, String> getParameterMap(final JSONObject jsondata) throws JSONException{

        final Map<UpsellURLParametersMap, String> bla = new HashMap<UpsellURLParametersMap, String>();

        bla.put(UpsellURLParametersMap.MAP_ATTR_USER,this.sessionObj.getUserlogin()); // users username
        bla.put(UpsellURLParametersMap.MAP_ATTR_PWD,this.sessionObj.getPassword()); // password
        bla.put(UpsellURLParametersMap.MAP_ATTR_MAIL,this.user.getMail()); // users email addy
        bla.put(UpsellURLParametersMap.MAP_ATTR_LOGIN,this.sessionObj.getLogin()); // users full login from UI mask
        bla.put(UpsellURLParametersMap.MAP_ATTR_IMAP_LOGIN,this.user.getImapLogin()); // imap login
        bla.put(UpsellURLParametersMap.MAP_ATTR_CID,String.valueOf(ctx.getContextId())); // context id
        bla.put(UpsellURLParametersMap.MAP_ATTR_USERID,String.valueOf(this.sessionObj.getUserId())); // user id
        bla.put(UpsellURLParametersMap.MAP_ATTR_LANGUAGE,this.user.getPreferredLanguage()); // language

        if(jsondata!=null && jsondata.has("purchase_type")){
            bla.put(UpsellURLParametersMap.MAP_ATTR_PURCHASE_TYPE,jsondata.getString("purchase_type"));
        }

        if(jsondata!=null && jsondata.has("invite")){
            bla.put(UpsellURLParametersMap.MAP_ATTR_INVITE,jsondata.getString("invite"));
        }

        if(jsondata!=null && jsondata.has("feature_clicked")){
            bla.put(UpsellURLParametersMap.MAP_ATTR_CLICKED_FEATURE,jsondata.getString("feature_clicked")); // the feature the user clicked on like calender, infostore, mobility etc.

        }
        if(jsondata!=null && jsondata.has("upsell_plan")){
            bla.put(UpsellURLParametersMap.MAP_ATTR_UPSELL_PLAN,jsondata.getString("upsell_plan")); //the upsell package the user wants to buy
        }


        return bla;

    }

    static public String getFileContents(final File file) {
        final StringBuilder stringBuilder = new StringBuilder();
        BufferedReader input = null;
        try {
            input = new BufferedReader(new FileReader(file));
            String line = null;
            while ((line = input.readLine()) != null) {
                stringBuilder.append(line);
                stringBuilder.append(System.getProperty("line.separator"));
            }
        } catch (final IOException e) {
            LOG.error("", e);
        } finally {
            Streams.close(input);
        }
        return stringBuilder.toString();
    }

    /**
     *
     * Return configured method of upsell plugin to handle actions different in UI.
     *
     * @param jsonObject
     * @return
     * @throws ServiceException
     * @throws JSONException
     * @throws OXException
     */
    private Object actionGetUpsellMethod(final JSONObject jsonObject) throws JSONException, OXException {
        final JSONObject jsonResponseObject = new JSONObject();

        jsonResponseObject.put("upsell_method",getFromConfig(PROPERTY_METHOD)); // send method

        return jsonResponseObject;
    }


    private String getFromConfig(final String key) throws OXException{
        return this.configView.get(key, String.class);
    }

    //	private static final HttpClient HTTPCLIENT;
    //
    //	    static {
    //	            MultiThreadedHttpConnectionManager manager = new MultiThreadedHttpConnectionManager();
    //	            HttpConnectionManagerParams params = manager.getParams();
    //	            params.setMaxConnectionsPerHost(HostConfiguration.ANY_HOST_CONFIGURATION, 23);
    //	            HTTPCLIENT = new HttpClient(manager);
    //	    }


}
