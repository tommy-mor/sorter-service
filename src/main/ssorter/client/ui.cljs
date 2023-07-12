(ns ssorter.client.ui
  (:require 
   [ssorter.model.integrations.linear :as linear]
   [ssorter.model.tags :as m.tags]
   
   [com.fulcrologic.fulcro.algorithms.merge :as merge]
   [com.fulcrologic.fulcro.algorithms.tempid :as tempid]
   [com.fulcrologic.fulcro.algorithms.data-targeting :as targeting]
   [com.fulcrologic.fulcro.algorithms.normalized-state :as norm]
   [com.fulcrologic.fulcro.components :as comp :refer [defsc transact!]]
   [com.fulcrologic.fulcro.ui-state-machines :as uism :refer [defstatemachine]]
   [com.fulcrologic.fulcro.mutations :as m :refer [defmutation]]
   [com.fulcrologic.fulcro.dom.html-entities :as ent]
   [com.fulcrologic.fulcro.dom.events :as evt]
   [com.fulcrologic.fulcro.raw.components :as rc]
   [com.fulcrologic.fulcro.data-fetch :as df]    
   [com.fulcrologic.fulcro.dom :as dom :refer [button div form h1 h2 h3 input label li ol p ul]]
   [com.fulcrologic.fulcro.routing.dynamic-routing :as dr :refer [defrouter]] 

   
   [com.fulcrologic.semantic-ui.factories :as f]
   [clojure.contrib.humanize :refer [truncate]]
   
   [ssorter.model.integrations.linear-tag :as linear-tag]
   [ssorter.model.users :as users]))

(defn thing [all]
  (loop [out []
         current []]
    (if (= current all)
      out
      (let [letter (first (drop (count current) all))]
        (recur (conj out (conj current letter))
               (conj current letter))))))

(comment
  (thing ["a" "b" "c"]) ;= [["a"] ["a" "b"] ["a" "b" "c"]] 
  )


(defn field [{:keys [label valid? error-message] :as props}]
  (let [input-props (-> props (assoc :name label) (dissoc :label :valid? :error-message))]
    (div :.ui.field
         (dom/label {:htmlFor label} label)
         (dom/input input-props)
         (dom/div :.ui.error.message {:classes [(when valid? "hidden")]}
                  error-message))))


(defrouter RootRouter [this props]
  {:router-targets [m.tags/TagList linear/IssueList m.tags/Tag #_linear-tag/LinearTag]
   :always-render-body? true}
  (comp/fragment
   #_(f/ui-segment {} (f/ui-breadcrumb {}
                                       (let [segments (-> props :router-state :path-segment)]
                                         (when segments
                                           (->> (map (fn [title link]
                                                       (f/ui-breadcrumb-section {:link true
                                                                                 :key title
                                                                                 :onClick #(dr/change-route! this link)} title))
                                                     segments
                                                     (thing  segments))
                                                (interleave (map #(f/ui-breadcrumb-divider {:key %}) (range)))
                                                (drop 1))))))
   (when (not= :routed (:current-state props))
     (div "pending"))
   (when-let [route-factory (:route-factory props)]
     (route-factory (comp/computed (:route-props props) (comp/get-computed this))))))

(def ui-root-router (comp/factory RootRouter))

(declare Session)

(defsc Login [this {:users/keys [user_name]
                    :ui/keys      [error open?] :as props}]
  {:query         [:ui/open? :ui/error :users/user_name
                   {[:component/id :session] (comp/get-query Session)}
                   [::uism/asm-id ::users/session]]
   :initial-state {:users/user_name "" :ui/error ""}
   :ident         (fn [] [:component/id :login])}
  (let [current-state (uism/get-active-state this ::users/session)
        {current-user :users/user_name} (get props [:component/id :session])
        initial?      (= :initial current-state)
        loading?      (= :state/checking-session current-state)
        logged-in?    (= :state/logged-in current-state)
        password (or (comp/get-state this :password) "")] ; c.l. state for security
    (dom/div
     (when-not initial?
       (dom/div :.right.menu
                (if logged-in?
                  (dom/button :.item
                              {:onClick #(uism/trigger! this ::users/session :event/logout)}
                              (dom/span current-user) ent/nbsp "Log out")
                  (dom/div :.item {:style   {:position "relative"}
                                   :onClick #(uism/trigger! this ::users/session :event/toggle-modal)}
                           "Login"
                           (when open?
                             (div :.four.wide.ui.raised.teal.segment
                                  {:style {:float true}
                                   :onClick (fn [e]
                                              ;; Stop bubbling (would trigger the menu toggle)
                                              (evt/stop-propagation! e))}
                                  (dom/h3 :.ui.header "Login")
                                  (div :.ui.form {:classes [(when (seq error) "error")]}
                                       (field {:label    "username"
                                               :value    user_name
                                               :onChange #(m/set-string! this :users/user_name :event %)})
                                       (field {:label    "Password"
                                               :type     "password"
                                               :value    password
                                               :onChange #(comp/set-state! this {:password (evt/target-value %)})})
                                       (div :.ui.error.message error)
                                       (div :.ui.field
                                            (dom/button :.ui.button
                                                        {:onClick (fn [] (uism/trigger! this ::users/session :event/login {:username user_name
                                                                                                                           :password password}))
                                                         :classes [(when loading? "loading")]} "Login"))
                                       (div :.ui.message
                                            (dom/p "Don't have an account?")
                                            (dom/a {:onClick (fn []
                                                               (uism/trigger! this ::users/session :event/toggle-modal {})
                                                               (dr/change-route this ["signup"]))}
                                                   "Please sign up!"))))))))))))

(def ui-login (comp/factory Login))

(defsc Session
  "Session representation. Used primarily for server queries. On-screen representation happens in Login component."
  [this {:keys [:session/valid? :users/user_name] :as props}]
  {:query         [:session/valid? :users/user_name]
   :ident         (fn [] [:component/id :session])
   :pre-merge     (fn [{:keys [data-tree]}]
                    (merge {:session/valid? false :account/name ""}
                           data-tree))
   :initial-state {:session/valid? false :users/user_name ""}})

(defsc Root [this props]
  {:query [{:root/router (comp/get-query RootRouter)}
           {:root/current-session (comp/get-query Session)}
           {:root/login (comp/get-query Login)}]
   :initial-state {:root/router {}
                   :root/current-session {}
                   :root/login {}}}

  (let [render (fn [] (comp/with-parent-context this
                        (ui-root-router (:root/router props))))
        nav-to (fn [route] (dr/change-route! this route))]
    
    (div
     (let [menu (f/ui-menu {:vertical false
                            :attached "bottom"}
                           (f/ui-menu-item {:link true
                                            :onClick #(nav-to ["tags"])} "all items")
                           (f/ui-menu-item {:link true
                                            :onClick #(nav-to ["linear.issues"])} "linear/issues")
                           (f/ui-menu-item {:link true
                                            :onClick #(nav-to ["youtube.videos"])} "youtube")
                           (ui-login (:root/login props)))]
       
       (->> (f/ui-grid {}
                       (f/ui-grid-row {:centered true}
                                      (f/ui-grid-column {} menu))
                       (f/ui-grid-row {:centered true}
                                      (f/ui-grid-column {:width 12} (render)))))))))
