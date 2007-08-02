/*
 * OPEN-XCHANGE - "the communication and information enviroment"
 *
 * All intellectual property rights in the Software are protected by
 * international copyright laws.
 *
 * OPEN-XCHANGE is a trademark of Netline Internet Service GmbH and all other
 * brand and product names are or may be trademarks of, and are used to identify
 * products or services of, their respective owners.
 *
 * Please make sure that third-party modules and libraries are used according to
 * their respective licenses.
 *
 * Any modifications to this package must retain all copyright notices of the
 * original copyright holder(s) for the original code used.
 *
 * After any such modifications, the original code will still remain copyrighted
 * by the copyright holder(s) or original author(s).
 *
 * Copyright (C) 1998 - 2005 Netline Internet Service GmbH
 * mail:                    info@netline-is.de
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 */

package com.openexchange.webdav.xml.parser;

import com.openexchange.groupware.Types;
import com.openexchange.groupware.container.AppointmentObject;
import com.openexchange.groupware.container.ContactObject;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.ldap.Group;
import com.openexchange.groupware.ldap.Resource;
import com.openexchange.groupware.ldap.ResourceGroup;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.test.TestException;
import com.openexchange.webdav.xml.XmlServlet;
import com.openexchange.webdav.xml.fields.DataFields;
import com.openexchange.webdav.xml.types.Response;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.xmlpull.v1.XmlPullParserException;

/**
 * Parses a Webdav XML response into the Response object.
 * @author <a href="mailto:sebastian.kauss@open-xchange.org">Sebastian Kauss</a>
 */

public class ResponseParser {
	
	public static final Namespace webdav = Namespace.getNamespace("D", "DAV:");
	
	public static Response[] parse(final Document doc, int module) throws Exception {
		return parse(doc, module, false);
	}
	
	public static Response[] parse(final Document doc, int module, boolean bList) throws Exception {
		Element rootElement = doc.getRootElement();
		List responseElements = rootElement.getChildren("response", webdav);
		
		Response[] response = null;
		
		if (module == Types.GROUPUSER) {
			if (responseElements.size() == 1) {
				response = parseGroupUserResponse((Element)responseElements.get(0));
			} else {
				throw new TestException("invalid number of response elements in response!");
			}
		} else {
			response = new Response[responseElements.size()];
			
			for (int a = 0; a < response.length; a++) {
				response[a] = parseResponse((Element)responseElements.get(a), module, bList);
			}
		}
		
		return response;
	}
	
	protected static Response parseResponse(Element eResponse, int module, boolean bList) throws Exception {
		Response response = new Response();
		
		Element ePropstat = eResponse.getChild("propstat", webdav);
		Element eProp = ePropstat.getChild("prop", webdav);
		
		if (bList) {
			response.setDataObject(parseList(eProp));
		} else {
			switch (module) {
				case Types.APPOINTMENT:
					response.setDataObject(parseAppointmentResponse(eProp));
					break;
				case Types.CONTACT:
					response.setDataObject(parseContactResponse(eProp));
					break;
				case Types.FOLDER:
					response.setDataObject(parseFolderResponse(eProp));
					break;
				case Types.TASK:
					response.setDataObject(parseTaskResponse(eProp));
					break;
				default:
					throw new TestException("invalid module!");
			}
		}
		
		Element eStatus = ePropstat.getChild("status", webdav);
		Element eResponsedescription = ePropstat.getChild("responsedescription", webdav);
		
		response.setStatus(Integer.valueOf(eStatus.getValue()));
		
		String responseDescription = eResponsedescription.getValue();
		
		int status = Integer.parseInt(eStatus.getValue());
		if (status != 200) {
			response.setErrorMessage(responseDescription);
		}
		
		return response;
	}
	
	protected static int[] parseList(final Element eProp) throws Exception {
		final Element eIdList = eProp.getChild("object_list", XmlServlet.NS);
		final List idList = eIdList.getChildren(DataFields.ID, XmlServlet.NS);
		int[] idArray = new int[idList.size()];
		for (int a = 0; a < idList.size(); a++) {
			final Element eId = (Element)idList.get(a);
			idArray[a] = DataParser.getValueAsInt(eId);
		}
		return idArray;
	}
	
