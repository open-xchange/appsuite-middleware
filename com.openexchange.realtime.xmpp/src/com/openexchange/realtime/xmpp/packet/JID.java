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

package com.openexchange.realtime.xmpp.packet;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * {@link JID}
 * 
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class JID {

    private String user;

    private String domain;

    private String resource;

    private static final Pattern PATTERN = Pattern.compile("([^@]+)@([^/]+)/?(.*)");

    public JID(String jid) {
        final Matcher matcher = PATTERN.matcher(jid);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Could not parse " + jid);
        }

        setUser(matcher.group(1));
        setDomain(matcher.group(2));
        setResource(matcher.group(3));

    }

    public JID(String user, String domain, String resource) {
        setUser(user);
        setDomain(domain);
        setResource(resource);
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        if (user == null || user.trim().equals("")) {
            throw new IllegalArgumentException("Invalid User: " + user);
        }
        this.user = sanitize(user);
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        if (domain == null || domain.trim().equals("")) {
            throw new IllegalArgumentException("Invalid Domain:" + domain);
        }
        this.domain = sanitize(domain);
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public boolean isBare() {
        return resource == null;
    }

    public boolean isFull() {
        return !isBare();
    }

    public String toString() {
        return toBareString() + (isBare() ? "" : "/" + resource);
    }

    public String toBareString() {
        return user + "@" + domain;
    }

    public JID getBare() {
        return new JID(user, domain, null);
    }

    private String sanitize(String string) {
        if (string == null) {
            return null;
        }

        String tmp = string.toLowerCase().trim();

        if (tmp.equals("")) {
            return null;
        }

        return tmp;
    }

}
