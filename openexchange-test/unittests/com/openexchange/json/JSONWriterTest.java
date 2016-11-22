
package com.openexchange.json;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

public class JSONWriterTest {

    @Test
    public void testEmptyObject() {
        final OXJSONWriter json = new OXJSONWriter();

        try {
            json.object();
            json.endObject();
            assertEquals("{}", json.getObject().toString());
        } catch (final JSONException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testObjectWithScalars() {
        final OXJSONWriter json = new OXJSONWriter();

        try {
            json.object();
            json.key("string");
            json.value("String");
            json.key("integer");
            json.value(23);
            json.key("float");
            json.value(3.14);
            json.key("boolean");
            json.value(true);
            json.endObject();
            final JSONObject object = (JSONObject) json.getObject();
            assertEquals("String", object.optString("string"));
            assertEquals(23, object.optLong("integer"));
            assertEquals(3.14, object.optDouble("float"));
            assertTrue(object.optBoolean("boolean"));

        } catch (final JSONException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testEmptyArray() {
        final OXJSONWriter json = new OXJSONWriter();

        try {
            json.array();
            json.endArray();
            assertEquals("[]", json.getObject().toString());
        } catch (final JSONException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testArrayWithScalars() {
        final OXJSONWriter json = new OXJSONWriter();

        try {
            json.array();
            json.value("string");
            json.value(23);
            json.value(3.14);
            json.value(true);
            json.endArray();
            final JSONArray arr = (JSONArray) json.getObject();
            assertEquals("string", arr.getString(0));
            assertEquals(23, arr.getLong(1));
            assertEquals(3.14, arr.getDouble(2));
            assertTrue(arr.getBoolean(3));

        } catch (final JSONException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testObjectWithObjects() {
        final OXJSONWriter json = new OXJSONWriter();

        try {
            json.object();
            json.key("object1");
            json.object();
            json.key("first");
            json.value("Tester");
            json.key("last");
            json.value("von Testingen");
            json.endObject();
            json.key("object2");
            json.object();
            json.key("first");
            json.value("Testerine");
            json.key("last");
            json.value("von Testeringen");
            json.endObject();
            json.endObject();
            final JSONObject object = json.isJSONObject() ? (JSONObject) json.getObject() : null;
            final JSONObject object1 = object.getJSONObject("object1");
            final JSONObject object2 = object.getJSONObject("object2");

            assertEquals("Tester", object1.optString("first"));
            assertEquals("von Testingen", object1.optString("last"));
            assertEquals("Testerine", object2.optString("first"));
            assertEquals("von Testeringen", object2.optString("last"));

        } catch (final JSONException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testObjectWithArrays() {
        final OXJSONWriter json = new OXJSONWriter();

        try {
            json.object();
            json.key("array1");
            json.array();
            json.value("Tester");
            json.value("von Testingen");
            json.endArray();
            json.key("array2");
            json.array();
            json.value("Testerine");
            json.value("von Testeringen");
            json.endArray();
            json.endObject();
            final JSONObject object = (JSONObject) json.getObject();
            final JSONArray arr1 = object.getJSONArray("array1");
            final JSONArray arr2 = object.getJSONArray("array2");

            assertEquals("Tester", arr1.getString(0));
            assertEquals("von Testingen", arr1.getString(1));
            assertEquals("Testerine", arr2.getString(0));
            assertEquals("von Testeringen", arr2.getString(1));

        } catch (final JSONException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    @Test
    public void testArrayWithObjects() {
        final OXJSONWriter json = new OXJSONWriter();

        try {
            json.array();
            json.object();
            json.key("first");
            json.value("Tester");
            json.key("last");
            json.value("von Testingen");
            json.endObject();
            json.object();
            json.key("first");
            json.value("Testerine");
            json.key("last");
            json.value("von Testeringen");
            json.endObject();
            json.endArray();
            final JSONArray arr = (JSONArray) json.getObject();

            final JSONObject object1 = arr.getJSONObject(0);
            final JSONObject object2 = arr.getJSONObject(1);

            assertEquals("Tester", object1.optString("first"));
            assertEquals("von Testingen", object1.optString("last"));
            assertEquals("Testerine", object2.optString("first"));
            assertEquals("von Testeringen", object2.optString("last"));

        } catch (final JSONException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testDeepLevelObjects() {
        final OXJSONWriter json = new OXJSONWriter();

        try {

            json.array();
            json.object().key("level-key" + String.valueOf(0));
            json.value("level-val" + String.valueOf(0)).endObject();

            final int level = 20;
            for (int i = 0; i < level; i++) {
                json.object().key("level-key" + String.valueOf(i + 1));
            }
            json.value("thevalue");
            for (int i = 19; i >= 0; i--) {
                json.endObject();
            }

            json.object().key("first1").object().key("second1").object().key("third1").value("dasd").endObject().key("asasasas").value("sdfsdfsdf").endObject().endObject();
            json.endArray();

            json.getObject().toString();

        } catch (final JSONException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    @Test
    public void testArrayInitializedWriter() {
        final JSONArray ja = new JSONArray().put("Value01").put("Value02");
        try {
            final OXJSONWriter json = new OXJSONWriter(ja);

            json.array().value("Foo").value("Bar").endArray();
            json.value("Value03");

            /*
             * Optional, but sets writer into completed mode
             */
            json.endArray();

            assertEquals(ja.toString(), json.getObject().toString());
        } catch (final JSONException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }

    }

    @Test
    public void testObjectInitializedWriter() {
        try {
            final JSONObject jo = new JSONObject().put("Key01", "Value01").put("Key02", "Value02");

            final OXJSONWriter json = new OXJSONWriter(jo);

            json.key("InnerArr");
            json.array().value("Foo").value("Bar").endArray();
            json.key("Key03").value("Value03");

            /*
             * Optional, but sets writer into completed mode
             */
            json.endObject();

            assertEquals(jo.toString(), json.getObject().toString());

        } catch (final JSONException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }

    }

    @Test
    public void testComplex() {

    }

    @Test
    public void testInvalid() {

    }
}
