package main;



import java.util.ArrayList;
import java.util.Map;


public class Graph {
    private static enum Colour{
        WHITE, GRAY, BLACK;
    }
    private ArrayList<ArrayList<Integer>> graph;
    private final Map<String, Integer> files;
    private Colour[] used;
    private ArrayList<Integer> number;
    public Graph(int n, Map map) {
        graph = new ArrayList<ArrayList<Integer>>();
        files = map;
        for (int i = 0; i < n; i++) {
            graph.add(new ArrayList<Integer>());
        }
    }
    public void add (int from, int to) {
        graph.get(from).add(to);
    }

    public ArrayList<Integer> topSort(String start) {
        used = new Colour[graph.size()];
        for (int i = 0; i < used.length; i++) {
            used[i] = Colour.WHITE;
        }
        number = new ArrayList<Integer>();
        dfs(files.get(start));
        return number;

    }

    private void dfs (int v) {
        used[v] = Colour.GRAY;
        for (int to : graph.get(v)) {
            if (used[to] == Colour.WHITE) {
                dfs(to);
            }
        }
        used[v] = Colour.BLACK;
        number.add(v);
    }

    @Override
    public String toString() {
        String result = "main.Graph size = "+graph.size()+"\n";
        for (int i = 0; i < graph.size(); i++) {
            String cur = i + ": size "+ graph.get(i).size() +":::";
            for (int j = 0; j < graph.get(i).size(); j++) {
                cur += graph.get(i).get(j) +" ";
            }
            result += cur + "\n";

        }
        return result;
    }

}
