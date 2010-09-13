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

package com.consol.citrus.report;

import java.util.List;

import org.springframework.util.StringUtils;

import com.consol.citrus.TestCaseMetaInfo;


/**
 * Class representing test results (failed, successful, skipped)
 * 
 * @author Christoph Deppisch
 */
public class TestResult {

    /** Possible test results */
    public static enum RESULT {SUCCESS, FAILURE, SKIP};

    /** Actual result */
    private RESULT result;
    
    /** Name of the test */
    private String testName;
    
    /** Name of the suite */
    private String suiteName;
    
    /** Meta Information of the test */
    private TestCaseMetaInfo testInformation;
    
    /** Description of the test */
    private String testDescription;
    
    /** List of TestAction names */
    private List<String> testActionNames;
    
    /** Index of the failed TestAction */
    private int failedActionIndex;

    /** Failure cause */
    private Throwable cause;
    
    /**
     * Constructor using fields.
     * @param name
     * @param result
     */
    public TestResult(String name, RESULT result) {
        this.testName = name;
        this.result = result;
    }

    /**
     * Constructor using fields.
     * @param name
     * @param result
     * @param cause
     */
    public TestResult(String name, RESULT result, Throwable cause) {
        this.testName = name;
        this.result = result;
        this.cause = cause;
    }
        
    /**
     * Constructor using all fields, for a detailed TestResult.
     * @param name
     * @param suite
     * @param testInformation
     * @param testDescription
     * @param testActionNames
     * @param result
     */
    public TestResult(String name, String suite, TestCaseMetaInfo testInformation, String testDescription, List<String> testActionNames, RESULT result) {
        this.testName = name;
        this.suiteName = suite;
        this.testInformation = testInformation;
        this.testDescription = testDescription;
        this.testActionNames = testActionNames;
        this.result = result;
    }
    
    /**
     * Constructor using all fields, for a detailed TestResult.
     * @param name
     * @param suite
     * @param testInformation
     * @param testDescription
     * @param testActionNames
     * @param result
     * @param failedActionName
     * @param cause
     */
    public TestResult(String name, String suite, TestCaseMetaInfo testInformation, String testDescription, List<String> testActionNames, RESULT result, int failedActionIndex, Throwable cause) {
        this.testName = name;
        this.suiteName = suite;
        this.testInformation = testInformation;
        this.testDescription = testDescription;
        this.testActionNames = testActionNames;
        this.result = result;
        this.failedActionIndex = failedActionIndex;
        this.cause = cause;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        builder.append(" " + testName + " ");

        int spaces = 62 - testName.length();
        for (int i = 0; i < spaces; i++) {
            builder.append(".");
        }
        
        switch (result) {
            case SUCCESS:
                builder.append(" SUCCESS");
                break;
            case SKIP:
                builder.append(" SKIPPED");
                break;
            case FAILURE:
                builder.append(" FAILED");
                
                if(cause!= null && StringUtils.hasText(cause.getLocalizedMessage())) {
                    builder.append("\n FAILED! Caused by: \n " + cause.getClass().getName() + ": " +  cause.getLocalizedMessage());
                } else {
                    builder.append("\n FAILED! Caused by: Unknown error");
                }
                
                builder.append("\n");
                
                break;
            default:
                break;
        }

        return builder.toString();
    }

    /**
     * Setter for the failure cause.
     * @param cause the cause to set
     */
    public void setCause(Throwable cause) {
        this.cause = cause;
    }

    /**
     * Getter for the failure cause.
     * @return the cause
     */
    public Throwable getCause() {
        return cause;
    }
    
    /**
     * Getter for the test name.
     * @return the testName
     */
    public String getTestName() {
        return testName;
    }

    /**
     * Setter for the test name.
     * @param testName the testName to set
     */
    public void setTestName(String testName) {
        this.testName = testName;
    }

    /**
     * Getter for the suite name
     * @return the suiteName
     */
    public String getSuiteName() {
    	return suiteName;
    }

	/**
	 * Setter for the suite name
     * @param suiteName the suiteName to set
     */
    public void setSuiteName(String suiteName) {
    	this.suiteName = suiteName;
    }

	/**
	 * Getter for the test meta information
     * @return the testInformation
     */
    public TestCaseMetaInfo getTestInformation() {
        return testInformation;
    }

    /**
     * Setter for the test meta information
     * @param testInformation the testInformation to set
     */
    public void setTestInformation(TestCaseMetaInfo testInformation) {
        this.testInformation = testInformation;
    }

    /**
     * Getter for the test description
     * @return the testDescription
     */
    public String getTestDescription() {
        return testDescription;
    }

    /**
     * Setter for the test description
     * @param testDescription the testDescription to set
     */
    public void setTestDescription(String testDescription) {
        this.testDescription = testDescription;
    }

    /**
     * Getter for test result.
     * @return the result
     */
    public RESULT getResult() {
        return result;
    }

    /**
     * Setter for the test result.
     * @param result the result to set
     */
    public void setResult(RESULT result) {
        this.result = result;
    }

    /**
     * Setter for TestAction names
     * @param testActionNames the testActionNames to set
     */
    public void setTestActionNames(List<String> testActionNames) {
        this.testActionNames = testActionNames;
    }

    /**
     * Getter for TestAction names
     * @return the testActionNames
     */
    public List<String> getTestActionNames() {
        return testActionNames;
    }

    /**
     * Setter for Failed TestAction name
     * @param failedActionName the failedActionName to set
     */
    public void setFailedActionIndex(int failedActionIndex) {
        this.failedActionIndex = failedActionIndex;
    }

    /**
     * Getter for Failed TestAction name
     * @return the failedActionName
     */
    public int getFailedActionIndex() {
        return failedActionIndex;
    }
}
