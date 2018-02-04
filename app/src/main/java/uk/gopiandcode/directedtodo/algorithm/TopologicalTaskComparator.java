package uk.gopiandcode.directedtodo.algorithm;


import java.util.AbstractQueue;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Stack;
import java.util.stream.Collectors;

import uk.gopiandcode.directedtodo.data.TaskListModel;
import uk.gopiandcode.directedtodo.data.TaskModel;

public class TopologicalTaskComparator implements Comparator<TaskModel> {
    HashMap<TaskModel, Integer> rank;

    public TopologicalTaskComparator(TaskListModel model) {
        List<TaskModel> tasks = model.getTasks();

        HashSet<TaskModel> seen = new HashSet<>();
        HashMap<TaskModel, Integer> ranking = new HashMap<>();

        Queue<TaskModel> toBeProcessed = new PriorityQueue<>();
        toBeProcessed.addAll(tasks);

        toBeProcessed.removeIf(taskModel -> taskModel.getDependants().size() > 0);
        int added = 0;
        for(TaskModel root : toBeProcessed) {
           ranking.put(root, added++);
        }

        while(!toBeProcessed.isEmpty()) {
            TaskModel n = toBeProcessed.remove();
            seen.add(n);
            List<TaskModel> collect = tasks.stream()

                    .filter(taskModel -> taskModel.getDependants().contains(n))


                    .filter(taskModel ->
                            // for each task
                            !taskModel
                                    .getDependants()
                                    .stream()
                                    .filter(dependant ->
                                            // find dependants for the task which we have not seen
                                            !seen.contains(dependant))
                                    // and if there are any, then ignore it
                                    .findFirst().isPresent())

                    .collect(Collectors.toList());
            for (TaskModel taskModel : collect) {
                ranking.put(taskModel, added++);
                toBeProcessed.add(taskModel);
            }
        }

        this.rank = ranking;
    }

    @Override
    public int compare(TaskModel taskModel, TaskModel t1) {
        return rank.get(taskModel).compareTo(rank.get(t1));
    }
}
