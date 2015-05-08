package com.comeon.mojo.gitchangelog;

import java.util.*;

public class ChangeLog {
	public final String currentBranch;
	public final String masterBranch;
	public final List<Commit> commits = new ArrayList<>();

	public ChangeLog(String currentBranch, String masterBranch) {
		this.currentBranch = currentBranch;
		this.masterBranch = masterBranch;
	}

	public void addCommit(Commit commit) {
		commits.add(commit);
	}

	@Override
	public String toString() {
		return commits.size() + " commits in " + currentBranch + " not in " + masterBranch;
	}
}
