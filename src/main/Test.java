package main;

import com.sun.jna.platform.win32.Winsvc;

import java.io.*;
import java.util.Scanner;

public class Test {
    public static void main(String[] args) throws IOException {
        Scanner scan = new Scanner(System.in);

        int l = 10;

        String x = scan.nextLine() + " l";

        System.out.println(x);
       // read();

    }

    public static void read() throws IOException {
        String filename = "binary1.zip";
        RandomAccessFile rac = new RandomAccessFile(filename, "r");
        rac.seek(12);
        System.out.println(rac.readInt());
    }
}
