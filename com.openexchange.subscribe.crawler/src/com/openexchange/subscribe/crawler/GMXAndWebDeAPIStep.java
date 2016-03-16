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

package com.openexchange.subscribe.crawler;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
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
import com.openexchange.subscribe.SubscriptionErrorMessage;
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

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(GMXAndWebDeAPIStep.class);

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
    public GMXAndWebDeAPIStep(final String url, final List<NameValuePair> parameters) {
        super();
        this.url = url;
        this.parameters = parameters;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.subscribe.crawler.internal.AbstractStep#execute(com.gargoylesoftware.htmlunit.WebClient)
     */
    @Override
    public void execute(final WebClient webClient) throws OXException {
        List<Contact> contactObjects = new ArrayList<Contact>();
        try {
            String urlString = url;
            String parameterString = "";

            // summing up the parameters into one string
            if (!parameters.isEmpty()) {
                //urlString += "?";
                boolean isFirst = true;
                for (NameValuePair nvp : parameters) {
                    if (!isFirst) {
                        parameterString += "&";
                    } else {
                        isFirst = false;
                    }
                    parameterString += URLEncoder.encode(nvp.getName(), "utf-8") + "=" + URLEncoder.encode(nvp.getValue(), "utf-8");
                }
            }

            if (debuggingEnabled) {
                LOG.error("DEBUG: complete URL : {}", urlString);
            }

            WebRequestSettings requestSettings = new WebRequestSettings(new URL(urlString), HttpMethod.POST);

            HashMap<String, String> initialMap = new HashMap<String, String>();
            initialMap.put("Content-Type", "application/x-www-form-urlencoded;charset=\"UTF-8\"");
            requestSettings.setAdditionalHeaders(initialMap);

            // Adding the parameters to the Body as well
            requestSettings.setRequestBody(parameterString);
            webClient.setRedirectEnabled(false);
            HtmlPage page = webClient.getPage(requestSettings);

            if (debuggingEnabled) {
                LOG.error("DEBUG: Status Code : {}", page.getWebResponse().getStatusCode());
                LOG.error("DEBUG: URL : {}", page.getWebResponse().getUrl());
                LOG.error("DEBUG: webResponse : {}", page.getWebResponse().getContentAsString());
            }
        } catch (FailingHttpStatusCodeException e) {
            // catch the 302 that appears after logging in
            if (e.getStatusCode() == 302 || e.getMessage().trim().startsWith("302")) {
                // LOG.error(e.getResponse().getUrl());
                Pattern pattern = Pattern.compile("([^?]*)\\?session=(.*)");
                //Matcher matcher = pattern.matcher(e.getResponse().getUrl().toString());
                String location = "";
                List<NameValuePair> responseHeaders = e.getResponse().getResponseHeaders();
                for (NameValuePair nvp : responseHeaders){
                    if (nvp.getName().equals("Location")){
                        location = nvp.getValue();
                    }
                }
                if (null != location && location.endsWith("error_bad_password")) {
                    throw SubscriptionErrorMessage.INVALID_LOGIN.create();
                }
                Matcher matcher = pattern.matcher(location);
                if (matcher.find() && matcher.groupCount() == 2) {
                    String newUrlBase = matcher.group(1);
                    String functionCall = "json/PersonService/getAll";
                    String session = matcher.group(2);

                    String toEncode = username + ":sid=" + session;
                    Base64 encoder = new Base64();
                    String base64Encoded = "";
                    try {
                        base64Encoded = new String(encoder.encode(toEncode.getBytes("UTF-8")));
                    } catch (UnsupportedEncodingException e2) {
                        LOG.error(e2.toString());
                    }

                    // remove the whitespaces otherwise there is an error
                    base64Encoded = base64Encoded.replaceAll("\\s", "");

                    String apiURL = newUrlBase + functionCall;
                    try {
                        WebRequestSettings requestSettingsForAPICall = new WebRequestSettings(new URL(apiURL), HttpMethod.POST);
                        HashMap<String, String> map = new HashMap<String, String>();
                        map.put("Authorization", "Basic " + base64Encoded);
                        map.put("Content-Type", "application/json;charset=\"UTF-8\"");
                        requestSettingsForAPICall.setAdditionalHeaders(map);
                        requestSettingsForAPICall.setRequestBody("{\"search\":null}");
                        Page page = webClient.getPage(requestSettingsForAPICall);
                        String allContactsPage = page.getWebResponse().getContentAsString("UTF-8");
                        contactObjects = parseJSONIntoContacts(allContactsPage);
                        executedSuccessfully = true;
                    } catch (MalformedURLException e1) {
                        LOG.error("", e1);
                    } catch (FailingHttpStatusCodeException e1) {
                        LOG.error("", e1);
                    } catch (IOException e1) {
                        LOG.error("", e1);
                    }

                } else {
                    LOG.error("", e);
                }
            } else {
                LOG.error("", e);
            }
        } catch (final IOException e) {
            LOG.error("", e);
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
                	final JSONObject contactJSON = allContactsJSON.getJSONObject(i);
                    final Contact contact = new Contact();
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

                    //setting the displayname
                    if (contactJSON.has("name") && contactJSON.has("firstName")){
                    	contact.setDisplayName(contact.getGivenName() + " " + contact.getSurName());
                    } else if (contactJSON.has("name")){
                    	contact.setDisplayName(contact.getSurName());
                    } else if (contactJSON.has("firstName")){
                    	contact.setDisplayName(contact.getGivenName());
                    } else {
                    	contact.setDisplayName("");
                    }

                    if (contactJSON.hasAndNotNull("birthday")/* && JSONObject.NULL != contactJSON.get("birthday")*/) {
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
                        final Calendar calendar = Calendar.getInstance();
                        if (!day.equals("") && !month.equals("") && !year.equals("")) {
                            calendar.set(Integer.parseInt(year), Integer.parseInt(month) - 1, Integer.parseInt(day));
                            contact.setBirthday(calendar.getTime());
                        } else if (!day.equals("") && !month.equals("")) {
                            calendar.set(1970, Integer.parseInt(month) - 1, Integer.parseInt(day));
                            contact.setBirthday(calendar.getTime());
                        }
                    }

                    if (contactJSON.has("location")) {
                        final JSONArray addressesJSON = contactJSON.getJSONArray("location");
                        for (int a = 0; a < addressesJSON.length(); a++) {
                            final JSONObject addressJSON = addressesJSON.getJSONObject(a);
                            if (addressJSON.has("classifier")) {
                                final JSONObject classifier = addressJSON.getJSONObject("classifier");
                                if (classifier.has("name")) {
                                    final String type = classifier.getString("name");
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
                        final JSONArray faxesJSON = contactJSON.getJSONArray("fax");
                        for (int a = 0; a < faxesJSON.length(); a++) {
                            final JSONObject faxJSON = faxesJSON.getJSONObject(a);
                            if (faxJSON.has("classifier")) {
                                final JSONObject classifier = faxJSON.getJSONObject("classifier");
                                if (classifier.has("name")) {
                                    final String type = classifier.getString("name");
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
                        final JSONArray messagingJSON = contactJSON.getJSONArray("messaging");
                        for (int a = 0; a < messagingJSON.length(); a++) {
                            final JSONObject instantMessengerJSON = messagingJSON.getJSONObject(a);
                            if (instantMessengerJSON.has("classifier")) {
                                final JSONObject classifier = instantMessengerJSON.getJSONObject("classifier");
                                if (classifier.has("name")) {
                                    final String type = classifier.getString("name");
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
                        final JSONArray mobilePhonesJSON = contactJSON.getJSONArray("mobilePhone");
                        for (int a = 0; a < mobilePhonesJSON.length(); a++) {
                            final JSONObject mobilePhoneJSON = mobilePhonesJSON.getJSONObject(a);
                            if (mobilePhoneJSON.has("classifier")) {
                                final JSONObject classifier = mobilePhoneJSON.getJSONObject("classifier");
                                if (classifier.has("name")) {
                                    final String type = classifier.getString("name");
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
                        final JSONArray phonesJSON = contactJSON.getJSONArray("phone");
                        for (int a = 0; a < phonesJSON.length(); a++) {
                            final JSONObject phoneJSON = phonesJSON.getJSONObject(a);
                            if (phoneJSON.has("classifier")) {
                                final JSONObject classifier = phoneJSON.getJSONObject("classifier");
                                if (classifier.has("name")) {
                                    final String type = classifier.getString("name");
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
                        final JSONArray emailsJSON = contactJSON.getJSONArray("email");
                        for (int a = 0; a < emailsJSON.length(); a++) {
                            final JSONObject emailJSON = emailsJSON.getJSONObject(a);
                            if (emailJSON.has("classifier")) {
                                final JSONObject classifier = emailJSON.getJSONObject("classifier");
                                if (classifier.has("name")) {
                                    final String type = classifier.getString("name");
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
                        final JSONArray urlsJSON = contactJSON.getJSONArray("phone");
                        for (int a = 0; a < urlsJSON.length(); a++) {
                            final JSONObject urlJSON = urlsJSON.getJSONObject(a);
                            if (urlJSON.has("classifier")) {
                                final JSONObject classifier = urlJSON.getJSONObject("classifier");
                                if (classifier.has("name")) {
                                    final String type = classifier.getString("name");
                                    if (type.equals("BUSINESS")) {
                                        if (urlJSON.has("uri")) {
                                            contact.setURL(urlJSON.getString("uri"));
                                        }
                                    }
                                }
                            }
                        }

                    }


                    contacts.add(contact);
                    // An error in parsing one contact should not bring them all down
                } catch (final JSONException e) {
                    LOG.error(e.toString());
                }
            }
        } catch (final JSONException e) {
            LOG.error(e.toString());
        }
        return contacts;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(final String url) {
        this.url = url;
    }

    public List<NameValuePair> getParameters() {
        return parameters;
    }

    public void setParameters(final List<NameValuePair> parameters) {
        this.parameters = parameters;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.subscribe.crawler.internal.LoginStep#setUsername(java.lang.String)
     */
    @Override
    public void setUsername(final String username) {
        parameters.add(new NameValuePair("username", username));
        this.username = username;

    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.subscribe.crawler.internal.LoginStep#setPassword(java.lang.String)
     */
    @Override
    public void setPassword(final String password) {
        parameters.add(new NameValuePair("password", password));
        this.password = password;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.subscribe.crawler.internal.LoginStep#getBaseUrl()
     */
    @Override
    public String getBaseUrl() {
        // Nothing to do
        return null;
    }

}
