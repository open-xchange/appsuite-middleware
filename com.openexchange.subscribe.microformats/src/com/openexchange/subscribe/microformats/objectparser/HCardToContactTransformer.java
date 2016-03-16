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

package com.openexchange.subscribe.microformats.objectparser;

import java.net.URI;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import org.microformats.hCard.HCard;
import org.microformats.hCard.HCard.Address;
import org.microformats.hCard.HCard.Email;
import org.microformats.hCard.HCard.Tel;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.java.Strings;
import com.openexchange.subscribe.SubscriptionErrorMessage;
import com.openexchange.subscribe.helpers.HTTPToolkit;
import com.openexchange.tools.encoding.Base64;

/**
 * {@link HCardToContactTransformer}
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class HCardToContactTransformer {

    public List<Contact> transform(List<HCard> hcards){
        LinkedList<Contact> results = new LinkedList<Contact>();
        for(HCard hcard: hcards) {
            results.add(transform(hcard));
        }
        return results;
    }


    private Contact transform(HCard hcard) {
        Contact contact = new Contact();
        if(hcard == null) {
            return contact;
        }

        handleName(hcard, contact);
        handleCompany(hcard, contact);
        handleAdresses(hcard, contact);
        handleTelephones(hcard, contact);
        handleEMail(hcard, contact);
        handleImage(hcard, contact);
        handleAdditionalInfo(hcard,contact);
        handleOXSpecific(hcard,contact);
        return contact;
    }

    private void handleAdditionalInfo(HCard hcard, Contact c) {
        if(hcard.bday != null) {
            c.setBirthday(new Date(hcard.bday));
        }
//        if( hcard.urls != null && hcard.urls.size() > 0)
//            c.setURL(hcard.urls.get(0).toString());
        if( hcard.notes != null && hcard.notes.size() > 0) {
            c.setNote(hcard.notes.get(0));
        }
    }


    private void handleOXSpecific(HCard hcard, Contact c) {
/*      if( key.equals("x-ox-maritalStatus"))
            c.setMaritalStatus(value);
        if( key.equals("x-ox-numberOfChildren"))
            c.setNumberOfChildren(value);
        if( key.equals("x-ox-profession"))
            c.setProfession(value);
        if( key.equals("x-ox-nickname"))
            c.setNickname(value);
        if( key.equals("x-ox-spouse_name""))
            c.setSpouseName(value);
        if( key.equals("x-ox-department"))
            c.setdepartment(value);
        if( key.equals("x-ox-employeeType"))
            c.setEmployeeType(value);
        if( key.equals("x-ox-roomNumber"))
            c.setRoomNumber(value);
        if( key.equals("x-ox-numberOfEmployees"))
            c.setNumberOfEmployee(value);
        if( key.equals("x-ox-salesVolume""))
            c.setSalesVolume(value);
        if( key.equals("x-ox-taxId"))
            c.setTaxID(value);
        if( key.equals("x-ox-commercialRegister"))
            c.setCommercialRegister(value);
        if( key.equals("x-ox-branches"))
            c.setBranches(value);
        if( key.equals("x-ox-businessCategory"))
            c.setBusinessCategory(value);
        if( key.equals("x-ox-info""))
            c.setInfo(value);
        if( key.equals("x-ox-managerName"))
            c.setManagerName(value);
        if( key.equals("x-ox-assistantName""))
            c.setAssistantName(value);
        if( key.equals("x-ox-instantMessenger1"))
            c.setInstantMessenger1(value);
        if( key.equals("x-ox-instantMessenger2"))
            c.setInstantMessenger2(value);
        if( key.equals("x-ox-userfield01"))
            c.setUserField01(value);
        if( key.equals("x-ox-userfield02""))
            c.setUserField02(value);
        if( key.equals("x-ox-userfield03""))
            c.setUserField03(value);
        if( key.equals("x-ox-userfield04""))
            c.setUserField04(value);
        if( key.equals("x-ox-userfield05""))
            c.setUserField05(value);
        if( key.equals("x-ox-userfield06""))
            c.setUserField06(value);
        if( key.equals("x-ox-userfield07"))
            c.setUserField07(value);
        if( key.equals("x-ox-userfield08"))
            c.setUserField08(value);
        if( key.equals("x-ox-userfield09"))
            c.setUserField09(value);
        if( key.equals("x-ox-userfield10"))
            c.setUserField10(value);
        if( key.equals("x-ox-userfield11"))
            c.setUserField11(value);
        if( key.equals("x-ox-userfield12"))
            c.setUserField12(value);
        if( key.equals("x-ox-userfield13"))
            c.setUserField13(value);
        if( key.equals("x-ox-userfield14"))
            c.setUserField14(value);
        if( key.equals("x-ox-userfield15"))
            c.setUserField15(value);
        if( key.equals("x-ox-userfield16"))
            c.setUserField16(value);
        if( key.equals("x-ox-userfield17"))
            c.setUserField17(value);
        if( key.equals("x-ox-userfield18"))
            c.setUserField18(value);
        if( key.equals("x-ox-userfield19"))
            c.setUserField19(value);
        if( key.equals("x-ox-userfield20"))
            c.setUserField20(value);
        */
    }


    private void handleImage(HCard hcard, Contact c) {
        if(hcard.photos == null || hcard.photos.size() == 0) {
            return;
        }
        URI uri = hcard.photos.get(0);
        if("data".equalsIgnoreCase( uri.getScheme() ) ){
            String info = uri.getSchemeSpecificPart();
            String mimetype = info.split(";")[0];
            String data = info.substring(mimetype.length() + ";base64,".length());
            c.setImage1( Base64.decode(data) );
            c.setImageContentType(mimetype);
            return;
        }
        try {
            HTTPToolkit.loadImageFromURL(c, uri.toString());
        } catch (final OXException e) {
            // log error, but don't fail. Simply don't store an image
            final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(HCardToContactTransformer.class);
            LOG.warn("Couldn't load image.", e);
            // Add warning
            final Throwable cause = e.getCause();
            if (null == cause) {
                c.addWarning(SubscriptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage()));
            } else {
                c.addWarning(SubscriptionErrorMessage.UNEXPECTED_ERROR.create(cause, cause.getMessage()));
            }
        }
    }


    private void handleEMail(HCard hcard, Contact c) {
        List<Email> postponed = new LinkedList<Email>();
        for(Email email: hcard.emails){
            if(email.types.contains("work")){
                c.setEmail1(email.value);
            } else if(email.types.contains("home")) {
                c.setEmail2(email.value);
            } else if(email.types.contains("x-ox-other")) {
                c.setEmail3(email.value);
            } else {
                postponed.add(email);
            }
        }
        for(Email email : postponed) {
            for(int field = Contact.EMAIL1; field <= Contact.EMAIL3; field++) {
                if(!c.contains(field)){
                    c.set(field, email.value);
                    continue;
                }
            }
        }
    }


    private void handleTelephones(HCard hcard, Contact c) {
        List<Tel> postponed = new LinkedList<Tel>();
        List<Tel> postponedFax = new LinkedList<Tel>();

        for(Tel tel : hcard.tels){
            if(tel.types.contains("home") && tel.types.contains("fax")) {
                c.setFaxHome(tel.value);
            } else if(tel.types.contains("home")) {
                c.setTelephoneHome1(tel.value);
            } else if(tel.types.contains("x-ox-home2") || tel.types.contains("ox_home2")) {
                c.setTelephoneHome2(tel.value);
            } else if(tel.types.contains("work") && tel.types.contains("fax")) {
                c.setFaxBusiness(tel.value);
            } else if(tel.types.contains("work")) {
                c.setTelephoneBusiness1(tel.value);
            } else if(tel.types.contains("x-ox-business2") || tel.types.contains("ox_business2")) {
                c.setTelephoneBusiness2(tel.value);
            } else if( (tel.types.contains("x-ox-other") || tel.types.contains("ox_other")) && tel.types.contains("fax")  ){
                c.setFaxOther(tel.value);
            } else if(tel.types.contains("x-ox-other") || tel.types.contains("ox_other")){
                c.setTelephoneOther(tel.value);
            } else if(tel.types.contains("fax")){
                postponedFax.add(tel);
            } else if(tel.types.contains("x-ox-callback") || tel.types.contains("ox_callback")){
                c.setTelephoneCallback(tel.value);
            } else if(tel.types.contains("x-ox-company") || tel.types.contains("ox_company")){
                c.setTelephoneCompany(tel.value);
            } else if(tel.types.contains("x-ox-cellular2") || tel.types.contains("ox_cellular2")){
                c.setCellularTelephone2(tel.value);
            } else if(tel.types.contains("x-ox-pager") || tel.types.contains("ox_pager")){
                c.setTelephonePager(tel.value);
            } else if(tel.types.contains("x-ox-primary") || tel.types.contains("ox_primary")){
                c.setTelephonePrimary(tel.value);
            } else if(tel.types.contains("x-ox-radio") || tel.types.contains("ox_radio")){
                c.setTelephoneRadio(tel.value);
            } else if(tel.types.contains("x-ox-telex") || tel.types.contains("ox_telex")){
                c.setTelephoneTelex(tel.value);
            } else if(tel.types.contains("x-ox-ttytdd") || tel.types.contains("ox_ttytdd")){
                c.setTelephoneTTYTTD(tel.value);
            } else if(tel.types.contains("x-ox-ip") || tel.types.contains("ox_ip")){
                c.setTelephoneIP(tel.value);
            } else if(tel.types.contains("x-ox-assistant") || tel.types.contains("ox_assistant")){
                c.setTelephoneAssistant(tel.value);
            } else {
                postponed.add(tel);
            }
        }
        for(Tel tel : postponedFax) {
            for(int field: new int[]{Contact.FAX_BUSINESS, Contact.FAX_HOME, Contact.FAX_OTHER}) {
                if(!c.contains(field)){
                    c.set(field, tel.value);
                    continue;
                }
            }
        }

        for(Tel tel : postponed) {
            for(int field = Contact.TELEPHONE_BUSINESS1; field <= Contact.FAX_OTHER ; field++) {
                if(!c.contains(field)){
                    c.set(field, tel.value);
                    continue;
                }
            }
        }
    }


    private void handleName(HCard hcard, Contact c) {

        c.setGivenName(hcard.n.givenName);
        c.setSurName(hcard.n.familyName);
        if(exist(hcard.n.additionalNames)) {
            c.setMiddleName( Strings.join( hcard.n.additionalNames, " "));
        }
        if(exist(hcard.titles)) {
            c.setTitle( Strings.join( hcard.titles, " "));
        }
        if(exist(hcard.n.honorificPrefixes)) {
            if(c.containsTitle()) {
                c.setTitle(c.getTitle() + " " + Strings.join( hcard.n.honorificSuffixes, " "));
            } else {
                c.setTitle( Strings.join( hcard.n.honorificSuffixes, " "));
            }
        }
        if(exist(hcard.n.honorificSuffixes)) {
            c.setSuffix( Strings.join( hcard.n.honorificSuffixes, " "));
        }
        if(exist(hcard.nicknames)) {
            c.setDisplayName( Strings.join(hcard.nicknames, ","));
        }
    }



    private boolean exist(Collection coll) {
        return coll != null && coll.size() > 0;
    }


    private void handleCompany(HCard hcard, Contact c) {
        if(exist(hcard.orgs)) {
            c.setCompany(Strings.join(hcard.orgs, ","));
        }
        if(exist(hcard.roles)) {
            c.setPosition(Strings.join(hcard.roles, ","));
        }
    }


    private void handleAdresses(HCard hcard, Contact contact) {
        boolean setHome = false, setWork = false, setOther = false;
        List<Address> postponed = new LinkedList<Address>();

        for(Address adr: hcard.adrs){
            if(adr.types.contains("home")){
                addHomeAddress(contact, adr);
                setHome = true;
            } else if(adr.types.contains("work")){
                addWorkAddress(contact, adr);
                setWork = true;
            } else if(adr.types.contains("ox_other")){
                addOtherAddress(contact, adr);
                setOther = true;
            } else {
                postponed.add(adr);
            }
        }

        for(Address adr: postponed){
            if(! setHome){
                addHomeAddress(contact, adr);
                setHome = true;
            } else if(! setWork){
                addWorkAddress(contact, adr);
                setWork = true;
            } else if(! setOther){
                addOtherAddress(contact, adr);
                setOther = true;
            }
        }
    }


    private void addOtherAddress(Contact contact, Address adr) {
        contact.setStateOther(adr.region);
        contact.setCityOther(adr.locality);
        contact.setPostalCodeOther(adr.postalCode);
        contact.setCountryOther(adr.countryName);
        contact.setStreetOther(adr.streetAddress);
    }


    private void addWorkAddress(Contact contact, Address adr) {
        contact.setStateBusiness(adr.region);
        contact.setCityBusiness(adr.locality);
        contact.setPostalCodeBusiness(adr.postalCode);
        contact.setCountryBusiness(adr.countryName);
        contact.setStreetBusiness(adr.streetAddress);
    }


    private void addHomeAddress(Contact contact, Address adr) {
        contact.setStateHome(adr.region);
        contact.setCityHome(adr.locality);
        contact.setPostalCodeHome(adr.postalCode);
        contact.setCountryHome(adr.countryName);
        contact.setStreetHome(adr.streetAddress);
    }
}
