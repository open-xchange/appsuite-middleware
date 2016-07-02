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

package com.openexchange.ajax.mail.categories;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONException;
import org.junit.Assume;
import com.openexchange.ajax.capabilities.actions.AllRequest;
import com.openexchange.ajax.capabilities.actions.AllResponse;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXClient.User;
import com.openexchange.ajax.framework.UserValues;
import com.openexchange.ajax.mail.AbstractMailTest;
import com.openexchange.exception.OXException;
import com.openexchange.mail.categories.MailCategoriesConstants;

/**
 * {@link AbstractMailCategoriesTest}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.2
 */
public abstract class AbstractMailCategoriesTest extends AbstractMailTest {


    protected static final String CAT_GENERAL = "general";
    protected static final String CAT_1 = "social";
    protected static final String CAT_1_FLAG = "$social";
    protected static final String CAT_2 = "promotion";
    protected static final String CAT_2_FLAG = "$promotion";

    protected UserValues values;

    protected String EML;

    /**
     * Initializes a new {@link AbstractMailCategoriesTest}.
     *
     * @param name
     * @throws JSONException
     * @throws IOException
     * @throws OXException
     */
    public AbstractMailCategoriesTest(String name) throws OXException, IOException, JSONException {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        AJAXClient configClient = new AJAXClient(User.User1);
        setUpConfiguration(configClient, true);
        super.setUp();
        AllResponse response = getClient().execute(new AllRequest());
        Assume.assumeTrue("User does not have the mail_categories capability. Probably the mailserver does not support imap4flags.", response.getCapabilities().contains("mail_categories"));
        values = getClient().getValues();
        clearFolder(values.getInboxFolder()); // always start with an empty inbox
        EML = "Date: Mon, 19 Nov 2012 21:36:51 +0100 (CET)\n" +
            "From: " + getSendAddress() + "\n" +
            "To: " + getSendAddress() + "\n" +
            "Message-ID: <1508703313.17483.1353357411049>\n" +
            "Subject: Test mail\n" +
            "MIME-Version: 1.0\n" +
            "Content-Type: multipart/alternative; \n" +
            "    boundary=\"----=_Part_17482_1388684087.1353357411002\"\n" +
            "\n" +
            "------=_Part_17482_1388684087.1353357411002\n" +
            "MIME-Version: 1.0\n" +
            "Content-Type: text/plain; charset=UTF-8\n" +
            "Content-Transfer-Encoding: 7bit\n" +
            "\n" +
            "Test\n" +
            "------=_Part_17482_1388684087.1353357411002\n" +
            "MIME-Version: 1.0\n" +
            "Content-Type: text/html; charset=UTF-8\n" +
            "Content-Transfer-Encoding: 7bit\n" +
            "\n" +
            "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\"><html xmlns=\"http://www.w3.org/1999/xhtml\">" +
            " <head>\n" +
            "    <meta content=\"text/html; charset=UTF-8\" http-equiv=\"Content-Type\"/>\n" +
            " </head><body style=\"font-family: verdana,geneva; font-size: 10pt; \">\n" +
            " \n" +
            "  <div>\n" +
            "   Test\n" +
            "  </div>\n" +
            " \n" +
            "</body></html>\n" +
            "------=_Part_17482_1388684087.1353357411002--\n";
    }

    @Override
    protected void tearDown() throws Exception {
        clearFolder(values.getSentFolder());
        clearFolder(values.getInboxFolder());
        clearFolder(values.getDraftsFolder());
        super.tearDown();
    }

    @Override
    protected Map<String, String> getNeededConfigurations() {
        Map<String, String> configs = new HashMap<>();
        configs.put("com.openexchange.mail.categories", Boolean.TRUE.toString());
        configs.put(MailCategoriesConstants.MAIL_CATEGORIES_SWITCH, Boolean.TRUE.toString());
        configs.put("com.openexchange.mail.categories.general.name.fallback", "General");
        configs.put(MailCategoriesConstants.MAIL_CATEGORIES_IDENTIFIERS, "promotion, social");
        configs.put(MailCategoriesConstants.MAIL_USER_CATEGORIES_IDENTIFIERS, "uc1");
        configs.put("com.openexchange.mail.categories.promotion.flag", "$promotion");
        configs.put("com.openexchange.mail.categories.promotion.force", Boolean.FALSE.toString());
        configs.put("com.openexchange.mail.categories.promotion.active", Boolean.TRUE.toString());
        configs.put("com.openexchange.mail.categories.promotion.fallback", "Offers");
        configs.put("com.openexchange.mail.categories.social.flag", "$social");
        configs.put("com.openexchange.mail.categories.social.force", Boolean.TRUE.toString());
        configs.put("com.openexchange.mail.categories.social.active", Boolean.FALSE.toString());
        configs.put("com.openexchange.mail.categories.social.fallback", "Social");
        configs.put("com.openexchange.mail.categories.uc1.flag", "$uc1");
        configs.put("com.openexchange.mail.categories.uc1.active", Boolean.TRUE.toString());
        configs.put("com.openexchange.mail.categories.uc1.fallback", "Family");
        return configs;
    }

    @Override
    protected String getScope() {
        return "user";
    }
}
