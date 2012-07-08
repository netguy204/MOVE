(defproject cljs-base "1.0.0-SNAPSHOT"
  :description "FIXME: write description"
  :dependencies [[org.clojure/clojure "1.4.0"]]
  :dev-dependencies [[lein-cljsbuild "0.2.4"]]

  :cljsbuild
  {
   :test-commands
   {"unit" ["phantomjs"
            "phantom/unit-test.js"
            "resources/private/html/unit-test.html"]}
   :builds
   {:standard
    {:source-path "src-cljs"
     :compiler {:output-to "javascripts/main.js"
                :optimizations :whitespace
                :pretty-print true
                :static-fns true
                }}
    
    :advanced
    {:source-path "src-cljs"
     :compiler {:output-to "javascripts/compiled.js"
                :optimizations :advanced
                :pretty-print false
                :static-fns true
                }}

    :test
    {:source-path "test-cljs"
     :compiler {:output-to "resources/private/js/unit-test.js"
                :optimizations :whitespace
                :pretty-print true}}}})