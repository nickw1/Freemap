<?php
require_once("../lib/latlong.php");
require_once("../lib/functionsnew.php");

define('HGTDIR', '/var/www/data/hgt');

// note $w $s $e $n in microdegrees
  
list($w,$s,$e,$n) = explode(",", $_GET["bbox"]);

if(!getSRTM($w,$s,$e,$n))
{
        header("HTTP/1.1 400 Bad Request");
        echo "Bad request";
}

function getSRTM($w,$s,$e,$n)
{
    $wd = $w/1000000; $ed = $e/1000000; $sd = $s/1000000; $nd = $n/1000000;

    $npts = 1201;
    $step = 1/1200;


    if($w % 5000 || $s % 5000 || $e % 5000 || $n % 5000 ||
        $w>=$e || $s >= $n ||
        floor ($wd) != floor($ed-0.000001) ||
        floor ($sd) != floor($nd-0.000001) )
    {
        return false;
    }

    $idx_w =  round(($wd-floor($wd))/$step);
    $idx_e =  round(($ed-floor($wd))/$step);
    $idx_n =  round((ceil($nd)-$nd)/$step);
    $idx_s =  round((ceil($nd)-$sd)/$step);

    $idx_e = ($idx_e==0) ? $npts-1: $idx_e;
    $idx_s = ($idx_s==0) ? $npts-1: $idx_s;

    $width = ($idx_e - $idx_w) + 1;

    $lon = floor($wd);
    $lat = floor($sd);

    $file = sprintf(HGTDIR . "/%s%02d%s%03d.hgt", 
                ($lat<0 ? "S":"N"),
                ($lat<0 ? -$lat:$lat),
                ($lon<0 ? "W":"E"),
                ($lon<0 ? -$lon:$lon) );
    
    //echo "file $file <br />";
    //echo "indices w $idx_w s $idx_s e $idx_e n $idx_n <br />";

    $fp = fopen($file,"r");
    $str = "";
    if($fp)
    {
        for($row=$idx_n; $row<=$idx_s; $row++)
        {
            fseek($fp,($row*$npts+$idx_w)*2);
            $bytes=fread($fp,$width*2);
            //echo "Read ". strlen($bytes). " bytes<br />";
            $str .= $bytes;
        }

        //echo "totla length = ".strlen($str)." <br />";
        fclose($fp);
    }
    echo $str;
    return true;
}
?>
