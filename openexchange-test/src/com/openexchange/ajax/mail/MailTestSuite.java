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

package com.openexchange.ajax.mail;

import com.openexchange.ajax.mail.categories.MailCategoriesTestSuite;
import junit.framework.JUnit4TestAdapter;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * {@link MailTestSuite}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MailTestSuite extends TestSuite {

    private MailTestSuite() {
        super();
    }

    public static Test suite() {
        final TestSuite mailSuite = new TestSuite("com.openexchange.ajax.mail.MailTestSuite");
        mailSuite.addTestSuite(AllTest.class);
        mailSuite.addTestSuite(AttachmentTest.class);
        mailSuite.addTestSuite(ClearTest.class);
        mailSuite.addTestSuite(CopyMailTest.class);
        mailSuite.addTestSuite(CountMailTest.class);
        mailSuite.addTestSuite(ForwardMailTest.class);
        mailSuite.addTestSuite(GetTest.class);
        mailSuite.addTestSuite(ListTest.class);
        mailSuite.addTestSuite(MailSearchTest.class);
        mailSuite.addTestSuite(MoveMailTest.class);
        mailSuite.addTestSuite(NewMailTest.class);
        mailSuite.addTestSuite(MultipleAttachmentTest.class);
        mailSuite.addTestSuite(ReplyAllTest.class);
        mailSuite.addTestSuite(ReplyTest.class);
        mailSuite.addTestSuite(SearchTest.class);
        mailSuite.addTestSuite(SendTest.class);
        mailSuite.addTestSuite(Send2Test.class);
        mailSuite.addTestSuite(ThreadSortTest.class);
        mailSuite.addTestSuite(UpdateMailTest.class);
        mailSuite.addTestSuite(ViewTest.class);
        mailSuite.addTestSuite(AllAliasTest.class);
        mailSuite.addTestSuite(ListAliasTest.class);
        mailSuite.addTestSuite(GetStructureTest.class);
        mailSuite.addTestSuite(Base64Test.class);
        mailSuite.addTestSuite(AllSeenMailTest.class);
        mailSuite.addTestSuite(com.openexchange.ajax.mail.MultipleGetTest.class);

        /*mailSuite.addTestSuite(AlwaysTest.class);*/

        mailSuite.addTestSuite(Bug12409Test.class);
        mailSuite.addTestSuite(Bug14234Test.class);
        mailSuite.addTestSuite(Bug15608Test.class);
        mailSuite.addTestSuite(Bug15777Test.class);
        mailSuite.addTestSuite(Bug15901Test.class);
        mailSuite.addTestSuite(Bug16087Test.class);
        mailSuite.addTestSuite(Bug16141Test.class);
        mailSuite.addTestSuite(Bug29865Test.class);
        mailSuite.addTestSuite(Bug19696Test.class);
        mailSuite.addTestSuite(Bug30703Test.class);
        mailSuite.addTestSuite(Bug30903Test.class);
        mailSuite.addTestSuite(Bug31855Test.class);
        mailSuite.addTestSuite(Bug32355Test.class);
        mailSuite.addTest(new JUnit4TestAdapter(Bug27708Test.class));
        mailSuite.addTest(new JUnit4TestAdapter(Bug28913Test.class));
        mailSuite.addTestSuite(Bug29437Test.class);
        mailSuite.addTestSuite(Bug34254Test.class);
        mailSuite.addTestSuite(Bug36333Test.class);
        mailSuite.addTestSuite(Bug37247Test.class);

        mailSuite.addTestSuite(MaxMailSizeTest.class);
        mailSuite.addTestSuite(MSISDNAddressTest.class);

        mailSuite.addTest(MailCategoriesTestSuite.suite());
        return mailSuite;
    }
}
