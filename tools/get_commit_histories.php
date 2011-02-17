#!/usr/bin/php
<?php

#cvs checkout mc-v
#svn co http://svn.ssec.wisc.edu/repos/visad
#svn co http://svn.unidata.ucar.edu/repos/idv

$MCV_ROOT = "/home/mcidasv/mc-v";
$VISAD_ROOT = "/home/mcidasv/svn_nightly/visad";
$IDV_ROOT = "/home/mcidasv/svn_nightly/idv";

$DATE = date("Y-m-d");
$END = false;
$HTML = false;

$gotopt = false;
$options = getopt("s:e:h");
if (isset($options["s"])) {
  $gotopt = true;
  $DATE = date("Y-m-d", strtotime($options["s"]));
}
if (isset($options["e"])) {
  $gotopt = true;
  $END = date("Y-m-d", strtotime($options["e"]));
}
if (isset($options["h"])) {
  $gotopt = true;
  $HTML = true;
}

if (!$gotopt) {
  print "Usage: $argv[0] [-s (start: YYYY-MM-DD)] [-e (end: YYYY-MM-DD)] [-h (html)]\n";
  exit(0);
}

$allCommits = array();

# Print HTML header
if ($HTML) {
  print "<html><head><title>";
}
print "Commits for date ";
if ($DATE == $END) {
  print $DATE;
}
else {
  print "range ".$DATE." to ".$END;
}
if ($HTML) {
  print "</title>\n";
  print "<style>\n";
  print ".mcidas-v { color: darkblue; }\n";
  print ".visad { color: darkred; }\n";
  print ".idv { color: darkgreen; }\n";
  print "</style>\n";
  print "</head><body><pre>\n";
}

# Get the diffs from McV (CVS)
if (!$HTML) {
  print "Getting commit logs from McIDAS-V CVS...\n";
}
$commits = getCVSCommits($MCV_ROOT, $DATE, $END);
foreach ($commits as $date=>$commitArray) {
  if (!isset($allCommits["$date"])) {
    $allCommits["$date"] = array();
  }
  foreach ($commitArray as $commit) {
    $commit["package"] = "McIDAS-V";
    array_push($allCommits["$date"], $commit);
  }
}

# Get the diffs from VisAD (SVN)
if (!$HTML) {
  print "Getting commit logs from VisAD SVN...\n";
}
$commits = getSVNCommits($VISAD_ROOT, $DATE, $END);
foreach ($commits as $date=>$commitArray) {
  if (!isset($allCommits["$date"])) {
    $allCommits["$date"] = array();
  }
  foreach ($commitArray as $commit) {
    $commit["package"] = "VisAD";
    array_push($allCommits["$date"], $commit);
  }
}

# Get the diffs from IDV (SVN)
if (!$HTML) {
  print "Getting commit logs from IDV SVN...\n";
}
$commits = getSVNCommits($IDV_ROOT, $DATE, $END);
foreach ($commits as $date=>$commitArray) {
  if (!isset($allCommits["$date"])) {
    $allCommits["$date"] = array();
  }
  foreach ($commitArray as $commit) {
    $commit["package"] = "IDV";
    array_push($allCommits["$date"], $commit);
  }
}

# Print all commits ordered by date
ksort($allCommits);
foreach ($allCommits as $date=>$commits) {
  foreach ($commits as $commit) {
    $date = $commit["date"];
    $revision = $commit["revision"];
    $author = $commit["author"];
    $file = $commit["file"];
    $package = $commit["package"];
    $description = $commit["description"];
    if ($HTML) {
      print "<div class=\"".strtolower($package)."\">";
    }
    print "$date\t$package\t$file\t$revision\t$author\n";
    print $description."\n\n";
    if ($HTML) {
      print "</div>\n";
    }
  }
}

if ($HTML) {
  print "</pre></body></html>\n";
}

################################################################################
# Functions

function getCVSCommits($DIR, $DATE, $END=false) {
  if (!@chdir($DIR)) {
    print "ERROR: Failed to change to directory: ".$DIR."\n";
    exit(1);
  }
  # Don't explicitly update.  Assume it is current.
  # This is looking at the build dir, we shouldn't mess with that here.
  $allLogs = array();
  $filelist = `cvs diff --brief -D $DATE 2>/dev/null |grep "^Index" |sed -e 's/^Index: //'`;
  $endstamp = strtotime($END);
  foreach (explode("\n", $filelist) as $file) {
    $file = trim($file);
    if (!$file) {
      continue;
    }
    $logs = getCVSLog($file, $DATE, $END);
    foreach ($logs as $date=>$logArray) {
      if ($END && strtotime($date) > $endstamp) {
        continue;
      }
      if (!isset($allLogs["$date"])) {
        $allLogs["$date"] = array();
      }
      foreach ($logArray as $log) {
        array_push($allLogs["$date"], $log);
      }
    }
  }
  return $allLogs;
}

