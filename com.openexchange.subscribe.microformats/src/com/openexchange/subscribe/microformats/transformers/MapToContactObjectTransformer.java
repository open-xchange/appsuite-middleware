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
package com.openexchange.subscribe.microformats.transformers;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import com.openexchange.groupware.container.Contact;
import com.openexchange.subscribe.microformats.objectparser.OXMFVisitor;
import com.openexchange.tools.ImageTypeDetector;
import com.openexchange.tools.stream.UnsynchronizedByteArrayOutputStream;

/**
 * @author <a href="mailto:karsten.will@open-xchange.com">Karsten Will</a>
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a> - refactoring
 */
public class MapToContactObjectTransformer implements MapToObjectTransformer{

    private static final SimpleDateFormat DATE = new SimpleDateFormat("yyyy-MM-dd");
    static {
        DATE.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    private static final String OXMF_PREFIX = OXMFVisitor.OXMF_PREFIX;

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(MapToContactObjectTransformer.class);

	@Override
    public List<Contact> transform (List<Map<String, String>> inlist){
		ArrayList<Contact> outlist = new ArrayList<Contact>();

		for (Map<String,String> map : inlist){
		    Contact contact = transform(map);
			outlist.add(contact);
		}

		return outlist;
	}

    public Contact transform(Map<String, String> map) {
        Contact contact = new Contact();
        if (map.containsKey(OXMF_PREFIX + "displayName")){
            contact.setDisplayName(map.get(OXMF_PREFIX + "displayName"));
        }
        if (map.containsKey(OXMF_PREFIX + "firstName")){
            contact.setGivenName(map.get(OXMF_PREFIX + "firstName"));
        }
        if (map.containsKey(OXMF_PREFIX + "lastName")){
            contact.setSurName(map.get(OXMF_PREFIX + "lastName"));
        }
        if (map.containsKey(OXMF_PREFIX + "secondName")){
            contact.setMiddleName(map.get(OXMF_PREFIX + "secondName"));
        }
        if (map.containsKey(OXMF_PREFIX + "suffix")){
            contact.setSuffix(map.get(OXMF_PREFIX + "suffix"));
        }
        if (map.containsKey(OXMF_PREFIX + "title")){
            contact.setTitle(map.get(OXMF_PREFIX + "title"));
        }
        if (map.containsKey(OXMF_PREFIX + "streetPrivate")){
            contact.setStreetHome(map.get(OXMF_PREFIX + "streetPrivate"));
        }
        if (map.containsKey(OXMF_PREFIX + "postalCodePrivate")){
            contact.setPostalCodeHome(map.get(OXMF_PREFIX + "postalCodePrivate"));
        }
        if (map.containsKey(OXMF_PREFIX + "cityPrivate")){
            contact.setCityHome(map.get(OXMF_PREFIX + "cityPrivate"));
        }
        if (map.containsKey(OXMF_PREFIX + "statePrivate")){
            contact.setStateHome(map.get(OXMF_PREFIX + "statePrivate"));
        }
        if (map.containsKey(OXMF_PREFIX + "countryPrivate")){
            contact.setCountryHome(map.get(OXMF_PREFIX + "countryPrivate"));
        }
        if (map.containsKey(OXMF_PREFIX + "streetBusiness")){
            contact.setStreetBusiness(map.get(OXMF_PREFIX + "streetBusiness"));
        }
        if (map.containsKey(OXMF_PREFIX + "postalCodeBusiness")){
            contact.setPostalCodeBusiness(map.get(OXMF_PREFIX + "postalCodeBusiness"));
        }
        if (map.containsKey(OXMF_PREFIX + "cityBusiness")){
            contact.setCityBusiness(map.get(OXMF_PREFIX + "cityBusiness"));
        }
        if (map.containsKey(OXMF_PREFIX + "stateBusiness")){
            contact.setStateBusiness(map.get(OXMF_PREFIX + "stateBusiness"));
        }
        if (map.containsKey(OXMF_PREFIX + "countryBusiness")){
            contact.setCountryBusiness(map.get(OXMF_PREFIX + "countryBusiness"));
        }
        if (map.containsKey(OXMF_PREFIX + "streetOther")){
            contact.setStreetOther(map.get(OXMF_PREFIX + "streetOther"));
        }
        if (map.containsKey(OXMF_PREFIX + "postalCodeOther")){
            contact.setPostalCodeOther(map.get(OXMF_PREFIX + "postalCodeOther"));
        }
        if (map.containsKey(OXMF_PREFIX + "cityOther")){
            contact.setCityOther(map.get(OXMF_PREFIX + "cityOther"));
        }
        if (map.containsKey(OXMF_PREFIX + "stateOther")){
            contact.setStateOther(map.get(OXMF_PREFIX + "stateOther"));
        }
        if (map.containsKey(OXMF_PREFIX + "countryOther")){
            contact.setCountryOther(map.get(OXMF_PREFIX + "countryOther"));
        }
        if (map.containsKey(OXMF_PREFIX + "birthday")){
            synchronized (DATE) {
                try {
                    contact.setBirthday(DATE.parse(map.get(OXMF_PREFIX + "birthday")));
                } catch (ParseException e) {
                    LOG.error("", e);
                }
            }
        }
        if (map.containsKey(OXMF_PREFIX + "maritalStatus")){
            contact.setMaritalStatus(map.get(OXMF_PREFIX + "maritalStatus"));
        }
        if (map.containsKey(OXMF_PREFIX + "numberOfChildren")){
            contact.setNumberOfChildren(map.get(OXMF_PREFIX + "numberOfChildren"));
        }
        if (map.containsKey(OXMF_PREFIX + "profession")){
            contact.setProfession(map.get(OXMF_PREFIX + "profession"));
        }
        if (map.containsKey(OXMF_PREFIX + "nickname")){
            contact.setNickname(map.get(OXMF_PREFIX + "nickname"));
        }
        if (map.containsKey(OXMF_PREFIX + "spouseName")){
            contact.setSpouseName(map.get(OXMF_PREFIX + "spouseName"));
        }
        if (map.containsKey(OXMF_PREFIX + "anniversary")){
            synchronized (DATE) {
                try {
                    contact.setAnniversary(DATE.parse(map.get(OXMF_PREFIX + "anniversary")));
                } catch (ParseException e) {
                    LOG.error("", e);
                }
            }
        }
        if (map.containsKey(OXMF_PREFIX + "comment")){
            contact.setNote(map.get(OXMF_PREFIX + "comment"));
        }
        if (map.containsKey(OXMF_PREFIX + "department")){
            contact.setDepartment(map.get(OXMF_PREFIX + "department"));
        }
        if (map.containsKey(OXMF_PREFIX + "position")){
            contact.setPosition(map.get(OXMF_PREFIX + "position"));
        }
        if (map.containsKey(OXMF_PREFIX + "jobTitle")){
            contact.setEmployeeType(map.get(OXMF_PREFIX + "jobTitle"));
        }
        if (map.containsKey(OXMF_PREFIX + "roomNumber")){
            contact.setRoomNumber(map.get(OXMF_PREFIX + "roomNumber"));
        }
        if (map.containsKey(OXMF_PREFIX + "numberOfEmployees")){
            contact.setNumberOfEmployee(map.get(OXMF_PREFIX + "numberOfEmployees"));
        }
        if (map.containsKey(OXMF_PREFIX + "salesVolume")){
            contact.setSalesVolume(map.get(OXMF_PREFIX + "salesVolume"));
        }
        if (map.containsKey(OXMF_PREFIX + "taxID")){
            contact.setTaxID(map.get(OXMF_PREFIX + "taxID"));
        }
        if (map.containsKey(OXMF_PREFIX + "commercialRegister")){
            contact.setCommercialRegister(map.get(OXMF_PREFIX + "commercialRegister"));
        }
        if (map.containsKey(OXMF_PREFIX + "branches")){
            contact.setBranches(map.get(OXMF_PREFIX + "branches"));
        }
        if (map.containsKey(OXMF_PREFIX + "businessCategory")){
            contact.setBusinessCategory(map.get(OXMF_PREFIX + "businessCategory"));
        }
        if (map.containsKey(OXMF_PREFIX + "infoCompany")){
            contact.setInfo(map.get(OXMF_PREFIX + "infoCompany"));
        }
        if (map.containsKey(OXMF_PREFIX + "managerName")){
            contact.setManagerName(map.get(OXMF_PREFIX + "managerName"));
        }
        if (map.containsKey(OXMF_PREFIX + "assistantName")){
            contact.setAssistantName(map.get(OXMF_PREFIX + "assistantName"));
        }
        if (map.containsKey(OXMF_PREFIX + "phonePrivate1")){
            contact.setTelephoneHome1(map.get(OXMF_PREFIX + "phonePrivate1"));
        }
        if (map.containsKey(OXMF_PREFIX + "phonePrivate2")){
            contact.setTelephoneHome2(map.get(OXMF_PREFIX + "phonePrivate2"));
        }
        if (map.containsKey(OXMF_PREFIX + "phoneBusiness1")){
            contact.setTelephoneBusiness1(map.get(OXMF_PREFIX + "phoneBusiness1"));
        }
        if (map.containsKey(OXMF_PREFIX + "phoneBusiness2")){
            contact.setTelephoneBusiness2(map.get(OXMF_PREFIX + "phoneBusiness2"));
        }
        if (map.containsKey(OXMF_PREFIX + "phoneOther")){
            contact.setTelephoneOther(map.get(OXMF_PREFIX + "phoneOther"));
        }
        if (map.containsKey(OXMF_PREFIX + "faxBusiness")){
            contact.setFaxBusiness(map.get(OXMF_PREFIX + "faxBusiness"));
        }
        if (map.containsKey(OXMF_PREFIX + "phoneCallback")){
            contact.setTelephoneCallback(map.get(OXMF_PREFIX + "phoneCallback"));
        }
        if (map.containsKey(OXMF_PREFIX + "phoneCar")){
            contact.setTelephoneCar(map.get(OXMF_PREFIX + "phoneCar"));
        }
        if (map.containsKey(OXMF_PREFIX + "phoneCompany")){
            contact.setTelephoneCompany(map.get(OXMF_PREFIX + "phoneCompany"));
        }
        if (map.containsKey(OXMF_PREFIX + "faxPrivate")){
            contact.setFaxHome(map.get(OXMF_PREFIX + "faxPrivate"));
        }
        if (map.containsKey(OXMF_PREFIX + "phoneMobile1")){
            contact.setCellularTelephone1(map.get(OXMF_PREFIX + "phoneMobile1"));
        }
        if (map.containsKey(OXMF_PREFIX + "phoneMobile2")){
            contact.setCellularTelephone2(map.get(OXMF_PREFIX + "phoneMobile2"));
        }
        if (map.containsKey(OXMF_PREFIX + "faxOther")){
            contact.setFaxOther(map.get(OXMF_PREFIX + "faxOther"));
        }
        if (map.containsKey(OXMF_PREFIX + "email1")){
            contact.setEmail1(map.get(OXMF_PREFIX + "email1"));
        }
        if (map.containsKey(OXMF_PREFIX + "email2")){
            contact.setEmail2(map.get(OXMF_PREFIX + "email2"));
        }
        if (map.containsKey(OXMF_PREFIX + "email3")){
            contact.setEmail3(map.get(OXMF_PREFIX + "email3"));
        }
        if (map.containsKey(OXMF_PREFIX + "URL")){
            contact.setURL(map.get(OXMF_PREFIX + "URL"));
        }
        if (map.containsKey(OXMF_PREFIX + "phoneISDN")){
            contact.setTelephoneISDN(map.get(OXMF_PREFIX + "phoneISDN"));
        }
        if (map.containsKey(OXMF_PREFIX + "pager")){
            contact.setTelephonePager(map.get(OXMF_PREFIX + "pager"));
        }
        if (map.containsKey(OXMF_PREFIX + "phonePrimary")){
            contact.setTelephonePrimary(map.get(OXMF_PREFIX + "phonePrimary"));
        }
        if (map.containsKey(OXMF_PREFIX + "phoneRadio")){
            contact.setTelephoneRadio(map.get(OXMF_PREFIX + "phoneRadio"));
        }
        if (map.containsKey(OXMF_PREFIX + "telephone_telex")){
            contact.setTelephoneTelex(map.get(OXMF_PREFIX + "telephone_telex"));
        }
        if (map.containsKey(OXMF_PREFIX + "telex")){
            contact.setTelephoneTelex(map.get(OXMF_PREFIX + "telex"));
        }
        if (map.containsKey(OXMF_PREFIX + "phoneTTYTDD")){
            contact.setTelephoneTTYTTD(map.get(OXMF_PREFIX + "phoneTTYTDD"));
        }
        if (map.containsKey(OXMF_PREFIX + "instantMessenger1")){
            contact.setInstantMessenger1(map.get(OXMF_PREFIX + "instantMessenger1"));
        }
        if (map.containsKey(OXMF_PREFIX + "instantMessenger2")){
            contact.setInstantMessenger2(map.get(OXMF_PREFIX + "instantMessenger2"));
        }
        if (map.containsKey(OXMF_PREFIX + "phoneIP")){
            contact.setTelephoneIP(map.get(OXMF_PREFIX + "phoneIP"));
        }
        if (map.containsKey(OXMF_PREFIX + "phoneAssistant")){
            contact.setTelephoneAssistant(map.get(OXMF_PREFIX + "phoneAssistant"));
        }
        if (map.containsKey(OXMF_PREFIX + "company")){
            contact.setCompany(map.get(OXMF_PREFIX + "company"));
        }
        if (map.containsKey(OXMF_PREFIX + "image")){
            try {
                loadImageFromURL(contact, new URL(map.get(OXMF_PREFIX + "image")));
            } catch (MalformedURLException e) {
                LOG.warn("", e);
                // Discard image. This is all best effort, nothing more, maybe next time.
            }
        }
        if (map.containsKey(OXMF_PREFIX + "userfield01")){
            contact.setUserField01(map.get(OXMF_PREFIX + "userfield01"));
        }
        if (map.containsKey(OXMF_PREFIX + "userfield02")){
            contact.setUserField02(map.get(OXMF_PREFIX + "userfield02"));
        }
        if (map.containsKey(OXMF_PREFIX + "userfield03")){
            contact.setUserField03(map.get(OXMF_PREFIX + "userfield03"));
        }
        if (map.containsKey(OXMF_PREFIX + "userfield04")){
            contact.setUserField04(map.get(OXMF_PREFIX + "userfield04"));
        }
        if (map.containsKey(OXMF_PREFIX + "userfield05")){
            contact.setUserField05(map.get(OXMF_PREFIX + "userfield05"));
        }
        if (map.containsKey(OXMF_PREFIX + "userfield06")){
            contact.setUserField06(map.get(OXMF_PREFIX + "userfield06"));
        }
        if (map.containsKey(OXMF_PREFIX + "userfield07")){
            contact.setUserField07(map.get(OXMF_PREFIX + "userfield07"));
        }
        if (map.containsKey(OXMF_PREFIX + "userfield08")){
            contact.setUserField08(map.get(OXMF_PREFIX + "userfield08"));
        }
        if (map.containsKey(OXMF_PREFIX + "userfield09")){
            contact.setUserField09(map.get(OXMF_PREFIX + "userfield09"));
        }
        if (map.containsKey(OXMF_PREFIX + "userfield10")){
            contact.setUserField10(map.get(OXMF_PREFIX + "userfield10"));
        }
        if (map.containsKey(OXMF_PREFIX + "userfield11")){
            contact.setUserField11(map.get(OXMF_PREFIX + "userfield11"));
        }
        if (map.containsKey(OXMF_PREFIX + "userfield12")){
            contact.setUserField12(map.get(OXMF_PREFIX + "userfield12"));
        }
        if (map.containsKey(OXMF_PREFIX + "userfield13")){
            contact.setUserField13(map.get(OXMF_PREFIX + "userfield13"));
        }
        if (map.containsKey(OXMF_PREFIX + "userfield14")){
            contact.setUserField14(map.get(OXMF_PREFIX + "userfield14"));
        }
        if (map.containsKey(OXMF_PREFIX + "userfield15")){
            contact.setUserField15(map.get(OXMF_PREFIX + "userfield15"));
        }
        if (map.containsKey(OXMF_PREFIX + "userfield16")){
            contact.setUserField16(map.get(OXMF_PREFIX + "userfield16"));
        }
        if (map.containsKey(OXMF_PREFIX + "userfield17")){
            contact.setUserField17(map.get(OXMF_PREFIX + "userfield17"));
        }
        if (map.containsKey(OXMF_PREFIX + "userfield18")){
            contact.setUserField18(map.get(OXMF_PREFIX + "userfield18"));
        }
        if (map.containsKey(OXMF_PREFIX + "userfield19")){
            contact.setUserField19(map.get(OXMF_PREFIX + "userfield19"));
        }
        if (map.containsKey(OXMF_PREFIX + "userfield20")){
            contact.setUserField20(map.get(OXMF_PREFIX + "userfield20"));
        }
        if (map.containsKey(OXMF_PREFIX + "tags")){
            contact.setCategories(map.get(OXMF_PREFIX + "tags"));
        }
        return contact;
    }

    // Shamelessly stolen from OXContainerConverter. Thanks. :)
	private static void loadImageFromURL(final Contact contactContainer, final URL url) {
        String mimeType = null;
        byte[] bytes = null;
        try {
            final URLConnection urlCon = url.openConnection();
            urlCon.setConnectTimeout(2500);
            urlCon.setReadTimeout(2500);
            urlCon.connect();
            mimeType = urlCon.getContentType();
            final BufferedInputStream in = new BufferedInputStream(urlCon.getInputStream(), 65536);
            try {
                final ByteArrayOutputStream buffer = new UnsynchronizedByteArrayOutputStream();
                final byte[] bbuf = new byte[8192];
                int read = -1;
                while ((read = in.read(bbuf, 0, bbuf.length)) != -1) {
                    buffer.write(bbuf, 0, read);
                }
                bytes = buffer.toByteArray(); //value.getBytes(CHARSET_ISO_8859_1);
                // In case the config-file was not read (yet) the default value is given here
                long maxSize=33750000;
                /*if (null != ContactConfig.getInstance().getProperty("max_image_size")){
                    maxSize = Long.parseLong(ContactConfig.getInstance().getProperty("max_image_size"));
                }
                if (maxSize > 0 && bytes.length > maxSize) {
                    LOG.warn("Contact image is too large and is therefore ignored", new Throwable());
                    bytes = null;
                }*/ // FIXME!
            } finally {
                try {
                    in.close();
                } catch (final IOException e) {
                    LOG.error("", e);
                }
            }
        } catch (final java.net.SocketTimeoutException e) {
            final String uri = url.toString();
            LOG.warn("Either connecting to or reading from an image's URI timed out: {}", uri, e);
        } catch (final IOException e) {
            final String uri = url.toString();
            LOG.warn("Image URI could not be loaded: {}", uri, e);
        }
        if (bytes != null) {
            contactContainer.setImage1(bytes);
            if (mimeType == null) {
                mimeType = ImageTypeDetector.getMimeType(bytes);
                /*if ("application/octet-stream".equals(mimeType)) {
                    mimeType = getMimeType(url.toString());
                }*/
            }
            contactContainer.setImageContentType(mimeType);
        }
    }



}
