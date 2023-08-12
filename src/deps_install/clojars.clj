(ns deps-install.clojars
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            #_[clojure.tools.logging :as log])
  (:import [java.net.http
            HttpClient
            HttpClient$Builder
            HttpRequest
            HttpResponse$BodyHandlers
            HttpClient$Redirect
            HttpRequest$BodyPublishers]
           [java.net URI]))

;; http://clojars.org/repo/feed.clj.gz

(defn get-request [uri]
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

(defn uncompress [byte-array]
  (with-open [in (java.util.zip.GZIPInputStream.
                  (io/input-stream byte-array))]
    (slurp in)))

(defn fetch [uri]
  #_(log/infof "fetch-acccount %s" uri)
  (->> uri
       get-request
       http-tx
       .body
       uncompress
       (format "[%s]")
       edn/read-string))


(comment
  (fetch "http://clojars.org/repo/feed.clj.gz")

  (last (fetch "http://clojars.org/repo/feed.clj.gz"))

  (count (fetch "http://clojars.org/repo/feed.clj.gz"))



  (slurp "http://clojars.org/repo/feed.clj.gz")
  ;;
  )