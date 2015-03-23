package JVMBEK;

import java.util.List;
import java.util.ArrayList;

import org.jfree.data.gantt.TaskSeries;

/**
 * A Representation of the critical path of a TaskSeries
 * @author Chris Katz (ckatz2009@gmail.com)
 */
public class CriticalPath {
	
	/** The root node of the critical path */
	private CriticalPathNode root;
	
	/** The TaskSeries object that this critical path refers to */
	private TaskSeries taskseries;
	
	/** The List of nodes in the dependency graph */
	private List<CriticalPathNode> nodes;
	
	/** The List of edges in the dependency graph */
	private List<CriticalPathEdge> edges;
	
	/** The List of edges that make up the critical path of the TaskSeries */
	private List<CriticalPathEdge> criticalpath;
	
	/**
	 * Creates a new CriticalPath
	 * 
	 * @param series The TaskSeries object whose critical path needs to be calculated
	 */
	public CriticalPath(TaskSeries series)
	{
		root = new CriticalPathNode(0);
		taskseries = series;
		
		nodes = new ArrayList<CriticalPathNode>();
		nodes.add(root);
		edges = new ArrayList<CriticalPathEdge>();
		criticalpath = new ArrayList<CriticalPathEdge>();
	}
	
	/**
	 * External method to start the critical path calculation
	 */
	public void createPath()
	{
		CreatePath();
	}
	
	/** 
	 * Internal method to begin critical path calculation 
	 */
	private void CreatePath()
	{
		CreateEdges();
		CreateRoot();
		CreateRestOfPath();
		CalculateEarliestStartDate();
		CalculateLatestStartDate();
	}
	
	/**
	 * Populates the dependency graph, putting a Task onto each edge in the graph
	 */
	private void CreateEdges()
	{
		List tasks = taskseries.getTasks();
		
		for(Object t: tasks)
		{
			Task task = (Task)t;
			CriticalPathEdge edge =  new CriticalPathEdge(task);
			edges.add(edge);
		}
		
	}
	
	/** 
	 * Determines which Task is the root
	 */
	private void CreateRoot()
	{
		for(CriticalPathEdge edge: edges)
		{
			if(edge.getTask().getPredecessorCount() == 0)
			{
				edge.setPrevious(root);
				root.addNext(edge);
			}
		}
	}
	
	/**
	 * Cycles through the rest of the edges in the graph and connects all dependent
	 * edges by either placing a new node in between them or connecting them via an existing node
	 * 
	 * 
	 */
	private void CreateRestOfPath()
	{
		CreateRoot();
		
		int nodecounter = 1;
		
		for(CriticalPathEdge edge: edges)
		{
			Task task = edge.getTask();
			if(task.getPredecessorCount() > 0)
			{
				List predecessors = task.getPredecessors();
				for(Object next: predecessors)
				{
					Task predecessor = (Task)next;
					for(CriticalPathEdge predecessoredge: edges)
					{
						if(predecessoredge.getTask() == predecessor)
						{
							// only create a new node if neither has a connection
							if(predecessoredge.getNext() == null && edge.getPrevious() == null)
							{
								CriticalPathNode nodebetween = new CriticalPathNode(nodecounter);
								nodebetween.addNext(edge);
								nodebetween.addPrevious(predecessoredge);
								edge.setPrevious(nodebetween);
								predecessoredge.setNext(nodebetween);
								nodes.add(nodebetween);
								nodecounter++;
							}
							/*
							 *  if the second edge already has a beginning node, connect the first edge
								to the second edge via the existing node
							 */
							else if(predecessoredge.getNext() == null && edge.getPrevious() != null)
							{
								CriticalPathNode nodebetween = edge.getPrevious();
								nodebetween.addPrevious(predecessoredge);
								predecessoredge.setNext(nodebetween);
							}
							
							/*
							 * if the first edge already has an ending node, connect the first edge to the second
							 * edge via the existing node
							 */
							else if(predecessoredge.getNext() != null && edge.getPrevious() == null)
							{
								CriticalPathNode nodebetween = predecessoredge.getNext();
								nodebetween.addNext(edge);
								edge.setPrevious(nodebetween);
							}
						}
					}
				}
			}
		}
		
		/*
		 * Now we have to deal with all of the edges that end on the final node
		 * by creating the end node and setting all the correct connections
		 */
		CriticalPathNode end = new CriticalPathNode(nodecounter);
		nodes.add(end);
		
		for(CriticalPathEdge edge: edges)
		{
			if(edge.getNext() == null)
			{
				edge.setNext(end);
				end.addPrevious(edge);
			}
		}
	}
	
	/**
	 * Calculates the earliest starting date of each node
	 * 
	 * Earliest start date is determined by adding the earliest start date
	 * of any preceeding tasks to the duration of the current task
	 */
	private void CalculateEarliestStartDate()
	{
		root.setEarliestStartDate(0);
		
		long time = 0;
		
		for(int i = 1; i < nodes.size(); i++)
		{
			List prevs = nodes.get(i).getPrevious();
			time = 0;
			
			for(int j = 0; j < prevs.size(); j++)
			{
				CriticalPathEdge edge = (CriticalPathEdge)prevs.get(j);
				if(edge != null)
				{
					if(edge.getPrevious() == null)
					{
						time = edge.getDuration();
					}
					else
					{
						time = edge.getDuration() + edge.getPrevious().getEarliestStartDate();
					}
					if(time >= nodes.get(i).getEarliestStartDate())
					{
						nodes.get(i).setEarliestStartDate(time);
					}
				}
			}
		}
	}
	
	/**
	 * Calculates the latest possible start date for each node
	 * 
	 * Latest possible start date is calculated by taking any dependent node's latest start date
	 * and subtracting the current task's duration
	 */
	private void CalculateLatestStartDate()
	{
		long largestTime = nodes.get(nodes.size()-1).getEarliestStartDate();
		long time = 0;
		
		for(int i = nodes.size()-1; i >= 0; i--)
		{
			CriticalPathNode currentNode = nodes.get(i);
			currentNode.setLatestStartDate(largestTime);
			
			List nextEdges = currentNode.getNext();
			
			for(int j = 0; j < nextEdges.size(); j++)
			{
				CriticalPathEdge edge = (CriticalPathEdge)nextEdges.get(j);
				time = edge.getNext().getLatestStartDate() - edge.getDuration();
				if(time <= currentNode.getLatestStartDate())
				{
					currentNode.setLatestStartDate(time);
				}
			}
		}
		
		// the first node's latest start date is the beginning of the project
		root.setLatestStartDate(0);
	}
	
	/**
	 * returns the critical path Task list
	 * 
	 * A task is on the critical path if it has no float time
	 * Float is defined as the latest possible start date of the following node minus the
	 * earliest possible start date of the preceeding node minus the duration of the task
	 * 
	 * @return the list of Task objects that make up the critical path 
	 */
	public List getCriticalPath()
	{
		List criticalPath = new ArrayList();
		for(CriticalPathEdge edge: edges)
		{
			long taskfloat =  edge.getNext().getLatestStartDate()-edge.getPrevious().getEarliestStartDate()-edge.getDuration();
			if(taskfloat == 0)
			{
				criticalPath.add(edge);
			}
		}
		
		return criticalPath;
	}
}