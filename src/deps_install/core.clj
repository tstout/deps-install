(ns deps-install.core
  (:require
   [clojure.java.io :as io]
   [deps-install.git :refer [clone-repo]]
   [clojure.edn :as edn]
   [clojure.pprint :refer [pprint]]
   [deps-install.git :refer [mk-staging-dir]])
  (:gen-class))

(defn read-conf []
  (-> (mk-staging-dir)
      (str "/conf.edn")
      slurp
      edn/read-string))


(defn -main [& args]
  (let [cfg-file (-> (mk-staging-dir)
                     (str "/conf.edn")
                     io/file)]
    (when-not (.exists cfg-file)
      (io/make-parents cfg-file)
      (with-open [out (io/writer (io/output-stream cfg-file))]
        (pprint {:group-id "foo" :artifact-id "bar"} out)
        (println "Stub config created at %s" cfg-file)
        (System/exit 0)))
    (clone-repo (read-conf))))


(comment
  (read-conf)




  ;;
  )