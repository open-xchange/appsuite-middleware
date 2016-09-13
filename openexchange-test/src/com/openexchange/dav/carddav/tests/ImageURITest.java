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

package com.openexchange.dav.carddav.tests;

import static org.junit.Assert.*;
import java.net.URI;
import java.util.Date;
import java.util.Map;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.MultiStatusResponse;
import org.apache.jackrabbit.webdav.client.methods.PropFindMethod;
import org.apache.jackrabbit.webdav.client.methods.PutMethod;
import org.apache.jackrabbit.webdav.client.methods.ReportMethod;
import org.apache.jackrabbit.webdav.property.DavPropertyNameSet;
import org.apache.jackrabbit.webdav.version.report.ReportInfo;
import org.junit.Assert;
import org.junit.Test;
import com.openexchange.dav.Headers;
import com.openexchange.dav.PropertyNames;
import com.openexchange.dav.StatusCodes;
import com.openexchange.dav.SyncToken;
import com.openexchange.dav.carddav.CardDAVTest;
import com.openexchange.dav.carddav.Photos;
import com.openexchange.dav.carddav.VCardResource;
import com.openexchange.dav.carddav.reports.AddressbookMultiGetReportInfo;
import com.openexchange.groupware.container.Contact;
import net.sourceforge.cardme.vcard.VCard;
import net.sourceforge.cardme.vcard.arch.EncodingType;
import net.sourceforge.cardme.vcard.types.PhotoType;
import net.sourceforge.cardme.vcard.types.media.ImageMediaType;

/**
 * {@link ImageURITest}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.3
 */
public class ImageURITest extends CardDAVTest {

    @Test
    public void testCreateOnServerAsURI() throws Exception {
        testCreateOnServer("photo=uri");
    }

    @Test
    public void testCreateOnServerBinary() throws Exception {
        testCreateOnServer("photo=binary");
    }

    @Test
    public void testCreateOnClientAsURI() throws Exception {
        testCreateOnClient("photo=uri");
    }

    @Test
    public void testCreateOnClientBinary() throws Exception {
        testCreateOnClient("photo=binary");
    }

    @Test
    public void testUpdateOnClientAsURI() throws Exception {
        testUpdateOnClient("photo=uri");
    }

    @Test
    public void testUpdateOnClientBinary() throws Exception {
        testUpdateOnClient("photo=binary");
    }

    @Test
    public void testRemoveOnClientAsURI() throws Exception {
        testRemoveOnClient("photo=uri");
    }

    @Test
    public void testRemoveOnClientBinary() throws Exception {
        testRemoveOnClient("photo=binary");
    }

    private void testCreateOnServer(String prefer) throws Exception {
        /*
         * fetch sync token for later synchronization
         */
        SyncToken syncToken = new SyncToken(fetchSyncToken());
        /*
         * create contact on server
         */
        String uid = randomUID();
        Contact contact = new Contact();
        contact.setImage1(Photos.PNG_100x100);
        contact.setImageContentType("image/png");
        contact.setUid(uid);
        rememberForCleanUp(create(contact));
        /*
         * sync collection
         */
        Map<String, String> eTags = syncCollection(syncToken).getETagsStatusOK();
        assertTrue("no resource changes reported on sync collection", 0 < eTags.size());
        String href = eTags.keySet().iterator().next();
        /*
         * get & verify photo in vCard
         */
        verifyPhoto(href, contact.getImage1(), prefer);
    }

