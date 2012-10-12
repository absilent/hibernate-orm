/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * Copyright (c) 2010, Red Hat Inc. or third-party contributors as
 * indicated by the @author tags or express copyright attribution
 * statements applied by the authors.  All third-party contributions are
 * distributed under license by Red Hat Inc.
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */
package org.hibernate.dialect;

import java.sql.SQLException;
import java.sql.Types;

import org.hibernate.MappingException;
import org.hibernate.dialect.function.NoArgSQLFunction;
import org.hibernate.dialect.function.NvlFunction;
import org.hibernate.dialect.function.SQLFunctionTemplate;
import org.hibernate.dialect.function.StandardAnsiSqlAggregationFunctions;
import org.hibernate.dialect.function.StandardSQLFunction;
import org.hibernate.dialect.function.VarArgsSQLFunction;
import org.hibernate.exception.spi.TemplatedViolatedConstraintNameExtracter;
import org.hibernate.exception.spi.ViolatedConstraintNameExtracter;
import org.hibernate.internal.util.JdbcExceptionHelper;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.sql.ANSICaseFragment;
import org.hibernate.sql.ANSIJoinFragment;
import org.hibernate.sql.CaseFragment;
import org.hibernate.sql.JoinFragment;
import org.hibernate.type.DateType;
import org.hibernate.type.StandardBasicTypes;

/**
 * Informix dialect.<br>
 * <br>
 * Seems to work with Informix Dynamic Server Version 7.31.UD3, Informix JDBC
 * driver version 2.21JC3.
 * 
 * @author Steve Molitor
 */
public class InformixDialect extends Dialect {

	/**
	 * Creates new <code>InformixDialect</code> instance. Sets up the JDBC /
	 * Informix type mappings.
	 */
	public InformixDialect() {
		super();

		registerNumericTypeMappings();
		registerDateTimeTypeMappings();
		registerBinaryTypeMappings();
		registerCharacterTypeMappings();

		registerFunctions();
	}

	protected void registerNumericTypeMappings() {
		registerColumnType(Types.BIT, "smallint"); // Informix doesn't have a
		// bit type
		registerColumnType(Types.BOOLEAN, "boolean");
		registerColumnType(Types.TINYINT, "smallint");
		registerColumnType(Types.SMALLINT, "smallint");
		registerColumnType(Types.INTEGER, "integer");
		// Prefer bigint over int8 (conserves space, more standard)
		registerColumnType(Types.BIGINT, "bigint"); // previously int8

		registerColumnType(Types.FLOAT, "smallfloat");
		registerColumnType(Types.DOUBLE, "float");
		registerColumnType(Types.NUMERIC, "decimal"); // or MONEY
		registerColumnType(Types.REAL, "smallfloat");
		registerColumnType(Types.DECIMAL, "decimal");
	}

	protected void registerDateTimeTypeMappings() {
		registerColumnType(Types.DATE, "date");
		registerColumnType(Types.TIME, "datetime hour to second");
		registerColumnType(Types.TIMESTAMP, "datetime year to fraction(5)");
	}

	protected void registerBinaryTypeMappings() {
		registerColumnType(Types.BINARY, "byte");
		// Prefer Smart-LOB types (CLOB and BLOB) over LOB types (TEXT and BYTE)
		registerColumnType(Types.VARBINARY, "blob");
		registerColumnType(Types.LONGVARBINARY, "blob"); // or BYTE
		registerColumnType(Types.BLOB, "blob");
	}

	protected void registerCharacterTypeMappings() {
		registerColumnType(Types.CHAR, "char($l)");
		registerColumnType(Types.VARCHAR, "varchar($l)");
		registerColumnType(Types.VARCHAR, 255, "varchar($l)");
		registerColumnType(Types.VARCHAR, 32739, "lvarchar($l)");
		// Prefer Smart-LOB types (CLOB and BLOB) over LOB types (TEXT and BYTE)
		registerColumnType(Types.LONGVARCHAR, "clob"); // or TEXT?
		registerColumnType(Types.CLOB, "clob");
	}

