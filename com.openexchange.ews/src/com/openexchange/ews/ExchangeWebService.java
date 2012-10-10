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

package com.openexchange.ews;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Holder;
import com.microsoft.schemas.exchange.services._2006.messages.ArrayOfResponseMessagesType;
import com.microsoft.schemas.exchange.services._2006.messages.BaseResponseMessageType;
import com.microsoft.schemas.exchange.services._2006.messages.ExchangeService;
import com.microsoft.schemas.exchange.services._2006.messages.ExchangeServicePortType;
import com.microsoft.schemas.exchange.services._2006.messages.ResponseMessageType;
import com.microsoft.schemas.exchange.services._2006.types.MailboxCultureType;
import com.microsoft.schemas.exchange.services._2006.types.RequestServerVersion;
import com.microsoft.schemas.exchange.services._2006.types.ResponseClassType;
import com.microsoft.schemas.exchange.services._2006.types.TimeZoneContextType;
import com.microsoft.schemas.exchange.services._2006.types.TimeZoneDefinitionType;

/**
 * {@link ExchangeWebService}
 * 
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class ExchangeWebService {
    
    private final ExchangeServicePortType service;
    private final EWSConfig config;

    /**
     * Initializes a new {@link ExchangeWebService}.
     * 
     * @param url The URL to the service (usually [SERVER]/EWS/Exchange.asmx)
     * @param userName The exchange username 
     * @param password The password
     */
    public ExchangeWebService(String url, String userName, String password) {
        super();
        this.service = createService();
        this.config = new EWSConfig((BindingProvider)service);
        config.setEndpointAddress(url);
        config.setUserName(userName);
        config.setPassword(password);
    }
    
    /**
     * Gets the underlying exchange service port.
     * 
     * @return the serivce port
     */
    public ExchangeServicePortType getServicePort() {
        return this.service;
    }
    
    /**
     * Gets the service's configuration.
     * 
     * @return The configuration
     */
    public EWSConfig getConfig() {
        return this.config;
    }
    
    /**
     * Gets the required server version for requests. 
     * 
     * @return The request server version
     */
    public RequestServerVersion getRequestVersion() {
        RequestServerVersion requestServerVersion = new RequestServerVersion();
        requestServerVersion.setVersion(config.getExchangeVersion());
        return requestServerVersion;
    }
    
    public static MailboxCultureType getMailboxCulture() {
        MailboxCultureType mailboxCulture = new MailboxCultureType();
        mailboxCulture.setValue("en-US");
        return mailboxCulture;
    }
    
    public static TimeZoneContextType getTimeZoneContext() {
        TimeZoneContextType timeZoneContextType = new TimeZoneContextType();
        timeZoneContextType.setTimeZoneDefinition(getUTCTimeZone());
        return timeZoneContextType;
    }
    
    public static TimeZoneDefinitionType getUTCTimeZone() {
        TimeZoneDefinitionType timeZoneDefinition = new TimeZoneDefinitionType();
        timeZoneDefinition.setId("UTC");
        timeZoneDefinition.setName("(UTC) Coordinated Universal Time");
        return timeZoneDefinition;
    }
    
    public static void check(ResponseMessageType responseMessage) throws EWSException {
        if (null != responseMessage && false == ResponseClassType.SUCCESS.equals(responseMessage.getResponseClass())) {
            throw new EWSException(responseMessage);
        }
    }
    
    /**
     * Extracts the response messages from the supplied response holder.
     * 
     * @param responseHolder the response holder
     * @return the response messages, or <code>null</code> if there are none
     */
    public static List<ResponseMessageType> getResponseMessages(Holder<BaseResponseMessageType> responseHolder) {
        if (null != responseHolder && null != responseHolder.value) {
            ArrayOfResponseMessagesType responseMessages = responseHolder.value.getResponseMessages();
            if (null != responseMessages) {
                List<JAXBElement<? extends ResponseMessageType>> elements = 
                        responseMessages.getCreateItemResponseMessageOrDeleteItemResponseMessageOrGetItemResponseMessage();
                if (null != elements) {
                    List<ResponseMessageType> responses = new ArrayList<ResponseMessageType>();
                    for (JAXBElement<? extends ResponseMessageType> element : elements) {
                        responses.add(element.getValue());
                    }
                }
            }
        }
        return null;
    }
    
    /**
     * Extracts a single response messages from the supplied response holder.
     * 
     * @param responseHolder the response holder
     * @return the response message, or <code>null</code> if there is none
     */
    public static ResponseMessageType getResponseMessage(Holder<BaseResponseMessageType> responseHolder) {
        List<ResponseMessageType> responseMessages = getResponseMessages(responseHolder);
        if (null == responseMessages || 0 == responseMessages.size()) {
            return null;
        } else if (1 != responseMessages.size()) {
            throw new IllegalStateException("Expected a single response message");
        } else {
            return responseMessages.get(0);
        }
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