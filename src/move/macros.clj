(ns move.macros)

(defmacro report-result [success statement]
  `(if ~success
     (.log js/console ~(str "PASSED: " (pr-str statement)))
     (.error js/console ~(str "FAILED: " (pr-str statement)))))

(defmacro assert-true [statement & msg]
  `(report-result ~statement ~statement))

(defmacro assert-false [statement & msg]
  `(report-result (not ~statement) ~statement))

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


(defmacro doasync [bindings]
  "Converts a sequence of binding forms into synchronous and
asynchronous calls.

  Bindings look mostly like the bindings in a let form (including
  destructuring capability) but the RHS of each binding takes on
  special meaning if it happens to be a vector.

  If the RHS of a binding is a vector then its treating like an
  asynchronous invocation. Async invocations look like normal
  invocations (other than the shape of their parentheses) but the
  function must accept a callback as its final argument. The LHS of
  the binding will only be established once the callback fires and it
  will take on the value that was given to the callback.

  The dosync form does not return a meaningful value because of its
  asynchronous execution behavior.

  For example:

  (dosync
    [name [get-json \"/name.json\"]
     profile-target (str name \".html\")
     profile [get-json profile-target]
     _ (set-profile-data profile)])

  Here the get-json call is executed asynchronously. It's real
  signature looks something like:

  (defn get-json [url callback] ...)

  When get-json calls its callback with the data, that data is boiund
  to name and the next binding in dosync is allowed to execute. Since
  the RHS of this form is not a vector, it executes immediately to
  create the binding on the RHS. Next, another get-json request fires
  asynchronously with the computed url. Finally, we synchronously tell
  our view to update with the data that we got back from the call.
"
  (reduce
   (fn [next [bind-var expr]]
     (if (vector? expr)
       ;; asynchronous expression
       `(~@expr (fn [~bind-var] ~next))

       ;; synchronous expression
       `(let [~bind-var ~expr]
          ~next)))
   'identity
   (reverse (partition 2 bindings))))