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

package com.openexchange.html.internal;

import org.junit.Assert;
import org.junit.Test;


/**
 * {@link SaneScriptTagsTest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class SaneScriptTagsTest {

    /**
     * Initializes a new {@link SaneScriptTagsTest}.
     */
    public SaneScriptTagsTest() {
        super();
    }

    @Test
    public void testSaneIt1() {
        String str = "<a href=\"https://zeke.com/i/redirect?url=https%3A%2F%2Fsabbi.zeke.com%3Fcn%3DZW1haWxf" +
            "Y29uZmlybQ%253E%253D&amp;t=1&amp;cn=ZW1haWxfY29uZmlybQ%3D%3D&amp;sig=" +
            "c379e14f694d464b604014d39b62beeb4a4ac57d&amp;iid=3169f397e21d4feea6595eed" +
            "2aa5ede2&amp;uid=2893158569&amp;nid=18+97\">Help</a> | <" +
            "a href=\"https://zeke.com/i/redirect?url=https%3A%2F%2Fzeke.com%2F" +
            "account%2Fnot_my_account%2Fmaxmurxy%2F77AHF-DEA3H-141699%3Fcn%3DZW1haWxfY29" +
            "uZmlybQ%253D%253D&amp;t=1&amp;cn=ZW1haWxfY29uZmlybQ%3D%3D&amp;sig=f33" +
            "5de8b1c2a305c0d8016c9d554f9274772853e&amp;iid=3169f397e21d4feea6595eed2aa" +
            "5ede2&amp;uid=2893158569&amp;nid=18+25\">Not my account</a>";

        String result = SaneScriptTags.saneScriptTags(str, new boolean[1]);

        Assert.assertTrue("Link no more intact.", result.indexOf("Q%253E%253D&amp;t=1") > 0);
    }

    @Test
    public void testSaneIt2() {
        String str = "<a href=\"https://zeke.com/i/redirect?url=https%3A%2F%2Fsabbi.zeke.com%3Fcn%3DZW1haWxf" +
            "Y29uZmlybQscript%253E%253D&amp;t=1&amp;cn=ZW1haWxfY29uZmlybQ%3D%3D&amp;sig=" +
            "c379e14f694d464b604014d39b62beeb4a4ac57d&amp;iid=3169f397e21d4feea6595eed" +
            "2aa5ede2&amp;uid=2893158569&amp;nid=18+97\">Help</a> | <" +
            "a href=\"https://zeke.com/i/redirect?url=https%3A%2F%2Fzeke.com%2F" +
            "account%2Fnot_my_account%2Fmaxmurxy%2F77AHF-DEA3H-141699%3Fcn%3DZW1haWxfY29" +
            "uZmlybQ%253D%253D&amp;t=1&amp;cn=ZW1haWxfY29uZmlybQ%3D%3D&amp;sig=f33" +
            "5de8b1c2a305c0d8016c9d554f9274772853e&amp;iid=3169f397e21d4feea6595eed2aa" +
            "5ede2&amp;uid=2893158569&amp;nid=18+25\">Not my account</a>";

        String result = SaneScriptTags.saneScriptTags(str, new boolean[1]);

        Assert.assertTrue("Link no more intact.", result.indexOf("Qscript%253E%253D&amp;t=1") > 0);
    }

    @Test
    public void testSaneIt3() {
        String str = "<a href=\"https://zeke.com/i/redirect?url=https%3A%2F%2Fsabbi.zeke.com%3Fcn%3DZW1haWxf" +
            "Y29uZmlybQ%253C%253D&amp;t=1&amp;cn=ZW1haWxfY29uZmlybQ%3D%3D&amp;sig=" +
            "c379e14f694d464b604014d39b62beeb4a4ac57d&amp;iid=3169f397e21d4feea6595eed" +
            "2aa5ede2&amp;uid=2893158569&amp;nid=18+97\">Help</a> | <" +
            "a href=\"https://zeke.com/i/redirect?url=https%3A%2F%2Fzeke.com%2F" +
            "account%2Fnot_my_account%2Fmaxmurxy%2F77AHF-DEA3H-141699%3Fcn%3DZW1haWxfY29" +
            "uZmlybQ%253D%253D&amp;t=1&amp;cn=ZW1haWxfY29uZmlybQ%3D%3D&amp;sig=f33" +
            "5de8b1c2a305c0d8016c9d554f9274772853e&amp;iid=3169f397e21d4feea6595eed2aa" +
            "5ede2&amp;uid=2893158569&amp;nid=18+25\">Not my account</a>";

        String result = SaneScriptTags.saneScriptTags(str, new boolean[1]);

        Assert.assertTrue("Link no more intact.", result.indexOf("Q%253C%253D&amp;t=1") > 0);
    }

    @Test
    public void testSaneIt4() {
        String str = "<a href=\"https://zeke.com/i/redirect?url=https%3A%2F%2Fsabbi.zeke.com%3Fcn%3DZW1haWxf" +
            "Y29uZmlybQ%253Cscript%253D&amp;t=1&amp;cn=ZW1haWxfY29uZmlybQ%3D%3D&amp;sig=" +
            "c379e14f694d464b604014d39b62beeb4a4ac57d&amp;iid=3169f397e21d4feea6595eed" +
            "2aa5ede2&amp;uid=2893158569&amp;nid=18+97\">Help</a> | <" +
            "a href=\"https://zeke.com/i/redirect?url=https%3A%2F%2Fzeke.com%2F" +
            "account%2Fnot_my_account%2Fmaxmurxy%2F77AHF-DEA3H-141699%3Fcn%3DZW1haWxfY29" +
            "uZmlybQ%253D%253D&amp;t=1&amp;cn=ZW1haWxfY29uZmlybQ%3D%3D&amp;sig=f33" +
            "5de8b1c2a305c0d8016c9d554f9274772853e&amp;iid=3169f397e21d4feea6595eed2aa" +
            "5ede2&amp;uid=2893158569&amp;nid=18+25\">Not my account</a>";

        String result = SaneScriptTags.saneScriptTags(str, new boolean[1]);
        System.out.println(result);
        Assert.assertTrue("Link no more intact.", result.indexOf("Q%253Cscript%253D&amp;t=1") > 0);
    }

    @Test
    public void testBug35630() {
        String str = "<a href=\"https://zeke.com/i/redirect?url=https%3A%2F%2Fsabbi.zeke.com%3Fcn%3DZW1haWxf" +
            "Y29uZmlybQ%253D%253D&amp;t=1&amp;cn=ZW1haWxfY29uZmlybQ%3D%3D&amp;sig=" +
            "c379e14f694d464b604014d39b62beeb4a4ac57d&amp;iid=3169f397e21d4feea6595eed" +
            "2aa5ede2&amp;uid=2893158569&amp;nid=18+97\">Help</a> | <" +
            "a href=\"https://zeke.com/i/redirect?url=https%3A%2F%2Fzeke.com%2F" +
            "account%2Fnot_my_account%2Fmaxmurxy%2F77AHF-DEA3H-141699%3Fcn%3DZW1haWxfY29" +
            "uZmlybQ%253D%253D&amp;t=1&amp;cn=ZW1haWxfY29uZmlybQ%3D%3D&amp;sig=f33" +
            "5de8b1c2a305c0d8016c9d554f9274772853e&amp;iid=3169f397e21d4feea6595eed2aa" +
            "5ede2&amp;uid=2893158569&amp;nid=18+25\">Not my account</a>";

        String result = SaneScriptTags.saneScriptTags(str, new boolean[1]);

        Assert.assertTrue("Link no more intact.", result.indexOf("Q%253D%253D&amp;t=1") > 0);
    }

}
