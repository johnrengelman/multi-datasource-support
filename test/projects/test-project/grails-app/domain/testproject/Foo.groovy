package testproject

import grails.plugin.Relation

class Foo {

    String foo

    @Relation
    Bar bar

    static constraints = {
    }
}
