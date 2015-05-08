package com.comeon.mojo.gitchangelog;

import com.google.gson.*;
import org.apache.maven.plugin.*;
import org.apache.maven.plugins.annotations.*;
import org.apache.maven.plugins.annotations.Mojo;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revwalk.*;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import java.io.*;

@Mojo(name = "generate", defaultPhase = LifecyclePhase.PREPARE_PACKAGE, aggregator = true)
public class GitReleaseLogMojo extends AbstractMojo {

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final String CHANGELOG_JSON = "changelog.json";

	@Parameter(property = "project.build.directory")
	private File outputDirectory;

	@Parameter(defaultValue = "master")
	private String masterBranch;

	public void execute() throws MojoExecutionException, MojoFailureException {
		Repository repository = findRepository();
		RevWalk commitsWalker = getCommitsWalker(repository);
		String currentBranch = getCurrentBranch(repository);

		ChangeLog changeLog = new ChangeLog(currentBranch, masterBranch);

		for (RevCommit revCommit : commitsWalker) {
			Commit commit = new Commit(revCommit);

			getLog().debug(commit.toString());

			changeLog.addCommit(commit);
		}

		commitsWalker.dispose();

		ensureOutputDirectory();

		File changelogJson = new File(outputDirectory, CHANGELOG_JSON);

		writeChangelogToFile(changeLog, changelogJson);

		getLog().info("Wrote to " + changelogJson.getAbsolutePath() + ": " + changeLog.toString());
	}

	private void writeChangelogToFile(ChangeLog changeLog, File file) throws MojoExecutionException {
		try(FileWriter writer = getJsonWriter(file)) {
			GSON.toJson(changeLog, writer);
		} catch (IOException e) {
			throw new MojoExecutionException("Could not write to " + file.getAbsolutePath(), e);
		}
	}

	private Repository findRepository() throws MojoExecutionException {
		FileRepositoryBuilder builder = new FileRepositoryBuilder();
		try {
			return builder.findGitDir().build();
		} catch (IOException e) {
			throw new MojoExecutionException("Could not find git directory", e);
		}
	}

	private String getCurrentBranch(Repository repository) throws MojoExecutionException {
		try {
			return repository.getBranch();
		} catch (IOException e) {
			throw new MojoExecutionException("Could not find current git branch", e);
		}
	}

	private RevWalk getCommitsWalker(Repository repository) throws MojoExecutionException {
		RevWalk commitsWalker = new RevWalk(repository);
		try {
			ObjectId from = repository.resolve(Constants.HEAD);
			ObjectId to = repository.resolve(masterBranch);

			if(to == null) {
				throw new MojoExecutionException("No such masterBranch " + masterBranch);
			}

			commitsWalker.markStart(commitsWalker.parseCommit(from));
			commitsWalker.markUninteresting(commitsWalker.parseCommit(to));
		} catch (IOException e) {
			throw new MojoExecutionException("Could not create RevWalk", e);
		}

		CommitFilter commitFilter = new CommitFilter();
		commitsWalker.setRevFilter(commitFilter);
		return commitsWalker;
	}

	private void ensureOutputDirectory() throws MojoExecutionException {
		if(!outputDirectory.mkdirs() && !outputDirectory.exists()) {
			throw new MojoExecutionException("Could not create " + outputDirectory);
		}
	}

	private FileWriter getJsonWriter(File changelogJson) throws MojoExecutionException {
		try {
			return new FileWriter(changelogJson);
		} catch (IOException e) {
			throw new MojoExecutionException("Could not create writer to " + outputDirectory, e);
		}
	}
}
