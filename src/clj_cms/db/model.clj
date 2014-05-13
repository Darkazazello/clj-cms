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

(defentity children
  (table (config :db :children)))

(defentity record
  (pk :id)
  (table  (config :db :records)))

(defentity user
  (pk :id)
  (table (config :db :users)))

(defn get-tree []
  (letfn [(get-children [id] (select children
                              (fields :child_id)
                                (where {:parent_id [= id]})))
        (fetch-children [id] (select record
                                (join children (= :children.child_id :id))
                                (where {:children.parent_id [= id]})))
        (fetch-record [id] (select record
                              (where {:id [= id]})))
        (build-tree [node-ids accum]
                     (if (empty? node-ids)
                       accum
                       (let [new-nodes (map #(assoc (fetch-record %)  :children (get-children %)) node-ids)]
                         (build-tree (reduce #(conj %1 (:children new-nodes)) [] new-nodes) (conj accum new-nodes)))))]
    (build-tree (select record
                        (fields :id)
                        (where {:is_root [= true]})) [])))

(defn save-new [{:keys [name body parent-id is-leaf is-root]
                 :or {parent-id nil is-leaf false is-root false body ""}}]
  (transaction
   (let [data {:name name :body body :parent_id parent-id :is_leaf is-leaf :is_root is-root}
         record_id (insert record (values data))]
     (insert children (values {:parent_id parent-id :child_id record_id})))))     

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
  (transaction
   (let [fields {:name name :body body :parent_id parent-id
                 :is_leaf is-leaf :is_root is-root :is_locked 0 :locked_by nil :locked_at nil}]
     (update record (set-fields fields)
             (where {:id [= id]}))
     (update children (set-fields {:parent_id parent-id})
             (where {:child_id [= id]})))))