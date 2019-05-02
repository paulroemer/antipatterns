# AntiPatterns


[![Maven metadata URL](https://img.shields.io/maven-metadata/v/http/oss.sonatype.org/content/groups/public/com/github/fluorumlabs/antipatterns/maven-metadata.xml.svg)](https://oss.sonatype.org/content/groups/public/com/github/fluorumlabs/antipatterns/) 
[![GitHub](https://img.shields.io/github/license/fluorumlabs/antipatterns.svg)](https://github.com/fluorumlabs/antipatterns/blob/master/LICENSE)
[![Build Status](https://travis-ci.org/fluorumlabs/antipatterns.svg?branch=master)](https://travis-ci.org/fluorumlabs/antipatterns) 

## What?

`antipatterns` is a collection of helpers and weird stuff to make Java development easier and support harder :)

## Features

### Access private methods/fields/constructors in a type-safe manner

```java
import com.github.fluorumlabs.antipatterns.AntiPatterns;

...

@TargetClass(Boolean.class)
private interface BooleanMirror {
    @Static
    @DirectField
    void FALSE(Boolean value);

    static BooleanMirror attach() {
        return AntiPatterns.attachStatic(BooleanMirror.class);
    }
}

// Change Boolean.FALSE to true
BooleanMirror.attach().FALSE(true);

...

// Add fluent API to third-party libraries
private interface FluentList<T> extends AntiPatterns.Attachable<List<T>> {
    @ReturnType(boolean.class)
    FluentList<T> add(T value);

    @ReturnType(boolean.class)
    FluentList<T> addAll(@ArgumentType(Collection.class) TestFluentAPI<T> other);

    static <T> FluentList<T> attach(List<T> instance) {
        return AntiPatterns.attach(FluentList.class, instance);
    }
}

...

FluentList<String> fluentList = FluentList.attach(new ArrayList<String>());
fluentList.add("1").add("2").add("3");
List<String> list = fluentList.instance();
```

### Upgrade object instance to a subclass

```java
import com.github.fluorumlabs.antipatterns.AntiPatterns;

...

public class WebpageRouteRegistry extends ApplicationRouteRegistry {
    @Override
    public Optional<Class<? extends Component>> getNavigationTarget(String pathString, List<String> segments) {
        ...
    }
}

...

// upgrade ApplicationRouteRegistry to WebpageRouteRegistry
RouteRegistry newRegistry = AntiPatterns.upgrade(getRouteRegistry(), WebpageRouteRegistry.class);

// make a shallow clone
Entity sameButDifferent = AntiPatterns.shallowClone(entity);
```

### Access trusted MethodHandles.Lookup instance

```java
import com.github.fluorumlabs.antipatterns.AntiPatterns;

...

MethodHandle Matcher_getMatchedGroupIndex = AntiPatterns.lookupAll().findVirtual(Matcher.class, "getMatchedGroupIndex", MethodType.methodType(int.class, String.class));
```

### String interpolation

```java
import static com.github.fluorumlabs.antipatterns.AntiPatterns.interpolate;

...

// Replace tokens with actual values of current user
String result = interpolate("Hello, ${user.firstName} ${user.lastName}!", user -> getCurrentUser());

// String interpolation with format specifiers
String result = interpolate("${percent %.2f}% completed", percent -> 100*progressValue);
``` 

### Ignoring run-time exceptions

```java
import com.github.fluorumlabs.antipatterns.AntiPatterns;

...

// Wrap suppliers
AntiPatterns.guarded(() -> AntiPatterns.of(Integer.parse("234234j"))).ifPresent(...);

// Wrap functions
someStringOptional.map(AntiPatterns.guarded(id -> Integer.parse(id)).ifPresent(...);

// Wrap runnables
AntiPatterns.guarded(() -> discussionService.incrementViewCount(root));
```

### Getting `Optional` elements of array or `List`, 

```java
import com.github.fluorumlabs.antipatterns.AntiPatterns;

...

// Get first element of list/array or Optional.empty() if list/array is null or empty
AntiPatterns.getFirst(someList).ifPresent(...);

// Get 16-th element of list/array or Optional.empty() if list/array is null or has less then 17 elements
AntiPatterns.get(someList, 16).ifPresent(...);
```

### Trying sequentially suppliers of `Optional`

```java
import com.github.fluorumlabs.antipatterns.AntiPatterns;

...

// First try getEntityFromUrl, then, if it fails, getEntityFromSession, otherwise obtain default entity
SomeEntity entity = AntiPatterns.trySequentially(this::getEntityFromUrl, 
                                              this::getEntityFromSession, 
                                              this::getDefaultEntity)
    .orElseThrow(EntityNotFound::new);
```

### Simple builders for arrays and maps

```java
import static com.github.fluorumlabs.antipatterns.AntiPatterns.array;
import static com.github.fluorumlabs.antipatterns.AntiPatterns.hashMap;

...

// Build array of strings
String[] options = array("option1", "option2");

// Build hashmap with entries [("option1", optionObject1), ("option2", optionObject2)]
Map<String,Option> options = hashMap(option1 -> optionObject1, 
                                     option2 -> optionObject2);
```

### RegExp helpers

```java
import com.github.fluorumlabs.antipatterns.AntiPatterns;

...

// Pattern matching [[...]]
private static final Pattern TEMPLATE_PATTERN = Pattern.compile("\\[\\[([^\\]]+)\\]\\]");
// Properties
private Map<String,String> properties;

...

// Replace tokens [[...]] with properties
String result = AntiPatterns.replaceFunctional(TEMPLATE_PATTERN, message, groups -> properties.get(groups[1]));

// Get pattern matches as stream
Stream<String[]> groups = AntiPatterns.matchAsStream(TEMPLATE_PATTERN, message);
```

### Safe casting of objects

```java
import com.github.fluorumlabs.antipatterns.AntiPatterns;

...

// Cast baseEntity to Entity, or return Optional.empty() if it's not possible
Optional<Entity> entity = AntiPatterns.safeCast(baseEntity, Entity.class);

// Cast baseEntity to Entity in stream
Stream<Entity> entities = baseEntities.stream()
    .map(AntiPatterns.safeCast(Entity.class))
    .filter(Objects::nonNull);
```

## Usage

```xml
<dependency>
   <groupId>com.github.fluorumlabs</groupId>
   <artifactId>antipatterns</artifactId>
   <version>1.0.0-alpha3</version>
</dependency>
```
