(ns com.gfredericks.catch-data)

(defn ^:private clause?
  [x]
  (and (seq? x)
       ('#{catch finally catch-data} (first x))))

(defn ^:private finally?
  [x]
  (and (seq? x) (= 'finally (first x))))

(defn ^:private cond-clause
  [clause throwable-sym data-sym]
  (let [[handler-name pred binder & body] clause]
    (cond
     (= 'catch handler-name)
     [`(instance? ~pred ~throwable-sym)
      `(let [~binder ~throwable-sym]
         ~@body)]

     (= 'catch-data handler-name)
     (let [condition
           ;; The first part of this and expression checks if the
           ;; throwable was an IExceptionInfo, because ex-data would
           ;; have returned nil otherwise
           `(and ~data-sym (~pred ~data-sym))

           ex-name (or (:ex binder) throwable-sym)
           binder (if (map? binder)
                    (dissoc binder :ex)
                    binder)

           wrapped-form
           `(let [~ex-name ~throwable-sym
                  ~binder ~data-sym]
              ~@body)]
       [condition wrapped-form])

     :else
     (throw (AssertionError.
             (str "Unknown try+ clause: " (pr-str clause)))))))

(defmacro try+
  "Like clojure's try, but allows catch-data clauses alongside catch
   clauses. A catch-data clause has the same structure as a catch
   clause, but it operates on the map inside a
   clojure.lang.IExceptionInfo object. For example, this clause will
   call the predicate :foo with the ex-data, and if it matches it
   will bind the data to the destructuring form and execute the body.

     (catch-data :foo {:keys [foo bar]}
       (+ foo bar))

   The binding form can be a symbol (to match the whole ex-data map),
   or a map to destructure the data. In the latter case, the special
   :ex key can be used to bind the exception object itself, like so:

     (catch-data :foo {:ex e}
       (println \"Oh noes!\")
       (throw e))"
  [& exprs]
  (let [[body clauses] (split-with (complement clause?) exprs)
        [handlers finalies] (split-with (complement finally?) clauses)
        t (gensym "t")
        data-name (gensym "info")
        cond-clauses (mapcat #(cond-clause % t data-name) handlers)]
    `(try
       ~@body
       ;; TODO: we could do some compile-time checks of the classes
       ;; involved so that we don't catch a more general class of
       ;; exceptions than necessary. Presumably most uses will only
       ;; be interested in IExceptionInfo.
       (catch Throwable ~t
         (let [~data-name (ex-data ~t)]
           (cond
            ~@cond-clauses
            :else (throw ~t))))
       ~@finalies)))

(defmacro throw-data
  "Like clojure clojure.core/throw but takes the same arguments as
   clojure.core/ex-info and then proceeds to throw the ExceptionInfo
   instance.

     (throw-data \"Oh noes!\" {:foo :bar})
     (throw-data \"Oh noes!\" {:foo :bar} (Throwable. \"Just because\"))"
  [& args]
  `(throw (ex-info ~@args)))
