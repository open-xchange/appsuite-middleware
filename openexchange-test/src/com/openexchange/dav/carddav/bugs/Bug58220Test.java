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
        String href = "/carddav/" + collection + "/" + uid + ".vcf";
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
