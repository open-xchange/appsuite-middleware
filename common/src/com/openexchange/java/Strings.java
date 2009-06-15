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

package com.openexchange.java;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;


/**
 * {@link Strings} - a library for performing operations that create Strings
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class Strings {
    
    /**
     * Joins a collection of objects by connecting the results of
     * their #toString() method with a connector
     * 
     * @param coll Collection to be connected
     * @param connector Connector place between two objects
     * @return connected strings or null if collection == null or empty string if collection is empty
     */
    public static String join(Collection<? extends Object> coll, String connector){
        if(coll == null)
            return null;
        StringBuilder builder = new StringBuilder();
        for(Object obj: coll){
            if(obj == null)
                builder.append(obj);
            else
                builder.append(obj.toString());
            builder.append(connector);
        }
        if(builder.length() == 0)
            return "";
        return builder.substring(0, builder.length() - connector.length());
    }
    
    public static <T> String join(T[] arr, String connector){
        return join( Arrays.asList(arr), connector );
    }
    
    public static String join(int[] arr, String connector){
        List<Integer> list = new LinkedList<Integer>();
        for(int i  : arr){
            list.add( Autoboxing.I(i) );
        }
        return join(list, connector);
    }
    
}
