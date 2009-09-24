<?php

error_reporting(E_ALL ^ E_NOTICE);

$terms=trim($_POST["terms"]);

$BASEDIR=dirname($_SERVER["SCRIPT_FILENAME"]);
if ($BASEDIR=="") { $BASEDIR="/var/apache/www/htdocs/mcidas/doc/mcv_guide/working"; }

################################################################################

if ($terms=="") {
  print "";
  exit(0);
}

chdir($BASEDIR);

$grep="/bin/grep -ics";

$results=array();
$terms=preg_split("/\s+/",$terms);
$eterm=implode("|",$terms);
$filestats=explode("\n",
    trim(`$grep "$eterm" *.html */*.html */*/*.html */*/*/*.html 2>/dev/null`));
if (count($filestats)==1 && preg_match("/^\d+$/",$filestats[0])) {
  $singlefile=trim(`ls -1 $name`);
  $filestats[0]=$singlefile.":$filestats[0]";
}

foreach ($filestats as $filestat) {
  list($file,$count)=explode(":",$filestat);
  if ($count==0) { continue; }
  $results["$file"]["count"]+=$count;
  $results["$file"]["name"]=`grep -i "<title>" $file |
      sed -e 's/<\\/\\{0,1\\}[Tt][Ii][Tt][Ll][Ee]>//g'`;
  if ($results["$file"]["name"]=="")
    $results["$file"]["name"]=basename($file);
}

if (!count($results)) {
  print "No results found";
  exit(0);
}

# At this point, we have a sorted array with filenames as keys and branches
#  "count" and "terms" indicating how well they were matched
uasort($results,"pagesorter");
$total=count($array);

print "<table border=0 cellspacing=0 cellpadding=0>\n";
foreach ($results as $page=>$stats) {
  print "<tr><td style=\"width:30px;vertical-align:top;\">";
  print $stats["count"]."</td>\n";
  print "<td><span ";
  print "  class=\"link\" ";
  print "  onMouseOver=\"try{hilite(this)}catch(err){};\" ";
  print "  onMouseOut=\"try{unhilite(this)}catch(err){};\" ";
  print "  onClick=\"setPage('".$page."');\" ";
  print ">".$stats["name"]."</span></td></tr>\n";
}
print "</table>\n";

###############################################################################
# Functions
function pagesorter($a,$b) {
  if ($a["count"]>$b["count"]) { return -1; }
  elseif ($a["count"]<$b["count"]) { return 1; }
  else {
    return(strcmp($a["name"],$b["name"]));
  }
}

?>
