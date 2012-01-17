package grails.plugin

import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.transform.GroovyASTTransformation
import org.codehaus.groovy.transform.ASTTransformation
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.FieldNode
import org.codehaus.groovy.grails.compiler.injection.GrailsASTUtils
import org.codehaus.groovy.ast.ClassNode

@GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
class RelationASTTransformation implements ASTTransformation {

    void visit(ASTNode[] astNodes, SourceUnit sourceUnit) {
        if(validNodes(astNodes)) {

            FieldNode field = (FieldNode) astNodes[1]
            
            String fieldName = field.getName()
            ClassNode classNode = field.getOwner()
            ClassNode fieldType = new ClassNode(Long)
            
            GrailsASTUtils.addFieldIfNonExistent(classNode, fieldType, "${fieldName}id")
        }
    }

    private boolean validNodes(ASTNode[] astNodes) {
        true
    }
}
