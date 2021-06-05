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

package com.openexchange.html;

/**
 * {@link XSSHolder}
 *
 * @author <a href="mailto:lars.hoogestraat@open-xchange.com">Lars Hoogestraat</a>
 */
public class XSSHolder {

    private final String xssAttack;
    private final String maliciousParam;
    private final AssertExpression assertExpression;

    /**
     * Initializes a new {@link XSSHolder}.
     * Asserts that the provided XSS attack should be completely sanitized. <code>AssertExpression.EMPTY</code> is assumed.
     *
     * @param xssAttack - the string containing the XSS injection
     */
    public XSSHolder(String xssAttack) {
        this(xssAttack, AssertExpression.EMPTY, null);
    }

    /**
     * Initializes a new {@link XSSHolder}.
     *
     * @param xssAttack - the string containing the XSS injection
     * @param assertExpression - the expression to be evaluated
     * @param maliciousParam - the param which should not be contained after sanitizing; important for <code>AssertExpression.NOT_CONTAINED</code>
     */
    public XSSHolder(String xssAttack, AssertExpression assertExpression, String maliciousParam) {
        this.xssAttack = xssAttack;
        this.maliciousParam = maliciousParam;
        this.assertExpression = assertExpression;
    }

    public String getXssAttack() {
        return xssAttack;
    }

    public String getMalicious() {
        return maliciousParam;
    }

    public AssertExpression getAssertExpression() {
        return assertExpression;
    }

}
