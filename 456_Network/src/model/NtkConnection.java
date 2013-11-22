package model;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import model.NtkModelChange.ChangeType;

public class NtkConnection extends AbstractNtkEntity {

	public enum Side {
		Left("L"), 
		Right("R"), 
		Top("T"), 
		Bottom("B");
		
		// Could be susceptible to class loading problems.
		private static final Map<String, Side> lookup = new HashMap<String, Side>();
		static {
			for (Side s : Side.values()) {
				lookup.put(s.getAbbreviation(), s);
			}
		}
		
		private final String _abbreviation;
		
		private Side(String abbreviation) {
			_abbreviation = abbreviation;
		}
		
		public String getAbbreviation() {
			return _abbreviation;
		}
		
		public static Side get(String abbreviation) {
			assert(lookup.containsKey(abbreviation));
			return lookup.get(abbreviation);
		}
	}
	
	private NtkNode node1;
	private NtkNode node2;
	private String node1OriginalName;
	private String node2OriginalName;
	private Side side1;
	private Side side2;
	private NtkModel model;
	private UUID id;
	
	public NtkConnection(String node1, Side side1, String node2, Side side2, NtkModel model) {
		this.node1OriginalName = node1;
		this.node2OriginalName = node2;
		this.side1 = side1;
		this.side2 = side2;
		this.model = model;
		this.id = UUID.randomUUID();
	}
	
	public NtkConnection (NtkConnection copy, NtkModel model) {
		this.node1OriginalName = copy.node1OriginalName;
		this.node2OriginalName = copy.node2OriginalName;
		this.side1 = copy.side1;
		this.side2 = copy.side2;
		this.model = model;
		this.id = UUID.randomUUID();
	}
	
	public void loadNodes (java.util.List<NtkNode> nodes) {
		for (NtkNode node : nodes) {
			if (node.getName().equals(node1OriginalName)) {
				node1 = node;
			} else if (node.getName().equals(node2OriginalName)) {
				node2 = node;
			}
		}
	}
	
	@Override
	public String toString() {
		return String.format("C \"%s\" %s \"%s\" %s", node1.getName(), side1.getAbbreviation(), node2.getName(), side2.getAbbreviation());
	}

	public NtkNode getNode1() {
		return node1;
	}

	public NtkNode getNode2() {
		return node2;
	}

	public Side getSide1() {
		return side1;
	}
	
	public void setSide1(Side side) {
		this.side1 = side;
		this.model.connectionChanged(new NtkModelChange(ChangeType.ConnectionSide1, this, null));
	}

	public Side getSide2() {
		return this.side2;
	}
	
	public void setSide2(Side side) {
		this.side2 = side;
		this.model.connectionChanged(new NtkModelChange(ChangeType.ConnectionSide2, this, null));
	}
	
	public UUID getId() {
		return this.id;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		NtkConnection other = (NtkConnection) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
	
	
}
