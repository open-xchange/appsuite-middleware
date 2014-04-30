
package com.openexchange.mailaccount.json.parser;

import java.util.HashSet;
import java.util.Set;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import com.openexchange.mailaccount.Attribute;
import com.openexchange.mailaccount.MailAccountDescription;
import com.openexchange.mailaccount.json.fields.MailAccountFields;

/**
 * {@link MailAccountParserTest}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.6.0
 */
public class MailAccountParserTest {

    private MailAccountDescription mailAccountDescription;

    private final String transportLogin = "ewaldbartkowiak@transport.com";

    private final String transportPassword = "myTransportPassword";

    private final String imapLogin = "ewaldbartkowiak@gmail.com";

    private final String imapPassword = "myPassword";

    private Set<Attribute> attributes;

    private JSONObject json;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        this.attributes = new HashSet<Attribute>();

        this.mailAccountDescription = new MailAccountDescription();
        this.mailAccountDescription.setId(-1);
        this.mailAccountDescription.setLogin(imapLogin);
        this.mailAccountDescription.setPassword(imapPassword);
        this.mailAccountDescription.addTransportProperty(MailAccountFields.TRANSPORT_AUTH, Boolean.toString(true));
    }

    @Test
    public void testParseTransportCredentials_transportAuthAndTransportPasswordNotProvided_setImapLoginAndPassword() throws Exception {
        json = new JSONObject("{\"unified_inbox_enabled\":false,\"transport_credentials\":false,\"login\":\"ewaldbartkowiak@gmail.com\",\"transport_login\":\"ewaldbartkowiak@transport.com\",\"mail_server\":\"imap.googlemail.com\",\"transport_server\":\"smtp.googlemail.com\",\"mail_port\":993,\"transport_port\":465,\"mail_protocol\":\"imap\",\"transport_protocol\":\"smtp\",\"mail_secure\":true,\"transport_secure\":true,\"config_source\":\"ISPDB\",\"primary_address\":\"ewaldbartkowiak@gmail.com\",\"password\":\"myPassword\",\"personal\":\"ewaldbartkowiak@gmail.com\",\"name\":\"ewaldbartkowiak@gmail.com\"}");

        MailAccountParser.getInstance().parseTransportCredentials(mailAccountDescription, json, attributes);

        Assert.assertEquals(transportLogin, mailAccountDescription.getTransportLogin());
        Assert.assertEquals(null, mailAccountDescription.getTransportPassword());
        Assert.assertEquals(2, attributes.size());
    }

    @Test
    public void testParseTransportCredentials_transportAuthAndTransportLoginNotProvided_setImapLoginAndPassword() throws Exception {
        json = new JSONObject("{\"unified_inbox_enabled\":false,\"transport_credentials\":false,\"login\":\"ewaldbartkowiak@gmail.com\",\"transport_password\":\"myTransportPassword\",\"mail_server\":\"imap.googlemail.com\",\"transport_server\":\"smtp.googlemail.com\",\"mail_port\":993,\"transport_port\":465,\"mail_protocol\":\"imap\",\"transport_protocol\":\"smtp\",\"mail_secure\":true,\"transport_secure\":true,\"config_source\":\"ISPDB\",\"primary_address\":\"ewaldbartkowiak@gmail.com\",\"password\":\"myPassword\",\"personal\":\"ewaldbartkowiak@gmail.com\",\"name\":\"ewaldbartkowiak@gmail.com\"}");

        MailAccountParser.getInstance().parseTransportCredentials(mailAccountDescription, json, attributes);

        Assert.assertEquals(null, mailAccountDescription.getTransportLogin());
        Assert.assertEquals(transportPassword, mailAccountDescription.getTransportPassword());
        Assert.assertEquals(2, attributes.size());
    }

    @Test
    public void testParseTransportCredentials_transportAuthNotProvidedButTransportLoginAndPasswordAppropriate_setTransportLoginAndPassword() throws Exception {
        json = new JSONObject("{\"unified_inbox_enabled\":false,\"transport_credentials\":false,\"login\":\"ewaldbartkowiak@gmail.com\",\"transport_login\":\"ewaldbartkowiak@transport.com\",\"transport_password\":\"myTransportPassword\",\"mail_server\":\"imap.googlemail.com\",\"transport_server\":\"smtp.googlemail.com\",\"mail_port\":993,\"transport_port\":465,\"mail_protocol\":\"imap\",\"transport_protocol\":\"smtp\",\"mail_secure\":true,\"transport_secure\":true,\"config_source\":\"ISPDB\",\"primary_address\":\"ewaldbartkowiak@gmail.com\",\"password\":\"myPassword\",\"personal\":\"ewaldbartkowiak@gmail.com\",\"name\":\"ewaldbartkowiak@gmail.com\"}");

        MailAccountParser.getInstance().parseTransportCredentials(mailAccountDescription, json, attributes);

        Assert.assertEquals(transportLogin, mailAccountDescription.getTransportLogin());
        Assert.assertEquals(transportPassword, mailAccountDescription.getTransportPassword());
        Assert.assertEquals(2, attributes.size());
    }

    @Test
    public void testParseTransportCredentials_transportAuthFalseTransportPasswordAndLoginNotProvided_setNullForLoginAndPassword() throws Exception {
        json = new JSONObject("{\"unified_inbox_enabled\":false,\"transport_credentials\":false,\"login\":\"ewaldbartkowiak@gmail.com\",\"transport_auth\":\"false\",\"mail_server\":\"imap.googlemail.com\",\"transport_server\":\"smtp.googlemail.com\",\"mail_port\":993,\"transport_port\":465,\"mail_protocol\":\"imap\",\"transport_protocol\":\"smtp\",\"mail_secure\":true,\"transport_secure\":true,\"config_source\":\"ISPDB\",\"primary_address\":\"ewaldbartkowiak@gmail.com\",\"password\":\"myPassword\",\"personal\":\"ewaldbartkowiak@gmail.com\",\"name\":\"ewaldbartkowiak@gmail.com\"}");

        MailAccountParser.getInstance().parseTransportCredentials(mailAccountDescription, json, attributes);

        Assert.assertEquals(null, mailAccountDescription.getTransportLogin());
        Assert.assertEquals(null, mailAccountDescription.getTransportPassword());
        Assert.assertEquals(2, attributes.size());
    }

    @Test
    public void testParseTransportCredentials_transportAuthFalseTransportPasswordAndLoginEmptyString_setEmptyStringForLoginAndPassword() throws Exception {
        json = new JSONObject("{\"unified_inbox_enabled\":false,\"transport_credentials\":false,\"login\":\"ewaldbartkowiak@gmail.com\",\"transport_login\":\"\",\"transport_password\":\"\",\"transport_auth\":\"false\",\"mail_server\":\"imap.googlemail.com\",\"transport_server\":\"smtp.googlemail.com\",\"mail_port\":993,\"transport_port\":465,\"mail_protocol\":\"imap\",\"transport_protocol\":\"smtp\",\"mail_secure\":true,\"transport_secure\":true,\"config_source\":\"ISPDB\",\"primary_address\":\"ewaldbartkowiak@gmail.com\",\"password\":\"myPassword\",\"personal\":\"ewaldbartkowiak@gmail.com\",\"name\":\"ewaldbartkowiak@gmail.com\"}");

        MailAccountParser.getInstance().parseTransportCredentials(mailAccountDescription, json, attributes);

        Assert.assertEquals(null, mailAccountDescription.getTransportLogin());
        Assert.assertEquals(null, mailAccountDescription.getTransportPassword());
        Assert.assertEquals(2, attributes.size());
    }

    @Test
    public void testParseTransportCredentials_transportAuthFalseTransportPasswordAndLoginSet_setProvudedForLoginAndPassword() throws Exception {
        json = new JSONObject("{\"unified_inbox_enabled\":false,\"transport_credentials\":false,\"login\":\"ewaldbartkowiak@gmail.com\",\"transport_login\":\"ewaldbartkowiak@transport.com\",\"transport_password\":\"myTransportPassword\",\"transport_auth\":\"false\",\"mail_server\":\"imap.googlemail.com\",\"transport_server\":\"smtp.googlemail.com\",\"mail_port\":993,\"transport_port\":465,\"mail_protocol\":\"imap\",\"transport_protocol\":\"smtp\",\"mail_secure\":true,\"transport_secure\":true,\"config_source\":\"ISPDB\",\"primary_address\":\"ewaldbartkowiak@gmail.com\",\"password\":\"myPassword\",\"personal\":\"ewaldbartkowiak@gmail.com\",\"name\":\"ewaldbartkowiak@gmail.com\"}");

        MailAccountParser.getInstance().parseTransportCredentials(mailAccountDescription, json, attributes);

        Assert.assertEquals(null, mailAccountDescription.getTransportLogin());
        Assert.assertEquals(null, mailAccountDescription.getTransportPassword());
        Assert.assertEquals(2, attributes.size());
    }

    @Test
    public void testParseTransportCredentials_transportAuthTrueTransportPasswordAndLoginNotProvided_useImapCredentials() throws Exception {
        json = new JSONObject("{\"unified_inbox_enabled\":false,\"transport_credentials\":false,\"login\":\"ewaldbartkowiak@gmail.com\",\"transport_auth\":\"true\",\"mail_server\":\"imap.googlemail.com\",\"transport_server\":\"smtp.googlemail.com\",\"mail_port\":993,\"transport_port\":465,\"mail_protocol\":\"imap\",\"transport_protocol\":\"smtp\",\"mail_secure\":true,\"transport_secure\":true,\"config_source\":\"ISPDB\",\"primary_address\":\"ewaldbartkowiak@gmail.com\",\"password\":\"myPassword\",\"personal\":\"ewaldbartkowiak@gmail.com\",\"name\":\"ewaldbartkowiak@gmail.com\"}");

        MailAccountParser.getInstance().parseTransportCredentials(mailAccountDescription, json, attributes);

        Assert.assertEquals(imapLogin, mailAccountDescription.getTransportLogin());
        Assert.assertEquals(imapPassword, mailAccountDescription.getTransportPassword());
        Assert.assertEquals(2, attributes.size());
    }

    @Test
    public void testParseTransportCredentials_transportAuthTrueTransportPasswordSet_useTransportCredentials() throws Exception {
        json = new JSONObject("{\"unified_inbox_enabled\":false,\"transport_credentials\":false,\"login\":\"ewaldbartkowiak@gmail.com\",\"transport_auth\":\"true\",\"transport_password\":\"myTransportPassword\",\"mail_server\":\"imap.googlemail.com\",\"transport_server\":\"smtp.googlemail.com\",\"mail_port\":993,\"transport_port\":465,\"mail_protocol\":\"imap\",\"transport_protocol\":\"smtp\",\"mail_secure\":true,\"transport_secure\":true,\"config_source\":\"ISPDB\",\"primary_address\":\"ewaldbartkowiak@gmail.com\",\"password\":\"myPassword\",\"personal\":\"ewaldbartkowiak@gmail.com\",\"name\":\"ewaldbartkowiak@gmail.com\"}");

        MailAccountParser.getInstance().parseTransportCredentials(mailAccountDescription, json, attributes);

        Assert.assertEquals(null, mailAccountDescription.getTransportLogin());
        Assert.assertEquals(transportPassword, mailAccountDescription.getTransportPassword());
        Assert.assertEquals(2, attributes.size());
    }

    @Test
    public void testParseTransportCredentials_transportAuthTrueTransportLoginSet_useTransportCredentials() throws Exception {
        json = new JSONObject("{\"unified_inbox_enabled\":false,\"transport_credentials\":false,\"login\":\"ewaldbartkowiak@gmail.com\",\"transport_auth\":\"true\",\"transport_login\":\"ewaldbartkowiak@transport.com\",\"mail_server\":\"imap.googlemail.com\",\"transport_server\":\"smtp.googlemail.com\",\"mail_port\":993,\"transport_port\":465,\"mail_protocol\":\"imap\",\"transport_protocol\":\"smtp\",\"mail_secure\":true,\"transport_secure\":true,\"config_source\":\"ISPDB\",\"primary_address\":\"ewaldbartkowiak@gmail.com\",\"password\":\"myPassword\",\"personal\":\"ewaldbartkowiak@gmail.com\",\"name\":\"ewaldbartkowiak@gmail.com\"}");

        MailAccountParser.getInstance().parseTransportCredentials(mailAccountDescription, json, attributes);

        Assert.assertEquals(transportLogin, mailAccountDescription.getTransportLogin());
        Assert.assertEquals(null, mailAccountDescription.getTransportPassword());
        Assert.assertEquals(2, attributes.size());
    }
}
