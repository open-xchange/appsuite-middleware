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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.openexchange.onboarding.registry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import org.json.JSONObject;
import org.junit.Test;
import com.openexchange.onboarding.DefaultEntity;
import com.openexchange.onboarding.DefaultOnboardingConfiguration;
import com.openexchange.onboarding.Entity;
import com.openexchange.onboarding.OnboardingConfiguration;
import com.openexchange.onboarding.Platform;

/**
 * {@link ConfigurationTree}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public class ConfigurationTreeTest {

    @Test
    public void testConfigTree() throws Exception {
        List<OnboardingConfiguration> configs = new ArrayList<>();

        {
            DefaultOnboardingConfiguration configuration = new DefaultOnboardingConfiguration();
            configuration.setId("MailApp-SMS");
            configuration.setEnabled(true);
            configuration.setDisplayName("Send an SMS to configure your MailApp");
            configuration.setPath(Arrays.<Entity> asList(new DefaultEntity("iOS", "iOS"), new DefaultEntity("iPad", "iPad"), new DefaultEntity("MailApp", "MailApp")));
            configuration.setPlatform(Platform.APPLE);
            configs.add(configuration);
        }

        {
            DefaultOnboardingConfiguration configuration = new DefaultOnboardingConfiguration();
            configuration.setId("EAS-Profile");
            configuration.setEnabled(true);
            configuration.setDisplayName("Downloads a profile to configure your EAS account");
            configuration.setPath(Arrays.<Entity> asList(new DefaultEntity("iOS", "iOS"), new DefaultEntity("iPad", "iPad"), new DefaultEntity("EAS", "EAS")));
            configuration.setPlatform(Platform.APPLE);
            configs.add(configuration);
        }

        {
            DefaultOnboardingConfiguration configuration = new DefaultOnboardingConfiguration();
            configuration.setId("EAS-Profile");
            configuration.setEnabled(true);
            configuration.setDisplayName("Downloads a profile to configure your EAS account");
            configuration.setPath(Arrays.<Entity> asList(new DefaultEntity("iOS", "iOS"), new DefaultEntity("iPhone", "iPhone"), new DefaultEntity("EAS", "EAS")));
            configuration.setPlatform(Platform.APPLE);
            configs.add(configuration);
        }

        {
            DefaultOnboardingConfiguration configuration = new DefaultOnboardingConfiguration();
            configuration.setId("IMAP-Profile");
            configuration.setEnabled(true);
            configuration.setDisplayName("Downloads a profile to configure your IMAP account");
            configuration.setPath(Arrays.<Entity> asList(new DefaultEntity("OSX", "OSX")));
            configuration.setPlatform(Platform.APPLE);
            configs.add(configuration);
        }

        {
            DefaultOnboardingConfiguration configuration = new DefaultOnboardingConfiguration();
            configuration.setId("MailApp-SMS");
            configuration.setEnabled(true);
            configuration.setDisplayName("Send an SMS to configure your MailApp");
            configuration.setPath(Arrays.<Entity> asList(new DefaultEntity("Windows8-10", "Windows8-10"), new DefaultEntity("EmClient", "EmClient")));
            configuration.setPlatform(Platform.WINDOWS);
            configs.add(configuration);
        }

        {
            DefaultOnboardingConfiguration configuration = new DefaultOnboardingConfiguration();
            configuration.setId("Manual");
            configuration.setEnabled(true);
            configuration.setDisplayName("Manually do something");
            configuration.setPath(Arrays.<Entity> asList());
            configuration.setPlatform(Platform.WINDOWS);
            configs.add(configuration);
        }

        ConfigurationTree tree = new ConfigurationTree(configs, null, Locale.US);

        JSONObject jTree = tree.toJsonObject();
        //System.out.println(jTree.toString(2));

        assertEquals("Unexpected size", 2, jTree.length());

        {
            JSONObject jApple = jTree.optJSONObject("apple");
            assertNotNull("Missing \"apple\" node", jApple);
        }
    }

}
