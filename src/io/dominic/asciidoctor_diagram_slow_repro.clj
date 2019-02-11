(ns io.dominic.asciidoctor-diagram-slow-repro
  (:require
    [clj-async-profiler.core :as profiler])
  (:import
    [org.asciidoctor
     Asciidoctor
     Asciidoctor$Factory
     Options
     Attributes
     SafeMode]
    [java.io File]))

(set! *warn-on-reflection* true)

(defn asciidoctor ^Asciidoctor []
  (doto
    (Asciidoctor$Factory/create)
    (.requireLibrary (into-array String ["asciidoctor-diagram"]))))

(defmacro flamegraph
  [& body]
  `(let [^File f# (profiler/profile
                    {:return-file true}
                    ~@body)]
     f#))

(defn -main
  [& _]
  (let [asciidoctor (asciidoctor)
        f (File. "foo.adoc")]
    ;; Warm up the JIT
    (println "Warming up JIT with 30 calls")
    (dotimes [_ 30]
      (.readDocumentHeader asciidoctor f))
    (println "Timing 100 calls of .readDocumentHeader")
    (time
      (dotimes [_ 100]
        (.readDocumentHeader asciidoctor f)))
    (println "Producing flamegraph for 100 runs")
    (println
      (.getAbsolutePath
        (flamegraph
          (dotimes [_ 100]
            (.readDocumentHeader asciidoctor f)))))
    (System/exit 0)))

