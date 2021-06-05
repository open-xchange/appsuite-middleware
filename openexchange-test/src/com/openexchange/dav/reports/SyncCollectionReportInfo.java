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

import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.property.DavPropertyNameSet;
import org.apache.jackrabbit.webdav.version.report.ReportInfo;
import org.apache.jackrabbit.webdav.xml.DomUtil;
import org.apache.jackrabbit.webdav.xml.Namespace;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import com.openexchange.dav.PropertyNames;

/**
 * {@link SyncCollectionReportInfo}
 * 
 * Encapsulates the BODY of a {@link SyncCollectionReport} request ("sync-collection").
 * 
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class SyncCollectionReportInfo extends ReportInfo {

    private final String syncToken;

    /**
     * Creates a new {@link SyncCollectionReportInfo}.
     * 
     * @param syncToken The sync-token to include in the request
     */
    public SyncCollectionReportInfo(final String syncToken) {
        this(syncToken, null);
    }

    /**
     * Creates a new {@link SyncCollectionReportInfo}.
     * 
     * @param syncToken The sync-token to include in the request
     * @param propertyNames the properties to include in the request
     */
    public SyncCollectionReportInfo(final String syncToken, final DavPropertyNameSet propertyNames) {
        super(SyncCollectionReport.SYNC_COLLECTION, DavConstants.DEPTH_1, propertyNames);
        this.syncToken = syncToken;
    }

    /**
     * Creates a new {@link SyncCollectionReportInfo}.
     * 
     * @param propertyNames the properties to include in the request
     */
    public SyncCollectionReportInfo(final DavPropertyNameSet propertyNames) {
        this(null, propertyNames);
    }

    @Override
    public Element toXml(final Document document) {
        /*
         * create sync-collection element
         */
        final Element syncCollectionElement = DomUtil.createElement(document, SyncCollectionReport.SYNC_COLLECTION.getLocalName(), SyncCollectionReport.SYNC_COLLECTION.getNamespace());
        syncCollectionElement.setAttributeNS(Namespace.XMLNS_NAMESPACE.getURI(), Namespace.XMLNS_NAMESPACE.getPrefix() + ":" + PropertyNames.NS_DAV.getPrefix(), PropertyNames.NS_DAV.getURI());
        /*
         * append sync-token element
         */
        if (null != this.syncToken) {
            syncCollectionElement.appendChild(DomUtil.createElement(document, PropertyNames.SYNC_TOKEN.getName(), PropertyNames.NS_DAV, this.syncToken));
        }
        /*
         * append properties element
         */
        syncCollectionElement.appendChild(super.getPropertyNameSet().toXml(document));
        return syncCollectionElement;
    }
}
