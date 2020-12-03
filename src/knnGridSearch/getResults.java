package knnGridSearch;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ThreadLocalRandom;
import java.util.PriorityQueue;

import javax.swing.text.html.HTMLDocument.Iterator;

public class getResults{
	static ArrayList<Long> timeToLoadIndex = new ArrayList<Long>();
	static ArrayList<Long> timeToExecuteKnnGrid = new ArrayList<Long>();
	static ArrayList<Long> timeToExecuteLinear = new ArrayList<Long>();
	
	public static String knn_grid(double x, double y, String index_path, int k, int n){
		// to get the k-NN result with the help of the grid index
		// Please store the k-NN results by a String of location ids, like "11, 789, 125, 2, 771"		
		
		
		HashMap<String, ArrayList<String>> hm = new HashMap<String, ArrayList<String>>();
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				String cell = "Cell " + i + ", " + j ;
				hm.put(cell, new ArrayList<String>());
			}
		}		
		
		long s = System.currentTimeMillis();
		File file = new File(index_path);
		try (BufferedReader br = new BufferedReader( new FileReader(file))) {
			
			String line;
			while ((line = br.readLine()) != null  ){
				
				ArrayList<String> dataPointsList = new ArrayList<String>();
				String[] divideCellAndDatas = line.split(":");
				//System.out.println(divideCellAndDatas[0]);
				String trim = divideCellAndDatas[1].replace("\t", "");
				trim = trim.trim();
				if (trim.length() > 3) {
					
				
					String[] dataPoints = trim.substring(1, trim.length()-1).split(", ");
					for (int i = 0; i < dataPoints.length; i++) {
						dataPointsList.add(dataPoints[i]);
					}
					hm.put(divideCellAndDatas[0], dataPointsList);		
				}
			}		
			
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		long t = System.currentTimeMillis();
		//System.out.println("Grid index search time: "+(t-s));
		timeToLoadIndex.add(t-s);
		
		s = System.currentTimeMillis();
		
		double x_totaldistance = 90.0 + 405.7;
		double y_totaldistance = 177.5 + 176.3;
		
		double x_gridDistance = x_totaldistance / n; // n value
		double y_gridDistance = y_totaldistance / n; // n value
		
		double x_min = -90.0;
		//double x_max = 405.7;
		double y_min = -176.3;
		//double y_max = 177.5;
		
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
		
		
		
		PriorityQueue<Map.Entry<String,Double>> priorityQueue = new PriorityQueue<>(new Comparator<Map.Entry<String, Double>> () {

			@Override
			public int compare(Entry<String, Double> o1, Entry<String, Double> o2) {
				// TODO Auto-generated method stub
				return o2.getValue().compareTo(o1.getValue());
			}
			
		});
		
		ArrayList<String> result = new ArrayList<String>();
		
		/*priorityQueue.add(new AbstractMap.SimpleEntry<>("A", 3));
		priorityQueue.add(new AbstractMap.SimpleEntry<>("B", 2));
		
		 Map.Entry<String,Integer> map = priorityQueue.poll();
		System.out.println(map.getValue());*/
		
		//1st step: Find cell that contains q -> find neighbors of q by computing the Euclidean distance of q and p in cell.
		ArrayList<String> accessedCell = new ArrayList<String>();
		
		String cellPos = "Cell " + cell_x + ", " + cell_y;
		accessedCell.add(cellPos);
		//System.out.println(cellPos);
		Double largestKNNDistance = 0.0;
		
		if (hm.get(cellPos).size() > 0) {
			ArrayList<String> dataPoints = new ArrayList<String>();
			dataPoints = hm.get(cellPos);
			
			for (int i = 0; i < dataPoints.size(); i++) {
				String pos = dataPoints.get(i);
				
				String[] coordinate = pos.split(" ");
				
				double distance = Math.sqrt((x - Double.parseDouble(coordinate[1])) * (x - Double.parseDouble(coordinate[1])) + (y - Double.parseDouble(coordinate[2])) * (y - Double.parseDouble(coordinate[2])));
				
				if (priorityQueue.size() < k) {
					priorityQueue.add(new AbstractMap.SimpleEntry<>(coordinate[0],distance));
				}
				else if (distance < priorityQueue.peek().getValue()) {
					//System.out.println(priorityQueue.peek().getValue() + "   and distance " + distance);
					priorityQueue.poll();
					priorityQueue.add(new AbstractMap.SimpleEntry<>(coordinate[0],distance));
				}
			}

			largestKNNDistance = priorityQueue.peek().getValue();
		}
		
		//2nd step: Access cells layered around the cell which the data located at
		boolean continuePrune = true;
		int index = 1;
		x_min = -90.0;
		y_min = -176.3;
		
		while( index < n && continuePrune == true) {
			
			int rowStart = Math.max(cell_x - index, 0);
			int rowFinish = Math.min(cell_x + index, n-1);
			int colStart = Math.max(cell_y - index, 0);
			int colFinish = Math.min(cell_y + index, n - 1);
			
			double nearestDistancePosX;
			double nearestDistancePosY;
			
			
			// If priorityQueue does not has length k before accessing new layer, add items and we will have to prune the next layer.
			if(priorityQueue.size() < k) {	
				
				for (int curRow = rowStart; curRow <= rowFinish; curRow++){
					for(int curCol = colStart; curCol <= colFinish; curCol++) {
						String selectingCell = "Cell " + curRow + ", " + curCol;
						if(!accessedCell.contains(selectingCell)) {
							// Find the nearestDistancePos from cell to q.
							if (curRow < cell_x) {
								nearestDistancePosX = x_min + x_gridDistance * (curRow + 1);
							} else if (curRow > cell_x) {
								nearestDistancePosX = x_min + x_gridDistance * curRow;
							} else {
								nearestDistancePosX = x;
							}
							
							if (curCol < cell_y) {
								nearestDistancePosY = y_min + y_gridDistance * (curCol + 1);
							} else if (curCol > cell_y) {
								nearestDistancePosY = y_min + y_gridDistance * curCol;
							} else {
								nearestDistancePosY = y;
							}
							
							//Since the queue is smaller than k, we will access the cell regardless of the distance and add items to queue until it reaches k items.							
							ArrayList<String> dataPoints = new ArrayList<String>();
							dataPoints = hm.get(selectingCell);
							for (int i = 0; i < dataPoints.size(); i++) {
								String pos = dataPoints.get(i);							
								String[] coordinate = pos.split(" ");
								
								double distance = Math.sqrt((x - Double.parseDouble(coordinate[1])) * (x - Double.parseDouble(coordinate[1])) + (y - Double.parseDouble(coordinate[2])) * (y - Double.parseDouble(coordinate[2])));
								//If the queue size reaches k, we starting replacing items which is shorter.
								if (priorityQueue.size() ==  k) {
									if (distance < priorityQueue.peek().getValue()) {
										priorityQueue.poll();
										priorityQueue.add(new AbstractMap.SimpleEntry<>(coordinate[0],distance));
									}
								} else
									priorityQueue.add(new AbstractMap.SimpleEntry<>(coordinate[0],distance));
							}
							
							accessedCell.add(selectingCell);
						}
					}
				}
				
			} else {
				largestKNNDistance = priorityQueue.peek().getValue();
				for (int curRow = rowStart; curRow <= rowFinish; curRow++){
					for(int curCol = colStart; curCol <= colFinish; curCol++) {
						String selectingCell = "Cell " + curRow + ", " + curCol;
						
						if(!accessedCell.contains(selectingCell)) {
							// Find the nearestDistancePos from cell to q.
							if (curRow < cell_x) {
								nearestDistancePosX = x_min + x_gridDistance * (curRow + 1);
							} else if (curRow > cell_x) {
								nearestDistancePosX = x_min + x_gridDistance * curRow;
							} else {
								nearestDistancePosX = x;
								
							}
							
							if (curCol < cell_y) {
								
								nearestDistancePosY = y_min + y_gridDistance * (curCol + 1);
							} else if (curCol > cell_y) {
								
								nearestDistancePosY = y_min + y_gridDistance * curCol;
							} else {
								nearestDistancePosY = y;
							}
							
							//Find the nearest distance
							double cellDistance = Math.sqrt(((x - nearestDistancePosX) * (x - nearestDistancePosX)) + ((y - nearestDistancePosY) * (y - nearestDistancePosY)));
							// Only when the cellDistance is smaller than the largestKNNDistance, do we have to access the cell. 
							
							if (cellDistance < priorityQueue.peek().getValue() ) {
								ArrayList<String> dataPoints = new ArrayList<String>();
								dataPoints = hm.get(selectingCell);
								for (int i = 0; i < dataPoints.size(); i++) {
									String pos = dataPoints.get(i);							
									String[] coordinate = pos.split(" ");
									
									double distance = Math.sqrt((x - Double.parseDouble(coordinate[1])) * (x - Double.parseDouble(coordinate[1])) + (y - Double.parseDouble(coordinate[2])) * (y - Double.parseDouble(coordinate[2])));
									
									// check if distance < largestKNNDistance -> yes: replace, no: bye
									if (distance < priorityQueue.peek().getValue()) {
										priorityQueue.poll();
										priorityQueue.add(new AbstractMap.SimpleEntry<>(coordinate[0],distance));
									}
									
								}
							}
							
							accessedCell.add(selectingCell);
						}
					}					
				}												
			}			
			index++;
			if(rowStart == 0 && rowFinish == n-1 && colStart == 0 && colFinish == n-1) {
				continuePrune = false;	
			}
			
		}
		
		while (priorityQueue.size() > 0) {
			Map.Entry<String, Double> map = priorityQueue.poll();
			//System.out.println(map.getKey() + " " + map.getValue());
			result.add(map.getKey());
		}
		
		Collections.reverse(result);
		//System.out.println(result.toString().substring(1, result.toString().length()-1));
		t = System.currentTimeMillis();
		timeToExecuteKnnGrid.add(t-s);
		return result.toString().substring(1, result.toString().length()-1);
	
	}


	public static String knn_linear_scan(double x, double y, String data_path_new, int k){
		// to get the k-NN result by linear scan
		// Please store the k-NN results by a String of location ids, like "11, 789, 125, 2, 771"
		
		PriorityQueue<Map.Entry<String,Double>> priorityQueue = new PriorityQueue<>(new Comparator<Map.Entry<String, Double>> () {

			@Override
			public int compare(Entry<String, Double> o1, Entry<String, Double> o2) {
				// TODO Auto-generated method stub
				return o2.getValue().compareTo(o1.getValue());
			}
			
		});
		long s = System.currentTimeMillis();
		File file = new File(data_path_new);
		try(BufferedReader br = new BufferedReader(new FileReader(file))) {
			String line;
			while((line = br.readLine()) != null) {
				String[] divideXYId = line.split("\t");
				double distance = Math.sqrt((x - Double.parseDouble(divideXYId[0])) * (x - Double.parseDouble(divideXYId[0])) + (y - Double.parseDouble(divideXYId[1])) * (y - Double.parseDouble(divideXYId[1])));
				if(priorityQueue.size() < k) {
					priorityQueue.add(new AbstractMap.SimpleEntry<>(divideXYId[2], distance));
				} else {
					if (distance < priorityQueue.peek().getValue()) {
						//System.out.println(priorityQueue.peek().getValue() + "   and distance " + distance);
						priorityQueue.poll();
						priorityQueue.add(new AbstractMap.SimpleEntry<>(divideXYId[2],distance));
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
		
		
		ArrayList<String> result = new ArrayList<String>();
		while (priorityQueue.size() > 0) {
			Map.Entry<String, Double> map = priorityQueue.poll();
			//System.out.println(map.getKey() + " " + map.getValue());
			result.add(map.getKey());
		}
		
		Collections.reverse(result);
		//System.out.println(result.toString().substring(1, result.toString().length()-1) );
		long t = System.currentTimeMillis();
		timeToExecuteLinear.add(t-s);
		return result.toString().substring(1, result.toString().length()-1);

		
	}

	public static void main(String args[]){
  		if(args.length != 6){
  			System.out.println("Usage: java getResults X Y DATA_PATH_NEW INDEX_PATH K N");
  			/*
			X(double): the latitude of the query point q
			Y(double): the longitude of the query point q
			DATA_PATH_NEW(String): the file path of dataset you generated without duplicates
			INDEX_PATH(String): the file path of the grid index
			K(integer): the k value for k-NN search
			N(integer): the grid index size
  			*/
  			
  			//knn_linear_scan(245.61008329261915,-33.41468021826461, "noDup.txt", 10);
  			//knn_grid(245.61008329261915,-33.41468021826461,"index_path.txt", 10, 50);
  			//System.out.println(timeToLoadIndex);
  			//System.out.println(timeToExecuteKnnGrid);
  			//System.out.println(timeToExecuteLinear);
  			
  			
  			
  			return;
  		}
		long s = System.currentTimeMillis();
  		System.out.println("Linear scan results: "+knn_linear_scan(Double.parseDouble(args[0]), Double.parseDouble(args[1]), args[2], Integer.parseInt(args[4])));
		long t = System.currentTimeMillis();
		System.out.println("Linear scan time: "+(t-s));
		
		s = System.currentTimeMillis();
  		System.out.println("Grid index search results: "+knn_grid(Double.parseDouble(args[0]), Double.parseDouble(args[1]), args[3], Integer.parseInt(args[4]), Integer.parseInt(args[5])));
		t = System.currentTimeMillis();
		System.out.println("Grid index search time: "+(t-s));
		
		
		
		
  	}
}


