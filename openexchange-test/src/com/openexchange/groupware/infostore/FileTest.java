package com.openexchange.groupware.infostore;

public abstract class FileTest extends InfostoreTest {

	public static final String TEST_FILE = "file.odt.path";
	
	public static final String TEST_DOC_FILE = "file.doc.path";
	
	public static final String TEST_PDF_FILE = "file.pdf.path";
	
	public static final String TEST_HTML_FILE = "file.html.path";
	
	protected String testFile;
	
	protected String testDocFile;
	
	protected String testPdfFile;
	
	protected String testHtmlFile;
	
	public FileTest(){
		super();
	}
	
	public FileTest(String filename) {
		super(filename);
	}
	
	public void initProperties(){
		super.initProperties();
		this.testFile = fixtures.getProperty(TEST_FILE);
		this.testDocFile = fixtures.getProperty(TEST_DOC_FILE);
		this.testPdfFile = fixtures.getProperty(TEST_PDF_FILE);
		this.testHtmlFile = fixtures.getProperty(TEST_HTML_FILE);	
	}

}
