graphit/build/bin:
	cd .. && pwd && git submodule init && git submodule update
	sudo apt install g++ cmake make
	cd graphit && \
	mkdir build && \
	cd build && \
	CXX="g++ -std=c++14" cmake .. && \
	make
	

./pr.so:
	cd ./graphit/build/bin/ && \
	python3 graphitc.py -f ./../../../pagerank.gt -o pr.cpp && \
 	g++ -std=c++14 -fPIC -I ../../src/runtime_lib/ -O3 -c pr.cpp -o pr.o && \
	gcc -shared -o pr.so pr.o && \
	mv pr.so ./../../../pr.so

clean:
	rm -f compile.o algo.gt compile.cpp

