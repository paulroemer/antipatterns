package com.github.fluorumlabs.antipatterns;

import com.github.fluorumlabs.antipatterns.annotations.*;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.*;
import java.util.function.Function;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.IsNull.nullValue;

/**
 * Created by Artem Godin on 5/1/2019.
 */
public class ClassesTest {
    @Test
    public void safeCast() {
        List<String> list = new ArrayList<>();

        Assert.assertThat("Result is empty if types are incompatible",
                Classes.safeCast(list, Set.class).isPresent(), is(false));

        Assert.assertThat("Result is not empty if types compatible",
                Classes.safeCast(list, ArrayList.class).orElse(null), is(list));
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void safeCastFunction() {
        List<String> list = new ArrayList<>();

        Function<List, Set> incompatible = Classes.safeCast(Set.class);
        Function<List, ArrayList> compatible = Classes.safeCast(ArrayList.class);

        Assert.assertThat("Result is null if types are incompatible",
                incompatible.apply(list), nullValue());

        Assert.assertThat("Result is not null if types compatible",
                compatible.apply(list), is(list));
    }

    //////////////////////////////////////////////////////////////////////////////////////////////

    @TargetClass(TestSuperClass.class)
    private interface TestInstanceFields {
        @DirectField
        String getField();

        @DirectField
        void setField(String value);

        @DirectField
        String getFinalField();

        @DirectField
        void setFinalField(String value);

        @DirectField
        String value();

        String getValue();

        static TestInstanceFields attach(TestSuperClass instance) {
            return Classes.attach(TestInstanceFields.class, instance);
        }
    }

    @Test
    public void testGet() {
        TestSuperClass testSuperClass = new TestSuperClass();
        TestInstanceFields mirror = TestInstanceFields.attach(testSuperClass);

        Assert.assertThat("Field is read",
                mirror.getField(), is("field"));

        Assert.assertThat("Final field is read",
                mirror.getFinalField(), is("finalField"));

        Assert.assertThat("Field is read, bypassing getter",
                mirror.value(), is("no-args"));

        Assert.assertThat("Field is read using getter",
                mirror.getValue(), is("no-args-get-value"));
    }

    @Test
    public void testSet() {
        TestSuperClass testSuperClass = new TestSuperClass();
        TestInstanceFields mirror = TestInstanceFields.attach(testSuperClass);

        mirror.setField("field-patched");
        Assert.assertThat("Field value is set",
                mirror.getField(), is("field-patched"));

        mirror.setField(null);
        Assert.assertThat("Field value is set to null",
                mirror.getField(), is((String) null));

        mirror.setFinalField("finalField-patched");
        Assert.assertThat("Final field value is set",
                mirror.getFinalField(), is("finalField-patched"));

        mirror.setFinalField(null);
        Assert.assertThat("Final field value is set to null",
                mirror.getFinalField(), is((String) null));
    }

    //////////////////////////////////////////////////////////////////////////////////////////////

    @TargetClass(TestSuperClass.class)
    private interface TestStaticFields {
        @Static
        @DirectField
        String staticField();

        @Static
        @DirectField
        Integer staticFinalField();

        @Static
        @DirectField
        void staticField(String value);

        @Static
        @DirectField
        void staticFinalField(Integer newValue);

        static TestStaticFields attach() {
            return Classes.attachStatic(TestStaticFields.class);
        }
    }

    @Test
    public void testGetStatic() {
        TestStaticFields mirror = TestStaticFields.attach();

        mirror.staticField("staticField");
        mirror.staticFinalField(5);

        Assert.assertThat("Static field value is read",
                mirror.staticField(), is("staticField"));

        Assert.assertThat("Static final field value is read",
                mirror.staticFinalField(), is(5));
    }

    @Test
    public void testSetStatic() {
        TestStaticFields mirror = TestStaticFields.attach();

        mirror.staticField("staticField-patched");
        Assert.assertThat("Static field is set",
                mirror.staticField(), is("staticField-patched"));

        mirror.staticField(null);
        Assert.assertThat("Static field is set to null",
                mirror.staticField(), is((String) null));

        mirror.staticFinalField(10);
        Assert.assertThat("Static final field is set",
                TestSuperClass.testStatic(), is(10));

        mirror.staticFinalField(null);
        Assert.assertThat("Static final field is set to null",
                TestSuperClass.testStatic(), is((Integer) null));

        mirror.staticField("staticField");
        mirror.staticFinalField(5);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////

    @TargetClass(TestSuperClass.class)
    private interface TestVirtualMethods {
        void voidMethod();

        String test();

        String test(String value);

        String finalMethod();

        static TestVirtualMethods attach(TestSuperClass instance) {
            return Classes.attach(TestVirtualMethods.class, instance);
        }
    }

    @Test
    public void testCall() {
        TestSuperClass testSuperClass = new TestSuperClass();
        TestVirtualMethods mirror = TestVirtualMethods.attach(testSuperClass);

        // no fail
        mirror.voidMethod();

        Assert.assertThat("Method with no arguments is called",
                mirror.test(), is("no-args"));

        Assert.assertThat("Method with 1 argument is called",
                mirror.test("args"), is("no-args-args"));

        Assert.assertThat("Final public method with no arguments is called",
                mirror.finalMethod(), is("hello-from-final"));

    }

    //////////////////////////////////////////////////////////////////////////////////////////////

    @TargetClass(TestSuperClass.class)
    private interface TestStaticMethods {
        @Static
        Integer testStatic();

        @Static
        Integer testStatic(int value);

        static TestStaticMethods attach() {
            return Classes.attachStatic(TestStaticMethods.class);
        }
    }

    @Test
    public void testStaticCall() {
        TestStaticMethods mirror = TestStaticMethods.attach();

        Assert.assertThat("Static method with no arguments is called",
                mirror.testStatic(), is(5));

        Assert.assertThat("Static method with 1 argument is called",
                mirror.testStatic(2), is(7));
    }

    //////////////////////////////////////////////////////////////////////////////////////////////

    @TargetClass(TestSuperClass.class)
    private interface TestConstructors {
        @Constructor
        TestSuperClass newInstance();

        @Constructor
        TestSuperClass newInstance(String value);

        static TestConstructors attach() {
            return Classes.attachStatic(TestConstructors.class);
        }
    }

    @Test
    public void testNewInstance() {
        TestConstructors mirror = TestConstructors.attach();

        TestSuperClass testSuperClass1 = mirror.newInstance();
        Assert.assertThat("New instance is created via public constructor",
                testSuperClass1.test(), is("no-args"));

        TestSuperClass testSuperClass2 = mirror.newInstance("super-class-2");
        Assert.assertThat("New instance is created via private constructor",
                testSuperClass2.test(), is("super-class-2"));
    }

    //////////////////////////////////////////////////////////////////////////////////////////////

    @TargetClass(TestSuperClass.class)
    private interface TestSuperMethods {
        @Super(TestSuperClass.class)
        String test();

        static TestSuperMethods attach(TestSuperClass instance) {
            return Classes.attach(TestSuperMethods.class, instance);
        }
    }

    @Test
    public void testCallSuper() {
        TestSuperClass.TestClass testClass = new TestSuperClass.TestClass();
        TestSuperMethods mirrorSuper = TestSuperMethods.attach(testClass);
        TestVirtualMethods mirror = TestVirtualMethods.attach(testClass);

        Assert.assertThat("Instance method is called",
                mirror.test(), is("from-test-class"));

        Assert.assertThat("Method of super class is called",
                mirrorSuper.test(), is("test-class"));
    }

    //////////////////////////////////////////////////////////////////////////////////////////////

    @TargetClass(TestSuperClass.class)
    private interface TestDefaultMethods extends TestInstanceFields {
        default String getActualValue() {
            return value();
        }

        static TestDefaultMethods attach(TestSuperClass instance) {
            return Classes.attach(TestDefaultMethods.class, instance);
        }
    }

    @Test
    public void testDefaultMethods() {
        TestSuperClass testSuperClass = new TestSuperClass();
        TestDefaultMethods mirror = TestDefaultMethods.attach(testSuperClass);

        Assert.assertThat("Field is read, bypassing getter",
                mirror.value(), is("no-args"));

        Assert.assertThat("Field is read using getter",
                mirror.getValue(), is("no-args-get-value"));

        Assert.assertThat("Field is read via default method",
                mirror.getActualValue(), is("no-args"));
    }

    //////////////////////////////////////////////////////////////////////////////////////////////

    @TargetClass(TestSuperClass.class)
    private interface TestOptionalWrapping extends TestInstanceFields {
        @DirectField
        @ReturnType(String.class)
        Optional<String> field();

        static TestOptionalWrapping attach(TestSuperClass instance) {
            return Classes.attach(TestOptionalWrapping.class, instance);
        }
    }

    @Test
    public void testOptionalWrapping() {
        TestSuperClass testSuperClass = new TestSuperClass();
        TestOptionalWrapping mirror = TestOptionalWrapping.attach(testSuperClass);

        mirror.setField(null);

        Assert.assertThat("Field is read via normal method",
                mirror.getField(), is((String) null));

        Assert.assertThat("Null field value is read via Optional method",
                mirror.field().isPresent(), is(false));

        mirror.setField("12345");

        Assert.assertThat("Non-null field value is read via Optional method",
                mirror.field().orElse("???"), is("12345"));
    }

    //////////////////////////////////////////////////////////////////////////////////////////////

    private interface TestFluentAPI<T> extends Classes.Attachable<List<T>> {
        @ReturnType(boolean.class)
        TestFluentAPI<T> add(T value);

        @ReturnType(boolean.class)
        TestFluentAPI<T> addAll(@ArgumentType(Collection.class) TestFluentAPI<T> other);

        static <T> TestFluentAPI<T> attach(List<T> instance) {
            return Classes.attach(TestFluentAPI.class, instance);
        }
    }

    @Test
    public void testFluentAPI() {
        List<String> list = new ArrayList<>();
        TestFluentAPI<String> mirror = TestFluentAPI.attach(list);

        mirror.add("1").add("2").add("3");

        Assert.assertThat("Elements were added via fluent API",
                Arrays.toString(list.toArray()), is("[1, 2, 3]"));

        TestFluentAPI<String> mirror2 = TestFluentAPI.attach(new ArrayList<>());
        mirror2.add("4");
        mirror2.addAll(mirror);

        Assert.assertThat("Elements were added via fluent API from external mirror",
                Arrays.toString(mirror2.instance().toArray()), is("[4, 1, 2, 3]"));

    }

    //////////////////////////////////////////////////////////////////////////////////////////////

    @Test
    public void testUpgrade() {
        TestSuperClass testSuperClass = new TestSuperClass();
        TestSuperClass.TestClass2 upgradedInstance = Classes.upgrade(testSuperClass, TestSuperClass.TestClass2.class);

        Assert.assertThat("Upgraded instance is of correct class",
                upgradedInstance, is(instanceOf(TestSuperClass.TestClass.class)));

        Assert.assertThat("Field values were copied",
                upgradedInstance.getValue(), is("no-args-get-value"));

        Assert.assertThat("New methods are in place",
                upgradedInstance.test(), is("from-test-class"));

        Assert.assertThat("No constructors were called",
                upgradedInstance.testClassMarker, is((String) null));
    }

    @Test
    public void testUpgradeIndirect() {
        OptionalInt optionalInt = OptionalInt.of(17);
        Integer integer = Classes.upgradeIndirect(optionalInt, Integer.class);

        Assert.assertThat("OptionalInt is upgraded to Integer",
                integer, is(17));
    }

    @Test
    public void testUpgradeIndirectWithRemap() {
        TestSuperClass testSuperClass = new TestSuperClass();
        TestSuperClass.TestClass2 upgradedInstance = Classes.upgradeIndirect(testSuperClass, TestSuperClass.TestClass2.class, Builders.hashMap(value -> "testClassMarker"));

        Assert.assertThat("Original field is uninitialized",
                upgradedInstance.getValue(), is("null-get-value"));

        Assert.assertThat("Remapped field is initialized",
                upgradedInstance.testClassMarker, is("no-args"));
    }

    //////////////////////////////////////////////////////////////////////////////////////////////

    @TargetClass(Boolean.class)
    private interface BooleanMirror {
        @Static
        @DirectField
        void FALSE(Boolean value);

        static BooleanMirror attach() {
            return Classes.attachStatic(BooleanMirror.class);
        }
    }

    @SuppressWarnings({"ConstantConditions", "IgnoredJUnitTest"})
    @Test
    @Ignore // It really works, but we don't want to upset tests running in parallel
    public void bullshittery() {
        Boolean original = false;

        BooleanMirror mirror = BooleanMirror.attach();

        mirror.FALSE(true);

        Assert.assertThat("Boolean.FALSE is redefined",
                String.format("Everything is %s", false), is("Everything is true"));

        mirror.FALSE(original);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Test class for AntiPattern tests
     */
    public static class TestSuperClass {
        private String field = "field";
        private final String finalField = "finalField";
        private static String staticField = "staticField";
        private static final Integer staticFinalField = 5;

        private final String value;

        public TestSuperClass() {
            this("no-args");
        }

        private TestSuperClass(String value) {
            this.value = value;
        }

        public String test() {
            return value;
        }

        public String getValue() {
            return value + "-get-value";
        }

        public final String finalMethod() {
            return "hello-from-final";
        }

        public void setField(String field) {
            this.field = field + "-set-field";
        }

        public static Integer testStatic() {
            return staticFinalField;
        }

        public String test(String x) {
            return value + "-" + x;
        }

        private static Integer testStatic(int x) {
            return staticFinalField + x;
        }

        public void voidMethod() {
        }

        public static class TestClass extends TestSuperClass {
            public String testClassMarker = "testClass";

            public TestClass() {
                super("test-class");
            }

            @Override
            public String test() {
                return "from-test-class";
            }
        }

        public static class TestClass2 extends TestClass {

        }
    }

}
