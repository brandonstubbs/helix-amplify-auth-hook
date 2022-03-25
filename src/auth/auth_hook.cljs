(ns auth.auth-hook
  (:require [helix.core    :refer [defnc provider]]
            [helix.hooks   :refer [use-state use-context use-effect]]
            [promesa.core  :as p]
            ["react"       :refer [createContext]]
            ["aws-amplify" :default Amplify :refer [Auth CognitoUser]]))



(defonce auth-context (createContext))



(defonce aws-config
  (.configure Amplify
    (clj->js {:Auth {:region              "<region>"
                     :userPoolId          "<user-pool-id>"
                     :userPoolWebClientId "<user-pool-web-client-id>"}})))



(defn format-user [^CognitoUser user]
  (when-let [username (. user -username)]
    {:uid   username
     :email (.. user -attributes -email)
     :name  (.. user -attributes -name)}))



(defn use-provide-auth []
  (let [[error set-error]     (use-state nil)
        [user set-user]       (use-state nil)
        [loading set-loading] (use-state true)
        reset-user            #(set-user nil)]
    (use-effect []
      (p/err reset-user
        (p/then (.currentAuthenticatedUser Auth)
          #(-> % (format-user) (set-user)))))
    {:user    user
     :loading loading
     :error   error
     :login   (fn [username password]
                (set-loading true)
                (p/err set-error
                  (p/let [user (.signIn Auth username password)
                          _    (p/finally user
                                 (fn [] (set-error nil) (set-loading false)))]
                    (-> user (format-user) (set-user)))))
     :logout  (fn []
                (set-error nil)
                (p/err set-error (p/then (.signOut Auth) reset-user)))}))



(defnc auth-provider [{:keys [children]}]
  (let [auth (use-provide-auth)]
    (provider {:context auth-context :value auth} children)))



(defn use-auth [] (use-context auth-context))
