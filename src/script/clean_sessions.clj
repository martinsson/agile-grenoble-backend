(ns script.clean-sessions
  (:use midje.sweet))

(defn is-email? [s] 
  (re-matches #".*@.*" s))

(facts 
  (is-email? "") => falsey
  (is-email? "@") => truthy
  (is-email? "a@b") => truthy
  (is-email? "a.b@c.d") => truthy)

(defn filter-names-that-are-emails [speaker]
  (if (nil? speaker) 
    nil
    (if (is-email? (:name speaker))
     (dissoc speaker :name )
     speaker)))

(facts 
  (filter-names-that-are-emails {:name "anais.victor@officience.com",
                                 :id 154}) => {:id 154}
  (filter-names-that-are-emails {:name "anais victor",
                                 :id 154}) => {:name "anais victor",
                                   :id 154}
  (filter-names-that-are-emails nil) => nil)