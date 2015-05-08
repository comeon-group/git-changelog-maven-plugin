package com.comeon.mojo.gitchangelog;

import org.eclipse.jgit.errors.StopWalkException;
import org.eclipse.jgit.revwalk.*;
import org.eclipse.jgit.revwalk.filter.RevFilter;

import java.io.IOException;

public class CommitFilter extends RevFilter {

	@Override
	public boolean include(RevWalk walker, RevCommit revCommit) throws StopWalkException, IOException {

		if (isBranchMerge(revCommit) || isMavenReleasePlugin(revCommit)) {
			return false;
		}

		return true;
	}

	private boolean isBranchMerge(RevCommit commit) {
		return commit.getParentCount() > 1;
	}

	private boolean isMavenReleasePlugin(RevCommit revCommit) {
		String shortMessage = revCommit.getShortMessage().trim();
		return shortMessage.startsWith("[maven-release-plugin]");
	}

	@Override
	public RevFilter clone() {
		return this;
	}
}
