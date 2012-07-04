(ns move.macros)

(defmacro report-result [success statement]
  `(if ~success
     (.log js/console ~(str "PASSED: " (pr-str statement)))
     (.error js/console ~(str "FAILED: " (pr-str statement)))))

(defmacro assert-true [statement & msg]
  `(report-result ~statement '~statement))

(defmacro assert-false [statement & msg]
  `(report-result (not ~statement) '~statement))

(defmacro with-test-group [group-name bindings & tests]
  (let [any-fail (gensym)]
    `(let ~bindings
       (let [~any-fail (atom false)]
         (.log js/console (str "==== " ~group-name " ===="))
         ~@(for [test tests]
             `(when-not ~test
                (reset! ~any-fail true)))
         (.log js/console ~(str "finished " group-name))

         ~any-fail))))

