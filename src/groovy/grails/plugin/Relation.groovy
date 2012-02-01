package grails.plugin

import java.lang.annotation.Target
import java.lang.annotation.Retention
import org.codehaus.groovy.transform.GroovyASTTransformationClass
import java.lang.annotation.ElementType
import java.lang.annotation.RetentionPolicy

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@GroovyASTTransformationClass(["grails.plugin.RelationASTTransformation"])
public @interface Relation {

    String datasource()
}