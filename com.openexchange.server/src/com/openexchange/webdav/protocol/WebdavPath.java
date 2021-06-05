/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
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
            if (c == '/') {
                if (component.length() > 0) {
                    components.add(component.toString());
                }
                component.setLength(0);
            } else {
                component.append(c);
            }
        }
        if (component.length() > 0) {
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
        if (components.size()<2) {
            return new WebdavPath();
        }
        return new WebdavPath(components.subList(0,components.size()-1));
    }

    public String name(){
        if (components.size() == 0) {
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
        if (path.size() > size()) {
            return false;
        }

        for(int i = 0; i < path.size(); i++) {
            if (!components.get(i).equals(path.components.get(i))){
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
        if (component.indexOf('/') < 0 && component.indexOf('\\') < 0) {
            return component;
        }
        return component.replaceAll("\\\\","\\\\\\\\").replaceAll("/","\\\\/");
    }

}
