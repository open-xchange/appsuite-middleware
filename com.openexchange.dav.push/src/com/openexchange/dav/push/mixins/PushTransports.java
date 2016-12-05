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

package com.openexchange.dav.push.mixins;

import com.openexchange.dav.DAVProtocol;
import com.openexchange.dav.push.DAVPushUtility;
import com.openexchange.dav.push.apn.DAVApnOptions;
import com.openexchange.dav.push.subscribe.PushSubscribeFactory;
import com.openexchange.exception.OXException;
import com.openexchange.pns.transport.apn.ApnOptions;
import com.openexchange.pns.transport.apn.ApnOptionsProvider;
import com.openexchange.webdav.protocol.WebdavProperty;
import com.openexchange.webdav.protocol.WebdavResource;
import com.openexchange.webdav.protocol.helpers.SingleResourcePropertyMixin;

/**
 * {@link PushTransports}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.4
 */
public class PushTransports extends SingleResourcePropertyMixin {

    private final PushSubscribeFactory factory;

    /**
     * Initializes a new {@link PushTransports}.
     *
     * @param <code>false</code> The push subscribe factory
     */
    public PushTransports(PushSubscribeFactory factory) {
        super(DAVProtocol.CALENDARSERVER_NS.getURI(), "push-transports");
        this.factory = factory;
    }

    @Override
    protected WebdavProperty getProperty(WebdavResource resource) throws OXException {
        if (null != DAVPushUtility.getPushKey(resource)) {
            String value = getValue(DAVPushUtility.getClientId(resource));
            if (null != value) {
                WebdavProperty property = prepareProperty(true);
                property.setValue(value);
                return property;
            }
        }
        return null;
    }

    private String getValue(String clientId) {
        DAVApnOptions options = getOptions(clientId);
        if (null == options) {
            return null;
        }
        return new StringBuilder()
            .append("<transport type='APSD'>")
            .append(  "<subscription-url>")
            .append(    "<href xmlns='DAV:'>" + DAVPushUtility.getSubscriptionURL(clientId) + "</href>")
            .append(  "</subscription-url>")
            .append(  "<apsbundleid>" + options.getBundleId() + "</apsbundleid>")
            .append(  "<env>" + (options.isProduction() ? "PRODUCTION" : "SANDBOX") + "</env>")
            .append(  "<refresh-interval>" + options.getRefreshInterval() + "</refresh-interval>")
            .append("</transport>")
        .toString();
    }

    private DAVApnOptions getOptions(String clientId) {
        if (null == clientId) {
            return null;
        }
        ApnOptionsProvider optionsProvider = factory.getApnOptionsProvider();
        if (null == optionsProvider) {
            return null;
        }
        ApnOptions options = optionsProvider.getOptions(clientId);
        if (null != options && DAVApnOptions.class.isInstance(options)) {
            return (DAVApnOptions) options;
        }
        return null;
    }

}
