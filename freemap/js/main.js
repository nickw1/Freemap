
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
                                {designation:null,foot:'permissive'},
                                {designation: 'permissive_footpath' }
                              ],
                        dash: [2,2], colour: 'magenta', width: 2 } 
                ]);
    
    this.lyr=0;
    this.mode=0;

    var urls = [ 'freemap/ws/bsvr.php', 
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

    this.walkrouteLayer=new L.GeoJSON(null,
    {    
        onEachFeature: function(feature,layer)
        {
            if(feature.geometryType=='LineString' &&
                feature.properties.description && 
                feature.properties.title)
            {
                layer.bindPopup
                                ('<h3>' + feature.properties.title +'</h3>' +
                                '<p>'+feature.properties.description+'</p>');
            }
        }
    }); 

    var FootIcon = L.icon (
        {iconUrl: '/freemap/images/foot.png',
        shadowUrl: null,
        iconSize: new L.Point(16,16),
        shadowSize: null,
        iconAnchor: new L.Point(8,15),
        popupAnchor: new L.Point(2,-2) } );

    this.walkrouteStartsLayer = new L.GeoJSON( null,
        { onEachFeature: 
                (function(feature,layer) 
                {
                    if(feature.properties.description && 
                        feature.properties.title)
                    {
                        layer.bindPopup('<h1>' + feature.properties.title + 
                            '</h1>'
                            +'<p>'+feature.properties.description+' ' +
                            '<a href="#" id="_wr_togpx">GPX</a> ' );
                        layer.on("click",(function(ev)
                        { 
                            document.getElementById('_wr_togpx').onclick = 
                                function() { 
                                    window.location=
                                    '/freemap/ws/wr.php?action=get&id='+
                                    feature.properties.id+'&format=gpx' 
                                };
                            this.wrViewMgr.loadRoute(feature.properties.id);
                        
                        } ).bind(this));
                    }
                }).bind(this) ,

            pointToLayer: function(geojson,latlng) {
                return new L.Marker(latlng, { icon: FootIcon } 
                        );
            }
        });
    
    this.map.addLayer(this.walkrouteStartsLayer);
    this.map.addLayer(this.walkrouteLayer);
    this.wrAddMgr=new WRAddMgr(this.walkrouteLayer,'main');
    this.wrViewMgr = new WRViewMgr(this);

    this.modes = [ { name: "Normal" },
                    { name: "Walk route",
                        submenu:  [
                            { name: "New", action:this.wrAddMgr.newWR.bind
                                (this.wrAddMgr) },
                            { name: 'Undo last point',
                                action: this.wrAddMgr.undo.bind
                                    (this.wrAddMgr) },
                            { name: 'Add stage descriptions', 
                                action: (function(e)
                                    { 
                                        this.wrAddMgr.mode=
                                            e.target.firstChild.nodeValue==
                                            'Add stage descriptions' ? 1:0;
                                        e.target.firstChild.nodeValue =
                                            e.target.firstChild.nodeValue ==
                                            'Add stage descriptions' ?
                                            'Finish adding stages' :
                                            'Add stage descriptions';
                                    } 
                                    ).bind(this) },
                            { name: "Save", action:this.wrAddMgr.wrDone.bind
                                (this.wrAddMgr) },
                                ]
                    } ]

    this.setupModes();
    this.map.setView(new L.LatLng(lat, lon), 0);
    this.map.on("click",this.onMapClick.bind(this));
    this.walkrouteStartsLoader = new FeatureLoader
        ('/freemap/ws/wr.php',
        this.walkrouteStartsLayer,
        'action=getByBbox&format=json');
    this.walkrouteStartsLoader.loadFeatures(this.map.getBounds());
    if(document.getElementById('loginbtn'))
    {
        document.getElementById('loginbtn').onclick =
            this.doLogin.bind(this);
    }
	var func = function() { this.walkrouteStartsLoader.loadFeatures
		(this.map.getBounds()); };

	this.map.on("dragend", (function()
		{
			this.walkrouteStartsLoader.loadFeatures
				(this.map.getBounds());
		}).bind(this));
}

Footpaths.prototype.setLocation = function(lon,lat)
{
    this.map.setView(new L.LatLng(lat,lon), 0);
    this.walkrouteStartsLoader.loadFeatures(this.map.getBounds());
}

