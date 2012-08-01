

package com.openexchange.custom.parallels.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.methods.PostMethod;
import org.osgi.framework.ServiceException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.ldap.User;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.session.Session;
import com.openexchange.spamhandler.spamassassin.api.SpamdProvider;
import com.openexchange.spamhandler.spamassassin.api.SpamdService;


public class ParallelsSpamdService implements SpamdService {

    private static final ParallelsSpamdService singleton = new ParallelsSpamdService();
    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(ParallelsSpamdService.class);
    private static final String POA_SPAM_PROVIDER_ATTRIBUTE_NAME = "POA_SPAM_PROVIDER";

    /**
     * Gets the singleton instance of {@link ParallelsSpamdService}.
     * 
     * @return The singleton instance of {@link ParallelsSpamdService}
     */
    public static ParallelsSpamdService getInstance() {
        return singleton;
    }

    @Override
    public SpamdProvider getProvider(final Session session) throws OXException{

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

        HttpClient http_client = new HttpClient();

        final PostMethod post_method = new PostMethod();

        try {

            // spamd port from configuration
            int spamd_provider_port = 783; // default port of spamd process
            try {
                spamd_provider_port = Integer.parseInt(ParallelsOXAuthentication.getFromConfig("com.openexchange.spamhandler.spamassassin.port"));

            } catch (final NumberFormatException e1) {
                LOG.error("error reading mandatory spamd port in antispam plugin, using fallback port "+spamd_provider_port+" now",e1);
            } catch (final ServiceException e1) {
                LOG.error("error reading mandatory spamd port in antispam plugin, using fallback port "+spamd_provider_port+" now",e1);
            }

            if(LOG.isDebugEnabled()){
                LOG.debug("Using port "+spamd_provider_port+" for connections to spamd service");

            }

            // get the user object from the OX API to retrieve users primary mail address
            final User oxuser = ParallelsInfoServlet.getUserObjectFromSession(session);



            // get all needed infos to make the xmlrpc request
            final String xml_rpc_prim_email = oxuser.getMail(); // primary mail of the user for the xml-rpc request to made

            final java.net.URI tp = new java.net.URI(oxuser.getSmtpServer());// this will always be the smtp://host:port of the user
            final String xmlrpc_server = tp.getHost();
            int xml_rpc_port = 3100; // xmlrpc service port to connect to
            try {
                xml_rpc_port = Integer.parseInt(ParallelsOXAuthentication.getFromConfig("com.openexchange.custom.parallels.antispam.xmlrpc.port"));
            } catch (final NumberFormatException e1) {
                LOG.error("error reading mandatory xmlrpc port in antispam plugin, using fallback port "+xml_rpc_port+" now",e1);
            } catch (final ServiceException e) {
                LOG.error("error reading mandatory xmlrpc port in antispam plugin, using fallback port "+xml_rpc_port+" now",e);
            }
            if(LOG.isDebugEnabled()){
                LOG.debug("Using port "+xml_rpc_port+" for connections to xmlrpc service");

            }


            final String URL_to_xmlrpc = "http://" +xmlrpc_server+":"+xml_rpc_port;
            post_method.setURI(new URI(URL_to_xmlrpc));
            post_method.setRequestHeader("Content-type", "text/xml;");
            post_method.setRequestBody(getXmlRpcRequestBody(xml_rpc_prim_email));

            if(LOG.isDebugEnabled()){
                LOG.debug("Using "+URL_to_xmlrpc+" to connect to xmlrpc service");
                LOG.debug("Using email address "+xml_rpc_prim_email+" for xmlrpc request");
            }


            http_client.executeMethod(post_method);

            final String xml_rpc_response = post_method.getResponseBodyAsString();

            if(LOG.isDebugEnabled()){
                LOG.debug("Got response from xmlrpc service:");
                LOG.debug(xml_rpc_response);
            }


            // check if contains a "faultcode" part, if no, parse for data
            if(!xml_rpc_response.contains("<name>faultCode</name>")){

                final DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
                final DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
                final Document doc = docBuilder.parse (new ByteArrayInputStream(xml_rpc_response.getBytes()));

                // normalize text representation
                doc.getDocumentElement().normalize();
                // get member element list to loop through
                final NodeList listOfMembers = doc.getElementsByTagName("member");
                // for each member look to its name
                String response_server = null;
                String response_user = null;
                for(int s=0; s<listOfMembers.getLength() ; s++){


                    final Node firstMemberNode = listOfMembers.item(s);
                    if(firstMemberNode.getNodeType() == Node.ELEMENT_NODE){


                        final Element firstMemberElement = (Element)firstMemberNode;


                        final NodeList firstMemberList = firstMemberElement.getElementsByTagName("name");
                        final Element firstNameElement = (Element)firstMemberList.item(0);
                        final NodeList textFNList = firstNameElement.getChildNodes();


                        final NodeList firstMemberList_ = firstMemberElement.getElementsByTagName("string");
                        final Element firstNameElement_ = (Element)firstMemberList_.item(0);
                        final NodeList textFNList_ = firstNameElement_.getChildNodes();

                        if((textFNList.item(0)).getNodeValue().trim().equals("server")){
                            // server ip ==
                            response_server = (textFNList_.item(0)).getNodeValue().trim();
                            if(LOG.isDebugEnabled()){
                                LOG.debug("Returning "+response_server+" as host to spamhandler");
                            }
                        }else{
                            // username ==
                            response_user = (textFNList_.item(0)).getNodeValue().trim();
                            if(LOG.isDebugEnabled()){
                                LOG.debug("Returning "+response_user+" as userame to spamhandler");
                            }
                        }

                    }
                }
                final SpamdProvider sp_provider = getSpamdProvider(response_server, spamd_provider_port, response_user);

                if(LOG.isDebugEnabled()){
                    LOG.debug("Returning spamprovider informations from xmlrpc response");
                }
                // return spamprovider to api
                return sp_provider;
            }else{
                LOG.error("got error response from xml-rpc service for primary mail "+xml_rpc_prim_email);
                LOG.error(xml_rpc_response);
                throw MailExceptionCode.SPAM_HANDLER_INIT_FAILED.create("got error response from xml-rpc service for primary mail "+xml_rpc_prim_email);
            }

        } catch (final OXException e) {
            LOG.fatal("error loading user object from session", e);
            throw MailExceptionCode.SPAM_HANDLER_INIT_FAILED.create(e,"error loading user object from session");
        } catch (final URIException e) {
            LOG.error("error sending request to xmlrpc service",e);
            throw MailExceptionCode.SPAM_HANDLER_INIT_FAILED.create(e,"error loading user object from session");
        } catch (final HttpException e) {
            LOG.error("error sending request to xmlrpc service",e);
            throw MailExceptionCode.SPAM_HANDLER_INIT_FAILED.create(e,"error loading user object from session");
        } catch (final IOException e) {
            LOG.error("error sending request to xmlrpc service",e);
            throw MailExceptionCode.SPAM_HANDLER_INIT_FAILED.create(e,"error loading user object from session");
        } catch (final ParserConfigurationException e) {
            LOG.error("error parsing response from xmlrpc service",e);
            throw MailExceptionCode.SPAM_HANDLER_INIT_FAILED.create(e,"error loading user object from session");
        } catch (final SAXException e) {
            LOG.error("error parsing response from xmlrpc service",e);
            throw MailExceptionCode.SPAM_HANDLER_INIT_FAILED.create(e,"error loading user object from session");
        } catch (final URISyntaxException e) {
            LOG.error("error parsing users smtp server as xmlrpc host",e);
            throw MailExceptionCode.SPAM_HANDLER_INIT_FAILED.create(e,"error loading user object from session");
        }finally{
            // free http client
            if(http_client!=null){
                http_client = null;
            }
        }
    }

    private String getXmlRpcRequestBody(final String primary_mail){

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

    private SpamdProvider getSpamdProvider(final String hostname,final int port, final String username) {
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
