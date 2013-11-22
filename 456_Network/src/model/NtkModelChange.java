package model;

public class NtkModelChange {

	public enum ChangeType {
		NoChange,
		NodeAdded,
		NodeRemoved,
		ConnectionAdded,
		ConnectionRemoved,
		NodeNameInsertion,
		NodeNameRemove,
		NodeLocationChanged,
		ConnectionNode1Name,
		ConnectionNode2Name,
		ConnectionSide1,
		ConnectionSide2,
		Highlight,
		InsertionPointChange,
		Deselect,
		ActTypeChange,
		FileChanged, 
		TransformChanged
	}
	
	private final ChangeType type;
	private final Object ref;
	private final Object oldRef;
	
	public NtkModelChange (ChangeType type, Object ref, Object oldRef) {
		this.type = type;
		this.ref = ref;
		this.oldRef = oldRef;
	}
	
	public ChangeType getType () {
		return this.type;
	}
	
	public Object getRef () {
		return this.ref;
	}
	
	public Object getOldRef () {
		return this.oldRef;
	}
}
