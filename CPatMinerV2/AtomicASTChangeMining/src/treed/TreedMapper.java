package treed;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;

import org.eclipse.jdt.core.dom.ASTMatcher;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnnotationTypeMemberDeclaration;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.SynchronizedStatement;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.TypeDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;

import utils.JavaASTUtil;
import utils.Pair;
import utils.PairDescendingOrder;
import utils.StringProcessor;

public class TreedMapper implements TreedConstants {
	private ASTNode astM, astN;
	private HashMap<ASTNode, ArrayList<ASTNode>> tree = new HashMap<>();
	private HashMap<ASTNode, Integer> treeHeight = new HashMap<>(), treeDepth = new HashMap<>();
	private HashMap<ASTNode, HashMap<String, Integer>> treeVector = new HashMap<>();
	private HashMap<ASTNode, HashMap<ASTNode, Double>> treeMap = new HashMap<>();
	private HashSet<ASTNode> pivotsM = new HashSet<>(), pivotsN = new HashSet<>();
	private HashMap<String, HashMap<String, Integer>> nameMapFrequency = new HashMap<>();
	private HashMap<String, Integer> nameFrequency = new HashMap<>();
	private HashMap<String, String> renameMap = new HashMap<>();
	private int numOfChanges = 0, numOfUnmaps = 0, numOfNonNameUnMaps = 0;
	private boolean visitDocTags;
	
	public TreedMapper(ASTNode astM, ASTNode astN) {
		this.astM = astM;
		this.astN = astN;
	}
	
	public int getNumOfChanges() {
		return numOfChanges;
	}

	public int getNumOfUnmaps() {
		return numOfUnmaps;
	}

	public boolean isChanged() {
		return this.numOfChanges > 0;
	}
	
	public boolean hasUnmap() {
		return this.numOfUnmaps > 0;
	}
	
	public boolean hasNonNameUnmap() {
		return this.numOfNonNameUnMaps > 0;
	}
	
	public void map(boolean visitDocTags) {
		this.visitDocTags = visitDocTags;
		buildTrees(visitDocTags);
		mapPivots();
		mapBottomUp();
		mapMoving();
		mapTopDown();
		markChanges();
	}

	public void printChanges() {
		printChanges(astM);
		printChanges(astN);
	}

	private void printChanges(ASTNode node) {
		node.accept(new ASTVisitor(visitDocTags) {
			private int indent = 0;
			
			private void printIndent() {
				for (int i = 0; i < indent; i++)
					System.out.print("\t");
			}
			
			@Override
			public void preVisit(ASTNode node) {
				printIndent();
				int status = (int) node.getProperty(PROPERTY_STATUS);
				System.out.print(TreedUtils.buildASTLabel(node) + ": " + getStatus(status));
				ASTNode mn = (ASTNode) node.getProperty(PROPERTY_MAP);
				if (status > STATUS_UNCHANGED && mn != null)
					System.out.print(" " + TreedUtils.buildASTLabel(mn));
				System.out.println();
				indent++;
			}
			
			@Override
			public void postVisit(ASTNode node) {
				indent--;
			}
		});
	}

	public static String getStatus(int status) {
		switch (status) {
		case STATUS_MOVED:
			return "MOVED";
		case STATUS_RELABELED:
			return "RELABELED";
		case STATUS_UNCHANGED:
			return "UNCHANGED";
		case STATUS_UNMAPPED:
			return "UNMAPPED";
		}
		return "NOTHING_ELSE";
	}

	private void markChanges() {
		markChanges(astM);
		detectRenaming();
		markUnmapped(astN);
	}

	private void detectRenaming() {
		HashMap<String, HashSet<String>> imap = new HashMap<>();
		for (String nameM : nameFrequency.keySet()) {
			int f = nameFrequency.get(nameM);
			/*if (f == 1) {
				if ((int) nodeM.getProperty(PROPERTY_STATUS) == STATUS_MOVED)
					return false;
				continue;
			}*/
			String mapped = null;
			HashMap<String, Integer> mapFreq = nameMapFrequency.get(nameM);
			for (String nameN : mapFreq.keySet()) {
				if (isSameName(nameM, nameN)) {
					mapped = nameN;
					break;
				}
				int mf = nameMapFrequency.get(nameM).get(nameN);
				/*if (mf == 1)
					continue;*/
				if (mf > f * MIN_SIM)
					mapped = nameN;
			}
			if (mapped != null) {
				renameMap.put(nameM, mapped);
				HashSet<String> s = imap.get(mapped);
				if (s == null) {
					s = new HashSet<>();
					imap.put(mapped, s);
				}
				s.add(nameM);
			}
		}
		for (String nameM : new HashSet<String>(renameMap.keySet())) {
			String nameN = renameMap.get(nameM);
			if (isSameName(nameM, nameN)) {
				renameMap.remove(nameM);
				continue;
			}
			for (String iname : imap.get(nameN))
				if (isSameName(iname, nameN)) {
					renameMap.remove(nameM);
					break;
				}
		}
	}

	private boolean isSameName(String nameM, String nameN) {
		int indexM = nameM.lastIndexOf('#'), indexN = nameN.lastIndexOf('#');
		if (indexM == -1 && indexN == -1)
			return nameM.equals(nameN);
		if (indexM > -1 && indexN > -1) {
			return nameM.substring(indexM).equals(nameN.substring(indexN));
		}
		return false;
	}

