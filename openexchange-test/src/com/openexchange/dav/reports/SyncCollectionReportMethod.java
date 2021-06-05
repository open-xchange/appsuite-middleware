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

import java.io.IOException;
import org.apache.commons.httpclient.HttpConnection;
import org.apache.commons.httpclient.HttpState;
import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.client.methods.ReportMethod;
import org.apache.jackrabbit.webdav.version.report.ReportInfo;
import org.apache.jackrabbit.webdav.xml.DomUtil;
import org.apache.jackrabbit.webdav.xml.ElementIterator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import com.openexchange.dav.PropertyNames;

/**
 * {@link SyncCollectionReportMethod} - Report method for the
 * "sync-collection" request.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class SyncCollectionReportMethod extends ReportMethod {

    private String syncToken;
    private Document responseDocument = null;

    public SyncCollectionReportMethod(String uri, ReportInfo reportInfo) throws IOException {
        super(uri, reportInfo);
        this.syncToken = null;
    }

    public String getSyncTokenFromResponse() {
        return syncToken;
    }

    public SyncCollectionResponse getResponseBodyAsSyncCollection() throws IOException, DavException {
        checkUsed();
        return new SyncCollectionResponse(this.getResponseBodyAsMultiStatus(), this.syncToken);
    }

    @Override
    public Document getResponseBodyAsDocument() throws IOException {
        if (null == this.responseDocument) {
            this.responseDocument = super.getResponseBodyAsDocument();
        }
        return responseDocument;
    }

    @Override
    protected void processResponseBody(HttpState httpState, HttpConnection httpConnection) {
        super.processResponseBody(httpState, httpConnection);
        Document document = null;
        try {
            document = getResponseBodyAsDocument();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (null != document) {
            ElementIterator it = DomUtil.getChildren(document.getDocumentElement(), PropertyNames.SYNC_TOKEN.getName(), PropertyNames.SYNC_TOKEN.getNamespace());
            if (it.hasNext()) {
                Element respElem = it.nextElement();
                this.syncToken = respElem.getTextContent();
            }
        }
    }

}
