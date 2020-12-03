package knnGridSearch;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

public class makeIndex {
	
	public static void duplicate_elimination(String data_path, String data_path_new){
		// read the original dataset from data_path
		// eliminate duplicates by deleting the corresponding lines
		// write the dataset without duplicates into data_path_new
		
		Hashtable<String, Integer> ht = new Hashtable<>();
		File file = new File(data_path);
		System.out.println((file.length()));
		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			String line;
			while ((line = br.readLine()) != null ) {
				String[] tokens = line.split("\\t");
				String newLine = tokens[2] + "\t" + tokens[3] ;
				int id = Integer.parseInt(tokens[4]);
				if (!ht.containsKey(newLine)) {
					ht.put(newLine, id);
				}  else {
					if(ht.get(newLine) > id) {
						ht.put(newLine,id);
					}
				}
				
			}
		} catch (FileNotFoundException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			
		
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(data_path_new));
			for (Map.Entry<String, Integer> e : ht.entrySet()){
				//System.out.println(e.getKey() + "\t" + e.getValue());
				writer.write(e.getKey() + "\t" + e.getValue());
				writer.newLine();
			}
			writer.close();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} 
		
		
		
	}
	public static void create_index(String data_path_new, String index_path, int n){
		// To create a grid index and save it to file on "index_path".
		// The output file should contain exactly n*n lines. If there is no point in the cell, just leave it empty after ":".
		
		double x_totaldistance = 90.0 + 405.7;
		double y_totaldistance = 177.5 + 176.3;
		
		double x_gridDistance = x_totaldistance / n;
		double y_gridDistance = y_totaldistance / n;
		
		
		
		HashMap <String, ArrayList<String>> hm = new HashMap<String, ArrayList<String>>();
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				String cell = "Cell " + i + ", " + j + ": ";
				hm.put(cell, new ArrayList<String>());
			}
		}
		
		File file = new File(data_path_new);
		double max = 0.0;
		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			String line;
			int index = 0;
			while((line = br.readLine()) != null && index < 2) {
				String[] tokens = line.split("\\t");
				double x = Double.parseDouble(tokens[0]);
				double y = Double.parseDouble(tokens[1]);
				int id = Integer.parseInt(tokens[2]);
				if (x > max) {
					max = x;
				}
				double x_min = -90.0;
				double x_max = 405.7;
				double y_min = -176.3;
				double y_max = 177.5;
				
				int cell_x = 0;
				int cell_y = 0;
				
				x_min = x_min + x_gridDistance;
				while(x > x_min) {
					cell_x = cell_x + 1;
					x_min = x_min + x_gridDistance;
				}
				
				y_min = y_min + y_gridDistance;
				while(y > y_min) {
					cell_y = cell_y + 1;
					y_min = y_min + y_gridDistance;
				}				
				
				String cellPos = "Cell " + cell_x + ", " + cell_y + ": ";
				String cellData = id + " " + x + " " + y ;
				
				hm.get(cellPos).add(cellData);				
			}
		} catch (FileNotFoundException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(index_path));
			for (int i = 0; i < n; i++) {
				for (int j = 0; j < n; j++) {
					String cell = "Cell " + i + ", " + j + ": ";
					writer.write(cell + "\t" + hm.get(cell));
					writer.newLine();
				}
			}
			
			writer.close();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} 
		
	}
	public static void main(String[]args){
		
		
		
  		if(args.length != 4){
  			System.out.println("Usage: java makeIndex DATA_PATH INDEX_PATH DATA_PATH_NEW N");
  			
  			/*
			DATA_PATH(String): the file path of Gowalla_totalCheckins.txt
			INDEX_PATH(String): the output file path of the grid index
			DATA_PATH_NEW(String): the file path of the dataset without duplicates
  			N(integer): the grid index size
			*/
  			return;
  		} else{
  			
  		}
		duplicate_elimination(args[0], args[2]);
		long s = System.currentTimeMillis();
  		create_index(args[2], args[1], Integer.parseInt(args[3]));
		long t = System.currentTimeMillis();
		System.out.println("Index construction time: "+(t-s));
  	}
}	