
package com.openexchange.ajax.mail.filter.tests.api;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.xml.sax.SAXException;
import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.PutMethodWebRequest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.openexchange.ajax.AJAXServlet;

public abstract class AJAXTest {

    public class WebconversationAndSessionID {

        private final WebConversation webConversation;

        private final String sessionid;

        /**
         * @param webConversation
         * @param sessionid
         */
        public WebconversationAndSessionID(WebConversation webConversation, String sessionid) {
            this.sessionid = sessionid;
            this.webConversation = webConversation;
        }

        public final WebConversation getWebConversation() {
            return webConversation;
        }

        public final String getSessionid() {
            return sessionid;
        }

    }

    private enum CurrentDate {
        date, time, weekday
    };

    public static final String PROTOCOL = "http://";

    private static final String MAILFILTER_URL = "/ajax/mailfilter";

    private static final String LOGOUT_URL = "/ajax/login";

    @Test
    public void MailfilterconfigTest() throws MalformedURLException, IOException, SAXException, JSONException {
        final WebconversationAndSessionID login = login();
        try {
            mailfilterconfig(login, getHostname(), getUsername());
        } finally {
            logout(login);
        }
    }

    @Test
    public void MailfilterlistTest() throws MalformedURLException, IOException, SAXException, JSONException {
        final WebconversationAndSessionID login = login();
        try {
            mailfilterlist(login, getHostname(), getUsername());
        } finally {
            logout(login);
        }
    }

    @Test
    public void MailfilterdeleteTest() throws MalformedURLException, IOException, SAXException, JSONException {
        final WebconversationAndSessionID login = login();
        try {
            final String test = "{\"active\":true,\"flags\":[],\"actioncmds\":[{\"into\":\"INBOX/Spam\",\"id\":\"move\"},{\"id\":\"stop\"}],\"id\":0,\"test\":{\"tests\":[{\"headers\":[\"from\"],\"values\":[\"zitate.at\"],\"comparison\":\"user\",\"id\":\"address\"},{\"headers\":[\"subject\"],\"values\":[\"Zitat des Tages\"],\"comparison\":\"contains\",\"id\":\"header\"}],\"id\":\"allof\"},\"rulename\":\"\"}";
            final String newid = mailfilternew(login, getHostname(), getUsername(), test, null);
            mailfilterdelete(login, getHostname(), getUsername(), Integer.parseInt(newid));
        } finally {
            logout(login);
        }
    }

    /**
     * @deprecated Use {@link NewTest#testNewAllOf()} instead
     */
    @Test
    public void MailfilternewTest() throws MalformedURLException, IOException, SAXException, JSONException {
        final WebconversationAndSessionID login = login();
        try {
            final String test = "{\"active\":true,\"position\":0,\"flags\":[],\"actioncmds\":[{\"into\":\"default.INBOX/Spam\",\"id\":\"move\"},{\"id\":\"stop\"}],\"id\":0,\"test\":{\"tests\":[{\"headers\":[\"from\"],\"values\":[\"zitate.at\"],\"comparison\":\"user\",\"id\":\"address\"},{\"headers\":[\"subject\"],\"values\":[\"Zitat des Tages\"],\"comparison\":\"contains\",\"id\":\"header\"}],\"id\":\"allof\"},\"rulename\":\"\"}";
            System.out.println(test);
            final String newid = mailfilternew(login, getHostname(), getUsername(), test, null);
            System.out.println("Rule created with newid: " + newid);
            mailfilterdelete(login, getHostname(), getUsername(), Integer.parseInt(newid));
        } finally {
            logout(login);
        }
    }

    @Test
    public void MailfilterVacationTestWithOutSubject() throws MalformedURLException, IOException, SAXException, JSONException {
        WebconversationAndSessionID login;
        login = login();
        try {
            final JSONObject base = new JSONObject();
            base.put("rulename", "Abwesenheitsbenachrichtigung");
            base.put("active", Boolean.TRUE);
            base.append("flags", "vacation");
            final JSONObject test = new JSONObject();
            test.put("id", "true");
            base.put("test", test);
            final JSONObject action = new JSONObject();
            action.put("id", "vacation");
            action.put("days", 7);
            action.append("addresses", "dennis.sieben@open-xchange.com");
            action.put("text", "I'm out of office");
            base.append("actioncmds", action);

            final String newid = mailfilternew(login, getHostname(), getUsername(), base.toString(), null);
            System.out.println("Rule created with newid: " + newid);
            mailfilterdelete(login, getHostname(), getUsername(), Integer.parseInt(newid));
        } finally {
            // Log out in any case
            logout(login);
        }
    }

    @Test
    public void MailfilternewVacationPlainAtTheEndTest() throws MalformedURLException, IOException, SAXException, JSONException {
        final WebconversationAndSessionID login = login();
        try {
            final String test = "{\"active\":true,\"text\":\"if true \\r\\n{\\r\\n    vacation :days 13 :addresses [ \\\"root@localhost\\\" , \\\"billg@microsoft.com\\\" ] :mime :subject \\\"Betreff\\\" \\\"Text\\r\\nText\\\" ;\\r\\n}\\r\\n\",\"errormsg\":\"\",\"flags\":[\"vacation\"],\"id\":3,\"rulename\":\"Vacation Notice\"}";
            final String newid = mailfilternew(login, getHostname(), getUsername(), test, null);
            System.out.println("Rule created with newid: " + newid);
            mailfilterdelete(login, getHostname(), getUsername(), Integer.parseInt(newid));
        } finally {
            logout(login);
        }
    }

