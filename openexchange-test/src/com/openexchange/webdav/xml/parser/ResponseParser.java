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

import com.openexchange.api.OXException;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.container.AppointmentObject;
import com.openexchange.groupware.container.ContactObject;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.webdav.xml.types.Response;
import java.util.List;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;

/**
 * Parses a Webdav XML response into the Response object.
 * @author <a href="mailto:sebastian.kauss@open-xchange.org">Sebastian Kauss</a>
 */

public class ResponseParser {
	
	public static final Namespace webdav = Namespace.getNamespace("D", "DAV:");
	
	public static Response[] parse(final Document doc, int module) throws Exception {
		Element rootElement = doc.getRootElement();
		List responseElements = rootElement.getChildren("response", webdav);
		
		Response[] response = new Response[responseElements.size()];
		
		for (int a = 0; a < response.length; a++) {
			response[a] = parseResponse((Element)responseElements.get(a), module);
		}
		
		return null;
	}
	
	protected static Response parseResponse(Element eResponse, int module) throws Exception {
		Response response = new Response();
		
		Element ePropstat = eResponse.getChild("propstat", webdav);
		Element eProp = ePropstat.getChild("prop", webdav);
		
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
				throw new OXException("invalid module!");
		}
		
		Element eStatus = ePropstat.getChild("status", webdav);
		Element eResponsedescription = ePropstat.getChild("responsedescription", webdav);
		
		response.setStatus(Integer.valueOf(eStatus.getValue()));
		
		String responseDescription = eResponsedescription.getValue();
		if (responseDescription != null && !responseDescription.equals("OK")) {
			response.setErrorMessage(responseDescription);
		}
		
		return response;
	}
	
	protected static AppointmentObject parseAppointmentResponse(Element eProp) throws Exception {
		AppointmentObject appointmentObj = new AppointmentObject();
		AppointmentParser appointmentParser = new AppointmentParser();
		appointmentParser.parseElement(appointmentObj, eProp);
		return appointmentObj;
	}
	
	protected static ContactObject parseContactResponse(Element eProp) throws Exception {
		ContactObject contactObj = new ContactObject();
		ContactParser contactParser = new ContactParser();
		contactParser.parseElement(contactObj, eProp);
		return contactObj;
	}
	
	protected static FolderObject parseFolderResponse(Element eProp) throws Exception {
		FolderObject folderObj = new FolderObject();
		FolderParser folderParser = new FolderParser();
		folderParser.parseElement(folderObj, eProp);
		return folderObj;
	}
	
	protected static Task parseTaskResponse(Element eProp) throws Exception {
		Task taskObj = new Task();
		TaskParser taskParser = new TaskParser();
		taskParser.parseElement(taskObj, eProp);
		return taskObj;
	}
}
