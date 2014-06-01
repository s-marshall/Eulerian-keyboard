(def entity-component-db (hash-map))

(defn entity []
  (keyword (str (java.util.UUID/randomUUID))))

(defmacro component [component-name params & settings]
  `(defn ~component-name ~params
     (merge ~{:name (keyword (name component-name))} ~@settings)))

(defn add-component-to-entity [entity-component-db component entity]
  (let [stored-component (entity entity-component-db)
        not-stored? (nil? stored-component)
        entity-component-db (if not-stored?
                              (merge {entity [component]} entity-component-db)
                              (let [has-component? (some #(= (:name %) (:name component)) stored-component)]
                                (if has-component?
                                  entity-component-db
                                  (swap! (entity entity-component-db) (conj stored-component component)))))]

    entity-component-db))

(defn remove-entity [entity]
  (apply dissoc entity-component-db [entity]))

(defn entities-with-component [component-name]
  (for [entity-components entity-component-db
        :let [entity (key entity-components)
              components (val entity-components)]
        :when (some #(= (:name %) component-name) components)]
    [entity
     (first (filter #(= component-name (:name %)) (entity entity-component-db)))]))

(defn component-in-entity [component-name entity]
  (first (filter #(= component-name (:name %)) (entity entity-component-db))))

(defn entities-satisfying [goal category entities]
  (filter #(= goal @(category (last %))) entities))

