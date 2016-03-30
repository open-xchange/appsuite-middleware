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

package com.openexchange.webdav.protocol.helpers;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import com.openexchange.webdav.protocol.WebdavCollection;
import com.openexchange.webdav.protocol.WebdavFactory;
import com.openexchange.webdav.protocol.WebdavPath;
import com.openexchange.webdav.protocol.WebdavProtocolException;
import com.openexchange.webdav.protocol.WebdavResource;

/**
 * {@link AbstractWebdavFactory}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public abstract class AbstractWebdavFactory implements WebdavFactory {

    private PropertyMixin[] mixins;

    @Override
    public void beginRequest() {

    }

    @Override
    public void endRequest(final int status) {

    }

    @Override
    public WebdavCollection resolveCollection(String url) throws WebdavProtocolException {
        url = normalize(url);
        return resolveCollection(decode(new WebdavPath(url)));
    }

    @Override
    public WebdavResource resolveResource(String url) throws WebdavProtocolException {
        url = normalize(url);
        return resolveResource(decode(new WebdavPath(url)));
    }

    public WebdavPath decode(final WebdavPath webdavPath) {
        final WebdavPath path = new WebdavPath();
        for(final String component : webdavPath) {
            try {
                path.append(URLDecoder.decode(component, "UTF-8"));
            } catch (final UnsupportedEncodingException e) {
                // Won't happen
            }
        }
        return path;
    }

    public <T extends AbstractResource> T mixin(T thing) {
        if (mixins != null) {
            thing.includeProperties(mixins);
        }
        return thing;
    }

    protected String normalize(String url) {
        if(url.length()==0) {
            return "/";
        }
        url = url.replaceAll("/+", "/");
        if(url.charAt(url.length()-1)=='/') {
            return url.substring(0,url.length()-1);
        }
        return url;
    }

    public void setGlobalMixins(final PropertyMixin...mixins) {
        this.mixins = mixins;
    }



}
