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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.openexchange.exception.OXException;
import com.openexchange.tools.servlet.AjaxExceptionCodes;

/**
 * {@link LibertyAppStoreResponse}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class LibertyAppStoreResponse {
	public enum Status {
		SUCCESS, ERROR
	};
	
	private Status status;
	private JSONArray apps;
	
	
	
	public LibertyAppStoreResponse(Status status, JSONArray apps) {
		super();
		this.status = status;
		this.apps = apps;
	}



	public static LibertyAppStoreResponse parse(String payload) throws OXException {
		try {
			int index = payload.indexOf(' ');
			JSONObject statusObject = new JSONObject(payload.substring(0, index));
			Status status;
			if (statusObject.has("success")) {
				status = Status.SUCCESS;
			} else {
				status = Status.ERROR;
			}
			
			JSONArray apps = new JSONArray(payload.substring(index+1));
			
			return new LibertyAppStoreResponse(status, apps);
			
		} catch (JSONException x) {
			throw AjaxExceptionCodes.JSON_ERROR.create(x);
		}
		
	}



	public Status getStatus() {
		return status;
	}



	public void setStatus(Status status) {
		this.status = status;
	}



	public JSONArray getApps() {
		return apps;
	}



	public void setApps(JSONArray apps) {
		this.apps = apps;
	}
	
	
	
	
}
