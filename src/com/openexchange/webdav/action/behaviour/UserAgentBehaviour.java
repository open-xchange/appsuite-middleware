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

package com.openexchange.webdav.action.behaviour;


import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import com.openexchange.configuration.ConfigurationException;
import com.openexchange.configuration.ConfigurationException.Code;
import com.openexchange.webdav.action.WebdavRequest;


public class UserAgentBehaviour implements Behaviour{

	private Map<Class<? extends Object>, Object> classes = null;
	private Pattern pattern;
	
	public UserAgentBehaviour(String userAgentPattern, Object...implementations) throws ConfigurationException {
		setPattern(userAgentPattern);
		setChanges(new HashSet<Object>(Arrays.asList(implementations)));
	}
	
	public UserAgentBehaviour(){
		
	}

	
	public void setPattern(String userAgentPattern) {
		pattern = Pattern.compile(userAgentPattern);
	}
	
	public void setChanges(Set<Object> implementations) throws ConfigurationException {
		classes = new HashMap<Class<? extends Object>, Object>();
		
		for(Object object : implementations) {
			
			Class<? extends Object> addMe = object.getClass();
			while(addMe != null) {
				Class[] interfaces = addMe.getInterfaces();
				for(Class<? extends Object> iFace : interfaces) {
					if(classes.get(iFace) != null) {
						throw new ConfigurationException(Code.INVALID_CONFIGURATION, "Two implemenations for "+iFace);
					} 
					classes.put(iFace, object);
				}
				addMe = addMe.getSuperclass();
			}
		}
	}
	
	public void setChange(Object implementation) throws ConfigurationException {
		setChanges(new HashSet(Arrays.asList(implementation)));
	}

	public <T> T get(Class<T> clazz) {
		return (T) classes.get(clazz);
	}

	public boolean matches(WebdavRequest req) {
		return pattern.matcher(req.getHeader("user-agent")).find();
	}

	public Set<Class<? extends Object>> provides() {
		return classes.keySet();
	}
	
	public String toString(){
		return "UserAgent matcher: "+pattern.toString();
	}

}
