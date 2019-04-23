import java.awt.*;
import java.lang.reflect.Array;
import java.util.*;
import java.util.List;

/**
 * Created by Andreas on 04.02.2019.
 */
public class Population {

    public int populationSize;
    public int vehiclesPerDepot;
    public int numCustomers;
    public int numDepots;
    public ArrayList<Depot> depots;
    public ArrayList<Customer> customers;


    public int matingPoolSize;
    public int tournamentSize;

    float crossOverFactor;
    float crossOverrate;

    public int elitismPercentage;
    public int elitismCount;
    float mutationRate;

    public double targetCost;
    public double mutationChangeTarget;
    public double bestCost = Double.POSITIVE_INFINITY;

    public float heuristicInitChance;
    public long maxRunTime;

    public int interventionRate;

    public ArrayList<Solution> population = new ArrayList<>();


    //TODO: Constructor, initialize a population of solutions

    public Population(int populationSize, int vehiclesPerDepot, int numCustomers, int numDepots, ArrayList<Depot> depots, ArrayList<Customer> customers,
                      int tournamentSize, int elitismPercentage, float mutationRate, float crossOverFactor, double targetCost, float crossOverrate, float heuristicInitChance, long maxRunTime, int interventionRate) {
        this.populationSize = populationSize;
        this.vehiclesPerDepot = vehiclesPerDepot;
        this.numCustomers = numCustomers;
        this.numDepots = numDepots;
        this.depots = depots;
        this.customers = customers;
        this.mutationRate = mutationRate;
        this.crossOverFactor = crossOverFactor;
        this.crossOverrate = crossOverrate;
        this.targetCost = targetCost;
        this.mutationChangeTarget = targetCost / 100.0 * 106.0;
        this.maxRunTime = maxRunTime;
        this.interventionRate = interventionRate;

        this.heuristicInitChance = heuristicInitChance;

        this.tournamentSize = tournamentSize;
        this.elitismCount = Math.round(populationSize / elitismPercentage);
        this.elitismPercentage = elitismPercentage;

        for(int i = 0; i < populationSize; i++){
            Solution solution = new Solution(numDepots, vehiclesPerDepot, numCustomers, depots, customers, heuristicInitChance, true);
            //solution.initialize(numCustomers);   right now it is initialized in constructor
            population.add(solution);
            solution.setFitnessAndCost();
        }
    }


    public ArrayList<Solution> survivalTournament(ArrayList<Solution> survivalOfTheFittest){

        int counter = 0;
        int tournamentSize = this.tournamentSize;

        ArrayList<Solution> theFittest = new ArrayList<>();

        while (counter < this.populationSize){

            counter += 1;
            ArrayList<Solution> tournament = new ArrayList<>();
            Random rand = new Random();
            double minValue = Double.POSITIVE_INFINITY;
            int indexOfMinValue = -1;
            for(int i = 0; i < tournamentSize; i++){
                Solution randomSolution = survivalOfTheFittest.get(rand.nextInt(survivalOfTheFittest.size()));
                tournament.add(randomSolution);
                if(randomSolution.fitness < minValue){
                    indexOfMinValue = i;
                }

            }
            theFittest.add(tournament.get(indexOfMinValue));
        }
        return theFittest;

    }


    //TODO: REPRODUCTION SELECTION
    public ArrayList<Solution> tournamentSelection(int missingIndividuals){

        int counter = 0;

        int tournamentSize = this.tournamentSize;

        ArrayList<Solution> matingPool = new ArrayList<>();

        while (counter < missingIndividuals*2){

            counter += 1;
            ArrayList<Solution> tournament = new ArrayList<>();
            Random rand = new Random();
            double minValue = Double.POSITIVE_INFINITY;
            int indexOfMinValue = -1;
            for(int i = 0; i < tournamentSize; i++){
                Solution randomSolution = population.get(rand.nextInt(population.size()));
                tournament.add(randomSolution);
                if(randomSolution.fitness < minValue){
                    indexOfMinValue = i;
                }

            }
            matingPool.add(tournament.get(indexOfMinValue));
        }
        return matingPool;

    }

    public Solution getFittestOfAGeneration(){
        Collections.sort(population, Comparator.comparingDouble(Solution :: setFitnessAndCost));

        return population.get(0);
    }





    //TODO: REPRODUCTION ALGORITHM
    public ArrayList<Solution> reproduce(ArrayList<Solution> matingPool){
        ArrayList<Solution> offSpring = new ArrayList<>();

        ArrayList<Solution> mothers = new ArrayList<>();
        ArrayList<Solution> fathers = new ArrayList<>();

        for(int i = 1; i < matingPool.size() + 1; i++){
            if(i % 2 == 0){
                mothers.add(matingPool.get(i-1));
            }
            else{
                fathers.add(matingPool.get(i-1));
            }
        }

        for(int i = 0; i < mothers.size(); i++){

            Solution child = crossover(mothers.get(i), fathers.get(i));
            offSpring.add(child);
        }

        return offSpring;

    }

    public ArrayList<Solution> mutate(ArrayList<Solution> offSpring){
        ArrayList<Solution> mutatedOffspring = new ArrayList<>();
        for(int i = 0; i < offSpring.size(); i++) {
            mutatedOffspring.add(mutateIndividual(offSpring.get(i)));
        }

        return mutatedOffspring;
    }

    public ArrayList<Solution> mutate2(ArrayList<Solution> offSpring){
        ArrayList<Solution> mutatedOffspring = new ArrayList<>();
        for(int i = 0; i < offSpring.size(); i++) {
            mutatedOffspring.add(mutateIndividual2(offSpring.get(i)));
        }

        return mutatedOffspring;
    }


