package main;

import main.Graph;

import java.io.*;
import java.util.*;

public class Solution {

    private final int lengthImport = 7;
    public Graph createGraph (String targetDir) {
        Map<String, Integer> current = new HashMap<String, Integer>();
        Map<String, List<String>> listDependencies = getAllDependencies(new File(targetDir), "");
        int number = 0;
        for (String i : listDependencies.keySet()) {
            current.put(i, number);
            System.out.println(i +" : "+ number);
            number++;
        }

        Graph result = new Graph(current.size(), current);
        Set<String> depends = listDependencies.keySet();
        for (String i : listDependencies.keySet() ){
            List<String> depend = listDependencies.get(i);
            int from = current.get(i);
            for (String j : depend) {
                List<String> names = getNames(j, depends);
                for (String to: names) {
                    if (current.containsKey(to)) {
                        result.add(from, current.get(to));
                    }
                }
            }
        }
        return result;
    }

    private Map<String, List<String>> getAllDependencies(File dir, String prefix) {
        Map<String, List<String>> result = new HashMap<String, List<String>>();
        File[] all = dir.listFiles();
        for (File f : all) {
            if (f.isFile()) {
                String name = f.getName();
                name = name.substring(0, name.lastIndexOf('.'));
                result.put(prefix + name, getDependencies(f));

            } else {
                result.putAll(getAllDependencies(f, prefix + f.getName() + "."));
            }
        }

        return result;
    }
    private List<String> getDependencies(File f) {
        List<String> result = new ArrayList<String>();
        try {
            BufferedReader bf = new BufferedReader(new FileReader(f));

            String s = "";
            while ((s = bf.readLine()) != null) {
                String depend = getName(s);
                if (!depend.equals("")) {
                    result.add(depend.replaceAll(" ",""));
                }
           }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }
    private List<String> getNames (String name, Set<String> files) {
        List<String> result = new ArrayList<String>();
        if (name.charAt(name.length() - 1) != '*') {
            result.add(name.replaceAll(" ",""));
        } else {
            String cur = name.substring(0, name.length() - 1);
            for (String i : files) {
                if (i.matches(cur+"[A-Z,a-z,0-9]+")) {
                    result.add(i);
                }
            }
        }
        return result;
    }
    private String getName (String s) {
        String res = s.trim();
        if (res.length() > lengthImport)
            if (res.substring(0,7).equals("import ")) {
                res = res.substring(6, res.length() - 1);
                return res.trim();
            }
        return "";
    }
}

