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
    (fetch "http://clojars.org/repo/feed.clj.gz")))

(defn filter-scm [m]
  (let [{{:keys [connection developer-connection]} :scm} m]
    {:conn connection
     :dev-conn developer-connection}))

(defn filter-meta [x]
  (-> (map #(select-keys % [:group-id :artifact-id :scm]) x)))

(defn select-group [grp]
  (->> @clojars-meta
       (filter #(= (:group-id %) grp))))

(defn select-artifact [grp artifact-id]
  (->> grp
       select-group
       (filter #(= (:artifact-id %) artifact-id))
       first))

(defn calc-github-address [m]
  (let [{{:keys [url]} :scm} m]
    (if (.contains url "github")
      (str url ".git")
      (throw (.Exception
              (format "url for artifact is '%s' - not in github" url))))))

(defn repo-clone-address [group artifact-id]
  (-> (select-artifact "org.rksm" "suitable")
      calc-github-address))'



(comment

  (select-group "org.rksm")
  (select-artifact "org.rksm" "suitable")

  (-> (select-artifact "org.rksm" "suitable")
      calc-github-address)




  (->> @clojars-meta
       (map filter-meta))



  (count @clojars-meta)
  (last @clojars-meta)

  (filter-meta @clojars-meta)

  ;;
  )