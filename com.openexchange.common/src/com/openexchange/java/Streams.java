package com.openexchange.java;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class Streams {

	public static byte[] stream2bytes(InputStream is) throws IOException{
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		int buff;

		while((buff = is.read()) != -1) {
            bos.write(buff);
        }

		return bos.toByteArray();
	}
}
