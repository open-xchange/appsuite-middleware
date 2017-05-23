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

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import com.openexchange.test.concurrent.ParallelSuite;

/**
 * {@link MailTestSuite}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@RunWith(ParallelSuite.class)
@Suite.SuiteClasses({
    AllTest.class,
    AttachmentTest.class,
    ClearTest.class,
    CopyMailTest.class,
    CountMailTest.class,
    ForwardMailTest.class,
    GetTest.class,
    ListTest.class,
    MailSearchTest.class,
    MoveMailTest.class,
    NewMailTest.class,
    MultipleAttachmentTest.class,
    ReplyAllTest.class,
    ReplyTest.class,
    SearchTest.class,
    SendTest.class,
    Send2Test.class,
    ThreadSortTest.class,
    UpdateMailTest.class,
    ViewTest.class,
    AllAliasTest.class,
    ListAliasTest.class,
    GetStructureTest.class,
    Base64Test.class,
    AllSeenMailTest.class,
    com.openexchange.ajax.mail.MultipleGetTest.class,

    /* AlwaysTest.class, */

    Bug12409Test.class,
    Bug14234Test.class,
    Bug15608Test.class,
    Bug15777Test.class,
    Bug15901Test.class,
    Bug16087Test.class,
    Bug16141Test.class,
    Bug29865Test.class,
    Bug19696Test.class,
    Bug30703Test.class,
    Bug30903Test.class,
    Bug31855Test.class,
    Bug32355Test.class,
//    Bug27708Test.class,
    Bug28913Test.class,
    Bug29437Test.class,
    Bug34254Test.class,
    Bug36333Test.class,
    Bug37247Test.class,

    TestEstimateLength.class,

    MaxMailSizeTest.class,
    MSISDNAddressTest.class,
    ExamineTest.class,

})
public final class MailTestSuite  {

}
