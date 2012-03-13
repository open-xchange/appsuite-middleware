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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.subscribe.json.actions;

import java.util.List;

import org.json.JSONArray;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.server.ServiceLookup;
import com.openexchange.subscribe.SubscriptionSource;
import com.openexchange.subscribe.json.SubscriptionSourceJSONWriter;

/**
 *
 * @author <a href="mailto:karsten.will@open-xchange.com">Karsten Will</a>
 *
 */
public class ListSourcesAction extends AbstractSubscribeSourcesAction{

	public ListSourcesAction(ServiceLookup services) {
		this.services = services;
	}

	@Override
	public AJAXRequestResult perform(SubscribeRequest subscribeRequest)
			throws OXException {
		final int module = getModule(subscribeRequest.getRequestData().getModule());
        final List<SubscriptionSource> sources = getAvailableSources(subscribeRequest.getServerSession()).getSources(module);
        final String[] columns = new String[]{"id", "displayName", "icon", "module",  "formDescription"};
        final JSONArray json = new SubscriptionSourceJSONWriter(createTranslator(subscribeRequest.getServerSession())).writeJSONArray(sources, columns);
//        JSONArray json;
//		try {
//			json = new JSONArray("[[\"com.openexchange.subscribe.crawler.google.calendar\",\"GoogleCalendar\",null,\"calendar\",[{\"mandatory\":true,\"name\":\"login\",\"displayName\":\"Login\",\"widget\":\"input\"},{\"mandatory\":true,\"name\":\"password\",\"displayName\":\"Password\",\"widget\":\"password\"}]],[\"com.openexchange.subscribe.crawler.googlemail\",\"GoogleMail\",null,\"contacts\",[{\"mandatory\":true,\"name\":\"login\",\"displayName\":\"Login\",\"widget\":\"input\"},{\"mandatory\":true,\"name\":\"password\",\"displayName\":\"Password\",\"widget\":\"password\"}]],[\"com.openexchange.subscribe.microformats.contacts.http\",\"OXMF Contacts\",null,\"contacts\",[{\"mandatory\":true,\"name\":\"url\",\"displayName\":\"URL\",\"widget\":\"input\"}]],[\"com.openexchange.subscribe.microformats.infostore.http\",\"OXMF Infostore\",null,\"infostore\",[{\"mandatory\":true,\"name\":\"url\",\"displayName\":\"URL\",\"widget\":\"input\"}]],[\"com.openexchange.subscribe.crawler.suncalendar\",\"Sun Calendar\",null,\"calendar\",[{\"mandatory\":true,\"name\":\"login\",\"displayName\":\"Login\",\"widget\":\"input\"},{\"mandatory\":true,\"name\":\"password\",\"displayName\":\"Password\",\"widget\":\"password\"}]],[\"com.openexchange.subscribe.crawler.suncontacts\",\"Sun Contacts\",null,\"contacts\",[{\"mandatory\":true,\"name\":\"login\",\"displayName\":\"Login\",\"widget\":\"input\"},{\"mandatory\":true,\"name\":\"password\",\"displayName\":\"Password\",\"widget\":\"password\"}]],[\"com.openexchange.subscribe.crawler.suntasks\",\"Sun Tasks\",null,\"tasks\",[{\"mandatory\":true,\"name\":\"login\",\"displayName\":\"Login\",\"widget\":\"input\"},{\"mandatory\":true,\"name\":\"password\",\"displayName\":\"Password\",\"widget\":\"password\"}]],[\"com.openexchange.subscribe.xing\",\"XING\",null,\"contacts\",[{\"mandatory\":true,\"name\":\"login\",\"displayName\":\"Login\",\"widget\":\"input\"},{\"mandatory\":true,\"name\":\"password\",\"displayName\":\"Password\",\"widget\":\"password\"}]],[\"com.openexchange.subscribe.crawler.gmx.com\",\"gmx.com\",null,\"contacts\",[{\"mandatory\":true,\"name\":\"login\",\"displayName\":\"Login\",\"widget\":\"input\"},{\"mandatory\":true,\"name\":\"password\",\"displayName\":\"Password\",\"widget\":\"password\"}]],[\"com.openexchange.subscribe.crawler.gmx\",\"gmx.de\",null,\"contacts\",[{\"mandatory\":true,\"name\":\"login\",\"displayName\":\"Login\",\"widget\":\"input\"},{\"mandatory\":true,\"name\":\"password\",\"displayName\":\"Password\",\"widget\":\"password\"}]],[\"com.openexchange.subscribe.crawler.t-online.de\",\"t-online.de\",null,\"contacts\",[{\"mandatory\":true,\"name\":\"login\",\"displayName\":\"Login\",\"widget\":\"input\"},{\"mandatory\":true,\"name\":\"password\",\"displayName\":\"Password\",\"widget\":\"password\"}]],[\"com.openexchange.subscribe.crawler.webde\",\"web.de\",null,\"contacts\",[{\"mandatory\":true,\"name\":\"login\",\"displayName\":\"Login\",\"widget\":\"input\"},{\"mandatory\":true,\"name\":\"password\",\"displayName\":\"Password\",\"widget\":\"password\"}]],[\"com.openexchange.subscribe.crawler.yahoocom\",\"yahoo.com\",null,\"contacts\",[{\"mandatory\":true,\"name\":\"login\",\"displayName\":\"Login\",\"widget\":\"input\"},{\"mandatory\":true,\"name\":\"password\",\"displayName\":\"Password\",\"widget\":\"password\"}]]]");
			return new AJAXRequestResult(json, "json");
//		} catch (JSONException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//        return null;
	}

	protected int getModule(String moduleAsString) {
        if(moduleAsString == null) {
            return -1;
        }
        if(moduleAsString.equals("contacts")) {
            return FolderObject.CONTACT;
        } else if (moduleAsString.equals("calendar")) {
            return FolderObject.CALENDAR;
        } else if (moduleAsString.equals("tasks")) {
            return FolderObject.TASK;
        } else if (moduleAsString.equals("infostore")) {
            return FolderObject.INFOSTORE;
        }
        return -1;
    }
}
