package testproject

import grails.test.mixin.TestFor
import grails.test.mixin.Mock
import org.junit.Before
import org.junit.Test

@TestFor(Foo)
@Mock(Bar)
class RelationUnitTest {
    
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
    public void testGetter() {
        assert foo
        assert bar
        assert !(foo.barId)
        assert bar.id
        
        foo.barId = bar.id
        
        assert Bar.get(bar.id) == foo.getBar()
    }
    
    @Test
    public void testSetter() {
        assert foo
        assert bar
        assert !(foo.@barId)
        assert bar.id

        assert bar.getId()
        assert Bar.exists(bar.getId())

        foo.setBar(bar)

        assert foo.barId == bar.id
        assert foo.getBar() == bar
    }
}
