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

package com.openexchange.oauth.yahoo;

import java.util.Date;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import junit.framework.TestCase;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.YahooApi;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.model.Verifier;
import org.scribe.oauth.OAuthService;
import com.openexchange.groupware.container.Contact;
import com.openexchange.oauth.yahoo.internal.YahooRequestTuner;

/**
 * {@link YahooSimpleConnectionTest}
 *
 * This does the complete OAuth-Dance with Yahoo without needing anything (running server etc) but the scribe library included in directory "lib".
 * It is meant as a reference implementation and a quick way to find out if the service itself is working properly
 *
 * @author <a href="mailto:karsten.will@open-xchange.com">Karsten Will</a>
 */
public class YahooSimpleConnectionTest extends TestCase {

    private static String singleContact = "{\"contact\":{\"uri\":\"https://social.yahooapis.com/v1/user/ANZAPAEE55TMMWPLYXQCJO7BAM/contact/1\",\"created\":\"2009-09-14T15:49:21Z\",\"updated\":\"2009-09-14T15:50:52Z\",\"isConnection\":false,\"id\":1,\"fields\":[{\"uri\":\"https://social.yahooapis.com/v1/user/ANZAPAEE55TMMWPLYXQCJO7BAM/contact/1/email/2\",\"created\":\"2009-09-14T15:49:21Z\",\"updated\":\"2009-09-14T15:49:21Z\",\"id\":2,\"type\":\"email\",\"value\":\"christine@example.com\",\"editedBy\":\"OWNER\",\"flags\":[],\"categories\":[]},{\"uri\":\"https://social.yahooapis.com/v1/user/ANZAPAEE55TMMWPLYXQCJO7BAM/contact/1/birthday/3\",\"created\":\"2009-09-14T15:49:21Z\",\"updated\":\"2009-09-14T15:49:21Z\",\"id\":3,\"type\":\"birthday\"," + "\"value\":{\"day\":\"1\",\"month\":\"4\",\"year\":\"1980\"},\"editedBy\":\"OWNER\",\"flags\":[],\"categories\":[]},{\"uri\":\"https://social.yahooapis.com/v1/user/ANZAPAEE55TMMWPLYXQCJO7BAM/contact/1/notes/4\",\"created\":\"2009-09-14T15:49:21Z\",\"updated\":\"2009-09-14T15:49:21Z\",\"id\":4,\"type\":\"notes\",\"value\":\"My private note on Christine\",\"editedBy\":\"OWNER\",\"flags\":[],\"categories\":[]},{\"uri\":\"https://social.yahooapis.com/v1/user/ANZAPAEE55TMMWPLYXQCJO7BAM/contact/1/company/5\",\"created\":\"2009-09-14T15:49:21Z\",\"updated\":\"2009-09-14T15:49:21Z\",\"id\":5,\"type\":\"company\",\"value\":\"Christines L�dchen\",\"editedBy\":\"OWNER\",\"flags\":[],\"categories\":[]},{\"uri\":\"https://social.yahooapis.com" + "/v1/user/ANZAPAEE55TMMWPLYXQCJO7BAM/contact/1/otherid/6\",\"created\":\"2009-09-14T15:49:21Z\",\"updated\":\"2009-09-14T15:49:21Z\",\"id\":6,\"type\":\"otherid\",\"value\":\"christine.weissenbruenner\",\"editedBy\":\"OWNER\",\"flags\":[\"SKYPE\"],\"categories\":[]},{\"uri\":\"https://social.yahooapis.com/v1/user/ANZAPAEE55TMMWPLYXQCJO7BAM/contact/1/jobTitle/7\",\"created\":\"2009-09-14T15:49:21Z\",\"updated\":\"2009-09-14T15:49:21Z\",\"id\":7,\"type\":\"jobTitle\",\"value\":\"Gesch�ftsf�hrerin\",\"editedBy\":\"OWNER\",\"flags\":[],\"categories\":[]},{\"uri\":\"https://social.yahooapis.com/v1/user/ANZAPAEE55TMMWPLYXQCJO7BAM/contact/1/phone/10\",\"created\":\"2009-09-14T15:50:52Z\",\"updated\":\"2009-09-14T15:50:52Z\",\"id\":10," + "\"type\":\"phone\",\"value\":\"02171 123456\",\"editedBy\":\"OWNER\",\"flags\":[\"HOME\"],\"categories\":[]},{\"uri\":\"https://social.yahooapis.com/v1/user/ANZAPAEE55TMMWPLYXQCJO7BAM/contact/1/phone/11\",\"created\":\"2009-09-14T15:50:52Z\",\"updated\":\"2009-09-14T15:50:52Z\",\"id\":11,\"type\":\"phone\",\"value\":\"0171 456987\",\"editedBy\":\"OWNER\",\"flags\":[\"MOBILE\"],\"categories\":[]},{\"uri\":\"https://social.yahooapis.com/v1/user/ANZAPAEE55TMMWPLYXQCJO7BAM/contact/1/phone/12\",\"created\":\"2009-09-14T15:50:52Z\",\"updated\":\"2009-09-14T15:50:52Z\",\"id\":12,\"type\":\"phone\",\"value\":\"0221 987654\",\"editedBy\":\"OWNER\",\"flags\":[\"WORK\"],\"categories\":[]},{\"uri\":\"https://social.yahooapis.com/v1/user/" + "ANZAPAEE55TMMWPLYXQCJO7BAM/contact/1/name/1\",\"created\":\"2009-09-14T15:49:21Z\",\"updated\":\"2009-09-14T15:49:21Z\",\"id\":1,\"type\":\"name\",\"value\":{\"givenName\":\"Christine\",\"middleName\":\"\",\"familyName\":\"Wei�enbr�nner-Doppelname\",\"prefix\":\"\",\"suffix\":\"\",\"givenNameSound\":\"\",\"familyNameSound\":\"\"},\"editedBy\":\"OWNER\",\"flags\":[],\"categories\":[]},{\"uri\":\"https://social.yahooapis.com/v1/user/ANZAPAEE55TMMWPLYXQCJO7BAM/contact/1/address/8\",\"created\":\"2009-09-14T15:49:21Z\",\"updated\":\"2009-09-14T15:49:21Z\",\"id\":8,\"type\":\"address\",\"value\":{\"street\":\"An der Luisenburg 2a\",\"city\":\"Leverkusen\",\"stateOrProvince\":\"NRW\",\"postalCode\":\"51379\",\"country\":\"Germany\"," + "\"countryCode\":\"DE\"},\"editedBy\":\"OWNER\",\"flags\":[\"HOME\"],\"categories\":[]},{\"uri\":\"https://social.yahooapis.com/v1/user/ANZAPAEE55TMMWPLYXQCJO7BAM/contact/1/address/9\",\"created\":\"2009-09-14T15:49:21Z\",\"updated\":\"2009-09-14T15:49:21Z\",\"id\":9,\"type\":\"address\",\"value\":{\"street\":\"Bonner Str 207\",\"city\":\"K�ln\",\"stateOrProvince\":\"NRW\",\"postalCode\":\"90768\",\"country\":\"Germany\",\"countryCode\":\"DE\"},\"editedBy\":\"OWNER\",\"flags\":[\"WORK\"],\"categories\":[]}],\"categories\":[]}}";

