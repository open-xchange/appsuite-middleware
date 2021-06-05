/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.carddav.reports;

import static com.openexchange.dav.DAVProtocol.CARD_NS;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import org.jdom2.Attribute;
import org.jdom2.Element;
import com.openexchange.carddav.resources.CardDAVCollection;
import com.openexchange.contact.ContactFieldOperand;
import com.openexchange.contact.vcard.VCardService;
import com.openexchange.dav.DAVFactory;
import com.openexchange.dav.DAVProtocol;
import com.openexchange.dav.PreconditionException;
import com.openexchange.dav.actions.PROPFINDAction;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.java.Strings;
import com.openexchange.search.CompositeSearchTerm;
import com.openexchange.search.CompositeSearchTerm.CompositeOperation;
import com.openexchange.search.SearchTerm;
import com.openexchange.search.SingleSearchTerm;
import com.openexchange.search.SingleSearchTerm.SingleOperation;
import com.openexchange.search.internal.operands.ConstantOperand;
import com.openexchange.webdav.action.WebdavRequest;
import com.openexchange.webdav.action.WebdavResponse;
import com.openexchange.webdav.protocol.WebdavProtocolException;
import com.openexchange.webdav.protocol.WebdavResource;
import com.openexchange.webdav.xml.resources.ResourceMarshaller;
/**
 * {@link AddressbookQueryReport}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.1
 */
public class AddressbookQueryReport extends PROPFINDAction {

    public static final String NAMESPACE = CARD_NS.getURI();
    public static final String NAME = "addressbook-query";

    /**
     * Initializes a new {@link AddressbookQueryReport}.
     *
     * @param protocol The protocol
     */
    public AddressbookQueryReport(DAVProtocol protocol) {
        super(protocol);
    }

    @Override
    public void perform(WebdavRequest request, WebdavResponse response) throws WebdavProtocolException {
        CardDAVCollection collection = requireResource(request, CardDAVCollection.class);
        Element rootElement = requireRootElement(request, CARD_NS, "addressbook-query");
        SearchTerm<?> searchTerm = parseFilter(collection.getFactory(), rootElement.getChild("filter", CARD_NS));
        List<WebdavResource> resources;
        if (null == searchTerm) {
            /*
             * get all elements in collection
             */
            resources = collection.getChildren();
        } else {
            /*
             * get filtered resources
             */
            resources = collection.getFilteredObjects(searchTerm);
        }
        ResourceMarshaller marshaller = getMarshaller(request, optRequestBody(request));
        Element multistatusElement = prepareMultistatusElement();
        List<Element> elements = new ArrayList<Element>();
        for (WebdavResource resource : resources) {
            elements.addAll(marshaller.marshal(resource));
        }
        multistatusElement.addContent(elements);
        sendMultistatusResponse(response, multistatusElement);
    }

    private SearchTerm<?> parseFilter(DAVFactory factory, Element filterElement) throws WebdavProtocolException {
        if (null == filterElement) {
            return null;
        }
        List<Element> propFilterElements = filterElement.getChildren("prop-filter", CARD_NS);
        if (null == propFilterElements || 0 == propFilterElements.size()) {
            return null;
        }
        if (1 == propFilterElements.size()) {
            return parsePropFilter(factory, propFilterElements.get(0));
        }
        CompositeOperation operation = CompositeOperation.OR;
        Attribute testAttribute = filterElement.getAttribute("test");
        if (null != testAttribute && "allof".equals(testAttribute.getValue())) {
            operation = CompositeOperation.AND;
        }
        CompositeSearchTerm compositeTerm = new CompositeSearchTerm(operation);
        for (Element propFilterElement : propFilterElements) {
            compositeTerm.addSearchTerm(parsePropFilter(factory, propFilterElement));
        }
        return compositeTerm;
    }

    private SearchTerm<?> parsePropFilter(DAVFactory factory, Element propFilterElement) throws WebdavProtocolException {
        String name = propFilterElement.getAttributeValue("name");
        ContactField[] matchingFields = getMatchingFields(factory, name);
        if (null == matchingFields || 0 == matchingFields.length) {
            throw new PreconditionException(CARD_NS.getURI(), "supported-filter", null, HttpServletResponse.SC_FORBIDDEN);
        }
        Element isNotDefinedElement = propFilterElement.getChild("is-not-defined", CARD_NS);
        if (null != isNotDefinedElement) {
            if (1 == matchingFields.length) {
                SingleSearchTerm isNullTerm = new SingleSearchTerm(SingleOperation.ISNULL);
                isNullTerm.addOperand(new ContactFieldOperand(matchingFields[0]));
                return isNullTerm;
            }
            CompositeSearchTerm compositeTerm = new CompositeSearchTerm(CompositeOperation.AND);
            for (ContactField field : matchingFields) {
                SingleSearchTerm isNullTerm = new SingleSearchTerm(SingleOperation.ISNULL);
                isNullTerm.addOperand(new ContactFieldOperand(field));
                compositeTerm.addSearchTerm(isNullTerm);
            }
            return compositeTerm;
        }
        Element textMatchElement = propFilterElement.getChild("text-match", CARD_NS);
        String matchType = null;
        String match = null;
        if (textMatchElement != null) {
            matchType = textMatchElement.getAttributeValue("match-type");
            match = textMatchElement.getValue();
        }
        if (Strings.isEmpty(matchType) || "contains".equals(matchType)) {
            match = '*' + match + '*';
        } else if ("starts-with".equals(matchType)) {
            match = match + '*';
        } else if ("ends-with".equals(matchType)) {
            match = '*' + match;
        }
        SearchTerm<?> term;
        if (1 == matchingFields.length) {
            SingleSearchTerm singleSearchTerm = new SingleSearchTerm(SingleOperation.EQUALS);
            singleSearchTerm.addOperand(new ContactFieldOperand(matchingFields[0]));
            singleSearchTerm.addOperand(new ConstantOperand<String>(match));
            term = singleSearchTerm;
        } else {
            CompositeSearchTerm compositeTerm = new CompositeSearchTerm(CompositeOperation.OR);
            for (ContactField field : matchingFields) {
                SingleSearchTerm singleSearchTerm = new SingleSearchTerm(SingleOperation.EQUALS);
                singleSearchTerm.addOperand(new ContactFieldOperand(field));
                singleSearchTerm.addOperand(new ConstantOperand<String>(match));
                compositeTerm.addSearchTerm(singleSearchTerm);
            }
            term = compositeTerm;
        }
        if (null != textMatchElement) {
            Attribute attribute = textMatchElement.getAttribute("negate-condition");
            if (null != attribute && "yes".equals(attribute.getValue())) {
                CompositeSearchTerm notTerm = new CompositeSearchTerm(CompositeOperation.NOT);
                notTerm.addSearchTerm(term);
                term = notTerm;
            }
        }
        return term;
    }

    private static ContactField[] getMatchingFields(DAVFactory factory, String attributeName) {
        if (null == attributeName) {
            return null;
        }
        return factory.getService(VCardService.class).getContactFields(Collections.singleton(attributeName));
    }

}
