(ns deps-install.git
  (:require [clojure.java.io :as io]
            [clojure.java.shell :as shell]
            [deps-install.clojars :refer [select-group
                                          repo-clone-address]]
            [clojure.tools.build.api :refer [git-process
                                             install]]))


(defn mk-staging-dir []
  (let [path (io/file
              (str (System/getProperty "user.home") "/.deps-staging/a"))]
    (when-not (.exists path)
      (io/make-parents path))
    (.getParent path)))


(defn sh-exec [arg-vec]
  (let [{:keys [exit err]}
        (shell/with-sh-dir (mk-staging-dir)
          (apply shell/sh arg-vec))]
    (when-not (zero? exit)
      (throw (Exception. err)))))

(defn clone-repo [m]
  (let [{:keys [group-id artifact-id]} m]
    (sh-exec
      ["git" "clone" (repo-clone-address group-id artifact-id)])))


(comment
  (user/trace! #'git-process)


  (sh-exec 
   ["git" "clone" "https://github.com/clojure-emacs/clj-suitable.git"])


  (System/getProperty "user.home")

  (clone-repo {:group-id "org.rksm"
               :artifact-id "suitable"
               :capture  :err})


  (mk-staging-dir)

  (git-process {:dir "/Users/tstout/.deps-staging"
                :capture :err
                :git-args "clone https://github.com/clojure-emacs/clj-suitable.git"})

  (select-group "aima-clj")

  ;;
  )


