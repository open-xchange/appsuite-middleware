package liquibase.sqlgenerator.core;

import static org.junit.Assert.assertEquals;
import java.util.SortedSet;
import java.util.TreeSet;
import org.junit.Test;
import liquibase.database.Database;
import liquibase.database.core.PostgresDatabase;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.SqlGenerator;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.DropIndexStatement;


public class DropIndexGeneratorTest {

	@Test
	public void shouldDropIndexInPostgreSQL() throws Exception {
		DropIndexGenerator dropIndexGenerator = new DropIndexGenerator();
		DropIndexStatement statement = new DropIndexStatement("indexName", "defaultCatalog", "defaultSchema", "aTable", null);
		Database database = new PostgresDatabase();
		SortedSet<SqlGenerator> sqlGenerators = new TreeSet<SqlGenerator>();
		SqlGeneratorChain sqlGenerationChain = new SqlGeneratorChain(sqlGenerators);
		Sql[] sqls = dropIndexGenerator.generateSql(statement, database, sqlGenerationChain);
		assertEquals("DROP INDEX \"defaultSchema\".\"indexName\"", sqls[0].toSql());

		statement = new DropIndexStatement("index_name", "default_catalog", "default_schema", "a_table", null);
		sqls = dropIndexGenerator.generateSql(statement, database, sqlGenerationChain);
		assertEquals("DROP INDEX default_schema.index_name", sqls[0].toSql());

		statement = new DropIndexStatement("index_name", null, null, "a_table", null);
		sqls = dropIndexGenerator.generateSql(statement, database, sqlGenerationChain);
		assertEquals("DROP INDEX index_name", sqls[0].toSql());
	}
}
