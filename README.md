# AntiPatterns


[![Maven metadata URL](https://img.shields.io/maven-metadata/v/http/oss.sonatype.org/content/groups/public/com/github/fluorumlabs/antipatterns/maven-metadata.xml.svg)](https://oss.sonatype.org/content/groups/public/com/github/fluorumlabs/antipatterns/) 
[![GitHub](https://img.shields.io/github/license/fluorumlabs/antipatterns.svg)](https://github.com/fluorumlabs/antipatterns/blob/master/LICENSE)
 [![Build Status](https://travis-ci.com/fluorumlabs/antipatterns.svg?branch=master)](https://travis-ci.com/fluorumlabs/antipatterns) 

## What?

`antipatterns` is a collection of helpers and weird stuff to make Java development easier and support harder :)

## Features

- Wrappers for `Function`/`Map`/`Runnable` for ignoring run-time exceptions (for use with `Optional` and `Stream` API)

```java
import com.github.fluorumlabs.antipatterns.Wrappers;

...

// Wrap suppliers
Wrappers.guarded(() -> Optional.of(Integer.parse("234234j"))).ifPresent(...);

// Wrap functions
someStringOptional.map(Wrappers.guarded(id -> Integer.parse(id)).ifPresent(...);

// Wrap runnables
Wrappers.guarded(() -> discussionService.incrementViewCount(root));
```

- Getting `Optional` elements of array or `List`, 

```java
import com.github.fluorumlabs.antipatterns.Optionals;

...

// Get first element of list/array or Optional.empty() if list/array is null or empty
Optionals.getFirst(someList).ifPresent(...);

// Get 16-th element of list/array or Optional.empty() if list/array is null or has less then 17 elements
Optionals.get(someList, 16).ifPresent(...);
```

- Trying sequentially suppliers of `Optional`

```java
import com.github.fluorumlabs.antipatterns.Optionals;

...

// First try getEntityFromUrl, then, if it fails, getEntityFromSession, otherwise obtain default entity
SomeEntity entity = Optionals.trySequentially(this::getEntityFromUrl, 
                                              this::getEntityFromSession, 
                                              this::getDefaultEntity)
    .orElseThrow(EntityNotFound::new);
```

- Simple builders for arrays and maps

```java
import static com.github.fluorumlabs.antipatterns.Builders.array;
import static com.github.fluorumlabs.antipatterns.Builders.hashMap;

...

// Build array of strings
String[] options = array("option1", "option2");

// Build hashmap with entries [("option1", optionObject1), ("option2", optionObject2)]
Map<String,Option> options = hashMap(option1 -> optionObject1, 
                                     option2 -> optionObject2);
```

- RegExp helpers

```java
import com.github.fluorumlabs.antipatterns.Strings;

...

// Pattern matching [[...]]
private static final Pattern TEMPLATE_PATTERN = Pattern.compile("\\[\\[([^\\]]+)\\]\\]");
// Properties
private Map<String,String> properties;

...

// Replace tokens [[...]] with properties
String result = Strings.replaceFunctional(TEMPLATE_PATTERN, message, groups -> properties.get(groups[1]));

// Get pattern matches as stream
Stream<String[]> groups = Strings.matchAsStream(TEMPLATE_PATTERN, message);
```

- String interpolation

```java
import static com.github.fluorumlabs.antipatterns.Strings.interpolate;

...

// Replace tokens with actual values of current user
String result = interpolate("Hello, ${user.firstName} ${user.lastName}!", user -> getCurrentUser());

// String interpolation with format specifiers
String result = interpolate("${percent %.2f}% completed", percent -> 100*progressValue);
``` 

- Safe casting of objects

```java
import com.github.fluorumlabs.antipatterns.Classes;

...

// Cast baseEntity to Entity, or return Optional.empty() if it's not possible
Optional<Entity> entity = Classes.safeCast(baseEntity, Entity.class);

// Cast baseEntity to Entity in stream
Stream<Entity> entities = baseEntities.stream()
    .map(Classes.safeCast(Entity.class))
    .filter(Objects::nonNull);
```

- Access private methods/fields/constructors in a type-safe manner

```java
import com.github.fluorumlabs.antipatterns.Classes;

...

@TargetClass(Boolean.class)
private interface BooleanMirror {
    @Static
    @DirectField
    void FALSE(Boolean value);

    static BooleanMirror attach() {
        return Classes.attachStatic(BooleanMirror.class);
    }
}

// Change Boolean.FALSE to true
BooleanMirror.attach().FALSE(true);

...

// Add fluent API to third-party libraries
private interface FluentList<T> extends Classes.Attachable<List<T>> {
    @ReturnType(boolean.class)
    FluentList<T> add(T value);

    @ReturnType(boolean.class)
    FluentList<T> addAll(@ArgumentType(Collection.class) TestFluentAPI<T> other);

    static <T> FluentList<T> attach(List<T> instance) {
        return Classes.attach(FluentList.class, instance);
    }
}

...

FluentList<String> fluentList = FluentList.attach(new ArrayList<String>());
fluentList.add("1").add("2").add("3");
List<String> list = fluentList.instance();
```

- Upgrade object instance to a subclass

```java
import com.github.fluorumlabs.antipatterns.Classes;

...

public class WebpageRouteRegistry extends ApplicationRouteRegistry {
	@Override
	public Optional<Class<? extends Component>> getNavigationTarget(String pathString, List<String> segments) {
    ...
    }
}

...

// upgrade ApplicationRouteRegistry to WebpageRouteRegistry
RouteRegistry newRegistry = Classes.upgrade(getRouteRegistry(), WebpageRouteRegistry.class);

// make a shallow clone
Entity sameButDifferent = Classes.shallowClone(entity);
```

- Access trusted MethodHandles.Lookup instance

```java
import com.github.fluorumlabs.antipatterns.Classes;

...

MethodHandle Matcher_getMatchedGroupIndex = Classes.lookupAll().findVirtual(Matcher.class, "getMatchedGroupIndex", MethodType.methodType(int.class, String.class));
```

## Usage

```xml
<dependency>
   <groupId>com.github.fluorumlabs</groupId>
   <artifactId>antipatterns</artifactId>
   <version>1.0.0-alpha1</version>
</dependency>
```
