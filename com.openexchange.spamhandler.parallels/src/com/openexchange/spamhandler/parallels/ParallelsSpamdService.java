/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 * 
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.spamhandler.parallels;

import static com.openexchange.custom.parallels.impl.ParallelsOptions.PROPERTY_ANTISPAM_XMLRPC_PORT;
import static com.openexchange.java.Autoboxing.I;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import com.openexchange.config.cascade.ComposedConfigProperty;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.exception.OXException;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.session.Session;
import com.openexchange.spamhandler.spamassassin.api.SpamdProvider;
import com.openexchange.spamhandler.spamassassin.api.SpamdService;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.user.User;
import com.openexchange.user.UserService;
import com.openexchange.xml.util.XMLUtils;

public class ParallelsSpamdService implements SpamdService {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(ParallelsSpamdService.class);

    private final ConfigViewFactory factory;
    private final UserService userService;

    public ParallelsSpamdService(ConfigViewFactory factory, UserService userService) {
        super();
        this.factory = factory;
        this.userService = userService;
    }

    private <V> V getPropertyFor(Session session, String propertyName, V defaultValue, Class<V> clazz) throws OXException {
        ConfigView view = factory.getView(session.getUserId(), session.getContextId());

        ComposedConfigProperty<V> property = view.property(propertyName, clazz);
        return (null != property && property.isDefined()) ? property.get() : defaultValue;
    }

    private int getPort(Session session) throws OXException {
        return getPropertyFor(session, "com.openexchange.spamhandler.spamassassin.port", Integer.valueOf(783), Integer.class).intValue();
    }

    private int getXmlPort(Session session) throws OXException {
        return getPropertyFor(session, PROPERTY_ANTISPAM_XMLRPC_PORT, Integer.valueOf(3100), Integer.class).intValue();
    }