    public Solution bless(Solution solution){
        double previousFitness = solution.setFitnessAndCost();
        for(int i = 0; i < solution.solution.length; i++){
            for(int k = 0; k < solution.solution[i].size(); k++){

                int cid = solution.solution[i].get(k);

                Solution alt = new Solution(solution.numDepots, solution.numVehiclesPerDepot, solution.numCustomers, solution.depots, solution.customers, solution.heuristicInitChance, false);
                ArrayList<Integer>[] altSol = new ArrayList[solution.numDepots * solution.numVehiclesPerDepot];
                for(int t = 0; t < solution.solution.length; t++){
                    altSol[t] = new ArrayList<Integer>();
                    for(int v = 0; v < solution.solution[t].size(); v++){
                        altSol[t].add(solution.solution[t].get(v));
                    }
                }
                alt.solution = altSol;
                for(int t = 0; t < alt.solution.length; t++){
                    if(alt.solution[t].contains(cid)){
                        alt.solution[t].remove(alt.solution[t].indexOf(cid));
                    }

                }
                for(int t = 0; t < alt.solution.length; t++){
                    for(int v = 0; v < alt.solution[t].size(); v++){
                        alt.solution[t].add(v, cid);
                        if(alt.setFitnessAndCost() < previousFitness){
                            solution.solution = alt.solution;
                            solution.setFitnessAndCost();
                            previousFitness = solution.fitness;
                            break;
                        }
                        else{
                            alt.solution[t].remove(alt.solution[t].indexOf(cid));
                        }
                    }
                }


            }
        }
        return solution;
    }

    public Solution bless2(Solution solution){
        double previousFitness = solution.setFitnessAndCost();
        for(int i = 0; i < solution.solution.length; i++){
            for(int k = 0; k < solution.solution[i].size(); k++){

                int cid = solution.solution[i].get(k);

                Solution alt = new Solution(solution.numDepots, solution.numVehiclesPerDepot, solution.numCustomers, solution.depots, solution.customers, solution.heuristicInitChance, false);
                ArrayList<Integer>[] altSol = new ArrayList[solution.numDepots * solution.numVehiclesPerDepot];
                for(int t = 0; t < solution.solution.length; t++){
                    altSol[t] = new ArrayList<Integer>();
                    for(int v = 0; v < solution.solution[t].size(); v++){
                        altSol[t].add(solution.solution[t].get(v));
                    }
                }
                alt.solution = altSol;
                for(int t = 0; t < alt.solution.length; t++){
                    if(alt.solution[t].contains(cid)){
                        alt.solution[t].remove(alt.solution[t].indexOf(cid));
                    }

                }
                double bestPlacementFitness = Double.POSITIVE_INFINITY;
                int bestRouteIndex = 0;
                int bestGeneIndex = 0;
                for(int t = 0; t < alt.solution.length; t++){
                    for(int v = 0; v < alt.solution[t].size(); v++){
                        alt.solution[t].add(v, cid);
                        if(alt.setFitnessAndCost() <= bestPlacementFitness){

                            bestPlacementFitness = solution.fitness;
                            bestRouteIndex = t;
                            bestGeneIndex = v;

                        }

                        alt.solution[t].remove(alt.solution[t].indexOf(cid));
                    }
                }
                alt.solution[bestRouteIndex].add(bestGeneIndex, cid);
                solution.solution = alt.solution;
                solution.setFitnessAndCost();

            }
        }
        return solution;
    }



    public Solution divineIntervention(Solution solution){
        int numChanges = getRandomNumberInRange(3, 10);

        for(int num = 0; num < numChanges; num++){
            double previousFitness = solution.setFitnessAndCost();
            //INSERT  ONE
            int routeIndex = -1;
            //only choose a route that is not empty
            while (routeIndex == -1){
                int r = getRandomNumberInRange(0, solution.solution.length -1);
                if(solution.solution[r].size() != 0){
                    routeIndex = r;
                }
            }
            int geneIndex = getRandomNumberInRange(0, solution.solution[routeIndex].size() -1);
            int cid = solution.solution[routeIndex].get(geneIndex);

            Solution alt = new Solution(solution.numDepots, solution.numVehiclesPerDepot, solution.numCustomers, solution.depots, solution.customers, solution.heuristicInitChance, false);
            ArrayList<Integer>[] altSol = new ArrayList[solution.numDepots * solution.numVehiclesPerDepot];

            for(int i = 0; i < solution.solution.length; i++){
                altSol[i] = new ArrayList<Integer>();
                for(int k = 0; k < solution.solution[i].size(); k++){
                    altSol[i].add(solution.solution[i].get(k));
                }
            }

            alt.solution = altSol;
            //alt.solution[routeIndex].remove(geneIndex);
            for(int i = 0; i < alt.solution.length; i++){
                if(alt.solution[i].contains(cid)){
                    alt.solution[i].remove(alt.solution[i].indexOf(cid));
                }

            }

            for(int i = 0; i < alt.solution.length; i++){
                for(int k = 0; k < alt.solution[i].size(); k++){
                    alt.solution[i].add(k, cid);
                    if(alt.setFitnessAndCost() < previousFitness){
                        solution.solution = alt.solution;
                        solution.setFitnessAndCost();
                        previousFitness = solution.fitness;
                        break;
                    }
                    else{
                        alt.solution[i].remove(alt.solution[i].indexOf(cid));
                    }
                }
            }


        }
        return solution;
    }


