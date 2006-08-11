package com.openexchange.webdav.xml;

import com.meterware.httpunit.WebConversation;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.container.ContactObject;
import com.openexchange.groupware.ldap.Group;
import com.openexchange.groupware.ldap.Resource;
import com.openexchange.groupware.ldap.ResourceGroup;
import com.openexchange.webdav.xml.parser.ResponseParser;
import com.openexchange.webdav.xml.request.PropFindMethod;
import com.openexchange.webdav.xml.types.Response;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Date;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;

public class GroupUserTest extends AbstractWebdavTest {
	
	public static final String GROUPUSER_URL = "/servlet/webdav.groupuser";

	protected void setUp() throws Exception {
		super.setUp();
	}
	
	public void testSearchUser() throws Exception {
		searchUser(webCon, "*", new Date(0), PROTOCOL + hostName, login, password);
	}
	
	public void testSearchLoginUser() throws Exception {
		ContactObject[] contactArray = searchUser(webCon, login, new Date(0), PROTOCOL + hostName, login, password);
		assertTrue("contact array size is 0", contactArray.length > 0);
		assertEquals("user id is not equals", userId, contactArray[0].getInternalUserId());
	}
	
	public void testSearchGroup() throws Exception {
		searchGroup(webCon, "*", new Date(0), PROTOCOL + hostName, login, password);
	}
	
	public void testSearchResource() throws Exception {
		searchResource(webCon, "*", new Date(0), PROTOCOL + hostName, login, password);
	}
	
	public void testSearchResourceGroup() throws Exception {
		searchResourcegroup(webCon, "*", new Date(0), PROTOCOL + hostName, login, password);
	}
	
	public static ContactObject[] searchUser(WebConversation webCon, String searchpattern, Date modified, String host, String login, String password) throws Exception {
		Element eUsers = new Element("user", XmlServlet.NS);
		eUsers.addContent(searchpattern);
		
		Document doc = addElement2PropFind(eUsers, modified);
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		XMLOutputter xo = new XMLOutputter();
		xo.output(doc, baos);
		
		baos.flush();
		
		HttpClient httpclient = new HttpClient();
		
		httpclient.getState().setCredentials(null, new UsernamePasswordCredentials(login, password));
		PropFindMethod propFindMethod = new PropFindMethod(host + GROUPUSER_URL);
		propFindMethod.setDoAuthentication( true );
		
		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		propFindMethod.setRequestBody(bais);
		
		int status = httpclient.executeMethod(propFindMethod);
		
		assertEquals("check propfind response", 207, status);
		
		byte responseByte[] = propFindMethod.getResponseBody();
		bais = new ByteArrayInputStream(responseByte);
		Response[] response = ResponseParser.parse(new SAXBuilder().build(bais), Types.GROUPUSER);
		
		ContactObject[] contactArray = new ContactObject[response.length];
		for (int a = 0; a < contactArray.length; a++) {
			if (response[a].hasError()) {
				fail("xml error: " + response[a].getErrorMessage());
			}
			
			contactArray[a] = (ContactObject)response[a].getDataObject();
		}
		
		return contactArray;
	}
	
	public static Group[] searchGroup(WebConversation webCon, String searchpattern, Date modified, String host, String login, String password) throws Exception {
		Element eGroups = new Element("group", XmlServlet.NS);
		eGroups.addContent(searchpattern);
		
		Document doc = addElement2PropFind(eGroups, modified);
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		XMLOutputter xo = new XMLOutputter();
		xo.output(doc, baos);
		
		baos.flush();
		
		HttpClient httpclient = new HttpClient();
		
		httpclient.getState().setCredentials(null, new UsernamePasswordCredentials(login, password));
		PropFindMethod propFindMethod = new PropFindMethod(host + GROUPUSER_URL);
		propFindMethod.setDoAuthentication( true );
		
		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		propFindMethod.setRequestBody(bais);
		
		int status = httpclient.executeMethod(propFindMethod);
		
		assertEquals("check propfind response", 207, status);
		
		byte responseByte[] = propFindMethod.getResponseBody();
		bais = new ByteArrayInputStream(responseByte);
		Response[] response = ResponseParser.parse(new SAXBuilder().build(bais), Types.GROUPUSER);
		
		Group[] groupArray = new Group[response.length];
		for (int a = 0; a < groupArray.length; a++) {
			if (response[a].hasError()) {
				fail("xml error: " + response[a].getErrorMessage());
			}
			
			groupArray[a] = (Group)response[a].getDataObject();
		}
		
		return groupArray;
	}
	
