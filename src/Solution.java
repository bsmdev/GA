import java.awt.geom.Point2D;
import java.lang.reflect.Array;
import java.util.*;

/**
 * Created by Andreas on 03.02.2019.
 */
public class Solution {

    public int numDepots;
    public int numVehiclesPerDepot;
    public int numCustomers;
    public List<Depot> depots;
    public List<Customer> customers;
    public int maxRouteDuration;
    public int maxLoadVehicles;

    public double overloadPenalityFactor = 0.5;
    public double durationPenaltyFactor = 0.5;

    public double fitness;
    public double cost;

    public float heuristicInitChance;

    public ArrayList<Integer>[] solution;

    public Solution(int numDepots, int numVehiclesPerDepot, int numCustomers,
                    List<Depot> depots, List<Customer> customers, float heuristicInitChance,
                    boolean initSolution) {
        this.numDepots = numDepots;
        this.numVehiclesPerDepot = numVehiclesPerDepot;
        this.numCustomers = numCustomers;
        this.depots = depots;
        this.customers = customers;

        this.maxLoadVehicles = depots.get(0).maxLoadVehicles;
        this.maxRouteDuration = depots.get(0).maxRouteDuration;
        this.heuristicInitChance = heuristicInitChance;

        solution = new ArrayList[numDepots * numVehiclesPerDepot];

        for(int i = 0; i < numDepots * numVehiclesPerDepot; i++){
            solution[i] = new ArrayList<>();
        }
        if(initSolution){
            Random rand = new Random();
            if(rand.nextFloat() < 1){
                //heuristicInitGreedy(numCustomers, heuristicInitChance);
                boolean shuffle = true;
                if(rand.nextFloat() < 0.3){
                    shuffle = false;
                }
                int numNearest = getRandomNumberInRange(1, 4);
                heuristicInitNearest(numNearest, numCustomers, heuristicInitChance, shuffle);

            }
            else{
                heuristicInit(numCustomers, heuristicInitChance);
            }
            //heuristicInitGreedy(numCustomers, heuristicInitChance);
            //heuristicInit(numCustomers, heuristicInitChance);
            //initialize(numCustomers);
        }


    }

    public boolean solutionIsValid(Solution solution){
        boolean isValid = true;

        //check if overload violation

        for(int i = 0; i < solution.solution.length; i++){
            int depotIndex = 0;
            if(i != 0){
                depotIndex = i / numVehiclesPerDepot;
            }
            double routeDuration = getDurationForRow(solution.solution[i], i, depotIndex);
            if(routeDuration > maxRouteDuration && maxRouteDuration != 0){
                isValid = false;
                break;
            }
            double overload = getOverloadPenaltyForRow(solution.solution[i]);
            if(overload != 0){
                isValid = false;
                break;
            }
        }


        return isValid;
    }



    public double getOverloadPenaltyForSolution(){
        double total = 0;
        for(int i = 0; i < solution.length; i++){
            total += getOverloadPenaltyForRow(solution[i]);
        }
        return total;
    }



    public ArrayList<Integer> sortAccordingToLengthFromStartDepot(ArrayList<Integer> route){
        ArrayList<Integer> sortedList = new ArrayList<>();
        int num = route.size();
        for(int i = 0; i < num; i++){
            int max = Collections.max(route);
            route.remove(route.indexOf(Collections.max(route)));
            sortedList.add(max);
        }
        return sortedList;

    }

    public ArrayList<Integer> straightenOut(ArrayList<Integer> route, int routeIndex){
        ArrayList<Integer> straightList = new ArrayList<>();
        for(int i = 0; i < route.size(); i++){
            straightList.add(route.get(i));
        }



        while(straightList.size() != 0){
            int cid = straightList.get(0);
            straightList.remove(straightList.indexOf(cid));
            int depotIndex = depots.indexOf(customers.get(cid - 1).closestDepot);
            double currentDuration = getDurationForRow(route, routeIndex, depotIndex);
            int indexOfCID = route.indexOf(cid);

            route.remove(route.indexOf(cid));
            int bestIndex = indexOfCID;

            for(int i = 0; i < route.size(); i++){
                route.add(i, cid);

                double duration = getDurationForRow(route, routeIndex, depotIndex);

                if(duration < currentDuration){
                    currentDuration = duration;
                    bestIndex = i;
                }
                route.remove(route.indexOf(cid));
            }
            route.add(bestIndex, cid);


        }

        return route;
    }