    // mutate single individual
    public Solution mutateIndividual(Solution solution){
        //simple mutation where there i 0.01 chance that an element inserts itself somewhere else


        Random rand = new Random();

        if(rand.nextFloat() < mutationRate ){


            //TODO: ---------->              ADJUST WHICH MUTATIONS ARE POSSIBLE HERE
            // 5, 7 is all the best
            int mutationChoiceIndex = getRandomNumberInRange(5,10);
            if(bestCost < mutationChangeTarget){
                //System.out.println("\n\n\n\n\n\n\n\n\n\n\n\n\n QUANTUM KICK  \n\n\n\n\n\n\n\n\n\n\n\n\n");

                //mutationRate = 0.5f;
                mutationChoiceIndex = getRandomNumberInRange(5, 9);
                //crossOverrate = 0.3f;


            }
            else{

                // REAL mutationChoiceIndex = getRandomNumberInRange(5,9);
                mutationChoiceIndex = getRandomNumberInRange(5, 9);
            }

            if(mutationChoiceIndex == 10){

               solution = divineIntervention(solution);


            }
            else if(mutationChoiceIndex == 0){

                // original implementation of mutation - every 'gene' in the chromosome has a small chance of being placed somewhere elese in the chromosome
                for(int i = 0; i < solution.solution.length; i++){
                    for(int k = 0; k < solution.solution[i].size(); k++){
                        if(rand.nextFloat() < mutationRate){
                            int randomRouteIndex = getRandomNumberInRange(0, solution.solution.length - 1);

                            int randomGeneIndex = 0;
                            if(solution.solution[randomRouteIndex].size() > 1){
                                randomGeneIndex = getRandomNumberInRange(0, solution.solution[randomRouteIndex].size() - 1);
                            }


                            int customerID = solution.solution[i].get(k);
                            solution.solution[i].remove(k);
                            solution.solution[randomRouteIndex].add(randomGeneIndex, customerID);
                            //System.out.println("MUTATION FROM ROUTE" + i + " TO ROUTE " + randomRouteIndex);
                        }
                        solution.setFitnessAndCost();
                    }

                }


            }
            else if(mutationChoiceIndex == 9){
                //INSERT  ONE
                int routeIndex = -1;
                //only choose a route that is not empty
                while (routeIndex == -1){
                    int r = getRandomNumberInRange(0, solution.solution.length -1);
                    if(solution.solution[r].size() != 0){
                        routeIndex = r;
                    }
                }
                int geneIndex = getRandomNumberInRange(0, solution.solution[routeIndex].size() -1);
                int cid = solution.solution[routeIndex].get(geneIndex);
                solution.solution[routeIndex].remove(geneIndex);

                int routeIndex2 = getRandomNumberInRange(0, solution.solution.length -1);
                int geneIndex2 = getRandomNumberInRange(0, solution.solution[routeIndex2].size() -1);
                solution.solution[routeIndex2].add(geneIndex2, cid);


            }
            else if(mutationChoiceIndex == 7){
                //INSERT
                int routeIndex = -1;
                //only choose a route that is not empty
                while (routeIndex == -1){
                    int r = getRandomNumberInRange(0, solution.solution.length -1);
                    if(solution.solution[r].size() != 0){
                        routeIndex = r;
                    }
                }
                int geneIndex = getRandomNumberInRange(0, solution.solution[routeIndex].size() -1);
                int geneIndexEnd = getRandomNumberInRange(geneIndex, solution.solution[routeIndex].size() - 1);

                ArrayList<Integer> list = new ArrayList<>();
                for(int i = 0; i < geneIndexEnd - geneIndex; i++){
                    list.add(solution.solution[routeIndex].get(geneIndex));
                    solution.solution[routeIndex].remove(geneIndex);

                }

                //int gene = solution.solution[routeIndex].get(geneIndex);
                //solution.solution[routeIndex].remove(geneIndex);


                int routeIndex2 = getRandomNumberInRange(0, solution.solution.length -1);
                int geneIndex2 = getRandomNumberInRange(0, solution.solution[routeIndex2].size() -1);

                for(int i = 0; i < list.size(); i++){
                    solution.solution[routeIndex2].add(geneIndex2, list.get(i));
                }

                //solution.solution[routeIndex2].add(geneIndex2, gene);



            }
            else if(mutationChoiceIndex == 8){
                //SWAP
                int routeIndex1 = -1;
                //only choose a route that is not empty
                while (routeIndex1 == -1){
                    int r = getRandomNumberInRange(0, solution.solution.length -1);
                    if(solution.solution[r].size() != 0){
                        routeIndex1 = r;
                    }
                }
                int geneIndex1 = getRandomNumberInRange(0, solution.solution[routeIndex1].size() -1);
                int gene1 = solution.solution[routeIndex1].get(geneIndex1);
                solution.solution[routeIndex1].remove(geneIndex1);


                int routeIndex2 = -1;
                //only choose a route that is not empty
                while (routeIndex2 == -1){
                    int r = getRandomNumberInRange(0, solution.solution.length -1);
                    if(solution.solution[r].size() != 0){
                        routeIndex2 = r;
                    }
                }
                int geneIndex2 = getRandomNumberInRange(0, solution.solution[routeIndex2].size() -1);
                int gene2 = solution.solution[routeIndex2].get(geneIndex2);
                solution.solution[routeIndex2].remove(geneIndex2);

                solution.solution[routeIndex2].add(geneIndex2, gene1);
                solution.solution[routeIndex1].add(geneIndex1, gene2);






            }
            else if(mutationChoiceIndex == 5){
                //SCRAMBLE
                int routeIndex1 = -1;
                //only choose a route that is not empty
                while (routeIndex1 == -1){
                    int r = getRandomNumberInRange(0, solution.solution.length -1);
                    if(solution.solution[r].size() != 0){
                        routeIndex1 = r;
                    }
                }
                int geneIndex1 = getRandomNumberInRange(0, solution.solution[routeIndex1].size() -1);
                int geneIndex2 = getRandomNumberInRange(geneIndex1, solution.solution[routeIndex1].size() - 1);

                ArrayList<Integer> shuffleList = new ArrayList<>();
                for(int i = 0; i < geneIndex2 - geneIndex1; i++){
                    shuffleList.add(solution.solution[routeIndex1].get(geneIndex1));
                    solution.solution[routeIndex1].remove(geneIndex1);

                }
                Collections.shuffle(shuffleList);
                for(int i = 0; i < shuffleList.size(); i++){
                    solution.solution[routeIndex1].add(geneIndex1, shuffleList.get(i));
                }





            }
            else if(mutationChoiceIndex == 6){
                //INVERSION
                int routeIndex1 = -1;
                //only choose a route that is not empty
                while (routeIndex1 == -1){
                    int r = getRandomNumberInRange(0, solution.solution.length -1);
                    if(solution.solution[r].size() != 0){
                        routeIndex1 = r;
                    }
                }
                int geneIndex1 = getRandomNumberInRange(0, solution.solution[routeIndex1].size() -1);
                int geneIndex2 = getRandomNumberInRange(geneIndex1, solution.solution[routeIndex1].size() - 1);

                ArrayList<Integer> shuffleList = new ArrayList<>();
                for(int i = 0; i < geneIndex2 - geneIndex1; i++){
                    shuffleList.add(solution.solution[routeIndex1].get(geneIndex1));
                    solution.solution[routeIndex1].remove(geneIndex1);

                }

                for(int i = shuffleList.size() - 1; i > -1; i--){
                    solution.solution[routeIndex1].add(geneIndex1, shuffleList.get(i));
                }


            }
            else if(mutationChoiceIndex == 5){
                //rowshifter
                int numSwitches = getRandomNumberInRange(0, Math.round(solution.solution.length /2));
                for (int i = 0; i < numSwitches; i++){
                    int index1 = getRandomNumberInRange(0,solution.solution.length - 1);
                    int index2 = getRandomNumberInRange(0,solution.solution.length - 1);
                    ArrayList<Integer> one = solution.solution[index1];
                    ArrayList<Integer> two = solution.solution[index2];
                    solution.solution[index2] = one;
                    solution.solution[index1] = two;
                }

            }

        }
        return solution;
    }




