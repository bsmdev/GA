import javafx.embed.swing.JFXPanel;
import sun.java2d.loops.DrawLine;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.awt.*;
import java.util.List;
import javax.swing.*;

/**
 * Created by Andreas on 01.02.2019.
 */
public class MDVRPInstance extends JPanel{



    public String problemNum = "demo2";
    public String file = "./data_files_demo/" + problemNum;
    public String solutionFile = "./solution_files_demo/" + problemNum + ".res";

    public int vehiclesPerDepot;
    public int numCustomers;
    public int numDepots;



    public int populationSize = 40;
    public float mutationRate = 0.99f;  // 20-35 % ?
    public int tournamentSize = 5;   //12
    public int elitismPercentage = 10;
    public float crossoverFactor = 0.1f;
    public float crossOverrate = 0.1f;

    public float heuristicInitChance = 1.0f;

    public int numberOfGenerations = 20000;

    public long maxRunTime = 1000*60*5; //milliseconds.    1000 ms = 1s            1000*60 = 1min

    public int interventionRate = 500;


    public Solution finalSolution;

    public double targetCost;


    public ArrayList<Customer> customers = new ArrayList<>();
    public ArrayList<Depot> depots = new ArrayList<>();

    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;


        Dimension size = getSize();

        Color[] depotColors = {Color.RED, Color.GREEN, Color.YELLOW, Color.BLUE,
                                Color.PINK, Color.CYAN, Color.MAGENTA, Color.GRAY,
                                Color.orange};

        int centerX = Math.round(size.width/2);
        int centerY = Math.round(size.height/2);
        int expandFactor = 2;


        //Draw depots
        g2d.setStroke(new BasicStroke(6.0f));
        g2d.setColor(Color.red);
        for (int i = 0; i < depots.size(); i++){
            Depot d = depots.get(i);
            g2d.setColor(depotColors[i]);
            g2d.drawLine(d.xCoord * expandFactor + centerX, d.yCoord * expandFactor+ centerY, d.xCoord * expandFactor+ centerX, d.yCoord * expandFactor+ centerY);
        }


        //Draw customers
        g2d.setStroke(new BasicStroke(3.0f));
        g2d.setColor(Color.black);
        for (int i = 0; i < customers.size(); i++){
            Customer c = customers.get(i);
            g2d.drawLine(c.xCoord * expandFactor+ centerX, c.yCoord * expandFactor+ centerY, c.xCoord * expandFactor+ centerX, c.yCoord * expandFactor+ centerY);
        }



        //draw solution routes
        g2d.setStroke(new BasicStroke(1.0f));
        int colorIndex = 0;

