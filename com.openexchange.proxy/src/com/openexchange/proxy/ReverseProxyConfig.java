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

package com.openexchange.proxy;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import org.apache.commons.httpclient.HttpHost;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import com.openexchange.httpclient.ssl.TrustAllSecureSocketFactory;

/**
 * {@link ReverseProxyConfig}
 *
 * @author <a href="mailto:matthias.biggeleben@open-xchange.com">Matthias Biggeleben</a>
 */
public class ReverseProxyConfig {

    public String id = "";
    public String protocol = "";
    public String host = "";
    public String location = "";
    public String agent = "";

    public ReverseProxyConfig(String id, String protocol, String host, String location, String agent) {
        
        this.id = id;
        this.protocol = protocol;
        this.host = host;
        this.location = location;
        this.agent = agent;
    }
    
    public URI getURI(String path, String query) throws URIException, NullPointerException {
        URI u = new URI(this.getPrefix() + path, false);
        try {
            if (query != null) {
                query = URLDecoder.decode(query, "UTF-8");
                u.setQuery(query);
            }
        } catch (UnsupportedEncodingException e) {}
        return u;
    }
    
    public String getURIPath(String path, String query) throws UnsupportedEncodingException {
        return location + path + (null == query ? "" : ("?" + URLDecoder.decode(query, "UTF-8")));
    }

    public String getPrefix() {
        return this.protocol + "://" + this.host + this.location;
    }

    public HttpHost getHost() throws URIException, NullPointerException {
        if ("https".equals(protocol)) {
            final int port;
            final String hostName;
            final int pos = host.indexOf(':');
            if (pos == -1) {
                port = 443;
                hostName = host;
            } else {
                hostName = host.substring(0, pos);
                port = Integer.parseInt(host.substring(pos + 1));
            }
            Protocol proto = new Protocol("https", (ProtocolSocketFactory) new TrustAllSecureSocketFactory(), port);
            return new HttpHost(hostName, port, proto);
        } else {
            return new HttpHost(new URI(protocol + "://" + host, false));
        }
    }
}