	private void markUnmapped(ASTNode node) {
		if (node.getProperty(PROPERTY_STATUS) == null) {
			node.setProperty(PROPERTY_STATUS, STATUS_UNMAPPED);
			numOfChanges++;
			numOfUnmaps++;
			if (!(node instanceof SimpleName))
				numOfNonNameUnMaps++;
		} else {
			if (node instanceof SimpleName) {
				SimpleName mappedNode = (SimpleName) node.getProperty(PROPERTY_MAP);
				if (mappedNode != null && !checkNameMap(mappedNode, (SimpleName) node)) {
					node.setProperty(PROPERTY_MAP, null);
					node.setProperty(PROPERTY_STATUS, STATUS_UNMAPPED);
					mappedNode.setProperty(PROPERTY_MAP, null);
					mappedNode.setProperty(PROPERTY_STATUS, STATUS_UNMAPPED);
				}
			}
		}
		ArrayList<ASTNode> children = tree.get(node);
		for (ASTNode child : children)
			markUnmapped(child);
	}

	private void markChanges(ASTNode node) {
		HashMap<ASTNode, Double> maps = treeMap.get(node);
		if (maps.isEmpty()) {
			node.setProperty(PROPERTY_STATUS, STATUS_UNMAPPED);
			numOfChanges++;
			numOfUnmaps++;
			if (!(node instanceof SimpleName) && !(node instanceof ReturnStatement) && !(node instanceof BreakStatement) && !(node instanceof ContinueStatement))
				numOfNonNameUnMaps++;
		} else {
			ASTNode mappedNode = maps.keySet().iterator().next();
			node.setProperty(PROPERTY_MAP, mappedNode);
			mappedNode.setProperty(PROPERTY_MAP, node);
			updateNameMap(node, mappedNode);
			if (node == astM) {
				astM.setProperty(PROPERTY_STATUS, STATUS_UNCHANGED);
				astN.setProperty(PROPERTY_STATUS, STATUS_UNCHANGED);
			} else {
				ASTNode p = node.getParent(), mp = mappedNode.getParent();
				if (!treeMap.get(p).containsKey(mp)) {
					node.setProperty(PROPERTY_STATUS, STATUS_MOVED);
					mappedNode.setProperty(PROPERTY_STATUS, STATUS_MOVED);
					numOfChanges += 2;
				} else {
					if (node.getProperty(PROPERTY_STATUS) == null) {
						node.setProperty(PROPERTY_STATUS, STATUS_MOVED);
						mappedNode.setProperty(PROPERTY_STATUS, STATUS_MOVED);
						numOfChanges += 2;
					}
				}
			}
			// mark moving for children
			ArrayList<ASTNode> children = tree.get(node), mappedChildren = tree.get(mappedNode);
			if (!children.isEmpty() && !mappedChildren.isEmpty()) {
				markChanges(children, mappedChildren);
			}
		}
		ArrayList<ASTNode> children = tree.get(node);
		for (ASTNode child : children)
			markChanges(child);
	}

	private void markChanges(ArrayList<ASTNode> nodes, ArrayList<ASTNode> mappedNodes) {
		int len = nodes.size(), lenN = mappedNodes.size();
		int[][] d = new int[2][lenN + 1];
		char[][] p = new char[len + 1][lenN + 1];
		d[1][0] = 0;
		for (int j = 1; j <= lenN; j++)
			d[1][j] = 0;
		for (int i = 1; i <= len; i++) {
			ASTNode node = nodes.get(i - 1);
			HashMap<ASTNode, Double> maps = treeMap.get(node);
			for (int j = 0; j <= lenN; j++)
				d[0][j] = d[1][j];
			for (int j = 1; j <= lenN; j++) {
				ASTNode nodeN = mappedNodes.get(j - 1);
				if (maps.containsKey(nodeN)) {
					d[1][j] = d[0][j - 1] + 1;
					p[i][j] = 'D';
				} else if (d[0][j] >= d[1][j - 1]) {
					d[1][j] = d[0][j];
					p[i][j] = 'U';
				} else {
					d[1][j] = d[1][j - 1];
					p[i][j] = 'L';
				}
			}
		}
		int i = len, j = lenN;
		while (i > 0 && j > 0) {
			if (p[i][j] == 'D') {
				ASTNode node = nodes.get(i-1), node2 = mappedNodes.get(j-1);
				if (TreedUtils.buildLabelForVector(node) == TreedUtils.buildLabelForVector(node2)) {
					node.setProperty(PROPERTY_STATUS, STATUS_UNCHANGED);
					node2.setProperty(PROPERTY_STATUS, STATUS_UNCHANGED);
				} else {
					node.setProperty(PROPERTY_STATUS, STATUS_RELABELED);
					node2.setProperty(PROPERTY_STATUS, STATUS_RELABELED);
					numOfChanges += 2;
				}
				i--;
				j--;
			} else if (p[i][j] == 'U') {
				i--;
			}
			else {
				j--;
			}
		}
	}

