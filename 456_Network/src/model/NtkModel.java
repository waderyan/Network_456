package model;
import java.util.*;
import java.awt.geom.AffineTransform;
import java.io.*;

import view.NtkNodeView;

import model.NtkConnection.Side;
import model.NtkModelChange.ChangeType;

/**
 * Objects of this class contain information about a network nodes and their connections.  
 */
public class NtkModel {
	
	private UUID id;
	private File file;
	private List<NtkNode> nodes;
	private List<NtkConnection> connections;
	private List<NtkModelListener> listeners;
	private FileProcessor ntkFileProcessor;
	
	private boolean saved;
	private boolean loadingMutex;

	private ActType actType;
	private int viewInstances;
	
	private AffineTransform transform;
	
	public enum ActType {
		Select,
		AddNode,
		AddConnection,
		Rotate
	}
	
	
	public NtkModel () {
		this.loadingMutex = true;
		init(new File("test/" + UUID.randomUUID().toString()));
		this.loadingMutex = false;
	}
	
	public NtkModel(File f) throws NtkException {
		try {
			this.loadingMutex = true;
			init(f);
			ntkFileProcessor.processFile(f);
			loadNodesIntoConnections();
			this.loadingMutex = false;
		} catch (Exception ex) {
			throw new NtkException(String.format("Invalid File.\nFile Name: %s", file));
		}
	}
	
	public NtkModel (NtkModel copy, File f) throws NtkException {
		this.loadingMutex = true;
		init(f);
		
		for (int i = 0; i < copy.nNodes(); i++) {
			this.nodes.add(new NtkNode(copy.getNode(i), this));
		}
		
		for (int i = 0; i < copy.nConnections(); i++) {
			this.connections.add(new NtkConnection(copy.getConnection(i), this));
		}
		
		this.loadNodesIntoConnections();
		
		this.save();
		this.loadingMutex = false;
	}
	
	private void init(File fileName) {
		setFile(fileName);
		this.id = UUID.randomUUID();
		this.actType = ActType.Select;
		this.listeners = new ArrayList<NtkModelListener>();
		this.nodes = new ArrayList<NtkNode>();
		this.connections = new ArrayList<NtkConnection>();
		this.saved = true;
		this.ntkFileProcessor = new FileProcessor(this);
		this.transform = new AffineTransform();
	}
	
	public AffineTransform getTransform() {
		return transform;
	}
	
	public void setTransform(AffineTransform t) {
		transform = t;
		this.notifyListeners(new NtkModelChange(ChangeType.TransformChanged, t, null));
	}
	
	public void damage() {
		this.saved = false;
	}
	
	public void unDamage() {
		this.saved = true;
	}
	
	public void incrementViewInstance () {
		this.viewInstances++;
	}
	
	public void decrementViewInstance () {
		this.viewInstances--;
	}
	
	public int getInstanceCnt () {
		return this.viewInstances;
	}
	
	private void loadNodesIntoConnections () {
		for (NtkConnection conn : this.connections) {
			conn.loadNodes(this.nodes);
		}
	}
	
	public File getFile() {
		return this.file;
	}
	
	public void setFile(File newFile) {
		File oldFile = null;
		if (this.file != null) {
			oldFile = new File(this.file.getAbsolutePath());
		}
		this.file = newFile;
		this.notifyListeners(new NtkModelChange(ChangeType.FileChanged, this, oldFile));
	}
	
	public UUID getId() {
		return this.id;
	}
	
	public void save() {
		try {
			this.ntkFileProcessor.save();
		} catch (Exception e) {
			System.out.println("Error while saving");
			return;
		}
		
		this.unDamage();
	}
	
	public boolean unsavedChanges() {
		return !saved;
	}

	// Manager Methods
	
	public void addNode(NtkNode newNode) {
		this.damage();
		nodes.add(newNode);
		notifyListeners(new NtkModelChange(ChangeType.NodeAdded, newNode, null));
	}
	
	public int nNodes() {
		return nodes.size();
	}
	
	public NtkNode getNode(int i) throws NtkException {
		if (i >= nNodes()) {
			throw new NtkException("node does not exist");
		}
		return nodes.get(i);
	}
	
	public void removeNode(int i) throws NtkException {
		if (i >= nNodes()) {
			throw new NtkException("node does not exist - cannot be removed");
		}
		this.damage();
		nodes.remove(i);
		notifyListeners(new NtkModelChange(ChangeType.NodeRemoved, i, null));
	}
	
