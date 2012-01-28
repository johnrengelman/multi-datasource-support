package testproject

import grails.plugin.Relation

class Foo {

    Long id
    String foo

    @Relation
    Bar bar

    static constraints = {
        bar nullable:  true
    }
}
