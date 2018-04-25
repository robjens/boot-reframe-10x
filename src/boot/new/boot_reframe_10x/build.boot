(def project '{{raw-name}})
(def +version+ "0.1.0-SNAPSHOT")

(set-env! :resource-paths #{"resources" "src/cljs"}
          :source-paths   #{"test"}
          :dependencies   '[[org.clojure/clojure         "1.8.0"]

                            [org.clojure/clojurescript   "1.9.908"]
                            [adzerk/boot-cljs            "2.0.0"          :scope "test"]
                            [adzerk/boot-cljs-repl       "0.3.3"          :scope "test"]
                            [crisptrutski/boot-cljs-test "0.3.0"          :scope "test"]

                            ;; https://github.com/adzerk-oss/boot-reload/issues/117
                            [adzerk/boot-reload          "0.5.2-SNAPSHOT" :scope "test"]

                            [pandeiro/boot-http          "0.8.3"          :scope "test" :exclusions [[org.clojure/clojure]]]
                            [com.cemerick/piggieback     "0.2.2"          :scope "test" :exclusions [[org.clojure/clojure]]]

                            [weasel                      "0.7.0"          :scope "test"]
                            [org.clojure/tools.nrepl     "0.2.12"         :scope "test"]
                            [com.pupeno/free-form        "0.6.0"          :scope "test"]
                            [com.novemberain/validateur  "2.6.0"          :scope "test"]
                            [cljsjs/create-react-class   "15.6.2-0"       :scope "test"]
                            [reagent                     "0.7.0"          :scope "provided"]
                            [re-frame                    "0.10.5"         :scope "provided"]
                            [day8.re-frame/re-frame-10x  "0.3.0"          :scope "provided"]
                            [binaryage/devtools          "0.9.4"          :scope "test"]
                            [day8.re-frame/tracing       "0.5.0"          :scope "test"]
                            [figwheel-sidecar            "0.5.13"         :scope "test"]
                            [re-frisk                    "0.5.3"          :scope "test"]
                            [secretary                   "1.2.3"          :scope "test"]
                            ])

(task-options!
  pom {:project     project
       :version     +version+
       :description "FIXME: write description"
       :url         "http://example/FIXME"
       :scm         {:url "https://github.com/yourname/{{name}}"}
       :license     {"Eclipse Public License"
                     "http://www.eclipse.org/legal/epl-v10.html"}})

(require
  '[adzerk.boot-cljs :refer [cljs]]
  '[adzerk.boot-cljs-repl :refer [cljs-repl start-repl]]
  '[adzerk.boot-reload :refer [reload]]
  '[pandeiro.boot-http :refer [serve]]
  '[crisptrutski.boot-cljs-test :refer [test-cljs]]
  '[boot.util :as util]
  )

(deftask build
  "This task contains all the necessary steps to produce a build
  You can use 'profile-tasks' like `production` and `development`
  to change parameters (like optimizations level of the cljs compiler)"
  []
  (comp (speak)
        (cljs)))

(deftask run
  "The `run` task wraps the building of your application in some useful tools for local development: an http server, a
  file watcher a ClojureScript REPL and a hot reloading mechanism"
  [] (comp (serve)
           (watch)
           (cljs-repl :nrepl-opts {:port 9090})
           (reload)
           (build)))

(deftask production []
  (task-options! cljs {:optimizations :advanced})
  identity)

(deftask development []
  (task-options! cljs {:optimizations :none}
                 reload {:on-jsload '{{namespace}}/main})
  identity)

(deftask dev
  "Simple alias to run application in development mode"
  []
  (comp (development)
        (run)))

(deftask testing []
  (set-env! :source-paths #(conj % "test/cljs"))
  identity)

;;; This prevents a name collision WARNING between the test task and
;;; clojure.core/test, a function that nobody really uses or cares
;;; about.
(ns-unmap 'boot.user 'test)

(deftask test []
  (comp (testing)
        (test-cljs :js-env :phantom
                   :exit?  true)))

(deftask auto-test []
  (comp (testing)
        (watch)
        (test-cljs :js-env :phantom)))
