cd database && gnome-terminal -- ./run-postgres.sh
cd -
gnome-terminal -- bb js

emacs --eval "(load-theme 'tsdh-light)" --file ./src/main/ssorter/client/ui.cljs  --eval "(call-interactively 'cider-connect-cljs)" &

emacs --file ./src/main/ssorter/rank.clj --eval "(call-interactively 'cider-jack-in)" &



echo "done"
