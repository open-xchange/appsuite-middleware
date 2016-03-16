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

package com.openexchange.groupware.importexport;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.importexport.importers.TestCSVContactImporter;
import com.openexchange.importexport.formats.Format;
import junit.framework.JUnit4TestAdapter;

/**
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias 'Tierlieb' Prinz</a>
 */
public class CSVContactImportTest extends AbstractContactTest {
    public String IMPORT_HEADERS = ContactTestData.IMPORT_HEADERS;
    public String IMPORT_ONE = ContactTestData.IMPORT_ONE;
    public String IMPORT_MULTIPLE = ContactTestData.IMPORT_MULTIPLE;
    public String IMPORT_DUPLICATE = IMPORT_MULTIPLE + "Laguna, francisco.laguna@open-xchange.com, Francisco Laguna\n";
    public String IMPORT_EMPTY = IMPORT_HEADERS+",,";
    public boolean doDebugging = false;

    public String notASingleImport = "I_E-0804";
    public String malformedCSV = "CSV-1000";
    public String malformedDate = "CON-0600";

    //workaround for JUnit 3 runner
    public static junit.framework.Test suite() {
        return new JUnit4TestAdapter(CSVContactImportTest.class);
    }

    public CSVContactImportTest() throws Exception{
        super();
        imp = new TestCSVContactImporter();
        defaultFormat = Format.CSV;
    }

    @Before
    public void TearUp() throws OXException {
        folderId = createTestFolder(FolderObject.CONTACT, sessObj, ctx, "csvContactTestFolder");
    }

    @Test public void canImport() throws OXException{
        final List <String> folders = new LinkedList<String>();
        folders.add(Integer.toString(folderId));
        //normal case
        assertTrue("Can import?", imp.canImport(sessObj, defaultFormat, folders, null));

        //too many
        folders.add("blaFolder");
        try{
            imp.canImport(sessObj, Format.CSV, folders, null);
            fail("Could import two folders, but should not");
        } catch (final OXException e){
            assertTrue("Cannot import more than one folder", true);
        }

        //wrong export type
        folders.remove("blaFolder");
        try{
            assertTrue("Cannot import ICAL" , !imp.canImport(sessObj, Format.ICAL, folders, null) );
        } catch (final OXException e){
            fail("Exception caught, but only 'false' value expected");
        }

    }

    @Test public void importOneContact() throws NumberFormatException, Exception{
        final List<ImportResult> results = importStuff(IMPORT_ONE);
        assertTrue("One result?" , results.size() == 1);
        final ImportResult res = results.get(0);
        if(res.hasError()){
        	fail("Should run flawlessly, but: " + res.getException().getMessage());
            res.getException().printStackTrace();
        }
        assertTrue( res.isCorrect() );

        //basic check: 1 entry in folder
        assertTrue("One contact in folder?", 1 == getNumberOfContacts(folderId));

        //detailed check:
        checkFirstResult(
            Integer.parseInt(
                res.getObjectId()));

        //cleaning up
        contactStorage.delete(sessObj, res.getFolder(), res.getObjectId(), res.getDate());
    }

    @Test public void importEmpty() throws NumberFormatException, Exception{
        final int numberOfContactsBefore = getNumberOfContacts(folderId);
        final List<ImportResult> results = importStuff(IMPORT_EMPTY);
        assertTrue("One result?" , 1 == results.size());
        final ImportResult res = results.get(0);
        assertTrue("Should have error", res.hasError() );
        assertEquals("Should contain error for not importing because fields are missing", 808, res.getException().getCode());

        //no import, please
        final int numberOfContactsAfter = getNumberOfContacts(folderId);
        assertEquals("Should not have imported a contact", numberOfContactsBefore, numberOfContactsAfter);
    }


    @Test public void importListOfContacts() throws NumberFormatException, Exception{
        final List<ImportResult> results = importStuff(IMPORT_MULTIPLE);
        assertTrue("Two results?" , results.size() == 2);
        for(final ImportResult res : results){
            if(res.hasError()){
                res.getException().printStackTrace();
            }
            assertTrue( res.isCorrect() );
        }

        //basic check
        assertEquals("Two contacts in folder?", 2 , getNumberOfContacts(folderId));

        //cleaning up
        for(final ImportResult res : results){
            contactStorage.delete(sessObj, res.getFolder(), res.getObjectId(), res.getDate());
        }
    }