    // add to one route at a time
    //choose the customers that is closest to all customers + depot in that route

    public void heuristicInitNearest(int numNearest, int numCustomers, float heuristicInitChance, boolean shuffle){
        ArrayList<Integer> cIds = new ArrayList<>();
        Random rand = new Random();
        //get list of customer ids used - to be randomized.
        for(int i = 1; i < numCustomers + 1; i++){
            cIds.add(i);
        }

        //mens det fortsatt er kunder igjen å plassere
        int counter = 0;
        while(cIds.size() > 0 && counter < numNearest){

            //addToNearestRoute(cIds.get(0));
            //cIds.remove(0);


            int depotIndex = 0;
            for(int i = 0; i < solution.length; i++){

                if(cIds.size() == 0){
                    break;
                }
                if(i != 0 && i%numVehiclesPerDepot == 0){
                    depotIndex += 1;
                }
                int nearestNeighbor = getClosestNeighbor(numNearest, solution[i], i, depotIndex, cIds);
                solution[i].add(nearestNeighbor);
                cIds.remove(cIds.indexOf(nearestNeighbor));
            }
            counter += 1;

        }

        if(shuffle){
            Collections.shuffle(cIds);
        }

        int num = cIds.size();

        if( rand.nextFloat() < heuristicInitChance){
            for(int i = 0; i < num; i++){

                int routeIndex = 0;
                int geneIndex = 0;
                double bestCost = Double.POSITIVE_INFINITY;

                for(int j = 0; j < numDepots*numVehiclesPerDepot; j++){
                    if(solution[j].size() == 0){
                        solution[j].add(0, cIds.get(i));
                        if(getFitness() < bestCost){
                            routeIndex = j;
                            geneIndex = 0;
                            bestCost = getFitness();
                        }
                        solution[j].remove(solution[j].indexOf(cIds.get(i)));
                    }
                    else{
                        for( int k = 0; k < solution[j].size(); k++){
                            solution[j].add(k, cIds.get(i));
                            if(getFitness() < bestCost){
                                routeIndex = j;
                                geneIndex = k;
                                bestCost = getFitness();
                            }
                            solution[j].remove(solution[j].indexOf(cIds.get(i)));

                        }
                    }


                }
                solution[routeIndex].add(geneIndex, cIds.get(i));




            }

        }





    }


    public void heuristicInitNearestOneByOne(int numCustomers, float heuristicInitChance){
        ArrayList<Integer> cIds = new ArrayList<>();

        ArrayList<Integer> routeIds = new ArrayList<>();

        //get list of customer ids used - to be randomized.
        for(int i = 1; i < numCustomers + 1; i++){
            cIds.add(i);
        }

        //get list of customer ids used - to be randomized.
        for(int i = 0; i < solution.length; i++){
            routeIds.add(i);
        }
        Collections.shuffle(routeIds);

        for(int i = 0; i < routeIds.size(); i++){



        }

    }

    public void addToNearestRoute(int cid){

        int bestRouteIndex = 0;
        double nearest = Double.POSITIVE_INFINITY;
        Customer cust = customers.get(cid -1);

        int depotIndex = 0;
        for(int i = 0; i < solution.length; i++){
            double distance = 0;
            if(i != 0 && i%numVehiclesPerDepot == 0){
                depotIndex += 1;
            }
            Depot startDepot = depots.get(depotIndex);
            distance = Point2D.distance(cust.xCoord, cust.yCoord, startDepot.xCoord, startDepot.yCoord);

            for(int c = 0; c < solution[i].size(); c++){
                distance += Point2D.distance(cust.xCoord, cust.yCoord, customers.get(solution[i].get(c)-1).xCoord, customers.get(solution[i].get(c)-1).yCoord);

            }
            if(distance < nearest){
                nearest = distance;
                bestRouteIndex = i;
            }



        }
        solution[bestRouteIndex].add(cid);



    }


