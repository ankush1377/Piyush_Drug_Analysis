package com.aexp.esp.test;

import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import static java.util.Comparator.comparingInt;
import static java.util.stream.Collectors.toMap;

public class DrugAnalysis {

    public static Map<String, List<String>> readProteinData() {
        BufferedReader reader;
        Map<String, List<String>> proteinMap = new HashMap<>();
        List<String> proteinOrder = new ArrayList<>(); // to keep protein order mapping
        try {
            reader = new BufferedReader(new FileReader(
                    "/Users/ashar743/Documents/Test/src/main/resources/data/bio2mat.csv"));
            String line = reader.readLine();

            // reading all proteins
            if (line!=null) {
                String[] proteins = line.split(",");
                for(int i=2;i<proteins.length;i++) {
                    if (StringUtils.isNotBlank(proteins[i])) {
                        proteinOrder.add(proteins[i]);
                        proteinMap.put(proteins[i], new ArrayList<>());
                    }
                }
            }
            line = reader.readLine();
            while (line != null) {
                String[] values = line.split(",");
                String drugName = values[1];
                //TODO clean & validate drug name

                for (int i=2;i<values.length;i++) {
                    if (values[i].contains("1")) {
                        proteinMap.get(proteinOrder.get(i-2)).add(drugName);
                    }
                }
                line = reader.readLine();
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return proteinMap;
    }

    public static Map<String, List<String>> readSymptomsData() {
        BufferedReader reader;
        Map<String, List<String>> symptomsMap = new HashMap<>();
        List<String> symptomsOrder = new ArrayList<>(); // to keep symptoms order mapping
        try {
            reader = new BufferedReader(new FileReader(
                    "/Users/ashar743/Documents/Test/src/main/resources/data/pharmat.csv"));
            String line = reader.readLine();

            // reading all symptoms
            if (line!=null) {
                String[] symptoms = line.split(",");
                for(int i=2;i<symptoms.length;i++) {
                    if (StringUtils.isNotBlank(symptoms[i])) {
                        symptomsOrder.add(symptoms[i]);
                    }
                }
            }
            line = reader.readLine();
            while (line != null) {
                String[] values = line.split(",");
                String drugName = values[1];
                //TODO clean & validate drug name
                symptomsMap.put(drugName, new ArrayList<>());
                for (int i=2;i<values.length;i++) {
                    if (values[i].contains("1")) {
                        symptomsMap.get(drugName).add(symptomsOrder.get(i-2));
                    }
                }
                line = reader.readLine();
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return symptomsMap;
    }

    public static Map<String, List<String>> generateResults(Map<String, List<String>> proteinMap, Map<String, List<String>> symptomsMap) {
        Map<String, List<String>> resultMap = new HashMap<>();

        Map<String, List<String>> sortedProteinMap = proteinMap.entrySet().stream()
                .sorted(comparingInt(e->e.getValue().size()))
                .collect(toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (a,b) -> {throw new AssertionError();},
                        LinkedHashMap::new
                ));

        ArrayList proteinList = new ArrayList(sortedProteinMap.keySet());
        for (int i = proteinList.size() - 1; i >= 0; i--) {
            String protein = (String) proteinList.get(i);
            List<String> drugList = sortedProteinMap.get(protein);
            resultMap.put(protein, new ArrayList<>());
            List<String> commonSymptoms = null;
            for (String drug : drugList) {
                if (commonSymptoms==null)
                    commonSymptoms = symptomsMap.get(drug);
                else {
                    commonSymptoms = intersection(commonSymptoms, symptomsMap.get(drug));
                }
            }
            resultMap.get(protein).addAll(commonSymptoms);
        }
        return resultMap;
    }

    public static <T> List<T> intersection(List<T> list1, List<T> list2) {
        List<T> list = new ArrayList<T>();

        for (T t : list1) {
            if(list2.contains(t)) {
                list.add(t);
            }
        }

        return list;
    }

    public static void main(String[] a) {
        Map<String, List<String>> proteinMap = readProteinData();
        Map<String, List<String>> symptomsMap = readSymptomsData();
        Map<String, List<String>> results = generateResults(proteinMap, symptomsMap);
    }
}
