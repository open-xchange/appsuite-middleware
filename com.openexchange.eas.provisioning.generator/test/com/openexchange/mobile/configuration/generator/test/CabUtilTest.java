package com.openexchange.mobile.configuration.generator.test;

import java.util.Calendar;
import org.junit.Assert;
import org.junit.Test;
import com.openexchange.mobile.configuration.generator.CabUtil;


public class CabUtilTest extends CabUtil {

     @Test
     public void testLittleEndianShort() {
        final short littleEndian = toLittleEndian((short)0x4010);
        Assert.assertTrue("Value of 0x4010 must be 0x1040, but is " + Integer.toHexString(littleEndian), littleEndian == (short)0x1040);
        System.out.println(Integer.toHexString(littleEndian));
    }

     @Test
     public void testLittleEndianShort2() {
        final short littleEndian = toLittleEndian((short)0xFFFE);
        Assert.assertTrue("Value of 0xFFFE must be 0xFEFF, but is " + Integer.toHexString(littleEndian), littleEndian == (short)0xFEFF);
    }

     @Test
     public void testLittleEndianShort3() {
        final short littleEndian = toLittleEndian((short)0x0FFE);
        Assert.assertTrue("Value of 0x0FFE must be 0xFE0F, but is " + Integer.toHexString(littleEndian), littleEndian == (short)0xFE0F);
    }

     @Test
     public void testLittleEndianShort4() {
        final short littleEndian = toLittleEndian((short)0xFFFF);
        Assert.assertTrue("Value of 0xFFFF must be 0xFFFF, but is " + Integer.toHexString(littleEndian), littleEndian == (short)0xFFFF);
    }

     @Test
     public void testLittleEndianInt() {
        final int littleEndian = toLittleEndian(0x4010);
        Assert.assertTrue("Value of 0x4010 must be 0x10400000, but is " + Integer.toHexString(littleEndian), littleEndian == 0x10400000);
    }

     @Test
     public void testLittleEndianInt2() {
        final int littleEndian = toLittleEndian(0x4010FFFF);
        Assert.assertTrue("Value of 0x4010FFFF must be 0xFFFF1040, but is " + Integer.toHexString(littleEndian), littleEndian == 0xFFFF1040);
    }

     @Test
     public void testLittleEndianInt3() {
        final int littleEndian = toLittleEndian(0xFFFEFFFF);
        Assert.assertTrue("Value of 0xFFFEFFFF must be 0xFFFFFEFF, but is " + Integer.toHexString(littleEndian), littleEndian == 0xFFFFFEFF);
    }

     @Test
     public void testLittleEndianInt4() {
        final int littleEndian = toLittleEndian(0xFEFFFFFF);
        Assert.assertTrue("Value of 0xFEFFFFFF must be 0xFFFFFFFE, but is " + Integer.toHexString(littleEndian), littleEndian == 0xFFFFFFFE);
    }

     @Test
     public void testGetDate() {
        final Calendar instance = Calendar.getInstance();
        //instance.setLenient(false);
        instance.set(2010, Calendar.MAY, 31);
        final int year = instance.get(Calendar.YEAR);
        final int month = instance.get(Calendar.MONTH);
        final int day = instance.get(Calendar.DAY_OF_MONTH);
        final short date = getDate(instance);
        System.out.println("Year: " + year + "; Month: " + month + "; Day: " + day);
        final short littleEndian = toLittleEndian(date);
        Assert.assertTrue("Date value should be 0xBF3C but is " + Integer.toHexString(littleEndian), (short)0xBF3C == littleEndian);
    }

     @Test
     public void testGetTime() {
        final Calendar instance = Calendar.getInstance();
        //instance.setLenient(false);
        instance.set(Calendar.HOUR_OF_DAY, 14);
        instance.set(Calendar.MINUTE, 11);
        instance.set(Calendar.SECOND, 24);

        final short time = getTime(instance);
        final short littleEndian = toLittleEndian(time);
        Assert.assertTrue("Date value should be 0x6C71 but is " + Integer.toHexString(littleEndian), (short)0x6C71 == littleEndian);
    }
}
