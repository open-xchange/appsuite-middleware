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

package com.openexchange.recaptcha.impl;

import java.util.Properties;
import net.tanesha.recaptcha.ReCaptcha;
import net.tanesha.recaptcha.ReCaptchaException;
import net.tanesha.recaptcha.ReCaptchaFactory;
import net.tanesha.recaptcha.ReCaptchaImpl;
import net.tanesha.recaptcha.ReCaptchaResponse;
import com.openexchange.recaptcha.ReCaptchaService;

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
