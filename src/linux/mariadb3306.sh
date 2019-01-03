docker stop mariadb3306
docker rm mariadb3306
docker run -itd -e MYSQL_ROOT_PASSWORD=123456 \
     --restart=always --privileged=true -p 3306:3306 --name mariadb3306 \
     -v /home/mariadb_home/my.cnf:/etc/mysql/my.cnf \
     -v /home/mariadb_home/mariadb3306:/var/lib/mysql \
     --network mynet --network-alias mariadb3306 mariadb
	 
