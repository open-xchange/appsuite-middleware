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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.groupware.container.Contact;
import com.openexchange.tools.ImageTypeDetector;
import com.openexchange.tools.stream.UnsynchronizedByteArrayOutputStream;

/**
 * @author <a href="mailto:karsten.will@open-xchange.com">Karsten Will</a>
 */
public class MapToContactObjectTransformer implements MapToObjectTransformer{
	
    private static final DateFormat DATE = new SimpleDateFormat("yyyy-MM-dd");
    
    private static final Log LOG = LogFactory.getLog(MapToContactObjectTransformer.class);
    
	public List<Contact> transform (List<Map<String, String>> inlist){
		ArrayList<Contact> outlist = new ArrayList<Contact>();
		
		for (Map<String,String> map : inlist){
		Contact contact = new Contact();	
			if (map.containsKey("ox_displayName")){
				contact.setDisplayName((String)map.get("ox_displayName"));
			}  
			if (map.containsKey("ox_firstName")){
				contact.setGivenName((String)map.get("ox_firstName"));
			}  
			if (map.containsKey("ox_lastName")){
				contact.setSurName((String)map.get("ox_lastName"));
			}  
			if (map.containsKey("ox_secondName")){
				contact.setMiddleName((String)map.get("ox_secondName"));
			}  
			if (map.containsKey("ox_suffix")){
				contact.setSuffix((String)map.get("ox_suffix"));
			}  
			if (map.containsKey("ox_title")){
				contact.setTitle((String)map.get("ox_title"));
			}  
			if (map.containsKey("ox_streetPrivate")){
				contact.setStreetHome((String)map.get("ox_streetPrivate"));
			}  
			if (map.containsKey("ox_postalCodePrivate")){
				contact.setPostalCodeHome((String)map.get("ox_postalCodePrivate"));
			}  
			if (map.containsKey("ox_cityPrivate")){
				contact.setCityHome((String)map.get("ox_cityPrivate"));
			}  
			if (map.containsKey("ox_statePrivate")){
				contact.setStateHome((String)map.get("ox_statePrivate"));
			}  
			if (map.containsKey("ox_countryPrivate")){
				contact.setCountryHome((String)map.get("ox_countryPrivate"));
			} 
			if (map.containsKey("ox_streetBusiness")){
				contact.setStreetBusiness((String)map.get("ox_streetBusiness"));
			} 
			if (map.containsKey("ox_postalCodeBusiness")){
				contact.setPostalCodeBusiness((String)map.get("ox_postalCodeBusiness"));
			} 
			if (map.containsKey("ox_cityBusiness")){
				contact.setCityBusiness((String)map.get("ox_cityBusiness"));
			} 
			if (map.containsKey("ox_stateBusiness")){
				contact.setStateBusiness((String)map.get("ox_stateBusiness"));
			} 
			if (map.containsKey("ox_countryBusiness")){
				contact.setCountryBusiness((String)map.get("ox_countryBusiness"));
			} 
			if (map.containsKey("ox_streetOther")){
				contact.setStreetOther((String)map.get("ox_streetOther"));
			} 
			if (map.containsKey("ox_postalCodeOther")){
				contact.setPostalCodeOther((String)map.get("ox_postalCodeOther"));
			} 
			if (map.containsKey("ox_cityOther")){
				contact.setCityOther((String)map.get("ox_cityOther"));
			} 
			if (map.containsKey("ox_stateOther")){
				contact.setStateOther((String)map.get("ox_stateOther"));
			} 
			if (map.containsKey("ox_countryOther")){
				contact.setCountryOther((String)map.get("ox_countryOther"));
			} 
			if (map.containsKey("ox_birthday")){
				try {
					contact.setBirthday(DATE.parse((String)map.get("ox_birthday")));
				} catch (ParseException e) {
					e.printStackTrace();
				}
			} 
			if (map.containsKey("ox_maritalStatus")){
				contact.setMaritalStatus((String)map.get("ox_maritalStatus"));
			}  
			if (map.containsKey("ox_numberOfChildren")){
				contact.setNumberOfChildren((String)map.get("ox_numberOfChildren"));
			} 
			if (map.containsKey("ox_profession")){
				contact.setProfession((String)map.get("ox_profession"));
			}  
			if (map.containsKey("ox_nickname")){
				contact.setNickname((String)map.get("ox_nickname"));
			}  
			if (map.containsKey("ox_spouseName")){
				contact.setSpouseName((String)map.get("ox_spouseName"));
			}  
			if (map.containsKey("ox_anniversary")){
				try {
					contact.setAnniversary(DATE.parse((String)map.get("ox_anniversary")));
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}  
			if (map.containsKey("ox_comment")){
				contact.setNote((String)map.get("ox_comment"));
			}    
			if (map.containsKey("ox_department")){
				contact.setDepartment((String)map.get("ox_department"));
			}  
			if (map.containsKey("ox_position")){
				contact.setPosition((String)map.get("ox_position"));
			}  
			if (map.containsKey("ox_jobTitle")){
				contact.setEmployeeType((String)map.get("ox_jobTitle"));
			}  
			if (map.containsKey("ox_roomNumber")){
				contact.setRoomNumber((String)map.get("ox_roomNumber"));
			}  
			if (map.containsKey("ox_numberOfEmployees")){
				contact.setNumberOfEmployee((String)map.get("ox_numberOfEmployees"));
			}  
			if (map.containsKey("ox_salesVolume")){
				contact.setSalesVolume((String)map.get("ox_salesVolume"));
			}  
			if (map.containsKey("ox_taxID")){
				contact.setTaxID((String)map.get("ox_taxID"));
			}  
			if (map.containsKey("ox_commercialRegister")){
				contact.setCommercialRegister((String)map.get("ox_commercialRegister"));
			}  
			if (map.containsKey("ox_branches")){
				contact.setBranches((String)map.get("ox_branches"));
			}  
			if (map.containsKey("ox_businessCategory")){
				contact.setBusinessCategory((String)map.get("ox_businessCategory"));
			}  
			if (map.containsKey("ox_infoCompany")){
				contact.setInfo((String)map.get("ox_infoCompany"));
			}  
			if (map.containsKey("ox_managerName")){
				contact.setManagerName((String)map.get("ox_managerName"));
			}  
			if (map.containsKey("ox_assistantName")){
				contact.setAssistantName((String)map.get("ox_assistantName"));
			}  
			if (map.containsKey("ox_phonePrivate1")){
				contact.setTelephoneHome1((String)map.get("ox_phonePrivate1"));
			}  
			if (map.containsKey("ox_phonePrivate2")){
				contact.setTelephoneHome2((String)map.get("ox_phonePrivate2"));
			}  
			if (map.containsKey("ox_phoneBusiness1")){
				contact.setTelephoneBusiness1((String)map.get("ox_phoneBusiness1"));
			}  
			if (map.containsKey("ox_phoneBusiness2")){
				contact.setTelephoneBusiness2((String)map.get("ox_phoneBusiness2"));
			}  
			if (map.containsKey("ox_phoneOther")){
				contact.setTelephoneOther((String)map.get("ox_phoneOther"));
			}  
			if (map.containsKey("ox_faxBusiness")){
				contact.setFaxBusiness((String)map.get("ox_faxBusiness"));
			}  
			if (map.containsKey("ox_phoneCallback")){
				contact.setTelephoneCallback((String)map.get("ox_phoneCallback"));
			}  
			if (map.containsKey("ox_phoneCar")){
				contact.setTelephoneCar((String)map.get("ox_phoneCar"));
			}  
			if (map.containsKey("ox_phoneCompany")){
				contact.setTelephoneCompany((String)map.get("ox_phoneCompany"));
			}  
			if (map.containsKey("ox_faxPrivate")){
				contact.setFaxHome((String)map.get("ox_faxPrivate"));
			}  
			if (map.containsKey("ox_phoneMobile1")){
				contact.setCellularTelephone1((String)map.get("ox_phoneMobile1"));
			}  
			if (map.containsKey("ox_phoneMobile2")){
				contact.setCellularTelephone2((String)map.get("ox_phoneMobile2"));
			}  
			if (map.containsKey("ox_faxOther")){
				contact.setFaxOther((String)map.get("ox_faxOther"));
			}  
			if (map.containsKey("ox_email1")){
				contact.setEmail1((String)map.get("ox_email1"));
			}  
			if (map.containsKey("ox_email2")){
				contact.setEmail2((String)map.get("ox_email2"));
			}  
			if (map.containsKey("ox_email3")){
				contact.setEmail3((String)map.get("ox_email3"));
			}  
			if (map.containsKey("ox_URL")){
				contact.setURL((String)map.get("ox_URL"));
			}  
			if (map.containsKey("ox_phoneISDN")){
				contact.setTelephoneISDN((String)map.get("ox_phoneISDN"));
			}  
			if (map.containsKey("ox_pager")){
				contact.setTelephonePager((String)map.get("ox_pager"));
			}  
			if (map.containsKey("ox_phonePrimary")){
				contact.setTelephonePrimary((String)map.get("ox_phonePrimary"));
			}  
			if (map.containsKey("ox_phoneRadio")){
				contact.setTelephoneRadio((String)map.get("ox_phoneRadio"));
			}  
			if (map.containsKey("ox_telephone_telex")){
				contact.setTelephoneTelex((String)map.get("ox_telephone_telex"));
			}  
			if (map.containsKey("ox_telex")){
				contact.setTelephoneTelex((String)map.get("ox_telex"));
			}  
			if (map.containsKey("ox_phoneTTYTDD")){
                contact.setTelephoneTTYTTD((String)map.get("ox_phoneTTYTDD"));
            }  
            if (map.containsKey("ox_instantMessenger1")){
				contact.setInstantMessenger1((String)map.get("ox_instantMessenger1"));
			}  
			if (map.containsKey("ox_instantMessenger2")){
				contact.setInstantMessenger2((String)map.get("ox_instantMessenger2"));
			}  
			if (map.containsKey("ox_phoneIP")){
				contact.setTelephoneIP((String)map.get("ox_phoneIP"));
			}  
			if (map.containsKey("ox_phoneAssistant")){
				contact.setTelephoneAssistant((String)map.get("ox_phoneAssistant"));
			}  
			if (map.containsKey("ox_company")){
				contact.setCompany((String)map.get("ox_company"));
			}  
			if (map.containsKey("ox_image")){
			    try {
                    loadImageFromURL(contact, new URL(map.get("ox_image")));
                } catch (MalformedURLException e) {
                    LOG.warn(e.getMessage(), e);
                    // Discard image. This is all best effort, nothing more, maybe next time.
                }
			}  
			if (map.containsKey("ox_userfield01")){
				contact.setUserField01((String)map.get("ox_userfield01"));
			}  
			if (map.containsKey("ox_userfield02")){
				contact.setUserField02((String)map.get("ox_userfield02"));
			}  
			if (map.containsKey("ox_userfield03")){
				contact.setUserField03((String)map.get("ox_userfield03"));
			}  
			if (map.containsKey("ox_userfield04")){
				contact.setUserField04((String)map.get("ox_userfield04"));
			}  
			if (map.containsKey("ox_userfield05")){
				contact.setUserField05((String)map.get("ox_userfield05"));
			}  
			if (map.containsKey("ox_userfield06")){
				contact.setUserField06((String)map.get("ox_userfield06"));
			}  
			if (map.containsKey("ox_userfield07")){
				contact.setUserField07((String)map.get("ox_userfield07"));
			}  
			if (map.containsKey("ox_userfield08")){
				contact.setUserField08((String)map.get("ox_userfield08"));
			}  
			if (map.containsKey("ox_userfield09")){
				contact.setUserField09((String)map.get("ox_userfield09"));
			}  
			if (map.containsKey("ox_userfield10")){
				contact.setUserField10((String)map.get("ox_userfield10"));
			}  
			if (map.containsKey("ox_userfield11")){
				contact.setUserField11((String)map.get("ox_userfield11"));
			}  
			if (map.containsKey("ox_userfield12")){
				contact.setUserField12((String)map.get("ox_userfield12"));
			}  
			if (map.containsKey("ox_userfield13")){
				contact.setUserField13((String)map.get("ox_userfield13"));
			}  
			if (map.containsKey("ox_userfield14")){
				contact.setUserField14((String)map.get("ox_userfield14"));
			}  
			if (map.containsKey("ox_userfield15")){
				contact.setUserField15((String)map.get("ox_userfield15"));
			}  
			if (map.containsKey("ox_userfield16")){
				contact.setUserField16((String)map.get("ox_userfield16"));
			}  
			if (map.containsKey("ox_userfield17")){
				contact.setUserField17((String)map.get("ox_userfield17"));
			}  
			if (map.containsKey("ox_userfield18")){
				contact.setUserField18((String)map.get("ox_userfield18"));
			}  
			if (map.containsKey("ox_userfield19")){
				contact.setUserField19((String)map.get("ox_userfield19"));
			}  
			if (map.containsKey("ox_userfield20")){
				contact.setUserField20((String)map.get("ox_userfield20"));
			}  
			if (map.containsKey("ox_tags")){
                contact.setCategories((String)map.get("ox_tags"));
            }
			outlist.add(contact);
		}
		
		return outlist;
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
            final BufferedInputStream in = new BufferedInputStream(urlCon.getInputStream());
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
                    LOG.error(e.getMessage(), e);
                }
            }
        } catch (final java.net.SocketTimeoutException e) {
            final String uri = url.toString();
            LOG.warn(new StringBuilder(64 + uri.length()).append("Either connecting to or reading from an image's URI timed out: ").append(
                uri).toString(), e);
        } catch (final IOException e) {
            final String uri = url.toString();
            LOG.warn(new StringBuilder(32 + uri.length()).append("Image  URI could not be loaded: ").append(uri).toString(), e);
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
