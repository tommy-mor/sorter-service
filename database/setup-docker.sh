# https://www.digitalocean.com/community/questions/how-to-fix-docker-got-permission-denied-while-trying-to-connect-to-the-docker-daemon-socket
sudo apt install -f docker.io
echo "1"
sudo groupadd -f docker
echo "2"
sudo usermod -aG docker ${USER}
echo "3"
newgrp docker 
docker run hello-world