    @Test
    public void MailfilternewVacationPlainInBetweenTest() throws MalformedURLException, IOException, SAXException, JSONException {
        final WebconversationAndSessionID login = login();
        try {
            final String test = "{\"active\":true,\"position\":0,\"text\":\"if true \\r\\n{\\r\\n    vacation :days 13 :addresses [ \\\"root@localhost\\\" , \\\"billg@microsoft.com\\\" ] :mime :subject \\\"Betreff\\\" \\\"Text\\r\\nText\\\" ;\\r\\n}\\r\\n\",\"errormsg\":\"\",\"flags\":[\"vacation\"],\"id\":3,\"rulename\":\"Vacation Notice\"}";
            final String newid = mailfilternew(login, getHostname(), getUsername(), test, null);
            System.out.println("Rule created with newid: " + newid);
            mailfilterdelete(login, getHostname(), getUsername(), Integer.parseInt(newid));
        } finally {
            logout(login);
        }
    }

    @Test
    public void MailfilternewPGPTestZero() throws MalformedURLException, IOException, SAXException, JSONException {
        final WebconversationAndSessionID login = login();
        try {
            final String test = "{\"rulename\":\"Inbox encryption\",\"active\":false,\"flags\":[],\"test\":{\"id\":\"true\"},\"actioncmds\":[{\"id\":\"pgp\",\"keys\":[]}]}";
            final String newid = mailfilternew(login, getHostname(), getUsername(), test, "Empty string-arrays are not allowed in sieve.");
            System.out.println("Rule created with newid: " + newid);
        } finally {
            logout(login);
        }
    }

    @Test
    public void MailfilternewPGPTest() throws MalformedURLException, IOException, SAXException, JSONException {
        final WebconversationAndSessionID login = login();
        try {
            final String test = "{\"rulename\":\"PGP\",\"active\":true,\"flags\":[\"\"],\"test\":{\"id\":\"true\"},\"actioncmds\":[{\"id\":\"pgp\",\"keys\":[\"-----BEGIN PGP PUBLIC KEY BLOCK-----\\u000a" + "Version: GnuPG v2.0.17 (GNU/Linux)\\u000a" + "\\u000a" + "mQENBFLreFoBCADOWZYrs/btv3DExwazPTxkmkzdmKgp3uw3+w0UDohFyyOcXowv\\u000a" + "81Q7DGEuTU9lk/R1TigzBWfVt8OOAKGGn1JGcDs+CVGdU++4VUoT9KvwoPL7K8Ys\\u000a" + "frxWGxheEP4XGEhN++92dsQ1p6hIeZPf5z3V3MofZRls+SDeo1zhi33DGiYVYQHp\\u000a" + "D3A6+8X9rITsdRUXeyT6Qrv8q4yr6hUf2BnX5B+HSLjJeQ7CPj1YCM01onZIwSlv\\u000a" + "g4hpEx/JmHSupMkmCk7FpXXCM+fvdq07PtQBAd1Cbw4IPhdvQYop+tYID/ChBC4K\\u000a" + "tv1dU+UBvSausx4GjmkLgcGooYSvHR5YZrnzABEBAAG0MkhhYmkgVGFrICh0ZXN0\\u000a" + "YWNjb3VudCkgPGhhYml0YWtAdGVzdC5mYWlsbWFpbC5vcmc+iQE+BBMBAgAoBQJS\\u000a" + "63haAhsDBQkB4TOABgsJCAcDAgYVCAIJCgsEFgIDAQIeAQIXgAAKCRAdGOi2gcHA\\u000a" + "Qgk+B/9Td7mc2i0NEa367LG6LQCq5EqdGWv9F3GeGZ+5eA/j058IwIedamLgpgh8\\u000a" + "x3DyHhv9cPtPEWN9ZNlxIwRMv8JhS08PgXScOfbyaOktF03W7a7Qq190nVUKfMfo\\u000a" + "4wPewKGNSpXqLn1wNiAaeSIftShylShTw+1nMKjyYBmWxRWcuta5wNLC4nJ1XcVb\\u000a" + "3kSkXvH9GGCTd6iiZ6who12XmjmRXmSkCpIgG57hwykeQJ1gqVoeOXYC2xJA3EsJ\\u000a" + "9m3o/ElVqsyUs7rzROeXImOYadIRwerVtcchsyPMCZJrJXwDan0dZykcwfgydMbA\\u000a" + "X+GSrdiYPqSl8xJp4l4mibQUlvxYuQENBFLreFoBCADHqj2Xgi1tiyO5qcLvh7LF\\u000a" + "qoA8Zfa9YNL8QyaZfEGWCY3inZ1BuvbjRw4P8B0deOoKkOxgc3BLaPOL3TMQIv2j\\u000a" + "wYuWXqLnn6zfC1eAlql4Ms+yMIm8nZ5y6Dua23bUeUpp/wd6+ZBD+jd7cVatsT9O\\u000a" + "COje4xiw3R7vAofz9iQl6WI5/7ILi5IGPJ/KtlLtDeufLG9loRNHyT5pRJqIXiMG\\u000a" + "p3kfe5YGuNPpktSLPKUZeZOmIQG3wBRAD21qjI0H93aG9M6KbaU4veiyBz6Pd+IA\\u000a" + "SzOMrOGc/usnAb7Ze8Xlm6ulQh/Zby3GiivzldQeJxuj2f9mo+1GdQPMzZhVk6tT\\u000a" + "ABEBAAGJASUEGAECAA8FAlLreFoCGwwFCQHhM4AACgkQHRjotoHBwEJ6iQf+NqIK\\u000a" + "O2VhNZdZFPO6sX9ENeW0skZz3x6iqJ+d7xxhmdNRNFihDcaXPc0CKSzjV2jq5xHP\\u000a" + "LcbqcPwLJZAIeXAOQyUQ0PAxdmJbPws8wg+evb3fQa9NpBbUnsQpBUrBvMhPidgH\\u000a" + "bKviYr9eU1u2XwF+YLx21KFJmHcBfZARbkg8bxweRIlF9K4WKEhA6bi62F/NNxFU\\u000a" + "D8vG92Pach9vxJmPTY6Afv2YQQzcE1ZFxsw4ilImLYd0l28GWyHHEmHJaC4RZWXg\\u000a" + "5HMSS3/MzcRwJMAOjloFzkAtM1NdqSIhXR9A+UWOqRrN9YsaZ0toygJ6+s4CuC5E\\u000a" + "xIkAf8c60CqC9IpP4Q==\\u000a" + "=lIKb\\u000a" + "-----END PGP PUBLIC KEY BLOCK-----\\u000a" + "\"]}]}";
            final String newid = mailfilternew(login, getHostname(), getUsername(), test, null);
            System.out.println("Rule created with newid: " + newid);
        } finally {
            logout(login);
        }
    }

