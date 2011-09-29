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

package com.openexchange.ant.data;

import java.util.HashSet;
import java.util.Set;


/**
 * {@link JDK}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class JDK {

    private static final Set<String> exports;

    static {
        exports = new HashSet<String>();
        exports.add("javax.crypto");
        exports.add("javax.crypto.interfaces");
        exports.add("javax.crypto.spec");
        exports.add("javax.management");
        exports.add("javax.management.openmbean");
        exports.add("javax.management.remote");
        exports.add("javax.management.remote.rmi");
        exports.add("javax.naming");
        exports.add("javax.net");
        exports.add("javax.net.ssl");
        exports.add("javax.security.auth");
        exports.add("javax.security.auth.callback");
        exports.add("javax.security.auth.kerberos");
        exports.add("javax.security.auth.login");
        exports.add("javax.security.auth.spi");
        exports.add("javax.security.auth.x500");
        exports.add("javax.security.cert");
        exports.add("javax.security.sasl");
        exports.add("javax.swing.text");
        exports.add("javax.swing.text.html");
        exports.add("javax.swing.text.html.parser");
        exports.add("javax.swing.text.rtf");
        exports.add("javax.swing.tree");
        exports.add("javax.xml");
        exports.add("javax.xml.xpath");
        exports.add("org.w3c.dom.css");
        exports.add("org.w3c.dom.events");
        exports.add("org.w3c.dom.html");
        exports.add("org.w3c.dom.ls");
        exports.add("org.w3c.dom.ranges");
        exports.add("org.w3c.dom.stylesheets");
        exports.add("org.w3c.dom.traversal");
    }

    private JDK() {
        super();
    }

    public static boolean exports(final String packageName) {
        return exports.contains(packageName);
    }
}