        // access every route
        for (int i = 0; i < finalSolution.solution.length; i++){
            if(i != 0){
                if((i)%(vehiclesPerDepot) == 0){
                    colorIndex +=1;
                }
            }

            g2d.setColor(depotColors[colorIndex]);
            Depot startCoord = depots.get(colorIndex);
            int startX = startCoord.xCoord;
            int startY = startCoord.yCoord;
            int nextX, nextY;

            Customer lastCustomer = null;

            //draw paths between points in route
            for(int k = 0; k < finalSolution.solution[i].size(); k++){
                Customer customerCoord = customers.get(finalSolution.solution[i].get(k) - 1);
                nextX = customerCoord.xCoord;
                nextY = customerCoord.yCoord;
                g2d.drawLine(startX * expandFactor + centerX, startY * expandFactor + centerY, nextX * expandFactor + centerX, nextY * expandFactor+centerY);
                startX = nextX;
                startY = nextY;
                lastCustomer = customerCoord;
            }
            if(lastCustomer != null){
                g2d.drawLine(startX * expandFactor + centerX, startY * expandFactor + centerY, lastCustomer.closestDepot.xCoord * expandFactor + centerX, lastCustomer.closestDepot.yCoord * expandFactor+centerY);

            }


            //System.out.println(finalSolution.solution[i]);


        }


    }

    public MDVRPInstance() {

    }

    public static void main(String[] args) {
        MDVRPInstance i = new MDVRPInstance();
        try{
            i.readFile(i.file);
        }
        catch(IOException ex){

        }

        for(int x = 0; x < i.customers.size(); x++){
            i.customers.get(x).setClosestNeighboorIndex(i.customers);
        }

        //populationSize - too small: premature convergence
        //               - too large: too long to compute
        //mutationrate - too low: not enough exploration
        //             - too high: too much noise
        //crossover:
        //   -late in the search: crossover has smaller effect
        //   - selective choice of crossoverpoint


        // HOW TO MEASURE?
        //        - never draw conclusions from a single run
        //        - more testing!  statistics, measure, experiment
                                    // efficiency(time), effectivity(quality)


        //mutationRate 1-3%

        Population population = new Population(i.populationSize, i.vehiclesPerDepot, i.numCustomers,
                i.numDepots, i.depots, i.customers,  i.tournamentSize, i.elitismPercentage,
                i.mutationRate, i.crossoverFactor, i.targetCost, i.crossOverrate, i.heuristicInitChance, i.maxRunTime, i.interventionRate);

        //population.preEvolution(50);

        i.finalSolution = population.evolution(i.numberOfGenerations);
        System.out.println(i.finalSolution);
        System.out.println(i.isSolutionValid(i.finalSolution));


        i.printFinalSolution(i.finalSolution, i.targetCost);



        //TODO: BETTER RECOMBINATION

        //TODO:  THINK ABOUT:   is there some mutation/crossover that does the thing i need ?   The trouble is getting rid of overload and also sharing the load

        //TODO: PENALTY FOR NOT USING ALL ROUTES ?


        //Draw the problem
        JFrame frame = new JFrame("Points");

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(i);
        int frameWidth = 1000;
        int frameHeight = 1000;
        //Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        //frame.setBounds((int) screenSize.getWidth() - frameWidth, 0, frameWidth, frameHeight);
        frame.setSize(new Dimension(frameWidth, frameHeight));
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);





    }


    public boolean isSolutionValid(Solution solution){
        boolean isValid = true;
        int totalNum = 0;
        int[] check = new int[solution.numCustomers];
        for(int i = 0; i < check.length; i++){
            check[i] = 0;
        }

        for(int i = 0; i < solution.solution.length; i++){
            totalNum += solution.solution[i].size();
            for(int j = 0; j < solution.solution[i].size(); j++){

                check[solution.solution[i].get(j) - 1] += 1;
            }
        }
        System.out.println("\n ");
        for(int i = 0; i < check.length; i++){
            System.out.print(" ," + check[i]);
            if(check[i] != 1){
                isValid = false;

            }
        }
        System.out.println("\n ");
        return isValid;
    }


    public void readFile(String file) throws IOException{

        FileReader fileReader = new FileReader(file);
        BufferedReader bufferedReader = new BufferedReader(fileReader);

        String line = null;

        //list for storing data from the file
        List<String> lines = new ArrayList<>();

        //add lines to the list
        while ((line = bufferedReader.readLine()) != null){
            lines.add(line);
        }

        bufferedReader.close();


        String metaData = lines.get(0);
        lines.remove(0);
        System.out.println(metaData);


        String delimiter = " ";
        String[] tempValues;

        tempValues = metaData.split(delimiter);

        System.out.println(tempValues);

        vehiclesPerDepot = Integer.parseInt(tempValues[0]);
        numCustomers = Integer.parseInt(tempValues[1]);
        numDepots = Integer.parseInt(tempValues[2]);

        System.out.println(vehiclesPerDepot);
        System.out.println(numCustomers);
        System.out.println(numDepots);

        System.out.println("\n\n");

        //get info about depots and make depot-objects
        //TODO: handle empty string like for customers
        for(int i = 0; i < numDepots; i++){
            System.out.println(lines.get(i));
            tempValues = lines.get(i).split(delimiter);
            List<String> tempVals = new ArrayList<>();
            for(int y = 0; y < tempValues.length; y++){
                if(!tempValues[y].isEmpty()){
                    tempVals.add(tempValues[y]);
                }
            }
            Depot d = new Depot(i, Integer.parseInt(tempVals.get(0)), Integer.parseInt(tempVals.get(1)));
            depots.add(d);
        }

        System.out.println("\n\n");


        //get info about customers and make customer-objects
        for(int i = numDepots; i < numCustomers + numDepots; i++){
            System.out.println(lines.get(i));

            tempValues = lines.get(i).split(delimiter);
            List<String> tempVals = new ArrayList<>();
            for(int y = 0; y < tempValues.length; y++){
                if(!tempValues[y].isEmpty()){
                    tempVals.add(tempValues[y]);
                }
            }

            Customer c = new Customer(Integer.parseInt(tempVals.get(0)), Integer.parseInt(tempVals.get(1)),
                    Integer.parseInt(tempVals.get(2)), Integer.parseInt(tempVals.get(3)), Integer.parseInt(tempVals.get(4)));
            customers.add(c);
        }

        System.out.println("\n\n");

        //add depot coordinates from end of list
        //TODO: handle empty string like for customers
        for(int i = numDepots + numCustomers; i < lines.size(); i++){
            System.out.println(lines.get(i));
            //add coordinates to depot objects
            int index = i - numDepots - numCustomers;
            tempValues = lines.get(i).split(delimiter);
            List<String> tempVals = new ArrayList<>();
            for(int y = 0; y < tempValues.length; y++){
                if(!tempValues[y].isEmpty()){
                    tempVals.add(tempValues[y]);
                }
            }
            depots.get(index).xCoord = Integer.parseInt(tempVals.get(1));
            depots.get(index).yCoord = Integer.parseInt(tempVals.get(2));

        }

        for(int index = 0; index < customers.size(); index++){
            customers.get(index).closestDepot = getClosestDepot(customers.get(index));
            customers.get(index).distanceClosestDepot = getDistanceClosestDepot(customers.get(index));
        }

    //check depots
        for(int x = 0; x < depots.size(); x++){
            System.out.println(depots.get(x));
        }

        //check customers
        for(int x = 0; x < customers.size(); x++){
            System.out.println(customers.get(x));
        }


        FileReader solReader = new FileReader(solutionFile);
        BufferedReader solBufferedReader = new BufferedReader(solReader);

        String lineTarget = null;

        //list for storing data from the file
        List<String> linesTarget = new ArrayList<>();

        //add lines to the list
        while ((lineTarget = solBufferedReader.readLine()) != null){
            linesTarget.add(lineTarget);
        }

        solBufferedReader.close();


        String target = linesTarget.get(0);
        targetCost = Double.parseDouble(target);
        targetCost = targetCost + (targetCost/100.00*5.0);
        System.out.println("TARGET COST :  " + targetCost);


    }

    public void printFinalSolution(Solution solution, double targetCost){
        double cost = solution.getCostOfSolution();
        double roundedCost = Math.round(cost*100.0) / 100.0;
        System.out.println("Max routeduration: " + depots.get(0).maxRouteDuration + "  |  Max load per vehicle: " + depots.get(0).maxLoadVehicles);
        System.out.println("target cost: " + targetCost);
        System.out.println("\n\n Final solution: \n");


        System.out.println(roundedCost);
        String[] strings = new String[depots.size() * vehiclesPerDepot];
        ArrayList<Integer> depotIndexes = new ArrayList<>();
        for(int i = 0; i < strings.length; i++){
            strings[i] = "";
        }
        for(int i = 0; i < depots.size(); i++){
            for(int j = 0; j < vehiclesPerDepot; j++){
                //depot id
                strings[i*vehiclesPerDepot + j] += ((i+1) + "  ");
                depotIndexes.add(i);
                //vehicle id for that depot
                strings[i*vehiclesPerDepot + j] += ((j+1) + "  ");
            }
        }
        //distance for route
        for( int i = 0; i < solution.solution.length; i++){
            double routeDuration = 0;
            routeDuration = solution.getDurationForRow(solution.solution[i], i, depotIndexes.get(i));
            double roundedDistance = Math.round(routeDuration*100.0) / 100.0;
            strings[i] += roundedDistance  + "     ";
        }

        //demand for route
        for( int i = 0; i < solution.solution.length; i++){
            int routeLoad = 0;
            for(int j = 0; j < solution.solution[i].size(); j++){
                routeLoad += customers.get(solution.solution[i].get(j)-1).demand;

            }
            strings[i] += routeLoad + "     ";
        }
        // end-depot index
        for( int i = 0; i < solution.solution.length; i++){
            if(solution.solution[i].size() != 0){
                int lastCustomerIndex = solution.solution[i].get(solution.solution[i].size() - 1);
                Customer lastCustomer = customers.get(lastCustomerIndex - 1);
                strings[i] += (lastCustomer.closestDepot.id + 1) + "      ";
            }
            else{
                strings[i] += "X      ";
            }

        }

        // ordered sequence of customers per route
        for(int i = 0; i < solution.solution.length; i++){
            String seq = "";
            for(int j = 0; j < solution.solution[i].size(); j++){
                seq += solution.solution[i].get(j) + "  ";
            }
            strings[i] += seq;
        }


        //print
        for (int i = 0; i < strings.length; i++){
            System.out.println(strings[i]);
        }
    }

    public Depot getClosestDepot(Customer customer){
        double distance = 9999999;
        Depot closestDepot = null;
        for(int i = 0; i < depots.size(); i++){
            Depot depot = depots.get(i);
            double tempDistance = Point2D.distance(depot.xCoord, depot.yCoord, customer.xCoord, customer.yCoord);
            if(tempDistance < distance){
                distance = tempDistance;
                closestDepot = depot;
            }

        }
        return closestDepot;

    }


    public double getDistanceClosestDepot(Customer customer){
        double distance = 9999999;

        for(int i = 0; i < depots.size(); i++){
            Depot depot = depots.get(i);
            double tempDistance = Point2D.distance(depot.xCoord, depot.yCoord, customer.xCoord, customer.yCoord);
            if(tempDistance < distance){
                distance = tempDistance;

            }

        }
        return distance;

    }


}