	private void updateNameMap(ASTNode nodeM, ASTNode nodeN) {
		if (nodeM instanceof SimpleName) {
			String nameM = getNameKey((SimpleName) nodeM), nameN = getNameKey((SimpleName) nodeN);
			HashMap<String, Integer> mapFreq = nameMapFrequency.get(nameM), imapFreq = nameMapFrequency.get(nameN);
			if (mapFreq == null) {
				mapFreq = new HashMap<>();
				nameMapFrequency.put(nameM, mapFreq);
			}
			if (imapFreq == null) {
				imapFreq = new HashMap<>();
				nameMapFrequency.put(nameN, imapFreq);
			}
			Integer c = mapFreq.get(nameN);
			if (c == null)
				c = 0;
			c++;
			mapFreq.put(nameN, c);
			imapFreq.put(nameN, c);
			c = nameFrequency.get(nameM);
			if (c == null)
				c = 0;
			nameFrequency.put(nameM, c+1);
		}
	}

	private boolean checkNameMap(SimpleName nodeM, SimpleName nodeN) {
		if (nodeM.getIdentifier().equals(nodeN.getIdentifier()))
			return true;
		String nameM = getNameKey(nodeM), nameN = getNameKey(nodeN);
		return nameN.equals(renameMap.get(nameM));
	}

	private String getNameKey(SimpleName name) {
		IBinding b = name.resolveBinding();
		if (b == null)
			return name.getIdentifier();
		return b.getKey();
	}

	private void mapPivots() {
		for (ASTNode node : tree.keySet())
			treeMap.put(node, new HashMap<>());
		setMap(astM, astN, 1.0);
		ArrayList<ASTNode> lM = getChildrenContainers(astM), lN = getChildrenContainers(astN);
		ArrayList<ASTNode> heightsM = new ArrayList<>(lM), heightsN = new ArrayList<>(lN);
		Collections.sort(heightsM, new Comparator<ASTNode>() {
			@Override
			public int compare(ASTNode node1, ASTNode node2) {
				return treeHeight.get(node2) - treeHeight.get(node1);
			}
		});
		Collections.sort(heightsN, new Comparator<ASTNode>() {
			@Override
			public int compare(ASTNode node1, ASTNode node2) {
				return treeHeight.get(node2) - treeHeight.get(node1);
			}
		});
		mapPivots(lM, lN, heightsM, heightsN);
	}

	private void mapPivots(ArrayList<ASTNode> lM, ArrayList<ASTNode> lN, ArrayList<ASTNode> heightsM, ArrayList<ASTNode> heightsN) {
		ArrayList<Integer> lcsM = new ArrayList<>(), lcsN = new ArrayList<>();
		lcs(lM, lN, lcsM, lcsN);
		for (int i = lcsM.size()-1; i >= 0; i--) {
			int indexM = lcsM.get(i), indexN = lcsN.get(i);
			ASTNode nodeM = lM.get(indexM), nodeN = lN.get(indexN);
			setMap(nodeM, nodeN, 1.0);
			pivotsM.add(nodeM);
			pivotsN.add(nodeN);
			lM.remove(indexM);
			lN.remove(indexN);
			heightsM.remove(nodeM);
			heightsN.remove(nodeN);
		}
		while (!lM.isEmpty() && !lN.isEmpty()) {
			int hM = treeHeight.get(heightsM.get(0));
			int hN = treeHeight.get(heightsN.get(0));
			boolean expandedM = false, expandedN = false;
			if (hM >= hN)
				expandedM = expandForPivots(lM, heightsM, hM);
			if (hN >= hM)
				expandedN = expandForPivots(lN, heightsN, hN);
			if (expandedM || expandedN) {
				mapPivots(lM, lN, heightsM, heightsN);
				break;
			}
		}
	}

	private boolean expandForPivots(ArrayList<ASTNode> l, ArrayList<ASTNode> heights, int h) {
		HashSet<ASTNode> nodes = new HashSet<>();
		for (ASTNode node : heights) {
			if (treeHeight.get(node) == h)
				nodes.add(node);
			else
				break;
		}
		boolean expanded = false;
		for (int i = l.size()-1; i >= 0; i--) {
			ASTNode node = l.get(i);
			if (nodes.contains(node)) {
				l.remove(i);
				heights.remove(0);
				ArrayList<ASTNode> children = getChildrenContainers(node);
				if (!children.isEmpty()) {
					expanded = true;
					for (int j = 0; j < children.size(); j++) {
						ASTNode child = children.get(j);
						l.add(i+j, child);
						int index = Collections.binarySearch(heights, child, new Comparator<ASTNode>() {
							@Override
							public int compare(ASTNode node1, ASTNode node2) {
								return treeHeight.get(node2) - treeHeight.get(node1);
							}
						});
						if (index < 0)
							index = -(index + 1);
						heights.add(index, child);
					}
				}
			}
		}
		return expanded;
	}

	private boolean expandForMoving(ArrayList<ASTNode> l, ArrayList<ASTNode> heights, int h) {
		HashSet<ASTNode> nodes = new HashSet<>();
		for (ASTNode node : heights) {
			if (treeHeight.get(node) == h)
				nodes.add(node);
			else
				break;
		}
		boolean expanded = false;
		for (int i = l.size()-1; i >= 0; i--) {
			ASTNode node = l.get(i);
			if (nodes.contains(node)) {
				l.remove(i);
				heights.remove(0);
				ArrayList<ASTNode> children = getNotYetMappedDescendantContainers(node);
				if (!children.isEmpty()) {
					expanded = true;
					for (int j = 0; j < children.size(); j++) {
						ASTNode child = children.get(j);
						l.add(i+j, child);
						int index = Collections.binarySearch(heights, child, new Comparator<ASTNode>() {
							@Override
							public int compare(ASTNode node1, ASTNode node2) {
								return treeHeight.get(node2) - treeHeight.get(node1);
							}
						});
						if (index < 0)
							index = -(index + 1);
						heights.add(index, child);
					}
				}
			}
		}
		return expanded;
	}

