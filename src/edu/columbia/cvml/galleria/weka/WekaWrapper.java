package edu.columbia.cvml.galleria.weka;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import android.util.Log;
import weka.clusterers.ClusterEvaluation;
import weka.clusterers.EM;
import weka.clusterers.SimpleKMeans;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.CSVLoader;


public class WekaWrapper {
    
   private String tag = "WEKA_WRAPPER";
   private String filepath = new String();
   private String indexpath = "F://Downloads//index.txt";
   private int numberOfClusters = 12;
   private Instances data; 
   private Integer seedCount = 20; //Number of times KMeans is run before final assignment is made
   private ArrayList<String> filenames;
   private SimpleKMeans kmeans = new SimpleKMeans();
   
   private HashMap<String, ArrayList<String>> cMapping = new HashMap<String, ArrayList<String>>();
   
   public String loadData(InputStream filepath) throws Exception
   {
   
       CSVLoader loader = new CSVLoader();
       loader.setSource(filepath);
       data = loader.getDataSet();
       Log.i(tag,data.toSummaryString());
       Log.i(tag, "Data loaded");
       
       return data.toSummaryString();

   }
   
   public void loadIndex(InputStream indexstream) throws Exception
   {
   
       Scanner scan = new Scanner(indexstream);
       
       ArrayList <String> fileNames = new ArrayList<String>();
       
       while(scan.hasNext())
       {    String newLine = scan.nextLine();
       System.out.println(newLine);    
       fileNames.add(newLine);
       }
       
       this.filenames = fileNames;
       
       Log.i(tag,"Index loaded");
       Log.i(tag, "filenames loaded:" + filenames.size());
       return;
   }       
   
   public String runKMeans() throws Exception{
       
       kmeans.setSeed(seedCount);
       kmeans.setMaxIterations(1000);
       kmeans.setPreserveInstancesOrder(true);
       kmeans.setNumClusters(numberOfClusters);
       kmeans.buildClusterer(data);
        int[] assignments = kmeans.getAssignments();
       
       HashMap<String, ArrayList<String>> hash = new HashMap<String, ArrayList<String>>();

        int i=0;
        for(int clusterNum : assignments) {               

            Log.i(tag,"New line\n");
               Log.i(tag,"Instance %d -> Cluster %d \n"+i +" " + clusterNum);
     
               if (hash.get(clusterNum)==null){
                   hash.put(""+clusterNum, new ArrayList<String>());                     
               }                
                   ArrayList<String> array = hash.get(""+clusterNum);
                   array.add(filenames.get(i));
               i++;
        }
        
        cMapping = hash;

       ClusterEvaluation eval = new ClusterEvaluation();
       eval.setClusterer(kmeans);
       eval.evaluateClusterer(data);
       System.out.println(eval.clusterResultsToString());

       return "Weka executing complete";
        
//        Instances centroids = kmeans.getClusterCentroids();
//        System.out.println(centroids.toString());
        
   }
  
public void describeClusters() {
    if (cMapping == null)
        Log.i(tag,"No clusters available currents: cMapping is set to null");
    else {   
            for (String clusterNum : cMapping.keySet()) {
                Log.i(tag,"Printing cluster number " +  clusterNum);
                for (String temp : cMapping.get(clusterNum) )
                    Log.i(tag,temp);
                
                Log.i(tag,"End cluster");
        }
    }
}


public HashMap<String, ArrayList<String>> getMappings(){
    
    if (cMapping!=null)
    {   Log.i(tag, "Returning cMapping");
        return cMapping;}
    
    Log.e(tag, "Mappings haven't been generated yet");
    return null;
}
   
   
public String getFilepath() {
    return filepath;
}

public void setFilepath(String filepath) {
    this.filepath = filepath;
}

public Integer getSeedCount() {
    return seedCount;
}

public void setSeedCount(Integer seedCount) {
    this.seedCount = seedCount;
}

public SimpleKMeans getKmeans() {
    return kmeans;
}

public void setKmeans(SimpleKMeans kmeans) {
    this.kmeans = kmeans;
}
   
    
}
