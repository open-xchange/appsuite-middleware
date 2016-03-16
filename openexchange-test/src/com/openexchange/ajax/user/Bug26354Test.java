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

package com.openexchange.ajax.user;

import static com.openexchange.java.Autoboxing.B;

import java.util.Random;
import java.util.TimeZone;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.openexchange.ajax.config.AttributeWriter;
import com.openexchange.ajax.config.BetaWriter;
import com.openexchange.ajax.config.actions.GetRequest;
import com.openexchange.ajax.config.actions.SetRequest;
import com.openexchange.ajax.config.actions.Tree;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXClient.User;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.user.actions.SetAttributeRequest;
import com.openexchange.ajax.user.actions.SetAttributeResponse;
import com.openexchange.exception.OXException;
import com.openexchange.java.util.TimeZones;
import com.openexchange.tools.RandomString;

/**
 * {@link Bug26354Test}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public final class Bug26354Test extends AbstractAJAXSession {

    private static final String ATTRIBUTE_NAME = "testForBug26354";

    private static final int ITERATIONS = 100;
    
    private static final TimeZone[] TIME_ZONES = new TimeZone[3];
    static {
    	TIME_ZONES[0] = TimeZones.PST;
    	TIME_ZONES[1] = TimeZones.UTC;
    	TIME_ZONES[2] = TimeZones.EET;
    }

    private final AttributeWriter[] writer = new AttributeWriter[2];
    private final Thread[] thread = new Thread[writer.length];

    private AJAXClient client;
    private int userId;
    private boolean origBetaValue;
	private String origTimeZoneValue;

    public Bug26354Test(String name) {
        super(name);
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        client = getClient();
        userId = client.getValues().getUserId();
        origBetaValue = client.execute(new GetRequest(Tree.Beta)).getBoolean();
        origTimeZoneValue = client.execute(new GetRequest(Tree.TimeZone)).getString();
        
        writer[0] = new BetaWriter(User.User1);
        thread[0] = new Thread(writer[0]);
        writer[1] = new AttributeWriter(Tree.TimeZone, User.User1) {	
        	private final Random r = new Random();
			@Override
			protected Object getValue() {
				return TIME_ZONES[r.nextInt(3)].getID();
			}
		};
		thread[1] = new Thread(writer[1]);
        
        for (int i = 0; i < thread.length; i++) {
            thread[i].start();
        }
    }

    @Override
    @After
    public void tearDown() throws Exception {
        for (int i = 0; i < writer.length; i++) {
            writer[i].stop();
        }
        for (int i = 0; i < thread.length; i++) {
            thread[i].join();
        }
        for (int i = 0; i < writer.length; i++) {
            final Throwable throwable = writer[i].getThrowable();
            assertNull("Expected no Throwable, but there is one: " + throwable, throwable);
        }
        client.execute(new SetRequest(Tree.Beta, B(origBetaValue)));
        client.execute(new SetRequest(Tree.TimeZone, origTimeZoneValue));
        assertTrue("Deleting the test attribute failed.", client.execute(new SetAttributeRequest(userId, ATTRIBUTE_NAME, null, false)).isSuccess());
        super.tearDown();
    }

    @Test
    public void testForDeadlocks() throws Throwable {
        boolean stop = false;
        for (int i = 0; i < ITERATIONS && !stop; i++) {
            String value = RandomString.generateChars(64);
            SetAttributeResponse response = client.execute(new SetAttributeRequest(userId, ATTRIBUTE_NAME, value, false, false));
            if (response.hasError()) {
                OXException e = response.getException();
                String logMessage = e.getLogMessage();
                assertFalse("Bug 26354 appears again. Deadlock in database detected.", logMessage.contains("Deadlock"));
            }
            assertTrue("Setting the attribute was not successful.", response.isSuccess());
            for (int j = 0; j < writer.length; j++) {
                stop = stop || null != writer[j].getThrowable();
            }
        }
    }
}
