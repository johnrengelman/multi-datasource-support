package testproject

class Bar {

    Long id
    String bar

    static constraints = {
    }

    static mapping = {
        datasources(['DEFAULT', 'lookup'])
    }
}
