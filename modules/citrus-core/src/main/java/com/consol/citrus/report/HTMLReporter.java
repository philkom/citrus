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

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Date;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;

import com.consol.citrus.TestAction;
import com.consol.citrus.TestCase;
import com.consol.citrus.TestSuite;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.report.TestResult.RESULT;
import com.consol.citrus.util.FileUtils;

/**
 * Logging reporter generating a HTML site with detailed test results.
 * 
 * @author Philipp Komninos
 */
public class HTMLReporter implements TestSuiteListener, TestListener, TestReporter {
    
    /** Collect test results for test report */
    private TestResults testResults = new TestResults();
    
    /** Name of the actual TestSuite */
    private String executingTestSuite;
    
    /** Static resource for the HTML test report template */
    private static final Resource htmlTestReportTemplateResource = new ClassPathResource("com/consol/citrus/report/citrus-test-report-template.html");
    
    /** HTML template resource for a TestSuite, which contains Placeholders; has TestCases as sub elements */
    private static final Resource testSuiteTemplateResource = new ClassPathResource("com/consol/citrus/report/testsuite-template.html");
    
    /** HTML template resource for a TestCase, which contains Placeholders; has TestActions and StackTrace as sub elements */
    private static final Resource testCaseTemplateResource = new ClassPathResource("com/consol/citrus/report/testcase-template.html");
    
    /** HTML template resource for a TestAction, which contains Placeholders */
    private static final Resource testActionTemplateResource = new ClassPathResource("com/consol/citrus/report/testaction-template.html");
    
    /** HTML template resource for a StackTrace, which contains Placeholders */
    private static final Resource stackTraceTemplateResource = new ClassPathResource("com/consol/citrus/report/stacktrace-template.html");
    
    /** Path of the directory, where the report file will be created */
    private static final String reportTargetPath = "target/";
    
    /** Name of the HTML test report file, which will be created */    
    private static final String reportTargetFileName = "CitrusTestReport.html";
    
    /** Path of the directory, where the resource files of the test report will be copied to */
    private static final String resourcesTargetPath = "target/report/";
    
    /** ClassPaths for resource files, required by HTML test report */
    private static final String[] resources = {"com/consol/citrus/report/citrus-test-report.css",
            "com/consol/citrus/report/citrus_logo.png",
            "com/consol/citrus/report/h3.jpg",
            "com/consol/citrus/report/icon_info_sml.gif",
            "com/consol/citrus/report/icon_success_sml.gif",
            "com/consol/citrus/report/icon_warning_sml.gif",
            "com/consol/citrus/report/icon_error_sml.gif",};
            
    /** Placeholder identifier for the amount of TestSuites in template*/
    private static final String testSuiteCount = "@TestSuiteCount@";
    
    /** Placeholder identifier for the amount of Tests in template*/
    private static final String testCount = "@TestCount@";
    
    /** Placeholder identifier for the amount of skipped Tests in template*/
    private static final String skippedTestCount = "@SkippedTestCount@";
    
    /** Placeholder identifier for the percentage of skipped Tests in template*/
    private static final String skippedTestPercent = "@SkippedTestPercent@";
    
    /** Placeholder identifier for the amount of failed Tests in template*/
    private static final String failedTestCount = "@FailedTestCount@";
    
    /** Placeholder identifier for the percentage of failed Tests in template*/
    private static final String failedTestPercent = "@FailedTestPercent@";
    
    /** Placeholder identifier for the amount of successful Tests in template*/
    private static final String successfulTestCount = "@SuccessfulTestCount@";
    
    /** Placeholder identifier for the percentage of successful Tests in template*/
    private static final String successfulTestPercent = "@SuccessfulTestPercent@";
    
    /** Placeholder identifier for Test result listing in template*/
    private static final String testDetails = "@TestDetails@";
    
    /** Placeholder identifier for TestSuite name in template*/
    private static final String testSuiteName = "@SuiteName@";
    
    /** Placeholder identifier for number of TestCases in a Suite in template*/
    private static final String testCountInSuite = "@TestCountInSuite@";
    
    /** Placeholder identifier for TestCases in template*/
    private static final String testCases = "@TestCases@";
    
    /** Placeholder identifier for css style class of test result in template*/
    private static final String testStyleClass = "@TestStyleClass@";
    
