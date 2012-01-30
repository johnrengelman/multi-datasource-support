package testproject

import org.junit.Before
import org.junit.Test

class RelationTests extends GroovyTestCase {

    Foo foo
    Bar bar

    @Before
    public void setUp() {
        bar = new Bar(bar: "bar1")
        assert bar.validate()
        assert bar.save()
        foo = new Foo(foo: "foo1")
        assert foo.validate()
        assert foo.save()
    }

    @Test
    public void testGetterIsDatasourceAware() {
        Bar bar2 = new Bar(bar: "bar2")
        assert bar2.validate()
        assert bar2.lookup.save()

        assert bar2.id
        assert Bar.lookup.count() == 1
        assert Bar.count() == 1
        assert Bar.lookup.get(bar2.id) == bar2
        assert Bar.get(bar2.id) != bar2
        
        foo.setBar(bar2)
        
        assert foo.getBar() == Bar.lookup.get(bar2.id)
    }
}
