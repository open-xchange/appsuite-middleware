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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

import liquibase.change.AbstractChangeTest;
import liquibase.change.AbstractSQLChangeTest;
import liquibase.change.BaseSQLChangeTest;
import liquibase.change.ChangeFactoryTest;
import liquibase.change.ChangeMetaDataTest;
import liquibase.change.ChangeParameterMetaDataTest;
import liquibase.change.CheckSumTest;
import liquibase.change.ColumnConfigTest;
import liquibase.change.ConstraintsConfigTest;
import liquibase.change.core.AddAutoIncrementChangeTest;
import liquibase.change.core.AddColumnChangeTest;
import liquibase.change.core.AddDefaultValueChangeTest;
import liquibase.change.core.AddForeignKeyConstraintChangeTest;
import liquibase.change.core.AddLookupTableChangeTest;
import liquibase.change.core.AddNotNullConstraintChangeTest;
import liquibase.change.core.AddPrimaryKeyChangeTest;
import liquibase.change.core.AddUniqueConstraintChangeTest;
import liquibase.change.core.AlterSequenceChangeTest;
import liquibase.change.core.CreateIndexChangeTest;
import liquibase.change.core.CreateSequenceChangeTest;
import liquibase.change.core.CreateTableChangeTest;
import liquibase.change.core.CreateViewChangeTest;
import liquibase.change.core.DropColumnChangeTest;
import liquibase.change.core.DropDefaultValueChangeTest;
import liquibase.change.core.DropForeignKeyConstraintChangeTest;
import liquibase.change.core.DropIndexChangeTest;
import liquibase.change.core.DropNotNullConstraintChangeTest;
import liquibase.change.core.DropPrimaryKeyChangeTest;
import liquibase.change.core.DropSequenceChangeTest;
import liquibase.change.core.DropTableChangeTest;
import liquibase.change.core.DropUniqueConstraintChangeTest;
import liquibase.change.core.DropViewChangeTest;
import liquibase.change.core.InsertDataChangeTest;
import liquibase.change.core.LoadDataChangeTest;
import liquibase.change.core.LoadUpdateDataChangeTest;
import liquibase.change.core.RenameColumnChangeTest;
import liquibase.change.core.RenameTableChangeTest;
import liquibase.change.core.RenameViewChangeTest;
import liquibase.change.core.SQLFileChangeTest;
import liquibase.change.core.TagDatabaseChangeTest;
import liquibase.change.core.UpdateDataChangeTest;
import liquibase.change.custom.CustomChangeWrapperTest;
import liquibase.changelog.ChangeLogIteratorTest;
import liquibase.changelog.ChangeLogParametersTest;
import liquibase.changelog.ChangeLogParserFactoryTest;
import liquibase.changelog.ChangeSetTest;
import liquibase.changelog.ExpressionExpanderTest;
import liquibase.changelog.filter.AfterTagChangeSetFilterTest;
import liquibase.changelog.filter.AlreadyRanChangeSetFilterTest;
import liquibase.changelog.filter.ContextChangeSetFilterTest;
import liquibase.changelog.filter.CountChangeSetFilterTest;
import liquibase.changelog.filter.DbmsChangeSetFilterTest;
import liquibase.changelog.filter.ExecutedAfterChangeSetFilterTest;
import liquibase.changelog.visitor.ValidatingVisitorPreConditionsTest;
import liquibase.changelog.visitor.ValidatingVisitorTest;
import liquibase.database.DatabaseFactoryTest;
import liquibase.database.DatabaseListTest;
import liquibase.database.core.CacheDatabaseTest;
import liquibase.database.core.DB2DatabaseTest;
import liquibase.database.core.DerbyDatabaseTest;
import liquibase.database.core.HsqlDatabaseTest;
import liquibase.database.core.InformixDatabaseTest;
import liquibase.database.core.MSSQLDatabaseTest;
import liquibase.database.core.MySQLDatabaseTest;
import liquibase.database.core.OracleDatabaseTest;
import liquibase.database.core.PostgresDatabaseTest;
import liquibase.database.core.UnsupportedDatabaseTest;
import liquibase.datatype.DataTypeFactoryTest;
import liquibase.diff.DiffGeneratorFactoryTest;
import liquibase.diff.DiffResultTest;
import liquibase.diff.output.changelog.DiffToChangeLogTest;
import liquibase.exception.DuplicateChangeSetExceptionTest;
import liquibase.exception.DuplicateStatementIdentifierExceptionTest;
import liquibase.exception.ValidatorErrorsTest;
import liquibase.executor.jvm.JdbcExecutorTest;
import liquibase.lockservice.LockServiceFactoryTest;
import liquibase.logging.jvm.LogFactoryTest;
import liquibase.parser.core.formattedsql.FormattedSqlChangeLogParserTest;
import liquibase.parser.core.xml.LiquibaseEntityResolverTest;
import liquibase.parser.core.xml.XMLChangeLogSAXParserTest;
import liquibase.parser.core.yaml.YamlChangeLogParserTest;
import liquibase.precondition.PreconditionFactoryTest;
import liquibase.resource.FileSystemFileOpenerTest;
import liquibase.resource.JUnitFileOpenerTest;
import liquibase.resource.UtfBomAwareReaderTest;
import liquibase.serializer.ChangeLogSerializerFactoryTest;
import liquibase.serializer.ReflectionSerializerTest;
import liquibase.serializer.core.json.JsonChangeLogSerializerTest;
import liquibase.serializer.core.string.StringChangeLogSerializerTest;
import liquibase.serializer.core.xml.XMLChangeLogSerializerTest;
import liquibase.serializer.core.yaml.YamlChangeLogSerializerTest;
import liquibase.servicelocator.ServiceLocatorTest;
import liquibase.sqlgenerator.GeneratorLevelTest;
import liquibase.sqlgenerator.SqlGeneratorChainTest;
import liquibase.sqlgenerator.SqlGeneratorFactoryTest;
import liquibase.sqlgenerator.core.AddColumnGeneratorDefaultClauseBeforeNotNullTest;
import liquibase.sqlgenerator.core.AddColumnGeneratorTest;
import liquibase.sqlgenerator.core.AddUniqueConstraintGeneratorInformixTest;
import liquibase.sqlgenerator.core.AddUniqueConstraintGeneratorTDSTest;
import liquibase.sqlgenerator.core.AddUniqueConstraintGeneratorTest;
import liquibase.sqlgenerator.core.CommentGeneratorTest;
import liquibase.sqlgenerator.core.CreateDatabaseChangeLogLockTableGeneratorTest;
import liquibase.sqlgenerator.core.CreateTableGeneratorTest;
import liquibase.sqlgenerator.core.DropIndexGeneratorTest;
import liquibase.sqlgenerator.core.GetViewDefinitionGeneratorSybaseTest;
import liquibase.sqlgenerator.core.InsertOrUpdateGeneratorMSSQLTest;
import liquibase.sqlgenerator.core.InsertOrUpdateGeneratorOracleTest;
import liquibase.sqlgenerator.core.MarkChangeSetRanGeneratorTest;
import liquibase.sqlgenerator.core.SelectFromDatabaseChangeLogGeneratorTest;
import liquibase.sqlgenerator.core.SelectFromDatabaseChangeLogLockGeneratorTest;
import liquibase.sqlgenerator.core.UnlockDatabaseChangeLogGeneratorTest;
import liquibase.statement.AutoIncrementConstraintTest;
import liquibase.statement.core.AddAutoIncrementStatementTest;
import liquibase.statement.core.AddColumnStatementTest;
import liquibase.statement.core.AddDefaultValueStatementTest;
import liquibase.statement.core.AddForeignKeyConstraintStatementTest;
import liquibase.statement.core.AddPrimaryKeyStatementTest;
import liquibase.statement.core.AddUniqueConstraintStatementTest;
import liquibase.statement.core.AlterSequenceStatementTest;
import liquibase.statement.core.CommentStatementTest;
import liquibase.statement.core.CreateDatabaseChangeLogLockTableStatementTest;
import liquibase.statement.core.CreateIndexStatementTest;
import liquibase.statement.core.CreateSequenceStatementTest;
import liquibase.statement.core.CreateTableStatementTest;
import liquibase.statement.core.CreateViewStatementTest;
import liquibase.statement.core.DeleteStatementTest;
import liquibase.statement.core.DropColumnStatementTest;
import liquibase.statement.core.DropDefaultValueStatementTest;
import liquibase.statement.core.DropForeignKeyConstraintStatementTest;
import liquibase.statement.core.DropIndexStatementTest;
import liquibase.statement.core.DropPrimaryKeyStatementTest;
import liquibase.statement.core.DropSequenceStatementTest;
import liquibase.statement.core.DropTableStatementTest;
import liquibase.statement.core.DropUniqueConstraintStatementTest;
import liquibase.statement.core.DropViewStatementTest;
import liquibase.statement.core.FindForeignKeyConstraintsStatementTest;
import liquibase.statement.core.InsertOrUpdateStatementTest;
import liquibase.statement.core.InsertStatementTest;
import liquibase.statement.core.RawSqStatementTest;
import liquibase.statement.core.ReindexStatementTest;
import liquibase.statement.core.RenameColumnStatementTest;
import liquibase.statement.core.RenameTableStatementTest;
import liquibase.statement.core.RenameViewStatementTest;
import liquibase.statement.core.ReorganizeTableStatementTest;
import liquibase.statement.core.SelectFromDatabaseChangeLogLockStatementTest;
import liquibase.statement.core.SelectFromDatabaseChangeLogStatementTest;
import liquibase.statement.core.SetColumnRemarksStatementTest;
import liquibase.statement.core.SetNullableStatementTest;
import liquibase.statement.core.SetTableRemarksStatementTest;
import liquibase.statement.core.StoredProcedureStatementTest;
import liquibase.statement.core.TagDatabaseStatementTest;
import liquibase.statement.core.UpdateStatementTest;
import liquibase.structure.core.PrimaryKeyTest;
import liquibase.util.ISODateFormatTest;
import liquibase.util.MD5UtilTest;
import liquibase.util.RegexMatcherTest;
import liquibase.util.StreamUtilTest;
import liquibase.util.StringUtilsTest;
import liquibase.verify.change.VerifyChangeClassesTest;
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
    NoJavaSpecificCodeTest.class,
    LiquibaseTest.class,
    AddAutoIncrementChangeTest.class,
    AddColumnChangeTest.class,
    AddDefaultValueChangeTest.class,
    AddForeignKeyConstraintChangeTest.class,
    AddLookupTableChangeTest.class,
    AddNotNullConstraintChangeTest.class,
    AddPrimaryKeyChangeTest.class,
    AddUniqueConstraintChangeTest.class,
    AlterSequenceChangeTest.class,
    CreateIndexChangeTest.class,
    CreateSequenceChangeTest.class,
    CreateTableChangeTest.class,
    CreateViewChangeTest.class,
    DropColumnChangeTest.class,
    DropDefaultValueChangeTest.class,
    DropForeignKeyConstraintChangeTest.class,
    DropIndexChangeTest.class,
    DropNotNullConstraintChangeTest.class,
    DropPrimaryKeyChangeTest.class,
    DropSequenceChangeTest.class,
    DropTableChangeTest.class,
    DropUniqueConstraintChangeTest.class,
    DropViewChangeTest.class,
    InsertDataChangeTest.class,
    LoadDataChangeTest.class,
    LoadUpdateDataChangeTest.class,
    RenameColumnChangeTest.class,
    RenameTableChangeTest.class,
    RenameViewChangeTest.class,
    SQLFileChangeTest.class,
    TagDatabaseChangeTest.class,
    UpdateDataChangeTest.class,
    CustomChangeWrapperTest.class,
    AbstractChangeTest.class,
    AbstractSQLChangeTest.class,
    BaseSQLChangeTest.class,
    ChangeFactoryTest.class,
    ChangeMetaDataTest.class,
    ChangeParameterMetaDataTest.class,
    CheckSumTest.class,
    ColumnConfigTest.class,
    ConstraintsConfigTest.class,

    // package liquibase.changelog
    AfterTagChangeSetFilterTest.class,
    AlreadyRanChangeSetFilterTest.class,
    ContextChangeSetFilterTest.class,
    CountChangeSetFilterTest.class,
    DbmsChangeSetFilterTest.class,
    ExecutedAfterChangeSetFilterTest.class,
    ValidatingVisitorPreConditionsTest.class,
    ValidatingVisitorTest.class,
    ChangeLogIteratorTest.class,
    ChangeLogParametersTest.class,
    ChangeLogParserFactoryTest.class,
    ChangeSetTest.class,
    ExpressionExpanderTest.class,

    // package liquibase.database
    CacheDatabaseTest.class,
    DB2DatabaseTest.class,
    DerbyDatabaseTest.class,
    HsqlDatabaseTest.class,
    InformixDatabaseTest.class,
    MSSQLDatabaseTest.class,
    MySQLDatabaseTest.class,
    OracleDatabaseTest.class,
    PostgresDatabaseTest.class,
    UnsupportedDatabaseTest.class,
    DatabaseFactoryTest.class,
    DatabaseListTest.class,

    // package liquibase.datatype
    DataTypeFactoryTest.class,

    // package liquibase diff
    DiffToChangeLogTest.class,
    DiffGeneratorFactoryTest.class,
    DiffResultTest.class,

    // package liquibase.exception
    DuplicateChangeSetExceptionTest.class,
    DuplicateStatementIdentifierExceptionTest.class,
    ValidatorErrorsTest.class,

    // package liquibase.executor
    JdbcExecutorTest.class,

    // package liquibase.lockservice
    LockServiceFactoryTest.class,

    // package liquibase.logging
    LogFactoryTest.class,

    // package liquibase.parser
    FormattedSqlChangeLogParserTest.class,
    LiquibaseEntityResolverTest.class,
    XMLChangeLogSAXParserTest.class,
    YamlChangeLogParserTest.class,
    ChangeLogParserFactoryTest.class,

    // package liquibase.precondition
    PreconditionFactoryTest.class,

    // package liquibase.resource
    FileSystemFileOpenerTest.class,
    JUnitFileOpenerTest.class,
    UtfBomAwareReaderTest.class,

    //package liquibase.serializer
    JsonChangeLogSerializerTest.class,
    StringChangeLogSerializerTest.class,
    XMLChangeLogSerializerTest.class,
    YamlChangeLogSerializerTest.class,
    ChangeLogSerializerFactoryTest.class,
    ReflectionSerializerTest.class,

    // package liquibase.servicelocator
    ServiceLocatorTest.class,

    // package liquibase.sqlgenerator
    AddColumnGeneratorDefaultClauseBeforeNotNullTest.class,
    AddColumnGeneratorTest.class,
    AddUniqueConstraintGeneratorTest.class,
    AddUniqueConstraintGeneratorInformixTest.class,
    AddUniqueConstraintGeneratorTDSTest.class,
    CommentGeneratorTest.class,
    CreateDatabaseChangeLogLockTableGeneratorTest.class,
    CreateTableGeneratorTest.class,
    DropIndexGeneratorTest.class,
    GetViewDefinitionGeneratorSybaseTest.class,
    InsertOrUpdateGeneratorMSSQLTest.class,
    InsertOrUpdateGeneratorOracleTest.class,
    MarkChangeSetRanGeneratorTest.class,
    SelectFromDatabaseChangeLogGeneratorTest.class,
    SelectFromDatabaseChangeLogLockGeneratorTest.class,
    UnlockDatabaseChangeLogGeneratorTest.class,
    GeneratorLevelTest.class,
    SqlGeneratorChainTest.class,
    SqlGeneratorFactoryTest.class,

    // package liquibase.statement
    AddAutoIncrementStatementTest.class,
    AddColumnStatementTest.class,
    AddDefaultValueStatementTest.class,
    AddForeignKeyConstraintStatementTest.class,
    AddPrimaryKeyStatementTest.class,
    AddUniqueConstraintStatementTest.class,
    AlterSequenceStatementTest.class,
    CommentStatementTest.class,
    CreateDatabaseChangeLogLockTableStatementTest.class,
    CreateIndexStatementTest.class,
    CreateSequenceStatementTest.class,
    CreateTableStatementTest.class,
    CreateViewStatementTest.class,
    DeleteStatementTest.class,
    DropColumnStatementTest.class,
    DropDefaultValueStatementTest.class,
    DropForeignKeyConstraintStatementTest.class,
    DropIndexStatementTest.class,
    DropPrimaryKeyStatementTest.class,
    DropSequenceStatementTest.class,
    DropTableStatementTest.class,
    DropUniqueConstraintStatementTest.class,
    DropViewStatementTest.class,
    FindForeignKeyConstraintsStatementTest.class,
    InsertOrUpdateStatementTest.class,
    InsertStatementTest.class,
    RawSqStatementTest.class,
    ReindexStatementTest.class,
    RenameColumnStatementTest.class,
    RenameTableStatementTest.class,
    RenameViewStatementTest.class,
    ReorganizeTableStatementTest.class,
    SelectFromDatabaseChangeLogLockStatementTest.class,
    SelectFromDatabaseChangeLogStatementTest.class,
    SetNullableStatementTest.class,
    SetColumnRemarksStatementTest.class,
    SetTableRemarksStatementTest.class,
    StoredProcedureStatementTest.class,
    TagDatabaseStatementTest.class,
    UpdateStatementTest.class,
    AutoIncrementConstraintTest.class,

    // package liquibase.structure
    PrimaryKeyTest.class,

    // package liquibase.util
    ISODateFormatTest.class,
    MD5UtilTest.class,
    RegexMatcherTest.class,
    StreamUtilTest.class,
    StringUtilsTest.class,

    VerifyChangeClassesTest.class,

    LiquibaseTest.class,
    NoJavaSpecificCodeTest.class,
})
public class UnitTests {

    /**
     * Initializes a new {@link UnitTests}.
     */
    public UnitTests() {
        super();
    }

}
