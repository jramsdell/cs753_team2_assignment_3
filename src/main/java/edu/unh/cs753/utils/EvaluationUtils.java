package edu.unh.cs753.utils;


import utils.KotlinEvaluationUtils;

import java.io.*;
import java.util.*;

public class EvaluationUtils {

    // Make a static function that takes this path as a function.
    // Open the file, split based on space, make a map of sets,
    // key = query, set = the results that are relevant to the query.
    public static HashMap<String, HashMap<String, Integer>> getRelevantQueries(String path) throws IOException {
        // Initialie hashmap
        HashMap<String, HashMap<String, Integer>> m = new HashMap<>();

        FileReader fstream = new FileReader(path);
        BufferedReader in = new BufferedReader(fstream);

        String line = in.readLine();
        while (line != null) {
            String[] elements = line.split(" ");
            String query = elements[0];
            String id = elements[2];
            Integer relevance = Integer.parseInt(elements[3]);

            if (!m.containsKey(query)) {
                m.put(query, new HashMap<>());
            }
            m.get(query).put(id, relevance);

            line = in.readLine();
        }
        in.close();
        return m;
    }

    public static HashMap<String, HashMap<String, Integer>> parseRunFile(File f) throws IOException {
        HashMap<String, HashMap<String, Integer>> m = new HashMap<>();

        FileReader fstream = new FileReader(f);
        BufferedReader in = new BufferedReader(fstream);

        String line = in.readLine();
        while (line != null) {
            String[] elements = line.split(" ");
            String query = elements[0];
            String id = elements[2];
            Integer rank = Integer.parseInt(elements[3]); // Rank should be int..

            if (!m.containsKey(query)) {
                m.put(query, new HashMap<>());
            }
            m.get(query).put(id, rank);

            line = in.readLine();
        }
        in.close();
        return m;
    }

    // This doesn't return anything...
//    public static void parseAll(String dirPath) throws IOException {
//        File dir = new File(dirPath);
//        File[] dirList = dir.listFiles();
//        for (File f : dirList) {
//            ParseRunFile(f);
//        }
//    }

    // Make a map of lists to get the list of PIDs
    public static HashMap<String, ArrayList<String>> ParseResults(String path) throws IOException {
        // Initialie hashmap
        HashMap<String, ArrayList<String>> m = new HashMap<>();

        FileReader fstream = new FileReader(path);
        BufferedReader in = new BufferedReader(fstream);
        String curLine;

        // If this query is in our first hashmap, then it is relevant, so add the id to the list
        String line = in.readLine();
        while (line != null) {
            String[] elements = line.split(" ");
            String query = elements[0];
            String ID = elements[2];

            if (!m.containsKey(query)) {
                ArrayList<String> a = new ArrayList<>();
                m.put(query, a);
            }
            ArrayList<String> al = m.get(query);
            al.add(ID);
            line = in.readLine();
        }
        in.close();
        return m;
    }

    public static double getPrecisionAtR(HashMap<String, HashMap<String, Integer>> qrels, HashMap<String, ArrayList<String>> res1) {
        // Iterate through the hash map of results (res1) and check if each id is in the relevant data hash map

        // Get the number of relevant documents
        double numberOfQueries = qrels.size();

        // Parse the first "size" elements of our results hash map
        Iterator it = res1.entrySet().iterator();
        double totalPrecision = 0.0;

        for (String query : qrels.keySet()) {
            double hits = 0;
            int counter = 0;
            HashMap<String, Integer> relevantDocuments = qrels.get(query);
            int r = relevantDocuments.size();
            List<String> retrievedDocuments = res1.get(query);
            if (retrievedDocuments == null) {
                continue;
            }

            while (counter < r && counter < retrievedDocuments.size()) {
                if (relevantDocuments.containsKey(retrievedDocuments.get(counter))) {
                    hits += 1.0;
                }
                counter++;
            }

            totalPrecision += hits / r;
        }

        return totalPrecision / numberOfQueries;
    }


    public static double getMap(HashMap<String, HashMap<String, Integer>> qrels, HashMap<String, ArrayList<String>> res1) {
        // Iterate through the hash map of results (res1) and check if each id is in the relevant data hash map

        // Get the number of relevant documents
        double numberOfQueries = qrels.size();


        double totalMap = 0.0;

        for (String query : qrels.keySet()) {

            double num=0;
            double denom=0;
            double mapresult=0;
            int counter = 0;
            HashMap<String, Integer> relevantDocuments = qrels.get(query);

            int r2=relevantDocuments.size();

            // r2 is our number of relavant documents
            List<String> retrievedDocuments = res1.get(query);
            if (retrievedDocuments == null) {
                continue;
            }
            int r1 = retrievedDocuments.size();
            // r1 is the number of retrieved documents.

            while (counter < r1) {
                if (relevantDocuments.containsKey(retrievedDocuments.get(counter))) {

                    num +=1.0;
                    denom +=1.0;

                    mapresult += num/denom;
                } else {
                    denom +=1.0;
                }
                counter++;
            }

            mapresult = mapresult/r2;
            // r2 is the number of relevant documents and we divide by this not by total number of documents.
            totalMap += mapresult;
        }

        return totalMap / numberOfQueries;
    }

    public static double calculateSpearman(List<Integer> rankings1 , List<Integer> rankings2) {

        double diff,ele1,ele2,sum=0.0;
        double summation;

        for(int i=0; i<rankings1.size();i++){

            ele1=rankings1.get(i);
            ele2=rankings2.get(i);
            diff=ele1-ele2;

            sum+=Math.pow(diff, 2.0);
        }

        return  summation= 6*sum/(rankings1.size() * (Math.pow(rankings1.size(),2))-1);
    }

    public static void main(String [] args) throws IOException {

            /*String qrels = "/home/hcgs/data_science/data/test200/test200-train/train.pages.cbor-article.qrels";
            HashMap<String, HashMap<String, Integer>> relevant = EvaluationUtils.GetRelevantQueries(qrels);

            String res1 = "/home/hcgs/Desktop/projects/cs753_team2_assignment2/results1.txt";
    //      String res2 = "/Users/abhinav/desktop/results2.txt";

            HashMap<String, ArrayList<String>> metrics1 = EvaluationUtils.ParseResults(res1);
    //      HashMap<String, ArrayList<String>> metrics2 = EvaluationUtils.ParseResults(res2);

            double nmap1 = KotlinEvaluationUtils.INSTANCE.getNDCG(relevant, metrics1);
    //      double nmap2 = getMap(relevant, metrics2);

            System.out.println("Map is " + nmap1);
    //      System.out.println("Map is " + nmap2);*/

    }
}