    @Test
    public void MailfilternewPGP2KeysTest() throws MalformedURLException, IOException, SAXException, JSONException {
        final WebconversationAndSessionID login = login();
        try {
            final String test = "{\"rulename\":\"PGP\",\"active\":true,\"flags\":[\"\"],\"test\":{\"id\":\"true\"},\"actioncmds\":[{\"id\":\"pgp\",\"keys\":[\"-----BEGIN PGP PUBLIC KEY BLOCK-----\\u000a" + "Version: GnuPG v2.0.17 (GNU/Linux)\\u000a" + "\\u000a" + "mQENBFLreFoBCADOWZYrs/btv3DExwazPTxkmkzdmKgp3uw3+w0UDohFyyOcXowv\\u000a" + "81Q7DGEuTU9lk/R1TigzBWfVt8OOAKGGn1JGcDs+CVGdU++4VUoT9KvwoPL7K8Ys\\u000a" + "frxWGxheEP4XGEhN++92dsQ1p6hIeZPf5z3V3MofZRls+SDeo1zhi33DGiYVYQHp\\u000a" + "D3A6+8X9rITsdRUXeyT6Qrv8q4yr6hUf2BnX5B+HSLjJeQ7CPj1YCM01onZIwSlv\\u000a" + "g4hpEx/JmHSupMkmCk7FpXXCM+fvdq07PtQBAd1Cbw4IPhdvQYop+tYID/ChBC4K\\u000a" + "tv1dU+UBvSausx4GjmkLgcGooYSvHR5YZrnzABEBAAG0MkhhYmkgVGFrICh0ZXN0\\u000a" + "YWNjb3VudCkgPGhhYml0YWtAdGVzdC5mYWlsbWFpbC5vcmc+iQE+BBMBAgAoBQJS\\u000a" + "63haAhsDBQkB4TOABgsJCAcDAgYVCAIJCgsEFgIDAQIeAQIXgAAKCRAdGOi2gcHA\\u000a" + "Qgk+B/9Td7mc2i0NEa367LG6LQCq5EqdGWv9F3GeGZ+5eA/j058IwIedamLgpgh8\\u000a" + "x3DyHhv9cPtPEWN9ZNlxIwRMv8JhS08PgXScOfbyaOktF03W7a7Qq190nVUKfMfo\\u000a" + "4wPewKGNSpXqLn1wNiAaeSIftShylShTw+1nMKjyYBmWxRWcuta5wNLC4nJ1XcVb\\u000a" + "3kSkXvH9GGCTd6iiZ6who12XmjmRXmSkCpIgG57hwykeQJ1gqVoeOXYC2xJA3EsJ\\u000a" + "9m3o/ElVqsyUs7rzROeXImOYadIRwerVtcchsyPMCZJrJXwDan0dZykcwfgydMbA\\u000a" + "X+GSrdiYPqSl8xJp4l4mibQUlvxYuQENBFLreFoBCADHqj2Xgi1tiyO5qcLvh7LF\\u000a" + "qoA8Zfa9YNL8QyaZfEGWCY3inZ1BuvbjRw4P8B0deOoKkOxgc3BLaPOL3TMQIv2j\\u000a" + "wYuWXqLnn6zfC1eAlql4Ms+yMIm8nZ5y6Dua23bUeUpp/wd6+ZBD+jd7cVatsT9O\\u000a" + "COje4xiw3R7vAofz9iQl6WI5/7ILi5IGPJ/KtlLtDeufLG9loRNHyT5pRJqIXiMG\\u000a" + "p3kfe5YGuNPpktSLPKUZeZOmIQG3wBRAD21qjI0H93aG9M6KbaU4veiyBz6Pd+IA\\u000a" + "SzOMrOGc/usnAb7Ze8Xlm6ulQh/Zby3GiivzldQeJxuj2f9mo+1GdQPMzZhVk6tT\\u000a" + "ABEBAAGJASUEGAECAA8FAlLreFoCGwwFCQHhM4AACgkQHRjotoHBwEJ6iQf+NqIK\\u000a" + "O2VhNZdZFPO6sX9ENeW0skZz3x6iqJ+d7xxhmdNRNFihDcaXPc0CKSzjV2jq5xHP\\u000a" + "LcbqcPwLJZAIeXAOQyUQ0PAxdmJbPws8wg+evb3fQa9NpBbUnsQpBUrBvMhPidgH\\u000a" + "bKviYr9eU1u2XwF+YLx21KFJmHcBfZARbkg8bxweRIlF9K4WKEhA6bi62F/NNxFU\\u000a" + "D8vG92Pach9vxJmPTY6Afv2YQQzcE1ZFxsw4ilImLYd0l28GWyHHEmHJaC4RZWXg\\u000a" + "5HMSS3/MzcRwJMAOjloFzkAtM1NdqSIhXR9A+UWOqRrN9YsaZ0toygJ6+s4CuC5E\\u000a" + "xIkAf8c60CqC9IpP4Q==\\u000a" + "=lIKb\\u000a" + "-----END PGP PUBLIC KEY BLOCK-----\\u000a" + "\",\"-----BEGIN PGP PUBLIC KEY BLOCK-----\\u000a" + "Version: GnuPG v2.0.17 (GNU/Linux)\\u000a" + "\\u000a" + "mQENBFLeQMwBCADj/5cH+zw67hEkM9JmzVj2IZjy7vuwrlCvO9908qvJ29r6oZti\\u000a" + "MbtVfZ5SZb1wxjnQhGHQqTUbSxHP916y0xKypPBf2UdkQr+yNsb6pZsvpB9gxn/N\\u000a" + "ch6TPwRD36g/XlsWxBe2V/JlYexzJELi6u/ilxjzf22oCOOjU5251hK3c9VpRfRW\\u000a" + "Lu1LSfXCGHsxZw2e32it7yb3yjpiZ/KhzuFBXNLcV/b5oosJcTx5sqn3ohLb6oHn\\u000a" + "LmBfw+6G9xEJsYMMvKqKiRK4eHTSHihOoXVYORPFbvygFo8uYVN5904fU9qZzSgK\\u000a" + "s9IEWA135P4z46p8VatvBAWPge/pK4hT2mKJABEBAAG0K1RhdWIgT3NzICh0ZXN0\\u000a" + "KSA8dGF1Ym9zc0B0ZXN0LmZhaWxtYWlsLm9yZz6JAT4EEwECACgFAlLeQMwCGwMF\\u000a" + "CQHhM4AGCwkIBwMCBhUIAgkKCwQWAgMBAh4BAheAAAoJEA0+C0wRU/tiYYgH/R/Q\\u000a" + "Cz6ozUt3dsU7i89YrJQAQAZAMEgOToofWNIdzbK2EUjMsLZsEnuJb9r3IB7pHZ6D\\u000a" + "X8jBwgRv9y30hF3Y3pDg1ivyL9sxI0DUKVq/ZzLEvPVZjjwKybAxffLBsU+OHDtK\\u000a" + "QPS1zyJJp9AxR10h6sQDFgPN9hq5X03YEectyO0T7ry9v/CzTk27Iq8fhC1ciRoB\\u000a" + "jrcXaHpdWBv0452ifz4D70CvHPxMDN8MYSoq7VQXHFm7JNK/3cCzahQzbcz7uS+U\\u000a" + "9jbXQ4GOYdIzMmx+WjAU+6ZHL2feMMpNKmYF6ivhv2IOxSszVoQIa2Ul97Fq0CxC\\u000a" + "RHKf/bT4GkNvFnG3hx65AQ0EUt5AzAEIAMuWswB/A1HP7nw901kpUZW15RMk5n1Q\\u000a" + "f2fHB/2u7w8XIXq+mcEJEveSXB57vTLU/piPTUctEClDFWcIt/b7hzeGU3IHy52w\\u000a" + "b5moPG5EYLk1vTxt31e5/D3wD5j5Sc6hw9AOBZGq5S4qMDWrWtgjFLy6fvPMv8MS\\u000a" + "qmt+E63mQfs3qXwGD8refeqP13j3o2oG7r/4o5u8DD1JLvfDJHsFtDYiE/ympXN8\\u000a" + "m3Z5CL5tOJl9Rgp4BImAbOHE0sGaOah6iRpclJVzKvBRfELfJa9xIOjZxfQLvDl/\\u000a" + "gO0/AdgAGyYGuh/II9SCO42wDQEjr1c0u9XGM2NaTuYnucYPiOHxZQMAEQEAAYkB\\u000a" + "JQQYAQIADwUCUt5AzAIbDAUJAeEzgAAKCRANPgtMEVP7YiXMB/95E3CKZtzONNMO\\u000a" + "RmuzDfmGRT8XKla/lYDpmmm1xKgBjyfkgGt/6/b5zq+B0bvsWYFKTiz3DJz6dQMr\\u000a" + "G12wV+k76slIqvtqoVjgovH+F69xvKcT9+fge1ZpFaq3fqHwhIevcm2PSUw8bE9G\\u000a" + "hBZ1O2eRcoc0+pEjhK5VQ8C/zRjfiy7VTFmx3S1v+Q1KfK8tw3UrevB4RSsg+VR2\\u000a" + "zNAsmnYoFnYM3rCBV/9muavmzo/A3/hY/vwJ2q2bNZSM3R2iIMwnp0vJ4+P0f5+F\\u000a" + "Q6bL51I8/InAJmcApBBpcaPESV1X3GIZUcrGwfOm6ycwF1wlKrD/pqOvRiGFEoH6\\u000a" + "YNOiosMN\\u000a" + "=xuj5\\u000a" + "-----END PGP PUBLIC KEY BLOCK-----\\u000a" + "\"]}]}";
            final String newid = mailfilternew(login, getHostname(), getUsername(), test, null);
            System.out.println("Rule created with newid: " + newid);
        } finally {
            logout(login);
        }
    }