	private void lcs(ArrayList<ASTNode> lM, ArrayList<ASTNode> lN, ArrayList<Integer> lcsM, ArrayList<Integer> lcsN) {
		int lenM = lM.size(), lenN = lN.size();
		int[][] d = new int[2][lenN + 1];
		char[][] p = new char[lenM + 1][lenN + 1];
		for (int j = 0; j <= lenN; j++)
			d[1][j] = 0;
		for (int i = lenM-1; i >= 0; i--) {
			ASTNode nodeM = lM.get(i);
			int hM = treeHeight.get(nodeM);
			HashMap<String, Integer> vM = treeVector.get(nodeM);
			for (int j = 0; j <= lenN; j++)
				d[0][j] = d[1][j];
			for (int j = lenN-1; j >= 0; j--) {
				ASTNode nodeN = lN.get(j);
				int hN = treeHeight.get(nodeN);
				HashMap<String, Integer> vN = treeVector.get(nodeN);
				if (hM == hN && nodeM.getNodeType() == nodeN.getNodeType() && vM.equals(vN) && nodeM.subtreeMatch(new ASTMatcher(false), nodeN)) {
					d[1][j] = d[0][j + 1] + 1;
					p[i][j] = 'D';
				} else if (d[0][j] >= d[1][j + 1]) {
					d[1][j] = d[0][j];
					p[i][j] = 'U';
				} else {
					d[1][j] = d[1][j + 1];
					p[i][j] = 'R';
				}
			}
		}
		int i = 0, j = 0;
		while (i < lenM && j < lenN) {
			if (p[i][j] == 'D') {
				lcsM.add(i);
				lcsN.add(j);
				i++;
				j++;
			} else if (p[i][j] == 'U')
				i++;
			else
				j++;
		}
	}

	@SuppressWarnings("unused")
	private void lss(ArrayList<ASTNode> lM, ArrayList<ASTNode> lN, ArrayList<Integer> lcsM, ArrayList<Integer> lcsN, double threshold) {
		int lenM = lM.size(), lenN = lN.size();
		double[][] d = new double[2][lenN + 1];
		char[][] p = new char[lenM + 1][lenN + 1];
		for (int j = 0; j <= lenN; j++)
			d[1][j] = 0;
		for (int i = lenM-1; i >= 0; i--) {
			ASTNode nodeM = lM.get(i);
			for (int j = 0; j <= lenN; j++)
				d[0][j] = d[1][j];
			for (int j = lenN-1; j >= 0; j--) {
				ASTNode nodeN = lN.get(j);
				double sim = computeSimilarity(nodeM, nodeN, threshold);
				if (nodeM.getNodeType() == nodeN.getNodeType() && sim >= threshold) {
					d[1][j] = d[0][j + 1] + sim;
					p[i][j] = 'D';
				} else if (d[0][j] >= d[1][j + 1]) {
					d[1][j] = d[0][j];
					p[i][j] = 'U';
				} else {
					d[1][j] = d[1][j + 1];
					p[i][j] = 'R';
				}
			}
		}
		int i = 0, j = 0;
		while (i < lenM && j < lenN) {
			if (p[i][j] == 'D') {
				lcsM.add(i);
				lcsN.add(j);
				i++;
				j++;
			} else if (p[i][j] == 'U')
				i++;
			else
				j++;
		}
	}

	private ArrayList<ASTNode> getChildrenContainers(ASTNode node) {
		ArrayList<ASTNode> children = new ArrayList<>();
		for (ASTNode child : tree.get(node)) {
			if (treeHeight.get(child) >= MIN_HEIGHT)
				children.add(child);
		}
		return children;
	}

	private void mapBottomUp() {
		ArrayList<ASTNode> heightsM = new ArrayList<>(pivotsM);
		Collections.sort(heightsM, new Comparator<ASTNode>() {
			@Override
			public int compare(ASTNode node1, ASTNode node2) {
				int d = treeHeight.get(node2) - treeHeight.get(node1);
				if (d != 0)
					return d;
				d = treeDepth.get(node1) - treeDepth.get(node2);
				if (d != 0)
					return d;
				return node1.getStartPosition() - node2.getStartPosition();
			}
		});
		for (ASTNode nodeM : heightsM) {
			ASTNode nodeN = treeMap.get(nodeM).keySet().iterator().next();
			ArrayList<ASTNode> ancestorsM = new ArrayList<>(), ancestorsN = new ArrayList<>();
			getNotYetMappedAncestors(nodeM, ancestorsM);
			getNotYetMappedAncestors(nodeN, ancestorsN);
			map(ancestorsM, ancestorsN, MIN_SIM);
		}
	}