	protected void registerFunctions() {
		// Algebraic Functions
		registerFunction("abs", new StandardSQLFunction("abs"));
		//registerFunction("cbrt", new SQLFunctionTemplate(StandardBasicTypes.DOUBLE, "root($1, 3)"));
		registerFunction("ceil", new StandardSQLFunction("ceil"));
		registerFunction("floor", new StandardSQLFunction("floor"));
		registerFunction("mod", new StandardSQLFunction("mod", StandardBasicTypes.INTEGER));
		registerFunction("pow", new StandardSQLFunction("pow", StandardBasicTypes.DOUBLE));
		registerFunction("power", new StandardSQLFunction("power", StandardBasicTypes.DOUBLE));
		registerFunction("root", new SQLFunctionTemplate(StandardBasicTypes.DOUBLE, "root($1, $2)"));
		registerFunction("round", new StandardSQLFunction("round"));
		registerFunction("trunc", new StandardSQLFunction("trunc"));

		// Exponential and Logarithmic Functions
		registerFunction("exp", new StandardSQLFunction("exp", StandardBasicTypes.DOUBLE));
		registerFunction("ln", new StandardSQLFunction("ln", StandardBasicTypes.DOUBLE));
		registerFunction("logn", new StandardSQLFunction("logn", StandardBasicTypes.DOUBLE));
		registerFunction("log10", new StandardSQLFunction("log10", StandardBasicTypes.DOUBLE));

		// Trigonometric Functions
		registerFunction("acos", new StandardSQLFunction("acos", StandardBasicTypes.DOUBLE));
		registerFunction("asin", new StandardSQLFunction("asin", StandardBasicTypes.DOUBLE));
		registerFunction("atan", new StandardSQLFunction("atan", StandardBasicTypes.DOUBLE));
		registerFunction("atan2", new StandardSQLFunction("atan", StandardBasicTypes.DOUBLE));
		registerFunction("degrees", new StandardSQLFunction("degrees", StandardBasicTypes.DOUBLE));
		registerFunction("cos", new StandardSQLFunction("cos", StandardBasicTypes.DOUBLE));
		registerFunction("radians", new StandardSQLFunction("radians", StandardBasicTypes.DOUBLE));
		registerFunction("sin", new StandardSQLFunction("sin", StandardBasicTypes.DOUBLE));
		registerFunction("tan", new StandardSQLFunction("tan", StandardBasicTypes.DOUBLE));
		// Hyperbolic functions are available in 11.70.xC6 and later
		if (isVersionPost1170xC6()) {
			registerFunction("acosh", new StandardSQLFunction("acosh", StandardBasicTypes.DOUBLE));
			registerFunction("asinh", new StandardSQLFunction("asinh", StandardBasicTypes.DOUBLE));
			registerFunction("atanh", new StandardSQLFunction("atanh", StandardBasicTypes.DOUBLE));
			registerFunction("cosh", new StandardSQLFunction("cosh", StandardBasicTypes.DOUBLE));
			registerFunction("sinh", new StandardSQLFunction("sinh", StandardBasicTypes.DOUBLE));
			registerFunction("tanh", new StandardSQLFunction("tanh", StandardBasicTypes.DOUBLE));
		}

		// Aggregate Expressions
		registerFunction("avg", new StandardAnsiSqlAggregationFunctions.AvgFunction());
		registerFunction("count", new StandardAnsiSqlAggregationFunctions.CountFunction());
		registerFunction("max", new StandardAnsiSqlAggregationFunctions.MaxFunction());
		registerFunction("min", new StandardAnsiSqlAggregationFunctions.MinFunction());
		registerFunction("sum", new StandardAnsiSqlAggregationFunctions.SumFunction());
		registerFunction("range", new StandardSQLFunction("range"));
		registerFunction("stdev", new StandardSQLFunction("stdev", StandardBasicTypes.DOUBLE)); // Informix only uses a single 'd' 
		registerFunction("stddev", new StandardSQLFunction("stdev", StandardBasicTypes.DOUBLE)); // Alias for 'stdev'
		registerFunction("variance", new StandardSQLFunction("variance", StandardBasicTypes.DOUBLE));

		// Misc Math functions
		if (isVersionPost1170xC6()) {
			registerFunction("sign", new StandardSQLFunction("sign", StandardBasicTypes.INTEGER));
		}

		// Conversions
		registerFunction("hex", new StandardSQLFunction("hex", StandardBasicTypes.STRING));
		registerFunction("to_number", new StandardSQLFunction("to_number", StandardBasicTypes.BIG_DECIMAL));
		
		// String Manipulation Functions
		registerFunction("ascii", new StandardSQLFunction("ascii", StandardBasicTypes.INTEGER));
		registerFunction("concat", new VarArgsSQLFunction(StandardBasicTypes.STRING, "(", "||", ")"));
		registerFunction("lpad", new StandardSQLFunction("lpad", StandardBasicTypes.STRING));
		registerFunction("ltrim", new StandardSQLFunction("ltrim", StandardBasicTypes.STRING));
		registerFunction("replace", new StandardSQLFunction("replace", StandardBasicTypes.STRING));
		registerFunction("reverse", new StandardSQLFunction("reverse", StandardBasicTypes.STRING));
		registerFunction("rpad", new StandardSQLFunction("rpad", StandardBasicTypes.STRING));
		registerFunction("rtrim", new StandardSQLFunction("rtrim", StandardBasicTypes.STRING));
		registerFunction("space", new StandardSQLFunction("space", StandardBasicTypes.STRING));
		registerFunction("trim", new StandardSQLFunction("trim", StandardBasicTypes.STRING));

		// 'translate' is equivalent to the UNIX 'tr' program
		// registerFunction("translate", new StandardSQLFunction("translate", StandardBasicTypes.STRING));
		
		// Substring Functions
		registerFunction("charindex", new SQLFunctionTemplate(StandardBasicTypes.INTEGER, "charindex(?1, ?2, ?3)" ) );
		registerFunction("instr", new StandardSQLFunction("instr", StandardBasicTypes.INTEGER));
		registerFunction("left", new StandardSQLFunction("left", StandardBasicTypes.STRING));
		registerFunction("locate", new SQLFunctionTemplate(StandardBasicTypes.INTEGER, "instr(?2,?1)"));
		registerFunction("right", new StandardSQLFunction("right", StandardBasicTypes.STRING));
		registerFunction("substr", new StandardSQLFunction("substr", StandardBasicTypes.STRING));
		registerFunction("substring", new SQLFunctionTemplate( StandardBasicTypes.STRING, "substring(?1 FROM ?2 FOR ?3)"));
		registerFunction("substring_index", new SQLFunctionTemplate(StandardBasicTypes.STRING, "substring_index(?1, ?2, ?3)"));
				
		// Case Conversion Functions
		registerFunction("upper", new StandardSQLFunction("upper"));
		registerFunction("lower", new StandardSQLFunction("lower"));
		registerFunction("initcap", new StandardSQLFunction("initcap"));

		// Bitwise Logical Functions
		// To correctly determine the return type, Hibernate would need to make
		// available all arguments, not just the first, to
		// SQLFunction#getReturnType as the return type depends on the wider of
		// the first and second arguments.
		registerFunction("bitand", new StandardSQLFunction("bitand"));
		registerFunction("bitor", new StandardSQLFunction("bitor"));
		registerFunction("bitxor", new StandardSQLFunction("bitxor"));
		registerFunction("bitandnot", new StandardSQLFunction("bitandnot"));
		registerFunction("bitnot", new StandardSQLFunction("bitnot"));
		
		// Length Functions
		registerFunction("len", new StandardSQLFunction("len", StandardBasicTypes.INTEGER));
		registerFunction("length", new StandardSQLFunction("length", StandardBasicTypes.INTEGER));
		registerFunction("char_length", new StandardSQLFunction("char_length", StandardBasicTypes.INTEGER));
		registerFunction("character_length", new StandardSQLFunction("character_length", StandardBasicTypes.INTEGER));
		registerFunction("octet_length", new StandardSQLFunction("octet_length", StandardBasicTypes.INTEGER));

		// Date & Time Functions
		registerFunction("to_char", new StandardSQLFunction("to_char", StandardBasicTypes.STRING));
		registerFunction("to_date", new StandardSQLFunction("to_date", StandardBasicTypes.TIMESTAMP));
		registerFunction("last_day", new StandardSQLFunction("last_day", StandardBasicTypes.DATE));
		registerFunction("add_months", new StandardSQLFunction("add_months", StandardBasicTypes.DATE));
		registerFunction("months_between", new StandardSQLFunction("months_between", StandardBasicTypes.FLOAT));
		registerFunction("next_day", new StandardSQLFunction("next_day", StandardBasicTypes.DATE));

		// Time Component Extraction Functions
		registerFunction("year", new SQLFunctionTemplate(StandardBasicTypes.INTEGER, "year(?1)"));
		registerFunction("month", new SQLFunctionTemplate(StandardBasicTypes.INTEGER, "month(?1)"));
		registerFunction("day", new SQLFunctionTemplate(StandardBasicTypes.INTEGER, "day(?1)"));
		// hour, minute, and second functions are emulated
		registerFunction("hour", new SQLFunctionTemplate(StandardBasicTypes.INTEGER, "extend(?1, hour to hour)::char(2)::integer"));
		registerFunction("minute", new SQLFunctionTemplate(StandardBasicTypes.INTEGER, "extend(?1, minute to minute)::char(2)::integer"));
		registerFunction("second", new SQLFunctionTemplate(StandardBasicTypes.INTEGER, "extend(?1, second to second)::char(2)::integer"));
		registerFunction("quarter", new SQLFunctionTemplate(StandardBasicTypes.INTEGER, "trunc((month(?1)+2)/3)")); // may need to add ::integer at the end

		if (Boolean.getBoolean("org.hibernate.dialect.InformixDialect.enableCurrentDateFunction")) {
			if (Boolean.getBoolean("org.hibernate.dialect.InformixDialect.useSysdualForCurrentDateFunction")) {
				// Alternate version of current_date using sysmaster's sysdual
				// table to avoid using "first 1" or "distinct"
				registerFunction("current_date", new NoArgSQLFunction("(select today from sysmaster:sysdual)", new DateType(), false));
			} else {
				// Huge hack to combat the fact that Informix does not have a
				// current_date function. This will probably fail in weird edge
				// cases.
				registerFunction("current_date", new NoArgSQLFunction("(select first 1 today from informix.systables)", new DateType(), false));
			}
		}

		registerFunction("current_timestamp", new NoArgSQLFunction("select first 1 current from informix.systables", StandardBasicTypes.TIMESTAMP));
		registerFunction("current_time", new NoArgSQLFunction( "select first 1 current hour to second from informix.systables", StandardBasicTypes.TIME));
		
		// Misc functions
		registerFunction("coalesce", new NvlFunction());
		registerFunction("nvl", new StandardSQLFunction("nvl"));
		if (isVersionPost1170xC6()) {
			registerFunction("nvl2", new StandardSQLFunction("nvl2"));
		}

		registerFunction("str", new StandardSQLFunction("to_char", StandardBasicTypes.STRING)); // str uses to_char
	}
	