Footpaths.prototype.setupModes = function()
{
    var div = document.getElementById("modebar");
    for(var i=0; i<this.modes.length; i++)
    {
        var modeDiv = document.createElement("a");
        modeDiv.href='#';
        modeDiv.id = "_mode" + i;
        modeDiv.innerHTML = this.modes[i].name;
        modeDiv.onclick = function(e)
        {
            document.getElementById("_mode"+this.mode).removeAttribute("class");
            e.target.setAttribute("class","selected");
            var oldmode=this.mode;
            this.mode = e.target.id.substring(5);
            if(this.mode==1 && oldmode!=1)
            {
                //this.map.removeLayer(this.geojsonLayer);
                this.map.removeLayer(this.walkrouteStartsLayer);
            }
            else if (oldmode==1 && this.mode!=1)
            {
                //this.map.addLayer(this.geojsonLayer);
                this.map.addLayer(this.walkrouteStartsLayer);
            }
        }.bind(this);
        if(this.modes[i].submenu)
        {
            var childDiv=document.createElement("div");
            for(var j=0; j<this.modes[i].submenu.length; j++)
            {
                var a = document.createElement("a");
                a.href='#';
                a.id = "_mode"+i+"_opt"+j;
                a.innerHTML = this.modes[i].submenu[j].name;
                a.onclick = ( function(e)
                                {
                                    var m=e.target.id[5];
                                    var n = e.target.id[10];
                                    e.stopPropagation();
                                    this.modes[m].submenu[n].action(e);
                                }
                            ).bind(this);
                childDiv.appendChild(a);
                childDiv.appendChild(document.createElement("br"));
            }
            modeDiv.appendChild(childDiv);
        }
        div.appendChild(modeDiv);
    }
    document.getElementById("_mode"+this.mode).setAttribute("class","selected");
}

Footpaths.prototype.onMapClick = function(e)
{
    var p = this.map.layerPointToContainerPoint
        (this.map.latLngToLayerPoint(e.latlng));
    this.mapClickPos=e.latlng;

    switch(parseInt(this.mode))
    {
        case 0:
            /*
            if(this.loggedIn===true)
            {
                this.dlg.setPosition((p.x<500 ? p.x:500),(p.y<500 ? p.y:500));
                if(!this.dlg.isVisible())
                {
                    this.dlg.show();
                    this.map.off('click',this.onMapClick,this);
                }
                document.getElementById('annotation').focus();
            }
            else
            {
                alert("Must be logged in to add annotation.");
            }
            */
            break;

        case 1:
            this.wrAddMgr.handleClick(e.latlng);
            break;
    }
}

Footpaths.prototype.onDragEnd=function(e)
{
    var x=Math.round(this.map.getCenter().lng*1000000),
        y=Math.round(this.map.getCenter().lat*1000000);
    //history.pushState({x:x,y:y},"","?x="+x+"&y="+y);
    this.walkrouteStartsLoader.loadFeatures(e.target.getBounds());
}

Footpaths.prototype.doLogin = function()
{
    new Ajax().sendRequest('freemap/user.php',
                { parameters: 'action=login&remote=1&username='+
                    document.getElementById('username').value + 
                    '&password='+ 
                    document.getElementById('password').value ,
                    method : 'POST',
                    callback: this.loginSuccess.bind(this),
                    errorCallback: function(code){alert('Incorrect login'); } }
                    );
}

Footpaths.prototype.loginSuccess = function(xmlHTTP)
{
    document.getElementById('logindiv').innerHTML = 
        '<em>Logged in as ' + xmlHTTP.responseText + 
        '</em> <a href="#" id="myroutes">My routes</a> | '+
        '<a href="freemap/user.php?action=logout&redirect=/index.php">'+
        'Log out</a>';
    this.loggedIn = true;
    this.setupMyRoutes();
}

Footpaths.prototype.setupMyRoutes = function()
{

    document.getElementById('myroutes').onclick = this.wrViewMgr.sendRequest.
        bind(this.wrViewMgr);
        
}

Footpaths.prototype.setLoggedIn = function(status)
{
    this.loggedIn = status;
}

function init()
{
    var app = new Footpaths();
    document.getElementById('lyr').onchange = function(e)
      {
        app.lyr = e.target.selectedIndex;
        app.osLayer.redraw();
      }
     app.setLoggedIn(loggedIn);
     if(loggedIn)
         app.setupMyRoutes();
    new SearchWidget ('search',
                        { url: 'freemap/ws/search.php',
                          callback: app.setLocation.bind(app),
                          parameters: 'poi=all&outProj=4326'},
                          { overflow: "auto",
                              maxHeight: "200px"}
                          );
}
