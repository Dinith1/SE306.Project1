package se306;


import se306.Input.InputReader;

import java.io.*;
import java.net.URL;

//Main class to test InputReader functionality
public class Main {


    public static void main(String[] args) throws IOException {
        InputStream in = Main.class.getResourceAsStream("/Nodes_8_Random.dot");
        InputStreamReader isr = new InputStreamReader(in);
        InputReader inputReader = new InputReader();
        inputReader.readInput(isr);
    }

}

