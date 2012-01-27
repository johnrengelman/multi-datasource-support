package grails.plugin

import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.transform.GroovyASTTransformation
import org.codehaus.groovy.transform.ASTTransformation
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.FieldNode
import org.codehaus.groovy.grails.compiler.injection.GrailsASTUtils
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.ClassHelper
import java.lang.reflect.Modifier
import org.codehaus.groovy.ast.expr.ConstructorCallExpression
import org.codehaus.groovy.ast.expr.ArgumentListExpression
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.ast.Parameter
import org.codehaus.groovy.ast.builder.AstBuilder
import static org.objectweb.asm.Opcodes.ACC_PUBLIC
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.ast.stmt.ReturnStatement
import org.codehaus.groovy.ast.VariableScope

@GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
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
        ClassNode relatedPropertyType = ClassHelper.make(annotatedField.getType().getName())
        ClassNode relatedPropertyIdType = ClassHelper.make(annotatedField.getType().getField("id").getType().getName())
        
        addIdFieldIfNonExistent(parentClass, relatedPropertyIdType, "${relatedPropertyName}Id")
        addAccessorMethodsIfNonExistent(parentClass, relatedPropertyType, relatedPropertyName)
    }

    private void addIdFieldIfNonExistent(ClassNode parentClass, ClassNode fieldType, String fieldName) {
        String idFieldName = "${fieldName}Id"
        if (parentClass != null && parentClass.getField(idFieldName) == null) {
            parentClass.addField(idFieldName, Modifier.PUBLIC, fieldType, ConstantExpression.NULL);
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
            MethodNode methodNode = new MethodNode(getMethodName,
                                                   ACC_PUBLIC,
                                                   ClassHelper.make(String, false),
                                                   Parameter.EMPTY_ARRAY,
                                                   [] as ClassNode[],
                                                   new BlockStatement(
                                                       [new ReturnStatement(
                                                               new ConstantExpression('test')
                                                       )],
                                                       new VariableScope()
                                                   ))
            parentClass.addMethod(methodNode)
        }
    }
    
    private void addSetterMethodIfNonExistent(ClassNode parentClass, ClassNode returnType, String fieldName) {
        
    }
}