	private static boolean isVersionPost1170xC6() {
		return Boolean.getBoolean("org.hibernate.dialect.InformixDialect.isVersion1170xC6");
	}

	// IDENTITY support ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	/**
	 * Informix supports identity columns through the SERIAL and SERIAL8 types.
	 * Informix also supports sequences to generated identity values. Hibernate
	 * iterates through strategies, picking the first that returns true. To
	 * ensure sequences are used, report false here.
	 */
	@Override
	public boolean supportsIdentityColumns() {
		return false;
	}

	@Override
	public boolean hasDataTypeInIdentityColumn() {
		return false;
	}

	@Override
	public String getIdentitySelectString(String table, String column, int type)
			throws MappingException {
		return type == Types.BIGINT ? "select dbinfo('bigserial') from systables where tabid=1"
				: "select dbinfo('sqlca.sqlerrd1') from systables where tabid=1";
	}

	@Override
	public String getIdentityColumnString(int type) throws MappingException {
		// return "generated by default as identity"; //not null ... (start with
		// 1) is implied
		return type == Types.BIGINT ? "bigserial not null" : "serial not null";
	}

	@Override
	public String getIdentityInsertString() {
		return "0";
	}

	// SEQUENCE support ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	@Override
	public boolean supportsSequences() {
		return true;
	}

