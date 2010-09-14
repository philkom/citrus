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

package com.consol.citrus.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

import com.consol.citrus.CitrusConstants;
import com.consol.citrus.exceptions.CitrusRuntimeException;

/**
 * Class that provides utilities for SQL.
 * 
 * @author Philipp Komninos
 * @since 14.09.2010
 */
public class SQLUtils {
    
    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(SQLUtils.class);
    
    /**
     * Prevent instantiation.
     */
    private SQLUtils() {
    }
    
    /** 
     * Extracts multilined SQL statements from a given resource file
     * 
     * @param sqlResource
     * @return
     */
    public static List<String> getStatementsFromResource(Resource sqlResource) {
        BufferedReader reader = null;
        String line = "";
        StringBuffer buffer = new StringBuffer();
        String stmt = "";
        List statements = new ArrayList<String>();
        
        log.info("Executing Sql file: " + sqlResource.getFilename());
        
        try {
            reader = new BufferedReader(new InputStreamReader(sqlResource.getInputStream()));
            while (reader.ready()) {
                line = reader.readLine();

                if (line != null && line.trim() != null && !line.trim().startsWith(CitrusConstants.SQL_COMMENT_PREFIX) && line.trim().length() > 0) {
                    buffer.append(line);
                    if (line.trim().endsWith(";")) {
                        stmt = buffer.toString();
                        buffer = new StringBuffer();

                        if(log.isDebugEnabled()) {
                            log.debug("Found statement: " + stmt);
                        }

                        statements.add(stmt);
                    }
                }
            }
        } catch (IOException e) {
            log.error("SQL resource could not be found - filename: "
                    + sqlResource.getFilename() + ". Nested Exception is: ");
            log.error(e.getLocalizedMessage());
            throw new CitrusRuntimeException(e);
        } finally {
            if(reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    log.warn("Warning: Error while closing reader instance", e);
                }
            }
        }
        
        return statements;
    }
}