	public void addConnection(NtkConnection newConnection) {
		assert (newConnection != null);
		
		this.damage();
		connections.add(newConnection);
		this.loadNodesIntoConnections();
		notifyListeners(new NtkModelChange(ChangeType.ConnectionAdded, newConnection, null));
	}
	
	public int nConnections() {
		return connections.size();
	}

	public NtkConnection getConnection(int i) throws NtkException {
		if (i >= nConnections()) {
			throw new NtkException("connection does not exist");
		}
		return connections.get(i);
	}
	
	public void removeConnection(int i) throws NtkException {
		if (i >= connections.size()) {
			throw new NtkException("connection does not exist - cannot be removed");
		}
		this.damage();
		connections.remove(i);
		this.loadNodesIntoConnections();
		notifyListeners(new NtkModelChange(ChangeType.ConnectionRemoved, i, null));
	}
	
	public void setActType (ActType type) {
		this.actType = type;
		this.notifyListeners(new NtkModelChange(ChangeType.ActTypeChange, type, null));
	}
	
	public ActType getActType () {
		return this.actType;
	}
	
	// Listener Methods
	
	public void nodeChanged(NtkModelChange change) {
		notifyListeners(change);
	}
	
	public void connectionChanged(NtkModelChange change) {
		notifyListeners(change);
	}
	
	public void addListener (NtkModelListener listener) {
		assert(listener != null);
		listeners.add(listener);
	}
	
	public void removeListener (NtkModelListener listener) {
		int toDelete = -1;
		for (int i = 0; i < listeners.size(); i++) {
			if (listener.equals(listeners.get(i))) {
				toDelete = i;
				break;
			}
		}
		if (toDelete != -1) {
			listeners.remove(toDelete);
		}
	}
	
	public Iterator<NtkModelListener> getListeners() {
		return listeners.iterator();
	}
	
	public void notifyListeners (NtkModelChange change) {
		if (this.loadingMutex) return;
		for (NtkModelListener listener : listeners) {
			listener.update(change);
		}
	}
	
	// Model-View methods
	
	public void select(AbstractNtkEntity entity, int insertionPoint) {
		this.notifyListeners(new NtkModelChange(ChangeType.Highlight, entity, null));
	}
	
	private NtkNode getNodeById (UUID id) {
		// Can put this in a map if performance is slow
		for (NtkNode n : nodes) {
			if (n.getId().equals(id)) {
				return n;
			}
		}
		return null;
	}
	
	public void backspace(NtkNodeView view, int insertionPt) {
		NtkNode node = getNodeById(view.getModel().getId());
		if (node != null && insertionPt >= 0) {
			node.setName(NtkNode.getStringWithRemovedChar(node, insertionPt));
			this.damage();
		}
	}
	
	public void insertText(NtkNodeView view, int insertionPt, char c) {	
		NtkNode node = getNodeById(view.getModel().getId());
		if (node != null) {
			node.setName(NtkNode.getStringWithInsertedChar(node, insertionPt, c));
			this.damage();
		}
	}
	
	public void move(NtkNodeView entity, double x, double y) {
		assert (entity != null);
		NtkNode node = this.getNodeById(entity.getModel().getId());
		if (node != null) {
			node.setLocation(x, y);
		}
		this.damage();
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((file == null) ? 0 : file.hashCode());
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
		NtkModel other = (NtkModel) obj;
		if (file == null) {
			if (other.file != null)
				return false;
		} else if (!file.equals(other.file))
			return false;
		return true;
	}