    @Test public void importBullshit(){
        final List <String> folders = Arrays.asList( Integer.toString(folderId) );
        final InputStream is = new ByteArrayInputStream( "Bla\nbla\nbla".getBytes() );

        try {
            imp.importData(sessObj, defaultFormat, is, folders, null);
        } catch (final OXException e) {
            assertEquals("Checking correct file with wrong header" , notASingleImport, e.getErrorCode());
            return;
        }
        fail("Should throw exception");
    }

    @Test public void importBullshit2(){
        final List <String> folders = Arrays.asList( Integer.toString(folderId) );
        final InputStream is = new ByteArrayInputStream( "Bla\nbla,bla".getBytes() );

        try {
            imp.importData(sessObj, defaultFormat, is, folders, null);
        } catch (final OXException e) {
            assertEquals("Checking malformed file with wrong header" , notASingleImport, e.getErrorCode());
            return;
        }
        fail("Should throw exception");
    }

    /*
     * Currently, the API allows for duplicate entries...
     */
    @Test public void importOfDuplicates() throws NumberFormatException, Exception{
        final List<ImportResult> results = importStuff(IMPORT_DUPLICATE);
        assertTrue("Three results?" , 3 == results.size());
        for(final ImportResult res : results){
            if(res.hasError()){
                res.getException().printStackTrace();
            }
            assertTrue( res.isCorrect() );
        }

        assertEquals("Three contacts in folder?", 3 , getNumberOfContacts(folderId));

        //cleaning up
        for(final ImportResult res : results){
            contactStorage.delete(sessObj, res.getFolder(), res.getObjectId(), res.getDate());
        }
    }
    // Change this for Bug 12987
    @Test public void importDates() throws NumberFormatException, Exception{
        dateTest("04.01.1981");
        dateTest("1981-04-01");
        dateTest("04/01/1981");
    }

    private void dateTest(String date) throws OXException, UnsupportedEncodingException {
        final List<ImportResult> results = importStuff(ContactField.GIVEN_NAME.getReadableName() + " , " + ContactField.BIRTHDAY.getReadableName() + "\n" + "Tobias Prinz ,"+date);
        assertTrue("One result?" , results.size() == 1);
        final ImportResult res = results.get(0);
        assertFalse("Got bug?" , res.hasError() );
    }

    /*
     * Counting the TIMEZONE element?
     */
    @Test
    public void bug7109() throws OXException, UnsupportedEncodingException, OXException {
        final List<ImportResult> results1 = importStuff(ContactField.DISPLAY_NAME.getReadableName()+", "+ContactField.GIVEN_NAME.getReadableName() + " , " + ContactField.BIRTHDAY.getReadableName() + "\n" + "Tobias Prinz , "+ "Tobias Prinz , "+System.currentTimeMillis());
        final List<ImportResult> results2 = importStuff(ContactField.DISPLAY_NAME.getReadableName()+", "+ContactField.GIVEN_NAME.getReadableName() + " , " + ContactField.BIRTHDAY.getReadableName() + "\n" + "Tobias Prinz , "+ "Tobias Prinz , 1981/04/01");
        final List<ImportResult> results3 = importStuff(ContactField.DISPLAY_NAME.getReadableName()+", "+ContactField.GIVEN_NAME.getReadableName() + " , " + "stupidColumnName\n" + "Tobias Prinz , "+ "Tobias Prinz , 1981/04/01");
        final List<ImportResult> results4 = importStuff(ContactField.DISPLAY_NAME.getReadableName()+", "+ContactField.BIRTHDAY.getReadableName() + "\nTobias Prinz, 1981/04/01");
        assertTrue("One result for first attempt?" , results1.size() == 1);
        assertTrue("One result for second attempt?" , results2.size() == 1);
        assertTrue("One result for third attempt?" , results3.size() == 1);
        assertTrue("One result for fourth attempt?" , results4.size() == 1);

        ImportResult tempRes = results1.get(0);
        assertTrue("Attempt 1 has no error", tempRes.isCorrect());
        assertTrue("Entry after attempt 1 exists?", existsEntry(Integer.parseInt(tempRes.getObjectId())));


        try    {
            importStuff("stupidColumnName, yet another stupid column name\n" + "Tobias Prinz , 1981/04/01");
            fail("Importing without any useful column titles should fail.");
        } catch (final OXException exc1){
            assertEquals("Could not translate any column title", notASingleImport, exc1.getErrorCode());
        }
    }

