watch( '(.*)cljs/move/(.*)\.cljs' ) { system("rm -rf resources/private/js"); system("lein cljsbuild test unit"); }

watch( 'src/move/(.*)\.clj' ) { system("rm -rf resources/private/js"); system("lein cljsbuild test unit"); }