	private ArrayList<ASTNode> map(ArrayList<ASTNode> nodesM, ArrayList<ASTNode> nodesN, double threshold) {
		HashMap<ASTNode, HashSet<Pair>> pairsOfAncestor = new HashMap<>();
		ArrayList<Pair> pairs = new ArrayList<Pair>();
		PairDescendingOrder comparator = new PairDescendingOrder();
		for (ASTNode nodeM : nodesM) {
			HashSet<Pair> pairs1 = new HashSet<Pair>();
			for (ASTNode nodeN : nodesN) {
				double sim = computeSimilarity(nodeM, nodeN, threshold);
				if (sim >= threshold) {
					Pair pair = new Pair(nodeM, nodeN, sim, 
							-Math.abs((nodeM.getParent().getStartPosition() - nodeM.getStartPosition()) - (nodeN.getParent().getStartPosition() - nodeN.getStartPosition())));
					pairs1.add(pair);
					HashSet<Pair> pairs2 = pairsOfAncestor.get(nodeN);
					if (pairs2 == null)
						pairs2 = new HashSet<Pair>();
					pairs2.add(pair);
					pairsOfAncestor.put(nodeN, pairs2);
					int index = Collections.binarySearch(pairs, pair, comparator);
					if (index < 0)
						pairs.add(-1 - index, pair);
					else
						pairs.add(index, pair);
				}
			}
			pairsOfAncestor.put(nodeM, pairs1);
		}
		ArrayList<ASTNode> nodes = new ArrayList<>();
		while (!pairs.isEmpty()) {
			Pair pair = pairs.get(0);
			ASTNode nodeM = (ASTNode) pair.getObj1(), nodeN = (ASTNode) pair.getObj2();
			setMap(nodeM, nodeN, pair.getWeight());
			nodes.add(nodeM);
			nodes.add(nodeN);
			for (Pair p : pairsOfAncestor.get(nodeM))
				pairs.remove(p);
			for (Pair p : pairsOfAncestor.get(nodeN))
				pairs.remove(p);
		}
		return nodes;
	}

	private void setMap(ASTNode nodeM, ASTNode nodeN, double w) {
		treeMap.get(nodeM).put(nodeN, w);
		treeMap.get(nodeN).put(nodeM, w);
	}

	private double computeSimilarity(ASTNode nodeM, ASTNode nodeN, double threshold) {
		if (nodeM.getNodeType() != nodeN.getNodeType())
			return 0;
		ArrayList<ASTNode> childrenM = tree.get(nodeM), childrenN = tree.get(nodeN);
		if (childrenM.isEmpty() && childrenN.isEmpty()) {
			if (nodeM instanceof Modifier) {
				Modifier mnM = (Modifier) nodeM, mnN = (Modifier) nodeN;
				if (JavaASTUtil.getType(mnM) != JavaASTUtil.getType(mnN))
					return 0;
			}
			int type = nodeM.getNodeType();
			double sim = 0;
			if (type == ASTNode.ARRAY_CREATION 
					|| type == ASTNode.ARRAY_INITIALIZER 
					|| type == ASTNode.BLOCK 
					|| type == ASTNode.INFIX_EXPRESSION 
					|| type == ASTNode.METHOD_INVOCATION
					|| type == ASTNode.SWITCH_STATEMENT
					)
				sim = MIN_SIM_MOVE;
			else {
				String sM = nodeM.toString(), sN = nodeN.toString();
				int lM = sM.length(), lN = sN.length();
				if (lM > 1000 || lN > 1000) {
					if (lM == 0 && lN == 0)
						sim = 1;
					else if (lM == 0 || lN == 0)
						sim = 0;
					else
						sim = lM > lN ? lN * 1.0 / lM : lM * 1.0 / lN;
				} else
					sim = StringProcessor.computeCharLCS(StringProcessor.serializeToChars(sM), StringProcessor.serializeToChars(sN));
			}
			sim = threshold + sim * (1 - threshold);
			return sim;
		}
		if (!childrenM.isEmpty() && !childrenN.isEmpty()) {
			HashMap<String, Integer> vM = treeVector.get(nodeM), vN = treeVector.get(nodeN);
			double sim = computeSimilarity(vM, vN);
			double[] sims = computeVectorSimilarity(childrenM, childrenN);
			for (double s : sims)
				sim += s;
			return sim / (sims.length + 1);
		}
		return 0;
	}

	private double[] computeVectorSimilarity(ArrayList<ASTNode> l1, ArrayList<ASTNode> l2) {
		double[] sims = new double[Math.max(l1.size(), l2.size())];
		Arrays.fill(sims, 0.0);
		HashMap<ASTNode, HashSet<Pair>> pairsOfNode = new HashMap<>();
		ArrayList<Pair> pairs = new ArrayList<Pair>();
		PairDescendingOrder comparator = new PairDescendingOrder();
		for (ASTNode node1 : l1) {
			HashSet<Pair> pairs1 = new HashSet<Pair>();
			for (ASTNode node2 : l2) {
				double sim = computeSimilarity(treeVector.get(node1), treeVector.get(node2));
				if (sim > 0) {
					Pair pair = new Pair(node1, node2, sim);
					pairs1.add(pair);
					HashSet<Pair> pairs2 = pairsOfNode.get(node2);
					if (pairs2 == null)
						pairs2 = new HashSet<Pair>();
					pairs2.add(pair);
					pairsOfNode.put(node2, pairs2);
					int index = Collections.binarySearch(pairs, pair,
							comparator);
					if (index < 0)
						pairs.add(-1 - index, pair);
					else
						pairs.add(index, pair);
				}
			}
			pairsOfNode.put(node1, pairs1);
		}
		int i = 0;
		while (!pairs.isEmpty()) {
			Pair pair = pairs.get(0);
			sims[i++] = pair.getWeight();
			for (Pair p : pairsOfNode.get(pair.getObj1()))
				pairs.remove(p);
			for (Pair p : pairsOfNode.get(pair.getObj2()))
				pairs.remove(p);
		}
		
		return sims;
	}

