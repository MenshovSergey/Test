package main;

import a.A;
import a.b.*;
import a.b.C.C;

import main.Graph;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;

public class Main {
    public static void main(String[] args) throws FileNotFoundException{
        String targetClass = args[0];
        String targetDir = args[1];
        Solution solve = new Solution();
        Graph graph = solve.createGraph(targetDir);
        PrintWriter out = new PrintWriter("result.out");
        out.println(graph.toString());
        out.close();
        ArrayList<Integer> answer = graph.topSort(targetClass);
        for (int i : answer) {
            System.out.print(i + " ");
        }

    }
}
