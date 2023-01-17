graphit/build/bin:
	cd .. && pwd && git submodule init && git submodule update
	sudo apt install g++ cmake make
	cd graphit && \
	mkdir build && \
	cd build && \
	CXX="g++ -std=c++14" cmake .. && \
	make
	

../resources/pr:
	cd ./graphit/build/bin/ && \
	python3 graphitc.py -f ./../../../pagerank.gt -o pr.cpp && \
 	g++ -std=c++14 -fPIC -I ../../src/runtime_lib/ -O3 pr.cpp -o pr && \
	mv pr ./../../../pr
	mv pr ../resources/pr

clean:
	rm -f compile.o algo.gt compile.cpp

