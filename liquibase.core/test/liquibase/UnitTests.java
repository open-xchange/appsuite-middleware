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

package liquibase;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;


/**
 * Unit tests from the 3rd party module Liquibase.
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.8.0
 */
@RunWith(Suite.class)
@SuiteClasses({
//    NoJavaSpecificCodeTest.class,
//    LiquibaseTest.class,
//    AddAutoIncrementChangeTest.class,
//    AddColumnChangeTest.class,
//    AddDefaultValueChangeTest.class,
//    AddForeignKeyConstraintChangeTest.class,
//    AddLookupTableChangeTest.class,
//    AddNotNullConstraintChangeTest.class,
//    AddPrimaryKeyChangeTest.class,
//    AddUniqueConstraintChangeTest.class,
//    AlterSequenceChangeTest.class,
//    CreateIndexChangeTest.class,
//    CreateSequenceChangeTest.class,
//    CreateTableChangeTest.class,
//    CreateViewChangeTest.class,
//    DropColumnChangeTest.class,
//    DropDefaultValueChangeTest.class,
//    DropForeignKeyConstraintChangeTest.class,
//    DropIndexChangeTest.class,
//    DropNotNullConstraintChangeTest.class,
//    DropPrimaryKeyChangeTest.class,
//    DropSequenceChangeTest.class,
//    DropTableChangeTest.class,
//    DropUniqueConstraintChangeTest.class,
//    DropViewChangeTest.class,
//    InsertDataChangeTest.class,
//    LoadDataChangeTest.class,
//    LoadUpdateDataChangeTest.class,
//    RenameColumnChangeTest.class,
//    RenameTableChangeTest.class,
//    RenameViewChangeTest.class,
//    SQLFileChangeTest.class,
//    TagDatabaseChangeTest.class,
//    UpdateDataChangeTest.class,
//    CustomChangeWrapperTest.class,
//    AbstractChangeTest.class,
//    AbstractSQLChangeTest.class,
//    BaseSQLChangeTest.class,
//    ChangeFactoryTest.class,
//    ChangeMetaDataTest.class,
//    ChangeParameterMetaDataTest.class,
//    CheckSumTest.class,
//    ColumnConfigTest.class,
//    ConstraintsConfigTest.class,
//
//    // package liquibase.changelog
//    AfterTagChangeSetFilterTest.class,
//    AlreadyRanChangeSetFilterTest.class,
//    ContextChangeSetFilterTest.class,
//    CountChangeSetFilterTest.class,
//    DbmsChangeSetFilterTest.class,
//    ExecutedAfterChangeSetFilterTest.class,
//    ValidatingVisitorPreConditionsTest.class,
//    ValidatingVisitorTest.class,
//    ChangeLogIteratorTest.class,
//    ChangeLogParametersTest.class,
//    ChangeLogParserFactoryTest.class,
//    ChangeSetTest.class,
//    ExpressionExpanderTest.class,
//
//    // package liquibase.database
//    CacheDatabaseTest.class,
//    DB2DatabaseTest.class,
//    DerbyDatabaseTest.class,
//    HsqlDatabaseTest.class,
//    InformixDatabaseTest.class,
//    MSSQLDatabaseTest.class,
//    MySQLDatabaseTest.class,
//    OracleDatabaseTest.class,
//    PostgresDatabaseTest.class,
//    UnsupportedDatabaseTest.class,
//    DatabaseFactoryTest.class,
//    DatabaseListTest.class,
//
//    // package liquibase.datatype
//    DataTypeFactoryTest.class,
//
//    // package liquibase diff
//    DiffToChangeLogTest.class,
//    DiffGeneratorFactoryTest.class,
//    DiffResultTest.class,
//
//    // package liquibase.exception
//    DuplicateChangeSetExceptionTest.class,
//    DuplicateStatementIdentifierExceptionTest.class,
//    ValidatorErrorsTest.class,
//
//    // package liquibase.executor
//    JdbcExecutorTest.class,
//
//    // package liquibase.lockservice
//    LockServiceFactoryTest.class,
//
//    // package liquibase.logging
//    LogFactoryTest.class,
//
//    // package liquibase.parser
//    FormattedSqlChangeLogParserTest.class,
//    LiquibaseEntityResolverTest.class,
//    XMLChangeLogSAXParserTest.class,
//    YamlChangeLogParserTest.class,
//    ChangeLogParserFactoryTest.class,
//
//    // package liquibase.precondition
//    PreconditionFactoryTest.class,
//
//    // package liquibase.resource
//    FileSystemFileOpenerTest.class,
//    JUnitFileOpenerTest.class,
//    UtfBomAwareReaderTest.class,
//
//    //package liquibase.serializer
//    JsonChangeLogSerializerTest.class,
//    StringChangeLogSerializerTest.class,
//    XMLChangeLogSerializerTest.class,
//    YamlChangeLogSerializerTest.class,
//    ChangeLogSerializerFactoryTest.class,
//    ReflectionSerializerTest.class,
//
//    // package liquibase.servicelocator
//    ServiceLocatorTest.class,
//
//    // package liquibase.sqlgenerator
//    AddColumnGeneratorDefaultClauseBeforeNotNullTest.class,
//    AddColumnGeneratorTest.class,
//    AddUniqueConstraintGeneratorTest.class,
//    AddUniqueConstraintGeneratorInformixTest.class,
//    AddUniqueConstraintGeneratorTDSTest.class,
//    CommentGeneratorTest.class,
//    CreateDatabaseChangeLogLockTableGeneratorTest.class,
//    CreateTableGeneratorTest.class,
//    DropIndexGeneratorTest.class,
//    GetViewDefinitionGeneratorSybaseTest.class,
//    InsertOrUpdateGeneratorMSSQLTest.class,
//    InsertOrUpdateGeneratorOracleTest.class,
//    MarkChangeSetRanGeneratorTest.class,
//    SelectFromDatabaseChangeLogGeneratorTest.class,
//    SelectFromDatabaseChangeLogLockGeneratorTest.class,
//    UnlockDatabaseChangeLogGeneratorTest.class,
//    GeneratorLevelTest.class,
//    SqlGeneratorChainTest.class,
//    SqlGeneratorFactoryTest.class,
//
//    // package liquibase.statement
//    AddAutoIncrementStatementTest.class,
//    AddColumnStatementTest.class,
//    AddDefaultValueStatementTest.class,
//    AddForeignKeyConstraintStatementTest.class,
//    AddPrimaryKeyStatementTest.class,
//    AddUniqueConstraintStatementTest.class,
//    AlterSequenceStatementTest.class,
//    CommentStatementTest.class,
//    CreateDatabaseChangeLogLockTableStatementTest.class,
//    CreateIndexStatementTest.class,
//    CreateSequenceStatementTest.class,
//    CreateTableStatementTest.class,
//    CreateViewStatementTest.class,
//    DeleteStatementTest.class,
//    DropColumnStatementTest.class,
//    DropDefaultValueStatementTest.class,
//    DropForeignKeyConstraintStatementTest.class,
//    DropIndexStatementTest.class,
//    DropPrimaryKeyStatementTest.class,
//    DropSequenceStatementTest.class,
//    DropTableStatementTest.class,
//    DropUniqueConstraintStatementTest.class,
//    DropViewStatementTest.class,
//    FindForeignKeyConstraintsStatementTest.class,
//    InsertOrUpdateStatementTest.class,
//    InsertStatementTest.class,
//    RawSqStatementTest.class,
//    ReindexStatementTest.class,
//    RenameColumnStatementTest.class,
//    RenameTableStatementTest.class,
//    RenameViewStatementTest.class,
//    ReorganizeTableStatementTest.class,
//    SelectFromDatabaseChangeLogLockStatementTest.class,
//    SelectFromDatabaseChangeLogStatementTest.class,
//    SetNullableStatementTest.class,
//    SetColumnRemarksStatementTest.class,
//    SetTableRemarksStatementTest.class,
//    StoredProcedureStatementTest.class,
//    TagDatabaseStatementTest.class,
//    UpdateStatementTest.class,
//    AutoIncrementConstraintTest.class,
//
//    // package liquibase.structure
//    PrimaryKeyTest.class,
//
//    // package liquibase.util
//    ISODateFormatTest.class,
//    MD5UtilTest.class,
//    RegexMatcherTest.class,
//    StreamUtilTest.class,
//    StringUtilsTest.class,
//
//    VerifyChangeClassesTest.class,
//
//    LiquibaseTest.class,
//    NoJavaSpecificCodeTest.class,
})
public class UnitTests {

    /**
     * Initializes a new {@link UnitTests}.
     */
    public UnitTests() {
        super();
    }

}
