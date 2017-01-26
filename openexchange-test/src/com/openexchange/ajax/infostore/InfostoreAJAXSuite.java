
package com.openexchange.ajax.infostore;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import com.openexchange.ajax.infostore.fileaccount.test.FilestorageAccountTest;
import com.openexchange.ajax.infostore.test.AnotherCreateAndDeleteInfostoreTest;
import com.openexchange.ajax.infostore.test.AppendDocumentTest;
import com.openexchange.ajax.infostore.test.Bug27722Test;
import com.openexchange.ajax.infostore.test.Bug32004Test;
import com.openexchange.ajax.infostore.test.Bug40142Test;
import com.openexchange.ajax.infostore.test.Bug44622Test;
import com.openexchange.ajax.infostore.test.CheckNameActionTest;
import com.openexchange.ajax.infostore.test.CreateAndDeleteInfostoreTest;
import com.openexchange.ajax.infostore.test.CreateFileWithIllegalCharactersTest;
import com.openexchange.ajax.infostore.test.InfostoreObjectCountTest;
import com.openexchange.ajax.infostore.test.TrashTest;
import com.openexchange.ajax.infostore.test.TryAddVersionTest;
import com.openexchange.ajax.infostore.test.ZipDocumentsTest;
import com.openexchange.test.concurrent.ParallelSuite;

@RunWith(ParallelSuite.class)
@Suite.SuiteClasses({
    AllTest.class,
    GetTest.class,
    ListTest.class,
    NewTest.class,
    UpdatesTest.class,
    UpdateTest.class,
    VersionsTest.class,
    DetachTest.class,
    DocumentTest.class,
    CopyTest.class,
    LockTest.class,
    SaveAsTest.class,
    SearchTest.class,
    CreateAndDeleteInfostoreTest.class,
    AnotherCreateAndDeleteInfostoreTest.class,
    InfostoreObjectCountTest.class,
    DeleteMultipleFilesTest.class,
    ZipDocumentsTest.class,
    Bug27722Test.class,
    TrashTest.class,
    Bug32004Test.class,
    Bug40142Test.class,
    CreateFileWithIllegalCharactersTest.class,
    CheckNameActionTest.class,
    FilestorageAccountTest.class,
    AppendDocumentTest.class,
    Bug44622Test.class,
    Bug44891Test.class,
    TryAddVersionTest.class,

})
public class InfostoreAJAXSuite  {

}
