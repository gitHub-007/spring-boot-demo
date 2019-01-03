#!/bin/bash
docker run  -p 8082:80  -v /home/gitlab_data/config:/etc/gitlab -v /home/gitlab_data/logs:/var/log/gitlab -v /home/gitlab_data/data:/var/opt/gitlab  gitlab/gitlab-ce
