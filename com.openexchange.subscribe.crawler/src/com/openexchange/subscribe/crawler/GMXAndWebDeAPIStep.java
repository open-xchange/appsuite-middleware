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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.subscribe.crawler;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.logging.Log;
import com.openexchange.log.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.HttpMethod;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequestSettings;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.subscribe.crawler.internal.AbstractStep;
import com.openexchange.subscribe.crawler.internal.LoginStep;

/**
 * {@link GMXAndWebDeAPIStep}
 *
 * @author <a href="mailto:karsten.will@open-xchange.com">Karsten Will</a>
 */
public class GMXAndWebDeAPIStep extends AbstractStep<Contact[], Object> implements LoginStep {

    private String url;

    private String username, password;

    private List<NameValuePair> parameters;

    private static final Log LOG = com.openexchange.log.Log.loggerFor(GMXAndWebDeAPIStep.class);

    public GMXAndWebDeAPIStep() {
        super();
        parameters = new ArrayList<NameValuePair>();
    }

    /**
     * Initializes a new {@link GMXAndWebDeAPIStep}.
     *
     * @param url
     * @param parameters
     */
    public GMXAndWebDeAPIStep(String url, List<NameValuePair> parameters) {
        super();
        this.url = url;
        this.parameters = parameters;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.subscribe.crawler.internal.AbstractStep#execute(com.gargoylesoftware.htmlunit.WebClient)
     */
    @Override
    public void execute(WebClient webClient) throws OXException {
        List<Contact> contactObjects = new ArrayList<Contact>();
        try {
            String urlString = url;

            // adding the parameters to the url
            if (!parameters.isEmpty()) {
                urlString += "?";
                boolean isFirst = true;
                for (NameValuePair nvp : parameters) {
                    if (!isFirst) {
                        urlString += "&";
                    } else {
                        isFirst = false;
                    }
                    urlString += URLEncoder.encode(nvp.getName(), "utf-8") + "=" + URLEncoder.encode(nvp.getValue(), "utf-8");
                }
            }

            if (debuggingEnabled) {
                LOG.error("complete URL : " + urlString);
            }

            WebRequestSettings requestSettings = new WebRequestSettings(new URL(urlString), HttpMethod.POST);

            // adding the parameters to the request as well (just to be sure)
            // requestSettings.setRequestParameters(new ArrayList());
            // for (NameValuePair nvp : parameters) {
            // requestSettings.getRequestParameters().add(nvp);
            // }

            HtmlPage page = webClient.getPage(requestSettings);

            if (debuggingEnabled) {
                LOG.error("Status Code : " + page.getWebResponse().getStatusCode());
                LOG.error("URL : " + page.getWebResponse().getUrl());
                LOG.error("webResponse : " + page.getWebResponse().getContentAsString());
            }
        } catch (FailingHttpStatusCodeException e) {
            // catch the 401 that appears after logging in (for whatever reason ...)
            if (e.getStatusCode() == 401) {
                // LOG.error(e.getResponse().getUrl());
                Pattern pattern = Pattern.compile("([^?]*)\\?session=(.*)");
                Matcher matcher = pattern.matcher(e.getResponse().getUrl().toString());
                if (matcher.find() && matcher.groupCount() == 2) {
                    String newUrlBase = matcher.group(1);
                    String functionCall = "json/PersonService/getAll";
                    String session = matcher.group(2);
                    // System.out.println("Session : " + session);
                    String toEncode = username + ":sid=" + session;
                    // System.out.println(toEncode);
                    Base64 encoder = new Base64();
                    byte[] bytes = encoder.encode(toEncode.getBytes());
                    String base64Encoded = new String(bytes);
                    // remove the whitespaces otherwise there is an error
                    base64Encoded = base64Encoded.replaceAll("\\W", "");
                    // System.out.println(base64Encoded);
                    String apiURL = newUrlBase + functionCall;
                    try {
                        // System.out.println(base64Encoded);
                        WebRequestSettings requestSettingsForAPICall = new WebRequestSettings(new URL(apiURL), HttpMethod.POST);
                        HashMap<String, String> map = new HashMap<String, String>();
                        map.put("Authorization", "Basic " + base64Encoded);
                        map.put("Content-Type", "application/json");
                        requestSettingsForAPICall.setAdditionalHeaders(map);
                        requestSettingsForAPICall.setRequestBody("{\"search\":null}");
                        Page page = webClient.getPage(requestSettingsForAPICall);
                        // System.out.println(page.getWebResponse().getContentAsString());
                        String allContactsPage = page.getWebResponse().getContentAsString("UTF-8");
                        contactObjects = parseJSONIntoContacts(allContactsPage);
                        executedSuccessfully = true;
                    } catch (MalformedURLException e1) {
                        LOG.error(e1);
                    } catch (FailingHttpStatusCodeException e1) {
                        LOG.error(e1);
                    } catch (IOException e1) {
                        LOG.error(e1);
                    }

                } else {
                    LOG.error(e);
                }
            } else {
                LOG.error(e);
            }
        } catch (IOException e) {
            LOG.error(e);
        }
        output = contactObjects.toArray(new Contact[contactObjects.size()]);

    }

    /**
     * @param allContactsPage
     * @return
     */
    private List<Contact> parseJSONIntoContacts(String allContactsPage) {
        System.out.println(allContactsPage);
        List<Contact> contacts = new ArrayList<Contact>();
        try {
            JSONObject allContentJSON = new JSONObject(allContactsPage);
            JSONArray allContactsJSON = (JSONArray) allContentJSON.get("response");
            for (int i = 0; i < allContactsJSON.length(); i++) {
                try {
                    JSONObject contactJSON = allContactsJSON.getJSONObject(i);
                    Contact contact = new Contact();
                    if (contactJSON.has("name")) {
                        contact.setSurName(contactJSON.getString("name"));
                    }
                    if (contactJSON.has("firstName")) {
                        contact.setGivenName(contactJSON.getString("firstName"));
                    }
                    if (contactJSON.has("comment")) {
                        contact.setNote(contactJSON.getString("comment"));
                    }
                    if (contactJSON.has("position")) {
                        contact.setPosition(contactJSON.getString("position"));
                    }
                    if (contactJSON.has("shortName")) {
                        contact.setNickname(contactJSON.getString("shortName"));
                    }
                    if (contactJSON.has("title")) {
                        contact.setTitle(contactJSON.getString("title"));
                    }
                    if (contactJSON.has("company")) {
                        contact.setCompany(contactJSON.getString("company"));
                    }

                    if (contactJSON.has("birthday")) {
                        JSONObject birthdayJSON = contactJSON.getJSONObject("birthday");
                        String day = "";
                        String month = "";
                        String year = "";
                        if (birthdayJSON.has("day")) {
                            day = birthdayJSON.getString("day");
                        }
                        if (birthdayJSON.has("month")) {
                            month = birthdayJSON.getString("month");
                        }
                        if (birthdayJSON.has("day")) {
                            year = birthdayJSON.getString("year");
                        }
                        Calendar calendar = Calendar.getInstance();
                        if (!day.equals("") && !month.equals("") && !year.equals("")) {
                            calendar.set(Integer.parseInt(year), Integer.parseInt(month) - 1, Integer.parseInt(day));
                            contact.setBirthday(calendar.getTime());
                        } else if (!day.equals("") && !month.equals("")) {
                            calendar.set(1970, Integer.parseInt(month) - 1, Integer.parseInt(day));
                            contact.setBirthday(calendar.getTime());
                        }

                        if (contactJSON.has("location")) {
                            JSONArray addressesJSON = contactJSON.getJSONArray("location");
                            for (int a = 0; a < addressesJSON.length(); a++) {
                                JSONObject addressJSON = addressesJSON.getJSONObject(a);
                                if (addressJSON.has("classifier")) {
                                    JSONObject classifier = addressJSON.getJSONObject("classifier");
                                    if (classifier.has("name")) {
                                        String type = classifier.getString("name");
                                        if (type.equals("BUSINESS")) {
                                            if (addressJSON.has("address")) {
                                                contact.setStreetBusiness(addressJSON.getString("address"));
                                            }
                                            if (addressJSON.has("postcode")) {
                                                contact.setPostalCodeBusiness(addressJSON.getString("postcode"));
                                            }
                                            if (addressJSON.has("town")) {
                                                contact.setCityBusiness(addressJSON.getString("town"));
                                            }
                                            if (addressJSON.has("country")) {
                                                contact.setCountryBusiness(addressJSON.getString("country"));
                                            }
                                        } else if (type.equals("PRIVATE")) {
                                            if (addressJSON.has("address")) {
                                                contact.setStreetHome(addressJSON.getString("address"));
                                            }
                                            if (addressJSON.has("postcode")) {
                                                contact.setPostalCodeHome(addressJSON.getString("postcode"));
                                            }
                                            if (addressJSON.has("town")) {
                                                contact.setCityHome(addressJSON.getString("town"));
                                            }
                                            if (addressJSON.has("country")) {
                                                contact.setCountryHome(addressJSON.getString("country"));
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        if (contactJSON.has("fax")) {
                            JSONArray faxesJSON = contactJSON.getJSONArray("fax");
                            for (int a = 0; a < faxesJSON.length(); a++) {
                                JSONObject faxJSON = faxesJSON.getJSONObject(a);
                                if (faxJSON.has("classifier")) {
                                    JSONObject classifier = faxJSON.getJSONObject("classifier");
                                    if (classifier.has("name")) {
                                        String type = classifier.getString("name");
                                        if (type.equals("BUSINESS")) {
                                            if (faxJSON.has("number")) {
                                                contact.setFaxBusiness(faxJSON.getString("number"));
                                            }
                                        } else if (type.equals("PRIVATE")) {
                                            if (faxJSON.has("number")) {
                                                contact.setFaxHome(faxJSON.getString("number"));
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        if (contactJSON.has("messaging")) {
                            JSONArray messagingJSON = contactJSON.getJSONArray("messaging");
                            for (int a = 0; a < messagingJSON.length(); a++) {
                                JSONObject instantMessengerJSON = messagingJSON.getJSONObject(a);
                                if (instantMessengerJSON.has("classifier")) {
                                    JSONObject classifier = instantMessengerJSON.getJSONObject("classifier");
                                    if (classifier.has("name")) {
                                        String type = classifier.getString("name");
                                        if (type.equals("BUSINESS")) {
                                            if (instantMessengerJSON.has("messenger") && instantMessengerJSON.has("messengerAccount")) {
                                                contact.setInstantMessenger1(instantMessengerJSON.getString("messengerAccount") + " (" + instantMessengerJSON.getString("messenger") + ")");
                                            }
                                        } else if (type.equals("PRIVATE")) {
                                            if (instantMessengerJSON.has("messenger") && instantMessengerJSON.has("messengerAccount")) {
                                                contact.setInstantMessenger2(instantMessengerJSON.getString("messengerAccount") + " (" + instantMessengerJSON.getString("messenger") + ")");
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        if (contactJSON.has("mobilePhone")) {
                            JSONArray mobilePhonesJSON = contactJSON.getJSONArray("mobilePhone");
                            for (int a = 0; a < mobilePhonesJSON.length(); a++) {
                                JSONObject mobilePhoneJSON = mobilePhonesJSON.getJSONObject(a);
                                if (mobilePhoneJSON.has("classifier")) {
                                    JSONObject classifier = mobilePhoneJSON.getJSONObject("classifier");
                                    if (classifier.has("name")) {
                                        String type = classifier.getString("name");
                                        if (type.equals("BUSINESS")) {
                                            if (mobilePhoneJSON.has("number")) {
                                                contact.setCellularTelephone1(mobilePhoneJSON.getString("number"));
                                            }
                                        } else if (type.equals("PRIVATE")) {
                                            if (mobilePhoneJSON.has("number")) {
                                                contact.setCellularTelephone2(mobilePhoneJSON.getString("number"));
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        if (contactJSON.has("phone")) {
                            JSONArray phonesJSON = contactJSON.getJSONArray("phone");
                            for (int a = 0; a < phonesJSON.length(); a++) {
                                JSONObject phoneJSON = phonesJSON.getJSONObject(a);
                                if (phoneJSON.has("classifier")) {
                                    JSONObject classifier = phoneJSON.getJSONObject("classifier");
                                    if (classifier.has("name")) {
                                        String type = classifier.getString("name");
                                        if (type.equals("BUSINESS")) {
                                            if (phoneJSON.has("number")) {
                                                contact.setTelephoneBusiness1(phoneJSON.getString("number"));
                                            }
                                        } else if (type.equals("PRIVATE")) {
                                            if (phoneJSON.has("number")) {
                                                contact.setTelephoneHome1(phoneJSON.getString("number"));
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        if (contactJSON.has("email")) {
                            JSONArray emailsJSON = contactJSON.getJSONArray("email");
                            for (int a = 0; a < emailsJSON.length(); a++) {
                                JSONObject emailJSON = emailsJSON.getJSONObject(a);
                                if (emailJSON.has("classifier")) {
                                    JSONObject classifier = emailJSON.getJSONObject("classifier");
                                    if (classifier.has("name")) {
                                        String type = classifier.getString("name");
                                        if (type.equals("BUSINESS")) {
                                            if (emailJSON.has("address")) {
                                                contact.setEmail1(emailJSON.getString("address"));
                                            }
                                        } else if (type.equals("PRIVATE")) {
                                            if (emailJSON.has("address")) {
                                                contact.setEmail2(emailJSON.getString("address"));
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        if (contactJSON.has("url")) {
                            JSONArray urlsJSON = contactJSON.getJSONArray("phone");
                            for (int a = 0; a < urlsJSON.length(); a++) {
                                JSONObject urlJSON = urlsJSON.getJSONObject(a);
                                if (urlJSON.has("classifier")) {
                                    JSONObject classifier = urlJSON.getJSONObject("classifier");
                                    if (classifier.has("name")) {
                                        String type = classifier.getString("name");
                                        if (type.equals("BUSINESS")) {
                                            if (urlJSON.has("uri")) {
                                                contact.setURL(urlJSON.getString("uri"));
                                            }
                                        }
                                    }
                                }
                            }

                        }
                    }

                    contacts.add(contact);
                    // An error in parsing one contact should not bring them all down
                } catch (JSONException e) {
                    LOG.error(e);
                }
            }
        } catch (JSONException e) {
            LOG.error(e);
        }
        return contacts;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public List<NameValuePair> getParameters() {
        return parameters;
    }

    public void setParameters(List<NameValuePair> parameters) {
        this.parameters = parameters;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.subscribe.crawler.internal.LoginStep#setUsername(java.lang.String)
     */
    @Override
    public void setUsername(String username) {
        parameters.add(new NameValuePair("username", username));
        this.username = username;

    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.subscribe.crawler.internal.LoginStep#setPassword(java.lang.String)
     */
    @Override
    public void setPassword(String password) {
        parameters.add(new NameValuePair("password", password));
        this.password = password;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.subscribe.crawler.internal.LoginStep#getBaseUrl()
     */
    @Override
    public String getBaseUrl() {
        // TODO Auto-generated method stub
        return null;
    }

}
