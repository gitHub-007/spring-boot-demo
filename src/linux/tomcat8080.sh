docker run -itd --restart=always --privileged=true -p 8080:8080 -v /home/tomcat8080/webapps:/usr/local/tomcat/webapps \
    -v /home/tomcat8080/conf:/usr/local/tomcat/conf \
	-v /home/tomcat8080/logs:/usr/local/tomcat/logs \
    --network mynet --network-alias tomcat8080 --name tomcat8080 tomcat
