/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.cxf.pmd;

import java.util.List;

import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule;
import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.lang.java.ast.ASTAdditiveExpression;
import net.sourceforge.pmd.lang.java.ast.ASTAllocationExpression;
import net.sourceforge.pmd.lang.java.ast.ASTArgumentList;
import net.sourceforge.pmd.lang.java.ast.ASTArrayDimsAndInits;
import net.sourceforge.pmd.lang.java.ast.ASTClassOrInterfaceType;
import net.sourceforge.pmd.lang.java.ast.ASTExpression;
import net.sourceforge.pmd.lang.java.ast.ASTName;
import net.sourceforge.pmd.lang.java.ast.JavaNode;
import net.sourceforge.pmd.lang.java.types.TypeTestUtil;

/**
 * Look for new String(byte[]) or new String(byte[], start, end)
 * and complain.
 */
public class UnsafeStringConstructorRule extends AbstractJavaRule {

    /** {@inheritDoc} */
    @Override
    public Object visit(ASTAllocationExpression node, Object data) {
        if (!(node.getChild(0) instanceof ASTClassOrInterfaceType)) {
            return data;
        }

        if (!TypeTestUtil.isA(String.class, (ASTClassOrInterfaceType)node.getChild(0))) {
            return data;
        }
        
        ASTArgumentList arglist = node.getFirstChildOfType(ASTArgumentList.class);
        if (arglist == null) { // unlikely
            return data;
        }
        
        // one of the two possibilities ...
        if (arglist.getNumChildren() == 1 || arglist.getNumChildren() == 3) {
            ASTExpression firstArgExpr = arglist.getFirstChildOfType(ASTExpression.class);
            Class<?> exprType = firstArgExpr.getType();
            // pmd reports the type as byte, not byte[]. But since
            // there is no such thing as new String(byte), it seems
            // safe enough to take that as good enough.
            if (exprType != null) {
                if (exprType == Byte.TYPE || 
                    (exprType.isArray() && exprType.getComponentType() == Byte.TYPE)) {
                        asCtx(data).addViolation(node);
                }
            }
        }
        return data;

    }

}
