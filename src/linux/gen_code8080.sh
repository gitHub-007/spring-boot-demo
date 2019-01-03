docker build -f /home/docker_files/gen_code_Dockerfile -t gen_code  /home/jenkins_home/workspace/checkstyle_gen_code/gen_code/target/
docker stop gen_code8080
docker rm gen_code8080
docker run -itd --restart=always --privileged=true -p 8080:8080 \
	-v /home/gen_code8080/logs:/usr/local/tomcat/logs \
	-v /home/gen_code8080/ROOT:/usr/local/tomcat/webapps/ROOT/ \
    --network mynet --network-alias gen_code8080 --name gen_code8080 gen_code
