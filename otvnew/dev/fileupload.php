<?php
session_start();
?>
<!DOCTYPE html>
<html>
<head>
<link rel="stylesheet" type="text/css" href="../css/otv.css" />
<style type="text/css">
progress
{
	background-color:#c0c0ff;
	border-radius:10px;
}

progress::-moz-progress-bar
{
	background-color:#0000ff;
	border-radius:10px;
}

progress::-webkit-progress-bar
{
	background-color:#c0c0ff;
	border-radius:10px;
}

progress::-webkit-progress-value
{
	background-color:#0000ff;
	border-radius:10px;
}
</style>
<title>Upload a Photosphere</title>
<script type="text/javascript">

function init()
{
	document.getElementById("btn1").addEventListener
		("click", fileUpload, false);
}

function fileUpload()
{
	var file = document.getElementById("file1").files[0];
	alert(file.size);
	var formData = new FormData();
	formData.append("file1", file);
	var ajax = new XMLHttpRequest();
	ajax.addEventListener("load",onComplete,false);
	ajax.addEventListener("error",onError,false);
	ajax.addEventListener("abort",onAbort,false);
	ajax.upload.addEventListener("progress",onProgress,false);
	ajax.open("POST", "panosubmit.php");
	ajax.send(formData);
}

function onComplete(e)
{
	document.getElementById("progress2").innerHTML = e.target.responseText;
}

function onProgress(e)
{
	var pct = Math.round (e.loaded/e.total * 100);
	document.getElementById("progress2").innerHTML =
		"Uploaded : " + e.loaded + " total:" + e.total + " ("+pct+"%)";
	document.getElementById("progress1").value = Math.round(pct);
}

function onError(e)
{
	alert(e.target);
}

function onAbort(e)
{
	alert(e.target);
}

</script>
</head>
<body onload="init()">

<?php

if(!isset($_SESSION["gatekeeper"]))
{
	echo "You need to be logged in to upload photospheres.";
}
else
{
	?>
	<h1>Upload a Photosphere</h1>
	<form method="post" enctype="multipart/form-data">
	<input type="file" id="file1" />
	<input type="button" value="upload" id="btn1" />
	<progress id="progress1" value="0" max="100" style="width: 400px">
	</progress>
	<div id="progress2"></div>
	</form>
	<?php
}
?>
</body>
</html>
