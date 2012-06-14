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

package com.openexchange.dav.caldav;

import java.io.IOException;

import com.openexchange.dav.caldav.ical.SimpleICal;
import com.openexchange.dav.caldav.ical.SimpleICal.SimpleICalException;

/**
 * {@link ICalResource}
 * 
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class ICalResource {
	
	private final String eTag;
	private final String href;
	private final SimpleICal iCal;
	
	public ICalResource(String iCalString, String href, String eTag) throws IOException, SimpleICalException {
		super();
		this.iCal = new SimpleICal(iCalString);
		this.href = href;
		this.eTag = eTag;
	}
	
	public ICalResource(String iCalString) throws IOException, com.openexchange.dav.caldav.ical.SimpleICal.SimpleICalException {
		this(iCalString, null, null);
	}
	
	public String getUID() {
		return this.iCal.getVEvent().getPropertyValue("UID");
	}	

	public String getSummary() {
		return this.iCal.getVEvent().getPropertyValue("SUMMARY");
	}	

	public String getLocation() {
		return this.iCal.getVEvent().getPropertyValue("LOCATION");
	}
	
	/**
	 * @return the eTag
	 */
	public String getETag() {
		return eTag;
	}

	/**
	 * @return the href
	 */
	public String getHref() {
		return href;
	}

	/**
	 * @return the iCal file
	 */
	public SimpleICal getICal() {
		return iCal;
	}

	@Override
    public String toString() {
		return this.iCal.toString();		
	}
	
//	private static Date parseDate(String[] line) throws ParseException {
//        String parameter = line[1];
//        String value = line[2];
//		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd'T'HHmm'00Z'");
//        if (null != parameter && parameter.toLowerCase().startsWith("tzid=")) {
//        	String tzName = parameter.substring(5);
//    		dateFormat.setTimeZone(TimeZone.getTimeZone(tzName));
//    		dateFormat.applyPattern("yyyyMMdd'T'HHmm'00'");
//        	
//        } else {
//    		dateFormat.applyPattern("yyyyMMdd'T'HHmm'00Z'");
//    		dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
//        }        
//		return dateFormat.parse(value);
//	}

}
