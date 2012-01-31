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
import org.codehaus.groovy.ast.builder.AstBuilder

@GroovyASTTransformation(phase = CompilePhase.INSTRUCTION_SELECTION)
class RelationASTTransformation implements ASTTransformation {

    String datasource

    void visit(ASTNode[] astNodes, SourceUnit sourceUnit) {
        if(validNodes(astNodes)) {
            FieldNode field = (FieldNode) astNodes[1]

            populateObject(field, datasource)
            
        }
    }

    private boolean validNodes(ASTNode[] astNodes) {
        if(astNodes.size() != 2) return false

        if(!(astNodes[0] instanceof AnnotationNode)) return false
        AnnotationNode annotationNode = (AnnotationNode) astNodes[0]

        if(annotationNode.getMember('datasource')) {
            if(!(annotationNode.getMember('datasource') instanceof ConstantExpression)) return false
            ConstantExpression constantExpression = (ConstantExpression) annotationNode.getMember('datasource')
            
            if(!(constantExpression.value instanceof String)) return false
            datasource = (String) constantExpression.value
        }

        if(!(astNodes[1] instanceof FieldNode)) return false
        return true
    }

    private populateObject(FieldNode annotatedField, String ds) {
        ClassNode parentClass = annotatedField.getOwner()

        String relatedPropertyName = annotatedField.getName()
        ClassNode relatedPropertyType = annotatedField.getType()
        String relatedPropertyIdType = annotatedField.getType().getField("id").getType().getName()
        
        addIdFieldIfNonExistent(parentClass, relatedPropertyIdType, relatedPropertyName)
        addAccessorMethodsIfNonExistent(parentClass, relatedPropertyType, relatedPropertyName, ds)
        addFieldToTransients(parentClass, relatedPropertyName)
    }

    private void addIdFieldIfNonExistent(ClassNode parentClass, String fieldType, String fieldName) {
        String idFieldName = "${fieldName}Id"
        if (parentClass != null && parentClass.getField(idFieldName) == null) {
            parentClass.addField(idFieldName, Modifier.PUBLIC, ClassHelper.make(fieldType), ConstantExpression.NULL);
        }
    }
    
    private void addFieldToTransients(ClassNode parentClass, String propertyName) {
        if(parentClass != null) {
            FieldNode transients = parentClass.getField("transients")
            if(!transients) {
                transients = parentClass.addField(
                    "transients",
                    Modifier.PUBLIC | Modifier.STATIC,
                    ClassHelper.DYNAMIC_TYPE,
                    new ListExpression()
                )
            }
            ((ListExpression) transients.getInitialExpression()).addExpression(new ConstantExpression(propertyName))
        }
    }

    private void addAccessorMethodsIfNonExistent(ClassNode parentClass, ClassNode returnType, String fieldName, String ds) {
        addGetterMethodIfNonExistent(parentClass, returnType, fieldName, ds)
        addSetterMethodIfNonExistent(parentClass, returnType, fieldName, ds)
    }
    
    private void addGetterMethodIfNonExistent(ClassNode parentClass, ClassNode returnType, String fieldName, String ds) {
        String getMethodName = "get${fieldName.capitalize()}"
        String idFieldName = "${fieldName}Id"
        Expression target = callStaticMethod(returnType, "get", new VariableExpression(idFieldName))
        Expression target2 = callStaticMethod(returnType, "exists", new VariableExpression(idFieldName))
        if (ds) {
            target = callMethod(
                new PropertyExpression(new ClassExpression(returnType), ds),
                "get",
                new VariableExpression(idFieldName)
            )
            target2 = callMethod(
                new PropertyExpression(new ClassExpression(returnType), ds),
                "exists",
                new VariableExpression(idFieldName)
            )
        }
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
                                   target2
                               )
                           ),
                           createBlockReturnStatement(target),
                           createBlockReturnStatement(ConstantExpression.NULL)
                       )
                   ],
                   new VariableScope()
                )
            )
            parentClass.addMethod(methodNode)
        }
    }
    
    private void addSetterMethodIfNonExistent(ClassNode parentClass, ClassNode returnType, String fieldName, String ds) {
        String setMethodName = "set${fieldName.capitalize()}"
        String idFieldName = "${fieldName}Id"
        if (parentClass != null && parentClass.getMethod(setMethodName, new Parameter(returnType, fieldName)) == null) {
            Expression target = callStaticMethod(
                returnType,
                "exists",
                callMethod(
                        new VariableExpression(fieldName),
                        "getId",
                        new ArgumentListExpression()
                )
            )
            if (ds) {
                target = callMethod(
                    new PropertyExpression(new ClassExpression(returnType), ds),
                    "exists",
                    callMethod(
                        new VariableExpression(fieldName),
                        "getId",
                        new ArgumentListExpression()
                    )
                )
            }
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
                                            new ArgumentListExpression(),
                                            true
                                        ),
                                        Token.newSymbol("!=", 0, 0),
                                        ConstantExpression.NULL
                                    ),
                                    Token.newSymbol("&&", 0, 0),
                                    target
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
    
    private static StaticMethodCallExpression callStaticMethod(ClassNode parentClass, String method, Expression arguments) {
        new StaticMethodCallExpression(
            parentClass,
            method,
            arguments
        )    
    }
    
    private static MethodCallExpression callMethod(Expression object, String method, Expression arguments, boolean safe = false) {
        MethodCallExpression exp = new MethodCallExpression(
            object,
            method,
            arguments
        )
        exp.setSafe(safe)
        return exp
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