    public Solution crossover(Solution mother, Solution father){

        int crossoverChoiceIndex = getRandomNumberInRange(0, 1);

        Solution child = new Solution(mother.numDepots, mother.numVehiclesPerDepot, mother.numCustomers, mother.depots, mother.customers, mother.heuristicInitChance, false);

        //make empty solution for child and copy mothers solution
        child.solution = new ArrayList[child.solution.length];
        for(int i = 0; i < mother.solution.length; i++){
            child.solution[i] = new ArrayList<Integer>();
            for(int k = 0; k < mother.solution[i].size(); k++){
                child.solution[i].add(mother.solution[i].get(k));
            }
        }

        Random rand = new Random();

        if(rand.nextFloat() > crossOverrate){


            return child;
        }



        if(crossoverChoiceIndex == 0){


            //TODO: keep the best row from parent only - need a "getfitness of row" function to compare
            int depotIndex = 0;
            for (int i = 0; i < child.solution.length; i++){
                if(i != 0 && i%vehiclesPerDepot == 0){
                    depotIndex += 1;
                }
                if(rand.nextFloat() < crossOverFactor){


                    if(child.solution[i].size() == 0 || father.solution[i].size() == 0 ){
                        if(rand.nextFloat() > 0.5){
                            continue;
                        }

                    }


                    if(child.getFitnessOfRoute(father.solution[i], i, depotIndex) < child.getFitnessOfRoute(child.solution[i], i, depotIndex)){

                        //for every element in the part we are replacing
                        for(int k = 0; k < child.solution[i].size(); k++){
                            //if that element is not in the part we get from the father

                            if(!father.solution[i].contains(child.solution[i].get(k))){
                                // add that element somewhere else in the solution
                                int index = i;
                                while(index == i){
                                    index = getRandomNumberInRange(0, child.solution.length - 1);
                                }

                                child.putInBestPlace(child.solution[i].get(k), child.solution[index], i, depotIndex);

                                //just adding that element to the end(?) of the list right now
                                //child.solution[index].add(child.solution[i].get(k));
                            }

                        }

                        //for every element we are receiving from the father-part
                        for(int k = 0; k < father.solution[i].size(); k++){

                            for(int j = 0; j < child.solution.length; j++){

                                //dont check same gene
                                if(j == i){
                                    continue;
                                }
                                //if that element is present somewhere else in the solution
                                if(child.solution[j].contains(father.solution[i].get(k))){
                                    //remove it.
                                    child.solution[j].remove(child.solution[j].indexOf(father.solution[i].get(k)));
                                }

                            }

                        }
                        //set child solution-part equal to that of father
                        child.solution[i] = new ArrayList<>();
                        for(int x = 0; x < father.solution[i].size(); x++){
                            child.solution[i].add(father.solution[i].get(x));
                        }

                    }

                }
            }
        }
        //SAME as the one above, but looping routes from the other direction
        else if(crossoverChoiceIndex == 1){

                int depotIndex = numDepots-1;
                //TODO: make this into a variable
                for (int i = child.solution.length - 1; i > -1; i--){
                    if(i != child.solution.length - 1 && i%vehiclesPerDepot == 0){
                        if(i != 0){
                            depotIndex -= 1;
                        }

                    }
                    if(rand.nextFloat() < crossOverFactor){

                        if(child.solution[i].size() == 0 || father.solution[i].size() == 0 ){
                            if(rand.nextFloat() > 0.5){
                                continue;
                            }

                        }


                        if(child.getFitnessOfRoute(father.solution[i], i, depotIndex) < child.getFitnessOfRoute(child.solution[i], i, depotIndex)){

                            //for every element in the part we are replacing
                            for(int k = 0; k < child.solution[i].size(); k++){
                                //if that element is not in the part we get from the father

                                if(!father.solution[i].contains(child.solution[i].get(k))){
                                    // add that element somewhere else in the solution
                                    int index = i;
                                    while(index == i){
                                        index = getRandomNumberInRange(0, child.solution.length - 1);
                                    }

                                    child.putInBestPlace(child.solution[i].get(k), child.solution[index], i, depotIndex);


                                    //just adding that element to the end(?) of the list right now
                                    //child.solution[index].add(child.solution[i].get(k));
                                }

                            }

                            //for every element we are receiving from the father-part
                            for(int k = 0; k < father.solution[i].size(); k++){

                                for(int j = 0; j < child.solution.length; j++){

                                    //dont check same gene
                                    if(j == i){
                                        continue;
                                    }
                                    //if that element is present somewhere else in the solution
                                    if(child.solution[j].contains(father.solution[i].get(k))){
                                        //remove it.
                                        child.solution[j].remove(child.solution[j].indexOf(father.solution[i].get(k)));
                                    }

                                }

                            }
                            //set child solution-part equal to that of father
                            child.solution[i] = new ArrayList<>();
                            for(int x = 0; x < father.solution[i].size(); x++){
                                child.solution[i].add(father.solution[i].get(x));
                            }

                        }




                    }
                }
        }

        // EVEN OUT CUSTOMER_NUM
        else if(crossoverChoiceIndex == 2){


            int numAdjustments = getRandomNumberInRange(0, child.solution.length - 1);
            ArrayList<Integer> routeLengthList = new ArrayList<>();
            for(int i = 0; i < child.solution.length; i ++){
                routeLengthList.add(i, child.solution[i].size());
            }
            for(int i = 0; i < numAdjustments; i++){
                int longestRouteIndex = routeLengthList.indexOf(Collections.max(routeLengthList));
                routeLengthList.set(routeLengthList.indexOf(Collections.max(routeLengthList)), Collections.max(routeLengthList) - 1);

                int geneIndex = getRandomNumberInRange(0, child.solution[longestRouteIndex].size() - 1);
                int gene = child.solution[longestRouteIndex].get(geneIndex);
                child.solution[longestRouteIndex].remove(geneIndex);

                int insertRouteIndex = getRandomNumberInRange(0, child.solution.length - 1);
                child.solution[insertRouteIndex].add(gene);


            }




        }








        return child;

    }