    @Test
    public void MailfilternewVacationTest() throws MalformedURLException, IOException, SAXException, JSONException {
        final WebconversationAndSessionID login = login();
        try {
            final String test = "{\"rulename\":\"Vacation Notice\",\"active\":true,\"flags\":[\"vacation\"],\"test\":{\"id\":\"true\"},\"actioncmds\":[{\"id\":\"vacation\",\"days\":13,\"addresses\":[\"root@localhost\",\"billg@microsoft.com\"],\"subject\":\"Betreff\",\"text\":\"Text\\u000aText\"}]}";
            final String newid = mailfilternew(login, getHostname(), getUsername(), test, null);
            System.out.println("Rule created with newid: " + newid);
            mailfilterdelete(login, getHostname(), getUsername(), Integer.parseInt(newid));
        } finally {
            logout(login);
        }
    }

    /**
     * This test case is used for testing the bug that the subject was written as text
     *
     * @throws MalformedURLException
     * @throws IOException
     * @throws SAXException
     * @throws JSONException
     */
    @Test
    public void MailfilternewVacation2Test() throws MalformedURLException, IOException, SAXException, JSONException {
        final WebconversationAndSessionID login = login();
        try {
            final String test = "{\"rulename\":\"Vacation Notice\",\"active\":true,\"flags\":[\"vacation\"],\"test\":{\"id\":\"true\"},\"actioncmds\":[{\"id\":\"vacation\",\"days\":13,\"addresses\":[\"root@localhost\",\"billg@microsoft.com\"],\"subject\":\"Betreff\",\"text\":\"Text\\u000aText\"}],\"id\":5}";
            final String newid = mailfilternew(login, getHostname(), getUsername(), test, null);
            System.out.println("Rule created with newid: " + newid);
            mailfilterdelete(login, getHostname(), getUsername(), Integer.parseInt(newid));
        } finally {
            logout(login);
        }
    }

