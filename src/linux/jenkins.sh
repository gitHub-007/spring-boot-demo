docker stop jenkins
docker rm jenkins
docker run -u root  -itd --restart=always --privileged=true  -p 8081:8080 -p 50000:50000 \
       -v /home/jenkins_home:/var/jenkins_home \
	   -v /var/run/docker.sock:/var/run/docker.sock \
	   -v $(which docker):/usr/bin/docker \
	   -v /usr/lib64/libltdl.so.7:/usr/lib/x86_64-linux-gnu/libltdl.so.7 \
	   -v /home/docker_files/:/home/docker_files/ \
       --name=jenkins jenkins/jenkins:lts
