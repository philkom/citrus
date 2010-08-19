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

package com.consol.citrus.variable;

import java.text.ParseException;

import com.consol.citrus.CitrusConstants;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.NoSuchVariableException;

/**
 * Utility class manipulating test variables.
 * 
 * @author Christoph Deppisch
 */
public class VariableUtils {
    /**
     * Cut off variables prefix
     * @param key
     * @return
     */
    public static String cutOffVariablesPrefix(String variable) {
        if (variable.indexOf(CitrusConstants.VARIABLE_PREFIX) == 0 && variable.charAt(variable.length()-1) == CitrusConstants.VARIABLE_SUFFIX) {
            return variable.substring(CitrusConstants.VARIABLE_PREFIX.length(), variable.length()-1);
        }

        return variable;
    }
    
    /**
     * Checks whether a given expression contains a variable name.
     * @param expression
     * @return flag true/false
     */
    public static boolean containsVariableName(final String expression) {
        if (expression == null || expression.length() == 0) {
            return false;
        }

        if (expression.contains(CitrusConstants.VARIABLE_PREFIX) && expression.substring(expression.indexOf((CitrusConstants.VARIABLE_PREFIX))).contains(Character.toString(CitrusConstants.VARIABLE_SUFFIX))) {
            return true;
        }

        return false;
    }
    
    /**
     * Checks whether a given expression is a variable name.
     * @param expression
     * @return flag true/false
     */
    public static boolean isVariableName(final String expression) {
        if (expression == null || expression.length() == 0) {
            return false;
        }

        if (expression.indexOf(CitrusConstants.VARIABLE_PREFIX) == 0 && expression.lastIndexOf(CitrusConstants.VARIABLE_SUFFIX) == expression.length()-1) {
            return true;
        }

        return false;
    }
    
   /**
    * Replace all variable expression in a string with 
    * its respective values.
    * @param str
    * @param context
    * @return
    * @throws ParseException
    */
   public static String replaceVariablesInString(final String str, TestContext context) throws ParseException {
       return replaceVariablesInString(str, context, false);
   }

   /**
    * Replace all variable expression in a string with
    * its respective value. Variable values are enclosed with quotes
    * if enabled.
    * 
    * @param str
    * @param context
    * @param enableQuoting
    * @return
    * @throws ParseException
    */
   public static String replaceVariablesInString(final String str, TestContext context, boolean enableQuoting) throws ParseException {
       StringBuffer newStr = new StringBuffer();

       boolean isVarComplete = false;

       StringBuffer variableNameBuf = new StringBuffer();

       int startIndex = 0;
       int curIndex;
       int searchIndex;

       while ((searchIndex = str.indexOf(CitrusConstants.VARIABLE_PREFIX, startIndex)) != -1) {
           int control = 0;
           isVarComplete = false;

           curIndex = searchIndex + CitrusConstants.VARIABLE_PREFIX.length();

           while (curIndex < str.length() && !isVarComplete) {
               if (str.indexOf(CitrusConstants.VARIABLE_PREFIX, curIndex) == curIndex) {
                   control++;
               }

               if ((!Character.isJavaIdentifierPart(str.charAt(curIndex)) && (str.charAt(curIndex) == CitrusConstants.VARIABLE_SUFFIX)) || (curIndex+1 == str.length())) {
                   if (control == 0) {
                       isVarComplete = true;
                   } else {
                       control--;
                   }
               }

               if (!isVarComplete) {
                   variableNameBuf.append(str.charAt(curIndex));
               }
               ++curIndex;
           }

           final String value = context.getVariable(variableNameBuf.toString());
           if (value == null) {
               throw new NoSuchVariableException("Variable: " + variableNameBuf.toString() + " could not be found");
           }

           newStr.append(str.substring(startIndex, searchIndex));

           if (enableQuoting) {
               newStr.append("'" + value + "'");
           } else {
               newStr.append(value);
           }

           startIndex = curIndex;

           variableNameBuf = new StringBuffer();
           isVarComplete = false;
       }

       newStr.append(str.substring(startIndex));

       return newStr.toString();
   }
   /**
    * Replace all variable expression in a string with 
    * its respective values of global variables.
    * @param str
    * @param globalVariables
    * @return
    * @throws ParseException
    */
   public static String replaceGlobalVariablesInString(final String str, GlobalVariables globalVariables) throws ParseException {
       return replaceGlobalVariablesInString(str, globalVariables, false);
   }

   /**
    * Replace all variable expression in a string with
    * its respective value of global variables. Variable values are enclosed with quotes
    * if enabled.
    * 
    * @param str
    * @param globalVariables
    * @param enableQuoting
    * @return
    * @throws ParseException
    */
   public static String replaceGlobalVariablesInString(final String str, GlobalVariables globalVariables, boolean enableQuoting) throws ParseException {
       StringBuffer newStr = new StringBuffer();

       boolean isVarComplete = false;

       StringBuffer variableNameBuf = new StringBuffer();

       int startIndex = 0;
       int curIndex;
       int searchIndex;

       while ((searchIndex = str.indexOf(CitrusConstants.VARIABLE_PREFIX, startIndex)) != -1) {
           int control = 0;
           isVarComplete = false;

           curIndex = searchIndex + CitrusConstants.VARIABLE_PREFIX.length();

           while (curIndex < str.length() && !isVarComplete) {
               if (str.indexOf(CitrusConstants.VARIABLE_PREFIX, curIndex) == curIndex) {
                   control++;
               }

               if ((!Character.isJavaIdentifierPart(str.charAt(curIndex)) && (str.charAt(curIndex) == CitrusConstants.VARIABLE_SUFFIX)) || (curIndex+1 == str.length())) {
                   if (control == 0) {
                       isVarComplete = true;
                   } else {
                       control--;
                   }
               }

               if (!isVarComplete) {
                   variableNameBuf.append(str.charAt(curIndex));
               }
               ++curIndex;
           }

           final String value = globalVariables.getVariables().get(variableNameBuf.toString());
           if (value == null) {
               throw new NoSuchVariableException("Variable: " + variableNameBuf.toString() + " could not be found");
           }

           newStr.append(str.substring(startIndex, searchIndex));

           if (enableQuoting) {
               newStr.append("'" + value + "'");
           } else {
               newStr.append(value);
           }

           startIndex = curIndex;

           variableNameBuf = new StringBuffer();
           isVarComplete = false;
       }

       newStr.append(str.substring(startIndex));

       return newStr.toString();
   }
}
