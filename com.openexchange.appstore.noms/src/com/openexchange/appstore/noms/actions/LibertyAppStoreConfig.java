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

package com.openexchange.appstore.noms.actions;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collection;

import org.apache.commons.codec.binary.Base64;

/**
 * {@link LibertyAppStoreConfig}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class LibertyAppStoreConfig {
	private String url, storefront, gateway, gatewayQuery, userName, password;
	
	public LibertyAppStoreConfig(String url, String storefront, String gateway, String gatewayQuery, String userName, String password) {
		super();
		this.url = url;
		this.storefront = storefront;
		this.gateway = gateway;
		this.gatewayQuery = gatewayQuery;
		this.userName = userName;
		this.password = password;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
	
	public String getStorefront() {
		return storefront;
	}
	
	public String getStorefrontWithSubstitutions() {
		String ret = getStorefront();
		ret = ret.replace("[user]", enc(getUserName()));
		ret = ret.replace("[password]", enc(getPassword()));
		return ret;
	}

	public void setStorefront(String storefront) {
		this.storefront = storefront;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getAppLink(String app, String path) {
		String query = gatewayQuery.replace("[app]", enc(app)).replace("[path]", path).replace("[user]", enc(getUserName())).replace("[password]", enc(getPassword()));
	
		try {
			return gateway + "?coded=" + URLEncoder.encode(Base64.encodeBase64String(query.getBytes()), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}
		
	}

	public String getMarkup(String parameter) {
		return parameter.replace("[user]", enc(getUserName())).replace("[password]", enc(getPassword()));
	}
	
	private String enc(String s) {
		try {
			return URLEncoder.encode(s, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			return s;
		}
	}
	
}
