package com.comeon.mojo.gitchangelog;

import org.eclipse.jgit.revwalk.RevCommit;

public class Commit {

	public final String id;
	public final Long commitTime;
	public final String authorName;
	public final String authorEmail;
	public final String committerName;
	public final String committerEmail;
	public final String shortMessage;
	public final String fullMessage;

	public Commit(RevCommit revCommit) {
		id = revCommit.getName();
		commitTime = revCommit.getCommitTime() * 1000L;
		shortMessage = revCommit.getShortMessage();
		fullMessage = revCommit.getFullMessage();
		authorName = revCommit.getAuthorIdent().getName();
		authorEmail = revCommit.getAuthorIdent().getEmailAddress();
		committerName = revCommit.getCommitterIdent().getName();
		committerEmail = revCommit.getCommitterIdent().getEmailAddress();
	}

	@Override
	public String toString() {
		return id + " " + shortMessage;
	}
}
