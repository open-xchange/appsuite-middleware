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
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
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
import com.openexchange.sessiond.SessiondService;

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
    @Mock
    private SessiondService sessiondServiceMock;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        performer = LoginPerformer.getInstance();
        SessiondService.SERVICE_REFERENCE.set(sessiondServiceMock);
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

            @Override
            public boolean markHttpSessionAuthenticated() {
                return false;
            }

            @SuppressWarnings("serial")
            @Override
            public Map<String, List<String>> getHeaders() {
                return new HashMap<String, List<String>>() {

                    {
                        put(testHeader.getName(), new ArrayList<String>() {

                            {
                                add(testHeader.getValue());
                            }
                        });
                    }
                };
            }

            @Override
            public String getHash() {
                return null;
            }

            @Override
            public Cookie[] getCookies() {
                return new Cookie[] { testCookie
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

            @Override
            public String getLocale() {
                return null;
            }

            @Override
            public boolean isStoreLocale() {
                return false;
            }

            @Override
            public boolean isStaySignedIn() {
                return false;
            }

            @Override
            public Map<String, String[]> getRequestParameter() {
                return null;
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
            @SuppressWarnings("unchecked") Map<String, List<String>> headersMap = (Map<String, List<String>>) properties.get("headers");
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
