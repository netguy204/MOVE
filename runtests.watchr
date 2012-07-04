watch( '(.*)cljs/move/(.*)\.cljs' ) { system("rm -rf resources/private/js"); system("lein cljsbuild test"); }

watch( 'src/move/(.*)\.clj' ) { system("rm -rf resources/private/js"); system("lein cljsbuild test"); }