    @Test
    public void MailfilternewVacation3Test() throws MalformedURLException, IOException, SAXException, JSONException {
        final WebconversationAndSessionID login = login();
        try {
            final String test = "{\"rulename\":\"New x\",\"test\":{\"id\":\"header\",\"comparison\":\"contains\",\"values\":[\"\"],\"headers\":[\"X-Been-There\",\"X-Mailinglist\"]},\"actioncmds\":[{\"id\":\"redirect\",\"to\":\"xyz@bla.de\"}],\"flags\":[],\"active\":true}";
            final String newid = mailfilternew(login, getHostname(), getUsername(), test, null);
            System.out.println("Rule created with newid: " + newid);
            mailfilterdelete(login, getHostname(), getUsername(), Integer.parseInt(newid));
        } finally {
            logout(login);
        }
    }

    @Test
    public void MailfilternewVacationDeactiveAtTheEndTest() throws MalformedURLException, IOException, SAXException, JSONException {
        final WebconversationAndSessionID login = login();
        try {
            final String test = "{\"rulename\":\"Vacation Notice\",\"active\":false,\"flags\":[\"vacation\"],\"test\":{\"id\":\"true\"},\"actioncmds\":[{\"id\":\"vacation\",\"days\":1,\"addresses\":[\"dsfa\"],\"subject\":\"123\",\"text\":\"123\"}]}";
            final String newid = mailfilternew(login, getHostname(), getUsername(), test, null);
            System.out.println("Rule created with newid: " + newid);
            mailfilterdelete(login, getHostname(), getUsername(), Integer.parseInt(newid));
        } finally {
            logout(login);
        }
    }

    @Test
    public void MailfilternewVacationDeactiveInBetweenTest() throws MalformedURLException, IOException, SAXException, JSONException {
        final WebconversationAndSessionID login = login();
        try {
            final String test = "{\"rulename\":\"Vacation Notice\",\"position\":0,\"active\":false,\"flags\":[\"vacation\"],\"test\":{\"id\":\"true\"},\"actioncmds\":[{\"id\":\"vacation\",\"days\":1,\"addresses\":[\"dsfa\"],\"subject\":\"123\",\"text\":\"123\"}]}";
            final String newid = mailfilternew(login, getHostname(), getUsername(), test, null);
            System.out.println("Rule created with newid: " + newid);
            mailfilterdelete(login, getHostname(), getUsername(), Integer.parseInt(newid));
        } finally {
            logout(login);
        }
    }

    /**
     * This test is used to check the correct operation of the size test, this was dealt in bug 11519
     * 
     * @throws MalformedURLException
     * @throws IOException
     * @throws SAXException
     * @throws JSONException
     * @deprecated Use {@link NewTest#testNewSize()} instead
     */
    @Test
    public void MailfilternewSizeTest() throws MalformedURLException, IOException, SAXException, JSONException {
        final WebconversationAndSessionID login = login();
        try {
            final String test = "{\"rulename\":\"sizerule\",\"test\":{\"id\":\"size\",\"comparison\":\"over\",\"size\":88},\"actioncmds\":[{\"id\":\"keep\"}],\"flags\":[],\"active\":true}";
            final String newid = mailfilternew(login, getHostname(), getUsername(), test, null);
            System.out.println("Rule created with newid: " + newid);
            mailfilterdelete(login, getHostname(), getUsername(), Integer.parseInt(newid));
        } finally {
            logout(login);
        }
    }

    /**
     * This test is used to check the correct operation of the currentdate test, this was dealt in bug 11519
     * 
     * @throws MalformedURLException
     * @throws IOException
     * @throws SAXException
     * @throws JSONException
     */
    @Test
    public void MailfilternewCurrentDateTest() throws MalformedURLException, IOException, SAXException, JSONException {
        final WebconversationAndSessionID login = login();
        try {
            final JSONObject base = new JSONObject();
            base.put("rulename", "sizerule");
            base.put("active", Boolean.TRUE);
            base.append("flags", "vacation");
            final JSONObject test = new JSONObject();
            test.put("id", "allof");
            test.append("tests", currentdate(1183759200000L, "ge", CurrentDate.date));
            test.append("tests", currentdate(1183759200000L, "le", CurrentDate.date));
            test.append("tests", currentdate(1183759200000L, "is", CurrentDate.date));
            base.put("test", test);
            final JSONObject action = new JSONObject();
            //            action.put("id", "keep");
            action.put("id", "vacation");
            action.put("days", 7);
            action.append("addresses", "dennis.sieben@open-xchange.com");
            action.put("text", "I'm out of office");
            base.append("actioncmds", action);

            final String newid = mailfilternew(login, getHostname(), getUsername(), base.toString(), null);
            System.out.println("Rule created with newid: " + newid);
            mailfilterdelete(login, getHostname(), getUsername(), Integer.parseInt(newid));
        } finally {
            logout(login);
        }
    }