    private void testCreateOnClient(String prefer) throws Exception {
        /*
         * create contact on client
         */
        String uid = randomUID();
        String href = "/carddav/Contacts/" + uid + ".vcf";
        VCardResource vCard = new VCardResource(
            "BEGIN:VCARD" + "\r\n" +
            "PRODID:-//Example Inc.//Example Client 1.0//EN" + "\r\n" +
            "VERSION:3.0" + "\r\n" +
            "UID:" + uid + "\r\n" +
            "REV:" + formatAsUTC(new Date()) + "\r\n" +
            "END:VCARD" + "\r\n"
            , href, null);
        PhotoType photo = new PhotoType();
        photo.setImageMediaType(ImageMediaType.PNG);
        photo.setEncodingType(EncodingType.BINARY);
        photo.setPhoto(Photos.PNG_100x100);
        vCard.getVCard().addPhoto(photo);
        PutMethod put = null;
        try {
            put = new PutMethod(getBaseUri() + href);
            put.addRequestHeader("Prefer", prefer);
            put.addRequestHeader(Headers.IF_NONE_MATCH, "*");
            put.setRequestEntity(new StringRequestEntity(vCard.toString(), "text/vcard", "UTF-8"));
            assertEquals("Response code wrong", StatusCodes.SC_CREATED, getWebDAVClient().executeMethod(put));
        } finally {
            release(put);
        }
        /*
         * get & verify contact on server
         */
        Contact contact = getContact(uid);
        assertNotNull(contact);
        Assert.assertArrayEquals("image data wrong", Photos.PNG_100x100, contact.getImage1());
        /*
         * get & verify photo in vCard
         */
        verifyPhoto(href, contact.getImage1(), prefer);
    }

    private void testUpdateOnClient(String prefer) throws Exception {
        /*
         * fetch sync token for later synchronization
         */
        SyncToken syncToken = new SyncToken(fetchSyncToken());
        /*
         * create contact on server
         */
        String uid = randomUID();
        Contact contact = new Contact();
        contact.setImage1(Photos.PNG_100x100);
        contact.setImageContentType("image/png");
        contact.setUid(uid);
        rememberForCleanUp(create(contact));
        /*
         * sync collection
         */
        Map<String, String> eTags = syncCollection(syncToken).getETagsStatusOK();
        assertTrue("no resource changes reported on sync collection", 0 < eTags.size());
        String href = eTags.keySet().iterator().next();
        /*
         * get & verify photo in vCard
         */
        VCardResource vCard = verifyPhoto(href, contact.getImage1(), prefer);
        /*
         * update photo on client
         */
        vCard.getVCard().removePhoto(vCard.getVCard().getPhotos().get(0));
        PhotoType photo = new PhotoType();
        photo.setImageMediaType(ImageMediaType.SGIF);
        photo.setEncodingType(EncodingType.BINARY);
        photo.setPhoto(Photos.GIF_100x100);
        vCard.getVCard().addPhoto(photo);
        putVCardUpdate(uid, vCard.toString(), vCard.getETag());
        /*
         * get & verify contact on server
         */
        contact = getContact(uid);
        assertNotNull(contact);
        Assert.assertArrayEquals("image data wrong", Photos.GIF_100x100, contact.getImage1());
        /*
         * get & verify photo in vCard
         */
        verifyPhoto(href, Photos.GIF_100x100, prefer);
    }

    private void testRemoveOnClient(String prefer) throws Exception {
        /*
         * fetch sync token for later synchronization
         */
        SyncToken syncToken = new SyncToken(fetchSyncToken());
        /*
         * create contact on server
         */
        String uid = randomUID();
        Contact contact = new Contact();
        contact.setImage1(Photos.PNG_100x100);
        contact.setImageContentType("image/png");
        contact.setUid(uid);
        rememberForCleanUp(create(contact));
        /*
         * sync collection
         */
        Map<String, String> eTags = syncCollection(syncToken).getETagsStatusOK();
        assertTrue("no resource changes reported on sync collection", 0 < eTags.size());
        String href = eTags.keySet().iterator().next();
        /*
         * get & verify photo in vCard
         */
        VCardResource vCard = verifyPhoto(href, contact.getImage1(), prefer);
        /*
         * remove photo on client
         */
        vCard.getVCard().removePhoto(vCard.getVCard().getPhotos().get(0));
        putVCardUpdate(uid, vCard.toString(), vCard.getETag());
        /*
         * get & verify contact on server
         */
        contact = getContact(uid);
        assertNotNull(contact);
        assertNull(contact.getImage1());
        /*
         * get & verify photo in vCard
         */
        verifyPhoto(href, null, prefer);
    }

