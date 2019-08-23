package se306.algorithm;

import java.util.*;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import se306.input.InputFileReader;

public class PartialSchedule {

    // User defined available processors placed in a list
    private HashMap<Integer,Processor> processorList = new HashMap();
    private int costFunction;

    public PartialSchedule(int processorNumber) {
        createProcessors(processorNumber);
    }

    public PartialSchedule(PartialSchedule ps) {
        for (Integer i : ps.getProcessorList().keySet()) {
            Processor p = ps.getProcessorList().get(i);
            this.processorList.put(p.getProcessorID(),new Processor(p));
        }

        this.costFunction = ps.costFunction;
    }

    /**
     * Comparator to be used with resorting the processor list back into the process
     * identifier number order
     */
    private Comparator<Processor> sortByIdentifierNumber = new Comparator<Processor>() {
        public int compare(Processor p1, Processor p2) {
            if (p1.getProcessorID() < p2.getProcessorID()) {
                return -1;
            }

            return 1;
        }
    };

    public HashSet<PartialSchedule> expandNewStates() {
        HashSet<PartialSchedule> newExpandedSchedule = new HashSet<>();
        // Find how many nodes need to be scheduled for the expansion
        Set<Integer> nodes = findSchedulableNodes();
        for (Integer node : nodes) {
            // Get each node that needs to be scheduled
            for (int j = 0; j < processorList.size(); j++) {
                PartialSchedule newSchedule = new PartialSchedule(this);
                // Add it to each processor and make that many corresponding schedules
                newSchedule.addToProcessor(j, node);
                    // Add the schedule to overall expanded list
                newExpandedSchedule.add(newSchedule);
            }
        }

        return newExpandedSchedule;
    }

    /**
     * This method iterates through the list of available nodes and finds nodes in
     * which all the parents of that node have already been used into a schedule and
     * updates the list
     * 
     * @return freeNodes
     */

    private Set<Integer> findSchedulableNodes() {
        Set<Integer> freeNodes = new HashSet<>();

        // Loops through all nodes
        for (int node = 0; node < InputFileReader.NUM_NODES; node++) {
            // Checks if the node is in used nodes already
            if (!this.getUsedNodes().contains(node)) {
                System.out.println(node);
                // If no parents then add to list
                System.out.println(Arrays.toString(InputFileReader.parents[node]));
                if (!Arrays.stream(InputFileReader.parents[node]).anyMatch(i -> i == 1)) {
                    freeNodes.add(node); // AUTOBOXING
                }

                // If all parents are used add to list
                else {
                    boolean allParentsUsed = true; // Should be true even if the node has no parents

                    for (int i = 0; i < InputFileReader.NUM_NODES; i++) {
                        int parent = InputFileReader.parents[node][i];

                        if ((parent == 1) && !this.getUsedNodes().contains(i)) {
                            allParentsUsed = false;
                            break;
                        }
                    }

                    if (allParentsUsed) {
                        freeNodes.add(node);
                    }
                }
            }
        }

        return freeNodes;
    }

    /**
     * This method adds the node into the specified processor number
     *
     * @param processorNumber - the processor for the node to be added to
     * @param node            - the node to be added
     */
    private void addToProcessor(int processorNumber, Integer node) {
        // Adds the node into the corresponding processor
        this.getProcessorList().get(processorNumber).addNode(node, this, processorNumber);
    }

    /**
     * Creates processors and adds it to the list
     *
     * @param numProcessors - Number of processors to add
     */
    private void createProcessors(int numProcessors) {
        for (int i = 0; i < numProcessors; i++) {
            processorList.put(i,new Processor(i));
        }
    }

    /**
     * Method that checks that every node in listOfAvailableNodes are in used nodes
     * for this current schedule
     *
     * @return true if all nodes used or else false
     */
    boolean isComplete() {
        for (int i = 0; i < InputFileReader.NUM_NODES; i++) {
            if (!getUsedNodes().contains(i)) {
                return false;
            }
        }
        return true;
    }

