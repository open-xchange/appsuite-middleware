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

package com.openexchange.groupware.integration;

import static com.openexchange.tools.io.IOUtils.closeStreamStuff;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;

import com.openexchange.configuration.ConfigurationException;
import com.openexchange.configuration.SystemConfig;
import com.openexchange.configuration.SystemConfig.Property;
import com.openexchange.tools.ajp13.AJPv13RequestHandler;

/**
 * Implements the config jump to the user admin interface for OXExpress.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class ExpressImpl extends SetupLink {

    /**
     * Lock for initialization.
     */
    private static final Lock LOCK = new ReentrantLock();

    /**
     * Configuration properties.
     */
    private static Properties props;

    /**
     * Default constructor.
     */
    public ExpressImpl() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public URL getLink(final Object... values) throws SetupLinkException {
        final String url = props.getProperty("URL");
        if (null == url) {
            throw new SetupLinkException(new ConfigurationException(
                ConfigurationException.Code.PROPERTY_MISSING, "URL"));
        }
        int pos = 1;
        final String userId = (String) values[pos++];
        final String password = (String) values[pos++];
        final String protocol = (String) values[pos++];
        final String host = (String) values[pos++];
        final int port = ((Integer) values[pos++]).intValue();
        final javax.servlet.http.Cookie[] cookies = (javax.servlet.http
            .Cookie[]) values[pos++];
        final URL urlInst;
        final URL newUrlInst;
        try {
            urlInst = new URL(url);
            newUrlInst = new URL(protocol, host, port, urlInst.getPath());
        } catch (MalformedURLException e1) {
            throw new SetupLinkException(SetupLinkException.Code.MALFORMED_URL,
                    e1);
        }
        final HttpClient httpClient = new HttpClient();
        final HttpState state = httpClient.getState();
        for (javax.servlet.http.Cookie cookie : cookies) {
            state.addCookie(new Cookie(urlInst.getHost(), cookie.getName(),
                cookie.getValue(), urlInst.getPath(), -1, false));
        }
        final PostMethod post = new PostMethod(url);
        post.setRequestHeader("Content-Type", PostMethod
            .FORM_URL_ENCODED_CONTENT_TYPE + "; charset=UTF-8");
        post.addParameter(new NameValuePair("loginUsername", userId));
        post.addParameter(new NameValuePair("loginPassword", password));
        try {
            httpClient.executeMethod(post.getHostConfiguration(), post, state);
            final String session = post.getResponseBodyAsString();
            return new URL(newUrlInst.toExternalForm() + AJPv13RequestHandler
                .JSESSIONID_URI + session);
        } catch (IOException e) {
            throw new SetupLinkException(SetupLinkException.Code.COMMUNICATION,
                e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize() throws SetupLinkException {
        LOCK.lock();
        try {
            if (null == props) {
                loadProperties();
            }
        } finally {
            LOCK.unlock();
        }
    }

    /**
     * Loads the properties.
     * @throws SetupLinkException if loading fails.
     */
    private void loadProperties() throws SetupLinkException {
        props = new Properties();
        final String fileName = SystemConfig.getProperty(Property
            .ConfigJumpConf);
        if (null == fileName) {
            throw new SetupLinkException(SetupLinkException.Code
                .MISSING_SETTING, Property.ConfigJumpConf.getPropertyName());
        }
        final File file = new File(fileName);
        if (!file.exists()) {
            throw new SetupLinkException(new ConfigurationException(
                ConfigurationException.Code.FILE_NOT_FOUND, file
                .getAbsolutePath()));
        }
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            props.load(fis);
        } catch (IOException e) {
            throw new SetupLinkException(new ConfigurationException(
                ConfigurationException.Code.NOT_READABLE, e, file
                .getAbsolutePath()));
        } finally {
            closeStreamStuff(fis);
        }
    }
}
