/* Copyright (c) 2002,2003, Stefan Haustein, Oberhausen, Rhld., Germany
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The  above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * IN THE SOFTWARE. */


package org.kxml2.wap;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.xmlpull.v1.XmlSerializer;

import com.openexchange.tools.stream.UnsynchronizedByteArrayOutputStream;

// TODO: make some of the "direct" WBXML token writing methods public??

/** 
 * A class for writing WBXML. 
 *  
 */



public class WbxmlSerializer implements XmlSerializer {

    private static final String ERR_NYI = "NYI";

	Hashtable<String, Integer> stringTable = new Hashtable<String, Integer>();

    OutputStream out;

    ByteArrayOutputStream buf = new UnsynchronizedByteArrayOutputStream();
    ByteArrayOutputStream stringTableBuf = new UnsynchronizedByteArrayOutputStream();

    String pending;
    int depth;
    String name;
    String namespace;
    List<String> attributes = new ArrayList<String>();

    Hashtable<String, int[]> attrStartTable = new Hashtable<String, int[]>();
    Hashtable<String, int[]> attrValueTable = new Hashtable<String, int[]>();
    Hashtable<String, int[]> tagTable = new Hashtable<String, int[]>();

	private int attrPage;
	private int tagPage;


    public XmlSerializer attribute(final String namespace, final String name, final String value) {
        attributes.add(name);
        attributes.add(value);
        return this;
    }


    public void cdsect (final String cdsect) throws IOException{
        text (cdsect);
    }



    /* silently ignore comment */

    public void comment (final String comment) {
    }

    
    public void docdecl (final String docdecl) {
        throw new RuntimeException ("Cannot write docdecl for WBXML");
    }


    public void entityRef (final String er) {
        throw new RuntimeException ("EntityReference not supported for WBXML");
    }
    
    public int getDepth() {
    	return depth;
    }


    public boolean getFeature (final String name) {
        return false;
    }
    
	public String getNamespace() {
		throw new RuntimeException(ERR_NYI);
	}
	
	public String getName() {
		throw new RuntimeException(ERR_NYI);
	}
	
	public String getPrefix(final String nsp, final boolean create) {
        throw new RuntimeException (ERR_NYI);
    }
    
    
    public Object getProperty (final String name) {
        return null;
    }

    public void ignorableWhitespace (final String sp) {
    }
    

    public void endDocument() throws IOException {
        writeInt(out, stringTableBuf.size());

        // write StringTable

        out.write(stringTableBuf.toByteArray());

        // write buf 

        out.write(buf.toByteArray());

        // ready!

        out.flush();
    }


    /** ATTENTION: flush cannot work since Wbxml documents require
    need buffering. Thus, this call does nothing. */

    public void flush() {
    }


    public void checkPending(final boolean degenerated) throws IOException {
        if (pending == null) {
			return;
		}

        final int len = attributes.size();

        int[] idx = tagTable.get(pending);

        // if no entry in known table, then add as literal
        if (idx == null) {
            buf.write(
                len == 0
                    ? (degenerated ? Wbxml.LITERAL : Wbxml.LITERAL_C)
                    : (degenerated ? Wbxml.LITERAL_A : Wbxml.LITERAL_AC));

            writeStrT(pending);
        }
        else {
        	if(idx[0] != tagPage){
        		tagPage=idx[0];
        		buf.write(0);
        		buf.write(tagPage);
        	}
        	
            buf.write(
                len == 0
                    ? (degenerated ? idx[1] : idx[1] | 64)
                    : (degenerated
                        ? idx[1] | 128
                        : idx[1] | 192));

        }

        for (int i = 0; i < len;) {
            idx = attrStartTable.get(attributes.get(i));
            
            if (idx == null) {
                buf.write(Wbxml.LITERAL);
                writeStrT(attributes.get(i));
            }
            else {
				if(idx[0] != attrPage){
					attrPage = idx[1];
					buf.write(0);
					buf.write(attrPage);					
				}
                buf.write(idx[1]);
            }
            idx = attrValueTable.get(attributes.get(++i));
            if (idx == null) {
                buf.write(Wbxml.STR_I);
                writeStrI(buf, attributes.get(i));
            }
            else {
				if(idx[0] != attrPage){
					attrPage = idx[1];
					buf.write(0);
					buf.write(attrPage);					
				}
                buf.write(idx[1]);
            }
            ++i;
        }

        if (len > 0) {
			buf.write(Wbxml.END);
		}

        pending = null;
        attributes.clear();
    }


    public void processingInstruction(final String pi) {
        throw new RuntimeException ("PI NYI");
    }


