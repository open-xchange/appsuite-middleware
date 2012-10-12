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

package com.openexchange.ews.internal;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import javax.xml.ws.Holder;
import com.microsoft.schemas.exchange.services._2006.messages.ArrayOfResponseMessagesType;
import com.microsoft.schemas.exchange.services._2006.messages.BaseResponseMessageType;
import com.microsoft.schemas.exchange.services._2006.messages.ExchangeServicePortType;
import com.microsoft.schemas.exchange.services._2006.messages.ResponseMessageType;
import com.microsoft.schemas.exchange.services._2006.types.ConstantValueType;
import com.microsoft.schemas.exchange.services._2006.types.ContainmentModeType;
import com.microsoft.schemas.exchange.services._2006.types.ContainsExpressionType;
import com.microsoft.schemas.exchange.services._2006.types.FieldURIOrConstantType;
import com.microsoft.schemas.exchange.services._2006.types.IsEqualToType;
import com.microsoft.schemas.exchange.services._2006.types.PathToUnindexedFieldType;
import com.microsoft.schemas.exchange.services._2006.types.RequestServerVersion;
import com.microsoft.schemas.exchange.services._2006.types.ResponseClassType;
import com.microsoft.schemas.exchange.services._2006.types.RestrictionType;
import com.microsoft.schemas.exchange.services._2006.types.SearchExpressionType;
import com.microsoft.schemas.exchange.services._2006.types.ServerVersionInfo;
import com.microsoft.schemas.exchange.services._2006.types.UnindexedFieldURIType;
import com.openexchange.ews.EWSException;
import com.openexchange.ews.ExchangeWebService;

/**
 * {@link Common}
 * 
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public abstract class Common {
    
    protected final ExchangeWebService service;
    protected final ExchangeServicePortType port;
    
    public Common(ExchangeWebService service, ExchangeServicePortType port) {
        super();
        this.service = service;
        this.port = port;
    }
            
    protected Holder<ServerVersionInfo> getVersionHolder() {
        return new Holder<ServerVersionInfo>();
    }
    
    protected RequestServerVersion getRequestVersion() {
        RequestServerVersion requestServerVersion = new RequestServerVersion();
        requestServerVersion.setVersion(service.getConfig().getExchangeVersion());
        return requestServerVersion;
    }
    
    protected RestrictionType getIsEqualRestriction(UnindexedFieldURIType fieldURI, String equalTo) {
        ConstantValueType constantValue = new ConstantValueType();
        constantValue.setValue(equalTo);
        FieldURIOrConstantType constantType = new FieldURIOrConstantType();
        constantType.setConstant(constantValue);
        IsEqualToType isEqualTo = new IsEqualToType();
        isEqualTo.setPath(getPathToUnindexedField(fieldURI));
        isEqualTo.setFieldURIOrConstant(constantType);
        RestrictionType restriction = new RestrictionType();
        restriction.setSearchExpression(new JAXBElement<SearchExpressionType>(new QName("http://schemas.microsoft.com/exchange/services/2006/types",
            "IsEqualTo"), SearchExpressionType.class, isEqualTo));
        return restriction;
    }
    
    protected RestrictionType getContainsRestriction(UnindexedFieldURIType fieldURI, String contains) {
        ConstantValueType constantValue = new ConstantValueType();
        constantValue.setValue(contains);
        ContainsExpressionType containsExpression = new ContainsExpressionType();
        containsExpression.setPath(getPathToUnindexedField(fieldURI));
        containsExpression.setConstant(constantValue);
        containsExpression.setContainmentMode(ContainmentModeType.SUBSTRING);
        RestrictionType restriction = new RestrictionType();
        restriction.setSearchExpression(new JAXBElement<SearchExpressionType>(new QName("http://schemas.microsoft.com/exchange/services/2006/types",
            "Contains"), SearchExpressionType.class, containsExpression));
        return restriction;
    }
    
    protected static JAXBElement<PathToUnindexedFieldType> getPathToUnindexedField(UnindexedFieldURIType fieldURI) {
        PathToUnindexedFieldType pathToUnindexedField = new PathToUnindexedFieldType();
        pathToUnindexedField.setFieldURI(fieldURI);
        QName qName = new QName("http://schemas.microsoft.com/exchange/services/2006/types", "FieldURI");
        return new JAXBElement<PathToUnindexedFieldType>(qName, PathToUnindexedFieldType.class, pathToUnindexedField);
    }

    protected static void check(ResponseMessageType responseMessage) throws EWSException {
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
    protected static List<ResponseMessageType> getResponseMessages(Holder<? extends BaseResponseMessageType> responseHolder) {
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
     * @throws EWSException 
     */
    protected static ResponseMessageType getResponseMessage(Holder<? extends BaseResponseMessageType> responseHolder) {
        List<ResponseMessageType> responseMessages = getResponseMessages(responseHolder);
        if (null == responseMessages || 0 == responseMessages.size()) {
            return null;
        } else if (1 != responseMessages.size()) {
            throw new IllegalStateException("Expected a single response message");
        } else {
            return responseMessages.get(0);
        }
    }


}