	private double computeSimilarity(HashMap<String, Integer> vM, HashMap<String, Integer> vN) {
		double sim = 0.0;
		HashSet<String> keys = new HashSet<>(vM.keySet());
		keys.retainAll(vN.keySet());
		for (String key : keys)
			sim += Math.min(vM.get(key), vN.get(key));
		sim = 2 * (sim + SIM_SMOOTH) / (length(vM) + length(vN) + 2 * SIM_SMOOTH);
		return sim;
	}

	private <E> int length(HashMap<E, Integer> vector) {
		int len = 0;
		for (int val : vector.values())
			len += val;
		return len;
	}

	private void getNotYetMappedAncestors(ASTNode node, ArrayList<ASTNode> ancestors) {
		ASTNode p = node.getParent();
		if (treeMap.get(p).isEmpty()) {
			ancestors.add(p);
			getNotYetMappedAncestors(p, ancestors);
		}
	}

	private void mapTopDown() {
		mapTopDown(astM);
	}

	private void mapTopDown(ASTNode nodeM) {
		ArrayList<ASTNode> childrenM = tree.get(nodeM);
		HashMap<ASTNode, Double> maps = treeMap.get(nodeM);
		if (!maps.isEmpty()) {
			ASTNode nodeN = maps.keySet().iterator().next();
			ArrayList<ASTNode> childrenN = tree.get(nodeN);
			if (pivotsM.contains(nodeM)) {
				mapUnchangedNodes(nodeM, nodeN);
				return;
			} else {
				ArrayList<ASTNode> nodesM = getNotYetMatchedNodes(childrenM), nodesN = getNotYetMatchedNodes(childrenN);
				ArrayList<ASTNode> mappedChildrenM = new ArrayList<>(), mappedChildrenN = new ArrayList<>();
				if (nodeM instanceof Statement) {
					if (nodeM instanceof DoStatement) {
						mappedChildrenM.add(((DoStatement) nodeM).getBody());
						mappedChildrenN.add(((DoStatement) nodeN).getBody());
					} else if (nodeM instanceof EnhancedForStatement) {
						mappedChildrenM.add(((EnhancedForStatement) nodeM).getBody());
						mappedChildrenN.add(((EnhancedForStatement) nodeN).getBody());
					} else if (nodeM instanceof ForStatement) {
						mappedChildrenM.add(((ForStatement) nodeM).getBody());
						mappedChildrenN.add(((ForStatement) nodeN).getBody());
					} else if (nodeM instanceof SynchronizedStatement) {
						mappedChildrenM.add(((SynchronizedStatement) nodeM).getBody());
						mappedChildrenN.add(((SynchronizedStatement) nodeN).getBody());
					} else if (nodeM instanceof ThrowStatement) {
						mappedChildrenM.add(((ThrowStatement) nodeM).getExpression());
						mappedChildrenN.add(((ThrowStatement) nodeN).getExpression());
					} else if (nodeM instanceof TryStatement) {
						mappedChildrenM.add(((TryStatement) nodeM).getBody());
						mappedChildrenN.add(((TryStatement) nodeN).getBody());
					} else if (nodeM instanceof TypeDeclarationStatement) {
						mappedChildrenM.add(((TypeDeclarationStatement) nodeM).getDeclaration());
						mappedChildrenN.add(((TypeDeclarationStatement) nodeN).getDeclaration());
					} else if (nodeM instanceof WhileStatement) {
						mappedChildrenM.add(((WhileStatement) nodeM).getBody());
						mappedChildrenN.add(((WhileStatement) nodeN).getBody());
					}
				} else if (nodeM instanceof MethodDeclaration) {
					mappedChildrenM.add(((MethodDeclaration) nodeM).getBody());
					mappedChildrenN.add(((MethodDeclaration) nodeN).getBody());
				} else if (nodeM instanceof CatchClause) {
					mappedChildrenM.add(((CatchClause) nodeM).getBody());
					mappedChildrenN.add(((CatchClause) nodeN).getBody());
				} else if (nodeM instanceof Expression) {
					if (nodeM instanceof ClassInstanceCreation) {
						ClassInstanceCreation cicM = (ClassInstanceCreation) nodeM, cicN = (ClassInstanceCreation) nodeN;
						mappedChildrenM.add(cicM.getExpression());
						mappedChildrenN.add(cicN.getExpression());
						mappedChildrenM.add(cicM.getType());
						mappedChildrenN.add(cicN.getType());
					} else if (nodeM instanceof MethodInvocation) {
						MethodInvocation miM = (MethodInvocation) nodeM, miN = (MethodInvocation) nodeN;
						mappedChildrenM.add(miM.getExpression());
						mappedChildrenN.add(miN.getExpression());
						mappedChildrenM.add(miM.getName());
						mappedChildrenN.add(miN.getName());
					} else if (nodeM instanceof SuperMethodInvocation) {
						SuperMethodInvocation miM = (SuperMethodInvocation) nodeM, miN = (SuperMethodInvocation) nodeN;
						mappedChildrenM.add(miM.getQualifier());
						mappedChildrenN.add(miN.getQualifier());
						mappedChildrenM.add(miM.getName());
						mappedChildrenN.add(miN.getName());
					}
				}
				if (!mappedChildrenM.isEmpty() && !mappedChildrenN.isEmpty()) {
					for (int i = 0; i < mappedChildrenM.size(); i++) {
						ASTNode childM = mappedChildrenM.get(i), childN = mappedChildrenN.get(i);
						if (childM != null && childN != null) {
							if (treeMap.get(childM).isEmpty() && treeMap.get(childN).isEmpty()) {
								double sim = 0;
								if (childM.getNodeType() == childN.getNodeType()) {
									int type = childM.getNodeType();
									if (type == ASTNode.BLOCK || (treeMap.get(childM).isEmpty() && treeMap.get(childN).isEmpty()))
										sim = 1.0;
									else 
										sim = computeSimilarity(childM, childN, MIN_SIM);
									if (sim >= MIN_SIM) {
										setMap(childM, childN, MIN_SIM);
										if (TreedUtils.buildASTLabel(childM).equals(TreedUtils.buildASTLabel(childN))) {
											childM.setProperty(PROPERTY_MAP, STATUS_UNCHANGED);
											childN.setProperty(PROPERTY_MAP, STATUS_UNCHANGED);
										} else {
											childM.setProperty(PROPERTY_MAP, STATUS_RELABELED);
											childN.setProperty(PROPERTY_MAP, STATUS_RELABELED);
										}
									}
								}
								if (sim < MIN_SIM) {
									ArrayList<ASTNode> tempM = new ArrayList<>(), tempN = new ArrayList<>();
									tempM.add(childM);
									tempN.add(childN);
									int hM = treeHeight.get(childM), hN = treeHeight.get(childN);
									if (hM >= hN) {
										tempM.remove(childM);
										tempM.addAll(getNotYetMatchedNodes(tree.get(childM)));
									}
									if (hN >= hM) {
										tempN.remove(childN);
										tempN.addAll(getNotYetMatchedNodes(tree.get(childN)));
									}
									ArrayList<ASTNode> mappedNodes = map(tempM, tempN, MIN_SIM_MOVE);
									for (int j = 0; j < mappedNodes.size(); j += 2) {
										ASTNode mappedNodeM = mappedNodes.get(j), mappedNodeN = mappedNodes.get(j+1);
										tempM.remove(mappedNodeM);
										tempN.remove(mappedNodeN);
									}
								}
							}
						}
						nodesM.remove(childM);
						nodesN.remove(childN);
					}
				}
				ArrayList<Integer> lcsM = new ArrayList<>(), lcsN = new ArrayList<>();
				lcs(nodesM, nodesN, lcsM, lcsN);
				for (int i = lcsM.size()-1; i >= 0; i--) {
					int iM = lcsM.get(i), iN = lcsN.get(i);
					ASTNode nM = nodesM.get(iM), nN = nodesN.get(iN);
					setMap(nM, nN, 1.0);
					nodesM.remove(iM);
					nodesN.remove(iN);
				}
				/*lcsM.clear(); lcsN.clear();
				lss(nodesM, nodesN, lcsM, lcsN, MIN_SIM);
				for (int i = lcsM.size()-1; i >= 0; i--) {
					int iM = lcsM.get(i), iN = lcsN.get(i);
					ASTNode nM = nodesM.get(iM), nN = nodesN.get(iN);
					setMap(nM, nN, 0.99050); // TODO
					nodesM.remove(iM);
					nodesN.remove(iN);
				}*/
				lcsM.clear(); lcsN.clear();
				ArrayList<ASTNode> mappedNodes = map(nodesM, nodesN, MIN_SIM);
				for (int i = 0; i < mappedNodes.size(); i += 2) {
					ASTNode mappedNodeM = mappedNodes.get(i), mappedNodeN = mappedNodes.get(i+1);
					nodesM.remove(mappedNodeM);
					nodesN.remove(mappedNodeN);
				}
				ArrayList<ASTNode> maxsM = new ArrayList<>(), maxsN = new ArrayList<>();
				int maxhM = maxHeight(nodesM, maxsM), maxhN = maxHeight(nodesN, maxsN);
				if (maxhM >= maxhN) {
					for (ASTNode node : maxsM) {
						nodesM.remove(node);
						nodesM.addAll(getNotYetMatchedNodes(tree.get(node)));
					}
				}
				if (maxhN >= maxhM) {
					for (ASTNode node : maxsN) {
						nodesN.remove(node);
						nodesN.addAll(getNotYetMatchedNodes(tree.get(node)));
					}
				}
				mappedNodes = map(nodesM, nodesN, MIN_SIM_MOVE);
				for (int i = 0; i < mappedNodes.size(); i += 2) {
					ASTNode mappedNodeM = mappedNodes.get(i), mappedNodeN = mappedNodes.get(i+1);
					nodesM.remove(mappedNodeM);
					nodesN.remove(mappedNodeN);
				}
			}
		}
		for (ASTNode child : childrenM)
			mapTopDown(child);
	}