    private JSONObject currentdate(long date, String comparison, CurrentDate cd) throws JSONException {
        final JSONObject currentdate2 = new JSONObject();
        currentdate2.put("id", "currentdate");
        currentdate2.put("comparison", comparison);
        currentdate2.append("datevalue", date);
        currentdate2.put("datepart", cd.toString());
        return currentdate2;
    }

    /**
     * @deprecated Use {@link NewTest#testNewMissingHeaders()} instead
     */
    @Test
    public void MailfilternewTestMissingHeaders() throws MalformedURLException, IOException, SAXException, JSONException {
        final WebconversationAndSessionID login = login();
        try {
            final String test = "{\"active\":true,\"position\":0,\"flags\":[],\"actioncmds\":[{\"into\":\"INBOX/Spam\",\"id\":\"move\"},{\"id\":\"stop\"}],\"id\":0,\"test\":{\"tests\":[{\"values\":[\"zitate.at\"],\"comparison\":\"user\",\"id\":\"address\"},{\"headers\":[\"subject\"],\"values\":[\"Zitat des Tages\"],\"comparison\":\"contains\",\"id\":\"header\"}],\"id\":\"allof\"},\"rulename\":\"\"}";
            final String newid = mailfilternew(login, getHostname(), getUsername(), test, "Exception while parsing JSON: \"Error while reading TestCommand address: JSONObject[\"headers\"] not found.\".");
            mailfilterdelete(login, getHostname(), getUsername(), Integer.parseInt(newid));
        } finally {
            logout(login);
        }
    }

    @Test
    public void MailfilternewTestWithoutPosition() throws MalformedURLException, IOException, SAXException, JSONException {
        final WebconversationAndSessionID login = login();
        try {
            final String test = "{\"active\":true,\"flags\":[],\"actioncmds\":[{\"into\":\"INBOX/Spam\",\"id\":\"move\"},{\"id\":\"stop\"}],\"id\":0,\"test\":{\"tests\":[{\"headers\":[\"from\"],\"values\":[\"zitate.at\"],\"comparison\":\"user\",\"id\":\"address\"},{\"headers\":[\"subject\"],\"values\":[\"Zitat des Tages\"],\"comparison\":\"contains\",\"id\":\"header\"}],\"id\":\"allof\"},\"rulename\":\"\"}";
            final String newid = mailfilternew(login, getHostname(), getUsername(), test, null);
            mailfilterupdate(login, getHostname(), getUsername(), test);
            mailfilterdelete(login, getHostname(), getUsername(), Integer.parseInt(newid));
        } finally {
            logout(login);
        }
    }

    @Test
    public void MailfilterreorderTest() throws MalformedURLException, IOException, SAXException, JSONException {
        final WebconversationAndSessionID login = login();
        try {
            final String test = "[5,7,8]";
            mailfilterreorder(login, getHostname(), getUsername(), test);
        } finally {
            logout(login);
        }
    }

    @Test
    public void MailfilterupdateTest() throws MalformedURLException, IOException, SAXException, JSONException {
        final WebconversationAndSessionID login = login();
        try {
            final String test = "{\"active\":true,\"position\":0,\"flags\":[],\"id\":7,\"rulename\":\"testrule\"}";
            mailfilterupdate(login, getHostname(), getUsername(), test);
        } finally {
            logout(login);
        }
    }

    @Test
    public void MailfilterupdateTest2() throws MalformedURLException, IOException, SAXException, JSONException {
        final WebconversationAndSessionID login = login();
        try {
            final String test = "{\"active\":false,\"position\":0,\"flags\":[],\"id\":7,\"test\":{\"tests\":[{\"headers\":[\"from\"],\"values\":[\"zitate.at\"],\"comparison\":\"contains\",\"id\":\"header\"}],\"id\":\"allof\"},\"rulename\":\"\"}";
            mailfilterupdate(login, getHostname(), getUsername(), test);
        } finally {
            logout(login);
        }
    }

    @Test
    public void MailfiltergetScriptTest() throws MalformedURLException, IOException, SAXException, JSONException {
        final WebconversationAndSessionID login = login();
        try {
            mailfiltergetScript(login, getHostname(), getUsername());
        } finally {
            logout(login);
        }
    }

    @Test
    public void MailfilterdeleteScriptTest() throws MalformedURLException, IOException, SAXException, JSONException {
        final WebconversationAndSessionID login = login();
        try {
            mailfilterdeleteScript(login, getHostname(), getUsername());
        } finally {
            logout(login);
        }
    }

    protected abstract String getHostname();

    protected abstract String getUsername();

    protected abstract WebconversationAndSessionID login() throws MalformedURLException, IOException, SAXException, JSONException;

    private void logout(final WebconversationAndSessionID conversation) throws MalformedURLException, IOException, SAXException, JSONException {
        final WebRequest req = new GetMethodWebRequest(PROTOCOL + getHostname() + LOGOUT_URL);
        req.setParameter("action", "logout");
        req.setParameter("session", conversation.getSessionid());
        final WebResponse resp = conversation.getWebConversation().getResponse(req);
        Assert.assertEquals(200, resp.getResponseCode());
    }

    private void setSessionParameter(final WebconversationAndSessionID conversation, final WebRequest reqmailfilter) {
        reqmailfilter.setParameter(AJAXServlet.PARAMETER_SESSION, conversation.getSessionid());
    }

    /**
     * @param conversation
     * @param hostname
     * @param username
     * @param number The id of the rule which should be deleted
     * @throws MalformedURLException
     * @throws IOException
     * @throws SAXException
     * @throws JSONException
     */
    private void mailfilterdelete(final WebconversationAndSessionID conversation, final String hostname, final String username, int number) throws MalformedURLException, IOException, SAXException, JSONException {
        final JSONObject object = new JSONObject();
        object.put("id", number);
        final byte[] bytes = object.toString().getBytes(com.openexchange.java.Charsets.UTF_8);
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        final WebRequest reqmailfilter;
        if (null != username) {
            reqmailfilter = new PutMethodWebRequest(PROTOCOL + hostname + MAILFILTER_URL + "?action=delete&session=" + conversation.getSessionid() + "&username=" + username, bais, "text/javascript; charset=UTF-8");
        } else {
            reqmailfilter = new PutMethodWebRequest(PROTOCOL + hostname + MAILFILTER_URL + "?action=delete&session=" + conversation.getSessionid(), bais, "text/javascript; charset=UTF-8");
        }
        final WebResponse mailfilterresp = conversation.getWebConversation().getResponse(reqmailfilter);
        Assert.assertEquals(200, mailfilterresp.getResponseCode());
    }

