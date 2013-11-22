package model;


import model.NtkModelChange.ChangeType;

/**
* Objects of this class describe a single node in a network.
**/
public class NtkNode extends AbstractNtkEntity {
	
	private NtkModel _model;
	private String _nodeName;
	private double _xCenter;
	private double _yCenter;
	
	public NtkNode(String nodeName, double xCenter, double yCenter, NtkModel model) {
		this._nodeName = nodeName;
		this._xCenter = xCenter;
		this._yCenter = yCenter;
		this._model = model;
	}
	
	public NtkNode(NtkNode copy, NtkModel model) {
		this._nodeName = copy._nodeName;
		this._xCenter = copy._xCenter;
		this._yCenter = copy._yCenter;
		this._model = model;
	}

	public String getName() {
		return _nodeName;
	}
	
	public void setName(String newName) {
		_nodeName = newName;
		_model.nodeChanged(new NtkModelChange(
				(newName.length() < this._nodeName.length()) 
				? ChangeType.NodeNameRemove 
				: ChangeType.NodeNameInsertion,
				this, 
				null
		));
	}
	
	public double getX() {
		return _xCenter;
	}
	
	public double getY() {
		return _yCenter;
	}
	
	public void setLocation(double xCenter, double yCenter) {
		_xCenter = xCenter;
		_yCenter = yCenter;
		_model.nodeChanged(new NtkModelChange(ChangeType.NodeLocationChanged, this, null));
	}

	public void setNetwork(NtkModel network) {
		_model = network;
	}
	
	public NtkModel getNetwork() {
		return _model;
	}
	
	public String toString() {
		return String.format("N %s %s \"%s\"", 
					String.valueOf(_xCenter), 
					String.valueOf(_yCenter), 
					_nodeName
				);
	}
	
	// Returns the new name of the node with a backspace applied.
	public static String getStringWithRemovedChar (NtkNode node, int index) {
		return new StringBuilder(node.getName()).deleteCharAt(index).toString();
	}
	
	// Returns the new name with the given char inserted into the given index.
	public static String getStringWithInsertedChar (NtkNode node, int index, char c) {
		return new StringBuilder(node.getName()).insert(index, c).toString();
	}

	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((_model == null) ? 0 : _model.hashCode());
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
		NtkNode other = (NtkNode) obj;
		if (_model == null) {
			if (other._model != null)
				return false;
		} else if (!_model.equals(other._model))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
	


	
}