	@Override
	public boolean supportsPooledSequences() {
		return true;
	}

	@Override
	public String getSequenceNextValString(String sequenceName) {
		return "select " + getSelectSequenceNextValString(sequenceName)
				+ " from systables where tabid=1";
	}

	@Override
	public String getSelectSequenceNextValString(String sequenceName) {
		return sequenceName + ".nextval";
	}

	@Override
	public String getCreateSequenceString(String sequenceName) {
		return "create sequence " + sequenceName;
	}

	@Override
	public String getDropSequenceString(String sequenceName) {
		// return "drop sequence " + sequenceName + " restrict";
		return "drop sequence " + sequenceName;
	}

	/**
	 * Informix treats sequences like a table from the standpoint of naming.
	 * Therefore, to retrieve the sequence name we must perform a join between
	 * systables and syssequences on the {@code}tabid column.
	 */
	@Override
	public String getQuerySequencesString() {
		return "select systables.tabname from systables,syssequences where systables.tabid = syssequences.tabid";
	}

	// GUID support ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	/*
	 * Informix does not have built-in support for this type of operation.
	 * However, stored-procedures, ideally C-UDRs, can be used to make this
	 * happen. Jacques Roy has authored a developerWorks article on this.
	 */

	// limit/offset support ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	@Override
	public boolean supportsLimit() {
		return true;
	}