    // This works (REST-API)
    private static String ALL_CONTACT_IDS_URL = "https://social.yahooapis.com/v1/user/GUID/contacts?format=json";

    private static String SINGLE_CONTACT_URL = "https://social.yahooapis.com/v1/user/GUID/contact/CONTACT_ID?format=json";

    // this does not (YQL)
    // "https://query.yahooapis.com/v1/user/GUID/yql?q=select%20*%20from%20social.contacts&format=json";

    public static void testYahooConnection() {

        // Keys are managed here: https://developer.apps.yahoo.com/projects (domain-specific)
        OAuthService service = new ServiceBuilder().provider(YahooApi.class).apiKey(
            "dj0yJmk9eDY3MW9VNXhqYTRWJmQ9WVdrOVJYWTFiRGhKTXpBbWNHbzlNelF6TURnMU5qWXkmcz1jb25zdW1lcnNlY3JldCZ4PTkx").apiSecret(
            "b94fbe3f52d364b4ae5a28228ac7b558fcfbe58c").callback("https://www.open-xchange.com").build();
        Scanner in = new Scanner(System.in);

        System.out.println("=== Yahoo's OAuth Workflow ===");
        System.out.println();

        // Obtain the Request Token
        System.out.println("Fetching the Request Token...");
        Token requestToken = service.getRequestToken();
        System.out.println("Got the Request Token!");
        System.out.println();

        System.out.println("Now go and authorize Scribe here:");
        System.out.println(service.getAuthorizationUrl(requestToken));
        System.out.println("And paste the verifier here");
        System.out.print(">>");
        Verifier verifier = new Verifier(in.nextLine());
        System.out.println();

        // Trade the Request Token and Verifier for the Access Token
        System.out.println("Trading the Request Token for an Access Token...");
        Token accessToken = service.getAccessToken(requestToken, verifier);
        System.out.println("Got the Access Token!");
        System.out.println("(if your curious it looks like this: " + accessToken + " )");
        System.out.println();

        // Get the GUID of the current user from yahoo. This is needed for later requests
        System.out.println("Now we're going to get the Users GUID");
        OAuthRequest request1 = new OAuthRequest(Verb.GET, "https://social.yahooapis.com/v1/me/guid?format=xml");
        service.signRequest(accessToken, request1);
        Response response1 = request1.send(YahooRequestTuner.getInstance());
        System.out.println("Lets see it ...");
        System.out.println();
        System.out.println(response1.getCode());
        System.out.println(response1.getBody());

        // Extract the Users ID from a response looking like this: <value>ANZAPAEE55TMMWPLYXQCJO7BAM<
        Pattern pattern = Pattern.compile("<value>([^<]*)<");
        Matcher matcher = pattern.matcher(response1.getBody());
        String guid = "";
        if (matcher.find()) {
            guid = matcher.group(1);
            System.out.println("Extracted GUID : " + guid);
        }

        // Now let's go and ask for a protected resource!
        System.out.println("Now we're going to access a protected resource (list of contact ids) ...");
        String resource = ALL_CONTACT_IDS_URL.replace("GUID", guid);
        System.out.println("This is its URL : " + resource);
        OAuthRequest request = new OAuthRequest(Verb.GET, resource);
        service.signRequest(accessToken, request);
        Response response = request.send(YahooRequestTuner.getInstance());
        System.out.println("Got it! Lets see what we found...");
        System.out.println();
        System.out.println(response.getCode());
        System.out.println(response.getBody());

        try {
            JSONObject allContactsWholeResponse = new JSONObject(response.getBody());
            if (allContactsWholeResponse.has("contacts")) {
                JSONObject contacts = (JSONObject) allContactsWholeResponse.get("contacts");
                if (contacts.has("contact")) {
                    JSONArray allContactsArray = (JSONArray) contacts.get("contact");
                    for (int i = 0; i < allContactsArray.length(); i++) {
                        JSONObject entry = allContactsArray.getJSONObject(i);
                        if (entry.has("id")) {
                            String contactId = entry.getString("id");
                            String singleContactUrl = SINGLE_CONTACT_URL.replace("GUID", guid).replace("CONTACT_ID", contactId);
                            System.out.println("***** Get a single contact with this URL : " + singleContactUrl);
                            OAuthRequest singleContactRequest = new OAuthRequest(Verb.GET, singleContactUrl);
                            service.signRequest(accessToken, singleContactRequest);
                            Response singleContactResponse = singleContactRequest.send(YahooRequestTuner.getInstance());
                            System.out.println(singleContactResponse.getCode());
                            System.out.println(singleContactResponse.getBody());

                        }
                    }
                }
            }
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void testParsingSingleContact() {
        Contact contact = parseSingleContact(singleContact);
        System.out.println("contact retrieved is : " + contact.getDisplayName());
        System.out.println("contacts first name : " + contact.getGivenName());
        System.out.println("contacts last name : " + contact.getSurName());
        System.out.println("contacts title : " + contact.getTitle());
        System.out.println("contacts position : " + contact.getPosition());
        System.out.println("contacts business email address : " + contact.getEmail1());
        System.out.println("contacts private email address : " + contact.getEmail2());
        System.out.println("contacts business mobile phone number : " + contact.getCellularTelephone1());
        System.out.println("contacts private mobile phone number : " + contact.getCellularTelephone2());
        System.out.println("contacts work phone number : " + contact.getTelephoneBusiness1());
        System.out.println("contacts home phone number : " + contact.getTelephoneHome1());
        System.out.println("contacts work fax number : " + contact.getFaxBusiness());
        System.out.println("contacts home fax number : " + contact.getFaxHome());
        System.out.println("contacts instant messenger : " + contact.getInstantMessenger1());
        System.out.println("contacts birthday : " + contact.getBirthday());
        System.out.println("contacts picture type : " + contact.getImageContentType());
        System.out.println("contacts street of work : " + contact.getStreetBusiness());
        System.out.println("contacts postal code of work : " + contact.getPostalCodeBusiness());
        System.out.println("contacts city of work : " + contact.getCityBusiness());
        System.out.println("contacts country of work : " + contact.getCountryBusiness());
        System.out.println("contacts street of private address : " + contact.getStreetHome());
        System.out.println("contacts postal code of private address : " + contact.getPostalCodeHome());
        System.out.println("contacts city of private address : " + contact.getCityHome());
        System.out.println("contacts country of private address : " + contact.getCountryHome());
        System.out.println("contacts company : " + contact.getCompany());
        System.out.println("contacts note : " + contact.getNote());
    }

    private static Contact parseSingleContact(String singleContact) {
        Contact oxContact = new Contact();
        // contact, fields -> danach type
        try {
            JSONObject all = new JSONObject(singleContact);
            if (all.has("contact")) {
                JSONObject contact = all.getJSONObject("contact");
                if (contact.has("fields")) {
                    JSONArray fields = contact.getJSONArray("fields");
                    for (int i = 0; i < fields.length(); i++) {
                        JSONObject field = fields.getJSONObject(i);
                        if (field.has("type")) {
                            String type = field.getString("type");

                            if (type.equals("name")) {
                                if (field.has("value")) {
                                    JSONObject value = field.getJSONObject("value");
                                    if (value.has("givenName")) {
                                        oxContact.setGivenName(value.getString("givenName"));
                                    }

                                    if (value.has("familyName")) {
                                        oxContact.setSurName(value.getString("familyName"));
                                    }
                                    if (value.has("prefix") && !value.get("prefix").equals("")) {
                                        oxContact.setTitle(value.getString("prefix"));
                                    }
                                    if (value.has("suffix") && !value.get("suffix").equals("")) {
                                        oxContact.setSuffix(value.getString("suffix"));
                                    }
                                    if (value.has("middleName") && !value.get("middleName").equals("")) {
                                        oxContact.setMiddleName(value.getString("middleName"));
                                    }
                                }
                            }

                            else if (type.equals("email")) {
                                if (field.has("value")) {
                                    oxContact.setEmail1(field.getString("value"));
                                }
                            }

                            else if (type.equals("phone")) {
                                if (field.has("flags") && field.has("value")) {
                                    String kind = field.getString("flags");
                                    if (kind.equals("[\"WORK\"]")) {
                                        oxContact.setTelephoneBusiness1(field.getString("value"));
                                    } else if (kind.equals("[\"HOME\"]")) {
                                        oxContact.setTelephoneHome1(field.getString("value"));
                                    } else if (kind.equals("[\"MOBILE\"]")) {
                                        oxContact.setCellularTelephone1((field.getString("value")));
                                    }
                                }
                            }

                            else if (type.equals("company")) {
                                if (field.has("value")) {
                                    oxContact.setCompany(field.getString("value"));
                                }
                            }

                            else if (type.equals("jobTitle")) {
                                if (field.has("value")) {
                                    oxContact.setPosition(field.getString("value"));
                                }
                            }

                            else if (type.equals("notes")) {
                                if (field.has("value")) {
                                    oxContact.setNote(field.getString("value"));
                                }
                            }

                            else if (type.equals("birthday")) {
                                int year = 0;
                                int month = 0;
                                int date = 0;
                                if (field.has("value")) {
                                    JSONObject value = field.getJSONObject("value");
                                    if (value.has("day")) {
                                        date = Integer.parseInt(value.getString("day"));
                                    }
                                    if (value.has("day")) {
                                        date = Integer.parseInt(value.getString("day"));
                                    }
                                    if (value.has("month")) {
                                        month = Integer.parseInt(value.getString("month")) -1;
                                    }
                                    if (value.has("year")) {
                                        year = Integer.parseInt(value.getString("year")) - 1900;
                                    }
                                    if (date != 0 && month != 0) {
                                        oxContact.setBirthday(new Date(year, month, date));
                                    }
                                }
                            }

                            else if (type.equals("otherid")){
                                if (field.has("value") && field.has("flags")){
                                    String kind = field.getString("flags");
                                    Pattern pattern = Pattern.compile("\\[\"([^\"]*)\"\\]");
                                    Matcher matcher = pattern.matcher(kind);
                                    if (matcher.find()){
                                        String service = matcher.group(1);
                                        oxContact.setInstantMessenger1(field.getString("value") + " ("+service+")");
                                    }
                                }
                            }

                            else if (type.equals("address")) {
                                if (field.has("flags")) {
                                    String kind = field.getString("flags");
                                    JSONObject address = field.getJSONObject("value");
                                    if (kind.equals("[\"WORK\"]")) {
                                        if (address.has("street")) {
                                            oxContact.setStreetBusiness(address.getString("street"));
                                        }
                                        if (address.has("postalCode")) {
                                            oxContact.setPostalCodeBusiness(address.getString("postalCode"));
                                        }
                                        if (address.has("stateOrProvince")) {
                                            oxContact.setStateBusiness(address.getString("stateOrProvince"));
                                        }
                                        if (address.has("country")) {
                                            oxContact.setCountryBusiness(address.getString("country"));
                                        }
                                    } else if (kind.equals("[\"HOME\"]")) {
                                        if (address.has("street")) {
                                            oxContact.setStreetHome(address.getString("street"));
                                        }
                                        if (address.has("postalCode")) {
                                            oxContact.setPostalCodeHome(address.getString("postalCode"));
                                        }
                                        if (address.has("stateOrProvince")) {
                                            oxContact.setStateHome(address.getString("stateOrProvince"));
                                        }
                                        if (address.has("country")) {
                                            oxContact.setCountryHome(address.getString("country"));
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return oxContact;
    }
}
