import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Solution {

	private final InputStream in;
	private final PrintStream out;
	private int n;
	private int m;
	private int[] deliveryPoints;
	private Road[] roads;
	private int boyCost;
	private int avgSpeed;
	private int petrolCost;
	private int time;
	private Road[][] plan;
	private double minc;
	private Map<Integer, Road[]> routes = new HashMap<Integer, Road[]>();
	private Map<Integer, Set<Integer>> net = new HashMap<Integer, Set<Integer>>();

	public static class Road {
		private int x;
		private int y;
		private int d;
		private double p;

		@Override
		public int hashCode() {
			return x * 1000 + y;
		}

		@Override
		public boolean equals(Object o) {
			Road r = (Road) o;
			return (x == r.x) && (y == r.y);
		}
	}

	public Solution(InputStream in, PrintStream out) {
		this.in = in;
		this.out = out;
	}

	public void calcPlan() {
		minc = Double.MAX_VALUE;
		init();
		for (int numBoys = 1; numBoys <= m; numBoys++) {
			List<Road[]> planList = new ArrayList<Road[]>();
			Set<Integer> dps = new HashSet<Integer>();
			for (int i = 0; i < deliveryPoints.length; i++) {
				dps.add(deliveryPoints[i]);
			}
			calcPlan(numBoys, dps, planList);
		}
	}

	private void init() {
		for (int i = 0; i < roads.length; i++) {
			Road road = roads[i];
			addRoutes(road);
			addNeighbour(road.x, road.y);
			addNeighbour(road.y, road.x);
		}
	}

	private void addRoutes(Road road) {
		routes.put(road.x * 1000 + road.y, new Road[] { road });
		routes.put(road.y * 1000 + road.x, new Road[] { road });
	}

	private void addNeighbour(int x, int y) {
		Set<Integer> neighbours = net.get(x);
		if (neighbours == null) {
			neighbours = new HashSet<Integer>();
			net.put(x, neighbours);
		}
		neighbours.add(y);
	}

	private void calcPlan(int numBoys, Set<Integer> dps, List<Road[]> planList) {
		if (numBoys > 1) {
			Set<Set<Integer>> sets = powerSet(dps);
			for (Set<Integer> bdps : sets) {
				if (!bdps.isEmpty()) {
					Set<Integer> rdps = new HashSet<Integer>(dps);
					rdps.removeAll(bdps);
					planList.add(calcBoyPlan(bdps));
					calcPlan(numBoys - 1, rdps, planList);
					planList.remove(planList.size() - 1);
				}
			}
		} else {
			planList.add(calcBoyPlan(dps));
			Road[][] planx = planList.toArray(new Road[][] {});
			double t = getPlanTime(planx);
			if (t < time) {
				double c = getPlanCost(planx);
				if (c < minc) {
					minc = c;
					plan = planx;
				}
			}
			planList.remove(planList.size() - 1);
		}
	}

	public static Set<Set<Integer>> powerSet(Set<Integer> originalSet) {
		Set<Set<Integer>> sets = new HashSet<Set<Integer>>();
		if (originalSet.isEmpty()) {
			sets.add(new HashSet<Integer>());
			return sets;
		}
		List<Integer> list = new ArrayList<Integer>(originalSet);
		Integer head = list.get(0);
		Set<Integer> rest = new HashSet<Integer>(list.subList(1, list.size()));
		for (Set<Integer> set : powerSet(rest)) {
			Set<Integer> newSet = new HashSet<Integer>();
			newSet.add(head);
			newSet.addAll(set);
			sets.add(newSet);
			sets.add(set);
		}
		return sets;
	}
	
	private Road[] calcBoyPlan(Set<Integer> dps) {
		List<Road> roads = new ArrayList<Road>();
		int current = 0;
		for (Integer next : dps) {
			roads.addAll(Arrays.asList(calcRoute(current, next, new HashSet<Integer>())));
			current = next;
		}
		return roads.toArray(new Road[] {});
	}

	private Road[] calcRoute(int a, int b, Set<Integer> traversed) {
		Road[] roads = routes.get(a * 1000 + b);
		if (roads != null) {
			return roads;
		} else {
			traversed.add(a);
			for (int c : net.get(a)) {
				if (!traversed.contains(c)) {
					Road[] roadsx = calcRoute(c, b, traversed);
					if (roadsx != null) {
						List<Road> roadsList = new ArrayList<Road>();
						roadsList.add(routes.get(a * 1000 + c)[0]);
						roadsList.addAll(Arrays.asList(roadsx));
						roads = roadsList.toArray(new Road[] {});
						routes.put(a * 1000 + b, roads);
						routes.put(b * 1000 + a, reverse(roads));
						return roads;
					}
				}
			}
			return null;
		}
	}

	private Road[] reverse(Road[] roads) {
		Road[] result = new Road[roads.length];
		for (int i = 0; i < roads.length; i++)
			result[roads.length - i - 1] = roads[i];
		return result;
	}

	public double getPlanTime() {
		return getPlanTime(plan);
	}

	public double getPlanCost() {
		return getPlanCost(plan);
	}

	private double getPlanTime(Road[][] plan) {
		double result = 0;
		for (int i = 0; i < plan.length; i++) {
			double time = 0;
			for (int j = 0; j < plan[i].length; j++) {
				Road road = plan[i][j];
				time += (double) road.d / ((double) avgSpeed * road.p) * 60;
			}
			result = Math.max(result, time);
		}
		return result;
	}

	private double getPlanCost(Road[][] plan) {
		double result = 0;
		for (int i = 0; i < plan.length; i++) {
			result += boyCost;
			for (int j = 0; j < plan[i].length; j++) {
				Road road = plan[i][j];
				result += (double) road.d * (double) petrolCost;
			}
		}
		return result;
	}

	public void readInput() throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		String line = reader.readLine();
		String[] numbers = line.split(" ");
		n = Integer.parseInt(numbers[0]);
		m = Integer.parseInt(numbers[1]);
		deliveryPoints = new int[m];
		line = reader.readLine();
		numbers = line.split(" ");
		for (int i = 0; i < m; i++) {
			deliveryPoints[i] = Integer.parseInt(numbers[i]);
		}
		roads = new Road[n - 1];
		for (int i = 0; i < n - 1; i++) {
			line = reader.readLine();
			numbers = line.split(" ");
			roads[i] = new Road();
			roads[i].x = Integer.parseInt(numbers[0]);
			roads[i].y = Integer.parseInt(numbers[1]);
			roads[i].d = Integer.parseInt(numbers[2]);
			roads[i].p = Double.parseDouble(numbers[3]);
		}
		line = reader.readLine();
		numbers = line.split(" ");
		boyCost = Integer.parseInt(numbers[0]);
		avgSpeed = Integer.parseInt(numbers[1]);
		petrolCost = Integer.parseInt(numbers[2]);
		time = Integer.parseInt(numbers[3]);
	}

	public void printInput() {
		out.println("" + n + " " + m);
		for (int i = 0; i < m; i++) {
			out.print("" + deliveryPoints[i] + " ");
		}
		out.println();
		for (int i = 0; i < roads.length; i++) {
			out.println("" + roads[i].x + " " + roads[i].y + " " + roads[i].d + " " + roads[i].p);
		}
		out.println("" + boyCost + " " + avgSpeed + " " + petrolCost + " " + time);
	}

	public void printOutput() {
		for (int i = 0; i < plan.length; i++) {
			out.println(plan[i].length);
			Set<Road> travelled = new HashSet<Road>();
			for (int j = 0; j < plan[i].length; j++) {
				Road road = plan[i][j];
				if (!travelled.contains(road)) {
					out.println("" + road.x + " " + road.y);
					travelled.add(road);
				} else {
					out.println("" + road.y + " " + road.x);
					travelled.remove(road);
				}
			}
		}
	}

	public static void main(String args[]) throws Exception {
		Solution sol = new Solution(System.in, System.out);
		sol.readInput();
		sol.calcPlan();
		sol.printOutput();
	}

}
