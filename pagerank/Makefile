graphit/build:
	git submodule init
	sudo apt install g++ cmake make
	cd graphit && \
	mkdir build && \
	cd build && \
	CXX="g++ -std=c++14" cmake .. && \
	make
	

pr.a: graphit/build
	cd ./graphit/build/bin/ && \
	python3 graphitc.py -f ./../../../pagerank.gt -o pr.cpp && \
 	g++ -std=c++14 -I ../../src/runtime_lib/ -O3 -c pr.cpp && \
	ar rvs pr.a pr.o && \
	mv pr.a ./../../../pr.a && \
	cd -

clean:
	rm -f compile.o algo.gt compile.cpp

