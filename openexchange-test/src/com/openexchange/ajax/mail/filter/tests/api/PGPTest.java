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

package com.openexchange.ajax.mail.filter.tests.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.openexchange.ajax.mail.filter.api.dao.Rule;
import com.openexchange.ajax.mail.filter.api.dao.action.Action;
import com.openexchange.ajax.mail.filter.api.dao.action.PGP;
import com.openexchange.ajax.mail.filter.api.dao.action.argument.ActionArgument;
import com.openexchange.ajax.mail.filter.api.dao.test.TrueTest;
import com.openexchange.ajax.mail.filter.tests.AbstractMailFilterTest;

/**
 * {@link PGPTest}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class PGPTest extends AbstractMailFilterTest {

    /**
     * Initialises a new {@link PGPTest}.
     * 
     * @param name test case's name
     */
    public PGPTest(String name) {
        super(name);
    }

    /**
     * Test new PGP filter without key
     */
    public void testNewPGPWithoutKey() throws Exception {
        Rule expected = new Rule();
        expected.setName("PGP without key");

        expected.setTest(new TrueTest());
        expected.setActions(Collections.<Action<? extends ActionArgument>> singletonList(new PGP()));

        int id = mailFilterAPI.createRule(expected);
        expected.setId(id);
        expected.setPosition(0);
        getAndAssert(Collections.singletonList(expected));
    }

    /**
     * Test new PGP filter with a single key
     */
    public void testNewPGP() throws Exception {
        Rule expected = new Rule();
        expected.setName("PGP with key");
        expected.setActive(true);

        expected.setTest(new TrueTest());
        String key = "-----BEGIN PGP PUBLIC KEY BLOCK-----\\u000a" + "Version: GnuPG v2.0.17 (GNU/Linux)\\u000a" + "\\u000a" + "mQENBFLreFoBCADOWZYrs/btv3DExwazPTxkmkzdmKgp3uw3+w0UDohFyyOcXowv\\u000a" + "81Q7DGEuTU9lk/R1TigzBWfVt8OOAKGGn1JGcDs+CVGdU++4VUoT9KvwoPL7K8Ys\\u000a" + "frxWGxheEP4XGEhN++92dsQ1p6hIeZPf5z3V3MofZRls+SDeo1zhi33DGiYVYQHp\\u000a" + "D3A6+8X9rITsdRUXeyT6Qrv8q4yr6hUf2BnX5B+HSLjJeQ7CPj1YCM01onZIwSlv\\u000a" + "g4hpEx/JmHSupMkmCk7FpXXCM+fvdq07PtQBAd1Cbw4IPhdvQYop+tYID/ChBC4K\\u000a" + "tv1dU+UBvSausx4GjmkLgcGooYSvHR5YZrnzABEBAAG0MkhhYmkgVGFrICh0ZXN0\\u000a" + "YWNjb3VudCkgPGhhYml0YWtAdGVzdC5mYWlsbWFpbC5vcmc+iQE+BBMBAgAoBQJS\\u000a" + "63haAhsDBQkB4TOABgsJCAcDAgYVCAIJCgsEFgIDAQIeAQIXgAAKCRAdGOi2gcHA\\u000a" + "Qgk+B/9Td7mc2i0NEa367LG6LQCq5EqdGWv9F3GeGZ+5eA/j058IwIedamLgpgh8\\u000a" + "x3DyHhv9cPtPEWN9ZNlxIwRMv8JhS08PgXScOfbyaOktF03W7a7Qq190nVUKfMfo\\u000a" + "4wPewKGNSpXqLn1wNiAaeSIftShylShTw+1nMKjyYBmWxRWcuta5wNLC4nJ1XcVb\\u000a" + "3kSkXvH9GGCTd6iiZ6who12XmjmRXmSkCpIgG57hwykeQJ1gqVoeOXYC2xJA3EsJ\\u000a" + "9m3o/ElVqsyUs7rzROeXImOYadIRwerVtcchsyPMCZJrJXwDan0dZykcwfgydMbA\\u000a" + "X+GSrdiYPqSl8xJp4l4mibQUlvxYuQENBFLreFoBCADHqj2Xgi1tiyO5qcLvh7LF\\u000a" + "qoA8Zfa9YNL8QyaZfEGWCY3inZ1BuvbjRw4P8B0deOoKkOxgc3BLaPOL3TMQIv2j\\u000a" + "wYuWXqLnn6zfC1eAlql4Ms+yMIm8nZ5y6Dua23bUeUpp/wd6+ZBD+jd7cVatsT9O\\u000a" + "COje4xiw3R7vAofz9iQl6WI5/7ILi5IGPJ/KtlLtDeufLG9loRNHyT5pRJqIXiMG\\u000a" + "p3kfe5YGuNPpktSLPKUZeZOmIQG3wBRAD21qjI0H93aG9M6KbaU4veiyBz6Pd+IA\\u000a" + "SzOMrOGc/usnAb7Ze8Xlm6ulQh/Zby3GiivzldQeJxuj2f9mo+1GdQPMzZhVk6tT\\u000a" + "ABEBAAGJASUEGAECAA8FAlLreFoCGwwFCQHhM4AACgkQHRjotoHBwEJ6iQf+NqIK\\u000a" + "O2VhNZdZFPO6sX9ENeW0skZz3x6iqJ+d7xxhmdNRNFihDcaXPc0CKSzjV2jq5xHP\\u000a" + "LcbqcPwLJZAIeXAOQyUQ0PAxdmJbPws8wg+evb3fQa9NpBbUnsQpBUrBvMhPidgH\\u000a" + "bKviYr9eU1u2XwF+YLx21KFJmHcBfZARbkg8bxweRIlF9K4WKEhA6bi62F/NNxFU\\u000a" + "D8vG92Pach9vxJmPTY6Afv2YQQzcE1ZFxsw4ilImLYd0l28GWyHHEmHJaC4RZWXg\\u000a" + "5HMSS3/MzcRwJMAOjloFzkAtM1NdqSIhXR9A+UWOqRrN9YsaZ0toygJ6+s4CuC5E\\u000a" + "xIkAf8c60CqC9IpP4Q==\\u000a" + "=lIKb\\u000a" + "-----END PGP PUBLIC KEY BLOCK-----\\u000a" + "";
        expected.setActions(Collections.<Action<? extends ActionArgument>> singletonList(new PGP(Collections.singletonList(key))));

        int id = mailFilterAPI.createRule(expected);
        expected.setId(id);
        expected.setPosition(0);
        getAndAssert(Collections.singletonList(expected));
    }

    /**
     * Test new PGP filter with multiple keys
     */
    public void testNewPGPWithMultipleKeys() throws Exception {
        Rule expected = new Rule();
        expected.setName("PGP with multiple keys");
        expected.setActive(true);

        expected.setTest(new TrueTest());
        String key1 = "-----BEGIN PGP PUBLIC KEY BLOCK-----\\u000a" + "Version: GnuPG v2.0.17 (GNU/Linux)\\u000a" + "\\u000a" + "mQENBFLreFoBCADOWZYrs/btv3DExwazPTxkmkzdmKgp3uw3+w0UDohFyyOcXowv\\u000a" + "81Q7DGEuTU9lk/R1TigzBWfVt8OOAKGGn1JGcDs+CVGdU++4VUoT9KvwoPL7K8Ys\\u000a" + "frxWGxheEP4XGEhN++92dsQ1p6hIeZPf5z3V3MofZRls+SDeo1zhi33DGiYVYQHp\\u000a" + "D3A6+8X9rITsdRUXeyT6Qrv8q4yr6hUf2BnX5B+HSLjJeQ7CPj1YCM01onZIwSlv\\u000a" + "g4hpEx/JmHSupMkmCk7FpXXCM+fvdq07PtQBAd1Cbw4IPhdvQYop+tYID/ChBC4K\\u000a" + "tv1dU+UBvSausx4GjmkLgcGooYSvHR5YZrnzABEBAAG0MkhhYmkgVGFrICh0ZXN0\\u000a" + "YWNjb3VudCkgPGhhYml0YWtAdGVzdC5mYWlsbWFpbC5vcmc+iQE+BBMBAgAoBQJS\\u000a" + "63haAhsDBQkB4TOABgsJCAcDAgYVCAIJCgsEFgIDAQIeAQIXgAAKCRAdGOi2gcHA\\u000a" + "Qgk+B/9Td7mc2i0NEa367LG6LQCq5EqdGWv9F3GeGZ+5eA/j058IwIedamLgpgh8\\u000a" + "x3DyHhv9cPtPEWN9ZNlxIwRMv8JhS08PgXScOfbyaOktF03W7a7Qq190nVUKfMfo\\u000a" + "4wPewKGNSpXqLn1wNiAaeSIftShylShTw+1nMKjyYBmWxRWcuta5wNLC4nJ1XcVb\\u000a" + "3kSkXvH9GGCTd6iiZ6who12XmjmRXmSkCpIgG57hwykeQJ1gqVoeOXYC2xJA3EsJ\\u000a" + "9m3o/ElVqsyUs7rzROeXImOYadIRwerVtcchsyPMCZJrJXwDan0dZykcwfgydMbA\\u000a" + "X+GSrdiYPqSl8xJp4l4mibQUlvxYuQENBFLreFoBCADHqj2Xgi1tiyO5qcLvh7LF\\u000a" + "qoA8Zfa9YNL8QyaZfEGWCY3inZ1BuvbjRw4P8B0deOoKkOxgc3BLaPOL3TMQIv2j\\u000a" + "wYuWXqLnn6zfC1eAlql4Ms+yMIm8nZ5y6Dua23bUeUpp/wd6+ZBD+jd7cVatsT9O\\u000a" + "COje4xiw3R7vAofz9iQl6WI5/7ILi5IGPJ/KtlLtDeufLG9loRNHyT5pRJqIXiMG\\u000a" + "p3kfe5YGuNPpktSLPKUZeZOmIQG3wBRAD21qjI0H93aG9M6KbaU4veiyBz6Pd+IA\\u000a" + "SzOMrOGc/usnAb7Ze8Xlm6ulQh/Zby3GiivzldQeJxuj2f9mo+1GdQPMzZhVk6tT\\u000a" + "ABEBAAGJASUEGAECAA8FAlLreFoCGwwFCQHhM4AACgkQHRjotoHBwEJ6iQf+NqIK\\u000a" + "O2VhNZdZFPO6sX9ENeW0skZz3x6iqJ+d7xxhmdNRNFihDcaXPc0CKSzjV2jq5xHP\\u000a" + "LcbqcPwLJZAIeXAOQyUQ0PAxdmJbPws8wg+evb3fQa9NpBbUnsQpBUrBvMhPidgH\\u000a" + "bKviYr9eU1u2XwF+YLx21KFJmHcBfZARbkg8bxweRIlF9K4WKEhA6bi62F/NNxFU\\u000a" + "D8vG92Pach9vxJmPTY6Afv2YQQzcE1ZFxsw4ilImLYd0l28GWyHHEmHJaC4RZWXg\\u000a" + "5HMSS3/MzcRwJMAOjloFzkAtM1NdqSIhXR9A+UWOqRrN9YsaZ0toygJ6+s4CuC5E\\u000a" + "xIkAf8c60CqC9IpP4Q==\\u000a" + "=lIKb\\u000a" + "-----END PGP PUBLIC KEY BLOCK-----\\u000a" + "";
        String key2 = "-----BEGIN PGP PUBLIC KEY BLOCK-----\\u000a" + "Version: GnuPG v2.0.17 (GNU/Linux)\\u000a" + "\\u000a" + "mQENBFLeQMwBCADj/5cH+zw67hEkM9JmzVj2IZjy7vuwrlCvO9908qvJ29r6oZti\\u000a" + "MbtVfZ5SZb1wxjnQhGHQqTUbSxHP916y0xKypPBf2UdkQr+yNsb6pZsvpB9gxn/N\\u000a" + "ch6TPwRD36g/XlsWxBe2V/JlYexzJELi6u/ilxjzf22oCOOjU5251hK3c9VpRfRW\\u000a" + "Lu1LSfXCGHsxZw2e32it7yb3yjpiZ/KhzuFBXNLcV/b5oosJcTx5sqn3ohLb6oHn\\u000a" + "LmBfw+6G9xEJsYMMvKqKiRK4eHTSHihOoXVYORPFbvygFo8uYVN5904fU9qZzSgK\\u000a" + "s9IEWA135P4z46p8VatvBAWPge/pK4hT2mKJABEBAAG0K1RhdWIgT3NzICh0ZXN0\\u000a" + "KSA8dGF1Ym9zc0B0ZXN0LmZhaWxtYWlsLm9yZz6JAT4EEwECACgFAlLeQMwCGwMF\\u000a" + "CQHhM4AGCwkIBwMCBhUIAgkKCwQWAgMBAh4BAheAAAoJEA0+C0wRU/tiYYgH/R/Q\\u000a" + "Cz6ozUt3dsU7i89YrJQAQAZAMEgOToofWNIdzbK2EUjMsLZsEnuJb9r3IB7pHZ6D\\u000a" + "X8jBwgRv9y30hF3Y3pDg1ivyL9sxI0DUKVq/ZzLEvPVZjjwKybAxffLBsU+OHDtK\\u000a" + "QPS1zyJJp9AxR10h6sQDFgPN9hq5X03YEectyO0T7ry9v/CzTk27Iq8fhC1ciRoB\\u000a" + "jrcXaHpdWBv0452ifz4D70CvHPxMDN8MYSoq7VQXHFm7JNK/3cCzahQzbcz7uS+U\\u000a" + "9jbXQ4GOYdIzMmx+WjAU+6ZHL2feMMpNKmYF6ivhv2IOxSszVoQIa2Ul97Fq0CxC\\u000a" + "RHKf/bT4GkNvFnG3hx65AQ0EUt5AzAEIAMuWswB/A1HP7nw901kpUZW15RMk5n1Q\\u000a" + "f2fHB/2u7w8XIXq+mcEJEveSXB57vTLU/piPTUctEClDFWcIt/b7hzeGU3IHy52w\\u000a" + "b5moPG5EYLk1vTxt31e5/D3wD5j5Sc6hw9AOBZGq5S4qMDWrWtgjFLy6fvPMv8MS\\u000a" + "qmt+E63mQfs3qXwGD8refeqP13j3o2oG7r/4o5u8DD1JLvfDJHsFtDYiE/ympXN8\\u000a" + "m3Z5CL5tOJl9Rgp4BImAbOHE0sGaOah6iRpclJVzKvBRfELfJa9xIOjZxfQLvDl/\\u000a" + "gO0/AdgAGyYGuh/II9SCO42wDQEjr1c0u9XGM2NaTuYnucYPiOHxZQMAEQEAAYkB\\u000a" + "JQQYAQIADwUCUt5AzAIbDAUJAeEzgAAKCRANPgtMEVP7YiXMB/95E3CKZtzONNMO\\u000a" + "RmuzDfmGRT8XKla/lYDpmmm1xKgBjyfkgGt/6/b5zq+B0bvsWYFKTiz3DJz6dQMr\\u000a" + "G12wV+k76slIqvtqoVjgovH+F69xvKcT9+fge1ZpFaq3fqHwhIevcm2PSUw8bE9G\\u000a" + "hBZ1O2eRcoc0+pEjhK5VQ8C/zRjfiy7VTFmx3S1v+Q1KfK8tw3UrevB4RSsg+VR2\\u000a" + "zNAsmnYoFnYM3rCBV/9muavmzo/A3/hY/vwJ2q2bNZSM3R2iIMwnp0vJ4+P0f5+F\\u000a" + "Q6bL51I8/InAJmcApBBpcaPESV1X3GIZUcrGwfOm6ycwF1wlKrD/pqOvRiGFEoH6\\u000a" + "YNOiosMN\\u000a" + "=xuj5\\u000a" + "-----END PGP PUBLIC KEY BLOCK-----\\u000a" + "";
        List<String> keys = new ArrayList<String>(2);
        Collections.addAll(keys, key1, key2);
        expected.setActions(Collections.<Action<? extends ActionArgument>> singletonList(new PGP(keys)));

        int id = mailFilterAPI.createRule(expected);
        expected.setId(id);
        expected.setPosition(0);
        getAndAssert(Collections.singletonList(expected));
    }
}