    private static int getRandomNumberInRange(int min, int max) {

        if (min >= max) {
            return min;
            //throw new IllegalArgumentException("max must be greater than min");
        }

        Random r = new Random();
        return r.nextInt((max - min) + 1) + min;
    }




    public ArrayList<Solution> determineTheElite(){
        Collections.sort(population, Comparator.comparingDouble(Solution :: setFitnessAndCost));
        ArrayList<Solution> elites = new ArrayList<>();
        for(int i = 0; i < this.elitismCount; i++){
            elites.add(population.get(i));
        }
        return elites;
    }


    public void preEvolution(int numGenerations){

        Population one = new Population(this.populationSize, this.vehiclesPerDepot, this.numCustomers, this.numDepots,
                this.depots, this.customers, this.tournamentSize, this.elitismPercentage, this.mutationRate, this.crossOverFactor,
                this.targetCost, this.crossOverrate, this.heuristicInitChance, 1000*60*60, this.interventionRate);
        Population two = new Population(this.populationSize, this.vehiclesPerDepot, this.numCustomers, this.numDepots,
                this.depots, this.customers, this.tournamentSize, this.elitismPercentage, this.mutationRate, this.crossOverFactor,
                this.targetCost, this.crossOverrate, this.heuristicInitChance, 1000*60*60, this.interventionRate);
        Population three = new Population(this.populationSize, this.vehiclesPerDepot, this.numCustomers, this.numDepots,
                this.depots, this.customers, this.tournamentSize, this.elitismPercentage, this.mutationRate, this.crossOverFactor,
                this.targetCost, this.crossOverrate, this.heuristicInitChance, 1000*60*60, this.interventionRate);

        Population four = new Population(this.populationSize, this.vehiclesPerDepot, this.numCustomers, this.numDepots,
                this.depots, this.customers, this.tournamentSize, this.elitismPercentage, this.mutationRate, this.crossOverFactor,
                this.targetCost, this.crossOverrate, this.heuristicInitChance, 1000*60*60, this.interventionRate);

        Population five = new Population(this.populationSize, this.vehiclesPerDepot, this.numCustomers, this.numDepots,
                this.depots, this.customers, this.tournamentSize, this.elitismPercentage, this.mutationRate, this.crossOverFactor,
                this.targetCost, this.crossOverrate, this.heuristicInitChance, 1000*60*60, this.interventionRate);

        Population six = new Population(this.populationSize, this.vehiclesPerDepot, this.numCustomers, this.numDepots,
                this.depots, this.customers, this.tournamentSize, this.elitismPercentage, this.mutationRate, this.crossOverFactor,
                this.targetCost, this.crossOverrate, this.heuristicInitChance, 1000*60*60, this.interventionRate);

        Population seven = new Population(this.populationSize, this.vehiclesPerDepot, this.numCustomers, this.numDepots,
                this.depots, this.customers, this.tournamentSize, this.elitismPercentage, this.mutationRate, this.crossOverFactor,
                this.targetCost, this.crossOverrate, this.heuristicInitChance, 1000*60*60, this.interventionRate);

        ArrayList<Solution> aOne = one.ancestors(numGenerations);
        ArrayList<Solution> aTwo = two.ancestors(numGenerations);
        ArrayList<Solution> aThree = three.ancestors(numGenerations);
        ArrayList<Solution> aFour = four.ancestors(numGenerations);
        ArrayList<Solution> aFive = five.ancestors(numGenerations);
        ArrayList<Solution> aSix = six.ancestors(numGenerations);
        ArrayList<Solution> aSeven = seven.ancestors(numGenerations);

        System.out.println(aOne.size());
        System.out.println(aTwo.size());
        System.out.println(aThree.size());
        int num = aOne.size();
        for(int i = 0; i < num; i++){
            aOne.add(aTwo.get(i));
            aOne.add(aThree.get(i));
            aOne.add(aFour.get(i));
            aOne.add(aFive.get(i));
            aOne.add(aSix.get(i));
            aOne.add(aSeven.get(i));
        }

        Collections.sort(aOne, Comparator.comparingDouble(Solution :: setFitnessAndCost));
        ArrayList<Solution> best = new ArrayList<>();
        for(int i = 0; i < this.populationSize; i++){
            best.add(aOne.get(i));
        }
        this.population = best;

    }

