(ns ringapp
  (:require [ring.adapter.jetty :as jetty]
            [ring.middleware.file :as file]
            [ring.middleware.resource :as resource]
            [ring.util.response :as response]
            [ring.middleware.content-type :as content-type]
            [ring.util.mime-type :as mime]
            )
  (:gen-class))

(defn render-app []
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body
   (str "<html>hello from <code>render-app</code>")})

(defn handler [request]
  (if (= "/banana" (:uri request))
      (response/redirect "/index.html")
      (render-app)))

(def app
  (-> handler
      (file/wrap-file "www")
      (content-type/wrap-content-type)
      ; The fact that host:port/ only gets
      ; an octet stream MIME type is infuriating!
  ))

;(resource/wrap-resource "public")
;:mime-types mime/default-mime-types


(defn -main [& args]
  (jetty/run-jetty app {:port 3000}))