	private int maxHeight(ArrayList<ASTNode> nodes, ArrayList<ASTNode> maxs) {
		int max = 0;
		for (ASTNode node : nodes) {
			int h = treeHeight.get(node);
			if (h >= max) {
				if (h > max) {
					max = h;
					maxs.clear();
				}
				maxs.add(node);
			}
		}
		return max;
	}

	private void mapUnchangedNodes(ASTNode nodeM, ASTNode nodeN) {
		setMap(nodeM, nodeN, 1.0);
		ArrayList<ASTNode> childrenM = tree.get(nodeM), childrenN = tree.get(nodeN);
		for (int i = 0; i < childrenM.size(); i++)
			mapUnchangedNodes(childrenM.get(i), childrenN.get(i));
	}

	private ArrayList<ASTNode> getNotYetMatchedNodes(ArrayList<ASTNode> l) {
		ArrayList<ASTNode> nodes = new ArrayList<>();
		for (ASTNode node : l)
			if (treeMap.get(node).isEmpty())
				nodes.add(node);
		return nodes;
	}

	private void mapMoving() {
		astM.accept(new ASTVisitor() {
			@Override
			public boolean visit(AnnotationTypeMemberDeclaration node) {
				HashMap<ASTNode, Double> maps = treeMap.get(node);
				if (!maps.isEmpty()) {
					ASTNode mapped = maps.keySet().iterator().next();
					mapMoving(node, mapped);
					return false;
				}
				return true;
			}
			
			@Override
			public boolean visit(EnumConstantDeclaration node) {
				HashMap<ASTNode, Double> maps = treeMap.get(node);
				if (!maps.isEmpty()) {
					ASTNode mapped = maps.keySet().iterator().next();
					mapMoving(node, mapped);
					return false;
				}
				return true;
			}
			
			@Override
			public boolean visit(FieldDeclaration node) {
				HashMap<ASTNode, Double> maps = treeMap.get(node);
				if (!maps.isEmpty()) {
					ASTNode mapped = maps.keySet().iterator().next();
					mapMoving(node, mapped);
					return false;
				}
				return true;
			}
			
			@Override
			public boolean visit(Initializer node) {
				HashMap<ASTNode, Double> maps = treeMap.get(node);
				if (!maps.isEmpty()) {
					ASTNode mapped = maps.keySet().iterator().next();
					mapMoving(node, mapped);
					return false;
				}
				return true;
			}
			
			@Override
			public boolean visit(MethodDeclaration node) {
				HashMap<ASTNode, Double> maps = treeMap.get(node);
				if (!maps.isEmpty()) {
					ASTNode mapped = maps.keySet().iterator().next();
					mapMoving(node, mapped);
					return false;
				}
				return true;
			}
		});
//		mapMoving(astM, astN);
	}

