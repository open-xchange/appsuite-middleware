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
 *    trademarks of the OX Software GmbH group of companies.
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
 *     Copyright (C) 2016-2020 OX Software GmbH
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

package com.openexchange.webdav.protocol;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class WebdavPath implements Iterable<String>{

    private final List<String> components = new ArrayList<String>();

    public WebdavPath(final CharSequence path) {
        final StringBuilder component = new StringBuilder();
        for(int i = 0; i < path.length(); i++) {
            final char c = path.charAt(i);
            if(c == '/') {
                if(component.length() > 0) {
                    components.add(component.toString());
                }
                component.setLength(0);
            } else {
                component.append(c);
            }
        }
        if(component.length() > 0) {
            components.add(component.toString());
        }
    }

    public WebdavPath(final String...components) {
        append(components);
    }

    public WebdavPath(final Collection<String> components) {
        append(components);
    }

    @Override
    public Iterator<String> iterator() {
        return components.iterator();
    }

    public int size() {
        return components.size();
    }

    public WebdavPath append(final String...components) {
        return append(Arrays.asList(components));
    }

    public WebdavPath append(final Collection<String> strings) {
        this.components.addAll(strings);
        return this;
    }

    public WebdavPath append(final WebdavPath webdavPath) {
        return append(webdavPath.components);
    }

    public WebdavPath parent(){
        if(components.size()<2) {
            return new WebdavPath();
        }
        return new WebdavPath(components.subList(0,components.size()-1));
    }

    public String name(){
        if(components.size() == 0) {
			return "";
		}
        return  components.get(components.size()-1);
    }

    public WebdavPath dup(){
        return new WebdavPath(components);
    }


    public WebdavPath subpath(final int from){
        return subpath(from, size());
    }

    public WebdavPath subpath(final int from, final int to) {
        return new WebdavPath(components.subList(from,to));
    }

    @Override
	public boolean equals(final Object other) {
        if (!(other instanceof WebdavPath)) {
            return false;
        }
        return components.equals(((WebdavPath)other).components);
    }

    public boolean startsWith(final WebdavPath path) {
        if(path.size() > size()) {
            return false;
        }

        for(int i = 0; i < path.size(); i++) {
            if(!components.get(i).equals(path.components.get(i))){
                return false;
            }
        }
        return true;
    }

    @Override
	public int hashCode(){
        return components.hashCode();
    }

    @Override
	public String toString(){
        final StringBuilder b = new StringBuilder("/");
        for(final String component : components) { b.append(component).append('/'); }
        b.setLength(b.length()-1);
        return b.toString();
    }

    public String toEscapedString() {
        final StringBuilder b = new StringBuilder("/");
        for(final String component : components) { b.append(_escape(component)).append('/'); }
        b.setLength(b.length()-1);
        return b.toString();
    }

    private String _escape(final String component) {
        if(component.indexOf('/') < 0 && component.indexOf('\\') < 0) {
            return component;
        }
        return component.replaceAll("\\\\","\\\\\\\\").replaceAll("/","\\\\/");
    }

}
