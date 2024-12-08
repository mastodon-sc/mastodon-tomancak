[![](https://github.com/mastodon-sc/mastodon-tomancak/actions/workflows/build-main.yml/badge.svg)](https://github.com/mastodon-sc/mastodon-tomancak/actions/workflows/build-main.yml)
[![License](https://img.shields.io/badge/License-BSD%202--Clause-orange.svg)](https://opensource.org/licenses/BSD-2-Clause)

# Mastodon Tomancak - a collection of plugins to edit, analyse and visualise lineages of tracked objects.

## Table of Contents

* [Documentation of Mastodon](#documentation-of-mastodon)
* [Installation Instructions](#installation-instructions)
* [Spots management](#spots-management)
    * [Transform spots](#transform-spots)
        * [Mirror spots along X-axis](#mirror-spots-along-x-axis)
      * [Remove spots solists](#remove-isolated-spots)
        * [Add center spot](#add-center-spot)
        * [Interpolate missing spots](#interpolate-missing-spots)
        * [Set radius of selected spots](#set-radius-of-selected-spots)
    * [Rename spots](#rename-spots)
        * [Label selected spots](#label-selected-spots)
        * [Change branch labels](#change-branch-labels)
        * [Systematically label spots (extern-intern)](#systematically-label-spots-extern-intern)
* [Tags](#tags)
    * [Locate tags](#locate-tags)
    * [Copy tag](#copy-tag)
    * [Add tag set to highlight cell divisions](#add-tag-set-to-highlight-cell-divisions)
    * [Create Dummy Tag Set](#create-dummy-tag-set)
* [Trees management](#trees-management)
    * [Flip descendants](#flip-descendants)
    * [Conflict resolution](#conflict-resolution)
        * [Create conflict tag set](#create-conflict-tag-set)
        * [Fuse selected spots](#fuse-selected-spots)
    * [Sort trackscheme](#sort-trackscheme)
        * [Sort lineage tree (left-right-anchors)](#sort-lineage-tree-left-right-anchors)
        * [Sort lineage tree (extern-intern)](#sort-lineage-tree-extern-intern)
        * [Sort lineage tree (cell life cycle duration)](#sort-lineage-tree-cell-life-cycle-duration)
* [Auxilliary displays](#auxilliary-displays)
    * [Show compact lineage](#show-compact-lineage)
* [Spatial track matching](#spatial-track-matching)
* [Export measurements](#export-measurements)
    * [Export spots counts per lineage](#export-spots-counts-per-lineage)
    * [Export spots counts per timepoint](#export-spots-counts-per-timepoint)
    * [Export lineage lengths](#export-lineage-lengths)

## Documentation of Mastodon

Mastodon Tomancak is an extension of Mastodon. For the full documentation of Mastodon, please visit
[mastodon.readthedocs.io](https://mastodon.readthedocs.io/en/latest/index.html).

## Installation Instructions

* Add the listed Mastodon update sites in Fiji:
    * `Help > Update > Manage update sites`
        1. `Mastodon`
        2. `Mastodon-Tomancak`
           ![Mastodon Update sites](doc/installation/Mastodon.png)

## Spots management

### Transform spots

#### Mirror spots along X-axis

* Menu Location: `Plugins > Spots management > Transform spots > Mirror spots along X-axis`
* The command first calculates the mean x-coordinate of all spots. Then the x-coordinate of each spot is mirrored on the
plane x = mean x. The ellipsoids are mirrored as well. The trackscheme is not modified.
* Important: The command does not support mirroring of the image data. The spots will therefore appear to be in the
  wrong
place.
* Example: ![mirror_spots.gif](doc/spotsmanagement/mirror_spots.gif)

#### Remove isolated spots

* Menu Location: `Plugins > Spots management > Transform spots > Remove isolated spots`
* An isolated spot has no ancestors and no descendants
* The command removes all isolated spots
* Two refining conditions can be set:
    * The spot must appear in the last time point
        * Lonely spots at the end of a video are much harder to find compared to lonely spots at the beginning.
    * The spot's label consists of numbers only
        * A label that does not only consist of numbers has likely been edited by the user. A label that consists of
          number only may indicate a spot that was automatically detected and not linked to a track.
* Example: ![isolated_spots.gif](doc/spotsmanagement/isolated_spots.gif)

#### Add center spot

#### Interpolate missing spots

#### Set radius of selected spots

* Menu Location: `Plugins > Spots management > Transform spots > Set radius of selected spots`
* Set the radius of all selected spots to the same value.
* Example: ![set_radius_selected_spots.gif](doc/spotsmanagement/set_radius_selected_spots.gif)

### Rename spots

#### Label selected spots

#### Change branch labels

#### Systematically label spots (extern-intern)

## Tags

### Locate tags

### Copy tag

### Add tag set to highlight cell divisions

### Create Dummy Tag Set

## Trees management

### Flip descendants

### Conflict resolution

#### Create conflict tag set

#### Fuse selected spots

### Sort trackscheme

#### Sort lineage tree (left-right-anchors)

#### Sort lineage tree (extern-intern)

#### Sort lineage tree (cell life cycle duration)

## Auxilliary displays

### Show compact lineage

## Spatial track matching

## Export measurements

### Export spots counts per lineage

### Export spots counts per timepoint

### Export lineage lengths
