package scube.graph;

public class Edge  implements Comparable<Edge> {

	private int ID;
	private int weight;
	public Edge(int id, int weight) {
		this.ID		= id;
		this.weight = weight;
	}
	
	public Edge(int id) {
		this.ID		= id;
		this.weight = 1;
	}

	public int compareTo(Edge other) {		
		if( this.ID < other.ID )
			return -1;
		if( this.ID > other.ID )
			return 1;
		return 0;
	}

	public int hashCode() {
	    return this.ID;
	}
	
	public boolean equals(Edge other) {
		return this.ID == other.ID;
	}

	public int getID() {
		return this.ID;
	}
	
	public void setID(int id) {
		this.ID = id;
	}
	
	public void setWeight(int weight){
		this.weight = weight;
	}
	
	public int getWeight(){
		return this.weight;
	}

	public void incrementWeight(){
		this.weight++;
	}
	
	public String toString(){
		String result=	"[";
		result +=		"edgeID: "	+	this.ID;
		result += 		"; ";
		result += 		"WEIGHT: "+	this.weight;
		result += 		"]";
		return result;
	}
}
