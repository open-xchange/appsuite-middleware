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
import com.microsoft.schemas.exchange.services._2006.types.OrType;
import com.microsoft.schemas.exchange.services._2006.types.PathToUnindexedFieldType;
import com.microsoft.schemas.exchange.services._2006.types.RequestServerVersion;
import com.microsoft.schemas.exchange.services._2006.types.ResponseClassType;
import com.microsoft.schemas.exchange.services._2006.types.RestrictionType;
import com.microsoft.schemas.exchange.services._2006.types.SearchExpressionType;
import com.microsoft.schemas.exchange.services._2006.types.ServerVersionInfo;
import com.microsoft.schemas.exchange.services._2006.types.UnindexedFieldURIType;
import com.openexchange.ews.EWSExceptionCodes;
import com.openexchange.ews.ExchangeWebService;
import com.openexchange.exception.OXException;

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
        restriction.setSearchExpression(getSearchExpression("IsEqualTo", isEqualTo));
        return restriction;
    }

    protected RestrictionType getIsEqualRestriction(UnindexedFieldURIType fieldURI, List<String> equalTos) {
        if (0 == equalTos.size()) {
            throw new IllegalArgumentException("equalTos");
        } else if (1 == equalTos.size()) {
            return getIsEqualRestriction(fieldURI, equalTos);
        } else {
            OrType or = new OrType();
            for (String equalTo : equalTos) {
                IsEqualToType isEqualTo = getIsEqualTo(fieldURI, equalTo);
                or.getSearchExpression().add(getSearchExpression("IsEqualTo", isEqualTo));
            }
            RestrictionType restriction = new RestrictionType();
            restriction.setSearchExpression(getSearchExpression("Or", or));
            return restriction;
        }
    }

    protected RestrictionType getContainsRestriction(UnindexedFieldURIType fieldURI, String contains) {
        ConstantValueType constantValue = new ConstantValueType();
        constantValue.setValue(contains);
        ContainsExpressionType containsExpression = new ContainsExpressionType();
        containsExpression.setPath(getPathToUnindexedField(fieldURI));
        containsExpression.setConstant(constantValue);
        containsExpression.setContainmentMode(ContainmentModeType.SUBSTRING);
        RestrictionType restriction = new RestrictionType();
        restriction.setSearchExpression(getSearchExpression("Contains", containsExpression));
        return restriction;
    }

    protected static JAXBElement<PathToUnindexedFieldType> getPathToUnindexedField(UnindexedFieldURIType fieldURI) {
        PathToUnindexedFieldType pathToUnindexedField = new PathToUnindexedFieldType();
        pathToUnindexedField.setFieldURI(fieldURI);
        QName qName = new QName("http://schemas.microsoft.com/exchange/services/2006/types", "FieldURI");
        return new JAXBElement<PathToUnindexedFieldType>(qName, PathToUnindexedFieldType.class, pathToUnindexedField);
    }

    protected static IsEqualToType getIsEqualTo(UnindexedFieldURIType fieldURI, String equalTo) {
        IsEqualToType isEqualTo = new IsEqualToType();
        isEqualTo.setPath(getPathToUnindexedField(fieldURI));
        isEqualTo.setFieldURIOrConstant(getConstantType(equalTo));
        return isEqualTo;
    }

    protected static FieldURIOrConstantType getConstantType(String value) {
        ConstantValueType constantValue = new ConstantValueType();
        constantValue.setValue(value);
        FieldURIOrConstantType constantType = new FieldURIOrConstantType();
        constantType.setConstant(constantValue);
        return constantType;
    }

    protected static JAXBElement<SearchExpressionType> getSearchExpression(String name, SearchExpressionType value) {
        return new JAXBElement<SearchExpressionType>(
            new QName("http://schemas.microsoft.com/exchange/services/2006/types", name), SearchExpressionType.class, value);
    }

    /**
     * Checks the supplied response message, and throws an appropriate exception if there are errors or warnings.
     *
     * @param responseMessage The response message to check
     * @throws OXException
     */
    protected static void check(ResponseMessageType responseMessage) throws OXException {
        if (null == responseMessage) {
            throw EWSExceptionCodes.NO_RESPONSE.create();
        }
        if (false == ResponseClassType.SUCCESS.equals(responseMessage.getResponseClass())) {
            throw EWSExceptionCodes.create(responseMessage);
        }
    }

    /**
     * Checks the supplied response messages, throwing an appropriate exception if there are errors or warnings.
     *
     * @param responseMessages The response messages to check
     * @throws OXException
     */
    protected static void check(List<ResponseMessageType> responseMessages) throws OXException {
        if (null == responseMessages || 0 == responseMessages.size()) {
            throw EWSExceptionCodes.NO_RESPONSE.create();
        }
        for (ResponseMessageType responseMessage : responseMessages) {
            check(responseMessage);

        }
    }

    /**
     * Extracts the response messages from the supplied response holder.
     *
     * @param responseHolder The response holder
     * @return The response messages
     * @throws OXException If there are no respsonses
     */
    protected static List<ResponseMessageType> getResponseMessages(Holder<? extends BaseResponseMessageType> responseHolder) throws OXException {
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
                    return responses;
                }
            }
        }
        throw EWSExceptionCodes.NO_RESPONSE.create();
    }

    /**
     * Extracts a single response messages from the supplied response holder.
     *
     * @param responseHolder The response holder
     * @return The response message
     * @throws OXException If there are 0 or more than 1 responses
     */
    protected static ResponseMessageType getResponseMessage(Holder<? extends BaseResponseMessageType> responseHolder) throws OXException {
        List<ResponseMessageType> responseMessages = getResponseMessages(responseHolder);
        if (null == responseMessages || 0 == responseMessages.size()) {
            throw EWSExceptionCodes.NO_RESPONSE.create();
        } else if (1 != responseMessages.size()) {
            throw EWSExceptionCodes.UNEXPECTED_RESPONSE_COUNT.create(1, 2);
        } else {
            return responseMessages.get(0);
        }
    }

}