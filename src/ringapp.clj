(ns ringapp
  (:require [ring.adapter.jetty :as jetty]
            [ring.middleware.file :as file]
            [ring.middleware.resource :as resource]
            [ring.util.response :as response]
            )
  (:gen-class))

(defn render-app []
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body
   (str "<html>hello from <code>render-app</code>")})

(defn handler [request]
  (if (= "/help" (:uri request))
      (response/redirect "/help.html")
      (render-app)))

(def app
  (-> handler
      ;;(resource/wrap-resource "public")
      (file/wrap-file "www")))

(defn -main [& args]
  (jetty/run-jetty app {:port 3000}))
