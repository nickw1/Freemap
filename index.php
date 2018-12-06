<!DOCTYPE html>
<html>
<head>
<title>FREEMAP - OpenStreetMap walking maps of Britain and Ireland</title>

<?php
require_once('common/defines.php');

if(file_exists(POPULATE_LOCK)) {
    echo "<p>The Freemap database updates just after midnight every Wednesday ".
        "and can take up to 6 hours. During this time the site is not ".
        "available, so please come back later!</p>";
} else {
    $zoom = false;
    if(isset($_GET['zoom'])) {
        if(is_numeric($_GET['zoom']) && $_GET['zoom']>=11 && $_GET['zoom']<=16) {
            $zoom = $_GET['zoom'];    
        } 
    } else {
        $zoom = 15;
    }

    if($zoom!==false) {
?>
<script type='text/javascript'>
var lat=<?php echo isset($_GET['lat']) ? (float)$_GET['lat']: 51.05; ?>;
var lon=<?php echo isset($_GET['lon']) ? (float)$_GET['lon']: -0.72; ?>;
var zoom=<?php echo $zoom; ?>;
</script>
<link rel='stylesheet' href='https://unpkg.com/leaflet@1.3.4/dist/leaflet.css'/>
<script type='text/javascript' src='https://unpkg.com/leaflet@1.3.4/dist/leaflet.js'></script>
<script src='https://unpkg.com/tangram/dist/tangram.min.js'></script>
<script src='fm/js/main.js'></script>
<script src='/jslib/Dialog.js'></script>
<link rel='stylesheet' type='text/css' href='fm/css/style.css' />
</head>
<body onload='init()'>
<div id='sidebar'>


<div id='title'> 
    <div class='titlebox' id='titlebox'>
    <img src='fm/images/freemap_small.png' alt='Freenap logo' /> <br />
    </div>
</div>
<p>OpenStreetMap walking maps of Britain and Ireland
(note: contours currently England and Wales only).
Now using <a href='https://github.com/tangrams/tangram'>Tangram</a>. 
<a href='common/about.html'>More...</a></p>
</div>
<div id='main'>
<div style='margin: 10px'><label for='q'>Search:</label><input id='q' />
<select id='type'>
<option selected>all</option>
<option value='place'>Place (village, town, city etc.)</option>
<option value='amenity'>Amenity (pub, restaurant, cafe etc.)</option>
<option value='natural'>Natural feature (hill, etc.)</option>
<option value='railway'>Railway station</option>
</select>
<input type='button' id='searchBtn' value='Go!' />
</div>
<div id='map'></div>
<?php
    } else {
        echo "ERROR: zoom must be between 11 and 16.";
    }
}
?>
</div>
</body>
</html>
