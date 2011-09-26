package com.openexchange.ajax.framework;

import junit.framework.TestCase;
import com.openexchange.ajax.framework.AJAXRequest.Parameter;

public class ParamsTest extends TestCase {
	protected Params defaultParams;

	@Override
	protected void setUp() throws Exception {
		defaultParams = new Params();
		defaultParams.add("key1", "value1");
		defaultParams.add(new Parameter("key2","value2"));
		defaultParams.add("key3","value3","key4","value4");
		defaultParams.add(new Parameter("key5","value5"),new Parameter("key6","value6"));
	}

	public void testToString(){
		assertEquals("?key1=value1&key2=value2&key3=value3&key4=value4&key5=value5&key6=value6", defaultParams.toString());
	}

	public void testToList(){
		//TODO
	}

	public void testToArray(){
		//TODO
	}

}
