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

package com.openexchange.ajax.framework;

import java.util.Date;

import junit.framework.Assert;

import com.openexchange.ajax.container.Response;
import com.openexchange.groupware.AbstractOXException;

/**
 * This class implements inheritable methods for AJAX responses.
 * 
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public abstract class AbstractAJAXResponse extends Assert {

	private final Response response;

	private long requestDuration;

	private long parseDuration;

	protected AbstractAJAXResponse(final Response response) {
		super();
		this.response = response;
	}

	/**
	 * Gets the sole request's duration in milliseconds
	 * 
	 * @return The sole request's duration in milliseconds
	 */
	public long getRequestDuration() {
		return requestDuration;
	}

	/**
	 * Sets the sole request's duration in milliseconds
	 * 
	 * @param duration
	 *            The sole request's duration in milliseconds
	 */
	void setRequestDuration(final long duration) {
		this.requestDuration = duration;
	}

	/**
	 * Gets the parse duration of request's JSON data in milliseconds
	 * 
	 * @return The parse duration of request's JSON data in milliseconds
	 */
	public long getParseDuration() {
		return parseDuration;
	}

	/**
	 * Sets the parse duration of request's JSON data in milliseconds
	 * 
	 * @param parseDuration
	 *            The parse duration of request's JSON data in milliseconds
	 */
	void setParseDuration(final long parseDuration) {
		this.parseDuration = parseDuration;
	}

	/**
	 * Gets the total duration (request + parse duration)
	 * 
	 * @return The total duration
	 */
	public long getTotalDuration() {
		return (requestDuration + parseDuration);
	}

	public Response getResponse() {
		return response;
	}

	public Object getData() {
		return response.getData();
	}

	/**
	 * If the server response contains an overall timestamp, it can be get with
	 * this method.
	 * 
	 * @return the timestamp from the response.
	 */
	public Date getTimestamp() {
		return response.getTimestamp();
	}

	public boolean hasError() {
		return response.hasError();
	}

	public AbstractOXException getException() {
		return response.getException();
	}

	public int[] getTruncatedIds() {
		return response.getException().getTruncatedIds();
	}
}
