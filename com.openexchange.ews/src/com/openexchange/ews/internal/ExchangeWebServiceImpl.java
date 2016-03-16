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

package com.openexchange.ews.internal;

import java.net.URL;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import com.microsoft.schemas.exchange.services._2006.messages.ExchangeService;
import com.microsoft.schemas.exchange.services._2006.messages.ExchangeServicePortType;
import com.openexchange.ews.Availability;
import com.openexchange.ews.Config;
import com.openexchange.ews.ExchangeWebService;
import com.openexchange.ews.Folders;
import com.openexchange.ews.Items;

/**
 * {@link ExchangeWebServiceImpl}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class ExchangeWebServiceImpl implements ExchangeWebService {

    private final ExchangeServicePortType port;
    private final ConfigImpl config;
    private final FoldersImpl folders;
    private final ItemsImpl items;
    private final AvailabilityImpl availibility;

    /**
     * Initializes a new {@link ExchangeWebServiceImpl}.
     *
     * @param url The URL to the service (usually [SERVER]/EWS/Exchange.asmx)
     * @param userName The exchange username
     * @param password The password
     */
    public ExchangeWebServiceImpl(String url, String userName, String password) {
        super();
        this.port = createService();
        this.config = new ConfigImpl((BindingProvider)port);
        config.setEndpointAddress(url);
        config.setUserName(userName);
        config.setPassword(password);
        this.folders = new FoldersImpl(this, port);
        this.items = new ItemsImpl(this, port);
        this.availibility = new AvailabilityImpl(this, port);
    }

    @Override
    public ExchangeServicePortType getServicePort() {
        return port;
    }

    @Override
    public Config getConfig() {
        return config;
    }

    @Override
    public Folders getFolders() {
        return folders;
    }

    @Override
    public Items getItems() {
        return items;
    }

    @Override
    public Availability getAvailability() {
        return availibility;
    }

    private static ExchangeServicePortType createService() {
        ExchangeService service = new ExchangeService(getWsdlLocation(),
                new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "ExchangeService"));
        return service.getExchangeServicePort();
    }

    private static URL getWsdlLocation() {
        return ExchangeWebService.class.getResource("/META-INF/Services.wsdl");
    }

}