	private void verifyPhoto(byte[] expectedPhoto, VCard vCard, String prefer) throws Exception {
        if (null == expectedPhoto) {
            assertTrue("PHOTO wrong", null == vCard.getPhotos() || 0 == vCard.getPhotos().size());
        } else {
            assertTrue("PHOTO wrong", null != vCard.getPhotos() && 0 < vCard.getPhotos().size());
            PhotoType photoProperty = vCard.getPhotos().get(0);
    	    if ("photo=uri".equals(prefer)) {
                URI photoURI = photoProperty.getPhotoURI();
                assertNotNull("POHTO wrong", photoURI);
                Assert.assertArrayEquals("image data wrong", expectedPhoto, downloadPhoto(photoURI));
            } else {
                byte[] vCardPhoto = photoProperty.getPhoto();
                assertNotNull("POHTO wrong", vCardPhoto);
                Assert.assertArrayEquals("image data wrong", expectedPhoto, vCardPhoto);
            }
        }
	}

	private byte[] downloadPhoto(URI uri) throws Exception {
        GetMethod get = null;
        try {
            get = new GetMethod(uri.toString());
            assertEquals(StatusCodes.SC_OK, getWebDAVClient().executeMethod(get));
            return get.getResponseBody();
        } finally {
            release(get);
        }
	}

	private VCardResource verifyPhoto(String href, byte[] expectedPhoto, String prefer) throws Exception {
        /*
         * get & verify vCard via plain GET
         */
        VCardResource card = get(href, prefer);
        assertNotNull(card);
        verifyPhoto(expectedPhoto, card.getVCard(), prefer);
        /*
         * get & verify vCard via addressbook-multiget REPORT
         */
        card = addressbookMultiGet(href, prefer);
        assertNotNull(card);
        verifyPhoto(expectedPhoto, card.getVCard(), prefer);
        /*
         * get & verify vCard via PROPFIND
         */
        card = propFind(href, prefer);
        assertNotNull(card);
        verifyPhoto(expectedPhoto, card.getVCard(), prefer);
        return card;
	}

    private VCardResource get(String href, String prefer) throws Exception {
        GetMethod get = null;
        try {
            get = new GetMethod(getWebDAVClient().getBaseURI() + href);
            get.setRequestHeader("Prefer", prefer);
            String vCard = getWebDAVClient().doGet(get);
            if (null == vCard) {
                return null;
            }
            Header eTagHeader = get.getResponseHeader("ETag");
            String eTag = null != eTagHeader ? eTagHeader.getValue() : null;
            return new VCardResource(vCard, href, eTag);
        } finally {
            release(get);
        }
    }

    private VCardResource addressbookMultiGet(String href, String prefer) throws Exception {
        DavPropertyNameSet props = new DavPropertyNameSet();
        props.add(PropertyNames.GETETAG);
        props.add(PropertyNames.ADDRESS_DATA);
        ReportInfo reportInfo = new AddressbookMultiGetReportInfo(new String[] { href }, props);
        ReportMethod report = null;
        MultiStatusResponse response = null;
        try {
            report = new ReportMethod(getWebDAVClient().getBaseURI() + "/carddav/Contacts/", reportInfo);
            report.setRequestHeader("Prefer", prefer);
            response = assertSingleResponse(getWebDAVClient().doReport(report, StatusCodes.SC_MULTISTATUS));
        } finally {
            release(report);
        }
        String eTag = extractTextContent(PropertyNames.GETETAG, response);
        String data = extractTextContent(PropertyNames.ADDRESS_DATA, response);
        return new VCardResource(data, href, eTag);
    }

    private VCardResource propFind(String href, String prefer) throws Exception {
        DavPropertyNameSet props = new DavPropertyNameSet();
        props.add(PropertyNames.GETETAG);
        props.add(PropertyNames.ADDRESS_DATA);
        MultiStatusResponse response = null;
        PropFindMethod propFind = null;
        try {
            propFind = new PropFindMethod(getBaseUri() + href, DavConstants.PROPFIND_BY_PROPERTY, props, DavConstants.DEPTH_0);
            propFind.setRequestHeader("Prefer", prefer);
            response = assertSingleResponse(getWebDAVClient().doPropFind(propFind, StatusCodes.SC_MULTISTATUS));
        } finally {
            release(propFind);
        }
        String eTag = extractTextContent(PropertyNames.GETETAG, response);
        String data = extractTextContent(PropertyNames.ADDRESS_DATA, response);
        return new VCardResource(data, href, eTag);
    }

}
