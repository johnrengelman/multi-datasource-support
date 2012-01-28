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
        assert !(foo.barId)
        assert bar.id

        assert bar.getId()
        assert Bar.exists(bar.getId())

        foo.setBar(bar)

        assert foo.barId == bar.id
        assert foo.getBar() == bar
    }
    
    @Test
    public void testSetterUnSavedArg() {
        Bar bar2 = new Bar(bar: "bar2")
        assert !(foo.barId)
        
        foo.setBar(bar2)
        
        assert !(foo.barId)
        assert !(foo.getBar())
        
        foo.setBar(bar)
        assert foo.barId
        assert foo.getBar() == bar
        
        foo.setBar(bar2)
        assert foo.barId
        assert foo.getBar() == bar
    }
    
    @Test
    public void testSetterNullArg() {
        
        assert !(foo.barId)
        
        foo.setBar(null)
        
        assert !(foo.barId)
        assert !(foo.getBar())

        foo.setBar(bar)
        assert foo.barId
        assert foo.getBar() == bar
        
        foo.setBar(null)
        
        assert foo.barId
        assert foo.getBar() == bar
    }
    
    @Test
    public void testGetterNonExistentId() {
        foo.setBar(bar)
        
        assert foo.barId
        assert foo.getBar() == bar
        
        foo.barId = bar.id+1
        
        assert !(foo.getBar())
    }
}