    public int getClosestNeighbor(int numNearest, ArrayList<Integer> route, int routeIndex, int depotIndex , ArrayList<Integer> cIds){

        Depot startDepot = depots.get(depotIndex);
        double nearest = Double.POSITIVE_INFINITY;
        int cid = 0;



        for(int i = 0; i < cIds.size(); i++){
            Customer c = customers.get(cIds.get(i)-1);

            double distance = Point2D.distance(c.xCoord, c.yCoord, startDepot.xCoord, startDepot.yCoord);



            for(int k = 0; k < route.size(); k++){


                distance += Point2D.distance(c.xCoord, c.yCoord, customers.get(route.get(k)-1).xCoord, customers.get(route.get(k)-1).yCoord);
            }
            if(distance < nearest){
                nearest = distance;
                cid = cIds.get(i);
            }


        }


        return cid;
    }

    public ArrayList<Integer> straightenPath(ArrayList<Integer> route){

        if(route.size() == 0 || route.size() == 1){
            return route;
        }

        Depot startDepot = customers.get(route.get(0)-1).closestDepot;
        Depot endDepot = customers.get(route.get(route.size()-1)-1).closestDepot;

        ArrayList<Integer> newRoute = new ArrayList<>();
        ArrayList<Integer> values = new ArrayList<>();
        for(int i = 0; i < route.size(); i++){
            if(i == 0){
                newRoute.add(route.get(i));
                continue;
            }
            if(i == route.size() - 1){
                newRoute.add(1, route.get(i));
                continue;
            }
            values.add(route.get(i));
        }
        int startX = customers.get(newRoute.get(0)-1).xCoord;
        int startY = customers.get(newRoute.get(0)-1).yCoord;
        int endX = customers.get(newRoute.get(1)-1).xCoord;
        int endY = customers.get(newRoute.get(1)-1).yCoord;

        int counter = 0;
        int lastStartIndex = 1;
        int lastEndIndex = 1;

        while(values.size() != 0){
            double bestDistance = Double.POSITIVE_INFINITY;
            int index = 0;

            if(counter % 2 == 0){

                for( int i = 0; i < values.size(); i++){
                    double distance = Point2D.distance(startX, startY, customers.get(values.get(i)-1).xCoord, customers.get(values.get(i)-1).yCoord);
                    if(distance < bestDistance){
                        index = i;
                        bestDistance = distance;
                    }
                }
                int value = values.get(index);
                newRoute.add(0+lastStartIndex, value);
                values.remove(index);
                lastStartIndex += 1;
                startX = customers.get(value-1).xCoord;
                startX = customers.get(value-1).yCoord;

                //measure every variables distance from start
                //keep the closest
                //remove from values
            }
            else{
                //measure every variables distance from start
                for( int i = 0; i < values.size(); i++){
                    double distance = Point2D.distance(endX, endY, customers.get(values.get(i)-1).xCoord, customers.get(values.get(i)-1).yCoord);
                    if(distance < bestDistance){
                        index = i;
                        bestDistance = distance;
                    }
                }
                int value = values.get(index);
                newRoute.add(newRoute.size()-1-lastEndIndex, value);
                values.remove(index);
                lastEndIndex += 1;
                endX = customers.get(value-1).xCoord;
                endX = customers.get(value-1).yCoord;
            }

            counter += 1;
        }

    return newRoute;
    }




    // assign customers to routes belonging to the closest depot
    public void heuristicInitGreedy(int numCustomers, float heuristicInitChance){

        Random rand = new Random();
        ArrayList<Integer> cIds = new ArrayList<>();

        //get list of customer ids used - to be randomized.
        for(int i = 1; i < numCustomers + 1; i++){
            cIds.add(i);
        }
        Collections.shuffle(cIds);

        if( rand.nextFloat() < heuristicInitChance){
            for(int i = 0; i < numCustomers; i++){

                int routeIndex = 0;
                int geneIndex = 0;
                double bestCost = Double.POSITIVE_INFINITY;

                for(int j = 0; j < numDepots*numVehiclesPerDepot; j++){
                    if(solution[j].size() == 0){
                        solution[j].add(0, cIds.get(i));
                        if(getFitness() < bestCost){
                            routeIndex = j;
                            geneIndex = 0;
                            bestCost = getFitness();
                        }
                        solution[j].remove(solution[j].indexOf(cIds.get(i)));
                    }
                    else{
                        for( int k = 0; k < solution[j].size(); k++){
                            solution[j].add(k, cIds.get(i));
                            if(getFitness() < bestCost){
                                routeIndex = j;
                                geneIndex = k;
                                bestCost = getFitness();
                            }
                            solution[j].remove(solution[j].indexOf(cIds.get(i)));

                        }
                    }


                }
                solution[routeIndex].add(geneIndex, cIds.get(i));




            }

        }




    }


