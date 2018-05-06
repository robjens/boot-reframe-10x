
(set-env! :resource-paths #{"resources" "src"}
          ;; uncomment this if you write tests for your template:
          ;; :source-paths   #{"test"}
          :dependencies   '[[org.clojure/clojure "1.8.0"]
                            [boot/new "RELEASE"]
                            [adzerk/boot-test "RELEASE" :scope "test"]
                            [adzerk/bootlaces "RELEASE" :scope "test"]
                            ])

(def +version+ "0.1.0")
(def project 'boot-reframe-10x/boot-template)

(require '[adzerk.bootlaces :refer :all]
         '[adzerk.boot-test :refer [test]]
         '[boot.new :refer [new]])

(task-options!
  pom {:project     project
       :version     +version+
       :description "A boot template to bootstrap Day8 re-frame TODO MVC with 10x enabled live event tracing."
       :url         "https://github.com/robjens/boot-reframe-10x"
       :scm         {:url "https://github.com/robjens/boot-reframe-10x"}
       :license     {"Eclipse Public License"
                     "http://www.eclipse.org/legal/epl-v10.html"}})

(deftask build
  "Build and install the project locally."
  []
  (comp (pom) (jar) (install)))


(bootlaces! +version+)
