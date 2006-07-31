package com.openexchange.webdav.xml;

import org.jdom.Element;

public abstract class CalendarTest extends AbstractWebdavTest {

	protected void confirmAppointment(int objectId) throws Exception {
		Element e_prop = new Element("prop", webdav);
		
		Element e_objectId = new Element("object_id", XmlServlet.NS);
		e_objectId.addContent(String.valueOf(objectId));
		e_prop.addContent(e_objectId);
		
		Element e_method = new Element("method", XmlServlet.NS);
		e_method.addContent("CONFIRM");
		e_prop.addContent(e_method);
		
		Element e_confirm = new Element("confirm", XmlServlet.NS);
		e_confirm.addContent("decline");
		e_prop.addContent(e_confirm);
		
		byte[] b = writeRequest(e_prop);
		sendPut(b);
	}
}

