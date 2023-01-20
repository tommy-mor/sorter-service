;; ((nil (cider-clojure-cli-global-options . "-A:dev")))

(
 (nil . 
   ((eval . (progn
               (local-set-key (kbd "C-c C-r")
                 (lambda () 
                   (interactive)
                   (cider-interactive-eval
                     "(require 'development) (in-ns 'development) (restart)"
                     nil 
                     nil 
                     (cider--nrepl-pr-request-map)))))))))
