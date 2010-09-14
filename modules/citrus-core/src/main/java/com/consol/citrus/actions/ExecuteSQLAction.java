/*
 * Copyright 2006-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.consol.citrus.actions;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.util.SQLUtils;

/**
 * Test action execute SQL statements. Use this action when executing
 * database altering statements like UPDATE, INSERT, ALTER, DELETE. Statements are either
 * embedded inline in the test case description or given by an external file resource.
 * 
 * When executing SQL query statements (SELECT) see {@link ExecuteSQLQueryAction}.
 * 
 * @author Christoph Deppisch, Jan Szczepanski
 * @since 2006
 */
public class ExecuteSQLAction extends AbstractDatabaseConnectingTestAction {
    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(ExecuteSQLAction.class);

    /** SQL file resource */
    private Resource sqlResource;

    /** List of SQL statements */
    private List<String> statements = new ArrayList<String>();

    /** boolean flag marking that possible SQL errors will be ignored */
    private boolean ignoreErrors = false;

    /**
     * @see com.consol.citrus.TestAction#execute(TestContext)
     * @throws CitrusRuntimeException
     */
    @Override
    public void execute(TestContext context) {
        String stmt = "";

        if (statements.isEmpty()) {
            statements = SQLUtils.getStatementsFromResource(sqlResource);
        }

        Iterator<String> it = statements.iterator();
        while (it.hasNext())  {
            try {
                stmt = it.next();
                stmt = stmt.trim();
                
                if (stmt.endsWith(";")) {
                    stmt = stmt.substring(0, stmt.length()-1);
                }
                
                stmt = context.replaceDynamicContentInString(stmt);
                
                log.info("Found Sql statement " + stmt);
                getJdbcTemplate().execute(stmt);
            } catch (Exception e) {
                if (ignoreErrors) {
                    log.error("Error while executing statement " + stmt + " " + e.getLocalizedMessage());
                    continue;
                } else {
                    throw new CitrusRuntimeException(e);
                }
            }
        }
    }

    /**
     * List of statements to execute. Declared inline in the test case. 
     * @param statements
     */
    public void setStatements(List<String> statements) {
        this.statements = statements;
    }
    
    /**
     * Setter for external file resource containing the SQL statements to execute.
     * @param sqlResource
     */
    public void setSqlResource(Resource sqlResource) {
        this.sqlResource = sqlResource;
    }

    /**
     * Ignore errors during execution.
     * @param ignoreErrors boolean flag to set
     */
    public void setIgnoreErrors(boolean ignoreErrors) {
        this.ignoreErrors = ignoreErrors;
    }
}
