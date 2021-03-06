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

package com.openexchange.carddav.action;

import static com.openexchange.webdav.protocol.Protocol.DAV_NS;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import com.openexchange.carddav.CarddavProtocol;
import com.openexchange.carddav.GroupwareCarddavFactory;
import com.openexchange.carddav.resources.BulkImportResult;
import com.openexchange.carddav.resources.CardDAVCollection;
import com.openexchange.carddav.resources.ContactResource;
import com.openexchange.dav.DAVProtocol;
import com.openexchange.dav.PreconditionException;
import com.openexchange.dav.SimilarityException;
import com.openexchange.dav.actions.POSTAction;
import com.openexchange.exception.OXException;
import com.openexchange.java.Streams;
import com.openexchange.webdav.action.WebdavPutAction.SizeExceededInputStream;
import com.openexchange.webdav.action.WebdavRequest;
import com.openexchange.webdav.action.WebdavResponse;
import com.openexchange.webdav.protocol.Protocol;
import com.openexchange.webdav.protocol.WebdavProperty;
import com.openexchange.webdav.protocol.WebdavProtocolException;
import com.openexchange.webdav.protocol.helpers.AbstractResource;
import com.openexchange.webdav.xml.resources.PropfindResponseMarshaller;

/**
 * {@link CardDAVPOSTAction}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.1
 */
public class CardDAVPOSTAction extends POSTAction {

    private final GroupwareCarddavFactory factory;

    private static final String MAX_SIMILARITY = "X-OX-MAX-SIMILARITY";

    /**
     * Initializes a new {@link CardDAVPOSTAction}.
     *
     * @param factory The factory
     */
    public CardDAVPOSTAction(GroupwareCarddavFactory factory) {
        super(factory.getProtocol());
        this.factory = factory;
	}

	@Override
	public void perform(WebdavRequest request, WebdavResponse response) throws WebdavProtocolException {
	    if (super.handle(request, response)) {
	        return;
	    }
	    if (ContactResource.CONTENT_TYPE.equalsIgnoreCase(getContentType(request))) {
	        /*
	         * check indicated content length
	         */
	        long contentLength = getContentLength(request);
	        long maxSize = factory.getState().getMaxUploadSize();
	        if (-1 != contentLength && 0 < maxSize && maxSize < contentLength) {
	            throw WebdavProtocolException.Code.GENERAL_ERROR.create(request.getUrl(), HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE);
	        }
	        /*
	         * perform "simple" bulk import
	         */
	        bulkImport(request, response);
	        return;
	    }
	    /*
	     * no support for other bodies
	     */
	    throw WebdavProtocolException.Code.GENERAL_ERROR.create(request.getUrl(), HttpServletResponse.SC_BAD_REQUEST);
	}