	/**
	* This method is a regression test to verify that this class is
	* implemented correctly. It should test all of the methods including
	* the exceptions. It should be completely self checking. This 
	* should write "testing NetworkModel" to System.out before it
	* starts and "NetworkModel OK" to System.out when the test
	* terminates correctly. Nothing else should appear on a correct
	* test. Other messages should report any errors discovered.
	**/
	public static void Test() {
		System.out.println("testing NetworkModel");
		
		try {
			File testFile = new File("test/test0");
			
			// Constructors
			NtkModel model =  new NtkModel(testFile);
			NtkConnection testConn = new NtkConnection("TestConnBob", Side.Bottom, "TestConnJoe", Side.Left, model);
			NtkNode testNode = new NtkNode("TestNodeBob", 0, 0, model);
			NtkModel emptyModel = new NtkModel();
			assertTrue("ERROR - fileName is empty when it should be random!", !emptyModel.getFile().equals(""));
			
			try {
				@SuppressWarnings("unused")
				NtkModel badFileModel = new NtkModel(new File("test/testbad"));
				assertTrue("ERROR - should not allow bad file", false);
			} catch (NtkException e) {
			}
			
			// Init methods
			assertTrue("ERROR - wrong number of connections", model.nConnections() == 1);
			assertTrue("ERROR - wrong number of nodes", model.nNodes() == 2);
			assertTrue("ERROR - wrong fileName", model.getFile().equals(testFile));
			
			// File name methods
			model.setFile(new File("test/newTestName"));
			assertTrue("ERROR - changing file name", model.getFile().equals("newTestName"));
			model.setFile(testFile);
			
			// Connection methods
			model.addConnection(testConn);
			assertTrue("ERROR - changes have not been saved after adding connection!", model.unsavedChanges());
			assertTrue("ERROR - wrong number of connections after adding one", model.nConnections() == 2);
			assertTrue("ERROR - wrong connection!", model.getConnection(1).equals(testConn));
			model.removeConnection(1);
			assertTrue("ERROR - changes have not been saved after removing connection!", model.unsavedChanges());
			assertTrue("ERROR - wrong number of connections after removing one", model.nConnections() == 1);
			assertTrue("ERROR - changes have not been saved after adding and removing connections!", model.unsavedChanges());
			
			assertTrue("incorrect side", testConn.getSide1() == Side.Bottom);
			assertTrue("incorrect side", testConn.getSide2() == Side.Left);
//			assertTrue(testConn.getNode1Name().equals("TestConnBob"));
//			assertTrue(testConn.getNode2Name().equals("TestConnJoe"));
			assertTrue(testConn.equals(testConn));
			assertTrue(!testConn.equals(new NtkConnection("bogus", Side.Right, "bogus2", Side.Left, model)));
			
			try {
				model.removeConnection(100);
				assertTrue("ERROR - should throw an exception when trying to access a connection that doesn't exist", false);
			} catch (NtkException ex) {}
			
			try {
				model.getConnection(100);
				assertTrue("ERROR - should throw an exception when trying to access a connection that doesn't exist", false);
			} catch (NtkException ex) {}
			
			model.addNode(testNode);
			assertTrue("ERROR - changes have not been saved after adding nodes!", model.unsavedChanges());
			assertTrue("ERROR - wrong number of nodes after adding one", model.nNodes() == 3);
			assertTrue("ERROR - wrong node!", model.getNode(2).equals(testNode));
			model.removeNode(2);
			assertTrue("ERROR - changes have not been saved after removing node!", model.unsavedChanges());
			assertTrue("ERROR - wrong number of nodes after removing one", model.nNodes() == 2);
			assertTrue("ERROR - changes have not been saved after adding and removing nodes!", model.unsavedChanges());
			
			assertTrue(testNode.getName().equals("TestNodeBob"));
			testNode.setName("NotTestNodeBob");
			assertTrue(testNode.getName().equals("NotTestNodeBob"));
			testNode.setName("TestNodeBob");
			assertTrue(testNode.getX() == 0 && testNode.getY() == 0);
			testNode.setLocation(200, 100);
			assertTrue(testNode.getX() == 200 && testNode.getY() == 100);
			testNode.setLocation(0, 0);
			assertTrue(testNode.getNetwork() == null);
			testNode.setNetwork(model);
			assertTrue(testNode.getNetwork() == model);
			testNode.setNetwork(null);
			
			try {
				model.removeNode(100);
				assertTrue("ERROR - should throw an exception when trying to access a node that doesn't exist", false);
			} catch (NtkException ex) {}
			
			try {
				model.getNode(100);
				assertTrue("ERROR - should throw an exception when trying to access a node that doesn't exist", false);
			} catch (NtkException ex) {}
			
			// Save methods
			model.addNode(testNode);
			model.addConnection(testConn);
			model.save();
			assertTrue("ERROR - changes were not saved correctly!", !model.unsavedChanges());
			assertTrue("ERROR - wrong number of connections after saving", model.nConnections() == 2);
			assertTrue("ERROR - wrong number of nodes after saving", model.nNodes() == 3);
			model.removeConnection(1);
			model.removeNode(2);
			model.save();
			
		} catch (NtkException e) {
			e.printStackTrace();
			System.out.println("FAILED TEST CASES!");
			return;
		}
		
		System.out.println("NetworkModel OK");
	}
	
	private static void assertTrue(String msg, boolean condition) throws NtkException {
		if (!condition) {
			System.out.println(msg);
			throw new NtkException(String.format("Failed test case. %s", msg));
		}
	}
	
	private static void assertTrue(boolean condition) throws NtkException {
		if (!condition) {
			System.out.println("ERROR!");
			throw new NtkException(String.format("Failed test case."));
		}
	}

	


	

	
}