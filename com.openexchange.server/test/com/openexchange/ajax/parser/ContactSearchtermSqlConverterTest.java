package com.openexchange.ajax.parser;

import java.util.List;
import junit.framework.TestCase;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.contact.sqlinjectors.SQLInjector;
import com.openexchange.search.CompositeSearchTerm;
import com.openexchange.search.CompositeSearchTerm.CompositeOperation;
import com.openexchange.search.SingleSearchTerm;
import com.openexchange.search.internal.operands.ColumnOperand;
import com.openexchange.search.internal.operands.ConstantOperand;

public class ContactSearchtermSqlConverterTest extends TestCase {

	private final String folderFieldName = ContactField.FOLDER_ID.getAjaxName();

	protected void assertEquals(List<SQLInjector> actual, String...expected){
		assertEquals("Should have same amount of elements", actual.size(), expected.length);

		for(int i = 0, length = actual.size(); i < length; i++){
			assertEquals("SQLInjector #"+i+" does not match", expected[i], actual.get(i).toString());
		}
	}

	public void testSingleSearchTermStringForString(){
		SingleSearchTerm term = new SingleSearchTerm(SingleSearchTerm.SingleOperation.EQUALS);
		term.addOperand(new ConstantOperand<String>("fieldname"));
		term.addOperand(new ConstantOperand<String>("value"));

		String expected = "( ? = ? )";

		ContactSearchtermSqlConverter converter = new ContactSearchtermSqlConverter();
		converter.parse(term);
		String actualString = converter.getPreparedWhereString();
		List<SQLInjector> actualInjectors = converter.getInjectors();

		assertEquals(expected, actualString);
		assertEquals(actualInjectors, "fieldname", "value");
	}


	public void testSingleSearchTermWithNegations(){
		SingleSearchTerm equals = new SingleSearchTerm(SingleSearchTerm.SingleOperation.EQUALS);
		equals.addOperand(new ConstantOperand<String>("fieldname"));
		equals.addOperand(new ConstantOperand<String>("value"));

		CompositeSearchTerm not = new CompositeSearchTerm(CompositeSearchTerm.CompositeOperation.NOT);
		not.addSearchTerm(equals);

		String expected = "( ! ( ? = ? ) )";

		ContactSearchtermSqlConverter converter = new ContactSearchtermSqlConverter();
		converter.parse(not);
		String actualString = converter.getPreparedWhereString();
		List<SQLInjector> actualInjectors = converter.getInjectors();

		assertEquals(expected, actualString);
		assertEquals(actualInjectors, "fieldname", "value");
	}

	public void testUseOfLikeInsteadOfEqualsInCaseOfAsteriskSearch(){
		SingleSearchTerm term = new SingleSearchTerm(SingleSearchTerm.SingleOperation.EQUALS);
		term.addOperand(new ConstantOperand<String>("fieldname"));
		term.addOperand(new ConstantOperand<String>("value*"));

		String expected = "( ? LIKE ? )";

		ContactSearchtermSqlConverter converter = new ContactSearchtermSqlConverter();
		converter.parse(term);
		String actualString = converter.getPreparedWhereString();
		List<SQLInjector> actualInjectors = converter.getInjectors();

		assertEquals(expected, actualString);
		assertEquals(actualInjectors, "fieldname", "value%");
	}

	public void testComplexSearchTermStringForStrings(){
		SingleSearchTerm term1 = new SingleSearchTerm(SingleSearchTerm.SingleOperation.EQUALS);
		term1.addOperand(new ConstantOperand<String>("field1name"));
		term1.addOperand(new ConstantOperand<String>("value1"));

		SingleSearchTerm term2 = new SingleSearchTerm(SingleSearchTerm.SingleOperation.GREATER_THAN);
		term2.addOperand(new ConstantOperand<String>("field2name"));
		term2.addOperand(new ConstantOperand<String>("value2"));

		CompositeSearchTerm term = new CompositeSearchTerm(CompositeOperation.OR);
		term.addSearchTerm(term1);
		term.addSearchTerm(term2);

		String expected = "( ( ? = ? ) OR ( ? > ? ) )";

		ContactSearchtermSqlConverter converter = new ContactSearchtermSqlConverter();
		converter.parse(term);
		String actualString = converter.getPreparedWhereString();
		List<SQLInjector> actualInjectors = converter.getInjectors();

		assertEquals(expected, actualString);
		assertEquals(actualInjectors, "field1name", "value1", "field2name", "value2");
	}

