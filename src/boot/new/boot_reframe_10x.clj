(ns boot.new.boot-reframe-10x
  (:require [boot.new.templates :refer [renderer year date project-name
                                        ->files sanitize-ns name-to-path
                                        multi-segment]]))

(defn boot-reframe-10x
  "A boot based re-frame 10x enabled SPA template in ClojureScript using TODOMVC for boilerplate & info."
  [name]
  (let [render (renderer "boot-reframe-10x")
        ns-name (sanitize-ns name)
        main-ns (multi-segment ns-name)
        data {:raw-name name
              :name (project-name name)
              :namespace main-ns
              :ns-name ns-name
              :nested-dirs (name-to-path main-ns)
              :year (year)
              :date (date)
              :sanitized (name-to-path name)}]
    (println (format "Generating new project named '%s'" ns-name))
    (->files data
             ["build.boot" (render "build.boot" data)]
             ["README.adoc" (render "README.adoc" data)]
             ["CHANGELOG.adoc" (render "CHANGELOG.adoc" data)]
             ["LICENSE" (render "LICENSE" data)]
             [".gitignore" (render "gitignore" data)]
             ["src/cljs/{{sanitized}}/core.cljs" (render "core.cljs" data)]
             ["src/cljs/{{sanitized}}/db.cljs" (render "db.cljs" data)]
             ["src/cljs/{{sanitized}}/events.cljs" (render "events.cljs" data)]
             ["src/cljs/{{sanitized}}/views.cljs" (render "views.cljs" data)]
             ["src/cljs/{{sanitized}}/subs.cljs" (render "subs.cljs" data)]
             ["test/cljs/{{sanitized}}/core_test.cljs" (render "core_test.cljs" data)]
             ["resources/public/js/client.cljs.edn" (render "client.cljs.edn" data)]
             ["resources/public/index.html" (render "index.html" data)]
             ["resources/public/styles.css" (render "styles.css" data)]
             )))
