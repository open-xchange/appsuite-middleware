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

package com.openexchange.recaptcha.impl;

import java.util.Properties;
import com.openexchange.recaptcha.ReCaptchaService;
import net.tanesha.recaptcha.ReCaptcha;
import net.tanesha.recaptcha.ReCaptchaException;
import net.tanesha.recaptcha.ReCaptchaFactory;
import net.tanesha.recaptcha.ReCaptchaImpl;
import net.tanesha.recaptcha.ReCaptchaResponse;

/**
 * {@link ReCaptchaServiceImpl}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class ReCaptchaServiceImpl implements ReCaptchaService {

    private final Properties props;
    private final Properties options;

    private static final String INVALID_CAPTCHA_ERROR = "incorrect-captcha-sol";

    /**
     * Initializes a new {@link ReCaptchaServiceImpl}.
     * @param props
     */
    public ReCaptchaServiceImpl(Properties props, Properties options) {
        this.props = props;
        this.options = options;
    }

    @Override
    public boolean check(String address, String challenge, String response, boolean strict) {
        ReCaptchaImpl reCaptcha = new ReCaptchaImpl();
        reCaptcha.setPrivateKey(props.getProperty("privateKey"));
        try {
            ReCaptchaResponse answer = reCaptcha.checkAnswer(address, challenge, response);
            if (answer.getErrorMessage() != null && !answer.getErrorMessage().equals(INVALID_CAPTCHA_ERROR) && !strict) {
                return true;
            }
            return answer.isValid();
        } catch (ReCaptchaException e) {
            return !strict;
        }
    }

    @Override
    public String getHTML() {
        ReCaptcha reCaptcha = ReCaptchaFactory.newReCaptcha(props.getProperty("publicKey"), props.getProperty("privateKey"), false);
        return reCaptcha.createRecaptchaHtml(null, options);
    }

}
