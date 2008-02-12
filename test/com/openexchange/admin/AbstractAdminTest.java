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

package com.openexchange.admin;

import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Vector;
import junit.framework.TestCase;

/**
 *
 * @author cutmasta
 */
public abstract class AbstractAdminTest extends TestCase{
    
    protected  static String TEST_DOMAIN = "example.org";
    protected  static String change_suffix = "_changed";
    
    @SuppressWarnings("unchecked")
    protected static void parseResponse(Vector resp) throws Exception{
        if(resp.size()==0){
            throw new Exception("Invalid Response:");
        }else{
            if(!(resp.get(0)!=null && resp.get(0).toString().equals("OK"))){                
                if(resp.size()>1){                
                    throw new Exception ("Error: "+resp.get(1));
                }else{
                    throw new Exception("Error without message");
                }
            }
        }
    }
    
    protected static String checkHost(String host) throws Exception{
        if(!host.startsWith("rmi://")){
            host = "rmi://"+host;
        }
        if(!host.endsWith("/")){
            host = host+"/";
        }
        return host;
    }
    
    protected static void log(Object obj){
        System.out.println(""+obj);
    }
    
    protected static void compareStringArray(String[] a,String[]b){
        if(a==null){
            assertNotNull("expected null array",b);
        }else{
            assertNotNull("array is null",b);
        }
        assertEquals("expected same size",a.length,b.length);
        SortedSet<String> aa = new TreeSet<String>();
        SortedSet<String> bb = new TreeSet<String>();
        for(int cc = 0;cc<a.length;cc++){
            aa.add(a[cc]);
            bb.add(b[cc]);
        }
//        boolean test = aa.(bb);
        assertEquals(aa,bb);
    }
   
}
