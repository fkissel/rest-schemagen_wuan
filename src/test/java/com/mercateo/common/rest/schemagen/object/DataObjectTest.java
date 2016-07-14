package com.mercateo.common.rest.schemagen.object;


import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.mercateo.common.rest.schemagen.generictype.GenericType;
import com.mercateo.common.rest.schemagen.object.properties.FieldCollector;
import com.mercateo.common.rest.schemagen.object.properties.MethodCollector;
import com.mercateo.common.rest.schemagen.plugin.FieldCheckerForSchema;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class DataObjectTest {

    @Mock
    private FieldCheckerForSchema fieldCheckerForSchema;

    @Before
    public void setUp() {
    }

    @Test
    public void shouldMapNothingWhenFullyDisabled() {
        final PropertyBehaviour propertyBehaviour = PropertyBehaviour.builder()
                .build();

        DataObject dataObject = new DataObject(GenericType.of(TestClass.class), null, null, propertyBehaviour);
        final Map<String, DataObject> children = dataObject.getChildren();

        assertThat(children.keySet()).isEmpty();
    }

    @Test
    public void test() throws IntrospectionException {
        final BeanInfo beanInfo = Introspector.getBeanInfo(TestClass.class, Object.class);

        assertThat(beanInfo).isNotNull();
    }

    @Test
    public void shouldMapMethods() {
        final PropertyBehaviour propertyBehaviour = PropertyBehaviour.builder().addPropertyCollectors(new MethodCollector()).build();
        DataObject dataObject = new DataObject(GenericType.of(TestClass.class), null, null, propertyBehaviour);
        final Map<String, DataObject> children = dataObject.getChildren();

        assertThat(children.keySet()).containsExactly("getFoo");
    }

    @Test
    public void shouldMapFields() {
        final PropertyBehaviour propertyBehaviour = PropertyBehaviour.builder().addPropertyCollectors(new FieldCollector()).build();
        DataObject dataObject = new DataObject(GenericType.of(TestClass.class), null, null, propertyBehaviour);
        final Map<String, DataObject> children = dataObject.getChildren();

        assertThat(children.keySet()).containsExactly("bar", "baz", "foo");

        final DataObject bar = children.get("bar");
        assertThat(bar.getChildren()).isEmpty();
        assertThat(bar.getType()).isEqualTo(boolean.class);

        final DataObject baz = children.get("baz");
        final Map<String, DataObject> bazChildren = baz.getChildren();
        assertThat(bazChildren.keySet()).containsExactly("value");
        assertThat(baz.getType()).isEqualTo(TestData.class);

        final DataObject bazChild = bazChildren.get("value");
        assertThat(bazChild.getChildren()).isEmpty();
        assertThat(bazChild.getType()).isEqualTo(String.class);

        final DataObject foo = children.get("foo");
        assertThat(foo.getChildren()).isEmpty();
        assertThat(foo.getType()).isEqualTo(String.class);
    }

    @Test
    public void shouldNotMapUnwrappedContent() throws IntrospectionException {
        final PropertyBehaviour propertyBehaviour = PropertyBehaviour.builder().addPropertyCollectors(new FieldCollector()).build();
        DataObject dataObject = new DataObject(GenericType.of(TestWithUnwrappedClass.class), null, null, propertyBehaviour);
        final Map<String, DataObject> children = dataObject.getChildren();


        assertThat(children.keySet()).containsExactly("testClass", "value");
    }

    static class TestData {
        String value;
    }

    static class TestClass {
        String foo;
        boolean bar;
        TestData baz;


        public String foo() {
            return foo;
        }

        public String isFoo() {
            return foo;
        }

        public boolean isBar() {
            return bar;
        }

        public boolean getBar() {
            return bar;
        }

        public String addFoo(String foo) {
            this.foo += foo;
            return this.foo;
        }

        public String getFoo() {
            return null;
        }

        public void setFoo(String foo) {
            this.foo = foo;
        }
    }

    static class TestWithUnwrappedClass {
        String value;

        @JsonUnwrapped
        TestClass testClass;
    }

}