	private void mapMoving(ASTNode astM, ASTNode astN) {
		ArrayList<ASTNode> lM = getNotYetMappedDescendantContainers(astM), lN = getNotYetMappedDescendantContainers(astN);
		ArrayList<ASTNode> heightsM = new ArrayList<ASTNode>(lM), heightsN = new ArrayList<ASTNode>(lN);
		Collections.sort(heightsM, new Comparator<ASTNode>() {
			@Override
			public int compare(ASTNode node1, ASTNode node2) {
				return treeHeight.get(node2) - treeHeight.get(node1);
			}
		});
		Collections.sort(heightsN, new Comparator<ASTNode>() {
			@Override
			public int compare(ASTNode node1, ASTNode node2) {
				return treeHeight.get(node2) - treeHeight.get(node1);
			}
		});
		mapMoving(lM, lN, heightsM, heightsN);
	}

	private void mapMoving(ArrayList<ASTNode> lM, ArrayList<ASTNode> lN, ArrayList<ASTNode> heightsM, ArrayList<ASTNode> heightsN) {
		ArrayList<ASTNode> mappedNodes = map(lM, lN, MIN_SIM_MOVE);
		for (int i = 0; i < mappedNodes.size(); i += 2) {
			ASTNode nodeM = mappedNodes.get(i), nodeN = mappedNodes.get(i+1);
			lM.remove(nodeM);
			lN.remove(nodeN);
			heightsM.remove(nodeM);
			heightsN.remove(nodeN);
		}
		while (!lM.isEmpty() && !lN.isEmpty()) {
			int hM = treeHeight.get(heightsM.get(0));
			int hN = treeHeight.get(heightsN.get(0));
			boolean expandedM = false, expandedN = false;
			if (hM >= hN)
				expandedM = expandForMoving(lM, heightsM, hM);
			if (hN >= hM)
				expandedN = expandForMoving(lN, heightsN, hN);
			if (expandedM || expandedN) {
				mapMoving(lM, lN, heightsM, heightsN);
				break;
			}
		}
	}

	private ArrayList<ASTNode> getNotYetMappedDescendantContainers(ASTNode node) {
		ArrayList<ASTNode> children = new ArrayList<>();
		for (ASTNode child : tree.get(node)) {
			if (!pivotsM.contains(child) && !pivotsN.contains(child) && treeHeight.get(child) >= MIN_HEIGHT) {
				if (treeMap.get(child).isEmpty())
					children.add(child);
				else
					children.addAll(getNotYetMappedDescendantContainers(child));
			}
		}
		return children;
	}

	private void buildTrees(boolean visitDocTags) {
		buildTree(astM, visitDocTags);
		buildTree(astN, visitDocTags);
	}

	private void buildTree(final ASTNode root, boolean visitDocTags) {
		final TreedBuilder visitor = new TreedBuilder(root, visitDocTags);
		root.accept(visitor);
		tree.putAll(visitor.tree);
		treeHeight.putAll(visitor.treeHeight);
		treeDepth.putAll(visitor.treeDepth);
		treeVector.putAll(visitor.treeVector);
	}
	
}
