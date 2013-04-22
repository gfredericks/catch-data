# Catch Data

A tiny clojure lib with a `try+` macro that lets you catch
`ExceptionInfo` objects based on their contents. Requires Clojure 1.4
or more recent, of course.

Also I haven't actually written the code yet. But I swear I'm about to
I just have to go home and have dinner first.

## Wait what about Slingshot?

At the moment slingshot doesn't deal fluently with `ExceptionInfo`
objects, and if you're only planning on using `ExceptionInfo` then
it has a lot of unnecessary features.

## Usage

``` clojure
(require '[com.gfredericks.catch-data :refer [try+]])

(try+
  (some codez)
  (catch-data :foo {bar :foo, :ex the-exception}
    (do something with bar or the-exception)))
```

The first argument to `catch-data` is any predicate that will be
passed the map inside the exception. The second is a binding for the
map. When using map destructuring, you can use the special `:ex` key
to get a handle on the exception object itself.

`catch-data` can be used with a regular `catch`, but all `catch-data`
clauses must appear before any `catch` clauses.

## License

Copyright © 2013 Gary Fredericks

Distributed under the Eclipse Public License, the same as Clojure.