    private void mailfilterlist(final WebconversationAndSessionID conversation, final String hostname, final String username) throws MalformedURLException, IOException, SAXException, JSONException {
        String body;
        final WebRequest reqmailfilter = new GetMethodWebRequest(PROTOCOL + hostname + MAILFILTER_URL);
        reqmailfilter.setParameter("action", "list");
        //        reqmailfilter.setParameter(AJAXServlet.PARAMETER_SESSION, conversation.);
        if (null != username) {
            reqmailfilter.setParameter("username", username);
        }
        //        reqmailfilter.setParameter("flag", "AdminFlag");
        setSessionParameter(conversation, reqmailfilter);
        final WebResponse mailfilterresp = conversation.getWebConversation().getResponse(reqmailfilter);
        body = mailfilterresp.getText();
        final JSONObject json;
        try {
            json = new JSONObject(body);
        } catch (JSONException e) {
            System.out.println("Can't parse this body to JSON: \"" + body + '\"');
            throw e;
        }
        assertFalse(String.format(json.optString("error"), json.optJSONArray("error_params")), json.has("error"));
        System.out.println("Rules:");
        System.out.println("------");
        final JSONArray testJsonArray = json.getJSONArray("data");
        for (int i = 0; i < testJsonArray.length(); i++) {
            System.out.println(testJsonArray.getJSONObject(i));
            System.out.println("Test: " + testJsonArray.getJSONObject(i).getJSONObject("test"));
            System.out.println("--------------");
        }
    }

    private void mailfilterconfig(final WebconversationAndSessionID conversation, final String hostname, final String username) throws MalformedURLException, IOException, SAXException, JSONException {
        String body;
        final WebRequest reqmailfilter = new GetMethodWebRequest(PROTOCOL + hostname + MAILFILTER_URL);
        reqmailfilter.setParameter("action", "config");
        //        reqmailfilter.setParameter(AJAXServlet.PARAMETER_SESSION, conversation.);
        if (null != username) {
            reqmailfilter.setParameter("username", username);
        }
        //        reqmailfilter.setParameter("flag", "AdminFlag");
        setSessionParameter(conversation, reqmailfilter);
        final WebResponse mailfilterresp = conversation.getWebConversation().getResponse(reqmailfilter);
        body = mailfilterresp.getText();
        final JSONObject json;
        try {
            json = new JSONObject(body);
        } catch (JSONException e) {
            System.out.println("Can't parse this body to JSON: \"" + body + '\"');
            throw e;
        }
        assertFalse(String.format(json.optString("error"), json.opt("error_params")), json.has("error"));
        System.out.println("Tests:");
        System.out.println("------");
        final JSONArray testJsonArray = json.getJSONObject("data").getJSONArray("tests");
        for (int i = 0; i < testJsonArray.length(); i++) {
            System.out.println(testJsonArray.getJSONObject(i));
        }
        System.out.println("Actioncommands:" + json.getJSONObject("data").getJSONArray("actioncommands"));
    }

    private String mailfilternew(final WebconversationAndSessionID conversation, final String hostname, final String username, String jsonString, String errorfound) throws MalformedURLException, IOException, SAXException, JSONException {
        final byte[] bytes = jsonString.getBytes(com.openexchange.java.Charsets.UTF_8);
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        final WebRequest reqmailfilter;
        if (null != username) {
            reqmailfilter = new PutMethodWebRequest(PROTOCOL + hostname + MAILFILTER_URL + "?action=new&username=" + username + "&session=" + conversation.getSessionid(), bais, "text/javascript; charset=UTF-8");
        } else {
            reqmailfilter = new PutMethodWebRequest(PROTOCOL + hostname + MAILFILTER_URL + "?action=new&session=" + conversation.getSessionid(), bais, "text/javascript; charset=UTF-8");
        }
        final WebResponse mailfilterresp = conversation.getWebConversation().getResponse(reqmailfilter);
        final String body = mailfilterresp.getText();
        final JSONObject json;
        try {
            json = new JSONObject(body);
        } catch (JSONException e) {
            System.out.println("Can't parse this body to JSON: \"" + body + '\"');
            throw e;
        }
        if (null != errorfound) {
            assertTrue("No error desc", json.has("error_desc"));
            assertTrue("The given error string: " + errorfound + " was not found in the error desc", json.optString("error_desc").contains(errorfound));
            return null;
        } else {
            assertFalse(String.format(json.optString("error"), json.opt("error_params")), json.has("error"));
            return json.getString("data");
        }
    }

    private void mailfilterreorder(final WebconversationAndSessionID conversation, final String hostname, final String username, String jsonArray) throws MalformedURLException, IOException, SAXException, JSONException {
        final byte[] bytes = jsonArray.getBytes(com.openexchange.java.Charsets.UTF_8);
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        final WebRequest reqmailfilter;
        if (null != username) {
            reqmailfilter = new PutMethodWebRequest(PROTOCOL + hostname + MAILFILTER_URL + "?action=new&username=" + username, bais, "text/javascript; charset=UTF-8");
        } else {
            reqmailfilter = new PutMethodWebRequest(PROTOCOL + hostname + MAILFILTER_URL + "?action=reorder", bais, "text/javascript; charset=UTF-8");
        }
        setSessionParameter(conversation, reqmailfilter);
        final WebResponse mailfilterresp = conversation.getWebConversation().getResponse(reqmailfilter);
        final String body = mailfilterresp.getText();
        final JSONObject json;
        try {
            json = new JSONObject(body);
        } catch (JSONException e) {
            System.out.println("Can't parse this body to JSON: \"" + body + '\"');
            throw e;
        }
        assertFalse(String.format(json.optString("error"), json.opt("error_params")), json.has("error"));
        System.out.println(json);
    }

