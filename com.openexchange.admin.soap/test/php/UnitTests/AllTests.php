<?php
require_once 'PHPUnit/Framework.php';
require_once 'ContextTests.php';
require_once 'UserTests.php';
require_once 'GroupTests.php';
require_once 'ResourceTests.php'; 

class AllTests
{
    public static function suite()
    {
        $suite = new PHPUnit_Framework_TestSuite('PHPUnit');
        $suite->addTestSuite('ContextTests');
        $suite->addTestSuite('UserTests');
        $suite->addTestSuite('GroupTests');
        $suite->addTestSuite('ResourceTests');
        // $suite->addTestSuite('UtilTests');
        // $suite->addTestSuite('CleanUpContexts');
        return $suite;
    }
}
?>
