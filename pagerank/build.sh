sudo apt install cmake gcc gcc-c++ g++
cd graphit
mkdir build
cd build
CXX="g++ -std=c++14" cmake ..
make
cd bin/
