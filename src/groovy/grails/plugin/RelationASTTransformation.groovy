package grails.plugin

import java.lang.reflect.Modifier
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.ast.stmt.ExpressionStatement
import org.codehaus.groovy.ast.stmt.IfStatement
import org.codehaus.groovy.ast.stmt.ReturnStatement
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.syntax.Token
import org.codehaus.groovy.transform.ASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformation
import org.codehaus.groovy.ast.*
import org.codehaus.groovy.ast.expr.*
import static org.objectweb.asm.Opcodes.ACC_PUBLIC
import org.codehaus.groovy.ast.stmt.EmptyStatement

@GroovyASTTransformation(phase = CompilePhase.SEMANTIC_ANALYSIS)
class RelationASTTransformation implements ASTTransformation {

    void visit(ASTNode[] astNodes, SourceUnit sourceUnit) {
        if(validNodes(astNodes)) {

            FieldNode field = (FieldNode) astNodes[1]

            addIdField(field)
            
        }
    }

    private boolean validNodes(ASTNode[] astNodes) {
        //TODO implement real node validation
        true
    }

    private addIdField(FieldNode annotatedField) {
        ClassNode parentClass = annotatedField.getOwner()

        String relatedPropertyName = annotatedField.getName()
        ClassNode relatedPropertyType = annotatedField.getType()
        String relatedPropertyIdType = annotatedField.getType().getField("id").getType().getName()
        
        addIdFieldIfNonExistent(parentClass, relatedPropertyIdType, relatedPropertyName)
        addAccessorMethodsIfNonExistent(parentClass, relatedPropertyType, relatedPropertyName)
    }

    private void addIdFieldIfNonExistent(ClassNode parentClass, String fieldType, String fieldName) {
        String idFieldName = "${fieldName}Id"
        if (parentClass != null && parentClass.getField(idFieldName) == null) {
            parentClass.addField(idFieldName, Modifier.PUBLIC, ClassHelper.make(fieldType), ConstantExpression.NULL);
        }
    }

    private void addAccessorMethodsIfNonExistent(ClassNode parentClass, ClassNode returnType, String fieldName) {
        addGetterMethodIfNonExistent(parentClass, returnType, fieldName)
        addSetterMethodIfNonExistent(parentClass, returnType, fieldName)
    }
    
    private void addGetterMethodIfNonExistent(ClassNode parentClass, ClassNode returnType, String fieldName) {
        String getMethodName = "get${fieldName.capitalize()}"
        String idFieldName = "${fieldName}Id"
        if (parentClass != null && parentClass.getMethod(getMethodName, Parameter.EMPTY_ARRAY) == null) {
            MethodNode methodNode = new MethodNode(
                getMethodName,
                ACC_PUBLIC,
                returnType,
                Parameter.EMPTY_ARRAY,
                [] as ClassNode[],
                new BlockStatement(
                   [
                       new IfStatement(
                           new BooleanExpression(
                               new BinaryExpression(
                                   new BinaryExpression(
                                       new VariableExpression(idFieldName),
                                       Token.newSymbol("!=", 0, 0),
                                       ConstantExpression.NULL
                                   ),
                                   Token.newSymbol("&&", 0, 0),
                                   callStaticMethod(returnType, "exists", new VariableExpression(idFieldName) as Expression[])
                               )
                           ),
                           createBlockReturnStatement(callStaticMethod(returnType, "get", new VariableExpression(idFieldName) as Expression[])),
                           createBlockReturnStatement(ConstantExpression.NULL)
                       )
                   ],
                   new VariableScope()
                )
            )
            parentClass.addMethod(methodNode)
        }
    }
    
    private void addSetterMethodIfNonExistent(ClassNode parentClass, ClassNode returnType, String fieldName) {
        String setMethodName = "set${fieldName.capitalize()}"
        String idFieldName = "${fieldName}Id"
        if (parentClass != null && parentClass.getMethod(setMethodName, new Parameter(returnType, fieldName)) == null) {
            MethodNode methodNode = new MethodNode(
                setMethodName,
                ACC_PUBLIC,
                ClassHelper.VOID_TYPE,
                [new Parameter(returnType, fieldName)] as Parameter[],
                [] as ClassNode[],
                new BlockStatement(
                    [
                        new IfStatement(
                            new BooleanExpression(
                                new BinaryExpression(
                                    new BinaryExpression(
                                        callMethod(
                                            new VariableExpression(fieldName),
                                            "getId",
                                            new ArgumentListExpression()
                                        ),
                                        Token.newSymbol("!=", 0, 0),
                                        ConstantExpression.NULL
                                    ),
                                    Token.newSymbol("&&", 0, 0),
                                    callStaticMethod(
                                        returnType,
                                        "exists",
                                        callMethod(
                                                new VariableExpression(fieldName),
                                                "getId",
                                                new ArgumentListExpression()
                                        ) as Expression[]
                                    )
                                )
                            ),
                            new BlockStatement(
                                [
                                    new ExpressionStatement(
                                        new BinaryExpression(
                                            new VariableExpression(idFieldName),
                                            Token.newSymbol("=", 0, 0),
                                            callMethod(
                                                new VariableExpression(fieldName),
                                                "getId",
                                                new ArgumentListExpression()
                                            )
                                        )
                                    )
                                ],
                                new VariableScope()
                            ),
                            new EmptyStatement()
                        )
                    ],
                    new VariableScope()
                )
            )
            parentClass.addMethod(methodNode)
        }
    }
    
    private static StaticMethodCallExpression callStaticMethod(ClassNode parentClass, String method, Expression[] arguments) {
        new StaticMethodCallExpression(
            parentClass,
            method,
            new ArgumentListExpression(arguments)
        )    
    }
    
    private static MethodCallExpression callMethod(Expression object, String method, Expression arguments) {
        new MethodCallExpression(
            object,
            method,
            arguments
        )    
    }
    
    private static BlockStatement createBlockReturnStatement(Expression expression) {
        new BlockStatement(
            [
                new ReturnStatement(expression)
            ],
            new VariableScope()
        )
    }

}
