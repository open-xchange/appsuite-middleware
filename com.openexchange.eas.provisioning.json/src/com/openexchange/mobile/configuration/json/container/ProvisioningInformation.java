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

package com.openexchange.mobile.configuration.json.container;

import java.util.HashMap;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.user.User;

/**
 *
 * @author <a href="mailto:benjamin.otterbach@open-xchange.com">Benjamin Otterbach</a>
 *
 */
public class ProvisioningInformation {

	private String target;
	private String url;
	private String urlEncoding;
	private String mailFrom;

	private final HashMap<String, ProvisioningEmailMessage> provisioningEmailMessages;
	private final HashMap<String, ProvisioningSMSMessage> provisioningSMSMessages;

	private ServerSession session;
	private Context ctx;
	private User user;

	public ProvisioningInformation(
			String target,
			String url,
			String urlEncoding,
			String mailFrom,
			HashMap<String, ProvisioningEmailMessage> provisioningEmailMessages,
			HashMap<String, ProvisioningSMSMessage> provisioningSMSMessages,
			ServerSession session, Context ctx, User user) {
		this.target = target;
		this.url = url;
		this.urlEncoding = urlEncoding;
		this.mailFrom = mailFrom;
		this.provisioningEmailMessages = provisioningEmailMessages;
		this.provisioningSMSMessages = provisioningSMSMessages;
		this.session = session;
		this.ctx = ctx;
		this.user = user;
	}

	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getUrlEncoding() {
		return urlEncoding;
	}

	public void setUrlEncoding(String urlEncoding) {
		this.urlEncoding = urlEncoding;
	}

	public String getMailFrom() {
		return mailFrom;
	}

	public void setMailFrom(String mailFrom) {
		this.mailFrom = mailFrom;
	}

	public ServerSession getSession() {
		return session;
	}

	public void setSession(ServerSession session) {
		this.session = session;
	}

	public Context getCtx() {
		return ctx;
	}

	public void setCtx(Context ctx) {
		this.ctx = ctx;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public boolean containsProvisioningEmailMessage(String locale) {
		return provisioningEmailMessages.containsKey(locale);
	}

	public ProvisioningEmailMessage getProvisioningEmailMessage(String locale) {
		return provisioningEmailMessages.get(locale);
	}

	public boolean containsProvisioningSMSMessage(String locale) {
		return provisioningSMSMessages.containsKey(locale);
	}

	public ProvisioningSMSMessage getProvisioningSMSMessage(String locale) {
		return provisioningSMSMessages.get(locale);
	}

}
