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

package com.openexchange.dav.reports;

import javax.servlet.http.HttpServletResponse;
import org.jdom2.Element;
import com.openexchange.dav.DAVProtocol;
import com.openexchange.dav.PreconditionException;
import com.openexchange.dav.actions.PROPFINDAction;
import com.openexchange.dav.resources.CommonFolderCollection;
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
         * extract client sync-token
         */
        Element rootElement = requireRootElement(request, Protocol.DAV_NS, "sync-collection");
        Element syncTokenElement = rootElement.getChild("sync-token", DAVProtocol.DAV_NS);
        String syncToken = null != syncTokenElement ? syncTokenElement.getText() : null;
        Element limitElement = rootElement.getChild("limit", DAVProtocol.DAV_NS);
        if (null != limitElement) {
            throw new PreconditionException(DAVProtocol.DAV_NS.getURI(), "number-of-matches-within-limits", request.getUrl(), DAVProtocol.SC_INSUFFICIENT_STORAGE);
        }
        /*
         * query sync status from targeted folder collection
         */
        CommonFolderCollection<?> folderCollection = requireResource(request, CommonFolderCollection.class);
        SyncStatus<WebdavResource> syncStatus = folderCollection.getSyncStatus(syncToken);
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
