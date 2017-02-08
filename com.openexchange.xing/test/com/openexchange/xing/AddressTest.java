package com.openexchange.xing;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;


/**
 * {@link AddressTest}
 * 
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.6.0
 */
public class AddressTest {

    private String empty = "{\"street\":null,\"zip_code\":null,\"city\":null,\"province\":null,\"country\":null,\"email\":null,\"fax\":null,\"phone\":null,\"mobile_phone\":null}";

    private String filledNothingInfrontOfCountryCode = "{\"street\":\"Karl-Wilhelma 99\",\"zip_code\":\"77777\",\"city\":\"Untereschbach\",\"province\":\"Nordrhein-Westfalen\",\"country\":\"DE\",\"email\":\"anton@zuhause.de\",\"fax\":null,\"phone\":\"49|771|12 34 56 1-0\",\"mobile_phone\":\"49|100|012345678\"}";

    private String filledPlusExistend = "{\"street\":\"Zullicher Strasse 1\",\"zip_code\":\"33333\",\"city\":\"Gross-Kleinostheim\",\"province\":\"Nordrhein-Westfalen\",\"country\":\"DE\",\"email\":\"huhu@huphup.hup\",\"fax\":\"+49|111|111111-1111\",\"phone\":\"+49|111|22222-2222\",\"mobile_phone\":\"+49|170|87654321\"}";

    private String filledZeroZeroExistend = "{\"street\":\"Zullicher Strasse 1\",\"zip_code\":\"33333\",\"city\":\"Gross-Kleinostheim\",\"province\":\"Nordrhein-Westfalen\",\"country\":\"DE\",\"email\":\"huhu@huphup.hup\",\"fax\":\"0049|111|111111-1111\",\"phone\":\"0049|111|22222-2222\",\"mobile_phone\":\"0049|170|87654321\"}";

     @Test
     public void testConstructor_JSONwithNullValues_stillNullAfterSanitizing() throws JSONException {
        JSONObject addressInformation = new JSONObject(empty);

        Address address = new Address(addressInformation);

        Assert.assertEquals(null, address.getMobilePhone());
        Assert.assertEquals(null, address.getFax());
        Assert.assertEquals(null, address.getPhone());
    }

     @Test
     public void testConstructor_noLeadingZeroZeroOrPlus_setCorrectlyAfterSanitizing() throws JSONException {
        JSONObject addressInformation = new JSONObject(filledNothingInfrontOfCountryCode);

        Address address = new Address(addressInformation);

        Assert.assertEquals("+49|100|012345678", address.getMobilePhone());
        Assert.assertEquals(null, address.getFax());
        Assert.assertEquals("+49|771|12 34 56 1-0", address.getPhone());
        Assert.assertEquals("Untereschbach", address.getCity());
        Assert.assertEquals("DE", address.getCountry());
        Assert.assertEquals("anton@zuhause.de", address.getEmail());
        Assert.assertEquals("Karl-Wilhelma 99", address.getStreet());
    }

     @Test
     public void testConstructor_leadingPlusExistend_setCorrectlyAfterSanitzing() throws JSONException {
        JSONObject addressInformation = new JSONObject(filledPlusExistend);

        Address address = new Address(addressInformation);

        Assert.assertEquals("+49|170|87654321", address.getMobilePhone());
        Assert.assertEquals("+49|111|111111-1111", address.getFax());
        Assert.assertEquals("+49|111|22222-2222", address.getPhone());
        Assert.assertEquals("Gross-Kleinostheim", address.getCity());
        Assert.assertEquals("DE", address.getCountry());
        Assert.assertEquals("huhu@huphup.hup", address.getEmail());
        Assert.assertEquals("Zullicher Strasse 1", address.getStreet());
    }

     @Test
     public void testConstructor_leadingZeroZeroExistent_setCorrectlyAfterSanitizing() throws JSONException {
        JSONObject addressInformation = new JSONObject(filledZeroZeroExistend);

        Address address = new Address(addressInformation);

        Assert.assertEquals("0049|170|87654321", address.getMobilePhone());
        Assert.assertEquals("0049|111|111111-1111", address.getFax());
        Assert.assertEquals("0049|111|22222-2222", address.getPhone());
        Assert.assertEquals("Gross-Kleinostheim", address.getCity());
        Assert.assertEquals("DE", address.getCountry());
        Assert.assertEquals("huhu@huphup.hup", address.getEmail());
        Assert.assertEquals("Zullicher Strasse 1", address.getStreet());
    }
}
