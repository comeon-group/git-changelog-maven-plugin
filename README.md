# git-changelog-maven-plugin

Finds all git commits that's in the current branch but not in another. The typical use case is to see what changes are in this branch in relation to the production one. Output is a JSON file that can be used to report on the status of the branch.

For instance, we use it to display a HTML table of all commits in the staging environment compared to production, so as to make double sure all issues are properly tested before a release.

Note: the plugin will skip merge commits (commits with more than one parent) and commits from maven-release-plugin. This is probably always what you want.

# Minimal example

Assuming your master branch is called `master` and you are on some other branch:

Add to pom.xml:

```xml
<plugin>
	<groupId>com.comeon.mojo</groupId>
	<artifactId>git-changelog-maven-plugin</artifactId>
	<version>1.0.0</version>		
</plugin>
```

Then run:

	mvn git-changelog:generate
	
A file `target/changelog.json` is created, containing the commits here but not in master. See below for documentation on the format.
	
# Add to build

Default phase is `prepare-package`.

```xml
<plugin>
	<groupId>com.comeon.mojo</groupId>
	<artifactId>git-changelog-maven-plugin</artifactId>
	<version>1.0.0</version>
	<executions>
		<execution>
			<id>git-changelog</id>
			<goals>
				<goal>generate</goal>
			</goals>
		</execution>
	</executions>
</plugin>
```

# Configure

Configuration, with defaults:

```xml
<plugin>
	<groupId>com.comeon.mojo</groupId>
	<artifactId>git-changelog-maven-plugin</artifactId>
	<version>1.0.0</version>
	<configuration>
		<outputDirectory>${project.build.directory}</outputDirectory>
		<masterBranch>master</masterBranch>
	</configuration>
</plugin>
```

Note: if there are no local branch `master`, you will need to use `origin/master`, for instance when this is done in automated builds. Will be updated in future release.

# JSON Output

```json
{
  "currentBranch": "feature/cool-feature",
  "masterBranch": "master",
  "commits": [
    {
      "id": "79e0b7f8c020198717982cb45f527dbc70345728",
      "commitTime": 1429722332000,
      "authorName": "Ralph Wiggum",
      "authorEmail": "ralph.wiggum@simpsons.com",
      "committerName": "Chief Wiggum",
      "committerEmail": "chief.wiggum@simpsons.com",
      "shortMessage": "Named my cat Mittens.",
      "fullMessage": "Named my cat Mittens.\nMy cat's name is Mittens."
    },
  ]
}
```

`commitTime` is a [UNIX timestamp](http://en.wikipedia.org/wiki/Unix_time) in milliseconds, unlike git's seconds, so it's ready to parse by Javascript, Java or any other language that takes UNIX time as input (all of them?). 

# Example jQuery-based viewer

We use something similar to this + styling and processing of issues found in messages, etc. It can be used as a base for your own needs.

```html
<!DOCTYPE html>
<html>
<head lang="en">
    <meta charset="UTF-8">
    <title>Changelog</title>
</head>
<body>
<table>
<tr>
    <th>Which</th>
    <th>When</th>
    <th>What</th>
    <th>Who</th>
</tr>
</table>
<script src="https://ajax.googleapis.com/ajax/libs/jquery/2.1.3/jquery.min.js"></script>
<script src="https://cdnjs.cloudflare.com/ajax/libs/moment.js/2.10.2/moment.min.js"></script>
<script>
    var $table = $('table');
    var jqxhr = $.getJSON("changelog.json");

    jqxhr.done(function (changes) {
        $.each(changes.commits, function(index, commit) {
            var $tr = $('<tr>');

            $tr.append('<td>' + commit.id.substring(0, 6) + '...');
            $tr.append('<td>' + moment(commit.commitTime).format('YYYY-MM-DD HH:mm'));
            $tr.append('<td>' + commit.fullMessage);
            $tr.append('<td>' + commit.authorName + ' <' + commit.authorEmail + '>');
            $table.append($tr);
        });
    });
</script>
</body>
</html>
```

# See also

Plugin was inspired by [maven-gitlog-plugin](https://github.com/danielflower/maven-gitlog-plugin) which we used (and contributed to), but in the end our use cases were to dissimilar, that plugin tries to get a changelog and tag releases, plus have multiple kinds of output and configuration available, we just really needed "What's happened since last release?" and other tools can process the JSON, so we ended up making a different tool.

Plugin uses [JGit](http://eclipse.org/jgit/) and [Gson](https://github.com/google/gson).
