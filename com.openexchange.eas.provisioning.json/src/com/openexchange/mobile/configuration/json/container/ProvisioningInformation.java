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

package com.openexchange.mobile.configuration.json.container;

import java.util.HashMap;

import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.tools.session.ServerSession;

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
