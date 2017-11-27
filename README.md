# Ethernet ARP in Java
Java发送ARP数据包，熟悉ARP协议。

对于IP数据包的传输过程，ARP协议用于完成IP地址与MAC地址间的转换，主要目的是通过封装与发送ARP帧，了解ARP协议的工作原理与ARP帧的结构。

关键字：ARP;协议;帧;网卡;Linux

## 环境
- Windows 8.1 Professional
- Ubuntu 15.04
- MyEclipse Enterprise Workbench 2014
- JDK 1.7.0_79
- WinPcap_4_1_3


## 命令格式
```
arpsend src_ip src_mac dst_ip dst_mac flag
```
其中arpsend作为程序名，各参数意义：
- src_ip		源IP地址
- src_mac		源MAC地址
- dst_ip 		目的IP地址
- dst_mac		目的MAC地址
- flag		    1表示ARP请求，2表示ARP应答

一个例子：
- ARP请求：
```
arpsend 192.168.111.1 00:50:56:c0:00:08 192.168.111.135 FF:FF:FF:FF:FF:FF 1
```
- ARP应答：
```
arpsend 192.168.111.1 00:1C:23:2E:A7:0A 192.168.111.135 00:0c:29:86:45:6e 2
```

## 运行与测试
### 主机Windows 8与虚拟机Linux间测试：

![windows-arp-cache-before-send](http://oss-aliyun.codz.me/images/project/arp/windows-arp-cache-before-send.png)

发送ARP包前主机的ARP Cache


![ubuntu-arp-cache-before-send](http://oss-aliyun.codz.me/images/project/arp/ubuntu-arp-cache-before-send.png)

发送ARP包前虚拟机（Ubuntu）的ARP Cache


![send-arp-fail](http://oss-aliyun.codz.me/images/project/arp/send-arp-fail.png)

Windows 8下控制台错误输入命令发送ARP包


![send-arp-ok](http://oss-aliyun.codz.me/images/project/arp/send-arp-ok.png)

Windows 8下控制台输入命令发送ARP请求包


![tcpdump](http://oss-aliyun.codz.me/images/project/arp/tcpdump.png)

Linux下监听到的ARP请求包（sudo tcpdump arp）


![ubuntu-arp-cache-after-send](http://oss-aliyun.codz.me/images/project/arp/ubuntu-arp-cache-after-send.png)

再次查看Linux下的ARP Cache


![windows-arp-cache-after-send](http://oss-aliyun.codz.me/images/project/arp/windows-arp-cache-after-send.png)

再次查看Windows下的ARP Cache



### 主机Windows 8与同一局域网的Windows 7测试：

![windows7-arp-cache-before-send](http://oss-aliyun.codz.me/images/project/arp/windows7-arp-cache-before-send.jpg)

先删除Windows7的该条ARP Cache


![send-arp-to-windows7](http://oss-aliyun.codz.me/images/project/arp/send-arp-to-windows7.png)

发送ARP包到Windows 7


![windows7-arp-cache-after-send](http://oss-aliyun.codz.me/images/project/arp/windows7-arp-cache-after-send.jpg)

再次查看Windows 7的ARP Cache

## Reference
- 谢希仁；计算机网络（第6版）；电子工业出版社；2013.6
- 李娜（译），Y.Daniel Liang；Java语言程序设计；机械工业出版社；2013.3
