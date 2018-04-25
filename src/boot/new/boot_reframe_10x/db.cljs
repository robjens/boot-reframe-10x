(ns {{ns-name}}.db
  (:require [cljs.reader]
            [cljs.spec.alpha :as s]
            [re-frame.core :as re-frame]))

;; -- Spec --------------------------------------------------------------------
;;
;; This is a clojure.spec specification for the value in app-db. It is like a
;; Schema. See: http://clojure.org/guides/spec
;;
;; The value in app-db should always match this spec. Only event handlers
;; can change the value in app-db so, after each event handler
;; has run, we re-check app-db for correctness (compliance with the Schema).
;;
;; How is this done? Look in events.cljs and you'll notice that all handlers
;; have an "after" interceptor which does the spec re-check.
;;
;; None of this is strictly necessary. It could be omitted. But we find it
;; good practice.

(s/def ::id int?)
(s/def ::title string?)
(s/def ::done boolean?)
(s/def ::post (s/keys :req-un [::id ::title ::done]))
(s/def ::posts (s/and                                       ;; should use the :kind kw to s/map-of (not supported yet)
                 (s/map-of ::id ::post)                     ;; in this map, each post is keyed by its :id
                 #(instance? PersistentTreeMap %)           ;; is a sorted-map (not just a map)
                 ))
(s/def ::showing                                            ;; what posts are shown to the user?
  #{:all                                                    ;; all posts are shown
    :active                                                 ;; only posts whose :done is false
    :done                                                   ;; only posts whose :done is true
    })
(s/def ::db (s/keys :req-un [::posts ::showing]))

;; -- Default app-db Value  ---------------------------------------------------
;;
;; When the application first starts, this will be the value put in app-db
;; Unless, of course, there are posts in the LocalStore (see further below)
;; Look in:
;;   1.  `core.cljs` for  "(dispatch-sync [:initialise-db])"
;;   2.  `events.cljs` for the registration of :initialise-db handler
;;

(def default-db           ;; what gets put into app-db by default.
  {:posts   (sorted-map)  ;; an empty list of posts. Use the (int) :id as the key
   :showing :all})        ;; show all posts


;; -- Local Storage  ----------------------------------------------------------
;;
;; Part of the postmvc challenge is to store posts in LocalStorage, and
;; on app startup, reload the posts from when the program was last run.
;; But the challenge stipulates to NOT load the setting for the "showing"
;; filter. Just the posts.
;;

(def ls-key "solobit-website")                         ;; localstore key

(defn posts->local-store
  "Puts posts into localStorage"
  [posts]
  (.setItem js/localStorage ls-key (str posts)))     ;; sorted-map written as an EDN map


;; -- cofx Registrations  -----------------------------------------------------

;; Use `reg-cofx` to register a "coeffect handler" which will inject the posts
;; stored in localstore.
;;
;; To see it used, look in `events.cljs` at the event handler for `:initialise-db`.
;; That event handler has the interceptor `(inject-cofx :local-store-posts)`
;; The function registered below will be used to fulfill that request.
;;
;; We must supply a `sorted-map` but in LocalStore it is stored as a `map`.
;;
(re-frame/reg-cofx
  :local-store-posts
  (fn [cofx _]
      ;; put the localstore posts into the coeffect under :local-store-posts
      (assoc cofx :local-store-posts
             ;; read in posts from localstore, and process into a sorted map
             (into (sorted-map)
                   (some->> (.getItem js/localStorage ls-key)
                            (cljs.reader/read-string)    ;; EDN map -> map
                            )))))

