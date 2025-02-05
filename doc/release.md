# Release

* [ ] Merge Open Pull Requests
* [ ] Update license information
    * [ ] "mvn license:update-file-header" ausführen and commit
* [x] Run release Script (de-snapshots, sets a tag and sets the next snapshot version, generates javadoc, runs unit
  tests)
    * [x] Check, if github action is installed, which copies the release to maven.scijava.org
    * [x] For reference: Release script https://github.com/scijava/scijava-scripts/blob/main/release-version.sh
    * [x] Clone https://github.com/scijava/scijava-scripts repo
    * [x] Ensure that one of the git remotes has the name "origin"
    * [x] Close IntelliJ, if open
    * [x] Run sh /path/to/release-version.sh from the mastodon-deep-lineage root directory
    * [x] Confirm version number
    * [x] The release script pushes to master on github.com
        * This triggers a *github Action* which copies the version to be released to maven.scijava.org.
          cf. https://maven.scijava.org/#nexus-search;quick~mastodon-deep-lineage)
* [x] Download created jar file from scijava Nexus (https://maven.scijava.org/#nexus-search;quick~mastodon-tomancak)
    * [x] Delete jar-file from last release version from local Fiji installation path
    * [x] Copy jar file of the new version to local Fiji installation path
    * [x] Test, if Fiji starts successfully
    * [x] Test new functionalities of released version in Fiji
* [x] Copy Jar-File to Mastodon-Tomancak Update-Site using Fiji/ImageJ Updaters (Fiji > Help > Update...)
    * [x] Set Updater to Advanced Mode
        * [x] If needed, add `webdav:stefanhahmann` as `Host` under `Manage Update Sites > Mastodon-Tomancak`
    * [x] Upload mastodon-tomancak-0.6.6.jar
    * [x] Check Upload success: https://sites.imagej.net/Mastodon-Tomancak/jars/
* [x] Communicate Update Site on Read the docs
