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
