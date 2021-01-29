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

package com.openexchange.test.tryagain;

import static com.openexchange.java.Autoboxing.I;
import static org.slf4j.LoggerFactory.getLogger;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * {@link TryAgainTestRule}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since 7.10.5
 */
public class TryAgainTestRule implements TestRule {

    /**
     * Initializes a new {@link TryAgainTestRule}.
     */
    public TryAgainTestRule() {
        super();
    }

    @Override
    public Statement apply(Statement base, Description description) {
        TryAgain tryAgain = description.getAnnotation(TryAgain.class);
        if (null == tryAgain || 0 >= tryAgain.maxRetries()) {
            return base;
        }
        return new Statement() {

            @Override
            public void evaluate() throws Throwable {
                Throwable error = null;
                for (int i = 0; i < tryAgain.maxRetries(); i++) {
                    try {
                        base.evaluate();
                        return;
                    } catch (Throwable t) {
                        getLogger(TryAgainTestRule.class).info("Failure for {}: \"{}\", trying again ({}/{}).",
                            description.getDisplayName(), t.getMessage(), I(i + 1), I(tryAgain.maxRetries()));
                        if (null == error) {
                            error = t;
                        }
                        if (0 < tryAgain.sleep()) {
                            Thread.sleep(tryAgain.sleep());
                        }
                    }
                }
                if (null != error) {
                    throw error;
                }
            }
        };
    }

}
