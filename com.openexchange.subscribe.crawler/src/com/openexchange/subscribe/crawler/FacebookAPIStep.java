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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.EnumSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.logging.Log;
import com.openexchange.log.LogFactory;
import com.facebook.api.FacebookException;
import com.facebook.api.FacebookJaxbRestClient;
import com.facebook.api.ProfileField;
import com.facebook.api.schema.FriendsGetResponse;
import com.facebook.api.schema.Location;
import com.facebook.api.schema.User;
import com.facebook.api.schema.UsersGetInfoResponse;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.subscribe.crawler.internal.AbstractStep;
import com.openexchange.subscribe.crawler.internal.LoginStep;
import com.openexchange.tools.versit.converter.ConverterException;
import com.openexchange.tools.versit.converter.OXContainerConverter;

public class FacebookAPIStep extends AbstractStep<Contact[], Object> implements LoginStep {

    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(FacebookAPIStep.class));

    String url, username, password, actionOfLoginForm, nameOfUserField, nameOfPasswordField, linkAvailableAfterLogin, apiKey, secret, birthdayPattern;

    public FacebookAPIStep() {

    }

    public FacebookAPIStep(final String description, final String url, final String username, final String password, final String actionOfLoginForm, final String nameOfUserField, final String nameOfPasswordField, final String linkAvailableAfterLogin) {
        this.description = description;
        this.url = url;
        this.username = username;
        this.password = password;
        this.actionOfLoginForm = actionOfLoginForm;
        this.nameOfUserField = nameOfUserField;
        this.nameOfPasswordField = nameOfPasswordField;
        this.linkAvailableAfterLogin = linkAvailableAfterLogin;
    }

    @Override
    public void execute(final WebClient webClient) throws OXException {
        output = new Contact[0];
        final ArrayList<Contact> contactObjects = new ArrayList<Contact>();

        try {
            // Create the client instance
            final FacebookJaxbRestClient client = new FacebookJaxbRestClient(apiKey, secret);

            // first, we need to get an auth-token to log in with
            final String token = client.auth_createToken();

            // Build the authentication URL for the user to fill out
            final String url = "http://www.facebook.com/login.php?api_key=" + apiKey + "&v=1.0" + "&auth_token=" + token;
            // open browser for user to log in
            final LoginPageByFormActionStep step = new LoginPageByFormActionStep(
                description,
                url,
                username,
                password,
                actionOfLoginForm,
                nameOfUserField,
                nameOfPasswordField,
                linkAvailableAfterLogin,
                1,
                "http://www.facebook.com");
            step.execute(webClient);
            // grant access to the application (needed only the first time someone uses the subscription)
            HtmlPage page = step.getOutput();
            PageByNamedHtmlElementStep step2 = new PageByNamedHtmlElementStep("", 0, "grant_clicked");
            step2.setInput(page);
            step2.execute(webClient);
            webClient.closeAllWindows();

            // fetch session key
            final String session = client.auth_getSession(token);

            // keep track of the logged in user id
            final Long userId = client.users_getLoggedInUser();

            // Get friends list
            client.friends_get();
            final FriendsGetResponse response = (FriendsGetResponse) client.getResponsePOJO();
            final List<Long> friends = response.getUid();

            // Go fetch the information for the user list of user ids
            client.users_getInfo(friends, EnumSet.of(
                ProfileField.NAME,
                ProfileField.FIRST_NAME,
                ProfileField.LAST_NAME,
                ProfileField.BIRTHDAY,
                ProfileField.HOMETOWN_LOCATION,
                ProfileField.PIC));
            final UsersGetInfoResponse userResponse = (UsersGetInfoResponse) client.getResponsePOJO();
            final List<User> users = userResponse.getUser();

            // insert the information for each user into an ox contact
            for (final User user : users) {
                final Contact contact = new Contact();
                final Location location = user.getHometownLocation().getValue();
                contact.setDisplayName(user.getName());
                contact.setGivenName(user.getFirstName());
                contact.setSurName(user.getLastName());
                if (user.getBirthday() != null) {
                    final Calendar calendar = Calendar.getInstance();
                    final String birthdayString = user.getBirthday().getValue();
                    final Pattern pattern = Pattern.compile(birthdayPattern);
                    if (birthdayString != null) {
                        final Matcher matcher = pattern.matcher(birthdayString);
                        if (matcher.matches()) {
                            // only set the contacts birthday if at least day and month are available
                            if (matcher.groupCount() >= 2) {
                                int month = 0;
                                int day = 0;
                                // set the year to the current year in case it is not available
                                int year = calendar.get(Calendar.YEAR);
                                // set the day
                                day = Integer.valueOf(matcher.group(1));
                                // set the month
                                if (matcher.group(2).equals("January")) {
                                    month = Calendar.JANUARY;
                                } else if (matcher.group(2).equals("February")) {
                                    month = Calendar.FEBRUARY;
                                } else if (matcher.group(2).equals("March")) {
                                    month = Calendar.MARCH;
                                } else if (matcher.group(2).equals("April")) {
                                    month = Calendar.APRIL;
                                } else if (matcher.group(2).equals("May")) {
                                    month = Calendar.MAY;
                                } else if (matcher.group(2).equals("June")) {
                                    month = Calendar.JUNE;
                                } else if (matcher.group(2).equals("July")) {
                                    month = Calendar.JULY;
                                } else if (matcher.group(2).equals("August")) {
                                    month = Calendar.AUGUST;
                                } else if (matcher.group(2).equals("September")) {
                                    month = Calendar.SEPTEMBER;
                                } else if (matcher.group(2).equals("October")) {
                                    month = Calendar.OCTOBER;
                                } else if (matcher.group(2).equals("November")) {
                                    month = Calendar.NOVEMBER;
                                } else if (matcher.group(2).equals("December")) {
                                    month = Calendar.DECEMBER;
                                }

                                // set the year
                                if (matcher.groupCount() == 4 && !matcher.group(4).equals("")) {
                                    year = Integer.valueOf(matcher.group(4));
                                }

                                calendar.set(year, month, day);

                                contact.setBirthday(calendar.getTime());
                            }
                        }
                    }
                }
                if (location != null) {
                    if (location.getStreet() != null && !location.getStreet().equals("null")) {
                        contact.setStreetHome(location.getStreet());
                    }
                    if (location.getZip() != null && location.getZip() != 0) {
                        contact.setPostalCodeHome(Integer.toString(location.getZip()));
                    }
                    if (location.getCity() != null && !location.getCity().equals("null")) {
                        contact.setCityHome(location.getCity());
                    }
                    if (location.getState() != null && !location.getState().equals("null")) {
                        contact.setStateHome(location.getState());
                    }
                    if (location.getCountry() != null && !location.getCountry().equals("null")) {
                        contact.setCountryHome(location.getCountry());
                    }
                }
                // add the image from a url to the contact
                if (user.getPic() != null) {
                    try {
                        OXContainerConverter.loadImageFromURL(contact, user.getPic().getValue());
                    } catch (final ConverterException e) {
                        LOG.error("No valid picture could be found at this URL : " + user.getPic().getValue());
                    }
                }
                contactObjects.add(contact);
            }
        } catch (final FacebookException e) {
            LOG.error(e.getMessage(), e);
        } catch (final IOException e) {
            LOG.error(e.getMessage(), e);
        }  catch (final ClassCastException e) {
            LOG.error(e.getMessage(), e);
        }
        executedSuccessfully = true;
        output = new Contact[contactObjects.size()];
        for (int i = 0; i < output.length && i < contactObjects.size(); i++) {
            output[i] = contactObjects.get(i);
        }
    }

    @Override
    public void setInput(final Object input) {

    }

    public String getUrl() {
        return url;
    }

    public void setUrl(final String url) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    @Override
    public void setUsername(final String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public void setPassword(final String password) {
        this.password = password;
    }

    public String getActionOfLoginForm() {
        return actionOfLoginForm;
    }

    public void setActionOfLoginForm(final String actionOfLoginForm) {
        this.actionOfLoginForm = actionOfLoginForm;
    }

    public String getNameOfUserField() {
        return nameOfUserField;
    }

    public void setNameOfUserField(final String nameOfUserField) {
        this.nameOfUserField = nameOfUserField;
    }

    public String getNameOfPasswordField() {
        return nameOfPasswordField;
    }

    public void setNameOfPasswordField(final String nameOfPasswordField) {
        this.nameOfPasswordField = nameOfPasswordField;
    }

    public String getLinkAvailableAfterLogin() {
        return linkAvailableAfterLogin;
    }

    public void setLinkAvailableAfterLogin(final String linkAvailableAfterLogin) {
        this.linkAvailableAfterLogin = linkAvailableAfterLogin;
    }

    @Override
    public String getBaseUrl() {
        return url;
    }


    public String getApiKey() {
        return apiKey;
    }


    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }


    public String getSecret() {
        return secret;
    }


    public void setSecret(String secret) {
        this.secret = secret;
    }


    public String getBirthdayPattern() {
        return birthdayPattern;
    }


    public void setBirthdayPattern(String birthdayPattern) {
        this.birthdayPattern = birthdayPattern;
    }




}
