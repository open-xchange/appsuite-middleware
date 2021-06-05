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

package com.openexchange.dav.carddav.bugs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.util.List;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.jackrabbit.webdav.client.methods.PutMethod;
import org.junit.Test;
import com.openexchange.dav.Config;
import com.openexchange.dav.Headers;
import com.openexchange.dav.StatusCodes;
import com.openexchange.dav.carddav.CardDAVTest;
import com.openexchange.dav.carddav.UserAgents;
import com.openexchange.dav.carddav.VCardResource;
import net.sourceforge.cardme.vcard.types.TelType;
import net.sourceforge.cardme.vcard.types.params.TelParamType;

/**
 * {@link Bug58220Test}
 *
 * Flag "Standard number" in Android deleted when usind 3rd party DardDav Sync Apps
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class Bug58220Test extends CardDAVTest {

    @Override
    protected String getDefaultUserAgent() {
        return UserAgents.IOS_8_4_0;
    }

    @Test
    public void testCreateWithStandardNumber() throws Exception {
        /*
         * prepare vCard
         */
        String collection = String.valueOf(getDefaultFolderID());
        String uid = randomUID();
        String vCard = // @formatter:off
            "BEGIN:VCARD" + "\r\n" +
            "VERSION:2.1" + "\r\n" +
            "N:test123;;;;" + "\r\n" +
            "FN:test123" + "\r\n" +
            "UID:" + uid + "\r\n" +
            "TEL;CELL:12345" + "\r\n" +
            "TEL;HOME;PREF:888999" + "\r\n" +
            "END:VCARD" + "\r\n"
        ; // @formatter:on
        /*
         * create vCard resource on server
         */
        String href = Config.getPathPrefix() + "/carddav/" + collection + "/" + uid + ".vcf";
        PutMethod put = null;
        try {
            put = new PutMethod(getBaseUri() + href);
            put.addRequestHeader(Headers.IF_NONE_MATCH, "*");
            put.setRequestEntity(new StringRequestEntity(vCard, "text/vcard", "UTF-8"));
            assertEquals("Response code wrong", StatusCodes.SC_CREATED, getWebDAVClient().executeMethod(put));
        } finally {
            release(put);
        }
        /*
         * get created vCard from server
         */
        VCardResource vCardResource;
        GetMethod get = null;
        try {
            get = new GetMethod(getBaseUri() + href);
            String reloadedVCard = getWebDAVClient().doGet(get);
            assertNotNull(reloadedVCard);
            Header eTagHeader = get.getResponseHeader("ETag");
            String eTag = null != eTagHeader ? eTagHeader.getValue() : null;
            vCardResource = new VCardResource(reloadedVCard, href, eTag);
        } finally {
            release(get);
        }
        /*
         * verify contact on client
         */
        TelType cellTel = null;
        TelType homeTel = null;
        List<TelType> tels = vCardResource.getVCard().getTels();
        for (TelType tel : tels) {
            if ("12345".equals(tel.getTelephone())) {
                cellTel = tel;
            } else if ("888999".equals(tel.getTelephone())) {
                homeTel = tel;
            }
        }
        assertNotNull(cellTel);
        assertTrue(cellTel.getParams().contains(TelParamType.CELL));
        assertNotNull(homeTel);
        assertTrue(homeTel.getParams().contains(TelParamType.HOME));
        assertTrue(homeTel.getParams().contains(TelParamType.PREF));
    }

}
