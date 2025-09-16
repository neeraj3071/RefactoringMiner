/**
 * 
 */
package mining;

import groum.GROUMGraph;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * @author Nguyen Anh Hoan
 *
 */
public class Pattern {
	public static final int minSize = 3, maxSize = Integer.MAX_VALUE;
	
	public static int mode = -1; // -1 hybrid, 0 within, 1 cross methods, 2 cross commits, 3 cross projects
	public static int minFreq = 3, maxFreq = 1000;
	
	public static int nextID = 1;
	private int id;
	private int size = 0;
	private Fragment representative;
	private int freq = 0;
	private HashSet<Fragment> fragments = new HashSet<Fragment>();
	
	public Pattern(HashSet<Fragment> group, int freq) {
		fragments = group;
		for (Fragment f : fragments) {
			size = f.getNodes().size();
			representative = f;
			break;
		}
		//computeFrequency();
		this.freq = freq;
	}
	
	public void add2Lattice(ArrayList<Lattice> lattices) {
		setId();
		Lattice l = null;
		if (lattices.size() < size) {
			int s = size - lattices.size();
			while (s > 0) {
				l = new Lattice();
				l.setStep(lattices.size() + 1);
				lattices.add(l);
				s--;
			}
		} else
			l = lattices.get(size - 1);
		l.add(this);
	}
	
	public int getId() {
		return id;
	}
	
	public void setId() {
		this.id = nextID++;
	}

	/**
	 * @return the size
	 */
	public int getSize() {
		return size;
	}
	/**
	 * @param size the size to set
	 */
	public void setSize(int size) {
		this.size = size;
	}

	/**
	 * @return the representative
	 */
	public Fragment getRepresentative() {
		return representative;
	}

	/**
	 * @param representative the representative to set
	 */
	public void setRepresentative(Fragment representative) {
		this.representative = representative;
		//representative.toGraphics("D:/temp/output/patterns/changes", String.valueOf(id));
	}

	/**
	 * @return the freq
	 */
	public int getFreq() {
		return freq;
	}

	/**
	 * @param freq the freq to set
	 */
	public void setFreq(int freq) {
		this.freq = freq;
	}

	/**
	 * @return the genPatterns
	 */
	public HashSet<Fragment> getFragments() {
		return fragments;
	}

	/**
	 * @param genPatterns the genPatterns to set
	 */
	public void setFragments(HashSet<Fragment> fragments) {
		this.fragments = fragments;
	}
	
	public void computeFrequency() {
		HashMap<GROUMGraph, HashSet<Fragment>> fragmentOfGraph = new HashMap<GROUMGraph, HashSet<Fragment>>();
		for (Fragment f : fragments) {
			GROUMGraph g = f.getGraph();
			HashSet<Fragment> fs = fragmentOfGraph.get(g);
			if (fs == null)
				fs = new HashSet<Fragment>();
			fs.add(f);
			fragmentOfGraph.put(g, fs);
		}
		if (fragmentOfGraph.size() >= Pattern.minFreq)
			this.freq = fragmentOfGraph.size();
		else {
			this.freq = 0;
			for (GROUMGraph g : fragmentOfGraph.keySet()) {
				HashSet<Fragment> fs = fragmentOfGraph.get(g);
				HashSet<Fragment> cluster = new HashSet<Fragment>();
				for (Fragment f : fs) {
					boolean isOverlap = false;
					for (Fragment c : cluster)
						if (c.overlap(f)) {
							isOverlap = true;
							break;
						}
					if (!isOverlap)
						cluster.add(f);
				}
				this.freq += cluster.size();
			}	
		}
	}
	
	public boolean contains(Fragment fragment) {
		if (this.size < fragment.getNodes().size())
			return false;
		for (Fragment f : fragments)
			if (f.contains(fragment))
				return true;
		return false;
	}
	
	public boolean containsOne(Pattern other) {
		if (this.size < other.getSize())
			return false;
		for (Fragment f : other.getFragments())
			if (contains(f))
				return true;
		return false;
	}

	public boolean containsAll(HashSet<Fragment> g) {
		for (Fragment f : g)
			if (!contains(f))
				return false;
		return true;
	}

	public void clear() {
		this.representative = null;
		for (Fragment f : this.fragments)
			f.delete();
		this.fragments.clear();
	}

	public boolean isAChange() {
		return this.representative.isAChange();
	}
}
