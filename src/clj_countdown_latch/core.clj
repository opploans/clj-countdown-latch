(ns clj-countdown-latch.core
  (:import (java.util.concurrent Executors TimeUnit)))

(def ^:dynamic ^:private executor nil)

(defn async-as-necessary*
  "Execute the given function asynchronously, but only if we are in the context
   of a `with-countdown-latch*`. Exceptions are not handled. If you want to see
   stack traces, the code you send to this function must catch and handle them."
  [f]
  (if executor (.submit executor f) (f)))

(defmacro async-as-necessary
  "Dispatch the given body as a function sent to `async-as-necessary*`"
  [& body]
  `(async-as-necessary* (fn [] ~@body)))

(defn with-countdown-latch*
  "Execute the given body with all log statements being executed asynchonously.
   Before the body is able to return, we will wait for all async events
   to complete. Using a Java-based Cached Thread Pool so we don't allow any
   events to block others, or waste memory creating new threads when we don't
   need to. See https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/Executors.html#newCachedThreadPool()"
  [timeout-ms f]
  (binding [executor (Executors/newCachedThreadPool)]
    (try
      (f)
      (finally
        (when-not
         (do (.shutdown executor)
             (.awaitTermination executor timeout-ms TimeUnit/MILLISECONDS))
          (println "Timed out waiting for log statements to finish"))))))

(defmacro with-countdown-latch
  "Dispatch the given body as a function sent to `with-countdown-latch*`
   with the given `timeout-ms`"
  [timeout-ms & body]
  `(with-countdown-latch* ~timeout-ms (fn [] ~@body)))

(defn wrap-timbre-appender
  "Reconfigures a timbre appender to use the countdown latch for its async
  processing instead of the built-in approach"
  [appender-config]
  (assoc appender-config
         :async? false
         :fn (fn [data] (async-as-necessary ((:fn appender-config) data)))))

(defn wrap-ring [timeout-ms handler]
  (fn [request] (with-countdown-latch timeout-ms (handler request))))

(comment
 (defn- do-lots-of-things []
   (dotimes [x 50]
     (async-as-necessary
      (Thread/sleep 50)
      (printf "Thing %d happened%n" x)
      (flush))))

 (time (do-lots-of-things))
 (time (with-countdown-latch 1000 (do-lots-of-things)))

 *e
 )