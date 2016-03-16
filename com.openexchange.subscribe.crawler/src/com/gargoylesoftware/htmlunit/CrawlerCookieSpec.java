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

package com.gargoylesoftware.htmlunit;

import java.util.Calendar;
import java.util.Date;
import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.cookie.CookieSpecBase;
import org.apache.commons.httpclient.cookie.MalformedCookieException;


/**
 * {@link CrawlerCookieSpec}
 *
 * @author <a href="mailto:karsten.will@open-xchange.com">Karsten Will</a>
 */
public class CrawlerCookieSpec extends CookieSpecBase {

    protected static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(CrawlerCookieSpec.class);

    public CrawlerCookieSpec(){
    }

    @Override
    public void validate(String host, int port, String path,
        boolean secure, final Cookie cookie)
        throws MalformedCookieException {

        LOG.trace("enter CrawlerCookieSpec.validate(String, port, path, boolean, Cookie)");
        if (host == null) {
            throw new IllegalArgumentException(
                "Host of origin may not be null");
        }
        if (host.trim().equals("")) {
            throw new IllegalArgumentException(
                "Host of origin may not be blank");
        }
        if (port < 0) {
            throw new IllegalArgumentException("Invalid port: " + port);
        }
        if (path == null) {
            throw new IllegalArgumentException(
                "Path of origin may not be null.");
        }
        if (path.trim().equals("")) {
            path = PATH_DELIM;
        }
        host = host.toLowerCase();
        // check version
        if (cookie.getVersion() < 0) {
            throw new MalformedCookieException ("Illegal version number "
                + cookie.getValue());
        }

        // security check... we musn't allow the server to give us an
        // invalid domain scope

        // Validate the cookies domain attribute.  NOTE:  Domains without
        // any dots are allowed to support hosts on private LANs that don't
        // have DNS names.  Since they have no dots, to domain-match the
        // request-host and domain must be identical for the cookie to sent
        // back to the origin-server.
        if (host.indexOf('.') >= 0) {
            // Not required to have at least two dots.  RFC 2965.
            // A Set-Cookie2 with Domain=ajax.com will be accepted.

            // domain must match host
            if (!host.endsWith(cookie.getDomain())) {
                String s = cookie.getDomain();
                if (s.length() > 0 && s.charAt(0) == '.') {
                    s = s.substring(1, s.length());
                }
//                if (!host.equals(s)) {
//                    throw new MalformedCookieException(
//                        "Illegal domain attribute \"" + cookie.getDomain()
//                        + "\". Domain of origin: \"" + host + "\"");
//                }
            }
        }
//        if (cookie.getName().equals("s_leo_auth_token") && cookie.getValue().equals("delete me")){
//            throw new MalformedCookieException ("Not accepting this cookie because it has a value of delete_me: "
//                + cookie);
//        }
        // setting expiry date to one year in the future for all cookies that have none
        if (null == cookie.getExpiryDate()){
            Calendar calendar = Calendar.getInstance();
            Date date = calendar.getTime();
            date.setYear(date.getYear() + 1);
            cookie.setExpiryDate(date);
        }
        //System.out.println("Added this cookie : "+ cookie + ", Expiry: " +cookie.getExpiryDate() + ", Path: "+cookie.getPath() + ", Domain: " + cookie.getDomain() + ", Secure ?: " + cookie.getSecure() + ", Comment: " + cookie.getComment());
//        else {
//            if (!host.equals(cookie.getDomain())) {
//                throw new MalformedCookieException(
//                    "Illegal domain attribute \"" + cookie.getDomain()
//                    + "\". Domain of origin: \"" + host + "\"");
//            }
//        }

        // another security check... we musn't allow the server to give us a
        // cookie that doesn't match this path

//        if (!path.startsWith(cookie.getPath())) {
//            throw new MalformedCookieException(
//                "Illegal path attribute \"" + cookie.getPath()
//                + "\". Path of origin: \"" + path + "\"");
//        }
    }

}