    /** Placeholder identifier for TestCase name in template*/
    private static final String testCaseName = "@TestCaseName@";
    
    /** Placeholder identifier for TestCase author in template*/
    private static final String testAuthor = "@TestAuthor@";
    
    /** Placeholder identifier for TestCase status in template*/
    private static final String testStatus = "@TestStatus@";
    
    /** Placeholder identifier for TestCase creation date in template*/
    private static final String testCreationDate = "@TestCreationDate@";
    
    /** Placeholder identifier for TestCase updater in template*/
    private static final String testUpdater = "@TestUpdater@";
    
    /** Placeholder identifier for TestCase update time in template*/
    private static final String testUpdateDate = "@TestUpdateDate@";
    
    /** Placeholder identifier for TestCase description in template*/
    private static final String testDescription = "@TestDescription@";
    
    /** Placeholder identifier for Test result in template*/
    private static final String testResult = "@Result@";
    
    /** Placeholder identifier for initial visibility of Actions under a TestCase */
    private static final String actionVisibility = "@ActionVisibility@";
    
    /** Placeholder identifier for TestActions in template*/
    private static final String testActions = "@TestActions@";
    
    /** Placeholder identifier for TestAction name in template*/
    private static final String testActionName = "@TestActionName@";
    
    /** Placeholder identifier for StackTrace in template*/
    private static final String stackTrace = "@StackTrace@";
    
