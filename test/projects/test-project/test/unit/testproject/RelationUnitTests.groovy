package testproject

import grails.test.mixin.TestFor
import grails.test.mixin.Mock
import org.junit.Before
import org.junit.Test
import grails.test.mixin.support.GrailsUnitTestMixin
import org.junit.Ignore

@Mixin(GrailsUnitTestMixin)
@Mock([Foo, Bar, Alpha, Beta])
class RelationUnitTests {
    
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
    
    @Test
    public void testTransientsListCreated() {
        assert Foo.transients
        assert Foo.transients.contains("bar")
    }
    
    @Test
    public void testTransientsListAddedTo() {
        Alpha alpha = new Alpha(alpha: "alpha")
        assert alpha.validate()
        alpha.save()
        
        Beta beta = new Beta(beta: "beta")
        assert beta.validate()
        beta.save()
        
        assert Alpha.transients
        assert Alpha.transients.contains("beta")
        assert Alpha.transients.contains("bar")
    }

    @Test
    public void testIdFieldWorksWithGormGeneratedId() {
        Alpha alpha = new Alpha(alpha: "alpha")
        assert alpha.validate()
        alpha.save()

        Beta beta = new Beta(beta: "beta")
        assert beta.validate()
        beta.save()

        assert Alpha.getField("betaId").type == Long
    }
    
    @Test
    @Ignore("This needs to be run as integration test for datasource support")
    public void testGetterIsDatasourceAware() {
        Bar bar2 = new Bar(bar: "bar2")
        assert bar2.validate()
        assert bar2.lookup.save()
        
        assert bar2.id
        assert Bar.lookup.count() == 1
        assert Bar.count() == 1
        assert Bar.lookup.get(bar2.id) == bar2
        assert Bar.get(bar2.id) != bar2
    }
}
