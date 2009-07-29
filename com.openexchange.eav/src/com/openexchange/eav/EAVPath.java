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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;


/**
 * {@link EAVPath}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public class EAVPath {

    private List<String> components;

    public EAVPath(String...components) {
        this.components = new ArrayList<String>(Arrays.asList(components));   
    }
    
    public EAVPath(List<String> components) {
        this.components = components;
    }
    
    public EAVPath(EAVPath original) {
        this.components = new ArrayList<String>(original.components);
    }

    public boolean equals(Object other) {
        if(EAVPath.class.isInstance(other)) {
            return ((EAVPath)other).components.equals(components);
        }
        return false;
    }
    
    public int hashCode() {
        return components.hashCode();
    }

    public EAVPath append(String...names) {
        EAVPath p = this;
        for (String name : names) {
            p = p.append(name);
        }
        return p;
    }
    
    public EAVPath append(String name) {
        ArrayList<String> list = new ArrayList<String>(components);
        list.add(name);
        return new EAVPath(list);
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        for(String component : components) {
            builder.append(component).append('/');
        }
        return builder.substring(0, builder.length()-1);
    }

    public boolean isEmpty() {
        return components.isEmpty();
    }

    public String first() {
        if(components.isEmpty()) {
            return null;
        }
        return components.get(0);
    }
    
    public String last() {
        if(components.isEmpty()) {
            return null;
        }
        return components.get(components.size()-1);
    }


    public EAVPath shiftLeft() {
        if(isEmpty()) {
            return new EAVPath();
        }
        return new EAVPath(new ArrayList<String>(components.subList(1, components.size())));
    }

    public EAVPath parent() {
        if(isEmpty()) {
            return null;
        }
        return new EAVPath(new ArrayList<String>(components.subList(0, components.size()-1)));
    }

    public List<EAVPath> subpaths(String...names) {
        ArrayList<EAVPath> list = new ArrayList<EAVPath>(names.length);
        for (String string : names) {
            list.add(this.append(string));
        }
        
        return list;
    }

    public static EAVPath parse(String string) {
        if(string.length() == 0) {
            return new EAVPath();
        }
        List<String> elements = new ArrayList<String>(Arrays.asList(string.split("/")));
        if(string.charAt(0) == '/') {
            elements = elements.subList(1, elements.size());
        }
        return new EAVPath(elements);
    }

}
