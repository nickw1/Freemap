<?php
// This relies on phpcoord, which can be downloaded from
// http://www.jstott.me.uk/phpcoord/

include('/var/www/qs/phpcoord-2.3.php');

$r=scandir("/home/nick/vmd/tiles");

foreach ($r as $file)
{
    if($file!="." && $file!="..")
    {
        $gr=getOSRefFromSixFigureReference(substr($file,0,2).$file[2]."00".
            $file[3]."00");
        $e=$gr->easting/1000;
        $n=$gr->northing/1000;
        $im=ImageCreateFromPNG("/home/nick/vmd/tiles/$file");
        for($x=0; $x<10; $x++)
        {
            for($y=0; $y<10; $y++)
            {
                $e_km=$e+$x;
                $n_km=$n+$y;
                $im2=ImageCreateTrueColor(400,400);
                ImageCopy($im2,$im,0,0,$x*400,(9-$y)*400,400,400);

                if(!file_exists("tilesout/$e_km"))
                    mkdir("tilesout/$e_km",0755);
                $file_out="tilesout/$e_km/$n_km.png";
                echo "$file_out\n";
                ImagePNG($im2,$file_out);
                ImageDestroy($im2);
            }
        }
        ImageDestroy($im);
    }
}

?>