    private void bulkImport(WebdavRequest request, WebdavResponse response) throws WebdavProtocolException {
        CardDAVCollection collection = requireResource(request, CardDAVCollection.class);
        List<BulkImportResult> importResults;
        InputStream inputStream = null;
        try {
            inputStream = request.getBody();
            if (null == inputStream) {
                throw WebdavProtocolException.Code.GENERAL_ERROR.create(request.getUrl(), HttpServletResponse.SC_BAD_REQUEST);
            }
            long maxSize = factory.getState().getMaxUploadSize();
            if (0 < maxSize) {
                inputStream = new SizeExceededInputStream(inputStream, maxSize);
            }
            String maxSimStr = request.getHeader(MAX_SIMILARITY);
            float maxSimilarity = 0;
            if (maxSimStr != null) {
                maxSimilarity = Float.parseFloat(maxSimStr);
                if (maxSimilarity > 1) {
                    maxSimilarity = 1;
                }
            }
            importResults = collection.bulkImport(inputStream, maxSimilarity);
        } catch (IOException e) {
            throw WebdavProtocolException.Code.GENERAL_ERROR.create(request.getUrl(), HttpServletResponse.SC_BAD_REQUEST, e);
        } catch (OXException e) {
            if (null != inputStream && SizeExceededInputStream.class.isInstance(inputStream) && ((SizeExceededInputStream) inputStream).hasExceeded()) {
                throw WebdavProtocolException.Code.GENERAL_ERROR.create(request.getUrl(), HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE);
            }
            if (WebdavProtocolException.class.isInstance(e)) {
                throw (WebdavProtocolException) e;
            }
            throw WebdavProtocolException.Code.GENERAL_ERROR.create(request.getUrl(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e);
        } finally {
            Streams.close(inputStream);
        }
        /*
         * create multistatus response for imported contacts
         */
        PropfindResponseMarshaller marshaller = new PropfindResponseMarshaller(request.getURLPrefix(), request.getCharset(), request.isBrief());
        boolean returnChangedData = "return-changed-data".equals(request.getHeader("X-MobileMe-DAV-Options"));
        Element multistatusElement = marshalImportResults(collection, importResults, marshaller, returnChangedData);
        /*
         * write back response
         */
        sendXMLResponse(response, new Document(multistatusElement), Protocol.SC_MULTISTATUS);
    }

    private Element marshalImportResults(CardDAVCollection collection, List<BulkImportResult> importResults, PropfindResponseMarshaller marshaller, boolean returnChangedData) throws WebdavProtocolException {
        Element multistatusElement = new Element("multistatus", DAV_NS);
        for (Namespace namespace : protocol.getAdditionalNamespaces()) {
            multistatusElement.addNamespaceDeclaration(namespace);
        }
        for (BulkImportResult importResult : importResults) {
            multistatusElement.addContent(marshalImportResult(collection, importResult, marshaller, returnChangedData));
        }
        return multistatusElement;
    }

    private Element marshalImportResult(CardDAVCollection collection, BulkImportResult importResult, PropfindResponseMarshaller marshaller, boolean returnChangedData) throws WebdavProtocolException {
        if (null == importResult.getError()) {
            Element responseElement = new Element("response", DAV_NS);
            responseElement.addContent(marshaller.marshalHREF(importResult.getHref(), false));
            Element propstatElement = new Element("propstat", DAV_NS);
            if (returnChangedData) {
                AbstractResource child = collection.getChild(importResult.getHref().name());
                Element propElement = new Element("prop", DAV_NS);
//                propElement.addContent(marshaller.marshalProperty(child.getProperty(DAV_NS.getURI(), "getetag"), protocol));
                propElement.addContent(new Element("uid", CarddavProtocol.CALENDARSERVER_NS).setText(importResult.getUid()));
                WebdavProperty addressDataProperty = child.getProperty(DAVProtocol.CARD_NS.getURI(), "address-data");
                if (null != addressDataProperty) {
                    propElement.addContent(marshaller.marshalProperty(addressDataProperty, protocol));
                }
                propstatElement.addContent(propElement);
                propstatElement.addContent(marshaller.marshalStatus(HttpServletResponse.SC_OK));
            } else {
                propstatElement.addContent(marshaller.marshalStatus(HttpServletResponse.SC_OK));
                propstatElement.addContent(new Element("uid", CarddavProtocol.CALENDARSERVER_NS).setText(importResult.getUid()));
            }
            responseElement.addContent(propstatElement);
            return responseElement;
        } else {
            return marshalErrorResponse(marshaller, importResult.getError(), importResult.getUid());
        }
    }

    private Element marshalErrorResponse(PropfindResponseMarshaller marshaller, OXException error, String uid) {
        if (error instanceof PreconditionException) {
            PreconditionException preError = (PreconditionException) error;
            Element responseElement = new Element("response", DAV_NS);
            responseElement.addContent(new Element("href", DAV_NS));
            responseElement.addContent(marshaller.marshalStatus(preError.getStatus()));
            Element errorElement = new Element("error", DAVProtocol.DAV_NS);
            errorElement.addContent(preError.getPreconditionElement());
            errorElement.addContent(new Element("uid", CarddavProtocol.CALENDARSERVER_NS).setText(uid));
            responseElement.addContent(errorElement);
            return responseElement;
        }
        if (error instanceof SimilarityException) {
            SimilarityException simError = (SimilarityException) error;
            Element responseElement = new Element("response", DAV_NS);
            responseElement.addContent(new Element("href", DAV_NS));
            responseElement.addContent(marshaller.marshalStatus(simError.getStatus()));
            Element errorElement = new Element("error", DAVProtocol.DAV_NS);
            errorElement.addContent(simError.getElement());
            responseElement.addContent(errorElement);
            return responseElement;
        }
        return null;
    }

}
