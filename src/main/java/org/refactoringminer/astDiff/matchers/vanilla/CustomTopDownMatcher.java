package org.refactoringminer.astDiff.matchers.vanilla;

import com.github.gumtreediff.matchers.*;
import com.github.gumtreediff.matchers.heuristic.gt.*;
import com.github.gumtreediff.tree.Tree;
import org.refactoringminer.astDiff.utils.Constants;
import org.refactoringminer.astDiff.utils.TreeUtilFunctions;

import java.util.*;

public class CustomTopDownMatcher extends GreedySubtreeMatcher {
	public CustomTopDownMatcher(int minP) {
		super();
		setMinPriority(minP);
	}

	protected void retainBestMapping(List<Mapping> mappingList, Set<Tree> srcIgnored, Set<Tree> dstIgnored) {
		List<Mapping> verifiedList = new ArrayList<>();
		for (Mapping mapping : mappingList) {
			if (TreeUtilFunctions.areBothFromThisType(mapping, Constants.SIMPLE_NAME) || TreeUtilFunctions.areBothFromThisType(mapping, Constants.QUALIFIED_NAME)) {
				if (isAcceptableMatch(mapping))
					verifiedList.add(mapping);
			}
			else verifiedList.add(mapping);
		}
		while (!verifiedList.isEmpty()) {
			Mapping mapping = verifiedList.remove(0);
			if (!(srcIgnored.contains(mapping.first) || dstIgnored.contains(mapping.second)))
			{
				mappings.addMappingRecursively(mapping.first, mapping.second);
				srcIgnored.add(mapping.first);
				srcIgnored.addAll(mapping.first.getDescendants());
				dstIgnored.add(mapping.second);
				dstIgnored.addAll(mapping.second.getDescendants());
			}
		}
	}

	private static boolean isAcceptableMatch(Mapping mapping) {
		Tree first = mapping.first;
		Tree second = mapping.second;
		if (isPartOfConditional(first) && isPartOfConditional(second))
			if (!isSamePositionInConditional(first,second))
				return false;
		if (isPartOfConditional(first) && getIndexInConditional(first) == 0 && !isPartOfConditional(second))
			return false;
		if (isPartOfConditional(second) && getIndexInConditional(second) == 0 && !isPartOfConditional(first))
			return false;
		return (!mapping.first.getParent().getType().name.equals(Constants.METHOD_INVOCATION)
				||
				mapping.second.getParent().getType().name.equals(Constants.METHOD_INVOCATION)) &&
				(!mapping.second.getParent().getType().name.equals(Constants.METHOD_INVOCATION)
						||
						mapping.first.getParent().getType().name.equals(Constants.METHOD_INVOCATION));
	}

	private static boolean isOnlyOneMethodInvocation(Tree input1, Tree input2) {
		if (input1 == null || input2 == null) return false;
		if (input1.getParent() == null || input2.getParent() == null) return false;
		if (input1.getParent().getType().name.equals(Constants.METHOD_INVOCATION) && !(input2.getParent().getType().name.equals(Constants.METHOD_INVOCATION) || input2.getParent().getType().name.equals(Constants.EXPRESSION_METHOD_REFERENCE)))
			return true;
		else if (!(input1.getParent().getType().name.equals(Constants.METHOD_INVOCATION) || input1.getParent().getType().name.equals(Constants.EXPRESSION_METHOD_REFERENCE)) && input2.getParent().getType().name.equals(Constants.METHOD_INVOCATION))
			return true;
		return false;

	}

	private static boolean isPartOfConditional(Tree input) {
		if (input.getType().name.equals(Constants.CONDITIONAL_EXPRESSION))
			return true;
		else {
			Tree parent = input.getParent();
			if (parent == null) return false;
			if (TreeUtilFunctions.isStatement(parent.getType().name)) return false;
			return isPartOfConditional(parent);
		}
	}

	private static boolean isSamePositionInConditional(Tree input1, Tree input2) {
		return getIndexInConditional(input1) == getIndexInConditional(input2);
	}

	private static int getIndexInConditional(Tree input1) {
		Tree current = input1;
		while (!current.getParent().getType().name.equals(Constants.CONDITIONAL_EXPRESSION))
			current = current.getParent();
		return current.positionInParent();
	}

	@Override
	public MappingStore match(Tree src, Tree dst, MappingStore mappings) {
		this.src = src;
		this.dst = dst;
		this.mappings = mappings;

		MultiMappingStore multiMappings = new MultiMappingStore();
		PriorityTreeQueue srcTrees = new DefaultPriorityTreeQueue(src, this.minPriority, this.priorityCalculator);
		PriorityTreeQueue dstTrees = new DefaultPriorityTreeQueue(dst, this.minPriority, this.priorityCalculator);

		while (!(srcTrees.isEmpty() || dstTrees.isEmpty())) {
			PriorityTreeQueue.synchronize(srcTrees, dstTrees);
			if (srcTrees.isEmpty() || dstTrees.isEmpty())
				break;

			List<Tree> currentPrioritySrcTrees = srcTrees.pop();
			List<Tree> currentPriorityDstTrees = dstTrees.pop();

			for (Tree currentSrc : currentPrioritySrcTrees)
				for (Tree currentDst : currentPriorityDstTrees)
					if (currentSrc.getMetrics().hash == currentDst.getMetrics().hash) {
						if (TreeUtilFunctions.isIsomorphicTo(currentSrc,currentDst)) {
							if (!isOnlyOneMethodInvocation(currentSrc, currentDst))
								multiMappings.addMapping(currentSrc, currentDst);
						}
					}
					else {
						if (currentSrc.getLabel().equals(currentDst.getLabel()))
						{
//							//TODO: Its definitely better to avoid this
//							if (currentSrc.getType().name.contains("_EXPRESSION_OPERATOR")
//									&&
//									currentDst.getType().name.contains("_EXPRESSION_OPERATOR"))
//								multiMappings.addMapping(currentSrc, currentDst);
						}
					}



			for (Tree t : currentPrioritySrcTrees)
				if (!multiMappings.hasSrc(t))
					srcTrees.open(t);
			for (Tree t : currentPriorityDstTrees)
				if (!multiMappings.hasDst(t))
					dstTrees.open(t);
		}

		filterMappings(multiMappings);
		return this.mappings;
	}

