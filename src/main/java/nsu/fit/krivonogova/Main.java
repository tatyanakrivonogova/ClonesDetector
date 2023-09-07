package main.java.nsu.fit.krivonogova;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class Main {
    public static void main(String ... args) {
        try {
            InetAddress multicastAddress = InetAddress.getByName(args[0]);
            ClonesDetector.detectClones(multicastAddress);
        } catch (UnknownHostException e) {
            System.out.println("Unknown host exception during using multicast address");
        }

    }
}
