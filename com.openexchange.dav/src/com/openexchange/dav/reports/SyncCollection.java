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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

import static com.openexchange.webdav.protocol.Protocol.DAV_NS;
import java.util.ArrayList;
import java.util.List;
import org.jdom2.Document;
import org.jdom2.Element;
import com.openexchange.dav.DAVProtocol;
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
    public void perform(WebdavRequest req, WebdavResponse res) throws WebdavProtocolException {
        Document requestBody = optRequestBody(req);
        String token = getSyncToken(req, requestBody);
        SyncStatus<WebdavResource> syncStatus = ((CommonFolderCollection<?>) req.getCollection()).getSyncStatus(token);
        final List<Element> all = new ArrayList<Element>();
        final int[] statusCodes = syncStatus.getStatusCodes();
        final PropertiesMarshaller helper = new PropertiesMarshaller(req.getURLPrefix(), req.getCharset());

        ResourceMarshaller marshaller = getMarshaller(req, requestBody);
        for (final int sc : statusCodes) {
            if (sc == 404) {
                for(final WebdavStatus<WebdavResource> status : syncStatus.toIterable(sc)) {
                    final WebdavResource resource = status.getAdditional();
                    final Element r =  new Element("response",DAV_NS);
                    r.addContent(helper.marshalHREF(resource.getUrl(), resource.getResourceType() != null));
                    r.addContent(helper.marshalStatus(404));
                    all.add(r);
                }
            } else {
                for(final WebdavStatus<WebdavResource> status : syncStatus.toIterable(sc)) {
                    final List<Element> marshalled = marshaller.marshal(status.getAdditional());
                    all.addAll(marshalled);
                }
            }
        }

        Element multistatusElement = prepareMultistatusElement();
        Element syncToken = new Element("sync-token", DAV_NS);
        syncToken.setText(syncStatus.getToken());
        multistatusElement.addContent(syncToken);
        multistatusElement.addContent(all);
        sendMultistatusResponse(res, multistatusElement);
    }

    private String getSyncToken(final WebdavRequest req, final Document requestBody) throws WebdavProtocolException {

        final List<Element> children = null == requestBody ? null : requestBody.getRootElement().getChildren("sync-token", DAV_NS);
        if (children == null || children.isEmpty()) {
            return null;
        }

        final Element tokenElement = children.get(0);
        return tokenElement.getText();
    }
}
