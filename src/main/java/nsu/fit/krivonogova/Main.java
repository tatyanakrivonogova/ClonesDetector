package main.java.nsu.fit.krivonogova;

public class Main {
    public static void main(String ... args) {
        if (args.length < 1) {
            System.out.println("Multicast address is not specified");
            return;
        }
        ClonesDetector.detectClones(args[0]);
    }
}