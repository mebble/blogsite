(defproject blogsite-clj "0.1.0-SNAPSHOT"
  :description "A blogging site demo in clojure"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [ring/ring-core "1.8.2"]
                 [ring/ring-jetty-adapter "1.8.2"]
                 [ring-refresh "0.1.3"]
                 [compojure "1.7.0"]
                 [selmer "1.12.59"]
                 [com.github.seancorfield/next.jdbc "1.3.894"]
                 [org.xerial/sqlite-jdbc "3.43.2.2"]
                 [com.github.rawleyfowler/sluj "1.0.2"]
                 [funcool/cats "2.4.2"]]
  :main ^:skip-aot blogsite-clj.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}})
