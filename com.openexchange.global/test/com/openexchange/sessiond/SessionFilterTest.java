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

package com.openexchange.sessiond;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import com.openexchange.java.util.UUIDs;
import com.openexchange.session.SimSession;
import com.openexchange.sessiond.SessionFilter.FilterType;
import com.openexchange.sessiond.SessionFilter.Matchee;
import com.openexchange.sessiond.SessionFilter.Matcher;


/**
 * {@link SessionFilterTest}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.1
 */
public class SessionFilterTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testParser1() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        new SessionFilter.Parser("").parse();
    }

    @Test
    public void testParser2() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        new SessionFilter.Parser("       ").parse();
    }

    @Test
    public void testParser3() throws Exception {
        Matcher matcher = new SessionFilter.Parser("(a=b)").parse();
        Assert.assertTrue(matcher.matches(new Matchee() {
            @Override
            public boolean matches(String attr, String value, FilterType type) {
                return "a".equals(attr) && "b".equals(value) && type == FilterType.EQUAL;
            }
        }));
    }

    @Test
    public void testParser4() throws Exception {
        Matcher matcher = new SessionFilter.Parser("(|(com.openexchange.attr.A=Ich bin lustiger Text)(com.openexchange.attr.B=Ich auch))").parse();
        Assert.assertTrue(matcher.matches(new Matchee() {
            @Override
            public boolean matches(String attr, String value, FilterType type) {
                if (attr.equals("com.openexchange.attr.A")) {
                    return type == FilterType.EQUAL && value.equals("Ich bin lustiger Text");
                }

                if (attr.equals("com.openexchange.attr.B")) {
                    return type == FilterType.EQUAL && value.equals("Ich auch");
                }

                return false;
            }
        }));
    }

    @Test
    public void testParser5() throws Exception {
        final Map<String, String> values = new HashMap<String, String>();
        Matchee mapMatchee = new Matchee() {
            @Override
            public boolean matches(String attr, String value, FilterType type) {
                if (type != FilterType.EQUAL) {
                    return false;
                }

                String string = values.get(attr);
                if (string == null) {
                    return false;
                }

                return string.equals(value);
            }
        };

        values.put("com.openexchange.attr.A", "valueForA");
        assertTrue(matches("(|(com.openexchange.attr.C=asdasfasdf)(com.openexchange.attr.B=sdfsdfsdfsdfd)(com.openexchange.attr.A=valueForA))", mapMatchee));
        assertFalse(matches("(|(com.openexchange.attr.C=asdasfasdf)(com.openexchange.attr.B=sdfsdfsdfsdfd))", mapMatchee));
        assertFalse(matches("(&(com.openexchange.attr.C=asdasfasdf)(com.openexchange.attr.B=sdfsdfsdfsdfd)(com.openexchange.attr.A=valueForA))", mapMatchee));
        values.put("com.openexchange.attr.B", "sdfsdfsdfsdfd");
        values.put("com.openexchange.attr.C", "asdasfasdf");
        assertTrue(matches("(&(com.openexchange.attr.C=asdasfasdf)(com.openexchange.attr.B=sdfsdfsdfsdfd)(com.openexchange.attr.A=valueForA))", mapMatchee));
        assertFalse(matches("(!(&(com.openexchange.attr.C=asdasfasdf)(com.openexchange.attr.B=sdfsdfsdfsdfd)(com.openexchange.attr.A=valueForA)))", mapMatchee));
        values.clear();
        assertTrue(matches("(!(&(com.openexchange.attr.C=asdasfasdf)(com.openexchange.attr.B=sdfsdfsdfsdfd)(com.openexchange.attr.A=valueForA)))", mapMatchee));
        values.put("com.openexchange.attr.A", "valueForA");
        values.put("com.openexchange.attr.B", "sdfsdfsdfsdfd");
        values.put("com.openexchange.attr.C", "asdasfasdf");
        assertTrue(matches("(&(!(com.openexchange.attr.A=mumpitz))(com.openexchange.attr.B=sdfsdfsdfsdfd)(com.openexchange.attr.C=asdasfasdf))", mapMatchee));
        assertFalse(matches("(!(&(!(com.openexchange.attr.A=mumpitz))(com.openexchange.attr.B=sdfsdfsdfsdfd)(com.openexchange.attr.C=asdasfasdf)))", mapMatchee));
    }

    @Test
    public void testParser6() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        new SessionFilter.Parser("(!(&(!(com.openexchange.attr.A=mumpitz))(com.openexchange.attr.B=sdfsdfsdfsdfd)(com.openexchange.attr.C=asdasfasdf))))").parse(); // one closing parenthesis too much
    }

    @Test
    public void testParser7() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        new SessionFilter.Parser("(!(&(!(com.openexchange.attr.A=mumpitz))(com.openexchange.attr.B=sdfsdfsdfsdfd)(com.openexchange.attr.C=asdasfasdf))").parse(); // one closing parenthesis too less
    }

    @Test
    public void testParser8() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        new SessionFilter.Parser("(!(&(!(com.openexchange.attr.A=mumpitz))(com.openexchange.attr.B=sdfsdfsdfsdfd)(com.openexchange.attr.C=asdasfasdf)))  )").parse(); // one closing parenthesis too much
    }

    @Test
    public void testParser9() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        new SessionFilter.Parser("(!(&(!(com.openexchange.attr.A=mumpitz))(com.openexchange.attr.B=sdfsdfsdfsdfd)(com.openexchange.attr.C=asdasfasdf)))asd").parse(); // illegal end sequence
    }

    @Test
    public void testParser10() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        new SessionFilter.Parser("(!(&(!(com.openexchange.attr.A=mumpitz))(com.openexchange.attr.B=sdfsdfsdfsdfd))(com.openexchange.attr.C=asdasfasdf)))asd").parse(); // illegal parenthesis in the middle
    }

    @Test
    public void testFilter1() throws Exception {
        String sessionId = UUIDs.getUnformattedString(UUID.randomUUID());
        String secret = UUIDs.getUnformattedString(UUID.randomUUID());
        String hash = UUIDs.getUnformattedString(UUID.randomUUID());
        String auth = UUIDs.getUnformattedString(UUID.randomUUID());
        String client = UUIDs.getUnformattedString(UUID.randomUUID());
        SimSession simSession = new SimSession(24, 48);
        simSession.setSessionID(sessionId);
        simSession.setHash(hash);
        simSession.setSecret(secret);
        simSession.setAuthId(auth);
        simSession.setClient(client);
        simSession.setParameter("com.openexchange.attr.A", "mumpitz");
        SessionFilter filter = SessionFilter.create("(&"
            + "(" + SessionFilter.CONTEXT_ID + "=48)"
            + "(" + SessionFilter.USER_ID + "=24)"
            + "(" + SessionFilter.SESSION_ID + "=" + sessionId + ")"
            + "(" + SessionFilter.SECRET + "=" + secret + ")"
            + "(" + SessionFilter.HASH + "=" + hash + ")"
            + "(" + SessionFilter.AUTH_ID + "=" + auth + ")"
            + "(" + SessionFilter.CLIENT + "=" + client + ")"
            + "(com.openexchange.attr.A=mumpitz)"
        + ")");
        Assert.assertTrue(filter.apply(simSession));
    }

    @Test
    public void testFilterSecret() throws Exception {
        String sessionId = UUIDs.getUnformattedString(UUID.randomUUID());
        String secret = UUIDs.getUnformattedString(UUID.randomUUID());
        String hash = UUIDs.getUnformattedString(UUID.randomUUID());
        String auth = UUIDs.getUnformattedString(UUID.randomUUID());
        String client = UUIDs.getUnformattedString(UUID.randomUUID());
        SimSession simSession = new SimSession(24, 48);
        simSession.setSessionID(sessionId);
        simSession.setHash(hash);
        simSession.setSecret(secret);
        simSession.setAuthId(auth);
        simSession.setClient(client);
        SessionFilter filter = SessionFilter.create("(" + SessionFilter.SECRET + "=" + secret + ")");
        Assert.assertTrue(filter.apply(simSession));
    }

    private static boolean matches(String filter, Matchee matchee) {
        return new SessionFilter.Parser(filter).parse().matches(matchee);
    }

}
