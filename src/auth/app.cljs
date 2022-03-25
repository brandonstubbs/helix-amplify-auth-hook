(ns auth.app
  (:require [helix.core     :refer [defnc $]]
            [helix.hooks    :refer [use-state]]
            [helix.dom      :as d]
            ["react-dom"    :as rdom]
            [auth.auth-hook :as auth]))



(defnc container [{:keys [children]}]
  (d/div {:style {:display         "flex"
                  :align-items     "center"
                  :justify-content "center"
                  :width           "100vw"
                  :height          "100vh"}}
    children))



(defnc login []
  (let [{:keys [login error]}   (auth/use-auth)
        [username set-username] (use-state "")
        [password set-password] (use-state "")
        on-submit
        (fn [e] (.preventDefault e)
          (login username password))]
    (d/form {:on-submit on-submit}
      (d/div {:style {:display        "flex"
                      :flex-direction "column"
                      :gap            "4px"
                      :width          "300px"}}
        (d/label {:for "uname"} (d/b "Username"))
        (d/input {:name        "uname"
                  :type        "text"
                  :placeholder "Enter Username"
                  :on-change   #(set-username (.. % -target -value))
                  :value       username
                  :required    true})
        (d/label {:for "psw"} (d/b "Password"))
        (d/input {:name        "psw"
                  :type        "password"
                  :placeholder "Enter Password"
                  :on-change   #(set-password (.. % -target -value))
                  :value       password
                  :required    true})
        (when error
          (d/p {:style {:text-align "center"}} "Invalid Username or Password."))
        (d/button {:type "submit"} "Login")))))



(defnc home []
  (let [{:keys [logout] {:keys [uid name] :as user} :user} (auth/use-auth)]
    (d/div {:style {:display        "flex"
                    :flex-direction "column"
                    :align-items    "center"}}
      (d/img {:style {:width  "72px"
                      :height "72px"}
              :src (str "https://avatars.dicebear.com/api/pixel-art/" uid ".svg")
              :alt "Users avatar"})
      (d/div (d/b "Welcome ") name)
      (d/button {:on-click logout} "Logout"))))



(defnc index []
  (let [{:keys [user]} (auth/use-auth)]
    ($ container
      (if user
        ($ home)
        ($ login)))))



;; start is called by init and after code reloading finishes
(defn ^:dev/after-load start []
  (rdom/render
    ($ auth/auth-provider ($ index))
    (.getElementById js/document "app")))



(defn ^:export init []
  ;; init is called ONCE when the page loads
  ;; this is called in the index.html and must be exported
  ;; so it is available even in :advanced release builds
  (start))
