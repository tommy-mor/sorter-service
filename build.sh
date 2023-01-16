sudo apt install cmake gcc gcc-c++ g++
cd graphit
mkdir build
cd build
CXX="g++ -std=c++14" cmake ..
make
cd bin/
python graphitc.py -a ../../test/input/pagerank_with_filename_arg.gt -f ../../test/input_with_schedules/pagerank_pull_parallel.gt -o test.cpp
