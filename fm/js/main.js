


function init(loggedIn)
{
 
 var freemap = {
  initialise: function (lat, lon, zoom, loggedIn) 
  {
    var url = "/fm/ws/tsvr.php?x={x}&y={y}&z={z}&way=all&poi=all"+
                "&kothic=1&contour=1&coastline=1";

    var kothic = new L.TileLayer.Kothic
            ( url, 
                            { minZoom: 11, attribution:
                    "&copy; 2004-" + new Date().getFullYear() + 
                    " OpenStreetMap Contributors, " +
                    "Open Database Licence, contours &copy; " +
                    "Crown Copyright and database right Ordnance Survey "+
                    "2011, Rendering by "+
                    "<a href='https://github.com/kothic/kothic-js'>"+
                    "Kothic JS</a>" } 
            );

    var mql = window.matchMedia("(max-device-aspect-ratio: 2/3)");
    mql.addListener( this.onOrientationChange.bind(this));
    this.onOrientationChange(mql);

    this.markersLayer = new L.GeoJSON( null,
        { 
            onEachFeature: (function (feature, layer)
            {
                layer.bindPopup(feature.properties.text);
                layer.id = feature.properties.id;    
                this.addAnnotationEditDeleteHandlers(layer);

                // !!! IMPORTANT !!!
                // To be able to edit/remove a feature with Leaflet.Draw
                // you have to add it to the layer group
                this.drawnItems.addLayer(layer);
            } ).bind(this) 
        } );

    this.walkrouteLayer = new L.GeoJSON ( null,
        {
            onEachFeature: (function(feature,layer)
            {
                layer.id = feature.properties.id;
                if(feature.geometry.type == 'LineString' && 
                    feature.properties.description &&
                    feature.properties.title)
                {
                    layer.bindPopup
                        ('<h3>' + feature.properties.title + '</h3>' +
                        '<p>' + feature.properties.description +'</p>' +
                        '<p><strong>Distance: </strong>' +
                        parseFloat(feature.properties.distance).toFixed(2) +
                        'km</p>');
                    this.drawnItems.addLayer(layer);
                    this.walkroutes[feature.properties.id] = layer;
                    this.addWalkrouteEditDeleteHandlers(layer);
                }
            } ).bind(this)
        } );

    var footIcon = L.icon (
        {iconUrl: '/fm/images/foot.png',
        shadowUrl: null,
        iconSize: new L.Point(16,16),
        shadowSize: null,
        iconAnchor: new L.Point(8,15),
        popupAnchor: new L.Point(2,-2) } );

    this.walkrouteStarts = [];
    this.walkroutes = [];

    this.walkrouteWaypointsLayer = new L.LayerGroup();

    this.walkrouteStartsLayer = new L.GeoJSON( null,
        { 
            onEachFeature: (function (f, layer)
            {
                if(f.properties.description && f.properties.title)
                {
                    layer.bindPopup('<h1>' + f.properties.title + '</h1>'+
                                    '<p>' + f.properties.description + '</p>' +
                                    '<p><strong>Distance: </strong>' +
                                    parseFloat(f.properties.distance).toFixed(2)
                                     + 'km ' +
                                    '<a href="#" id="_wr_togpx">GPX</a>');
                    this.walkrouteStarts[f.properties.id] = layer;
                    this.drawnItems.addLayer(layer);
                    layer.on("click", (function(ev)
                        {
                            if(!this.deleting)
                            {
                                document.getElementById("_wr_togpx").
                                    addEventListener ("click",
                                        function()
                                        {
                                            window.location=
                                            '/fm/ws/wr.php?action=get&id='+
                                            f.properties.id+'&format=gpx';
                                        } );
                                this.wrViewMgr.loadRoute(f.properties.id);
                            }
                        } ).bind(this));
                    layer.addToDelList = function(deletionList)
                    {
                        deletionList.walkroutes.push([layer,true]);
                    };
                    layer.id=f.properties.id;
                }
            }).bind(this),

            pointToLayer: function(geojson,latlng)
            {
                return new L.Marker(latlng, { icon: footIcon } );
            }
        }    );

    this.wrViewMgr = new WRViewMgr(this);

    this.map = L.map ("map", {layers:
        [kothic, this.markersLayer, this.walkrouteStartsLayer,
            this.walkrouteLayer, this.walkrouteWaypointsLayer] } );

    L.control.layers ( null,
                        { "Annotations" : this.markersLayer,
                           "Walk routes": L.layerGroup
                            ( [this.walkrouteStartsLayer, 
                              this.walkrouteLayer,
                              this.walkrouteWaypointsLayer] ) } ).
                        addTo(this.map);

    this.drawnItems = new L.FeatureGroup();
    this.map.addLayer(this.drawnItems);

    L.drawLocal.draw.toolbar.buttons.polyline = 'Draw walk route';
    L.drawLocal.draw.toolbar.buttons.marker = 'Add annotation';

    this.drawControl = new L.Control.Draw ( { 
            draw: {
            polyline:
                { shapeOptions: { color: "blue" } },
            polygon: false,
            rectangle: false,
            circle: false
                    }, 
            edit: { featureGroup: this.drawnItems } 
            } );

    if(loggedIn)
    {
        this.map.addControl(this.drawControl);
        document.getElementById('myroutes').addEventListener("click", 
           this.wrViewMgr.sendRequest.bind(this.wrViewMgr));
   }

    this.map.on('draw:created', (function (e)
        {
            switch(e.layerType)
            {
                case "marker":
                    this.onAddMarker(e.layer);
                    this.addAnnotationEditDeleteHandlers(e.layer);
                    break;

                case "polyline":
                    this.drawnItems.addLayer(e.layer);

                    this.sendDlg = new Dialog ( 'main',
                         { ok: this.saveWalkrouteToServer.bind(this,e.layer),
                              cancel:   (function() 
                                            {
                                                this.sendDlg.hide();
                                                this.drawnItems.removeLayer
                                                    (e.layer);
                                            }).bind(this)
                        },
                        { backgroundColor: '#8080ff',
                            width: '400px' ,
                            height: '400px',
                            color: 'white',
                            borderRadius: '15px' }
                            );
                    this.sendDlg.setContent  
                ("<h3>Please enter details of your walk:</h3><p>Walk title: "+
            "<input id='_wrmgr_wtitle' /></p>"+
            "<p>Description:<br /><textarea id='_wrmgr_wdesc' " +
            "style='margin: 10px 10px 10px 10px;width:360px;height:150px'>"+
            "</textarea></p>" );

                this.sendDlg.setPosition("100px","100px");
                this.sendDlg.show();
                this.addWalkrouteEditDeleteHandlers(e.layer);
            }
        }).bind(this));

    var app = this;


    this.map.on('draw:edited', function (e)
        {
            e.layers.eachLayer (function(layer) 
                {
                    if(layer.edit)
                        layer.edit();
                } );
            // edit the polyline
        } );
    this.map.on('draw:deleted', (function (e)
        {
            e.layers.eachLayer ( (function(layer)
                    {
                        layer.addToDelList(this.deletionList);
                    }).bind(this));
            this.deletionList.doDelete();
        }).bind(this));

    this.deleting=false;

    this.map.on('draw:deletestart', 
        (function() { this.deleting=true; }).bind(this));
    this.map.on('draw:deletestop', 
        (function() { this.deleting=false; }).bind(this));

    this.annotationLoader = new FeatureLoader
        ("/fm/ws/bsvr.php", this.markersLayer, "format=json&ann=1");
    this.walkrouteStartsLoader = new FeatureLoader
        ("/fm/ws/wr.php", this.walkrouteStartsLayer, 
            "action=getByBbox&format=json");

    if(lat===null)
    {
        lat = (window.localStorage &&
                window.localStorage.getItem("lat")!=null) 
                ? window.localStorage.getItem("lat") : 51.05;
    }
    if(lon===null)
    {
        lon = (window.localStorage &&
                window.localStorage.getItem("lon")!=null) 
                ? window.localStorage.getItem("lon") : -0.72;
    }
    if(zoom===null)
    {
        zoom = (window.localStorage &&
                window.localStorage.getItem("zoom")!=null) 
                ? window.localStorage.getItem("zoom") : 14;
    }
    this.map.setView(new L.LatLng(lat, lon), zoom);
    this.map.on("dragend",  (function(e)
            {
                var bounds = e.target.getBounds();
                this.annotationLoader.loadFeatures(bounds);
                this.walkrouteStartsLoader.loadFeatures(bounds);
                this.saveLocation();
            } ).bind(this));


    if(!this.isMob)
    {
        new SearchWidget ('searchdiv',
                { url: '/fm/ws/search.php',
                    callback: this.setLocation.bind(this),
                    parameters: 'poi=all&outProj=4326' } );
    }

    this.walkroutesDlg = new Dialog ('main',
                { ok: (function() { this.walkroutesDlg.hide(); } ).bind(this) },
                 { backgroundColor: '#8080ff',
                    width: '200px',
                    height: '150px',
                    color: 'white',
                    opacity: '0.5',
                    borderRadius: '15px' }
                );
    this.walkroutesDlg.setPosition("100px", "100px");
    
    this.loggedIn = loggedIn;


    if(document.getElementById('loginbtn'))
    {
        document.getElementById('loginbtn').addEventListener ("click",
            this.doLogin.bind(this));
    }


    this.map.on("viewreset", (function(e) 
        { 
            if(this.map.getZoom()<=13)
                this.map.removeLayer(this.markersLayer);
            else
                this.map.addLayer(this.markersLayer);

            if(this.map.getZoom()<=11)
                this.map.removeLayer(this.walkrouteStartsLayer);
            else
                this.map.addLayer(this.walkrouteStartsLayer);

        } ).  bind(this) );
  
    var bounds = this.map.getBounds();
    this.annotationLoader.loadFeatures(bounds);
    this.walkrouteStartsLoader.loadFeatures(bounds);
  
    this.deletionList = {
        annotations: [],
        walkroutes: [],
        waypoints: [],
        doDelete: function()
        {
            var wrIds = new Array(), wpIds = new Array();
            for(var i=0; i<this.walkroutes.length; i++)
                wrIds.push(this.walkroutes[i][0].id);
            for(var i=0; i<this.waypoints.length; i++)
                wpIds.push(this.waypoints[i].id);
            var annJson = JSON.stringify(this.annotations);
            var wrJson = JSON.stringify(wrIds);
            var wpJson = JSON.stringify(wpIds);
    
            if(this.annotations.length>0)
            {    
                this.sendPointDeletionRequest 
                    (annJson, "/fm/ws/annotation.php", "deleteMulti",
                         (function()
                            { 
                                alert("Annotation(s) deleted!");
                                this.annotations = [];
                            } ).bind(this));
            }
            if(this.waypoints.length>0)
            {
                this.sendPointDeletionRequest
                    (wpJson, "/fm/ws/wr.php", "deleteMultiWaypoints",
                        (function(e)
                            {
                                var deleted = JSON.parse(e.target.responseText);
                                var nUndeleted = this.waypoints.length-
                                    deleted.length;
                                if(nUndeleted>0)
                                {
                                    alert(nUndeleted + " walk route waypoints "+
                                          "could not be deleted as the walk "+
                                          "route(s) are not yours!");
                                }
                                else
                                    alert("Walk route waypoints deleted!");
                                this.deleteWaypointCallback(deleted);
                            } ).bind(this));}

            if(this.walkroutes.length>0)
            {
                var xhr2_2 = new XMLHttpRequest();
                xhr2_2.addEventListener ("load", (function(e) 
                { 
                    if(e.target.status==200)
                    {
                        var deleted = JSON.parse(e.target.responseText);
                        var nUndeleted = wrIds.length-deleted.length;
                        if(nUndeleted>0)
                            alert(nUndeleted + ' walk routes not deleted ' +
                                    'as they are not yours!');
                        else
                            alert('Walk route(s) deleted!');    
                        this.deleteWalkrouteCallback(deleted);
                        this.walkroutes=[];
                    }
                    else
                        alert('Server error: ' + e.target.status);
                }).bind(this));
                xhr2_2.open("POST","/fm/ws/wr.php");
                var data = new FormData();
                data.append("ids", wrJson);
                data.append("action", "deleteMulti");
                xhr2_2.send(data);
            }
        },

        sendPointDeletionRequest: function(json,url,action,callback)
        {
                var xhr2 = new XMLHttpRequest();
                xhr2.addEventListener ("load", (function(e) 
                { 
                    if(e.target.status==200)
                        callback(e);
                    else
                        alert('Server error: ' + e.target.status);
                }).bind(this));
                xhr2.open("POST",url);
                var data = new FormData();
                data.append("ids", json);
                data.append("action", action);
                xhr2.send(data);
        }
    };
    
    this.deletionList.deleteWalkrouteCallback = (function(deleted)
    {
        for(var i=0; i<this.deletionList.walkroutes.length; i++)
        {
            var parentLayer = this.deletionList.walkroutes[i][1] ?
                this.walkrouteStartsLayer: this.walkrouteLayer,
                otherLayer = this.deletionList.walkroutes[i][1] ?
                this.walkrouteLayer: this.walkrouteStartsLayer,
                others = this.deletionList.walkroutes[i][1] ?
                this.walkroutes: this.walkrouteStarts;
            var id = this.deletionList.walkroutes[i][0].id;
            var idx = deleted.indexOf(id);
            // if walkroute or walkroute start not deleted by server,
            // add it back
            if (idx==-1)    
            {
                parentLayer.addLayer(this.deletionList.walkroutes[i][0]);

                // needed otherwise the marker will not reappear
                this.drawnItems.addLayer(this.deletionList.walkroutes[i][0]);

            }
            // if walkroute start deleted, remove walkroute itself and
            // vice-versa
            else
            {
                otherLayer.removeLayer(others[id]);

                // remove all walkroute waypoints belonging to this
                // walkroute 
            
                this.walkrouteWaypointsLayer.eachLayer (
                    (function(layer)
                    {
                        if(layer.walkroute==id)
                        {
                            this.walkrouteWaypointsLayer.removeLayer(layer);
                            this.drawnItems.removeLayer(layer);
                        }
                    }).bind(this));
            }
        }
    }).bind(this);


    this.deletionList.deleteWaypointCallback = (function(deleted)
    {
        for(var i=0; i<this.deletionList.waypoints.length; i++)
        {
            var id = this.deletionList.waypoints[i].id;
            var idx = deleted.indexOf(id);
            if(idx==-1)
            {
                this.walkrouteWaypointsLayer.addLayer
                    (this.deletionList.waypoints[i]);
                this.drawnItems.addLayer
                    (this.deletionList.waypoints[i]);
            }
        }    
        this.deletionList.waypoints = [];
    }).bind(this);
  },

  saveAnnotationToServer : function(marker, wrWaypoint)
  {
    var xhr2 = new XMLHttpRequest();
    xhr2.addEventListener 
    ("load", (function(e)
                {
                    if(e.target.status==200)
                    {
                        alert('Moved successfully.');
                    }                        
                    else
                    {
                        if(e.target.status==401)
                            alert('Need to login to move.');
                        else
                            alert('Server error: ' + e.target.status);
                    }
                }).bind(this));
        xhr2.open('POST', '/fm/ws/' + 
                    (wrWaypoint ? 'wr.php' : 'annotation.php'));
        var data = new FormData();
        data.append("action", wrWaypoint ? "moveWaypoint": "move");
        data.append("lat", marker.getLatLng().lat);
        data.append("lon", marker.getLatLng().lng);
        data.append("id", marker.id);
        xhr2.send(data);
  },

  saveLocation : function()
  {
    if(window.localStorage)
    {
        window.localStorage.setItem("lat", this.map.getCenter().lat);
        window.localStorage.setItem("lon", this.map.getCenter().lng);
        window.localStorage.setItem("zoom", this.map.getZoom());
    }
  },

  setLocation : function (x,y)
  {
    this.map.panTo(new L.LatLng(y,x));
    this.saveLocation();
  },

  onAddMarker : function(marker)
  {
    var p = this.map.layerPointToContainerPoint
        (this.map.latLngToLayerPoint(marker.getLatLng()));

    var nearWalkroute=0;

    if(this.loggedIn===true) 
    {
        this.walkrouteLayer.eachLayer (function(layer)
            {
                var latlngs = layer.getLatLngs();
                for(var j=0; j<latlngs.length-1; j++)
                {
                    var dist = 
                        haversineDistToLine(marker.getLatLng(),
                        latlngs[j], latlngs[j+1]);
                    if(dist!=-1 && dist<100)
                    {            
                        nearWalkroute=layer.id;
                        break;
                    }
                }
            });
    

        this.dlg = new Dialog('main',
            { ok: 
                (function()
                {
                  if(document.getElementById("addToWalkroute") &&
                    document.getElementById('addToWalkroute').checked)
                  {
                     var wp = this.addWalkrouteWaypoint
                            (nearWalkroute,marker.getLatLng(),
                                document.getElementById("annotation").value,
                                    (function(wp)
                                    {
                                         this.walkrouteWaypointsLayer.
                                            addLayer(wp);
                                         this.drawnItems.addLayer(wp);
                                         wp.walkroute=nearWalkroute;
                                         this.dlg.hide();
                                    }).bind(this));
            
                  }
                  else
                  {    
                    var xhr2 = new XMLHttpRequest();
                    xhr2.addEventListener ('load', 
                        (function(e)
                        {
                            alert('Annotation added with ID: ' + 
                                e.target.responseText);
                            marker.id = e.target.responseText;
                            this.annotationLoader.indexedFeatures
                                [e.target.responseText] = marker;
                            this.dlg.hide();
                            this.drawnItems.addLayer(marker);
                        }).bind(this));
                    xhr2.open('POST','/fm/ws/annotation.php');
                    var data = new FormData();
                    data.append
                        ("text", document.getElementById('annotation').value);
                    data.append("lat", marker.getLatLng().lat);
                    data.append("lon", marker.getLatLng().lng);
                    data.append("action", "create");
                    xhr2.send(data);
                 }  
                }).bind(this),


                cancel: 
                    (function(e)
                    {
                        this.dlg.hide();
                        e.stopPropagation();
                    }).bind(this)
                },
                { backgroundColor: '#8080ff',
                    width: '400px',
                    height: '300px',
                    color: 'white',
                    borderRadius: '15px' } );

          var content= 
        "<p>Please enter details of the annotation: </p>"+
            "<p><textarea id='annotation' " +
            "style='margin: 10px 10px 10px 10px;width:360px;height:150px'>"+
            "</textarea></p>" ;
        if(nearWalkroute>0)
            content += "<p>"+
               "<input type='checkbox' id='addToWalkroute' checked='checked'/>"+
                "<label for='addToWalkroute'>Add to walk route?</label></p>";
        this.dlg.setContent(content);
        this.dlg.setPosition("100px", "100px");
        if(!this.dlg.isVisible())
        {
            this.dlg.show();
        }
        document.getElementById('annotation').focus();
    }
    else
    {
        alert("Must be logged in to add annotation.");
    }
  },

  doLogin: function()
  {
    var xhr2 = new XMLHttpRequest();
    xhr2.addEventListener ("load", 
            (function(e)
                {
                    if(e.target.status==200)
                        this.loginSuccess(e.target.responseText);
                    else if(e.target.status==401)
                            alert('Incorrect login');
                        else if(e.target.status==503)
                            alert('Please note that you need to re-register '+
                                    'for an account. This is due to the ' +
                                    'login system being upgraded to reflect '+
                                    'current security standards. ' +
                                    '(SHA1 replaced by PHP password library)');
                } ).bind(this));    
    xhr2.open('POST','/fm/user.php');
    var data = new FormData();
    data.append("action", "login");
    data.append("remote", 1);
    data.append("username", document.getElementById('username').value);
    data.append("password", document.getElementById('password').value);
    xhr2.send(data);
  },

  loginSuccess : function(json)
  {
    var userData = JSON.parse(json);
    document.getElementById('logindiv').innerHTML = 
        '<em>Logged in as ' + userData[0] + 
        '</em> <a href="#" id="myroutes">My routes</a> | '+
        '<a href="/fm/user.php?action=logout&redirect=/index.php">Log out</a>';
    document.getElementById('myroutes').addEventListener("click", 
        this.wrViewMgr.sendRequest.bind(this.wrViewMgr));
    this.loggedIn = true;
    this.map.addControl(this.drawControl);
  },

  saveWalkrouteToServer : function(polyline)
  {
    var newRoute = !polyline.id;
    var json = new Object();
    json.type = 'FeatureCollection';
    var route = new Object();
    route.type = 'Feature';
    route.properties = new Object();
    route.properties.isRoute = 'yes';
    route.geometry = new Object();
    route.geometry.type='LineString';
    route.geometry.coordinates = [];
    var latlngs = polyline.getLatLngs();
    var dist = 0.0;
    for(var i=0; i<latlngs.length-1; i++)
    {
        route.geometry.coordinates.push( [ latlngs[i].lng,
                                latlngs[i].lat ] );
       dist += latlngs[i].distanceTo(latlngs[i+1]);
    }
    route.geometry.coordinates.push ( [latlngs[i].lng, latlngs[i].lat] );
    if(newRoute)
    {
        route.properties.title=document.getElementById('_wrmgr_wtitle').value; 
        route.properties.description=document.getElementById('_wrmgr_wdesc').
            value;
        route.properties.distance = dist/1000;  
    }
    else
        this.walkrouteStarts[polyline.id].setLatLng (latlngs[0]);

    json.features = [];
    json.features.push(route);

    var xhr2 = new XMLHttpRequest();
    xhr2.addEventListener ("load", (function(e)
                {
                    if(e.target.status==200) 
                    {
                        if(polyline.id)
                            alert("Successfully edited");
                        else
                        {    
                            alert("Walk route added with id "+
                                e.target.responseText);
                            polyline.id = e.target.responseText;    
                            this.sendDlg.hide();
                            this.walkrouteLayer.addLayer(polyline);
                        }
                    }
                    else if (e.target.status==401 && polyline.id)
                        alert('You can only edit your own walk routes!');
                    else
                        alert("Server error: code=" + e.target.status);
                }).bind(this));
    xhr2.open ('POST', '/fm/ws/wr.php');
    
    var data = new FormData();
    data.append("action", "add");
    data.append("route", JSON.stringify(json));
    if(polyline.id)
    {
        data.append("id", polyline.id);
    }
    xhr2.send(data);
  },

  addAnnotationEditDeleteHandlers: function(marker)
  {
    marker.edit = this.saveAnnotationToServer.bind (this,marker,false);
    marker.addToDelList = function(deletionList)
                    {
                        deletionList.annotations.push(marker.id);
                    };
  },

  addWalkrouteEditDeleteHandlers: function(layer)
  {
    layer.edit = this.saveWalkrouteToServer.bind(this,layer);
    layer.addToDelList = function(deletionList)
                    {
                        deletionList.walkroutes.push([layer,false]);
                    };
  },

  addWalkrouteWaypoint: function (wrId,latlng, text, callback)
  {
    var wp = new Waypoint(latlng,text,'WP',this.saveAnnotationToServer,this);
    var xhr2 = new XMLHttpRequest();
    xhr2.addEventListener ("load", 
                function(e)
                {
                    if(e.target.status==200)
                    {
                        wp.id=e.target.responseText;
                        callback (wp);
                    }
                    else
                        alert('Server error: ' + e.target.status);
                } );
    xhr2.open ("POST", "/fm/ws/wr.php");
    var data = new FormData();
    data.append("id", wrId);
    data.append("data", JSON.stringify(wp.toGeoJSONObj()));
    data.append("action", "addWaypoint");
    xhr2.send(data);
  },
  
  onOrientationChange: function(mql)
  {
     var title=document.getElementById("title");
     if(mql.matches)
     {
        title.innerHTML = "<h1 id='mobheading'>Freemap</h1>";
     }
     else
     {
        title.innerHTML = 
            "<div class='titlebox' id='titlebox'>" + 
        "<img src='fm/images/freemap_small.png' alt='freemap_small' /><br/>"+
        "</div>";
     }
  }
 };

 var parts = window.location.href.split("?");
 var get = {};
 if (parts.length==2)
 {
     var params = parts[1].split("&");

     for(var i=0; i<params.length; i++)
     {
        var param = params[i].split("=");
        get[param[0]] = param[1];
     }
 }
 get.lat = get.lat || null;
 get.lon = get.lon || null;
 get.zoom = get.zoom || null;

 freemap.initialise (get.lat, get.lon, get.zoom, loggedIn);
}

// taken from freemaplib (java) which was in turn taken from osmeditor2
// and in turn from no-longer available source
// astronomy.swin.edu.au/~pbourke/geometry/pointline
// returns: metres
function haversineDistToLine(p, p1, p2)
{
    var u= ((p.lng-p1.lng)*(p2.lng-p1.lng)+(p.lat-p1.lat)*(p2.lat-p1.lat)) /
        (Math.pow(p2.lng-p1.lng,2) + Math.pow(p2.lat-p1.lat,2));
    var lngIntersect = p1.lng+u*(p2.lng-p1.lng),
        latIntersect=p1.lat+u*(p2.lat-p1.lat);
    return (u>=0 && u<=1 ) ?  new L.LatLng
            (latIntersect,lngIntersect).distanceTo(p): -1;
}