    /*
     * This was listed as 6825, 7107 or 7386
     */
    @Test public void bugTooMuchInformation() throws UnsupportedEncodingException, NumberFormatException, OXException, OXException{
        final String stringTooLong = "aaaaaaaaaabbbbbbbbbbccccccccccddddddddddeeeeeeeeeeffffffffffgggggggggghhhhhhhhhhiiiiiiiiiijjjjjjjjjjkkkkkkkkkkllllllllllmmmmmmmmmmnnnnnnnnnnooooooooooppppppppppqqqqqqqqqqrrrrrrrrrrttttttttttuuuuuuuuuvvvvvvvvvwwwwwwwwwwxxxxxxxxxxyyyyyyyyyyzzzzzzzzzz00000000001111111111222222222233333333334444444444455555555556666666666777777777788888888889999999999";
        final String expected = "aaaaaaaaaabbbbbbbbbbccccccccccddddddddddeeeeeeeeeeffffffffffgggg";
        final List<ImportResult> results = importStuff(ContactField.GIVEN_NAME.getReadableName() + " , " +ContactField.SUFFIX.getReadableName() + "\nElvis," + stringTooLong);
        assertTrue("One result?" , 1 == results.size());
        final ImportResult res = results.get(0);
        assertFalse("Should not fail", res.hasError());

        final Contact conObj = getEntry( Integer.parseInt( res.getObjectId() ) );

        assertEquals("Fields correct?" ,  expected, conObj.getSuffix());
    }

    /*
     * "private" flag is being set
     */
    @Test public void bug7710() throws UnsupportedEncodingException, NumberFormatException, OXException, OXException {
        final String file = ContactField.DISPLAY_NAME.getReadableName()+", "+ContactField.GIVEN_NAME.getReadableName() + " , " + ContactField.PRIVATE_FLAG.getReadableName() + "\nTobias Prinz, Tobias Prinz,true";
        final List<ImportResult> results = importStuff(file);
        assertTrue("Only one result", 1 == results.size());
        final ImportResult res = results.get(0);
        final Contact conObj = getEntry( Integer.parseInt( res.getObjectId() ) );
        assertTrue("Is private?", conObj.getPrivateFlag());
    }

    @Test public void dontImportIfDisplayNameCanBeFormedAtAll() throws Exception{
        final String file = ContactField.COUNTRY_BUSINESS.getReadableName() + "\nNo one likes an empty entry with a country field only";
        try {
            importStuff(file);
            fail("Should throw exception");
        } catch (OXException e){
            assertEquals("Should throw exception for missing fields to build a display name" , 807, e.getCode());
        }
    }


    @Test public void dontImportIfNoDisplayNameCanBeFormedForAGivenContact() throws Exception{
        final String file = ContactField.SUR_NAME.getReadableName() + "," + ContactField.COUNTRY_BUSINESS.getReadableName()+ "\n,Something unimportant";
        final List<ImportResult> results = importStuff(file);
        assertEquals("Should give one result", 1, results.size());
        ImportResult res = results.get(0);
        assertTrue("Needs to contain one error", res.hasError());
        OXException exception = res.getException();
        assertEquals("Should have a problem because there is no material for a display name", 808, exception.getCode());
    }


    protected void checkFirstResult(final int objectID ) throws OXException, OXException {
        final Contact co = getEntry(objectID);
        assertEquals("Checking name" ,  NAME1 , co.getGivenName());
        assertEquals("Checking e-Mail" ,  EMAIL1 , co.getEmail1());
    }
}
