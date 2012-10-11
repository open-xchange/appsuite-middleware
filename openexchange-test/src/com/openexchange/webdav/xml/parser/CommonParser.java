/*
 *
 *    OPEN-XCHANGE - "the communication and information enviroment"
 *
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    OPEN-XCHANGE is a trademark of Netline Internet Service GmbH and all
 *    other brand and product names are or may be trademarks of, and are
 *    used to identify products or services of, their respective owners.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original code will still remain
 *    copyrighted by the copyright holder(s) or original author(s).
 *
 *
 *     Copyright (C) 1998 - 2005 Netline Internet Service GmbH
 *     mail:	                 info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License as published by the Free
 *     Software Foundation; either version 2 of the License, or (at your option)
 *     any later version.
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
 *
 */

package com.openexchange.webdav.xml.parser;

import java.util.List;
import org.jdom2.Element;
import com.openexchange.groupware.container.CommonObject;
import com.openexchange.webdav.xml.XmlServlet;
import com.openexchange.webdav.xml.fields.CommonFields;

/**
 * CommonParser
 *
 * @author <a href="mailto:sebastian.kauss@open-xchange.com">Sebastian Kauss</a>
 */

public abstract class CommonParser extends FolderChildParser {

	protected void parseElementCommon(final CommonObject commonobject, final Element eProp) {
		if (hasElement(eProp.getChild(CommonFields.CATEGORIES, XmlServlet.NS))) {
			commonobject.setCategories(getValue(eProp.getChild(CommonFields.CATEGORIES, XmlServlet.NS)));
		}

		if (hasElement(eProp.getChild(CommonFields.PRIVATE_FLAG, XmlServlet.NS))) {
			commonobject.setPrivateFlag(getValueAsBoolean(eProp.getChild(CommonFields.PRIVATE_FLAG, XmlServlet.NS)));
		}

		if (hasElement(eProp.getChild(CommonFields.COLORLABEL, XmlServlet.NS))) {
			commonobject.setLabel(getValueAsInt(eProp.getChild(CommonFields.COLORLABEL, XmlServlet.NS)));
		}

		if (hasElement(eProp.getChild("attachments", XmlServlet.NS))) {
			parseElementAttachments(commonobject, eProp.getChild("attachments", XmlServlet.NS));
		}

		parseElementFolderChildObject(commonobject, eProp);
	}

	protected void parseElementAttachments(final CommonObject commonobject, final Element eAttachments) {
		final List elementEntries = eAttachments.getChildren("attachment", XmlServlet.NS);

		if (elementEntries == null) {
			commonobject.setNumberOfAttachments(0);
		} else {
			commonobject.setNumberOfAttachments(elementEntries.size());
		}
	}
}