	@Override
	public boolean supportsLimitOffset() {
		return true;
	}

	@Override
	public boolean supportsVariableLimit() {
		return false;
	}

	/**
	 * Previously incorrectly claimed that Informix Dynamic Server (IDS)
	 * required using the maximim row number for the limit. Overriding
	 * explicitly so that user's of this dialect are aware (even though 'false'
	 * is the default inherited from Dialect).
	 */
	@Override
	public boolean useMaxForLimit() {
		return false;
	}

	@Override
	public String getLimitString(String query, int offset, int limit) {
		/*
		 * SQL Syntax: SELECT FIRST <limit> ... SELECT SKIP <offset> FIRST
		 * <limit> ...
		 */

		if (offset < 0 || limit < 0) {
			throw new IllegalArgumentException(
					"Cannot perform limit query with negative limit and/or offset value(s)");
		}

		StringBuffer limitQuery = new StringBuffer(query.length() + 10);
		limitQuery.append(query);
		int indexOfEndOfSelect = query.toLowerCase().indexOf("select") + 6;

		if (offset == 0) {
			limitQuery.insert(indexOfEndOfSelect, " first " + limit);
		} else {
			limitQuery.insert(indexOfEndOfSelect, " skip " + offset + " first "
					+ limit);
		}
		return limitQuery.toString();
	}

