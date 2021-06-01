/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 * 
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
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
        System.out.println(result);
        System.out.println("------------------");

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
        System.out.println(result);
        System.out.println("------------------");

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
        System.out.println(result);
        System.out.println("------------------");

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
        System.out.println("------------------");

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
        System.out.println(result);
        System.out.println("------------------");

        Assert.assertTrue("Link no more intact.", result.indexOf("Q%253D%253D&amp;t=1") > 0);
    }

}