	public void testJsonToMysqlFieldnameTranslation(){
		ContactField field = ContactField.DISPLAY_NAME;

		SingleSearchTerm term = new SingleSearchTerm(SingleSearchTerm.SingleOperation.EQUALS);
		term.addOperand(new ColumnOperand(field .getAjaxName()));
		term.addOperand(new ConstantOperand<String>("value1"));

		String expected = "( co."+field.getDbName()+" = ? )";

		ContactSearchtermSqlConverter converter = new ContactSearchtermSqlConverter();
		converter.parse(term);
		String actualString = converter.getPreparedWhereString();
		List<SQLInjector> actualInjectors = converter.getInjectors();

		assertEquals(expected, actualString);
		assertEquals(1, actualInjectors.size());
		assertEquals( "value1", actualInjectors.get(0).toString() );
	}

	public void testFolderExtraction(){
		ContactField distractingField = ContactField.CITY_HOME;
		SingleSearchTerm term1 = new SingleSearchTerm(SingleSearchTerm.SingleOperation.EQUALS);
		term1.addOperand(new ColumnOperand(folderFieldName));
		term1.addOperand(new ConstantOperand<String>("1"));

		SingleSearchTerm term2 = new SingleSearchTerm(SingleSearchTerm.SingleOperation.LESS_THAN);
		term2.addOperand(new ColumnOperand(distractingField.getAjaxName()));
		term2.addOperand(new ConstantOperand<String>("not-a-folder"));

		SingleSearchTerm term3 = new SingleSearchTerm(SingleSearchTerm.SingleOperation.GREATER_THAN);
		term3.addOperand(new ColumnOperand(folderFieldName));
		term3.addOperand(new ConstantOperand<String>("2"));

		SingleSearchTerm term4 = new SingleSearchTerm(SingleSearchTerm.SingleOperation.GREATER_THAN);
		term4.addOperand(new ColumnOperand(folderFieldName));
		term4.addOperand(new ConstantOperand<String>("strings-can-be-folder-names-too"));

		CompositeSearchTerm term = new CompositeSearchTerm(CompositeOperation.OR);
		term.addSearchTerm(term1);
		term.addSearchTerm(term2);
		term.addSearchTerm(term3);
		term.addSearchTerm(term4);

		ContactSearchtermSqlConverter converter = new ContactSearchtermSqlConverter();
		converter.parse(term);

		String expected = "( ( co.fid = ? ) OR ( co."+distractingField.getDbName()+" < ? ) OR ( co.fid > ? ) OR ( co.fid > ? ) )";
		String actualString = converter.getPreparedWhereString();
		assertEquals(expected, actualString);

		List<String> folders = converter.getFolders();
		assertEquals(3, folders.size());
		assertTrue(folders.contains("1"));
		assertTrue(folders.contains("2"));
		assertTrue(folders.contains("strings-can-be-folder-names-too"));
	}

	public void testIsNullOperator(){
		 ContactField field = ContactField.YOMI_LAST_NAME;
		SingleSearchTerm term = new SingleSearchTerm(SingleSearchTerm.SingleOperation.ISNULL);
		term.addOperand(new ColumnOperand(field.getAjaxName()));

		ContactSearchtermSqlConverter converter = new ContactSearchtermSqlConverter();
		converter.parse(term);

		String expected = "( co.yomiLastName IS NULL )";
		String actualString = converter.getPreparedWhereString();
		assertEquals(expected, actualString);
	}

	public void testNotOperator(){
		SingleSearchTerm equalsTerm = new SingleSearchTerm(SingleSearchTerm.SingleOperation.EQUALS);
		equalsTerm.addOperand(new ColumnOperand("yomiLastName"));
		equalsTerm.addOperand(new ConstantOperand<String>("value1"));


		CompositeSearchTerm notTerm = new CompositeSearchTerm(CompositeOperation.NOT);
		notTerm.addSearchTerm(equalsTerm);

		ContactSearchtermSqlConverter converter = new ContactSearchtermSqlConverter();
		converter.parse(notTerm);

		String expected = "( ! ( co.yomiLastName = ? ) )";
		String actualString = converter.getPreparedWhereString();
		assertEquals(expected, actualString);
	}


	public void testUsingCharset(){
		SingleSearchTerm equalsTerm = new SingleSearchTerm(SingleSearchTerm.SingleOperation.EQUALS);
		equalsTerm.addOperand(new ColumnOperand("yomiLastName"));
		equalsTerm.addOperand(new ConstantOperand<String>("value1"));


		ContactSearchtermSqlConverter converter = new ContactSearchtermSqlConverter();
		converter.setCharset("gb2312");
		converter.parse(equalsTerm);

		String expected = "( CONVERT(co.yomiLastName USING gb2312) = CONVERT(? USING gb2312) )";
		String actualString = converter.getPreparedWhereString();
		assertEquals(expected, actualString);
	}
}
