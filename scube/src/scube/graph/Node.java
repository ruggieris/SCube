package scube.graph;

public class Node implements Comparable<Node> {

	private int ID;
	
	public Node(int ID) {
		this.ID = ID;
	}
	
	public void setID(Integer id){
		this.ID=id;
	}
	
	public int getID() {
		return this.ID;
	}

	public int hashCode() {
		return this.ID % 9999 ; // more efficient than this.ID
	}

	public boolean equals(Object other) {
		return this.ID == ((Node)other).ID;
	}

	public int compareTo(Node other) {
		if( this.ID < other.ID )
			return -1;
		if( this.ID > other.ID )
			return 1;
		return 0;
	}

	public String toString(){
		String result="[";
		result +=	"nodeID: "	+	this.ID;
		//result += 	"- {Type: "+	this.type.toString()+"}";
		result += "]";
		return result;
	}

}
