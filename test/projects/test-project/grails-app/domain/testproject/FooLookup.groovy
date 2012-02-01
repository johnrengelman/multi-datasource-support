package testproject

import grails.plugin.Relation

class FooLookup {

    Long id
    String foo

    @Relation(datasource='lookup')
    Bar bar

    static constraints = {
        bar nullable:  true
    }
}
