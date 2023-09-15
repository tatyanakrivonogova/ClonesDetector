package main.java.nsu.fit.krivonogova;

import java.net.*;
import java.util.Enumeration;

public class NetworkInterfaceGetter {
    public static NetworkInterface getNetworkInterface(String nameInterface) {
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();

            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
                if (!inetAddresses.hasMoreElements()) { continue; }
                if (networkInterface.getName().equals(nameInterface)) return networkInterface;
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return null;
    }
}