echo "已经开放的端口"
firewall-cmd --zone=public --list-ports
echo "enter the port: "
read port
firewall-cmd --add-port=$port/tcp --permanent
systemctl reload firewalld
