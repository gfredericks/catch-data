# Catch Data

A tiny clojure lib with a `try+` macro that lets you catch
`ExceptionInfo` objects based on their contents. Requires Clojure 1.4
or more recent, of course.

## Wait what about Slingshot?

At the moment slingshot doesn't deal fluently with `ExceptionInfo`
objects, and if you're only planning on using `ExceptionInfo` then
it has a lot of unnecessary features.

## Usage

Leiningen dependency coordinates:

[![version](https://clojars.org/com.gfredericks/catch-data/latest-version.svg)](https://clojars.org/com.gfredericks/catch-data)

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

`catch-data` clauses can be intermingled with `catch` clauses, and
each will be tried in order. Currently `try+` compiles to a single
`catch` clause with a `cond` as the body.

## License

Copyright © 2013 Gary Fredericks

Distributed under the Eclipse Public License, the same as Clojure.
