/*
 * Copyright 2006-2010 ConSol* Software GmbH.
 * 
 * This file is part of Citrus.
 * 
 * Citrus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Citrus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Citrus. If not, see <http://www.gnu.org/licenses/>.
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
