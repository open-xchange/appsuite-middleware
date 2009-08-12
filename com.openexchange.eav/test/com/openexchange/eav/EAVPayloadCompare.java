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

package com.openexchange.eav;

import java.io.IOException;
import java.io.InputStream;

/**
 * {@link EAVPayloadCompare}
 * 
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class EAVPayloadCompare implements EAVTypeSwitcher {

    public Object binary(Object... args) {
        InputStream expected = (InputStream) args[0];
        InputStream actual = (InputStream) args[1];
        
        int expectedData;
        try {
            while((expectedData = expected.read())!= -1){
                if(expectedData != actual.read()) {
                    return false;
                }
            }
            if(actual.read() != -1) {
                return false;
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Object date(Object... args) {
        return equals(args[0], args[1]);
    }

    public Object number(Object... args) {
        if(equals(args[0], args[1])) {
            return true;
        }
        long a = -1;
        long b = -1;
        
        boolean aCast = false;
        boolean bCast = false;
        if(Long.class.isInstance(args[0])) {
            a = (Long)args[0];
            aCast = true;
        }
        
        if(Long.class.isInstance(args[1])) {
            b = (Long) args[1];
            bCast = true;
        }
        
        if(Integer.class.isInstance(args[0])) {
            a = (Integer) args[0];
            aCast = true;
        }
        
        if(Integer.class.isInstance(args[1])) {
            b = (Integer) args[1];
            bCast = true;
        }
        
        if(aCast != bCast) {
            return false;
        }
        
        if(aCast) {
            return 0 == a-b;
        }
        
        // TODO Floats
        return false;
    }

    public Object object(Object... args) {
        return true;
    }

    public Object string(Object... args) {
        return equals(args[0], args[1]);
    }

    public Object time(Object... args) {
        return equals(args[0], args[1]);
    }
    
    public Object bool(Object... args) {
        return equals(args[0], args[1]);
    }

    public Object nullValue(Object... args) {
        return true;
    }

    private boolean equals(Object o1, Object o2) {
        return o1.equals(o2);
    }



}