    // assign customers to routes belonging to the closest depot
    public void heuristicInit(int numCustomers, float heuristicInitChance){

        Random rand = new Random();

        if( rand.nextFloat() < heuristicInitChance){
            int numRoutes = this.numDepots * this.numVehiclesPerDepot - 1;
            for(int i = 1; i < numCustomers + 1; i++){
                Customer customer = customers.get(i-1);
                Depot depot = customer.closestDepot;
                int depotIndex = depots.indexOf(depot);
                //get which range of routes
                int minRouteIndex = depotIndex * numVehiclesPerDepot;
                int maxRouteIndex = minRouteIndex + numVehiclesPerDepot - 1;
                int routeNumber = getRandomNumberInRange(minRouteIndex, maxRouteIndex);
                ArrayList<Integer> route = solution[routeNumber];
                int routeSize = route.size();
                int index = 0;
                if(routeSize > 0){
                    index = getRandomNumberInRange(0, routeSize);
                }
                route.add(index, i);
            }
            for(int i = 0; i < solution.length; i++){
                solution[i] = sortAccordingToLengthFromStartDepot(solution[i]);
            }
        }
        else{
            //random init
            int numRoutes = this.numDepots * this.numVehiclesPerDepot - 1;
            for(int i = 1; i < numCustomers + 1; i++){
                int routeNumber = getRandomNumberInRange(0, numRoutes);
                ArrayList<Integer> route = solution[routeNumber];
                int routeSize = route.size();
                int index = 0;
                if(routeSize > 0){
                    index = getRandomNumberInRange(0, routeSize);
                }
                route.add(index, i);
            }
        }



    }



    public void initialize(int numCustomers){

        //random init
        int numRoutes = this.numDepots * this.numVehiclesPerDepot - 1;
        for(int i = 1; i < numCustomers + 1; i++){
            int routeNumber = getRandomNumberInRange(0, numRoutes);
            ArrayList<Integer> route = solution[routeNumber];
            int routeSize = route.size();
            int index = 0;
            if(routeSize > 0){
                index = getRandomNumberInRange(0, routeSize);
            }
            route.add(index, i);
        }

    }

    //TODO: if duration of route ( actually distance )  is larger than maxRouteDuration, use that overflow as the basis for penalty
    public double getDurationForRow(ArrayList<Integer> route, int routeIndex, int depotIndex){


        //get distance for  this route
        double routeDistance = 0;

        Depot startDepot = depots.get(depotIndex);
        int startX = startDepot.xCoord;
        int startY = startDepot.yCoord;
        for(int k = 0; k < route.size(); k++){
            Customer customer = customers.get(route.get(k)- 1);
            int nextX = customer.xCoord;
            int nextY = customer.yCoord;
            double distance = Point2D.distance(startX, startY, nextX, nextY);
            routeDistance += distance;
            startX = nextX;
            startY = nextY;
            if(k == route.size() - 1){
                routeDistance += customers.get(route.get(k) - 1).distanceClosestDepot;
            }

        }

        //check if routedistance is over maxRouteDistance

        return routeDistance;
    }


    public double getDurationPenaltyForSolution(){
        double total = 0;
        int depotIndex = 0;
        for(int i = 0; i < solution.length; i++){
            if(i != 0){
                if((i % numVehiclesPerDepot) == 0){
                    depotIndex += 1;
                }
            }
            total += getDurationPenaltyForRow(solution[i], i, depotIndex);


        }
        return total;
    }




    public double getOverloadPenaltyForRow(ArrayList<Integer> route){
        double total = 0;
        for(int i = 0; i < route.size(); i++){
            total += customers.get(route.get(i)-1).demand;
        }
        double diff = total - maxLoadVehicles;
        if( diff > 0 ){
            //return diff*diff;

            return diff;
        }
        else{
            return 0;
        }

    }


