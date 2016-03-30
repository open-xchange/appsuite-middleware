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