	// temporary table support ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	/**
	 * Overrides {@link Dialect#supportsTemporaryTables()} to return
	 * {@code true} since Informix does, in fact, support temporary tables.
	 * 
	 * @return {@code true} when invoked
	 */
	@Override
	public boolean supportsTemporaryTables() {
		return true;
	}

	/**
	 * Overrides {@link Dialect#getCreateTemporaryTableString()} to return
	 * {@code create temp table}.
	 * 
	 * @return {@code create temp table} when invoked
	 */
	@Override
	public String getCreateTemporaryTableString() {
		return "create temp table";
	}

	// callable statement support ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	// current timestamp support ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	@Override
	public boolean supportsCurrentTimestampSelection() {
		return true;
	}

	@Override
	public boolean isCurrentTimestampSelectStringCallable() {
		return false;
	}

	@Override
	public String getCurrentTimestampSelectString() {
		return "select distinct current timestamp from informix.systables";
	}

	// SQLException support ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	@Override
	public ViolatedConstraintNameExtracter getViolatedConstraintNameExtracter() {
		return EXTRACTER;
	}

	private static ViolatedConstraintNameExtracter EXTRACTER = new TemplatedViolatedConstraintNameExtracter() {

		/**
		 * Extract the name of the violated constraint from the given
		 * SQLException.
		 * 
		 * @param sqle
		 *            The exception that was the result of the constraint
		 *            violation.
		 * @return The extracted constraint name.
		 */
		public String extractConstraintName(SQLException sqle) {
			String constraintName = null;

			int errorCode = JdbcExceptionHelper.extractErrorCode(sqle);
			if (errorCode == -268) {
				constraintName = extractUsingTemplate("Unique constraint (",
						") violated.", sqle.getMessage());
			} else if (errorCode == -691) {
				constraintName = extractUsingTemplate(
						"Missing key in referenced table for referential constraint (",
						").", sqle.getMessage());
			} else if (errorCode == -692) {
				constraintName = extractUsingTemplate(
						"Key value for constraint (",
						") is still being referenced.", sqle.getMessage());
			}

			if (constraintName != null) {
				// strip table-owner because Informix always returns constraint
				// names as "<table-owner>.<constraint-name>"
				int i = constraintName.indexOf('.');
				if (i != -1) {
					constraintName = constraintName.substring(i + 1);
				}
			}

			return constraintName;
		}

	};

	// union subclass support ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	/**
	 * Does this dialect support UNION ALL, which is generally a faster variant
	 * of UNION?
	 * 
	 * @return True if UNION ALL is supported; false otherwise.
	 */
	@Override
	public boolean supportsUnionAll() {
		return true;
	}

	// miscellaneous support ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	/**
	 * Create a {@link org.hibernate.sql.JoinFragment} strategy responsible for
	 * handling this dialect's variations in how joins are handled.
	 * 
	 * @return This dialect's {@link org.hibernate.sql.JoinFragment} strategy.
	 */
	@Override
	public JoinFragment createOuterJoinFragment() {
		return new ANSIJoinFragment();
	}

	/**
	 * Create a {@link org.hibernate.sql.CaseFragment} strategy responsible for
	 * handling this dialect's variations in how CASE statements are handled.
	 * 
	 * @return This dialect's {@link org.hibernate.sql.CaseFragment} strategy.
	 */
	@Override
	public CaseFragment createCaseFragment() {
		return new ANSICaseFragment();
	}

	/**
	 * The fragment used to insert a row without specifying any column values.
	 * Informix does not support this concept at present.
	 * 
	 * @return The appropriate empty values clause.
	 */
	@Override
	public String getNoColumnsInsertString() {
		return "values (0)";
	}

	/**
	 * Overrides {@link InformixDialect#toBooleanValueString(boolean)} to return
	 * {@code t} or {@code f}, not {@code 1} or {@code 0}.
	 * 
	 * @param value
	 *            the {@code boolean} value to translate
	 * 
	 * @return {@code t} or {@code f} if {@code value} is {@code true} or
	 *         {@code false} respectively
	 * 
	 * @see <a href="https://hibernate.onjira.com/browse/HHH-3551">HHH-3551</a>
	 */
	@Override
	public String toBooleanValueString(final boolean value) {
		return value ? "'t'" : "'f'";
	}

	// DDL support ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	@Override
	public String getAddColumnString() {
		return "add";
	}

	/**
	 * The syntax used to add a primary key constraint to a table. Informix
	 * constraint name must be at the end.
	 * 
	 * @return String
	 */
	@Override
	public String getAddPrimaryKeyConstraintString(String constraintName) {
		return " add constraint primary key constraint " + constraintName + " ";
	}

	/**
	 * The syntax used to add a foreign key constraint to a table. Informix
	 * constraint name must be at the end.
	 * 
	 * @return String
	 */
	@Override
	public String getAddForeignKeyConstraintString(String constraintName,
			String[] foreignKey, String referencedTable, String[] primaryKey,
			boolean referencesPrimaryKey) {
		StringBuilder result = new StringBuilder(30).append(" add constraint ")
				.append(" foreign key (")
				.append(StringHelper.join(", ", foreignKey))
				.append(") references ").append(referencedTable);

		if (!referencesPrimaryKey) {
			result.append(" (").append(StringHelper.join(", ", primaryKey))
			.append(')');
		}

		result.append(" constraint ").append(constraintName);

		return result.toString();
	}

	/**
	 * Overrides {@link Dialect#getCreateTemporaryTablePostfix()} to return "
	 * {@code with no log}" when invoked.
	 * 
	 * @return "{@code with no log}" when invoked
	 */
	@Override
	public String getCreateTemporaryTablePostfix() {
		return "with no log";
	}

}
