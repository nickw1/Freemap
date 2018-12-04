
function Footpaths()
{

    var crs = 
        L.CRS.proj4js('EPSG:27700',
             "+proj=tmerc +lat_0=49 +lon_0=-2 "+
             "+k=0.9996012717 +x_0=400000 +y_0=-100000 "+
             "+ellps=airy +datum=OSGB36 +units=m +no_defs",
             new L.Transformation(1, 0, -1, 0) );
    crs.scale = function(){ return 0.2; } 

    this.map = new L.Map("map", { crs: crs} );

    this.styles = new Styles (
                [
                    {  rules: 
                                [
                                    {designation:'public_footpath'},
                                    {row_type: 'Footpath' }
                                ],
                        colour: 'red',
                        dash: [2,2],
                        width: 2 } ,
                    {  rules: 
                                [
                                    {designation:'public_bridleway'},
                                    {row_type: 'Bridleway' }
                                ],
                        colour: 'red',
                        dash: [6,2],
                        width: 2} ,
                    {  rules: 
                                [
                                    {designation:'byway_open_to_all_traffic'},
                                    {designation:'public_byway'},
                                    {designation:'restricted_byway'},
                                    {designation:'byway'},
                                    {designation: 'unknown_byway' },
                                    {row_type:'BOAT'},
                                    {row_type:'Restricted Byway'}
                                ],

                        colour: 'red',
                        dash: [3,3],
                        width: 4} ,
                    {  rules: [
                                {designation:null,foot:'permissive',
									bicycle: null },
                                {designation: 'permissive_footpath',
									bicycle: null},
                                {designation:null,foot:'permissive',
									bicycle: 'no' },
                                {designation: 'permissive_footpath',
									bicycle: 'no'}
                              ],
                        dash: [2,2], colour: 'magenta', width: 2 } ,
                    { rules: 
                            [
                                { designation: null, highway: 'cycleway' },
                                { designation: null, highway: 'track',
									bicycle: 'yes' },
                                { designation: null, highway: 'path',
									bicycle: 'yes' },
                                { designation: null, highway: 'footway',
									bicycle: 'yes' },
                                { designation: null, highway: 'track',
									bicycle: 'permissive' },
                                { designation: null, highway: 'path',
									bicycle: 'permissive' },
                                { designation: null, highway: 'footway',
									bicycle: 'permissive' }
                            ],
                        dash: [2,2], colour: 'blue', width: 2 } ,

                    { rules: 
                            [
                                { designation:null, highway:'path' },
                                {designation:null, highway:'footway' }
                            ],    
                            dash: [2,2], colour: 'black', width:1 },
                    { rules: 
                            [
                                {designation:null, highway:'track' }
                            ],    
                            dash: [6,2], colour: 'black', width:1 },
                ]);
    
    this.lyr=0;
    this.mode=0;

    var urls = [ '/0.6/ws/bsvr.php', 
                  'http://www.fixmypaths.org/hampshire.php' ];

    var qs = [ 'format=geojson&inProj=27700&outProj=27700&way=all',
               'format=geojson&inProj=27700&outProj=27700' ];
    
    this.osLayer = new L.TileLayer.Canvas (
        { tms:true, maxZoom:0, minZoom:0, tileSize: 200, continuousWorld:true,
            attribution:"&copy; Ordnance Survey, OS OpenData Licence"} ); 

    this.osLayer.drawTile = (function(canvas,tilePoint,zoom)
        {
            var ctx = canvas.getContext("2d");
            var x = tilePoint.x;
            var y = -tilePoint.y;
            
            var t = new Tile(x, y, 0.2);
            var fpRenderer = new FootpathRenderer(ctx,t,this.styles);
            var img = new Image();
            img.onload = (function()
            {
                ctx.drawImage(img,0,0);
                var parms = qs[this.lyr] + '&' + t.getBboxString();

                new Ajax().sendRequest
                    (urls[this.lyr],
                        { method: 'GET',
                         parameters:  parms,
                        callback: function(xmlHTTP)
                            {
                                //status(xmlHTTP.responseText);
                                var json = JSON.parse(xmlHTTP.responseText);
                                for (var i=0; i<json.features.length; i++)
                                {
                                    fpRenderer.drawPath(json.features[i]);
                                }
                            }
                        }
                    );
            }).bind(this);
            img.src=
                'http://www.free-map.org.uk/lfp/0/'+tilePoint.x+'/'+
                    (-tilePoint.y)+'.png';
        } ).bind(this);

    this.map.addLayer(this.osLayer);

    this.map.setView(new L.LatLng(lat, lon), 0);
}

Footpaths.prototype.setLocation = function(lon,lat)
{
    this.map.setView(new L.LatLng(lat,lon), 0);
}

function init()
{
    var app = new Footpaths();
    document.getElementById('lyr').onchange = function(e)
      {
        app.lyr = e.target.selectedIndex;
        app.osLayer.redraw();
      }
}
