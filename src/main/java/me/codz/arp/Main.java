package me.codz.arp;

import jpcap.JpcapCaptor;
import jpcap.JpcapSender;
import jpcap.NetworkInterface;
import jpcap.NetworkInterfaceAddress;
import jpcap.packet.ARPPacket;
import jpcap.packet.EthernetPacket;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.Arrays;

/**
 * <p>Created with IDEA
 * <p>Author: dukang.liu
 * <p>Date: 2015/06/07
 * <p>Time: 10:55
 * <p>Version: 1.0
 */
public class Main {

    public static void main(String[] args) throws IllegalArgumentException, IOException {

        StringBuffer command;
        // arpsend 192.168.111.1 00:50:56:c0:00:08 192.168.111.135 FF:FF:FF:FF:FF:FF 1
        // arpsend 172.18.94.152 20:89:84:21:24:01 192.168.112.80 FF:FF:FF:FF:FF:FF 1
        // arpsend 172.18.94.152 20:89:84:21:24:01 172.18.94.199 FF:FF:FF:FF:FF:FF 1
        // arpsend 172.18.94.152 20:89:84:21:24:01 172.18.94.199 DC:0E:A1:D0:50:3A 2
        // arpsend 192.168.111.1 00:1C:23:2E:A7:0A 192.168.111.135 00:0c:29:86:45:6e 2
        // arpsend 192.168.111.3 00:50:56:c0:00:09 192.168.111.135 FF:FF:FF:FF:FF:FF 1
        while (true) {
            command = sqlReaderFromConsole();
            if (command.toString().split("\\s+").length == 6) {
                break;
            }
            System.out.println("Input error, please try again");
        }
        byte[] temp = getMACAddressByARP(command);
        if (temp != null && temp.length != 0) {
            System.out.println(">Send OK\n");
        } else {
            System.out.println(">Send FAIL\n");
        }
    }

    private static String getMACAdressByIp(StringBuffer command)
            throws IOException, IllegalArgumentException {

        byte[] mac = getMACAddressByARP(command);

        StringBuilder formattedMac = new StringBuilder();
        boolean first = true;
        for (byte b : mac) {
            if (first) {
                first = false;
            } else {
                formattedMac.append(":");
            }
            String hexStr = Integer.toHexString(b & 0xff);
            if (hexStr.length() == 1) {
                formattedMac.append("0");
            }
            formattedMac.append(hexStr);
        }

        return formattedMac.toString();
    }

    private static byte[] getMACAddressByARP(StringBuffer command)
            throws IOException, IllegalArgumentException {// Inet4Address ip

        String[] cmd = command.toString().split("\\s+");
        Inet4Address ip = (Inet4Address) (InetAddress.getByName(cmd[3]));

        NetworkInterface networkDevice = getNetworkDeviceByTargetIP(ip);

        JpcapCaptor captor = JpcapCaptor.openDevice(networkDevice, 2000, false,
                3000);
        captor.setFilter("arp", true);
        JpcapSender sender = captor.getJpcapSenderInstance();

        InetAddress srcip = null;
        for (NetworkInterfaceAddress addr : networkDevice.addresses)
            if (addr.address instanceof Inet4Address) {
                srcip = addr.address;
                break;
            }

        ARPPacket arp = new ARPPacket();
        arp.hardtype = ARPPacket.HARDTYPE_ETHER;// 选择以太网类型(Ethernet)
        arp.prototype = ARPPacket.PROTOTYPE_IP; // 选择IP网络协议类型
        arp.operation = Short.valueOf(cmd[5]);// ARPPacket.ARP_REQUEST,1表示ARP请求,2表示ARP应答
        arp.hlen = 6;// MAC地址长度固定6个字节
        arp.plen = 4;// IP地址长度固定4个字节
        arp.sender_hardaddr = getMacBytes(cmd[2]);// networkDevice.mac_address,src的MAC地址
        arp.sender_protoaddr = srcip.getAddress();// getMacBytesByIP(cmd[1]),网关IP
        arp.target_hardaddr = getMacBytes(cmd[4]);// 广播地址
        arp.target_protoaddr = ip.getAddress();// 目标的IP

        EthernetPacket ether = new EthernetPacket();
        ether.frametype = EthernetPacket.ETHERTYPE_ARP;
        ether.src_mac = getMacBytes(cmd[2]);// networkDevice.mac_address;
        ether.dst_mac = getMacBytes(cmd[4]);// broadcast,广播地址
        arp.datalink = ether;

        // 发送ARP应答包
        if (cmd[5].equals("1")) {
            sender.sendPacket(arp);
        } else {
            System.out.println("just sending arp..");
            sender.sendPacket(arp);
            captor.close();
            return "OK".getBytes();
        }

        while (true) {
            System.out.println("sending arp ...");
            ARPPacket p = (ARPPacket) captor.getPacket();
            if (p == null) {
                System.out.println(ip + " is not a local address");
                throw new IllegalArgumentException(ip
                        + " is not a local address");
            }
            if (Arrays.equals(p.target_protoaddr, srcip.getAddress())) {// getMacBytesByIP(cmd[1])
                captor.close();
                return p.sender_hardaddr;
            }
        }
    }

    private static NetworkInterface getNetworkDeviceByTargetIP(Inet4Address ip)
            throws IllegalArgumentException {

        NetworkInterface networkDevice = null;
        NetworkInterface[] devices = JpcapCaptor.getDeviceList();

        loop:
        for (NetworkInterface device : devices) {
            for (NetworkInterfaceAddress addr : device.addresses) {
                if (!(addr.address instanceof Inet4Address)) {
                    continue;
                }
                byte[] bip = ip.getAddress();
                byte[] subnet = addr.subnet.getAddress();
                byte[] bif = addr.address.getAddress();
                for (int i = 0; i < 4; i++) {
                    bip[i] = (byte) (bip[i] & subnet[i]);
                    bif[i] = (byte) (bif[i] & subnet[i]);
                }
                if (Arrays.equals(bip, bif)) {
                    networkDevice = device;
                    break loop;
                }
            }
        }

        if (networkDevice == null) {
            System.out.println(ip + " is not a local address");
            throw new IllegalArgumentException(ip + " is not a local address");
        }

        return networkDevice;
    }

    private static StringBuffer sqlReaderFromConsole() throws IOException {
        System.out.print(">");
        InputStreamReader in = new InputStreamReader(System.in);
        StringBuffer sql = new StringBuffer("");
        int c;
        c = in.read();
        while ((c != 13) && (c != -1)) // 一个回车\t（13），一个是换行\n（10）
        {
            sql.append((char) c);
            c = in.read();
        }
        return sql;
    }

    private static byte[] getMacBytes(String mac) {
        byte[] macBytes = new byte[6];
        String[] strArr = mac.split(":");

        for (int i = 0; i < strArr.length; i++) {
            int value = Integer.parseInt(strArr[i], 16);
            macBytes[i] = (byte) value;
        }
        return macBytes;
    }

    private static byte[] getMacBytesByIP(String mac) {
        byte[] macBytes = new byte[4];
        String[] strArr = mac.split("\\.");
        for (int i = 0; i < strArr.length; i++) {
            int value = Integer.parseInt(strArr[i]);
            macBytes[i] = (byte) value;
        }
        return macBytes;
    }


}
