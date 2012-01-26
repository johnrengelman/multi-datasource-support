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
        String relatedPropertyName = annotatedField.getName()
        ClassNode relatedPropertyIdType = ClassHelper.make(annotatedField.getType().getField("id").getType().getTypeClass())
        ClassNode parentClass = annotatedField.getOwner()
        
        addFieldIfNonExistent(parentClass, relatedPropertyIdType, "${relatedPropertyName}Id")
    }

    private static void addFieldIfNonExistent(ClassNode classNode, ClassNode fieldType, String fieldName) {
        if (classNode != null && classNode.getField(fieldName) == null) {
            classNode.addField(fieldName, Modifier.PUBLIC, fieldType, ConstantExpression.NULL);
        }
    }
}