	public void filterMappings(MultiMappingStore multiMappings) {
		// Select unique mappings first and extract ambiguous mappings.
		List<Mapping> ambiguousList = new ArrayList<>();
		Set<Tree> ignored = new HashSet<>();
		for (Tree srcTree : multiMappings.allMappedSrcs()) {
			boolean mappingIsUnique = false;
			if (multiMappings.isSrcUnique(srcTree)) {
				Tree mappedDst = multiMappings.getDsts(srcTree).stream().findAny().get();
				if (multiMappings.isDstUnique(mappedDst)) {
					mappings.addMappingRecursively(srcTree, mappedDst);
					mappingIsUnique = true;
				}
			}

			if (!(ignored.contains(srcTree) || mappingIsUnique)) {
				Set<Tree> adsts = multiMappings.getDsts(srcTree);
				Set<Tree> asrcs = multiMappings.getSrcs(multiMappings.getDsts(srcTree).iterator().next());
				for (Tree asrc : asrcs)
					for (Tree adst : adsts) {
						ambiguousList.add(new Mapping(asrc, adst));
					}
				ignored.addAll(asrcs);
			}
		}

		// Rank the mappings by score.
		Set<Tree> srcIgnored = new HashSet<>();
		Set<Tree> dstIgnored = new HashSet<>();
		Collections.sort(ambiguousList, new ExtendedFullMappingComparator(mappings));
		if (ambiguousList.size() > 1)
			ambiguousLeafModification(ambiguousList,srcIgnored,dstIgnored);
		// Select the best ambiguous mappings
		retainBestMapping(ambiguousList, srcIgnored, dstIgnored);
	}

	private void ambiguousLeafModification(List<Mapping> ambiguousList, Set<Tree> srcIgnored, Set<Tree> dstIgnored) {
		for (Mapping mapping : ambiguousList) {
			Tree asrc = mapping.first;
			Tree adst = mapping.second;
			if (
					(asrc.getType().name.equals(Constants.STRING_LITERAL) && adst.getType().name.equals(Constants.STRING_LITERAL))
					 || (asrc.getType().name.equals(Constants.NUMBER_LITERAL) && adst.getType().name.equals(Constants.NUMBER_LITERAL))
					 || (asrc.getType().name.equals(Constants.PREFIX_EXPRESSION) && adst.getType().name.equals(Constants.PREFIX_EXPRESSION))
					 || (asrc.getType().name.equals(Constants.BOOLEAN_LITERAL) && adst.getType().name.equals(Constants.BOOLEAN_LITERAL))
				) {
				if (asrc.getParent() != null && adst.getParent() != null) {
					int i = asrc.positionInParent();
					int j = adst.positionInParent();
					if (i == j && asrc.getParent().getType().name.equals(adst.getParent().getType().name))
					{
						srcIgnored.add(asrc);
						dstIgnored.add(adst);
					}
					else if (i > 0 && j > 0) {
						Tree srcYoungerSibling = asrc.getParent().getChild(i - 1);
						Tree dstYoungerSibling = adst.getParent().getChild(j - 1);
						if (mappings.getSrcForDst(dstYoungerSibling) != null)
							if (mappings.getSrcForDst(dstYoungerSibling).equals(srcYoungerSibling)) {
								srcIgnored.add(asrc);
								dstIgnored.add(adst);
							}
					}
				}
			} else {
				break;
			}
		}
	}
	public static class ExtendedFullMappingComparator implements Comparator<Mapping> {
		private final MappingComparators.FullMappingComparator fullMappingComparator;

		public ExtendedFullMappingComparator(MappingStore ms) {
			fullMappingComparator = new MappingComparators.FullMappingComparator(ms);
		}

		@Override
		public int compare(Mapping m1, Mapping m2) {
			int result = fullMappingComparator.compare(m1,m2);
			if (result != 0)
				return result;
			return new AbsoluteOffsetComparator().compare(m1,m2);
		}
	}
	public static class AbsoluteOffsetComparator implements Comparator<Mapping> {
		@Override
		public int compare(Mapping m1, Mapping m2) {
			int result =  Integer.compare(m1.first.getPos(), m2.first.getPos());
			if (result != 0) return result;
			result =  Integer.compare(m1.second.getPos(), m2.second.getPos());
			return result;
		}
	}
}