function getCVSLog($FILE, $DATE, $END=false) {
  $linelist = `cvs log -d">=$DATE" $FILE 2>/dev/null`;
  $in_description = false;
  $revision = 0;
  $date = 0;
  $author = "";
  $description = "";
  $log = array();
  $logs = array();
  foreach (explode("\n", $linelist) as $line) {
    $line = trim($line);
    if ($line == "") {
      continue;
    }
    if (!$in_description && preg_match("/^description:/", $line)) {
      $in_description = true;
    }
    if (!$in_description) {
      continue;
    }

    # Now we look for each revision commit
    if (preg_match("/^revision /", $line)) {
      $revision = preg_replace("/^revision /", "", $line);
      continue;
    }
    if (preg_match("/^date: /", $line)) {
      $words = preg_split("/\s+/", $line);
      $date = preg_replace("/\//", "-", $words[1]);
      $author = preg_replace("/;/", "", $words[4]);
      continue;
    }
    if (preg_match("/^----/", $line) ||
        preg_match("/^====/", $line)) {

      # We should have a complete record now
      if ($revision) {
        $description = preg_replace("/\[\d+\]/", "", $description);
        $description = trim($description);
        $log["date"] = $date;
        $log["revision"] = $revision;
        $log["author"] = $author;
        $log["file"] = $FILE;
        $log["description"] = $description;
        if (!isset($logs["$date"])) {
          $logs["$date"] = array();
        }
        array_push($logs["$date"], $log);
      }

      $revision = 0;
      $date = 0;
      $author = "";
      $description = "";
      $log = array();
      continue;
    }

    # Description line
    $description .= $line."\n";
  }

  return $logs;
}

function getSVNCommits($DIR, $DATE, $END=false) {
  if (!@chdir($DIR)) {
    print "ERROR: Failed to change to directory: ".$DIR."\n";
    exit(1);
  }
  `svn update -r HEAD 2>/dev/null`;
  $allLogs = array();
  if (!$END) {
    $datespec = "\{$DATE\}";
  }
  else {
    # Add one day to be inclusive on the end
    $END = date("Y-m-d", strtotime($END) + (60*60*24));
    $datespec = "\{$DATE\}:\{$END\}";
  }
  $filelist = `svn diff -r $datespec --diff-cmd "diff" -x "-q" . 2>/dev/null | grep Index | cut -d " " -f 2`;
  foreach (explode("\n", $filelist) as $file) {
    $file = trim($file);
    if (!$file) {
      continue;
    }
    $logs = getSVNLog($file, $DATE, $END);
    foreach ($logs as $date=>$logArray) {
      if (!isset($allLogs["$date"])) {
        $allLogs["$date"] = array();
      }
      foreach ($logArray as $log) {
        array_push($allLogs["$date"], $log);
      }
    }
  }
  return $allLogs;
}

function getSVNLog($FILE, $DATE, $END=false) {
  if (!$END) {
    $TODAY = date("Y-m-d");
    $datespec = "\{$DATE\}:\{$TODAY\}";
  }
  else {
    $datespec = "\{$DATE\}:\{$END\}";
  }
  $linelist = `svn log -r $datespec $FILE 2>/dev/null`;
  $revision = 0;
  $date = 0;
  $author = "";
  $description = "";
  $log = array();
  $logs = array();
  foreach (explode("\n", $linelist) as $line) {
    $line = trim($line);
    if ($line == "") {
      continue;
    }

    # Look for each revision commit
    if (preg_match("/^r\d+\s+\|/", $line)) {
      $words = preg_split("/\|/", $line);
      $revision = preg_replace("/^r/", "", trim($words[0]));
      $author = preg_replace("/;/", "", trim($words[1]));
      $date = preg_replace("/\s.*/", "", trim($words[2]));
      continue;
    }
    if (preg_match("/^----/", $line) ||
        preg_match("/^====/", $line)) {

      # We should have a complete record now
      if ($revision) {
        $description = preg_replace("/\[\d+\]/", "", $description);
        $description = trim($description);
        $log["date"] = $date;
        $log["revision"] = $revision;
        $log["author"] = $author;
        $log["file"] = $FILE;
        $log["description"] = $description;
        if (!isset($logs["$date"])) {
          $logs["$date"] = array();
        }
        array_push($logs["$date"], $log);
      }

      $revision = 0;
      $date = 0;
      $author = "";
      $description = "";
      $log = array();
      continue;
    }

    # Description line
    $description .= $line."\n";
  }

  return $logs;
}