    public void setFeature(final String name, final boolean value) {
        throw new IllegalArgumentException ("unknown feature "+name);
    }
        


    public void setOutput (final Writer writer) {
        throw new RuntimeException ("Wbxml requires an OutputStream!");
    }

    public void setOutput (final OutputStream out, final String encoding) throws IOException {
        
        if (encoding != null) {
			throw new IllegalArgumentException ("encoding not yet supported for WBXML");
		}
        
        this.out = out;

        buf = new UnsynchronizedByteArrayOutputStream();
        stringTableBuf = new UnsynchronizedByteArrayOutputStream();

        // ok, write header 
    }


    public void setPrefix(final String prefix, final String nsp) {
        throw new RuntimeException(ERR_NYI);
    }

    public void setProperty(final String property, final Object value) {
        throw new IllegalArgumentException ("unknown property "+property);
    }

    
    public void startDocument(final String s, final Boolean b) throws IOException{
        out.write(0x01); // version
        out.write(0x01); // unknown or missing public identifier
        out.write(0x04); // iso-8859-1
    }


    public XmlSerializer startTag(final String namespace, final String name) throws IOException {

        if (namespace != null && !"".equals(namespace)) {
			throw new RuntimeException ("NSP NYI");
		}

        //current = new State(current, prefixMap, name);

        checkPending(false);
        pending = name;
		depth++;
		
        return this;
    }

    public XmlSerializer text(final char[] chars, final int start, final int len) throws IOException {

        checkPending(false);

        buf.write(Wbxml.STR_I);
        writeStrI(buf, new String(chars, start, len));

        return this;
    }

    public XmlSerializer text(final String text) throws IOException {

        checkPending(false);

        buf.write(Wbxml.STR_I);
        writeStrI(buf, text);

        return this;
    }
    
    

    public XmlSerializer endTag(final String namespace, final String name) throws IOException {

//        current = current.prev;

        if (pending != null) {
			checkPending(true);
		} else {
			buf.write(Wbxml.END);
		}

		depth--;

        return this;
    }

    /** currently ignored! */

    public void writeLegacy(final int type, final String data) {
    }

    // ------------- internal methods --------------------------

    static void writeInt(final OutputStream out, int i) throws IOException {
        byte[] buf = new byte[5];
        int idx = 0;

        do {
            buf[idx++] = (byte) (i & 0x7f);
            i = i >> 7;
        }
        while (i != 0);

        while (idx > 1) {
            out.write(buf[--idx] | 0x80);
        }
        out.write(buf[0]);
    }

    static void writeStrI(final OutputStream out, final String s) throws IOException {
        for (int i = 0; i < s.length(); i++) {
            out.write((byte) s.charAt(i));
        }
        out.write(0);
    }

    void writeStrT(final String s) throws IOException {

        Integer idx = stringTable.get(s);

        if (idx == null) {
            idx = Integer.valueOf(stringTableBuf.size());
            stringTable.put(s, idx);
            writeStrI(stringTableBuf, s);
            stringTableBuf.flush();
        }

        writeInt(buf, idx.intValue());
    }

    /** 
     * Sets the tag table for a given page.
     * The first string in the array defines tag 5, the second tag 6 etc.
     */
    
    public void setTagTable(final int page, final String[] tagTable) {
        // clear entries in tagTable!
		if (page != 0) {
			return;
		}

        for (int i = 0; i < tagTable.length; i++) {
            if (tagTable[i] != null) {
                final int[] idx = new int[]{page, i+5};
                this.tagTable.put(tagTable[i], idx);
            }
        }
    }

    /** 
     * Sets the attribute start Table for a given page.
     * The first string in the array defines attribute 
     * 5, the second attribute 6 etc.
     *  Please use the 
     *  character '=' (without quote!) as delimiter 
     *  between the attribute name and the (start of the) value 
     */
    public void setAttrStartTable(final int page, final String[] attrStartTable) {
        
        for (int i = 0; i < attrStartTable.length; i++) {
            if (attrStartTable[i] != null) {
                final int[] idx = new int[] {page, i + 5};
                this.attrStartTable.put(attrStartTable[i], idx);
            }
        }
    }

    /** 
     * Sets the attribute value Table for a given page.
     * The first string in the array defines attribute value 0x85, 
     * the second attribute value 0x86 etc.
     */
    public void setAttrValueTable(final int page, final String[] attrValueTable) {
        // clear entries in this.table!
        for (int i = 0; i < attrValueTable.length; i++) {
            if (attrValueTable[i] != null) {
                final int[] idx = new int[]{page, i + 0x085};
                this.attrValueTable.put(attrValueTable[i], idx);
            }
        }
    }
}
