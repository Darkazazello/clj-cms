(ns clj-cms.db
  (:use korma.db
        korma.core
        carica.core))

(def db-con (mysql {:host (config :db :host)
                    :db (config :db :schema)
                    :user (config :db :user)
                    :password (config :db :password)
                    :make-pool? false}))

(defdb cms-db db-con)

(defentity record
  (pk :id)
  (table  (config :db :records)))

(defentity user
  (pk :id)
  (table (config :db :users)))

(defn get-tree [] (select record))

(defn save-new [{:keys [name body parent-id is-leaf is-root]
                   :or {parent-id nil is-leaf false is-root false body ""}}]
  (insert record (values {:name name :body body :parent_id parent-id :is_leaf is-leaf :is_root is-root})))

(defn lock-node [{:keys [id user-id] :or {user-id -1}}]
  (transaction
   (println id user-id)
   (let [query ["select id from record where id = ? and is_locked = false for update" [id]]
         update-query (fn [id]
                        (if (nil? id)
                          (do (rollback)
                              {:status :failed  :message "Not found"})
                          
                          (do
                            (update record (set-fields {:is_locked 1 :locked_by user-id
                                                        :locked_at (java.util.Date.)})
                                    (where {:id [= (:id id)]}))
                            {:status :success})))]
     (-> (exec-raw query :results)
         first
         update-query))))

(defn unlock-node [{:keys [id user-id] :or {:user-id -1}}]
  "Unlock record"
  (update record (set-fields {:is_locked 0 :locked_by nil :locked_at nil}) (where {:id [= id] :locked_by [= user-id]
                                                                                   :is_locked [= true]})))

(defn save-old [& {:keys [id name body parent-id is-leaf is-root]
                   :or {parent-id nil is-leaf false is-root false body ""}}]
  (update record (set-fields {:name name :body body :parent_id parent-id
                              :is_leaf is-leaf :is_root is-root :is_locked 0 :locked_by nil :locked_at nil})
          (where {:id [= id]})))