    /**
     * This method finds the nodes that already have been scheduled in this schedule
     * 
     * @return scheduledNodes
     */
    public Set<Integer> getUsedNodes() {
        Set<Integer> scheduledNodes = new HashSet<>();

        for (Integer i : processorList.keySet()) {
            Processor p = processorList.get(i);
            // For each processor node map turn it into a hashSet of keys
            scheduledNodes.addAll(p.getScheduledNodes());
        }
        return scheduledNodes;
    }

    /**
     * Returns list of Processor objects that have the nodes scheduled in order of
     * the processor identifier number
     */
    public HashMap<Integer,Processor> getProcessorList() {
        return processorList;
    }

    public int getFinishTime() {
        int finishTime = 0;
        for (Integer i : processorList.keySet()) {
            if (processorList.get(i).getCurrentCost() > finishTime) {
                finishTime = processorList.get(i).getCurrentCost();
            }
        }
        return finishTime;
    }

    public int calculateCostFunction() {
        return getFinishTime();
    }

    public int getCostFunction() {
        return costFunction;
    }

    public void setCostFunction(int costFunction) {
        this.costFunction = costFunction;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        PartialSchedule secondSchedule = (PartialSchedule) obj;
        return(processorList.hashCode() == (secondSchedule.processorList.hashCode()));
        // // Check for process normalisation
        // if (!processNormalisation(secondSchedule.getProcessorList())){
        // return false;
        // }2
    }

    /**
     * hashCode() must be overridden whenever equals() is overridden
     **/
    @Override
    public int hashCode() {
        // Hash table prime numbers from https://planetmath.org/goodhashtableprimes

        return new HashCodeBuilder().append(processorList).toHashCode();
    }

    /**
     * Method calculates the start time of the current node by finding the latest
     * parent that has been scheduled, as well as the communication costs
     * 
     * @param node
     * @param processorNumber
     * @return
     */
    public int calculateStartTime(int node, int processorNumber) {
        int maxStartTime = 0;

        //Gets parents of the current node
        int[] parentNodes = InputFileReader.parents[node];
        // If no parents
        if (!Arrays.stream(parentNodes).anyMatch(i -> i == 1)) {
//            System.out.println("Node " + InputFileReader.nodeNames.get(node) + " has no parents");
            maxStartTime = processorList.get(processorNumber).getCurrentCost();
        }
        for (Integer i : processorList.keySet()) {
            Processor p = processorList.get(i);
            for (int parentID = 0; parentID < parentNodes.length; parentID++) {

                int parent = parentNodes[parentID];

                if (parent != 0) {
                    // ========================
                    // NEED TO CHECK EQUALS IN THIS METHOD AND ALL OTHER PLACES
                    // ========================

//                System.out.println("CURRENT START TIME : " + currentStartTime);

                    // If current processor contains a parent of "node" then calculate the the start
                    // time needed
                    if (p.getScheduledNodes().contains(parentID)) {
                        int currentStartTime = processorList.get(p.getProcessorID()).getCurrentCost();

                        // If parent node is not scheduled in same processor
                        if (p.getProcessorID() != processorNumber) {

                            // Find end time of the parent node
                            int endTimeOfParent = p.getStartTimes().get(p.getScheduledNodes().indexOf(parentID))
                                    + InputFileReader.nodeWeights.get(parentID);

                            // Gets communication cost of the parent
                            int communicationCost = 0; // NEED TO CHECK THIS ====================================
                            for (int[] edge : InputFileReader.listOfEdges) {
                                if ((edge[0] == parentID) && (edge[1] == node)) {
                                    communicationCost = edge[2];
                                    break;
                                }
                            }

                            // If end time of parent is longer than that means we need to schedule when
                            // parent is finished
                            // instead of right when processor is free
                            if ((endTimeOfParent >= currentStartTime)
                                    || (endTimeOfParent + communicationCost >= currentStartTime)) {
                                currentStartTime = endTimeOfParent + communicationCost;
                            }
                        }
                        // Finds the most start time as it is dependent on all parents
                        if (maxStartTime < currentStartTime) {
                            maxStartTime = currentStartTime;
                        }
                    }
                }
            }
        }
        return maxStartTime;
    }
}
