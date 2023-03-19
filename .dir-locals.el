;; ((nil ))

(
 (nil . 
	  ((cider-default-cljs-repl . shadow)
	   (cider-shadow-default-options . ":main")
	   (cider-shadow-watched-builds . (":main"))
	   (cider-clojure-cli-global-options . "-A:dev")
	   (eval . (progn
               (local-set-key (kbd "C-c C-r")
                 (lambda () 
                   (interactive)
                   (cider-interactive-eval
                     "(require 'development) (in-ns 'development) (restart)"
                     nil 
                     nil 
                     (cider--nrepl-pr-request-map)))))))))