    public ArrayList<Solution> ancestors(int numGenerations){

        // initialize  population with random candidate solutions -INITIALIZED IN CONSTRUCTOR

        //evaluate each candidate

        int gen = 0;
        while(gen < numGenerations){

            //System.out.println("\n\n ANCESTRY " + gen + ":");
            //System.out.println("````````````````````````````````");


            // find the 10-15% best individuals and store (elitism)
            ArrayList<Solution> elites = determineTheElite();

            // make as many children as you need to

            // select parents
            ArrayList<Solution> parents = tournamentSelection(populationSize - elitismCount);
            //System.out.println(parents.size() + " parents selected!");

            // recombine pairs of parents
            ArrayList<Solution> offSpring = reproduce(parents);
            //System.out.println(offSpring.size() + " offspring produced!");

            // mutate the resulting offspring
            offSpring = mutate(offSpring);
            //System.out.println("----  mutation -----");






            // evaluate new candidates
            // select individuals for the next generation
            ArrayList<Solution> survivalOFTheFittest = new ArrayList<>();

            for(int i = 0; i < elites.size(); i ++){
                survivalOFTheFittest.add(elites.get(i));
            }
            for(int i = 0; i < populationSize - elites.size(); i ++){
                survivalOFTheFittest.add(offSpring.get(i));
            }
            //System.out.println(" combine offspring with population :  NEW population size: " + survivalOFTheFittest.size());

            //ArrayList<Solution> theFittest = new ArrayList<>();
            //theFittest = survivalTournament(survivalOFTheFittest);
            //System.out.println("After survival selection: Survivors into next generation are:" + theFittest.size());



            population = survivalOFTheFittest;


            gen += 1;
        }
        // return best solution



        return population;
    }



    //TODO:  EVOLUTION
    public Solution evolution(int numGenerations){

        // initialize  population with random candidate solutions -INITIALIZED IN CONSTRUCTOR

        //evaluate each candidate
        Solution theBest = population.get(0);
        int gen = 0;
        boolean targetReached = false;
        long start = System.currentTimeMillis();
        double previousBestCost = 0;
        int rutCounter = 0;

        while(gen < numGenerations && !targetReached){






            // find the 10-15% best individuals and store (elitism)
            ArrayList<Solution> elites = determineTheElite();

            // make as many children as you need to

            // select parents
            ArrayList<Solution> parents = tournamentSelection(populationSize - elitismCount);
            //System.out.println(parents.size() + " parents selected!");

            // recombine pairs of parents
            ArrayList<Solution> offSpring = reproduce(parents);
            //System.out.println(offSpring.size() + " offspring produced!");

            // mutate the resulting offspring
            offSpring = mutate(offSpring);
            //System.out.println("----  mutation -----");



            //TODO: PRODUCE TWICE AS MANY OFFSPRING, and select the best

            // select parents
            ArrayList<Solution> parents2 = tournamentSelection(populationSize - elitismCount);
            // recombine pairs of parents
            ArrayList<Solution> offSpring2 = reproduce(parents);
            // mutate the resulting offspring
            offSpring2 = mutate(offSpring2);

            ArrayList<Solution> offSpring3 = new ArrayList<>();
            for(int i = 0; i < offSpring.size(); i++){
                offSpring3.add(offSpring.get(i));
                offSpring3.add(offSpring2.get(i));
            }
            Collections.sort(offSpring3, Comparator.comparingDouble(Solution :: setFitnessAndCost));

            //TODO: PRODUCE TWICE AS MANY OFFSPRING, and select the best


            // evaluate new candidates
            // select individuals for the next generation
            ArrayList<Solution> survivalOFTheFittest = new ArrayList<>();

            for(int i = 0; i < elites.size(); i ++){
                survivalOFTheFittest.add(elites.get(i));
            }
            for(int i = 0; i < populationSize - elites.size(); i ++){
                //survivalOFTheFittest.add(offSpring3.get(i));
                survivalOFTheFittest.add(offSpring3.get(i));
            }
            //System.out.println(" combine offspring with population :  NEW population size: " + survivalOFTheFittest.size());

            //ArrayList<Solution> theFittest = new ArrayList<>();
            //theFittest = survivalTournament(survivalOFTheFittest);
            //System.out.println("After survival selection: Survivors into next generation are:" + theFittest.size());



            population = survivalOFTheFittest;

            Solution theFittestOFAGeneration = getFittestOfAGeneration();

            theBest = theFittestOFAGeneration;
            bestCost = theBest.cost;

            if(previousBestCost == bestCost){
                rutCounter += 1;
            }
            else{
                rutCounter = 0;
                previousBestCost = bestCost;
            }

            if(rutCounter > this.interventionRate){
                System.out.println("\n .. \n");
                theBest = bless(theBest);
                previousBestCost = theBest.cost;
                rutCounter = 0;
            }

            if(gen%100 == 0){
                System.out.println("\n\n GENERATION " + gen + ":");
                System.out.println("````````````````````````````````");
                System.out.println("   BEST COST :" + theFittestOFAGeneration.cost);
                System.out.println("BEST FITNESS : " + theFittestOFAGeneration.fitness);
            }


            //if termination condition:
                //BREAK

            gen += 1;
            if(bestCost < targetCost && theBest.solutionIsValid(theBest)){
                targetReached = true;
            }
            else if(bestCost < targetCost){
                Solution alt = searchForOtherValidSolution();
                if (alt != null){
                    theBest = alt;
                    bestCost = theBest.cost;
                    System.out.println("\n\n\n\n  FOUND ALT \n\n\n\n");
                    break;
                }
            }
            if(System.currentTimeMillis() - start > this.maxRunTime){
                System.out.println("\n\n TIMEOUT  \n\n");
                break;
            }
            Collections.shuffle(population);

        }

        //if best fitness solution at termination is not valid - search for the best valid one
        if(!theBest.solutionIsValid(theBest)){
            Collections.sort(population, Comparator.comparingDouble(Solution :: setFitnessAndCost));
            for(int i = 0; i < population.size(); i++){
                if(population.get(i).solutionIsValid(population.get(i))){
                    theBest = population.get(i);
                }
            }
        }

        System.out.println("\n\n \n\n Is the solution valid?:");
        System.out.println(theBest.solutionIsValid(theBest));
        System.out.println("\n");

        // return best solution
        return theBest;
    }