	protected static Response[] parseGroupUserResponse(Element eResponse) throws Exception {
		Element ePropstat = eResponse.getChild("propstat", webdav);
		Element eProp = ePropstat.getChild("prop", webdav);
		Element eUsers = eProp.getChild("users", XmlServlet.NS);
		Element eGroups = eProp.getChild("groups", XmlServlet.NS);
		Element eResources = eProp.getChild("resources", XmlServlet.NS);
		Element eResourcegroups = eProp.getChild("resourcegroups", XmlServlet.NS);
		
		List userList = null;
		
		if (eUsers != null) {
			userList = eUsers.getChildren("user", XmlServlet.NS);
		} else {
			userList = new ArrayList();
		}
		
		
		List groupList = null;
		
		if (eGroups != null) {
			groupList = eGroups.getChildren("group", XmlServlet.NS);
		} else {
			groupList = new ArrayList();
		}
		
		List resourceList = null;
		
		if (eResources != null) {
			resourceList = eResources.getChildren("resource", XmlServlet.NS);
		} else {
			resourceList = new ArrayList();
		}
		
		List resourcegroupList = null;
		if (eResourcegroups != null) {
			resourcegroupList = eResourcegroups.getChildren("resourcegroup", XmlServlet.NS);
		} else {
			resourcegroupList = new ArrayList();
		}
		
		Response[] response = new Response[userList.size() + groupList.size() + resourceList.size() + resourcegroupList.size()];
		
		int counter = 0;
		
		for (int a = 0; a < userList.size(); a++) {
			response[counter] = new Response();
			response[counter].setDataObject(parseUserResponse((Element)userList.get(a)));
			response[counter].setStatus(200);
			
			counter++;
		}
		
		for (int a = 0; a < groupList.size(); a++) {
			response[counter] = new Response();
			response[counter].setDataObject(parseGroupResponse((Element)groupList.get(a)));
			response[counter].setStatus(200);
			
			counter++;
		}
		
		for (int a = 0; a < resourceList.size(); a++) {
			response[counter] = new Response();
			response[counter].setDataObject(parseResourceResponse((Element)resourceList.get(a)));
			response[counter].setStatus(200);
			
			counter++;
		}
		
		for (int a = 0; a < resourcegroupList.size(); a++) {
			response[counter] = new Response();
			response[counter].setDataObject(parseResourceGroupResponse((Element)resourcegroupList.get(a)));
			response[counter].setStatus(200);
			
			counter++;
		}
		
		
		return response;
	}
	
	protected static AppointmentObject parseAppointmentResponse(Element eProp) throws Exception {
		AppointmentObject appointmentObj = new AppointmentObject();
		AppointmentParser appointmentParser = new AppointmentParser();
		appointmentParser.parse(appointmentObj, eProp);
		return appointmentObj;
	}
	
	protected static ContactObject parseContactResponse(Element eProp) throws Exception {
		ContactObject contactObj = new ContactObject();
		ContactParser contactParser = new ContactParser();
		contactParser.parse(contactObj, eProp);
		return contactObj;
	}
	
	protected static FolderObject parseFolderResponse(Element eProp) throws Exception {
		FolderObject folderObj = new FolderObject();
		FolderParser folderParser = new FolderParser();
		folderParser.parse(folderObj, eProp);
		return folderObj;
	}
	
	protected static Task parseTaskResponse(Element eProp) throws Exception {
		Task taskObj = new Task();
		TaskParser taskParser = new TaskParser();
		taskParser.parse(taskObj, eProp);
		return taskObj;
	}
	
	protected static ContactObject parseUserResponse(Element eProp) throws Exception {
		ContactObject contactObj = new ContactObject();
		ContactParser contactParser = new ContactParser();
		contactParser.parse(contactObj, eProp);
		
		HashMap hm = new HashMap();
		
		if (DataParser.hasElement(eProp.getChild("myidentity", XmlServlet.NS))) {
			hm.put("myidentity", DataParser.getValue(eProp.getChild("myidentity", XmlServlet.NS)));
		}
		
		if (DataParser.hasElement(eProp.getChild("context_id", XmlServlet.NS))) {
			hm.put("context_id", DataParser.getValue(eProp.getChild("context_id", XmlServlet.NS)));
		}
		
		contactObj.setMap(hm);
		
		return contactObj;
	}
	
	protected static Group parseGroupResponse(Element eProp) throws Exception {
		Group groupObj = new Group();
		GroupParser groupParser = new GroupParser();
		groupParser.parse(groupObj, eProp);
		return groupObj;
	}
	
	protected static Resource parseResourceResponse(Element eProp) throws Exception {
		Resource resourceObj = new Resource();
		ResourceParser resourceParser = new ResourceParser();
		resourceParser.parse(resourceObj, eProp);
		return resourceObj;
	}
	
	protected static ResourceGroup parseResourceGroupResponse(Element eProp) throws Exception {
		ResourceGroup resourcegroupObj = new ResourceGroup();
		ResourceGroupParser resourcegroupParser = new ResourceGroupParser();
		resourcegroupParser.parse(resourcegroupObj, eProp);
		return resourcegroupObj;
	}
}