    /** Format for creation and update date of TestCases */
    private static final DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM);
    
    /** Common decimal format for percentage calculation in report */
    private static DecimalFormat decFormat = new DecimalFormat("0.0");
    
    static {
        DecimalFormatSymbols symbol = new DecimalFormatSymbols();
        symbol.setDecimalSeparator('.');
        decFormat.setDecimalFormatSymbols(symbol);
    }

    /**
     * @see com.consol.citrus.report.TestReporter#generateTestResults(com.consol.citrus.TestSuite[])
     */
    public void generateTestResults(TestSuite[] suites) {
        //Load HTML test report and sub templates into String variables
        String htmlTestReportTemplate = "";
        String testSuiteTemplate = "";
        String testCaseTemplate = "";
        String testActionTemplate = "";
        String stackTraceTemplate = "";
        try {
            htmlTestReportTemplate = FileUtils.readToString(htmlTestReportTemplateResource);
            testSuiteTemplate = FileUtils.readToString(testSuiteTemplateResource);
            testCaseTemplate = FileUtils.readToString(testCaseTemplateResource);
            testActionTemplate = FileUtils.readToString(testActionTemplateResource);
            stackTraceTemplate = FileUtils.readToString(stackTraceTemplateResource);
        } catch (IOException e) {
            throw new CitrusRuntimeException("Error loading HTML test report or sub template from file resource", e);
        }
        
        //Fill the placeholders of Test Summary
        htmlTestReportTemplate = htmlTestReportTemplate
        .replace(testSuiteCount, Integer.toString(suites.length))
        .replace(testCount, Integer.toString(testResults.size()))
        .replace(skippedTestCount, Integer.toString(testResults.getSkipped()))
        .replace(skippedTestPercent, decFormat.format((double)testResults.getSkipped() / testResults.size()*100))
        .replace(failedTestCount, Integer.toString(testResults.getFailed()))
        .replace(failedTestPercent, decFormat.format((double)testResults.getFailed() / testResults.size()*100))
        .replace(successfulTestCount, Integer.toString(testResults.getSuccess()))
        .replace(successfulTestPercent, decFormat.format((double)testResults.getSuccess() / testResults.size()*100));
        
        //TestDetailsPart for TestSuites
        String testDetailsPart = "";
        for (TestSuite suite : suites) {
            String suiteName = suite.getName();
            
            //TestCasesPart for TestCases
            String testCasePart = "";
            int testsInSuiteCounter = 0;
            for (TestResult result : testResults) {
                if (result.getSuiteName().equals(suiteName)) {
                    testsInSuiteCounter++;
                    String testName = result.getTestName();
                    String resultStatus = "";
                    boolean testFailed = false;
                    switch (result.getResult()) {
                    case SUCCESS:
                        resultStatus = "ok";
                        break;
                    case SKIP:
                        resultStatus = "skipped";
                        break;
                    case FAILURE:
                        resultStatus = "failed";
                        testFailed = true;
                    }
                    
                    //TestActionsPart for TestActions
                    String testActionPart = "";
                    int index = 0;
                    boolean actionFailed = false;
                    for (String actionName : result.getTestActionNames()) {
                        //Create TestAction and add it to TestActionsPart
                        if (testFailed && result.getFailedActionIndex() == index) {
                            actionFailed = true;
                            testActionPart += testActionTemplate
                            .replace(testStyleClass, resultStatus)
                            .replace(testActionName, actionName);
                            if(result.getCause()!= null && StringUtils.hasText(result.getCause().getLocalizedMessage())) {
                                testActionPart += stackTraceTemplate.replace(stackTrace, result.getCause().getLocalizedMessage());                                
                            }
                        } else {
                            testActionPart +=testActionTemplate
                            .replace(testStyleClass, actionFailed ? "" : (testFailed ? "ok" : resultStatus))
                            .replace(testActionName, actionName);
                        }
                        index++;
                    }
                    
                    //Get meta information and description from TestCase
                    String author = result.getTestInformation().getAuthor();
                    author = author.equals("") ? "N/A" : author;
                    String status = result.getTestInformation().getStatus().toString();
                    Date cDate = result.getTestInformation().getCreationDate();
                    String creationDate = cDate == null ? "N/A" : dateFormat.format(cDate);
                    String updater = result.getTestInformation().getLastUpdatedBy();
                    updater = updater.equals("") ? "N/A" : updater;
                    Date uDate = result.getTestInformation().getLastUpdatedOn();
                    String updateDate = uDate == null ? "N/A" : dateFormat.format(uDate);
                    String description = (result.getTestDescription() == null) ? "N/A" : result.getTestDescription();
                    
                    //Create TestCase with TestActionPart included and add it to TestCasesPart
                    testCasePart += testCaseTemplate
                    .replace(testStyleClass, resultStatus)
                    .replace(testCaseName, testName)
                    .replace(testAuthor, author)
                    .replace(testStatus, status)
                    .replace(testCreationDate, creationDate)
                    .replace(testUpdater, updater)
                    .replace(testUpdateDate, updateDate)
                    .replace(testDescription, description)
                    .replace(testResult, resultStatus.toUpperCase())
                    .replace(testSuiteName, suiteName)
                    .replace(actionVisibility, testFailed ? "table-row" : "none")
                    .replace(testActions, testActionPart);
                }
            }
            //Create TestSuite with TestCasePart included and add it to TestDetailsPart
            testDetailsPart += testSuiteTemplate
            .replace(testSuiteName, suiteName)
            .replace(testCountInSuite, String.valueOf(testsInSuiteCounter))
            .replace(testCases, testCasePart);
        }
        //Insert TestDetailsPart into report
        htmlTestReportTemplate = htmlTestReportTemplate.replace(testDetails, testDetailsPart);
        
        createResources(resources, resourcesTargetPath);
        
        createReportFile(htmlTestReportTemplate, reportTargetPath, reportTargetFileName);
    }

    
    /**
     * Creates resources(images and CSS file) in target directory
     * @param resources A String array of classpath resources to be copied
     * @param targetPath The target directory where the files should be written to
     */
    private void createResources(String[] resources, String targetPath) {
        Resource resource = null;
        InputStream  in = null;
        OutputStream out = null;
        File targetDirectory = new File(targetPath);
        if (!targetDirectory.exists()) {
            targetDirectory.mkdirs();
        }
        
        for (String classPath : resources) {
            try {
                resource = new ClassPathResource(classPath);
                in = resource.getInputStream();
                if (!targetPath.endsWith("/") && !targetPath.equals("")) {
                    targetPath += "/";
                }
                out = new FileOutputStream(targetPath + resource.getFilename());
                byte[] buffer = new byte[ 0xFFFF ];
                for ( int len; (len = in.read(buffer)) != -1; ) {
                    out.write( buffer, 0, len );
                }
            } catch ( IOException e ) {
                throw new CitrusRuntimeException("Error copying the HTML test report resource files", e);
            }
            finally {
                if ( in != null )
                    try { in.close(); } catch ( IOException e ) { throw new CitrusRuntimeException("Error closing FileInputStream", e); }
                if ( out != null )
                    try { out.close(); } catch ( IOException e ) { throw new CitrusRuntimeException("Error closing FileOutputStream", e); }
            }
        }
    }
    
    
    /**
     * Creates the html report file
     * @param content The String content of the report file
     * @param targetPath The directory where the report file is created
     * @param reportFileName The name of the report file
     */
    private void createReportFile(String content, String targetPath, String reportFileName) {
        Writer fw = null;
        try {
            if (!targetPath.endsWith("/") && !targetPath.equals("")) {
                targetPath += "/";
            }
            fw = new FileWriter(targetPath + reportFileName);
            fw.append(content);
            fw.flush();
        } catch (IOException e) {
            throw new CitrusRuntimeException("Error writing the HTML test report", e);
        } finally {
            if (fw != null) {
                try { fw.close(); } catch ( IOException e ) { throw new CitrusRuntimeException("Error closing FileWriter", e); } 
            }
        }
    }
    
    /**
     * @see com.consol.citrus.report.TestListener#onTestSuccess(com.consol.citrus.TestCase)
     */
    public void onTestSuccess(TestCase test) {
        ArrayList<String> actionNames = new ArrayList<String>();
        for (TestAction action : test.getActions()) {
            actionNames.add(action.getName());
        }
        testResults.addResult(new TestResult(test.getName(), executingTestSuite, test.getMetaInfo(), test.getDescription(), actionNames, RESULT.SUCCESS));        
    }
    
    /**
     * @see com.consol.citrus.report.TestListener#onTestFailure(com.consol.citrus.TestCase, java.lang.Throwable)
     */
    public void onTestFailure(TestCase test, Throwable cause) {
        ArrayList<String> actionNames = new ArrayList<String>();
        for (TestAction action : test.getActions()) {
            actionNames.add(action.getName());
        }
        int failedActionIndex = test.getActionIndex(test.getLastExecutedAction());
        if (cause != null) {
            testResults.addResult(new TestResult(test.getName(), executingTestSuite, test.getMetaInfo(), test.getDescription(), actionNames, RESULT.FAILURE, failedActionIndex, cause));
        } else {
            testResults.addResult(new TestResult(test.getName(), executingTestSuite, test.getMetaInfo(), test.getDescription(), actionNames, RESULT.FAILURE, failedActionIndex, null));
        }
    }
    
    /**
     * @see com.consol.citrus.report.TestListener#onTestSkipped(com.consol.citrus.TestCase)
     */
    public void onTestSkipped(TestCase test) {
        ArrayList<String> actionNames = new ArrayList<String>();
        for (TestAction action : test.getActions()) {
            actionNames.add(action.getName());
        }
        testResults.addResult(new TestResult(test.getName(), executingTestSuite, test.getMetaInfo(), test.getDescription(), actionNames, RESULT.SKIP));
    }
    
    /**
     * @see com.consol.citrus.report.TestSuiteListener#onStart(com.consol.citrus.TestSuite)
     */
    public void onStart(TestSuite testsuite) {
        executingTestSuite = testsuite.getName();
    }
    
    /**
     * @see com.consol.citrus.report.TestListener#onTestStart(com.consol.citrus.TestCase)
     */
    public void onTestStart(TestCase test) {}
    
    /**
     * @see com.consol.citrus.report.TestListener#onTestFinish(com.consol.citrus.TestCase)
     */
    public void onTestFinish(TestCase test) {}
    
    /**
     * @see com.consol.citrus.report.TestSuiteListener#onStartSuccess(com.consol.citrus.TestSuite)
     */
	public void onStartSuccess(TestSuite testsuite) {}
	
	/**
     * @see com.consol.citrus.report.TestSuiteListener#onStartFailure(com.consol.citrus.TestSuite, java.lang.Throwable)
     */
	public void onStartFailure(TestSuite testsuite, Throwable cause) {}
	
	/**
     * @see com.consol.citrus.report.TestSuiteListener#onFinish(com.consol.citrus.TestSuite)
     */
	public void onFinish(TestSuite testsuite) {}
	
	/**
     * @see com.consol.citrus.report.TestSuiteListener#onFinishSuccess(com.consol.citrus.TestSuite)
     */
	public void onFinishSuccess(TestSuite testsuite) {}
	
	/**
     * @see com.consol.citrus.report.TestSuiteListener#onFinishFailure(com.consol.citrus.TestSuite, java.lang.Throwable)
     */
	public void onFinishFailure(TestSuite testsuite, Throwable cause) {}
}
