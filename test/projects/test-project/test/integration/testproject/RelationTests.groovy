package testproject

import org.junit.Before
import org.junit.Test

class RelationTests extends GroovyTestCase {

    Foo foo
    FooLookup fooLookup
    Bar bar

    @Before
    public void setUp() {
        bar = new Bar(bar: "bar1")
        assert bar.validate()
        assert bar.save()
        fooLookup = new FooLookup(foo: "fooLookup")
        assert fooLookup.validate()
        assert fooLookup.save()
        foo = new Foo(foo: "foo1")
        assert foo.validate()
        assert foo.save()
    }

    @Test
    public void testDefaultDataSource() {

        foo.setBar(bar)

        assert foo.getBar() == bar
    }

    @Test
    public void testIsDatasourceAware() {
        Bar bar2 = new Bar(bar: "bar2")
        assert bar2.validate()
        assert bar2.lookup.save()

        assert bar2.id
        assert Bar.lookup.count() == 1
        assert Bar.count() == 1
        assert Bar.lookup.get(bar2.id) == bar2
        assert Bar.get(bar2.id) != bar2
        
        fooLookup.setBar(bar2)
        
        assert fooLookup.barId == bar2.id
        assert fooLookup.getBar() == Bar.lookup.get(bar2.id)
        assert fooLookup.getBar() == bar2
    }
}
