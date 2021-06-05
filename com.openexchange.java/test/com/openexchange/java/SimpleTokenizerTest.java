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

package com.openexchange.java;

import java.util.List;
import org.junit.Assert;
import org.junit.Test;


/**
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.0
 */
public class SimpleTokenizerTest extends SimpleTokenizer {

     @Test
     public void testBasicPhraseQuery() {
        List<String> tokens = tokenize("\"Only one token\"");
        Assert.assertEquals("Wrong number of tokens", 1, tokens.size());
        Assert.assertEquals("Wrong token", "Only one token", tokens.get(0));
    }

     @Test
     public void testBasicQuery() {
        List<String> tokens = tokenize("These are four tokens");
        Assert.assertEquals("Wrong number of tokens", 4, tokens.size());
        Assert.assertEquals("Wrong token", "These", tokens.get(0));
        Assert.assertEquals("Wrong token", "are", tokens.get(1));
        Assert.assertEquals("Wrong token", "four", tokens.get(2));
        Assert.assertEquals("Wrong token", "tokens", tokens.get(3));
    }

     @Test
     public void testComplexQuery() {
        List<String> tokens = tokenize(" Expect \"two strings\" as \"not tokenized\" and\nfive\rothers ");
        Assert.assertEquals("Wrong number of tokens", 7, tokens.size());
        Assert.assertEquals("Wrong token", "Expect", tokens.get(0));
        Assert.assertEquals("Wrong token", "two strings", tokens.get(1));
        Assert.assertEquals("Wrong token", "as", tokens.get(2));
        Assert.assertEquals("Wrong token", "not tokenized", tokens.get(3));
        Assert.assertEquals("Wrong token", "and", tokens.get(4));
        Assert.assertEquals("Wrong token", "five", tokens.get(5));
        Assert.assertEquals("Wrong token", "others", tokens.get(6));
    }

     @Test
     public void testBrokenQuery() {
        List<String> tokens = tokenize("Something \" went wrong here");
        Assert.assertEquals("Wrong number of tokens", 4, tokens.size());
        Assert.assertEquals("Wrong token", "Something", tokens.get(0));
        Assert.assertEquals("Wrong token", "went", tokens.get(1));
        Assert.assertEquals("Wrong token", "wrong", tokens.get(2));
        Assert.assertEquals("Wrong token", "here", tokens.get(3));
    }

     @Test
     public void testBrokenQuery2() {
        List<String> tokens = tokenize("Something \"went wrong\" here \"");
        Assert.assertEquals("Wrong number of tokens", 3, tokens.size());
        Assert.assertEquals("Wrong token", "Something", tokens.get(0));
        Assert.assertEquals("Wrong token", "went wrong", tokens.get(1));
        Assert.assertEquals("Wrong token", "here", tokens.get(2));
    }

     @Test
     public void testBrokenQuery3() {
        List<String> tokens = tokenize("\" Something went wrong here ");
        Assert.assertEquals("Wrong number of tokens", 4, tokens.size());
        Assert.assertEquals("Wrong token", "Something", tokens.get(0));
        Assert.assertEquals("Wrong token", "went", tokens.get(1));
        Assert.assertEquals("Wrong token", "wrong", tokens.get(2));
        Assert.assertEquals("Wrong token", "here", tokens.get(3));
    }

     @Test
     public void testEmptyQuery() {
        List<String> tokens = tokenize("");
        Assert.assertEquals("Wrong number of tokens", 0, tokens.size());
    }

     @Test
     public void testWhitespaceQuery() {
        List<String> tokens = tokenize("  \" \" ");
        Assert.assertEquals("Wrong number of tokens", 0, tokens.size());
    }

}