    public double getFitnessOfRoute(ArrayList<Integer> route, int routeIndex, int depotIndex){
        double overloadPen = getOverloadPenaltyForRow(route);
        double durationPen = getDurationPenaltyForRow(route, routeIndex, depotIndex);
        double duration = getDurationForRow(route, routeIndex, depotIndex);


        double result = overloadPen*5 + durationPen*5 + duration;
        return result;

    }


    public ArrayList<Integer> putInBestPlace(int cid, ArrayList<Integer> route, int routeIndex, int depotIndex){

        double bestFitness = Double.POSITIVE_INFINITY;
        int bestindex = 0;

        for(int i = 0; i < route.size(); i++){
            route.add(i, cid);
            double fitness = getFitnessOfRoute(route, routeIndex, depotIndex);
            if( fitness < bestFitness){
                bestFitness = fitness;
                bestindex = i;
            }
            route.remove(route.indexOf(cid));


        }

        route.add(bestindex, cid);



        return route;
    }


    //TODO: if duration of route ( actually distance )  is larger than maxRouteDuration, use that overflow as the basis for penalty
    public double getDurationPenaltyForRow(ArrayList<Integer> route, int routeIndex, int depotIndex){




        //get distance for  this route
        double routeDistance = 0;

        Depot startDepot = depots.get(depotIndex);
        int startX = startDepot.xCoord;
        int startY = startDepot.yCoord;
        for(int k = 0; k < route.size(); k++){
            Customer customer = customers.get(route.get(k)- 1);
            int nextX = customer.xCoord;
            int nextY = customer.yCoord;
            double distance = Point2D.distance(startX, startY, nextX, nextY);
            routeDistance += distance;
            startX = nextX;
            startY = nextY;
            if(k == route.size() - 1){
                routeDistance += customers.get(route.get(k) - 1).distanceClosestDepot;
            }

        }

        //gjør noe smartere her !



        double diff = routeDistance - maxRouteDuration;
        if( diff > 0 ){
            //return diff*diff;
            if(maxRouteDuration != 0){
                return diff;
            }

        }

        return 0;

    }

    public double getFitness(){
        double cost = getCostOfSolution();
        double durationPen = getDurationPenaltyForSolution();
        double overloadPen = getOverloadPenaltyForSolution();
        if(this.maxRouteDuration == 0){
            //durationPen = durationPen / 2;
        }

        double fitnessX = cost + durationPen * 20 + overloadPen * 20;
        //double fitnessX = cost + overloadPen*2;


        return fitnessX;
    }


    public double setFitnessAndCost(){
        double cost = getCostOfSolution();
        double durationPen = getDurationPenaltyForSolution();
        double overloadPen = getOverloadPenaltyForSolution();
        if(this.maxRouteDuration == 0){
            //durationPen = durationPen / 2;
        }
        this.cost = cost;
        double fitnessX = cost + durationPen * 20 + overloadPen * 20;
        //double fitnessX = cost + overloadPen*2;

        this.fitness = fitnessX;
        return fitnessX;

    }


    public double getCostOfSolution(){
        double cost = 0;
        int depotIndex = 0;
        for(int i = 0; i < solution.length; i++){
            if(i != 0){
                if((i % numVehiclesPerDepot) == 0){
                    depotIndex += 1;
                }
            }
            Depot startDepot = depots.get(depotIndex);
            int startX = startDepot.xCoord;
            int startY = startDepot.yCoord;

            for(int k = 0; k < solution[i].size(); k++){
                Customer customer = customers.get(solution[i].get(k)- 1);
                int nextX = customer.xCoord;
                int nextY = customer.yCoord;
                double distance = Point2D.distance(startX, startY, nextX, nextY);
                cost += distance;
                startX = nextX;
                startY = nextY;
                if(k == solution[i].size() - 1){
                    cost += customers.get(solution[i].get(k) - 1).distanceClosestDepot;
                }

            }
        }
        return cost;
    }






    private static int getRandomNumberInRange(int min, int max) {

        if (min >= max) {
            return min;
            //throw new IllegalArgumentException("max must be greater than min");
        }

        Random r = new Random();
        return r.nextInt((max - min) + 1) + min;
    }


    @Override
    public String toString() {
        return "Solution{" +
                "numDepots=" + numDepots +
                ", numVehiclesPerDepot=" + numVehiclesPerDepot +
                ", numCustomers=" + numCustomers +
                ", solution=" + Arrays.toString(solution) +
                '}';
    }
}
