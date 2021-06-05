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

package com.openexchange.dav.caldav.tests;

import org.apache.commons.httpclient.Header;
import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.client.methods.PropFindMethod;
import org.apache.jackrabbit.webdav.property.DavPropertyNameSet;
import org.junit.Assert;
import org.junit.Test;
import com.openexchange.dav.Config;
import com.openexchange.dav.PropertyNames;
import com.openexchange.dav.StatusCodes;
import com.openexchange.dav.caldav.CalDAVTest;

/**
 * {@link CookieTest}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class CookieTest extends CalDAVTest {

    @Test
    public void testNoSessionCookieForCalDAV() throws Exception {
        /*
         * execute simple propfind
         */
        DavPropertyNameSet props = new DavPropertyNameSet();
        props.add(PropertyNames.PRINCIPAL_URL);
        PropFindMethod propFind = new PropFindMethod(getWebDAVClient().getBaseURI() + Config.getPathPrefix() + "/", DavConstants.PROPFIND_BY_PROPERTY, props, DavConstants.DEPTH_0);
        try {
            Assert.assertEquals("unexpected http status", StatusCodes.SC_MULTISTATUS, getWebDAVClient().executeMethod(propFind));
        } finally {
            release(propFind);
        }
        /*
         * check for Set-Cookie header
         */
        Header[] headers = propFind.getResponseHeaders("Set-Cookie");
        if (null != headers) {
            for (Header header : headers) {
                if (header.getValue().contains("JSESSIONID")) {
                    Assert.fail(header.getName());
                }
            }
        }
    }

}
