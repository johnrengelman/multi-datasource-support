package grails.plugin

import grails.plugin.spock.*

class RelationSpec extends UnitSpec {

    def "Id field is added"() {
        when:
        Foo foo = new Foo()
        foo.barId = 123L
        
        then:
        foo.barId == 123L
    }
}

class Foo {
    
    Long id

    @Relation
    Bar bar
    
    String foo
    
}

class Bar {
    
    Long id
}
