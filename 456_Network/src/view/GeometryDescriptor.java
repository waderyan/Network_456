package view;


public class GeometryDescriptor {

	private int _index;
	private NtkEntityView _comp;
	private int _textIndex;
	
	public GeometryDescriptor(int index, NtkEntityView model) {
		_index = index;
		_comp = model;
		_textIndex = -1;
	}
	
	public void addTextIndex (int textIndex) {
		_textIndex = textIndex;
	}
	
	public int getTextIndex () {
		return _textIndex;
	}
	
	public boolean hasTextIndex () {
		return _textIndex != -1;
	}
	
	public NtkEntityView getDrawingComp () {
		return _comp;
	}
	
	public String toString () {
		if (_comp == null) {
			return String.format(NO_RESULT);
		} else if (hasTextIndex()) {
			return String.format(TEXT_RESULT, _textIndex, _index, _comp.getModel().toString());
		} else {
			return String.format(MODEL_RESULT, _index, _comp.getModel().toString());
		}
	}
	
	private final static String TEXT_RESULT = "TextIndex: %d, ModelIndex: %d, Model_Entity: %s";
	private final static String NO_RESULT = "-1";
	private final static String MODEL_RESULT = "Index: %d, Model_Entity: %s";
	
	public static GeometryDescriptor noResult () {
		return new GeometryDescriptor (-1, null);
	}
	
	
}