	public static Resource[] searchResource(WebConversation webCon, String searchpattern, Date modified, String host, String login, String password) throws Exception {
		Element eResources = new Element("resource", XmlServlet.NS);
		eResources.addContent(searchpattern);
		
		Document doc = addElement2PropFind(eResources, modified);
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		XMLOutputter xo = new XMLOutputter();
		xo.output(doc, baos);
		
		baos.flush();
		
		HttpClient httpclient = new HttpClient();
		
		httpclient.getState().setCredentials(null, new UsernamePasswordCredentials(login, password));
		PropFindMethod propFindMethod = new PropFindMethod(host + GROUPUSER_URL);
		propFindMethod.setDoAuthentication( true );
		
		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		propFindMethod.setRequestBody(bais);
		
		int status = httpclient.executeMethod(propFindMethod);
		
		assertEquals("check propfind response", 207, status);
		
		byte responseByte[] = propFindMethod.getResponseBody();
		bais = new ByteArrayInputStream(responseByte);
		Response[] response = ResponseParser.parse(new SAXBuilder().build(bais), Types.GROUPUSER);
		
		Resource[] resourceArray = new Resource[response.length];
		for (int a = 0; a < resourceArray.length; a++) {
			if (response[a].hasError()) {
				fail("xml error: " + response[a].getErrorMessage());
			}
			
			resourceArray[a] = (Resource)response[a].getDataObject();
		}
		
		return resourceArray;
	}
	
	public static ResourceGroup[] searchResourcegroup(WebConversation webCon, String searchpattern, Date modified, String host, String login, String password) throws Exception {
		Element eResourceGroups = new Element("resourcegroup", XmlServlet.NS);
		eResourceGroups.addContent(searchpattern);
		
		Document doc = addElement2PropFind(eResourceGroups, modified);
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		XMLOutputter xo = new XMLOutputter();
		xo.output(doc, baos);
		
		baos.flush();
		
		HttpClient httpclient = new HttpClient();
		
		httpclient.getState().setCredentials(null, new UsernamePasswordCredentials(login, password));
		PropFindMethod propFindMethod = new PropFindMethod(host + GROUPUSER_URL);
		propFindMethod.setDoAuthentication( true );
		
		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		propFindMethod.setRequestBody(bais);
		
		int status = httpclient.executeMethod(propFindMethod);
		
		assertEquals("check propfind response", 207, status);
		
		byte responseByte[] = propFindMethod.getResponseBody();
		bais = new ByteArrayInputStream(responseByte);
		Response[] response = ResponseParser.parse(new SAXBuilder().build(bais), Types.GROUPUSER);
		
		ResourceGroup[] resourcegroupArray = new ResourceGroup[response.length];
		for (int a = 0; a < resourcegroupArray.length; a++) {
			if (response[a].hasError()) {
				fail("xml error: " + response[a].getErrorMessage());
			}
			
			resourcegroupArray[a] = (ResourceGroup)response[a].getDataObject();
		}
		
		return resourcegroupArray;
	}
	
	public static Document addElement2PropFind(Element e, Date modified) throws Exception {
		Element ePropfind = new Element("propfind", webdav);
		Element eProp = new Element("prop", webdav);
		
		Element eLastSync = new Element("lastsync", XmlServlet.NS);
		eLastSync.addContent(String.valueOf(modified.getTime()));
		
		ePropfind.addContent(eProp);
		eProp.addContent(eLastSync);
		eProp.addContent(e);
		
		return new Document(ePropfind);
	}
}

