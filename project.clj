(defproject leonhard "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/tools.trace "0.7.6"]
                 [org.clojure/math.combinatorics "0.0.7"]
                 [quil "2.0.0"]
                 [overtone "0.8.1" :exclusions [org.clojure/clojure]]]
  :main leonhard.core)
