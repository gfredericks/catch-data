(ns com.gfredericks.catch-data)

(defn ^:private clause?
  [x]
  (and (seq? x)
       ('#{catch finally catch-data} (first x))))

(defn ^:private finally?
  [x]
  (and (seq? x) (= 'finally (first x))))

(defn ^:private cond-clause
  [clause throwable-sym]
  (let [[handler-name pred binder & body] clause]
    (cond
     (= 'catch handler-name)
     [`(instance? ~pred ~throwable-sym)
      (cons 'do body)]

     (= 'catch-data handler-name)
     (let [condition
           `(and (instance? clojure.lang.IExceptionInfo ~throwable-sym)
                 (~pred (ex-data ~throwable-sym)))

           data-name (gensym "data")

           ex-name (or (:ex binder) throwable-sym)
           binder (if (map? binder)
                    (dissoc binder :ex)
                    binder)

           wrapped-form
           `(let [~ex-name ~throwable-sym
                  ~binder (ex-data ~throwable-sym)]
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
        cond-clauses (mapcat #(cond-clause % t) handlers)]
    `(try
       ~@body
       ;; TODO: we could do some compile-time checks of the classes
       ;; involved so that we don't catch a more general class of
       ;; exceptions than necessary. Presumably most uses will only
       ;; be interested in IExceptionInfo.
       (catch Throwable ~t
         (cond
          ~@cond-clauses
          :else (throw ~t)))
       ~@finalies)))
