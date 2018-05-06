
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
         '[boot.git :refer [last-commit]]
         '[boot.new :refer [new]])

(task-options!
  pom {:project     project
       :version     +version+
       :description "A boot template to bootstrap Day8 re-frame TODO MVC with 10x enabled live event tracing."
       :url         "https://github.com/robjens/boot-reframe-10x"
       :scm         {:url "https://github.com/robjens/boot-reframe-10x"}
       :license     {"Eclipse Public License"
                     "http://www.eclipse.org/legal/epl-v10.html"}}
  push {:repo "clojars" :gpg-sign false})

(deftask build
  "Build and install the project locally."
  []
  (comp (pom) (jar) (install)))

(bootlaces! +version+)

(ns-unmap 'boot.user 'push-release)

;; Ugly hack to get around bootlaces static gpg-sign true param
;; Using Git Bash, even after creating GPG secret key, it will fail due to "no tty" (mingw)
(def ^:private +last-commit+
  (try (last-commit) (catch Throwable _)))

(defn- get-creds []
  (mapv #(System/getenv %) ["CLOJARS_USER" "CLOJARS_PASS"]))

(deftask ^:private collect-clojars-credentials
  "Collect CLOJARS_USER and CLOJARS_PASS from the user if they're not set."
  []
  (fn [next-handler]
    (fn [fileset]
      (let [[user pass] (get-creds), clojars-creds (atom {})]
        (if (and user pass)
          (swap! clojars-creds assoc :username user :password pass)
          (do (println "CLOJARS_USER and CLOJARS_PASS were not set; please enter your Clojars credentials.")
              (print "Username: ")
              (#(swap! clojars-creds assoc :username %) (read-line))
              (print "Password: ")
              (#(swap! clojars-creds assoc :password %)
               (apply str (.readPassword (System/console))))))
        (merge-env! :repositories [["deploy-clojars" (merge @clojars-creds {:url "https://clojars.org/repo"})]])
        (next-handler fileset)))))


(deftask push-release
  "Deploy release version to Clojars."
  [f file PATH str "The jar file to deploy."
   s sign bool "Enable GPG signing of the JAR."]
  (comp
   (collect-clojars-credentials)
   (push
    :file           file
    :tag            (boolean +last-commit+)
    :gpg-sign       sign
    :ensure-release true
    :repo           "deploy-clojars")))
