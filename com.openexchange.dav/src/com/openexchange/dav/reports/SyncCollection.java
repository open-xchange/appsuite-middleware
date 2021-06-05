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

package com.openexchange.dav.reports;

import javax.servlet.http.HttpServletResponse;
import org.jdom2.Element;
import com.openexchange.dav.DAVProtocol;
import com.openexchange.dav.PreconditionException;
import com.openexchange.dav.actions.PROPFINDAction;
import com.openexchange.dav.resources.FolderCollection;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.webdav.action.WebdavRequest;
import com.openexchange.webdav.action.WebdavResponse;
import com.openexchange.webdav.protocol.Protocol;
import com.openexchange.webdav.protocol.WebdavProtocolException;
import com.openexchange.webdav.protocol.WebdavResource;
import com.openexchange.webdav.protocol.WebdavStatus;
import com.openexchange.webdav.xml.resources.PropertiesMarshaller;
import com.openexchange.webdav.xml.resources.ResourceMarshaller;

/**
 * {@link SyncCollection}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class SyncCollection extends PROPFINDAction {

    public static final String NAMESPACE = Protocol.DEFAULT_NAMESPACE;
    public static final String NAME = "sync-collection";

    /**
     * Initializes a new {@link SyncCollection}.
     *
     * @param protocol The underlying WebDAV protocol
     */
    public SyncCollection(DAVProtocol protocol) {
        super(protocol);
    }

    @Override
    public void perform(WebdavRequest request, WebdavResponse response) throws WebdavProtocolException {
        /*
         * extract client sync-token & limit if defined
         */
        Element rootElement = requireRootElement(request, Protocol.DAV_NS, "sync-collection");
        Element syncTokenElement = rootElement.getChild("sync-token", DAVProtocol.DAV_NS);
        String syncToken = null != syncTokenElement ? syncTokenElement.getText() : null;
        int limit = -1;
        Element limitElement = rootElement.getChild("limit", DAVProtocol.DAV_NS);
        if (null != limitElement) {
            String nResults = limitElement.getChildText("nresults", DAVProtocol.DAV_NS);
            if (Strings.isNotEmpty(nResults)) {
                try {
                    limit = Integer.parseInt(nResults);
                } catch (NumberFormatException e) {
                    throw new PreconditionException(new OXException(e), DAVProtocol.DAV_NS.getURI(), "number-of-matches-within-limits", request.getUrl(), DAVProtocol.SC_INSUFFICIENT_STORAGE);
                }
            }
        }
        /*
         * query sync status from targeted folder collection
         */
        FolderCollection<?> folderCollection = requireResource(request, FolderCollection.class);
        SyncStatus<WebdavResource> syncStatus = folderCollection.getSyncStatus(syncToken, limit);
        if (null == syncStatus) {
            throw new PreconditionException(DAVProtocol.DAV_NS.getURI(), "supported-report", request.getUrl(), HttpServletResponse.SC_FORBIDDEN);
        }
        /*
         * marshal multistatus response
         */
        ResourceMarshaller marshaller = getMarshaller(request, requireRequestBody(request));
        PropertiesMarshaller helper = new PropertiesMarshaller(request.getURLPrefix(), request.getCharset());
        Element multistatusElement = prepareMultistatusElement();
        for (int status : syncStatus.getStatusCodes()) {
            if (HttpServletResponse.SC_NOT_FOUND == status) {
                /*
                 * marshal explicit "not found" response for each deleted resource
                 */
                for (WebdavStatus<WebdavResource> webdavStatus : syncStatus.toIterable(status)) {
                    WebdavResource resource = webdavStatus.getAdditional();
                    multistatusElement.addContent(new Element("response", Protocol.DAV_NS)
                        .addContent(helper.marshalHREF(resource.getUrl(), resource.isCollection()))
                        .addContent(helper.marshalStatus(status))
                    );
                }
            } else if (DAVProtocol.SC_INSUFFICIENT_STORAGE == status) {
                /*
                 * marshal HTTP 507 response in case of truncated results 
                 */
                for (WebdavStatus<WebdavResource> webdavStatus : syncStatus.toIterable(status)) {
                    WebdavResource resource = webdavStatus.getAdditional();
                    multistatusElement.addContent(new Element("response", Protocol.DAV_NS)
                        .addContent(helper.marshalHREF(resource.getUrl(), resource.isCollection()))
                        .addContent(helper.marshalStatus(status))
                        .addContent(new Element("error", Protocol.DAV_NS).addContent(new Element("number-of-matches-within-limits", Protocol.DAV_NS.getURI())))
                    );
                }
            } else {
                /*
                 * marshal common multistatus response for each new/updated resource
                 */
                for (WebdavStatus<WebdavResource> webdavStatus : syncStatus.toIterable(status)) {
                    multistatusElement.addContent(marshaller.marshal(webdavStatus.getAdditional()));
                }
            }
        }
        /*
         * include next sync-token & send response
         */
        multistatusElement.addContent(new Element("sync-token", Protocol.DAV_NS).setText(syncStatus.getToken()));
        sendMultistatusResponse(response, multistatusElement);
    }

}
