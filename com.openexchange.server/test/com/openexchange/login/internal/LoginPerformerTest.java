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

package com.openexchange.login.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.authentication.Authenticated;
import com.openexchange.authentication.AuthenticationService;
import com.openexchange.authentication.Cookie;
import com.openexchange.authentication.Header;
import com.openexchange.authentication.LoginInfo;
import com.openexchange.authentication.ResponseEnhancement;
import com.openexchange.authentication.ResultCode;
import com.openexchange.authentication.service.Authentication;
import com.openexchange.exception.OXException;
import com.openexchange.login.Interface;
import com.openexchange.login.LoginRequest;
import com.openexchange.login.LoginResult;

/**
 * {@link LoginPerformerTest}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 * @since 7.6.0
 */
public class LoginPerformerTest {

    private LoginPerformer performer;
    private TestAuthenticationService testAuthenticationService;
    Cookie testCookie;
    Header testHeader;
    private LoginRequest loginRequest;

    public LoginPerformerTest() {
        super();
    }

    @Before
    public void setUp() {
        performer = LoginPerformer.getInstance();
        testAuthenticationService = new TestAuthenticationService();
        Authentication.setService(testAuthenticationService);
        testCookie = new Cookie() {
            @Override
            public String getValue() {
                return "cookieValue";
            }
            @Override
            public String getName() {
                return "cookieName";
            }
        };
        testHeader = new Header() {
            @Override
            public String getValue() {
                return "headerValue";
            }
            @Override
            public String getName() {
                return "headerName";
            }
        };
        loginRequest = new LoginRequest() {
            @Override
            public boolean isTransient() {
                return false;
            }
            @Override
            public boolean isSecure() {
                return false;
            }
            @Override
            public String getVersion() {
                return null;
            }
            @Override
            public String getUserAgent() {
                return null;
            }
            @Override
            public int getServerPort() {
                return 0;
            }
            @Override
            public String getServerName() {
                return null;
            }
            @Override
            public String getPassword() {
                return null;
            }
            @Override
            public String getLogin() {
                return null;
            }
            @Override
            public Interface getInterface() {
                return Interface.HTTP_JSON;
            }
            @Override
            public String getHttpSessionID() {
                return null;
            }
            @SuppressWarnings("serial")
            @Override
            public Map<String, List<String>> getHeaders() {
                return new HashMap<String, List<String>>() {{
                    put(testHeader.getName(), new ArrayList<String>() {{
                        add(testHeader.getValue());
                    }});
                }};
            }
            @Override
            public String getHash() {
                return null;
            }
            @Override
            public Cookie[] getCookies() {
                return new Cookie[] {
                    testCookie
                };
            }
            @Override
            public String getClientToken() {
                return null;
            }
            @Override
            public String getClientIP() {
                return null;
            }
            @Override
            public String getClient() {
                return null;
            }
            @Override
            public String getAuthId() {
                return null;
            }

            @Override
            public String getLanguage() {
                return null;
            }

            @Override
            public boolean isStoreLanguage() {
                return false;
            }
        };
    }

    @After
    public void tearDown() {
        loginRequest = null;
        testHeader = null;
        testCookie = null;
        Authentication.dropService(testAuthenticationService);
        testAuthenticationService = null;
        performer = null;
    }

    /**
     * Verifies that certain properties from the login are passed through to the AuthenticationService.
     */
    @Test
    public void testAutoLoginProperties() throws OXException {
        LoginResult result = performer.doAutoLogin(loginRequest);
        Cookie[] cookies = result.getCookies();
        assertNotNull("Cookies should be passed through", cookies);
        assertEquals("Exactly one cookie should be passed through", 1, cookies.length);
        assertEquals("The same cookie should be passed through", testCookie, cookies[0]);
        Header[] headers = result.getHeaders();
        assertNotNull("Headers should be passed through", headers);
        assertEquals("Exactly one header should be passed through", 1, headers.length);
        assertEquals("The same header should be passed through", testHeader.getName(), headers[0].getName());
        assertEquals("The same header should be passed through", testHeader.getValue(), headers[0].getValue());
    }

    /**
     * Verifies that certain properties from the login are passed through to the AuthenticationService.
     */
    @Test
    public void testLoginProperties() throws OXException {
        LoginResult result = performer.doLogin(loginRequest);
        Cookie[] cookies = result.getCookies();
        assertNotNull("Cookies should be passed through", cookies);
        assertEquals("Exactly one cookie should be passed through", 1, cookies.length);
        assertEquals("The same cookie should be passed through", testCookie, cookies[0]);
        Header[] headers = result.getHeaders();
        assertNotNull("Headers should be passed through", headers);
        assertEquals("Exactly one header should be passed through", 1, headers.length);
        assertEquals("The same header should be passed through", testHeader.getName(), headers[0].getName());
        assertEquals("The same header should be passed through", testHeader.getValue(), headers[0].getValue());
    }

    private static class TestAuthenticationService implements AuthenticationService {

        public TestAuthenticationService() {
            super();
        }

        @Override
        public Authenticated handleLoginInfo(LoginInfo loginInfo) {
            return handleAutoLoginInfo(loginInfo);
        }

        @Override
        public Authenticated handleAutoLoginInfo(LoginInfo loginInfo) {
            Map<String, Object> properties = loginInfo.getProperties();
            Cookie[] cookies = (Cookie[]) properties.get("cookies");
            @SuppressWarnings("unchecked")
            Map<String, List<String>> headersMap = (Map<String, List<String>>) properties.get("headers");
            List<Header> headers = new ArrayList<Header>();
            for (final Entry<String, List<String>> entry : headersMap.entrySet()) {
                headers.add(new Header() {
                    @Override
                    public String getName() {
                        return entry.getKey();
                    }
                    @Override
                    public String getValue() {
                        return entry.getValue().get(0);
                    }
                });
            }
            return new TestAuthenticated(cookies, headers);
        }
    }

    private static class TestAuthenticated implements Authenticated, ResponseEnhancement {

        private final Cookie[] cookies;
        private final List<Header> headers;

        public TestAuthenticated(Cookie[] cookies, List<Header> headers) {
            super();
            this.cookies = cookies;
            this.headers = headers;
        }

        @Override
        public ResultCode getCode() {
            return ResultCode.REDIRECT;
        }

        @Override
        public Header[] getHeaders() {
            return headers.toArray(new Header[headers.size()]);
        }

        @Override
        public Cookie[] getCookies() {
            return cookies;
        }

        @Override
        public String getRedirect() {
            return null;
        }

        @Override
        public String getContextInfo() {
            return null;
        }

        @Override
        public String getUserInfo() {
            return null;
        }
    }
}
