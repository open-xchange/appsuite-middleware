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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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

package com.openexchange.report.client.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

import javax.management.Attribute;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanException;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.TabularDataSupport;

import com.openexchange.report.client.container.ClientLoginCount;
import com.openexchange.report.client.container.ContextDetail;
import com.openexchange.report.client.container.ContextModuleAccessCombination;
import com.openexchange.report.client.container.Total;

public class ObjectHandler {
	
	protected static List<Total> getTotalObjects(MBeanServerConnection mbsc) throws AttributeNotFoundException, InstanceNotFoundException, MBeanException, ReflectionException, IOException, MalformedObjectNameException, NullPointerException {
    	TabularDataSupport data = (TabularDataSupport) mbsc.getAttribute(new ObjectName("com.openexchange.reporting", "name", "Reporting"), "Total");
        
    	List<Total> retval = new ArrayList<Total>();
    	int count = 0;
    	for (Object tmp : data.keySet()) {
        	retval.add(new Total(
        			((List<Object>) tmp).get(0).toString(),
        			((List<Object>) tmp).get(1).toString())
        	);
        	count++;
        }
    	
    	return (retval);
    }
 
	public static ClientLoginCount getClientLoginCount(MBeanServerConnection mbsc) throws IOException, InstanceNotFoundException, ReflectionException, MBeanException, MalformedObjectNameException, AttributeNotFoundException, InvalidAttributeValueException {
		
		ClientLoginCount retval = new ClientLoginCount();
		
		// method parameters for LoginCounterMBean.getNumberOfLogins()
		Object[] gnl_params    = new Object[2];
		String[] gnl_signature = new String[2];
	
		gnl_signature[0] = "java.util.Date";
		gnl_signature[1] = "java.util.Date";
		
		Calendar c = Calendar.getInstance();
		gnl_params[1] = c.getTime();  // endDate
		c.add(Calendar.DATE, -30);
		gnl_params[0] = c.getTime();  // startDate
		
		mbsc.setAttribute(new ObjectName("com.openexchange.reporting", "name", "Login Counter"), new Attribute("DeviceWildcard", "USM-EAS"));
		int usmeas = (Integer) mbsc.invoke(new ObjectName("com.openexchange.reporting", "name", "Login Counter"), "getNumberOfLogins", gnl_params, gnl_signature);
        retval.setUsmeas(Integer.toString(usmeas));
  
        mbsc.setAttribute(new ObjectName("com.openexchange.reporting", "name", "Login Counter"), new Attribute("DeviceWildcard", "OpenXchange.HTTPClient.OXAddIn"));
		int olox2 = (Integer) mbsc.invoke(new ObjectName("com.openexchange.reporting", "name", "Login Counter"), "getNumberOfLogins", gnl_params, gnl_signature);
	    retval.setOlox2(Integer.toString(olox2));
	    
		return retval;
	}
	
    protected static List<ContextDetail> getDetailObjects(MBeanServerConnection mbsc) throws AttributeNotFoundException, InstanceNotFoundException, MBeanException, ReflectionException, IOException, MalformedObjectNameException, NullPointerException {
    	TabularDataSupport data = (TabularDataSupport) mbsc.getAttribute(
    			new ObjectName("com.openexchange.reporting", "name", "Reporting"), "Detail");

    	List<ContextDetail> retval = new ArrayList<ContextDetail>();
    	for (Object tmp : data.values()) {
    		CompositeDataSupport context = (CompositeDataSupport) tmp;
    		TabularDataSupport moduleAccessCombinations =
    			(TabularDataSupport)context.get("module access combinations");

    		ContextDetail contextDetail = new ContextDetail();
    		contextDetail.setId(context.get("identifier").toString());
    		contextDetail.setAge(context.get("age").toString());
    		contextDetail.setCreated(context.get("created").toString());
    		contextDetail.setAdminmac(context.get("admin permission").toString());
    		for (Object tmp2 : moduleAccessCombinations.values()) {
    			CompositeDataSupport moduleAccessCombination = (CompositeDataSupport) tmp2;

    			contextDetail.addModuleAccessCombination(
    					new ContextModuleAccessCombination(
    							moduleAccessCombination.get("module access combination").toString(),
    							moduleAccessCombination.get("users").toString(),
    							moduleAccessCombination.get("inactive").toString()
    					));
    		}
    		retval.add(contextDetail);
    	}	

    	return (retval);
    }
    
    protected static List<List<Object>> createTotalList(List<Total> totals) {
    	List<List<Object>> retval = new ArrayList<List<Object>>();
    	retval.add(Arrays.asList((Object)"contexts", "users"));

    	for (Total tmp : totals) {
    		retval.add(Arrays.asList((Object)
    				tmp.getContexts(),
    				tmp.getUsers())
    				);
    	}

    	return retval;
    }

    protected static List<List<Object>> createDetailList(List<ContextDetail> contextDetails) {
    	List<List<Object>> retval = new ArrayList<List<Object>>();
    	retval.add(Arrays.asList((Object) "id", "age", "created", "admin permission", "module access combination", "users", "inactive" ));

    	TreeSet<Integer> sorted = new TreeSet<Integer>(new Comparator<Integer>() {
			public int compare(Integer o1, Integer o2) {
				return o1.compareTo(o2);
			}
    	});
    	
    	HashMap<String, List<List<Object>>> sortDetails = new HashMap<String, List<List<Object>>>();

    	for (ContextDetail tmp : contextDetails) {         
    		for (ContextModuleAccessCombination moduleAccessCombination : tmp.getModuleAccessCombinations()) {
    			
    			sorted.add(new Integer(tmp.getId()));
    			
    			List<List<Object>> tmpList;
    			if (!sortDetails.containsKey(tmp.getId())) {
    				tmpList = new ArrayList<List<Object>>();
    			} else {
    				tmpList = sortDetails.get(tmp.getId());
    			}
				tmpList.add(Arrays.asList((Object) 
    					new Integer(tmp.getId()),
    					tmp.getAge(),
    					tmp.getCreated(),
    					tmp.getAdminmac(),
    					moduleAccessCombination.getUserAccessCombination(),
    					moduleAccessCombination.getUserCount(),
    					moduleAccessCombination.getInactiveCount()
    				));
				sortDetails.put(tmp.getId(), tmpList);
    		}
    	}

    	for (Integer tmp : sorted) {
    		for (List<Object> tmpList : sortDetails.get(String.valueOf(tmp))) {
    			retval.add(tmpList);
    		}
    	}

    	return retval;
    }
    
    protected static List<List<Object>> createVersionList(String[] versions) {
    	List<List<Object>> retval = new ArrayList<List<Object>>();
    	retval.add(Arrays.asList((Object)"module", "version"));
    	retval.add(Arrays.asList((Object)"admin", versions[0]));
    	retval.add(Arrays.asList((Object)"groupware", versions[0]));
    	return retval;
    }

	
}
