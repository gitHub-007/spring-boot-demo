# tomcat and svnservie
FROM tomcat
#更新apt-get源 使用163的源
RUN mv /etc/apt/sources.list /etc/apt/sources.list.bak && \
    echo "deb http://mirrors.163.com/debian/ stretch main non-free contrib" >/etc/apt/sources.list && \
    echo "deb http://mirrors.163.com/debian/ stretch-proposed-updates main non-free contrib" >>/etc/apt/sources.list && \
    echo "deb-src http://mirrors.163.com/debian/ stretch main non-free contrib" >>/etc/apt/sources.list && \
    echo "deb-src http://mirrors.163.com/debian/ stretch-proposed-updates main non-free contrib" >>/etc/apt/sources.list && \
    #echo "deb http://opensource.wandisco.com/debian/ stretch svn19" >> /etc/apt/sources.list &&\
    #wget -q http://opensource.wandisco.com/wandisco-debian-new.gpg  -O- |  apt-key add - &&\
    apt-get update &&\
    apt-get install -y subversion &&\
    mkdir /root/svn_dir
VOLUME /root/svn_dir
EXPOSE 3690
CMD ["svnserve","-d","--foreground","-r","/root/svn_dir"]
