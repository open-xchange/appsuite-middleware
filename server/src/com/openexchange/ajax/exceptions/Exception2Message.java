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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.ajax.exceptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.openexchange.api.OXConflictException;
import com.openexchange.api.OXObjectNotFoundException;
import com.openexchange.api.OXPermissionException;
import com.openexchange.api2.OXConcurrentModificationException;
import com.openexchange.api2.OXException;

public class Exception2Message {
	
	public static final class ErrorMessage{
		public String error;
		public String[] error_params;
		
		public ErrorMessage(final String error) {
			this(error, new String[0]);
		}
		
		public ErrorMessage(final String error, final String...error_params) {
			this.error = error;
			this.error_params = error_params;
		}
		
		public JSONObject toJSONObject() throws JSONException{
			final JSONObject responseObject = new JSONObject();
			responseObject.put("error", error);
			final JSONArray params = new JSONArray();
			for (String param : error_params) {
				params.put(param);
			}
			responseObject.put("error_params", params);
			return responseObject;
		}
	}
	
	/**
	 * 
	 * @param The OX Exception
	 * @return the corresponding error message
	 */
	public ErrorMessage getMessage(final OXException x) {
		if(x instanceof OXObjectNotFoundException) {
			return objectNotFound((OXObjectNotFoundException) x);
		} else if (x instanceof OXConcurrentModificationException ){
			return concurrentModification((OXConcurrentModificationException)x);
		} else if (x instanceof OXConflictException) {
			return conflictException((OXConflictException)x);
		} else if (x instanceof OXPermissionException) {
			return permissionException((OXPermissionException)x);
		} else {
			return exception(x);
		}
	}
	
	protected ErrorMessage permissionException(final OXPermissionException exception) {
		return new ErrorMessage("You do not have the required permissions.");
	}

	protected ErrorMessage conflictException(final OXConflictException exception) {
		return new ErrorMessage("Conflict.");
	}

	protected ErrorMessage concurrentModification(final OXConcurrentModificationException exception) {
		return new ErrorMessage("Concurrent Modification");
	}

	protected ErrorMessage objectNotFound(final OXObjectNotFoundException exception) {
		return new ErrorMessage("No object with the given id could be found");
	}
	
	protected ErrorMessage exception(final OXException x) {
		return new ErrorMessage(x.toString());
	}

	
}
