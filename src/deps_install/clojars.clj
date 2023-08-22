(ns deps-install.clojars
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            #_[clojure.tools.logging :as log])
  (:import [java.net.http
            HttpClient
            HttpRequest
            HttpResponse$BodyHandlers
            HttpClient$Redirect]
           [java.net URI]))

(defn get-request
  "Generic htpt GET request utilizing java11 http client."
  [uri]
  (-> (HttpRequest/newBuilder)
      .GET
      (.uri (URI/create uri))
      (.setHeader "User-Agent" "Java 11+")
      #_(.followRedirects HttpClient$Redirect/ALWAYS)
      .build))

(defn http-tx
  "Transmit an http request. The response is a byte array."
  [req]
  (-> (HttpClient/newBuilder)
      (.followRedirects HttpClient$Redirect/ALWAYS)
      .build
      (.send req (HttpResponse$BodyHandlers/ofByteArray))))

(defn uncompress
  "Given a gzipped byte array, convert it to an uncompressed
   string"
  [byte-array]
  (with-open [in (java.util.zip.GZIPInputStream.
                  (io/input-stream byte-array))]
    (slurp in)))

(defn fetch
  "Download meta data from clojars. The data from clojars is a compressed
   gz file. Each line is a map. This fn uncompresses the file, and returns
   a vector of maps."
  [uri]
  #_(log/infof "fetch-acccount %s" uri)
  (->> uri
       get-request
       http-tx
       .body
       uncompress
       (format "[%s]")
       edn/read-string))

(def clojars-meta
  "Download of clojars meta data"
  (delay
    (println "Fetching clojars metadata...this takes a while.")
    (let [meta (fetch "http://clojars.org/repo/feed.clj.gz")]
      (println "Fetching metadata complete")
      meta)))

(defn filter-scm [m]
  (let [{{:keys [connection developer-connection]} :scm} m]
    {:conn connection
     :dev-conn developer-connection}))

(defn filter-meta [x]
  (-> (map #(select-keys % [:group-id :artifact-id :scm]) x)))

(defn select-group [grp]
  (->> @clojars-meta
       (filter #(= (:group-id %) grp))))

(defn- throw-if-nil [msg m]
  (if (nil? m)
    (throw (Exception. (format "Does not exist %s" msg)))
    m))

(defn select-artifact [grp artifact-id]
  (->> grp
       select-group
       (filter #(= (:artifact-id %) artifact-id))
       first
       (throw-if-nil (format "group: %s artifact %s" grp artifact-id))))

;; TODO - need better error handling here when 
;; coordinates not found in meta from clojars
(defn calc-github-address [m]
  (let [{{:keys [url]} :scm} m]
    (if (.contains url "github")
      (str url ".git")
      (throw (.Exception
              (format "url for artifact is '%s' - not in github" url))))))

(defn repo-clone-address [group artifact-id]
  (-> #_(select-artifact "org.rksm" "suitable")
   (select-artifact group artifact-id)
      calc-github-address))


(comment

  (select-group "org.rksm")
  (select-artifact "org.rksm" "suitable")

  (select-artifact "foo" "bar")


  (-> (select-artifact "org.rksm" "suitable")
      calc-github-address)

  (count @clojars-meta)
  (last @clojars-meta)

  (filter-meta @clojars-meta)

  ;;
  )