    private void mailfilterupdate(final WebconversationAndSessionID conversation, final String hostname, final String username, String test) throws MalformedURLException, IOException, SAXException, JSONException {
        final byte[] bytes = test.getBytes(com.openexchange.java.Charsets.UTF_8);
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        final WebRequest reqmailfilter;
        if (null != username) {
            reqmailfilter = new PutMethodWebRequest(PROTOCOL + hostname + MAILFILTER_URL + "?action=update&username=" + username, bais, "text/javascript; charset=UTF-8");
        } else {
            reqmailfilter = new PutMethodWebRequest(PROTOCOL + hostname + MAILFILTER_URL + "?action=update", bais, "text/javascript; charset=UTF-8");
        }
        setSessionParameter(conversation, reqmailfilter);
        final WebResponse mailfilterresp = conversation.getWebConversation().getResponse(reqmailfilter);
        final String body = mailfilterresp.getText();
        final JSONObject json;
        try {
            json = new JSONObject(body);
        } catch (JSONException e) {
            System.out.println("Can't parse this body to JSON: \"" + body + '\"');
            throw e;
        }
        assertFalse(String.format(json.optString("error"), json.opt("error_params")), json.has("error"));
        System.out.println(json);
    }

    private void mailfiltergetScript(final WebconversationAndSessionID conversation, final String hostname, final String username) throws MalformedURLException, IOException, SAXException, JSONException {
        String body;
        final WebRequest reqmailfilter = new GetMethodWebRequest(PROTOCOL + hostname + MAILFILTER_URL);
        reqmailfilter.setParameter("action", "getscript");
        if (null != username) {
            reqmailfilter.setParameter("username", username);
        }
        //        reqmailfilter.setParameter("flag", "AdminFlag");
        setSessionParameter(conversation, reqmailfilter);
        final WebResponse mailfilterresp = conversation.getWebConversation().getResponse(reqmailfilter);
        body = mailfilterresp.getText();
        final JSONObject json;
        try {
            json = new JSONObject(body);
        } catch (JSONException e) {
            System.out.println("Can't parse this body to JSON: \"" + body + '\"');
            throw e;
        }
        assertFalse(String.format(json.optString("error"), json.opt("error_params")), json.has("error"));
        System.out.println(json);
    }

    private void mailfilterdeleteScript(final WebconversationAndSessionID conversation, final String hostname, final String username) throws MalformedURLException, IOException, SAXException, JSONException {
        String body;
        final WebRequest reqmailfilter = new GetMethodWebRequest(PROTOCOL + hostname + MAILFILTER_URL);
        reqmailfilter.setParameter("action", "deletescript");
        if (null != username) {
            reqmailfilter.setParameter("username", username);
        }
        //        reqmailfilter.setParameter("flag", "AdminFlag");
        setSessionParameter(conversation, reqmailfilter);
        final WebResponse mailfilterresp = conversation.getWebConversation().getResponse(reqmailfilter);
        body = mailfilterresp.getText();
        final JSONObject json;
        try {
            json = new JSONObject(body);
        } catch (JSONException e) {
            System.out.println("Can't parse this body to JSON: \"" + body + '\"');
            throw e;
        }
        assertFalse(String.format(json.optString("error"), json.opt("error_params")), json.has("error"));
        System.out.println(json);
    }

    @Test
    public void testWeekDayField() throws MalformedURLException, IOException, SAXException, JSONException {
        final WebconversationAndSessionID login = login();
        try {
            final JSONObject rule = new JSONObject();
            rule.put("rulename", "weekday rule");
            rule.put("active", Boolean.TRUE);
            rule.append("flags", "vacation");
            final JSONObject test = new JSONObject();
            test.put("id", "allof");
            test.append("tests", currentdate(3, "is", CurrentDate.weekday));
            rule.put("test", test);
            final JSONObject action = new JSONObject();
            action.put("id", "vacation");
            action.put("days", 7);
            action.append("addresses", "foo@invalid.tld");
            action.put("text", "I'm out of office");
            rule.append("actioncmds", action);

            final String newid = mailfilternew(login, getHostname(), getUsername(), rule.toString(), null);
            System.out.println("Rule created with newid: " + newid);
            mailfilterdelete(login, getHostname(), getUsername(), Integer.parseInt(newid));
        } finally {
            logout(login);
        }
    }

    @Test
    public void testDateField() throws MalformedURLException, IOException, SAXException, JSONException {
        final WebconversationAndSessionID login = login();
        try {
            final JSONObject rule = new JSONObject();
            rule.put("rulename", "time rule");
            rule.put("active", Boolean.TRUE);
            rule.append("flags", "vacation");
            final JSONObject test = new JSONObject();
            test.put("id", "allof");
            test.append("tests", currentdate(3627279000000L, "is", CurrentDate.time));
            rule.put("test", test);
            final JSONObject action = new JSONObject();
            action.put("id", "vacation");
            action.put("days", 7);
            action.append("addresses", "foo@invalid.tld");
            action.put("text", "I'm out of office");
            rule.append("actioncmds", action);

            final String newid = mailfilternew(login, getHostname(), getUsername(), rule.toString(), null);
            System.out.println("Rule created with newid: " + newid);
            mailfilterdelete(login, getHostname(), getUsername(), Integer.parseInt(newid));
        } finally {
            logout(login);
        }
    }
}
