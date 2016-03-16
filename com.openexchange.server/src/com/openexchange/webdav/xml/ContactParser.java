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

package com.openexchange.webdav.xml;

import java.io.IOException;
import java.util.ArrayList;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.DistributionListEntryObject;
import com.openexchange.session.Session;
import com.openexchange.tools.encoding.Base64;
import com.openexchange.webdav.WebdavExceptionCode;
import com.openexchange.webdav.xml.fields.CommonFields;
import com.openexchange.webdav.xml.fields.ContactFields;
import com.openexchange.webdav.xml.fields.DataFields;
import com.openexchange.webdav.xml.fields.FolderChildFields;

/**
 * ContactParser
 *
 * @author <a href="mailto:sebastian.kauss@open-xchange.com">Sebastian Kauss</a>
 */

public class ContactParser extends CommonParser {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ContactParser.class);

    public ContactParser(final Session sessionObj) {
        this.sessionObj = sessionObj;
    }

    public void parse(final XmlPullParser parser, final Contact contactobject) throws OXException, XmlPullParserException, IOException {

        while (true) {
            if (parser.getEventType() == XmlPullParser.END_TAG && parser.getName().equals("prop")) {
                break;
            }

            parseElementContact(contactobject, parser);
            parser.nextTag();
        }

    }

    protected void parseElementContact(final Contact contactobject, final XmlPullParser parser) throws XmlPullParserException, IOException, OXException {
        if (!hasCorrectNamespace(parser)) {
            LOG.trace("unknown namespace in tag: {}", parser.getName());
            parser.nextText();
            return;
        }

        if (isTag(parser, ContactFields.LAST_NAME)) {
            contactobject.setSurName(getValue(parser));
            return;
        } else if (isTag(parser, ContactFields.FIRST_NAME)) {
            contactobject.setGivenName(getValue(parser));
            return;
        } else if (isTag(parser, ContactFields.ANNIVERSARY)) {
            contactobject.setAnniversary(getValueAsDate(parser));
            return;
        } else if (isTag(parser, ContactFields.ASSISTANTS_NAME)) {
            contactobject.setAssistantName(getValue(parser));
            return;
        } else if (isTag(parser, ContactFields.BIRTHDAY)) {
            contactobject.setBirthday(getValueAsDate(parser));
            return;
        } else if (isTag(parser, ContactFields.BRANCHES)) {
            contactobject.setBranches(getValue(parser));
            return;
        } else if (isTag(parser, ContactFields.BUSINESS_CATEGORY)) {
            contactobject.setBusinessCategory(getValue(parser));
            return;
        } else if (isTag(parser, CommonFields.CATEGORIES)) {
            contactobject.setCategories(getValue(parser));
            return;
        } else if (isTag(parser, ContactFields.MOBILE1)) {
            contactobject.setCellularTelephone1(getValue(parser));
            return;
        } else if (isTag(parser, ContactFields.MOBILE2)) {
            contactobject.setCellularTelephone2(getValue(parser));
            return;
        } else if (isTag(parser, ContactFields.CITY)) {
            contactobject.setCityHome(getValue(parser));
            return;
        } else if (isTag(parser, ContactFields.BUSINESS_CITY)) {
            contactobject.setCityBusiness(getValue(parser));
            return;
        } else if (isTag(parser, ContactFields.SECOND_CITY)) {
            contactobject.setCityOther(getValue(parser));
            return;
        } else if (isTag(parser, ContactFields.COMMERCIAL_REGISTER)) {
            contactobject.setCommercialRegister(getValue(parser));
            return;
        } else if (isTag(parser, ContactFields.COMPANY)) {
            contactobject.setCompany(getValue(parser));
            return;
        } else if (isTag(parser, ContactFields.COUNTRY)) {
            contactobject.setCountryHome(getValue(parser));
            return;
        } else if (isTag(parser, ContactFields.BUSINESS_COUNTRY)) {
            contactobject.setCountryBusiness(getValue(parser));
            return;
        } else if (isTag(parser, ContactFields.SECOND_COUNTRY)) {
            contactobject.setCountryOther(getValue(parser));
            return;
        } else if (isTag(parser, ContactFields.DEPARTMENT)) {
            contactobject.setDepartment(getValue(parser));
            return;
        } else if (isTag(parser, ContactFields.DISPLAY_NAME)) {
            contactobject.setDisplayName(getValue(parser));
            return;
        } else if (isTag(parser, ContactFields.EMAIL1)) {
            contactobject.setEmail1(getValue(parser));
            return;
        } else if (isTag(parser, ContactFields.EMAIL2)) {
            contactobject.setEmail2(getValue(parser));
            return;
        } else if (isTag(parser, ContactFields.EMAIL3)) {
            contactobject.setEmail3(getValue(parser));
            return;
        } else if (isTag(parser, ContactFields.EMPLOYEE_TYPE)) {
            contactobject.setEmployeeType(getValue(parser));
            return;
        } else if (isTag(parser, ContactFields.FAX_BUSINESS)) {
            contactobject.setFaxBusiness(getValue(parser));
            return;
        } else if (isTag(parser, ContactFields.FAX_HOME)) {
            contactobject.setFaxHome(getValue(parser));
            return;
        } else if (isTag(parser, ContactFields.FAX_OTHER)) {
            contactobject.setFaxOther(getValue(parser));
            return;
        } else if (isTag(parser, ContactFields.FILE_AS)) {
            contactobject.setFileAs(getValue(parser));
            return;
        } else if (isTag(parser, ContactFields.IMAGE1)) {
            final String image = getValue(parser);
            if (image != null) {
                contactobject.setImage1(Base64.decode(image));
            } else {
                contactobject.setImage1(null);
            }
            return;
        } else if (isTag(parser, ContactFields.IMAGE_CONTENT_TYPE)) {
            contactobject.setImageContentType(getValue(parser));
            return;
        } else if (isTag(parser, ContactFields.NOTE)) {
            contactobject.setNote(getValue(parser));
            return;
        } else if (isTag(parser, ContactFields.MORE_INFO)) {
            contactobject.setInfo(getValue(parser));
            return;
        } else if (isTag(parser, ContactFields.INSTANT_MESSENGER)) {
            contactobject.setInstantMessenger1(getValue(parser));
            return;
        } else if (isTag(parser, ContactFields.INSTANT_MESSENGER2)) {
            contactobject.setInstantMessenger2(getValue(parser));
            return;
        } else if (isTag(parser, ContactFields.MARTITAL_STATUS)) {
            contactobject.setMaritalStatus(getValue(parser));
            return;
        } else if (isTag(parser, ContactFields.MANAGERS_NAME)) {
            contactobject.setManagerName(getValue(parser));
            return;
        } else if (isTag(parser, ContactFields.SECOND_NAME)) {
            contactobject.setMiddleName(getValue(parser));
            return;
        } else if (isTag(parser, ContactFields.NICKNAME)) {
            contactobject.setNickname(getValue(parser));
            return;
        } else if (isTag(parser, ContactFields.NUMBER_OF_CHILDREN)) {
            contactobject.setNumberOfChildren(getValue(parser));
            return;
        } else if (isTag(parser, ContactFields.NUMBER_OF_EMPLOYEE)) {
            contactobject.setNumberOfEmployee(getValue(parser));
            return;
        } else if (isTag(parser, ContactFields.POSITION)) {
            contactobject.setPosition(getValue(parser));
            return;
        } else if (isTag(parser, ContactFields.POSTAL_CODE)) {
            contactobject.setPostalCodeHome(getValue(parser));
            return;
        } else if (isTag(parser, ContactFields.BUSINESS_POSTAL_CODE)) {
            contactobject.setPostalCodeBusiness(getValue(parser));
            return;
        } else if (isTag(parser, ContactFields.SECOND_POSTAL_CODE)) {
            contactobject.setPostalCodeOther(getValue(parser));
            return;
        } else if (isTag(parser, ContactFields.PROFESSION)) {
            contactobject.setProfession(getValue(parser));
            return;
        } else if (isTag(parser, ContactFields.ROOM_NUMBER)) {
            contactobject.setRoomNumber(getValue(parser));
            return;
        } else if (isTag(parser, ContactFields.SALES_VOLUME)) {
            contactobject.setSalesVolume(getValue(parser));
            return;
        } else if (isTag(parser, ContactFields.SPOUSE_NAME)) {
            contactobject.setSpouseName(getValue(parser));
            return;
        } else if (isTag(parser, ContactFields.STATE)) {
            contactobject.setStateHome(getValue(parser));
            return;
        } else if (isTag(parser, ContactFields.BUSINESS_STATE)) {
            contactobject.setStateBusiness(getValue(parser));
            return;
        } else if (isTag(parser, ContactFields.SECOND_STATE)) {
            contactobject.setStateOther(getValue(parser));
            return;
        } else if (isTag(parser, ContactFields.STREET)) {
            contactobject.setStreetHome(getValue(parser));
            return;
        } else if (isTag(parser, ContactFields.BUSINESS_STREET)) {
            contactobject.setStreetBusiness(getValue(parser));
            return;
        } else if (isTag(parser, ContactFields.SECOND_STREET)) {
            contactobject.setStreetOther(getValue(parser));
            return;
        } else if (isTag(parser, ContactFields.SUFFIX)) {
            contactobject.setSuffix(getValue(parser));
            return;
        } else if (isTag(parser, ContactFields.TAX_ID)) {
            contactobject.setTaxID(getValue(parser));
            return;
        } else if (isTag(parser, ContactFields.PHONE_ASSISTANT)) {
            contactobject.setTelephoneAssistant(getValue(parser));
            return;
        } else if (isTag(parser, ContactFields.PHONE_BUSINESS)) {
            contactobject.setTelephoneBusiness1(getValue(parser));
            return;
        } else if (isTag(parser, ContactFields.PHONE_BUSINESS2)) {
            contactobject.setTelephoneBusiness2(getValue(parser));
            return;
        } else if (isTag(parser, ContactFields.CALLBACK)) {
            contactobject.setTelephoneCallback(getValue(parser));
            return;
        } else if (isTag(parser, ContactFields.PHONE_CAR)) {
            contactobject.setTelephoneCar(getValue(parser));
            return;
        } else if (isTag(parser, ContactFields.PHONE_COMPANY)) {
            contactobject.setTelephoneCompany(getValue(parser));
            return;
        } else if (isTag(parser, ContactFields.PHONE_HOME)) {
            contactobject.setTelephoneHome1(getValue(parser));
            return;
        } else if (isTag(parser, ContactFields.PHONE_HOME2)) {
            contactobject.setTelephoneHome2(getValue(parser));
            return;
        } else if (isTag(parser, ContactFields.IP_PHONE)) {
            contactobject.setTelephoneIP(getValue(parser));
            return;
        } else if (isTag(parser, ContactFields.ISDN)) {
            contactobject.setTelephoneISDN(getValue(parser));
            return;
        } else if (isTag(parser, ContactFields.PHONE_OTHER)) {
            contactobject.setTelephoneOther(getValue(parser));
            return;
        } else if (isTag(parser, ContactFields.PAGER)) {
            contactobject.setTelephonePager(getValue(parser));
            return;
        } else if (isTag(parser, ContactFields.PRIMARY)) {
            contactobject.setTelephonePrimary(getValue(parser));
            return;
        } else if (isTag(parser, ContactFields.RADIO)) {
            contactobject.setTelephoneRadio(getValue(parser));
            return;
        } else if (isTag(parser, ContactFields.TELEX)) {
            contactobject.setTelephoneTelex(getValue(parser));
            return;
        } else if (isTag(parser, ContactFields.TTY_TDD)) {
            contactobject.setTelephoneTTYTTD(getValue(parser));
            return;
        } else if (isTag(parser, ContactFields.URL)) {
            contactobject.setURL(getValue(parser));
            return;
        } else if (isTag(parser, ContactFields.TITLE)) {
            contactobject.setTitle(getValue(parser));
            return;
        } else if (isTag(parser, ContactFields.USERFIELD01)) {
            contactobject.setUserField01(getValue(parser));
            return;
        } else if (isTag(parser, ContactFields.USERFIELD02)) {
            contactobject.setUserField02(getValue(parser));
            return;
        } else if (isTag(parser, ContactFields.USERFIELD03)) {
            contactobject.setUserField03(getValue(parser));
            return;
        } else if (isTag(parser, ContactFields.USERFIELD04)) {
            contactobject.setUserField04(getValue(parser));
            return;
        } else if (isTag(parser, ContactFields.USERFIELD05)) {
            contactobject.setUserField05(getValue(parser));
            return;
        } else if (isTag(parser, ContactFields.USERFIELD06)) {
            contactobject.setUserField06(getValue(parser));
            return;
        } else if (isTag(parser, ContactFields.USERFIELD07)) {
            contactobject.setUserField07(getValue(parser));
            return;
        } else if (isTag(parser, ContactFields.USERFIELD08)) {
            contactobject.setUserField08(getValue(parser));
            return;
        } else if (isTag(parser, ContactFields.USERFIELD09)) {
            contactobject.setUserField09(getValue(parser));
            return;
        } else if (isTag(parser, ContactFields.USERFIELD10)) {
            contactobject.setUserField10(getValue(parser));
            return;
        } else if (isTag(parser, ContactFields.USERFIELD11)) {
            contactobject.setUserField11(getValue(parser));
            return;
        } else if (isTag(parser, ContactFields.USERFIELD12)) {
            contactobject.setUserField12(getValue(parser));
            return;
        } else if (isTag(parser, ContactFields.USERFIELD13)) {
            contactobject.setUserField13(getValue(parser));
            return;
        } else if (isTag(parser, ContactFields.USERFIELD14)) {
            contactobject.setUserField14(getValue(parser));
            return;
        } else if (isTag(parser, ContactFields.USERFIELD15)) {
            contactobject.setUserField15(getValue(parser));
            return;
        } else if (isTag(parser, ContactFields.USERFIELD16)) {
            contactobject.setUserField16(getValue(parser));
            return;
        } else if (isTag(parser, ContactFields.USERFIELD17)) {
            contactobject.setUserField17(getValue(parser));
            return;
        } else if (isTag(parser, ContactFields.USERFIELD18)) {
            contactobject.setUserField18(getValue(parser));
            return;
        } else if (isTag(parser, ContactFields.USERFIELD19)) {
            contactobject.setUserField19(getValue(parser));
            return;
        } else if (isTag(parser, ContactFields.USERFIELD20)) {
            contactobject.setUserField20(getValue(parser));
            return;
        } else if (isTag(parser, ContactFields.DEFAULTADDRESS)) {
            contactobject.setDefaultAddress(getValueAsInt(parser));
            return;
        } else if (isTag(parser, ContactFields.DISTRIBUTIONLIST)) {
            parseElementDistributionlists(contactobject, parser);
            return;
        } else {
            parseElementCommon(contactobject, parser);
        }
    }

    protected void parseElementDistributionlists(final Contact oxobject, final XmlPullParser parser) throws OXException, XmlPullParserException, IOException {
        final ArrayList<DistributionListEntryObject> distributionlist = new ArrayList<DistributionListEntryObject>();

        boolean isDistributionList = true;

        while (isDistributionList) {
            parser.nextTag();

            if (isEnd(parser)) {
                throw WebdavExceptionCode.IO_ERROR.create("invalid xml in distributionlist!");
            }

            if (parser.getName().equals(ContactFields.DISTRIBUTIONLIST) && parser.getEventType() == XmlPullParser.END_TAG) {
                isDistributionList = false;
                break;
            }

            final DistributionListEntryObject entry = new DistributionListEntryObject();

            if (isTag(parser, "email")) {
                parseElementEntry(parser, entry);
            } else {
                throw WebdavExceptionCode.IO_ERROR.create("unknown xml tag in distributionlist!");
            }

            distributionlist.add(entry);
        }

        oxobject.setDistributionList(distributionlist.toArray(new DistributionListEntryObject[distributionlist.size()]));
    }

    protected void parseElementEntry(final XmlPullParser parser, final DistributionListEntryObject entry) throws OXException, XmlPullParserException, IOException {
        String s = null;

        if ((s = parser.getAttributeValue(XmlServlet.NAMESPACE, DataFields.ID)) != null) {
            final int contact_id = Integer.parseInt(s);
            entry.setEntryID(contact_id);
        }

        if ((s = parser.getAttributeValue(XmlServlet.NAMESPACE, FolderChildFields.FOLDER_ID)) != null) {
            final int folderId = Integer.parseInt(s);
            entry.setFolderID(folderId);
        }

        entry.setEmailfield(Integer.parseInt(parser.getAttributeValue(XmlServlet.NAMESPACE, "emailfield")));
        entry.setDisplayname(parser.getAttributeValue(XmlServlet.NAMESPACE, ContactFields.DISPLAY_NAME));
        entry.setEmailaddress(getValue(parser));

    }

}