    public Solution searchForOtherValidSolution(){
        Solution sol = null;
        for(int i = 0; i < population.size(); i++){
            if(population.get(i).cost < targetCost && population.get(i).solutionIsValid(population.get(i))){
                sol = population.get(i);
            }
        }
        return sol;
    }

    @Override
    public String toString() {
        return "Population{" +
                "populationSize=" + populationSize +
                ", population=" + population +
                '}';
    }





    // mutate single individual
    public Solution mutateIndividual2(Solution solution){
        //simple mutation where there i 0.01 chance that an element inserts itself somewhere else


        Random rand = new Random();

        if(rand.nextFloat() < mutationRate ){


            //TODO: ---------->              ADJUST WHICH MUTATIONS ARE POSSIBLE HERE
            // 5, 7 is all the best
            int mutationChoiceIndex = getRandomNumberInRange(5,10);
            if(bestCost < mutationChangeTarget){
                //System.out.println("\n\n\n\n\n\n\n\n\n\n\n\n\n QUANTUM KICK  \n\n\n\n\n\n\n\n\n\n\n\n\n");

                //mutationRate = 0.5f;
                mutationChoiceIndex = getRandomNumberInRange(5, 9);
                //crossOverrate = 0.3f;


            }
            else{

                // REAL mutationChoiceIndex = getRandomNumberInRange(5,9);
                mutationChoiceIndex = getRandomNumberInRange(5, 9);
            }

            if(mutationChoiceIndex == 10){

                solution = divineIntervention(solution);


            }
            else if(mutationChoiceIndex == 0){

                // original implementation of mutation - every 'gene' in the chromosome has a small chance of being placed somewhere elese in the chromosome
                for(int i = 0; i < solution.solution.length; i++){
                    for(int k = 0; k < solution.solution[i].size(); k++){
                        if(rand.nextFloat() < mutationRate){
                            int randomRouteIndex = getRandomNumberInRange(0, solution.solution.length - 1);

                            int randomGeneIndex = 0;
                            if(solution.solution[randomRouteIndex].size() > 1){
                                randomGeneIndex = getRandomNumberInRange(0, solution.solution[randomRouteIndex].size() - 1);
                            }


                            int customerID = solution.solution[i].get(k);
                            solution.solution[i].remove(k);
                            solution.solution[randomRouteIndex].add(randomGeneIndex, customerID);
                            //System.out.println("MUTATION FROM ROUTE" + i + " TO ROUTE " + randomRouteIndex);
                        }
                        solution.setFitnessAndCost();
                    }

                }


            }
            else if(mutationChoiceIndex == 9){
                //INSERT  ONE
                int routeIndex = -1;
                //only choose a route that is not empty
                while (routeIndex == -1){
                    int r = getRandomNumberInRange(0, solution.solution.length -1);
                    if(solution.solution[r].size() != 0){
                        routeIndex = r;
                    }
                }
                int geneIndex = getRandomNumberInRange(Math.round(solution.solution[routeIndex].size()/4), solution.solution[routeIndex].size() -1);
                int cid = solution.solution[routeIndex].get(geneIndex);
                solution.solution[routeIndex].remove(geneIndex);

                int routeIndex2 = getRandomNumberInRange(0, solution.solution.length -1);
                int geneIndex2 = getRandomNumberInRange(Math.round(solution.solution[routeIndex2].size()/4), solution.solution[routeIndex2].size() -1);
                solution.solution[routeIndex2].add(geneIndex2, cid);


            }
            else if(mutationChoiceIndex == 7){
                //INSERT
                int routeIndex = -1;
                //only choose a route that is not empty
                while (routeIndex == -1){
                    int r = getRandomNumberInRange(0, solution.solution.length -1);
                    if(solution.solution[r].size() != 0){
                        routeIndex = r;
                    }
                }
                int geneIndex = getRandomNumberInRange(Math.round(solution.solution[routeIndex].size()/4), solution.solution[routeIndex].size() -1);
                int geneIndexEnd = getRandomNumberInRange(geneIndex, solution.solution[routeIndex].size() - 1);

                ArrayList<Integer> list = new ArrayList<>();
                for(int i = 0; i < geneIndexEnd - geneIndex; i++){
                    list.add(solution.solution[routeIndex].get(geneIndex));
                    solution.solution[routeIndex].remove(geneIndex);

                }

                //int gene = solution.solution[routeIndex].get(geneIndex);
                //solution.solution[routeIndex].remove(geneIndex);


                int routeIndex2 = getRandomNumberInRange(0, solution.solution.length -1);
                int geneIndex2 = getRandomNumberInRange(Math.round(solution.solution[routeIndex2].size()/4), solution.solution[routeIndex2].size() -1);

                for(int i = 0; i < list.size(); i++){
                    solution.solution[routeIndex2].add(geneIndex2, list.get(i));
                }

                //solution.solution[routeIndex2].add(geneIndex2, gene);



            }
            else if(mutationChoiceIndex == 8){
                //SWAP
                int routeIndex1 = -1;
                //only choose a route that is not empty
                while (routeIndex1 == -1){
                    int r = getRandomNumberInRange(0, solution.solution.length -1);
                    if(solution.solution[r].size() != 0){
                        routeIndex1 = r;
                    }
                }
                int geneIndex1 = getRandomNumberInRange(Math.round(solution.solution[routeIndex1].size()/4), solution.solution[routeIndex1].size() -1);
                int gene1 = solution.solution[routeIndex1].get(geneIndex1);
                solution.solution[routeIndex1].remove(geneIndex1);


                int routeIndex2 = -1;
                //only choose a route that is not empty
                while (routeIndex2 == -1){
                    int r = getRandomNumberInRange(0, solution.solution.length -1);
                    if(solution.solution[r].size() != 0){
                        routeIndex2 = r;
                    }
                }
                int geneIndex2 = getRandomNumberInRange(Math.round(solution.solution[routeIndex2].size()/4), solution.solution[routeIndex2].size() -1);
                int gene2 = solution.solution[routeIndex2].get(geneIndex2);
                solution.solution[routeIndex2].remove(geneIndex2);

                solution.solution[routeIndex2].add(geneIndex2, gene1);
                solution.solution[routeIndex1].add(geneIndex1, gene2);






            }
            else if(mutationChoiceIndex == 5){
                //SCRAMBLE
                int routeIndex1 = -1;
                //only choose a route that is not empty
                while (routeIndex1 == -1){
                    int r = getRandomNumberInRange(0, solution.solution.length -1);
                    if(solution.solution[r].size() != 0){
                        routeIndex1 = r;
                    }
                }
                int geneIndex1 = getRandomNumberInRange(0, solution.solution[routeIndex1].size() -1);
                int geneIndex2 = getRandomNumberInRange(geneIndex1, solution.solution[routeIndex1].size() - 1);

                ArrayList<Integer> shuffleList = new ArrayList<>();
                for(int i = 0; i < geneIndex2 - geneIndex1; i++){
                    shuffleList.add(solution.solution[routeIndex1].get(geneIndex1));
                    solution.solution[routeIndex1].remove(geneIndex1);

                }
                Collections.shuffle(shuffleList);
                for(int i = 0; i < shuffleList.size(); i++){
                    solution.solution[routeIndex1].add(geneIndex1, shuffleList.get(i));
                }





            }
            else if(mutationChoiceIndex == 6){
                //INVERSION
                int routeIndex1 = -1;
                //only choose a route that is not empty
                while (routeIndex1 == -1){
                    int r = getRandomNumberInRange(0, solution.solution.length -1);
                    if(solution.solution[r].size() != 0){
                        routeIndex1 = r;
                    }
                }
                int geneIndex1 = getRandomNumberInRange(0, solution.solution[routeIndex1].size() -1);
                int geneIndex2 = getRandomNumberInRange(geneIndex1, solution.solution[routeIndex1].size() - 1);

                ArrayList<Integer> shuffleList = new ArrayList<>();
                for(int i = 0; i < geneIndex2 - geneIndex1; i++){
                    shuffleList.add(solution.solution[routeIndex1].get(geneIndex1));
                    solution.solution[routeIndex1].remove(geneIndex1);

                }

                for(int i = shuffleList.size() - 1; i > -1; i--){
                    solution.solution[routeIndex1].add(geneIndex1, shuffleList.get(i));
                }


            }
            else if(mutationChoiceIndex == 5){
                //rowshifter
                int numSwitches = getRandomNumberInRange(0, Math.round(solution.solution.length /2));
                for (int i = 0; i < numSwitches; i++){
                    int index1 = getRandomNumberInRange(0,solution.solution.length - 1);
                    int index2 = getRandomNumberInRange(0,solution.solution.length - 1);
                    ArrayList<Integer> one = solution.solution[index1];
                    ArrayList<Integer> two = solution.solution[index2];
                    solution.solution[index2] = one;
                    solution.solution[index1] = two;
                }

            }

        }
        return solution;
    }




}