    @Override
    public SpamdProvider getProvider(final Session session) throws OXException {

        /**
         * 1. sent primary email address via xmlrpc to smtpserver of user to port specified in config
         *
         *
         *   <?xml version="1.0"?>
		 		<methodCall>
				<methodName>pem.spamassassin</methodName>
				<params>
				<param>
				<value><struct>
				<member><name>mailbox</name>
				<value><string>a@serik-qmail1.bcom</string></value>
				</member>
				</struct></value>
				</param>
				</params>
				</methodCall>
         *
         *
         *
         *
         *
         *
         *
         *
         *
         *
         *
         *
         *
         *
         * 2. read response from xmlrpc and parse hostname and username
         * which will then be returned in the spamdprovider.
         * port of spamd process will always be read from config file.
         *
         * ERROR response:
         *
         * <?xml version="1.0" encoding="utf-8"?>
<methodResponse>
<fault>
<value>
  <struct>
    <member>
      <name>faultCode</name>
      <value>
        <i4>-1</i4>
      </value>
    </member>
    <member>
      <name>faultString</name>
      <value>
        <string>'spam-protection' is not enabled</string>
      </value>
    </member>
  </struct>
</value>
</fault>
</methodResponse>


         *
         *
         *  SUCCESS RESPONSE:
         *
         *  <?xml version="1.0" encoding="utf-8"?>
<methodResponse>
<params>
<param>
  <value>
    <struct>
      <member>
        <name>server</name>
        <value>
          <string>172.16.53.123</string>
        </value>
      </member>
      <member>
        <name>username</name>
        <value>
          <string>t/m/217</string>
        </value>
      </member>
    </struct>
  </value>
</param>
</params>
</methodResponse>
         *
         *
         *
         *
         *
         *
         * 3. set attributes in session object to remember the spam settings
         * so that no 2nd request must be made to the xmlrpc to keep the load low.
         *
         */
        CloseableHttpClient http_client = HttpClientBuilder.create().build();
        try {

            // spamd port from configuration
            int spamd_provider_port = getPort(session);
            LOG.debug("Using port {} for connections to spamd service", I(spamd_provider_port));

            // get the user object from the OX API to retrieve users primary mail address
            final User oxuser = getUser(session);

            // get all needed infos to make the xmlrpc request
            final String xml_rpc_prim_email = oxuser.getMail(); // primary mail of the user for the xml-rpc request to made

            final java.net.URI tp = new java.net.URI(oxuser.getSmtpServer());// this will always be the smtp://host:port of the user
            final String xmlrpc_server = tp.getHost();
            int xml_rpc_port = getXmlPort(session);
            LOG.debug("Using port {} for connections to xmlrpc service", I(xml_rpc_port));

            final String URL_to_xmlrpc = "http://" +xmlrpc_server+":"+xml_rpc_port;
            HttpPost post_method = new HttpPost();
            post_method.setURI(new URI(URL_to_xmlrpc));
            post_method.setEntity(new StringEntity(getXmlRpcRequestBody(xml_rpc_prim_email), ContentType.TEXT_XML));

            LOG.debug("Using {} to connect to xmlrpc service", URL_to_xmlrpc);
            LOG.debug("Using email address {} for xmlrpc request", xml_rpc_prim_email);


            HttpResponse resp = http_client.execute(post_method);

            final String xml_rpc_response = EntityUtils.toString(resp.getEntity());

            LOG.debug("Got response from xmlrpc service:");
            LOG.debug(xml_rpc_response);


            // check if contains a "faultcode" part, if no, parse for data
            if (!xml_rpc_response.contains("<name>faultCode</name>")){
                DocumentBuilderFactory dbf = XMLUtils.safeDbf(DocumentBuilderFactory.newInstance());
                final DocumentBuilder docBuilder = dbf.newDocumentBuilder();
                final Document doc = docBuilder.parse (new ByteArrayInputStream(xml_rpc_response.getBytes(StandardCharsets.UTF_8)));

                // normalize text representation
                doc.getDocumentElement().normalize();
                // get member element list to loop through
                final NodeList listOfMembers = doc.getElementsByTagName("member");
                // for each member look to its name
                String response_server = null;
                String response_user = null;
                for(int s=0; s<listOfMembers.getLength() ; s++){


                    final Node firstMemberNode = listOfMembers.item(s);
                    if (firstMemberNode.getNodeType() == Node.ELEMENT_NODE){


                        final Element firstMemberElement = (Element)firstMemberNode;


                        final NodeList firstMemberList = firstMemberElement.getElementsByTagName("name");
                        final Element firstNameElement = (Element)firstMemberList.item(0);
                        final NodeList textFNList = firstNameElement.getChildNodes();


                        final NodeList firstMemberList_ = firstMemberElement.getElementsByTagName("string");
                        final Element firstNameElement_ = (Element)firstMemberList_.item(0);
                        final NodeList textFNList_ = firstNameElement_.getChildNodes();

                        if ((textFNList.item(0)).getNodeValue().trim().equals("server")){
                            // server ip ==
                            response_server = (textFNList_.item(0)).getNodeValue().trim();
                                LOG.debug("Returning {} as host to spamhandler", response_server);
                        }else{
                            // username ==
                            response_user = (textFNList_.item(0)).getNodeValue().trim();
                            LOG.debug("Returning {} as userame to spamhandler", response_user);
                        }

                    }
                }
                final SpamdProvider sp_provider = getSpamdProvider(response_server, spamd_provider_port, response_user);

                LOG.debug("Returning spamprovider informations from xmlrpc response");
                // return spamprovider to api
                return sp_provider;
            }
            LOG.error("got error response from xml-rpc service for primary mail {}", xml_rpc_prim_email);
            LOG.error(xml_rpc_response);
            throw MailExceptionCode.SPAM_HANDLER_INIT_FAILED.create("got error response from xml-rpc service for primary mail "+xml_rpc_prim_email);
        } catch (OXException e) {
            LOG.error("error loading user object from session", e);
            throw MailExceptionCode.SPAM_HANDLER_INIT_FAILED.create(e,"error loading user object from session");
        } catch (IOException e) {
            LOG.error("error sending request to xmlrpc service",e);
            throw MailExceptionCode.SPAM_HANDLER_INIT_FAILED.create(e,"error loading user object from session");
        } catch (ParserConfigurationException e) {
            LOG.error("error parsing response from xmlrpc service",e);
            throw MailExceptionCode.SPAM_HANDLER_INIT_FAILED.create(e,"error loading user object from session");
        } catch (SAXException e) {
            LOG.error("error parsing response from xmlrpc service",e);
            throw MailExceptionCode.SPAM_HANDLER_INIT_FAILED.create(e,"error loading user object from session");
        } catch (URISyntaxException e) {
            LOG.error("error parsing users smtp server as xmlrpc host",e);
            throw MailExceptionCode.SPAM_HANDLER_INIT_FAILED.create(e,"error loading user object from session");
        } finally {
            // free http client
            if (http_client!=null){
                try {
                    http_client.close();
                } catch (IOException e) {
                    // ignore
                }
                http_client = null;
            }
        }
    }

    private User getUser(Session session) throws OXException {
        if (session instanceof ServerSession) {
            return ((ServerSession) session).getUser();
        }

        return userService.getUser(session.getUserId(), session.getContextId());
    }

    private static String getXmlRpcRequestBody(final String primary_mail){
        // make the xml-rpc request
        final StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\"?>");
        sb.append("<methodCall>");
        sb.append("<methodName>pem.spamassassin</methodName>");
        sb.append("<params>");
        sb.append("<param>");
        sb.append("<value><struct>");
        sb.append("<member><name>mailbox</name>");
        sb.append("<value><string>");
        sb.append(primary_mail);
        sb.append("</string></value>");
        sb.append("</member>");
        sb.append("</struct></value>");
        sb.append("</param>");
        sb.append("</params>");
        sb.append("</methodCall>");
        return sb.toString();
    }

    private static SpamdProvider getSpamdProvider(final String hostname,final int port, final String username) {
        return new SpamdProvider() {

            @Override
            public String getHostname() {
                return hostname;
            }

            @Override
            public int getPort() {
                return port; // 783
            }

            @Override
            public String getUsername() {
                return username;
            }
        };
    }
}
