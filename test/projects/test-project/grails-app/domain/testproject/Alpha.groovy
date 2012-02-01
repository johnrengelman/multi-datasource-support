package testproject

import grails.plugin.Relation

class Alpha {

    String alpha

    Bar bar

    @Relation
    Beta beta

    static constraints = {
        beta nullable:  true
    }

    static transients = [ 'bar' ]
}
