package exas;

import java.util.ArrayList;
import java.util.HashMap;

import codemining.Feature;

public abstract class ExasFeature implements Feature {
	protected static int numOfFeatures = 0, numOfBranches = 0;
	public static int MAX_LENGTH = 4;
	
	protected int hash;
	
	protected HashMap<ExasSingleFeature, ExasSequentialFeature> next = new HashMap<ExasSingleFeature, ExasSequentialFeature>();

	abstract public int getFeatureLength();
	
	protected int id, frequency = 0;
	
	protected ExasFeature() {
		this.id = ++numOfFeatures;
	}
	
	public static int getNumOfFeatures() {
		return numOfFeatures;
	}

	@Override
	public int getId() {
		return id;
	}

	public int getFrequency() {
		return frequency;
	}

	public void setFrequency(int frequency) {
		this.frequency = frequency;
	}
	
	public static ExasFeature getFeature(ArrayList<ExasSingleFeature> sequence)
	{
		if (sequence.size() == 1)
			return sequence.get(0);
		ExasFeature feature = sequence.get(0);
		int i = 1;
		while (i < sequence.size())
		{
			ExasSingleFeature s = sequence.get(i);
			if (feature.next.containsKey(s))
			{
				feature = feature.next.get(s);
			}
			else
			{
				feature = new ExasSequentialFeature(sequence.subList(0, i+1), feature, s);
			}
			i++;
		}
		return feature;
	}
	
	public static ExasSingleFeature getFeature(String label)
	{
		ExasSingleFeature f = ExasSingleFeature.features.get(label);
		if (f == null)
			f = new ExasSingleFeature(label);
		return f;
	}
	
	@Override
	abstract public int hashCode();
	
	@Override
	abstract public boolean equals(Object obj);
	
	@Override
	abstract public String toString();

	public int compareTo(ExasFeature other) {
		if (getFeatureLength() < other.getFeatureLength())
			return -1;
		if (getFeatureLength() > other.getFeatureLength())
			return 1;
		if (this instanceof ExasSingleFeature)
		{
			return ((ExasSingleFeature)this).getLabel().compareTo(((ExasSingleFeature)other).getLabel());
		}
		for (int i = 0; i < getFeatureLength(); i++)
		{
			int c = ((ExasSequentialFeature)this).getSequence().get(i).getLabel().compareTo(((ExasSequentialFeature)other).getSequence().get(i).getLabel());
			if (c != 0)
				return c;
		}
		return 0;
	}
}
