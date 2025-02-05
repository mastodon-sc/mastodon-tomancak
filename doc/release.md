# Release

* [ ] Merge Open Pull Requests
* [ ] Check out the latest master branch
* [ ] Update license information
    * [ ] Open command line and navigate to the project root directory
    * [ ] Run "mvn license:update-file-header" and commit
* [ ] Run release Script (de-snapshots, sets a tag and sets the next snapshot version, generates javadoc, runs unit
  tests)
    * [x] Check, if github action is installed, which copies the release to maven.scijava.org
  * [ ] For reference: Release script https://github.com/scijava/scijava-scripts/blob/main/release-version.sh
  * [ ] Clone https://github.com/scijava/scijava-scripts repo
  * [ ] Ensure that one of the git remotes has the name "origin"
  * [ ] Close IntelliJ, if open
  * [ ] Run sh /path/to/release-version.sh from the mastodon-deep-lineage root directory
  * [ ] Confirm version number
  * [ ] The release script pushes to master on github.com
        * This triggers a *github Action* which copies the version to be released to maven.scijava.org.
          cf. https://maven.scijava.org/#nexus-search;quick~mastodon-deep-lineage)
* [ ] Download created jar file from scijava Nexus (https://maven.scijava.org/#nexus-search;quick~mastodon-tomancak)
    * [ ] Delete jar-file from last release version from local Fiji installation path
    * [ ] Copy jar file of the new version to local Fiji installation path
    * [ ] Test, if Fiji starts successfully
    * [ ] Test new functionalities of released version in Fiji
* [ ] Copy Jar-File to Mastodon-Tomancak Update-Site using Fiji/ImageJ Updaters (Fiji > Help > Update...)
    * [ ] Set Updater to Advanced Mode
        * [ ] If needed, add `webdav:username_for_update_site` as `Host` under `Manage Update Sites > Mastodon-Tomancak`
    * [ ] Upload mastodon-tomancak-release-version.jar
    * [ ] Check Upload success: https://sites.imagej.net/Mastodon-Tomancak/jars/
